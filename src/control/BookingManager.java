/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 *
 * @author asus-z
 */
import adt.BPlusTree;
import adt.BPlusTree.SimpleList;
import entitiy.BookingRecord;
import entitiy.Facility;
import entitiy.SlotStatus;
import entitiy.Student;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import utility.IndexHelper;

public class BookingManager {

    private final BPlusTree<String, BookingRecord> mainTree;
    private final BPlusTree<String, Facility> facilityTree;
    private final BPlusTree<String, Student> studentTree;
    private BPlusTree<String, SimpleList<String>> userIndex;
    private BPlusTree<String, SimpleList<String>> facilityIdIndex;
    private BPlusTree<String, SimpleList<String>> facilityDateIndex;
    private int nextIdNum;

    public BookingManager(BPlusTree<String, Facility> facilityTree, BPlusTree<String, Student> studentTree) {
        this.facilityTree = facilityTree;
        this.studentTree = studentTree;

        String path = "bookings.bin";
        BPlusTree<String, BookingRecord> loaded = BPlusTree.load(path);
        this.mainTree = (loaded != null) ? loaded : new BPlusTree<>(10, path);

        this.nextIdNum = calculateInitialCounter();
        rebuildAllIndexes();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (mainTree != null) {
                System.out.println("[Auto-Save] Saving booking data to disk...");
                mainTree.commit();
            }
        }));
    }

    // --- Execute appointment ---
    public boolean makeBooking(String userId, String facilityId, String date, String start, String end) {
        // Final security check: Preventing the submission of illegal timestamps via UI vulnerabilities.
        if (start.compareTo(end) >= 0) {
            return false;
        }

        Student s = studentTree.read(userId);
        if (s == null) {
            JOptionPane.showMessageDialog(null, "Student not found!");
            return false;
        }

        String bookingId = String.format("BK%03d", ++nextIdNum);
        BookingRecord record = new BookingRecord(bookingId, userId, facilityId, date, start, end, "Confirmed");

        mainTree.create(bookingId, record);
        IndexHelper.addToIndex(userIndex,userId, bookingId);
        String compositeKey = facilityId + "_" + date;
        IndexHelper.addToIndex(facilityDateIndex,compositeKey, bookingId);
        IndexHelper.addToIndex(facilityIdIndex,facilityId, bookingId);
        return true;
    }

    public void cancelBooking(String bookingId) {
        BookingRecord r = mainTree.read(bookingId);
        if (r != null) {
            IndexHelper.removeFromIndex(userIndex, r.getUserId(), bookingId);

            String compositeKey = r.getFacilityId() + "_" + r.getBookingDate();
            IndexHelper.removeFromIndex(facilityDateIndex, compositeKey, bookingId);

            IndexHelper.removeFromIndex(facilityIdIndex, r.getFacilityId(), bookingId);

            mainTree.delete(bookingId);
        }
    }

    public BPlusTree.SimpleList<BookingRecord> getAllBookings() {
        return mainTree.sort();
    }

    // --- Internal conversion tools ---
    public SimpleList<SlotStatus> getTimetable(String facilityId, String dateStr) {
        SimpleList<SlotStatus> timetable = new SimpleList<>();
        Facility f = facilityTree.read(facilityId);
        if (f == null) {
            return timetable;
        }

        // 1. Obtain facility operating hours (Format "08:00-22:00")
        String rawTime = f.getOperationTime();
        String opStart = "00:00"; // Default start
        String opEnd = "23:59";   //Default End

        if (rawTime != null && rawTime.contains("-")) {
            String[] op = rawTime.split("-");
            if (op.length >= 2) {
                opStart = op[0].trim();
                opEnd = op[1].trim();
            }
        } else {
            // If the format is incorrect, print a warning log.
            System.out.println("[Warning] Facility " + facilityId + " has invalid operation time: " + rawTime);
        }
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
        String today = sdfDate.format(new Date());
        String nowTime = sdfTime.format(new Date());

        // 3. Get all reservations for the facility on the same day.
        SimpleList<BookingRecord> dailyRecords = getBookingsByFacilityAndDate(facilityId, dateStr);

        // 4.Generate 48 time periods (00:00 to 23:30) in a loop.
        for (int i = 0; i < 48; i++) {
            String slotTime = indexToTime(i);
            boolean available = true;
            String reason = "OK";

            // Rule A: Operational Hours Check
            if (slotTime.compareTo(opStart) < 0 || slotTime.compareTo(opEnd) >= 0) {
                available = false;
                reason = "Closed";
            } // Rule B: Expiration Check (if it's today)
            else if (dateStr.equals(today) && slotTime.compareTo(nowTime) < 0) {
                available = false;
                reason = "Past";
            } // Rule C: Conflict Check
            else {
                for (int j = 0; j < dailyRecords.size(); j++) {
                    BookingRecord r = dailyRecords.get(j);
                    // If slotTime falls within the range [Start, End), then the slot is occupied.
                    if (slotTime.compareTo(r.getStartTime()) >= 0 && slotTime.compareTo(r.getEndTime()) < 0) {
                        available = false;
                        reason = "Occupied";
                        break;
                    }
                }
            }
            timetable.add(new SlotStatus(slotTime, available, reason));
        }
        return timetable;
    }

    private String indexToTime(int index) {
        int hour = index / 2;
        int min = (index % 2) * 30;
        return String.format("%02d:%02d", hour, min);
    }

    private int calculateInitialCounter() {
        SimpleList<BookingRecord> all = mainTree.sort();
        int max = 0;
        for (int i = 0; i < all.size(); i++) {
            try {
                int num = Integer.parseInt(all.get(i).getId().substring(2));
                if (num > max) {
                    max = num;
                }
            } catch (NumberFormatException e) {
            }
        }
        return max;
    }

    public boolean isRangeAvailable(String facilityId, String date, String start, String end) {
        SimpleList<SlotStatus> timetable = getTimetable(facilityId, date);

        boolean checking = false;
        for (int i = 0; i < timetable.size(); i++) {
            SlotStatus s = timetable.get(i);

            // Enter the user-selected range
            if (s.getTimeLabel().equals(start)) {
                checking = true;
            }

            if (checking) {
                // Reaching the end point (the end point itself is not included, because it is [start, end)).
                if (s.getTimeLabel().equals(end)) {
                    break;
                }
                //If even one cell within the range is unavailable (occupied, expired, or closed), it is immediately deemed invalid.
                if (!s.isAvailable()) {
                    return false;
                }

            }
        }
        return true;
    }

    public boolean isDurationValid(String start, String end) {
        int startMins = timeToMinutes(start);
        int endMins = timeToMinutes(end);
        int duration = endMins - startMins;

        return duration > 0 && duration <= 120;
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    // --- Secondary index ---
    public SimpleList<BookingRecord> getBookingsByUser(String userId) {
        SimpleList<BookingRecord> results = new SimpleList<>();
        SimpleList<String> ids = userIndex.read(userId);
        if (ids != null) {
            for (int i = 0; i < ids.size(); i++) {
                results.add(mainTree.read(ids.get(i)));
            }
        }
        return results;
    }

    private SimpleList<BookingRecord> getBookingsByFacilityAndDate(String fId, String date) {
        SimpleList<BookingRecord> results = new SimpleList<>();
        String compositeKey = fId + "_" + date;
        SimpleList<String> txIds = facilityDateIndex.read(compositeKey);
        if (txIds != null) {
            for (int i = 0; i < txIds.size(); i++) {
                BookingRecord r = mainTree.read(txIds.get(i));
                if (r != null) {
                    results.add(r);
                }
            }
        }
        return results;
    }

    private void rebuildAllIndexes() {
        this.userIndex = new BPlusTree<>(10);
        this.facilityDateIndex = new BPlusTree<>(10);
        this.facilityIdIndex = new BPlusTree<>(10);
        SimpleList<BookingRecord> all = mainTree.sort();
        for (int i = 0; i < all.size(); i++) {
            BookingRecord r = all.get(i);
            IndexHelper.addToIndex(userIndex,r.getUserId(), r.getId());

            String compositeKey = r.getFacilityId() + "_" + r.getBookingDate();
            IndexHelper.addToIndex(facilityDateIndex,compositeKey, r.getId());

            IndexHelper.addToIndex(facilityIdIndex,r.getFacilityId(), r.getId());
        }
    }

    public SimpleList<Object[]> getPopularityReport() {
        if (facilityIdIndex == null) {
            return new SimpleList<>();
        }

        // 1. Get a list of all booked facility IDs and booking records.
        SimpleList<String> bookedFacilityIds = facilityIdIndex.sortKeys();
        SimpleList<SimpleList<String>> bookingLists = facilityIdIndex.sort();

        // 2. Calculate total reservations
        int totalBookings = 0;
        for (int i = 0; i < bookingLists.size(); i++) {
            totalBookings += bookingLists.get(i).size();
        }

        // 3. Build reports：[ID, Name, Count, Percentage]
        SimpleList<Object[]> reportRows = new SimpleList<>();
        for (int i = 0; i < bookedFacilityIds.size(); i++) {
            String fId = bookedFacilityIds.get(i);
            int count = bookingLists.get(i).size();

            // Collaborate with FacilityTree to get a name
            Facility f = facilityTree.read(fId);
            String fName = (f != null) ? f.getName() : "Unknown Facility";

            double percent = (totalBookings == 0) ? 0 : (count * 100.0 / totalBookings);

            reportRows.add(new Object[]{
                fId,
                fName,
                count,
                String.format("%.2f%%", percent)
            });
        }
        return reportRows;
    }
}
