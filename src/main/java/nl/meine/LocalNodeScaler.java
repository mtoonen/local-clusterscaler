package nl.meine;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class LocalNodeScaler {

    private static Log log = LogFactory.getLog(LocalNodeScaler.class);

    @Inject
    KubernetesClient client;

    @Inject
    LocalNodeResourceCache cache;

    public void podAdded(Pod pod){
        log.info("Pod added");
        if(mustScaleUp(pod)){
            scaleUp(pod);
        }
    }

    public void podRemoved(Pod pod){
        log.info("Pod removed");
    }

    private void scaleUp(Pod pod){
        log.info("Scaling up");
    }

    private void scaleDown(){

        log.info("Scaling down");
    }
    public boolean mustScaleUp(Pod newPod){
        // genoeg ruimte op nodes?
        // genoeg ruimte op specifieke architectuur
        //client.nodes().list().
        return false;
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
