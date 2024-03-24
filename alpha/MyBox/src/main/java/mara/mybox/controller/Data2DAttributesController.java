package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-16
 * @License Apache License Version 2.0
 */
public class Data2DAttributesController extends BaseChildController {

    protected Data2DManufactureController dataController;
    protected TableData2DDefinition tableData2DDefinition;
    protected String dataName;
    protected short scale;
    protected int maxRandom;

    @FXML
    protected ControlData2DColumns columnsController;
    @FXML
    protected TextArea descInput;
    @FXML
    protected TextField idInput, timeInput, dataTypeInput, dataNameInput;
    @FXML
    protected ComboBox<String> scaleSelector, randomSelector;
    @FXML
    protected WebView webView;

    public Data2DAttributesController() {
        baseTitle = message("DataDefinition");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            scale = (short) UserConfig.getInt(baseName + "Scale", 2);
            if (scale < 0) {
                scale = 2;
            }
            scaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            scaleSelector.setValue(scale + "");

            maxRandom = UserConfig.getInt(baseName + "MaxRandom", 100000);
            if (maxRandom < 0) {
                maxRandom = 100000;
            }
            randomSelector.getItems().addAll(Arrays.asList("1", "100", "10", "1000", "10000", "1000000", "10000000"));
            randomSelector.setValue(maxRandom + "");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean isInvalid() {
        return dataController == null
                || !dataController.isShowing()
                || dataController.data2D == null
                || dataController.data2D.isPagesChanged();
    }

    protected void setParameters(Data2DManufactureController controller) {
        try {
            dataController = controller;
            if (isInvalid()) {
                close();
                return;
            }
            tableData2DDefinition = dataController.tableData2DDefinition;

            loadValues();

            dataController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    loadValues();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadValues() {
        try {
            if (isInvalid()) {
                close();
                return;
            }
            Data2D data2D = dataController.data2D;
            columnsController.setParameters(dataController);

            idInput.setText(data2D.getD2did() >= 0 ? data2D.getD2did() + "" : message("NewData"));
            timeInput.setText(DateTools.datetimeToString(data2D.getModifyTime()));
            dataTypeInput.setText(message(data2D.getType().name()));
            isSettingValues = true;
            dataNameInput.setText(data2D.getDataName());
            scaleSelector.setValue(data2D.getScale() + "");
            randomSelector.setValue(data2D.getMaxRandom() + "");
            descInput.setText(data2D.getComments());
            webView.getEngine().loadContent(HtmlWriteTools.table(data2D.pageInfo()));
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean pickValues() {
        String name = dataNameInput.getText();
//        if (name == null || name.isBlank()) {
//            popError(message("InvalidParameter") + ": " + message("DataName"));
//            return false;
//        }
        dataName = name;

        int v = -1;
        try {
            v = Integer.parseInt(scaleSelector.getValue());
        } catch (Exception e) {
        }
        if (v >= 0 && v <= 15) {
            scale = (short) v;
        } else {
            popError(message("InvalidParameter") + ": " + message("DecimalScale"));
            return false;
        }

        v = -1;
        try {
            v = Integer.parseInt(randomSelector.getValue());
        } catch (Exception e) {
        }
        if (v > 0) {
            maxRandom = v;
        } else {
            popError(message("InvalidParameter") + ": " + message("MaxRandom"));
            return false;
        }

        return true;
    }

    @FXML
    @Override
    public void okAction() {
        if (isInvalid()) {
            close();
            return;
        }
        if (!pickValues()) {
            return;
        }
        List<Data2DColumn> columns = columnsController.pickColumns();
        if (columns == null || columns.isEmpty()) {
            popError(message("DataColumnsShouldNotEmpty"));
            return;
        }
        Data2D data2D = dataController.data2D;
        boolean isMutiplePages = data2D.isMutiplePages();
        if (isMutiplePages && !PopTools.askSure(getTitle(), message("SureChangeDataAttributes"))) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            List<List<String>> pageData;

            @Override
            protected boolean handle() {
                try {
                    pageData = new ArrayList<>();
                    for (List<String> rowValues : dataController.tableData) {
                        List<String> newRow = new ArrayList<>();
                        newRow.add(rowValues.get(0));
                        for (Data2DColumn column : columns) {
                            int col = data2D.colOrder(column.getIndex()) + 1;
                            if (col <= 0 || col >= rowValues.size()) {
                                newRow.add(null);
                            } else {
                                newRow.add(rowValues.get(col));
                            }
                        }
                        pageData.add(newRow);
                    }
                    data2D.setDataName(dataName);
                    data2D.setScale(scale);
                    data2D.setMaxRandom(maxRandom);
                    data2D.setComments(descInput.getText());
                    data2D.setModifyTime(new Date());
                    data2D.setColumns(columns);
                    return true;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }

            }

            @Override
            protected void whenSucceeded() {
                dataController.makeColumns();
                isSettingValues = true;
                dataController.tableData.setAll(pageData);
                data2D.setPageData(dataController.tableData);
                isSettingValues = false;
                if (isMutiplePages) {
                    dataController.saveAction();
                } else {
                    dataController.tableChanged(true);
                }
                dataController.popInformation(message("Changed"));
                close();
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void recoverAction() {
        loadValues();
    }

    /*
        static
     */
    public static Data2DAttributesController open(Data2DManufactureController tableController) {
        try {
            Data2DAttributesController controller = (Data2DAttributesController) WindowTools.childStage(
                    tableController, Fxmls.Data2DAttributesFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
