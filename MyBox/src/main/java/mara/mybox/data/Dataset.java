package mara.mybox.data;

import java.io.File;
import javafx.scene.paint.Color;
import mara.mybox.db.TableBase;
import mara.mybox.db.TableDataset;

/**
 * @Author Mara
 * @CreateDate 2020-7-13
 * @License Apache License Version 2.0
 */
public class Dataset extends TableData {

    protected String dataCategory, dataSet, comments;
    protected Color textColor, bgColor, chartColor;
    protected File image;
    protected Era.Format timeFormat;
    protected boolean omitAD;

    private void init() {
        id = -1;
        textColor = Color.BLACK;
        bgColor = Color.WHITE;
        chartColor = Color.web("#961c1c");
        timeFormat = Era.Format.Datetime;
        omitAD = true;
    }

    public Dataset() {
        init();
    }

    public Dataset(String dataCategory, String dataSet) {
        init();
        this.dataCategory = dataCategory;
        this.dataSet = dataSet;
    }

    @Override
    public boolean valid() {
        return dataCategory != null && dataSet != null;
    }

    public static Dataset create() {
        return new Dataset();
    }

    /*
        customized  get/set
     */
    @Override
    public TableBase getTable() {
        if (table == null) {
            table = new TableDataset();
        }
        return table;
    }

    /*
        get/set
     */
    public String getDataCategory() {
        return dataCategory;
    }

    public Dataset setDataCategory(String dataCategory) {
        this.dataCategory = dataCategory;
        return this;
    }

    public String getDataSet() {
        return dataSet;
    }

    public Dataset setDataSet(String dataSet) {
        this.dataSet = dataSet;
        return this;
    }

    public String getComments() {
        return comments;
    }

    public Dataset setComments(String comments) {
        this.comments = comments;
        return this;
    }

    public Era.Format getTimeFormat() {
        return timeFormat;
    }

    public Dataset setTimeFormat(Era.Format timeFormat) {
        this.timeFormat = timeFormat;
        return this;
    }

    public Color getTextColor() {
        return textColor;
    }

    public Dataset setTextColor(Color textColor) {
        this.textColor = textColor;
        return this;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public Dataset setBgColor(Color bgColor) {
        this.bgColor = bgColor;
        return this;
    }

    public Color getChartColor() {
        return chartColor;
    }

    public Dataset setChartColor(Color chartColor) {
        this.chartColor = chartColor;
        return this;
    }

    public File getImage() {
        return image;
    }

    public Dataset setImage(File image) {
        this.image = image;
        return this;
    }

    public boolean isOmitAD() {
        return omitAD;
    }

    public Dataset setOmitAD(boolean omitAD) {
        this.omitAD = omitAD;
        return this;
    }

}
