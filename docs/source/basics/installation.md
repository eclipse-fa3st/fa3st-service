# Installation

## Requirements

-   Java Runtime 17 or newer

## Precompiled JAR

<!--start:download-release-->
<!--end:download-release-->

<!--start:download-snapshot-->
{download}`Latest SNAPSHOT version (1.0.0-SNAPSHOT) <https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=org.eclipse.digitaltwin.fa3st.service&a=fa3st-service-starter&v=1.0.0-SNAPSHOT>`<!--end:download-snapshot-->

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
cd fa3st-service
mvn clean install
```