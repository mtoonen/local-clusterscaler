package nl.meine.models;


import io.fabric8.kubernetes.api.model.KubernetesResource;

public class LocalNodeStatus implements KubernetesResource  {

    public String getLabelSelector() {
        return labelSelector;
    }

    public void setLabelSelector(String labelSelector) {
        this.labelSelector = labelSelector;
    }

    private String labelSelector;

    @Override
    public String toString() {
        return "LocalNodeStatus{" +
                " , labelSelector='" + labelSelector + "'" +
                "}";
    }
}
