FROM ubuntu:latest

ARG PROPS-PATH

WORKDIR /app
#puts all files inside the container  
COPY . /app
EXPOSE 8080/tcp

COPY $PROPS-PATH /app/src/main/resources

RUN apt update -y
RUN apt install maven -y
RUN mvn clean package -Pprod -DskipTests

CMD java -jar target/BackendServer-0.0.1-SNAPSHOT.jar