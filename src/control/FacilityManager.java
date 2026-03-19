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
    private final BPlusTree<String,Facility> mainFacilityTree=new BPlusTree<>(4);
      private final BPlusTree<String,BookingRecord> mainBookingRecordTree=new BPlusTree<>(4);
    private FacilityDAO facilityDAO = new FacilityDAO();
    private BookingDAO bookingDAO = new BookingDAO();

     public void addFacility(Facility newFacility) {
        mainFacilityTree.create(newFacility.getId(),newFacility); 
    }

    // 根据ID查询
    public Facility readFacility(String id) {
        return mainFacilityTree.read(id);
    }
    
    // 更新书籍信息
    public void updateBook(Facility UpdatedFacility) {
        mainFacilityTree.update(UpdatedFacility.getId(),UpdatedFacility);
        
    }

     public void deleteFacility(String id) {
        mainFacilityTree.delete(id);
    }
     
    public boolean makeBooking(String userId, String facilityId, String date, String start, String end) {
        Facility f = mainFacilityTree.read(facilityId);
        
        if (f == null || !f.getStatus().equalsIgnoreCase("Available")) {
            return false;
        }

        // 修改设施状态
        f.setStatus("Reserved");
        mainFacilityTree.create(f.getId(),f);

        // 储存预约单
        String bid = "BK" + System.currentTimeMillis();
        BookingRecord br = new BookingRecord(bid, userId, facilityId, date, start, end, "Confirmed");
        mainBookingRecordTree.create(br.getId(),br);
        return true;
    }

   

    public BPlusTree.SimpleList<Facility> getAllFacilities() {
        return mainFacilityTree.sort();
    }
}