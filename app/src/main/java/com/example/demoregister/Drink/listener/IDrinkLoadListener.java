package com.example.demoregister.Drink.listener;

import com.example.demoregister.Drink.DrinkModel;

import java.util.List;

public interface IDrinkLoadListener {
    void onDrinkLoadSuccess(List<DrinkModel> drinkModelList);
    void onDrinkLoadFailed(String message);
}
