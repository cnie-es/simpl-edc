package eu.europa.ec.simpl.provision.s3;

import java.util.Optional;
import java.util.UUID;
import org.eclipse.edc.connector.controlplane.transfer.spi.provision.ConsumerResourceDefinitionGenerator;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.ResourceDefinition;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcess;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.types.domain.DataAddress;

/**
 * Triggers on {@code AmazonS3} push destinations and produces an {@link S3StsResourceDefinition}.
 * The role to assume comes from the {@code roleArn} property of the destination, falling back to the
 * connector-wide default ({@code edc.aws.provision.sts.role.arn}). Same for {@code externalId}.
 */
public class S3StsConsumerResourceDefinitionGenerator implements ConsumerResourceDefinitionGenerator {

    private final String defaultRoleArn;
    private final String defaultExternalId;

    public S3StsConsumerResourceDefinitionGenerator(String defaultRoleArn, String defaultExternalId) {
        this.defaultRoleArn = defaultRoleArn;
        this.defaultExternalId = defaultExternalId;
    }

    @Override
    public ResourceDefinition generate(TransferProcess transferProcess, Policy policy) {
        DataAddress destination = transferProcess.getDataDestination();

        // region is client-selected per transfer (the "region" property of the dataDestination).
        // It is mandatory: Builder.verify() rejects a missing region rather than guessing one.
        var region = destination.getStringProperty("region");
        var roleArn = Optional.ofNullable(destination.getStringProperty("roleArn"))
                .filter(it -> !it.isBlank())
                .orElse(defaultRoleArn);
        var externalId = Optional.ofNullable(destination.getStringProperty("externalId"))
                .filter(it -> !it.isBlank())
                .orElse(defaultExternalId);

        return S3StsResourceDefinition.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .regionId(region)
                .bucketName(destination.getStringProperty("bucketName"))
                .objectName(destination.getStringProperty("objectName"))
                .endpointOverride(destination.getStringProperty("endpointOverride"))
                .roleArn(roleArn)
                .externalId(externalId)
                .build();
    }

    @Override
    public boolean canGenerate(TransferProcess transferProcess, Policy policy) {
        return "AmazonS3".equals(transferProcess.getDestinationType());
    }
}
