package mara.mybox.db.data;

import java.io.File;
import javafx.scene.paint.Color;
import mara.mybox.data.Era;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlColor;

/**
 * @Author Mara
 * @CreateDate 2020-7-13
 * @License Apache License Version 2.0
 */
public class Dataset extends BaseData {

    protected long dsid;
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

    /*
        static methods
     */
    public static Dataset create() {
        return new Dataset();
    }

    public static boolean valid(Dataset data) {
        return data != null
                && data.getDataCategory() != null && data.getDataSet() != null;
    }

    public static boolean setValue(Dataset data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "dsid":
                    data.setId(value == null ? -1 : (long) value);
                    return true;
                case "data_category":
                    data.setDataCategory(value == null ? null : (String) value);
                    return true;
                case "data_set":
                    data.setDataSet(value == null ? null : (String) value);
                    return true;
                case "time_format":
                    data.setTimeFormat(value == null ? Era.Format.Datetime : Era.format((short) value));
                    return true;
                case "time_format_omitAD":
                    data.setOmitAD(value == null ? false : (boolean) value);
                    return true;
                case "text_color":
                    data.setTextColor(value == null ? null : Color.web((String) value));
                    return true;
                case "text_background_color":
                    data.setBgColor(value == null ? null : Color.web((String) value));
                    return true;
                case "chart_color":
                    data.setChartColor(value == null ? null : Color.web((String) value));
                    return true;
                case "dataset_image":
                    data.setImage(null);
                    if (value != null) {
                        File file = new File((String) value);
                        if (file.exists()) {
                            data.setImage(file);
                        }
                    }
                    return true;
                case "dataset_comments":
                    data.setComments(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(Dataset data, String column) {
        if (data == null || column == null) {
            return null;
        }
        try {
            switch (column) {
                case "dsid":
                    return data.getId();
                case "data_category":
                    return data.getDataCategory();
                case "data_set":
                    return data.getDataSet();
                case "time_format":
                    return Era.format(data.getTimeFormat());
                case "time_format_omitAD":
                    return data.isOmitAD();
                case "text_color":
                    return data.getTextColor() == null ? null : FxmlColor.color2rgba(data.getTextColor());
                case "text_background_color":
                    return data.getBgColor() == null ? null : FxmlColor.color2rgba(data.getBgColor());
                case "chart_color":
                    return data.getChartColor() == null ? null : FxmlColor.color2rgba(data.getChartColor());
                case "dataset_image":
                    return data.getImage() == null ? null : data.getImage().getAbsolutePath();
                case "dataset_comments":
                    return data.getComments();
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
