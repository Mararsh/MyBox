package mara.mybox.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.data.GeographyCode;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.failed;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-2-3
 * @License Apache License Version 2.0
 */
public class TableGeographyCode extends DerbyBase {

    public TableGeographyCode() {
        Table_Name = "Geography_Code";
        Keys = new ArrayList<>() {
            {
                add("level");
                add("address");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE Geography_Code ( "
                + "  address VARCHAR(2048) NOT NULL, "
                + "  longitude DOUBLE NOT NULL, "
                + "  latitude DOUBLE NOT NULL, "
                + "  full_address VARCHAR(2048), "
                + "  country VARCHAR(1024), "
                + "  province VARCHAR(1024), "
                + "  city VARCHAR(1024), "
                + "  citycode VARCHAR(1024), "
                + "  district VARCHAR(2048), "
                + "  township VARCHAR(2048), "
                + "  neighborhood VARCHAR(2048), "
                + "  building VARCHAR(2048), "
                + "  administrative_code VARCHAR(1024), "
                + "  street VARCHAR(2048), "
                + "  number VARCHAR(1024), "
                + "  level VARCHAR(1024) NOT NULL, "
                + "  PRIMARY KEY (level, address)"
                + " )";

    }

    public static int size() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT count(address) FROM Geography_Code";
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                return results.getInt(1);
            } else {
                return 0;
            }
        } catch (Exception e) {
            failed(e);
            return 0;
        }
    }

    public static List<String> address() {
        List<String> addresses = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT address FROM Geography_Code";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                addresses.add(results.getString("address"));
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return addresses;
    }

    public static List<String> countries() {
        List<String> countries = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT DISTINCT country FROM Geography_Code WHERE "
                    + " country IS NOT NULL AND level='" + message("Country") + "' ORDER BY country";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                countries.add(results.getString("country"));
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return countries;
    }

    public static List<String> provinces(String country) {
        List<String> provinces = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT DISTINCT address FROM Geography_Code WHERE "
                    + ((country == null) ? "" : " country='" + country + "' AND ")
                    + " province IS NOT NULL  ORDER BY address";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                provinces.add(results.getString("address"));
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return provinces;
    }

    public static List<String> cities(String country, String province) {
        List<String> cities = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT DISTINCT address FROM Geography_Code WHERE "
                    + ((country == null) ? "" : " country='" + country + "' AND ")
                    + ((province == null) ? "" : " province='" + province + "' AND")
                    + " city IS NOT NULL  ORDER BY address";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                cities.add(results.getString("address"));
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return cities;
    }

    public static List<String> chineseCities(String province) {
        List<String> cities = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT DISTINCT address FROM Geography_Code WHERE "
                    + " country='" + message("China") + "' "
                    + " AND ( province='" + province + "' "
                    + " OR SUBSTR(province, 1, 2) = SUBSTR('" + province + "', 1, 2) ) "
                    + " AND city IS NOT NULL AND NOT(level='" + message("Province") + "') ORDER BY address";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                cities.add(results.getString("address"));
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return cities;
    }

    public static List<String> districts(String country, String province,
            String city) {
        List<String> districts = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT DISTINCT district FROM Geography_Code WHERE "
                    + ((country == null) ? "" : " country='" + country + "' AND ")
                    + ((province == null) ? "" : " province='" + province + "' AND")
                    + ((city == null) ? "" : " city='" + city + "' AND")
                    + " district IS NOT NULL ORDER BY city";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                districts.add(results.getString("district"));
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return districts;
    }

    public static List<String> townships(String country, String province,
            String city, String district) {
        List<String> townships = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT DISTINCT township FROM Geography_Code WHERE "
                    + ((country == null) ? "" : " country='" + country + "' AND ")
                    + ((province == null) ? "" : " province='" + province + "' AND")
                    + ((city == null) ? "" : " city='" + city + "' AND")
                    + ((district == null) ? "" : " district='" + district + "' AND")
                    + " township IS NOT NULL ORDER BY township";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                townships.add(results.getString("township"));
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return townships;
    }

    public static List<String> neighborhoods(String country, String province,
            String city, String district, String township) {
        List<String> neighborhoods = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT DISTINCT neighborhood FROM Geography_Code WHERE "
                    + ((country == null) ? "" : " country='" + country + "' AND ")
                    + ((province == null) ? "" : " province='" + province + "' AND")
                    + ((city == null) ? "" : " city='" + city + "' AND")
                    + ((district == null) ? "" : " district='" + district + "' AND")
                    + ((township == null) ? "" : " township='" + township + "' AND")
                    + " neighborhood IS NOT NULL ORDER BY neighborhood";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                neighborhoods.add(results.getString("neighborhood"));
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return neighborhoods;
    }

