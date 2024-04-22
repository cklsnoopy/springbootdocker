FROM openjdk
LABEL authors="fei.wei"
RUN mkdir "/home/test"
WORKDIR "/home/test"
VOLUME /tmp
#ADD springbootdocker-0.0.1-SNAPSHOT.jar app.jar
COPY ./springbootdocker-0.0.1-SNAPSHOT.jar /home/test/app.jar
ENTRYPOINT ["java","-jar","/home/test/app.jar"]