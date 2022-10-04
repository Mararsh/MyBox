package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.NumberTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.apache.commons.math3.stat.Frequency;

/**
 * @Author Mara
 * @CreateDate 2022-4-15
 * @License Apache License Version 2.0
 */
public class Data2DFrequencyController extends BaseData2DHandleController {

    protected List<String> handledNames;
    protected int freCol;
    protected String freName;
    protected List<Integer> colsIndices;
    protected List<String> colsNames;
    protected Frequency frequency;

    @FXML
    protected CheckBox caseInsensitiveCheck;

    public Data2DFrequencyController() {
        baseTitle = message("FrequencyDistributions");
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            freName = colSelector.getSelectionModel().getSelectedItem();
            freCol = data2D.colOrder(freName);
            Data2DColumn freColumn = data2D.column(freCol);
            if (freColumn == null) {
                outOptionsError(message("SelectToHandle") + ": " + message("Column"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            handledNames = new ArrayList<>();
            outputColumns = new ArrayList<>();
            outputColumns.add(freColumn.cloneAll());
            handledNames.add(freName);

            String cName = freName + "_" + message("Count");
            Random random = new Random();
            while (handledNames.contains(cName)) {
                cName += random.nextInt(10);
            }
            outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Long));
            handledNames.add(cName);

            cName = freName + "_" + message("CountPercentage");
            while (handledNames.contains(cName)) {
                cName += random.nextInt(10);
            }
            outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            handledNames.add(cName);

            frequency = caseInsensitiveCheck.isSelected()
                    ? new Frequency(String.CASE_INSENSITIVE_ORDER)
                    : new Frequency();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean handleRows() {
        try {
            outputData = new ArrayList<>();
            filteredRowsIndices = filteredRowsIndices();
            if (filteredRowsIndices == null || filteredRowsIndices.isEmpty()) {
                if (task != null) {
                    task.setError(message("NoData"));
                }
                return false;
            }
            for (int r : filteredRowsIndices) {
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
                    row.add(NumberTools.format(frequency.getPct(value) * 100, scale));
                    outputData.add(row);
                }
            }
            frequency.clear();
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    @Override
    public DataFileCSV generatedFile() {
        return data2D.frequency(targetController.name(), frequency, freName, freCol, scale);
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
    public static Data2DFrequencyController open(ControlData2DLoad tableController) {
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
