package nl.meine.models;

import io.fabric8.kubernetes.api.model.KubernetesResource;

public class LocalNodeSpec implements KubernetesResource {

    private String macAddress;
    private String name;

    private Architecture architecture;

    private String username;

    private String password;

    private boolean scaleDownProtected = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Architecture getArchitecture() {
        return architecture;
    }

    public void setArchitecture(Architecture architecture) {
        this.architecture = architecture;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isScaleDownProtected() {
        return scaleDownProtected;
    }

    public void setScaleDownProtected(boolean scaleDownProtected) {
        this.scaleDownProtected = scaleDownProtected;
    }

    @Override
    public String toString() {
        return "LocalNodeSpec{" +
                "macAddress='" + macAddress + '\'' +
                ", name='" + name + '\'' +
                ", architecture=" + architecture +
                '}';
    }
}

