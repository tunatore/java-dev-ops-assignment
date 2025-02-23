# Use Amazon Corretto 21 as the base image
FROM amazoncorretto:21

# Copy the built JAR file into the container
COPY target/product-service-1.0.0.jar /app/

# Set the working directory in the container
WORKDIR /app

# Expose the port your application runs on
EXPOSE 8080

CMD ["java", "-jar", "product-service-1.0.0.jar"]