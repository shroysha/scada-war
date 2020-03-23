package dev.shroysha.scada.war.view;


import javax.swing.*;
import java.awt.*;

public class PagingProgressPanel extends JPanel {

    private JProgressBar bar;
    private JLabel label;

    public PagingProgressPanel() {
        super(new BorderLayout());
        init();
    }

    private void init() {
        bar = new JProgressBar(0, 100);
        label = new JLabel("No running pages");

        this.add(label, BorderLayout.NORTH);
        this.add(bar, BorderLayout.SOUTH);
    }

    public JProgressBar getProgressBar() {
        return bar;
    }

    public JLabel getLabel() {
        return label;
    }
}
