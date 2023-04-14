package nl.meine;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics;
import io.fabric8.kubernetes.client.KubernetesClient;
import nl.meine.models.Architecture;
import nl.meine.models.LocalNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@ApplicationScoped
public class LocalNodeScaler {

    private static Log log = LogFactory.getLog(LocalNodeScaler.class);

    @Inject
    @Named("nodewatcher")
    KubernetesClient client;

    @Inject
    LocalNodeResourceCache cache;

    @Inject
    ScalerClient scaler;

    public void podAdded(Pod pod) {
        log.info("Cache status: " + cache.isReady());
        if (cache.isReady()) {
            doPodAdded(pod);
        } else {
            log.info("Cache not ready");
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

            exec.schedule(() -> podAdded(pod), 1, TimeUnit.SECONDS);
        }

    }

    private void doPodAdded(Pod pod) {
        log.info("Pod added");
        if (mustScaleUp(pod)) {
            scaleUp(pod);
        } else {
            log.info("Sufficient resources");
        }
    }

    public void podRemoved(Pod pod){
        log.info("Pod removed");
        String owningNodeName = pod.getSpec().getNodeName();
        int a = 0;

        List<Pod> podsWithRequestedArchitecture = client
                .pods()
                .inAnyNamespace()
                .withField("spec.nodeName", owningNodeName)
                .list().getItems().stream()
                .filter(p -> p.getSpec().getNodeSelector().get("kubernetes.io/arch") != null)
                .toList();

        if(podsWithRequestedArchitecture.isEmpty()){
            log.info(String.format("No more pods needed explicitely on node %s. Shutdown node %s", owningNodeName, owningNodeName));
            LocalNode ln = cache.get(owningNodeName);
            try {
                scaler.down(ln);
            } catch (Exception e) {
                log.error("Cannot scale down node " + owningNodeName, e);
            }
        }else{
            String pods = podsWithRequestedArchitecture.stream().map(pod1 -> pod1.getMetadata().getName()).collect(Collectors.joining(","));
            log.info(String.format("Pods existing on node %s that requested the architecture of the node: %s", owningNodeName, pods));
        }

        int b = 1;
    }

    private void scaleUp(Pod pod) {
        log.info("Scaling up");
        LocalNode ln = getNodeToScale(pod);
        log.info("Scaling localnode: " + ln);
        if (ln != null) {
            scaler.up(ln);
        } else {
            log.error("Cannot scale due to too few available nodes");
        }
    }


    private boolean mustScaleUp(Pod newPod) {

        if (checkGlobal(newPod)) {
            log.info("Globally too few resources");
            return true;
        }
        if (checkArchitecture(newPod)) {
            log.info("Too few resources on specific architecture");
            return true;
        }
        return false;
    }

    private boolean checkGlobal(Pod newPod) {
        double memoryToBeRequested = getPodResource(newPod, "memory").doubleValue();
        double cpuToBeRequested = getPodResource(newPod, "cpu").doubleValue();
        Architecture architecture = Architecture.valueOf(newPod.getSpec().getNodeSelector().getOrDefault("kubernetes.io/arch", Architecture.arm64.toString()));

        log.info(String.format("Pod %s requesting %f cpu and %f memory on architecture %s.", newPod.getMetadata().getName(), cpuToBeRequested, memoryToBeRequested, architecture));

        AtomicReference<Double> totalCpuNode = new AtomicReference<>(0.0);
        AtomicReference<Double> totalMemoryNode = new AtomicReference<>(0.0);
        client.nodes().list().getItems().forEach(n -> {
            totalMemoryNode.updateAndGet(v -> Double.valueOf((v + getAvailableResourceNode(n, "memory").doubleValue())));
            totalCpuNode.updateAndGet(v -> Double.valueOf((v + getAvailableResourceNode(n, "cpu").doubleValue())));
        });

        log.info(String.format("Currently available on nodes: %f memory, %f cpu", totalMemoryNode.get(), totalCpuNode.get()));

        if (mustScale(memoryToBeRequested, totalMemoryNode.get(), cpuToBeRequested, totalCpuNode.get())) {
            return true;
        }
        return false;
    }

    private boolean checkArchitecture(Pod newPod) {
        Architecture architecture = Architecture.valueOf(newPod.getSpec().getNodeSelector().getOrDefault("kubernetes.io/arch", Architecture.arm64.toString()));
        Set<LocalNode> nodes = cache.getByArchitecture(architecture);
        if (nodes == null || nodes.size() == 0) {
            return false;
        }
        double memoryToBeRequested = getPodResource(newPod, "memory").doubleValue();
        double cpuToBeRequested = getPodResource(newPod, "cpu").doubleValue();

        AtomicReference<Double> totalCpuNode = new AtomicReference<>(0.0);
        AtomicReference<Double> totalMemoryNode = new AtomicReference<>(0.0);
        nodes.forEach(localNode -> {
            if (localNode.getStatus().isRunning()) {
                Node n = getNode(localNode.getSpec().getName());
                if (n != null) {
                    totalMemoryNode.updateAndGet(v -> Double.valueOf((v + getAvailableResourceNode(n, "memory").doubleValue())));
                    totalCpuNode.updateAndGet(v -> Double.valueOf((v + getAvailableResourceNode(n, "cpu").doubleValue())));
                }
            }
        });

        log.info(String.format("Currently available on nodes with arch %s: %f memory, %f cpu", architecture, totalMemoryNode.get(), totalCpuNode.get()));

        if (mustScale(memoryToBeRequested, totalMemoryNode.get(), cpuToBeRequested, totalCpuNode.get())) {
            return true;
        }
        return false;
    }


    private boolean mustScale(double memoryRequested, double memoryAvailable, double cpuRequested, double cpuAvailable) {
        if ((memoryAvailable - memoryRequested) < 0) {
            log.info("Memory insufficient");
            return true;
        }
        if ((cpuAvailable - cpuRequested) < 0) {
            log.info("CPU insufficient");
            return true;
        }
        return false;
    }

    private Node getNode(String name) {
        List<Node> ns = client.nodes().list().getItems().stream().filter(node -> node.getMetadata().getName().equals(name)).collect(Collectors.toList());
        return ns.size() > 0 ? ns.get(0) : null;
    }


    private BigDecimal getPodResource(Pod pod, String resource) {
        BigDecimal total = new BigDecimal(0.0);
        for (Container c : pod.getSpec().getContainers()) {
            Quantity q = c.getResources().getRequests().get(resource);
            if (q != null) {
                BigDecimal temp = q.getNumericalAmount();
                total = total.add(temp);
            }
        }
        return total;
    }

    private BigDecimal getAvailableResourceNode(Node node, String resource) {
        return node.getStatus().getAllocatable().get(resource).getNumericalAmount();
    }

    private LocalNode getNodeToScale(Pod pod) {
        Architecture architecture = Architecture.valueOf(pod.getSpec().getNodeSelector().getOrDefault("kubernetes.io/arch", Architecture.arm64.toString()));

        Set<LocalNode> localNodes = cache.getByArchitecture(architecture);
        LocalNode nodeToScale = null;
        for (LocalNode ln : localNodes) {
            if (!ln.getStatus().isRunning()) {
                nodeToScale = ln;
                break;
            }
        }
        return nodeToScale;
    }

    private void getMetrics() {

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
