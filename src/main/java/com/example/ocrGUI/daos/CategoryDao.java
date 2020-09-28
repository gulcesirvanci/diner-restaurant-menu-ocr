package com.example.ocrGUI.daos;

import com.example.ocrGUI.models.Category;
import com.example.ocrGUI.models.Dish;

import java.util.List;

public interface CategoryDao {
    void insertCategory(int menuId, Category category);
    void updateCategory(Category category);
    void deleteCategory(Category category);
    List<Category> selectCategory(int categoryId);
}
