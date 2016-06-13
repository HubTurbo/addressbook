# Configuration

Certain properties of the application can be customize through the configuration file (default: config.ini).
The application creates a config file with default values if no existing config file can be found.
- Logging level
- Update interval
- Cloud mode (unreliability)

Most of the variable names are rather straightforward. However, the logging section is slightly more complex and will be elaborated on.

Also note that **case sensitivity has not been handled** as of yet.


# Logging
There are many variables for the different logging levels in the config file:
- Adding class names to the variables will impose a special logging level for that class (priority over the application-wide `loggingLevel`)

For example:
```
...
[Logging]
loggingLevel = INFO
TRACE = ModelManager, SyncManager
ALL =
ERROR =
...
```
Such a configuration will log messages at the `INFO` level throughout the application, except `ModelManager` and `SyncManager` which will log messages at the `TRACE` level.

# Adding config variables (For developers)
- Ensure that a default value is written to the config file (in `createConfigFileWithDefaults`)
- Ensure that the config file value is read if it exists (in `readAndSetConfigValues`)
- Access the read config value from the global config object obtainable through `getConfig()`
