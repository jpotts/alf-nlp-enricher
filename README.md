# Alfresco NLP Enricher

This is a Spring Boot microservice that subscribes to Kafka events coming out
of Alfresco (see [Alfresco Kafka](https://github.com/jpotts/alfresco-kafka)).

When the listener sees a CREATE, UPDATE, or PING event, it uses Apache Chemistry
[OpenCMIS](https://chemistry.apache.org/java/opencmis.html) to fetch the content
from Alfresco. It then runs the content through Apache NLP to extract the people,
places, and organizations from the content which is written back to the object
in Alfresco as metadata.

## Setup

The app needs to know how to connect to Alfresco and Kafka. The
Alfresco and Kafka settings are in application.yml.

## Running

1. First, start Zookeeper, then start Kafka.
2. Now start Alfresco. At a minimum it must be running the alfresco-kafka AMP.
   One way to make that happen for demo purposes is just to add that project
   as a platformModule to an existing project's pom.xml.
3. Finally, start this app with `mvn install spring-boot:run`.
