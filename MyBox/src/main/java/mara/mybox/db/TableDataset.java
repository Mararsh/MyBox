package mara.mybox.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.paint.Color;
import mara.mybox.data.Dataset;
import mara.mybox.data.Era;
import mara.mybox.db.ColumnDefinition.ColumnType;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.failed;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.tableMessage;

/**
 * @Author Mara
 * @CreateDate 2020-7-13
 * @License Apache License Version 2.0
 */
public class TableDataset extends TableBase<Dataset> {

    public TableDataset() {
        tableName = "Dataset";
        addColumn(new ColumnDefinition("dsid", ColumnType.Long, true, true).setIsID(true));
        Map<Object, String> lvalues = new LinkedHashMap<>();
        lvalues.put("Location_data", tableMessage("Location_data"));
        addColumn(new ColumnDefinition("data_category", ColumnType.String, true).setLength(128).setValues(lvalues));
        addColumn(new ColumnDefinition("data_set", ColumnType.String, true).setLength(1024));
        addColumn(new ColumnDefinition("time_format", ColumnType.Short).setValues(Era.values()));
        addColumn(new ColumnDefinition("time_format_omitAD", ColumnType.Boolean));
        addColumn(new ColumnDefinition("text_color", ColumnType.Color));
        addColumn(new ColumnDefinition("text_background_color", ColumnType.Color));
        addColumn(new ColumnDefinition("chart_color", ColumnType.Color));
        addColumn(new ColumnDefinition("dataset_image", ColumnType.File));
        addColumn(new ColumnDefinition("dataset_comments", ColumnType.Text).setLength(32672));
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

    @Override
    public Dataset readData(Connection conn, Dataset data) {
        if (conn == null || data == null) {
            return null;
        }
        try {
            if (data.getId() > 0) {
                try ( PreparedStatement statement = conn.prepareStatement(queryStatement())) {
                    statement.setLong(1, data.getId());
                    return query(conn, statement);
                }
            } else {
                Dataset dataset = (Dataset) data;
                try ( PreparedStatement statement = conn.prepareStatement(UniqueQeury)) {
                    statement.setString(1, dataset.getDataCategory());
                    statement.setString(2, dataset.getDataSet());
                    return query(conn, statement);
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    @Override
    public Dataset readData(ResultSet results) {
        if (results == null) {
            return null;
        }
        try {
            Dataset data = new Dataset();
            data.setId(results.getLong("dsid"));
            data.setDataCategory(results.getString("data_category"));
            data.setDataSet(results.getString("data_set"));
            data.setTimeFormat(Era.format(results.getShort("time_format")));
            data.setOmitAD(results.getBoolean("time_format_omitAD"));
            String rgba = results.getString("text_color");
            if (rgba != null) {
                try {
                    Color color = Color.web(rgba);
                    if (color != null) {
                        data.setTextColor(color);
                    }
                } catch (Exception e) {
                }
            }
            rgba = results.getString("text_background_color");
            if (rgba != null) {
                try {
                    Color color = Color.web(rgba);
                    if (color != null) {
                        data.setBgColor(color);
                    }
                } catch (Exception e) {
                }
            }
            rgba = results.getString("chart_color");
            if (rgba != null) {
                try {
                    Color color = Color.web(rgba);
                    if (color != null) {
                        data.setChartColor(color);
                    }
                } catch (Exception e) {
                }
            }
            String image = results.getString("dataset_image");
            if (image != null) {
                File file = new File(image);
                if (file.exists()) {
                    data.setImage(file);
                }
            }
            data.setComments(results.getString("dataset_comments"));
            return data;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return null;
        }
    }

    @Override
    public int setValues(PreparedStatement statement, Dataset data) {
        if (statement == null || data == null) {
            return -1;
        }
        try {
            int count = 1;
            Dataset dataset = (Dataset) data;
            statement.setString(count++, dataset.getDataCategory());
            statement.setString(count++, dataset.getDataSet());
            statement.setShort(count++, Era.format(dataset.getTimeFormat()));
            statement.setBoolean(count++, dataset.isOmitAD());
            if (dataset.getTextColor() != null) {
                statement.setString(count++, dataset.getTextColor().toString());
            } else {
                statement.setNull(count++, Types.VARCHAR);
            }
            if (dataset.getBgColor() != null) {
                statement.setString(count++, dataset.getBgColor().toString());
            } else {
                statement.setNull(count++, Types.VARCHAR);
            }
            if (dataset.getChartColor() != null) {
                statement.setString(count++, dataset.getChartColor().toString());
            } else {
                statement.setNull(count++, Types.VARCHAR);
            }
            if (dataset.getImage() == null) {
                statement.setNull(count++, Types.VARCHAR);
            } else {
                statement.setString(count++, dataset.getImage().getAbsolutePath());
            }
            if (dataset.getComments() == null) {
                statement.setNull(count++, Types.VARCHAR);
            } else {
                statement.setString(count++, dataset.getComments());
            }
            return count;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return -1;
        }
    }

    public Dataset read(String category, String dataset) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            return read(conn, category, dataset);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public Dataset read(Connection conn, String category, String datasetName) {
        if (conn == null || category == null || datasetName == null) {
            return null;
        }
        Dataset data = new Dataset();
        data.setDataCategory(category);
        data.setDataSet(datasetName);
        return (Dataset) readData(conn, data);
    }

    public List<Dataset> datasets(String category) {
        List<Dataset> dataList = new ArrayList<>();
        if (category == null || category.trim().isBlank()) {
            return dataList;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            return datasets(conn, category);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
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
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Dataset data = (Dataset) readData(results);
                    dataList.add(data);
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return dataList;
    }

    public List<String> datasetNames(String category) {
        List<String> dataList = new ArrayList<>();
        if (category == null || category.trim().isBlank()) {
            return dataList;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return datasetNames(conn, category);
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
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
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Dataset data = (Dataset) readData(results);
                    dataList.add(data.getDataSet());
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return dataList;
    }

}
