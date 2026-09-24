package eu.europa.ec.simpl.contractsign.service;

import eu.europa.ec.simpl.contractsign.listener.ContractAgreementListener;
import eu.europa.ec.simpl.contractsign.listener.SignContractNegotiationPendingGuard;
import eu.europa.ec.simpl.contractsign.controller.ContractSignApiController;
import eu.europa.ec.simpl.contractsign.event.ContractSignedEvent;
import eu.europa.ec.simpl.contractsign.controller.HealthApiController;
import org.eclipse.edc.connector.controlplane.asset.spi.index.AssetIndex;
import org.eclipse.edc.connector.controlplane.contract.spi.negotiation.ContractNegotiationPendingGuard;
import org.eclipse.edc.connector.controlplane.contract.spi.negotiation.observe.ContractNegotiationListener;
import org.eclipse.edc.connector.controlplane.contract.spi.negotiation.observe.ContractNegotiationObservable;
import org.eclipse.edc.connector.controlplane.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.controlplane.services.spi.contractnegotiation.ContractNegotiationService;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.event.EventRouter;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;

import java.time.Clock;

/**
 * Extension for EDC to interact with an external Contract Manager to sign or reject a contract negotiation explicitly.
 *
 */
@Extension(ContractSignCallbackEndpointExtension.EXTENSION_NAME)
public class ContractSignCallbackEndpointExtension implements ServiceExtension {

    public static final String EXTENSION_NAME = "SIMPL Contract Sign Callback Extension";
    @Setting
    static final String CONTRACT_MANAGER_URL = "contractmanager.url";
    @Setting
    static final String CONTRACT_MANAGER_CONSUMER_URL = "contractmanager.consumer.url";
    @Setting
    static final String CONTRACT_MANAGER_PROVIDER_URL = "contractmanager.provider.url";
    @Setting
    static final String CONTRACT_MANAGER_APIKEY = "contractmanager.apikey";
    @Setting(required = true)
    static final String CONTRACT_MANAGER_EXTENSION_ENABLED = "contractmanager.extension.enabled";
    /**
     * the webservice to use.
     */
    @Inject
    WebService webService;

    /**
     * the contract negotiation service to use.
     */
    @Inject
    ContractNegotiationService service;

    /**
     * the http client to use.
     */
    @Inject
    EdcHttpClient httpClient;

    @Inject
    ContractNegotiationStore negotiationStore;

    @Inject
    ContractNegotiationPendingGuard pendingGuard;

    @Inject
    ContractNegotiationObservable contractNegotiationObservable;

    @Inject
    AssetIndex assetIndex;

    @Inject
    private Clock clock;

    @Inject
    private EventRouter eventRouter;

    /**
     * Initialize the service extension.
     * 
     * @param context the context of interest.
     */
    @Override
    public void initialize(ServiceExtensionContext context) {
        var extensionEnabled = context.getSetting(CONTRACT_MANAGER_EXTENSION_ENABLED, "false");
        var monitor = context.getMonitor();
        monitor.info("SIMPL Contract Sign Callback Extension enabled: " + extensionEnabled);
        if (Boolean.parseBoolean(extensionEnabled)) {
            var apikey = context.getConfig().getString(CONTRACT_MANAGER_APIKEY);
            monitor.info("Registering Contract API KEY");
            // Route the outgoing sign/status callbacks to a per-role Contract Manager instance based on the
            // negotiation type. Both resolve to the legacy single url when the role-specific settings are
            // absent, so existing single-instance deployments keep working unchanged.
            var legacyUrl = context.getConfig().getString(CONTRACT_MANAGER_URL, null);
            var consumerUrl = context.getConfig().getString(CONTRACT_MANAGER_CONSUMER_URL, legacyUrl);
            var providerUrl = context.getConfig().getString(CONTRACT_MANAGER_PROVIDER_URL, legacyUrl);
            monitor.info("Contract manager consumer url: " + consumerUrl + " | provider url: " + providerUrl);

            webService.registerResource(new HealthApiController(monitor));
            webService.registerResource(new ContractSignApiController(monitor, service, negotiationStore, eventRouter, clock));
            contractNegotiationObservable.registerListener(new ContractAgreementListener(monitor, consumerUrl, providerUrl, apikey, httpClient, assetIndex));
            contractNegotiationObservable.registerListener((ContractNegotiationListener) pendingGuard);
            eventRouter.register(ContractSignedEvent.class, (SignContractNegotiationPendingGuard) pendingGuard);
        }
    }
}
