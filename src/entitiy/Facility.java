/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entitiy;

/**
 *
 * @author asus-z
 */
public class Facility {
    private String id;
    private String name;
    private String location;
    private String venue;
    private String venueType;
    private String operationTime;
    private int capacity;
    private String status;

    public Facility(String id, String name, String location, String venue, String venueType, String operationTime, int capacity, String status) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.venue = venue;
        this.venueType = venueType;
        this.operationTime = operationTime;
        this.capacity = capacity;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getVenue() {
        return venue;
    }

    public String getVenueType() {
        return venueType;
    }

    public String getOperationTime() {
        return operationTime;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public void setVenueType(String venueType) {
        this.venueType = venueType;
    }

    public void setOperationTime(String operationTime) {
        this.operationTime = operationTime;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Facility{" + "id=" + id + ", name=" + name + ", location=" + location + ", venue=" + venue + ", venueType=" + venueType + ", operationTime=" + operationTime + ", capacity=" + capacity + ", status=" + status + '}';
    }
    
}
