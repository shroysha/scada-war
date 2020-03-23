

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dev.shroysha.scada.war.controller.notify.alert;

import dev.shroysha.scada.war.controller.NotificationSystem;
import dev.shroysha.scada.war.model.Employee;
import dev.shroysha.scada.war.controller.EmployeeFactory;

import java.util.Calendar;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class OnDutyNotifier {

    private final NotificationSystem notification;
    private final EmployeeFactory handler;

    private ScheduledExecutorService executor;

    public OnDutyNotifier(NotificationSystem notification, EmployeeFactory handler) {
        this.notification = notification;
        this.handler = handler;
    }

    public void startOnDutyNotifications() {
        for (Employee employee : handler.getAllEmployees()) {
            Calendar now = Calendar.getInstance();
            Calendar startShift = Calendar.getInstance();

            int startHour;
            int startMinute;

            startShift.set(Calendar.DAY_OF_WEEK, employee.getDayWorking());
            startShift.set(Calendar.HOUR, employee.getStartTime().getHour());
            startShift.set(Calendar.MINUTE, employee.getStartTime().getMinute());
            executor.scheduleWithFixedDelay(new NotifyEmployee(employee), 0, 0,
                    TimeUnit.MILLISECONDS);
        }
    }

    private class NotifyEmployee implements Runnable {

        private final Employee employee;

        public NotifyEmployee(Employee employee) {
            this.employee = employee;
        }


        public void run() {
            Alert alert = new Alert(0, "You are now on duty");

            notification.notify(employee, alert);
        }

    }
}
