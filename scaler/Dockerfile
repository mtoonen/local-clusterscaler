FROM eclipse-temurin:17-jre

EXPOSE 8080
ENV PROFILE=docker
ENV JVM_ARGS "-XX:+UseContainerSupport -XX:MaxRAMPercentage=80 -XX:+UseCompressedOops"

ARG VERSION

ADD target/scaler-*.jar /scaler.jar
ENTRYPOINT ["sh", "-c", "java -jar $JVM_ARGS -Dspring.profiles.active=\"k8s,$PROFILE\" /scaler.jar"]
