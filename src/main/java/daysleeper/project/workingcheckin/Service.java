package daysleeper.project.workingcheckin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Service {

    private static final Logger LOG = Logger.getLogger(Service.class.getName());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private Date[] dateRange = new Date[2];
    private final Comparator<Date> comparator = new Comparator<Date>() {
        @Override
        public int compare(Date o1, Date o2) {
            return o2.compareTo(o1);
        }
    };

    private final File file;

    //-------------------------------------------------------------
    // Map id with all check in time & get methods
    //-------------------------------------------------------------
    private Map<String, List<Date>> timeCheckInFromId = new HashMap<>();

    public Map<String, List<Date>> getTimeCheckInFromId() {
        return timeCheckInFromId;
    }

    //-------------------------------------------------------------
    // Private constructor
    //-------------------------------------------------------------
    private Service(File file) {
        this.file = file;
        this.dateRange = setDateRange();
    }

    //-------------------------------------------------------------
    // New instance method
    //-------------------------------------------------------------
    public static Service newInstance(String fPath, String fName) {
        File f = new File(fPath, fName + ".dat");
        if (!f.exists()) {
            System.out.println(f.getName() + " is not existed!!!");
            return null;
        }
        return new Service(f);
    }

    //-------------------------------------------------------------
    // Check valid convert string to date with 
    // Date: yyyy-MM-dd
    // Time: HH:mm:ss (24h)
    //-------------------------------------------------------------
    private Date convertToDate(String dateInput) {
        try {
            dateFormat.setLenient(false);
            Date dateOutput = dateFormat.parse(dateInput);
            return dateOutput;
        } catch (ParseException e) {
            System.out.println(dateInput + " is not valid");
        }
        return null;
    }

    private Date convertToTime(String timeInput) {
        try {
            timeFormat.setLenient(false);
            Date timeOutput = timeFormat.parse(timeInput);
            return timeOutput;
        } catch (ParseException e) {
            System.out.println(timeInput + " is not valid");
        }
        return null;
    }

    //-------------------------------------------------------------
    // Check valid date input in range 
    //-------------------------------------------------------------
    public String showDateRange() {
        return String.format("Date must be between %s and %s",
                dateFormat.format(dateRange[0]), dateFormat.format(dateRange[1]));
    }

    private Date[] setDateRange() {
        Date[] range = new Date[2];
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.file));
            List<Date> dates = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                //--- cut out white space
                String[] components = line.trim().split("\\s+");
                //--- get date from database
                String dateDBInput = components[1];
                //--- check valid convert to date
                Date dateDB = convertToDate(dateDBInput);
                if (dateDB == null) {
                    return null;
                }
                dates.add(dateDB);
            }
            dates.sort(comparator);
            range[0] = dates.get(dates.size() - 1);
            range[1] = dates.get(0);
            return range;
        } catch (Exception e) {
        }
        return null;
    }

    public boolean checkValidDateInRange(String dateCheckInput) {
        //--- check valid convert input
        Date dateCheck = convertToDate(dateCheckInput);
        if (dateCheck == null) {
            return false;
        }
        if (dateCheck.after(dateRange[1]) || dateCheck.before(dateRange[0])) {
            LOG.log(Level.SEVERE, showDateRange());
            return false;
        } else {
            return true;
        }
    }
    //-------------------------------------------------------------
    // Set map
    //-------------------------------------------------------------

    private Map<String, List<Date>> mappingTimeCheckInById(String dateInput) {
        try {
            Date dateInputFormat = convertToDate(dateInput);
            BufferedReader br = new BufferedReader(new FileReader(this.file));
            String line;
            while ((line = br.readLine()) != null) {
                //--- cut out white space
                String[] components = line.trim().split("\\s+");
                //--- components index will be: 0- id, 1- date (yyyy-MM-dd), 2- time (hh:mm:ss)
                //--- get date from database
                String dateDBInput = components[1];
                //--- check valid convert to date
                Date dateDB = convertToDate(dateDBInput);
                if (dateDB == null) {
                    return null;
                }
                //--- condition dateInput and date from database is match
                if (dateDB.compareTo(dateInputFormat) == 0) {
                    //--- list time checkIn at date input
                    List<Date> checkInTime = new ArrayList<>();
                    //--- get Id from DB
                    String id = components[0];
                    //--- get Time from DB
                    String timeInput = components[2];
                    //--- check valid convert to time
                    Date time = convertToTime(timeInput);
                    if (time == null) {
                        return null;
                    }
                    //--- condition the id is existed in the map
                    if (!timeCheckInFromId.containsKey(id)) {
                        //--- if no: add time to checkInTime and put into the map
                        checkInTime.add(time);
                        timeCheckInFromId.put(id, checkInTime);
                    } else {
                        //--- if yes: get list from key and add time to list
                        timeCheckInFromId.get(id).add(time);
                    }
                }
            }
        } catch (IOException e) {
        }
        return timeCheckInFromId;
    }// end

    private String workingTime(Date firstCheckIn, Date lastCheckIn) {
        Long workingTime = Math.abs(firstCheckIn.getTime() - lastCheckIn.getTime());
        int totalSecond = workingTime.intValue() / 1000;
        int totalMinute = totalSecond / 60;
        int remainSecond = totalSecond % 60;
        int totalHour = totalMinute / 60;
        int remainMinute = totalMinute % 60;
        return String.format("%dh:%2dm:%2ds", totalHour, remainMinute, remainSecond);
    }

    public void infoIdDetail(String dateInput, String id) {
        Map<String, List<Date>> mapping = mappingTimeCheckInById(dateInput);
        if (mapping == null) {
            LOG.log(Level.SEVERE, "Oops... Something wrong T_T... exit now!!!");
        } else if (mapping.isEmpty()) {
            LOG.log(Level.SEVERE, "Empty file {0}", file.getAbsolutePath());
        } else if (!mapping.containsKey(id)) {
            LOG.log(Level.INFO, "Id {0} is not check-in", id);
        } else {
            List<Date> lstCheckInTime = mapping.get(id);
            lstCheckInTime.sort(comparator);
            System.out.println("---------INFO-----------");
            System.out.println("Date: " + dateInput);
            System.out.println("Id: " + id);
            //--- get first time check in
            Date firstCheckIn = lstCheckInTime.get(lstCheckInTime.size() - 1);
            //--- get last time check in
            Date lastCheckIn = lstCheckInTime.get(0);
            String firstCheckInFormat = timeFormat.format(firstCheckIn);
            String lastCheckInFormat = timeFormat.format(lastCheckIn);
            System.out.println("First check in time: " + firstCheckInFormat);
            System.out.println("Last check in time: " + lastCheckInFormat);
            if (firstCheckInFormat.equals(lastCheckInFormat)) {
                System.out.println("Id " + id + " did not check out...!!!");
            } else {
                //--- total working times
                System.out.println("Total hours working: " + workingTime(firstCheckIn, lastCheckIn));
            }
        }
    }

}
