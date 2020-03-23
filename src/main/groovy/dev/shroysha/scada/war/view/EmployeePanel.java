package dev.shroysha.scada.war.view;

import dev.shroysha.scada.war.util.ScadaUtilities;
import dev.shroysha.scada.war.model.Employee;
import dev.shroysha.scada.war.controller.EmployeeFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class EmployeePanel extends JPanel {

    private final int[] daysOfWeek = {
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY
    };

    private final EmployeeFactory handler;

    private EmployeeDayPanel[] dayPanels;

    public EmployeePanel(EmployeeFactory handler) {
        super(new BorderLayout());
        this.handler = handler;
        init();
    }

    private void init() {
        String[] namesOfDaysOfWeek = ScadaUtilities.getDaysOfWeek();
        dayPanels = new EmployeeDayPanel[daysOfWeek.length];
        JTabbedPane weekTabbed = new JTabbedPane();
        for (int i = 0; i < daysOfWeek.length; i++) {
            dayPanels[i] = new EmployeeDayPanel(daysOfWeek[i]);
            weekTabbed.addTab(namesOfDaysOfWeek[i], dayPanels[i]);
            System.out.println(daysOfWeek[i]);
        }
        this.add(weekTabbed, BorderLayout.CENTER);
    }

    private void updateLists() {
        for (EmployeeDayPanel panel : dayPanels) {
            panel.updateList();
        }
    }

    /**
     * Displays all the employees that work on the day imputed into the constructor. They are sorted
     * by priority
     */
    private class EmployeeDayPanel extends JPanel {

        private final int dayOfWeek;

        private EmployeeList list;

        public EmployeeDayPanel(int dayOfWeek) {
            super(new BorderLayout());
            this.dayOfWeek = dayOfWeek;
            init();
        }

        private void init() {
            this.setBorder(new EmptyBorder(10, 10, 10, 10));

            JScrollPane scroller = new JScrollPane();
            list = new EmployeeList();
            updateList();

            JButton upButton = new JButton("^");
            upButton.addActionListener(
                    ae -> {
                        int sIndex = list.getSelectedIndex();
                        int tIndex = sIndex - 1;

                        if (sIndex != -1) {
                            if (tIndex >= 0) {
                                ListModel lm = list.getModel();
                                Employee[] array = new Employee[lm.getSize()];
                                for (int i = 0; i < array.length; i++) {
                                    array[i] = (Employee) lm.getElementAt(i);
                                }

                                array[sIndex].goUpPriority();
                                array[tIndex].goDownPriority();

                                updateLists();
                            } else {
                                System.out.println("OOB");
                            }
                        } else {
                            System.out.println("Nothing selected");
                        }
                    });

            JButton downButton = new JButton("v");
            downButton.addActionListener(
                    ae -> {
                        int sIndex = list.getSelectedIndex();
                        int tIndex = sIndex + 1;

                        if (sIndex != -1) {
                            if (tIndex < list.getModel().getSize()) {
                                ListModel lm = list.getModel();
                                Employee[] array = new Employee[lm.getSize()];
                                for (int i = 0; i < lm.getSize(); i++) {
                                    array[i] = (Employee) lm.getElementAt(i);
                                }

                                array[sIndex].goDownPriority();
                                array[tIndex].goUpPriority();

                                updateLists();
                            }
                        }
                    });

            JButton addButton = new JButton("Add");
            addButton.addActionListener(
                    ae -> {
                        Employee newEmp = new Employee();
                        handler.getAllEmployees().add(newEmp);
                        EmployeeEditDialog dialog = new EmployeeEditDialog(newEmp);
                        dialog.setVisible(true);
                        System.out.println("HIT");
                        if (newEmp.getName() == null) {
                            handler.getAllEmployees().remove(newEmp);
                        } else {
                            updateLists();
                        }
                    });

            JButton removeButton = new JButton("Remove");
            removeButton.addActionListener(
                    ae -> {
                        int index = list.getSelectedIndex();
                        if (index != -1) {
                            ListModel lm = list.getModel();
                            Employee[] array = new Employee[lm.getSize() - 1];
                            Employee delete = (Employee) lm.getElementAt(index);

                            handler.getAllEmployees().remove(delete);
                            updateLists();
                        }
                    });

            scroller.setViewportView(list);

            JPanel buttonPanel = new JPanel(new GridLayout(2, 1));
            buttonPanel.add(upButton);
            buttonPanel.add(downButton);

            JPanel buttonPanel2 = new JPanel(new GridLayout(1, 2));
            buttonPanel2.add(addButton);
            buttonPanel2.add(removeButton);

            this.add(scroller, BorderLayout.CENTER);
            this.add(buttonPanel, BorderLayout.EAST);
            this.add(buttonPanel2, BorderLayout.SOUTH);
        }

        private void updateList() {
            ArrayList<Employee> onDay = new ArrayList<>();
            for (Employee employee : handler.getAllEmployees()) {
                if (employee.getDayWorking() == dayOfWeek) {
                    onDay.add(employee);
                }
            }

            EmployeeFactory.sortByPriority(onDay);

            rectifyPriority(onDay);

            list.setListData(onDay.toArray(new Employee[0]));

            handler.save();
        }

        private void rectifyPriority(ArrayList<Employee> emps) {
            for (int i = 0; i < emps.size(); i++) {
                emps.get(i).setPriority(i);
            }
        }

        private class EmployeeList extends JList<Employee> {

            private final String ACTION_KEY = "ACTION-ALERTED";

            public EmployeeList() {
                super();
                this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ACTION_KEY);

                //  Add the Action to the ActionMap
                Action action = new AbstractAction() {
                    public void actionPerformed(ActionEvent ae) {
                        int index = EmployeeList.this.getSelectedIndex();
                        System.out.println("ACTION");
                        if (index != -1) {
                            System.out.println("INDEX");
                            Employee employee = EmployeeList.this.getModel().getElementAt(index);
                            EmployeeEditDialog frame = new EmployeeEditDialog(employee);
                            frame.setVisible(true);
                        }
                    }
                };
                this.getActionMap().put(ACTION_KEY, action);

                //  Handle mouse double click

                this.addMouseListener(
                        new MouseAdapter() {

                            public void mouseClicked(MouseEvent e) {

                                if (e.getClickCount() == 2) {
                                    Action action = EmployeeList.this.getActionMap().get(ACTION_KEY);

                                    if (action != null) {
                                        ActionEvent event =
                                                new ActionEvent(EmployeeList.this, ActionEvent.ACTION_PERFORMED, "");
                                        action.actionPerformed(event);
                                    }
                                }
                            }
                        });
                this.setCellRenderer(new EmployeeListRenderer());
            }

            private class EmployeeListRenderer extends JPanel implements ListCellRenderer<Employee> {

                private JLabel nameLabel, pagerLabel, startLabel, stopLabel, emailLabel;

                public EmployeeListRenderer() {
                    super(new BorderLayout());
                    init();
                }

                private void init() {
                    JPanel temp = new JPanel(new GridLayout(2, 2));
                    temp.setOpaque(false);
                    nameLabel = new JLabel();
                    pagerLabel = new JLabel();
                    startLabel = new JLabel();
                    stopLabel = new JLabel();
                    emailLabel = new JLabel();

                    temp.add(nameLabel);
                    temp.add(pagerLabel);
                    temp.add(startLabel);
                    temp.add(stopLabel);

                    this.add(temp, BorderLayout.CENTER);
                    this.add(emailLabel, BorderLayout.SOUTH);

                    this.setPreferredSize(new Dimension(400, 50));
                }


                protected void paintComponent(Graphics grphcs) {
                    super.paintComponent(grphcs);

                    Graphics2D g2d = (Graphics2D) grphcs;
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

                public Component getListCellRendererComponent(JList<? extends Employee> jList, Employee o, int index, boolean isSelected, boolean b1) {
                    Color background;
                    Color foreground;

                    // check if this cell represents the current DnD drop location
                    JList.DropLocation dropLocation = jList.getDropLocation();
                    if (dropLocation != null
                            && !dropLocation.isInsert()
                            && dropLocation.getIndex() == index) {

                        background = Color.BLACK;
                        foreground = Color.WHITE;

                        // check if this cell is selected
                    } else if (isSelected) {
                        background = Color.CYAN;
                        foreground = Color.WHITE;

                        // unselected, and not the DnD drop location
                    } else {
                        background = Color.WHITE;
                        foreground = Color.BLACK;
                    }

                    nameLabel.setText("Name: " + o.getName());
                    pagerLabel.setText("Pager:" + o.getPager());
                    startLabel.setText("Shift Start: " + o.getStartTime().toString());
                    stopLabel.setText("Shift Stop:" + o.getStopTime().toString());
                    emailLabel.setText("Email: " + o.getEmail());

                    setBackground(background);
                    setForeground(foreground);

                    repaint();

                    return this;
                }

            }
        }

        private class EmployeeEditDialog extends JDialog {

            private final Employee employee;

            private final JLabel nameLabel = new JLabel("Employee Name"),
                    pagerLabel = new JLabel("Pager ID"),
                    emailLabel = new JLabel("Email Address"),
                    startHourLabel = new JLabel("Time of Start of Shift"),
                    stopHourLabel = new JLabel("Time of End of Shift"),
                    dayWorkingLabel = new JLabel("Day Working");
            private final JTextField nameArea;
            private final JTextField pagerIDArea;
            private final JTextField emailArea;
            private final JComboBox<String> dayWorkingCombo;
            private JSpinner startTimeSpinner, stopTimeSpinner;

            public EmployeeEditDialog(Employee employee) {
                super((Dialog) null, "Edit Employee", true);
                this.employee = employee;

                nameArea = new JTextField(employee.getName());
                pagerIDArea = new JTextField(employee.getPager());
                emailArea = new JTextField(employee.getEmail());

                DateFormat format = new SimpleDateFormat("HH:mm");
                try {
                    Date start = format.parse(employee.getStartTime().toString());
                    Date stop = format.parse(employee.getStopTime().toString());

                    Date minDate = format.parse("00:00");
                    Date maxDate = format.parse("23:59");
                    SpinnerDateModel startModel =
                            new SpinnerDateModel(start, minDate, maxDate, Calendar.MINUTE);
                    SpinnerDateModel stopModel =
                            new SpinnerDateModel(stop, minDate, maxDate, Calendar.MINUTE);
                    startTimeSpinner = new JSpinner(startModel);
                    stopTimeSpinner = new JSpinner(stopModel);
                    JSpinner.DateEditor de = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
                    startTimeSpinner.setEditor(de);
                    de = new JSpinner.DateEditor(stopTimeSpinner, "HH:mm");
                    stopTimeSpinner.setEditor(de);
                } catch (ParseException ex) {
                    ex.printStackTrace(System.err);
                }

                dayWorkingCombo = new JComboBox<>(ScadaUtilities.getDaysOfWeek());
                dayWorkingCombo.setSelectedIndex(employee.getDayWorking() - 1);

                init();
            }

            private void init() {
                this.setLayout(new BorderLayout());

                JPanel contentPanel = new JPanel(new BorderLayout());
                contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

                JPanel changePanel = new JPanel(new GridLayout(6, 2, 15, 15));

                changePanel.add(nameLabel);
                changePanel.add(nameArea);

                changePanel.add(pagerLabel);
                changePanel.add(pagerIDArea);

                changePanel.add(emailLabel);
                changePanel.add(emailArea);

                changePanel.add(startHourLabel);
                changePanel.add(startTimeSpinner);

                changePanel.add(stopHourLabel);
                changePanel.add(stopTimeSpinner);

                changePanel.add(dayWorkingLabel);
                changePanel.add(dayWorkingCombo);

                JButton saveButton = new JButton("Save");
                saveButton.addActionListener(
                        ae -> save());

                contentPanel.add(changePanel, BorderLayout.CENTER);
                contentPanel.add(saveButton, BorderLayout.SOUTH);

                this.add(contentPanel, BorderLayout.CENTER);

                this.pack();
                this.setMinimumSize(this.getSize());
            }

            private void save() {
                String name = nameArea.getText();
                String page = pagerIDArea.getText();
                String email = emailArea.getText();
                double startHour = getStartHour();
                double stopHour = getStopHour();
                int dayWorking = dayWorkingCombo.getSelectedIndex() + 1;

                if (name.equals("")) {
                    JOptionPane.showMessageDialog(
                            this, "Must enter employee name", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (page.equals("")) {
                    JOptionPane.showMessageDialog(
                            this, "Must enter pager ID", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (startHour >= stopHour) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Start of shift must be less than end of shift",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (dayWorking == 0) {
                    JOptionPane.showMessageDialog(
                            this, "Must choose a day to work", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                employee.setName(name);
                employee.setPager(page);
                employee.setEmail(email);
                employee.setDayWorking(dayWorking);
                handler.save();
                this.dispose();
                updateLists();
            }

            private double getStartHour() {
                Calendar cal = getCal(startTimeSpinner);
                return getTime(cal);
            }

            private double getStopHour() {
                Calendar cal = getCal(stopTimeSpinner);
                return getTime(cal);
            }

            private Calendar getCal(JSpinner spinner) {
                Calendar cal = Calendar.getInstance();
                Date date = ((SpinnerDateModel) spinner.getModel()).getDate();
                cal.setTime(date);
                return cal;
            }

            private double getTime(Calendar cal) {
                double hour = cal.get(Calendar.HOUR_OF_DAY);
                double minute = cal.get(Calendar.MINUTE);
                return hour + (minute / 60.0);
            }
        }
    }
}
