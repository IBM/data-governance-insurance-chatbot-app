<p align="center">
    <a href="https://cloudnativetoolkit.dev">
        <img src="https://cloudnativetoolkit.dev/static/catalyst-0819c47b678df4dd3790a4e78bf73608.svg" height="100" alt="Cloud-native Toolkit">
    </a>
</p>

<p align="center">
This template can be used as a starter kit for the <a href="https://cloudnativetoolkit.dev">Cloud-native toolkit</a>
</p>


# Create and deploy a Java - MicroProfile / Jakarta EE application

> We have applications available for [Node.js Express](https://github.com/IBM/node-express-app), [Go Gin](https://github.com/IBM/go-gin-app), [Python Flask](https://github.com/IBM/python-flask-app), [Python Django](https://github.com/IBM/python-django-app), [Java Spring](https://github.com/IBM/java-spring-app), [Java Liberty](https://github.com/IBM/java-liberty-app), [Swift Kitura](https://github.com/IBM/swift-kitura-app), [Android](https://github.com/IBM/android-app), and [iOS](https://github.com/IBM/ios-app).

In this sample application, you will create a Java cloud-native application using Liberty. This provides a starting point for creating Java web applications running on [Open Liberty](https://openliberty.io/). It contains no default application code, but comes with standard best practices, including a health check.

This application exposes the following endpoints:

* Health endpoint: `<host>:<port>/health`
* Web content: `<host>:<port>/<contextRoot>`
* Web Application: `<host>:<port>/v1/example`
* Swagger UI: `<host>:<port>/openapi/ui`
* Openapi Document: `<host>:<port>/openapi`

The web application has a health endpoint which is accessible at `<host>:<port>/health`. The ports are set in the `pom.xml` file.

## Steps

You can [deploy this application to IBM Cloud](https://cloud.ibm.com/developer/appservice/starter-kits/java-liberty-app) or [build it locally](#building-locally) by cloning this repo first.  Once your app is live, you can access the `/health` endpoint to build out your cloud native application.

### Deploying 
After you have created a new git repo from this git template, remember to rename the project. Edit the `pom.xml` and change the artifactId from the default name to the name you used to create the template.

Make sure you are logged into IBM Cloud using the IBM Cloud CLI and have access to your development cluster. If you are using OpenShift make sure you have logged into the OpenShift CLI on the command line.

Install the IBM Garage for Cloud CLI.

```$bash
curl -sfL get.cloudnativetoolkit.dev | sh -
```
Use the IBM Garage for Cloud CLI to create the dev namespace
```$bash
igc namespace dev --dev
```
Use the IBM Garage for Cloud CLI to register the GIT Repo
```$bash
igc pipeline -n dev --tekton --pipeline ibm-appmod-liberty
```
See the **Deploy an app** guide under **Day 1 - Build and deploy** in the [IBM Cloud-Native toolkit](https://cloudnativetoolkit.dev/) for details.

### Building Locally

To get started building this application locally, you can either run the application natively or use the [IBM Cloud Developer Tools](https://cloud.ibm.com/docs/cli?topic=cloud-cli-getting-started) for containerization and easy deployment to IBM Cloud.

#### Native Application Development

* [Maven](https://maven.apache.org/install.html)
* Java 8: Any compliant JVM should work.
  * [Java 8 JDK from Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  * [Java 8 JDK from IBM (AIX, Linux, z/OS, IBM i)](http://www.ibm.com/developerworks/java/jdk/),
    or [Download a Liberty server package](https://developer.ibm.com/assets/wasdev/#filter/assetTypeFilters=PRODUCT)
    that contains the IBM JDK (Windows, Linux)

To build and run the application:
1. `mvn liberty:dev`
1. Press enter once the server is running to run integration tests.

To run an application in Docker use the Docker file called `Dockerfile`. If you do not want to install Maven locally you can use `Dockerfile-tools` to build a container with Maven installed.

You can verify the state of your locally running application using the Selenium UI test script included in the `scripts` directory.

## More Details
For more details on how to use this Starter Kit Template please review the [IBM Garage for Cloud Cloud-Native Toolkit Guide](https://cloudnativetoolkit.dev/)


## Next Steps
* Learn more about [Open Liberty](https://openliberty.io/).
* Learn more about augmenting your Java applications on IBM Cloud with the [Java Programming Guide](https://cloud.ibm.com/docs/java?topic=java-getting-started).
* Explore other [sample applications](https://cloud.ibm.com/developer/appservice/starter-kits) on IBM Cloud.

## License

This sample application is licensed under the Apache License, Version 2. Separate third-party code objects invoked within this code pattern are licensed by their respective providers pursuant to their own separate licenses. Contributions are subject to the [Developer Certificate of Origin, Version 1.1](https://developercertificate.org/) and the [Apache License, Version 2](https://www.apache.org/licenses/LICENSE-2.0.txt).

[Apache License FAQ](https://www.apache.org/foundation/license-faq.html#WhatDoesItMEAN)
