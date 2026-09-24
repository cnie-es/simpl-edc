# Modification notice (EUPL 1.2, Art. 5)

**This is a modified version of SIMPL simpl-edc. It is not the original work.**

The original work, simpl-edc, is part of the SIMPL programme (© European Union / SIMPL Programme)
and is licensed under the **European Union Public Licence v. 1.2 (EUPL-1.2)**. See
[LICENSE](LICENSE), where the full official text of the licence is reproduced, and
[NOTICE](NOTICE) / [NOTICE.json](NOTICE.json) / [CREDITS.pdf](CREDITS.pdf) for the third-party
components included in it. All original copyright, licence and disclaimer notices are kept intact
and unmodified in this fork.

## Upstream baseline

| | |
|---|---|
| Original work | simpl-edc (`eu.europa.ec.simpl:simpledc`) |
| Upstream repository | https://code.europa.eu/simpl/simpl-open/development/gaia-x-edc/simpl-edc |
| Baseline version | `1.0.21` |
| Baseline commit | `4acc3ceec5dc5fd05539eec02ae8c79a27dd7def` (2025-12-17) |

## Modifications

| | |
|---|---|
| Modified by | EDNEL-RIOJA project team, for CNIE-ES |
| Public repository of this derivative work | https://github.com/cnie-es/simpl-edc |
| Version of this derivative work | `1.0.21-edval` |
| Dates of modification | **2025-12-11 to 2026-09-11** |

The modifications are licensed under the **EUPL-1.2**, the same licence as the original work.

The complete source code of this derivative work is available at the public repository above as a
vetted release snapshot, and will remain freely available there for as long as the Work is
distributed. The upstream repository and exact baseline commit are recorded above. Each release
snapshot includes `SBOM.cyclonedx.json`, which binds the upstream and work revisions, release tag,
public repository, snapshot hash and image digest. The distribution history contains release
snapshots rather than a copy of the upstream Git history, so the complete set of changes is the
diff between the baseline commit `4acc3ce`, fetched from its authoritative upstream repository,
and the published snapshot. The tables below record what each file contributed. The published
container images (`ghcr.io/cnie-es/gaia-x-edc-simpl-edc`) are built from that snapshot.

### Summary of the changes

- **Manual payment approval for paid assets**: `PaymentApprovalExtension` and
  `PaymentApprovalApiController` expose the endpoints the provider uses to confirm payment before a
  contract is signed, registered as an EDC service extension, with the price type carried through
  the sign-contract flow. This is the connector side of the same feature added to the contract
  component. Documented in `documents/payment-approval-proxy.md`.
- **S3 provisioning with STS**: a provisioning extension that issues temporary credentials for
  transfers to S3 destinations, comprising `S3StsProvisionExtension`, `S3StsProvisioner`,
  `S3StsResourceDefinition`, `S3StsProvisionedResource` and
  `S3StsConsumerResourceDefinitionGenerator`.
- **Hybrid connector deployment**: `charts/values-hybrid.yaml` and `documents/deployment-ednel.md`,
  letting a single deployment act in both the consumer and the provider role.
- EDC dependencies added to `pom.xml`, including `token-core` and `data-plane-iam`, and a GitHub
  Actions pipeline that builds and publishes the container image.

A second, non-functional group of changes (2026-09-11) adds the notices this licence requires of a
derivative work: this file, the notice at the top of [README.MD](README.MD), the licence and
source-code metadata in `pom.xml` and in the OCI labels of the `Dockerfile`, and the reproduction of
the full official licence text inside [LICENSE](LICENSE), which previously only linked to it. See
[CHANGELOG.md](CHANGELOG.md) for the itemised list.

### Files added

| File | Date |
|---|---|
| `.github/workflows/build-and-push-image.yaml` | 2026-05-27,2026-06-18 2026-08-03, 2026-09-11 |
| `.github/workflows/sync.yaml` | 2026-05-27 |
| `charts/values-hybrid.yaml` | 2026-08-03 |
| `documents/deployment-ednel.md` | 2026-06-15,2026-06-16 |
| `documents/payment-approval-proxy.md` | 2026-06-15 |
| `src/main/java/eu/europa/ec/simpl/contractsign/controller/PaymentApprovalApiController.java` | 2026-06-15 |
| `src/main/java/eu/europa/ec/simpl/contractsign/service/PaymentApprovalExtension.java` | 2026-06-15 |
| `src/main/java/eu/europa/ec/simpl/provision/s3/S3StsConsumerResourceDefinitionGenerator.java` | 2026-06-15 |
| `src/main/java/eu/europa/ec/simpl/provision/s3/S3StsProvisionExtension.java` | 2026-06-15 |
| `src/main/java/eu/europa/ec/simpl/provision/s3/S3StsProvisionedResource.java` | 2026-06-15 |
| `src/main/java/eu/europa/ec/simpl/provision/s3/S3StsProvisioner.java` | 2026-06-15 |
| `src/main/java/eu/europa/ec/simpl/provision/s3/S3StsResourceDefinition.java` | 2026-06-15 |
| `src/test/java/eu/europa/ec/simpl/contractsign/controller/PaymentApprovalApiControllerTest.java` | 2026-06-15 |
| `NOTICE.EDNEL.md` (this file) | 2026-09-11, 2026-09-14 |

### Files modified

| File | Date |
|---|---|
| `.dockerignore` | 2026-09-14 |
| `CHANGELOG.md` | 2026-06-15, 2026-09-11 |
| `Dockerfile` | 2026-09-11, 2026-09-14 |
| `LICENSE` | 2026-09-11 |
| `README.MD` | 2026-09-11, 2026-09-14 |
| `charts/Chart.yaml` | 2026-09-11 |
| `charts/README.MD` | 2026-08-03 |
| `charts/templates/deployment.yaml` | 2026-08-03, 2026-09-11 |
| `charts/values.yaml` | 2026-08-03, 2026-09-11 |
| `documents/deployment-guide.md` | 2026-08-03 |
| `local/consumer-config.properties` | 2026-06-15,2026-08-03 |
| `local/provider-config.properties` | 2026-08-03 |
| `pipeline.variables.sh` | 2025-12-12,2025-12-16 2025-12-17, 2026-09-11 |
| `pom.xml` | 2025-12-16,2025-12-17 2026-05-27,2026-06-11 2026-06-15, 2026-09-11 |
| `src/main/java/eu/europa/ec/simpl/contractsign/listener/ContractAgreementListener.java` | 2025-12-11,2026-06-18 2026-08-03 |
| `src/main/java/eu/europa/ec/simpl/contractsign/service/ContractSignCallbackEndpointExtension.java` | 2026-06-18,2026-08-03 |
| `src/main/resources/META-INF/services/org.eclipse.edc.spi.system.ServiceExtension` | 2025-12-17,2026-06-15 |
| `src/test/java/eu/europa/ec/simpl/contractsign/listener/ContractAgreementListenerTest.java` | 2026-06-18,2026-08-03 |

No file of the original work has been removed, and no copyright, licence or disclaimer notice of
the original work has been altered. The only change to [LICENSE](LICENSE) is the addition, below
the original heading and credits line, of the full official text of the EUPL-1.2 that the file
previously referenced only by hyperlink; nothing in the original file was removed or reworded.

The per-file diff for every change is obtainable with:

```
git diff 4acc3ceec5dc5fd05539eec02ae8c79a27dd7def..ednel
```

For a published image, substitute the release tag it was built from for `ednel`.