    public static List<String> levels() {
        List<String> levels = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT DISTINCT level FROM Geography_Code WHERE level IS NOT NULL";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                levels.add(results.getString("level"));
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return levels;
    }

    public static List<GeographyCode> read() {
        return read(0);
    }

    public static List<GeographyCode> read(int max) {
        List<GeographyCode> codes = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            statement.setMaxRows(max);
            String sql = "SELECT * FROM Geography_Code ORDER BY level, country, province, city";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                GeographyCode code = read(results);
                codes.add(code);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return codes;
    }

    public static List<GeographyCode> read(int offset, int number) {
        List<GeographyCode> codes = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT * FROM Geography_Code ORDER BY level, country, province, city OFFSET "
                    + offset + " ROWS FETCH NEXT " + number + " ROWS ONLY";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                GeographyCode code = read(results);
                codes.add(code);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return codes;
    }

    public static GeographyCode read(String address) {
        if (address == null || address.trim().isBlank()) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT * FROM Geography_Code WHERE "
                    + " address='" + address + "' OR full_address='" + address + "' "
                    + " OR SUBSTR(full_address, 1, 2) = SUBSTR('" + address + "', 1, 2) ";
//            logger.debug(sql);
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                return read(results);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return null;
    }

    public static GeographyCode readArea(String address) {
        if (address == null || address.trim().isBlank()) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT * FROM Geography_Code WHERE "
                    + " ( level='" + message("Country") + "' "
                    + "  AND ( address='" + address + "' OR country='" + address + "' OR full_address='" + address + "') ) "
                    + " OR ( level='" + message("Province") + "' "
                    + "  AND ( address='" + address + "' OR province='" + address + "' OR full_address='" + address + "') ) ";
//            logger.debug(sql);
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                return read(results);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return null;
    }

    public static GeographyCode readProvince(String country, String province) {
        if (province == null || province.trim().isBlank()) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT * FROM Geography_Code WHERE "
                    + " province IS NOT NULL AND level='" + message("Province") + "' "
                    + " AND country='" + country + "' AND "
                    + " address='" + province + "' OR full_address='" + province + "'";
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                return read(results);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return null;
    }

    public static GeographyCode readChineseProvince(String province) {
        if (province == null || province.trim().isBlank()) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT * FROM Geography_Code WHERE "
                    + " province IS NOT NULL AND level='" + message("Province") + "' "
                    + " AND country='" + message("China") + "' AND "
                    + " (address='" + province + "' OR full_address='" + province + "' "
                    + " OR SUBSTR(province, 1, 2) = SUBSTR('" + province + "', 1, 2)  "
                    + " OR SUBSTR(full_address, 1, 2) = SUBSTR('" + province + "', 1, 2) ) ";
//            logger.debug(sql);
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                return read(results);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return null;
    }

    public static GeographyCode readCity(String country, String province,
            String city) {
        if (city == null || city.trim().isBlank()) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT * FROM Geography_Code WHERE "
                    + " city IS NOT NULL AND NOT(level='" + message("Province") + "') "
                    + " AND country='" + country + "' "
                    + " AND SUBSTR(province, 1, 2) = SUBSTR('" + province + "', 1, 2)  "
                    + " (address='" + city + "' OR city like '" + city + "%') ";
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                return read(results);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return null;
    }

    public static GeographyCode readChineseCity(String province, String city) {
        if (city == null || city.trim().isBlank()) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql;

            sql = "SELECT * FROM Geography_Code WHERE "
                    + " city IS NOT NULL AND NOT(level='" + message("Province") + "') "
                    + " AND SUBSTR(province, 1, 2) = SUBSTR('" + province + "', 1, 2) "
                    + " AND (address='" + city + "' OR city like '" + city + "%' "
                    + " OR  SUBSTR(city, 1, 2) = SUBSTR('" + city + "', 1, 2) "
                    + " OR (district IS NOT NULL AND SUBSTR(district, 1, 2) = SUBSTR('" + city + "', 1, 2) ))";
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                return read(results);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return null;
    }

    public static GeographyCode read(double longitude, double latitude) {
        if (longitude < -180 || latitude < -90) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT * FROM Geography_Code WHERE "
                    + "longitude=" + longitude + " AND latitude=" + latitude;
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                return read(results);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return null;
    }

