package com.example.demoregister.model;

public class RegisterActivityJava {

    String userid;
    String custName;
    String custGender;
    String custAge;
    String custPhone;
    String custEmail;
    String custPassword;
    String custIC;
    String accountType;
    String online;
    String custImage;

    public RegisterActivityJava() {
    }

    public RegisterActivityJava(String userid,String custName, String custGender, String custAge, String custPhone, String custEmail, String custPassword, String custIC, String accountType, String online, String custImage) {
        this.userid = userid;
        this.custName = custName;
        this.custGender = custGender;
        this.custAge = custAge;
        this.custPhone = custPhone;
        this.custEmail = custEmail;
        this.custPassword = custPassword;
        this.custIC = custIC;
        this.accountType = accountType;
        this.online = online;
        this.custImage = custImage;
    }

    public String getUserid() {
        return userid;
    }

    public String getCustName() {
        return custName;
    }

    public String getCustGender() {
        return custGender;
    }

    public String getCustAge() {
        return custAge;
    }

    public String getCustPhone() {
        return custPhone;
    }

    public String getCustEmail() {
        return custEmail;
    }

    public String getCustPassword() {
        return custPassword;
    }

    public String getCustIC() {
        return custIC;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getOnline() {
        return online;
    }

    public String getCustImage() {
        return custImage;
    }

}
