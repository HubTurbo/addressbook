package address.model;

public interface CommandInfo {
    int getCommandId(); // id == command counter (starting from 1)
    String getName();
    String statusString();
}
