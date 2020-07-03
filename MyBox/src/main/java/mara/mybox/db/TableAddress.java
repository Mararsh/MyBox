package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.data.Address;
import mara.mybox.data.AddressExtension;
import mara.mybox.data.AddressExtension.AddressExtensionType;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.GeographyCodeLevel;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.failed;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import static mara.mybox.db.TableGeographyCode.LevelNameQeury;
import static mara.mybox.db.TableGeographyCode.setNameParameters;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2020-5-27
 * @License Apache License Version 2.0
 */
public class TableAddress extends DerbyBase {

    public static final String Create_Index_adidIndex
            = " CREATE INDEX  Address_adid_index on Address ( "
            + "  adid, level, owner"
            + " )";

    public static final String AllQeury
            = "SELECT * FROM Address ORDER BY adid";

    public static final String PageQeury
            = "SELECT * FROM Address ORDER BY adid "
            + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    public static final String GCidQeury
            = "SELECT * FROM Address WHERE adid=?";

    public static final String GCidExistedQuery
            = "SELECT adid FROM Address where adid=?";

    public static final String LevelNameEqual
            = "SELECT * FROM Address_View WHERE level=? AND ext_type>5 AND ext_value=?";

    public static final String Update
            = "UPDATE Address SET "
            + " level=?, longitude=?, latitude=?, predefined=?, chinese_name=?, english_name=?, "
            + " code1=?, code2=?, code3=?, code4=?, code5=?,alias1=?, alias2=?, alias3=?, alias4=?, alias5=?, "
            + " area=?, population=?, comments=?, "
            + " continent=?, country=?, province=? , city=?, county=?, town=?, village=? , building=? "
            + " WHERE adid=?";

    public static final String Insert
            = "INSERT INTO Address( "
            + " adid, level, predefined, longitude, latitude, chinese_name, english_name,"
            + " code1, code2, code3,  code4, code5, alias1, alias2, alias3, alias4, alias5, "
            + " area, population, comments,"
            + " continent, country, province, city, county, town, village, building) "
            + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? ) ";

    public static final String Delete
            = "DELETE FROM Address WHERE adid=?";

    /*
        View
     */
    public static final String CreateExtensionView
            = " CREATE VIEW Address_View AS "
            + "  SELECT Address.*, Address_Extension.*,  "
            + "  FROM Address LEFT JOIN Address_Extension ON Address.adid=Address_Extension.location";

    /**
     * One of following can determine an address: 1. adid. This is accurate
     * matching. 2. level + name + owner. This is accurate matching.
     *
     * Notice: coordinate can not determine an address
     */
    public TableAddress() {
        Table_Name = "Address";
        Keys = new ArrayList<>() {
            {
                add("adid");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE Address ( "
                + "  adid BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                + "  source SMALLINT NOT NULL, "
                + "  level SMALLINT NOT NULL, "
                + "  owner BIGINT, "
                + "  longitude DOUBLE NOT NULL, "
                + "  latitude DOUBLE NOT NULL, "
                + "  altitude DOUBLE, "
                + "  precision DOUBLE, "
                + "  coordinate_system SMALLINT, "
                + "  area BIGINT, " // m2
                + "  population BIGINT, "
                + "  comments VARCHAR(32672), "
                + "  PRIMARY KEY (adid)"
                + "  FOREIGN KEY (owner) REFERENCES Address (adid) ON DELETE CASCADE ON UPDATE RESTRICT"
                + " )";
    }

    private static long adjustIndex(Connection conn) {
        try {
            long maxid = 1;
            try ( ResultSet results = conn.createStatement().executeQuery(
                    "SELECT max(adid) FROM Address")) {
                if (results.next()) {
                    maxid = results.getInt(1);
                }
            }
            maxid = Math.max(100000, maxid) + 1;
            String sql = "ALTER TABLE Address ALTER COLUMN adid RESTART WITH " + (maxid + 1);
            try ( Statement statement = conn.createStatement()) {
                statement.executeUpdate(sql);
            }
            return maxid;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return -1;
        }
    }

    public static int size() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            final String sql = " SELECT count(adid) FROM Address";
            return size(conn, sql);
        } catch (Exception e) {
            failed(e);
            return 0;
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

    public static Address earth() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            return earth(conn);
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return null;
        }
    }

    public static Address earth(Connection conn) {
        return read(conn, 1, "Earth", false);
    }

    public static Address read(int level, String name, boolean decodeAncestors) {
        if (level <= 0 || name == null) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            return read(conn, level, name, decodeAncestors);
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return null;
        }
    }

    public static Address read(Connection conn, int level, String name, boolean decodeAncestors) {
        if (level <= 0 || name == null) {
            return null;
        }
        try {
            Address address;
            try ( PreparedStatement statement = conn.prepareStatement(LevelNameQeury)) {
                statement.setInt(1, level);
                statement.setString(2, name);
                address = read(conn, statement);
            }
            return address;
        } catch (Exception e) {
            failed(e);
//            logger.debug(e.toString());
            return null;
        }
    }

    public static Address read(Connection conn, PreparedStatement statement) {
        if (conn == null || statement == null) {
            return null;
        }
        Address address = null;
        List<String> aliases = null, codes = null;
        try ( ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                if (address == null) {
                    address = Address.create();
                    address.setAdid(results.getLong("adid"));
                    address.setSource(results.getShort("source"));
                    address.setLevel(results.getShort("level"));
                    address.setOwner(results.getLong("owner"));
                    address.setLongitude(results.getDouble("longitude"));
                    address.setLatitude(results.getDouble("latitude"));
                    address.setAltitude(results.getDouble("altitude"));
                    address.setPrecision(results.getDouble("precision"));
                    address.setCoordinatetSystem(results.getShort("coordinate_system"));
                    address.setArea(results.getLong("area"));
                    address.setPopulation(results.getLong("population"));
                    address.setComments(results.getString("comments"));
                }
                Object extType = results.getObject("ext_type");
                String value = results.getString("ext_value");
                if (extType != null && value != null) {
                    short type = (short) extType;
                    switch (type) {
                        case 1:
                            address.setChineseName(value);
                            break;
                        case 2:
                            address.setEnglishName(value);
                            break;
                        case 3:
                            address.setChineseFullName(value);
                            break;
                        case 4:
                            address.setEnglishFullName(value);
                            break;
                        case 100:
                            if (aliases == null) {
                                aliases = new ArrayList<>();
                            }
                            aliases.add(value);
                            break;
                        case 500:
                            address.setAdministrativeCode(value);
                            break;
                        case 501:
                            address.setPostalCode(value);
                            break;
                        default:
                            if (codes == null) {
                                codes = new ArrayList<>();
                            }
                            codes.add(value);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            failed(e);
//            logger.debug(e.toString());
        }
        return address;
    }

}