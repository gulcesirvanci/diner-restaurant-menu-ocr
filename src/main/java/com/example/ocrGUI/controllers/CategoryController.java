package com.example.ocrGUI.controllers;

import com.example.ocrGUI.daos.CategoryDao;
import com.example.ocrGUI.daos.MenuDao;
import com.example.ocrGUI.models.Category;
import com.example.ocrGUI.models.Menu;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CategoryController {

    @RequestMapping("/update-category")
    public String updateCategory(Model model, @RequestParam("ocrText") String ocrText, @RequestParam("categoryId") int categoryId, @RequestParam("menuId") int menuId ){
        Resource r=new ClassPathResource("applicationContext.xml");
        BeanFactory factory=new XmlBeanFactory(r);
        CategoryDao categoryDao = (CategoryDao) factory.getBean("cdao");
        MenuDao menuDao = (MenuDao) factory.getBean("mdao");

        List<Category> categories = categoryDao.selectCategory(categoryId);
        Category category = categories.get(0);
        category.setName(ocrText);
        categoryDao.updateCategory(category);

        Menu menu = menuDao.selectMenu(menuId).get(0);
        model.addAttribute("menuId", menuId);
        model.addAttribute("menu", menu);
        return "categoriesUpdate";

    }

    @RequestMapping("add-new-category")
    public String addNewCategory(Model model, @RequestParam("menuId") int menuId, @RequestParam("categoryName") String categoryName){

        Resource r=new ClassPathResource("applicationContext.xml");
        BeanFactory factory=new XmlBeanFactory(r);
        CategoryDao categoryDao = (CategoryDao) factory.getBean("cdao");
        MenuDao menuDao = (MenuDao) factory.getBean("mdao");

        Category category = new Category(menuId, categoryName);
        categoryDao.insertCategory(category.getMenuID(), category);

        Menu currentMenu = menuDao.selectMenu(menuId).get(0);
        currentMenu.addCategory(category);
        menuDao.updateMenu(currentMenu);
        currentMenu = menuDao.selectMenu(menuId).get(0);
        model.addAttribute("menuId", menuId);
        model.addAttribute("menu", currentMenu);

        return "categoriesUpdate";

    }
}
