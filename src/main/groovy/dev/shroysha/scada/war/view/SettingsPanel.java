

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dev.shroysha.scada.war.view;

import dev.shroysha.scada.war.util.ServerProperties;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;


public class SettingsPanel extends JPanel {

    private final String BLANK = "Blank";
    private final String PAGING = "Paging";
    private final String EMAIL = "Email";
    private final String VOICE = "Acknowldgement";
    private final ServerProperties props;
    private JButton okayButton;
    private JButton cancelButton;
    private JPanel currentlyConfiguringPanel;
    private PagingConfigPanel pagingConfigPanel;
    private EmailConfigPanel emailConfigPanel;
    private VoiceConfigPanel voiceConfigPanel;
    private JTree settingsSelectionTree;
    private DefaultMutableTreeNode pagingNode;
    private DefaultMutableTreeNode emailNode;
    private DefaultMutableTreeNode voiceNode;

    public SettingsPanel(ServerProperties properties) {
        super(new BorderLayout());
        this.props = properties;
        init();
    }

    public static void main(String[] args) {
        ServerProperties props = new ServerProperties();
        JFrame frame = new JFrame();
        frame.setContentPane(new SettingsPanel(props));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void init() {
        makeSelectionTree();
        makeOkayButton();
        makeCancelButton();
        makeCurrentlyConfiguringPanel();

        JScrollPane scroller = new JScrollPane(settingsSelectionTree);
        this.add(scroller, BorderLayout.WEST);
        // this.add(settingsSelectionTree, BorderLayout.WEST);
        this.add(currentlyConfiguringPanel, BorderLayout.CENTER);

        JPanel temp = new JPanel();
        temp.add(okayButton);
        temp.add(cancelButton);
        this.add(temp, BorderLayout.SOUTH);
    }

    private void makeSelectionTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Settings");
        settingsSelectionTree = new JTree(root);

        DefaultMutableTreeNode notificationRoot = new DefaultMutableTreeNode("Notification");
        pagingNode = new DefaultMutableTreeNode(PAGING);
        emailNode = new DefaultMutableTreeNode(EMAIL);
        notificationRoot.add(pagingNode);
        notificationRoot.add(emailNode);

        voiceNode = new DefaultMutableTreeNode(VOICE);
        root.add(notificationRoot);
        root.add(voiceNode);

        settingsSelectionTree.expandPath(new TreePath(root));
        settingsSelectionTree.setToggleClickCount(1);
        settingsSelectionTree.addTreeSelectionListener(new SettingsTreeListener());
        settingsSelectionTree.setMinimumSize(settingsSelectionTree.getPreferredSize());
    }

    private void makeOkayButton() {
        okayButton = new JButton("Okay");
        okayButton.addActionListener(
                e -> validateAndSaveProperties());
    }

    private void makeCancelButton() {
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(
                e -> closeWindow());
    }

    private void makeCurrentlyConfiguringPanel() {
        currentlyConfiguringPanel = new JPanel(new CardLayout());

        pagingConfigPanel = new PagingConfigPanel();
        emailConfigPanel = new EmailConfigPanel();
        voiceConfigPanel = new VoiceConfigPanel();

        currentlyConfiguringPanel.add(new JPanel(), BLANK);
        currentlyConfiguringPanel.add(pagingConfigPanel, PAGING);
        currentlyConfiguringPanel.add(emailConfigPanel, EMAIL);
        currentlyConfiguringPanel.add(voiceConfigPanel, VOICE);
    }

    private void changeToPanel(String identifier) {
        CardLayout layout = (CardLayout) currentlyConfiguringPanel.getLayout();
        layout.show(currentlyConfiguringPanel, identifier);
    }

    private void changeToPagingPanel() {
        changeToPanel(PAGING);
    }

    private void changeToVoicePanel() {
        changeToPanel(VOICE);
    }

    private void changeToEmailPanel() {
        changeToPanel(EMAIL);
    }

    private void changeToBlankPanel() {
        changeToPanel(BLANK);
    }

    private void validateAndSaveProperties() {
        boolean hadErrors = checkForAndShowErrors();
        if (!hadErrors) {
            saveProperties();
        }

        closeWindow();
    }

    /**
     * Will return true if had errors
     */
    private boolean checkForAndShowErrors() {
        return pagingConfigPanel.checkForAndShowErrors()
                && voiceConfigPanel.checkForAndShowErrors()
                && emailConfigPanel.checkForAndShowErrors();
    }

    private void closeWindow() {
        Window window = SwingUtilities.getWindowAncestor(SettingsPanel.this);
        window.dispose();
    }

    private void saveProperties() {
        pagingConfigPanel.saveProperties();
        voiceConfigPanel.saveProperties();
        emailConfigPanel.saveProperties();
    }

