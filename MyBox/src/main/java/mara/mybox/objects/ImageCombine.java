package mara.mybox.objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-9-6 9:08:16
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageCombine {

    private static final Logger logger = LogManager.getLogger();

    protected ObservableList<ImageFileInformation> sourceImages = FXCollections.observableArrayList();
    private int columnsValue, intervalValue, edgesValue, arrayType, sizeType;
    private int eachWidthValue, eachHeightValue, totalWidthValue, totalHeightValue;
    private Color bgColor = Color.WHITE;

    public static class ArrayType {

        public static int SingleColumn = 0;
        public static int SingleRow = 1;
        public static int ColumnsNumber = 2;
    }

    public static class CombineSizeType {

        public static int KeepSize = 0;
        public static int AlignAsBigger = 1;
        public static int AlignAsSmaller = 2;
        public static int EachWidth = 3;
        public static int EachHeight = 4;
        public static int TotalWidth = 5;
        public static int TotalHeight = 6;
    }

    public ImageCombine() {

    }

    public ObservableList<ImageFileInformation> getSourceImages() {
        return sourceImages;
    }

    public void setSourceImages(ObservableList<ImageFileInformation> sourceImages) {
        this.sourceImages = sourceImages;
    }

    public int getColumnsValue() {
        return columnsValue;
    }

    public void setColumnsValue(int columnsValue) {
        this.columnsValue = columnsValue;
    }

    public int getIntervalValue() {
        return intervalValue;
    }

    public void setIntervalValue(int intervalValue) {
        this.intervalValue = intervalValue;
    }

    public int getEdgesValue() {
        return edgesValue;
    }

    public void setEdgesValue(int edgesValue) {
        this.edgesValue = edgesValue;
    }

    public int getArrayType() {
        return arrayType;
    }

    public void setArrayType(int arrayType) {
        this.arrayType = arrayType;
    }

    public int getSizeType() {
        return sizeType;
    }

    public void setSizeType(int sizeType) {
        this.sizeType = sizeType;
    }

    public int getEachWidthValue() {
        return eachWidthValue;
    }

    public void setEachWidthValue(int eachWidthValue) {
        this.eachWidthValue = eachWidthValue;
    }

    public int getEachHeightValue() {
        return eachHeightValue;
    }

    public void setEachHeightValue(int eachHeightValue) {
        this.eachHeightValue = eachHeightValue;
    }

    public int getTotalWidthValue() {
        return totalWidthValue;
    }

    public void setTotalWidthValue(int totalWidthValue) {
        this.totalWidthValue = totalWidthValue;
    }

    public int getTotalHeightValue() {
        return totalHeightValue;
    }

    public void setTotalHeightValue(int totalHeightValue) {
        this.totalHeightValue = totalHeightValue;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

}
