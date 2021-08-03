package mara.mybox.controller;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.data.CoordinateSystem;
import mara.mybox.db.data.Dataset;
import mara.mybox.db.data.Location;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.TableDataset;
import mara.mybox.db.table.TableLocationData;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2020-07-26
 * @License Apache License Version 2.0
 */
public class LocationDataImportCSVController extends BaseImportCsvController<Location> {

    protected Map<String, Dataset> datasets;
    protected TableLocationData tableLocationData;
    protected TableDataset tableDataset;

    public LocationDataImportCSVController() {
        baseTitle = Languages.message("ImportLocationDataCVS");
    }

    @Override
    public void initValues() {
        super.initValues();
        datasets = new HashMap<>();
        tableLocationData = new TableLocationData();
    }

    @Override
    public void initControls() {
        super.initControls();
        csvEditController.init(this, tableLocationData);
    }

    @Override
    public BaseTable getTableDefinition() {
        tableDefinition = tableLocationData;
        if (tableDataset == null) {
            tableDataset = new TableDataset();
        }
        return tableDefinition;
    }

    @Override
    protected boolean validHeader(List<String> names) {
        String lang = names.contains(Languages.message("zh", "Dataset")) ? "zh" : "en";
        if (!names.contains(Languages.message(lang, "Longitude"))
                || !names.contains(Languages.message(lang, "Latitude"))) {
            updateLogs(Languages.message("InvalidFormat"), true);
            return false;
        }
        return true;
    }

    @Override
    protected Location readRecord(Connection conn, List<String> names, CSVRecord record) {
        try {
            String lang = names.contains(Languages.message("zh", "Dataset")) ? "zh" : "en";
            Location data = new Location();
            String datasetName = record.get(Languages.message(lang, "Dataset"));
            Dataset dataset = datasets.get(datasetName);
            if (dataset == null) {
                dataset = tableLocationData.queryAndCreateDataset(conn, datasetName);
                datasets.put(datasetName, dataset);
            }
            data.setDataset(dataset);
            data.setDatasetid(dataset.getId());
            if (names.contains(Languages.message(lang, "Label"))) {
                data.setLabel(record.get(Languages.message(lang, "Label")));
            }
            if (names.contains(Languages.message(lang, "Address"))) {
                data.setAddress(record.get(Languages.message(lang, "Address")));
            }
            String v = record.get(Languages.message(lang, "Longitude"));
            if (v != null) {
                data.setLongitude(Double.parseDouble(v));
            }
            v = record.get(Languages.message(lang, "Latitude"));
            if (v != null) {
                data.setLatitude(Double.parseDouble(v));
            }
            if (names.contains(Languages.message(lang, "Altitude"))) {
                v = record.get(Languages.message(lang, "Altitude"));
                if (v != null) {
                    data.setAltitude(Double.parseDouble(v));
                }
            }
            if (names.contains(Languages.message(lang, "Precision"))) {
                v = record.get(Languages.message(lang, "Precision"));
                if (v != null) {
                    data.setPrecision(Double.parseDouble(v));
                }
            }
            if (names.contains(Languages.message(lang, "Speed"))) {
                v = record.get(Languages.message(lang, "Speed"));
                if (v != null) {
                    data.setSpeed(Double.parseDouble(v));
                }
            }
            if (names.contains(Languages.message(lang, "Direction"))) {
                v = record.get(Languages.message(lang, "Direction"));
                if (v != null) {
                    data.setDirection(Short.parseShort(v));
                }
            }
            if (names.contains(Languages.message(lang, "CoordinateSystem"))) {
                v = record.get(Languages.message(lang, "CoordinateSystem"));
                if (v != null) {
                    data.setCoordinateSystem(new CoordinateSystem(v));
                }
            }
            if (names.contains(Languages.message(lang, "DataValue"))) {
                v = record.get(Languages.message(lang, "DataValue"));
                if (v != null) {
                    data.setDataValue(Double.parseDouble(v));
                }
            }
            if (names.contains(Languages.message(lang, "DataSize"))) {
                v = record.get(Languages.message(lang, "DataSize"));
                if (v != null) {
                    data.setDataSize(Double.parseDouble(v));
                }
            }
            if (names.contains(Languages.message(lang, "StartTime"))) {
                v = record.get(Languages.message(lang, "StartTime"));
                if (v != null) {
                    Date d = DateTools.encodeEra(v);
                    if (d != null) {
                        data.setStartTime(d.getTime());
                    }
                }
            }
            if (names.contains(Languages.message(lang, "EndTime"))) {
                v = record.get(Languages.message(lang, "EndTime"));
                if (v != null) {
                    data.setEndTime(DateTools.encodeEra(v).getTime());
                    Date d = DateTools.encodeEra(v);
                    if (d != null) {
                        data.setEndTime(d.getTime());
                    }
                }
            }
            if (names.contains(Languages.message(lang, "Image"))) {
                data.setImageName(record.get(Languages.message(lang, "Image")));
            }
            if (names.contains(Languages.message(lang, "Comments"))) {
                data.setComments(record.get(Languages.message(lang, "Comments")));
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    @Override
    protected String dataValues(Location data) {
        return data.getDatasetName() + " \""
                + (data.getLabel() != null ? data.getLabel() : data.getAddress())
                + "\" " + data.getLongitude() + "," + data.getLatitude();
    }

}
