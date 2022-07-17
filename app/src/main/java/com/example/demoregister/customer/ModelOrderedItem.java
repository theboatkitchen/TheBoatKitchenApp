package com.example.demoregister.customer;

public class ModelOrderedItem {

    private String menuId,menuName,image,customerID,priceEach,TotalPrice,quantity;
    //private float priceEach,TotalPrice;
    //int quantity;


    public ModelOrderedItem() {
    }

    public ModelOrderedItem(String menuId, String menuName, String image, String customerID, String priceEach, String totalPrice, String quantity) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.image = image;
        this.customerID = customerID;
        this.priceEach = priceEach;
        TotalPrice = totalPrice;
        this.quantity = quantity;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getPriceEach() {
        return priceEach;
    }

    public void setPriceEach(String priceEach) {
        this.priceEach = priceEach;
    }

    public String getTotalPrice() {
        return TotalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        TotalPrice = totalPrice;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
