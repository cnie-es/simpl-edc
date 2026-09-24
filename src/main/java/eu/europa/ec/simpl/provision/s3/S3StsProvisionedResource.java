package eu.europa.ec.simpl.provision.s3;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.ProvisionedDataDestinationResource;

/**
 * Provisioned {@code AmazonS3} destination. Its {@link #getDataAddress()} carries the bucket/region/
 * object plus a {@code keyName} under which the temporary {@code AwsTemporarySecretToken} is stored
 * in the vault. The provider's S3 data-plane sink reads that token to perform the push.
 */
@JsonDeserialize(builder = S3StsProvisionedResource.Builder.class)
@JsonTypeName("dataspaceconnector:s3stsprovisionedresource")
public class S3StsProvisionedResource extends ProvisionedDataDestinationResource {

    private S3StsProvisionedResource() {
    }

    public String getRegion() {
        return getDataAddress().getStringProperty("region");
    }

    public String getBucketName() {
        return getDataAddress().getStringProperty("bucketName");
    }

    public String getObjectName() {
        return getDataAddress().getStringProperty("objectName");
    }

    public String getEndpointOverride() {
        return getDataAddress().getStringProperty("endpointOverride");
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends ProvisionedDataDestinationResource.Builder<S3StsProvisionedResource, Builder> {

        private Builder() {
            super(new S3StsProvisionedResource());
            dataAddressBuilder.type("AmazonS3");
        }

        @JsonCreator
        public static Builder newInstance() {
            return new Builder();
        }

        public Builder region(String region) {
            dataAddressBuilder.property("region", region);
            return this;
        }

        public Builder bucketName(String bucketName) {
            dataAddressBuilder.property("bucketName", bucketName);
            return this;
        }

        public Builder objectName(String objectName) {
            if (objectName != null) {
                dataAddressBuilder.property("objectName", objectName);
            }
            return this;
        }

        public Builder endpointOverride(String endpointOverride) {
            if (endpointOverride != null) {
                dataAddressBuilder.property("endpointOverride", endpointOverride);
            }
            return this;
        }

        public Builder keyName(String keyName) {
            dataAddressBuilder.keyName(keyName);
            return this;
        }
    }
}
