package com.example.ocrGUI.models;

import com.example.ocrGUI.daos.CategoryDao;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    private String restaurantName;

    private ArrayList<Category> menuCategories;

    private int menuID = 0;

    public Menu(String restaurantName){
        this.restaurantName = restaurantName;
        this.menuCategories = new ArrayList<>();
    }

    public String getRestaurantName(){ return this.restaurantName; }

    public void addCategory(Category category){
        menuCategories.add(category);
    }
    public void setCategories(int[] categoryIdList){
        Resource r=new ClassPathResource("applicationContext.xml");
        BeanFactory factory=new XmlBeanFactory(r);
        CategoryDao categoryDao = (CategoryDao) factory.getBean("cdao");
        for(int i = 0; i< categoryIdList.length; i++){
            List<Category> category = categoryDao.selectCategory(categoryIdList[i]);
            if(category.size() != 0){
                this.addCategory(category.get(0));
            }
        }
    }

    public ArrayList<Category> getMenuCategories() {
        return menuCategories;
    }

    public int getMenuID(){ return this.menuID; }
    public void setMenuID(int menuId){ this.menuID = menuId; }

    public void createMenuFromLabeledEntries(List<LabeledEntry> labeledEntries){
        ArrayList<LabeledEntry> priceList = new ArrayList<>();
        Category dishesWithoutCategory = new Category(menuID, "dishes-without-category");
        menuCategories.add(dishesWithoutCategory);
        Category current = dishesWithoutCategory;
        Dish currentDish = null;
        String previousLabel = "";
        for(LabeledEntry entry : labeledEntries){
            if(entry.getLabel().equals("category")){
               /* if(previousLabel.equals("category")){
                    current.setName(current.getName() + entry.getOcrText());
                }*/
                // else {
                current = new Category(menuID, entry.getOcrText());
                menuCategories.add(current);
                // }
                previousLabel = "category";
            }
            else if(entry.getLabel().equals("name")){
                currentDish = new Dish(current.getCategoryID(), entry.getOcrText());
                current.addDish(currentDish);
                previousLabel = "name";
            }
            else if(entry.getLabel().equals("description")){
                if(currentDish != null)
                    currentDish.addToDescription(entry.getOcrText());
                previousLabel = "description";
            }
            else{
                if(currentDish != null)
                    currentDish.setPrice(entry.getOcrText());
                previousLabel = "price";
            }
        }

    }

    @Override
    public String toString() {
        String menu = "";
        for(Category category : menuCategories){
            menu += category.getName()+"\n";
            for(Dish dish : category.getDishes()){
                menu += "\t"+dish.getName()+"\n";
                menu += "\t\t"+dish.getDescription()+"\n";
                menu += "\t\t\t"+dish.getPrice()+"\n";
            }
        }
        return menu;
    }
}
