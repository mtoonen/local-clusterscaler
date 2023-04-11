package nl.meine;

import io.netty.handler.codec.http.HttpMethod;
import nl.meine.models.LocalNode;
import nl.meine.models.LocalNodeSpec;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpHeaders;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class ScalerClient {

    @Inject
    private WakeOnLan waker;

    @Inject
    private NodeTerminator arnold;
    public  String endpoint = "192.168.68.117:8080";

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

    public String up(LocalNode ln){
        URL url = null;
        try {
//
//            Map<String, String> parameters = new HashMap<>();
//            parameters.put("macAddress", ln.getSpec().getMacAddress());

            waker.wake(ln.getSpec().getMacAddress());
            /*
            String params = ParameterStringBuilder.getParamsString(parameters);
            url = new URL("http://" + endpoint + "/scaler/up?"+params);
            System.out.println(url.toString());

            StringBuilder result = new StringBuilder();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    result.append(line);
                }
            }
            String s = result.toString();
            System.out.println(s);*/
            return "maybe";
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void down(LocalNode ln) throws Exception {
        arnold.shutdown(ln.getSpec().getUsername(),ln.getSpec().getPassword(), ln.getSpec().getIpAddress(), 22);
    }

    public class ParameterStringBuilder {
        public static String getParamsString(Map<String, String> params)
                throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                result.append("&");
            }

            String resultString = result.toString();
            return resultString.length() > 0
                    ? resultString.substring(0, resultString.length() - 1)
                    : resultString;
        }
    }
}