    public static List<GeographyCode> readLike(String input) {
        if (input == null || input.trim().isBlank()) {
            return read();
        }
        List<GeographyCode> codes = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT * FROM Geography_Code WHERE "
                    + " address like '" + input + "%'" + "' OR full_address like '" + input + "%'";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                GeographyCode code = read(results);
                codes.add(code);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return codes;
    }

    public static List<String> readAddressLike(String input) {
        List<String> addresses = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql;
            if (input == null || input.trim().isBlank()) {
                sql = "SELECT * FROM Geography_Code";
            } else {
                sql = "SELECT * FROM Geography_Code WHERE "
                        + " address like '" + input + "%'" + "' OR full_address like '" + input + "%'";
            }
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                addresses.add(results.getString("address"));
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return addresses;
    }

    public static GeographyCode read(ResultSet results) {
        if (results == null) {
            return null;
        }
        try {
            GeographyCode code = new GeographyCode();
            code.setLongitude(results.getDouble("longitude"));
            code.setLatitude(results.getDouble("latitude"));
            code.setAddress(results.getString("address"));
            code.setFullAddress(results.getString("full_address"));
            code.setCountry(results.getString("country"));
            code.setProvince(results.getString("province"));
            code.setCity(results.getString("city"));
            code.setCitycode(results.getString("citycode"));
            code.setDistrict(results.getString("district"));
            code.setTownship(results.getString("township"));
            code.setNeighborhood(results.getString("neighborhood"));
            code.setBuilding(results.getString("building"));
            code.setAdministrativeCode(results.getString("administrative_code"));
            code.setStreet(results.getString("street"));
            code.setNumber(results.getString("number"));
            code.setLevel(results.getString("level"));
            return code;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return null;
        }
    }

