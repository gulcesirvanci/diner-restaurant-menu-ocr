package com.example.ocrGUI.daos;

import com.example.ocrGUI.models.Dish;

import java.util.List;

public interface DishDao {
    void insertDish(int categoryId, Dish dish);
    void updateDish(Dish dish);
    void deleteDish(Dish dish);
    List<Dish> selectDish(int dishId);
}
