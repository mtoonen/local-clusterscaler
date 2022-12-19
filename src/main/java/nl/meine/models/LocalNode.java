package nl.meine.models;


import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("meinetoonen.nl")
@Version("v1")
public class LocalNode extends CustomResource<LocalNodeSpec, LocalNodeStatus> implements Namespaced { }