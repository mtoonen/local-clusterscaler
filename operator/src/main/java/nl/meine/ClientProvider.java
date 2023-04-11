package nl.meine;


import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import nl.meine.models.LocalNode;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ClientProvider {

    @Produces
    @Singleton
    @Named("namespace")
    String findNamespace() throws IOException {
        try {
            return new String(Files.readAllBytes(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/namespace")));
        } catch (Exception e) {
            return "default";
        }
    }

    @Produces
    @Singleton
    @Named("mediaNamespace")
    String findMediaNamespace() throws IOException {
        return "media";
    }

    @Produces
    @Singleton
    @Named("nodewatcher")
    KubernetesClient newClient(@Named("namespace") String namespace) {
/*
        Config config = new ConfigBuilder().withNamespace(namespace).build();
        return new KubernetesClientBuilder().withConfig(config).build();*/
        return new DefaultKubernetesClient().inNamespace(namespace);
    }

    @Produces
    @Singleton
    @Named("podClient")
    KubernetesClient podClient(@Named("mediaNamespace") String namespace) {
/*
        Config config = new ConfigBuilder().withNamespace(namespace).build();
        return new KubernetesClientBuilder().withConfig(config).build();*/
        return new DefaultKubernetesClient().inNamespace(namespace);
    }

    @Produces
    @Singleton
    MixedOperation<LocalNode, KubernetesResourceList<LocalNode>, Resource<LocalNode>> provideLocalNodeClient(@Named("nodewatcher")KubernetesClient defaultClient) {
        MixedOperation<LocalNode, KubernetesResourceList<LocalNode>, Resource<LocalNode>> cronTabClient = defaultClient.resources(LocalNode.class);
        return cronTabClient;
    }
}