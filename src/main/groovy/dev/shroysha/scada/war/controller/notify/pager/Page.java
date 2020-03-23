package dev.shroysha.scada.war.controller.notify.pager;

import dev.shroysha.scada.war.util.ServerProperties;
import dev.shroysha.scada.war.model.Employee;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Page {

    private final char CR = 0x0D;
    private final String formedMsg;
    private final PagingSystem ps;
    private final Employee employee;
    private final ServerProperties props;
    private InputStream is = null;
    private OutputStream os = null;
    private Socket socket = null;
    private String buffer = "";
    private boolean sentMessage = false;
    private boolean pageSent;
    private long startTime;
    private long currentTime;
    private boolean sawp;
    private boolean loggedOff;

    public Page(PagingSystem ps, Employee employee, String aMessage, ServerProperties props) {
        this.ps = ps;
        this.employee = employee;
        char ETX = 0x03;
        char STX = 0x02;
        formedMsg = "" + STX + employee.getPager() + CR + aMessage + CR + ETX;
        this.props = props;
    }

    public void start() throws IOException {
        int numTries = 0;
        setPagingProgressText("Sending page to " + employee.getName());
        setPagingProgress(0);
        run();
    }

    private void connect() throws IOException {
        socket = new Socket(props.getPagerIP(), props.getPagerPort());
        is = socket.getInputStream();
        os = socket.getOutputStream();
        sendCR();
        startTime = System.currentTimeMillis();
        sawp = false;

        setPagingProgress(25);

        alertAllLogListeners("Connected to paging main.java.server");

        // this.run();
        loggedOff = false;
    }

    private void sendCR() throws IOException {
        if (!socket.isClosed()) {
            write("" + CR);
        }
    }

    private void sendLoginAndMessage() throws IOException {
        char ESC = 0x1B;
        String everything = "" + ESC + (char) 0x050 + (char) 0x47 + (char) 0x31 + CR;
        write(everything);
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(Page.class.getName()).log(Level.SEVERE, null, ex);
        }
        write((formedMsg + calculateChecksum(formedMsg)));
        sentMessage = true;
        alertAllLogListeners("Logged on and sent message");
    }

    private void logoff() throws IOException {
        alertAllLogListeners("Logging off paging main.java.server");
        if (!socket.isClosed()) {
            // write(("CR" + EOT + CR));
            char EOT = 0x04;
            write("" + EOT + CR);
            // os.flush();
        }
    }

    private void disconnect() throws IOException {
        alertAllLogListeners("Disconnecting");
        socket.close();
        loggedOff = true;
    }

    private void reconnect() throws IOException {
        alertAllLogListeners("Starting reconnection");
        startTime = System.currentTimeMillis();
        disconnect();

        try {
            Thread.sleep(5000);
        } catch (Exception ex) {
        }

        connect();
    }

    private void respond(String recieved) throws IOException {
        if (recieved.contains("ID=")) {
            sendLoginAndMessage();
        } else if (recieved.contains("[p")) {
            sawp = true;
        } else {
            sendCR();
        }
    }

    public void run() throws IOException {
        while (!pageSent) {
            setPagingProgress(0);

            connect();

            WatchdogThread watchdog = new WatchdogThread();
            watchdog.start();

            try {
                while (!loggedOff) {
          /*currentTime = System.currentTimeMillis();
          if(currentTime - startTime > 5000)
              try
              {
                  numTries++;
                  reconnect();
              } catch (IOException ex)
              {
              Logger.getLogger(Page.class.getName()).log(Level.SEVERE, null, ex);
              } catch (InterruptedException ex)
              {
              Logger.getLogger(Page.class.getName()).log(Level.SEVERE, null, ex);
              }*/

                    if (is == null) {
                        continue;
                    }

                    int read = is.read();
                    if (read == -1) {
                        throw new IOException("Inputstream was closed");
                    }

                    try {
                        char temp = (char) read;

                        buffer += temp;
                        // System.out.println(temp);
                        if (temp == CR || buffer.contains("ID=")) {

                            setPagingProgress(50);

                            respond(buffer);
                            buffer = "";
                        }

                        char ACK = 0x06;
                        if (sentMessage && temp == ACK) {
                            alertAllLogListeners("Page succesfully sent");
                            setPagingProgress(75);

                            pageSent = true;
                            logoff();
                            loggedOff = true;
                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            Date date = new Date();
                            Logger.getGlobal()
                                    .log(Level.SEVERE, "Page: {0} Sent. " + dateFormat.format(date), formedMsg);
                        }

                    } catch (IOException e) {
                        disconnect(); // if it gives any kind of IOException, disconnect

                        System.out.println(e.toString());
                        Logger.getGlobal().log(Level.SEVERE, e.toString());
                    }
                }

            } catch (IOException ex) {
                // the watchdog closed the input stream
                // the loop will run again
                alertAllLogListeners("Watchdog closed stream, retrying");
            }

            watchdog.interrupt();
        }

        setPagingProgress(100);
        setPagingProgressText("No running pages");
        System.out.println("All should be well");
    }

    private String calculateChecksum(String toSend) {

        char[] bobints = toSend.toCharArray();
        int total = 0;

        for (char c : bobints) {
            total += c;
        }

        total %= 4096;

        StringBuilder hexString = new StringBuilder(Integer.toHexString(total).toUpperCase());

        while (hexString.length() < 3) {
            hexString.insert(0, "0");
        }

        int[] hexPlaces = new int[3];

        for (int i = 0; i < hexPlaces.length; i++) {
            hexPlaces[i] = Integer.parseInt(hexString.substring(i, i + 1), 16) + 0x30;
        }

        StringBuilder checkSum = new StringBuilder();

        for (int digit : hexPlaces) {
            checkSum.append((char) digit);
        }

        return checkSum.toString();
    }

    public boolean finished() {
        return pageSent;
    }

    private void setPagingProgressText(String text) {
        if (ps != null) {
            ps.getPagingProgressPanel().getLabel().setText(text);
        }
    }

    private void setPagingProgress(int progress) {
        if (ps != null) {
            ps.getPagingProgressPanel().getProgressBar().setValue(progress);
        }
    }

    private void alertAllLogListeners(String text) {
        if (ps != null) {
            ps.notifyAllLogListeners(text);
        }
    }

    private void write(String string) throws IOException {
    /*for(byte byt : string.getBytes()) {
        alertAllLogListeners("" + (int)byt);
    }*/
        os.write(string.getBytes());
        os.flush();
    }

    private class WatchdogThread extends Thread {


        public void run() {
            try {
                Thread.sleep(5000);
                disconnect(); // this will close the inputstream and result in the above thread being
                // restarted
            } catch (InterruptedException ex) {
                // the page is finished
            } catch (IOException ex) {
                // ps.errorRecovery(ex); let the thread deal with it
            }
        }
    }
}
