/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utility;

/**
 *
 * @author asus-z
 */
import control.*;

public class GlobalManager {

    private static BookManager bookManager;
    private static StudentManager studentManager;
    private static StaffManager staffManager;
    private static FacilityManager facilityManager;
    private static BorrowManager borrowManager;
    private static BookingManager bookingManager;

    public static void init() {
        if (bookManager == null) {
            bookManager = new BookManager();
            studentManager = new StudentManager();
            staffManager = new StaffManager();
            facilityManager = new FacilityManager();

            borrowManager = new BorrowManager(bookManager.getTree(), studentManager.getTree());
            bookingManager = new BookingManager(facilityManager.getTree(),studentManager.getTree());
        }
    }

    public static BookManager getBookManager() {
        return bookManager;
    }

    public static StudentManager getStudentManager() {
        return studentManager;
    }

    public static StaffManager getStaffManager() {
        return staffManager;
    }

    public static FacilityManager getFacilityManager() {
        return facilityManager;
    }

    public static BorrowManager getBorrowManager() {
        return borrowManager;
    }

    public static BookingManager getBookingManager() {
        return bookingManager;
    }
}
