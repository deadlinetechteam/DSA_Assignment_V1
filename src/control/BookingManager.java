/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

/**
 *
 * @author asus-z
 */


import dao.BookingDAO;
import dao.FacilityDAO;
import entitiy.BookingRecord;
import entitiy.Facility;
import adt.BPlusTree;


public class BookingManager {
    private final BPlusTree<String,BookingRecord> mainBookingTree=new BPlusTree<>(4);
    private final BPlusTree<String,Facility> mainFacilityTree=new BPlusTree<>(4);
    private BookingDAO bookingDAO = new BookingDAO();
    private FacilityDAO facilityDAO = new FacilityDAO();

//    /**
//     * 处理设施预约逻辑
//     * 1. 验证设施是否存在且当前可用
//     * 2. 更新设施状态为 Reserved
//     * 3. 创建并持久化预约记录
//     */
    public boolean makeBooking(String userId, String facilityId, String date, String start, String end) {
        // 从 DAO 获取设施实体
        Facility f = mainFacilityTree.read(facilityId);
        
        // 业务规则校验：设施必须存在且状态为 Available
        if (f == null || !f.getStatus().equalsIgnoreCase("Available")) {
            return false;
        }

        // 1. 更新设施状态并保存（触发文件更新）
        f.setStatus("Reserved");
        mainFacilityTree.create(f.getId(),f);

        // 2. 生成唯一的预约 ID 并创建记录
        String bookingId = "BK" + System.currentTimeMillis();
        BookingRecord record = new BookingRecord(
            bookingId, userId, facilityId, date, start, end, "Confirmed"
        );
        
        // 3. 通过 DAO 存入 B+ 树并写入 .dat 文件
        mainBookingTree.create(record.getId(),record);
        return true;
    }

//    /**
//     * 取消预约逻辑
//     * 1. 释放设施状态为 Available
//     * 2. 从 B+ 树中删除预约记录（触发平衡算法）
//     */
    public boolean cancelBooking(String bookingId) {
        BookingRecord record = mainBookingTree.read(bookingId);
        if (record == null) {
            return false;
        }

        // 恢复设施可用性
        Facility f = mainFacilityTree.read(record.getFacilityId());
        if (f != null) {
            f.setStatus("Available");
            mainFacilityTree.create(f.getId(),f);
        }

        // 执行 B+ 树删除操作
        mainBookingTree.delete(bookingId);
        return true;
    }

//    /**
//     * 获取所有预约记录用于 GUI 展示
//     */
    public BPlusTree.SimpleList<BookingRecord> getAllBookings() {
        return mainBookingTree.sort();
    }
}