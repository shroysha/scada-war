package dev.shroysha.scada.war.controller;

import dev.shroysha.scada.war.controller.notify.alert.Alert;
import dev.shroysha.scada.war.controller.notify.alert.AlertMonitoringSystem;
import dev.shroysha.scada.war.controller.notify.alert.OnDutyNotifier;
import dev.shroysha.scada.war.controller.notify.email.EmailSystem;
import dev.shroysha.scada.war.controller.notify.pager.PagingSystem;
import dev.shroysha.scada.war.util.ServerProperties;
import dev.shroysha.scada.war.model.Employee;
import dev.shroysha.scada.war.view.NotificationSystemPanel;


public class NotificationSystem {

    private final ServerProperties props;
    private final NotificationSystemPanel panel;
    private PagingSystem pagingSystem;
    private EmailSystem emailSystem;
    private AlertMonitoringSystem alertSystem;
    private EmployeeFactory employeeHandler;

    public NotificationSystem(ServerProperties props) {
        super();
        this.props = props;
        init();

        panel = new NotificationSystemPanel(this);
    }

    private void init() {
        employeeHandler = new EmployeeFactory();

        alertSystem = new AlertMonitoringSystem();

        pagingSystem = new PagingSystem(props);
        emailSystem = new EmailSystem(props);

        pagingSystem.setAlertMonitoringSystem(alertSystem);
        pagingSystem.setEmployeeHandler(employeeHandler);

        emailSystem.setAlertMonitoringSystem(alertSystem);
        emailSystem.setEmployeeHandler(employeeHandler);

        OnDutyNotifier onDuty = new OnDutyNotifier(this, employeeHandler);
    }

    public PagingSystem getPagingSystem() {
        return pagingSystem;
    }

    public EmailSystem getEmailSystem() {
        return emailSystem;
    }

    public AlertMonitoringSystem getAlertMonitoringSystem() {
        return alertSystem;
    }

    public EmployeeFactory getEmployeeHandler() {
        return employeeHandler;
    }

    public NotificationSystemPanel getNotificationSystemPanel() {
        return panel;
    }

    public void notify(Employee employee, Alert alert) {
        if (employee.hasEmail()) {
            emailSystem.sendEmail(alert, employee);
        }

        if (employee.hasPager()) {
            pagingSystem.page(alert, employee);
        }
    }
}
