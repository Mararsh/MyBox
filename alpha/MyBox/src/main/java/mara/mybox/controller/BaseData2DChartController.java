package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.Chart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.SingletonTask;
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
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DChartController extends BaseData2DHandleController {

    protected ChangeListener<Boolean> tableStatusListener, tableLoadListener;
    protected String selectedCategory, selectedValue;
    protected LabelType labelType;
    protected int tickFontSize, titleFontSize, labelFontSize;
    protected Side titleSide, legendSide;
    protected Chart chart;
    protected List<Integer> checkedColsIndices;
    protected List<Integer> colsIndices;

    @FXML
    protected ComboBox<String> categoryColumnSelector, valueColumnSelector,
            titleFontSizeSelector;
    @FXML
    protected VBox snapBox;
    @FXML
    protected AnchorPane chartPane;
    @FXML
    protected Text xyText;
    @FXML
    protected TextField titleInput;
    @FXML
    protected CheckBox coordinateCheck, autoTitleCheck, popLabelCheck, animatedCheck;
    @FXML
    protected ToggleGroup titleSideGroup, labelGroup, legendGroup;

    public abstract void checkChartType();

    public abstract void makeChart();

    public abstract void writeChartData();

    public abstract void setChartStyle();

    public abstract boolean initData();

    @Override
    public void initControls() {
        try {
            super.initControls();

            initDataTab();
            initPlotTab();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initDataTab() {
        try {
            if (categoryColumnSelector != null) {
                categoryColumnSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkAutoTitle();
                        checkOptions();
                    }
                });
            }

            if (valueColumnSelector != null) {
                valueColumnSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkAutoTitle();
                        checkOptions();
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initPlotTab() {
        try {
            tickFontSize = UserConfig.getInt(baseName + "TickFontSize", 12);
            if (tickFontSize < 0) {
                tickFontSize = 12;
            }

            coordinateCheck.setSelected(UserConfig.getBoolean(baseName + "PointCoordinate", false));
            coordinateCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "PointCoordinate", coordinateCheck.isSelected());
            });

            labelType = LabelType.NameAndValue;
            labelGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues || newValue == null) {
                        return;
                    }
                    String value = ((RadioButton) newValue).getText();
                    if (message("NameAndValue").equals(value)) {
                        labelType = LabelType.NameAndValue;
                    } else if (message("Value").equals(value)) {
                        labelType = LabelType.Value;
                    } else if (message("Name").equals(value)) {
                        labelType = LabelType.Name;
                    } else if (message("Point").equals(value)) {
                        labelType = LabelType.Point;
                    } else if (message("NotDisplay").equals(value)) {
                        labelType = LabelType.NotDisplay;
                    } else {
                        labelType = LabelType.NameAndValue;
                    }
                    okAction();
                }
            });

            popLabelCheck.setSelected(UserConfig.getBoolean(baseName + "PopLabel", true));
            popLabelCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "PopLabel", popLabelCheck.isSelected());
                    if (chart != null) {
                        okAction();
                    }
                }
            });

            autoTitleCheck.setSelected(UserConfig.getBoolean(baseName + "AutoTitle", true));
            autoTitleCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "AutoTitle", autoTitleCheck.isSelected());
                checkAutoTitle();
            });

            titleFontSize = UserConfig.getInt(baseName + "TitleFontSize", 12);
            if (titleFontSize < 0) {
                titleFontSize = 12;
            }
            titleFontSizeSelector.getItems().addAll(Arrays.asList(
                    "12", "14", "10", "8", "15", "16", "18", "9", "6", "4", "20", "24"
            ));
            titleFontSizeSelector.getSelectionModel().select(titleFontSize + "");
            titleFontSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            titleFontSize = v;
                            titleFontSizeSelector.getEditor().setStyle(null);
                            UserConfig.setInt(baseName + "TitleFontSize", titleFontSize);
                            if (chart != null) {
                                okAction();
                            }
                        } else {
                            titleFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        titleFontSizeSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            labelFontSize = UserConfig.getInt(baseName + "LabelFontSize", 12);
            if (labelFontSize < 0) {
                labelFontSize = 2;
            }

            titleSide = Side.TOP;
            titleSideGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues || newValue == null) {
                        return;
                    }
                    String value = ((RadioButton) newValue).getText();
                    if (message("Top").equals(value)) {
                        titleSide = Side.TOP;
                    } else if (message("Bottom").equals(value)) {
                        titleSide = Side.BOTTOM;
                    } else if (message("Left").equals(value)) {
                        titleSide = Side.LEFT;
                    } else if (message("Right").equals(value)) {
                        titleSide = Side.RIGHT;
                    }
                    if (chart != null) {
                        chart.setTitleSide(titleSide);
                        chart.requestLayout();
                    }
                }
            });

            legendSide = Side.TOP;
            legendGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
                        if (isSettingValues || newValue == null) {
                            return;
                        }
                        String value = ((RadioButton) newValue).getText();
                        if (message("NotDisplay").equals(value)) {
                            legendSide = null;
                        } else if (message("Left").equals(value)) {
                            legendSide = Side.LEFT;
                        } else if (message("Top").equals(value)) {
                            legendSide = Side.TOP;
                        } else if (message("Bottom").equals(value)) {
                            legendSide = Side.BOTTOM;
                        } else {
                            legendSide = Side.RIGHT;
                        }
                        if (chart != null) {
                            if (legendSide == null) {
                                chart.setLegendVisible(false);
                            } else {
                                chart.setLegendVisible(true);
                                chart.setLegendSide(legendSide);
                            }
                            chart.requestLayout();
                        }
                    });

            animatedCheck.setSelected(UserConfig.getBoolean(baseName + "Animated", false));
            animatedCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "Animated", animatedCheck.isSelected());
                if (chart != null) {
                    chart.setAnimated(animatedCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkAutoTitle() {
        try {
            if (autoTitleCheck.isSelected()) {
                defaultTitle();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean scaleChanged() {
        if (super.scaleChanged()) {
            okAction();
            return true;
        }
        return false;
    }

    @Override
    public void setParameters(ControlData2DEditTable tableController) {
        try {
            super.setParameters(tableController);

            tableStatusListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    refreshControls();
                }
            };
            tableController.statusNotify.addListener(tableStatusListener);

            tableLoadListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    okAction();
                }
            };
            tableController.loadedNotify.addListener(tableLoadListener);

            checkChartType();
            refreshControls();
            afterInit();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void afterInit() {
        okAction();
    }

    public void refreshControls() {
        try {
            List<String> names = tableController.data2D.columnNames();
            if (names == null || names.isEmpty()) {
                return;
            }
            isSettingValues = true;
            if (categoryColumnSelector != null) {
                categoryColumnSelector.getItems().clear();
                selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
                categoryColumnSelector.getItems().setAll(names);
                if (selectedCategory != null && names.contains(selectedCategory)) {
                    categoryColumnSelector.setValue(selectedCategory);
                } else {
                    categoryColumnSelector.getSelectionModel().select(0);
                }
            }
            if (valueColumnSelector != null) {
                valueColumnSelector.getItems().clear();

                selectedValue = valueColumnSelector.getSelectionModel().getSelectedItem();
                valueColumnSelector.getItems().setAll(names);
                if (selectedValue != null && names.contains(selectedValue)) {
                    valueColumnSelector.setValue(selectedValue);
                } else {
                    valueColumnSelector.getSelectionModel().select(names.size() > 1 ? 1 : 0);
                }
            }
            isSettingValues = false;

            checkAutoTitle();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public String categoryName() {
        return categoryColumnSelector.getSelectionModel().getSelectedItem();
    }

    public String valueName() {
        return valueColumnSelector.getSelectionModel().getSelectedItem();
    }

    public boolean isCategoryNumbers() {
        return false;
    }

    public String title() {
        String prefix = categoryName() + " - ";
        return prefix + valueName();
    }

    public String valuesNames() {
        return sourceController.checkedColsNames().toString();
    }

    public String numberName(int index) {
        try {
            return sourceController.checkedColsNames().get(index);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean displayLabel() {
        return labelType != null && labelType != LabelType.NotDisplay;
    }

    @Override
    public boolean checkOptions() {
        if (isSettingValues) {
            return true;
        }
        boolean ok = super.checkOptions();
        if (categoryColumnSelector != null) {
            selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
            if (selectedCategory == null) {
                infoLabel.setText(message("SelectToHandle"));
                okButton.setDisable(true);
                return false;
            }
        }
        if (valueColumnSelector != null) {
            selectedValue = valueColumnSelector.getSelectionModel().getSelectedItem();
            if (selectedValue == null) {
                infoLabel.setText(message("SelectToHandle"));
                okButton.setDisable(true);
                return false;
            }
        }
        return ok;
    }

    @FXML
    @Override
    public void okAction() {
        if (!checkOptions() || !initData()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(task);
                    if (sourceController.allPages()) {
                        outputData = data2D.allRows(colsIndices, false);
                    } else {
                        outputData = sourceController.selectedData(
                                sourceController.checkedRowsIndices(), colsIndices, false);
                    }
                    return outputData != null && !outputData.isEmpty();
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                drawChart();
            }

        };
        start(task);
    }

    public void drawChart() {
        try {
            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            clearChart();
            makeChart();
            writeChartData();
            setChartStyle();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearChart() {
        chart = null;
        chartPane.getChildren().clear();
    }

    public void makeFinalChart() {
        try {
            if (chart == null) {
                return;
            }
            chart.setStyle("-fx-font-size: " + titleFontSize + "px; -fx-tick-label-font-size: " + tickFontSize + "px; ");

            chart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(chart, Priority.ALWAYS);
            HBox.setHgrow(chart, Priority.ALWAYS);
            chart.setAnimated(animatedCheck.isSelected());
            chart.setTitle(titleInput.getText());
            chart.setTitleSide(titleSide);
            chartPane.getChildren().addAll(chart, xyText);
            AnchorPane.setTopAnchor(chart, 2d);
            AnchorPane.setBottomAnchor​(chart, 2d);
            AnchorPane.setLeftAnchor(chart, 2d);
            AnchorPane.setRightAnchor​(chart, 2d);

            xyText.setStyle("-fx-font-size: 10px; -fx-text-fill: #003472;");
            chart.setOnMouseMoved(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    mouseMoved(event);
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void mouseMoved(MouseEvent event) {
        try {
            if (coordinateCheck.isSelected()) {
                double x = event.getX();
                double y = event.getY();
                double w = xyText.getBoundsInParent().getWidth();
                double h = xyText.getBoundsInParent().getHeight();
                xyText.setText((int) x + "," + (int) y);
                xyText.setX(x + w >= chartPane.getWidth() ? x - w - 2 : x);
                xyText.setY(y + h >= chartPane.getHeight() ? y - h - 2 : y);
                xyText.setVisible(true);
            } else {
                xyText.setVisible(false);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    public void defaultTitle() {
        titleInput.setText(title());
    }

    @FXML
    public void goTitle() {
        if (chart != null) {
            chart.setTitle(titleInput.getText());
        }
    }

    @FXML
    public void refreshAction() {
        okAction();
    }

    @FXML
    public void snapAction() {
        ImageViewerController.load(NodeTools.snap(snapBox));
    }

    @FXML
    public void htmlAction() {
        try {
            if (chart == null || colsIndices == null || outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            Image image = NodeTools.snap(snapBox);
            File imageFile = new File(AppPaths.getGeneratedPath() + File.separator + DateTools.nowFileString() + ".jpg");
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            ImageFileWriters.writeImageFile(bufferedImage, "jpg", imageFile.getAbsolutePath());

            StringTable table = new StringTable(data2D.columnNames(colsIndices));
            for (List<String> row : outputData) {
                table.add(row);
            }

            StringBuilder s = new StringBuilder();
            s.append("<h1  class=\"center\">").append(titleInput.getText()).append("</h1>\n");
            s.append("<hr>\n");

            s.append("<h2  class=\"center\">").append(message("Image")).append("</h2>\n");
            s.append("<div align=\"center\"><img src=\"").append(imageFile.toURI().toString()).append("\"  style=\"max-width:95%;\"></div>\n");
            s.append("<hr>\n");
            s.append(table.div());

            String html = HtmlWriteTools.html("", HtmlStyles.styleValue("Default"), s.toString());
            HtmlEditorController.load(html);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    public void cleanPane() {
        try {
            tableController.statusNotify.removeListener(tableStatusListener);
            tableController.loadedNotify.removeListener(tableLoadListener);
            tableStatusListener = null;
            tableLoadListener = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        get/set
     */
    public CheckBox getPopLabelCheck() {
        return popLabelCheck;
    }

    public LabelType getLabelType() {
        return labelType;
    }

    public int getLabelFontSize() {
        return labelFontSize;
    }

    public int getScale() {
        return scale;
    }

}
