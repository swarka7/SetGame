# Build & run the Set card game.
# Default: headless-friendly (no UI needed). For UI, see README notes.

FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy pom first to leverage Maven cache
COPY pom.xml .
RUN mvn -B -q -DskipTests package || true

# Copy sources and build
COPY src ./src
RUN mvn -B -q package

FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy built artifacts
COPY --from=build /app/target/Set_Card_Game-1.0-SNAPSHOT.jar ./Set_Card_Game-1.0-SNAPSHOT.jar
COPY --from=build /app/src/main/resources/config.properties ./config.properties

# For UI mode, you may need: -Djava.awt.headless=false and DISPLAY env.
CMD ["java", "-jar", "Set_Card_Game-1.0-SNAPSHOT.jar"]
