package com.example.ocrGUI.daos;

import com.example.ocrGUI.models.Category;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoryRowMapper implements RowMapper<Category> {
    @Override
    public Category mapRow(ResultSet resultSet, int i) throws SQLException {
        String ocrText = resultSet.getString("ocr_text");
        int menuId = resultSet.getInt("menu_id");
        int categoryId = resultSet.getInt("id");
        int[] itemIds = parseIdString(resultSet.getString("item_ids"));
        Category category = new Category(menuId, ocrText);
        category.setMenuID(menuId);
        category.setCategoryID(categoryId);
        category.setDishes(itemIds);

        return category;
    }

    public int[] parseIdString(String dishIds){
        int index = dishIds.indexOf("{");
        if(index != -1){
            dishIds = dishIds.substring(index+1, dishIds.length()-1);
            String[] ids = dishIds.split(",");
            int[] dishIdList = new int[ids.length];
            for(int i = 0; i<ids.length; i++){
                try{
                    dishIdList[i] = Integer.parseInt(ids[i]);
                } catch (Exception e){

                }

            }
            return dishIdList;
        }
        return null;
    }
}
