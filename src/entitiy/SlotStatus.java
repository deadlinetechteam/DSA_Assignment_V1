/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entitiy;

/**
 *
 * @author asus-z
 */
public class SlotStatus {

    private String timeLabel;   // "08:30"
    private boolean isAvailable;
    private String reason;      // "OK", "Closed", "Past", "Occupied"

    public SlotStatus(String timeLabel, boolean isAvailable, String reason) {
        this.timeLabel = timeLabel;
        this.isAvailable = isAvailable;
        this.reason = reason;
    }

    // Getters
    public String getTimeLabel() {
        return timeLabel;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public String getReason() {
        return reason;
    }
}
