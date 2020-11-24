FROM ubuntu:latest

WORKDIR /app
#puts all files inside the container  
COPY . /app
EXPOSE 8080/tcp

ADD application-prod.properties /app/src/main/resources

RUN mvn clean package -Pprod -DskipTests

CMD java -jar target/BackendServer-0.0.1-SNAPSHOT.jar