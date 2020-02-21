package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.StringTable;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-2-3
 * @License Apache License Version 2.0
 */
public class GeographyCodeController extends TableManageController<GeographyCode> {

    @FXML
    protected TableColumn<GeographyCode, String> addressColumn, fullAddressColumn,
            countryColumn, provinceColumn, cityColumn, citycodeColumn, districtColumn, townshipColumn,
            neighborhoodColumn, buildingColumn, administrativeCodeColumn, streetColumn, numberColumn, levelColumn;
    @FXML
    protected TableColumn<GeographyCode, Double> longtitudeColumn, latitudeColumn;
    @FXML
    protected Button locationButton, examplesButton;

    public GeographyCodeController() {
        baseTitle = message("GeographyCode");

        dataName = "Geography_Code";

    }

    @Override
    protected void initColumns() {
        try {
            addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
            if (citycodeColumn != null) {
                longtitudeColumn.setCellValueFactory(new PropertyValueFactory<>("longitude"));
            }
            if (citycodeColumn != null) {
                latitudeColumn.setCellValueFactory(new PropertyValueFactory<>("latitude"));
            }
            if (citycodeColumn != null) {
                fullAddressColumn.setCellValueFactory(new PropertyValueFactory<>("fullAddress"));
            }
            if (citycodeColumn != null) {
                countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
            }
            if (citycodeColumn != null) {
                provinceColumn.setCellValueFactory(new PropertyValueFactory<>("province"));
            }
            if (citycodeColumn != null) {
                cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
            }
            if (citycodeColumn != null) {
                citycodeColumn.setCellValueFactory(new PropertyValueFactory<>("citycode"));
            }
            if (citycodeColumn != null) {
                districtColumn.setCellValueFactory(new PropertyValueFactory<>("district"));
            }
            if (citycodeColumn != null) {
                townshipColumn.setCellValueFactory(new PropertyValueFactory<>("township"));
            }
            if (citycodeColumn != null) {
                neighborhoodColumn.setCellValueFactory(new PropertyValueFactory<>("neighborhood"));
            }
            if (citycodeColumn != null) {
                buildingColumn.setCellValueFactory(new PropertyValueFactory<>("building"));
            }
            if (citycodeColumn != null) {
                administrativeCodeColumn.setCellValueFactory(new PropertyValueFactory<>("AdministrativeCode"));
            }
            if (citycodeColumn != null) {
                streetColumn.setCellValueFactory(new PropertyValueFactory<>("street"));
            }
            if (citycodeColumn != null) {
                numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
            }
            if (citycodeColumn != null) {
                levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        FxmlControl.setTooltip(examplesButton, message("GeographyCodeExamplesComments"));

        String backFile = AppVariables.getSystemConfigValue("GeographyCodeBackup6.1.5", "");
        if (!backFile.isBlank()) {
            alertInformation(message("GeographyCodeBackup615Comments")
                    + "\n\n" + backFile);
            AppVariables.deleteSystemConfigValue("GeographyCodeBackup6.1.5");
            examplesAction();
        } else if (!AppVariables.getSystemConfigBoolean("GeographyCodeExamples6.1.5", false)) {
            AppVariables.setSystemConfigValue("GeographyCodeExamples6.1.5", true);
            examplesAction();
        }
    }

    @Override
    public int readDataSize() {
        return TableGeographyCode.size();
    }

    @Override
    public List<GeographyCode> readData(int offset, int number) {
        return TableGeographyCode.read(offset, number);
    }

    @Override
    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        super.checkSelected();
        int selection = tableView.getSelectionModel().getSelectedIndices().size();
        if (locationButton != null) {
            locationButton.setDisable(selection == 0);
        }
    }

    @Override
    public void itemDoubleClicked() {
        editAction();
    }

    @FXML
    @Override
    public void addAction() {
        try {
            GeographyCodeEditController controller = (GeographyCodeEditController) openStage(CommonValues.GeographyCodeEditFxml);
            controller.parent = this;
            controller.getMyStage().toFront();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void editAction() {
        GeographyCode selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            GeographyCodeEditController controller = (GeographyCodeEditController) openStage(CommonValues.GeographyCodeEditFxml);
            controller.parent = this;
            controller.loadCode(selected);
            controller.getMyStage().toFront();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void viewAction() {
        GeographyCode selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        HtmlTools.viewHtml(message("GeographyCode"), selected.geography("</br>"));
    }

    @FXML
    public void locationAction() {
        try {
            GeographyCode code = tableView.getSelectionModel().getSelectedItem();
            if (code == null) {
                return;
            }
            int mapZoom = 4;
            if (code.getLevel() != null && message("Country").equals(code.getLevel())) {
                mapZoom = 3;
            }
            LocationInMapController controller = (LocationInMapController) openStage(CommonValues.LocationInMapFxml);
            controller.load(code.getLongitude(), code.getLatitude(), mapZoom);
            controller.getMyStage().setAlwaysOnTop(true);
            controller.getMyStage().toFront();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected boolean deleteSelectedData() {
        List<GeographyCode> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return false;
        }
        return TableGeographyCode.delete(selected);
    }

    @Override
    protected boolean clearData() {
        return new TableGeographyCode().clear();
    }

    @FXML
    public void htmlAction() {
        try {
            List<GeographyCode> rows = tableView.getSelectionModel().getSelectedItems();
            if (rows == null || rows.isEmpty()) {
                rows = tableData;
            }
            if (rows == null || rows.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            List<String> names = new ArrayList<>();
            for (TableColumn column : tableView.getColumns()) {
                names.add(column.getText());
            }
            StringTable table = new StringTable(names, message("GeographyCode"));
            for (GeographyCode data : rows) {
                List<String> row = new ArrayList<>();
                for (TableColumn column : tableView.getColumns()) {
                    if (message("Address").equals(column.getText())) {
                        row.add(data.getAddress());
                    } else if (message("Longitude").equals(column.getText())) {
                        row.add(data.getLongitude() + "");
                    } else if (message("Latitude").equals(column.getText())) {
                        row.add(data.getLongitude() + "");
                    } else if (message("FullAddress").equals(column.getText())) {
                        if (data.getFullAddress() != null) {
                            row.add(data.getFullAddress());
                        } else {
                            row.add("");
                        }
                    } else if (message("Country").equals(column.getText())) {
                        if (data.getCountry() != null) {
                            row.add(data.getCountry());
                        } else {
                            row.add("");
                        }
                    } else if (message("Province").equals(column.getText())) {
                        if (data.getProvince() != null) {
                            row.add(data.getProvince());
                        } else {
                            row.add("");
                        }
                    } else if (message("City").equals(column.getText())) {
                        if (data.getCity() != null) {
                            row.add(data.getCity());
                        } else {
                            row.add("");
                        }
                    } else if (message("Citycode").equals(column.getText())) {
                        if (data.getCitycode() != null) {
                            row.add(data.getCitycode());
                        } else {
                            row.add("");
                        }
                    } else if (message("District").equals(column.getText())) {
                        if (data.getDistrict() != null) {
                            row.add(data.getDistrict());
                        } else {
                            row.add("");
                        }
                    } else if (message("Township").equals(column.getText())) {
                        if (data.getTownship() != null) {
                            row.add(data.getTownship());
                        } else {
                            row.add("");
                        }
                    } else if (message("Neighborhood").equals(column.getText())) {
                        if (data.getNeighborhood() != null) {
                            row.add(data.getNeighborhood());
                        } else {
                            row.add("");
                        }
                    } else if (message("Building").equals(column.getText())) {
                        if (data.getBuilding() != null) {
                            row.add(data.getBuilding());
                        } else {
                            row.add("");
                        }
                    } else if (message("AdministrativeCode").equals(column.getText())) {
                        if (data.getAdministrativeCode() != null) {
                            row.add(data.getAdministrativeCode());
                        } else {
                            row.add("");
                        }
                    } else if (message("Street").equals(column.getText())) {
                        if (data.getStreet() != null) {
                            row.add(data.getStreet());
                        } else {
                            row.add("");
                        }
                    } else if (message("Number").equals(column.getText())) {
                        if (data.getNumber() != null) {
                            row.add(data.getNumber());
                        } else {
                            row.add("");
                        }
                    } else if (message("Level").equals(column.getText())) {

                        if (data.getLevel() != null) {
                            row.add(data.getLevel());
                        } else {
                            row.add("");
                        }
                    }
                }

                table.add(row);
            }
            table.editHtml();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void loadExamples() {
        GeographyCode.importCodes();

//        GeographyCode.initChineseProvincesCodes();
//        GeographyCode.initCountriesCodes();
//        try {
//            StringBuilder s = new StringBuilder();
//            try ( BufferedReader reader = new BufferedReader(
//                    new FileReader("D:\\tmp\\0\\aa.txt", Charset.forName("utf-8")))) {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    String a = line.trim();
//                    if (!a.isEmpty()) {
//                        GeographyCode.query(a);
//                    }
//                }
//            }
//        } catch (Exception e) {
//
//        }
    }

}
