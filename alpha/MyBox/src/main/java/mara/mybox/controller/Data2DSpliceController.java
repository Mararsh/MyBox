package mara.mybox.controller;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2022-5-21
 * @License Apache License Version 2.0
 */
public class Data2DSpliceController extends BaseController {

    @FXML
    protected Tab aTab, bTab, spliceTab;
    @FXML
    protected ControlData2DSource dataAController, dataBController;
    @FXML
    protected RadioButton horizontalRadio, aRadio, bRadio, longerRadio, shorterRadio;
    @FXML
    protected ControlData2DTarget targetController;
    @FXML
    protected Label numberLabel;
    @FXML
    protected ToggleGroup directionGroup;

    public Data2DSpliceController() {
        baseTitle = message("SpliceData");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            dataAController.setParameters(this);
            dataBController.setParameters(this);

            directionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (horizontalRadio.isSelected()) {
                        numberLabel.setText(message("RowsNumber"));
                    } else {
                        numberLabel.setText(message("ColumnsNumber"));
                    }
                }
            });

            targetController.setParameters(this, null);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        tabPane.getSelectionModel().select(aTab);
        if (dataAController.data2D == null || !dataAController.data2D.hasData()) {
            popError(message("DataA") + ": " + message("NoData"));
            return;
        } else if (!dataAController.checkSelections()) {
            return;
        }
        tabPane.getSelectionModel().select(bTab);
        if (dataBController.data2D == null || !dataBController.data2D.hasData()) {
            popError(message("DataB") + ": " + message("NoData"));
            return;
        } else if (!dataBController.checkSelections()) {
            return;
        }
        tabPane.getSelectionModel().select(spliceTab);
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private DataFileCSV targetCSV;

            @Override
            protected boolean handle() {
                try {
                    DataFileCSV csvA, csvB;
                    dataAController.data2D.startTask(this, dataAController.filterController.filter);
                    if (!dataAController.data2D.fillFilterStatistic()) {
                        return false;
                    }
                    if (dataAController.isAllPages()) {
                        csvA = dataAController.data2D.copy(task, null, null,
                                dataAController.checkedColsIndices, false, true, false);
                    } else {
                        csvA = DataFileCSV.save(task, null, null, ",",
                                dataAController.checkedColumns, dataAController.tableFiltered(false));
                    }
                    dataAController.data2D.stopTask();
                    if (csvA == null) {
                        error = message("InvalidData") + ": " + message("DataA");
                        return false;
                    }

                    dataBController.data2D.startTask(this, dataBController.filterController.filter);
                    if (!dataBController.data2D.fillFilterStatistic()) {
                        return false;
                    }
                    if (dataBController.isAllPages()) {
                        csvB = dataBController.data2D.copy(task, null, null,
                                dataBController.checkedColsIndices, false, true, false);
                    } else {
                        csvB = DataFileCSV.save(task, null, null, ",",
                                dataBController.checkedColumns, dataBController.tableFiltered(false));
                    }
                    dataBController.data2D.stopTask();
                    if (csvB == null) {
                        error = message("InvalidData") + ": " + message("DataB");
                        return false;
                    }

                    if (horizontalRadio.isSelected()) {
                        targetCSV = spliceHorizontally(csvA, csvB);
                    } else {
                        targetCSV = spliceVertically(csvA, csvB);
                    }
                    csvA.drop();
                    csvB.drop();
                    return targetCSV != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                Data2DSaveAsController.createData(targetCSV, targetController.target,
                        targetController.name(), targetController.file());
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                dataAController.data2D.stopTask();
                dataBController.data2D.stopTask();
            }

        };
        start(task);
    }

    protected DataFileCSV spliceVertically(DataFileCSV csvA, DataFileCSV csvB) {
        List<Data2DColumn> columns = null;
        try {
            List<Data2DColumn> columnsA = csvA.getColumns();
            List<Data2DColumn> columnsB = csvB.getColumns();
            if (aRadio.isSelected()) {
                columns = columnsA;
            } else if (bRadio.isSelected()) {
                columns = columnsB;
            } else if (longerRadio.isSelected()) {
                if (columnsA.size() >= columnsB.size()) {
                    columns = columnsA;
                } else {
                    columns = columnsB;
                }
            } else if (shorterRadio.isSelected()) {
                if (columnsA.size() <= columnsB.size()) {
                    columns = columnsA;
                } else {
                    columns = columnsB;
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
        }
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        DataFileCSV targetCSV = DataFileCSV.tmpCSV(targetController.name());
        int rowCount = 0;
        try (CSVPrinter csvPrinter = new CSVPrinter(
                new FileWriter(targetCSV.getFile(), targetCSV.getCharset()), targetCSV.cvsFormat())) {
            List<String> row = new ArrayList<>();
            for (Data2DColumn c : columns) {
                row.add(c.getColumnName());
            }
            csvPrinter.printRecord(row);
            int colLen = columns.size();
            try (CSVParser parser = CSVParser.parse(csvA.getFile(), csvA.getCharset(), csvA.cvsFormat())) {
                for (CSVRecord record : parser) {
                    if (task == null || task.isCancelled()) {
                        return null;
                    }
                    row.clear();
                    int dLen = Math.min(record.size(), colLen);
                    for (int i = 0; i < dLen; i++) {
                        row.add(record.get(i));
                    }
                    for (int i = dLen; i < colLen; i++) {
                        row.add(null);
                    }
                    csvPrinter.printRecord(row);
                    rowCount++;
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e);
            }
            try (CSVParser parser = CSVParser.parse(csvB.getFile(), csvB.getCharset(), csvB.cvsFormat())) {
                for (CSVRecord record : parser) {
                    if (task == null || task.isCancelled()) {
                        return null;
                    }
                    row.clear();
                    int dLen = Math.min(record.size(), colLen);
                    for (int i = 0; i < dLen; i++) {
                        row.add(record.get(i));
                    }
                    for (int i = dLen; i < colLen; i++) {
                        row.add(null);
                    }
                    csvPrinter.printRecord(row);
                    rowCount++;
                }
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e);
            }

        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
        }
        targetCSV.setColumns(columns).setColsNumber(columns.size()).setRowsNumber(rowCount);
        targetCSV.saveAttributes();
        return targetCSV;
    }

    protected DataFileCSV spliceHorizontally(DataFileCSV csvA, DataFileCSV csvB) {
        long size = 0;
        List<Data2DColumn> columns = new ArrayList<>();
        List<Data2DColumn> columnsA = csvA.getColumns();
        List<Data2DColumn> columnsB = csvB.getColumns();
        try {
            long sizeA = csvA.getRowsNumber();
            long sizeB = csvB.getRowsNumber();
            if (aRadio.isSelected()) {
                size = sizeA;
            } else if (bRadio.isSelected()) {
                size = sizeB;
            } else if (longerRadio.isSelected()) {
                if (sizeA >= sizeB) {
                    size = sizeA;
                } else {
                    size = sizeB;
                }
            } else if (shorterRadio.isSelected()) {
                if (sizeA <= sizeB) {
                    size = sizeA;
                } else {
                    size = sizeB;
                }
            }
            columns.addAll(columnsA);
            columns.addAll(columnsB);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
        }
        int colLen = columns.size(), colLenA = columnsA.size(), colLenB = columnsB.size();
        if (size <= 0 || colLen == 0) {
            return null;
        }
        DataFileCSV targetCSV = DataFileCSV.tmpCSV(targetController.name());
        int rowCount = 0;
        try (CSVPrinter csvPrinter = new CSVPrinter(
                new FileWriter(targetCSV.getFile(), targetCSV.getCharset()), targetCSV.cvsFormat());
                CSVParser parserA = CSVParser.parse(csvA.getFile(), csvA.getCharset(), csvA.cvsFormat());
                CSVParser parserB = CSVParser.parse(csvB.getFile(), csvB.getCharset(), csvB.cvsFormat())) {
            List<String> row = new ArrayList<>();
            List<String> validNames = new ArrayList<>();
            for (Data2DColumn c : columns) {
                String name = DerbyBase.checkIdentifier(validNames, c.getColumnName(), true);
                row.add(name);
                validNames.add(name);
            }
            csvPrinter.printRecord(row);

            Iterator<CSVRecord> iteratorA = parserA.iterator();
            Iterator<CSVRecord> iteratorB = parserB.iterator();
            while (rowCount < size && task != null && !task.isCancelled()) {
                row.clear();
                if (iteratorA.hasNext()) {
                    try {
                        CSVRecord record = iteratorA.next();
                        if (record != null) {
                            int dLen = Math.min(record.size(), colLenA);
                            for (int i = 0; i < dLen; i++) {
                                row.add(record.get(i));
                            }
                        }
                    } catch (Exception e) {  // skip  bad lines
//                            MyBoxLog.debug(e);
                    }
                }
                for (int i = row.size(); i < colLenA; i++) {
                    row.add(null);
                }
                if (iteratorB.hasNext()) {
                    try {
                        CSVRecord record = iteratorB.next();
                        if (record != null) {
                            int dLen = Math.min(record.size(), colLenB);
                            for (int i = 0; i < dLen; i++) {
                                row.add(record.get(i));
                            }
                        }
                    } catch (Exception e) {  // skip  bad lines
//                            MyBoxLog.debug(e);
                    }
                }
                for (int i = row.size(); i < colLen; i++) {
                    row.add(null);
                }
                csvPrinter.printRecord(row);
                rowCount++;
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
        }
        targetCSV.setColumns(columns).setColsNumber(columns.size()).setRowsNumber(rowCount);
        targetCSV.saveAttributes();
        return targetCSV;
    }

}
