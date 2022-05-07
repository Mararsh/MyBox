package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.chart.Chart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.chart.ChartTools.LabelType;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-5-7
 * @License Apache License Version 2.0
 */
public class ControlFxChart extends BaseController {

    protected Chart chart;
    protected List<List<String>> outputData;
    protected List<Data2DColumn> outputColumns;
    protected LabelType labelType;
    protected SimpleBooleanProperty redrawNotify;

    @FXML
    protected CheckBox popLabelCheck, nameCheck;
    @FXML
    protected ToggleGroup labelGroup;
    @FXML
    protected RadioButton pointRadio, valueRadio, categoryValueRadio, categoryRadio, noRadio;
    @FXML
    protected ScrollPane scrollPane;
    @FXML
    protected AnchorPane chartPane;

    public ControlFxChart() {
        redrawNotify = new SimpleBooleanProperty(false);
    }

    public void notifyRedraw() {
        redrawNotify.set(!redrawNotify.get());
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            labelType = LabelType.Point;
            labelGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkLabelType();
                    notifyRedraw();
                }
            });

            popLabelCheck.setSelected(UserConfig.getBoolean(baseName + "PopLabel", true));
            popLabelCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "PopLabel", popLabelCheck.isSelected());
                    notifyRedraw();
                }
            });

            nameCheck.setSelected(UserConfig.getBoolean(baseName + "Key", false));
            nameCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Key", nameCheck.isSelected());
                    notifyRedraw();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initType(String defaultLabel) {
        try {
            String type = UserConfig.getString(baseName + "DisplayLabel", defaultLabel);
            if (type == null) {
                type = "Point";
            }
            switch (type) {
                case "CategoryAndValue":
                    categoryValueRadio.fire();
                    break;
                case "Value":
                    valueRadio.fire();
                    break;
                case "Category":
                    categoryRadio.fire();
                    break;
                case "Point":
                    pointRadio.fire();
                    break;
                case "NotDisplay":
                    noRadio.fire();
                    break;
            }
            checkLabelType();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkLabelType() {
        try {
            if (isSettingValues) {
                return;
            }
            if (categoryValueRadio.isSelected()) {
                labelType = LabelType.CategoryAndValue;

            } else if (valueRadio.isSelected()) {
                labelType = LabelType.Value;

            } else if (categoryRadio.isSelected()) {
                labelType = LabelType.Category;

            } else if (pointRadio.isSelected()) {
                labelType = LabelType.Point;

            } else if (noRadio.isSelected()) {
                labelType = LabelType.NotDisplay;

            } else {
                labelType = LabelType.Point;

            }

            UserConfig.setString(baseName + "DisplayLabel", labelType.name());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setChart(Chart chart, List<Data2DColumn> outputColumns, List<List<String>> outputData) {
        try {
            if (chart == null) {
                return;
            }
            this.chart = chart;
            chartPane.getChildren().clear();
            chartPane.getChildren().add(chart);

            this.outputColumns = outputColumns;
            this.outputData = outputData;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void paneSize() {
        if (chartPane == null || chart == null) {
            return;
        }
        try {
            AnchorPane.setTopAnchor(chart, 2d);
            AnchorPane.setBottomAnchor​(chart, 2d);
            AnchorPane.setLeftAnchor(chart, 2d);
            AnchorPane.setRightAnchor​(chart, 2d);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void zoomIn() {
        if (chartPane == null || chart == null) {
            return;
        }
        try {
            Bounds bounds = chart.getBoundsInLocal();
            AnchorPane.clearConstraints(chart);
            chart.setPrefSize(bounds.getWidth() + 40, bounds.getHeight() + 40);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public boolean popAction() {
        ImagePopController.openImage(this, snapChart());
        return true;
    }

    public Image snapChart() {
        return NodeTools.snap(chart);
    }

    @FXML
    public void htmlAction() {
        try {
            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            Image image = snapChart();
            File imageFile = new File(AppPaths.getGeneratedPath() + File.separator + DateTools.nowFileString() + ".jpg");
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            ImageFileWriters.writeImageFile(bufferedImage, "jpg", imageFile.getAbsolutePath());

            StringBuilder s = new StringBuilder();
            String title = chart.getTitle();
            if (title != null) {
                s.append("<h1  class=\"center\">").append(title).append("</h1>\n");
                s.append("<hr>\n");
            }
            s.append("<h2  class=\"center\">").append(message("Image")).append("</h2>\n");
            s.append("<div align=\"center\"><img src=\"").append(imageFile.toURI().toString()).append("\"  style=\"max-width:95%;\"></div>\n");
            s.append("<hr>\n");

            List<String> names = new ArrayList<>();
            if (outputColumns != null) {
                for (Data2DColumn c : outputColumns) {
                    names.add(c.getColumnName());
                }
            }
            StringTable table = new StringTable(names);
            for (List<String> row : outputData) {
                table.add(row);
            }

            s.append(table.div());

            String html = HtmlWriteTools.html("", HtmlStyles.styleValue("Default"), s.toString());
            HtmlEditorController.load(html);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean controlAlt2() {
        paneSize();
        return true;
    }

    @Override
    public boolean controlAlt3() {
        zoomIn();
        return true;
    }

    public boolean popLabel() {
        return popLabelCheck.isSelected();
    }

    public boolean disName() {
        return nameCheck.isSelected();
    }

    /*
        get/set
     */
    public LabelType getLabelType() {
        return labelType;
    }

}
