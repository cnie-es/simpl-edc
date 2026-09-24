# EDNEL — AWS S3 push with temporary STS credentials (custom provisioner)

## Goal

Stop sending the consumer's long-lived AWS `accessKeyId`/`secretAccessKey` to the provider.
Instead, the **consumer connector** holds the long-lived credentials (ambiently, via its EKS/IRSA
identity) and, **when a transfer starts**, assumes a pre-existing IAM role via STS to mint a
short-lived `AwsTemporarySecretToken` (`accessKeyId` + `secretAccessKey` + `sessionToken` +
`expiration`). Only that temporary token reaches the provider, which uses it to push the object
(multipart for large files) and lets it expire automatically.

## Why a custom provisioner instead of `provision-aws-s3`

The upstream `provision-aws-s3` module is unusable here on real AWS:

- It needs a **single endpoint** serving S3 + IAM + STS (LocalStack-style). On real AWS those are
  different endpoints, so a single `endpointOverride` misroutes IAM/STS.
- Without an `endpointOverride` its `AwsClientProviderImpl.iamAsyncClient` **NPEs** (the IAM client
  cache key is the null endpoint).
- It **creates a bucket and an IAM role per transfer** (`s3:CreateBucket`, `iam:CreateRole`,
  `iam:PutRolePolicy`), which our EKS node role neither has nor should have.

Our provisioner (`eu.europa.ec.simpl.provision.s3`) instead **only** calls `sts:AssumeRole` on a
pre-existing role, with default per-service AWS endpoints. No bucket/role creation, no IAM client.

## How it works

```
                CONSUMER                                         PROVIDER
   ambient identity (EKS node role / IRSA / env vars)
                 │
   transfer start → S3StsProvisioner:
        sts:AssumeRole(role = edc.aws.provision.sts.role.arn)
        → AwsTemporarySecretToken{accessKey, secretKey, sessionToken, expiration}
        stored in the vault under keyName "s3-sts-<transferProcessId>"
                 │
        temporary token ────────────────────────────────────►  data-plane-aws-s3 sink
                                                                 multipart PutObject,
                                                                 token expires.
```

The provider needs **no AWS credentials** and **no code change**: the `data-plane-aws-s3` sink
already resolves the temporary token from the vault by `keyName` and uses it.

## Components added (consumer)

| File | Role |
|------|------|
| `S3StsResourceDefinition` | resource definition (region, bucket, object, endpointOverride, roleArn, externalId) |
| `S3StsConsumerResourceDefinitionGenerator` | triggers on destination type `AmazonS3` |
| `S3StsProvisioner` | `sts:AssumeRole` → `AwsTemporarySecretToken` (no bucket/role creation) |
| `S3StsProvisionedResource` | provisioned `AmazonS3` data address with the token `keyName` |
| `S3StsProvisionExtension` | registers the generator + provisioner (auto-loaded via `META-INF/services`) |

`pom.xml`: the whole AWS S3 stack is pinned to `${awsS3}` (= `0.10.1`, aligned with EDC core
`0.10.1`); `aws-s3-core` is a compile dependency (the provisioner needs `AwsClientProvider`,
`AwsTemporarySecretToken`, `S3ClientRequest` and, transitively, the AWS SDK STS client). The upstream
`provision-aws-s3` module is intentionally NOT included.

> Version note: the data-plane S3 sink at `0.10.1` already does multipart uploads, so large files are
> supported without the `0.15.1` "BulkS3" line (which would clash with EDC core `0.10.1`).

## Configuration (consumer)

| Setting | Default | Notes |
|---|---|---|
| `edc.aws.provision.sts.role.arn` | – | Pre-existing role to assume. Per-transfer override: `roleArn` destination property. Required (unless every transfer sets `roleArn`). |
| `edc.aws.provision.sts.external.id` | – | Optional; only if the role's trust policy requires it. Per-transfer: `externalId`. |
| `edc.aws.provision.role.duration.session.max` | `3600` | Requested STS session seconds. Must be ≤ the role's MaxSessionDuration and cover the longest transfer. |

