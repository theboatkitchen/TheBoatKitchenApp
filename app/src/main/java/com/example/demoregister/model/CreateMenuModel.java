package com.example.demoregister.model;

public class CreateMenuModel {
    String menuID;
    String menuName;
    String description;
    String category;
    String price;
    String menuImage;
    String empid;
    String availability;


    public CreateMenuModel(){

    }


    public CreateMenuModel(String menuID, String menuName,String description, String category, String price, String menuImage, String empid, String availability) {
        this.menuID = menuID;
        this.menuName = menuName;
        this.description = description;
        this.category = category;
        this.price = price;
        this.menuImage = menuImage;
        this.empid = empid;
        this.availability = availability;
    }

    public String getMenuID() {
        return menuID;
    }

    public void setMenuID(String menuID) {
        this.menuID = menuID;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getMenuImage() {
        return menuImage;
    }

    public void setMenuImage(String menuImage) {
        this.menuImage = menuImage;
    }

    public String getEmpid() {
        return empid;
    }

    public void setEmpid(String empid) {
        this.empid = empid;
    }


    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }


}
