# Usage

## Command-Line Interface (CLI)

To start FA³ST Service from command-line you need to run the `starter` module by calling

```sh
> java -jar fa3st-service-starter-{version}.jar
```

When started without arguments, FA³ST Service will try to auto-detect a configuration file named `config.json` and a model file named `model.[ext]` where `[ext]` is a supported file extension like `json`, `xml`, or `aasx`.

To manually pass a model file `my-model.aasx` and a configuration file `my-config.json` run the following command:

```sh
> java -jar fa3st-service-starter-{version}.jar --model my-model.aasx --config my-config.json
```

An example model file can be found in the [FA³ST Github-Repository](https://github.com/eclipse-fa3st/fa3st-service/blob/main/misc/examples/model.aasx).
Regarding AAS examples from other sources it is important to note that the AAS specification is not backwards compatible. Therefore, it is necessary to make sure that the model file conforms to the latest AAS specification (V3).
Passing an outdated model file will lead to an error. If a model file conforms to V3 of the Asset Administration Shell specification but has duplicate identifiers, FA³ST will not load the file.
Starting FA³ST with the `--no-validation` flag loads the file in any case but will lead to problems during runtime if there are duplicate identifiers present.

:::{table} Supported CLI arguments and environment variables.
| CLI (short)   | CLI (long)            | Environment variable                                           | Allowed<br>Values                       | Description                                                                                                                                              | Default<br>Value |
| ------------- | --------------------- | -------------------------------------------------------------- | --------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------- |
| `-c`          | `--config`            | faaast_config                                                  | <file path>                             | The config file to use.                                                                                                                                  | config.json      |
| `-e`          | `--empty-model`       |                                                                |                                         | Starts the FAST service with an empty Asset Administration Shell Environment.                                                                            |                  |
|               | `--endpoint`          |                                                                | HTTP<br>OPCUA                           | Additional endpoints that should be started.                                                                                                             |                  |
| `-h`          | `--help`              |                                                                |                                         | Print help message and exit.                                                                                                                             |                  | 
|               | `--loglevel-external` | faaast_loglevel_external                                       | TRACE<br>DEBUG<br>INFO<br>WARN<br>ERROR | Sets the log level for external packages.<br>This overrides the log level defined by other commands such as *-q* or *-v*.                                | WARN             |
|               | `--loglevel-fa3st`    | faaast_loglevel_fa3st                                          | TRACE<br>DEBUG<br>INFO<br>WARN<br>ERROR | Sets the log level for FA³ST packages.<br>This overrides the log level defined by other commands such as *-q* or *-v*.                                   | WARN             |
| `-m`          | `--model`             |                                                                | <file path>                             | The model file to load.                                                                                                                                  | model.*          |
|               | `--no-validation`     | faaast_no_validation                                           |                                         | Disables all validation, overrides validation defined in the configuration Environment.                                                                  |                  |
| `-q`          | `--quite`             |                                                                |                                         | Reduces log output (*ERROR* for FAST packages, *ERROR* for all other packages).<br>Default information about the starting process will still be printed. |                  |
| `-v`          | `--verbose`           |                                                                |                                         | Enables verbose logging (*INFO* for FAST packages, *WARN* for all other packages).                                                                       |                  |
| `-V`          | `--version`           |                                                                |                                         | Print version information and exit.                                                                                                                      |                  |
| `-vv`         |                       |                                                                |                                         | Enables very verbose logging (*DEBUG* for FAST packages, *INFO* for all other packages).                                                                 |                  |
| `-vvv`        |                       |                                                                |                                         | Enables very very verbose logging (*TRACE* for FAST packages, *DEBUG* for all other packages).                                                           |                  |
|               | `{key}={value}`       | fa3st_config_extension_{key}<br>with *{key}* separated by *_*  | any                                     | Additional properties to override values of configuration using [JSONPath](https://goessner.net/articles/JsonPath/) notation without starting *$.*       |                  |
:::

## From Java Code

You can run FA³ST Service directly from your Java code as embedded library.
This way, you can create your configuration and model directly in code and don't have to create them as files (you can still load them from files if you want to).
The following code snippet shows how to create and run a new FA³ST Service from code using a model file.

```{code-block} java
:caption: Create a FA³ST Service from code.
:lineno-start: 1
Service service = new Service(ServiceConfig.builder()
	.core(CoreConfig.builder()
		.requestHandlerThreadPoolSize(2)
		.build())
	.persistence(PersistenceInMemoryConfig.builder()
		.initialModelFile(new File("{pathTo}\\FAAAST-Service\\misc\\examples\\model.aasx"))
		.build())
	.endpoint(HttpEndpointConfig.builder().build())
	.messageBus(MessageBusInternalConfig.builder().build())
	.fileStorage(FileStorageInMemoryConfig.builder().build())
	.build());
service.start();
```