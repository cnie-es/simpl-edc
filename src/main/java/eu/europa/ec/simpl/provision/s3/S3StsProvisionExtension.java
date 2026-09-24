package eu.europa.ec.simpl.provision.s3;

import org.eclipse.edc.aws.s3.AwsClientProvider;
import org.eclipse.edc.aws.s3.AwsTemporarySecretToken;
import org.eclipse.edc.connector.controlplane.transfer.spi.provision.ProvisionManager;
import org.eclipse.edc.connector.controlplane.transfer.spi.provision.ResourceManifestGenerator;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;

/**
 * Registers a custom AWS S3 provisioner that, on transfer start, assumes a pre-existing IAM role via
 * STS and hands the resulting temporary credentials to the provider for the push. Replaces sending
 * static AWS keys, and avoids the upstream provision-aws-s3 limitations on real AWS.
 */
@Extension(value = S3StsProvisionExtension.NAME)
public class S3StsProvisionExtension implements ServiceExtension {

    public static final String NAME = "AWS S3 STS Provision";

    @Setting(value = "ARN of the pre-existing IAM role assumed via STS to mint the temporary push credentials. "
            + "Can be overridden per transfer with the 'roleArn' dataDestination property.")
    private static final String ROLE_ARN = "edc.aws.provision.sts.role.arn";

    @Setting(value = "Optional STS externalId, if the assumed role's trust policy requires one. "
            + "Can be overridden per transfer with the 'externalId' dataDestination property.")
    private static final String EXTERNAL_ID = "edc.aws.provision.sts.external.id";

    @Setting(value = "Requested session duration (seconds) of the temporary STS credentials. Must be <= the "
            + "role's MaxSessionDuration and should exceed the longest expected transfer.", defaultValue = "3600")
    private static final String ROLE_DURATION = "edc.aws.provision.role.duration.session.max";

    @Inject
    private AwsClientProvider clientProvider;

    @Inject
    private TypeManager typeManager;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        var roleArn = context.getSetting(ROLE_ARN, null);
        var externalId = context.getSetting(EXTERNAL_ID, null);
        var roleMaxSessionDuration = context.getSetting(ROLE_DURATION, 3600);

        if (roleArn == null || roleArn.isBlank()) {
            monitor.warning("%s: '%s' is not set; AmazonS3 transfers must carry a 'roleArn' dataDestination "
                    .formatted(NAME, ROLE_ARN) + "property or provisioning will fail.");
        }

        var provisionManager = context.getService(ProvisionManager.class);
        provisionManager.register(new S3StsProvisioner(clientProvider, monitor, roleMaxSessionDuration));

        var manifestGenerator = context.getService(ResourceManifestGenerator.class);
        manifestGenerator.registerGenerator(new S3StsConsumerResourceDefinitionGenerator(roleArn, externalId));

        typeManager.registerTypes(S3StsProvisionedResource.class, S3StsResourceDefinition.class, AwsTemporarySecretToken.class);

        monitor.info("%s initialized (role=%s, sessionDuration=%ds)".formatted(NAME, roleArn, roleMaxSessionDuration));
    }
}
