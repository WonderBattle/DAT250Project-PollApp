# 1. Use Java 21 image
FROM eclipse-temurin:21-jdk-alpine

# 2. Set working directory
WORKDIR /app

# 3. Copy source code
COPY . .

# 4. Build the application
RUN ./gradlew bootJar -x test

# 5. Remove the 'plain' jar so only the main jar remains
RUN rm -f build/libs/*-plain.jar

# 6. Move the remaining jar to app.jar
RUN mv build/libs/*.jar app.jar

# 7. Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]