package mara.mybox.controller;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import mara.mybox.data.CoordinateSystem;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.GeographyCodeLevel;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.TableBase;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2020-03-28
 * @License Apache License Version 2.0
 */
public class GeographyCodeImportGeonamesFileController extends DataImportController {

    public GeographyCodeImportGeonamesFileController() {
        baseTitle = AppVariables.message("ImportGeographyCodeGeonamesFormat");
    }

    @Override
    public void setLink() {
        link.setText("http://download.geonames.org/export/zip/");
    }

    @Override
    public TableBase getTableDefinition() {
        if (tableDefinition == null) {
            tableDefinition = new TableGeographyCode();
        }
        return tableDefinition;
    }

    //http://download.geonames.org/export/zip/
    // country	postal	place	state	stateCode	city	cityCode	county	countyCode	latitude	longitude 	accuracy
    @Override
    public long importFile(File file) {
        long importCount = 0, insertCount = 0, updateCount = 0, skipCount = 0, failedCount = 0;
        try ( CSVParser parser = CSVParser.parse(file, StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.withDelimiter('\t').withTrim().withNullString(""))) {
            GeographyCode code, countryCode = null, provinceCode = null, cityCode = null, countyCode = null;
            String lastCountry = null, lastProvince = null, lastCity = null, lastCounty = null;
            String sql;
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                     PreparedStatement insert = conn.prepareStatement(TableGeographyCode.Insert);
                     PreparedStatement update = conn.prepareStatement(TableGeographyCode.Update)) {
                conn.setAutoCommit(false);
                for (CSVRecord record : parser) {
                    if (task == null || task.isCancelled()) {
                        updateLogs("Canceled", true);
                        return importCount;
                    }
                    String country = record.get(0);
                    if (!country.equals(lastCountry)) {
                        sql = "SELECT * FROM Geography_Code WHERE "
                                + " level=3 AND code2='" + country + "'";
                        countryCode = TableGeographyCode.queryCode(conn, sql, false);
                    }
                    lastCountry = country;
                    if (countryCode == null) {
                        failedCount++;
                        updateLogs("Unknown country code:" + country, true);
                        continue;
                    }
                    String name = countryCode.getName();
                    String place = record.get(2);
                    if (place == null) {
                        failedCount++;
                        updateLogs("null pance:" + country, true);
                        continue;
                    }
                    try {
                        code = new GeographyCode();
                        code.setContinent(countryCode.getContinent());
                        code.setCountry(countryCode.getGcid());
                        code.setEnglishName(place);
                        code.setCode1(record.get(1));
                        code.setCoordinateSystem(CoordinateSystem.WGS84());
                        String province = record.get(3);
                        String city = record.get(5);
                        String county = record.get(7);
                        double longitude = Double.valueOf(record.get(10));
                        double latitude = Double.valueOf(record.get(9));
                        int level;
                        if (county != null) {
                            level = 7;
                        } else if (city != null) {
                            level = 6;
                        } else if (province != null) {
                            level = 5;
                        } else {
                            level = 4;
                        }
                        code.setLevelCode(new GeographyCodeLevel(level));
                        code.setLatitude(latitude);
                        code.setLongitude(longitude);

                        if (province != null) {
                            if (!province.equals(lastProvince)) {
                                sql = "SELECT * FROM Geography_Code WHERE "
                                        + " level=4 AND country=" + countryCode.getGcid() + " AND "
                                        + "  ( " + TableGeographyCode.nameEqual(province) + " )";
                                provinceCode = TableGeographyCode.queryCode(conn, sql, false);
                            }
                            if (provinceCode == null) {
                                provinceCode = new GeographyCode();
                                provinceCode.setSource(GeographyCode.AddressSource.Geonames);
                                provinceCode.setContinent(countryCode.getContinent());
                                provinceCode.setCountry(countryCode.getGcid());
                                provinceCode.setEnglishName(province);
                                provinceCode.setCode1(record.get(4));
                                provinceCode.setLevelCode(new GeographyCodeLevel(4));
                                provinceCode.setLatitude(latitude);
                                provinceCode.setLongitude(longitude);
                                provinceCode.setCoordinateSystem(CoordinateSystem.WGS84());
                                if (TableGeographyCode.insert(conn, insert, provinceCode)) {
                                    insertCount++;
                                    importCount++;
                                    updateLogs(message("Insert") + ": " + insertCount + " "
                                            + provinceCode.getLevelCode().getName() + " \"" + province
                                            + "\" " + code.getLongitude() + "," + code.getLatitude(), true);
                                } else {
                                    ++failedCount;
                                    updateLogs(message("Failed") + ": " + failedCount + " "
                                            + provinceCode.getLevelCode().getName() + " \"" + province
                                            + "\" " + code.getLongitude() + "," + code.getLatitude(), true);
                                    continue;
                                }
                            }
                            code.setProvince(provinceCode.getGcid());
                            name += " - " + province;
                        } else {
                            provinceCode = null;
                        }
                        lastProvince = province;

                        if (city != null) {
                            if (!city.equals(lastCity)) {
                                sql = "SELECT * FROM Geography_Code WHERE "
                                        + " level=5 AND country=" + countryCode.getGcid() + " AND ";
                                if (provinceCode != null) {
                                    sql += " province=" + provinceCode.getGcid() + " AND ";
                                }
                                sql += " ( " + TableGeographyCode.nameEqual(city) + " )";
                                cityCode = TableGeographyCode.queryCode(conn, sql, false);
                            }
                            if (cityCode == null) {
                                cityCode = new GeographyCode();
                                cityCode.setSource(GeographyCode.AddressSource.Geonames);
                                cityCode.setContinent(countryCode.getContinent());
                                cityCode.setCountry(countryCode.getGcid());
                                if (provinceCode != null) {
                                    cityCode.setProvince(provinceCode.getGcid());
                                }
                                cityCode.setEnglishName(city);
                                cityCode.setCode1(record.get(6));
                                cityCode.setLevelCode(new GeographyCodeLevel(5));
                                cityCode.setLatitude(latitude);
                                cityCode.setLongitude(longitude);
                                cityCode.setCoordinateSystem(CoordinateSystem.WGS84());
                                if (TableGeographyCode.insert(conn, insert, cityCode)) {
                                    insertCount++;
                                    importCount++;
                                    updateLogs(message("Insert") + ": " + insertCount + " "
                                            + cityCode.getLevelCode().getName() + " \"" + city
                                            + "\" " + code.getLongitude() + "," + code.getLatitude(), true);
                                } else {
                                    ++failedCount;
                                    updateLogs(message("Failed") + ": " + failedCount + " "
                                            + cityCode.getLevelCode().getName() + " \"" + city
                                            + "\" " + code.getLongitude() + "," + code.getLatitude(), true);
                                    continue;
                                }
                            }
                            code.setCity(cityCode.getGcid());
                            name += " - " + city;
                        } else {
                            cityCode = null;
                        }
                        lastCity = city;

                        if (county != null) {
                            if (!county.equals(lastCounty)) {
                                sql = "SELECT * FROM Geography_Code WHERE "
                                        + " level=6 AND country=" + countryCode.getGcid() + " AND ";
                                if (provinceCode != null) {
                                    sql += " province=" + provinceCode.getGcid() + " AND ";
                                }
                                if (cityCode != null) {
                                    sql += " city=" + cityCode.getGcid() + " AND ";
                                }
                                sql += " ( " + TableGeographyCode.nameEqual(county) + " )";
                                countyCode = TableGeographyCode.queryCode(conn, sql, false);
                            }
                            if (countyCode == null) {
                                countyCode = new GeographyCode();
                                countyCode.setSource(GeographyCode.AddressSource.Geonames);
                                countyCode.setContinent(countryCode.getContinent());
                                countyCode.setCountry(countryCode.getGcid());
                                if (provinceCode != null) {
                                    countyCode.setProvince(provinceCode.getGcid());
                                }
                                if (cityCode != null) {
                                    countyCode.setCity(cityCode.getGcid());
                                }
                                countyCode.setEnglishName(county);
                                countyCode.setCode1(record.get(8));
                                countyCode.setLevel(6);
                                countyCode.setLatitude(latitude);
                                countyCode.setLongitude(longitude);
                                countyCode.setCoordinateSystem(CoordinateSystem.WGS84());
                                if (TableGeographyCode.insert(conn, insert, countyCode)) {
                                    insertCount++;
                                    importCount++;
                                    updateLogs(message("Insert") + ": " + insertCount + " "
                                            + countyCode.getLevelCode().getName() + " \"" + county
                                            + "\" " + code.getLongitude() + "," + code.getLatitude(), true);
                                } else {
                                    ++failedCount;
                                    updateLogs(message("Failed") + ": " + failedCount + " "
                                            + countyCode.getLevelCode().getName() + " \"" + county
                                            + "\" " + code.getLongitude() + "," + code.getLatitude(), true);
                                    continue;
                                }
                            }
                            code.setCounty(countyCode.getGcid());
                            name += " - " + county;
                        } else {
                            countyCode = null;
                        }
                        lastCounty = county;

                        name += " - " + place;

                        GeographyCode exist = TableGeographyCode.readCode(conn, code, false);
                        if (exist != null) {
                            if (replaceCheck.isSelected()) {
                                code.setGcid(exist.getGcid());
                                if (TableGeographyCode.update(conn, update, code)) {
                                    updateCount++;
                                    importCount++;
                                    if (verboseCheck.isSelected() || (importCount % 100 == 0)) {
                                        updateLogs(message("Update") + ": " + updateCount + " "
                                                + code.getLevelCode().getName() + " \"" + name
                                                + "\" " + code.getLongitude() + "," + code.getLatitude(), true);
                                    }
                                } else {
                                    ++failedCount;
                                    updateLogs(message("Failed") + ": " + failedCount + " "
                                            + code.getLevelCode().getName() + " \"" + name
                                            + "\" " + code.getLongitude() + "," + code.getLatitude(), true);
                                }
                            } else {
                                skipCount++;
                                if (verboseCheck.isSelected() || (skipCount % 100 == 0)) {
                                    updateLogs(message("Skip") + ": " + skipCount + " "
                                            + code.getLevelCode().getName() + " \"" + name
                                            + "\" " + code.getLongitude() + "," + code.getLatitude(), true);
                                }
                            }
                        } else {
                            code.setSource(GeographyCode.AddressSource.Geonames);
                            if (TableGeographyCode.insert(conn, insert, code)) {
                                insertCount++;
                                importCount++;
                                if (verboseCheck.isSelected() || (importCount % 100 == 0)) {
                                    updateLogs(message("Insert") + ": " + insertCount + " "
                                            + code.getLevelCode().getName() + " \"" + name
                                            + "\" " + code.getLongitude() + "," + code.getLatitude(), true);
                                }
                            } else {
                                ++failedCount;
                                updateLogs(message("Failed") + ": " + failedCount + " "
                                        + code.getLevelCode().getName() + " \"" + name
                                        + "\" " + code.getLongitude() + "," + code.getLatitude(), true);
                            }
                        }
                    } catch (Exception e) {
                        updateLogs(e.toString() + " " + place + " " + name, true);
                    }
                }
                conn.commit();
            }
        } catch (Exception e) {
            updateLogs(e.toString(), true);
        }
        updateLogs(message("Imported") + ":" + importCount + "  " + file + "\n"
                + message("Insert") + ":" + insertCount + " "
                + message("Update") + ":" + updateCount + " "
                + message("FailedCount") + ":" + failedCount + " "
                + message("Skipped") + ":" + skipCount, true);
        return importCount;
    }

}
