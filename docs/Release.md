# Release
Addressbook is configured to be released as a [non-fat JAR](http://stackoverflow.com/questions/19150811/what-is-a-fat-jar),
that is, libraries that it depends on will not be included inside the Addressbook JAR. Instead, the JARs of these libraries
will be put in `lib` folder at the same level of Addressbook JAR for the application to run and setting the classpath of
Addressbook JAR to include the `lib` folder.

For example:
```
addressbook/
  |-> addressbook.jar
  |-> lib/
       |-> apache-commons-io.jar
       |-> apache-logging.jar
       |-> jxbrowser.jar
```

This set-up is used to reduce the size of updates to be downloaded whenever a new version of Addressbook is released. With
this set-up, only main application JAR and libraries that need to be updated will be downloaded, essentially reducing the
time taken to download updates.

To give convenience to user in preparing such application set-up, the whole set-up mentioned above is packed into 1 JAR file
which will self-unpack itself on first run and start the main app from then onwards whenever it is opened (essentially
a wrapper to the main application).

Several gradle tasks have been prepared to make it effortless to create a release of Addressbook with the set-up mentioned above.

## Gradle Tasks for Release

The following explains the tasks under `release` task category in gradle.

### createJarUpdater
Create Jar Updater executable file, which job is to apply updates to the JARs of the main application. Use this task
if you would like to test Jar Updater only.

### addJarUpdaterToMainApp
Include Jar Updater into the main application for it to update itself.

### copyDependencyLibrariesToReleaseDir
As JARs of addressbook libraries will be kept separate from the main app, they will be needed to run the main app JAR.
Use this task to copy the JARs of the libraries into the release directory if you are testing the JAR of the main app.
This task is not needed if you are running the app from IDE.

### createMainAppExecutable
Create the JAR of the main application. This task will also run all the other tasks required to enable the main app JAR
to run, such as addJarUpdaterToMainApp and copyDependencyLibrariesToReleaseDir.

### createInstallerJar
Create the packed JAR to be provided to user to use.

## ~~How to create a release~~ (Developing...)
~~To create a release:~~

~~1. Run createInstallerJar.~~
~~2. Upload addressbook.jar and the JARs in the directory 'lib' that requires update.~~
~~3. Update UpdateData.xml accordingly.~~