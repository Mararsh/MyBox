package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DChartController extends BaseData2DHandleController {

    protected ChangeListener<Boolean> tableStatusListener, tableLoadListener;
    protected String selectedCategory, selectedValue;
    protected List<Integer> checkedColsIndices;
    protected List<Integer> dataColsIndices;
    protected Map<String, String> palette;

    @FXML
    protected ComboBox<String> categoryColumnSelector, valueColumnSelector;
    @FXML
    protected VBox snapBox;
    @FXML
    protected AnchorPane chartPane;
    @FXML
    protected ControlWebView webViewController;

    @Override
    public void initControls() {
        try {
            super.initControls();

            initDataTab();

            if (webViewController != null) {
                webViewController.setParent(this);
            }

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
                        checkOptions();
                    }
                });
            }

            if (valueColumnSelector != null) {
                valueColumnSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkOptions();
                    }
                });
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
                selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
                categoryColumnSelector.getItems().setAll(names);
                if (selectedCategory != null && names.contains(selectedCategory)) {
                    categoryColumnSelector.setValue(selectedCategory);
                } else {
                    categoryColumnSelector.getSelectionModel().select(0);
                }
            }
            if (valueColumnSelector != null) {
                selectedValue = valueColumnSelector.getSelectionModel().getSelectedItem();
                valueColumnSelector.getItems().setAll(names);
                if (selectedValue != null && names.contains(selectedValue)) {
                    valueColumnSelector.setValue(selectedValue);
                } else {
                    valueColumnSelector.getSelectionModel().select(names.size() > 1 ? 1 : 0);
                }
            }
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        if (isSettingValues) {
            return true;
        }
        boolean ok = super.checkOptions();
        if (categoryColumnSelector != null) {
            selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
        }
        if (valueColumnSelector != null) {
            selectedValue = valueColumnSelector.getSelectionModel().getSelectedItem();
        }
        if (ok) {
            noticeMemory();
        }
        return ok;
    }

    public void noticeMemory() {
        if (isSettingValues) {
            return;
        }
        if (sourceController.allPages()) {
            infoLabel.setText(message("AllRowsLoadComments"));
        }
    }

    public boolean initData() {
        dataColsIndices = new ArrayList<>();
        checkedColsIndices = sourceController.checkedColsIndices();
        if (checkedColsIndices == null || checkedColsIndices.isEmpty()) {
            popError(message("SelectToHandle"));
            return false;
        }
        dataColsIndices.addAll(checkedColsIndices);
        outputColumns = sourceController.checkedCols();
        return true;
    }

    public String chartTitle() {
        return null;
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
                    readData();
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

    public void readData() {
        if (sourceController.allPages()) {
            outputData = data2D.allRows(dataColsIndices, false);
        } else {
            outputData = sourceController.selectedData(sourceController.checkedRowsIndices(), dataColsIndices, false);
        }
    }

    public void drawChart() {
        try {
            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            clearChart();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void makePalette() {
        try {
            Random random = new Random();
            palette = new HashMap();
            for (int i = 0; i < outputColumns.size(); i++) {
                Data2DColumn column = outputColumns.get(i);
                Color color = column.getColor();
                if (color == null) {
                    color = FxColorTools.randomColor(random);
                }
                String rgb = FxColorTools.color2rgb(color);
                palette.put(column.getColumnName(), rgb);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setChartStyle() {
        makePalette();
    }

    public void clearChart() {
        chartPane.getChildren().clear();
    }

    public void redrawChart() {
        drawChart();
    }

    @FXML
    public void refreshAction() {
        okAction();
    }

    @FXML
    public void editAction() {
        webViewController.editAction();
    }

    @FXML
    public void dataAction() {
        if (outputData == null || outputData.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        DataManufactureController.open(outputColumns, outputData);
    }

    @FXML
    @Override
    public boolean popAction() {
        ImagePopController.openImage(this, snapChart());
        return true;
    }

    public Image snapChart() {
        return NodeTools.snap(snapBox);
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
            String title = chartTitle();
            if (title != null) {
                s.append("<h1  class=\"center\">").append(title).append("</h1>\n");
                s.append("<hr>\n");
            }
            s.append("<h2  class=\"center\">").append(message("Image")).append("</h2>\n");
            s.append("<div align=\"center\"><img src=\"").append(imageFile.toURI().toString()).append("\"  style=\"max-width:95%;\"></div>\n");
            s.append("<hr>\n");

            if (webViewController != null) {
                s.append(HtmlReadTools.body(webViewController.currentHtml(), false));
            } else if (outputData != null) {
                s.append(dataHtmlTable().div());
            }

            String html = HtmlWriteTools.html("", HtmlStyles.styleValue("Default"), s.toString());
            HtmlEditorController.load(html);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadDataHtml() {
        if (webViewController != null && outputData != null) {
            webViewController.loadContents(dataHtmlTable().html());
        }
    }

    public StringTable dataHtmlTable() {
        try {
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
            return table;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        webViewController.popFunctionsMenu(mouseEvent);
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
    public int getScale() {
        return scale;
    }

}
