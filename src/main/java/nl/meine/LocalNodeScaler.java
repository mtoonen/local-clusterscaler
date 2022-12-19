package nl.meine;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics;
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
public class LocalNodeScaler {

    private static Log log = LogFactory.getLog(LocalNodeScaler.class);

    @Inject
    KubernetesClient client;


    @Inject
    LocalNodeResourceCache cache;

    @Named("namespace") String namespace;

    void onStartup(@Observes StartupEvent _ev) {
        //new Thread(this::runWatch).start();
        new Thread(this::podWatcher).start();
    }

    private void runWatch() {
        cache.listThenWatch();
    }


    public void podWatcher() {
        cache.listThenWatch();
        List<Pod> pods = client.pods().inNamespace(namespace).list().getItems();

        log.debug("Current running pods in " + namespace);
        for (Pod pod: pods) {
            log.debug("" + pod.getMetadata().getName());
        }

        client.pods().watch(new Watcher<Pod>() {

            @Override
            public void eventReceived(Watcher.Action action, Pod pod) {

                log.info("Received " + action + ", pod name " + pod.getMetadata().getName());

                if (action == Watcher.Action.ADDED) {

                    String podName = pod.getMetadata().getName();

                    try {
                        Thread.sleep(5 * 1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }

            }

            @Override
            public void onClose(WatcherException e) {

            }
        });


    }


    private void getMetrics()  {

        io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetricsList s = client.top().nodes().metrics();


        for (NodeMetrics item : s.getItems()) {

            System.out.println(item.getMetadata().getName());
            System.out.println("------------------------------");
            for (String key : item.getUsage().keySet()) {
                System.out.println("\t" + key);

                System.out.println("\t" + item.getUsage().get(key));
            }
            System.out.println();
        }

    }
}
