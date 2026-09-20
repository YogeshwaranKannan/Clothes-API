# ---------- Build stage ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copy Gradle wrapper + build files first so dependency layers are cached
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon > /dev/null 2>&1 || true

# Copy source and build the executable jar
COPY src src
RUN ./gradlew bootJar -x test --no-daemon \
    && cp "$(ls build/libs/*.jar | grep -v plain | head -n 1)" /workspace/app.jar

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --create-home spring \
    && mkdir -p /app/uploads \
    && chown -R spring /app
USER spring

COPY --from=build /workspace/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]