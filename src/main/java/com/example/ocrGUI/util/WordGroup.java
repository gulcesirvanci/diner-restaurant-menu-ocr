package com.example.ocrGUI.util;

public class WordGroup{
    private int rowIndex;
    private int colIndex;
    private double leftCoordinate;
    private double topCoordinate;
    private double boundingBoxWidth;
    private double boundingBoxHeight;
    private String content = "";
    private boolean isPrice = false;
    private boolean isCategory = false;

    public WordGroup(double leftCoordinate, double topCoordinate, double boundingBoxWidth, double boundingBoxHeight, int rowIndex, int colIndex){
        this.leftCoordinate = leftCoordinate;
        this.topCoordinate = topCoordinate;
        this.boundingBoxWidth = boundingBoxWidth;
        this.boundingBoxHeight = boundingBoxHeight;
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
    }

    public void addContent(String content){
        this.content = this.content + content + " ";
    }
    public String getContent(){
        return content;
    }

    public void markAsPrice(){
        this.isPrice = true;
    }
    public boolean isPrice(){
        return this.isPrice;
    }

    public void markAsCategory() { this.isCategory = true; }
    public boolean isCategory(){ return this.isCategory; }

    public void setColIndex(int colIndex){
        this.colIndex = colIndex;
    }
    public int getColIndex(){
        return colIndex;
    }

    public void setRowIndex(int rowIndex){
        this.rowIndex = rowIndex;
    }
    public int getRowIndex(){
        return rowIndex;
    }

    public double getLeftCoordinate() {
        return leftCoordinate;
    }

    public void setLeftCoordinate(double leftCoordinate) {
        this.leftCoordinate = leftCoordinate;
    }
    public double getTopCoordinate() {
        return topCoordinate;
    }

    public void setTopCoordinate(double topCoordinate) {
        this.topCoordinate = topCoordinate;
    }
    public double getBoundingBoxWidth() {
        return boundingBoxWidth;
    }

    public void setBoundingBoxWidth(double boundingBoxWidth) {
        this.boundingBoxWidth = boundingBoxWidth;
    }
    public double getBoundingBoxHeight() {
        return boundingBoxHeight;
    }

    public void setBoundingBoxHeight(double boundingBoxHeight) {
        this.boundingBoxHeight = boundingBoxHeight;
    }
}