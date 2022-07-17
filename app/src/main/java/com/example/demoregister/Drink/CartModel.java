package com.example.demoregister.Drink;

public class CartModel {

    private String key,name,image,price;
    private int quantity;
    private float totalPrice_item;
    private float totalPrice;

    public CartModel(){

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
    }

    public float getTotalPrice_item() {
        return totalPrice_item;
    }

    public void setTotalPrice_item(float totalPrice_item) {
        this.totalPrice_item = totalPrice_item;
    }
}
