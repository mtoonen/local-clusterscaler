package nl.meine;

import nl.meine.models.LocalNode;
import nl.meine.models.LocalNodeSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class ScalerClient {
    private final static Log log = LogFactory.getLog(ScalerClient.class);

    @Inject
    NodeTerminator arnold;

    @Inject
    LocalNodeResourceCache cache;

    @Inject
    @Named("scalerEndpoint")
    String endpoint;

    public static void main(String[] args) {
        String mac = "7C:10:C9:B8:32:C5";
        ScalerClient sc = new ScalerClient();
        sc.endpoint = "localhost:8080";
        LocalNode ln = new LocalNode();
        LocalNodeSpec lns = new LocalNodeSpec();
        lns.setMacAddress(mac);
        ln.setSpec(lns);
        sc.up(ln);
    }

    public void up(LocalNode ln) {
        URL url;
        try {

            Map<String, String> parameters = new HashMap<>();
            parameters.put("macAddress", ln.getSpec().getMacAddress());

            String params = ParameterStringBuilder.getParamsString(parameters);
            url = new URL("http://" + endpoint + "/scaler/up?" + params);

            StringBuilder result = new StringBuilder();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    result.append(line);
                }
            }
            String s = result.toString();
            log.info("Scaling up returned: " + s);
            cache.setLocalNodeRunning(ln, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void down(LocalNode ln) throws Exception {
        arnold.shutdown(ln.getSpec().getUsername(), ln.getSpec().getPassword(), ln.getSpec().getIpAddress(), 22, ln.getSpec().getName());
        cache.setLocalNodeRunning(ln, false);
    }

    public static class ParameterStringBuilder {
        public static String getParamsString(Map<String, String> params)
                throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                result.append("&");
            }

            String resultString = result.toString();
            return resultString.length() > 0
                    ? resultString.substring(0, resultString.length() - 1)
                    : resultString;
        }
    }
}
