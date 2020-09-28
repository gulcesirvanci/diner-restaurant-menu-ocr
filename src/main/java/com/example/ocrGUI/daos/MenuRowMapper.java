package com.example.ocrGUI.daos;

import com.example.ocrGUI.models.Menu;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MenuRowMapper implements RowMapper<Menu> {

    @Override
    public Menu mapRow(ResultSet resultSet, int i) throws SQLException {
        String restaurantName = resultSet.getString("restaurant_name");
        int id = resultSet.getInt("id");
        int[] categories = parseCategoryIds(resultSet.getString("categories"));
        Menu menu = new Menu(restaurantName);
        menu.setCategories(categories);
        menu.setMenuID(id);
        return menu;
    }

    private int[] parseCategoryIds(String categoryIds){
        int index = categoryIds.indexOf("{");
        if(index != -1){
            categoryIds = categoryIds.substring(index+1, categoryIds.length()-1);
            String[] ids = categoryIds.split(",");
            int[] categoryIdList = new int[ids.length];
            for(int i = 0; i<ids.length; i++){
                categoryIdList[i] = Integer.parseInt(ids[i]);
            }
            return categoryIdList;
        }
        return null;
    }
}
