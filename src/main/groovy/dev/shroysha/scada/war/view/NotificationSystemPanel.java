package dev.shroysha.scada.war.view;

import dev.shroysha.scada.war.controller.NotificationSystem;
import dev.shroysha.scada.war.util.ServerProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;


public class NotificationSystemPanel extends JRootPane {

    private final NotificationSystem notificationSystem;
    private JTabbedPane tabbed;

    public NotificationSystemPanel(NotificationSystem ns) {
        super();
        this.notificationSystem = ns;
        initPanel();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ServerProperties props = new ServerProperties();
        NotificationSystem system = new NotificationSystem(props);
        NotificationSystemPanel panel = new NotificationSystemPanel(system);
        panel.setGlassVisible(false);
        frame.add(panel);

        frame.setVisible(true);
    }

    private void initPanel() {
        tabbed = new JTabbedPane();

        tabbed.addTab("Paging", notificationSystem.getPagingSystem().getPagingSystemPanel());
        tabbed.addTab("Email", notificationSystem.getEmailSystem().getEmailSystemPanel());
        tabbed.addTab(
                "Alerts", notificationSystem.getAlertMonitoringSystem().getAlertMonitoringPanel());
        tabbed.addTab("Employees", notificationSystem.getEmployeeHandler().getEmployeePanel());

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(tabbed, BorderLayout.CENTER);

        this.setGlassPane(new InactiveGlassPanel());

        setGlassVisible(true); // because the paging system will initially be off.
    }

    public void setGlassVisible(boolean bool) {
        this.getGlassPane().setVisible(bool);
    }

    public JTabbedPane getTabbedPane() {
        return tabbed;
    }


    static class InactiveGlassPanel extends JComponent {

        public InactiveGlassPanel() {
            super();

            this.addMouseListener(new MouseAdapter() {
            });
            this.addMouseMotionListener(new MouseAdapter() {
            });
        }


        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Color tGray = new Color(25, 25, 25, 200); // r,g,b,alpha

            g.setColor(tGray);

            g.fillRect(0, 0, this.getWidth(), this.getHeight());

            g.setColor(Color.white);

            Font font = new Font("Arial", Font.BOLD, 16);
            g.setFont(font);

            String string = "Paging Disabled";
            FontMetrics metrics = g.getFontMetrics();

            int stringWidth = metrics.stringWidth(string);
            int stringHeight = metrics.getHeight();

            int x = this.getWidth() / 2 - stringWidth / 2;
            int y = this.getHeight() / 2 - stringHeight / 2;

            g.drawString(string, x, y);
        }

        public void setVisible(boolean aFlag) {
            super.setVisible(aFlag);
            requestFocus();
        }
    }
}
