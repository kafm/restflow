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


## Getting started
Restflow is not yet hosted on maven, so you have to compile and install it yourself for now.
Here are the steps:

    Clone the repo:    
        git clone https://github.com/kafm/restflow/

    Compile:
        cd restflow/restflow-core && mvn clean compile assembly:single 

    Install:
        mvn install:install-file -Dfile=target/restflow.jar  -DpomFile=pom.xml 


    Import the project restflow-example into eclipse and run the class com.platum.restflow.example.App
    


And then test the example:
    
    # create some products
    curl --header "Content-Type: application/json" \
      --request POST \
      --data '{"name":"Milk"}' http://localhost:8080/api/v1/product

    curl --header "Content-Type: application/json" \
      --request POST \
      --data '{"name":"Bread"}' http://localhost:8080/api/v1/product

    curl --header "Content-Type: application/json" \
      --request POST \
      --data '{"name":"Apple"}' http://localhost:8080/api/v1/product

    # fetch the products
    curl http://localhost:8080/api/v1/product

    [
       {
          "name" : "Milk",
          "id" : 1
       },
       {
          "name" : "Bread",
          "id" : 2
       },
       {
          "name" : "Apple",
          "id" : 3
       }
    ]


## Wiki & API Docs
Soon, also see [restflow-example](./restflow-example)


## Use Cases
### [flexbundle](https://flexbundle.com/)
Flexbundle (also known as flex) allows you to build custom collaborative databases to manage your work your way. 
You can easily collect, track and analyze everything 
including clients, projects, and operations.


Copyright [Platum Inc](https://platum.io/) 2016-2018
