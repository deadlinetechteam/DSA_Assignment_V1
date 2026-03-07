/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entitiy;

/**
 *
 * @author asus-z
 */
public class BookingRecord {
    private String id;
    private String userId;
    private String facilityId;
    
    private String bookingDate;
    private String startTime;
    private String endTime;
    
    private String status;

    public BookingRecord(String id, String userId, String facilityId, String bookingDate, String startTime, String endTime, String status) {
        this.id = id;
        this.userId = userId;
        this.facilityId = facilityId;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "BookingRecord{" + "id=" + id + ", userId=" + userId + ", facilityId=" + facilityId + ", bookingDate=" + bookingDate + ", startTime=" + startTime + ", endTime=" + endTime + ", status=" + status + '}';
    }
    
    
}
