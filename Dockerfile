FROM openjdk:11-jre-slim
VOLUME /tmp
ADD target/docker-liqiqi.jar app.jar
EXPOSE 8181
ENTRYPOINT ["java","-jar","app.jar"]