package address.sync.task;

import address.sync.RemoteManager;
import address.util.AppLogger;
import address.util.LoggerManager;

public class CreateAddressBookOnRemote extends RemoteTaskWithResult<Boolean> {
    private static final AppLogger logger = LoggerManager.getLogger(CreateAddressBookOnRemote.class);
    private final String addressBookName;

    public CreateAddressBookOnRemote(RemoteManager remoteManager, String addressBookName) {
        super(remoteManager);
        this.addressBookName = addressBookName;
    }

    @Override
    public Boolean call() throws Exception {
        logger.info("Creating new address book {} on remote", addressBookName);
        return remoteManager.createAddressBook(addressBookName);
    }
}
