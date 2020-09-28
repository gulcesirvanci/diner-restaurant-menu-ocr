package com.example.ocrGUI.daos;

import com.example.ocrGUI.models.Menu;

import java.util.List;

public interface MenuDao {
    void insertMenu(Menu menu);
    void updateMenu(Menu menu);
    void deleteMenu(Menu menu);
    List<Menu> selectMenu(int menuId);
}
