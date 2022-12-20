package nl.meine.models;

import io.fabric8.kubernetes.api.model.KubernetesResource;

public class LocalNodeSpec implements KubernetesResource {

    private String macAddress;
    private String name;

    private String type;

    private Integer cpu;

    private Integer memory;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCpu() {
        return cpu;
    }

    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public String toString() {
        return "LocalNodeSpec{" +
                "macAddress='" + macAddress + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", cpu=" + cpu +
                ", memory=" + memory +
                '}';
    }
}

