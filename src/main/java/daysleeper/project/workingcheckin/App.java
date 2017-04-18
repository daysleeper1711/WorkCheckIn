package daysleeper.project.workingcheckin;

import java.io.File;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    private static final Logger LOG = Logger.getLogger(App.class.getName());
    private static final String fPath
            = System.getProperty("user.dir") + File.separator + "data" + File.separator + "chamcong";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("------------------------------------------");
        System.out.println("| Starting program check in working time |");
        System.out.println("------------------------------------------");
        File folder = new File(fPath);
        System.out.println("List of the available files:");
        for (File file : folder.listFiles()) {
            String fileName = file.getName();
            if (fileName.contains(".dat")) {
                System.out.println("- " + fileName.replace(".dat", ""));
            }
        }
        String fName;
        boolean found = false;
        do {
            System.out.print("Pls choose file: ");
            fName = sc.nextLine();
            for (File file : folder.listFiles()) {
                if (file.getName().replace(".dat", "").equals(fName)) {
                    found = true;
                    break;
                }
            }
        } while (!found);
        Service service = Service.newInstance(fPath, fName);
        String inputDate;
        if (service == null) {
            System.out.println("Exit now...");
        } else {
            System.out.println(service.showDateRange());
            do {
                System.out.print("Input date (yyyy-MM-dd): ");
                inputDate = sc.nextLine().trim();
                if (!service.checkValidDateInRange(inputDate)) {
                } else {
                    break;
                }
            } while (true);
            System.out.print("Input id: ");
            String id = sc.nextLine().trim();
            service.infoIdDetail(inputDate, id);
        }
        System.out.println("Press any key to exit...");
        sc.nextLine();
        LOG.log(Level.INFO, "Finished at {0}", new Date().toString());
    }
}
