package nl.meine;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.runtime.RawExtension;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import nl.meine.models.Architecture;
import nl.meine.models.LocalNode;
import nl.meine.models.LocalNodeSpec;
import nl.meine.models.LocalNodeStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class LocalNodeResourceCache {
    private static Log log = LogFactory.getLog(LocalNodeScaler.class);

    private boolean ready = false;

    private final Map<String, LocalNode> cache = new ConcurrentHashMap<>();
    private final Set<String> nodeUUIDs = new HashSet<>();

    private final Map<Architecture, Set<LocalNode>> nodesByArchitecture = new ConcurrentHashMap<>();

    @Inject
    MixedOperation<LocalNode, KubernetesResourceList<LocalNode>, Resource<LocalNode>> localNodeClient;

    @Inject
    KubernetesClient client;

    public LocalNode get(String uid) {
        return cache.get(uid);
    }

    public Set<LocalNode> getByArchitecture(Architecture arch) {
        return nodesByArchitecture.get(arch);
    }

    public void listThenWatch() {

        try {

            // list

            localNodeClient
                    .list()
                    .getItems()
                    .forEach(resource -> {

                                String name = resource.getMetadata().getName();

                                if (cache.containsKey(name)) {
                                    int knownResourceVersion = Integer.parseInt(cache.get(name).getMetadata().getResourceVersion());
                                    int receivedResourceVersion = Integer.parseInt(resource.getMetadata().getResourceVersion());
                                    if (knownResourceVersion > receivedResourceVersion) {
                                        return;
                                    }
                                }
                                cache.put(name, resource);
                                Object lnsObj = resource.getSpec();
                                LocalNodeSpec lns = convertSpec((RawExtension) lnsObj);
                                resource.setSpec(lns);

                                LocalNodeStatus status = new LocalNodeStatus();
                                status.setRunning(isRunning(name));
                                resource.setStatus(status);

                                Architecture architecture = lns.getArchitecture();
                                nodesByArchitecture.putIfAbsent(architecture, new HashSet<>());
                                nodesByArchitecture.get(architecture).add(resource);
                                log.info("LocalNode added: " + name);
                            }
                    );
            log.info("Initial loading of resources completed");
            ready = true;

            // watch

            localNodeClient.watch(new Watcher<LocalNode>() {
                @Override
                public void eventReceived(Action action, LocalNode resource) {
                    try {
                        Object lnsObj = resource.getSpec();
                        LocalNodeSpec lns = convertSpec((RawExtension) lnsObj);
                        resource.setSpec(lns);

                        String name = resource.getSpec().getName();

                        LocalNodeStatus status = new LocalNodeStatus();
                        status.setRunning(isRunning(name));
                        resource.setStatus(status);

                        Architecture architecture = resource.getSpec().getArchitecture();
                        if (cache.containsKey(name)) {
                            int knownResourceVersion = Integer.parseInt(cache.get(name).getMetadata().getResourceVersion());
                            int receivedResourceVersion = Integer.parseInt(resource.getMetadata().getResourceVersion());
                            if (knownResourceVersion > receivedResourceVersion) {
                                return;
                            }
                        }
                        System.out.println("received " + action + " for resource " + resource);
                        if (action == Action.ADDED || action == Action.MODIFIED) {
                            cache.put(name, resource);
                            nodesByArchitecture.putIfAbsent(architecture, new HashSet<>()).add(resource);
                        } else if (action == Action.DELETED) {
                            nodesByArchitecture.get(architecture).remove(resource);
                            cache.remove(name);
                        } else {
                            System.err.println("Received unexpected " + action + " event for " + resource);
                            System.exit(-1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }

                @Override
                public void onClose(WatcherException cause) {
                    cause.printStackTrace();
                    System.exit(-1);
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private LocalNodeSpec convertSpec(RawExtension re) {
        Map<String, String> map = (Map) re.getValue();
        LocalNodeSpec lns = new LocalNodeSpec();
        lns.setArchitecture(Architecture.valueOf(map.get("architecture")));
        lns.setPassword(map.get("password"));
        lns.setIpAddress(map.get("ipAddress"));
        lns.setUsername(map.get("username"));
        if (map.containsKey("scaleDownProtected")) {
            Object a = map.get("scaleDownProtected");
            lns.setScaleDownProtected((boolean) a);
        } else {
            lns.setScaleDownProtected(false);
        }
        lns.setName(map.get("name"));
        lns.setMacAddress(map.get("macAddress"));

        return lns;
    }

    private boolean isRunning(String name) {
        boolean n = client.nodes().list().getItems().stream().noneMatch(node -> node.getMetadata().getName().equals(name));
        return !n;
    }

    public boolean isReady() {
        return ready;
    }
}