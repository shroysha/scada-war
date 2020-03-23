package dev.shroysha.scada.war.util;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class ServerProperties {

    private static final String PAGER_IP = "Pager IP",
            PAGER_PORT = "Pager Port",
            MODEM_IP = "Modem IP",
            MODEM_PORT = "Modem Port",
            SMTP_SERVER = "SMTP Server",
            FROM_ADDRESS = "From Address";

    private static final File configFile =
            new File(ScadaUtilities.getMainDirPath() + "pagingsystem/" + "modemProps.cfg");

    private Properties props;

    public ServerProperties() {
        super();

        loadProps();
        checkProps();
    }

    public static boolean isValidIPv4(String ip) {
        if (ip == null || ip.equals("")) {
            return false;
        }

        try {
            final InetAddress inet = InetAddress.getByName(ip);
            return inet.getHostAddress().equals(ip) && inet instanceof Inet4Address;
        } catch (final UnknownHostException ex) {
            // JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static boolean isValidPort(int port) {
        if (port == -1) {
            return false;
        }

        try {
            if (port >= 0 && port <= 65535) {
                return true;
            } else {
                throw new IllegalArgumentException("Port must be between 0 and 65535");
            }
        } catch (Exception ex) {
            // JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static boolean isValidSMTP(String smtp) {
        return smtp != null && !smtp.equals("");
    }

    public static boolean isValidFromAddress(String from) {
        return from != null && !from.equals("");
    }

    private void loadProps() {
        if (props == null) {
            props = new Properties();
        }

        if (!configFile.exists()) {
            String makePath = configFile.getParentFile().getPath();
            new File(makePath).mkdirs();
            try {
                configFile.createNewFile();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Couldn't create paging configuration file.\n"
                                + "Please check application permissions and try again\n"
                                + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(5);
            }
            props.setProperty(MODEM_IP, "");
            props.setProperty(MODEM_PORT, "-1");
            props.setProperty(PAGER_IP, "");
            props.setProperty(PAGER_PORT, "-1");
            props.setProperty(SMTP_SERVER, "");
            props.setProperty(FROM_ADDRESS, "");
            saveProps();
        } else {
            try {
                props.load(new FileInputStream(configFile));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Couldn't load paging configuration file.\n"
                                + "Please check application permissions and try again\n"
                                + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void checkProps() {
        checkPagerIP();
        checkPagerPort();
        checkModemIP();
        checkModemPort();
        checkSMTP();
        checkFromAddress();
        saveProps();
    }

    private void saveProps() {
        try {
            props.store(
                    new FileOutputStream(configFile),
                    "Modem and Paging Server Properties, used by Scada main.java.server");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Couldn't save to paging configuration file.\n"
                            + "All of your data may have been lost\n"
                            + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getPagerIP() {
        return props.getProperty(PAGER_IP);
    }

    public void setPagerIP(String ip) {
        props.setProperty(PAGER_IP, ip);

        checkPagerIP();
        saveProps();
    }

    public int getPagerPort() {
        return Integer.parseInt(props.getProperty(PAGER_PORT));
    }

    public void setPagerPort(int port) {
        props.setProperty(PAGER_PORT, "" + port);

        checkPagerPort();
        saveProps();
    }

    public String getModemIP() {
        return props.getProperty(MODEM_IP);
    }

    public void setModemIP(String ip) {
        props.setProperty(MODEM_IP, ip);

        checkModemIP();
        saveProps();
    }

    public int getModemPort() {
        return Integer.parseInt(props.getProperty(MODEM_PORT));
    }

    public void setModemPort(int port) {
        props.setProperty(MODEM_PORT, "" + port);

        checkModemPort();
        saveProps();
    }

    public String getSMTPServer() {
        return props.getProperty(SMTP_SERVER);
    }

    public void setSMTPServer(String serverAddress) {
        props.setProperty(SMTP_SERVER, serverAddress);

        checkSMTP();
        saveProps();
    }

    public String getFromAddress() {
        return props.getProperty(FROM_ADDRESS);
    }

    public void setFromAddress(String fromAddress) {
        props.setProperty(FROM_ADDRESS, fromAddress);

        checkFromAddress();
        saveProps();
    }

    private void checkPagerIP() {
        while (!isValidIPv4(props.getProperty(PAGER_IP))) {
            String ip = JOptionPane.showInputDialog("Enter Paging Server IP").trim();
            props.setProperty(PAGER_IP, ip);
        }
    }

    private void checkPagerPort() {
        while (props.getProperty(PAGER_PORT).equals("")
                || !isValidPort(Integer.parseInt(props.getProperty(PAGER_PORT)))) {
            String port = JOptionPane.showInputDialog("Enter Paging Server Port").trim();
            props.setProperty(PAGER_PORT, port);
        }
    }

    private void checkModemIP() {
        while (!isValidIPv4(props.getProperty(MODEM_IP))) {
            String ip = JOptionPane.showInputDialog("Enter Phone Modem IP").trim();
            props.setProperty(MODEM_IP, ip);
        }
    }

    private void checkModemPort() {
        while (props.getProperty(MODEM_PORT).equals("")
                || !isValidPort(Integer.parseInt(props.getProperty(MODEM_PORT)))) {
            String port = JOptionPane.showInputDialog("Enter Phone Modem Port".trim());
            props.setProperty(MODEM_PORT, port);
        }
    }

    private void checkSMTP() {
        while (props.getProperty(SMTP_SERVER).equals("")
                || !isValidSMTP(props.getProperty(SMTP_SERVER))) {
            String smtp = JOptionPane.showInputDialog("Enter SMTP Server".trim());
            props.setProperty(SMTP_SERVER, smtp);
        }
    }

    private void checkFromAddress() {
        while (props.getProperty(FROM_ADDRESS).equals("")
                || !isValidFromAddress(props.getProperty(FROM_ADDRESS))) {
            String from = JOptionPane.showInputDialog("Enter Email From Address".trim());
            props.setProperty(FROM_ADDRESS, from);
        }
    }
}
