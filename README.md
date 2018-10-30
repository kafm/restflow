# Restflow - powerful rest apps with your flow

Restflow is a JVM targeted framework used to build high performance, 
and easily deployable rest apis and microservices.


## Main objectives
    
    High performance apis
    Easily scalable


## Architecture

Restflow was built using [vertx](https://vertx.io/), [hazelcast](https://hazelcast.com/) and a very easy to understand nonblocking architecture.

The architecture adopted allows restflow to easily scale from a single to a multitude of commodity hardware.

Fork the code make changes.


## Tests
    TODO


## Use Cases
### [flexbundle](https://flexbundle.com/)
Flexbundle (also known as flex) allows you to build custom collaborative databases to manage your work your way. 
You can easily collect, track and analyze everything 
including clients, projects, and operations.

<!-- ### Flexmarket (soon)
    Flexmarket is a platform that allows people to trade stocks, bonds, currencies using a confortable mobile app (android & ios)
    and a webapp that offers advanced technical analysis, notifications and gorgeous graphs. Soon
 -->

## Getting started
You can start forking the [restflow-example](./restflow-example) project.
Restflow is not yet hosted on maven, so you have to compile and install it yourself for now.
Here are the steps
    
    git clone https://github.com/kafm/restflow/

    cd restflow/restflow-core && mvn clean compile assembly:single && mvn install:install-file -Dfile=target/restflow.jar  -DpomFile=pom.xml 


## Wiki & API Docs
Soon, also see [restflow-example](./restflow-example)

Copyright [Platum Inc](https://platum.io/) 2016-2018
