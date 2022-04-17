package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.apache.commons.math3.stat.Frequency;

/**
 * @Author Mara
 * @CreateDate 2022-4-15
 * @License Apache License Version 2.0
 */
public class Data2DFrequencyController extends Data2DHandleController {

    protected List<String> handledNames;
    protected int freCol;
    protected String freName;
    protected List<Integer> colsIndices;
    protected List<String> colsNames;
    protected ChangeListener<Boolean> tableStatusListener;
    protected Frequency frequency;

    @FXML
    protected ComboBox<String> colSelector;
    @FXML
    protected CheckBox caseInsensitiveCheck;
    @FXML
    protected Label memoryNoticeLabel;

    @Override
    public void initValues() {
        try {
            super.initValues();

            sourceController.noColumnSelection(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setParameters(ControlData2DEditTable tableController) {
        try {
            super.setParameters(tableController);

            colSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkOptions();
                }
            });

            tableStatusListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    refreshControls();
                }
            };
            tableController.statusNotify.addListener(tableStatusListener);

            refreshControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void refreshControls() {
        try {
            List<String> names = tableController.data2D.columnNames();
            if (names == null || names.isEmpty()) {
                colSelector.getItems().clear();
                return;
            }
            String selectedCol = colSelector.getSelectionModel().getSelectedItem();
            colSelector.getItems().setAll(names);
            if (selectedCol != null && names.contains(selectedCol)) {
                colSelector.setValue(selectedCol);
            } else {
                colSelector.getSelectionModel().select(0);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        boolean ok = super.checkOptions();
        targetController.setNotInTable(sourceController.allPages());
        if (!data2D.isTable() && sourceController.allPages()) {
            memoryNoticeLabel.setVisible(true);
        } else {
            memoryNoticeLabel.setVisible(false);
        }
        ok = ok && prepareRows();
        okButton.setDisable(!ok);
        return ok;
    }

    public boolean prepareRows() {
        try {
            freName = colSelector.getSelectionModel().getSelectedItem();
            freCol = data2D.colOrder(freName);
            Data2DColumn freColumn = data2D.column(freCol);
            if (freColumn == null) {
                infoLabel.setText(message("SelectToHandle"));
                return false;
            }
            handledNames = new ArrayList<>();
            handledColumns = new ArrayList<>();
            handledColumns.add(freColumn.cloneAll());
            handledNames.add(freName);

            String cName = freName + "_" + message("Count");
            while (handledNames.contains(cName)) {
                cName += "m";
            }
            handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Long));
            handledNames.add(cName);

            cName = freName + "_" + message("CountPercentage");
            while (handledNames.contains(cName)) {
                cName += "m";
            }
            handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            handledNames.add(cName);
            return true;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (!checkOptions() || !prepareRows()) {
                return;
            }
            frequency = caseInsensitiveCheck.isSelected()
                    ? new Frequency(String.CASE_INSENSITIVE_ORDER)
                    : new Frequency();
            if (sourceController.allPages()) {
                handleAllTask();
            } else {
                handleRowsTask();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean handleRows() {
        try {
            handledData = new ArrayList<>();
            int scale = data2D.getScale();
            for (int r : sourceController.checkedRowsIndices) {
                List<String> tableRow = tableController.tableData.get(r);
                String d = tableRow.get(freCol + 1);
                frequency.addValue(d);
            }
            Iterator iterator = frequency.valuesIterator();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    List<String> row = new ArrayList<>();
                    String value = (String) iterator.next();
                    row.add(value);
                    row.add(frequency.getCount(value) + "");
                    row.add(DoubleTools.format(frequency.getPct(value) * 100, scale));
                    handledData.add(row);
                }
            }
            frequency.clear();
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public DataFileCSV generatedFile() {
        return data2D.frequency(frequency, freName, freCol, scale);
    }

    @Override
    public void cleanPane() {
        try {
            tableController.statusNotify.removeListener(tableStatusListener);
            tableStatusListener = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static
     */
    public static Data2DFrequencyController open(ControlData2DEditTable tableController) {
        try {
            Data2DFrequencyController controller = (Data2DFrequencyController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DFrequencyFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
