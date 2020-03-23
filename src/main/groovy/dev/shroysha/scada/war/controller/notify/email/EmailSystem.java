package dev.shroysha.scada.war.controller.notify.email;

import dev.shroysha.scada.war.controller.notify.alert.Alert;
import dev.shroysha.scada.war.controller.notify.alert.AlertMonitoringSystem;
import dev.shroysha.scada.war.model.AlertListener;
import dev.shroysha.scada.war.model.LogListener;
import dev.shroysha.scada.war.util.ServerProperties;
import dev.shroysha.scada.war.model.Employee;
import dev.shroysha.scada.war.controller.EmployeeFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.Stack;


public class EmailSystem implements AlertListener {

    public static final String DEF_FROM_ADDRESS = "noreply@washco-md.net";
    public static final String DEF_SMTP_SERVER = "smtp.washco-md.net";

    private final ServerProperties props;
    private final EmailSystemPanel parent;
    private final Stack<LogListener> logListeners = new Stack<>();
    private EmployeeFactory employeeHandler;

    public EmailSystem(ServerProperties props) {
        super();

        this.props = props;
        parent = new EmailSystemPanel(this);
    }

    public void sendEmail(Alert alert, Employee employee) {

        if (!employee.hasEmail()) {
            return; // if the employee doesn't have an email... then don't email him
        }

        // Recipient's email ID needs to be mentioned.
        String to = employee.getEmail();

        // Sender's email ID needs to be mentioned
        String from = props.getFromAddress();

        // Assuming you are sending email from localhost
        String host = props.getSMTPServer();

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail main.java.server
        properties.setProperty("mail.smtp.host", host);

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("Scada Alert System! Critical alert #" + alert.getJobID());

            // Send the actual HTML message, as big as you like
            message.setContent("<h1>This is actual message</h1>", "text/html");

            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully....");

            notifyAllLogListeners("Sent email to " + employee.getEmail());
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    public void addLogListener(LogListener listener) {
        logListeners.add(listener);
    }

    public void removeLogListner(LogListener listener) {
        logListeners.remove(listener);
    }

    private void notifyAllLogListeners(String logText) {
        for (LogListener listener : logListeners) {
            listener.onLog(logText);
        }
    }

    public EmailSystemPanel getEmailSystemPanel() {
        return parent;
    }


    public void alertReceived(Alert alert) {
        Employee[] employees = employeeHandler.getCurrentPrioritizedEmployees();
        int length = Math.min(alert.getTimesPaged(), employees.length);

        if (employees.length == 0) {
            notifyAllLogListeners("There are no employees on duty");
            return;
        }

        for (int i = 0; i < length; i++) {
            sendEmail(alert, employees[i]);
        }
    }

    public void setEmployeeHandler(EmployeeFactory eh) {
        employeeHandler = eh;
    }

    public void setAlertMonitoringSystem(AlertMonitoringSystem ams) {
        if (ams != null) {
            ams.removeAlertListner(this);
        }

        if (ams != null) {
            ams.addAlertListener(this);
        }
    }

    public class EmailSystemPanel extends JPanel implements LogListener {

        private final EmailSystem es;

        private JTextArea logArea;
        private JLabel ipLabel, portLabel;

        protected EmailSystemPanel(EmailSystem aThis) {
            super();
            es = aThis;
            init();
        }

        private void init() {
            this.setBorder(new EmptyBorder(10, 10, 10, 10));
            this.setLayout(new BorderLayout());

            JPanel contentPanel = new JPanel(new BorderLayout());

            ipLabel = new JLabel("lol");
            portLabel = new JLabel("lol");
            setSMTPLabelText();
            setFromLabelText();

            JButton changeIPButton = new JButton("Change SMTP Server");
            changeIPButton.addActionListener(
                    ae -> {
                        props.setSMTPServer("");
                        setSMTPLabelText();
                    });

            JButton changePortButton = new JButton("Change From Address");
            changePortButton.addActionListener(
                    ae -> {
                        props.setFromAddress("");
                        setFromLabelText();
                    });

            logArea = new JTextArea();
            logArea.setEditable(false);

            JPanel alertPanel = new JPanel(new GridLayout(2, 2));

            alertPanel.add(ipLabel);
            alertPanel.add(changeIPButton);
            alertPanel.add(portLabel);
            alertPanel.add(changePortButton);

            contentPanel.add(alertPanel, BorderLayout.CENTER);

            this.add(logArea, BorderLayout.CENTER);
            this.add(contentPanel, BorderLayout.NORTH);

            es.addLogListener(this);
        }

        private void setSMTPLabelText() {
            ipLabel.setText("SMTP Server: " + props.getPagerIP());
        }

        private void setFromLabelText() {
            portLabel.setText("From Email Address: " + props.getFromAddress());
        }


        public void onLog(String logText) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            String formattedDate = sdf.format(Calendar.getInstance().getTime());
            logArea.append(logText + " on " + formattedDate + "\n");
        }
    }
}
