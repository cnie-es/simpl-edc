## 1.0.21-edval (2026-09-11)

> Derivative work by the **EDNEL-RIOJA** project team for **CNIE-ES**, based on the upstream
> simpl-edc `1.0.21` line (commit `c9368a6`). Modified between **2025-12-11 and 2026-09-11**,
> licensed under EUPL-1.2 like the original work. See [NOTICE.EDNEL.md](NOTICE.EDNEL.md) for the
> full modification notice.

### Added (2025-12-11 → 2026-08-03)

- **Manual payment approval for paid assets**: `PaymentApprovalExtension` and
  `PaymentApprovalApiController` expose the endpoints the provider uses to confirm payment before
  a contract is signed, registered as an EDC service extension. The price type is carried in the
  sign-contract flow. Documented in `documents/payment-approval-proxy.md`.
- **S3 provisioning with STS**: `S3StsProvisionExtension`, `S3StsProvisioner`,
  `S3StsResourceDefinition`, `S3StsProvisionedResource` and
  `S3StsConsumerResourceDefinitionGenerator`, so that transfers to S3 destinations use temporary
  credentials.
- **Hybrid connector deployment**: `charts/values-hybrid.yaml` and `documents/deployment-ednel.md`,
  letting one deployment act in both roles.
- GitHub Actions pipelines for building and publishing the container image and for syncing from
  upstream.

### Changed (2025-12-11 → 2026-08-03)

- `pom.xml`: EDC dependencies added, including `token-core` and `data-plane-iam`.
- `ContractAgreementListener` and `ContractSignCallbackEndpointExtension` extended for the payment
  approval flow, with tests.
- Local consumer and provider configuration updated, and the Helm chart restructured so that
  `values.yaml` is a base completed by `values-hybrid.yaml`.

### Licence compliance (2026-09-11)

Notices required by Art. 5 of the EUPL-1.2 (Attribution right, Provision of Source Code) for this
derivative work:

- `NOTICE.EDNEL.md`: modification notice stating that the work has been modified, by whom, when and
  what was changed, with the repository where the complete corresponding source code is available.
- `README.MD`: prominent notice at the top of the file identifying this repository as a modified
  version of simpl-edc, plus a Licence section.
- `LICENSE`: the full official text of the EUPL-1.2 is now reproduced in the file, which previously
  only linked to it, so that a copy of the Licence travels with every copy of the Work. The
  original SIMPL heading and credits line are kept intact.
- `Dockerfile`: OCI image labels (`licenses`, `source`, `vendor`, `description`) and `LICENSE`,
  `NOTICE`, `NOTICE.json`, `CREDITS.pdf` and `NOTICE.EDNEL.md` copied into `/licenses/`, so the
  notices and the pointer to the source code travel with the published container image.
- `pom.xml`: the project coordinates move from `eu.europa.ec.simpl:simpledc` to
  `es.cnie.simpl:simpl-edc`, so that a modified artifact is not identified under a namespace
  belonging to the licensor (Art. 5, Legal Protection); `licenses`, `scm`, `url` and `developers`
  metadata filled in. The version is pinned to `1.0.21-edval`: it carried
  `${env.PROJECT_RELEASE_VERSION}`, a variable of the upstream GitLab pipeline that nothing in the
  GitHub workflow or the `Dockerfile` sets, so the placeholder reached the build unresolved.
- `.github/workflows/build-and-push-image.yaml`: the container image namespace is derived from the
  repository owner instead of being hard-coded, the project version is published as an image tag
  alongside the commit SHA so the tag the chart resolves to by default exists, and the build fails
  if the POM version and the chart `appVersion` ever drift apart.
- Helm chart made loadable outside the upstream GitLab pipeline: `Chart.yaml` and `values.yaml`
  carried unsubstituted `${PROJECT_RELEASE_VERSION}` and `${CI_REGISTRY_IMAGE}` placeholders, which
  that pipeline replaced and which made the chart fail to load anywhere else. `deployment.image`
  now defaults to the published image of this fork, `deployment.tag` falls back to the chart
  `appVersion`, and optional `deployment.pullSecrets` are added, as the published image is private.
  The chart still expects an additional values file, as the hybrid connector work left it.
- `pipeline.variables.sh`: `PROJECT_VERSION_NUMBER` aligned with the fork version.


## Unreleased

### added (1 change)

- Payment Approval Proxy Extension: new `PaymentApprovalExtension` and `PaymentApprovalApiController` expose `GET /payment/pending`, `POST /payment/{id}/confirm`, and `POST /payment/{id}/reject` on the EDC management API, proxying the contract service's `PENDING_PAYMENT` endpoints so the provider UI does not need a direct route to the contract service.

