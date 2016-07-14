# Release
## Versioning
Addressbook version has the format of `V<MAJOR>.<MINOR>.<PATCH>` with suffix of `ea` if it is an early access, e.g.
`V1.0.0ea` for early access version and `V1.0.0` for stable version.

- `MAJOR` version is bumped up when a release introduces major changes to the application.
- `MINOR` version is bumped up on every release when `MAJOR` version does not change.
- `PATCH` version is bumped up when there is a hotfix needed for the current release version.

This versioning system is loosely based on [Semantic Versioning](http://semver.org/).

## Release cycle
Addressbook has 3 branches for release, namely `master`, `early-access` and `stable`:
- `master` : development will occur in this branch.
- `early-access`: when we have a release candidate from master, the latest commit in `master` will be merged to this
branch and an early access release will be made for people to try out the new features. This release candidate will be
polished in this branch, i.e. any bug fixes to early access version will go to this branch, increasing the `PATCH` version number
(e.g. from `V1.1.-0-ea` to `V1.1.-1-ea`). This change is then merged back to master.

  If there are more features to be published for people to try out, a new early access release will be created with a bump
in `MINOR` version number, resetting the `PATCH` version number to 0.

  This release channel is not meant for production use. Those who are interested to try out the latest (possibly unstable)
features of Addressbook should use this early access version.
- `stable` : Once `early-access` version has enough polished features to be included in the new version for
production use, it will be released as a stable version with the same version number as the `early-access` version,
excluding the `ea` mark of `early-access` version. If there is any hotfix to stable version, it will be done in this branch,
bumping up the `PATCH` version number (e.g. from `V1.1.-1-` to `V1.1.-2-`). This change is then merged to `early-access`
branch with a bump in `early-access` `PATCH` version number from whatever it is (e.g. from `V1.2.-0-ea` to `V1.2.-1-ea`)
and this will then be merged to `master`.

To illustrate, look at the diagram below.

<img src="images/Release Cycle.jpg" width="600">

Development in master does not stop, even after creating an early access release. Version does not matter as well in master.

On creating an early access release, merge the `master` branch to `early-access` branch and create a release (instruction below)
with the `PATCH` version of 0, like `V1.0.0ea` and `V1.1.0ea` in the diagram. If the release has a bug, it will be fixed in
`early-access` branch, and the bug fix needs to be merged back `master` branch as shown in `V1.0.1ea`.

When `early-access` version has a set of new features that is well polished, we will create a stable release by merging
`early-access` branch to `stable` branch and create a release. The version of the release uses the latest `early-access`
version without early access flag (from `V1.0.2ea` to `V1.0.2` and from `V1.1.2ea` to `V1.1.2` in the diagram).

If the stable release requires a bug fix, the bug fix will be done in `stable` branch. A re-release will be done with
a bump in `PATCH` version, as shown in `V1.0.3` in the diagram. This bug fix will be merged back to `early-access` branch
in which we will re-release the early access version with a bump in early access `PATCH` version, as shown in `V1.1.0ea`
to `V1.1.1ea` in the diagram (and not to `V1.0.3ea`). This bug fix will also be merged to `master` branch from
`early-access` branch.


References to how teams have multiple release channel:
- http://blog.atom.io/2015/10/21/introducing-the-atom-beta-channel.html
- https://docs.google.com/presentation/d/1uv_dNkPVlDFG1kaImq7dW-6PasJQU1Yzpj5IKG_2coA/present?slide=id.i0
- http://blog.rust-lang.org/2014/10/30/Stability.html

## How to create a release
If this is release of a new version, merge `master` branch to `early-access` branch then run the steps below. If this
is release to public on a polished early access version, merge `early-access` branch to `stable` branch then run the
steps below.

*In merging the branches to create a release, use `git merge --no-commit --no-ff` so that a merge commit won't be made,
 in which you can make relevant changes (for example changing version number and early access flag) before committing with
 the version of the software as the commit message.

0. **Pre-requisite** Run `gradle` task `createLauncherJar` under `release` category
  - This is to ensure that all binaries can be created successfully (i.e. no compile-time error).
  - If there is any compile-time error, resolve them first before continuing on the next step.
1. Update version in `MainApp` and in `build.gradle`. If this is an early access version, set `IS_EARLY_ACCESS` in `MainApp`
as `true` and add `ea` at the end of version in `build.gradle`.
2. Run `gradle` task `generateVersionData` under `release` category
  - The console will print a list of libraries which should be updated
3. Open `VersionData.json` and update the new fields accordingly
  - Put the link to download the new libraries. For now, we upload it to the new release we are going to create after this
  but the URL will follow GitHub release download link - `https://github.com/HubTurbo/addressbook/releases/download/<release version>/<filename>`.
  - Change the OS compatibility of the new libraries to ensure that only the libraries relevant to an OS will be loaded and checked
4. Commit and push the  files for release - name the commit `V<MAJOR>.<MINOR>.<PATCH>` (with suffix `ea` if it's an early access version)
  - This is so that the git tag that GitHub release creates will appropriately tag the commit with updated `VersionData.json`
5. Create a release in [GitHub](https://github.com/HubTurbo/addressbook/releases) and tag the corresponding branch (`early-access` or `stable`)
6. Run `gradle` task `createLauncherJar` under `release` category (this must be run again to use the updated `VersionData.json`)
7. Upload `addressbook.jar` to the latest release.
8. Upload the following as binaries to the (`resource` release)[https://github.com/HubTurbo/addressbook/releases/tag/resources]:
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

### generateVersionData
Generates the version data (VersionData.json) which will be used by user's application instance to know if it has an update
and what to update. It reads the dependencies of the latest version of addressbook and put those dependencies into the version
data. To make it easier for developer to update only things that get updated, it will use previous update data value for
libraries that do not change. Developers then need to update the new dependencies information (such as URL to download
the libraries and the OS that needed them) manually.

generateVersionData has its own source set which includes everything - main application and launcher - and its dependencies
are extended from main application compile dependencies. This is to make it easier for generateVersionData to read any
information it needs to create version data. The main class it uses is `updater/VersionDataGenerator.java`

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

### createLauncherJar
Create the packed JAR to be provided to user to use. The packed JAR contains the main app executable and all the
libraries needed for the main app executable to run. It will run the other tasks necessary to generate those files and
keep them as resources.

Launcher is defined into its own source set to make it easier to compile.

Currently, Launcher needs Jackson to parse the update data for dependency setting purposes. However, it is not compiled
to Launcher JAR; it will need `lib/[jackson].jar` to work. Those Jackson JARs are inside Launcher JAR as resource, though,
which will be unpacked anyway. Hence, JSON parsing in Launcher must be called only after it unpacks itself.

Launcher will launch the main application with `enable assertion` argument to enable assertion in production. On first
run, it will unpack the libraries JARs and the main app JAR then launch the main app. On the next runs, it acts as a
launcher to enable assertion in production.

## To be improved

### Automate generateVersionData
Ideally, we don't need to host those libraries JAR on our own in GitHub release. We can grab the JARs from the Maven repositories
that `gradle` uses to get those libraries. Unfortunately, the lack of documentation of `gradle` makes it impossible to
get the URL of those JAR in the Maven repositories, hence the manual need to update the download links and upload the new
libraries to GitHub release.

### GUI for generateVersionData
Instead of having to deal with text file of update data which is prone to error, we can have a GUI which generateUpdateData
uses to show what libraries have been changed in the latest version with fields to update the download links. Also,
we can have a dropdown option for the OS which the libraries are needed in so developers don't need to type them manually.

### Launcher dependencies
Currently Launcher only has Jackson dependency which is easy to manage on compiling side and easy to control on program flow
side. However, it will be better if dependencies of Launcher to be compiled (i.e. converted to classes) and packed together
into the JAR so that it does not need to depend on external JAR libraries which need to be unpacked before running
some methods. It's also safer when there is a call to a library method early in the program, say Apache IO on file opening.
In this case, use of ShadowJAR or tinkering around `gradle` system to compile libraries as classes into JAR should be considered.
