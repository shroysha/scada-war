package dev.shroysha.scada.war.model;


import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;


public final class Employee implements Comparable<Employee> {

    @Getter @Setter
    private String name, pager, email; // pager should equal the ip or something to send to employee
    /*
     * The time the employee starts his/her shift.
     * Expressed in 24 hour format.
     * 8:30PM would be expressed as 19.50. 19th hour of the day; halfway through the hour.
     */
    @Getter @Setter
    private int priority, dayWorking;

    @Getter @Setter
    private Time startTime, stopTime;

    public Employee() {
        super();
    }

    public Employee(
            String name,
            String pager,
            String email,
            double startHour,
            double stopHour,
            int priority,
            int dayWorking) {
        super();
        this.name = name;
        this.email = email;
        this.pager = pager;
        this.dayWorking = dayWorking;
        this.priority = priority;
    }

    public boolean isCurrentlyWorking() {
        Calendar now = Calendar.getInstance();

        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);

        double minuteInDec =
                (double) minute / 60.0; // convert minutes to decimal. 30 minutes converts to 0.5
        double adjustedHour = (double) hour + minuteInDec;
//
//        if (adjustedHour >= startTime.getTimeAsDecimal()
//                && adjustedHour
//                <= stopTime.getTimeAsDecimal()) { // if the adjusted time is in between their hours
//            return dayWorking == dayOfWeek;
//        }

        return false;
    }


    public int compareTo(Employee t) {
        return priority - t.priority;
    }

    public void goUpPriority() {
        priority--;
    }

    public void goDownPriority() {
        priority++;
    }

    public boolean hasEmail() {
        return email != null;
    }

    public boolean hasPager() {
        return pager != null;
    }

    public static class Pin {

        private final String person, pin;

        public Pin(String personName, String aPin) {
            person = personName;
            pin = aPin;
        }

        public String getPersonName() {
            return person;
        }

        public String getPin() {
            return pin;
        }
    }

    public static class Time {
        public int getHour() {
            return 0;
        }
        public int getMinute() {
            return 0;
        }
        public int getTimeAsDecimal() {
            return 0;
        }
    }
}
