package dev.shroysha.scada.war.view;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class JLightSwitch extends AbstractButton {

    private static final String ON = "ON";
    private static final String OFF = "OFF";
    private static final Color ON_COLOR = Color.green;
    private static final Color OFF_COLOR = Color.red;
    private static final int pWidth = 125, pHeight = 25;
    private static final int BUFFER = 10;
    private int titleWidth;

    public JLightSwitch(String title) {
        super();
        setText(title);
        setFont(new JLabel().getFont());

        FontMetrics fontMetrics = this.getFontMetrics(getFont());
        titleWidth = fontMetrics.stringWidth(title) + BUFFER * 2;

        setModel(new JToggleButton.ToggleButtonModel());

        setSelected(false);

        addMouseListener(
                new MouseAdapter() {
                    private long lastInt;


                    public void mouseClicked(MouseEvent me) {
                        // super.mouseClicked(me);
                        if (lastInt < me.getWhen()) {
                            if (isEnabled()) {
                                setEnabled(false);
                                setSelected(!isSelected());
                                new RepaintThread().start();
                                ActionEvent e =
                                        new ActionEvent(JLightSwitch.this, 0, JLightSwitch.this.getActionCommand());
                                fireActionPerformed(e);
                                setEnabled(true);
                                new RepaintThread().start();
                            }
                            lastInt = System.currentTimeMillis();
                        }
                    }
                });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("HELLo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JLightSwitch swit = new JLightSwitch("Hello");
        swit.addActionListener(
                ae -> {
                    System.out.println("ON");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(JLightSwitch.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(swit);

        frame.add(panel, BorderLayout.CENTER);
        frame.setSize(300, 300);
        frame.setVisible(true);
    }


    public void setSelected(boolean selected) {
        super.setSelected(selected);

        if (selected) {
            setBackground(ON_COLOR);
        } else {
            setBackground(OFF_COLOR);
        }
    }


    public Dimension getPreferredSize() {
        return new Dimension(pWidth + titleWidth, pHeight);
    }


    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Dimension dim = this.getPreferredSize();
        Color background = getBackground();
        Color foreground = Color.gray;

        g.setFont(getFont());
        FontMetrics metrics = g.getFontMetrics();

        if (!this.getText().equals("")) {
            String title = getText();

            Rectangle2D stringBounds = metrics.getStringBounds(title, g);
            titleWidth = BUFFER * 2 + metrics.stringWidth(title);
            int titleHeight = (int) stringBounds.getHeight();

            int y = dim.height / 2 + titleHeight / 2;
            g.setColor(Color.black);
            g.drawString(title, BUFFER, y);
        } else {
            titleWidth = 0;
        }

        g.setColor(background);

        final int backgroundX = titleWidth;
        final int backgroundY = 0;
        final int backgroundWidth = dim.width - titleWidth;
        final int backgroundHeight = dim.height;
        g.fillRect(backgroundX, backgroundY, backgroundWidth, backgroundHeight);

        g.setColor(foreground);

        // draw slider
        if (isSelected()) {
            int width = backgroundWidth / 2;
            g.fillRect(backgroundX, backgroundY, width, backgroundHeight);
        } else {
            int x = backgroundX + backgroundWidth / 2 + 1;
            int width = backgroundWidth / 2;
            g.fillRect(x, backgroundY, width, backgroundHeight);
        }

        // draw on or off
        g.setColor(Color.black);
        if (!isSelected()) {
            int width = backgroundWidth / 2;

            int offWidth = metrics.stringWidth(OFF);
            Rectangle2D stringBounds = metrics.getStringBounds(OFF, g);
            int offHeight = (int) stringBounds.getHeight();

            int offX = backgroundX + width / 2 - offWidth / 2;
            int offY = (int) (backgroundHeight / 2 + stringBounds.getHeight() / 2);
            g.drawString(OFF, offX, offY);
        } else {
            int x = backgroundX + backgroundWidth / 2;
            int width = backgroundWidth / 2;

            int onWidth = metrics.stringWidth(ON);
            Rectangle2D stringBounds = metrics.getStringBounds(ON, g);
            int onHeight = (int) stringBounds.getHeight();

            int onX = x + width / 2 - onWidth / 2;
            int onY = (int) (backgroundHeight / 2 + stringBounds.getHeight() / 2);
            g.drawString(ON, onX, onY);
        }

        g.fillRect(backgroundX, backgroundY, backgroundWidth, 2);
        g.fillRect(backgroundX, backgroundY, 2, backgroundHeight);
        g.fillRect(backgroundX, backgroundHeight - 2, backgroundWidth, 2);
        g.fillRect(backgroundX + backgroundWidth - 2, backgroundY, 2, backgroundHeight);
        g.fillRect(backgroundX + backgroundWidth / 2 - 1, backgroundY, 3, backgroundHeight);

        if (!isEnabled()) {
            Color color = new Color(150, 150, 150, 200);
            g.setColor(color);
            g.fillRect(backgroundX, backgroundY, backgroundWidth, backgroundHeight);
        }
    }

    private class RepaintThread extends Thread {

        public void run() {
            System.out.println("Repainting");
            update(getGraphics());
            System.out.println("Repaint finished");
        }
    }
}
