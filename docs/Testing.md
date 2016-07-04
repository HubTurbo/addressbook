# Running Tests

## Gradle Tasks
There are a few key gradle tasks defined that we can play around with:  
- `headless` to run **headless testing**
- `headful` to run **headful testing**
- `checkStyle` to run code style checks
- `clean` to remove previously built files

## Local Testing
### How to do some common testing-related tasks
- To run all tests in headless mode: `./gradlew`
  - It will run `clean`, `headless` tasks.
- To run checkstyle followed by headful tests `./gradlew checkStyle headful`
- To run **headless testing** only: `./gradlew headless`
  - Running this or `headful` repeatedly will not re-run the tests unless the build files are `clean`ed first.
    e.g. `./gradlew clean headless`
- To run **headful testing** only: `./gradlew headful`
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
  - runs the `./gradlew` command.
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

## Current issues

**Headless Testing**
  - Running headless tests on Mac OSX no longer causes the window to lose focus!
  - [Causes JVM to crash when using Mac OSX](https://github.com/HubTurbo/addressbook/issues/108)
      - Temporarily disabled initializing the browser in test mode. This should and will be changed to a mock instead in future PRs.
