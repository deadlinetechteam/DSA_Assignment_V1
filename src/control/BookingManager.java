/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 *
 * @author asus-z
 */
import entitiy.BookingRecord;
import entitiy.Facility;
import adt.BPlusTree;

public class BookingManager {

    private final BPlusTree<String, BookingRecord> mainTree;
    private final BPlusTree<String, Facility> facilityTree;

    public BookingManager(BPlusTree<String, Facility> sharedFacilityTree) {
        this.facilityTree = sharedFacilityTree;
        String path = "bookings.bin";
        BPlusTree<String, BookingRecord> loadedTree = BPlusTree.load(path);

        if (loadedTree != null) {
            this.mainTree = loadedTree;
        } else {
            this.mainTree = new BPlusTree<>(10, path);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (mainTree != null) {
                   System.out.println("[Auto-Save] Saving booking record data to disk...");
                mainTree.commit();
            }
        }));
    }

    public boolean makeBooking(String userId, String facilityId, String date, String start, String end) {
        Facility f = facilityTree.read(facilityId);
        if (f == null) {
            System.out.println("Booking Failed: Facility not found.");
            return false;
        }

        if (!"Available".equalsIgnoreCase(f.getStatus())) {
            System.out.println("Booking Failed: Facility is currently " + f.getStatus());
            return false;
        }

        BPlusTree.SimpleList<BookingRecord> allRecords = mainTree.sort();
        for (int i = 0; i < allRecords.size(); i++) {
            BookingRecord r = allRecords.get(i);
            if (r.getFacilityId().equals(facilityId) && r.getBookingDate().equals(date)) {
                // 重叠判断算法：(新开始 < 已存结束) 且 (新结束 > 已存开始)
                if (start.compareTo(r.getEndTime()) < 0 && end.compareTo(r.getStartTime()) > 0) {
                    System.out.println("Booking Conflict: Time slot already reserved.");
                    return false;
                }
            }
        }

        // 4. 执行预约
        String bookingId = "BK" + System.currentTimeMillis();
        BookingRecord newRecord = new BookingRecord(
                bookingId, userId, facilityId, date, start, end, "Confirmed"
        );

        // 5. 存入树中
        mainTree.create(bookingId, newRecord);

        // 6. 联动修改：如果你的业务要求预约后设施变为 "Reserved"，可在此修改
        f.setStatus("Reserved");
        facilityTree.update(facilityId, f);
        // 7. 提交持久化
        mainTree.commit();
        // facilityTree.commit(); // 如果修改了设施状态，记得也提交它

        return true;
    }

    public boolean cancelBooking(String bookingId) {
        BookingRecord r = mainTree.read(bookingId);
        if (r == null) {
            return false;
        }

        boolean deleted = mainTree.delete(bookingId);
        if (deleted) {
            mainTree.commit();
            return true;
        }
        return false;
    }

    public BPlusTree.SimpleList<BookingRecord> getAllBookings() {
        return mainTree.sort();
    }

}
