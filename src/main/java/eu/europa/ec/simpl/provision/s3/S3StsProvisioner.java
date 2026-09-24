package eu.europa.ec.simpl.provision.s3;

import java.util.concurrent.CompletableFuture;
import org.eclipse.edc.aws.s3.AwsClientProvider;
import org.eclipse.edc.aws.s3.AwsTemporarySecretToken;
import org.eclipse.edc.aws.s3.S3ClientRequest;
import org.eclipse.edc.connector.controlplane.transfer.spi.provision.Provisioner;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.DeprovisionedResource;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.ProvisionResponse;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.ProvisionedResource;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.ResourceDefinition;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.response.StatusResult;
import software.amazon.awssdk.services.sts.StsAsyncClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.Credentials;

/**
 * Mints short-lived AWS credentials for an {@code AmazonS3} push destination by assuming a
 * pre-existing IAM role via STS. It deliberately does NOT create buckets or IAM roles, and never
 * touches the IAM client (avoiding the NPE in {@code AwsClientProviderImpl.iamAsyncClient} on a null
 * endpoint). The resulting {@link AwsTemporarySecretToken} is handed to the provider, which uses it
 * to upload (multipart for large objects) and lets it expire.
 */
public class S3StsProvisioner implements Provisioner<S3StsResourceDefinition, S3StsProvisionedResource> {

    private final AwsClientProvider clientProvider;
    private final Monitor monitor;
    private final int roleMaxSessionDuration;

    public S3StsProvisioner(AwsClientProvider clientProvider, Monitor monitor, int roleMaxSessionDuration) {
        this.clientProvider = clientProvider;
        this.monitor = monitor;
        this.roleMaxSessionDuration = roleMaxSessionDuration;
    }

    @Override
    public boolean canProvision(ResourceDefinition resourceDefinition) {
        return resourceDefinition instanceof S3StsResourceDefinition;
    }

    @Override
    public boolean canDeprovision(ProvisionedResource resource) {
        return resource instanceof S3StsProvisionedResource;
    }

    @Override
    public CompletableFuture<StatusResult<ProvisionResponse>> provision(S3StsResourceDefinition definition, Policy policy) {
        // endpointOverride is passed through only if the client set it; for real AWS it is null and
        // the STS client uses the default regional endpoint. STS is never the source of the IAM NPE
        // because its cache key is "region/endpointOverride" (never null).
        var clientRequest = S3ClientRequest.from(definition.getRegionId(), definition.getEndpointOverride());
        var sts = clientProvider.stsAsyncClient(clientRequest);

        var request = AssumeRoleRequest.builder()
                .roleArn(definition.getRoleArn())
                .roleSessionName(sessionName(definition.getTransferProcessId()))
                .durationSeconds(roleMaxSessionDuration)
                .externalId(blankToNull(definition.getExternalId()))
                .build();

        monitor.debug("S3StsProvisioner: assuming role %s (region %s) for transfer %s"
                .formatted(definition.getRoleArn(), definition.getRegionId(), definition.getTransferProcessId()));

        return sts.assumeRole(request)
                .thenApply(response -> onSuccess(definition, response.credentials()));
    }

    @Override
    public CompletableFuture<StatusResult<DeprovisionedResource>> deprovision(S3StsProvisionedResource resource, Policy policy) {
        // Nothing to undo: the STS token expires on its own and no AWS resources were created.
        return CompletableFuture.completedFuture(StatusResult.success(
                DeprovisionedResource.Builder.newInstance().provisionedResourceId(resource.getId()).build()));
    }

    private StatusResult<ProvisionResponse> onSuccess(S3StsResourceDefinition definition, Credentials credentials) {
        var keyName = "s3-sts-" + definition.getTransferProcessId();
        var resource = S3StsProvisionedResource.Builder.newInstance()
                .id(definition.getId())
                .resourceDefinitionId(definition.getId())
                .transferProcessId(definition.getTransferProcessId())
                .hasToken(true)
                .resourceName(definition.getBucketName())
                .region(definition.getRegionId())
                .bucketName(definition.getBucketName())
                .objectName(definition.getObjectName())
                .endpointOverride(definition.getEndpointOverride())
                .keyName(keyName)
                .build();

        var token = new AwsTemporarySecretToken(
                credentials.accessKeyId(),
                credentials.secretAccessKey(),
                credentials.sessionToken(),
                credentials.expiration().toEpochMilli());

        monitor.debug("S3StsProvisioner: temporary credentials issued for transfer %s, expiring at %s"
                .formatted(definition.getTransferProcessId(), credentials.expiration()));

        var response = ProvisionResponse.Builder.newInstance().resource(resource).secretToken(token).build();
        return StatusResult.success(response);
    }

    private static String sessionName(String transferProcessId) {
        var name = "edc-" + transferProcessId;
        return name.length() > 64 ? name.substring(0, 64) : name;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
