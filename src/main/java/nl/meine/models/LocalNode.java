package nl.meine.models;


import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("meinetoonen.nl")
@Version("v1")
public class LocalNode extends CustomResource<LocalNodeSpec, LocalNodeStatus> implements Namespaced {
    @Override
    public boolean equals(Object o) {
        if(o instanceof LocalNode){
            return this.getMetadata().getUid().equals(((LocalNode) o).getMetadata().getUid());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getMetadata().getUid().hashCode();
    }
}