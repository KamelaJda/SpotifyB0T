FROM gradle:jdk17-alpine AS build
USER gradle

RUN mkdir build

ADD --chown=gradle:gradle . /home/gradle/build

RUN cd /home/gradle/build/ && gradle shadowjar

FROM openjdk:17-alpine AS final

EXPOSE 2137

WORKDIR /app

COPY --from=build /home/gradle/build/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]
