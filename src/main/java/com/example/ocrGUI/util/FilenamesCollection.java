package com.example.ocrGUI.util;

import java.util.List;

public class FilenamesCollection {
    private List<String> filenames;

    public FilenamesCollection(List<String> filenames){
        this.filenames = filenames;
    }

    public List<String> getFilenames(){
        return filenames;
    }
}
