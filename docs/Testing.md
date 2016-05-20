# Running the Tests

**GRADLE TASKS**  
There are a few key gradle tasks defined that we can play around with:  
- `headless` to run **headless testing**
- `headful` to run **headful testing**
- `checkStyle` to run code style checks
- `clean` to remove previously built files

*Common commands used*  
- `./gradlew` to run `clean`, `checkStyle`, `headless` tasks.  
This is run on Travis CI by default. However, even though it is supposed to be headless, there will be interruptions during testing for Mac OSX users, since launching of JVM will cause a focus loss of other windows.  
- `./gradlew checkStyle headful clean`
 - HT's way of doing testing  
- `./gradlew headless` to run **headless testing** only.  
 - Running this or `headful` repeatedly will not re-run the tests unless the build files are `clean`ed
- `./gradlew headful` to run **headful testing** only.

# Types of Tests

We have grouped the tests into the following types.

1. Unit Tests
  - Logic Testing

2. GUI Unit Testing
    - Tests the UI interaction within a single component, and ensure its behaviour holds.

3. GUI Testing (Integration)
  - Tests the UI interaction with the user as well as the interaction between various components (e.g. passing of data)

# Current issues

**Headless Testing**
  - Local Machine
    - [Causes JVM to crash when using Mac OSX](https://github.com/HubTurbo/addressbook/issues/108)
      - disabling JxBrowser (by not initializing/using `BrowserManager`) prevents this error