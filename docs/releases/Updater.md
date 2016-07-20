# Updater
Compiled as a separate updater jar which contains key classes to perform updates - downloading of dependencies as well as update migration. It is typically named as `updater-<MAJOR>.<MINOR>.<PATCH>.jar`.

## How does the update work
Updater jar contains the `Updater` class which is required for making updates.
The main application has the updater jar as a dependency, so it is able to use its functionalities.

1. Upon instancing the `Updater` object and starting it, the updater will check for the latest version on the server.
It will then determine if the current application needs to be updated, download the new resources and produce a update specification file containing all the information required for the application to be migrated to the latest version.
 - If the application needs to be updated, and the updater jar is part of the update, the main application will attempt to upgrade the updater jar immediately after it finishes its job and closes
    - We cannot let the `Updater` itself to perform this, because on Windows, the updater jar cannot replace itself when it is running
 - Therefore, the updater jar will exclude the information about itself from the update specification file.

2. Upon the next initialization of the application through the launcher, the launcher will check if there are pending updates (from the update specification file), and does the upgrade if required.
