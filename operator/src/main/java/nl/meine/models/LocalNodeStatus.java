package nl.meine.models;


import io.fabric8.kubernetes.api.model.KubernetesResource;

public class LocalNodeStatus implements KubernetesResource  {
    private boolean running = false;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public String toString() {
        return "LocalNodeStatus{" +
                "running=" + running +
                '}';
    }
}
