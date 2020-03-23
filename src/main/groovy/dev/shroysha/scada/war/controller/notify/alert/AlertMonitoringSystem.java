package dev.shroysha.scada.war.controller.notify.alert;

import dev.shroysha.scada.war.model.AlertListener;
import dev.shroysha.scada.war.model.LogListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class AlertMonitoringSystem {

    private static final int BADSYNTAX = -2;
    private static final int SUCCESS = 1;
    private static final int EXCEPTION = 2;
    private static final int NOTINSYSTEM = 3;
    private static final String AAP = "AAP"; // all active pages
    private static final String STATUS = "S";
    private static final String START = "ST"; // start paging
    private static final String STOP = "SP"; // stop paging
    private static final String ACKNOWLEDGE = "ACK";
    private static final String STOPALL = "SPA";
    private static final int ALREADY = 1;
    private static final int PENDING = 2;
    private final AlertMonitoringPanel parent;
    private final Logger log = Logger.getGlobal();
    private final Stack<AlertListener> alertListeners = new Stack<>();
    private final Stack<LogListener> logListeners = new Stack<>();
    private final ArrayList<Alert> activeAlerts = new ArrayList<>();
    private final Alert[] pastAlerts = new Alert[50];
    private AlertMonitorThread amt;
    private AlertDispatchThread dispatch;

    public AlertMonitoringSystem() {
        super();

        amt = new AlertMonitorThread(this);
        amt.start();

        parent = new AlertMonitoringPanel(this);
    }

    public void addAlertListener(AlertListener listener) {
        alertListeners.add(listener);
    }

    public void removeAlertListner(AlertListener listener) {
        alertListeners.remove(listener);
    }

    public void addLogListener(LogListener listener) {
        logListeners.add(listener);
    }

    public void removeLogListner(LogListener listener) {
        logListeners.remove(listener);
    }

    private void alertAllLogListeners(String log) {
        for (LogListener listener : logListeners) {
            listener.onLog(log);
        }
    }

    private void alertAllAlertListeners(Alert alert) {
        for (AlertListener listener : alertListeners) {
            listener.alertReceived(alert); // go through all of the listners and tell them the alert
        }
    }

    public JPanel getAlertMonitoringPanel() {
        return parent;
    }

    public synchronized int doTask(String task) {
        try {
            String[] split = task.split(" ", 2); // split by spaces
            String command = split[0];
            String rest = "";
            if (split.length > 1) {
                rest = split[1].trim();
            }

            switch (command) {
                case STATUS: {
                    int jobID = Integer.parseInt(rest);
                    return getStatus(jobID);
                }
                case START:
                    Alert alert = parseAlert(rest);
                    try {
                        activeAlerts.add(alert);
                        // alertAllAlertListeners(alert);

                        makeSurePageThreadIsRunning();

                        alert.setNextAlertTime(Calendar.getInstance());
                        alertAllLogListeners("Created alert: " + alert.toString());

                        return SUCCESS;
                    } catch (Exception ex) {
                        alertAllLogListeners(ex.getClass().getName() + ": " + ex.getMessage());
                        log.log(Level.INFO, ex.getMessage());
                        Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
                        return EXCEPTION;
                    }

                case STOP:
                    try {
                        int jobID;
                        try {
                            jobID = Integer.parseInt(rest);
                        } catch (Exception ex) {
                            Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
                            return BADSYNTAX;
                        }

                        int index = searchFor(jobID);
                        if (index == -1) {
                            return NOTINSYSTEM;
                        }

                        Alert remove =
                                activeAlerts.remove(
                                        index); // and since arraylist works on .equals(), it will remove the active alert
                        // that has the jobid
                        remove.acknowledge();
                        addToPastAlerts(remove);
                        alertAllLogListeners("Stopped alert: " + remove.toString());
                        return SUCCESS;
                    } catch (Exception ex) {
                        Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
                        return EXCEPTION;
                    }

                case ACKNOWLEDGE: {

                    int jobID;
                    try {
                        jobID = Integer.parseInt(rest);
                    } catch (Exception ex) {
                        Logger.getGlobal().log(Level.SEVERE, "Bad syntax n acknowledgement", ex);
                        return BADSYNTAX;
                    }

                    int index = searchFor(jobID);

                    if (index == -1) {
                        return NOTINSYSTEM;
                    }

                    Alert remove =
                            activeAlerts.remove(
                                    index); // and since arraylist works on .equals(), it will remove the active alert

                    // that has the jobid
                    remove.acknowledge();
                    addToPastAlerts(remove);
                    alertAllLogListeners("Acknowledged alert: " + remove.toString());
                    return SUCCESS;

                }
                case STOPALL:
                    try {
                        for (Alert nextAlert : activeAlerts) {
                            nextAlert.acknowledge();
                        }
                        activeAlerts.clear();
                        alertAllLogListeners("Stopped all alerts");
                        return SUCCESS;
                    } catch (Exception ex) {
                        alertAllLogListeners("EXCEPTION");
                        Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
                        return EXCEPTION;
                    }

                default:
                    // syntax is incorrect
                    return BADSYNTAX;
            }
        } catch (Exception ex) {
            alertAllLogListeners(ex.getClass().getName() + ": " + ex.getMessage());
            return BADSYNTAX;
        }
    }

    private Alert parseAlert(String alertText) {
        String[] split = alertText.split(" ", 2);
        int jobID = Integer.parseInt(split[0]);
        String message = split[1];
        return new Alert(jobID, message);
    }

    private int searchFor(int jobID) {
        for (int i = 0; i < activeAlerts.size(); i++) {
            Alert alert = activeAlerts.get(i);
            if (alert.getJobID() == jobID) {
                return i;
            }
        }

        return -1;
    }

    private String getAllAlertText() {
        StringBuilder alertText = new StringBuilder();
        for (int i = 0; i < activeAlerts.size(); i++) {
            alertText.append(activeAlerts.get(i));
            if (i != activeAlerts.size() - 1) {
                alertText.append("\n");
            }
        }
        return alertText.toString();
    }

    private int getStatus(int jobID) {
        // check if already acknowledged
        boolean hitNull = false;
        for (int i = 0; i < pastAlerts.length && !hitNull; i++) {
            Alert alert = pastAlerts[i];
            if (alert == null) {
                hitNull = true;
            } else {
                if (jobID == alert.getJobID()) {
                    return ALREADY;
                }
            }
        }

        for (Alert alert : activeAlerts) {
            if (alert.getJobID() == jobID) {
                return PENDING;
            }
        }

        return NOTINSYSTEM;
    }

    private void addToPastAlerts(Alert alert) {
        // move all to right
        if (pastAlerts.length - 1 >= 0) System.arraycopy(pastAlerts, 0, pastAlerts, 1, pastAlerts.length - 1);
        pastAlerts[0] = alert;
    }

    private void makeSurePageThreadIsRunning() {
        if (dispatch == null || !dispatch.isAlive()) {
            dispatch = new AlertDispatchThread();
            dispatch.start();
        }
    }

    public static class AlertMonitoringPanel extends JPanel implements LogListener {

        private final AlertMonitoringSystem ams;

        private JTextArea logArea;

        protected AlertMonitoringPanel(AlertMonitoringSystem aThis) {
            super();
            ams = aThis;
            init();
        }

        private void init() {
            this.setBorder(new EmptyBorder(10, 10, 10, 10));
            this.setLayout(new BorderLayout());

            logArea = new JTextArea();
            logArea.setEditable(false);

            JScrollPane scroller = new JScrollPane(logArea);

            this.add(scroller, BorderLayout.CENTER);

            ams.addLogListener(this);
        }

        private void log(String toLog) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            String formattedDate = sdf.format(Calendar.getInstance().getTime());
            logArea.append(toLog + " at " + formattedDate + "\n");
        }


        public void onLog(String logText) {
            log(logText);
        }
    }

    private static class ErrorLoggingThread extends Thread {


        public UncaughtExceptionHandler getUncaughtExceptionHandler() {
            return (t, e) -> {
                Logger.getGlobal().log(Level.SEVERE, null, e);
                makeGUI(t, e);
            };
        }

        private void makeGUI(Thread t, Throwable e) {

            JFrame frame = new JFrame("ALERT: Uncaught Exception");

            frame.getContentPane().setLayout(new BorderLayout());

            JTextArea area = new JTextArea();
            area.setWrapStyleWord(true);
            area.setLineWrap(true);
            area.setEditable(false);

            area.append(
                    "There was an uncaught exception in a thread.\nThis is a serious problem. Please report this to Specialized Programming LLC\n");
            area.append("Exception in " + t.getClass().getName() + "\n");
            area.append("Exception type: " + e.getClass().getName() + "\n");

            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement ele : stackTrace) {
                area.append(ele.toString() + "\n");
            }

            frame.add(area, BorderLayout.CENTER);
            frame.setSize(400, 400);
            frame.setVisible(true);
        }
    }

    private class AlertDispatchThread extends ErrorLoggingThread {

        public AlertDispatchThread() {
            super();
        }


        public void run() {
            while (!activeAlerts.isEmpty()) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex);
                }

                for (int i = 0; i < activeAlerts.size(); i++) {
                    try {
                        Alert alert = activeAlerts.get(i);

                        if (alert.isReadyToAlert()) {

                            alert.incrementTimesPaged();

                            alertAllAlertListeners(alert); // page employees

                            Calendar cal = Calendar.getInstance();
                            cal.add(Calendar.MINUTE, 15); // add fifteen minutes.
                            alert.setNextAlertTime(cal);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        log.log(Level.SEVERE, e.getMessage());
                        i = activeAlerts.size() + 1;
                    }
                }
            }
        }
    }

    private class AlertMonitorThread extends ErrorLoggingThread {

        private final AlertMonitoringSystem ams;

        private Socket socket = null;
        private InputStream is = null;
        private OutputStream os = null;

        public AlertMonitorThread(AlertMonitoringSystem ams) {
            super();
            this.ams = ams;
        }

        public void run() {

            try {
                String ip = "127.0.0.1";
                int port = 7655;

                socket = new Socket(ip, port);

                is = socket.getInputStream();
                os = socket.getOutputStream();

                //noinspection InfiniteLoopStatement
                while (true) {

                    StringBuilder buffer = new StringBuilder();
                    do {
                        int read = is.read();
                        if (read == -1) {
                            throw new IOException("The connection was broken");
                        }
                        buffer.append((char) read);

                    } while (is.available() > 0);

                    if (buffer.toString().equals(AAP)) {
                        os.write(getAllAlertText().getBytes());
                        os.flush();
                    } else {
                        log.log(Level.FINE, "Received: " + buffer);
                        alertAllLogListeners("Received: " + buffer);
                        int toWrite = doTask(buffer.toString());
                        write(toWrite);
                        // alertAllLogListeners("" + toWrite);
                        // alertAllLogListeners("Did stuff");
                    }
                }
            } catch (Exception ex) {
        /*
        if(amt != this) {
            System.out.println("User wanted to change something");
            return;
        }*/

                System.out.println(ex.getClass());

                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex1) {
                        Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }

                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ex1) {
                        Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }

                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ex1) {
                        log.log(Level.INFO, ex1.getMessage());
                    }
                }

                if (!ex.getClass().getName().contains("ConnectException")) {
                    Logger.getGlobal().log(Level.SEVERE, "AMS :", ex);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(AlertMonitoringSystem.class.getName()).log(Level.SEVERE, null, ex1);
                }
                ams.amt = new AlertMonitorThread(ams);
                amt.start();
            }
        }

        /**
         * Simplified way of writing status codes
         */
        private void write(int i) throws IOException {
            os.write(("" + i).getBytes());
            os.flush();
        }
    }
}
