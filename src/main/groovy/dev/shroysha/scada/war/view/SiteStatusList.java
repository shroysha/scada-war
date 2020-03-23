package dev.shroysha.scada.war.view;

import dev.shroysha.scada.ejb.ScadaSite;
import dev.shroysha.scada.ejb.ScadaUpdateListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class SiteStatusList extends JList<ScadaSite> implements ScadaUpdateListener {

    public SiteStatusList() {
        super();
        this.setCellRenderer(new ScadaListRenderer());
        this.setModel(new DefaultListModel<>());
    }

    public void addSite(ScadaSite site) {
        DefaultListModel<ScadaSite> model = (DefaultListModel<ScadaSite>) this.getModel();
        model.addElement(site);
        site.addScadaUpdateListener(this);
    }


    public void update(ScadaSite site) {
        updateSite(site);
    }

    public void updateSite(ScadaSite site) {
        for (int i = 0; i < this.getModel().getSize(); i++) {
            if (site.getId() == this.getModel().getElementAt(i).getId()) {
                this.repaint(this.getCellBounds(i, i));
                return;
            }
        }
    }

    private static class ScadaListRenderer extends JPanel implements ListCellRenderer<ScadaSite> {

        /*

        public Component getListCellRendererComponent(JList<? extends ScadaSite> list, ScadaSite value, int index, boolean isSelected, boolean cellHasFocus) {
             return new ScadaListComponent(value);
        }*/
        private JLabel label = new JLabel("");

        public ScadaListRenderer() {
            super();
            init();
        }


        public Component getListCellRendererComponent(
                JList<? extends ScadaSite> list,
                ScadaSite value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            Color color;

            final int ALPHA = 150;

            if (value.isCritical()) {
                color = new Color(255, 0, 0, ALPHA);
            } else if (value.isWarning()) {
                color = new Color(255, 165, 0, ALPHA);
            } else {
                color = new Color(0, 255, 0, ALPHA);
            }

            if (cellHasFocus) {
                color = new Color(0, 255, 255, ALPHA);
            }

            setBackground(color);

            label.setText(value.getName());
            return this;
        }
    /*
    private class ScadaListComponent extends JPanel {

        private ScadaSite site;

        public ScadaListComponent(ScadaSite site) {
            super();
            this.site = site;
            init();
        }*/

        private void init() {
            this.setLayout(new BorderLayout());
            this.setBorder(new EmptyBorder(10, 10, 10, 10));

            label = new JLabel("");

            this.add(label, BorderLayout.CENTER);
        }


        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // To change body of generated methods, choose Tools | Templates.

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp =
                    new GradientPaint(
                            0,
                            0,
                            getBackground().brighter().brighter(),
                            0,
                            getHeight(),
                            getBackground().darker());

            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
