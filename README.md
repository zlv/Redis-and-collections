Redis java collections
1. Copy app.properties.example to app.properties, enter the password and redis urls
2. $ export REDIS_PASSWORD=password; docker-compose up
   $ mvn package
   $ java -jar target/Redis-and-collections-1.0-SNAPSHOT-jar-with-dependencies.jar