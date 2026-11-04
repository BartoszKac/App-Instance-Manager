FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build/App-Instance-Manager
COPY proto /build/proto
COPY App-Instance-Manager/pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -B dependency:go-offline
COPY App-Instance-Manager/src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
RUN apt-get update \
 && apt-get install -y --no-install-recommends python3 g++ curl nmap libcap2-bin \
 && setcap cap_net_raw+eip /usr/bin/nmap \
 && rm -rf /var/lib/apt/lists/* \
 && useradd --create-home --uid 10001 runner
WORKDIR /opt/aim
RUN mkdir -p data app && chown -R runner:runner /opt/aim
COPY --from=build --chown=runner:runner /build/App-Instance-Manager/target/DynamicCode-0.0.1-SNAPSHOT.jar app.jar
USER runner
EXPOSE 8888
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
  CMD curl -fsS http://localhost:8888/v3/api-docs > /dev/null || exit 1
ENTRYPOINT ["java", "-jar", "/opt/aim/app.jar"]
