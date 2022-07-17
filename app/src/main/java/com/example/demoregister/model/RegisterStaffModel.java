package com.example.demoregister.model;

public class RegisterStaffModel {
    String userid;
    String staffName;
    String staffGender;
    String staffAge;
    String staffPhone;
    String staffEmail;
    String staffPassword;
    String staffIC;
    String accountType;
    String online;
    String staffImage;

    public RegisterStaffModel() {
    }

    public RegisterStaffModel(String userid, String staffName, String staffGender, String staffAge, String staffPhone, String staffEmail, String staffPassword, String staffIC, String accountType, String online, String staffImage) {
        this.userid = userid;
        this.staffName = staffName;
        this.staffGender = staffGender;
        this.staffAge = staffAge;
        this.staffPhone = staffPhone;
        this.staffEmail = staffEmail;
        this.staffPassword = staffPassword;
        this.staffIC = staffIC;
        this.accountType = accountType;
        this.online = online;
        this.staffImage=staffImage;
    }

    public String getUserid() {
        return userid;
    }

    public String getStaffName() {
        return staffName;
    }

    public String getStaffGender() {
        return staffGender;
    }

    public String getStaffAge() {
        return staffAge;
    }

    public String getStaffPhone() {
        return staffPhone;
    }

    public String getStaffEmail() {
        return staffEmail;
    }

    public String getStaffPassword() {
        return staffPassword;
    }

    public String getStaffIC() {
        return staffIC;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getOnline() {
        return online;
    }

    public String getStaffImage() {
        return staffImage;
    }
}
