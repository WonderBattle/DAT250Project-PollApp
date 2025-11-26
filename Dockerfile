# 1. Use a base image with Java 21
FROM eclipse-temurin:21-jdk-alpine

# 2. Set the working directory
WORKDIR /app

# 3. Copy your project files into the image
COPY . .

# 4. Build the application (skip tests to save time)
RUN ./gradlew bootJar -x test

# 5. Move the built jar to the root and rename it
RUN mv build/libs/*.jar app.jar

# 6. Command to run the app
ENTRYPOINT ["java", "-jar", "app.jar"]