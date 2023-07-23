package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.data.GeoCoordinateSystem;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.DerbyBase.stringValue;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeLevel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-2-3
 * @License Apache License Version 2.0
 */
public class TableGeographyCode extends BaseTable<GeographyCode> {

    /**
     * One of following can determine an address: 1. gcid. This is accurate
     * matching. 2. level + chinese_name/english_name/alias + ancestors. This is
     * accurate matching. 3. level + chinese_name/english_name/alias. This is
     * fuzzy matching. Duplaited names can happen.
     *
     * Notice: coordinate can not determine an address
     *
     * Extra information, like higher level attribuets, can avoid wrong matching
     * due to duplicated names. Example, 2 cities have same name "A" while they
     * have different country names, then distinct them by appending country
     * name.
     *
     * Should not run batch insert/update of external data because each new data
     * need compare previous data. External data may include unexpected data
     * inconsistent.
     */
    public TableGeographyCode() {
        tableName = "Geography_Code";
        defineColumns();
    }

    public TableGeographyCode(boolean defineColumns) {
        tableName = "Geography_Code";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableGeographyCode defineColumns() {
        addColumn(new ColumnDefinition("gcid", ColumnType.Long, true, true).setAuto(true).setMinValue((long) 0));
        addColumn(new ColumnDefinition("level", ColumnType.Short, true).setMaxValue((short) 10).setMinValue((short) 1));
        addColumn(new ColumnDefinition("coordinate_system", ColumnType.Short));
        addColumn(new ColumnDefinition("longitude", ColumnType.Double, true).setMaxValue((double) 180).setMinValue((double) -180));
        addColumn(new ColumnDefinition("latitude", ColumnType.Double, true).setMaxValue((double) 90).setMinValue((double) -90));
        addColumn(new ColumnDefinition("altitude", ColumnType.Double));
        addColumn(new ColumnDefinition("precision", ColumnType.Double));
        addColumn(new ColumnDefinition("chinese_name", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("english_name", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("continent", ColumnType.Long).setMinValue((long) 0));
        addColumn(new ColumnDefinition("country", ColumnType.Long).setMinValue((long) 0));
        addColumn(new ColumnDefinition("province", ColumnType.Long).setMinValue((long) 0));
        addColumn(new ColumnDefinition("city", ColumnType.Long).setMinValue((long) 0));
        addColumn(new ColumnDefinition("county", ColumnType.Long).setMinValue((long) 0));
        addColumn(new ColumnDefinition("town", ColumnType.Long).setMinValue((long) 0));
        addColumn(new ColumnDefinition("village", ColumnType.Long).setMinValue((long) 0));
        addColumn(new ColumnDefinition("building", ColumnType.Long).setMinValue((long) 0));
        addColumn(new ColumnDefinition("code1", ColumnType.String).setLength(1024));
        addColumn(new ColumnDefinition("code2", ColumnType.String).setLength(1024));
        addColumn(new ColumnDefinition("code3", ColumnType.String).setLength(1024));
        addColumn(new ColumnDefinition("code4", ColumnType.String).setLength(1024));
        addColumn(new ColumnDefinition("code5", ColumnType.String).setLength(1024));
        addColumn(new ColumnDefinition("alias1", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("alias2", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("alias3", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("alias4", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("alias5", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("area", ColumnType.Long).setMinValue((long) 0));
        addColumn(new ColumnDefinition("population", ColumnType.Long).setMinValue((long) 0));
        addColumn(new ColumnDefinition("owner", ColumnType.Long).setMinValue((long) 0)
                .setReferName("Geography_Code_owner_fk").setReferTable("Geography_Code").setReferColumn("gcid"));
        addColumn(new ColumnDefinition("comments", ColumnType.String).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("gcsource", ColumnType.Short).setMinValue((short) 0));
        return this;
    }

    public static long MaxID = -1;

    public static final String Create_Index_levelIndex
            = " CREATE INDEX  Geography_Code_level_index on Geography_Code ( "
            + "  level, continent, country ,province ,city ,county ,town , village , building "
            + " )";

    public static final String Create_Index_codeIndex
            = " CREATE INDEX  Geography_Code_code_index on Geography_Code ( "
            + "  level, country ,province ,city ,county ,town , village , building "
            + " )";

    public static final String Create_Index_gcidIndex
            = " CREATE INDEX  Geography_Code_gcid_index on Geography_Code ( "
            + "  gcid DESC, level, continent, country ,province ,city ,county ,town , village , building "
            + " )";

    public static final String AllQeury
            = "SELECT * FROM Geography_Code ORDER BY gcid";

    public static final String PageQeury
            = "SELECT * FROM Geography_Code ORDER BY gcid "
            + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String GCidQeury
            = "SELECT * FROM Geography_Code WHERE gcid=?";

    public static final String NameEqual
            = "( chinese_name IS NOT NULL AND LCASE(chinese_name)=? ) OR "
            + " ( english_name IS NOT NULL AND LCASE(english_name)=? ) OR "
            + " ( alias1 IS NOT NULL AND LCASE(alias1)=? ) OR "
            + " ( alias2 IS NOT NULL AND LCASE(alias2)=? ) OR "
            + " ( alias3 IS NOT NULL AND LCASE(alias3)=? ) OR "
            + " ( alias4 IS NOT NULL AND LCASE(alias4)=? ) OR "
            + " ( alias5 IS NOT NULL AND LCASE(alias5)=? )";

    public static final String LevelNameEqual
            = "level=? AND  ( " + NameEqual + " )";

    public static final String LevelNameQeury
            = "SELECT * FROM Geography_Code WHERE " + LevelNameEqual;

    public static final String CoordinateQeury
            = "SELECT * FROM Geography_Code WHERE coordinate_system=? AND longitude=? AND latitude=?";

    public static final String MaxLevelGCidQuery
            = "SELECT max(gcid) FROM Geography_Code WHERE level=?";

    public static final String GCidExistedQuery
            = "SELECT gcid FROM Geography_Code where gcid=?";

    public static final String MaxGCidQuery
            = "SELECT max(gcid) FROM Geography_Code";

    public static final String FirstChildQuery
            = "SELECT gcid FROM Geography_Code WHERE "
            + " owner=? OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY";

    public static final String ChildrenQuery
            = "SELECT * FROM Geography_Code WHERE owner=? ORDER BY gcid";

    public static final String LevelSizeQuery
            = "SELECT count(gcid) FROM Geography_Code WHERE level=?";

    public static final String Update
            = "UPDATE Geography_Code SET "
            + " level=?, longitude=?, latitude=?, gcsource=?, chinese_name=?, english_name=?, "
            + " code1=?, code2=?, code3=?, code4=?, code5=?,alias1=?, alias2=?, alias3=?, alias4=?, alias5=?, "
            + " area=?, population=?, comments=?, "
            + " continent=?, country=?, province=? , city=?, county=?, town=?, village=? , building=?, "
            + " owner=?, altitude=? , precision=?, coordinate_system=? "
            + " WHERE gcid=?";

    public static final String Insert
            = "INSERT INTO Geography_Code( "
            + " gcid, level, gcsource, longitude, latitude, chinese_name, english_name,"
            + " code1, code2, code3,  code4, code5, alias1, alias2, alias3, alias4, alias5, "
            + " area, population, comments,"
            + " continent, country, province, city, county, town, village, building,"
            + " owner, altitude, precision,coordinate_system  ) "
            + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? ) ";

    public static final String Delete
            = "DELETE FROM Geography_Code WHERE gcid=?";

    private static long generateID(Connection conn) {
        try ( Statement statement = conn.createStatement()) {
            try ( ResultSet results = statement.executeQuery("SELECT max(gcid) FROM Geography_Code")) {
                if (results.next()) {
                    MaxID = results.getInt(1);
                }
            }
            long gcid = Math.max(2000000, MaxID + 1);
            MaxID = gcid;
            String sql = "ALTER TABLE Geography_Code ALTER COLUMN gcid RESTART WITH " + (MaxID + 1);
            statement.executeUpdate(sql);
            return gcid;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public static boolean validCoordinate(double longitude, double latitude) {
        return longitude >= -180 && longitude <= 180
                && latitude >= -90 && latitude <= 90;
    }

    public static boolean validCoordinate(GeographyCode code) {
        return code.getLongitude() >= -180 && code.getLongitude() <= 180
                && code.getLatitude() >= -90 && code.getLatitude() <= 90;
    }

    public static void setNameParameters(PreparedStatement statement, String name, int fromIndex) {
        if (statement == null || name == null || fromIndex < 0) {
            return;
        }
        try {
            String lname = name.toLowerCase();
            statement.setString(fromIndex + 1, lname);
            statement.setString(fromIndex + 2, lname);
            statement.setString(fromIndex + 3, lname);
            statement.setString(fromIndex + 4, lname);
            statement.setString(fromIndex + 5, lname);
            statement.setString(fromIndex + 6, lname);
            statement.setString(fromIndex + 7, lname);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static String nameEqual(String value) {
        String v = stringValue(value).toLowerCase();
        return "  ( chinese_name IS NOT NULL AND LCASE(chinese_name)='" + v + "' ) OR "
                + " ( english_name IS NOT NULL AND LCASE(english_name)='" + v + "' ) OR "
                + " ( alias1 IS NOT NULL AND LCASE(alias1)='" + v + "' ) OR "
                + " ( alias2 IS NOT NULL AND LCASE(alias2)='" + v + "' ) OR "
                + " ( alias3 IS NOT NULL AND LCASE(alias3)='" + v + "' ) OR "
                + " ( alias4 IS NOT NULL AND LCASE(alias4)='" + v + "' ) OR "
                + " ( alias5 IS NOT NULL AND LCASE(alias5)='" + v + "' ) ";
    }

    public static String codeEqual(GeographyCode code) {
        if (code.getGcid() > 0) {
            return "gcid=" + code.getGcid();
        }
        int level = code.getLevel();
        String s = "level=" + level;
        switch (level) {
            case 3:
                break;
            case 4:
                s += " AND country=" + code.getCountry();
                break;
            case 5:
                s += " AND country=" + code.getCountry()
                        + " AND province=" + code.getProvince();
                break;
            case 6:
                s += " AND country=" + code.getCountry()
                        + " AND province=" + code.getProvince()
                        + " AND city=" + code.getCity();
                break;
            case 7:
                s += " AND country=" + code.getCountry()
                        + " AND province=" + code.getProvince()
                        + " AND city=" + code.getCity()
                        + " AND county=" + code.getCounty();
                break;
            case 8:
                s += " AND country=" + code.getCountry()
                        + " AND province=" + code.getProvince()
                        + " AND city=" + code.getCity()
                        + " AND county=" + code.getCounty()
                        + " AND town=" + code.getTown();
                break;
            case 9:
                s += " AND country=" + code.getCountry()
                        + " AND province=" + code.getProvince()
                        + " AND city=" + code.getCity()
                        + " AND county=" + code.getCounty()
                        + " AND town=" + code.getTown()
                        + " AND village=" + code.getVillage();
                break;
            case 10:
            default:
                s += " AND country=" + code.getCountry()
                        + " AND province=" + code.getProvince()
                        + " AND city=" + code.getCity()
                        + " AND county=" + code.getCounty()
                        + " AND town=" + code.getTown()
                        + " AND village=" + code.getVillage()
                        + " AND building=" + code.getBuilding();
                break;
        }

        if (code.getChineseName() != null) {
            String name = stringValue(code.getChineseName()).toLowerCase();
            s += " AND  ( ( chinese_name IS NOT NULL AND LCASE(chinese_name)='" + name + "' ) OR "
                    + " ( alias1 IS NOT NULL AND LCASE(alias1)='" + name + "' ) OR "
                    + " ( alias2 IS NOT NULL AND LCASE(alias2)='" + name + "' ) OR "
                    + " ( alias3 IS NOT NULL AND LCASE(alias3)='" + name + "' ) OR "
                    + " ( alias4 IS NOT NULL AND LCASE(alias4)='" + name + "' ) OR "
                    + " ( alias5 IS NOT NULL AND LCASE(alias5)='" + name + "' ) ) ";

        } else if (code.getEnglishName() != null) {
            String name = stringValue(code.getEnglishName()).toLowerCase();
            s += " AND ( ( english_name IS NOT NULL AND LCASE(english_name)='" + name + "' ) OR "
                    + " ( alias1 IS NOT NULL AND LCASE(alias1)='" + name + "' ) OR "
                    + " ( alias2 IS NOT NULL AND LCASE(alias2)='" + name + "' ) OR "
                    + " ( alias3 IS NOT NULL AND LCASE(alias3)='" + name + "' ) OR "
                    + " ( alias4 IS NOT NULL AND LCASE(alias4)='" + name + "' ) OR "
                    + " ( alias5 IS NOT NULL AND LCASE(alias5)='" + name + "' ) ) ";
        }
        return s;
    }

    public static String codeEqual(String value) {
        String v = stringValue(value).toLowerCase();
        return " ( ( code1 IS NOT NULL AND LCASE(code1)='" + v + "' ) OR "
                + " ( code2 IS NOT NULL AND LCASE(code2)='" + v + "' ) OR "
                + " ( code3 IS NOT NULL AND LCASE(code3)='" + v + "' ) OR "
                + " ( code4 IS NOT NULL AND LCASE(code4)='" + v + "' ) OR "
                + " ( code5 IS NOT NULL AND LCASE(code5)='" + v + "' ) )  ";
    }

    public static GeographyCode earth() {
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return earth(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static GeographyCode earth(Connection conn) {
        String sql = "SELECT * FROM Geography_Code WHERE level=1 AND chinese_name='地球'";
        return queryCode(conn, sql, false);
    }

    public static GeographyCode China() {
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return China(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static GeographyCode China(Connection conn) {
        String sql = "SELECT * FROM Geography_Code WHERE level=3 AND chinese_name='中国'";
        return queryCode(conn, sql, false);
    }

    public static GeographyCode queryCode(String sql, boolean decodeAncestors) {
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return queryCode(conn, sql, decodeAncestors);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static GeographyCode queryCode(Connection conn, String sql, boolean decodeAncestors) {
        if (conn == null || sql == null) {
            return null;
        }
        try {
            GeographyCode code;
            try ( Statement statement = conn.createStatement()) {
                statement.setMaxRows(1);
                try ( ResultSet results = statement.executeQuery(sql)) {
                    if (results.next()) {
                        code = readResults(results);
                    } else {
                        return null;
                    }
                }
            }
            if (decodeAncestors && code != null) {
                decodeAncestors(conn, code);
            }
            return code;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    // this way is not accurate since multiple addresses can have same coordinate
    public static GeographyCode readCode(GeoCoordinateSystem coordinateSystem,
            double longitude, double latitude, boolean decodeAncestors) {
        if (coordinateSystem == null || !validCoordinate(longitude, latitude)) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return readCode(conn, coordinateSystem, longitude, latitude, decodeAncestors);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static GeographyCode readCode(Connection conn,
            GeoCoordinateSystem coordinateSystem,
            double longitude, double latitude, boolean decodeAncestors) {
        if (coordinateSystem == null || !validCoordinate(longitude, latitude)) {
            return null;
        }
        try {
            GeographyCode code;
            try ( PreparedStatement statement = conn.prepareStatement(CoordinateQeury)) {
                statement.setShort(1, coordinateSystem.shortValue());
                statement.setDouble(2, longitude);
                statement.setDouble(3, latitude);
                code = readCode(conn, statement, decodeAncestors);
            }
            return code;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static GeographyCode readCode(long gcid, boolean decodeAncestors) {
        if (gcid <= 0) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return readCode(conn, gcid, decodeAncestors);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static GeographyCode readCode(Connection conn, long gcid, boolean decodeAncestors) {
        if (conn == null || gcid < 0) {
            return null;
        }
        try {
            GeographyCode code;
            try ( PreparedStatement query = conn.prepareStatement(GCidQeury)) {
                query.setLong(1, gcid);
                code = readCode(conn, query, decodeAncestors);
            }
            return code;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static GeographyCode readCode(int level, String name, boolean decodeAncestors) {
        if (level <= 0 || name == null) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return readCode(conn, level, name, decodeAncestors);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static GeographyCode readCode(Connection conn, int level, String name, boolean decodeAncestors) {
        if (level <= 0 || name == null) {
            return null;
        }
        try {
            GeographyCode code;
            try ( PreparedStatement statement = conn.prepareStatement(LevelNameQeury)) {
                statement.setInt(1, level);
                setNameParameters(statement, name, 1);
                code = readCode(conn, statement, decodeAncestors);
            }
            return code;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static GeographyCode readCode(GeographyCode code, boolean decodeAncestors) {
        if (code == null || !GeographyCode.valid(code)) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return readCode(conn, code, decodeAncestors);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static boolean setCodeQueryParameters2(PreparedStatement statement, GeographyCode code) {
        if (statement == null || code == null) {
            return false;
        }
        try {
            fixValues(code);
            int level = code.getLevel();
            String name;
            if (code.getChineseName() != null) {
                name = stringValue(code.getChineseName()).toLowerCase();
            } else if (code.getEnglishName() != null) {
                name = stringValue(code.getEnglishName()).toLowerCase();
            } else {
                return false;
            }
            statement.setShort(1, (short) level);
            statement.setLong(2, level > 2 ? code.getContinent() : -1);
            statement.setLong(3, level > 3 ? code.getCountry() : -1);
            statement.setLong(4, level > 4 ? code.getProvince() : -1);
            statement.setLong(5, level > 5 ? code.getCity() : -1);
            statement.setLong(6, level > 6 ? code.getCounty() : -1);
            statement.setLong(7, level > 7 ? code.getTown() : -1);
            statement.setLong(8, level > 8 ? code.getVillage() : -1);
            statement.setString(9, name);
            statement.setString(10, name);
            statement.setString(11, name);
            statement.setString(12, name);
            statement.setString(13, name);
            statement.setString(14, name);
            statement.setString(15, name);

            MyBoxLog.debug(name);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static GeographyCode readCode(Connection conn, GeographyCode code, boolean decodeAncestors) {
        if (conn == null || code == null || !GeographyCode.valid(code)) {
            return null;
        }
        try {
            if (code.getGcid() > 0) {
                try ( PreparedStatement statement = conn.prepareStatement(GCidQeury)) {
                    statement.setLong(1, code.getGcid());
                    return readCode(conn, statement, decodeAncestors);
                }
            } else {
                int level = code.getLevel();
                String condition = "level=" + level;
                switch (level) {
                    case 3:
//                        condition += " AND continent=" + code.getContinent();
                        break;
                    case 4:
                        condition += " AND continent=" + code.getContinent()
                                + " AND country=" + code.getCountry();
                        break;
                    case 5:
                        condition += " AND continent=" + code.getContinent()
                                + " AND country=" + code.getCountry()
                                + " AND province=" + code.getProvince();
                        break;
                    case 6:
                        condition += " AND continent=" + code.getContinent()
                                + " AND country=" + code.getCountry()
                                + " AND province=" + code.getProvince()
                                + " AND city=" + code.getCity();
                        break;
                    case 7:
                        condition += " AND country=" + code.getCountry()
                                + " AND province=" + code.getProvince()
                                + " AND city=" + code.getCity()
                                + " AND county=" + code.getCounty();
                        break;
                    case 8:
                        condition += " AND continent=" + code.getContinent()
                                + " AND country=" + code.getCountry()
                                + " AND province=" + code.getProvince()
                                + " AND city=" + code.getCity()
                                + " AND county=" + code.getCounty()
                                + " AND town=" + code.getTown();
                        break;
                    case 9:
                        condition += " AND continent=" + code.getContinent()
                                + " AND country=" + code.getCountry()
                                + " AND province=" + code.getProvince()
                                + " AND city=" + code.getCity()
                                + " AND county=" + code.getCounty()
                                + " AND town=" + code.getTown()
                                + " AND village=" + code.getVillage();
                        break;
                    case 10:
                    default:
                        condition += " AND continent=" + code.getContinent()
                                + " AND country=" + code.getCountry()
                                + " AND province=" + code.getProvince()
                                + " AND city=" + code.getCity()
                                + " AND county=" + code.getCounty()
                                + " AND town=" + code.getTown()
                                + " AND village=" + code.getVillage()
                                + " AND building=" + code.getBuilding();
                        break;
                }
                String nameEqual = "";
                if (code.getChineseName() != null) {
                    String name = stringValue(code.getChineseName()).toLowerCase();
                    nameEqual = " ( chinese_name IS NOT NULL AND LCASE(chinese_name)='" + name + "' ) OR "
                            + " ( alias1 IS NOT NULL AND LCASE(alias1)='" + name + "' ) OR "
                            + " ( alias2 IS NOT NULL AND LCASE(alias2)='" + name + "' ) OR "
                            + " ( alias3 IS NOT NULL AND LCASE(alias3)='" + name + "' ) OR "
                            + " ( alias4 IS NOT NULL AND LCASE(alias4)='" + name + "' ) OR "
                            + " ( alias5 IS NOT NULL AND LCASE(alias5)='" + name + "' ) ";
                }
                if (code.getEnglishName() != null) {
                    String name = stringValue(code.getEnglishName()).toLowerCase();
                    nameEqual = nameEqual.isBlank() ? "" : nameEqual + " OR ";
                    nameEqual += " ( english_name IS NOT NULL AND LCASE(english_name)='" + name + "' ) OR "
                            + " ( alias1 IS NOT NULL AND LCASE(alias1)='" + name + "' ) OR "
                            + " ( alias2 IS NOT NULL AND LCASE(alias2)='" + name + "' ) OR "
                            + " ( alias3 IS NOT NULL AND LCASE(alias3)='" + name + "' ) OR "
                            + " ( alias4 IS NOT NULL AND LCASE(alias4)='" + name + "' ) OR "
                            + " ( alias5 IS NOT NULL AND LCASE(alias5)='" + name + "' )  ";
                }
                if (nameEqual.isBlank()) {
                    return null;
                }
                String sql = "SELECT * FROM Geography_Code WHERE " + condition + " AND (" + nameEqual + ")";
                return queryCode(conn, sql, decodeAncestors);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    // Generally, when location full name is need, "decodeAncestors" should be true
    public static GeographyCode readCode(Connection conn, PreparedStatement query, boolean decodeAncestors) {
        if (conn == null || query == null) {
            return null;
        }
        try {
            GeographyCode code;
            query.setMaxRows(1);
            conn.setAutoCommit(true);
            try ( ResultSet results = query.executeQuery()) {
                if (results.next()) {
                    code = readResults(results);
                } else {
                    return null;
                }
            }
            if (decodeAncestors && code != null) {
                decodeAncestors(conn, query, code);
            }
            return code;
        } catch (Exception e) {
            MyBoxLog.error(e);
//            MyBoxLog.debug(e);
            return null;
        }
    }

    public static GeographyCode readResults(Connection conn, ResultSet results, boolean decodeAncestors) {
        if (conn == null || results == null) {
            return null;
        }
        try {
            GeographyCode code = readResults(results);
            if (decodeAncestors && code != null) {
                decodeAncestors(conn, code);
            }
            return code;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static GeographyCode decodeAncestors(GeographyCode code) {
        if (code == null) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            decodeAncestors(conn, code);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return code;
    }

    public static void decodeAncestors(Connection conn, GeographyCode code) {
        if (conn == null || code == null) {
            return;
        }
        try ( PreparedStatement query = conn.prepareStatement(GCidQeury)) {
            decodeAncestors(conn, query, code);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void decodeAncestors(Connection conn, PreparedStatement query, GeographyCode code) {
        if (conn == null || code == null) {
            return;
        }
        int level = code.getLevel();
        if (level < 3 || level > 10) {
            return;
        }
        try {
            conn.setAutoCommit(true);

            if (code.getContinent() > 0) {
                query.setLong(1, code.getContinent());

                try ( ResultSet cresults = query.executeQuery()) {
                    if (cresults.next()) {
                        code.setContinentCode(readResults(cresults));
                    }
                }
            }
            if (level < 4) {
                return;
            }
            if (code.getCountry() > 0) {
                query.setLong(1, code.getCountry());
                try ( ResultSet cresults = query.executeQuery()) {
                    if (cresults.next()) {
                        code.setCountryCode(readResults(cresults));
                    }
                }
            }

            if (level < 5) {
                return;
            }
            if (code.getProvince() > 0) {
                query.setLong(1, code.getProvince());
                try ( ResultSet presults = query.executeQuery()) {
                    if (presults.next()) {
                        code.setProvinceCode(readResults(presults));
                    }
                }
            }

            if (level < 6) {
                return;
            }
            if (code.getCity() > 0) {
                query.setLong(1, code.getCity());
                try ( ResultSet iresults = query.executeQuery()) {
                    if (iresults.next()) {
                        code.setCityCode(readResults(iresults));
                    }
                }
            }

            if (level < 7) {
                return;
            }
            if (code.getCounty() > 0) {
                query.setLong(1, code.getCounty());
                try ( ResultSet iresults = query.executeQuery()) {
                    if (iresults.next()) {
                        code.setCountyCode(readResults(iresults));
                    }
                }
            }

            if (level < 8) {
                return;
            }
            if (code.getTown() > 0) {
                query.setLong(1, code.getTown());
                try ( ResultSet iresults = query.executeQuery()) {
                    if (iresults.next()) {
                        code.setTownCode(readResults(iresults));
                    }
                }
            }

            if (level < 9) {
                return;
            }
            if (code.getVillage() > 0) {
                query.setLong(1, code.getVillage());
                try ( ResultSet iresults = query.executeQuery()) {
                    if (iresults.next()) {
                        code.setVillageCode(readResults(iresults));
                    }
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static long readGCid(Connection conn, PreparedStatement statement) {
        if (conn == null || statement == null) {
            return -1;
        }
        try {
            statement.setMaxRows(1);
            conn.setAutoCommit(true);
            try ( ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getLong("gcid");
                } else {
                    return -1;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return -1;
        }
    }

    public static GeographyCode readResults(ResultSet results) {
        if (results == null) {
            return null;
        }
        try {
            GeographyCode code = new GeographyCode();
            code.setGcid(results.getLong("gcid"));
            code.setSource(results.getShort("gcsource"));
            code.setLevel(results.getShort("level"));
            code.setLevelCode(new GeographyCodeLevel(code.getLevel()));
            code.setLongitude(results.getDouble("longitude"));
            code.setLatitude(results.getDouble("latitude"));
            code.setAltitude(results.getDouble("altitude"));
            code.setPrecision(results.getDouble("precision"));
            code.setCoordinateSystem(new GeoCoordinateSystem(results.getShort("coordinate_system")));
            code.setChineseName(results.getString("chinese_name"));
            code.setEnglishName(results.getString("english_name"));
            code.setCode1(results.getString("code1"));
            code.setCode2(results.getString("code2"));
            code.setCode3(results.getString("code3"));
            code.setCode4(results.getString("code4"));
            code.setCode5(results.getString("code5"));
            code.setAlias1(results.getString("alias1"));
            code.setAlias2(results.getString("alias2"));
            code.setAlias3(results.getString("alias3"));
            code.setAlias4(results.getString("alias4"));
            code.setAlias5(results.getString("alias5"));
            code.setArea(results.getLong("area"));
            code.setPopulation(results.getLong("population"));
            code.setComments(results.getString("comments"));
            code.setOwner(results.getLong("owner"));
            code.setContinent(results.getLong("continent"));
            code.setCountry(results.getLong("country"));
            code.setProvince(results.getLong("province"));
            code.setCity(results.getLong("city"));
            code.setCounty(results.getLong("county"));
            code.setTown(results.getLong("town"));
            code.setVillage(results.getLong("village"));
            code.setBuilding(results.getLong("building"));
            return code;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<GeographyCode> queryCodes(String sql, int max, boolean decodeAncestors) {
        List<GeographyCode> codes = new ArrayList<>();
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            codes = queryCodes(conn, sql, max, decodeAncestors);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return codes;
    }

    public static List<GeographyCode> queryCodes(Connection conn, String sql, boolean decodeAncestors) {
        return queryCodes(conn, sql, -1, decodeAncestors);
    }

    public static List<GeographyCode> queryCodes(Connection conn, String sql, int max, boolean decodeAncestors) {
        List<GeographyCode> codes = new ArrayList<>();
        try ( Statement statement = conn.createStatement()) {
            if (max > 0) {
                statement.setMaxRows(max);
            }
            try ( ResultSet results = statement.executeQuery(sql)) {
                while (results.next()) {
                    GeographyCode code = readResults(results);
                    codes.add(code);
                }
            }
            for (GeographyCode code : codes) {
                decodeAncestors(conn, code);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return codes;
    }

    public static List<GeographyCode> readAll(boolean decodeAncestors) {
        return readMax(0, decodeAncestors);
    }

    public static List<GeographyCode> readMax(int max, boolean decodeAncestors) {
        List<GeographyCode> codes = new ArrayList<>();
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            try ( PreparedStatement statement = conn.prepareStatement(AllQeury)) {
                if (max > 0) {
                    statement.setMaxRows(max);
                }
                codes = readCodes(conn, statement, decodeAncestors);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return codes;
    }

    public static List<GeographyCode> readRangle(long offset, long number, boolean decodeAncestors) {
        List<GeographyCode> codes = new ArrayList<>();
        if (offset < 0 || number <= 0) {
            return codes;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            try ( PreparedStatement statement = conn.prepareStatement(PageQeury)) {
                statement.setLong(1, offset);
                statement.setLong(2, number);
                codes = readCodes(conn, statement, decodeAncestors);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return codes;
    }

    public static List<GeographyCode> readCodes(Connection conn, PreparedStatement statement, boolean decodeAncestors) {
        List<GeographyCode> codes = new ArrayList<>();
        try {
            conn.setAutoCommit(true);
            try ( ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    GeographyCode code = readResults(results);
                    codes.add(code);
                }
            }
            for (GeographyCode code : codes) {
                decodeAncestors(conn, code);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return codes;
    }

    public static List<GeographyCode> queryChildren(Connection conn, long gcid) {
        List<GeographyCode> codes = new ArrayList<>();
        if (gcid <= 0) {
            return codes;
        }
        try ( PreparedStatement query = conn.prepareStatement(ChildrenQuery)) {
            query.setLong(1, gcid);
            conn.setAutoCommit(true);
            try ( ResultSet results = query.executeQuery()) {
                while (results.next()) {
                    GeographyCode code = readResults(results);
                    codes.add(code);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return codes;
    }

    public static boolean write(GeographyCode code) {
        if (code == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return write(conn, code);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean write(Connection conn, GeographyCode code) {
        if (code == null || conn == null || !GeographyCode.valid(code)) {
            return false;
        }
        try {
            GeographyCode exist = readCode(conn, code, false);
            if (exist != null) {
                code.setGcid(exist.getGcid());
                update(conn, code);
            } else {
                insert(conn, code);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean write(List<GeographyCode> codes) {
        if (codes == null || codes.isEmpty()) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return write(conn, codes);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean write(Connection conn, List<GeographyCode> codes) {
        if (codes == null || codes.isEmpty()) {
            return false;
        }
        try ( PreparedStatement update = conn.prepareStatement(Update);
                 PreparedStatement insert = conn.prepareStatement(Insert);) {
            conn.setAutoCommit(false);
            for (GeographyCode code : codes) {
                GeographyCode exist = readCode(conn, code, false);
                if (exist != null) {
                    code.setGcid(exist.getGcid());
                    update(conn, update, code);
                } else {
                    insert(conn, insert, code);
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean insert(GeographyCode code) {
        if (code == null || !GeographyCode.valid(code)) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            return insert(conn, code);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static void fixValues(GeographyCode code) {
        if (code == null) {
            return;
        }
        try {
            setOwner(code);
            switch (code.getLevel()) {
                case 1:
                    code.setContinent(-1);
                    code.setCountry(-1);
                    code.setProvince(-1);
                    code.setCity(-1);
                    code.setCounty(-1);
                    code.setTown(-1);
                    code.setVillage(-1);
                    code.setBuilding(-1);
                    break;
                case 2:
                    if (code.getGcid() > 0) {
                        code.setContinent(code.getGcid());
                    }
                    code.setCountry(-1);
                    code.setProvince(-1);
                    code.setCity(-1);
                    code.setCounty(-1);
                    code.setTown(-1);
                    code.setVillage(-1);
                    code.setBuilding(-1);
                    break;
                case 3:
                    if (code.getGcid() > 0) {
                        code.setCountry(code.getGcid());
                    }
                    code.setProvince(-1);
                    code.setCity(-1);
                    code.setCounty(-1);
                    code.setTown(-1);
                    code.setVillage(-1);
                    code.setBuilding(-1);
                    break;
                case 4:
                    if (code.getGcid() > 0) {
                        code.setProvince(code.getGcid());
                    }
                    code.setCity(-1);
                    code.setCounty(-1);
                    code.setTown(-1);
                    code.setVillage(-1);
                    code.setBuilding(-1);
                    break;
                case 5:
                    if (code.getGcid() > 0) {
                        code.setCity(code.getGcid());
                    }
                    code.setCounty(-1);
                    code.setTown(-1);
                    code.setVillage(-1);
                    code.setBuilding(-1);
                    break;
                case 6:
                    if (code.getGcid() > 0) {
                        code.setCounty(code.getGcid());
                    }
                    code.setTown(-1);
                    code.setVillage(-1);
                    code.setBuilding(-1);
                    break;
                case 7:
                    if (code.getGcid() > 0) {
                        code.setTown(code.getGcid());
                    }
                    code.setVillage(-1);
                    code.setBuilding(-1);
                    break;
                case 8:
                    if (code.getGcid() > 0) {
                        code.setVillage(code.getGcid());
                    }
                    code.setBuilding(-1);
                    break;
                case 9:
                    if (code.getGcid() > 0) {
                        code.setBuilding(code.getGcid());
                    }
                    break;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void setOwner(GeographyCode code) {
        if (code == null) {
            return;
        }
        try {
            switch (code.getLevel()) {
                case 1:
                case 2:
                    code.setOwner(1);
                    break;
                case 3:
                    code.setOwner(code.getContinent() > 0 ? code.getContinent() : 1);
                    break;
                case 4:
                    code.setOwner(
                            code.getCountry() > 0 ? code.getCountry()
                            : code.getContinent() > 0 ? code.getContinent() : 1
                    );
                    break;
                case 5:
                    code.setOwner(
                            code.getProvince() > 0 ? code.getProvince()
                            : code.getCountry() > 0 ? code.getCountry()
                            : (code.getContinent() > 0 ? code.getContinent() : 1)
                    );
                    break;
                case 6:
                    code.setOwner(
                            code.getCity() > 0 ? code.getCity()
                            : code.getProvince() > 0 ? code.getProvince()
                            : code.getCountry() > 0 ? code.getCountry()
                            : (code.getContinent() > 0 ? code.getContinent() : 1)
                    );
                    break;
                case 7:
                    code.setOwner(
                            code.getCounty() > 0 ? code.getCounty()
                            : code.getCity() > 0 ? code.getCity()
                            : code.getProvince() > 0 ? code.getProvince()
                            : code.getCountry() > 0 ? code.getCountry()
                            : (code.getContinent() > 0 ? code.getContinent() : 1)
                    );
                    break;
                case 8:
                    code.setOwner(
                            code.getTown() > 0 ? code.getTown()
                            : code.getCounty() > 0 ? code.getCounty()
                            : code.getCity() > 0 ? code.getCity()
                            : code.getProvince() > 0 ? code.getProvince()
                            : code.getCountry() > 0 ? code.getCountry()
                            : (code.getContinent() > 0 ? code.getContinent() : 1)
                    );
                    break;
                case 9:
                    code.setOwner(
                            code.getVillage() > 0 ? code.getVillage()
                            : code.getTown() > 0 ? code.getTown()
                            : code.getCounty() > 0 ? code.getCounty()
                            : code.getCity() > 0 ? code.getCity()
                            : code.getProvince() > 0 ? code.getProvince()
                            : code.getCountry() > 0 ? code.getCountry()
                            : (code.getContinent() > 0 ? code.getContinent() : 1)
                    );
                    break;
                case 10:
                default:
                    code.setOwner(
                            code.getBuilding() > 0 ? code.getBuilding()
                            : code.getVillage() > 0 ? code.getVillage()
                            : code.getTown() > 0 ? code.getTown()
                            : code.getCounty() > 0 ? code.getCounty()
                            : code.getCity() > 0 ? code.getCity()
                            : code.getProvince() > 0 ? code.getProvince()
                            : code.getCountry() > 0 ? code.getCountry()
                            : (code.getContinent() > 0 ? code.getContinent() : 1)
                    );
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            MyBoxLog.debug(code.getLevelName() + " " + code.getName());
        }
    }

    public static boolean insert(Connection conn, GeographyCode code) {
        if (conn == null || code == null || !GeographyCode.valid(code)) {
            return false;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Insert)) {
            return insert(conn, statement, code);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return false;
    }

    public static boolean insert(Connection conn, PreparedStatement statement, GeographyCode code) {
        if (conn == null || code == null || !GeographyCode.valid(code)) {
            return false;
        }
        try {
            if (setInsert(conn, statement, code)) {
                return statement.executeUpdate() > 0;
            }
        } catch (Exception e) {
//            MyBoxLog.error(e);
            MyBoxLog.debug(code.getLevelName() + " " + code.getName() + " " + code.getGcid() + " " + code.getOwner());
        }
        return false;
    }

    public static boolean setInsert(Connection conn, PreparedStatement statement, GeographyCode code) {
        if (conn == null || statement == null || code == null || !GeographyCode.valid(code)) {
            return false;
        }
        try {
            if (code.getGcid() <= 0) {
                long gcid = generateID(conn);
                code.setGcid(gcid);
            }
            fixValues(code);
            statement.setLong(1, code.getGcid());
            statement.setShort(2, (short) code.getLevel());
            statement.setShort(3, code.getSourceValue());
            statement.setDouble(4, code.getLongitude());
            statement.setDouble(5, code.getLatitude());
            if (code.getChineseName() == null) {
                statement.setNull(6, Types.VARCHAR);
            } else {
                statement.setString(6, code.getChineseName());
            }
            if (code.getEnglishName() == null) {
                statement.setNull(7, Types.VARCHAR);
            } else {
                statement.setString(7, code.getEnglishName());
            }
            if (code.getCode1() == null) {
                statement.setNull(8, Types.VARCHAR);
            } else {
                statement.setString(8, code.getCode1());
            }
            if (code.getCode2() == null) {
                statement.setNull(9, Types.VARCHAR);
            } else {
                statement.setString(9, code.getCode2());
            }
            if (code.getCode3() == null) {
                statement.setNull(10, Types.VARCHAR);
            } else {
                statement.setString(10, code.getCode3());
            }
            if (code.getCode4() == null) {
                statement.setNull(11, Types.VARCHAR);
            } else {
                statement.setString(11, code.getCode4());
            }
            if (code.getCode5() == null) {
                statement.setNull(12, Types.VARCHAR);
            } else {
                statement.setString(12, code.getCode5());
            }
            if (code.getAlias1() == null) {
                statement.setNull(13, Types.VARCHAR);
            } else {
                statement.setString(13, code.getAlias1());
            }
            if (code.getAlias2() == null) {
                statement.setNull(14, Types.VARCHAR);
            } else {
                statement.setString(14, code.getAlias2());
            }
            if (code.getAlias3() == null) {
                statement.setNull(15, Types.VARCHAR);
            } else {
                statement.setString(15, code.getAlias3());
            }
            if (code.getAlias4() == null) {
                statement.setNull(16, Types.VARCHAR);
            } else {
                statement.setString(16, code.getAlias4());
            }
            if (code.getAlias5() == null) {
                statement.setNull(17, Types.VARCHAR);
            } else {
                statement.setString(17, code.getAlias5());
            }
            statement.setLong(18, code.getArea());
            statement.setLong(19, code.getPopulation());
            if (code.getComments() == null) {
                statement.setNull(20, Types.VARCHAR);
            } else {
                statement.setString(20, code.getComments());
            }
            statement.setLong(21, code.getContinent());
            statement.setLong(22, code.getCountry());
            statement.setLong(23, code.getProvince());
            statement.setLong(24, code.getCity());
            statement.setLong(25, code.getCounty());
            statement.setLong(26, code.getTown());
            statement.setLong(27, code.getVillage());
            statement.setLong(28, code.getBuilding());
            statement.setLong(29, code.getOwner());
            statement.setDouble(30, code.getAltitude());
            statement.setDouble(31, code.getPrecision());
            statement.setShort(32, code.getCoordinateSystem().shortValue());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean update(Connection conn, GeographyCode code) {
        if (conn == null || code == null || !GeographyCode.valid(code)) {
            return false;
        }
        try ( PreparedStatement statement = conn.prepareStatement(Update)) {
            return update(conn, statement, code);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean update(Connection conn, PreparedStatement statement, GeographyCode code) {
        if (conn == null || code == null || !GeographyCode.valid(code)) {
            return false;
        }
        try {
            if (code.getGcid() <= 0) {
                GeographyCode exist = readCode(conn, code, false);
                if (exist == null) {
                    return insert(conn, code);
                }
                code.setGcid(exist.getGcid());
            }
            setUpdate(conn, statement, code);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean setUpdate(Connection conn, PreparedStatement statement, GeographyCode code) {
        if (conn == null || statement == null || code == null || !GeographyCode.valid(code) || code.getGcid() <= 0) {
            return false;
        }
        try {
            fixValues(code);
            statement.setShort(1, (short) code.getLevel());
            statement.setDouble(2, code.getLongitude());
            statement.setDouble(3, code.getLatitude());
            statement.setShort(4, code.getSourceValue());
            if (code.getChineseName() == null) {
                statement.setNull(5, Types.VARCHAR);
            } else {
                statement.setString(5, code.getChineseName());
            }
            if (code.getEnglishName() == null) {
                statement.setNull(6, Types.VARCHAR);
            } else {
                statement.setString(6, code.getEnglishName());
            }
            if (code.getCode1() == null) {
                statement.setNull(7, Types.VARCHAR);
            } else {
                statement.setString(7, code.getCode1());
            }
            if (code.getCode2() == null) {
                statement.setNull(8, Types.VARCHAR);
            } else {
                statement.setString(8, code.getCode2());
            }
            if (code.getCode3() == null) {
                statement.setNull(9, Types.VARCHAR);
            } else {
                statement.setString(9, code.getCode3());
            }
            if (code.getCode4() == null) {
                statement.setNull(10, Types.VARCHAR);
            } else {
                statement.setString(10, code.getCode4());
            }
            if (code.getCode5() == null) {
                statement.setNull(11, Types.VARCHAR);
            } else {
                statement.setString(11, code.getCode5());
            }
            if (code.getAlias1() == null) {
                statement.setNull(12, Types.VARCHAR);
            } else {
                statement.setString(12, code.getAlias1());
            }
            if (code.getAlias2() == null) {
                statement.setNull(13, Types.VARCHAR);
            } else {
                statement.setString(13, code.getAlias2());
            }
            if (code.getAlias3() == null) {
                statement.setNull(14, Types.VARCHAR);
            } else {
                statement.setString(14, code.getAlias3());
            }
            if (code.getAlias4() == null) {
                statement.setNull(15, Types.VARCHAR);
            } else {
                statement.setString(15, code.getAlias4());
            }
            if (code.getAlias5() == null) {
                statement.setNull(16, Types.VARCHAR);
            } else {
                statement.setString(16, code.getAlias5());
            }
            statement.setLong(17, code.getArea());
            statement.setLong(18, code.getPopulation());
            if (code.getComments() == null) {
                statement.setNull(19, Types.VARCHAR);
            } else {
                statement.setString(19, code.getComments());
            }
            statement.setLong(20, code.getContinent());
            statement.setLong(21, code.getCountry());
            statement.setLong(22, code.getProvince());
            statement.setLong(23, code.getCity());
            statement.setLong(24, code.getCounty());
            statement.setLong(25, code.getTown());
            statement.setLong(26, code.getVillage());
            statement.setLong(27, code.getBuilding());
            statement.setLong(28, code.getOwner());
            statement.setDouble(29, code.getAltitude());
            statement.setDouble(30, code.getPrecision());
            statement.setShort(31, code.getCoordinateSystem().shortValue());
            statement.setLong(32, code.getGcid());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean delete(GeographyCode code) {
        if (code == null || !GeographyCode.valid(code) || GeographyCode.isPredefined(code)) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            try ( PreparedStatement statement = conn.prepareStatement(Delete)) {
                return delete(conn, statement, code);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean delete(Connection conn, PreparedStatement statement, GeographyCode code) {
        if (statement == null || code == null
                || !GeographyCode.valid(code) || GeographyCode.isPredefined(code)) {
            return false;
        }
        try {
            GeographyCode exist = readCode(conn, code, false);
            if (exist == null || GeographyCode.isPredefined(exist)) {
                return false;
            }
            statement.setLong(1, exist.getGcid());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            MyBoxLog.error(e);

            return false;
        }
    }

    public static int delete(List<GeographyCode> codes) {
        if (codes == null || codes.isEmpty()) {
            return 0;
        }
        int count = 0;
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setAutoCommit(false);
            try ( PreparedStatement statement = conn.prepareStatement(Delete)) {
                for (int i = 0; i < codes.size(); ++i) {
                    GeographyCode code = codes.get(i);
                    if (code.getGcid() <= 0 || GeographyCode.isPredefined(code)) {
                        continue;
                    }
                    statement.setLong(1, code.getGcid());
                    statement.addBatch();
                    if (i > 0 && (i % Database.BatchSize == 0)) {
                        int[] res = statement.executeBatch();
                        for (int r : res) {
                            if (r > 0) {
                                count += r;
                            }
                        }
                        conn.commit();
                        statement.clearBatch();
                    }
                }
                int[] res = statement.executeBatch();
                for (int r : res) {
                    if (r > 0) {
                        count += r;
                    }
                }
                conn.commit();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);

        }
        return count;
    }

    public static GeographyCode getOwner(GeographyCode code, boolean decodeAncestors) {
        if (code == null) {
            return null;
        }
        fixValues(code);
        return readCode(code.getOwner(), decodeAncestors);
    }

    public static List<Long> haveChildren(Connection conn, List<GeographyCode> nodes) {
        List<Long> haveChildren = new ArrayList();
        if (conn == null || nodes == null) {
            return haveChildren;
        }
        try ( PreparedStatement query = conn.prepareStatement(FirstChildQuery)) {
            query.setMaxRows(1);
            conn.setAutoCommit(true);
            for (GeographyCode code : nodes) {
                long gcid = code.getGcid();
                query.setLong(1, gcid);
                try ( ResultSet results = query.executeQuery()) {
                    if (results.next()) {
                        haveChildren.add(gcid);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return haveChildren;
    }

    @Override
    public List<String> importNecessaryFields() {
        return Arrays.asList(Languages.message("Level"), Languages.message("Longitude"), Languages.message("Latitude"), Languages.message("ChineseName"), Languages.message("EnglishName")
        );
    }

    @Override
    public List<String> importAllFields() {
        return Arrays.asList(Languages.message("Level"), Languages.message("ChineseName"), Languages.message("EnglishName"),
                Languages.message("Longitude"), Languages.message("Latitude"), Languages.message("Altitude"),
                Languages.message("Precision"), Languages.message("CoordinateSystem"),
                Languages.message("Code1"), Languages.message("Code2"), Languages.message("Code3"), Languages.message("Code4"), Languages.message("Code5"),
                Languages.message("Alias1"), Languages.message("Alias2"), Languages.message("Alias3"), Languages.message("Alias4"), Languages.message("Alias5"),
                Languages.message("SquareMeters"), Languages.message("Population"),
                Languages.message("Continent"), Languages.message("Country"), Languages.message("Province"), Languages.message("City"), Languages.message("County"),
                Languages.message("Town"), Languages.message("Village"), Languages.message("Building"), Languages.message("Comments")
        );
    }

}
