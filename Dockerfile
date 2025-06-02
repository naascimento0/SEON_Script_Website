FROM openjdk:21-jdk

# Create app directory
WORKDIR /app

# Copy the Astah JAR files and command script
COPY jars/ /app/jars/

# Make the script executable
RUN chmod +x /app/jars/astah-command.sh

# Create directories for files
VOLUME ["/app/input", "/app/output"]

# Define entrypoint to run the Astah command
ENTRYPOINT ["/app/jars/astah-command.sh"]