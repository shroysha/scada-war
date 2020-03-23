

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dev.shroysha.scada.war.controller;

import dev.shroysha.scada.war.model.Employee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


public abstract class ShutdownSecurity {

    public static Employee.Pin[] getPins() throws IOException {
        ArrayList<Employee.Pin> pins = new ArrayList<>();
        File pinFile = new File(ServerUtilities.getBaseDirectory() + "/security/pins.csv");
        if (!pinFile.exists()) {
            pinFile.createNewFile();
        }

        Scanner scanner = new Scanner(pinFile);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] tokens = line.split(",");
            Employee.Pin newPin = new Employee.Pin(tokens[0], tokens[1]);
            pins.add(newPin);
        }

        return pins.toArray(new Employee.Pin[0]);
    }
}
