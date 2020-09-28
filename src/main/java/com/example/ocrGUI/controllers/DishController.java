package com.example.ocrGUI.controllers;

import com.example.ocrGUI.daos.CategoryDao;
import com.example.ocrGUI.daos.DishDao;
import com.example.ocrGUI.daos.MenuDao;
import com.example.ocrGUI.models.Category;
import com.example.ocrGUI.models.Dish;
import com.example.ocrGUI.models.Menu;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DishController {

    @RequestMapping("/list-menu-items")
    public String listMenuItems(Model model, @RequestParam("menuId") int menuId){
        Resource r=new ClassPathResource("applicationContext.xml");
        BeanFactory factory=new XmlBeanFactory(r);
        MenuDao menuDao = (MenuDao) factory.getBean("mdao");

        Menu menu = menuDao.selectMenu(menuId).get(0);
        model.addAttribute("menuId", menuId);
        model.addAttribute("menu", menu);

        return "menuItemsUpdatePage";
    }

    @RequestMapping("/edit-menu-item")
    public String editMenuItem(Model model, @RequestParam("menuId") int menuId,
                                               @RequestParam("dishId") int dishId,
                                               @RequestParam("name") String name,
                                               @RequestParam("category") String category,
                                               @RequestParam("description") String description,
                                               @RequestParam("price") String price){

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
        item.addToDescription(description);
        item.setPrice(price);
        dishDao.updateDish(item);

        Category oldCategory = categoryDao.selectCategory(oldCategoryID).get(0);
        oldCategory.removeDish(item);
        categoryDao.updateCategory(oldCategory);

        Category newCategory = categoryDao.selectCategory(item.getCategoryID()).get(0);
        newCategory.addDish(item);
        categoryDao.updateCategory(newCategory);

        item = dishDao.selectDish(dishId).get(0);
        Menu currentMenu = menuDao.selectMenu(menuId).get(0);
        model.addAttribute("menu", currentMenu);
        model.addAttribute("menuId", menuId);

        return "menuItemsUpdatePage";

    }

    @RequestMapping("/add-menu-item")
    public String addMenuItem(Model model, @RequestParam("menuId") int menuId,
                                            @RequestParam("name") String name,
                                            @RequestParam("category") int category,
                                            @RequestParam("description") String description,
                                            @RequestParam("price") String price){

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

        return "menuItemsUpdatePage";
    }

    @RequestMapping("/delete-menu-item")
    public String deleteMenuItem(Model model, @RequestParam("dishId") int dishId, @RequestParam("menuId") int menuId){
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

        return "menuItemsUpdatePage";
    }
}