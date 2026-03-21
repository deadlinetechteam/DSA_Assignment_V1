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
import java.text.SimpleDateFormat;
import java.util.Date;

public class BookingManager {

    private final BPlusTree<String, BookingRecord> mainTree;
    private final BPlusTree<String, Facility> facilityTree; // 共享 Facility 树
    private BPlusTree<String, SimpleList<String>> userIndex; // 二级索引：UserId -> BookingIDs
    private int nextIdNum;

    public BookingManager(BPlusTree<String, Facility> facilityTree) {
        this.facilityTree = facilityTree;
        String path = "bookings.bin";
        BPlusTree<String, BookingRecord> loaded = BPlusTree.load(path);
        this.mainTree = (loaded != null) ? loaded : new BPlusTree<>(10, path);

        this.nextIdNum = calculateInitialCounter();
        rebuildUserIndex();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (mainTree != null) {
                System.out.println("[Auto-Save] Saving booking data to disk...");
                mainTree.commit();
            }
        }));
    }

    // --- 核心业务：获取时间表 (48格逻辑) ---
    public SimpleList<SlotStatus> getTimetable(String facilityId, String dateStr) {
        SimpleList<SlotStatus> timetable = new SimpleList<>();
        Facility f = facilityTree.read(facilityId);
        if (f == null) {
            return timetable;
        }

        // 1. 获取设施运营时间 (格式 "08:00-22:00")
        String rawTime = f.getOperationTime();
        String opStart = "00:00"; // 默认起始
        String opEnd = "23:59";   // 默认结束

        if (rawTime != null && rawTime.contains("-")) {
            String[] op = rawTime.split("-");
            if (op.length >= 2) {
                opStart = op[0].trim();
                opEnd = op[1].trim();
            }
        } else {
            // 如果格式不对，打印一条警告日志，方便你检查数据
            System.out.println("[Warning] Facility " + facilityId + " has invalid operation time: " + rawTime);
        }
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
        String today = sdfDate.format(new Date());
        String nowTime = sdfTime.format(new Date());

        // 3. 获取该设施当天的所有预约
        SimpleList<BookingRecord> dailyRecords = getBookingsByFacilityAndDate(facilityId, dateStr);

        // 4. 循环生成 48 个时段 (00:00 到 23:30)
        for (int i = 0; i < 48; i++) {
            String slotTime = indexToTime(i);
            boolean available = true;
            String reason = "OK";

            // 规则 A: 运营时间检查
            if (slotTime.compareTo(opStart) < 0 || slotTime.compareTo(opEnd) >= 0) {
                available = false;
                reason = "Closed";
            } // 规则 B: 过期检查 (如果是今天)
            else if (dateStr.equals(today) && slotTime.compareTo(nowTime) < 0) {
                available = false;
                reason = "Past";
            } // 规则 C: 冲突检查
            else {
                for (int j = 0; j < dailyRecords.size(); j++) {
                    BookingRecord r = dailyRecords.get(j);
                    // 只要 slotTime 落在 [Start, End) 之间，就是被占用了
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

    // --- 执行预约 ---
    public boolean makeBooking(String userId, String facilityId, String date, String start, String end) {
        // 最后的安全校验：防止通过 UI 漏洞提交非法时间
        if (start.compareTo(end) >= 0) {
            return false;
        }

        // 生成 BK001 格式 ID
        String bookingId = String.format("BK%03d", ++nextIdNum);
        BookingRecord record = new BookingRecord(bookingId, userId, facilityId, date, start, end, "Confirmed");

        mainTree.create(bookingId, record);
        addUserToIndex(userId, bookingId);
        return true;
    }

    // --- 内部转换工具 ---
    private String indexToTime(int index) {
        int hour = index / 2;
        int min = (index % 2) * 30;
        return String.format("%02d:%02d", hour, min);
    }

    private SimpleList<BookingRecord> getBookingsByFacilityAndDate(String fId, String date) {
        SimpleList<BookingRecord> results = new SimpleList<>();
        SimpleList<BookingRecord> all = mainTree.sort();
        for (int i = 0; i < all.size(); i++) {
            BookingRecord r = all.get(i);
            if (r.getFacilityId().equals(fId) && r.getBookingDate().equals(date)) {
                results.add(r);
            }
        }
        return results;
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

            // 进入用户选定的范围
            if (s.getTimeLabel().equals(start)) {
                checking = true;
            }

            if (checking) {
                // 到达结束点（结束点本身不计入，因为是 [start, end) ）
                if (s.getTimeLabel().equals(end)) {
                    break;
                }
                // 只要范围内有一个格子不可用（被占用、已过期或已关闭），直接判定非法
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

        // 必须大于 0 且小于等于 120 分钟
        return duration > 0 && duration <= 120;
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

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

    public void cancelBooking(String bookingId) {
        BookingRecord r = mainTree.read(bookingId);
        if (r != null) {
            SimpleList<String> ids = userIndex.read(r.getUserId());
            if (ids != null) {
                ids.remove(bookingId);
            }
            mainTree.delete(bookingId);
        }
    }

    public BPlusTree.SimpleList<BookingRecord> getAllBookings() {
        return mainTree.sort();
    }

    // --- 二级索引维护 (用于查看“我的预约”) ---
    private void rebuildUserIndex() {
        this.userIndex = new BPlusTree<>(10);
        SimpleList<BookingRecord> all = mainTree.sort();
        for (int i = 0; i < all.size(); i++) {
            addUserToIndex(all.get(i).getUserId(), all.get(i).getId());
        }
    }

    private void addUserToIndex(String uId, String bId) {
        SimpleList<String> ids = userIndex.read(uId);
        if (ids == null) {
            ids = new SimpleList<>();
            userIndex.create(uId, ids);
        }
        if (!ids.contains(bId)) {
            ids.add(bId);
        }
    }
}
