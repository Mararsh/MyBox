package mara.mybox.data;

import java.io.File;
import javafx.scene.paint.Color;
import mara.mybox.db.TableBase;
import mara.mybox.db.TableDataset;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-7-13
 * @License Apache License Version 2.0
 */
public class Dataset extends TableData {

    protected long dsid;
    protected String dataCategory, dataSet, comments;
    protected Color textColor, bgColor, chartColor;
    protected File image;
    protected Era.Format timeFormat;
    protected boolean omitAD;

    private void init() {
        dsid = -1;
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

    public static Dataset create() {
        return new Dataset();
    }

    @Override
    public TableBase getTable() {
        if (table == null) {
            table = new TableDataset();
        }
        return table;
    }

    @Override
    public boolean valid() {
        return dataCategory != null && dataSet != null;
    }

    @Override
    public boolean setValue(String column, Object value) {
        if (column == null) {
            return false;
        }
        try {
            switch (column) {
                case "dsid":
                    dsid = value == null ? -1 : (long) value;
                    return true;
                case "data_category":
                    dataCategory = value == null ? null : (String) value;
                    return true;
                case "data_set":
                    dataSet = value == null ? null : (String) value;
                    return true;
                case "time_format":
                    timeFormat = value == null ? Era.Format.Datetime : Era.format((short) value);
                    return true;
                case "time_format_omitAD":
                    omitAD = value == null ? false : (boolean) value;
                    return true;
                case "text_color":
                    textColor = value == null ? null : Color.web((String) value);
                    return true;
                case "text_background_color":
                    bgColor = value == null ? null : Color.web((String) value);
                    return true;
                case "chart_color":
                    chartColor = value == null ? null : Color.web((String) value);
                    return true;
                case "dataset_image":
                    image = null;
                    if (value != null) {
                        File file = new File((String) value);
                        if (file.exists()) {
                            image = file;
                        }
                    }
                    return true;
                case "dataset_comments":
                    comments = value == null ? null : (String) value;
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @Override
    public Object getValue(String column) {
        if (column == null) {
            return null;
        }
        try {
            switch (column) {
                case "dsid":
                    return dsid;
                case "data_category":
                    return dataCategory;
                case "data_set":
                    return dataSet;
                case "time_format":
                    return Era.format(timeFormat);
                case "time_format_omitAD":
                    return omitAD;
                case "text_color":
                    return textColor == null ? null : FxmlColor.color2rgba(textColor);
                case "text_background_color":
                    return bgColor == null ? null : FxmlColor.color2rgba(bgColor);
                case "chart_color":
                    return chartColor == null ? null : FxmlColor.color2rgba(chartColor);
                case "dataset_image":
                    return image == null ? null : image.getAbsolutePath();
                case "dataset_comments":
                    return comments;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return null;
    }

    /*
        get/set
     */
    public long getDsid() {
        return dsid;
    }

    public void setDsid(long dsid) {
        this.dsid = dsid;
    }

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
