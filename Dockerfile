FROM openjdk:11
MAINTAINER georgemalliaris8@gmail.com
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]