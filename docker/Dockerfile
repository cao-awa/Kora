FROM ghcr.io/graalvm/jdk-community:24

WORKDIR /app

COPY Kora-1.0.3.jar /app/Kora-1.0.3.jar
COPY assets/ /app/assets/
COPY config/launch.json /app/config/launch.json

EXPOSE 12345

ENTRYPOINT ["java", "-jar", "/app/Kora-1.0.3.jar", "-server", "-XX:+UseZGC", "-Xmx128M", "-Xms128M"]