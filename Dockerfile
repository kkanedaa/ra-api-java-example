# Build stage

# Build container from an image including JDK 17 and Maven for the building stage

FROM maven:3.8.5-openjdk-17 as builder

# Create group and user kei with ID 1500 and set user kei's primary group to group kei
RUN groupadd -g 1500 kei && useradd -u 1500 -g 1500 kei

# Cached steps
WORKDIR /build
COPY . .
RUN mvn package 

# Production stage

# Build container from an image including only JRE for the production stage
FROM ubuntu/jre:17_edge

WORKDIR /app

# Copy jar file from building stage to production and rename it
COPY --from=builder /build/registration-authority-app/target/registration-authority-app-0.0.1-SNAPSHOT.jar /app/app.jar

# Set container's user to 1500:1500 (kei:kei)
USER 1500:1500

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
