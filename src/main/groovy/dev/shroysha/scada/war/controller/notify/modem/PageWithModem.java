package dev.shroysha.scada.war.controller.notify.modem;

import dev.shroysha.scada.war.controller.NotificationSystem;
import dev.shroysha.scada.war.util.ServerProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PageWithModem implements Runnable, ReadListener {

    private final ServerProperties props;
    private final JobAckCodeGenerator jacg = new JobAckCodeGenerator();
    private final Logger log = Logger.getGlobal();
    private ModemConnector mc;
    private PagingPlug plug;
    private NotificationSystem notificationSystem;
    private ServerSocket pagingModuleServer;
    private Socket pagingModuleSocket;
    private boolean active = false;

    public PageWithModem() {
        super();
        log.log(Level.INFO, "-------Making New Page and Voice Properties-------");
        props = new ServerProperties();
        log.log(Level.INFO, "-------Properties Made-------");
        createNotificationSystem();
    }

    public static void main(String[] args) {
        JobAckCodeGenerator gen = new JobAckCodeGenerator();
        int code = gen.generateAckCode(0);
        System.out.println(code);
        System.out.println(gen.activeCodes.toString());
        gen.acknowledgeCode(code);
        System.out.println(gen.activeCodes.toString());
        code = gen.generateAckCode(5);
        int jobID = gen.getJobID(code);
        System.out.println(jobID);
        // gen.acknowledgeScadaID(0);
        System.out.println(gen.activeCodes.toString());
    }

    private void createNotificationSystem() {
        log.log(Level.INFO, "-------Making The GUI!-------");
        notificationSystem = new NotificationSystem(props);
        log.log(Level.INFO, "-------GUi Created!-------");
        notificationSystem
                .getNotificationSystemPanel()
                .getTabbedPane()
                .addTab("Phone", new ModemPanel(this));
    }

    private void initModem() {
        try {
            String ip = props.getModemIP();
            String port = "" + props.getModemPort();
            mc = new ModemConnector(ip, port);
            log.log(Level.INFO, "New Modem Connector created");
            mc.addReadListener(this);
            log.log(Level.INFO, "Made new Read Listener");
            mc.start();
            log.log(Level.INFO, "Started MC");
        } catch (IOException ex) {
            Logger.getLogger(PageWithModem.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Voice Modem ip and port incorrect");
        }
    }

    public void startPage(int jobID, String message) {
        try {
            plug.startPage(jobID, message);
            log.log(Level.INFO, "started page");
        } catch (IOException ex) {
            fix();
            startPage(jobID, message);
        }
    }

    public void stopPage(int jobID) {
        try {
            plug.stopPage(jobID);
        } catch (IOException ex) {
            fix();
            stopPage(jobID);
        }
    }

    public void acknowledgePage(int jobID) {
        try {
            plug.acknowledgePage(jobID);
        } catch (IOException ex) {
            fix();
            acknowledgePage(jobID);
        }
    }

    public int getStatus(int jobID) {
        try {
            return plug.getStatus(jobID);
        } catch (IOException ex) {
            fix();
            return getStatus(jobID);
        }
    }

    public String getAllActivePages() {
        try {
            return plug.getAllActivePages();
        } catch (IOException ex) {
            fix();
            return getAllActivePages();
        }
    }

    public void stopAllRunningPages() {
        try {
            plug.stopAllRunningPages();
        } catch (IOException ex) {
            fix();
            stopAllRunningPages();
        }
    }

    private void resetPagingModule() {
        stopPagingModule();
        startPagingModule();
    }

    private void startPagingModule() {
        new Thread(this).start();
    }

    private void stopPagingModule() {
        if (pagingModuleServer != null && !pagingModuleServer.isClosed()) {
            try {
                plug.stopAllRunningPages();
                pagingModuleServer.close();
                pagingModuleServer = null;
            } catch (IOException ex) {
                Logger.getLogger(PageWithModem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (pagingModuleSocket != null && !pagingModuleSocket.isClosed()) {
            try {
                pagingModuleSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(PageWithModem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public synchronized NotificationSystem getNotificationSystem() {
        return notificationSystem;
    }

    private void resetModemConnector() {
        stopModemConnector();
        startModemConnector();
    }

    private void stopModemConnector() {
        mc = null;
    }

    private void startModemConnector() {
        initModem();
    }

    public synchronized void start() {
        log.log(Level.INFO, "Preping the business for opening.");
        startModemConnector();
        log.log(Level.INFO, "Modem connector started");
        startPagingModule();
        active = true;
    }

    public void stop() {
        stopModemConnector();
        stopPagingModule();
        active = false;
    }

    public boolean isActive() {
        return active;
    }


    public void run() {
        try {
            // pagingSystem = new PagingSystem();
            int port = 7655;
            log.log(Level.INFO, "Starting ServerSocket");
            pagingModuleServer = new ServerSocket(port);
            log.log(Level.INFO, "We're open for business!");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Port 7655 is needed for this application to run.\n"
                            + "Stop the other process running on this port or contact developers.");
            return;
        }
        try {
            while (!pagingModuleServer.isClosed()) {
                pagingModuleSocket = pagingModuleServer.accept();
                try {
                    plug = new PagingPlug(pagingModuleSocket);
                    log.log(Level.INFO, "Created the paging plug!!!!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error opening streams, please try again");
                    Logger.getLogger(PageWithModem.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Paging System turned off");
        }
    }

    private void fix() {
        JOptionPane.showMessageDialog(
                null,
                "The paging module needs to connect to: 127.0.0.1:7655. Contact network administrator for further help.");
        System.exit(42);
    }

    public PagingPlug getPagingPlug() {
        return plug;
    }


    public void onRead(final String pinText) {
        Runnable r =
                () -> {
                    try {
                        if (plug == null) {
                            JOptionPane.showMessageDialog(
                                    null, "Paging system not connected. Please connect and click OK");
                            onRead(pinText);
                            return;
                        }
                        // JOptionPane.showMessageDialog(null, pinText);
                        int pin = Integer.parseInt(pinText);
                        int jobID = jacg.getJobID(pin);
                        jacg.acknowledgeCode(pin);
                        plug.acknowledgePage(jobID);
                    } catch (IOException ex) {
                        fix();
                        onRead(pinText);
                    }
                };
        new Thread(r).start();
    }

    private static class JobAckCodeGenerator {

        private final Random random;
        private final ArrayList<AckCode> activeCodes;

        public JobAckCodeGenerator() {
            super();
            random = new Random();
            activeCodes = new ArrayList<>();
        }

        public int generateAckCode(int jobID) {

            int ran;
            do {
                StringBuilder codeText = new StringBuilder();
                for (int i = 0; i < 4; i++) {
                    int randomInt = random.nextInt(9); // 0-8
                    randomInt++; // 1-9

                    codeText.append(randomInt);
                }

                ran = Integer.parseInt(codeText.toString());
            } while (randomUsed(ran));

            // now we have a random int that isn't used yet
            AckCode code = new AckCode(jobID, ran);
            activeCodes.add(code);
            return code.getAckCode();
        }

        private boolean randomUsed(int randomInt) {
            for (AckCode code : activeCodes) {
                if (code.getAckCode() == randomInt) {
                    return true;
                }
            }
            return false;
        }

        public int getAckCode(int jobID) {
            for (AckCode code : activeCodes) {
                if (code.jobID == jobID) {
                    return code.getAckCode();
                }
            }

            return -2;
        }

        public int getJobID(int ackCode) {
            for (AckCode code : activeCodes) {
                if (code.ackCode == ackCode) {
                    return code.jobID;
                }
            }

            return -2;
        }

        /*
         * Acknoledge the jobID
         */
    /*
    public void acknowledgeScadaID(int jobID) {
        for(AckCode code: activeCodes) {
            if(code.getScadaID() == jobID) {
                activeCodes.remove(code);
                return;
            }
        }
    }*/
        // cant do this anymore because there can be two acknowledgment codes for one scada site

        public void acknowledgeCode(int ackcode) {
            for (AckCode code : activeCodes) {
                if (code.getAckCode() == ackcode) {
                    activeCodes.remove(code);
                    return;
                }
            }
        }

        private static class AckCode {

            private final int jobID;
            private final int ackCode;

            public AckCode(int jobID, int ackCode) {
                super();
                this.jobID = jobID;
                this.ackCode = ackCode;
            }

            public int getAckCode() {
                return ackCode;
            }

            public int getJobID() {
                return jobID;
            }

            public String toString() {
                return "ACKCODE :" + ackCode + " ScadaID: " + jobID;
            }
        }
    }

    private class PagingPlug {

        private InputStream is;
        private OutputStream os;

        public PagingPlug(Socket socket) throws IOException {
            super();
            is = socket.getInputStream();
            os = socket.getOutputStream();
        }

        protected void startPage(int jobID, String message) throws IOException {
            int ackCode = jacg.generateAckCode(jobID);
            log.log(Level.INFO, "Generated Ack Code");
            String compose = "ST " + jobID + " " + message + " ACKCODE:" + ackCode;
            os.write(compose.getBytes());
            os.flush();
            // LoggingSystem.getLoggingSystem().alertAllLogListeners("Page sent: jobID-" + jobID + "
            // message-" + message);
        }

        protected void acknowledgePage(int jobID) throws IOException {
            // log.log(Level.INFO, "Clearing ScadaID {0} from the system", ""+jobID);
            String compose = "ACK " + jobID;
            os.write(compose.getBytes());
            os.flush();
            // LoggingSystem.getLoggingSystem().alertAllLogListeners("Acknowledgement received: jobID-" +
            // jobID);
        }

        protected void stopPage(int jobID) throws IOException {
            log.log(Level.INFO, "Stopped the page");
            String compose = "SP " + jobID;
            os.write(compose.getBytes());
            os.flush();
        }

        protected int getStatus(int jobID) throws IOException {
            String compose = "S " + jobID;
            os.write(compose.getBytes());
            os.flush();
            return Integer.parseInt(readBuffer());
        }

        protected String getAllActivePages() throws IOException {
            String compose = "AAP";
            os.write(compose.getBytes());
            os.flush();
            return readBuffer();
        }

        protected void stopAllRunningPages() throws IOException {
            String compose = "SPA";
            os.write(compose.getBytes());
            os.flush();
        }

        private String readBuffer() throws IOException {
            StringBuilder buffer = new StringBuilder();

            while (is.available() > 0) {
                int read = is.read();
                if (read == -1) {
                    throw new IOException("The connection was broken.");
                }
                buffer.append((char) read);
            }

            return buffer.toString();
        }
    }

    private class ModemPanel extends JPanel {

        private final JLabel modemIPLabel = new JLabel();
        private final JLabel modemPortLabel = new JLabel();
        private final JButton changeMIPButton = new JButton("Change");
        private final JButton changeMPButton = new JButton("Change");

        private final PageWithModem pwm;

        public ModemPanel(PageWithModem pwm) {
            super(new GridLayout(3, 2, 10, 10));
            this.pwm = pwm;
            init();
        }

        private void init() {
            this.setBorder(new EmptyBorder(10, 10, 10, 10));

            changeMIPButton.addActionListener(
                    ae -> {
                        props.setModemIP("");
                        resetModemConnector();
                        updateLabels();
                    });

            changeMPButton.addActionListener(
                    ae -> {
                        props.setModemPort(-1);
                        resetModemConnector();
                        updateLabels();
                    });

            this.add(modemIPLabel);
            this.add(changeMIPButton);

            this.add(modemPortLabel);
            this.add(changeMPButton);

            updateLabels();
        }

        private void updateLabels() {
            if (pwm != null) {
                modemIPLabel.setText("Phone Modem IP: " + props.getModemIP());
                modemPortLabel.setText("Phone Modem Port: " + props.getModemPort());
            }
        }
    }
}
