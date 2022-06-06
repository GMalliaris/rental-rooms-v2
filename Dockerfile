FROM openjdk:11
MAINTAINER georgemalliaris8@gmail.com
EXPOSE 8443
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]