package nl.meine;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import nl.meine.models.LocalNode;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class LocalNodeResourceCache {

    private final Map<String, LocalNode> cache = new ConcurrentHashMap<>();

    @Inject
    MixedOperation<LocalNode, KubernetesResourceList<LocalNode>, Resource<LocalNode>> localNodeClient;

    public LocalNode get(String uid) {
        return cache.get(uid);
    }

    public void listThenWatch() {

        try {

            // list

            localNodeClient
                    .list()
                    .getItems()
                    .forEach(resource -> {
                                cache.put(resource.getMetadata().getUid(), resource);
                                String uid = resource.getMetadata().getUid();
                            }
                    );

            // watch

            localNodeClient.watch(new Watcher<LocalNode>() {
                @Override
                public void eventReceived(Action action, LocalNode resource) {
                    try {
                        String uid = resource.getMetadata().getUid();
                        if (cache.containsKey(uid)) {
                            int knownResourceVersion = Integer.parseInt(cache.get(uid).getMetadata().getResourceVersion());
                            int receivedResourceVersion = Integer.parseInt(resource.getMetadata().getResourceVersion());
                            if (knownResourceVersion > receivedResourceVersion) {
                                return;
                            }
                        }
                        System.out.println("received " + action + " for resource " + resource);
                        if (action == Action.ADDED || action == Action.MODIFIED) {
                            cache.put(uid, resource);
                        } else if (action == Action.DELETED) {
                            cache.remove(uid);
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
}