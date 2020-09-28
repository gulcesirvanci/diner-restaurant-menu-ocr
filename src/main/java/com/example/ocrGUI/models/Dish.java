package com.example.ocrGUI.models;

import java.util.Objects;

public class Dish {

    private String name;

    private String price;

    private String description = "";

    private int dishID = 0;

    private int categoryID;

    public Dish(int categoryID, String name){
        this.name = name;
        this.categoryID = categoryID;
    }

    public String getName(){ return this.name; }
    public void setName(String name){ this.name = name; }
    public void changeName(String name){
        this.name = name;
    }

    public int getDishID(){ return this.dishID; }
    public void setDishID(int dishID){ this.dishID = dishID;}


    public String getPrice(){ return this.price; }
    public void setPrice(String price){ this.price = price; }

    public String getDescription(){ return this.description; }
    public void addToDescription(String description){ this.description += description + " "; }

    public int getCategoryID(){ return this.categoryID; }
    public void setCategoryID(int categoryID){ this.categoryID = categoryID; }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return dishID == dish.dishID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dishID);
    }
}
