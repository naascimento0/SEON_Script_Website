FROM gradle:8.10-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre AS runtime
# Set Astah's file.stack to 64 MB (max allowed) so it can deserialize complex .asta files.
# Without this, Astah defaults to 5 MB for its deserialization thread, causing StackOverflow.
RUN mkdir -p /root/.astah/uml && \
    printf 'file.stack=64\n' > /root/.astah/uml/JudeU.properties
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
COPY --from=builder /app/jars/ jars/
ENTRYPOINT ["java", "-jar", "app.jar"]
