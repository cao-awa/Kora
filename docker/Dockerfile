FROM ghcr.io/graalvm/jdk-community:24

WORKDIR /app

COPY Kalmia-1.0.10.jar /app/Kalmia-1.0.10.jar
COPY assets/ /app/assets/
COPY config/launch.json /app/config/launch.json

EXPOSE 12345

ENTRYPOINT ["java", "-jar", "/app/Kalmia-1.0.10.jar", "-server", "-XX:+UseZGC", "-Xmx128M", "-Xms128M"]