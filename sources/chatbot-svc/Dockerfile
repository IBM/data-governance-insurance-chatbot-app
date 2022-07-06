# Package the application as a war file
FROM maven:3.8.1-ibmjava-8-alpine AS builder
LABEL maintainer="IBM Java Engineering at IBM Cloud"
WORKDIR /
COPY pom.xml ./
COPY src src/
RUN mvn clean package

# Copy the war file over to the open liberty image
FROM icr.io/appcafe/open-liberty:full-java11-openj9-ubi

COPY --from=builder --chown=1001:0 src/main/liberty/config/ /config/
COPY --from=builder --chown=1001:0 target/*.war /config/apps/

ENV PORT 9080

EXPOSE 9080

RUN configure.sh
