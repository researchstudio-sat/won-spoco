# before docker build can be executed, the jar file and the conf
# directory have to be copied into this folder (done by maven build)

# use java as a base image
# fix java version here until the following issue is resolved: https://github.com/researchstudio-sat/webofneeds/issues/1229
FROM openjdk:8u121-jdk

# add webofneeds default config env variables
ENV WON_CONFIG_DIR=/usr/src/conf.local
ENV LOGBACK_CONFIG=logback.xml

# add the default monitoring output directory
ENV RAID_BOT=won.spoco.raid.bot.app.RaidBotApp

# add the jar and the conf directory
ADD ./raid-bot.jar /usr/src/
ADD ./conf ${WON_CONFIG_DIR}

# add certificates directory
RUN mkdir -p /usr/src/certs


# start echo bot
WORKDIR /usr/src/
CMD java -DWON_CONFIG_DIR=${WON_CONFIG_DIR}/ \
  -Dlogback.configurationFile=${WON_CONFIG_DIR}/${LOGBACK_CONFIG} \
  ${JMEM_OPTS} \
  ${JMX_OPTS} \
  -cp "raid-bot.jar" ${RAID_BOT}