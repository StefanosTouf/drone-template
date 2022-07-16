FROM sbtscala/scala-sbt:11.0.15_1.7.1_2.12.16 AS test

RUN mkdir /opt/template

COPY . /opt/template/

WORKDIR /opt/template

RUN sbt test

FROM sbtscala/scala-sbt:11.0.15_1.7.1_2.12.16 AS build

RUN mkdir /opt/template

COPY . /opt/template/

WORKDIR /opt/template

RUN sbt assembly

FROM openjdk:17.0.2-slim AS final

RUN mkdir /opt/template

COPY --from=build /opt/template/target/scala-2.12/drone-sample-project-assembly-0.1.0.jar /opt/app.jar

ENTRYPOINT ["java", "-jar", "/opt/app.jar"]