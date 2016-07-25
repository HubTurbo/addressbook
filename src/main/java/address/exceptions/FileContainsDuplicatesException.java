package address.exceptions;


import java.io.File;

public class FileContainsDuplicatesException extends Exception {

    private final File offender;

    public FileContainsDuplicatesException(File f) {
        offender = f;
    }

    @Override
    public String toString() {
        return "Duplicate data detected in file: " + offender.getName();
    }
}
