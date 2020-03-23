package dev.shroysha.scada.war.controller;

import dev.shroysha.scada.war.model.Employee;
import dev.shroysha.scada.war.util.ScadaUtilities;
import dev.shroysha.scada.war.view.EmployeePanel;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;


public class EmployeeFactory {

    private static final File employeeDir = new File(ScadaUtilities.getMainDirPath() + "/pagingsystem/employees/");
    private static final String[] days = ScadaUtilities.getDaysOfWeek();
    private final EmployeePanel parent;
    private ArrayList<Employee> allEmployees;
    private ScheduledExecutorService executor;

    public EmployeeFactory() {
        super();
        readEmployees();
        parent = new EmployeePanel(this);
    }

    public static void sortByPriority(Employee[] employees) {
        Arrays.sort(employees);
    }

    public static void sortByPriority(ArrayList<Employee> employeesList) {
        Collections.sort(employeesList);
    }

    private void readEmployees() {
        // if there is an error parsing employee
        int lineStop = 1;
        String fileStop = null;
        File file = null;
        try {
            if (!employeeDir.exists()) {
                employeeDir.mkdirs();
            }

            allEmployees = new ArrayList<>();

            for (int i = 0; i < days.length; i++) {

                try {
                    fileStop = days[i];
                    file = new File(employeeDir.getPath() + "/" + days[i] + ".csv");

                    Scanner scanner = new Scanner(file);
                    lineStop = 1;
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (!line.trim().equals("")) {
                            String[] tokens = line.split(",");
                            Employee employee;

                            if (tokens.length == 5) {
                                String name = tokens[0];
                                String pager = tokens[1];
                                double startTime = Double.parseDouble(tokens[2]);
                                double stopTime = Double.parseDouble(tokens[3]);
                                int priority = Integer.parseInt(tokens[4]);
                                employee = new Employee(name, pager, null, startTime, stopTime, priority, i + 1);
                            } else {
                                String name = tokens[0];
                                String pager = tokens[1];
                                String email = tokens[2];
                                double startTime = Double.parseDouble(tokens[3]);
                                double stopTime = Double.parseDouble(tokens[4]);
                                int priority = Integer.parseInt(tokens[5]);
                                if (pager.equals("null")) {
                                    pager = null;
                                }
                                if (email.equals("null")) {
                                    email = null;
                                }
                                employee = new Employee(name, pager, email, startTime, stopTime, priority, i + 1);
                            }

                            allEmployees.add(employee);
                            lineStop++;
                        }
                    }

                } catch (FileNotFoundException ex) {
                    try {
                        file.createNewFile();
                    } catch (IOException ex1) {
                        JOptionPane.showMessageDialog(parent, "Couldn't create file " + fileStop + ".csv");
                        Logger.getLogger(EmployeeFactory.class.getName()).log(Level.SEVERE, null, ex);
                        System.exit(5);
                    }
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    parent, "Error parsing employee in " + fileStop + ".csv at line " + lineStop);
            Logger.getLogger(EmployeeFactory.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(4);
        }
    }

    public ArrayList<Employee> getAllEmployees() {
        return allEmployees;
    }

    public Employee[] getAvailibleEmployees() {
        ArrayList<Employee> employees = getAllEmployees();
        ArrayList<Employee> workingEmployees = new ArrayList<>();

        for (Employee employee : employees) {
            if (employee.isCurrentlyWorking()) {
                workingEmployees.add(employee);
            }
        }

        return workingEmployees.toArray(new Employee[0]);
    }

    public Employee[] getCurrentPrioritizedEmployees() {
        Employee[] availible = getAvailibleEmployees();
        sortByPriority(availible);
        return availible;
    }

    public EmployeePanel getEmployeePanel() {
        return parent;
    }

    public void save() {
        ArrayList<ArrayList<Employee>> daysEmployees = new ArrayList<>();
        for (int i = 0; i < ScadaUtilities.getDaysOfWeek().length; i++) {
            daysEmployees.add(new ArrayList<>());
        }

        for (Employee employee : getAllEmployees()) {
            daysEmployees.get(employee.getDayWorking() - 1).add(employee);
        }

        for (int i = 0; i < days.length; i++) {
            String day = days[i];
            File file = new File(employeeDir.getPath() + "/" + day + ".csv");
            try {
                PrintWriter writer = new PrintWriter(new FileWriter(file));

                String text;
                for (Employee employee : daysEmployees.get(i)) {
                    String name = employee.getName();
                    String pager = employee.getPager();
                    String email = employee.getEmail();
                    String startHour = "" + employee.getStartTime().getTimeAsDecimal();
                    String stopHour = "" + employee.getStopTime().getTimeAsDecimal();
                    String priority = "" + employee.getPriority();
                    if (email == null) {
                        email = "null";
                    }
                    if (pager == null) {
                        pager = "null";
                    }
                    text =
                            "" + name + "," + pager + "," + email + "," + startHour + "," + stopHour + ","
                                    + priority;
                    writer.println(text);
                }

                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(EmployeeFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