    public static boolean write(GeographyCode code) {
        if (code == null) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT address FROM Geography_Code WHERE "
                    + "address='" + code.getAddress() + "' OR full_address='" + code.getAddress() + "'";
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                update(statement, code);
            } else {
                create(statement, code);
            }
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean write(List<GeographyCode> codes) {
        if (codes == null || codes.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            conn.setAutoCommit(false);
            for (GeographyCode code : codes) {
                String sql = "SELECT address FROM Geography_Code WHERE "
                        + "address='" + code.getAddress() + "' OR full_address='" + code.getAddress() + "'";
                boolean exist;
                try ( ResultSet results = statement.executeQuery(sql)) {
                    exist = results.next();
                }
                if (exist) {
                    update(statement, code);
                } else {
                    create(statement, code);
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean create(Statement statement, GeographyCode code) {
        if (statement == null || code == null
                || code.getAddress() == null || code.getAddress().isBlank()) {
            return false;
        }
        try {
            String sql = "INSERT INTO Geography_Code(longitude, latitude, address, full_address,"
                    + " country, province, city, citycode, district, township,  neighborhood, building,"
                    + " administrative_code, street, number, level) VALUES(";
            sql += code.getLongitude() + ", " + code.getLatitude() + ", '" + code.getAddress() + "', ";
            if (code.getFullAddress() != null) {
                sql += "'" + code.getFullAddress() + "', ";
            } else {
                sql += "null, ";
            }
            if (code.getCountry() != null) {
                sql += "'" + code.getCountry() + "', ";
            } else {
                sql += "null, ";
            }
            if (code.getProvince() != null) {
                sql += "'" + code.getProvince() + "', ";
            } else {
                sql += "null, ";
            }
            if (code.getCity() != null) {
                sql += "'" + code.getCity() + "', ";
            } else {
                sql += "null, ";
            }
            if (code.getCitycode() != null) {
                sql += "'" + code.getCitycode() + "', ";
            } else {
                sql += "null, ";
            }
            if (code.getDistrict() != null) {
                sql += "'" + code.getDistrict() + "', ";
            } else {
                sql += "null, ";
            }
            if (code.getTownship() != null) {
                sql += "'" + code.getTownship() + "', ";
            } else {
                sql += "null, ";
            }
            if (code.getNeighborhood() != null) {
                sql += "'" + code.getNeighborhood() + "', ";
            } else {
                sql += "null, ";
            }
            if (code.getBuilding() != null) {
                sql += "'" + code.getBuilding() + "', ";
            } else {
                sql += "null, ";
            }
            if (code.getAdministrativeCode() != null) {
                sql += "'" + code.getAdministrativeCode() + "', ";
            } else {
                sql += "null, ";
            }
            if (code.getStreet() != null) {
                sql += "'" + code.getStreet() + "', ";
            } else {
                sql += "null, ";
            }
            if (code.getNumber() != null) {
                sql += "'" + code.getNumber() + "', ";
            } else {
                sql += "null, ";
            }
            if (code.getLevel() != null) {
                sql += "'" + code.getLevel() + "' ";
            } else {
                sql += "null  ";
            }
            sql += " )";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean update(Statement statement, GeographyCode code) {
        if (statement == null || code == null) {
            return false;
        }
        try {
            String sql = "UPDATE Geography_Code SET ";
            sql += "longitude=" + code.getLongitude() + ", latitude=" + code.getLatitude() + ", ";
            if (code.getFullAddress() != null) {
                sql += "full_address='" + code.getFullAddress() + "', ";
            } else {
                sql += "full_address=null, ";
            }
            if (code.getCountry() != null) {
                sql += "country='" + code.getCountry() + "', ";
            } else {
                sql += "country=null, ";
            }
            if (code.getProvince() != null) {
                sql += "province='" + code.getProvince() + "', ";
            } else {
                sql += "province=null, ";
            }
            if (code.getCity() != null) {
                sql += "city='" + code.getCity() + "', ";
            } else {
                sql += "city=null, ";
            }
            if (code.getCitycode() != null) {
                sql += "citycode='" + code.getCitycode() + "', ";
            } else {
                sql += "citycode=null, ";
            }
            if (code.getDistrict() != null) {
                sql += "district='" + code.getDistrict() + "', ";
            } else {
                sql += "district=null, ";
            }
            if (code.getTownship() != null) {
                sql += "township='" + code.getTownship() + "', ";
            } else {
                sql += "township=null, ";
            }
            if (code.getNeighborhood() != null) {
                sql += "neighborhood='" + code.getNeighborhood() + "', ";
            } else {
                sql += "neighborhood=null, ";
            }
            if (code.getBuilding() != null) {
                sql += "building='" + code.getBuilding() + "', ";
            } else {
                sql += "building=null, ";
            }
            if (code.getAdministrativeCode() != null) {
                sql += "administrative_code='" + code.getAdministrativeCode() + "', ";
            } else {
                sql += "administrative_code=null, ";
            }
            if (code.getStreet() != null) {
                sql += "street='" + code.getStreet() + "', ";
            } else {
                sql += "street=null, ";
            }
            if (code.getNumber() != null) {
                sql += "number='" + code.getNumber() + "', ";
            } else {
                sql += "number=null, ";
            }
            if (code.getLevel() != null) {
                sql += "level='" + code.getLevel() + "' ";
            } else {
                sql += "level=null  ";
            }
            sql += "WHERE address='" + code.getAddress() + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(double longitude, double latitude) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM Geography_Code WHERE "
                    + "longitude=" + longitude + " AND latitude=" + latitude;
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(String address) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM Geography_Code WHERE address='" + address
                    + "' OR full_address='" + address + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(List<GeographyCode> codes) {
        if (codes == null || codes.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String inStr = "( '" + codes.get(0).getAddress() + "'";
            for (int i = 1; i < codes.size(); ++i) {
                inStr += ", '" + codes.get(i).getAddress() + "'";
                if (codes.get(i).getFullAddress() != null) {
                    inStr += ", '" + codes.get(i).getFullAddress() + "'";
                }
            }
            inStr += " )";
            String sql = "DELETE FROM Geography_Code WHERE address IN " + inStr;
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean migrate() {
        int size = TableGeographyCode.size();
        if (size > 0) {
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                     Statement statement = conn.createStatement()) {
                String sql = "UPDATE Geography_Code SET level='" + message("City")
                        + "' WHERE level IS NULL";
                statement.executeUpdate(sql);

            } catch (Exception e) {
                logger.debug(e.toString());
                failed(e);
                return false;
            }

            File tmpFile = new File(AppVariables.MyboxDataPath + File.separator + "data"
                    + File.separator + "Geography_Code" + (new Date().getTime()) + ".del");
            tmpFile.mkdirs();
            DerbyBase.exportData("Geography_Code", tmpFile.getAbsolutePath());
            AppVariables.setSystemConfigValue("GeographyCodeBackup6.1.5", tmpFile.getAbsolutePath());

        }
        new TableGeographyCode().drop();
        new TableGeographyCode().init();

        return true;
    }

}