Credentials come from the AWS `DefaultCredentialsProvider` (EKS node role / IRSA / env vars). Do NOT
set `edc.aws.access.key` / `edc.aws.secret.access.key` (those are vault key names, not secrets).

## AWS setup (one-time)

1. A role, e.g. `ednel-s3-push`, with permission policy:
   ```json
   { "Version": "2012-10-17", "Statement": [
     { "Effect": "Allow", "Action": "s3:PutObject", "Resource": "arn:aws:s3:::<consumer-bucket>/*" }
   ]}
   ```
   (add `s3:AbortMultipartUpload` / `s3:ListBucketMultipartUploads` for large multipart uploads).
2. Trust policy allowing the connector's identity (EKS node role or IRSA role) to assume it.
3. The connector's identity needs only `sts:AssumeRole` on that role.
4. Set the role's **MaxSessionDuration** ≥ `edc.aws.provision.role.duration.session.max`.

## Connector AWS identity (Vault injection)

The connector authenticates the STS call with the AWS SDK `DefaultCredentialsProvider`, whose
resolution order is: system properties → **environment variables** → IRSA (web identity) →
`~/.aws/credentials` → container credentials → **EC2/EKS instance profile (node role)**. Environment
variables therefore take precedence over the EKS node role (and over IRSA).

To use a dedicated IAM identity instead of the node role, inject its keys as environment variables
from Vault. **No chart change is needed**: the Vault Agent template in
`charts/templates/deployment.yaml` already turns every key of the secret into `export KEY=value`
(`{{ range $k, $v := .Data.data }} export {{ $k }}={{ $v }}`), and the container does
`source /vault/secrets/config.txt && java -jar /connector.jar`.

Add the AWS keys to the existing secret (path `kv/gaia-x/<namespace>-simpl-edc`). Use
`kv patch`, not `kv put` — `put` would wipe the other keys (`fr_gxfs_s3_*`, etc.):

```bash
vault kv patch kv/gaia-x/<namespace>-simpl-edc \
    AWS_ACCESS_KEY_ID="AKIA..." \
    AWS_SECRET_ACCESS_KEY="..." \
    AWS_REGION="eu-west-1"
```

Notes:
- Key names must be the **exact uppercase** names the AWS SDK reads (`AWS_ACCESS_KEY_ID`,
  `AWS_SECRET_ACCESS_KEY`, optional `AWS_REGION` / `AWS_SESSION_TOKEN`). Vault keys are case-sensitive.
- The template `export`s values **unquoted**. Standard AWS keys (access key `[A-Z0-9]`, secret
  `[A-Za-z0-9/+]`) are safe; a value with spaces or shell metacharacters (`$`, quotes, backtick)
  would break `source`.
- This identity needs `sts:AssumeRole` on the role above, and the role's trust policy must allow
  **this identity** (not the node role) to assume it.
- Do NOT use `edc.aws.access.key` / `edc.aws.secret.access.key` for this — those are vault key
  *names* for a different EDC mechanism, not the literal credentials.

## Example transfer request (no static keys)

```json
{
  "@context": { "@vocab": "https://w3id.org/edc/v0.0.1/ns/" },
  "transferType": "AmazonS3-PUSH",
  "dataDestination": {
    "type": "AmazonS3",
    "region": "eu-west-1",
    "bucketName": "ednel.devhiberus",
    "objectName": "example-s3-javi.txt"
  }
}
```

- `region` is mandatory and client-selected (each consumer picks the region of its bucket).
- `roleArn` / `externalId` may be set here to override the connector defaults.
- Do **not** set `endpointOverride` for real AWS (S3/STS use their default endpoints).

## Validate at runtime

1. Connector boots; log line `AWS S3 STS Provision initialized (role=…, sessionDuration=…s)`.
2. On transfer start: `S3StsProvisioner: assuming role …` then `temporary credentials issued …`.
3. The provider's sink uploads with the temporary token; the object appears in the bucket.
4. Tune `edc.aws.provision.role.duration.session.max` so the token outlives large transfers.
