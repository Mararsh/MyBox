package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyEvent;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.CSV;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.DatabaseTable;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Excel;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Matrix;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.MyBoxClipboard;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Text;
import mara.mybox.data2d.DataClipboard;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.data2d.DataFileText;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.operate.Data2DSaveAs;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-4-11
 * @License Apache License Version 2.0
 */
public class Data2DCreateController extends Data2DAttributesController {

    protected int rows;

    @FXML
    protected ControlData2DNew attributesController;
    @FXML
    protected ComboBox<String> rowsSelector;
    @FXML
    protected RadioButton randomRadio, randomNonnegativeRadio,
            emptyRadio, nullRadio, zeroRadio, oneRadio;

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableData2DDefinition = new TableData2DDefinition();
            tableData2DColumn = new TableData2DColumn();
            tableData = FXCollections.observableArrayList();

            dataNameInput = attributesController.nameInput;
            descInput = attributesController.descInput;
            scaleSelector = attributesController.scaleSelector;
            randomSelector = attributesController.randomSelector;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean isInvalid() {
        return false;
    }

    @Override
    protected void setParameters(Data2DManufactureController controller) {
        try {
            dataController = controller;
            attributesController.setParameters(this);

            rowsSelector.getItems().addAll(
                    Arrays.asList("3", "10", "0", "5", "1", "20", "50", "100", "300", "500")
            );
            rowsSelector.setValue("3");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void loadValues() {
        try {
            TargetType type = attributesController.format;
            switch (type) {
                case CSV:
                    data2D = new DataFileCSV();
                    break;
                case Excel:
                    data2D = new DataFileExcel();
                    break;
                case Text:
                    data2D = new DataFileText();
                    break;
                case Matrix:
                    data2D = new DataMatrix(attributesController.matrixType());
                    break;
                case MyBoxClipboard:
                    data2D = new DataClipboard();
                    break;
                case DatabaseTable:
                    data2D = new DataTable();
                    break;
                default:
                    data2D = new DataFileCSV();
            }
            data2D.setColumns(data2D.tmpColumns(3));
            columnsController.setParameters(this);
            attributesController.dbController.setParameters(this, data2D);

            isSettingValues = true;
            dataNameInput.setText(data2D.getDataName());
            scaleSelector.setValue(data2D.getScale() + "");
            randomSelector.setValue(data2D.getMaxRandom() + "");
            descInput.setText(data2D.getComments());
            attributesChanged(false);
            columnsChanged(false);
            isSettingValues = false;
            checkStatus();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void columnsChanged(boolean changed) {
        super.columnsChanged(changed);
        data2D.setColumns(columnsController.pickColumns());
        if (data2D.isTable()) {
            attributesController.dbController.setData(data2D);
        }
    }

    @Override
    public void checkStatus() {
        setTitle(baseTitle + " - " + data2D.displayName());
    }

    @FXML
    @Override
    public void okAction() {
        data2D = pickValues();
        if (data2D == null) {
            return;
        }
        try {
            rows = Integer.parseInt(rowsSelector.getValue());
        } catch (Exception e) {
            rows = -1;
        }
        if (rows < 0) {
            popError(message("InvalidParameter") + ": " + message("RowsNumber"));
            return;
        }
        attributesController.data2D = data2D;
        Data2DWriter writer = attributesController.pickWriter();
        if (writer == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    Data2D sourceData = Data2D.create(Data2DDefinition.DataType.CSV);
                    sourceData.setColumns(data2D.getColumns());
                    int cols = sourceData.columnsNumber();
                    Random random = new Random();
                    tableData.clear();
                    for (int i = 0; i < rows; i++) {
                        List<String> row = new ArrayList<>();
                        row.add("" + (i + 1));
                        for (int j = 0; j < cols; j++) {
                            if (randomRadio.isSelected()) {
                                row.add(data2D.randomString(random, false));
                            } else if (randomNonnegativeRadio.isSelected()) {
                                row.add(data2D.randomString(random, true));
                            } else if (emptyRadio.isSelected()) {
                                row.add("");
                            } else if (nullRadio.isSelected()) {
                                row.add(null);
                            } else if (zeroRadio.isSelected()) {
                                row.add("0");
                            } else if (oneRadio.isSelected()) {
                                row.add("1");
                            }
                        }
                        tableData.add(row);
                    }
                    sourceData.setPageData(tableData);
                    data2D.startTask(this, null);
                    Data2DSaveAs operate = Data2DSaveAs.writeTo(sourceData, writer);
                    if (operate == null) {
                        return false;
                    }
                    operate.setController(myController)
                            .setTask(this)
                            .start();
                    return !operate.isFailed();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                dataController.loadDef(writer.getTargetData(), false);
                dataController.popInformation(message("Created"));
                close();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
            }
        };
        start(task);
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == attributesTab) {
            if (attributesController.keyEventsFilter(event)) {
                return true;
            }
        } else if (tab == columnsTab) {
            if (columnsController.keyEventsFilter(event)) {
                return true;
            }
        }
        return super.keyEventsFilter(event);
    }

    /*
        static
     */
    public static Data2DCreateController open(Data2DManufactureController tableController) {
        try {
            Data2DCreateController controller = (Data2DCreateController) WindowTools.childStage(
                    tableController, Fxmls.Data2DCreateFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
