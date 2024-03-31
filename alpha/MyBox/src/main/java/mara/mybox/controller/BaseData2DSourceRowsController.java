package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Operations.ObjectType;
import mara.mybox.data2d.DataFilter;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class BaseData2DSourceRowsController extends BaseData2DLoadController {

    protected BaseData2DLoadController dataController;
    protected ObjectType objectType;

    protected List<Integer> selectedRowsIndices;

    @FXML
    protected ToggleGroup rowsGroup;
    @FXML
    protected RadioButton selectedRadio, allPagesRadio, currentPageRadio;

    public void setParameters(BaseController parent, BaseData2DLoadController controller) {
        try {
            if (controller == null) {
                return;
            }
            this.parentController = parent;
            this.dataController = controller;

            tableView.requestFocus();

            String rowsSelectionType = UserConfig.getString(baseName + "RowsSelectionType", "Selected");
            if ("AllPages".equals(rowsSelectionType)) {
                allPagesRadio.setSelected(true);
            } else if ("CurrentPage".equals(rowsSelectionType)) {
                currentPageRadio.setSelected(true);
            } else {
                selectedRadio.setSelected(true);
            }
            rowsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (allPagesRadio.isSelected()) {
                        UserConfig.setString(baseName + "RowsSelectionType", "AllPages");
                    } else if (selectedRadio.isSelected()) {
                        UserConfig.setString(baseName + "RowsSelectionType", "Selected");
                    } else {
                        UserConfig.setString(baseName + "RowsSelectionType", "CurrentPage");
                    }
                    notifySelected();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void sourceChanged(Data2D data) {
        try {
            data2D = data;
            makeColumns();
            updateTable(dataController.tableData);
            isSettingValues = true;
            currentPage = dataController.currentPage;
            startRowOfCurrentPage = dataController.startRowOfCurrentPage;
            pageSize = dataController.pageSize;
            pagesNumber = dataController.pagesNumber;
            dataSize = dataController.dataSize;
            dataSizeLoaded = true;
            data2D.setDataLoaded(true);
            isSettingValues = false;
            refreshControls();
            notifyLoaded();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void refreshControls() {
        try {
            if (data2D == null) {
                return;
            }
            isSettingValues = true;
            if (data2D.isMutiplePages()) {
                allPagesRadio.setDisable(false);
                showPaginationPane(true);
                setPagination();
            } else {
                if (allPagesRadio.isSelected()) {
                    currentPageRadio.setSelected(true);
                }
                allPagesRadio.setDisable(true);
                showPaginationPane(false);
            }
            isSettingValues = false;
            restoreSelections();

        } catch (Exception e) {
            MyBoxLog.error(e);
            isSettingValues = false;
        }
    }

    public void restoreSelections() {
        try {
            isSettingValues = true;
            if (selectedRowsIndices != null && !selectedRowsIndices.isEmpty()
                    && selectedRowsIndices.size() != tableData.size()) {
                for (int i = 0; i < tableData.size(); i++) {
                    if (selectedRowsIndices.contains(i)) {
                        tableView.getSelectionModel().select(i);
                    } else {
                        tableView.getSelectionModel().clearSelection(i);
                    }
                }
            } else {
                tableView.getSelectionModel().clearSelection();
            }

            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }

    }

    public boolean isAllPages() {
        return allPagesRadio.isSelected();
    }

    @Override
    public void updateInterface() {
        if (dataBox != null) {
            dataBox.setDisable(data2D == null);
        }
    }

    // If none selected then select all
    public boolean checkedRows() {
        try {
            selectedRowsIndices = new ArrayList<>();
            DataFilter filter = data2D.filter;
            filter.start(null, data2D);
            List<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
            if (allPagesRadio.isSelected() || currentPageRadio.isSelected()
                    || selected == null || selected.isEmpty()) {
                for (int i = 0; i < tableData.size(); i++) {
                    selectedRowsIndices.add(i);
                }
            } else {
                for (int i : selected) {
                    selectedRowsIndices.add(i);
                }
            }
            if (!allPagesRadio.isSelected() && selectedRowsIndices.isEmpty()) {
                popError(message("SelectToHandle") + ": " + message("Rows"));
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public List<Integer> tableRows() {
        try {
            List<Integer> rows = new ArrayList<>();
            for (int i = 0; i < tableData.size(); i++) {
                rows.add(i);
            }
            return rows;
        } catch (Exception e) {
            error = e.toString();
            MyBoxLog.debug(e);
            return null;
        }
    }

    public boolean hasData() {
        return data2D != null && data2D.isValidDefinition() && !tableData.isEmpty();
    }

}
