package dev.shroysha.scada.war.controller.notify.pager;

import dev.shroysha.scada.war.controller.LogListener;
import dev.shroysha.scada.war.controller.notify.alert.Alert;
import dev.shroysha.scada.war.controller.notify.alert.AlertMonitoringSystem;
import dev.shroysha.scada.war.model.AlertListener;
import dev.shroysha.scada.war.util.ServerProperties;
import dev.shroysha.scada.war.model.UpdateListener;
import dev.shroysha.scada.war.model.Employee;
import dev.shroysha.scada.war.controller.EmployeeFactory;
import dev.shroysha.scada.war.view.PagingProgressPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Stack;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class PagingSystem implements AlertListener {

    private static final Logger log = Logger.getGlobal();
    private final PagingSystemPanel parent;
    private final ServerProperties props;
    private final PagingProgressPanel ppp;
    private final Stack<LogListener> logListeners = new Stack<>();
    private EmployeeFactory employeeHandler;
    private PrintWriter pageLog;

    public PagingSystem(ServerProperties props) {
        super();
        this.props = props;

        String[] verbose = {"v"};
        dispatch(verbose);

        File pageFile = new File("pagelog.txt");
        try {
            pageLog = new PrintWriter(pageFile);
        } catch (FileNotFoundException ex) {
            Logger.getGlobal().log(Level.SEVERE, "Could not open page log file for writing.");
        }

        parent = new PagingSystemPanel(this);
        ppp = new PagingProgressPanel();
    }

    static void dispatch(String[] args) {
        for (String s : args) {
            s = s.replaceAll("-", "");
            char command = s.charAt(0);

            switch (command) {
                case 'v':
                    log.setLevel(Level.ALL);
                    try {
                        FileHandler fh = new FileHandler("pagelog.xml");
                        log.addHandler(fh);
                        log.info("Hai");
                    } catch (IOException | SecurityException ex) {
                        Logger.getGlobal().info(ex.toString());
                    }
            }
        }
    }

    protected void setIPAddress(String address) {
        props.setPagerIP(address);
    }

    protected void setPort(String port) {
        props.setPagerPort(Integer.parseInt(port));
    }

    public void addLogListener(LogListener listener) {
        logListeners.add(listener);
    }

    public void removeLogListner(LogListener listener) {
        logListeners.remove(listener);
    }

  /*
  protected void errorRecovery(Exception ex) {
      final String RETRY = "Retry";
      final String CHANGE_IP = "Change IP";
      final String CHANGE_PORT = "Change Port";
      final String QUIT = "Quit";

      String[] options = {QUIT, CHANGE_PORT, CHANGE_IP, RETRY};

      int choseInt = JOptionPane.CLOSED_OPTION;
      while(choseInt == JOptionPane.CLOSED_OPTION) {
          choseInt = JOptionPane.showOptionDialog(parent, "Paging Server Connection Error\n"+ex.getMessage(), "Error Recovery", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, RETRY);
      }

      String chose = options[choseInt];
      if(chose.equals(RETRY)) {
          // do nothing
      } else if(chose.equals(CHANGE_IP)) {
          props.setPagerIP("");
      } else if(chose.equals(CHANGE_PORT)) {
          props.setPagerPort(-1);
      } else if(chose.equals(QUIT)) {
          int dialogResult = JOptionPane.showConfirmDialog(this.getPagingSystemPanel(), "Are you sure you want to quit?\n"
                  + "All active pages will be deleted.\n"
                  + "If any errors are sitll active, they will alert again when the program is reopened", "Are you sure?",JOptionPane.YES_NO_OPTION);
          if(dialogResult == JOptionPane.YES_OPTION)
              System.exit(4);
          else
              errorRecovery(ex);
      }
  }*/

    public void notifyAllLogListeners(String logText) {
        for (LogListener listener : logListeners) {
            listener.onLog(logText);
        }
    }

    protected JDialog errorRecovery(Exception ex) {
        JButton changeIPButton = new JButton("Change IP");

        JButton changePortButton = new JButton("Change Port");

        JButton quitButton = new JButton("Quit");

        Object[] options = {quitButton, changePortButton, changeIPButton};
        final JDialog dialog =
                new JOptionPane(
                        "\"Paging Server Connection Error\n" + ex.getMessage(),
                        JOptionPane.ERROR_MESSAGE,
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        null,
                        options,
                        changeIPButton)
                        .createDialog(parent, "Error Recovery");
        dialog.setModal(false);

        changeIPButton.addActionListener(
                e -> {
                    props.setPagerIP("");
                    dialog.dispose();
                });

        changePortButton.addActionListener(
                e -> {
                    props.setPagerPort(-1);
                    dialog.dispose();
                });

        quitButton.addActionListener(
                e -> {
                    int dialogResult =
                            JOptionPane.showConfirmDialog(
                                    getPagingSystemPanel(),
                                    "Are you sure you want to quit?\n"
                                            + "All active alerts will be deleted.\n"
                                            + "If any errors are sitll active, they will alert again when the program is reopened",
                                    "Are you sure?",
                                    JOptionPane.YES_NO_OPTION);
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        System.exit(4);
                    } else {
                        dialog.dispose();
                    }
                });

        return dialog;
    /*
            final String CHANGE_IP = "Change IP";
            final String CHANGE_PORT = "Change Port";
            final String QUIT = "Quit";

            JButton changeIPButton = new JButton("Change IP");

            JButton changePortButton = new JButton("Change Port");

            JButton quitButton = new JButton("Quit");

            String[] options = {QUIT, CHANGE_PORT, CHANGE_IP, RETRY};


            int choseInt = JOptionPane.CLOSED_OPTION;
            while(choseInt == JOptionPane.CLOSED_OPTION) {
                choseInt = JOptionPane.showOptionDialog(parent, "Paging Server Connection Error\n"+ex.getMessage(), "Error Recovery", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, RETRY);
            }
    */
    /*
    String chose = options[choseInt];
    if(chose.equals(RETRY)) {
        // do nothing
    } else if(chose.equals(CHANGE_IP)) {
        props.setPagerIP("");
    } else if(chose.equals(CHANGE_PORT)) {
        props.setPagerPort(-1);
    } else if(chose.equals(QUIT)) {
        int dialogResult = JOptionPane.showConfirmDialog(this.getPagingSystemPanel(), "Are you sure you want to quit?\n"
                + "All active pages will be deleted.\n"
                + "If any errors are sitll active, they will alert again when the program is reopened", "Are you sure?",JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION)
            System.exit(4);
        else
            errorRecovery(ex);
    }*/
    }


    public void alertReceived(Alert alert) {
        Employee[] employees = employeeHandler.getCurrentPrioritizedEmployees();

        int length = Math.min(employees.length, alert.getTimesPaged());

        Employee[] pageEmployee = new Employee[length];

        if (employees.length == 0) {
            notifyAllLogListeners("There are no employees on duty");
            return;
        }

        System.arraycopy(employees, 0, pageEmployee, 0, pageEmployee.length);

        for (Employee employee : pageEmployee) {
            page(alert, employee);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void page(Alert alert, Employee employee) {
        Page page = new Page(this, employee, alert.getMessage(), props);
        boolean worked = false;
        JDialog errorDialog = null;
        do {
            try {
                page.start();
                worked = true;
            } catch (IOException ex) {
                if (errorDialog == null) { // if first time
                    errorDialog = errorRecovery(ex);
                }
                if (!errorDialog.isVisible()) {
                    errorDialog.setVisible(true);
                }

                Logger.getGlobal().log(Level.SEVERE, null, ex);
            }
        } while (!worked);
        if (errorDialog != null) {
            errorDialog.setVisible(false);
        }

        pageLog.println("Paged: " + employee.getName() + " with message " + alert.getMessage());
        pageLog.flush();
        notifyAllLogListeners("Paged: " + employee.getName() + " with message " + alert.getMessage());
    }

    public PagingSystemPanel getPagingSystemPanel() {
        return parent;
    }

    public PagingProgressPanel getPagingProgressPanel() {
        return ppp;
    }

    public void setAlertMonitoringSystem(AlertMonitoringSystem ams) {
        if (ams != null) {
            ams.removeAlertListner(this);
        }

        if (ams != null) {
            ams.addAlertListener(this);
        }
    }

    public void setEmployeeHandler(EmployeeFactory employeeHandler) {
        this.employeeHandler = employeeHandler;
    }

    public class PagingSystemPanel extends JPanel implements UpdateListener, LogListener {

        private final PagingSystem ps;

        private JTextArea logArea;
        private JLabel ipLabel, portLabel;

        protected PagingSystemPanel(PagingSystem aThis) {
            super();
            ps = aThis;
            init();
        }

        private void init() {
            this.setBorder(new EmptyBorder(10, 10, 10, 10));
            this.setLayout(new BorderLayout());

            JPanel contentPanel = new JPanel(new BorderLayout());

            ipLabel = new JLabel("lol");
            portLabel = new JLabel("lol");
            setIPLabelText();
            setPortLabelText();

            JButton changeIPButton = new JButton("Change IP");
            changeIPButton.addActionListener(
                    ae -> {
                        props.setPagerIP("");
                        setIPLabelText();
                    });

            JButton changePortButton = new JButton("Change Port");
            changePortButton.addActionListener(
                    ae -> {
                        props.setPagerPort(-1);
                        setPortLabelText();
                    });

            logArea = new JTextArea();
            logArea.setEditable(false);

            JScrollPane scroller = new JScrollPane(logArea);

            JPanel alertPanel = new JPanel(new GridLayout(2, 2));

            alertPanel.add(ipLabel);
            alertPanel.add(changeIPButton);
            alertPanel.add(portLabel);
            alertPanel.add(changePortButton);

            contentPanel.add(alertPanel, BorderLayout.CENTER);

            this.add(scroller, BorderLayout.CENTER);
            this.add(contentPanel, BorderLayout.NORTH);

            ps.addLogListener(this);
        }

        private void setIPLabelText() {
            ipLabel.setText("IP: " + ps.props.getPagerIP());
        }

        private void setPortLabelText() {
            portLabel.setText("Port: " + ps.props.getPagerPort());
        }


        public void onUpdate() {
            setIPLabelText();
            setPortLabelText();
        }


        public void onLog(String logText) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            String formattedDate = sdf.format(Calendar.getInstance().getTime());
            logArea.append(logText + " on " + formattedDate + "\n");
        }
    }
}
