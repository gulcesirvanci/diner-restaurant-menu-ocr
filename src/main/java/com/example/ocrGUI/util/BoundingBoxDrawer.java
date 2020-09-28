package com.example.ocrGUI.util;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.List;

public class BoundingBoxDrawer {


    private Scalar RED_FOR_CATEGORY = new Scalar(0, 0, 255);
    private Scalar BLUE_FOR_DESCRIPTION = new Scalar(255,0,0);
    private Scalar GREEN_FOR_NAME = new Scalar(0,255 ,0);

    public void drawBoundingBoxes(String filename, List<List<WordGroup>> wordGroupsPerCell){
        try {
            nu.pattern.OpenCV.loadShared();
            //System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
            Mat image = Imgcodecs.imread("./src/main/resources/images/"+filename, 1);
            Mat output = image.clone();
            for(List<WordGroup> wordGroupList : wordGroupsPerCell){
                for(WordGroup group : wordGroupList){
                    double width = group.getBoundingBoxWidth() * image.width();
                    double height = group.getBoundingBoxHeight() * image.height();
                    double left = group.getLeftCoordinate() * image.width();
                    double top = group.getTopCoordinate() * image.height();
                    Imgproc.rectangle(output, new Point(left,top), new Point(left+width,top+height), new Scalar(0, 200, 200), 2);
                }
            }
            Imgcodecs.imwrite("./src/main/resources/imagesWithBoundingBoxes/"+filename, output);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}


