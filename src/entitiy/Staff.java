/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entitiy;

/**
 *
 * @author asus-z
 */
public class Staff {
    private String Id;
    private String Name;
    private String Password;
    private String location;
    private String department;
    private String Gender;
    private String Email;

    public Staff(String Id, String Name, String Password, String location, String department, String Gender, String Email) {
        this.Id = Id;
        this.Name = Name;
        this.Password = Password;
        this.location = location;
        this.department = department;
        this.Gender = Gender;
        this.Email = Email;
    }

    public String getId() {
        return Id;
    }

    public String getName() {
        return Name;
    }

    public String getPassword() {
        return Password;
    }

    public String getLocation() {
        return location;
    }

    public String getDepartment() {
        return department;
    }

    public String getGender() {
        return Gender;
    }

    public String getEmail() {
        return Email;
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public void setPassword(String Password) {
        this.Password = Password;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setGender(String Gender) {
        this.Gender = Gender;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    @Override
    public String toString() {
        return "Staff{" + "Id=" + Id + ", Name=" + Name + ", location=" + location + ", department=" + department + ", Gender=" + Gender + ", Email=" + Email + '}';
    }
    
}
