package com.example.ocrGUI.models;

public class LabeledEntry {
    private String ocrText;
    private String label;

    public LabeledEntry(String ocrText, String label){
        this.ocrText = ocrText;
        this.label = label;
    }

    public String getOcrText(){ return ocrText; }
    public String getLabel(){ return label; }

}
