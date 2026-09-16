# syntax=docker/dockerfile:1.7
FROM eclipse-temurin:25-jdk-noble AS build

WORKDIR /workspace
COPY . .

ARG MODULE
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    test -n "$MODULE" \
    && sed -i 's/\r$//' mvnw \
    && chmod +x mvnw \
    && ./mvnw -B -pl "$MODULE" -am package -DskipTests -Dmaven.javadoc.skip=true

FROM eclipse-temurin:25-jre-noble

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home linkforge

WORKDIR /app
ARG JAR_PATH
COPY --from=build --chown=linkforge:linkforge "/workspace/${JAR_PATH}" /app/app.jar

USER 10001
EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/urandom"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
