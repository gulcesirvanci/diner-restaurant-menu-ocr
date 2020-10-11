package com.example.ocrGUI.util;

import com.amazonaws.services.textract.model.BoundingBox;
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
                    double right= group.getRightCoordinate() * image.width();
                    double height = group.getBoundingBoxHeight() * image.height();
                    double left = group.getLeftCoordinate() * image.width();
                    double top = group.getTopCoordinate() * image.height();
                    Imgproc.rectangle(output, new Point(left,top), new Point(right,top+height), new Scalar(0, 200, 200), 2);
                }
            }
            Imgcodecs.imwrite("./src/main/resources/static/images/"+filename, output);
        } catch (Exception e){
            System.out.println("HERE");
            e.printStackTrace();
        }
    }
    public void drawBoundingBoxesForMod1(String filename, List<BoundingBox> boundingBoxes){
        try {
            nu.pattern.OpenCV.loadShared();
            //System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
            Mat image = Imgcodecs.imread("./src/main/resources/static/images/"+filename, 1);
            Mat output = image.clone();
            for(BoundingBox box : boundingBoxes){
                    double right= (box.getWidth()+box.getLeft()) * image.width();
                    double height = box.getHeight() * image.height();
                    double left = box.getLeft() * image.width();
                    double top = box.getTop() * image.height();
                    Imgproc.rectangle(output, new Point(left,top), new Point(right,top+height), new Scalar(0, 200, 200), 2);
            }
            Imgcodecs.imwrite("./src/main/resources/static/images/"+filename, output);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}


