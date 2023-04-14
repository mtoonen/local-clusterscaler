package nl.meine;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.ByteArrayOutputStream;

@ApplicationScoped
public class NodeTerminator {

    private final String[] commands = { "kubectl drain --ignore-daemonsets %s", "sudo shutdown now"};
    private final static Log log = LogFactory.getLog(NodeTerminator.class);

    public void shutdown(String username, String password,
                                           String host, int port, String nodeName) throws Exception {


        Session session = null;
        ChannelExec channel = null;

        try {
            session = new JSch().getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            for (String command: commands) {

                channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(String.format(command, nodeName));
                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                channel.setOutputStream(responseStream);
                channel.connect();

                while (channel.isConnected()) {
                    Thread.sleep(100);
                }

                String responseString = new String(responseStream.toByteArray());
                log.error(String.format("Executing %s. Received response: %s", command, responseString));
                System.out.println(responseString);
            }

        } finally {
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
    }
}
