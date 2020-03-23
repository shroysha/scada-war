package dev.shroysha.scada.war.model;

import dev.shroysha.scada.war.controller.notify.alert.Alert;


public interface AlertListener {

    void alertReceived(Alert alert);
}
