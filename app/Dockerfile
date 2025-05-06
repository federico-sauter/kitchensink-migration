FROM gradle:8-jdk21 AS build
COPY . /src
WORKDIR /src
RUN chmod +x gradlew && ./gradlew bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /src/build/libs/*-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
