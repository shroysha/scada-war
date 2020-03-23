package dev.shroysha.scada.war

import dev.shroysha.scada.ejb.ScadaComponent
import dev.shroysha.scada.ejb.ScadaSite
import dev.shroysha.scada.ejb.ScadaUpdateListener
import dev.shroysha.scada.war.controller.ClientConnection
import dev.shroysha.scada.war.controller.notify.modem.PageWithModem
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

import javax.swing.*
import java.util.logging.Level
import java.util.logging.Logger

import static dev.shroysha.scada.ejb.ScadaComponent.ScadaDiscrete
import static dev.shroysha.scada.ejb.ScadaComponent.ScadaRegister

@SpringBootApplication
class App implements ScadaUpdateListener {

    private static final int NUM_THREADS = 1
    private static final boolean DONT_INTERRUPT_IF_RUNNING = false
    final Logger log = Logger.getGlobal()
    final ArrayList<ScadaSite> sites = new ArrayList<>()
    final File siteList = new File("SiteConfigs.dat")
    private final int DISCRETE_OFFSET = 10001
    private final int REGISTER_OFFSET = 30001
    private final int DEVICE_INFO_LINES = 4
    private final ArrayList<ClientConnection> clients = new ArrayList<>()
    int second = 0
    PageWithModem pageServ
    private String totalStatus = ""
    private JTextArea textArea
    private boolean isChecking = false
    private int jobID = 300

    App() {
        PrintWriter clientPrinter = null
        pageServ = null
        long initDelay = 5
        long delay = 5

        log.info("Starting up sites.")
        this.startUpSites()

        log.log(Level.INFO, "Started Client Listening Thread.")
        this.startClientCon()

        pageServ = new PageWithModem()
    }

    static void main(String[] args) {
        SpringApplication.run(App.class, args)
    }

    private void startClientCon() {
        Thread cc = new Thread(new ClientConnector())
        cc.start()
    }

