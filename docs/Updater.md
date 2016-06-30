# Updater
Updater has the main responsibility of updating the application to a newer version. In doing so,
it does several things:

- updates the application and the components it depends on, i.e. the libraries JAR that it uses
- maintains backups of the application so that user can use the application even if an update fails
- maintains dependencies of current version and backup versions and clean up dependencies no longer used

## How Updater updates the application
Updater runs when the application starts. It will work as per the diagram below.

<img src="images/How Updater Works.jpg" width="600">

Update to files will be applied when user closes the application. As a JAR file cannot modify itself,
a proxy to modify the main application JAR (`addressbook.jar`) is used, named `jarUpdater.jar`. `jarUpdater.jar` will
apply the update to files (in this case by replacing them with a newer version).

## Backups and Dependencies Maintenance
A backup of the application will be created before applying update, i.e. upon closing the application. Dependencies
of all backup versions are remembered so that they will not be deleted erroneously.

Several backups will be kept to allow user to roll back to older versions if update keeps failing. Once the amount of
backups exceeds the max number of backups stored (specified in `BackupHandler`), the oldest backup version will be
removed. If there are dependencies which are no longer used as the oldest backup version is removed, those dependencies
will be removed as well so as not to take up disk space unnecessarily.

Backups will be stored at the root folder with its version appended into the name, e.g. `addressbook_V0.1.0.jar`. To use
a backup version, open the JAR file of that backup version.