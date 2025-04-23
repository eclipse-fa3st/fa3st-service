# Installation

## Requirements

-	Java Runtime 17 or newer

## Precompiled JAR

<!--start:download-release-->
{download}`Latest RELEASE version (1.2.0) <https://repo1.maven.org/maven2/org/eclipse/digitaltwin/fa3st/service/starter/1.0.0/fa3st-serivce-starter-1.0.0.jar>`<!--end:download-release-->

<!--start:download-snapshot-->
{download}`Latest SNAPSHOT version (1.3.0-SNAPSHOT) <https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=org.eclipse.digitaltwin.fa3st.service&a=fa3st-service-starter&v=1.0.0-SNAPSHOT>`<!--end:download-snapshot-->

## Maven Dependency

```xml
<dependency>
	<groupId>org.eclipse.digitaltwin.fa3st.service</groupId>
	<artifactId>fa3st-service</artifactId>
	<version>1.0.0</version>
</dependency>
```

## Gradle Dependency

```groovy
implementation 'org.eclipse.digitaltwin.fa3st.service:fa3st-service:1.0.0'
```

## Build from Source

```sh
git clone https://github.com/eclipse-fa3st/fa3st-service
cd FAAAST-Service
mvn clean install
```