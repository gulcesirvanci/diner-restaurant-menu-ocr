package com.example.ocrGUI.models;

import com.example.ocrGUI.daos.DishDao;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;

public class Category {

    private String name;

    private ArrayList<Dish> dishes;

    private int categoryID = 0;

    private int menuID;

    public Category(int menuID, String name){
        this.name = name;
        this.menuID = menuID;
        this.dishes = new ArrayList<>();
    }

    public void addDish(Dish dish){ this.dishes.add(dish); }

    public void removeDish(Dish dish){ this.dishes.remove(dish); }

    public ArrayList<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(int[] dishIdList){

        Resource r=new ClassPathResource("applicationContext.xml");
        BeanFactory factory=new XmlBeanFactory(r);
        DishDao dishDao = (DishDao)factory.getBean("ddao");

        if(dishIdList != null){
            for(int i = 0; i< dishIdList.length; i++){
                List<Dish> dishes = dishDao.selectDish(dishIdList[i]);
                if(dishes.size() != 0)
                    this.addDish(dishes.get(0));
            }
        }
    }

    public int getMenuID(){ return  this.menuID; }

    public void setMenuID(int menuID){ this.menuID = menuID; }

    public String getName(){ return this.name; }

    public void setName(String name){ this.name = name; }

    public int getCategoryID(){ return this.categoryID; }

    public void setCategoryID(int categoryID){ this.categoryID = categoryID; }



}
