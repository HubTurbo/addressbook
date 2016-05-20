#Release
In adding updating function to addressbook, its release is tweaked to separate the dependencies libraries (their JARS)
from the main app, so that only affected JARs need to be updated. This saves bandwidth in downloading update.

At the same time, to give convenience to user in getting the application, the whole JARs of main app and the libraries
are packed into 1 JAR file which will self-unpack itself on first run and start the main app from then onwards
whenever it is executed.

##Gradle Tasks for Release

The following explains the tasks under release task category in gradle.

###createJarUpdater
Create Jar Updater executable file, which job is to apply updates to the JARs of the main application. Use this task
if you would like to test Jar Updater only.

###addJarUpdaterToMainApp
Include Jar Updater into the main application for it to update itself.

###copyDependencyLibrariesToReleaseDir
As JARs of addressbook libraries will be kept separate from the main app, they will be needed to run the main app JAR.
Use this task to copy the JARs of the libraries into the release directory if you are testing the JAR of the main app.
This task is not needed if you are running the app from IDE.

###createMainAppRelease
Create the JAR of the main application. This task will also run all the other tasks required to enable the main app JAR
to run, such as addJarUpdaterToMainApp and copyDependencyLibrariesToReleaseDir.

###createInstallerJar
Create the packed JAR to be provided to user to use.

##How to create a release
To create a release:

1. Run createInstallerJar.
2. Upload addressbook.jar and the JARs in the directory 'lib' that requires update.
3. Update UpdateData.xml accordingly.