package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.data.Dataset;
import mara.mybox.data.Era;
import mara.mybox.db.TableDataset;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.TableBooleanCell;
import mara.mybox.fxml.TableColorCell;
import mara.mybox.fxml.TableTableMessageCell;
import mara.mybox.fxml.TableTimeFormatCell;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-7-18
 * @License Apache License Version 2.0
 */
public class DatasetController extends DataAnalysisController<Dataset> {

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
    protected TableColumn<Dataset, Era.Format> timeFormatColumn;
    @FXML
    protected TableColumn<Dataset, Boolean> omitADColumn;

    public DatasetController() {
        baseTitle = message("Dataset");
    }

    @Override
    public void setTableDefinition() {
        tableDefinition = new TableDataset();
    }

    @Override
    protected void initColumns() {
        try {

            dataidColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
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
            timeFormatColumn.setCellFactory(new TableTimeFormatCell());
            omitADColumn.setCellValueFactory(new PropertyValueFactory<>("omitAD"));
            omitADColumn.setCellFactory(new TableBooleanCell());
            imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
            commentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            loadTrees(true);

            setButtons();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void setButtons() {
        try {
            FxmlControl.setTooltip(deleteButton, message("Delete") + "\nDELETE / CTRL+d / ALT+d\n\n"
                    + message("DataDeletedComments"));

            FxmlControl.removeTooltip(queryButton);
            queryButton.requestFocus();

        } catch (Exception e) {
            logger.error(e.toString());
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
            popError(message("MissDataset") + "\n" + message("SetConditionsComments"));
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
            DatasetEditController controller = (DatasetEditController) FxmlStage.openStage(CommonValues.DatasetEditFxml);
            controller.setValues(this, null);
            controller.getMyStage().toFront();
        } catch (Exception e) {
            logger.error(e.toString());
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
            DatasetEditController controller = (DatasetEditController) FxmlStage.openStage(CommonValues.DatasetEditFxml);
            controller.setValues(this, selected);
            controller.getMyStage().toFront();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void load(String category) {
        sourceController.select(category);
        queryData();
    }

}
