package com.example.ocrGUI;


import com.example.ocrGUI.daos.CategoryDao;
import com.example.ocrGUI.daos.MenuDao;
import com.example.ocrGUI.models.Menu;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SimpleTest {

    public static void main(String[] args) {
        Resource r=new ClassPathResource("applicationContext.xml");
        BeanFactory factory=new XmlBeanFactory(r);
        MenuDao menuDao = (MenuDao)factory.getBean("mdao");

        Menu menu1 = menuDao.selectMenu(32).get(0);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("menu4.txt"));
            writer.write(menu1.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
