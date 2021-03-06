# Dockerfile for won-debugbot
FROM maven:3.5.2-jdk-8-alpine AS mvnbuild
WORKDIR /build

COPY won-spoco-core/pom.xml ./won-spoco-core/pom.xml
COPY won-raid-bot/pom.xml ./won-raid-bot/pom.xml
COPY pom.xml .

# RUN mvn dependency:go-offline does not work since the multimodule dependency (won-spoco-core would have to be built before hand)

COPY won-spoco-core/src ./won-spoco-core/src
COPY won-raid-bot/src ./won-raid-bot/src
RUN mvn -f pom.xml clean package
# use java as a base image
# fix java version here until the following issue is resolved: https://github.com/researchstudio-sat/webofneeds/issues/1229
FROM openjdk:8u121-jdk as botrun
COPY --from=mvnbuild /build/won-raid-bot/target/bot.jar /usr/src/bots/bot.jar
# add certificates directory
RUN mkdir -p /usr/src/bots/client-certs
# start echo bot
WORKDIR /usr/src/bots/
# add webofneeds default config env variables
ENTRYPOINT ["java", "-jar", "/usr/src/bots/bot.jar"]