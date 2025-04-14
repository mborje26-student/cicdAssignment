# Use an official Maven image as the base image to build the project
FROM maven:3.8.1-jdk-11 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and source code to the container
COPY pom.xml .
COPY src ./src

# Run Maven to build the project
RUN mvn clean install

# Use a smaller base image to run the application
FROM openjdk:11-jre-slim

# Set the working directory for the running container
WORKDIR /app

# Copy the compiled artifact from the build stage to the running stage
COPY --from=build /app/target/your-application.jar /app/your-application.jar

# Expose the port the app will listen to
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "your-application.jar"]
