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
public class LocationDataImportMovebankController extends BaseImportCsvController<Location> {

    protected Map<String, Dataset> datasets;
    protected TableLocationData tableLocationData;
    protected TableDataset tableDataset;

    public LocationDataImportMovebankController() {
        baseTitle = AppVariables.message("ImportLocationDataMovebank");
    }

    @Override
    public void initValues() {
        super.initValues();
        datasets = new HashMap<>();
        tableLocationData = new TableLocationData();
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
    public void setLink() {
        link.setText("https://www.datarepository.movebank.org/");
    }

    @Override
    protected boolean validHeader(List<String> names) {
        if (!names.contains("timestamp") || !names.contains("study-name")
                || !names.contains("location-long") || !names.contains("location-lat")) {
            updateLogs(message("InvalidFormat"), true);
            return false;
        }
        return true;
    }

    @Override
    protected Location readRecord(Connection conn, List<String> names, CSVRecord record) {
        try {
            Location data = new Location();
            String datasetName = record.get("study-name");
            Dataset dataset = datasets.get(datasetName);
            if (dataset == null) {
                dataset = tableLocationData.queryAndCreateDataset(conn, datasetName);
                datasets.put(datasetName, dataset);
            }
            data.setDataset(dataset);
            String v = record.get("location-long");
            if (v != null) {
                data.setLongitude(Double.parseDouble(v));
            }
            v = record.get("location-lat");
            if (v != null) {
                data.setLatitude(Double.parseDouble(v));
            }
            data.setCoordinateSystem(CoordinateSystem.WGS84());
            v = record.get("timestamp");
            if (v != null) {
                Date d = DateTools.encodeEra(v);
                if (d != null) {
                    data.setStartTime(d.getTime());
                    data.setEndTime(d.getTime());
                }
            }
            if (names.contains("individual-taxon-canonical-name")) {
                data.setLabel(record.get("individual-taxon-canonical-name"));
            }
            if (names.contains("individual-local-identifiere")) {
                data.setAddress(record.get("individual-local-identifier"));
            }
            if (names.contains("sensor-type")) {
                data.setComments(record.get("sensor-type"));
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
