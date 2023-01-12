package nl.meine.models;

public enum Architecture {

    amd64("amd64"),

    arm64("arm64");

    Architecture(String architecture) {
        this.value = architecture;
    }

    private String value;

    @Override
    public String toString() {
        return value;
    }
}
