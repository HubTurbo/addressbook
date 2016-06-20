# Release
## How to create a release
0. **Pre-requisite** Run `gradle` task `createInstallerJar` under `release` category
  - This is to ensure that all binaries can be created successfully (i.e. no compile-time error).
  - If there is any compile-time error, resolve them first before continuing on the next step.
1. Update version in `MainApp` and in `build.gradle`
2. Run `gradle` task `generateUpdateData` under `release` category
  - The console will print a list of libraries which should be updated
3. Open `UpdateData.json` and update the new fields accordingly
  - Put the link to download the new libraries. For now, we upload it to the new release we are going to create after this
  but the URL will follow GitHub release download link - `https://github.com/HubTurbo/addressbook/releases/download/<release version>/<filename>`.
  - Change the OS compatibility of the new libraries to ensure that only the libraries relevant to an OS will be loaded and checked
4. Commit the  files for release - name the commit `V<MAJOR>.<MINOR>.<PATCH>`
  - This is so that the git tag that GitHub release creates will appropriately tag the commit with updated `UpdateData.json`
5. Create a release in [GitHub](https://github.com/HubTurbo/addressbook/releases)
6. Run `gradle` task `createInstallerJar` under `release` category
7. Upload the following as binaries to the latest release:
  - addressbook.jar
  - resource-\<version\>.jar
  - all the jars inside `lib` directory which are mentioned in (2)

## More About Release
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

### generateUpdateData
Generates the update data (UpdateData.json) which will be used by user's application instance to know if it has an update
and what to update. It reads the depencies of the latest version of addressbook and put those dependencies into the update
data. To make it easier for developer to update only things that get updated, it will use previous update data value for
libraries that do not change. Developers then need to update the new dependencies information (such as URL to download
the libraries and the OS that needed them) manually.

generateUpdateData has its own source set which includes everything - main application and installer - and its dependencies
are extended from main application compile dependencies. This is to make it easier for generateUpdateData to read any
information it needs to create update data. The main class it uses is `installer/UpdateDataGenerator.java`

### createJarUpdater
Create Jar Updater executable file, which job is to apply updates to the JARs of the main application.
Use this task if you would like to only test Jar Updater.

Its custom source set is defined in the task itself instead of as a separate configuration.

### addJarUpdaterToMainApp
Include Jar Updater into the main application for it to update itself.

This task is necessary as `gradle` will run `processResource` of the main app before compiling the classes, hence
outputting the Jar Updater JAR into resource directory will be ignored (since it has been processed); it needs to be
copied manually to the resource output directory before the main application JAR is packaged, i.e. creating executables.

### copyDependencyLibrariesToReleaseDir
As JARs of addressbook libraries will be kept separate from the main app, they will be needed to run the main app JAR.
Use this task to copy the JARs of the libraries into the release directory if you are testing the JAR of the main app.
This task is not needed if you are running the app from IDE.

### createMainAppExecutable
Create the JAR of the main application. This task will also run all the other tasks required to enable the main app JAR
to run, such as addJarUpdaterToMainApp and copyDependencyLibrariesToReleaseDir.

### createInstallerJar
Create the packed JAR to be provided to user to use. The packed JAR contains the main app executable and all the
libraries needed for the main app executable to run. It will run the other tasks necessary to generate those files and
keep them as resources.

Installer is defined into its own source set to make it easier to compile.

Currently, Installer needs Jackson to parse the update data for dependency setting purposes. However, it is not compiled
to Installer JAR; it will need `lib/[jackson].jar` to work. Those Jackson JARs are inside Installer JAR as resource, though,
which will be unpacked anyway. Hence, JSON parsing in Installer must be called only after it unpacks itself.

## To be improved

### Automate generateUpdateData
Ideally, we don't need to host those libraries JAR on our own in GitHub release. We can grab the JARs from the Maven repositories
that `gradle` uses to get those libraries. Unfortunately, the lack of documentation of `gradle` makes it impossible to
get the URL of those JAR in the Maven repositories, hence the manual need to update the download links and upload the new
libraries to GitHub release.

### GUI for generateUpdateData
Instead of having to deal with text file of update data which is prone to error, we can have a GUI which generateUpdateData
uses to show what libraries have been changed in the latest version with fields to update the download links. Also,
we can have a dropdown option for the OS which the libraries are needed in so developers don't need to type them manually.

### Installer dependencies
Currently Installer only has Jackson dependency which is easy to manage on compiling side and easy to control on program flow
side. However, it will be better if dependencies of Installer to be compiled (i.e. converted to classes) and packed together
into the JAR so that it does not need to depend on external JAR libraries which need to be unpacked before running
some methods. It's also safer when there is a call to a library method early in the program, say Apache IO on file opening.
In this case, use of ShadowJAR or tinkering around `gradle` system to compile libraries as classes into JAR should be considered.