    private void startUpSites() {
        boolean starting = true
        String name = ""
        String lon = ""
        String lat = ""
        String compName
        String compIP
        int isModBus
        ArrayList<ScadaDiscrete> discreteList = new ArrayList<>()
        ArrayList<ScadaRegister> registerList = new ArrayList<>()
        ArrayList<ScadaComponent> components = new ArrayList<>()

        try {
            Scanner scanner = new Scanner(siteList)
            int scadaID = 1

            while (scanner.hasNextLine()) {
                String stuff = scanner.nextLine()

                log.log(Level.INFO, "Processing line: {0}", stuff)

                if (stuff.equals("") || stuff.charAt(0) == '#') {
                    continue
                }

                if (stuff.contains("Site Name")) {
                    name = stuff.substring(stuff.indexOf("=") + 1).trim()
                }

                if (stuff.contains("Lat")) {
                    lat = stuff.substring(stuff.indexOf("=") + 1).trim()
                }

                if (stuff.contains("Long")) {
                    lon = stuff.substring(stuff.indexOf("=") + 1).trim()
                }

                if (stuff.contains("Device Name")) {
                    compName = stuff.substring(stuff.indexOf("=") + 1).trim()

                    String temp = scanner.nextLine()
                    compIP = temp.substring(temp.indexOf("=") + 1).trim()

                    temp = scanner.nextLine()
                    isModBus = Integer.parseInt(temp.substring(temp.indexOf("=") + 1).trim())

                    // Setup the Discrete List
                    String discretes = scanner.nextLine()
                    StringTokenizer tokenizer =
                            new StringTokenizer(discretes.substring(discretes.indexOf("=") + 1), ",\n")

                    while (tokenizer.hasMoreTokens()) {
                        int warningType = 0
                        String discreteTemp = tokenizer.nextToken()
                        String discreteName = discreteTemp.substring(0, discreteTemp.indexOf(":"))
                        discreteTemp = discreteTemp.substring(discreteTemp.indexOf(":") + 1)

                        int discretePort =
                                Integer.parseInt(discreteTemp.substring(0, discreteTemp.length() - 1))
                        String warningStr = discreteTemp.substring(discreteTemp.length() - 1)

                        if (warningStr.equals("w")) {
                            warningType = 1
                        } else if (warningStr.equals("c")) {
                            warningType = 2
                        }

                        discreteList.add(new ScadaDiscrete(discreteName, discretePort, warningType))
                    }

                    // Setup the Register List
                    String registers = in.nextLine()
                    tokenizer = new StringTokenizer(registers.substring(registers.indexOf("=") + 1), ",\n")

                    while (tokenizer.hasMoreTokens()) {
                        int warningType = 0
                        String registerTemp = tokenizer.nextToken()
                        String registerName = registerTemp.substring(0, registerTemp.indexOf(":"))
                        registerTemp = registerTemp.substring(registerTemp.indexOf(":") + 1)

                        int registerPort =
                                Integer.parseInt(registerTemp.substring(0, registerTemp.length() - 1))
                        String warningStr = registerTemp.substring(registerTemp.length() - 1)

                        if (warningStr.equals("w")) {
                            warningType = 1
                        } else if (warningStr.equals("c")) {
                            warningType = 2
                        }

                        registerList.add(new ScadaRegister(registerName, registerPort, warningType))
                    }

                    // Add the compnent to the ArrayList
                    components.add(
                            new ScadaComponent(compName, compIP, isModBus, discreteList, registerList))
                }

                // Finally, add the new site
                if (stuff.equalsIgnoreCase("end")) {
                    log.info("Reached end of site!")

                    ScadaSite site = new ScadaSite(scadaID, name, Double.parseDouble(lat), Double.parseDouble(lon))
                    sites.add(site)
                    site.addScadaUpdateListener(this)

                    scadaID++
                    name = ""
                    lat = ""
                    lon = ""
                    discreteList = new ArrayList<>()
                    registerList = new ArrayList<>()
                    components = new ArrayList<>()
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("File not found!")
        }
    }

    String getSites() {
        String info = ""
        for (ScadaSite ss : sites) {
            info += ss.toString()
        }

        return info
    }


//  private synchronized void printToClients()
//  {
//      log.info("Printing to clients.");
//      for(ClientConnection oos: clients)
//      {
//          try {
//              oos.resetOutStream();
//          } catch (IOException ex) {
//              Logger.getLogger(ScadaServer.class.getName()).log(Level.SEVERE, null, ex);
//          }
//          log.log(Level.INFO, "Printing to main.java.client:{0}", oos.getIP());
//          for(ScadaSite ss: sites)
//          {
//              try
//              {
//
//                  log.log(Level.FINE, ss.getStatus());
//                  oos.printSite(ss);
//              }catch (IOException ex)
//              {
//                  log.log(Level.SEVERE, "Printing to main.java.client:" + oos.getIP() + " failed.");
//                  log.log(Level.SEVERE, ex.toString());
//              }
//          }
//
//          try
//          {
//              oos.printString("End Sites");
//              oos.resetOutStream();
//              log.info("Sent to main.java.client: " + oos.getIP());
//          }
//          catch (IOException se)
//          {
//              log.log(Level.SEVERE, se.toString());
//              oos.connectionProblem();
//              log.log(Level.SEVERE, "Printing End Sites didn't work.");
//          }
//      }
//
//  }

    private synchronized void printToClients(ScadaSite site) {
        log.info("Printing to clients.")
        for (ClientConnection oos : clients) {
            try {
                oos.resetOutStream()
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex)
            }
            log.log(Level.INFO, "Printing to main.java.client:{0}", oos.getIP())

            try {

                log.log(Level.FINE, site.getStatus())
                oos.printSite(site)
            } catch (IOException ex) {
                log.log(Level.SEVERE, "Printing to main.java.client:" + oos.getIP() + " failed.")
                log.log(Level.SEVERE, ex.toString())
            }

            try {
                oos.printString("End Sites")
                oos.resetOutStream()
                log.info("Sent to main.java.client: " + oos.getIP())
            } catch (IOException se) {
                log.log(Level.SEVERE, se.toString())
                oos.connectionProblem()
                log.log(Level.SEVERE, "Printing End Sites didn't work.")
            }
        }
    }

    private void removeClients() {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).connectionDown()) {
                log.info("Removing: " + clients.get(i).getSocket().getInetAddress())
                clients.remove(i)
            }
        }
    }

