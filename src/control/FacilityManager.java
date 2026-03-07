/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 *
 * @author asus-z
 */

import dao.FacilityDAO;
import dao.BookingDAO;
import entitiy.Facility;
import entitiy.BookingRecord;
import adt.BPlusTree;

public class FacilityManager {
    private FacilityDAO facilityDAO = new FacilityDAO();
    private BookingDAO bookingDAO = new BookingDAO();

    // 预约设施
    public boolean makeBooking(String userId, String facilityId, String date, String start, String end) {
        Facility f = facilityDAO.findById(facilityId);
        
        if (f == null || !f.getStatus().equalsIgnoreCase("Available")) {
            return false;
        }

        // 修改设施状态
        f.setStatus("Reserved");
        facilityDAO.add(f);

        // 储存预约单
        String bid = "BK" + System.currentTimeMillis();
        BookingRecord br = new BookingRecord(bid, userId, facilityId, date, start, end, "Confirmed");
        bookingDAO.add(br);
        return true;
    }

    public void deleteFacility(String id) {
        facilityDAO.delete(id);
    }

    public BPlusTree.SimpleList<Facility> getAllFacilities() {
        return facilityDAO.getAll();
    }
}