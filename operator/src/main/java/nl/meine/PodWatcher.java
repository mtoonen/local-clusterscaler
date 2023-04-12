package nl.meine;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.quarkus.runtime.StartupEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@ApplicationScoped
public class PodWatcher {

    private static Log log = LogFactory.getLog(PodWatcher.class);

    @Named("podClient")
    KubernetesClient client;

    @Inject
    LocalNodeResourceCache cache;

    @Inject
    LocalNodeScaler scaler;

    @Named("mediaNamespace") String namespace;

    void onStartup(@Observes StartupEvent _ev) {
        cache.listThenWatch();
        new Thread(this::podWatcher).start();
    }

    public void podWatcher() {
        log.error("lalala, niewu");
        cache.listThenWatch();

        log.error("Current running pods in 1" + namespace);
        List<Pod> pods = client.pods().inNamespace(namespace).list().getItems();

        log.error("Current running pods in 2" + namespace);
        for (Pod pod: pods) {
            log.debug("" + pod.getMetadata().getName());
        }

        client.pods().watch(new Watcher<Pod>() {

            @Override
            public void eventReceived(Watcher.Action action, Pod pod) {

                log.info("Received " + action + ", pod name " + pod.getMetadata().getName());

                switch(action){
                    case ADDED -> scaler.podAdded(pod);
                    case DELETED -> scaler.podRemoved(pod);
                }

            }

            @Override
            public void onClose(WatcherException e) {
                log.error("An error occured: ",e);
            }
        });
    }

}
