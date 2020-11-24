# backend-cuckoo

<h2> Processo de instalação </h2>
<b>ATENÇÃO</b> 
Estas configurações têm em conta que está a fazer as configurações num computador com a máquina Ubuntu.
Deverá fazer num ambiente igual ou semelhante.
<h3> Backend </h3>
O Backend encontra-se a ser desenvolvido em Java 11 e Postgres. O Postgres irá correr (para fins de desenvolvimento) num <i>docker container</i>

<h4> Java 11 </h4>
Para instalar o Java 11 corra os seguintes comandos:

- sudo apt-get update

- sudo apt-get upgrade

- sudo apt install openjdk-11-jre-headless

O Projeto também irá necessitar de instalar o maven:

- sudo apt-get install maven

<h4> Postgres </h4>
Para correr o Postgres iremos instalar o docker terá de correr os seguintes comandos:

- sudo apt update

- sudo apt install apt-transport-https ca-certificates curl software-properties-common

- curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -

- sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu bionic stable"

- sudo apt update

- sudo apt-get install docker-ce docker-ce-cli containerd.io

A partir desta fase o docker já deverá estar instalado, no entando deve ser necessário correr o docker com permissões de administrador. Para testar se isto aconteceu mesmo deve correr o seguinte comando:

- docker run hello-world

Se tiver havido algum erro com o processo corra os seguintes comandos:

- sudo groupadd docker

- sudo usermod -aG docker $USER

Reinicie o terminal em que se encontra e corra o seguinte commando:

- newgrp docker 

Volte a correr o comando <b> docker run hello-world </b> e averigue se correu tudo bem

Para configurar a Base de Dados deverá correr os seguintes passos:

- docker pull postgres:alpine

- docker run --name cuckoo-db -e POSTGRES_PASSWORD=1234 -d -p 5435:5432 postgres:alpine

- docker exec -it cuckoo-db psql -U postgres -c \ "CREATE DATABASE cuckoodb;"

O processo de configuração está acabado. Deverá colocar o servidor a correr.



<h2> Execução </h2>
Estes são os passos necessários para colocar os sistemas a correr:

- <b> Docker (uma vez aberto o terminal só é necessário executar este comando uma vez)</b> docker start cuckoo-db

- <b> Back End (dentro da pasta backend/UserServer)</b> mvn clean spring-boot:run

Cada um deve correr ao mesmo tempo pelo que exige que seja corrido em terminais separados. Para encerrar cada um deverá usar o <b>CTRL+C</b> em cada um.

<h2> Reiniciar a Base de Dados </h2>
Sempre que alguma atualização é feita, a base de dados deve ser reiniciada. Para tal deveremos executar os seguintes comandos:

- docker start cuckoo-db

- docker exec -it cuckoo-db psql -U postgres

- DROP DATABASE cuckoodb; CREATE DATABASE cuckoodb;

- \q

Após isto basta executar diretamente o servidor.


  <h2> Executar o docker container </h2>
  É necessário ter um application-prod.properties e um .env na diretoria principal.

  Para fazer build deverá ser executado:
  - docker build --tag backend-cuckoo .

  Para correr é necessário executar:
  - docker run --network="host" --env-file .env  backend-cuckoo:latest

  A porta 8080 irá ficar exposta para tcp
