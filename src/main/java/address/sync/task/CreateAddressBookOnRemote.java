package address.sync.task;

import address.sync.RemoteManager;

import java.util.concurrent.Callable;

public class CreateAddressBookOnRemote implements Callable<Boolean> {
    private final RemoteManager remoteManager;
    private final String addressBookName;

    public CreateAddressBookOnRemote(RemoteManager remoteManager, String addressBookName) {
        this.remoteManager = remoteManager;
        this.addressBookName = addressBookName;
    }

    @Override
    public Boolean call() throws Exception {
        return remoteManager.createAddressBook(addressBookName);
    }
}
