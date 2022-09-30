package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.data.Era;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Dataset;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-7-13
 * @License Apache License Version 2.0
 */
public class TableDataset extends BaseTable<Dataset> {
    
    public TableDataset() {
        tableName = "Dataset";
        defineColumns();
    }
    
    public TableDataset(boolean defineColumns) {
        tableName = "Dataset";
        if (defineColumns) {
            defineColumns();
        }
    }
    
    public final TableDataset defineColumns() {
        addColumn(new ColumnDefinition("dsid", ColumnType.Long, true, true).setAuto(true));
        Map<Object, String> lvalues = new LinkedHashMap<>();
        lvalues.put("Location_data", Languages.tableMessage("Location_data"));
        addColumn(new ColumnDefinition("data_category", ColumnType.String, true).setLength(StringMaxLength).setDisplayMap(lvalues));
        addColumn(new ColumnDefinition("data_set", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("time_format", ColumnType.Short).setDisplayMap(Era.values()));
        addColumn(new ColumnDefinition("time_format_omitAD", ColumnType.Boolean));
        addColumn(new ColumnDefinition("text_color", ColumnType.Color));
        addColumn(new ColumnDefinition("text_background_color", ColumnType.Color));
        addColumn(new ColumnDefinition("chart_color", ColumnType.Color));
        addColumn(new ColumnDefinition("dataset_image", ColumnType.Image));
        addColumn(new ColumnDefinition("dataset_comments", ColumnType.Text).setLength(StringMaxLength));
        return this;
    }
    
    public static final String Create_Index_unique
            = "CREATE UNIQUE INDEX Dataset_unique_index on Dataset (  data_category, data_set )";
    
    public static final String UniqueQeury
            = "SELECT * FROM Dataset WHERE data_category=? AND data_set=?";
    
    public static final String CategoryQeury
            = "SELECT * FROM Dataset WHERE data_category=? ";
    
    public static List<String> dataCategories() {
        List<String> dataCategories = new ArrayList<>();
        dataCategories.addAll(Arrays.asList(
                "Location_Data"
        ));
        return dataCategories;
    }
    
    public Dataset read(String category, String dataset) {
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return read(conn, category, dataset);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }
    
    public Dataset read(Connection conn, String category, String datasetName) {
        if (conn == null || category == null || datasetName == null) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(UniqueQeury)) {
            statement.setString(1, category);
            statement.setString(2, datasetName);
            return query(conn, statement);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }
    
    public List<Dataset> datasets(String category) {
        List<Dataset> dataList = new ArrayList<>();
        if (category == null || category.trim().isBlank()) {
            return dataList;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return datasets(conn, category);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return dataList;
    }
    
    public List<Dataset> datasets(Connection conn, String category) {
        List<Dataset> dataList = new ArrayList<>();
        if (conn == null || category == null || category.trim().isBlank()) {
            return dataList;
        }
        try ( PreparedStatement statement = conn.prepareStatement(CategoryQeury)) {
            statement.setString(1, category);
            conn.setAutoCommit(true);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Dataset data = (Dataset) readData(results);
                    dataList.add(data);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return dataList;
    }
    
    public List<String> datasetNames(String category) {
        List<String> dataList = new ArrayList<>();
        if (category == null || category.trim().isBlank()) {
            return dataList;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return datasetNames(conn, category);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return dataList;
    }
    
    public List<String> datasetNames(Connection conn, String category) {
        List<String> dataList = new ArrayList<>();
        if (conn == null || category == null || category.trim().isBlank()) {
            return dataList;
        }
        try ( PreparedStatement statement = conn.prepareStatement(CategoryQeury)) {
            statement.setString(1, category);
            conn.setAutoCommit(true);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Dataset data = (Dataset) readData(results);
                    dataList.add(data.getDataSet());
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return dataList;
    }
    
    @Override
    public List<String> importNecessaryFields() {
        return Arrays.asList(Languages.message("DataSet"), Languages.message("Time"), Languages.message("Confirmed"), Languages.message("Healed"), Languages.message("Dead"),
                Languages.message("Level"), Languages.message("Continent"), Languages.message("Country"), Languages.message("Province"), Languages.message("City"),
                Languages.message("County"), Languages.message("Town"), Languages.message("Village"), Languages.message("Building"), Languages.message("PointOfInterest")
        );
    }
    
    @Override
    public List<String> importAllFields() {
        return Arrays.asList(Languages.message("DataSet"), Languages.message("Time"), Languages.message("Confirmed"), Languages.message("Healed"), Languages.message("Dead"), Languages.message("DataSource"),
                Languages.message("IncreasedConfirmed"), Languages.message("IncreasedHealed"), Languages.message("IncreasedDead"),
                Languages.message("HealedConfirmedPermillage"), Languages.message("DeadConfirmedPermillage"), Languages.message("ConfirmedPopulationPermillage"),
                Languages.message("DeadPopulationPermillage"), Languages.message("HealedPopulationPermillage"),
                Languages.message("ConfirmedAreaPermillage"), Languages.message("HealedAreaPermillage"), Languages.message("DeadAreaPermillage"),
                Languages.message("Longitude"), Languages.message("Latitude"), Languages.message("Level"),
                Languages.message("Continent"), Languages.message("Country"), Languages.message("Province"), Languages.message("City"),
                Languages.message("County"), Languages.message("Town"), Languages.message("Village"), Languages.message("Building"), Languages.message("PointOfInterest")
        );
    }
    
}
