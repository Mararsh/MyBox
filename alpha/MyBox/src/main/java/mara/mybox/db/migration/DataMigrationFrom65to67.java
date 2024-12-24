package mara.mybox.db.migration;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.application.Platform;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.ImageConvertTools;
import mara.mybox.data.GeoCoordinateSystem;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.tools.Data2DTableTools;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DCell;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.WebHistory;
import static mara.mybox.db.migration.DataMigration.alterColumnLength;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.db.table.TableAlarmClock;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.db.table.TableData2D;
import mara.mybox.db.table.TableData2DCell;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.db.table.TableData2DStyle;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.db.table.TableWebHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.TimeFormats;

/**
 * @Author Mara
 * @CreateDate 2020-06-09
 * @License Apache License Version 2.0
 */
public class DataMigrationFrom65to67 {

    public static void handleVersions(int lastVersion, Connection conn) {
        try {
            if (lastVersion < 6005001) {
                updateIn651(conn);
            }
            if (lastVersion < 6005002) {
                updateIn652(conn);
            }
            if (lastVersion < 6005003) {
                updateIn653(conn);
            }
            if (lastVersion < 6005004) {
                updateIn654(conn);
            }
            if (lastVersion < 6005005) {
                updateIn655(conn);
            }
            if (lastVersion < 6005006) {
                updateIn656(conn);
            }
            if (lastVersion < 6005007) {
                updateIn657(conn);
            }
            if (lastVersion < 6005008) {
                updateIn658(conn);
            }
            if (lastVersion < 6005009) {
                updateIn659(conn);
            }
            if (lastVersion < 6006000) {
                updateIn660(conn);
            }
            if (lastVersion < 6006001) {
                updateIn661(conn);
            }
            if (lastVersion < 6006002) {
                updateIn662(conn);
            }
            if (lastVersion < 6007000) {
                updateIn67(conn);
            }
            if (lastVersion < 6007001) {
                updateIn671(conn);
            }
            if (lastVersion < 6007003) {
                updateIn673(conn);
            }
            if (lastVersion < 6007007) {
                updateIn677(conn);
            }
            if (lastVersion < 6007008) {
                updateIn678(conn);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn678(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            MyBoxLog.info("Updating tables in 6.7.8...");

            conn.setAutoCommit(true);
            statement.executeUpdate("DROP INDEX Tree_Node_title_index");
            statement.executeUpdate(TableInfoNode.Create_Title_Index);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn677(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            MyBoxLog.info("Updating tables in 6.7.7...");

            conn.setAutoCommit(true);
            statement.executeUpdate("ALTER TABLE Tree_Node ADD COLUMN info CLOB ");
            TableInfoNode tableTreeNode = new TableInfoNode();
            ResultSet query = statement.executeQuery("SELECT * FROM Tree_Node");
            while (query.next()) {
                InfoNode node = tableTreeNode.readData(query);
                String value = query.getString("value");
                String more = query.getString("more");
                String info;
                if (value != null && !value.isBlank()) {
                    info = value.trim() + "\n";
                } else {
                    info = "";
                }
                if (more != null && !more.isBlank()) {
                    info += InfoNode.MoreSeparater + "\n" + more.trim();
                }
                node.setInfo(info);
                tableTreeNode.updateData(conn, node);
            }

            statement.executeUpdate("ALTER TABLE Tree_Node DROP COLUMN value");
            statement.executeUpdate("ALTER TABLE Tree_Node DROP COLUMN more");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn673(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            MyBoxLog.info("Updating tables in 6.7.3...");

            conn.setAutoCommit(true);
            statement.executeUpdate("ALTER TABLE Image_Edit_History ADD COLUMN thumbnail_file VARCHAR(" + StringMaxLength + ")");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn671(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            MyBoxLog.info("Updating tables in 6.7.1...");

            conn.setAutoCommit(true);
            statement.executeUpdate("ALTER TABLE Color ADD COLUMN ryb FLOAT ");
            statement.executeUpdate("ALTER TABLE Color_Palette ADD COLUMN description VARCHAR(" + StringMaxLength + ")");
            statement.executeUpdate("ALTER TABLE Color_Palette_Name ADD COLUMN visit_time TIMESTAMP ");
            statement.executeUpdate("DROP INDEX Color_Palette_unique_index");
            statement.executeUpdate("DROP VIEW Color_Palette_View");
            statement.executeUpdate(TableColorPalette.CreateView);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn67(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.7...");

            conn.setAutoCommit(true);
            TableStringValues.clear("GameEliminationImage");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn662(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            MyBoxLog.info("Updating tables in 6.6.2...");

            conn.setAutoCommit(true);
            statement.executeUpdate("ALTER TABLE Data2D_Column DROP COLUMN label");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn661(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            MyBoxLog.info("Updating tables in 6.6.1...");

            conn.setAutoCommit(true);
            statement.executeUpdate("ALTER TABLE Data2D_Column ADD COLUMN scale INT");
            statement.executeUpdate("ALTER TABLE Data2D_Column ADD COLUMN format VARCHAR(" + StringMaxLength + ")");
            statement.executeUpdate("ALTER TABLE Data2D_Column ADD COLUMN fix_year BOOLEAN");
            statement.executeUpdate("ALTER TABLE Data2D_Column ADD COLUMN century INT");
            statement.executeUpdate("ALTER TABLE Data2D_Column ADD COLUMN invalid_as SMALLINT");
            statement.executeUpdate("UPDATE Data2D_Column SET column_type=0  WHERE column_type=2");
            statement.executeUpdate("UPDATE Data2D_Column SET format='" + TimeFormats.Datetime + "' WHERE time_format < 2 AND column_type < 6");
            statement.executeUpdate("UPDATE Data2D_Column SET format='" + TimeFormats.Date + "' WHERE time_format=2 AND column_type < 6");
            statement.executeUpdate("UPDATE Data2D_Column SET format='" + TimeFormats.Year + "' WHERE time_format=3 AND column_type < 6");
            statement.executeUpdate("UPDATE Data2D_Column SET format='" + TimeFormats.Month + "' WHERE time_format=4 AND column_type < 6");
            statement.executeUpdate("UPDATE Data2D_Column SET format='" + TimeFormats.Time + "' WHERE time_format=5 AND column_type < 6");
            statement.executeUpdate("UPDATE Data2D_Column SET format='" + TimeFormats.TimeMs + "' WHERE time_format=6 AND column_type < 6");
            statement.executeUpdate("UPDATE Data2D_Column SET format='" + TimeFormats.DatetimeMs + "' WHERE time_format=7 AND column_type < 6");
            statement.executeUpdate("UPDATE Data2D_Column SET format='" + TimeFormats.Datetime + " Z' WHERE time_format=8 AND column_type < 6");
            statement.executeUpdate("UPDATE Data2D_Column SET format='" + TimeFormats.DatetimeMs + " Z' WHERE time_format=9 AND column_type < 6");
            statement.executeUpdate("UPDATE Data2D_Column SET format='#,###' WHERE need_format=true AND column_type >= 6 AND column_type <= 10");
            statement.executeUpdate("UPDATE Data2D_Column SET format=null WHERE column_type = 1");
            statement.executeUpdate("ALTER TABLE Data2D_Column DROP COLUMN need_format");
            statement.executeUpdate("ALTER TABLE Data2D_Column DROP COLUMN time_format");
            statement.executeUpdate("ALTER TABLE Data2D_Column DROP COLUMN values_list");

            updateIn661MoveLocations(conn);
            updateIn661MoveEpidemicReports(conn);

            statement.executeUpdate("DELETE FROM VISIT_HISTORY WHERE resource_type=4 AND resource_value='Location Data'");
            statement.executeUpdate("DELETE FROM VISIT_HISTORY WHERE resource_type=4 AND resource_value='位置数据'");
            statement.executeUpdate("DELETE FROM VISIT_HISTORY WHERE resource_type=4 AND resource_value='Epidemic Report'");
            statement.executeUpdate("DELETE FROM VISIT_HISTORY WHERE resource_type=4 AND resource_value='疫情报告'");

            Platform.runLater(() -> {
                if ("zh".equals(Locale.getDefault().getLanguage().toLowerCase())) {
                    PopTools.alertInformation(null, "功能'位置数据'和'疫情报告'已被移除。\n"
                            + "它们已存在的数据可在菜单'数据 - 数据库 - 数据库表'下访问。");
                } else {
                    PopTools.alertInformation(null, "Functions 'Location Data' and 'Epidemic Report' have been removed.\n"
                            + "Their existed data can be accessed under menu 'Data - Database - Database Table'.");
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn661MoveLocations(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            DataTable locations = new DataTable();
            String tableName = message("LocationData");
            if (DerbyBase.exist(conn, tableName) == 1) {
                tableName = message("LocationData") + "_" + DateTools.nowString3();
            }
            List<Data2DColumn> columns = new ArrayList<>();
            columns.add(new Data2DColumn(message("DataSet"), ColumnType.String));
            columns.add(new Data2DColumn(message("Label"), ColumnType.String));
            columns.add(new Data2DColumn(message("Address"), ColumnType.String));
            columns.add(new Data2DColumn(message("Longitude"), ColumnType.Longitude));
            columns.add(new Data2DColumn(message("Latitude"), ColumnType.Latitude));
            columns.add(new Data2DColumn(message("Altitude"), ColumnType.Double));
            columns.add(new Data2DColumn(message("Precision"), ColumnType.Double));
            columns.add(new Data2DColumn(message("Speed"), ColumnType.Double));
            columns.add(new Data2DColumn(message("Direction"), ColumnType.Short));
            columns.add(new Data2DColumn(message("CoordinateSystem"), ColumnType.String));
            columns.add(new Data2DColumn(message("DataValue"), ColumnType.Double));
            columns.add(new Data2DColumn(message("DataSize"), ColumnType.Double));
            columns.add(new Data2DColumn(message("StartTime"), ColumnType.Era));
            columns.add(new Data2DColumn(message("EndTime"), ColumnType.Era));
            columns.add(new Data2DColumn(message("Image"), ColumnType.String));
            columns.add(new Data2DColumn(message("Comments"), ColumnType.String));
            locations = Data2DTableTools.createTable(null, conn, tableName, columns, null, null, null, false);
            TableData2D tableLocations = locations.getTableData2D();
            long count = 0;
            try (ResultSet query = statement.executeQuery("SELECT * FROM Location_Data_View");
                    PreparedStatement insert = conn.prepareStatement(tableLocations.insertStatement())) {
                conn.setAutoCommit(false);
                while (query.next()) {
                    try {
                        Data2DRow data2DRow = tableLocations.newRow();
                        data2DRow.setValue(message("DataSet"), query.getString("data_set"));
                        data2DRow.setValue(message("Label"), query.getString("label"));
                        data2DRow.setValue(message("Address"), query.getString("address"));
                        data2DRow.setValue(message("Longitude"), query.getDouble("longitude"));
                        data2DRow.setValue(message("Latitude"), query.getDouble("latitude"));
                        data2DRow.setValue(message("Altitude"), query.getDouble("altitude"));
                        data2DRow.setValue(message("Precision"), query.getDouble("precision"));
                        data2DRow.setValue(message("Speed"), query.getDouble("speed"));
                        data2DRow.setValue(message("Direction"), query.getShort("direction"));
                        data2DRow.setValue(message("CoordinateSystem"), GeoCoordinateSystem.name(query.getShort("coordinate_system")));
                        data2DRow.setValue(message("DataValue"), query.getDouble("data_value"));
                        data2DRow.setValue(message("DataSize"), query.getDouble("data_size"));
                        data2DRow.setValue(message("StartTime"), DateTools.datetimeToString(query.getLong("start_time")));
                        data2DRow.setValue(message("EndTime"), DateTools.datetimeToString(query.getLong("end_time")));
                        data2DRow.setValue(message("Image"), query.getString("dataset_image"));
                        data2DRow.setValue(message("Comments"), query.getString("location_comments"));
                        tableLocations.insertData(conn, insert, data2DRow);
                        if (++count % Database.BatchSize == 0) {
                            conn.commit();
                        }
                    } catch (Exception e) {
                        MyBoxLog.console(e);
                    }
                }
                conn.commit();
            }
            conn.setAutoCommit(true);

            statement.executeUpdate("DROP INDEX Dataset_unique_index");
            statement.executeUpdate("DROP VIEW Location_Data_View");
            statement.executeUpdate("ALTER TABLE  Location_Data DROP CONSTRAINT  Location_Data_datasetid_fk");
            statement.executeUpdate("DROP TABLE Dataset");
            statement.executeUpdate("DROP TABLE Location_Data");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn661MoveEpidemicReports(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            String tableName = message("EpidemicReport");
            if (DerbyBase.exist(conn, tableName) == 1) {
                tableName = message("EpidemicReport") + "_" + DateTools.nowString3();
            }
            List<Data2DColumn> columns = new ArrayList<>();
            columns.add(new Data2DColumn(message("DataSet"), ColumnType.String));
            columns.add(new Data2DColumn(message("Time"), ColumnType.Datetime));
            columns.add(new Data2DColumn(message("Address"), ColumnType.String));
            columns.add(new Data2DColumn(message("Longitude"), ColumnType.Longitude));
            columns.add(new Data2DColumn(message("Latitude"), ColumnType.Latitude));
            columns.add(new Data2DColumn(message("Level"), ColumnType.String));
            columns.add(new Data2DColumn(message("Continent"), ColumnType.String));
            columns.add(new Data2DColumn(message("Country"), ColumnType.String));
            columns.add(new Data2DColumn(message("Province"), ColumnType.String));
            columns.add(new Data2DColumn(message("CoordinateSystem"), ColumnType.String));
            columns.add(new Data2DColumn(message("Confirmed"), ColumnType.Long));
            columns.add(new Data2DColumn(message("Healed"), ColumnType.Long));
            columns.add(new Data2DColumn(message("Dead"), ColumnType.Long));
            columns.add(new Data2DColumn(message("IncreasedConfirmed"), ColumnType.Long));
            columns.add(new Data2DColumn(message("IncreasedHealed"), ColumnType.Long));
            columns.add(new Data2DColumn(message("IncreasedDead"), ColumnType.Long));
            columns.add(new Data2DColumn(message("Source"), ColumnType.String)); // 1:predefined 2:added 3:filled 4:statistic others:unknown
            columns.add(new Data2DColumn(message("Comments"), ColumnType.String));
            DataTable reports = Data2DTableTools.createTable(null, conn, tableName, columns, null, null, null, false);
            TableData2D tableReports = reports.getTableData2D();
            long count = 0;
            try (ResultSet query = statement.executeQuery("SELECT * FROM Epidemic_Report");
                    PreparedStatement insert = conn.prepareStatement(tableReports.insertStatement())) {
                conn.setAutoCommit(false);
                while (query.next()) {
                    try {
                        Data2DRow data2DRow = tableReports.newRow();
                        data2DRow.setValue(message("DataSet"), query.getString("data_set"));
                        data2DRow.setValue(message("Time"), query.getTimestamp("time"));
                        long locationid = query.getLong("locationid");
                        GeographyCode code = TableGeographyCode.readCode(conn, locationid, true);
                        if (code != null) {
                            try {
                                data2DRow.setValue(message("Address"), code.getName());
                                data2DRow.setValue(message("Longitude"), code.getLongitude());
                                data2DRow.setValue(message("Latitude"), code.getLatitude());
                                data2DRow.setValue(message("Level"), code.getLevelName());
                                data2DRow.setValue(message("Continent"), code.getContinentName());
                                data2DRow.setValue(message("Country"), code.getCountryName());
                                data2DRow.setValue(message("Province"), code.getProvinceName());
                                data2DRow.setValue(message("CoordinateSystem"), code.getCoordinateSystem().name());
                                data2DRow.setValue(message("Comments"), code.getFullName());
                            } catch (Exception e) {
                                MyBoxLog.console(e);
                            }
                        }
                        data2DRow.setValue(message("Confirmed"), query.getLong("confirmed"));
                        data2DRow.setValue(message("Healed"), query.getLong("healed"));
                        data2DRow.setValue(message("Dead"), query.getLong("dead"));
                        data2DRow.setValue(message("IncreasedConfirmed"), query.getLong("increased_confirmed"));
                        data2DRow.setValue(message("IncreasedHealed"), query.getLong("increased_healed"));
                        data2DRow.setValue(message("IncreasedDead"), query.getLong("increased_dead"));
                        short sd = query.getShort("source");
                        String source;
                        source = switch (sd) {
                            case 1 ->
                                message("PredefinedData");
                            case 2 ->
                                message("InputtedData");
                            case 3 ->
                                message("FilledData");
                            case 4 ->
                                message("StatisticData");
                            default ->
                                message("Unknown");
                        };
                        data2DRow.setValue(message("Source"), source);

                        tableReports.insertData(conn, insert, data2DRow);
                        if (++count % Database.BatchSize == 0) {
                            conn.commit();
                        }
                    } catch (Exception e) {
                        MyBoxLog.console(e);
                    }
                }
                conn.commit();
            }
            conn.setAutoCommit(true);

            statement.executeUpdate("DROP INDEX Epidemic_Report_DatasetTimeDesc_index");
            statement.executeUpdate("DROP INDEX Epidemic_Report_DatasetTimeAsc_index");
            statement.executeUpdate("DROP INDEX Epidemic_Report_timeAsc_index");
            statement.executeUpdate("DROP VIEW Epidemic_Report_Statistic_View");
            statement.executeUpdate("ALTER TABLE  Epidemic_Report DROP CONSTRAINT  Epidemic_Report_locationid_fk");
            statement.executeUpdate("DROP TABLE Epidemic_Report");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn660(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            MyBoxLog.info("Updating tables in 6.6...");

            conn.setAutoCommit(true);
            statement.executeUpdate("ALTER TABLE Data2D_Column ADD COLUMN description VARCHAR(" + StringMaxLength + ")");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn659(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            MyBoxLog.info("Updating tables in 6.5.9...");

            conn.setAutoCommit(true);
            statement.executeUpdate("ALTER TABLE Data2D_Column ADD COLUMN need_format Boolean");

            // Users' data are discarded. Sorry!
            statement.executeUpdate("DROP TABLE Alarm_Clock");
            new TableAlarmClock().createTable(conn);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn658(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            MyBoxLog.info("Updating tables in 6.5.8...");

            conn.setAutoCommit(true);
            statement.executeUpdate("ALTER TABLE Data2D_Style ADD COLUMN filter VARCHAR(" + StringMaxLength + ")");
            statement.executeUpdate("ALTER TABLE Data2D_Style ADD COLUMN filterReversed Boolean");

            TableData2DStyle tableData2DStyle = new TableData2DStyle();
            ResultSet query = statement.executeQuery("SELECT * FROM Data2D_Style ORDER BY d2id,sequence,d2sid");
            while (query.next()) {
                String rowFilter = query.getString("rowFilter");
                Data2DStyle style = tableData2DStyle.readData(query);
                if (rowFilter != null && !rowFilter.isBlank()) {
                    if (rowFilter.startsWith("Reversed::")) {
                        style.setFilter(rowFilter.substring("Reversed::".length()));
                        style.setMatchFalse(true);
                    } else {
                        style.setFilter(rowFilter);
                        style.setMatchFalse(false);
                    }
                }
                // discard data about column filter. Sorry!
                tableData2DStyle.updateData(conn, style);
            }
            statement.executeUpdate("ALTER TABLE Data2D_Style DROP COLUMN rowFilter");
            statement.executeUpdate("ALTER TABLE Data2D_Style DROP COLUMN columnFilter");

            FxFileTools.getInternalFile("/js/tianditu.html", "js", "tianditu.html", true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn657(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            MyBoxLog.info("Updating tables in 6.5.7...");

            conn.setAutoCommit(true);
            statement.executeUpdate("ALTER TABLE Data2D_Style ADD COLUMN rowFilter VARCHAR(" + StringMaxLength + ")");
            statement.executeUpdate("UPDATE Data2D_Style SET rowFilter=moreConditions");
            statement.executeUpdate("ALTER TABLE Data2D_Style DROP COLUMN moreConditions");
            statement.executeUpdate("ALTER TABLE Data2D_Style ADD COLUMN columnFilter VARCHAR(" + StringMaxLength + ")");
            statement.executeUpdate("ALTER TABLE Data2D_Style ADD COLUMN abnoramlValues Boolean");
            statement.executeUpdate("ALTER TABLE Data2D_Style ADD COLUMN title VARCHAR(" + StringMaxLength + ")");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn656(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            MyBoxLog.info("Updating tables in 6.5.6...");

            conn.setAutoCommit(true);
            statement.executeUpdate("DROP INDEX Data2D_Style_unique_index");
            statement.executeUpdate("ALTER TABLE Data2D_Style ADD COLUMN rowStart BigInt");
            statement.executeUpdate("ALTER TABLE Data2D_Style ADD COLUMN rowEnd BigInt");
            statement.executeUpdate("ALTER TABLE Data2D_Style ADD COLUMN columns VARCHAR(" + StringMaxLength + ")");
            statement.executeUpdate("ALTER TABLE Data2D_Style ADD COLUMN moreConditions VARCHAR(" + StringMaxLength + ")");
            statement.executeUpdate("ALTER TABLE Data2D_Style ADD COLUMN fontColor VARCHAR(64)");
            statement.executeUpdate("ALTER TABLE Data2D_Style ADD COLUMN fontSize VARCHAR(64)");
            statement.executeUpdate("ALTER TABLE Data2D_Style ADD COLUMN bgColor VARCHAR(64)");
            statement.executeUpdate("ALTER TABLE Data2D_Style ADD COLUMN bold Boolean");
            statement.executeUpdate("ALTER TABLE Data2D_Style ADD COLUMN moreStyle VARCHAR(" + StringMaxLength + ")");
            statement.executeUpdate("ALTER TABLE Data2D_Style ADD COLUMN sequence int");

            conn.setAutoCommit(false);
            TableData2DStyle tableData2DStyle = new TableData2DStyle();
            ResultSet query = statement.executeQuery("SELECT * FROM Data2D_Style ORDER BY d2id,colName,row");
            long lastD2id = -1, lastRow = -2, rowStart = -1, sequence = 1;
            String lastColName = null, lastStyle = null;
            Data2DStyle data2DStyle = Data2DStyle.create()
                    .setRowStart(rowStart).setRowEnd(rowStart);
            while (query.next()) {
                long d2id = query.getLong("d2id");
                String colName = query.getString("colName");
                String style = query.getString("style");
                long row = query.getLong("row");
                if (lastD2id != d2id || !colName.equals(lastColName) || !style.equals(lastStyle)) {
                    if (data2DStyle.getRowStart() >= 0) {
                        if (lastD2id != d2id) {
                            sequence = 1;
                        }
                        data2DStyle.setD2sid(-1).setRowEnd(lastRow + 1).setSequence(sequence++);
                        tableData2DStyle.insertData(conn, data2DStyle);
                        conn.commit();
                    }
                    rowStart = row;
                    data2DStyle.setD2id(d2id).setColumns(colName).setMoreStyle(style)
                            .setRowStart(rowStart).setRowEnd(rowStart + 1);
                } else if (row > lastRow + 1) {
                    data2DStyle.setD2sid(-1).setRowEnd(lastRow + 1).setSequence(sequence++);
                    tableData2DStyle.insertData(conn, data2DStyle);
                    conn.commit();
                    rowStart = row;
                    data2DStyle.setD2id(d2id).setColumns(colName).setMoreStyle(style)
                            .setRowStart(rowStart).setRowEnd(rowStart + 1);
                }
                lastD2id = d2id;
                lastRow = row;
                lastColName = colName;
                lastStyle = style;
            }
            if (data2DStyle.getRowStart() >= 0) {
                data2DStyle.setD2sid(-1).setRowEnd(lastRow + 1).setSequence(sequence++);
                tableData2DStyle.insertData(conn, data2DStyle);
                conn.commit();
            }

            conn.setAutoCommit(true);
            statement.executeUpdate("ALTER TABLE Data2D_Style DROP COLUMN row");
            statement.executeUpdate("ALTER TABLE Data2D_Style DROP COLUMN colName");
            statement.executeUpdate("ALTER TABLE Data2D_Style DROP COLUMN style");
            statement.executeUpdate("DELETE FROM Data2D_Style WHERE columns IS NULL");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn655(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            MyBoxLog.info("Updating tables in 6.5.5...");

            ImageAttributes attributes = new ImageAttributes()
                    .setImageFormat("png").setColorSpaceName("sRGB")
                    .setAlpha(ImageAttributes.Alpha.Keep).setQuality(100);
            File iconPath = new File(AppPaths.getIconsPath());
            for (File s : iconPath.listFiles()) {
                String name = s.getAbsolutePath();
                if (s.isFile() && name.endsWith(".ico")) {
                    File t = new File(name.substring(0, name.lastIndexOf(".")) + ".png");
                    ImageConvertTools.convertColorSpace(null, s, attributes, t);
                    if (t.exists()) {
                        FileDeleteTools.delete(s);
                    }
                }
            }

            conn.setAutoCommit(false);
//            TableTreeNode tableTreeNode = new TableTreeNode();
//            ResultSet query = statement.executeQuery("SELECT * FROM Tree_Node "
//                    + "WHERE category='" + InfoNode.WebFavorite + "' AND more is not null");
//            while (query.next()) {
//                InfoNode node = tableTreeNode.readData(query);
//                String icon = node.getMore();
//                if (icon != null && icon.endsWith(".ico")) {
//                    icon = icon.replace(".ico", ".png");
//                    node.setMore(icon);
//                    tableTreeNode.updateData(conn, node);
//                }
//            }
//            conn.commit();

            ResultSet query = statement.executeQuery("SELECT * FROM Web_History "
                    + "WHERE icon is not null");
            TableWebHistory tableWebHistory = new TableWebHistory();
            while (query.next()) {
                WebHistory his = tableWebHistory.readData(query);
                String icon = his.getIcon();
                if (icon != null && icon.endsWith(".ico")) {
                    icon = icon.substring(0, icon.lastIndexOf(".")) + ".png";
                    his.setIcon(icon);
                    tableWebHistory.updateData(conn, his);
                }
            }
            conn.commit();

            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn654(Connection conn) {
        try (Statement statement = conn.createStatement()) {
            MyBoxLog.info("Updating tables in 6.5.4...");

            conn.setAutoCommit(true);
            statement.executeUpdate("ALTER TABLE Tag ADD COLUMN category VARCHAR(" + StringMaxLength + ") NOT NULL DEFAULT 'Root'");
            statement.executeUpdate("ALTER TABLE Tag ADD COLUMN color VARCHAR(" + StringMaxLength + ")");
            statement.executeUpdate("DROP INDEX Tag_unique_index");
            statement.executeUpdate("CREATE UNIQUE INDEX Tag_unique_index on Tag (  category, tag )");
            statement.executeUpdate("UPDATE Tag SET category='" + InfoNode.Notebook + "'");

            TableInfoNode tableTreeNode = new TableInfoNode();
            tableTreeNode.checkBase(conn);
            statement.executeUpdate("ALTER TABLE tree_node ADD COLUMN oldNodeid BIGINT");
            statement.executeUpdate("ALTER TABLE tree_node ADD COLUMN oldParentid BIGINT");
            statement.executeUpdate("INSERT INTO tree_node (category, title, value, oldNodeid, oldParentid) "
                    + "SELECT '" + InfoNode.WebFavorite + "', title, attribute, nodeid, parent FROM tree WHERE nodeid > 1");
            statement.executeUpdate("INSERT INTO tree_node ( category, title, value, more, oldParentid) "
                    + "SELECT '" + InfoNode.WebFavorite + "', title, address, icon, owner FROM Web_Favorite");
            statement.executeUpdate("Update tree_node AS A set parentid="
                    + "(select B.nodeid from tree_node AS B WHERE A.oldParentid=B.oldNodeid AND B.category='" + InfoNode.WebFavorite + "')  "
                    + "WHERE A.category='" + InfoNode.WebFavorite + "'");

            statement.executeUpdate("INSERT INTO tree_node (category, title, value, oldNodeid, oldParentid)  "
                    + "SELECT '" + InfoNode.Notebook + "', name , description, nbid, owner FROM notebook");
            statement.executeUpdate("Update tree_node AS A set parentid="
                    + "(select B.nodeid from tree_node AS B WHERE A.oldParentid=B.oldNodeid AND B.category='"
                    + InfoNode.Notebook + "' AND A.category='" + InfoNode.Notebook + "') "
                    + " WHERE A.category='" + InfoNode.Notebook + "'");
            statement.executeUpdate("INSERT INTO tree_node (category, title, value, update_time, oldNodeid, oldParentid) "
                    + "SELECT 'Note', title, html, update_time, ntid, notebook FROM note");
            statement.executeUpdate("Update tree_node AS A set parentid="
                    + "(select B.nodeid from tree_node AS B WHERE A.oldParentid=B.oldNodeid AND B.category='Notebook' AND A.category='Note')  "
                    + "WHERE A.category='Note'");
            statement.executeUpdate("INSERT INTO tree_node_tag (tnodeid, tagid)  "
                    + "SELECT tree_node.nodeid, note_tag.tagid FROM tree_node, note_tag "
                    + "where tree_node.oldNodeid=note_tag.noteid AND tree_node.category='Note'");
            statement.executeUpdate("Update tree_node set category='" + InfoNode.Notebook + "' WHERE category='Note'");

            statement.executeUpdate("ALTER TABLE tree_node DROP COLUMN oldNodeid");
            statement.executeUpdate("ALTER TABLE tree_node DROP COLUMN oldParentid");
            statement.executeUpdate("DROP TABLE Web_Favorite");
            statement.executeUpdate("DROP TABLE Note_tag");
            statement.executeUpdate("DROP TABLE Note");
            statement.executeUpdate("DROP TABLE Notebook");
            statement.executeUpdate("DROP TABLE tree");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn653(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.5.3...");

            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("ALTER TABLE Data2D_Column DROP COLUMN is_id");
                statement.executeUpdate("ALTER TABLE Data2D_Column ADD COLUMN is_auto BOOLEAN");
                conn.commit();
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn652(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.5.2...");

            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("ALTER TABLE User_Conf DROP COLUMN default_int_Value");
                statement.executeUpdate("ALTER TABLE User_Conf DROP COLUMN default_string_Value");
                statement.executeUpdate("ALTER TABLE System_Conf DROP COLUMN default_int_Value");
                statement.executeUpdate("ALTER TABLE System_Conf DROP COLUMN default_string_Value");
                statement.executeUpdate("ALTER TABLE Data2D_Column ADD COLUMN color  VARCHAR(16)");
                conn.commit();
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn651(Connection conn) {
        try {
            MyBoxLog.info("Updating tables in 6.5.1...");

            updateIn651ExtendColumns(conn);

            updateIn651ReplaceDataDefinition(conn);

            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn651ExtendColumns(Connection conn) {
        try {
            conn.setAutoCommit(true);

            alterColumnLength(conn, "User_Conf", "key_Name", StringMaxLength);
            alterColumnLength(conn, "System_Conf", "key_Name", StringMaxLength);
            alterColumnLength(conn, "String_Values", "key_name", StringMaxLength);
            alterColumnLength(conn, "String_Value", "key_name", StringMaxLength);
            alterColumnLength(conn, "Query_Condition", "data_name", StringMaxLength);
            alterColumnLength(conn, "visit_history", "resource_value", StringMaxLength);
            alterColumnLength(conn, "visit_history", "data_more", StringMaxLength);
            alterColumnLength(conn, "Web_History", "address", StringMaxLength);
            alterColumnLength(conn, "Web_History", "title", StringMaxLength);
            alterColumnLength(conn, "Web_History", "icon", StringMaxLength);
            alterColumnLength(conn, "Web_Favorite", "title", StringMaxLength);
            alterColumnLength(conn, "Web_Favorite", "address", StringMaxLength);
            alterColumnLength(conn, "Web_Favorite", "icon", StringMaxLength);
            alterColumnLength(conn, "Tree", "title", StringMaxLength);
            alterColumnLength(conn, "Tree", "attribute", StringMaxLength);
            alterColumnLength(conn, "Tag", "tag", StringMaxLength);
            alterColumnLength(conn, "Notebook", "name", StringMaxLength);
            alterColumnLength(conn, "Notebook", "description", StringMaxLength);
            alterColumnLength(conn, "Note", "title", StringMaxLength);
            alterColumnLength(conn, "MyBox_Log", "log", StringMaxLength);
            alterColumnLength(conn, "MyBox_Log", "file_name", StringMaxLength);
            alterColumnLength(conn, "MyBox_Log", "class_name", StringMaxLength);
            alterColumnLength(conn, "MyBox_Log", "method_name", StringMaxLength);
            alterColumnLength(conn, "MyBox_Log", "callers", StringMaxLength);
            alterColumnLength(conn, "MyBox_Log", "comments", StringMaxLength);
            alterColumnLength(conn, "image_scope", "outline", StringMaxLength);
            alterColumnLength(conn, "Image_Edit_History", "scope_name", StringMaxLength);
            alterColumnLength(conn, "Geography_Code", "chinese_name", StringMaxLength);
            alterColumnLength(conn, "Geography_Code", "english_name", StringMaxLength);
            alterColumnLength(conn, "Geography_Code", "code1", 1024);
            alterColumnLength(conn, "Geography_Code", "code2", 1024);
            alterColumnLength(conn, "Geography_Code", "code3", 1024);
            alterColumnLength(conn, "Geography_Code", "code4", 1024);
            alterColumnLength(conn, "Geography_Code", "code5", 1024);
            alterColumnLength(conn, "Geography_Code", "alias1", StringMaxLength);
            alterColumnLength(conn, "Geography_Code", "alias2", StringMaxLength);
            alterColumnLength(conn, "Geography_Code", "alias3", StringMaxLength);
            alterColumnLength(conn, "Geography_Code", "alias4", StringMaxLength);
            alterColumnLength(conn, "Geography_Code", "alias5", StringMaxLength);
            alterColumnLength(conn, "Float_Matrix", "name", StringMaxLength);
            alterColumnLength(conn, "Convolution_Kernel", "name", StringMaxLength);
            alterColumnLength(conn, "Convolution_Kernel", "description", StringMaxLength);
            alterColumnLength(conn, "Color_Palette_Name", "palette_name", StringMaxLength);
            alterColumnLength(conn, "Color_Palette", "name_in_palette", StringMaxLength);
            alterColumnLength(conn, "Color", "color_name", StringMaxLength);
            alterColumnLength(conn, "Alarm_Clock", "description", StringMaxLength);
            alterColumnLength(conn, "Alarm_Clock", "sound", StringMaxLength);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void updateIn651ReplaceDataDefinition(Connection conn) {
        try {
            conn.setAutoCommit(false);
            TableData2DDefinition tableData2DDefinition = new TableData2DDefinition();
            TableData2DColumn tableData2DColumn = new TableData2DColumn();
            try (ResultSet dquery = conn.createStatement().executeQuery("SELECT * FROM Data_Definition");) {
                while (dquery.next()) {
                    long dfid = dquery.getLong("dfid");
                    short type = dquery.getShort("data_type");
                    String name = dquery.getString("data_name");
                    if (name == null || name.isBlank()) {
                        continue;
                    }
                    File file = null;
                    if (type == 2) {               // Matrix
                        type = 4;
                    } else if (type == 1) {       // DataFile
                        file = new File(name);
                        if (file.exists()) {
                            if (name.endsWith(".csv")) {
                                type = 1;
                            } else {
                                type = 0;    // texts
                            }
                            name = file.getName();
                        } else {
                            continue;
                        }
                    } else if (type == 4) {          // DataClipboard
                        file = new File(name);
                        if (!file.exists()) {
                            continue;
                        }
                        name = file.getName();
                        type = 3;
                    } else {
                        continue;
                    }
                    String charset = dquery.getString("charset");
                    String delimiter = dquery.getString("delimiter");
                    boolean has_header = dquery.getBoolean("has_header");
                    Data2DDefinition def = Data2DDefinition.create()
                            .setType(Data2DDefinition.type(type))
                            .setFile(file).setDataName(name)
                            .setHasHeader(has_header).setDelimiter(delimiter)
                            .setCharsetName(charset);
                    def = tableData2DDefinition.insertData(conn, def);
                    conn.commit();

                    long d2did = def.getD2did();
                    ResultSet cquery = conn.createStatement().executeQuery("SELECT * FROM Data_Column WHERE dataid=" + dfid);
                    while (cquery.next()) {
                        Data2DColumn column = Data2DColumn.create().setD2id(d2did);
                        column.setType(ColumnDefinition.columnTypeFromValue(cquery.getShort("column_type")));
                        column.setColumnName(cquery.getString("column_name"));
                        column.setIndex(cquery.getInt("index"));
                        column.setLength(cquery.getInt("length"));
                        column.setWidth(cquery.getInt("width"));
                        tableData2DColumn.insertData(conn, column);
                    }
                    conn.commit();
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }

            try (ResultSet mquery = conn.createStatement().executeQuery("SELECT * FROM Matrix")) {
                conn.setAutoCommit(false);
                TableData2DCell tableData2DCell = new TableData2DCell();
                while (mquery.next()) {
                    long mxid = mquery.getLong("mxid");
                    Data2DDefinition def = Data2DDefinition.create()
                            .setType(Data2DDefinition.DataType.Matrix)
                            .setDataName(mquery.getString("name"))
                            .setScale(mquery.getShort("scale"))
                            .setColsNumber(mquery.getInt("columns_number"))
                            .setRowsNumber(mquery.getInt("rows_number"))
                            .setModifyTime(mquery.getTimestamp("modify_time"))
                            .setComments(mquery.getString("comments"));
                    def = tableData2DDefinition.insertData(conn, def);
                    conn.commit();
                    long d2did = def.getD2did();
                    try (ResultSet cquery = conn.createStatement()
                            .executeQuery("SELECT * FROM Matrix_Cell WHERE mcxid=" + mxid)) {
                        while (cquery.next()) {
                            Data2DCell cell = Data2DCell.create().setD2did(d2did)
                                    .setCol(cquery.getInt("col"))
                                    .setRow(cquery.getInt("row"))
                                    .setValue(cquery.getDouble("value") + "");
                            tableData2DCell.insertData(conn, cell);
                        }
                    } catch (Exception e) {
                        MyBoxLog.debug(e);
                    }
                    conn.commit();
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            conn.commit();

            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("DROP TABLE Matrix_Cell");
                statement.executeUpdate("DROP TABLE Matrix");
                statement.executeUpdate("DROP VIEW Data_Column_View");
                statement.executeUpdate("DROP TABLE Data_Column");
                statement.executeUpdate("DROP TABLE Data_Definition");
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
