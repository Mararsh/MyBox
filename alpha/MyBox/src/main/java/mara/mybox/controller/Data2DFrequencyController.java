package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
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
public class Data2DFrequencyController extends BaseData2DTaskTargetsController {

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
    public boolean checkOptions() {
        try {
            if (!super.checkOptions()) {
                return false;
            }
            freName = colSelector.getSelectionModel().getSelectedItem();
            freCol = data2D.colOrder(freName);
            Data2DColumn freColumn = data2D.column(freCol);
            if (freColumn == null) {
                popError(message("SelectToHandle") + ": " + message("Column"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            handledNames = new ArrayList<>();
            outputColumns = new ArrayList<>();
            outputColumns.add(freColumn.cloneAll());
            handledNames.add(freName);

            String cName = DerbyBase.checkIdentifier(handledNames, freName + "_" + message("Count"), true);
            outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Long, 200));

            cName = DerbyBase.checkIdentifier(handledNames, freName + "_" + message("CountPercentage"), true);
            outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double, 200));

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
            List<Integer> filteredRowsIndices = sourceController.filteredRowsIndices;
            if (filteredRowsIndices == null || filteredRowsIndices.isEmpty()) {
                if (task != null) {
                    task.setError(message("NoData"));
                }
                return false;
            }
            for (int r : filteredRowsIndices) {
                List<String> tableRow = sourceController.tableData.get(r);
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
    public boolean handleAllData(FxTask currentTask, Data2DWriter writer) {
        return data2D.frequency(currentTask, writer, frequency, outputColumns, freCol, scale);
    }

    /*
        static
     */
    public static Data2DFrequencyController open(BaseData2DLoadController tableController) {
        try {
            Data2DFrequencyController controller = (Data2DFrequencyController) WindowTools.branchStage(
                    tableController, Fxmls.Data2DFrequencyFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