## 1.0.13 (2025-09-05)

### changed (1 change)

- [[SIMPL-6472] (https://jira.simplprogramme.eu/browse/SIMPL-6472) PSO | Code verification: CORS configuration issue in ingress file](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/commit/de3c63254dcf70b08a05831d387d947550901fb4) ([merge request](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/merge_requests/96))

## 1.0.12 (2025-09-05)

### changed (1 change)

- [[SIMPL-14812] (https://jira.simplprogramme.eu/browse/SIMPL-14812) fix sonar issues](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/commit/8732d10e3dd11cb63ee0c0bc8de8ae4a03632a6c) ([merge request](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/merge_requests/86))



## 1.0.11 (2025-08-11)

### changed (1 change)

- [[SIMPL-6472] (https://jira.simplprogramme.eu/browse/SIMPL-6472) resolved conflicts](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/commit/a477df309fa0f9194438ecbbe3cdd2d68dce57e6) ([merge request](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/merge_requests/73))

## 1.0.10 (2025-08-05)

### changed (3 changes)

- [[SIMPL-16096] (https://jira.simplprogramme.eu/browse/SIMPL-16096) resolved conflicts](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/commit/3c5bed20f9e53360e938988f964928908f717f66) ([merge request](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/merge_requests/84))
- [[SIMPL-16096](https://jira.simplprogramme.eu/browse/SIMPL-16096) moved cluster...](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/commit/52d9533c7023721fc2afc6548b03ce31e9869024) ([merge request](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/merge_requests/84))
- [[SIMPL-16125](https://jira.simplprogramme.eu/browse/SIMPL-16125) Rework the...](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/commit/aabc3d279129c11ef8fb1ea9362acd8f92b6aa51) ([merge request](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/merge_requests/83))


## 1.0.9 (2025-08-01)

### changed (1 change)

- [[SIMPL-5947](https://jira.simplprogramme.eu/browse/SIMPL-5947) updated protobuf-java to 4.31.1](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/commit/c354b4c9dc2bd1a2872ffcc77bae82972992a2ed) ([merge request](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/merge_requests/80))


## 1.0.8 (2025-07-11)

No changes.


## 1.0.7 (2025-07-04)

### changed (5 changes)

- [[SIMPL-14638](https://jira.simplprogramme.eu/browse/SIMPL-14638) changed auth provider url](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/commit/ba749ff963cf04ab9b0d7853f61ca470a15b8d2b) ([merge request](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/merge_requests/76))
- [[SIMPL-14638](https://jira.simplprogramme.eu/browse/SIMPL-14638) solved sonar issues](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/commit/ecbb760b27614d4414945c45bfe41cd94f54366a) ([merge request](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/merge_requests/75))
- [[SIMPL-14638](https://jira.simplprogramme.eu/browse/SIMPL-14638) removed creds](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/commit/22c7b6db2be93c6c4645d6c7f42f4f5e92a5b04d) ([merge request](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/merge_requests/75))
- [[SIMPL-14638](https://jira.simplprogramme.eu/browse/SIMPL-14638) updated...](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/commit/a358ba3993c308584b9b5b5357355f03fd053fb5) ([merge request](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/merge_requests/75))
- [[SIMPL-14638](https://jira.simplprogramme.eu/browse/SIMPL-14638) updated connector-core to 1.1.5](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/commit/f548b154b2fd4f6db4b71546dac9b5b40085b857) ([merge request](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/merge_requests/75))

### added (1 change)

- [[SIMPL-14638](https://jira.simplprogramme.eu/browse/SIMPL-14638) added logger](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/commit/d3f09c63e197885368e0dc5d78030c04cd16c2d9) ([merge request](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/merge_requests/76))


## 1.0.6 (2025-05-30)

### changed (2 changes)

- [[SIMPL-6470](https://jira.simplprogramme.eu/browse/SIMPL-6470) removed image...](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/commit/73b09a9a8eaf4b68fd207936d581b897196bfceb) ([merge request](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/merge_requests/72))
- [[SIMPL-12190](https://jira.simplprogramme.eu/browse/SIMPL-12190) Implement changelog for simpl-edc](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/commit/89f749b0f615caf11e515ba1ceb459d67eca4ac9) ([merge request](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/merge_requests/70))

### added (1 change)

- [[SIMPL-12095](https://jira.simplprogramme.eu/browse/SIMPL-12095). Add opentelemetry to simpl-edc](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/commit/6cf8a6f4b589aa196a914342b2e07800fb94e789) ([merge request](https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc/-/merge_requests/71))


