#!/bin/bash
set -e  # Exit if any command fails

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
  echo "Warning: Docker is not installed. Please install Docker and try again."
  exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
  echo "Warning: Docker Compose is not installed. Please install Docker Compose and try again."
  exit 1
fi

# Check if 'mvn' is available; if not, use the Maven wrapper
if command -v mvn &> /dev/null; then
    echo "Using mvn from PATH"
    mvn clean package
elif [ -x "./mvnw" ]; then
    echo "Using Maven wrapper"
    ./mvnw clean package
else
    echo "Error: Maven is not installed and no Maven wrapper (mvnw) found."
    exit 1
fi

# Stop existing containers (if running)
docker-compose down -v

# Build the Docker image
docker build -t product-service .

# Run the Docker container using docker-compose
docker-compose up --build