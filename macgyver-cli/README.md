

# Testing

The easiest way to test the CLI is to build and run it like so:

```shell
gradle install  && java -jar build/libs/macgyver-cli-<version>-capsule.jar <command>
```

If you want to redirect logging to the cli log file instead of the console, add -Dcli.launch=true


```shell
gradle install  && java -Dcli.launch=true -jar build/libs/macgyver-cli-<version>-capsule.jar <command>
```