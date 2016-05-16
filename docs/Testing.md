Running the Tests

`./gradlew` to run checkstyle and **headless testing**. This is run on Travis CI by default. However, even though it is supposed to be headless, there will be interruptions during testing for Mac OSX users, since launching of JVM will cause a focus loss of other windows.  
`./gradlew check clean` to run checkstyle and **headful testing**.  
`./gradlew headless` to run **headless testing** only.  

There are 3 main types of GUI Tests.

1. Unit Testing
  - Logic Testing
  - GUI Testing (Unit)
    - Tests the UI interaction within a single component, and ensure its behaviour holds.

2. GUI Testing (Integration)
  - Tests the UI interaction with the user as well as the interaction between various components (e.g. passing of data)
