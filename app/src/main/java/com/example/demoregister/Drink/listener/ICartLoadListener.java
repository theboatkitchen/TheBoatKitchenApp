package com.example.demoregister.Drink.listener;

import com.example.demoregister.Drink.CartModel;

import java.util.List;

public interface ICartLoadListener {
    void onCartLoadSuccess(List<CartModel> cartModelList);
    void onCartLoadFailed(String message);
}
