package mara.mybox.controller;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.data.CoordinateSystem;
import mara.mybox.data.Dataset;
import mara.mybox.data.Location;
import mara.mybox.db.TableBase;
import mara.mybox.db.TableDataset;
import mara.mybox.db.TableLocationData;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2020-07-26
 * @License Apache License Version 2.0
 */
public class LocationDataImportCSVController extends DataImportController<Location> {

    protected Map<String, Dataset> datasets;
    protected TableLocationData tableLocationData;
    protected TableDataset tableDataset;

    public LocationDataImportCSVController() {
        baseTitle = AppVariables.message("ImportLocationDataCVS");
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
    public TableBase getTableDefinition() {
        tableDefinition = tableLocationData;
        if (tableDataset == null) {
            tableDataset = new TableDataset();
        }
        return tableDefinition;
    }

    @Override
    protected boolean validHeader(List<String> names) {
        String lang = names.contains(message("zh", "Dataset")) ? "zh" : "en";
        if (!names.contains(message(lang, "Longitude"))
                || !names.contains(message(lang, "Latitude"))) {
            updateLogs(message("InvalidFormat"), true);
            return false;
        }
        return true;
    }

    @Override
    protected Location readRecord(Connection conn, List<String> names, CSVRecord record) {
        try {
            String lang = names.contains(message("zh", "Dataset")) ? "zh" : "en";
            Location data = new Location();
            String datasetName = record.get(message(lang, "Dataset"));
            Dataset dataset = datasets.get(datasetName);
            if (dataset == null) {
                dataset = tableLocationData.queryAndCreateDataset(conn, datasetName);
                datasets.put(datasetName, dataset);
            }
            data.setDataset(dataset);
            data.setDatasetid(dataset.getDsid());
            if (names.contains(message(lang, "Label"))) {
                data.setLabel(record.get(message(lang, "Label")));
            }
            if (names.contains(message(lang, "Address"))) {
                data.setAddress(record.get(message(lang, "Address")));
            }
            String v = record.get(message(lang, "Longitude"));
            if (v != null) {
                data.setLongitude(Double.parseDouble(v));
            }
            v = record.get(message(lang, "Latitude"));
            if (v != null) {
                data.setLatitude(Double.parseDouble(v));
            }
            if (names.contains(message(lang, "Altitude"))) {
                v = record.get(message(lang, "Altitude"));
                if (v != null) {
                    data.setAltitude(Double.parseDouble(v));
                }
            }
            if (names.contains(message(lang, "Precision"))) {
                v = record.get(message(lang, "Precision"));
                if (v != null) {
                    data.setPrecision(Double.parseDouble(v));
                }
            }
            if (names.contains(message(lang, "Speed"))) {
                v = record.get(message(lang, "Speed"));
                if (v != null) {
                    data.setSpeed(Double.parseDouble(v));
                }
            }
            if (names.contains(message(lang, "Direction"))) {
                v = record.get(message(lang, "Direction"));
                if (v != null) {
                    data.setDirection(Short.parseShort(v));
                }
            }
            if (names.contains(message(lang, "CoordinateSystem"))) {
                v = record.get(message(lang, "CoordinateSystem"));
                if (v != null) {
                    data.setCoordinateSystem(new CoordinateSystem(v));
                }
            }
            if (names.contains(message(lang, "DataValue"))) {
                v = record.get(message(lang, "DataValue"));
                if (v != null) {
                    data.setDataValue(Double.parseDouble(v));
                }
            }
            if (names.contains(message(lang, "DataSize"))) {
                v = record.get(message(lang, "DataSize"));
                if (v != null) {
                    data.setDataSize(Double.parseDouble(v));
                }
            }
            if (names.contains(message(lang, "StartTime"))) {
                v = record.get(message(lang, "StartTime"));
                if (v != null) {
                    Date d = DateTools.encodeEra(v);
                    if (d != null) {
                        data.setStartTime(d.getTime());
                    }
                }
            }
            if (names.contains(message(lang, "EndTime"))) {
                v = record.get(message(lang, "EndTime"));
                if (v != null) {
                    data.setEndTime(DateTools.encodeEra(v).getTime());
                    Date d = DateTools.encodeEra(v);
                    if (d != null) {
                        data.setEndTime(d.getTime());
                    }
                }
            }
            if (names.contains(message(lang, "Image"))) {
                data.setImage(record.get(message(lang, "Image")));
            }
            if (names.contains(message(lang, "Comments"))) {
                data.setComments(record.get(message(lang, "Comments")));
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
