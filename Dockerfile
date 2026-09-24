FROM eclipse-temurin:21-jdk-alpine@sha256:cd87715a8d45cfaa42419207c64680234f62785c49055cccd20437b5c9018380

# EUPL-1.2 (Art. 5): this image ships a modified version of SIMPL simpl-edc. The licence, the
# third-party notices and the modification notice travel with the image, and the labels below point
# to the repository where the complete corresponding source code is available.
LABEL org.opencontainers.image.title="simpl-edc (CNIE-ES fork)" \
      org.opencontainers.image.description="Modified version of SIMPL simpl-edc v1.0.21 (upstream commit 4acc3ce), modified by the EDNEL-RIOJA project team for CNIE-ES between 2025-12-11 and 2026-09-11. See /licenses/NOTICE.EDNEL.md." \
      org.opencontainers.image.version="1.0.21-edval" \
      org.opencontainers.image.vendor="CNIE-ES" \
      org.opencontainers.image.licenses="EUPL-1.2" \
      org.opencontainers.image.source="https://github.com/cnie-es/simpl-edc"

# The notices must travel with every copy of the Work (EUPL-1.2, Art. 5).
COPY LICENSE NOTICE NOTICE.json CREDITS.pdf NOTICE.EDNEL.md /licenses/

# The release/build.sh hook produces the artifact; the image only copies it.
COPY target/basic-connector.jar connector.jar
COPY otel/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar

ENV JAVA_TOOL_OPTIONS="-javaagent:/otel/opentelemetry-javaagent.jar"

RUN mkdir /files && chmod 775 /files
RUN adduser -u 8877 -D dockerUser
RUN chown -R dockerUser:dockerUser /files

USER dockerUser

ENTRYPOINT ["java", "-javaagent:/otel/opentelemetry-javaagent.jar", "-jar", "/connector.jar"]
