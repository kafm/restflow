# Restflow - A BaaS framework to expose datasources as Rest Api

Restflow is a JVM targeted framework used to build high performance, 
and easily deployable rest apis and microservices and to help
organizations expose their existing datasources as Rest Api.


## Main objectives
    
    High performance apis
    Easily scalable


## Architecture

Restflow was built using [vertx](https://vertx.io/), [hazelcast](https://hazelcast.com/) and a very easy to understand nonblocking architecture.

The architecture adopted allows restflow to easily scale from a single to a multitude of commodity hardware.

Fork the code, make changes, contributions are welcome.


## Tests
    TODO

## Tasks
- [ ] Customize logs

- [ ] Write api docs

- [ ] Write wiki

- [ ] Write [example](restflow-example/) README


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

## License
restflow source code is released under MIT LICENSE.

Check [LICENSE](LICENSE) file for more information.
