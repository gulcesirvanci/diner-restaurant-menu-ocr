package com.example.ocrGUI.controllers;

import com.example.ocrGUI.daos.MenuDao;
import com.example.ocrGUI.models.Menu;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
public class FileUploadController {

    public static String uploadDirectory = "./src/main/resources/static/images";

    @RequestMapping(value= "/upload", method = RequestMethod.POST)
    public String upload(Model model, @RequestParam("restaurantName") String restaurantName, @RequestParam("files") MultipartFile[] files,
                         @RequestParam("mod") String mod) {
        try{

            Resource r = new ClassPathResource("applicationContext.xml");
            BeanFactory factory = new XmlBeanFactory(r);
            ArrayList<String> fileNames = new ArrayList<>();
            for (MultipartFile file : files) {
                Path fileNameAndPath = Paths.get(uploadDirectory, file.getOriginalFilename());
                try {
                    fileNames.add(file.getOriginalFilename());
                    Files.write(fileNameAndPath, file.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            MenuDao menuDao = (MenuDao) factory.getBean("mdao");
            MenuController ocr = new MenuController();
            Menu menu = new Menu(restaurantName);
            if(mod.equals("mod2"))
                menu = ocr.implementOcrAndClassifyMod2(restaurantName, fileNames);
            else
                menu = ocr.implementOcrAndClassifyMod1(restaurantName, fileNames);
            menuDao.insertMenu(menu);
            model.addAttribute("menu", menu);
            model.addAttribute("menuId", menu.getMenuID());
            model.addAttribute("filenames", fileNames);

            return "categoriesUpdatePage";

        }catch (Exception e){
            model.addAttribute("message", e.getMessage());
            return "error";
        }


        }
    @RequestMapping(value= "/destroy-photos", method = RequestMethod.POST)
    public String destroyPhotos(@RequestParam("filename") List<String> filenames) {
        for(String filename : filenames){
            File file = new File("./src/main/resources/static/images/" + filename);
            file.delete();
        }
        return "redirect:/";
    }
}