//   synchronized void checkAllAlarms()
//  {
//      long startSec = System.currentTimeMillis()/1000;
//      log.log(Level.INFO, "Started Checking at: {0}", startSec);
//      for(ScadaSite ss: sites)
//      {
//          log.log(Level.INFO, "Checking Site:  {0}", ss.getName());
//          ss.checkAlarms();
//
//          if(pageServ != null && pageServ.isActive() && ss.isNewAlarm() && ss.getAlarm()) { // checks if it is a new alarm and critical
//              log.log(Level.WARNING, "About to page");
//              log.log(Level.WARNING, ss.getCritcialInfo());
//              pageServ.startPage(ss.getID(), ss.getCritcialInfo());
//              log.log(Level.WARNING, "Finished Paging");
//          }
//
//      }
//
//      ScadaRunner.gui.updateTree(sites);
//      long endSec = System.currentTimeMillis()/1000;
//
//      log.log(Level.INFO, "Stopped Checking at: {0}", endSec);
//
//      this.printToClients();
//      log.log(Level.INFO, "Printed to all clients.");
//      this.removeClients();
//      log.log(Level.INFO, "Removed all nonresponsive clients.");
//
//  }

    boolean isChecking() {
        return isChecking
    }

    boolean pagingOff() {
        if (pageServ != null) {
            pageServ.stop()
        } else {
            return false
        }

        return true
    }

    synchronized void switchPaging() {
        log.log(Level.INFO, "--------Switching Paging--------")

        if (!pageServ.isActive()) {
            try {
                log.log(Level.INFO, "Starting Paging Server.")
                pageServ.start()
                log.log(Level.INFO, "Returning with started main.java.server.")
            } catch (Exception ex) {
                log.log(Level.SEVERE, ex.toString())
            }
        } else {
            pageServ.stop()
        }
    }

    PageWithModem getPageServ() {
        return pageServ
    }


    void update(ScadaSite site) {
        doPagingUpdate(site)

        this.printToClients(site)
        log.log(Level.INFO, "Printed to all clients.")
        this.removeClients()
        log.log(Level.INFO, "Removed all nonresponsive clients.")
    }

    private void doPagingUpdate(ScadaSite site) {
        // the batch of alerts only comes from one site
        if (pageServ != null && pageServ.isActive()) { // checks if it is a new alarm and critical
            if (site.isCritical()) {
                log.log(Level.WARNING, "About to page")
                log.log(Level.WARNING, site.getName() + " Critical")
                pageServ.startPage(jobID, site.getName())
                log.log(Level.WARNING, "Finished Paging")
                jobID++
            }
        }
    }

    void startChecking() {
        isChecking = true
    }

    void stopChecking() {
        isChecking = false
    }

    ArrayList<ScadaSite> getScadaSites() {
        return sites
    }

    private class ClientConnector implements Runnable {

        final int port = 10000


        void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(port)
                boolean listening = true
                while (true) {
                    try {
                        ClientConnection client = new ClientConnection(serverSocket.accept())
                        clients.add(client)
                        log.log(Level.INFO, "Client at: {0}", client.getIP())
                        boolean connected = true
                    } catch (IOException e) {
                        log.log(Level.SEVERE, "Accept failed.")
                    }
                }
            } catch (IOException e) {
                log.log(Level.SEVERE, "Could not listen on port: {0}", port)
            }
        }
    }
}
