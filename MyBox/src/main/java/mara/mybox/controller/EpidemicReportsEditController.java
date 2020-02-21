package mara.mybox.controller;

import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.util.converter.IntegerStringConverter;
import mara.mybox.data.EpidemicReport;
import mara.mybox.db.TableEpidemicReport;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import thridparty.TableAutoCommitCell;

/**
 * @Author Mara
 * @CreateDate 2020-2-4
 * @License Apache License Version 2.0
 */
public class EpidemicReportsEditController extends TableManageController<EpidemicReport> {

    protected EpidemicReportsController parent;
    protected int confirmed, suspected, dead, healed;
    protected Date time;
    protected String currentDataset;

    @FXML
    protected TextField timeInput;
    @FXML
    protected ComboBox<String> datasetSelector;
    @FXML
    protected TableColumn<EpidemicReport, String> countryColumn, provinceColumn, commentsColumn;
    @FXML
    protected TableColumn<EpidemicReport, Integer> confirmedColumn, suspectedColumn, headledColumn, deadColumn;

    public EpidemicReportsEditController() {
        baseTitle = AppVariables.message("DiseaseReport");
    }

    @Override
    public void initializeNext() {
        try {
            tableData = FXCollections.observableArrayList();

            initColumns();
            tableView.setItems(tableData);

            datasetSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkDataset();
                    });
            checkDataset();

            timeInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkTime();
                    });

            FxmlControl.setTooltip(timeInput, message("LocationDataTimeComments"));

            saveButton.disableProperty().bind(datasetSelector.getEditor().styleProperty().isEqualTo(badStyle)
                    .or(timeInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void initColumns() {
        try {

            if (countryColumn != null) {
                countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
            }
            if (provinceColumn != null) {
                provinceColumn.setCellValueFactory(new PropertyValueFactory<>("province"));
            }

            confirmedColumn.setCellValueFactory(new PropertyValueFactory<>("confirmed"));
            confirmedColumn.setCellFactory((TableColumn<EpidemicReport, Integer> param) -> {
                TableAutoCommitCell<EpidemicReport, Integer> cell
                        = new TableAutoCommitCell<EpidemicReport, Integer>(new IntegerStringConverter()) {
                    @Override
                    public void commitEdit(Integer val) {
                        if (val < 0) {
                            cancelEdit();
                        } else {
                            super.commitEdit(val);
                        }
                    }
                };
                return cell;
            });
            confirmedColumn.setOnEditCommit((TableColumn.CellEditEvent<EpidemicReport, Integer> t) -> {
                if (t == null) {
                    return;
                }
                if (t.getNewValue() >= 0) {
                    EpidemicReport row = t.getRowValue();
                    row.setConfirmed(t.getNewValue());
                }
            });
            confirmedColumn.getStyleClass().add("editable-column");

            suspectedColumn.setCellValueFactory(new PropertyValueFactory<>("suspected"));
            suspectedColumn.setCellFactory((TableColumn<EpidemicReport, Integer> param) -> {
                TableAutoCommitCell<EpidemicReport, Integer> cell
                        = new TableAutoCommitCell<EpidemicReport, Integer>(new IntegerStringConverter()) {
                    @Override
                    public void commitEdit(Integer val) {
                        if (val < 0) {
                            cancelEdit();
                        } else {
                            super.commitEdit(val);
                        }
                    }
                };
                return cell;
            });
            suspectedColumn.setOnEditCommit((TableColumn.CellEditEvent<EpidemicReport, Integer> t) -> {
                if (t == null) {
                    return;
                }
                if (t.getNewValue() >= 0) {
                    EpidemicReport row = t.getRowValue();
                    row.setSuspected(t.getNewValue());
                }
            });
            suspectedColumn.getStyleClass().add("editable-column");

            headledColumn.setCellValueFactory(new PropertyValueFactory<>("healed"));
            headledColumn.setCellFactory((TableColumn<EpidemicReport, Integer> param) -> {
                TableAutoCommitCell<EpidemicReport, Integer> cell
                        = new TableAutoCommitCell<EpidemicReport, Integer>(new IntegerStringConverter()) {
                    @Override
                    public void commitEdit(Integer val) {
                        if (val < 0) {
                            cancelEdit();
                        } else {
                            super.commitEdit(val);
                        }
                    }
                };
                return cell;
            });
            headledColumn.setOnEditCommit((TableColumn.CellEditEvent<EpidemicReport, Integer> t) -> {
                if (t == null) {
                    return;
                }
                if (t.getNewValue() >= 0) {
                    EpidemicReport row = t.getRowValue();
                    row.setHealed(t.getNewValue());
                }
            });
            headledColumn.getStyleClass().add("editable-column");

            deadColumn.setCellValueFactory(new PropertyValueFactory<>("dead"));
            deadColumn.setCellFactory((TableColumn<EpidemicReport, Integer> param) -> {
                TableAutoCommitCell<EpidemicReport, Integer> cell
                        = new TableAutoCommitCell<EpidemicReport, Integer>(new IntegerStringConverter()) {
                    @Override
                    public void commitEdit(Integer val) {
                        if (val < 0) {
                            cancelEdit();
                        } else {
                            super.commitEdit(val);
                        }
                    }
                };
                return cell;
            });
            deadColumn.setOnEditCommit((TableColumn.CellEditEvent<EpidemicReport, Integer> t) -> {
                if (t == null) {
                    return;
                }
                if (t.getNewValue() >= 0) {
                    EpidemicReport row = t.getRowValue();
                    row.setDead(t.getNewValue());
                }
            });
            deadColumn.getStyleClass().add("editable-column");

            commentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));
            commentsColumn.setCellFactory(TableAutoCommitCell.forTableColumn());
            commentsColumn.setOnEditCommit((TableColumn.CellEditEvent<EpidemicReport, String> t) -> {
                if (t == null) {
                    return;
                }
                EpidemicReport row = t.getRowValue();
                row.setComments(t.getNewValue());
            });
            commentsColumn.getStyleClass().add("editable-column");

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void load(String dataset) {
        tableData.clear();
        currentDataset = dataset;
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private List<String> datasets;
                private List<EpidemicReport> data;

                @Override
                protected boolean handle() {
                    datasets = TableEpidemicReport.datasets();
                    if (time == null) {
                        time = new Date();
                    }
                    data = initData();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    isSettingValues = true;
                    datasetSelector.getItems().clear();
                    if (datasets != null) {
                        datasetSelector.getItems().addAll(datasets);
                    }
                    if (currentDataset != null && !currentDataset.trim().isBlank()) {
                        datasetSelector.setValue(dataset);
                        datasetSelector.getEditor().setStyle(null);
                    } else {
                        datasetSelector.setValue(null);
                        datasetSelector.getEditor().setStyle(badStyle);
                    }

                    timeInput.setText(DateTools.datetimeToString(time));
                    if (data != null) {
                        tableData.addAll(data);
                    }
                    isSettingValues = false;
                    tableView.refresh();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected List<EpidemicReport> initData() {
        return null;
    }

    protected void checkDataset() {
        if (isSettingValues) {
            return;
        }
        try {
            load(datasetSelector.getValue());
        } catch (Exception e) {
            datasetSelector.getEditor().setStyle(badStyle);
        }
    }

    protected void checkTime() {
        try {
            String value = timeInput.getText().trim();
            if (value.isBlank()) {
                time = new Date();
                timeInput.setStyle(null);
                return;
            }
            Date v = DateTools.stringToDatetime(value);
            if (v != null) {
                time = v;
                timeInput.setStyle(null);
            } else {
                timeInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            timeInput.setStyle(badStyle);
        }
    }

    @FXML
    @Override
    public void saveAction() {
        if (currentDataset == null || tableData.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    TableEpidemicReport.write(tableData);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (parent != null) {
                        parent.loadTree();
                        parent.getMyStage().toFront();
                    }
                    if (saveCloseCheck.isSelected()) {
                        closeStage();
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

}
