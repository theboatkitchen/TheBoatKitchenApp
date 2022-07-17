package com.example.demoregister.model;

public class ModelCartItem {

    String cartid;
    String menuId;
    String name;
    float totalprice;
    float cost;
    int quantity;
    String image;
    String userid;

    public ModelCartItem() {
    }

    public ModelCartItem(String cartid, String menuId, String name, float totalprice, float cost, int quantity, String image, String userid) {
        this.cartid = cartid;
        this.menuId = menuId;
        this.name = name;
        this.totalprice = totalprice;
        this.cost = cost;
        this.quantity = quantity;
        this.image = image;
        this.userid = userid;
    }

    public String getCartid() {
        return cartid;
    }

    public void setCartid(String cartid) {
        this.cartid = cartid;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getTotalprice() {
        return totalprice;
    }

    public void setTotalprice(float totalprice) {
        this.totalprice = totalprice;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
