package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataClipboard;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableDataColumn;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-11
 * @License Apache License Version 2.0
 */
public class DataClipboardController extends BaseDataTableController<DataDefinition> {

    protected TableDataDefinition tableDataDefinition;
    protected TableDataColumn tableDataColumn;
    protected boolean checkedInvalid;
    protected DataDefinition currentData;

    @FXML
    protected TableColumn<DataDefinition, Long> dfidColumn;
    @FXML
    protected TableColumn<DataDefinition, String> nameColumn;
    @FXML
    protected Label nameLabel;
    @FXML
    protected ControlSheetCSV sheetController;

    public DataClipboardController() {
        baseTitle = message("DataClipboard");
        TipsLabelKey = "DataClipboardTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            sheetController.setParent(this);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.CSV);
    }

    @Override
    public void setTableDefinition() {
        tableDataDefinition = sheetController.tableDataDefinition;
        tableDataColumn = sheetController.tableDataColumn;
        tableDefinition = tableDataDefinition;

        tableDataDefinition.setOrderColumns("dfid");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            loadTableData();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initColumns() {
        try {
            dfidColumn.setCellValueFactory(new PropertyValueFactory<>("dfid"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("dataName"));
            nameColumn.setCellFactory(new Callback<TableColumn<DataDefinition, String>, TableCell<DataDefinition, String>>() {
                @Override
                public TableCell<DataDefinition, String> call(TableColumn<DataDefinition, String> param) {
                    TableCell<DataDefinition, String> cell = new TableCell<DataDefinition, String>() {
                        @Override
                        public void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            setGraphic(null);
                            setText(null);
                            if (empty || item == null) {
                                return;
                            }
                            try {
                                File file = new File(item);
                                if (file.exists()) {
                                    setText(FileNameTools.namePrefix(file.getName()));
                                }
                            } catch (Exception e) {
                            }
                        }
                    };
                    return cell;
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
//            if (isMatrix) {
//                NodeStyleTools.setTooltip(tipsView, new Tooltip(message("MatrixInputComments")));
//            } else {
//                NodeStyleTools.setTooltip(tipsView, new Tooltip(message("DataInputComments")));
//            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public int readDataSize() {
        if (checkedInvalid) {
            return DataClipboard.size(tableDataDefinition);
        } else {
            int size = DataClipboard.checkValid(tableDataDefinition);
            checkedInvalid = true;
            return size;
        }
    }

    @Override
    public List<DataDefinition> readPageData() {
        return DataClipboard.queryPage(tableDataDefinition, currentPageStart - 1, currentPageSize);
    }

    @Override
    protected int deleteData(List<DataDefinition> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        for (DataDefinition d : data) {
            FileDeleteTools.delete(d.getDataName());
        }
        return tableDefinition.deleteData(data);
    }

    @Override
    protected void afterClear() {
        FileDeleteTools.clearDir(new File(AppPaths.getDataClipboardPath()));
        refreshAction();
    }

    @Override
    public void itemClicked() {
        DataDefinition selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        File file = new File(selected.getDataName());
        if (!file.exists()) {
            tableDefinition.deleteData(selected);
            refreshAction();
            return;
        }
        sheetController.sourceFile = file;
        sheetController.userSavedDataDefinition = true;
        sheetController.sourceCharset = Charset.forName(selected.getCharset());
        sheetController.sourceCsvDelimiter = selected.getDelimiter().charAt(0);
        sheetController.autoDetermineSourceCharset = false;
        sheetController.sourceWithNames = selected.isHasHeader();
        sheetController.initCurrentPage();
        sheetController.loadFile();
    }

    public void loadNull() {
        currentData = null;
        nameLabel.setText("");
    }

    public void load(String[][] data, List<ColumnDefinition> columns) {
        loadNull();
        sheetController.makeSheet(data, columns);
    }

    @FXML
    @Override
    public void createAction() {
        try {
            loadNull();
            sheetController.createAction();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void openPath() {
        try {
            browseURI(new File(AppPaths.getDataClipboardPath() + File.separator).toURI());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public boolean popAction() {
        return sheetController.popAction();
    }

    @FXML
    @Override
    public boolean menuAction() {
        return sheetController.menuAction();
    }

    /*
        static
     */
    public static DataClipboardController open(String[][] data, List<ColumnDefinition> columns) {
        DataClipboardController controller = (DataClipboardController) WindowTools.openStage(Fxmls.DataClipboardFxml);
        controller.load(data, columns);
        controller.toFront();
        return controller;
    }

//    public static DataClipboardController open(BaseSheetController sheetController) {
//        DataClipboardController controller = (DataClipboardController) WindowTools.openStage(Fxmls.DataClipboardFxml);
////        controller.setSourceController(sheetController);
//        controller.toFront();
//        return controller;
//    }
}
