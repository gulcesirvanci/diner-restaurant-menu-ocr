package com.example.ocrGUI.daos;

import com.example.ocrGUI.models.Dish;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DishRowMapper implements RowMapper<Dish> {
    @Override
    public Dish mapRow(ResultSet resultSet, int i) throws SQLException {
        int id = resultSet.getInt("id");
        int categoryId = resultSet.getInt("category_id");
        String ocrText = resultSet.getString("ocr_text");
        String description = resultSet.getString("description");
        String price = resultSet.getString("price");
        Dish dish = new Dish(categoryId, ocrText);
        dish.setCategoryID(categoryId);
        dish.setDishID(id);
        dish.addToDescription(description);
        dish.setPrice(price);

        return dish;
    }
}
