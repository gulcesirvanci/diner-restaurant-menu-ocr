package com.example.ocrGUI.controllers;

import com.example.ocrGUI.daos.CategoryDao;
import com.example.ocrGUI.daos.DishDao;
import com.example.ocrGUI.daos.MenuDao;
import com.example.ocrGUI.models.Category;
import com.example.ocrGUI.models.Dish;
import com.example.ocrGUI.models.Menu;
import com.example.ocrGUI.util.FilenamesCollection;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DishController {

    @RequestMapping("/list-menu-items")
    public String listMenuItems(Model model, @RequestParam("menuId") int menuId, @RequestParam("filename") List<String> filenames){
        Resource r=new ClassPathResource("applicationContext.xml");
        BeanFactory factory=new XmlBeanFactory(r);
        MenuDao menuDao = (MenuDao) factory.getBean("mdao");

        Menu menu = menuDao.selectMenu(menuId).get(0);
        model.addAttribute("menuId", menuId);
        model.addAttribute("menu", menu);
        String[] filenameArray = new String[filenames.size()];
        filenames.toArray(filenameArray);
        model.addAttribute("filenames", filenameArray);

        return "menuItemsUpdatePage";
    }

    @RequestMapping("/edit-menu-item")
    public String editMenuItem(Model model, @RequestParam("menuId") int menuId,
                                               @RequestParam("dishId") int dishId,
                                               @RequestParam("name") String name,
                                               @RequestParam("category") String category,
                                               @RequestParam("description") String description,
                                               @RequestParam("price") String price,
                                               @RequestParam("filename") List<String> filenames){

        Resource r=new ClassPathResource("applicationContext.xml");
        BeanFactory factory=new XmlBeanFactory(r);
        DishDao dishDao = (DishDao) factory.getBean("ddao");
        MenuDao menuDao = (MenuDao) factory.getBean("mdao");
        CategoryDao categoryDao = (CategoryDao) factory.getBean("cdao");
        int oldCategoryID = -1;

        Dish item = dishDao.selectDish(dishId).get(0);
        oldCategoryID = item.getCategoryID();
        item.setCategoryID(Integer.parseInt(category));
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        dishDao.updateDish(item);

        Category oldCategory = categoryDao.selectCategory(oldCategoryID).get(0);
        oldCategory.removeDish(item);
        categoryDao.updateCategory(oldCategory);

        Category newCategory = categoryDao.selectCategory(item.getCategoryID()).get(0);
        newCategory.addDish(item);
        categoryDao.updateCategory(newCategory);
        System.out.println(filenames);
        item = dishDao.selectDish(dishId).get(0);
        Menu currentMenu = menuDao.selectMenu(menuId).get(0);
        model.addAttribute("menu", currentMenu);
        model.addAttribute("menuId", menuId);
        String[] filenameArray = new String[filenames.size()];
        filenames.toArray(filenameArray);
        model.addAttribute("filenames", filenameArray);
        for(int i = 0; i<filenameArray.length; i++){
            System.out.println(filenameArray[i]);
        }
        return "menuItemsUpdatePage";

    }

    @RequestMapping("/add-menu-item")
    public String addMenuItem(Model model, @RequestParam("menuId") int menuId,
                                            @RequestParam("name") String name,
                                            @RequestParam("category") int category,
                                            @RequestParam("description") String description,
                                            @RequestParam("price") String price,
                                            @RequestParam("filename") List<String> filenames){

        Resource r=new ClassPathResource("applicationContext.xml");
        BeanFactory factory=new XmlBeanFactory(r);
        DishDao dishDao = (DishDao) factory.getBean("ddao");
        MenuDao menuDao = (MenuDao) factory.getBean("mdao");
        CategoryDao categoryDao = (CategoryDao) factory.getBean("cdao");

        Category oldCategory = categoryDao.selectCategory(category).get(0);
        Dish dish = new Dish(category, name);
        dish.setPrice(price);
        dish.addToDescription(description);
        dishDao.insertDish(oldCategory.getCategoryID(), dish);

        oldCategory.addDish(dish);
        categoryDao.updateCategory(oldCategory);
        Menu menu = menuDao.selectMenu(menuId).get(0);
        model.addAttribute("menuId", menuId);
        model.addAttribute("menu", menu);
        String[] filenameArray = new String[filenames.size()];
        filenames.toArray(filenameArray);
        model.addAttribute("filenames", filenameArray);
        return "menuItemsUpdatePage";
    }

    @RequestMapping("/delete-menu-item")
    public String deleteMenuItem(Model model, @RequestParam("dishId") int dishId, @RequestParam("menuId") int menuId, @RequestParam("filename") List<String> filenames){
        Resource r=new ClassPathResource("applicationContext.xml");
        BeanFactory factory=new XmlBeanFactory(r);
        DishDao dishDao = (DishDao) factory.getBean("ddao");
        MenuDao menuDao = (MenuDao)factory.getBean("mdao");
        CategoryDao categoryDao = (CategoryDao) factory.getBean("cdao");

        Dish dish = dishDao.selectDish(dishId).get(0);
        int categoryId = dish.getCategoryID();
        Category category = categoryDao.selectCategory(categoryId).get(0);
        category.removeDish(dish);
        categoryDao.updateCategory(category);
        dishDao.deleteDish(dish);

        Menu menu = menuDao.selectMenu(menuId).get(0);
        model.addAttribute("menuId", menuId);
        model.addAttribute("menu", menu);
        String[] filenameArray = new String[filenames.size()];
        filenames.toArray(filenameArray);
        model.addAttribute("filenames", filenameArray);

        return "menuItemsUpdatePage";
    }

    private String[] processFilenames(String filenames){
        filenames = filenames.substring(1, filenames.length()-1);
        String[] filenameList = filenames.split(",");
        for(int i = 0; i < filenameList.length; i++)
            filenameList[i] = filenameList[i].trim();
        return filenameList;
    }
}
