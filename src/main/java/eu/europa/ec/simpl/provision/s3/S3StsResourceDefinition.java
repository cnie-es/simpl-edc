package eu.europa.ec.simpl.provision.s3;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.Objects;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.ResourceDefinition;

/**
 * Resource definition for an {@code AmazonS3} push destination that is secured with temporary STS
 * credentials. Unlike the upstream {@code provision-aws-s3} module this never creates buckets or IAM
 * roles: it only assumes a pre-existing role ({@link #getRoleArn()}) to mint a short-lived token.
 */
@JsonDeserialize(builder = S3StsResourceDefinition.Builder.class)
public class S3StsResourceDefinition extends ResourceDefinition {

    private String regionId;
    private String bucketName;
    private String objectName;
    private String endpointOverride;
    private String roleArn;
    private String externalId;

    private S3StsResourceDefinition() {
    }

    public String getRegionId() {
        return regionId;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getEndpointOverride() {
        return endpointOverride;
    }

    public String getRoleArn() {
        return roleArn;
    }

    public String getExternalId() {
        return externalId;
    }

    @Override
    public Builder toBuilder() {
        return initializeBuilder(new Builder())
                .regionId(regionId)
                .bucketName(bucketName)
                .objectName(objectName)
                .endpointOverride(endpointOverride)
                .roleArn(roleArn)
                .externalId(externalId);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends ResourceDefinition.Builder<S3StsResourceDefinition, Builder> {

        private Builder() {
            super(new S3StsResourceDefinition());
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder regionId(String regionId) {
            resourceDefinition.regionId = regionId;
            return this;
        }

        public Builder bucketName(String bucketName) {
            resourceDefinition.bucketName = bucketName;
            return this;
        }

        public Builder objectName(String objectName) {
            resourceDefinition.objectName = objectName;
            return this;
        }

        public Builder endpointOverride(String endpointOverride) {
            resourceDefinition.endpointOverride = endpointOverride;
            return this;
        }

        public Builder roleArn(String roleArn) {
            resourceDefinition.roleArn = roleArn;
            return this;
        }

        public Builder externalId(String externalId) {
            resourceDefinition.externalId = externalId;
            return this;
        }

        @Override
        protected void verify() {
            super.verify();
            Objects.requireNonNull(resourceDefinition.bucketName, "bucketName");
            Objects.requireNonNull(resourceDefinition.regionId, "regionId");
            Objects.requireNonNull(resourceDefinition.roleArn,
                    "roleArn (set edc.aws.provision.sts.role.arn or the 'roleArn' dataDestination property)");
        }
    }
}
