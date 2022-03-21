FROM openjdk:11.0.3-jre-slim-stretch

USER root

# install some basic command line tools
RUN apt-get update; \
	  apt-get install -y curl wget vim tar bash

RUN mkdir /api
#RUN groupadd everyone
#RUN useradd -m 1001 -p changeit
#RUN usermod -a -G everyone 1001
#RUN mkdir /file-uploads && chown -R 1001:everyone /file-uploads
RUN mkdir /file-uploads
RUN chmod 777 /file-uploads


COPY src/main/resources/dist /src/main/resources/dist
#COPY src/main/resources/yuuviskeystoremacpro.jks /src/main/resources/yuuviskeystoremacpro.jks
COPY src/main/resources/yuuviskeystoremacpro.jks /api/yuuviskeystoremacpro.jks
#RUN chmod 777 /api/yuuviskeystoremacpro.jks
COPY target/yuuvis-v-api-3.0.14-SNAPSHOT-fat.jar /api/app.jar

#USER 1001

CMD [ "java", "-jar", "/api/app.jar"]
