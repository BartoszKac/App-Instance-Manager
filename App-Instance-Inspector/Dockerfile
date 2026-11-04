FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build/App-Instance-Inspector
COPY proto /build/proto
COPY App-Instance-Inspector/pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -B dependency:go-offline
COPY App-Instance-Inspector/src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B package -DskipTests

FROM eclipse-temurin:17-jre-jammy
RUN useradd --create-home --uid 10002 inspector
WORKDIR /opt/inspector
COPY --from=build --chown=inspector:inspector /build/App-Instance-Inspector/target/inspector-0.0.1-SNAPSHOT.jar app.jar
USER inspector
EXPOSE 9090
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/opt/inspector/app.jar"]
