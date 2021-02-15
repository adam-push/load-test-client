FROM pushtechnology/docker-baseimage-java:0.9.22
MAINTAINER support@pushtechnology.com

# Put everything in /opt
WORKDIR /opt

ADD load-subscriber-${project.version}.jar /opt/load-subscriber-${project.version}.jar
ADD *.json /opt/resources/

ENTRYPOINT ["java", "-Xmx2g", "-jar", "/opt/load-subscriber-${project.version}.jar"]


