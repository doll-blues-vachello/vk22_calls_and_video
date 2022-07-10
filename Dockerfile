FROM openjdk:11
ADD . /app
WORKDIR /app
RUN ./gradlew shadowJar

