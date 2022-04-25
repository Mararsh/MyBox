package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
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
    protected List<Integer> colsIndices;

    @FXML
    protected ComboBox<String> categoryColumnSelector, valueColumnSelector;
    @FXML
    protected VBox snapBox;
    @FXML
    protected AnchorPane chartPane;

    @Override
    public void initControls() {
        try {
            super.initControls();

            initDataTab();

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

    public boolean initData() {
        return true;
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearChart() {
        chartPane.getChildren().clear();
    }

    @FXML
    public void refreshAction() {
        okAction();
    }

    @FXML
    public void snapAction() {
        ImageViewerController.load(NodeTools.snap(snapBox));
    }

    public String chartTitle() {
        return null;
    }

    @FXML
    public void htmlAction() {
        try {
            if (colsIndices == null || outputData == null || outputData.isEmpty()) {
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
            String title = chartTitle();
            if (title != null) {
                s.append("<h1  class=\"center\">").append(title).append("</h1>\n");
                s.append("<hr>\n");
            }
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
    public int getScale() {
        return scale;
    }

}
