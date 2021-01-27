package mara.mybox.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.util.converter.LongStringConverter;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.data.EpidemicReport;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.db.table.TableEpidemicReport;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import thridparty.TableAutoCommitCell;

/**
 * @Author Mara
 * @CreateDate 2020-2-4
 * @License Apache License Version 2.0
 */
public class EpidemicReportsEditController extends BaseDataTableController<EpidemicReport> {

    protected EpidemicReportsController reportsController;
    protected long time;
    protected boolean isChineseProvince;

    @FXML
    protected Label titleLabel;
    @FXML
    protected TextField timeInput;
    @FXML
    protected ComboBox<String> datasetSelector;
    @FXML
    protected TableColumn<EpidemicReport, String> locationColumn;
    @FXML
    protected TableColumn<EpidemicReport, Long> confirmedColumn, headledColumn, deadColumn;

    public EpidemicReportsEditController() {
        baseTitle = AppVariables.message("EpidemicReport");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            tableData = FXCollections.observableArrayList();
            time = -1;
            isChineseProvince = true;

            initColumns();
            tableView.setItems(tableData);

            timeInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkTime();
                    });

            FxmlControl.setTooltip(timeInput, message("TimeComments"));

            saveButton.disableProperty().bind(timeInput.styleProperty().isEqualTo(badStyle));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initColumns() {
        try {
            locationColumn.setCellValueFactory(new PropertyValueFactory<>("locationFullName"));
            confirmedColumn.setCellValueFactory(new PropertyValueFactory<>("confirmed"));
            confirmedColumn.setCellFactory((TableColumn<EpidemicReport, Long> param) -> {
                TableAutoCommitCell<EpidemicReport, Long> cell
                        = new TableAutoCommitCell<EpidemicReport, Long>(new LongStringConverter()) {
                    @Override
                    public void commitEdit(Long val) {
                        if (val < 0) {
                            cancelEdit();
                        } else {
                            super.commitEdit(val);
                        }
                    }
                };
                return cell;
            });
            confirmedColumn.setOnEditCommit((TableColumn.CellEditEvent<EpidemicReport, Long> t) -> {
                if (t == null) {
                    return;
                }
                if (t.getNewValue() >= 0) {
                    EpidemicReport row = t.getRowValue();
                    row.setConfirmed(t.getNewValue());
                }
            });
            confirmedColumn.getStyleClass().add("editable-column");

            headledColumn.setCellValueFactory(new PropertyValueFactory<>("healed"));
            headledColumn.setCellFactory((TableColumn<EpidemicReport, Long> param) -> {
                TableAutoCommitCell<EpidemicReport, Long> cell
                        = new TableAutoCommitCell<EpidemicReport, Long>(new LongStringConverter()) {
                    @Override
                    public void commitEdit(Long val) {
                        if (val < 0) {
                            cancelEdit();
                        } else {
                            super.commitEdit(val);
                        }
                    }
                };
                return cell;
            });
            headledColumn.setOnEditCommit((TableColumn.CellEditEvent<EpidemicReport, Long> t) -> {
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
            deadColumn.setCellFactory((TableColumn<EpidemicReport, Long> param) -> {
                TableAutoCommitCell<EpidemicReport, Long> cell
                        = new TableAutoCommitCell<EpidemicReport, Long>(new LongStringConverter()) {
                    @Override
                    public void commitEdit(Long val) {
                        if (val < 0) {
                            cancelEdit();
                        } else {
                            super.commitEdit(val);
                        }
                    }
                };
                return cell;
            });
            deadColumn.setOnEditCommit((TableColumn.CellEditEvent<EpidemicReport, Long> t) -> {
                if (t == null) {
                    return;
                }
                if (t.getNewValue() >= 0) {
                    EpidemicReport row = t.getRowValue();
                    row.setDead(t.getNewValue());
                }
            });
            deadColumn.getStyleClass().add("editable-column");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void load(EpidemicReportsController reportsController, boolean isChineseProvince) {
        this.reportsController = reportsController;
        tableData.clear();
        if (isChineseProvince) {
            titleLabel.setText(message("ChineseProvincesEpidemicReports"));
            locationColumn.setText(message("Province"));
        } else {
            titleLabel.setText(message("CountriesEpidemicReports"));
            locationColumn.setText(message("Country"));
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private List<String> datasets;
                private List<EpidemicReport> data;

                @Override
                protected boolean handle() {
                    datasets = TableEpidemicReport.datasets();
                    if (time <= 0) {
                        time = new Date().getTime();
                    }
                    if (isChineseProvince) {
                        data = loadChineseProvinces();
                    } else {
                        data = loadCountries();
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    isSettingValues = true;
                    datasetSelector.getItems().clear();
                    if (datasets != null) {
                        datasetSelector.getItems().addAll(datasets);
                        datasetSelector.getSelectionModel().select(0);
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
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected List<EpidemicReport> loadChineseProvinces() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            String sql = "SELECT * FROM Geography_Code WHERE "
                    + " level=4  AND country=100 ORDER BY gcid ";
            List<GeographyCode> provinces = TableGeographyCode.queryCodes(conn, sql, true);
            if (provinces == null || provinces.isEmpty()) {
                GeographyCodeTools.importPredefined(conn);
                provinces = TableGeographyCode.queryCodes(conn, sql, true);
                if (provinces == null || provinces.isEmpty()) {
                    return null;
                }
            }
            List<EpidemicReport> reports = new ArrayList();
            for (GeographyCode province : provinces) {
                EpidemicReport report = new EpidemicReport();
                report.setLocation(province);
                reports.add(report);
            }
            return reports;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    protected List<EpidemicReport> loadCountries() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            String sql = "SELECT * FROM Geography_Code WHERE level=3 ORDER BY gcid ";
            List<GeographyCode> countries = TableGeographyCode.queryCodes(conn, sql, true);
            if (countries == null || countries.isEmpty()) {
                GeographyCodeTools.importPredefined(conn);
                countries = TableGeographyCode.queryCodes(conn, sql, true);
                if (countries == null || countries.isEmpty()) {
                    return null;
                }
            }
            List<EpidemicReport> reports = new ArrayList();
            for (GeographyCode country : countries) {
                EpidemicReport report = new EpidemicReport();
                report.setLocation(country);
                reports.add(report);
            }
            return reports;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    protected void checkTime() {
        try {
            String value = timeInput.getText().trim();
            if (value.isBlank()) {
                time = new Date().getTime();
                timeInput.setStyle(null);
                return;
            }
            Date v = DateTools.stringToDatetime(value);
            if (v != null) {
                time = v.getTime();
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
    public void clearAction() {
        loadTableData();
    }

    @FXML
    @Override
    public void saveAction() {
        checkTime();
        if (time <= 0) {
            popError(message("MissTime"));
            return;
        }
        String dataset = datasetSelector.getValue().trim();
        if (dataset.isBlank()) {
            popError(message("MissDataset"));
            return;
        }
        if (tableData.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private long count = 0;

                @Override
                protected boolean handle() {
                    for (Object o : tableData) {
                        EpidemicReport report = (EpidemicReport) o;
                        report.setDataSet(dataset);
                        report.setTime(time);
                        report.setSource((short) 2);
                    }
                    count = TableEpidemicReport.write(tableData, true);
                    return count > 0;
                }

                @Override
                protected void whenSucceeded() {
                    if (reportsController != null) {
                        reportsController.loadTrees(true);
                    }
                    if (saveCloseCheck.isSelected()) {
                        closeStage();
                        if (reportsController != null) {
                            reportsController.getMyStage().toFront();
                        }
                    } else {
                        popInformation(message("Written") + ": " + count);
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

}
