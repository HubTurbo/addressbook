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

There are 3 main types of tests.

1. Unit Tests
  - Logic Testing

2. GUI Unit Testing
    - Tests the UI interaction within a single component, and ensure its behaviour holds.

3. GUI Testing (Integration)
  - Tests the UI interaction with the user as well as the interaction between various components (e.g. passing of data)

# Current issues
GUI Testing barely works:

**Headful Testing**
  - Local Machine **PASSES**
  - Travis **FAILS**
      - Exceptions
      - Stuck until Travis automatically stops it

**Headless Testing**
  - Local Machine
    - causes JVM to crash when using Mac OSX
    - passes on windows
  - Travis
    - "Passes" headless tests even with faulty tests
      - Might be that it isn't being run at all
    - Fails when trying another test implementation

*Problems*:

- These cases seem to appear only after JxBrowser was added.
- Extend `GuiTest` or `FxRobot` or `ApplicationTest`?

# Resources
- Clone [TextFX](https://github.com/TestFX/TestFX)
  -  Use `./gradlew javadoc` to obtain some documentation
- [TestFX Issues](https://github.com/TestFX/TestFX/issues)
  - Look through comments to figure out implementation
  - [Problem with getRootNode after extending GuiTest](https://github.com/TestFX/TestFX/issues/190)
  - [Problem with test independence](https://github.com/TestFX/TestFX/issues/173)
- Key API
  - `FXMLLoader.setDefaultClassLoader(TestApp.class.getClassLoader())` is a must to load fxml files correctly
  - `FxToolkit.registerPrimaryStage()`
  - `FxToolkit.setupApplication(TestApp.class)`
  - `FxToolkit.toolkitContext().getRegisteredStage()`
  - `FxToolkit.showStage()` and `FxToolkit.hideStage()`
