package mara.mybox.controller;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.FlowPane;
import mara.mybox.data.Location;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

/**
 * @Author Mara
 * @CreateDate 2020-07-27
 * @License Apache License Version 2.0
 */
public class LocationDataExportController extends DataExportController {

    @FXML
    protected FlowPane fieldsPane;

    public LocationDataExportController() {
        baseTitle = message("LocationData") + " " + message("Export");
        baseName = "LocationData";
    }

    @Override
    protected void initExportOptions() {
        try {
            super.initExportOptions();
            for (Node node : fieldsPane.getChildren()) {
                CheckBox cb = (CheckBox) node;
                cb.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            if (isSettingValues) {
                                return;
                            }
                            AppVariables.setUserConfigValue(baseName + cb.getText(), cb.isSelected());
                        });
            }
            isSettingValues = true;
            for (Node node : fieldsPane.getChildren()) {
                CheckBox cb = (CheckBox) node;
                cb.setSelected(AppVariables.getUserConfigBoolean(baseName + cb.getText(), true));
            }
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected List<String> columnLabels() {
        columnNames = new ArrayList();
        for (Node node : fieldsPane.getChildren()) {
            CheckBox cb = (CheckBox) node;
            if (cb.isSelected()) {
                columnNames.add(cb.getText());
            }
        }
        return columnNames;
    }

