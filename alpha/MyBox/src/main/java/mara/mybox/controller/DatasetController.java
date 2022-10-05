package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.db.data.Dataset;
import mara.mybox.db.table.TableDataset;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableBooleanCell;
import mara.mybox.fxml.cell.TableColorCell;
import mara.mybox.fxml.cell.TableTableMessageCell;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-7-18
 * @License Apache License Version 2.0
 */
public class DatasetController extends BaseDataManageController<Dataset> {

    protected String predefinedColor, inputtedColor;
    protected LoadingController loading;

    @FXML
    protected DatasetSourceController sourceController;
    @FXML
    protected TableColumn<Dataset, Long> dataidColumn;
    @FXML
    protected TableColumn<Dataset, String> categoryColumn, datasetColumn, textColorColumn,
            textBgColorColumn, chartColorColumn, imageColumn, commentsColumn;
    @FXML
    protected TableColumn<Dataset, String> timeFormatColumn;
    @FXML
    protected TableColumn<Dataset, Boolean> omitADColumn;

    public DatasetController() {
        baseTitle = Languages.message("Dataset");
    }

    @Override
    public void setTableDefinition() {
        tableDefinition = new TableDataset();
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();

            dataidColumn.setCellValueFactory(new PropertyValueFactory<>("dsid"));
            categoryColumn.setCellValueFactory(new PropertyValueFactory<>("dataCategory"));
            categoryColumn.setCellFactory(new TableTableMessageCell());
            datasetColumn.setCellValueFactory(new PropertyValueFactory<>("dataSet"));
            textColorColumn.setCellValueFactory(new PropertyValueFactory<>("textColor"));
            textColorColumn.setCellFactory(new TableColorCell());
            textBgColorColumn.setCellValueFactory(new PropertyValueFactory<>("bgColor"));
            textBgColorColumn.setCellFactory(new TableColorCell());
            chartColorColumn.setCellValueFactory(new PropertyValueFactory<>("chartColor"));
            chartColorColumn.setCellFactory(new TableColorCell());
            timeFormatColumn.setCellValueFactory(new PropertyValueFactory<>("timeFormat"));
//            timeFormatColumn.setCellFactory(new TableTimeFormatCell());
            omitADColumn.setCellValueFactory(new PropertyValueFactory<>("omitAD"));
            omitADColumn.setCellFactory(new TableBooleanCell());
            imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
            commentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            loadTrees(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadTrees(boolean load) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        sourceController.loadTree();
        if (load) {
            queryData();
            tabsPane.getSelectionModel().select(dataTab);
        }
    }

    @Override
    protected String checkWhere() {
        sourceController.check();
        String sourceConditions = sourceController.getFinalConditions();
        if (sourceConditions == null) {
            popError(Languages.message("MissDataset") + "\n" + Languages.message("SetConditionsComments"));
            return null;
        }
        sourceConditions = sourceConditions.trim();
        return sourceConditions;
    }

    @Override
    protected String checkTitle() {
        return sourceController.getFinalTitle();
    }

    @FXML
    @Override
    public void addAction() {
        try {
            DatasetEditController controller = (DatasetEditController) WindowTools.openStage(Fxmls.DatasetEditFxml);
            controller.initEditor(this, null);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void editAction() {
        Dataset selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            DatasetEditController controller = (DatasetEditController) WindowTools.openStage(Fxmls.DatasetEditFxml);
            controller.initEditor(this, selected);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void load(BaseController parent, String category) {
        parentController = parent;
        sourceController.select(category);
        queryData();
    }

}
