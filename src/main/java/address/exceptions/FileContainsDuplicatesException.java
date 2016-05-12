package address.exceptions;


public class FileContainsDuplicatesException extends Exception {

  @Override
  public String toString() {
    return "Duplicate data detected in file.";
  }
}