    @Override
    protected void writeExternalCSV(Connection conn, CSVPrinter printer, ResultSet results) {
        try {
            if (columnNames == null) {
                columnLabels();
            }
            Location data = (Location) (dataController.tableDefinition.readData(results));
            printer.printRecord(Location.externalValues(columnNames, data));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    protected void writeXML(Connection conn, FileWriter writer, ResultSet results, String indent) {
        try {
            if (columnNames == null) {
                columnLabels();
            }
            String lang = columnNames.contains(message("zh", "Longitude")) ? "zh" : "en";
            Location data = (Location) (dataController.tableDefinition.readData(results));
            StringBuilder s = new StringBuilder();
            s.append(indent).append("<LocationData ");
            if (columnNames.contains(message(lang, "Dataset"))) {
                s.append(" dataSet=\"").append(data.getDatasetName()).append("\" ");
            }
            if (columnNames.contains(message(lang, "Label")) && data.getLabel() != null) {
                s.append(" label=\"").append(data.getLabel()).append("\"");
            }
            if (columnNames.contains(message(lang, "Address")) && data.getAddress() != null) {
                s.append(" address=\"").append(data.getAddress()).append("\"");
            }
            if (columnNames.contains(message(lang, "Longitude")) && data.getLongitude() >= -180 && data.getLongitude() <= 180) {
                s.append(" longitude=\"").append(data.getLongitude()).append("\"");
            }
            if (columnNames.contains(message(lang, "Latitude")) && data.getLatitude() >= -90 && data.getLatitude() <= 90) {
                s.append(" latitude=\"").append(data.getLatitude()).append("\"");
            }
            if (columnNames.contains(message(lang, "Altitude")) && data.getAltitude() != CommonValues.InvalidDouble) {
                s.append(" altitude=\"").append(data.getAltitude()).append("\"");
            }
            if (columnNames.contains(message(lang, "Precision")) && data.getPrecision() != CommonValues.InvalidDouble) {
                s.append(" precision=\"").append(data.getPrecision()).append("\"");
            }
            if (columnNames.contains(message(lang, "Speed")) && data.getSpeed() != CommonValues.InvalidDouble) {
                s.append(" speed=\"").append(data.getSpeed()).append("\"");
            }
            if (columnNames.contains(message(lang, "Direction")) && data.getDirection() != CommonValues.InvalidInteger) {
                s.append(" direction=\"").append(data.getDirection()).append("\"");
            }
            if (columnNames.contains(message(lang, "CoordinateSystem")) && data.getCoordinateSystem() != null) {
                s.append(" coordinateSystem=\"").append(data.getCoordinateSystem().name()).append("\"");
            }
            if (columnNames.contains(message(lang, "DataValue")) && data.getDataValue() != CommonValues.InvalidDouble) {
                s.append(" dataValue=\"").append(data.getDataValue()).append("\"");
            }
            if (columnNames.contains(message(lang, "DataSize")) && data.getDataSize() != CommonValues.InvalidDouble) {
                s.append(" dataSize=\"").append(data.getDataSize()).append("\"");
            }
            if (columnNames.contains(message(lang, "StartTime")) && data.getStartTime() != CommonValues.InvalidLong) {
                s.append(" startTime=\"").append(data.getStartTimeText()).append("\"");
            }
            if (columnNames.contains(message(lang, "EndTime")) && data.getEndTime() != CommonValues.InvalidLong) {
                s.append(" endTime=\"").append(data.getEndTimeText()).append("\"");
            }
            if (columnNames.contains(message(lang, "Image")) && data.getImage() != null) {
                s.append(" image=\"").append(data.getImage()).append("\"");
            }
            if (columnNames.contains(message(lang, "Comments")) && data.getComments() != null) {
                s.append(" comments=\"").append(data.getComments()).append("\"");
            }
            s.append(" />\n");
            writer.write(s.toString());
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    @Override
    protected String writeJSON(Connection conn, FileWriter writer, ResultSet results, String indent) {
        try {
            if (columnNames == null) {
                columnLabels();
            }
            String lang = columnNames.contains(message("zh", "Longitude")) ? "zh" : "en";
            Location data = (Location) (dataController.tableDefinition.readData(results));
            StringBuilder s = new StringBuilder();
            s.append(indent).append("{");
            boolean v = false;
            if (columnNames.contains(message(lang, "Dataset"))) {
                s.append("\"dataSet\":\"").append(data.getDatasetName()).append("\"");
                v = true;
            }
            if (columnNames.contains(message(lang, "Label")) && data.getLabel() != null) {
                if (v) {
                    s.append(",");
                }
                v = true;
                s.append("\"label\":\"").append(data.getLabel()).append("\"");
            }
            if (columnNames.contains(message(lang, "Address")) && data.getAddress() != null) {
                if (v) {
                    s.append(",");
                }
                v = true;
                s.append("\"address\":\"").append(data.getAddress()).append("\"");
            }
            if (columnNames.contains(message(lang, "Longitude")) && data.getLongitude() >= -180 && data.getLongitude() <= 180) {
                if (v) {
                    s.append(",");
                }
                v = true;
                s.append("\"longitude\":").append(data.getLongitude());
            }
            if (columnNames.contains(message(lang, "Latitude")) && data.getLatitude() >= -90 && data.getLatitude() <= 90) {
                if (v) {
                    s.append(",");
                }
                v = true;
                s.append("\"latitude\":").append(data.getLatitude());
            }
            if (columnNames.contains(message(lang, "Altitude")) && data.getAltitude() != CommonValues.InvalidDouble) {
                if (v) {
                    s.append(",");
                }
                v = true;
                s.append("\"altitude\":").append(data.getAltitude());
            }
            if (columnNames.contains(message(lang, "Precision")) && data.getPrecision() != CommonValues.InvalidDouble) {
                if (v) {
                    s.append(",");
                }
                v = true;
                s.append("\"precision\":").append(data.getPrecision());
            }
            if (columnNames.contains(message(lang, "Speed")) && data.getSpeed() != CommonValues.InvalidDouble) {
                if (v) {
                    s.append(",");
                }
                v = true;
                s.append("\"speed\":").append(data.getSpeed());
            }
            if (columnNames.contains(message(lang, "Direction")) && data.getDirection() != CommonValues.InvalidInteger) {
                if (v) {
                    s.append(",");
                }
                v = true;
                s.append("\"direction\":").append(data.getDirection());
            }
            if (columnNames.contains(message(lang, "CoordinateSystem")) && data.getCoordinateSystem() != null) {
                if (v) {
                    s.append(",");
                }
                v = true;
                s.append("\"coordinateSystem\":\"").append(data.getCoordinateSystem().name()).append("\"");
            }
            if (columnNames.contains(message(lang, "DataValue")) && data.getDataValue() != CommonValues.InvalidDouble) {
                if (v) {
                    s.append(",");
                }
                v = true;
                s.append("\"dataValue\":").append(data.getDataValue());
            }
            if (columnNames.contains(message(lang, "DataSize")) && data.getDataSize() != CommonValues.InvalidDouble) {
                if (v) {
                    s.append(",");
                }
                v = true;
                s.append("\"dataSize\":").append(data.getDataSize());
            }
            if (columnNames.contains(message(lang, "StartTime")) && data.getStartTime() != CommonValues.InvalidLong) {
                if (v) {
                    s.append(",");
                }
                v = true;
                s.append("\"startTime\":\"").append(data.getStartTimeText()).append("\"");
            }
            if (columnNames.contains(message(lang, "EndTime")) && data.getEndTime() != CommonValues.InvalidLong) {
                if (v) {
                    s.append(",");
                }
                v = true;
                s.append("\"endTime\":\"").append(data.getEndTimeText()).append("\"");
            }
            if (columnNames.contains(message(lang, "Image")) && data.getImage() != null) {
                if (v) {
                    s.append(",");
                }
                v = true;
                s.append("\"image\":\"").append(data.getImage()).append("\"");
            }
            if (columnNames.contains(message(lang, "Comments")) && data.getComments() != null) {
                if (v) {
                    s.append(",");
                }
                s.append("\"comments\":\"").append(data.getComments()).append("\"");
            }
            return s.toString();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    @Override
    protected void writeExcel(Connection conn, XSSFSheet sheet, ResultSet results, int count) {
        try {
            if (columnNames == null) {
                columnLabels();
            }
            Location data = (Location) (dataController.tableDefinition.readData(results));
            List<String> row = Location.externalValues(columnNames, data);
            XSSFRow sheetRow = sheet.createRow(count + 1);
            for (int j = 0; j < row.size(); j++) {
                XSSFCell cell = sheetRow.createCell(j);
                cell.setCellValue(row.get(j));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    protected List<String> htmlRow(Connection conn, ResultSet results) {
        try {
            if (columnNames == null) {
                columnLabels();
            }
            Location data = (Location) (dataController.tableDefinition.readData(results));
            return Location.externalValues(columnNames, data);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

}