    private class SettingsTreeListener implements TreeSelectionListener {


        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) e.getNewLeadSelectionPath().getLastPathComponent();
            if (node == pagingNode) {
                changeToPagingPanel();
            } else if (node == voiceNode) {
                changeToVoicePanel();
            } else if (node == emailNode) {
                changeToEmailPanel();
            } else {
                changeToBlankPanel();
            }
        }
    }

    private class PagingConfigPanel extends JPanel {

        private JTextField ipField;
        private JTextField portField;

        private JLabel errorLabel;

        public PagingConfigPanel() {
            super(new BorderLayout());
            init();
        }

        private void init() {

            JPanel temp1 = new JPanel(new BorderLayout());
            JPanel temp2 = new JPanel(new BorderLayout());
            int MAX_IP_LENGTH = 15;
            ipField = new JTextField(props.getPagerIP(), MAX_IP_LENGTH);
            int MAX_PORT_LENGTH = 5;
            portField = new JTextField("" + props.getPagerPort(), MAX_PORT_LENGTH);

            temp1.add(new JLabel("Paging Server IP       "), BorderLayout.WEST);
            temp1.add(ipField, BorderLayout.CENTER);

            temp2.add(new JLabel("Paging Server Port       "), BorderLayout.WEST);
            temp2.add(portField, BorderLayout.CENTER);

            errorLabel = new JLabel("");
            errorLabel.setForeground(Color.red);

            JPanel temp = new JPanel(new GridLayout(3, 1));
            temp.add(temp1);
            temp.add(temp2);
            temp.add(errorLabel);

            this.add(temp, BorderLayout.NORTH);
            this.add(new JPanel(), BorderLayout.CENTER);
        }

        /**
         * Will return true if had errors
         */
        private boolean checkForAndShowErrors() {
            String ip = ipField.getText().trim();
            String port = portField.getText().trim();

            boolean validIP = ServerProperties.isValidIPv4(ip);
            boolean validPort;
            try {
                validPort = ServerProperties.isValidPort(Integer.parseInt(port));
            } catch (NumberFormatException ex) {
                errorLabel.setText("Port entered was not a number");
                return true;
            }

            if (!validIP) {
                errorLabel.setText("Invalid Paging Server IP Entered");
                return true;
            }

            if (!validPort) {
                errorLabel.setText("Invalid Paging Server Port Entered");
                return true;
            }

            errorLabel.setText("");
            return false;
        }

        public void saveProperties() {
            props.setPagerIP(ipField.getText().trim());
            props.setPagerPort(Integer.parseInt(portField.getText().trim()));
            System.out.println("Saved paging properties");
        }
    }

    private class EmailConfigPanel extends JPanel {

        private JTextField smtpField;
        private JTextField fromField;

        private JLabel errorLabel;

        public EmailConfigPanel() {
            super(new BorderLayout());
            init();
        }

        private void init() {
            JPanel temp1 = new JPanel(new BorderLayout());
            JPanel temp2 = new JPanel(new BorderLayout());
            smtpField = new JTextField(props.getSMTPServer());
            fromField = new JTextField(props.getFromAddress());

            temp1.add(new JLabel("Email SMTP Server       "), BorderLayout.WEST);
            temp1.add(smtpField, BorderLayout.CENTER);

            temp2.add(new JLabel("From Email Address       "), BorderLayout.WEST);
            temp2.add(fromField, BorderLayout.CENTER);

            errorLabel = new JLabel("");
            errorLabel.setForeground(Color.red);

            JPanel temp = new JPanel(new GridLayout(3, 1));
            temp.add(temp1);
            temp.add(temp2);
            temp.add(errorLabel);

            this.add(temp, BorderLayout.NORTH);
            this.add(new JPanel(), BorderLayout.CENTER);
        }

        /**
         * Will return true if had errors
         */
        private boolean checkForAndShowErrors() {
            String smtp = smtpField.getText().trim();
            String from = smtpField.getText().trim();

            boolean validSMTP = ServerProperties.isValidSMTP(smtp);
            boolean validFrom = ServerProperties.isValidFromAddress(from);

            if (!validSMTP) {
                errorLabel.setText("Invalid SMTP Server Address Entered");
                return true;
            }

            if (!validFrom) {
                errorLabel.setText("Invalid From Email Address Entered");
                return true;
            }

            errorLabel.setText("");
            return false;
        }

        public void saveProperties() {
            props.setSMTPServer(smtpField.getText().trim());
            props.setFromAddress(fromField.getText().trim());
            System.out.println("Saved email properties");
        }
    }

    private class VoiceConfigPanel extends JPanel {

        private JTextField ipField;
        private JTextField portField;

        private JLabel errorLabel;

        public VoiceConfigPanel() {
            super(new BorderLayout());
            init();
        }

        private void init() {
            JPanel temp1 = new JPanel(new BorderLayout());
            JPanel temp2 = new JPanel(new BorderLayout());
            ipField = new JTextField(props.getModemIP());
            portField = new JTextField("" + props.getModemPort());
            temp1.add(new JLabel("Voice Modem IP       "), BorderLayout.WEST);
            temp1.add(ipField, BorderLayout.CENTER);

            temp2.add(new JLabel("Voice Modem Port       "), BorderLayout.WEST);
            temp2.add(portField, BorderLayout.CENTER);

            errorLabel = new JLabel("");
            errorLabel.setForeground(Color.red);

            JPanel temp = new JPanel(new GridLayout(3, 1));
            temp.add(temp1);
            temp.add(temp2);
            temp.add(errorLabel);

            this.add(temp, BorderLayout.NORTH);
            this.add(new JPanel(), BorderLayout.CENTER);
        }

        /**
         * Will return true if had errors
         */
        private boolean checkForAndShowErrors() {
            String ip = ipField.getText().trim();
            String port = portField.getText().trim();

            boolean validIP = ServerProperties.isValidIPv4(ip);
            boolean validPort;
            try {
                validPort = ServerProperties.isValidPort(Integer.parseInt(port));
            } catch (NumberFormatException ex) {
                errorLabel.setText("Port entered was not a number");
                return true;
            }

            if (!validIP) {
                errorLabel.setText("Invalid Voice Modem IP Entered");
                return true;
            }

            if (!validPort) {
                errorLabel.setText("Invalid Voice Modem Port Entered");
                return true;
            }

            errorLabel.setText("");
            return false;
        }

        public void saveProperties() {
            props.setModemIP(ipField.getText().trim());
            props.setModemPort(Integer.parseInt(portField.getText().trim()));
            System.out.println("Saved voice properties");
        }
    }
}
