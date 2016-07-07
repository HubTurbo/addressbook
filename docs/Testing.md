# Running Tests

Tests' settings are mostly contained in `build.gradle` and `.travis.yml`.

## Gradle Tasks
There are a few key gradle tasks defined that we can play around with:  
- `alltests` to run all tests:
  - `guiTest`
  - `guiUnitTest`
  - `unitTest`
- `headless` to set headless properties (applies only to the above test tasks, and not to default gradle's `test` tasks)
- `checkStyle` to run code style checks
- `clean` to remove previously built files

## Local Testing
### How to do some common testing-related tasks
- To run all tests in headless mode: `./gradlew`
  - It will run `clean`, `headless`, `alltests`, `jacocoRootReport` tasks.
- To run checkstyle followed by headful tests `./gradlew checkStyle alltests`
- To run **headless testing** only: `./gradlew headless` alltests
  - Running tests repeatedly will not re-run the tests unless the build files are `clean`ed first.
    e.g. `./gradlew clean headless alltests`
- To run **headful testing** only: `./gradlew alltests`
- Running specific test classes in a specific order: When troubleshooting test failures,
  you might want to run some specific tests in a specific order. Here are the steps to do that.
  - Create a test suite (to specify the test order). Here is an example:
     ```java
     package address;

     import org.junit.runner.RunWith;
     import org.junit.runners.Suite;

     @RunWith(Suite.class)
     @Suite.SuiteClasses({
             /*The tests to run, in the order they should run*/
             address.unittests.browser.BrowserManagerTest.class,
             address.guitests.PersonOverviewTest.class
     })

     public class TestsInOrder {
         // the class remains empty,
         // used only as a holder for the above annotations
     }
     ```
  - Run the test suite using the gradle command <br>
   `gradle clean headless --tests address.TestsInOrder`


## CI Testing
- The current Travis set up
  - runs the `./gradlew clean headless alltests jacocoRootReport coveralls -i` command.
  - At the moment, we do not check the code style. It is up to the contributor to verify his or her coding style locally by running `./gradlew checkStyle`.
  - Automatically retries up to 3 times if a task fails


## Types of Tests

We have grouped the tests into the following types.

1. Unit Tests
  - Logic Testing

2. GUI Unit Testing
    - Tests the UI interaction within a single component, and ensure its behaviour holds.

3. GUI Testing (Integration)
  - Tests the UI interaction with the user as well as the interaction between various components (e.g. passing of data)
