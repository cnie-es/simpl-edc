package eu.europa.ec.simpl.contractsign.service;

import eu.europa.ec.simpl.contractsign.controller.PaymentApprovalApiController;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;

/**
 * EDC extension that exposes payment-approval proxy endpoints on the management API so the UI
 * can list, confirm, and reject {@code PENDING_PAYMENT} contract agreements without requiring
 * direct access to the contract service.
 *
 * <p>Only active when {@code contractmanager.extension.enabled=true}. Reuses the same
 * {@code contractmanager.url} and {@code contractmanager.apikey} settings as
 * {@link ContractSignCallbackEndpointExtension}.</p>
 */
@Extension(PaymentApprovalExtension.EXTENSION_NAME)
public class PaymentApprovalExtension implements ServiceExtension {

    public static final String EXTENSION_NAME = "SIMPL Payment Approval Proxy Extension";

    @Setting
    static final String CONTRACT_MANAGER_EXTENSION_ENABLED = "contractmanager.extension.enabled";
    @Setting
    static final String CONTRACT_MANAGER_URL = "contractmanager.url";
    @Setting
    static final String CONTRACT_MANAGER_APIKEY = "contractmanager.apikey";

    @Inject
    WebService webService;

    @Inject
    EdcHttpClient httpClient;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        var enabled = context.getSetting(CONTRACT_MANAGER_EXTENSION_ENABLED, "false");
        monitor.info(EXTENSION_NAME + " enabled: " + enabled);

        if (!Boolean.parseBoolean(enabled)) {
            return;
        }

        var url = context.getConfig().getString(CONTRACT_MANAGER_URL);
        var apiKey = context.getConfig().getString(CONTRACT_MANAGER_APIKEY);
        webService.registerResource("management", new PaymentApprovalApiController(monitor, httpClient, url, apiKey));
        monitor.info("PaymentApprovalApiController registered at /management/v3/payment");
    }
}
