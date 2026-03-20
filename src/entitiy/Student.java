/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entitiy;

import java.io.Serializable;

/**
 *
 * @author asus-z
 */
public class Student implements Serializable {

    private String Id;
    private String Name;
    private String Password;
    private String Gender;
    private String MykadNO;
    private String Email;
    private String Programme;
    private String Address;
    private String ContactNo;
    private static final long serialVersionUID = 1L;

    public Student(String Id, String Name, String Password, String Gender, String MykadNO, String Email, String Programme, String Address, String ContactNo) {
        this.Id = Id;
        this.Name = Name;
        this.Password = Password;
        this.Gender = Gender;
        this.MykadNO = MykadNO;
        this.Email = Email;
        this.Programme = Programme;
        this.Address = Address;
        this.ContactNo = ContactNo;
    }

    public String getName() {
        return Name;
    }

    public String getId() {
        return Id;
    }

    public String getGender() {
        return Gender;
    }

    public String getMykadNO() {
        return MykadNO;
    }

    public String getEmail() {
        return Email;
    }

    public String getProgramme() {
        return Programme;
    }

    public String getAddress() {
        return Address;
    }

    public String getContactNo() {
        return ContactNo;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getPassword() {
        return Password;
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    public void setGender(String Gender) {
        this.Gender = Gender;
    }

    public void setMykadNO(String MykadNO) {
        this.MykadNO = MykadNO;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    public void setProgramme(String Programme) {
        this.Programme = Programme;
    }

    public void setAddress(String Address) {
        this.Address = Address;
    }

    public void setContactNo(String ContactNo) {
        this.ContactNo = ContactNo;
    }

    public void setPassword(String Password) {
        this.Password = Password;
    }

    @Override
    public String toString() {
        return "student{" + "Id=" + Id + ", Gender=" + Gender + ", MykadNO=" + MykadNO + ", Email=" + Email + ", Programme=" + Programme + ", Address=" + Address + ", ContactNo=" + ContactNo + '}';
    }

}
