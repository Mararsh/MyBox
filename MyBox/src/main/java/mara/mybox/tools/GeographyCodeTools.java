package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.xml.parsers.DocumentBuilderFactory;
import mara.mybox.controller.LoadingController;
import mara.mybox.data.CoordinateSystem;
import static mara.mybox.data.CoordinateSystem.Value.GCJ_02;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.GeographyCodeLevel;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.tools.NetworkTools.trustAllManager;
import static mara.mybox.tools.NetworkTools.trustAllVerifier;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import thridparty.CoordinateConverter;

/**
 * @Author Mara
 * @CreateDate 2020-8-11
 * @License Apache License Version 2.0
 */
public class GeographyCodeTools {

    /*
        Query codes by web service
     */
    public static GeographyCode geoCode(CoordinateSystem coordinateSystem, String address) {
        try {
            if (coordinateSystem == null) {
                return null;
            }
            if (coordinateSystem.getValue() == CoordinateSystem.Value.GCJ_02) {
                // GaoDe Map only supports info codes of China
                String urlString = "https://restapi.amap.com/v3/geocode/geo?address="
                        + URLEncoder.encode(address, "UTF-8") + "&output=xml&key="
                        + AppVariables.getUserConfigValue("GaoDeMapServiceKey", CommonValues.GaoDeMapServiceKey);
                GeographyCode geographyCode = new GeographyCode();
                geographyCode.setChineseName(address);
                geographyCode.setCoordinateSystem(coordinateSystem);
                return gaodeCode(urlString, geographyCode);
            } else if (coordinateSystem.getValue() == CoordinateSystem.Value.CGCS2000) {
                //  http://api.tianditu.gov.cn/geocoder?ds={"keyWord":"泰山"}&tk=0ddeb917def62b4691500526cc30a9b1
                String urlString = "http://api.tianditu.gov.cn/geocoder?ds="
                        + URLEncoder.encode("{\"keyWord\":\""
                                + address
                                + "\"}", "UTF-8") + "&tk="
                        + AppVariables.getUserConfigValue("TianDiTuWebKey", CommonValues.TianDiTuWebKey);
                URL url = new URL(urlString);
                File jsonFile = FileTools.getTempFile(".json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                connection.connect();
                try (final BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                        final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(jsonFile))) {
                    byte[] buf = new byte[CommonValues.IOBufferLength];
                    int len;
                    while ((len = inStream.read(buf)) != -1) {
                        outputStream.write(buf, 0, len);
                    }
                }
                String data = FileTools.readTexts(jsonFile);
                double longitude = -200;
                double latitude = -200;
                String flag = "\"lon\":";
                int pos = data.indexOf(flag);
                if (pos >= 0) {
                    String subData = data.substring(pos + flag.length());
                    flag = ",";
                    pos = subData.indexOf(flag);
                    if (pos >= 0) {
                        try {
                            longitude = Double.parseDouble(subData.substring(0, pos));
                        } catch (Exception e) {
                        }
                    }
                }
                flag = "\"lat\":";
                pos = data.indexOf(flag);
                if (pos >= 0) {
                    String subData = data.substring(pos + flag.length());
                    flag = ",";
                    pos = subData.indexOf(flag);
                    if (pos >= 0) {
                        try {
                            latitude = Double.parseDouble(subData.substring(0, pos));
                        } catch (Exception e) {
                        }
                    }
                }
                if (longitude >= -180 && latitude >= -180) {
                    return GeographyCodeTools.geoCode(coordinateSystem, longitude, latitude, true);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static GeographyCode geoCode(CoordinateSystem coordinateSystem, double longitude, double latitude, boolean decodeAncestors) {
        try {
            GeographyCode geographyCode = TableGeographyCode.readCode(coordinateSystem, longitude, latitude, decodeAncestors);
            if (geographyCode != null) {
                return geographyCode;
            }
            return GeographyCodeTools.geoCode(coordinateSystem, longitude, latitude);
        } catch (Exception e) {
            return null;
        }
    }

    public static GeographyCode geoCode(CoordinateSystem coordinateSystem, double longitude, double latitude) {
        try {
            if (coordinateSystem == null) {
                return null;
            }
            if (coordinateSystem.getValue() == CoordinateSystem.Value.GCJ_02) {
                String urlString = "https://restapi.amap.com/v3/geocode/regeo?location="
                        + longitude + "," + latitude + "&output=xml&key="
                        + AppVariables.getUserConfigValue("GaoDeMapServiceKey", CommonValues.GaoDeMapServiceKey);
                GeographyCode geographyCode = new GeographyCode();
                geographyCode.setLongitude(longitude);
                geographyCode.setLatitude(latitude);
                geographyCode.setCoordinateSystem(coordinateSystem);
                return gaodeCode(urlString, geographyCode);
            } else if (coordinateSystem.getValue() == CoordinateSystem.Value.CGCS2000) {
                String urlString = "http://api.tianditu.gov.cn/geocoder?postStr={'lon':"
                        + longitude + ",'lat':" + latitude
                        + ",'ver':1}&type=geocode&tk="
                        + AppVariables.getUserConfigValue("TianDiTuWebKey", CommonValues.TianDiTuWebKey);
                GeographyCode geographyCode = new GeographyCode();
                geographyCode.setLongitude(longitude);
                geographyCode.setLatitude(latitude);
                geographyCode.setCoordinateSystem(coordinateSystem);
                return tiandituCode(urlString, geographyCode);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    //{"result":{"formatted_address":"泰安市泰山区升平街39号（泰山区区政府大院内）泰山区统计局",
    // "location":{"lon":117.12899999995,"lat":36.19280999998},
    // "addressComponent":{"address":"泰山区升平街39号（泰山区区政府大院内）","city":"泰安市",
    // "county_code":"156370902","nation":"中国","poi_position":"西北","county":"泰山区","city_code":"156370900",
    // "address_position":"西北","poi":"泰山区统计局","province_code":"156370000","province":"山东省","road":"运粮街",
    //"road_distance":65,"poi_distance":10,"address_distance":10}},"msg":"ok","status":"0"}
    public static GeographyCode tiandituCode(String urlString, GeographyCode geographyCode) {
        try {
            URL url = new URL(urlString);
            File jsonFile = FileTools.getTempFile(".json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            connection.connect();
            try (final BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                    final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(jsonFile))) {
                byte[] buf = new byte[CommonValues.IOBufferLength];
                int len;
                while ((len = inStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }
            }
            String data = FileTools.readTexts(jsonFile);
//            logger.debug(data);
            String flag = "\"formatted_address\":\"";
            int pos = data.indexOf(flag);
            if (pos >= 0) {
                String subData = data.substring(pos + flag.length());
                flag = "\"";
                pos = subData.indexOf(flag);
                if (pos >= 0) {
                    String v = subData.substring(0, pos);
                    if (geographyCode.getChineseName() == null) {
                        geographyCode.setChineseName(v);
                    } else {
                        geographyCode.setAlias1(v);
                    }
                }
            }
            flag = "\"lon\":";
            pos = data.indexOf(flag);
            if (pos >= 0) {
                String subData = data.substring(pos + flag.length());
                flag = ",";
                pos = subData.indexOf(flag);
                if (pos >= 0) {
                    try {
                        double longitude = Double.parseDouble(subData.substring(0, pos));
                        geographyCode.setLongitude(longitude);
                    } catch (Exception e) {
                    }
                }
            }
            flag = "\"lat\":";
            pos = data.indexOf(flag);
            if (pos >= 0) {
                String subData = data.substring(pos + flag.length());
                flag = "}";
                pos = subData.indexOf(flag);
                if (pos >= 0) {
                    try {
                        double latitude = Double.parseDouble(subData.substring(0, pos));
                        geographyCode.setLatitude(latitude);
                    } catch (Exception e) {
                    }
                }
            }
            flag = "\"address\":\"";
            pos = data.indexOf(flag);
            if (pos >= 0) {
                String subData = data.substring(pos + flag.length());
                flag = "\"";
                pos = subData.indexOf(flag);
                if (pos >= 0) {
                    String name = subData.substring(0, pos);
                    if (geographyCode.getChineseName() == null) {
                        geographyCode.setChineseName(name);
                    } else if (!geographyCode.getChineseName().equals(name)) {
                        geographyCode.setAlias1(geographyCode.getChineseName());
                        geographyCode.setChineseName(name);
                    }
                }
            }
            flag = "\"city\":\"";
            pos = data.indexOf(flag);
            if (pos >= 0) {
                String subData = data.substring(pos + flag.length());
                flag = "\"";
                pos = subData.indexOf(flag);
                if (pos >= 0) {
                    geographyCode.setCityName(subData.substring(0, pos));
                }
            }
            flag = "\"county_code\":\"";
            pos = data.indexOf(flag);
            if (pos >= 0) {
                String subData = data.substring(pos + flag.length());
                flag = "\"";
                pos = subData.indexOf(flag);
                if (pos >= 0) {
                    geographyCode.setCode3(subData.substring(0, pos));
                }
            }
            flag = "\"nation\":\"";
            pos = data.indexOf(flag);
            if (pos >= 0) {
                String subData = data.substring(pos + flag.length());
                flag = "\"";
                pos = subData.indexOf(flag);
                if (pos >= 0) {
                    geographyCode.setCountryName(subData.substring(0, pos));
                }
            }
            flag = "\"county\":\"";
            pos = data.indexOf(flag);
            if (pos >= 0) {
                String subData = data.substring(pos + flag.length());
                flag = "\"";
                pos = subData.indexOf(flag);
                if (pos >= 0) {
                    geographyCode.setCountyName(subData.substring(0, pos));
                }
            }
            flag = "\"city_code\":\"";
            pos = data.indexOf(flag);
            if (pos >= 0) {
                String subData = data.substring(pos + flag.length());
                flag = "\"";
                pos = subData.indexOf(flag);
                if (pos >= 0) {
                    geographyCode.setCode2(subData.substring(0, pos));
                }
            }
            flag = "\"poi\":\"";
            pos = data.indexOf(flag);
            if (pos >= 0) {
                String subData = data.substring(pos + flag.length());
                flag = "\"";
                pos = subData.indexOf(flag);
                if (pos >= 0) {
                    String name = subData.substring(0, pos);
                    if (geographyCode.getChineseName() == null) {
                        geographyCode.setChineseName(name);
                    } else if (!geographyCode.getChineseName().equals(name)) {
                        if (geographyCode.getAlias1() == null) {
                            geographyCode.setAlias1(geographyCode.getChineseName());
                            geographyCode.setChineseName(name);
                        } else if (!geographyCode.getAlias1().equals(name)) {
                            if (geographyCode.getAlias2() == null) {
                                geographyCode.setAlias2(geographyCode.getChineseName());
                                geographyCode.setChineseName(name);
                            } else if (!geographyCode.getAlias2().equals(name)) {
                                if (geographyCode.getAlias3() == null) {
                                    geographyCode.setAlias3(geographyCode.getChineseName());
                                    geographyCode.setChineseName(name);
                                } else if (!geographyCode.getAlias3().equals(name)) {
                                    if (geographyCode.getAlias4() == null) {
                                        geographyCode.setAlias4(geographyCode.getChineseName());
                                        geographyCode.setChineseName(name);
                                    } else if (!geographyCode.getAlias4().equals(name)) {
                                        geographyCode.setAlias5(geographyCode.getChineseName());
                                        geographyCode.setChineseName(name);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            flag = "\"province_code\":\"";
            pos = data.indexOf(flag);
            if (pos >= 0) {
                String subData = data.substring(pos + flag.length());
                flag = "\"";
                pos = subData.indexOf(flag);
                if (pos >= 0) {
                    geographyCode.setCode1(subData.substring(0, pos));
                }
            }
            flag = "\"province\":\"";
            pos = data.indexOf(flag);
            if (pos >= 0) {
                String subData = data.substring(pos + flag.length());
                flag = "\"";
                pos = subData.indexOf(flag);
                if (pos >= 0) {
                    geographyCode.setProvinceName(subData.substring(0, pos));
                }
            }
            flag = "\"road\":\"";
            pos = data.indexOf(flag);
            if (pos >= 0) {
                String subData = data.substring(pos + flag.length());
                flag = "\"";
                pos = subData.indexOf(flag);
                if (pos >= 0) {
                    geographyCode.setVillageName(subData.substring(0, pos));
                }
            }
            if (geographyCode.getLongitude() < -180 || geographyCode.getLongitude() > 180
                    || geographyCode.getLatitude() < -90 && geographyCode.getLatitude() > 90) {
                return null;
            }
            return geographyCode;
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
            return null;
        }
    }

    // {"status":"1","info":"OK","infocode":"10000","count":"1","geocodes":
    // [{"formatted_address":"山东省泰安市泰山区泰山","country":"中国","province":"山东省","citycode":"0538",
    //"city":"泰安市","county":"泰山区","township":[],
    //"village":{"name":[],"type":[]},
    //"building":{"name":[],"type":[]},
    //"adcode":"370902","town":[],"number":[],"location":"117.157242,36.164988","levelCode":"兴趣点"}]}
    public static GeographyCode gaodeCode(String urlString, GeographyCode geographyCode) {
        try {
            URL url = new URL(urlString);
            File xmlFile = FileTools.getTempFile(".xml");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            SSLContext sc = SSLContext.getInstance(CommonValues.HttpsProtocal);
            sc.init(null, trustAllManager(), new SecureRandom());
            connection.setSSLSocketFactory(sc.getSocketFactory());
            connection.setHostnameVerifier(trustAllVerifier());
            connection.connect();
            try (final BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                    final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(xmlFile))) {
                byte[] buf = new byte[CommonValues.IOBufferLength];
                int len;
                while ((len = inStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }
            }
//            logger.debug(FileTools.readTexts(xmlFile));
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
            NodeList nodes = doc.getElementsByTagName("formatted_address");
            if (nodes != null && nodes.getLength() > 0) {
                String v = nodes.item(0).getTextContent();
                if (geographyCode.getChineseName() == null) {
                    geographyCode.setChineseName(v);
                } else {
                    geographyCode.setAlias1(v);
                }
            }
            nodes = doc.getElementsByTagName("country");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setCountryName(nodes.item(0).getTextContent());
            }
            nodes = doc.getElementsByTagName("province");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setProvinceName(nodes.item(0).getTextContent());
            }
            nodes = doc.getElementsByTagName("citycode");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setCode2(nodes.item(0).getTextContent());
            }
            nodes = doc.getElementsByTagName("city");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setCityName(nodes.item(0).getTextContent());
            }
            nodes = doc.getElementsByTagName("district");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setCountyName(nodes.item(0).getTextContent());
            }
            nodes = doc.getElementsByTagName("township");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setTownName(nodes.item(0).getTextContent());
            }
            nodes = doc.getElementsByTagName("neighborhood");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setVillageName(nodes.item(0).getFirstChild().getTextContent());
            }
            nodes = doc.getElementsByTagName("building");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setBuildingName(nodes.item(0).getTextContent());
            }
            nodes = doc.getElementsByTagName("adcode");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setCode1(nodes.item(0).getTextContent());
            }
            nodes = doc.getElementsByTagName("streetNumber");
            if (nodes != null && nodes.getLength() > 0) {
                nodes = nodes.item(0).getChildNodes();
                if (nodes != null) {
                    Map<String, String> values = new HashMap<>();
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Node node = nodes.item(i);
                        values.put(node.getNodeName(), node.getTextContent());
                    }
                    String s = "";
                    String v = values.get("street");
                    if (v != null) {
                        s += v;
                    }
                    v = values.get("number");
                    if (v != null) {
                        s += v;
                    }
                    v = values.get("location");
                    if (v != null) {
                        s += v;
                    }
                    v = values.get("direction");
                    if (v != null) {
                        s += v;
                    }
                    v = values.get("distance");
                    if (v != null) {
                        s += v;
                    }
                    if (geographyCode.getBuildingName() == null) {
                        geographyCode.setBuildingName(s);
                    } else if (geographyCode.getAlias1() == null) {
                        geographyCode.setAlias1(s);
                    }
                }
            } else {
                nodes = doc.getElementsByTagName("street");
                if (nodes != null && nodes.getLength() > 0) {
                    String s = nodes.item(0).getTextContent();
                    if (geographyCode.getBuildingName() == null) {
                        geographyCode.setBuildingName(s);
                    } else if (geographyCode.getAlias1() == null) {
                        geographyCode.setAlias1(s);
                    } else if (geographyCode.getAlias2() == null) {
                        geographyCode.setAlias2(s);
                    }
                }
                nodes = doc.getElementsByTagName("number");
                if (nodes != null && nodes.getLength() > 0) {
                    geographyCode.setCode5(nodes.item(0).getTextContent());
                }
                if (geographyCode.getLongitude() < -180) {
                    nodes = doc.getElementsByTagName("location");
                    if (nodes != null && nodes.getLength() > 0) {
                        String[] values = nodes.item(0).getFirstChild().getTextContent().split(",");
                        geographyCode.setLongitude(Double.valueOf(values[0].trim()));
                        geographyCode.setLatitude(Double.valueOf(values[1].trim()));
                    } else {
                        geographyCode.setLongitude(-200);
                        geographyCode.setLatitude(-200);
                    }
                }
                if (geographyCode.getChineseName() == null) {
                    geographyCode.setChineseName(geographyCode.getLongitude() + "," + geographyCode.getLatitude());
                }
                nodes = doc.getElementsByTagName("level");
                if (nodes != null && nodes.getLength() > 0) {
                    String v = nodes.item(0).getTextContent();
                    if (message("zh", "Country").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("Country"));
                    } else if (message("zh", "Province").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("Province"));
                    } else if (message("zh", "City").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("City"));
                    } else if (message("zh", "County").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("County"));
                    } else if (message("zh", "Town").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("Town"));
                    } else if (message("zh", "Neighborhood").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("Village"));
                    } else if (message("zh", "PointOfInterest").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("Point Of Interest"));
                    } else if (message("zh", "Street").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("Village"));
                    } else if (message("zh", "Building").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("Building"));
                    } else {
                        geographyCode.setLevelCode(new GeographyCodeLevel("Point Of Interest"));
                    }
                }
            }
            if (!geographyCode.validCoordinate()) {
                return null;
            }
            return geographyCode;
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
            return null;
        }
    }

    public static GeographyCode encode(GeographyCode code) {
        if (code == null) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 PreparedStatement geoInsert = conn.prepareStatement(TableGeographyCode.Insert)) {
            Map<String, Object> codeRet = encode(conn, geoInsert, code.getLevel(),
                    code.getLongitude(), code.getLatitude(),
                    code.getContinentName(), code.getCountryName(),
                    code.getProvinceName(), code.getCityName(), code.getCountyName(),
                    code.getTownName(), code.getVillageName(), code.getBuildingName(),
                    code.getName(), true, false);
            conn.commit();
//            if (codeRet.get("message") != null) {
//                logger.error((String) codeRet.get("message"));
//            }
            if (codeRet.get("code") != null) {
                GeographyCode encoded = (GeographyCode) codeRet.get("code");
                encoded = TableGeographyCode.readCode(conn, encoded.getGcid(), true);
                if (code.getGcid() < 0) {
                    code.setGcid(encoded.getGcid());
                }
                code.setOwnerCode(encoded.getOwnerCode());
                code.setContinentCode(encoded.getContinentCode());
                code.setCountryCode(encoded.getCountryCode());
                code.setCityCode(encoded.getCityCode());
                code.setTownCode(encoded.getTownCode());
                code.setCountyCode(encoded.getCountyCode());
                code.setVillageCode(encoded.getVillageCode());
                code.setBuildingCode(encoded.getBuildingCode());
                return code;
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return null;
    }

    // !! Caller responses for committing the update
    public static Map<String, Object> encode(Connection conn, PreparedStatement geoInsert,
            int level, double longitude, double latitude, String continent, String country, String province,
            String city, String county, String town, String village, String building, String poi,
            boolean create, boolean decodeAncestors) {
        Map<String, Object> ret = new HashMap<>();
        try {
            String msg = "";
            if (conn == null) {
                ret.put("message", "conn is null");
                return ret;
            }
            String sql;
            GeographyCode earch = TableGeographyCode.earth(conn);
            if (level == 1) {
                if (earch == null) {
                    importPredefined(conn, null);
                    earch = TableGeographyCode.earth(conn);
                }
                earch.setOwnerCode(earch);
                ret.put("code", earch);
                return ret;
            }
            GeographyCode continentCode = null;
            if (continent != null) {
                continentCode = TableGeographyCode.readCode(conn, 2, continent, decodeAncestors);
                if (continentCode == null) {
                    continentCode = new GeographyCode();
                    continentCode.setLevelCode(new GeographyCodeLevel("Continent"));
                    continentCode.setLongitude(longitude);
                    continentCode.setLatitude(latitude);
                    continentCode.setChineseName(continent);
                    continentCode.setEnglishName(continent);
                    continentCode.setOwnerCode(earch);
                    msg += "continent :" + continent + ", " + longitude + "," + latitude;
                    if (create) {
                        if (!TableGeographyCode.insert(conn, geoInsert, continentCode)) {
                            continentCode = null;
                            msg += " is not existed, and failed to create. ";
                        } else {
                            msg += " is not existed, and  created now.";
                            ret.put("inserted", "true");
                        }
                    } else {
                        msg += " is not existed, and will not be created.";
                    }
                }
            }
            if (level == 2) {
                ret.put("message", msg);
                ret.put("code", continentCode);
                return ret;
            }
            GeographyCode countryCode = null;
            if (country != null) {
                countryCode = TableGeographyCode.readCode(conn, 3, country, decodeAncestors);
                if (countryCode == null) {
                    countryCode = new GeographyCode();
                    countryCode.setLevelCode(new GeographyCodeLevel("Country"));
                    countryCode.setLongitude(longitude);
                    countryCode.setLatitude(latitude);
                    countryCode.setChineseName(country);
                    countryCode.setEnglishName(country);
                    if (continentCode != null) {
                        countryCode.setContinent(continentCode.getGcid());
                        countryCode.setOwner(continentCode.getGcid());
                    }
                    msg += "country :" + country + ", " + longitude + "," + latitude;
                    if (create) {
                        if (!TableGeographyCode.insert(conn, geoInsert, countryCode)) {
                            countryCode = null;
                            msg += " is not existed, and failed to create. ";
                        } else {
                            msg += " is not existed, and  created now.";
                            ret.put("inserted", "true");
                        }
                    } else {
                        msg += " is not existed, and will not be created.";
                    }
                }
            }
            if (level == 3) {
                ret.put("message", msg);
                ret.put("code", countryCode);
                return ret;
            }
            GeographyCode provinceCode = null;
            if (province != null) {
                if (countryCode != null && create) {
                    sql = "SELECT * FROM Geography_Code WHERE " + " level=4 AND country="
                            + countryCode.getGcid() + " AND (" + TableGeographyCode.nameEqual(province) + ")";
                    provinceCode = TableGeographyCode.queryCode(conn, sql, decodeAncestors);
                } else {
                    provinceCode = TableGeographyCode.readCode(conn, 4, province, decodeAncestors);
                }
                if (provinceCode == null) {
                    provinceCode = new GeographyCode();
                    provinceCode.setLevelCode(new GeographyCodeLevel("Province"));
                    provinceCode.setLongitude(longitude);
                    provinceCode.setLatitude(latitude);
                    provinceCode.setChineseName(province);
                    provinceCode.setEnglishName(province);
                    if (countryCode != null) {
                        provinceCode.setContinent(countryCode.getContinent());
                        provinceCode.setCountry(countryCode.getGcid());
                        msg += "\nprovince :" + country + "," + province + "," + longitude + "," + latitude;
                    } else if (continentCode != null) {
                        provinceCode.setContinent(continentCode.getGcid());
                        provinceCode.setOwner(continentCode.getGcid());
                        msg += "\nprovince :" + continent + ", " + province + "," + longitude + "," + latitude;
                    } else {
                        msg += "\nprovince :" + province + "," + longitude + "," + latitude;
                    }
                    if (create) {
                        if (!TableGeographyCode.insert(conn, geoInsert, provinceCode)) {
                            provinceCode = null;
                            msg += " is not existed, and failed to create. ";
                        } else {
                            msg += " is not existed, and  created now.";
                            ret.put("inserted", "true");
                        }
                    } else {
                        msg += " is not existed, and will not be created.";
                    }
                }
            }
            if (level == 4) {
                ret.put("message", msg);
                ret.put("code", provinceCode);
                return ret;
            }
            GeographyCode cityCode = null;
            if (city != null) {
                sql = "SELECT * FROM Geography_Code WHERE " + " (level=5 OR level=6 ) AND ";
                if (countryCode != null) {
                    sql += " country=" + countryCode.getGcid() + " AND ";
                }
                if (provinceCode != null) {
                    sql += " province=" + provinceCode.getGcid() + " AND ";
                }
                sql += " ( " + TableGeographyCode.nameEqual(city) + " )";
                cityCode = TableGeographyCode.queryCode(conn, sql, decodeAncestors);
                if (cityCode == null) {
                    cityCode = new GeographyCode();
                    cityCode.setLevelCode(new GeographyCodeLevel("City"));
                    cityCode.setLongitude(longitude);
                    cityCode.setLatitude(latitude);
                    cityCode.setChineseName(city);
                    cityCode.setEnglishName(city);
                    if (provinceCode != null) {
                        cityCode.setContinent(provinceCode.getContinent());
                        cityCode.setCountry(provinceCode.getCountry());
                        cityCode.setProvince(provinceCode.getGcid());
                        msg += "\ncity :" + province + "," + city + "," + longitude + "," + latitude;
                    } else if (countryCode != null) {
                        cityCode.setContinent(countryCode.getContinent());
                        cityCode.setCountry(countryCode.getGcid());
                        msg += "\ncity :" + country + ", " + city + "," + longitude + "," + latitude;
                    } else if (continentCode != null) {
                        cityCode.setContinent(continentCode.getGcid());
                        cityCode.setOwner(continentCode.getGcid());
                        msg += "\ncity :" + continent + ", " + city + "," + longitude + "," + latitude;
                    }
                    if (create) {
                        if (!TableGeographyCode.insert(conn, geoInsert, cityCode)) {
                            cityCode = null;
                            msg += " is not existed, and failed to create. ";
                        } else {
                            msg += " is not existed, and  created now.";
                            ret.put("inserted", "true");
                        }
                    } else {
                        msg += " is not existed, and will not be created.";
                    }
                }
            }
            if (level == 5) {
                ret.put("message", msg);
                ret.put("code", cityCode);
                return ret;
            }
            GeographyCode countyCode = null;
            if (county != null) {
                sql = "SELECT * FROM Geography_Code WHERE level=6 AND ";
                if (countryCode != null) {
                    sql += " country=" + countryCode.getGcid() + " AND ";
                }
                if (provinceCode != null) {
                    sql += " province=" + provinceCode.getGcid() + " AND ";
                }
                if (cityCode != null) {
                    sql += " city=" + cityCode.getGcid() + " AND ";
                }
                sql += " ( " + TableGeographyCode.nameEqual(county) + " )";
                countyCode = TableGeographyCode.queryCode(conn, sql, decodeAncestors);
                if (countyCode == null) {
                    countyCode = new GeographyCode();
                    countyCode.setLevelCode(new GeographyCodeLevel("County"));
                    countyCode.setLongitude(longitude);
                    countyCode.setLatitude(latitude);
                    countyCode.setChineseName(county);
                    countyCode.setEnglishName(county);
                    if (cityCode != null) {
                        countyCode.setContinent(cityCode.getContinent());
                        countyCode.setCountry(cityCode.getCountry());
                        countyCode.setProvince(cityCode.getProvince());
                        countyCode.setCity(cityCode.getGcid());
                        msg += "\ncounty :" + city + ", " + county + "," + longitude + "," + latitude;
                    } else if (provinceCode != null) {
                        countyCode.setContinent(provinceCode.getContinent());
                        countyCode.setCountry(provinceCode.getCountry());
                        countyCode.setProvince(provinceCode.getGcid());
                        msg += "\ncounty :" + province + ", " + county + "," + longitude + "," + latitude;
                    } else if (countryCode != null) {
                        countyCode.setContinent(countryCode.getContinent());
                        countyCode.setCountry(countryCode.getGcid());
                        msg += "\ncounty :" + country + ", " + county + "," + longitude + "," + latitude;
                    } else if (continentCode != null) {
                        countyCode.setContinent(continentCode.getGcid());
                        countyCode.setOwner(continentCode.getGcid());
                        msg += "\ncounty :" + continent + ", " + county + "," + longitude + "," + latitude;
                    }
                    if (create) {
                        if (!TableGeographyCode.insert(conn, geoInsert, countyCode)) {
                            countyCode = null;
                            msg += " is not existed, and failed to create. ";
                        } else {
                            msg += " is not existed, and  created now.";
                            ret.put("inserted", "true");
                        }
                    } else {
                        msg += " is not existed, and will not be created.";
                    }
                }
            }
            if (level == 6) {
                ret.put("message", msg);
                ret.put("code", countyCode);
                return ret;
            }
            GeographyCode townCode = null;
            if (town != null) {
                sql = "SELECT * FROM Geography_Code WHERE level=7 AND ";
                if (countryCode != null) {
                    sql += " country=" + countryCode.getGcid() + " AND ";
                }
                if (provinceCode != null) {
                    sql += " province=" + provinceCode.getGcid() + " AND ";
                }
                if (cityCode != null) {
                    sql += " city=" + cityCode.getGcid() + " AND ";
                }
                if (countyCode != null) {
                    sql += " county=" + countyCode.getGcid() + " AND ";
                }
                sql += "  ( " + TableGeographyCode.nameEqual(county) + " )";
                townCode = TableGeographyCode.queryCode(conn, sql, decodeAncestors);
                if (townCode == null) {
                    townCode = new GeographyCode();
                    townCode.setLevelCode(new GeographyCodeLevel("Town"));
                    townCode.setLongitude(longitude);
                    townCode.setLatitude(latitude);
                    townCode.setChineseName(town);
                    townCode.setEnglishName(town);
                    if (countyCode != null) {
                        townCode.setContinent(countyCode.getContinent());
                        townCode.setCountry(countyCode.getCountry());
                        townCode.setProvince(countyCode.getProvince());
                        townCode.setCity(countyCode.getCity());
                        townCode.setCounty(countyCode.getGcid());
                        msg += "\ntown :" + county + ", " + town + "," + longitude + "," + latitude;
                    } else if (cityCode != null) {
                        townCode.setContinent(cityCode.getContinent());
                        townCode.setCountry(cityCode.getCountry());
                        townCode.setProvince(cityCode.getProvince());
                        townCode.setCity(cityCode.getGcid());
                        msg += "\ntown :" + city + ", " + town + "," + longitude + "," + latitude;
                    } else if (provinceCode != null) {
                        townCode.setContinent(provinceCode.getContinent());
                        townCode.setCountry(provinceCode.getCountry());
                        townCode.setProvince(provinceCode.getGcid());
                        msg += "\ntown :" + province + ", " + town + "," + longitude + "," + latitude;
                    } else if (countryCode != null) {
                        townCode.setContinent(countryCode.getContinent());
                        townCode.setCountry(countryCode.getGcid());
                        msg += "\ntown :" + county + ", " + town + "," + longitude + "," + latitude;
                    } else if (continentCode != null) {
                        townCode.setContinent(continentCode.getGcid());
                        townCode.setOwner(continentCode.getGcid());
                        msg += "\ntown :" + continent + ", " + town + "," + longitude + "," + latitude;
                    }
                    if (create) {
                        if (!TableGeographyCode.insert(conn, geoInsert, townCode)) {
                            townCode = null;
                            msg += " is not existed, and failed to create. ";
                        } else {
                            msg += " is not existed, and  created now.";
                            ret.put("inserted", "true");
                        }
                    } else {
                        msg += " is not existed, and will not be created.";
                    }
                }
            }
            if (level == 7) {
                ret.put("message", msg);
                ret.put("code", townCode);
                return ret;
            }
            GeographyCode villageCode = null;
            if (village != null) {
                sql = "SELECT * FROM Geography_Code WHERE level=8 AND ";
                if (countryCode != null) {
                    sql += " country=" + countryCode.getGcid() + " AND ";
                }
                if (provinceCode != null) {
                    sql += " province=" + provinceCode.getGcid() + " AND ";
                }
                if (cityCode != null) {
                    sql += " city=" + cityCode.getGcid() + " AND ";
                }
                if (countyCode != null) {
                    sql += " county=" + countyCode.getGcid() + " AND ";
                }
                if (townCode != null) {
                    sql += " town=" + townCode.getGcid() + " AND ";
                }
                sql += " ( " + TableGeographyCode.nameEqual(village) + " )";
                villageCode = TableGeographyCode.queryCode(conn, sql, decodeAncestors);
                if (villageCode == null) {
                    villageCode = new GeographyCode();
                    villageCode.setLevelCode(new GeographyCodeLevel("Village"));
                    villageCode.setLongitude(longitude);
                    villageCode.setLatitude(latitude);
                    villageCode.setChineseName(village);
                    villageCode.setEnglishName(village);
                    if (townCode != null) {
                        villageCode.setContinent(townCode.getContinent());
                        villageCode.setCountry(townCode.getCountry());
                        villageCode.setProvince(townCode.getProvince());
                        villageCode.setCity(townCode.getCity());
                        villageCode.setCounty(townCode.getCounty());
                        villageCode.setTown(townCode.getGcid());
                        msg += "\nvillage :" + town + ", " + village + "," + longitude + "," + latitude;
                    } else if (countyCode != null) {
                        villageCode.setContinent(countyCode.getContinent());
                        villageCode.setCountry(countyCode.getCountry());
                        villageCode.setProvince(countyCode.getProvince());
                        villageCode.setCity(countyCode.getCity());
                        villageCode.setCounty(countyCode.getGcid());
                        msg += "\nvillage :" + county + ", " + village + "," + longitude + "," + latitude;
                    } else if (cityCode != null) {
                        villageCode.setContinent(cityCode.getContinent());
                        villageCode.setCountry(cityCode.getCountry());
                        villageCode.setProvince(cityCode.getProvince());
                        villageCode.setCity(cityCode.getGcid());
                        msg += "\nvillage :" + city + ", " + village + "," + longitude + "," + latitude;
                    } else if (provinceCode != null) {
                        villageCode.setContinent(provinceCode.getContinent());
                        villageCode.setCountry(provinceCode.getCountry());
                        villageCode.setProvince(provinceCode.getGcid());
                        msg += "\nvillage :" + province + ", " + village + "," + longitude + "," + latitude;
                    } else if (countryCode != null) {
                        villageCode.setContinent(countryCode.getContinent());
                        villageCode.setCountry(countryCode.getGcid());
                        msg += "\nvillage :" + county + ", " + village + "," + longitude + "," + latitude;
                    } else if (continentCode != null) {
                        villageCode.setContinent(continentCode.getGcid());
                        villageCode.setOwner(continentCode.getGcid());
                        msg += "\nvillage :" + continent + ", " + village + "," + longitude + "," + latitude;
                    }
                    if (create) {
                        if (!TableGeographyCode.insert(conn, geoInsert, villageCode)) {
                            villageCode = null;
                            msg += " is not existed, and failed to create. ";
                        } else {
                            msg += " is not existed, and  created now.";
                            ret.put("inserted", "true");
                        }
                    } else {
                        msg += " is not existed, and will not be created.";
                    }
                }
            }
            if (level == 8) {
                ret.put("message", msg);
                ret.put("code", villageCode);
                return ret;
            }
            GeographyCode buildingCode = null;
            if (building != null) {
                sql = "SELECT * FROM Geography_Code WHERE level=9 AND ";
                if (countryCode != null) {
                    sql += " country=" + countryCode.getGcid() + " AND ";
                }
                if (provinceCode != null) {
                    sql += " province=" + provinceCode.getGcid() + " AND ";
                }
                if (cityCode != null) {
                    sql += " city=" + cityCode.getGcid() + " AND ";
                }
                if (countyCode != null) {
                    sql += " county=" + countyCode.getGcid() + " AND ";
                }
                if (townCode != null) {
                    sql += " town=" + townCode.getGcid() + " AND ";
                }
                if (villageCode != null) {
                    sql += " village=" + villageCode.getGcid() + " AND ";
                }
                sql += " ( " + TableGeographyCode.nameEqual(building) + " )";
                buildingCode = TableGeographyCode.queryCode(conn, sql, decodeAncestors);
                if (buildingCode == null) {
                    buildingCode = new GeographyCode();
                    buildingCode.setLevelCode(new GeographyCodeLevel("Building"));
                    buildingCode.setLongitude(longitude);
                    buildingCode.setLatitude(latitude);
                    buildingCode.setChineseName(building);
                    buildingCode.setEnglishName(building);
                    if (villageCode != null) {
                        buildingCode.setContinent(villageCode.getContinent());
                        buildingCode.setCountry(villageCode.getCountry());
                        buildingCode.setProvince(villageCode.getProvince());
                        buildingCode.setCity(villageCode.getCity());
                        buildingCode.setCounty(villageCode.getCounty());
                        buildingCode.setTown(villageCode.getTown());
                        buildingCode.setVillage(villageCode.getGcid());
                        msg += "\nbuilding :" + village + ", " + building + "," + longitude + "," + latitude;
                    } else if (townCode != null) {
                        buildingCode.setContinent(townCode.getContinent());
                        buildingCode.setCountry(townCode.getCountry());
                        buildingCode.setProvince(townCode.getProvince());
                        buildingCode.setCity(townCode.getCity());
                        buildingCode.setCounty(townCode.getCounty());
                        buildingCode.setTown(townCode.getGcid());
                        msg += "\nbuilding :" + town + ", " + building + "," + longitude + "," + latitude;
                    } else if (countyCode != null) {
                        buildingCode.setContinent(countyCode.getContinent());
                        buildingCode.setCountry(countyCode.getCountry());
                        buildingCode.setProvince(countyCode.getProvince());
                        buildingCode.setCity(countyCode.getCity());
                        buildingCode.setCounty(countyCode.getGcid());
                        msg += "\nbuilding :" + county + ", " + building + "," + longitude + "," + latitude;
                    } else if (cityCode != null) {
                        buildingCode.setContinent(cityCode.getContinent());
                        buildingCode.setCountry(cityCode.getCountry());
                        buildingCode.setProvince(cityCode.getProvince());
                        buildingCode.setCity(cityCode.getGcid());
                        msg += "\nbuilding :" + city + ", " + building + "," + longitude + "," + latitude;
                    } else if (provinceCode != null) {
                        buildingCode.setContinent(provinceCode.getContinent());
                        buildingCode.setCountry(provinceCode.getCountry());
                        buildingCode.setProvince(provinceCode.getGcid());
                        msg += "\nbuilding :" + province + ", " + building + "," + longitude + "," + latitude;
                    } else if (countryCode != null) {
                        buildingCode.setContinent(countryCode.getContinent());
                        buildingCode.setCountry(countryCode.getGcid());
                        msg += "\nbuilding :" + country + ", " + building + "," + longitude + "," + latitude;
                    } else if (continentCode != null) {
                        buildingCode.setContinent(continentCode.getGcid());
                        buildingCode.setOwner(continentCode.getGcid());
                        msg += "\nbuilding :" + continent + ", " + building + "," + longitude + "," + latitude;
                    }
                    if (create) {
                        if (!TableGeographyCode.insert(conn, geoInsert, buildingCode)) {
                            buildingCode = null;
                            msg += " is not existed, and failed to create. ";
                        } else {
                            msg += " is not existed, and  created now.";
                            ret.put("inserted", "true");
                        }
                    } else {
                        msg += " is not existed, and will not be created.";
                    }
                }
            }
            if (level == 9) {
                ret.put("message", msg);
                ret.put("code", buildingCode);
                return ret;
            }
            GeographyCode poiCode = null;
            if (poi != null) {
                sql = "SELECT * FROM Geography_Code WHERE ";
                if (countryCode != null) {
                    sql += " country=" + countryCode.getGcid() + " AND ";
                }
                if (provinceCode != null) {
                    sql += " province=" + provinceCode.getGcid() + " AND ";
                }
                if (cityCode != null) {
                    sql += " city=" + cityCode.getGcid() + " AND ";
                }
                if (countyCode != null) {
                    sql += " county=" + countyCode.getGcid() + " AND ";
                }
                if (townCode != null) {
                    sql += " town=" + townCode.getGcid() + " AND ";
                }
                if (villageCode != null) {
                    sql += " village=" + villageCode.getGcid() + " AND ";
                }
                if (buildingCode != null) {
                    sql += " building=" + buildingCode.getGcid() + " AND ";
                }
                sql += " ( " + TableGeographyCode.nameEqual(poi) + " )";
                poiCode = TableGeographyCode.queryCode(conn, sql, decodeAncestors);
                if (poiCode == null) {
                    poiCode = new GeographyCode();
                    poiCode.setLevelCode(new GeographyCodeLevel("Point Of Interest"));
                    poiCode.setLongitude(longitude);
                    poiCode.setLatitude(latitude);
                    poiCode.setChineseName(poi);
                    poiCode.setEnglishName(poi);
                    if (buildingCode != null) {
                        poiCode.setContinent(buildingCode.getContinent());
                        poiCode.setCountry(buildingCode.getCountry());
                        poiCode.setProvince(buildingCode.getProvince());
                        poiCode.setCity(buildingCode.getCity());
                        poiCode.setCounty(buildingCode.getCounty());
                        poiCode.setTown(buildingCode.getTown());
                        poiCode.setVillage(buildingCode.getVillage());
                        poiCode.setBuilding(buildingCode.getGcid());
                        poiCode.setOwner(buildingCode.getGcid());
                        msg += "\npoi :" + building + ", " + poi + "," + longitude + "," + latitude;
                    } else if (villageCode != null) {
                        poiCode.setContinent(villageCode.getContinent());
                        poiCode.setCountry(villageCode.getCountry());
                        poiCode.setProvince(villageCode.getProvince());
                        poiCode.setCity(villageCode.getCity());
                        poiCode.setCounty(villageCode.getCounty());
                        poiCode.setTown(villageCode.getTown());
                        poiCode.setVillage(villageCode.getGcid());
                        poiCode.setOwner(villageCode.getGcid());
                        msg += "\npoi :" + village + ", " + poi + "," + longitude + "," + latitude;
                    } else if (townCode != null) {
                        poiCode.setContinent(townCode.getContinent());
                        poiCode.setCountry(townCode.getCountry());
                        poiCode.setProvince(townCode.getProvince());
                        poiCode.setCity(townCode.getCity());
                        poiCode.setCounty(townCode.getCounty());
                        poiCode.setTown(townCode.getGcid());
                        poiCode.setOwner(townCode.getGcid());
                        msg += "\npoi :" + town + ", " + poi + "," + longitude + "," + latitude;
                    } else if (countyCode != null) {
                        poiCode.setContinent(countyCode.getContinent());
                        poiCode.setCountry(countyCode.getCountry());
                        poiCode.setProvince(countyCode.getProvince());
                        poiCode.setCity(countyCode.getCity());
                        poiCode.setCounty(countyCode.getGcid());
                        poiCode.setOwner(countyCode.getGcid());
                        msg += "\npoi :" + county + ", " + poi + "," + longitude + "," + latitude;
                    } else if (cityCode != null) {
                        poiCode.setContinent(cityCode.getContinent());
                        poiCode.setCountry(cityCode.getCountry());
                        poiCode.setProvince(cityCode.getProvince());
                        poiCode.setCity(cityCode.getGcid());
                        poiCode.setOwner(cityCode.getGcid());
                        msg += "\npoi :" + city + ", " + poi + "," + longitude + "," + latitude;
                    } else if (provinceCode != null) {
                        poiCode.setContinent(provinceCode.getContinent());
                        poiCode.setCountry(provinceCode.getCountry());
                        poiCode.setProvince(provinceCode.getGcid());
                        poiCode.setOwner(provinceCode.getGcid());
                        msg += "\npoi :" + province + ", " + poi + "," + longitude + "," + latitude;
                    } else if (countryCode != null) {
                        poiCode.setContinent(countryCode.getContinent());
                        poiCode.setCountry(countryCode.getGcid());
                        poiCode.setOwner(countryCode.getGcid());
                        msg += "\npoi :" + country + ", " + poi + "," + longitude + "," + latitude;
                    } else if (continentCode != null) {
                        poiCode.setContinent(continentCode.getGcid());
                        poiCode.setOwner(continentCode.getGcid());
                        msg += "\npoi :" + continent + ", " + poi + "," + longitude + "," + latitude;
                    }
                    if (create) {
                        if (!TableGeographyCode.insert(conn, geoInsert, poiCode)) {
                            poiCode = null;
                            msg += " is not existed, and failed to create. ";
                        } else {
                            msg += " is not existed, and  created now.";
                            ret.put("inserted", "true");
                        }
                    } else {
                        msg += " is not existed, and will not be created.";
                    }
                }
            }

            ret.put("message", msg);
            ret.put("code", poiCode);
            return ret;
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
            ret.put("message", e.toString());
            return ret;
        }
    }

    public static Map<String, Object> encode(Connection conn, PreparedStatement geoInsert,
            int level, double longitude, double latitude, String continent, String country, String province,
            String city, String county, String town, String village, String building, boolean create) {
        Map<String, Object> ret = new HashMap<>();
        try {
            String msg = "";
            if (conn == null || level > 9) {
                ret.put("message", "level is wrong");
                return ret;
            }
            String sql;
            if (level == 1) {
                GeographyCode earch = TableGeographyCode.earth(conn);
                if (earch == null) {
                    importPredefined(conn, null);
                    earch = TableGeographyCode.earth(conn);
                }
                ret.put("code", earch);
                return ret;
            }
            GeographyCode continentCode = null;
            if (continent != null) {
                continentCode = TableGeographyCode.readCode(conn, 2, continent, false);
                if (continentCode == null) {
                    continentCode = new GeographyCode();
                    continentCode.setLevelCode(new GeographyCodeLevel("Continent"));
                    continentCode.setLongitude(longitude);
                    continentCode.setLatitude(latitude);
                    continentCode.setChineseName(continent);
                    continentCode.setEnglishName(continent);
                    msg += "continent :" + continent + ", " + longitude + "," + latitude;
                    if (create) {
                        if (!TableGeographyCode.insert(conn, geoInsert, continentCode)) {
                            continentCode = null;
                            msg += " is not existed, and failed to create. ";
                        } else {
                            msg += " is not existed, and  created now.";
                            ret.put("inserted", "true");
                        }
                    } else {
                        msg += " is not existed, and will not be created.";
                    }
                }
            }
            if (level == 2) {
                ret.put("message", msg);
                ret.put("code", continentCode);
                return ret;
            }
            GeographyCode countryCode = null;
            if (country != null) {
                countryCode = TableGeographyCode.readCode(conn, 3, country, false);
                if (countryCode == null) {
                    countryCode = new GeographyCode();
                    countryCode.setLevelCode(new GeographyCodeLevel("Country"));
                    countryCode.setLongitude(longitude);
                    countryCode.setLatitude(latitude);
                    countryCode.setChineseName(country);
                    countryCode.setEnglishName(country);
                    if (continentCode != null) {
                        countryCode.setContinent(continentCode.getGcid());
                    }
                    msg += "country :" + country + ", " + longitude + "," + latitude;
                    if (create) {
                        if (!TableGeographyCode.insert(conn, geoInsert, countryCode)) {
                            countryCode = null;
                            msg += " is not existed, and failed to create. ";
                        } else {
                            msg += " is not existed, and  created now.";
                            ret.put("inserted", "true");
                        }
                    } else {
                        msg += " is not existed, and will not be created.";
                    }
                }
            }
            if (level == 3) {
                ret.put("message", msg);
                ret.put("code", countryCode);
                return ret;
            }
            GeographyCode provinceCode = null;
            if (province != null) {
                if (countryCode != null && create) {
                    sql = "SELECT * FROM Geography_Code WHERE " + " level=4 AND country=" + countryCode.getGcid() + " AND (" + TableGeographyCode.nameEqual(province) + ")";
                    provinceCode = TableGeographyCode.queryCode(conn, sql, false);
                } else {
                    provinceCode = TableGeographyCode.readCode(conn, 4, province, false);
                }
                if (provinceCode == null) {
                    provinceCode = new GeographyCode();
                    provinceCode.setLevelCode(new GeographyCodeLevel("Province"));
                    provinceCode.setLongitude(longitude);
                    provinceCode.setLatitude(latitude);
                    provinceCode.setChineseName(province);
                    provinceCode.setEnglishName(province);
                    if (countryCode != null) {
                        provinceCode.setContinent(countryCode.getContinent());
                        provinceCode.setCountry(countryCode.getGcid());
                        msg += "\nprovince :" + province + "," + longitude + "," + latitude;
                    } else {
                        msg += "\nprovince :" + country + ", " + province + "," + longitude + "," + latitude;
                    }
                    if (create) {
                        if (!TableGeographyCode.insert(conn, geoInsert, provinceCode)) {
                            provinceCode = null;
                            msg += " is not existed, and failed to create. ";
                        } else {
                            msg += " is not existed, and  created now.";
                            ret.put("inserted", "true");
                        }
                    } else {
                        msg += " is not existed, and will not be created.";
                    }
                }
            }
            if (level == 4) {
                ret.put("message", msg);
                ret.put("code", provinceCode);
                return ret;
            }
            GeographyCode cityCode = null;
            if (city != null) {
                sql = "SELECT * FROM Geography_Code WHERE " + " (level=5 OR level=6 ) AND ";
                if (countryCode != null) {
                    sql += " country=" + countryCode.getGcid() + " AND ";
                }
                if (provinceCode != null) {
                    sql += " province=" + provinceCode.getGcid() + " AND ";
                }
                sql += " ( " + TableGeographyCode.nameEqual(city) + " )";
                cityCode = TableGeographyCode.queryCode(conn, sql, false);
                if (cityCode == null) {
                    cityCode = new GeographyCode();
                    cityCode.setLevelCode(new GeographyCodeLevel("City"));
                    cityCode.setLongitude(longitude);
                    cityCode.setLatitude(latitude);
                    cityCode.setChineseName(city);
                    cityCode.setEnglishName(city);
                    if (provinceCode != null) {
                        cityCode.setContinent(provinceCode.getContinent());
                        cityCode.setCountry(provinceCode.getCountry());
                        cityCode.setProvince(provinceCode.getGcid());
                        msg += "\ncity :" + province + "," + city + "," + longitude + "," + latitude;
                    } else if (countryCode != null) {
                        cityCode.setContinent(countryCode.getContinent());
                        cityCode.setCountry(countryCode.getGcid());
                        msg += "\ncity :" + country + ", " + city + "," + longitude + "," + latitude;
                    }
                    if (create) {
                        if (!TableGeographyCode.insert(conn, geoInsert, cityCode)) {
                            cityCode = null;
                            msg += " is not existed, and failed to create. ";
                        } else {
                            msg += " is not existed, and  created now.";
                            ret.put("inserted", "true");
                        }
                    } else {
                        msg += " is not existed, and will not be created.";
                    }
                }
            }
            if (level == 5) {
                ret.put("message", msg);
                ret.put("code", cityCode);
                return ret;
            }
            GeographyCode countyCode = null;
            if (county != null) {
                sql = "SELECT * FROM Geography_Code WHERE level=6 AND ";
                if (countryCode != null) {
                    sql += " country=" + countryCode.getGcid() + " AND ";
                }
                if (provinceCode != null) {
                    sql += " province=" + provinceCode.getGcid() + " AND ";
                }
                if (cityCode != null) {
                    sql += " city=" + cityCode.getGcid() + " AND ";
                }
                sql += " ( " + TableGeographyCode.nameEqual(county) + " )";
                countyCode = TableGeographyCode.queryCode(conn, sql, false);
                if (countyCode == null) {
                    countyCode = new GeographyCode();
                    countyCode.setLevelCode(new GeographyCodeLevel("County"));
                    countyCode.setLongitude(longitude);
                    countyCode.setLatitude(latitude);
                    countyCode.setChineseName(county);
                    countyCode.setEnglishName(county);
                    if (cityCode != null) {
                        countyCode.setContinent(cityCode.getContinent());
                        countyCode.setCountry(cityCode.getCountry());
                        countyCode.setProvince(cityCode.getProvince());
                        countyCode.setCity(cityCode.getGcid());
                        msg += "\ncounty :" + city + ", " + county + "," + longitude + "," + latitude;
                    } else if (provinceCode != null) {
                        countyCode.setContinent(provinceCode.getContinent());
                        countyCode.setCountry(provinceCode.getCountry());
                        countyCode.setProvince(provinceCode.getGcid());
                        msg += "\ncounty :" + province + ", " + county + "," + longitude + "," + latitude;
                    } else if (countryCode != null) {
                        countyCode.setContinent(countryCode.getContinent());
                        countyCode.setCountry(countryCode.getGcid());
                        msg += "\ncounty :" + country + ", " + county + "," + longitude + "," + latitude;
                    }
                    if (create) {
                        if (!TableGeographyCode.insert(conn, geoInsert, countyCode)) {
                            countyCode = null;
                            msg += " is not existed, and failed to create. ";
                        } else {
                            msg += " is not existed, and  created now.";
                            ret.put("inserted", "true");
                        }
                    } else {
                        msg += " is not existed, and will not be created.";
                    }
                }
            }
            if (level == 6) {
                ret.put("message", msg);
                ret.put("code", countyCode);
                return ret;
            }
            GeographyCode townCode = null;
            if (town != null) {
                sql = "SELECT * FROM Geography_Code WHERE level=7 AND ";
                if (countryCode != null) {
                    sql += " country=" + countryCode.getGcid() + " AND ";
                }
                if (provinceCode != null) {
                    sql += " province=" + provinceCode.getGcid() + " AND ";
                }
                if (cityCode != null) {
                    sql += " city=" + cityCode.getGcid() + " AND ";
                }
                if (countyCode != null) {
                    sql += " county=" + countyCode.getGcid() + " AND ";
                }
                sql += "  ( " + TableGeographyCode.nameEqual(county) + " )";
                townCode = TableGeographyCode.queryCode(conn, sql, false);
                if (townCode == null) {
                    townCode = new GeographyCode();
                    townCode.setLevelCode(new GeographyCodeLevel("Town"));
                    townCode.setLongitude(longitude);
                    townCode.setLatitude(latitude);
                    townCode.setChineseName(town);
                    townCode.setEnglishName(town);
                    if (countyCode != null) {
                        townCode.setContinent(countyCode.getContinent());
                        townCode.setCountry(countyCode.getCountry());
                        townCode.setProvince(countyCode.getProvince());
                        townCode.setCity(countyCode.getCity());
                        townCode.setCounty(countyCode.getGcid());
                        msg += "\ntown :" + county + ", " + town + "," + longitude + "," + latitude;
                    } else if (cityCode != null) {
                        townCode.setContinent(cityCode.getContinent());
                        townCode.setCountry(cityCode.getCountry());
                        townCode.setProvince(cityCode.getProvince());
                        townCode.setCity(cityCode.getGcid());
                        msg += "\ntown :" + city + ", " + town + "," + longitude + "," + latitude;
                    } else if (provinceCode != null) {
                        townCode.setContinent(provinceCode.getContinent());
                        townCode.setCountry(provinceCode.getCountry());
                        townCode.setProvince(provinceCode.getGcid());
                        msg += "\ntown :" + province + ", " + town + "," + longitude + "," + latitude;
                    } else if (countryCode != null) {
                        townCode.setContinent(countryCode.getContinent());
                        townCode.setCountry(countryCode.getGcid());
                        msg += "\ntown :" + county + ", " + town + "," + longitude + "," + latitude;
                    }
                    if (create) {
                        if (!TableGeographyCode.insert(conn, geoInsert, townCode)) {
                            townCode = null;
                            msg += " is not existed, and failed to create. ";
                        } else {
                            msg += " is not existed, and  created now.";
                            ret.put("inserted", "true");
                        }
                    } else {
                        msg += " is not existed, and will not be created.";
                    }
                }
            }
            if (level == 7) {
                ret.put("message", msg);
                ret.put("code", townCode);
                return ret;
            }
            GeographyCode villageCode = null;
            if (village != null) {
                sql = "SELECT * FROM Geography_Code WHERE level=8 AND ";
                if (countryCode != null) {
                    sql += " country=" + countryCode.getGcid() + " AND ";
                }
                if (provinceCode != null) {
                    sql += " province=" + provinceCode.getGcid() + " AND ";
                }
                if (cityCode != null) {
                    sql += " city=" + cityCode.getGcid() + " AND ";
                }
                if (countyCode != null) {
                    sql += " county=" + countyCode.getGcid() + " AND ";
                }
                if (townCode != null) {
                    sql += " town=" + townCode.getGcid() + " AND ";
                }
                sql += " ( " + TableGeographyCode.nameEqual(village) + " )";
                villageCode = TableGeographyCode.queryCode(conn, sql, false);
                if (villageCode == null) {
                    villageCode = new GeographyCode();
                    villageCode.setLevelCode(new GeographyCodeLevel("Village"));
                    villageCode.setLongitude(longitude);
                    villageCode.setLatitude(latitude);
                    villageCode.setChineseName(village);
                    villageCode.setEnglishName(village);
                    if (townCode != null) {
                        villageCode.setContinent(townCode.getContinent());
                        villageCode.setCountry(townCode.getCountry());
                        villageCode.setProvince(townCode.getProvince());
                        villageCode.setCity(townCode.getCity());
                        villageCode.setCounty(townCode.getCounty());
                        villageCode.setTown(townCode.getGcid());
                        msg += "\nvillage :" + town + ", " + village + "," + longitude + "," + latitude;
                    } else if (countyCode != null) {
                        villageCode.setContinent(countyCode.getContinent());
                        villageCode.setCountry(countyCode.getCountry());
                        villageCode.setProvince(countyCode.getProvince());
                        villageCode.setCity(countyCode.getCity());
                        villageCode.setCounty(countyCode.getGcid());
                        msg += "\nvillage :" + county + ", " + village + "," + longitude + "," + latitude;
                    } else if (cityCode != null) {
                        villageCode.setContinent(cityCode.getContinent());
                        villageCode.setCountry(cityCode.getCountry());
                        villageCode.setProvince(cityCode.getProvince());
                        villageCode.setCity(cityCode.getGcid());
                        msg += "\nvillage :" + city + ", " + village + "," + longitude + "," + latitude;
                    } else if (provinceCode != null) {
                        villageCode.setContinent(provinceCode.getContinent());
                        villageCode.setCountry(provinceCode.getCountry());
                        villageCode.setProvince(provinceCode.getGcid());
                        msg += "\nvillage :" + province + ", " + village + "," + longitude + "," + latitude;
                    } else if (countryCode != null) {
                        villageCode.setContinent(countryCode.getContinent());
                        villageCode.setCountry(countryCode.getGcid());
                        msg += "\nvillage :" + county + ", " + village + "," + longitude + "," + latitude;
                    }
                    if (create) {
                        if (!TableGeographyCode.insert(conn, geoInsert, villageCode)) {
                            villageCode = null;
                            msg += " is not existed, and failed to create. ";
                        } else {
                            msg += " is not existed, and  created now.";
                            ret.put("inserted", "true");
                        }
                    } else {
                        msg += " is not existed, and will not be created.";
                    }
                }
            }
            if (level == 8) {
                ret.put("message", msg);
                ret.put("code", villageCode);
                return ret;
            }
            GeographyCode buildingCode = null;
            if (building != null) {
                sql = "SELECT * FROM Geography_Code WHERE level=9 AND ";
                if (countryCode != null) {
                    sql += " country=" + countryCode.getGcid() + " AND ";
                }
                if (provinceCode != null) {
                    sql += " province=" + provinceCode.getGcid() + " AND ";
                }
                if (cityCode != null) {
                    sql += " city=" + cityCode.getGcid() + " AND ";
                }
                if (countyCode != null) {
                    sql += " county=" + countyCode.getGcid() + " AND ";
                }
                if (townCode != null) {
                    sql += " town=" + townCode.getGcid() + " AND ";
                }
                if (villageCode != null) {
                    sql += " village=" + villageCode.getGcid() + " AND ";
                }
                sql += " ( " + TableGeographyCode.nameEqual(building) + " )";
                buildingCode = TableGeographyCode.queryCode(conn, sql, false);
                if (buildingCode == null) {
                    buildingCode = new GeographyCode();
                    buildingCode.setLevelCode(new GeographyCodeLevel("Building"));
                    buildingCode.setLongitude(longitude);
                    buildingCode.setLatitude(latitude);
                    buildingCode.setChineseName(building);
                    buildingCode.setEnglishName(building);
                    if (villageCode != null) {
                        buildingCode.setContinent(villageCode.getContinent());
                        buildingCode.setCountry(villageCode.getCountry());
                        buildingCode.setProvince(villageCode.getProvince());
                        buildingCode.setCity(villageCode.getCity());
                        buildingCode.setCounty(villageCode.getCounty());
                        buildingCode.setTown(villageCode.getTown());
                        buildingCode.setVillage(villageCode.getGcid());
                        msg += "\nbuilding :" + village + ", " + building + "," + longitude + "," + latitude;
                    } else if (townCode != null) {
                        buildingCode.setContinent(townCode.getContinent());
                        buildingCode.setCountry(townCode.getCountry());
                        buildingCode.setProvince(townCode.getProvince());
                        buildingCode.setCity(townCode.getCity());
                        buildingCode.setCounty(townCode.getCounty());
                        buildingCode.setTown(townCode.getGcid());
                        msg += "\nbuilding :" + town + ", " + building + "," + longitude + "," + latitude;
                    } else if (countyCode != null) {
                        buildingCode.setContinent(countyCode.getContinent());
                        buildingCode.setCountry(countyCode.getCountry());
                        buildingCode.setProvince(countyCode.getProvince());
                        buildingCode.setCity(countyCode.getCity());
                        buildingCode.setCounty(countyCode.getGcid());
                        msg += "\nbuilding :" + county + ", " + building + "," + longitude + "," + latitude;
                    } else if (cityCode != null) {
                        buildingCode.setContinent(cityCode.getContinent());
                        buildingCode.setCountry(cityCode.getCountry());
                        buildingCode.setProvince(cityCode.getProvince());
                        buildingCode.setCity(cityCode.getGcid());
                        msg += "\nbuilding :" + city + ", " + building + "," + longitude + "," + latitude;
                    } else if (provinceCode != null) {
                        buildingCode.setContinent(provinceCode.getContinent());
                        buildingCode.setCountry(provinceCode.getCountry());
                        buildingCode.setProvince(provinceCode.getGcid());
                        msg += "\nbuilding :" + province + ", " + building + "," + longitude + "," + latitude;
                    } else if (countryCode != null) {
                        buildingCode.setContinent(countryCode.getContinent());
                        buildingCode.setCountry(countryCode.getGcid());
                        msg += "\nbuilding :" + country + ", " + building + "," + longitude + "," + latitude;
                    }
                    if (create) {
                        if (!TableGeographyCode.insert(conn, geoInsert, buildingCode)) {
                            buildingCode = null;
                            msg += " is not existed, and failed to create. ";
                        } else {
                            msg += " is not existed, and  created now.";
                            ret.put("inserted", "true");
                        }
                    } else {
                        msg += " is not existed, and will not be created.";
                    }
                }
            }
            if (level == 9) {
                ret.put("message", msg);
                ret.put("code", buildingCode);
                return ret;
            }
            ret.put("message", msg);
            return ret;
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
            ret.put("message", e.toString());
            return ret;
        }
    }

    /*
        Import
     */
    public static void importPredefined() {
        importPredefined(null, null);
    }

    public static void importPredefined(Connection conn) {
        importPredefined(conn, null);
    }

    public static void importPredefined(Connection conn, LoadingController loading) {
        if (conn == null) {
            try (final Connection conn1 = DriverManager.getConnection(DerbyBase.protocol + dbHome() + DerbyBase.login)) {
                importPredefined(conn1, loading);
            } catch (Exception e) {
                AppVariables.logger.debug(e.toString());
            }
            return;
        }
        try {
            conn.setAutoCommit(false);
            File file = FxmlControl.getInternalFile("/data/db/Geography_Code_global_internal.csv", "data", "Geography_Code_global_internal.csv", true);
            importInternalCSV(conn, loading, file, true);
            file = FxmlControl.getInternalFile("/data/db/Geography_Code_countries_internal.csv", "data", "Geography_Code_countries_internal.csv", true);
            importInternalCSV(conn, loading, file, true);
            if (!AppVariables.isChinese()) {
                try {
                    String sql = "UPDATE Geography_Code SET comments=null WHERE level=3 AND predefined=1";
                    conn.prepareStatement(sql).executeUpdate();
                } catch (Exception e) {
                    AppVariables.logger.debug(e.toString());
                }
            }
            file = FxmlControl.getInternalFile("/data/db/Geography_Code_china_provinces_internal.csv", "data", "Geography_Code_china_provinces_internal.csv", true);
            importInternalCSV(conn, loading, file, true);
            file = FxmlControl.getInternalFile("/data/db/Geography_Code_china_cities_internal.csv", "data", "Geography_Code_china_cities_internal.csv", true);
            importInternalCSV(conn, loading, file, true);
            file = FxmlControl.getInternalFile("/data/db/Geography_Code_china_counties_internal.csv", "data", "Geography_Code_china_counties_internal.csv", true);
            importInternalCSV(conn, loading, file, true);
            file = FxmlControl.getInternalFile("/data/db/Geography_Code_special.csv", "data", "Geography_Code_special.csv", true);
            importInternalCSV(conn, loading, file, true);
            conn.commit();
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
            if (loading != null) {
                loading.setInfo(e.toString());
            }
        }
    }

    // gcid,levelid,longitude,latitude,chinese_name,english_name,code1,code2,code3,code4,code5,alias1,alias2,alias3,alias4,alias5,
    // area,population,continentid,countryid,provinceid,cityid,countyid,townid,villageid,buildingid,comments
    public static void importInternalCSV(LoadingController loading, File file, boolean predefined) {
        try (final Connection conn = DriverManager.getConnection(DerbyBase.protocol + dbHome() + DerbyBase.login)) {
            importInternalCSV(conn, loading, file, predefined);
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
        }
    }

    public static void importInternalCSV(Connection conn, LoadingController loading, File file, boolean predefined) {
        long importCount = 0;
        long insertCount = 0;
        long updateCount = 0;
        long failedCount = 0;
        try (final CSVParser parser = CSVParser.parse(file, StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(',').withTrim().withNullString(""))) {
            conn.setAutoCommit(false);
            List<String> names = parser.getHeaderNames();
            if (loading != null) {
                loading.setInfo(message("Importing") + " " + file.getAbsolutePath());
            }
            try (final PreparedStatement gcidQeury = conn.prepareStatement(TableGeographyCode.GCidQeury);
                    final PreparedStatement insert = conn.prepareStatement(TableGeographyCode.Insert);
                    final PreparedStatement update = conn.prepareStatement(TableGeographyCode.Update)) {
                gcidQeury.setMaxRows(1);
                boolean exist;
                for (CSVRecord record : parser) {
                    GeographyCode code = GeographyCodeTools.readIntenalRecord(names, record);
                    code.setPredefined(predefined);
                    gcidQeury.setLong(1, code.getGcid());
                    try (final ResultSet results = gcidQeury.executeQuery()) {
                        exist = results.next();
                    }
                    if (exist) {
                        if (TableGeographyCode.update(conn, update, code)) {
                            updateCount++;
                            importCount++;
                            if (loading != null && (importCount % 20 == 0)) {
                                loading.setInfo(message("Update") + ": " + updateCount + " " + code.getLevelCode().getName()
                                        + " " + code.getName() + " " + code.getLongitude() + " " + code.getLatitude());
                            }
                        } else {
                            ++failedCount;
                            if (loading != null) {
                                loading.setInfo(message("Failed") + ": " + failedCount + " " + code.getLevelCode().getName()
                                        + " " + code.getName() + " " + code.getLongitude() + " " + code.getLatitude());
                            }
                        }
                    } else {
                        if (TableGeographyCode.insert(conn, insert, code)) {
                            insertCount++;
                            importCount++;
                            if (loading != null && (importCount % 20 == 0)) {
                                loading.setInfo(message("Insert") + ": " + insertCount + " " + code.getLevelCode().getName()
                                        + " " + code.getName() + " " + code.getLongitude() + " " + code.getLatitude());
                            }
                        } else {
                            ++failedCount;
                            if (loading != null) {
                                loading.setInfo(message("Failed") + ": " + failedCount + " " + code.getLevelCode().getName()
                                        + " " + code.getName() + " " + code.getLongitude() + " " + code.getLatitude());
                            }
                        }
                    }
                }
                conn.commit();
            }
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
        }
    }

    public static List<GeographyCode> readInternalCSV(File file) {
        List<GeographyCode> codes = new ArrayList();
        try (final CSVParser parser = CSVParser.parse(file, StandardCharsets.UTF_8, CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(',').withTrim().withNullString(""))) {
            List<String> names = parser.getHeaderNames();
            for (CSVRecord record : parser) {
                GeographyCode code = GeographyCodeTools.readIntenalRecord(names, record);
                if (code != null) {
                    codes.add(code);
                }
            }
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
        }
        return codes;
    }

    public static GeographyCode readIntenalRecord(List<String> names, CSVRecord record) {
        try {
            GeographyCode code = new GeographyCode();
            if (names.contains("gcid")) {
                code.setGcid(Long.valueOf(record.get("gcid")));
            } else {
                code.setGcid(Long.valueOf(record.get("dataid")));
            }
            code.setLevelCode(new GeographyCodeLevel(Integer.valueOf(record.get("levelid"))));
            try {
                code.setLongitude(Double.valueOf(record.get("longitude")));
                code.setLatitude(Double.valueOf(record.get("latitude")));
            } catch (Exception e) {
            }
            if (names.contains("altitude")) {
                try {
                    code.setAltitude(Double.valueOf(record.get("altitude")));
                } catch (Exception e) {
                }
            }
            if (names.contains("precision")) {
                try {
                    code.setPrecision(Double.valueOf(record.get("precision")));
                } catch (Exception e) {
                }
            }
            if (names.contains("coordinate_system")) {
                code.setCoordinateSystem(new CoordinateSystem(record.get("coordinate_system")));
            } else {
                code.setCoordinateSystem(CoordinateSystem.defaultCode());
            }
            if (names.contains("chinese_name")) {
                code.setChineseName(record.get("chinese_name"));
            }
            if (names.contains("english_name")) {
                code.setEnglishName(record.get("english_name"));
            }
            if (names.contains("code1")) {
                code.setCode1(record.get("code1"));
            }
            if (names.contains("code2")) {
                code.setCode2(record.get("code2"));
            }
            if (names.contains("code3")) {
                code.setCode3(record.get("code3"));
            }
            if (names.contains("code4")) {
                code.setCode4(record.get("code4"));
            }
            if (names.contains("code5")) {
                code.setCode5(record.get("code5"));
            }
            if (names.contains("alias1")) {
                code.setAlias1(record.get("alias1"));
            }
            if (names.contains("alias2")) {
                code.setAlias2(record.get("alias2"));
            }
            if (names.contains("alias3")) {
                code.setAlias3(record.get("alias3"));
            }
            if (names.contains("alias4")) {
                code.setAlias4(record.get("alias4"));
            }
            if (names.contains("alias5")) {
                code.setAlias5(record.get("alias5"));
            }
            if (names.contains("area") && record.get("area") != null) {
                code.setArea(Long.valueOf(record.get("area")));
            }
            if (names.contains("population") && record.get("population") != null) {
                code.setPopulation(Long.valueOf(record.get("population")));
            }
            try {
                if (record.get("continentid") != null) {
                    code.setContinent(Long.valueOf(record.get("continentid")));
                }
            } catch (Exception e) {
            }
            try {
                if (record.get("countryid") != null) {
                    code.setCountry(Long.valueOf(record.get("countryid")));
                }
            } catch (Exception e) {
            }
            try {
                if (record.get("provinceid") != null) {
                    code.setProvince(Long.valueOf(record.get("provinceid")));
                }
            } catch (Exception e) {
            }
            try {
                if (record.get("cityid") != null) {
                    code.setCity(Long.valueOf(record.get("cityid")));
                }
            } catch (Exception e) {
            }
            try {
                if (record.get("countyid") != null) {
                    code.setCounty(Long.valueOf(record.get("countyid")));
                }
            } catch (Exception e) {
            }
            try {
                if (record.get("townid") != null) {
                    code.setTown(Long.valueOf(record.get("townid")));
                }
            } catch (Exception e) {
            }
            try {
                if (record.get("villageid") != null) {
                    code.setVillage(Long.valueOf(record.get("villageid")));
                }
            } catch (Exception e) {
            }
            try {
                if (record.get("buildingid") != null) {
                    code.setBuilding(Long.valueOf(record.get("buildingid")));
                }
            } catch (Exception e) {
            }
            if (names.contains("comments")) {
                code.setComments(record.get("comments"));
            }
            return code;
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
            return null;
        }
    }

    public static List<String> externalNames() {
        try {
            List<String> columns = new ArrayList<>();
            columns.addAll(Arrays.asList(
                    message("Level"),
                    message("ChineseName"),
                    message("EnglishName"),
                    message("Longitude"),
                    message("Latitude"),
                    message("Altitude"),
                    message("Precision"),
                    message("CoordinateSystem"),
                    message("Code1"),
                    message("Code2"),
                    message("Code3"),
                    message("Code4"),
                    message("Code5"),
                    message("Alias1"),
                    message("Alias2"),
                    message("Alias3"),
                    message("Alias4"),
                    message("Alias5"),
                    message("SquareMeters"),
                    message("Population"),
                    message("Continent"),
                    message("Country"),
                    message("Province"),
                    message("City"),
                    message("County"),
                    message("Town"),
                    message("Village"),
                    message("Building"),
                    message("Comments")
            ));
            return columns;
        } catch (Exception e) {
            return null;
        }
    }

    public static GeographyCode readExtenalRecord(Connection conn, List<String> names, CSVRecord record) {
        try {
            String lang = names.contains(message("zh", "Level")) ? "zh" : "en";
            GeographyCode code = new GeographyCode();
            if (names.contains("level")) {
                code.setLevelCode(new GeographyCodeLevel(record.get("level")));
            } else if (names.contains(message(lang, "Level"))) {
                code.setLevelCode(new GeographyCodeLevel(record.get(message(lang, "Level"))));
            } else {
                return null;
            }
            if (names.contains("longitude")) {
                try {
                    code.setLongitude(Double.valueOf(record.get("longitude")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "Longitude"))) {
                try {
                    code.setLongitude(Double.valueOf(record.get(message(lang, "Longitude"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("latitude")) {
                try {
                    code.setLatitude(Double.valueOf(record.get("latitude")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "Latitude"))) {
                try {
                    code.setLatitude(Double.valueOf(record.get(message(lang, "Latitude"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("altitude")) {
                try {
                    code.setAltitude(Double.valueOf(record.get("altitude")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "Altitude"))) {
                try {
                    code.setAltitude(Double.valueOf(record.get(message(lang, "Altitude"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("precision")) {
                try {
                    code.setPrecision(Double.valueOf(record.get("precision")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "Precision"))) {
                try {
                    code.setPrecision(Double.valueOf(record.get(message(lang, "Precision"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("coordinate_system")) {
                code.setCoordinateSystem(new CoordinateSystem(record.get("coordinate_system")));
            } else if (names.contains(message(lang, "CoordinateSystem"))) {
                code.setCoordinateSystem(new CoordinateSystem(record.get(message(lang, "CoordinateSystem"))));
            } else {
                code.setCoordinateSystem(CoordinateSystem.defaultCode());
            }
            if (names.contains("chinese_name")) {
                code.setChineseName(record.get("chinese_name"));
            } else if (names.contains(message(lang, "ChineseName"))) {
                code.setChineseName(record.get(message(lang, "ChineseName")));
            }
            if (names.contains("english_name")) {
                code.setEnglishName(record.get("english_name"));
            } else if (names.contains(message(lang, "EnglishName"))) {
                code.setEnglishName(record.get(message(lang, "EnglishName")));
            }
            if (!code.valid()) {
                return null;
            }
            if (names.contains("code1")) {
                code.setCode1(record.get("code1"));
            } else if (names.contains(message(lang, "Code1"))) {
                code.setCode1(record.get(message(lang, "Code1")));
            }
            if (names.contains("code2")) {
                code.setCode2(record.get("code2"));
            } else if (names.contains(message(lang, "Code2"))) {
                code.setCode2(record.get(message(lang, "Code2")));
            }
            if (names.contains("code3")) {
                code.setCode3(record.get("code3"));
            } else if (names.contains(message(lang, "Code3"))) {
                code.setCode3(record.get(message(lang, "Code3")));
            }
            if (names.contains("code4")) {
                code.setCode4(record.get("code4"));
            } else if (names.contains(message(lang, "Code4"))) {
                code.setCode4(record.get(message(lang, "Code4")));
            }
            if (names.contains("code5")) {
                code.setCode5(record.get("code5"));
            } else if (names.contains(message(lang, "Code5"))) {
                code.setCode5(record.get(message(lang, "Code5")));
            }
            if (names.contains("alias1")) {
                code.setAlias1(record.get("alias1"));
            } else if (names.contains(message(lang, "Alias1"))) {
                code.setAlias1(record.get(message(lang, "Alias1")));
            }
            if (names.contains("alias2")) {
                code.setAlias2(record.get("alias2"));
            } else if (names.contains(message(lang, "Alias2"))) {
                code.setAlias2(record.get(message(lang, "Alias2")));
            }
            if (names.contains("alias3")) {
                code.setAlias3(record.get("alias3"));
            } else if (names.contains(message(lang, "Alias3"))) {
                code.setAlias3(record.get(message(lang, "Alias3")));
            }
            if (names.contains("alias4")) {
                code.setAlias4(record.get("alias4"));
            } else if (names.contains(message(lang, "Alias4"))) {
                code.setAlias4(record.get(message(lang, "Alias4")));
            }
            if (names.contains("alias5")) {
                code.setAlias5(record.get("alias5"));
            } else if (names.contains(message(lang, "Alias5"))) {
                code.setAlias5(record.get(message(lang, "Alias5")));
            }
            if (names.contains("area") && record.get("area") != null) {
                try {
                    code.setArea(1000000 * Long.valueOf(record.get("area")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "SquareMeters")) && record.get(message(lang, "SquareMeters")) != null) {
                try {
                    code.setArea(Long.valueOf(record.get(message(lang, "SquareMeters"))));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "SquareKilometers")) && record.get(message(lang, "SquareKilometers")) != null) {
                try {
                    code.setArea(1000000 * Long.valueOf(record.get(message(lang, "SquareKilometers"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("population") && record.get("population") != null) {
                try {
                    code.setPopulation(Long.valueOf(record.get("population")));
                } catch (Exception e) {
                }
            } else if (names.contains(message(lang, "Population")) && record.get(message(lang, "Population")) != null) {
                try {
                    code.setArea(Long.valueOf(record.get(message(lang, "Population"))));
                } catch (Exception e) {
                }
            }
            GeographyCode continentC = null;
            GeographyCode countryC = null;
            GeographyCode provinceC = null;
            GeographyCode cityC = null;
            GeographyCode countyC = null;
            GeographyCode townC = null;
            GeographyCode villageC = null;
            GeographyCode buildingC = null;
            long continentid;
            long countryid;
            long provinceid;
            long cityid;
            long countyid;
            long townid;
            long villageid;
            long buildingid;
            String continent = names.contains(message(lang, "Continent"))
                    ? record.get(message(lang, "Continent")) : (names.contains("continent") ? record.get("continent") : null);
            if (continent != null && !continent.isBlank()) {
                continentC = TableGeographyCode.readCode(conn, 2, continent, false);
            }
            if (continentC != null) {
                continentid = continentC.getGcid();
            } else {
                continentid = -1;
            }
            String country = names.contains(message(lang, "Country"))
                    ? record.get(message(lang, "Country")) : (names.contains("country") ? record.get("country") : null);
            if (country != null && !country.isBlank()) {
                final String sql = "SELECT * FROM Geography_Code WHERE " + " level=3 AND continent=? AND (" + TableGeographyCode.NameEqual + ")";
                try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setLong(1, continentid);
                    TableGeographyCode.setNameParameters(statement, country, 1);
                    countryC = TableGeographyCode.readCode(conn, statement, false);
                }
            }
            if (countryC != null) {
                countryid = countryC.getGcid();
            } else {
                countryid = -1;
            }
            String province = names.contains(message(lang, "Province"))
                    ? record.get(message(lang, "Province")) : (names.contains("province") ? record.get("province") : null);
            if (province != null && !province.isBlank()) {
                final String sql = "SELECT * FROM Geography_Code WHERE " + " level=4 AND continent=? AND country=? AND (" + TableGeographyCode.NameEqual + ")";
                try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setLong(1, continentid);
                    statement.setLong(2, countryid);
                    TableGeographyCode.setNameParameters(statement, province, 2);
                    provinceC = TableGeographyCode.readCode(conn, statement, false);
                }
            }
            if (provinceC != null) {
                provinceid = provinceC.getGcid();
            } else {
                provinceid = -1;
            }
            String city = names.contains(message(lang, "City"))
                    ? record.get(message(lang, "City")) : (names.contains("city") ? record.get("city") : null);
            if (city != null && !city.isBlank()) {
                final String sql = "SELECT * FROM Geography_Code WHERE " + " level=5 AND continent=? AND country=? AND province=?" + " AND (" + TableGeographyCode.NameEqual + ")";
                try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setLong(1, continentid);
                    statement.setLong(2, countryid);
                    statement.setLong(3, provinceid);
                    TableGeographyCode.setNameParameters(statement, city, 3);
                    cityC = TableGeographyCode.readCode(conn, statement, false);
                }
            }
            if (cityC != null) {
                cityid = cityC.getGcid();
            } else {
                cityid = -1;
            }
            String county = names.contains(message(lang, "County"))
                    ? record.get(message(lang, "County")) : (names.contains("county") ? record.get("county") : null);
            if (county != null && !county.isBlank()) {
                final String sql = "SELECT * FROM Geography_Code WHERE " + " level=6 AND continent=? AND country=? AND province=?" + " AND city=?" + " AND (" + TableGeographyCode.NameEqual + ")";
                try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setLong(1, continentid);
                    statement.setLong(2, countryid);
                    statement.setLong(3, provinceid);
                    statement.setLong(4, cityid);
                    TableGeographyCode.setNameParameters(statement, county, 4);
                    countyC = TableGeographyCode.readCode(conn, statement, false);
                }
            }
            if (countyC != null) {
                countyid = countyC.getGcid();
            } else {
                countyid = -1;
            }
            String town = names.contains(message(lang, "Town"))
                    ? record.get(message(lang, "Town")) : (names.contains("town") ? record.get("town") : null);
            if (town != null && !town.isBlank()) {
                final String sql = "SELECT * FROM Geography_Code WHERE " + " level=7 AND continent=? AND country=? AND province=?" + " AND city=? AND county=?" + " AND (" + TableGeographyCode.NameEqual + ")";
                try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setLong(1, continentid);
                    statement.setLong(2, countryid);
                    statement.setLong(3, provinceid);
                    statement.setLong(4, cityid);
                    statement.setLong(5, countyid);
                    TableGeographyCode.setNameParameters(statement, town, 5);
                    townC = TableGeographyCode.readCode(conn, statement, false);
                }
            }
            if (townC != null) {
                townid = townC.getGcid();
            } else {
                townid = -1;
            }
            String village = names.contains(message(lang, "Village"))
                    ? record.get(message(lang, "Village")) : (names.contains("village") ? record.get("village") : null);
            if (village != null && !village.isBlank()) {
                final String sql = "SELECT * FROM Geography_Code WHERE " + " level=8 AND continent=? AND country=? AND province=?" + " AND city=? AND county=? AND town=?" + " AND (" + TableGeographyCode.NameEqual + ")";
                try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setLong(1, continentid);
                    statement.setLong(2, countryid);
                    statement.setLong(3, provinceid);
                    statement.setLong(4, cityid);
                    statement.setLong(5, countyid);
                    statement.setLong(6, townid);
                    TableGeographyCode.setNameParameters(statement, village, 6);
                    villageC = TableGeographyCode.readCode(conn, statement, false);
                }
            }
            if (villageC != null) {
                villageid = villageC.getGcid();
            } else {
                villageid = -1;
            }
            String building = names.contains(message(lang, "Building"))
                    ? record.get(message(lang, "Building")) : (names.contains("building") ? record.get("building") : null);
            if (building != null && !building.isBlank()) {
                final String sql = "SELECT * FROM Geography_Code WHERE " + " level=9 AND continent=? AND country=? AND province=?" + " AND city=? AND county=? AND town=? AND village=?" + " AND (" + TableGeographyCode.NameEqual + ")";
                try (final PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setLong(1, continentid);
                    statement.setLong(2, countryid);
                    statement.setLong(3, provinceid);
                    statement.setLong(4, cityid);
                    statement.setLong(5, countyid);
                    statement.setLong(6, townid);
                    statement.setLong(7, villageid);
                    TableGeographyCode.setNameParameters(statement, building, 7);
                    buildingC = TableGeographyCode.readCode(conn, statement, false);
                }
            }
            if (buildingC != null) {
                buildingid = buildingC.getGcid();
            } else {
                buildingid = -1;
            }
            code.setContinent(continentid);
            code.setCountry(countryid);
            code.setProvince(provinceid);
            code.setCity(cityid);
            code.setCounty(countyid);
            code.setTown(townid);
            code.setVillage(villageid);
            code.setBuilding(buildingid);
            if (names.contains("comments")) {
                code.setComments(record.get("comments"));
            }
            return code;
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
            return null;
        }
    }

    // https://www.geonames.org/countries/
    // ISO-3166-alpha2	ISO-3166-alpha3	ISO-3166-numeric	fips	Country	Capital	Area	Population	Continent
    // AD	AND	020	AN	Andorra	Andorra la Vella	468.0	84,000	EU
    public static void importGeonamesCountriesCodes() {
        File file = new File("D:\\\u739b\u745e\\Mybox\\\u5730\u7406\u4ee3\u7801\\countries\\geonames_contries.csv");
        GeographyCode code;
        try (final Connection conn = DriverManager.getConnection(DerbyBase.protocol + dbHome() + DerbyBase.login);
                final CSVParser parser = CSVParser.parse(file, StandardCharsets.UTF_8, CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter('\t').withTrim().withNullString(""))) {
            conn.setAutoCommit(false);
            for (CSVRecord record : parser) {
                String country = record.get("Country");
                String sql = "SELECT * FROM Geography_Code WHERE level=3 AND ( " + TableGeographyCode.nameEqual(country) + " )";
                try (final ResultSet results = conn.createStatement().executeQuery(sql)) {
                    if (results.next()) {
                        code = TableGeographyCode.readResults(results);
                    } else {
                        code = null;
                    }
                }
                if (code == null) {
                    code = new GeographyCode();
                    code.setEnglishName(country);
                    code.setLevelCode(new GeographyCodeLevel(3));
                    AppVariables.logger.debug(record.get("ISO-3166-alpha3") + " " + country + " " + record.get("Area") + " " + record.get("Population") + " " + record.get("Continent"));
                } else {
                    if (code.getEnglishName() == null) {
                        code.setEnglishName(country);
                        AppVariables.logger.debug(code.getChineseName() + " " + country);
                    } else if (!code.getEnglishName().equals(country)) {
                        AppVariables.logger.debug(code.getChineseName() + " " + code.getEnglishName() + " " + country);
                        if (code.getAlias1() == null || code.getAlias1().equals(country)) {
                            code.setAlias1(country);
                        } else if (code.getAlias2() == null || code.getAlias2().equals(country)) {
                            code.setAlias2(country);
                        } else if (code.getAlias3() == null || code.getAlias3().equals(country)) {
                            code.setAlias3(country);
                        } else if (code.getAlias4() == null || code.getAlias4().equals(country)) {
                            code.setAlias4(country);
                        } else {
                            code.setAlias5(country);
                        }
                    }
                }
                code.setCode1(record.get("ISO-3166-alpha3"));
                code.setCode2(record.get("ISO-3166-alpha2"));
                code.setCode3(record.get("ISO-3166-numeric"));
                code.setCode4(record.get("fips"));
                code.setArea(Math.round(Double.valueOf(record.get("Area").replaceAll(",", ""))));
                code.setPopulation(Math.round(Double.valueOf(record.get("Population").replaceAll(",", ""))));
                TableGeographyCode.write(conn, code);
            }
            conn.commit();
            String sql = "SELECT * FROM Geography_Code WHERE level=3 ";
            List<GeographyCode> codes = TableGeographyCode.queryCodes(conn, sql, false);
            writeInternalCSV(new File("D:\\tmp\\1\\Geography_Code_countries_internal.csv"), codes);
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
        }
    }

    public static void importChinaCitiesCoordinates() {
        File file = new File("D:\\\u739b\u745e\\Mybox\\\u5730\u7406\u4ee3\u7801\\cities\\citiesCoordinates.csv");
        GeographyCode code;
        try (final Connection conn = DriverManager.getConnection(DerbyBase.protocol + dbHome() + DerbyBase.login);
                final CSVParser parser = CSVParser.parse(file, StandardCharsets.UTF_8, CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(',').withTrim().withNullString(""))) {
            conn.setAutoCommit(false);
            for (CSVRecord record : parser) {
                String city = record.get("chinese_name");
                double longitude = Double.valueOf(record.get("longitude"));
                double latitude = Double.valueOf(record.get("latitude"));
                String sql = "SELECT * FROM Geography_Code WHERE level=5 AND ( " + TableGeographyCode.nameEqual(city) + " )";
                try (final ResultSet results = conn.createStatement().executeQuery(sql)) {
                    if (results.next()) {
                        code = TableGeographyCode.readResults(results);
                    } else {
                        code = null;
                    }
                }
                if (code == null) {
                    //                    encode = new GeographyCode();
                    //                    encode.setEnglishName(country);
                    //                    encode.setLevel(new GeographyCodeLevel(3));
                    AppVariables.logger.debug(city + " " + longitude + " " + latitude);
                } else {
                    code.setLongitude(longitude);
                    code.setLatitude(latitude);
                    code.setCode1(record.get("code1"));
                    code.setCode2(record.get("code2"));
                    code.setCode3(record.get("code3"));
                    TableGeographyCode.write(conn, code);
                    //                    logger.debug(country + " " + encode.getLongitude() + " " + encode.getLatitude() + " " + longitude + " " + latitude);
                }
            }
            conn.commit();
            String sql = "SELECT * FROM Geography_Code WHERE " + " country=100 AND level=5 ORDER BY gcid ";
            List<GeographyCode> codes = TableGeographyCode.queryCodes(conn, sql, false);
            writeInternalCSV(new File("D:\\tmp\\1\\Geography_Code_china_cities_internal.csv"), codes);
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
        }
    }

    public static void importCountriesCoordinate() {
        File file = new File("D:\\\u739b\u745e\\Mybox\\\u5730\u7406\u4ee3\u7801\\countries\\countries_coordinate.csv");
        GeographyCode code;
        try (final Connection conn = DriverManager.getConnection(DerbyBase.protocol + dbHome() + DerbyBase.login);
                final CSVParser parser = CSVParser.parse(file, StandardCharsets.UTF_8, CSVFormat.DEFAULT.withDelimiter(',').withTrim().withNullString(""))) {
            conn.setAutoCommit(false);
            for (CSVRecord record : parser) {
                String country = record.get(0);
                double longitude = Double.valueOf(record.get(1));
                double latitude = Double.valueOf(record.get(2));
                String sql = "SELECT * FROM Geography_Code WHERE level=3 AND ( " + TableGeographyCode.nameEqual(country) + " )";
                try (final ResultSet results = conn.createStatement().executeQuery(sql)) {
                    if (results.next()) {
                        code = TableGeographyCode.readResults(results);
                    } else {
                        code = null;
                    }
                }
                if (code == null) {
                    //                    encode = new GeographyCode();
                    //                    encode.setEnglishName(country);
                    //                    encode.setLevel(new GeographyCodeLevel(3));
                    AppVariables.logger.debug(country + " " + longitude + " " + latitude);
                } else {
                    code.setLongitude(DoubleTools.scale(longitude, 6));
                    code.setLatitude(DoubleTools.scale(latitude, 6));
                    TableGeographyCode.write(conn, code);
                    //                    logger.debug(country + " " + encode.getLongitude() + " " + encode.getLatitude() + " " + longitude + " " + latitude);
                }
            }
            conn.commit();
            String sql = "SELECT * FROM Geography_Code WHERE level=3 ORDER BY gcid ";
            List<GeographyCode> codes = TableGeographyCode.queryCodes(conn, sql, false);
            writeInternalCSV(new File("D:\\tmp\\1\\Geography_Code_countries_internal.csv"), codes);
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
        }
    }

    /*
        Export
     */
    public static void exportPredefined() {
        try (final Connection conn = DriverManager.getConnection(DerbyBase.protocol + dbHome() + DerbyBase.login)) {
            conn.setReadOnly(true);
            File file = new File("D:\\tmp\\3\\Geography_Code_global_internal.csv");
            String sql = "SELECT * FROM Geography_Code WHERE level<3 ORDER BY gcid";
            writeInternalCSV(conn, file, sql);
            file = new File("D:\\tmp\\3\\Geography_Code_countries_internal.csv");
            sql = "SELECT * FROM Geography_Code WHERE level=3 ORDER BY gcid";
            writeInternalCSV(conn, file, sql);
            file = new File("D:\\tmp\\3\\Geography_Code_china_provinces_internal.csv");
            sql = "SELECT * FROM Geography_Code WHERE level=4 AND country=100 ORDER BY gcid";
            writeInternalCSV(conn, file, sql);
            file = new File("D:\\tmp\\3\\Geography_Code_china_cities_internal.csv");
            sql = "SELECT * FROM Geography_Code WHERE level=5 AND country=100 ORDER BY gcid";
            writeInternalCSV(conn, file, sql);
            file = new File("D:\\tmp\\3\\Geography_Code_china_counties_internal.csv");
            sql = "SELECT * FROM Geography_Code WHERE level=6 AND country=100 ORDER BY gcid";
            writeInternalCSV(conn, file, sql);
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
        }
    }

    public static void writeInternalCSVHeader(CSVPrinter printer) {
        try {
            printer.printRecord("gcid", "levelid", "longitude", "latitude", "chinese_name", "english_name",
                    "code1", "code2", "code3", "code4", "code5", "alias1", "alias2", "alias3", "alias4", "alias5",
                    "area", "population", "owner", "continentid", "countryid", "provinceid", "cityid",
                    "countyid", "townid", "villageid", "buildingid", "comments");
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
        }
    }

    protected static void writeInternalCSV(File file, String sql) {
        try (final Connection conn = DriverManager.getConnection(DerbyBase.protocol + dbHome() + DerbyBase.login)) {
            conn.setReadOnly(true);
            writeInternalCSV(conn, file, sql);
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
        }
    }

    protected static void writeInternalCSV(Connection conn, File file, String sql) {
        try (final CSVPrinter printer = new CSVPrinter(new FileWriter(file, Charset.forName("utf-8")), CSVFormat.DEFAULT)) {
            writeInternalCSVHeader(printer);
            try (final ResultSet results = conn.createStatement().executeQuery(sql)) {
                while (results.next()) {
                    GeographyCode code = TableGeographyCode.readResults(results);
                    writeInternalCSV(printer, code);
                }
            }
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
        }
    }

    public static void writeInternalCSV(File file, List<GeographyCode> codes) {
        writeInternalCSV(file, codes, true);
    }

    // externalValues of internal format are not related to languages, and headers are in English
    public static void writeInternalCSV(File file, List<GeographyCode> codes, boolean writeHeader) {
        try (final CSVPrinter printer = new CSVPrinter(new FileWriter(file, Charset.forName("utf-8")), CSVFormat.DEFAULT)) {
            if (writeHeader) {
                writeInternalCSVHeader(printer);
            }
            for (GeographyCode code : codes) {
                writeInternalCSV(printer, code);
            }
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
        }
    }

    public static void writeInternalCSV(CSVPrinter printer, GeographyCode code) {
        try {
            printer.printRecord(
                    code.getGcid(),
                    code.getLevel(),
                    code.getLongitude(),
                    code.getLatitude(),
                    code.getChineseName() == null ? "" : code.getChineseName(),
                    code.getEnglishName() == null ? "" : code.getEnglishName(),
                    code.getCode1() == null ? "" : code.getCode1(),
                    code.getCode2() == null ? "" : code.getCode2(),
                    code.getCode3() == null ? "" : code.getCode3(),
                    code.getCode4() == null ? "" : code.getCode4(),
                    code.getCode5() == null ? "" : code.getCode5(),
                    code.getAlias1() == null ? "" : code.getAlias1(),
                    code.getAlias2() == null ? "" : code.getAlias2(),
                    code.getAlias3() == null ? "" : code.getAlias3(),
                    code.getAlias4() == null ? "" : code.getAlias4(),
                    code.getAlias5() == null ? "" : code.getAlias5(),
                    code.getArea() > 0 ? code.getArea() : "",
                    code.getPopulation() > 0 ? code.getPopulation() : "",
                    code.getOwner() > 0 ? code.getOwner() : "",
                    code.getContinent() > 0 ? code.getContinent() : "",
                    code.getCountry() > 0 ? code.getCountry() : "",
                    code.getProvince() > 0 ? code.getProvince() : "",
                    code.getCity() > 0 ? code.getCity() : "",
                    code.getCounty() > 0 ? code.getCounty() : "",
                    code.getTown() > 0 ? code.getTown() : "",
                    code.getVillage() > 0 ? code.getVillage() : "",
                    code.getBuilding() > 0 ? code.getBuilding() : "",
                    code.getComments() == null ? "" : code.getComments());
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
        }
    }

    public static List<String> externalValues(GeographyCode code) {
        List<String> row = new ArrayList<>();
        row.addAll(Arrays.asList(
                code.getLevelCode() == null ? "" : code.getLevelCode().getName(),
                code.getChineseName() == null ? "" : code.getChineseName(),
                code.getEnglishName() == null ? "" : code.getEnglishName(),
                code.getLongitude() >= -180 && code.getLongitude() <= 180 ? code.getLongitude() + "" : "",
                code.getLatitude() >= -90 && code.getLatitude() <= 90 ? code.getLatitude() + "" : "",
                code.getAltitude() != Double.MAX_VALUE ? code.getAltitude() + "" : "",
                code.getPrecision() != Double.MAX_VALUE ? code.getPrecision() + "" : "",
                code.getCoordinateSystem() != null ? code.getCoordinateSystem().name() : "",
                code.getCode1() == null ? "" : code.getCode1(),
                code.getCode2() == null ? "" : code.getCode2(),
                code.getCode3() == null ? "" : code.getCode3(),
                code.getCode4() == null ? "" : code.getCode4(),
                code.getCode5() == null ? "" : code.getCode5(),
                code.getAlias1() == null ? "" : code.getAlias1(),
                code.getAlias2() == null ? "" : code.getAlias2(),
                code.getAlias3() == null ? "" : code.getAlias3(),
                code.getAlias4() == null ? "" : code.getAlias4(),
                code.getAlias5() == null ? "" : code.getAlias5(),
                code.getArea() > 0 ? code.getArea() + "" : "",
                code.getPopulation() > 0 ? code.getPopulation() + "" : "",
                code.getContinentName() == null ? "" : code.getContinentName(),
                code.getCountryName() == null ? "" : code.getCountryName(),
                code.getProvinceName() == null ? "" : code.getProvinceName(),
                code.getCityName() == null ? "" : code.getCityName(),
                code.getCountyName() == null ? "" : code.getCountyName(),
                code.getTownName() == null ? "" : code.getTownName(),
                code.getVillageName() == null ? "" : code.getVillageName(),
                code.getBuildingName() == null ? "" : code.getBuildingName(),
                code.getComments() == null ? "" : code.getComments()
        ));
        return row;
    }

    public static void writeExternalCSVHeader(CSVPrinter printer) {
        try {
            printer.printRecord(GeographyCodeTools.externalNames());
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
        }
    }

    public static void writeExternalCSV(File file, List<GeographyCode> codes) {
        writeExternalCSV(file, codes, true);
    }

    // externalValues of external format are related to languages
    public static void writeExternalCSV(File file, List<GeographyCode> codes, boolean writeHeader) {
        try (final CSVPrinter printer = new CSVPrinter(new FileWriter(file, Charset.forName("utf-8")), CSVFormat.DEFAULT)) {
            if (writeHeader) {
                writeExternalCSVHeader(printer);
            }
            for (GeographyCode code : codes) {
                writeExternalCSV(printer, code);
            }
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
        }
    }

    public static void writeExternalCSV(CSVPrinter printer, GeographyCode code) {
        try {
            printer.printRecord(GeographyCodeTools.externalValues(code));
        } catch (Exception e) {
            AppVariables.logger.debug(e.toString());
        }
    }

    public static void exportExternalCSV(File file) {
        List<GeographyCode> codes = TableGeographyCode.readAll(true);
        if (codes != null && !codes.isEmpty()) {
            writeExternalCSV(file, codes);
        }
    }

    public static void writeExcel(File file, List<GeographyCode> codes) {
        try {
            if (file == null || codes == null || codes.isEmpty()) {
                return;
            }
            XSSFWorkbook wb = new XSSFWorkbook();
            XSSFSheet sheet = wb.createSheet("sheet1");
            List<String> columns = writeExcelHeader(wb, sheet);
            for (int i = 0; i < codes.size(); i++) {
                GeographyCode code = codes.get(i);
                writeExcel(sheet, i, code);
            }
            for (int i = 0; i < columns.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            try (final OutputStream fileOut = new FileOutputStream(file)) {
                wb.write(fileOut);
            }
        } catch (Exception e) {
        }
    }

    public static void writeExcel(XSSFSheet sheet, int i, GeographyCode code) {
        try {
            List<String> row = GeographyCodeTools.externalValues(code);
            XSSFRow sheetRow = sheet.createRow(i + 1);
            for (int j = 0; j < row.size(); j++) {
                XSSFCell cell = sheetRow.createCell(j);
                cell.setCellValue(row.get(j));
            }
        } catch (Exception e) {
        }
    }

    public static List<String> writeExcelHeader(XSSFWorkbook wb, XSSFSheet sheet) {
        try {
            List<String> columns = GeographyCodeTools.externalNames();
            sheet.setDefaultColumnWidth(20);
            XSSFRow titleRow = sheet.createRow(0);
            XSSFCellStyle horizontalCenter = wb.createCellStyle();
            horizontalCenter.setAlignment(HorizontalAlignment.CENTER);
            for (int i = 0; i < columns.size(); i++) {
                XSSFCell cell = titleRow.createCell(i);
                cell.setCellValue(columns.get(i));
                cell.setCellStyle(horizontalCenter);
            }
            return columns;
        } catch (Exception e) {
            return null;
        }
    }

    public static void writeJson(File file, List<GeographyCode> codes) {
        if (file == null || codes == null || codes.isEmpty()) {
            return;
        }
        String indent = "    ";
        try (final FileWriter writer = new FileWriter(file, Charset.forName("utf-8"))) {
            StringBuilder s = new StringBuilder();
            s.append("{\"GeographyCodes\": [\n");
            writer.write(s.toString());
            for (int i = 0; i < codes.size(); i++) {
                GeographyCode code = codes.get(i);
                s = writeJson(writer, indent, code);
                if (i == codes.size() - 1) {
                    s.append(indent).append("}\n");
                } else {
                    s.append(indent).append("},\n");
                }
                writer.write(s.toString());
            }
            writer.write("]}\n");
        } catch (Exception e) {
            AppVariables.logger.error(e.toString());
        }
    }

    public static StringBuilder writeJson(FileWriter writer, String indent, GeographyCode code) {
        try {
            StringBuilder s = new StringBuilder();
            s.append(indent).append("{\"level\":\"").append(code.getLevelCode().getName()).append("\"");
            if (code.getLongitude() >= -180 && code.getLongitude() <= 180) {
                s.append(",\"longitude\":").append(code.getLongitude());
            }
            if (code.getLatitude() >= -90 && code.getLatitude() <= 90) {
                s.append(",\"latitude\":").append(code.getLatitude());
            }
            if (code.getAltitude() != Double.MAX_VALUE) {
                s.append(",\"altitude\":").append(code.getAltitude());
            }
            if (code.getPrecision() != Double.MAX_VALUE) {
                s.append(",\"precision\":").append(code.getPrecision());
            }
            if (code.getCoordinateSystem() != null) {
                s.append(",\"coordinate_system\":\"").append(code.getCoordinateSystem().name()).append("\"");
            }
            if (code.getChineseName() != null) {
                s.append(",\"chinese_name\":\"").append(code.getChineseName()).append("\"");
            }
            if (code.getEnglishName() != null) {
                s.append(",\"english_name\":\"").append(code.getEnglishName()).append("\"");
            }
            if (code.getCode1() != null) {
                s.append(",\"code1\":\"").append(code.getCode1()).append("\"");
            }
            if (code.getCode2() != null) {
                s.append(",\"code2\":\"").append(code.getCode2()).append("\"");
            }
            if (code.getCode3() != null) {
                s.append(",\"code3\":\"").append(code.getCode3()).append("\"");
            }
            if (code.getCode4() != null) {
                s.append(",\"code4\":\"").append(code.getCode4()).append("\"");
            }
            if (code.getCode5() != null) {
                s.append(",\"code5\":\"").append(code.getCode5()).append("\"");
            }
            if (code.getAlias1() != null) {
                s.append(",\"alias1\":\"").append(code.getAlias1()).append("\"");
            }
            if (code.getAlias2() != null) {
                s.append(",\"alias2\":\"").append(code.getAlias2()).append("\"");
            }
            if (code.getAlias3() != null) {
                s.append(",\"alias3\":\"").append(code.getAlias3()).append("\"");
            }
            if (code.getAlias4() != null) {
                s.append(",\"alias4\":\"").append(code.getAlias4()).append("\"");
            }
            if (code.getAlias5() != null) {
                s.append(",\"alias5\":\"").append(code.getAlias5()).append("\"");
            }
            if (code.getArea() > 0) {
                s.append(",\"area\":").append(code.getArea());
            }
            if (code.getPopulation() > 0) {
                s.append(",\"population\":").append(code.getPopulation());
            }
            if (code.getContinentName() != null) {
                s.append(",\"continent\":\"").append(code.getContinentName()).append("\"");
            }
            if (code.getCountryName() != null) {
                s.append(",\"country\":\"").append(code.getCountryName()).append("\"");
            }
            if (code.getProvinceName() != null) {
                s.append(",\"province\":\"").append(code.getProvinceName()).append("\"");
            }
            if (code.getCityName() != null) {
                s.append(",\"city\":\"").append(code.getCityName()).append("\"");
            }
            if (code.getCountyName() != null) {
                s.append(",\"county\":\"").append(code.getCountyName()).append("\"");
            }
            if (code.getTownName() != null) {
                s.append(",\"town\":\"").append(code.getTownName()).append("\"");
            }
            if (code.getVillageName() != null) {
                s.append(",\"village\":\"").append(code.getVillageName()).append("\"");
            }
            if (code.getBuildingName() != null) {
                s.append(",\"building\":\"").append(code.getBuildingName()).append("\"");
            }
            if (code.getComments() != null) {
                s.append(",\"comments\":\"").append(code.getComments()).append("\"");
            }
            return s;
        } catch (Exception e) {
            AppVariables.logger.error(e.toString());
            return null;
        }
    }

    public static void writeXml(File file, List<GeographyCode> codes) {
        if (file == null || codes == null || codes.isEmpty()) {
            return;
        }
        String indent = "    ";
        try (final FileWriter writer = new FileWriter(file, Charset.forName("utf-8"))) {
            StringBuilder s = new StringBuilder();
            s.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n").append("<GeographyCodes>\n");
            writer.write(s.toString());
            for (GeographyCode code : codes) {
                writeXml(writer, indent, code);
            }
            writer.write("</GeographyCodes>\n");
        } catch (Exception e) {
            AppVariables.logger.error(e.toString());
        }
    }

    public static void writeXml(FileWriter writer, String indent, GeographyCode code) {
        try {
            StringBuilder s = new StringBuilder();
            s.append(indent).append("<GeographyCode ").append(" level=\"").append(code.getLevelCode().getName()).append("\" ");
            if (code.getLongitude() >= -180 && code.getLongitude() <= 180) {
                s.append(" longitude=\"").append(code.getLongitude()).append("\"");
            }
            if (code.getLatitude() >= -90 && code.getLatitude() <= 90) {
                s.append(" latitude=\"").append(code.getLatitude()).append("\"");
            }
            if (code.getAltitude() != Double.MAX_VALUE) {
                s.append(" altitude=\"").append(code.getAltitude()).append("\"");
            }
            if (code.getPrecision() != Double.MAX_VALUE) {
                s.append(" precision=\"").append(code.getPrecision()).append("\"");
            }
            if (code.getCoordinateSystem() != null) {
                s.append(" coordinate_system=\"").append(code.getCoordinateSystem().name()).append("\"");
            }
            if (code.getChineseName() != null) {
                s.append(" chinese_name=\"").append(code.getChineseName()).append("\"");
            }
            if (code.getEnglishName() != null) {
                s.append(" english_name=\"").append(code.getEnglishName()).append("\"");
            }
            if (code.getCode1() != null) {
                s.append(" code1=\"").append(code.getCode1()).append("\"");
            }
            if (code.getCode2() != null) {
                s.append(" code2=\"").append(code.getCode2()).append("\"");
            }
            if (code.getCode3() != null) {
                s.append(" code3=\"").append(code.getCode3()).append("\"");
            }
            if (code.getCode4() != null) {
                s.append(" code4=\"").append(code.getCode4()).append("\"");
            }
            if (code.getCode5() != null) {
                s.append(" code5=\"").append(code.getCode5()).append("\"");
            }
            if (code.getAlias1() != null) {
                s.append(" alias1=\"").append(code.getAlias1()).append("\"");
            }
            if (code.getAlias2() != null) {
                s.append(" alias2=\"").append(code.getAlias2()).append("\"");
            }
            if (code.getAlias3() != null) {
                s.append(" alias3=\"").append(code.getAlias3()).append("\"");
            }
            if (code.getAlias4() != null) {
                s.append(" alias4=\"").append(code.getAlias4()).append("\"");
            }
            if (code.getAlias5() != null) {
                s.append(" alias5=\"").append(code.getAlias5()).append("\"");
            }
            if (code.getArea() > 0) {
                s.append(" area=\"").append(code.getArea()).append("\"");
            }
            if (code.getPopulation() > 0) {
                s.append(" population=\"").append(code.getPopulation()).append("\"");
            }
            if (code.getContinentName() != null) {
                s.append(" continent=\"").append(code.getContinentName()).append("\"");
            }
            if (code.getCountryName() != null) {
                s.append(" country=\"").append(code.getCountryName()).append("\"");
            }
            if (code.getProvinceName() != null) {
                s.append(" province=\"").append(code.getProvinceName()).append("\"");
            }
            if (code.getCityName() != null) {
                s.append(" city=\"").append(code.getCityName()).append("\"");
            }
            if (code.getCountyName() != null) {
                s.append(" county=\"").append(code.getCountyName()).append("\"");
            }
            if (code.getTownName() != null) {
                s.append(" town=\"").append(code.getTownName()).append("\"");
            }
            if (code.getVillageName() != null) {
                s.append(" village=\"").append(code.getVillageName()).append("\"");
            }
            if (code.getBuildingName() != null) {
                s.append(" building=\"").append(code.getBuildingName()).append("\"");
            }
            if (code.getComments() != null) {
                s.append(" comments=\"").append(code.getComments()).append("\"");
            }
            s.append(" />\n");
            writer.write(s.toString());
        } catch (Exception e) {
            AppVariables.logger.error(e.toString());
        }
    }

    public static void writeHtml(File file, List<GeographyCode> codes, String title) {
        try {
            if (file == null || codes == null || codes.isEmpty()) {
                return;
            }
            List<String> names = GeographyCodeTools.externalNames();
            StringTable table = new StringTable(names, title);
            for (GeographyCode code : codes) {
                List<String> row = GeographyCodeTools.externalValues(code);
                table.add(row);
            }
            FileTools.writeFile(file, StringTable.tableHtml(table));
        } catch (Exception e) {
        }
    }


    /*
        Convert
     */
    public static GeographyCode toCGCS2000(GeographyCode code) {
        GeographyCode converted = toWGS84(code);
//        if (converted != null) {
//            converted.setCoordinateSystem(CoordinateSystem.CGCS2000());
//        }
        return converted;
    }

    public static List<GeographyCode> toCGCS2000(List<GeographyCode> codes) {
        if (codes == null) {
            return codes;
        }
        List<GeographyCode> newCodes = new ArrayList<>();
        for (GeographyCode code : codes) {
            if (!code.validCoordinate()) {
                continue;
            }
            GeographyCode newCode = toCGCS2000(code);
            if (newCode != null) {
                newCodes.add(newCode);
            }
        }
        return newCodes;
    }

    public static GeographyCode toWGS84(GeographyCode code) {
        try {
            if (code == null || !code.validCoordinate()
                    || code.getCoordinateSystem() == null) {
                return code;
            }
            CoordinateSystem cs = code.getCoordinateSystem();
            double[] coordinate;
            switch (cs.getValue()) {
                case GCJ_02:
                    coordinate = CoordinateConverter.GCJ02ToWGS84(code.getLongitude(), code.getLatitude());
                    break;
                case BD_09:
                    coordinate = CoordinateConverter.BD09ToGCJ02(code.getLongitude(), code.getLatitude());
                    coordinate = CoordinateConverter.GCJ02ToWGS84(coordinate[0], coordinate[1]);
                    break;
                case Mapbar:
                    coordinate = toGCJ02ByWebService(code.getCoordinateSystem(),
                            code.getLongitude(), code.getLatitude());
                    coordinate = CoordinateConverter.GCJ02ToWGS84(coordinate[0], coordinate[1]);
                    break;
                case CGCS2000:
                case WGS_84:
                default:
                    return code;
            }
            GeographyCode newCode = (GeographyCode) code.clone();
            newCode.setLongitude(coordinate[0]);
            newCode.setLatitude(coordinate[1]);
            newCode.setCoordinateSystem(CoordinateSystem.WGS84());
            return newCode;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<GeographyCode> toWGS84(List<GeographyCode> codes) {
        if (codes == null) {
            return codes;
        }
        List<GeographyCode> newCodes = new ArrayList<>();
        for (GeographyCode code : codes) {
            if (!code.validCoordinate()) {
                continue;
            }
            GeographyCode newCode = toWGS84(code);
            if (newCode != null) {
                newCodes.add(newCode);
            }
        }
        return newCodes;
    }

    public static GeographyCode toGCJ02(GeographyCode code) {
        try {
            if (code == null || !code.validCoordinate()
                    || code.getCoordinateSystem() == null) {
                return code;
            }
            CoordinateSystem cs = code.getCoordinateSystem();
            double[] coordinate;
            switch (cs.getValue()) {
                case CGCS2000:
                case WGS_84:
                    coordinate = CoordinateConverter.WGS84ToGCJ02(code.getLongitude(), code.getLatitude());
                    break;
                case BD_09:
                    coordinate = CoordinateConverter.BD09ToGCJ02(code.getLongitude(), code.getLatitude());
                    break;
                case Mapbar:
                    coordinate = toGCJ02ByWebService(code.getCoordinateSystem(),
                            code.getLongitude(), code.getLatitude());
                    break;
                case GCJ_02:
                default:
                    return code;
            }
            GeographyCode newCode = (GeographyCode) code.clone();
            newCode.setLongitude(coordinate[0]);
            newCode.setLatitude(coordinate[1]);
            newCode.setCoordinateSystem(CoordinateSystem.GCJ02());
            return newCode;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<GeographyCode> toGCJ02(List<GeographyCode> codes) {
        if (codes == null) {
            return codes;
        }
        List<GeographyCode> newCodes = new ArrayList<>();
        for (GeographyCode code : codes) {
            if (!code.validCoordinate()) {
                continue;
            }
            GeographyCode newCode = toGCJ02(code);
            if (newCode != null) {
                newCodes.add(newCode);
            }
        }
        return newCodes;
    }

    public static List<GeographyCode> toGCJ02ByWebService(CoordinateSystem sourceCS, List<GeographyCode> codes) {
        try {
            if (codes == null || codes.isEmpty()) {
                return null;
            }
            int size = codes.size();
            int batch = size % 40 == 0 ? size / 40 : size / 40 + 1;
            List<GeographyCode> newCodes = new ArrayList<>();
            for (int k = 0; k < batch; k++) {
                String locationsString = null;
                for (int i = k * 40; i < Math.min(size, k * 40 + 40); i++) {
                    GeographyCode code = codes.get(i);
                    if (locationsString == null) {
                        locationsString = "";
                    } else {
                        locationsString += "|";
                    }
                    locationsString += DoubleTools.scale(code.getLongitude(), 6) + "," + DoubleTools.scale(code.getLatitude(), 6);
                }
                String results = toGCJ02ByWebService(sourceCS, locationsString);
                String[] locationsValues = results.split(";");
                CoordinateSystem GCJ02 = CoordinateSystem.GCJ02();
                for (int i = 0; i < locationsValues.length; i++) {
                    String locationValue = locationsValues[i];
                    String[] values = locationValue.split(",");
                    double longitudeC = Double.parseDouble(values[0]);
                    double latitudeC = Double.parseDouble(values[1]);
                    GeographyCode newCode = (GeographyCode) codes.get(i).clone();
                    newCode.setLongitude(longitudeC);
                    newCode.setLatitude(latitudeC);
                    newCode.setCoordinateSystem(GCJ02);
                    newCodes.add(newCode);
                }
            }
            return newCodes;
        } catch (Exception e) {
            return null;
        }
    }

    public static double[] toGCJ02ByWebService(CoordinateSystem sourceCS, double longitude, double latitude) {
        try {

            if (sourceCS == null || sourceCS.getValue() == GCJ_02) {
                double[] coordinate = {DoubleTools.scale(longitude, 6), DoubleTools.scale(latitude, 6)};
                return coordinate;
            }
            String results = toGCJ02ByWebService(sourceCS, DoubleTools.scale(longitude, 6) + "," + DoubleTools.scale(latitude, 6));
            String[] values = results.split(",");
            double longitudeC = Double.parseDouble(values[0]);
            double latitudeC = Double.parseDouble(values[1]);
            double[] coordinate = {DoubleTools.scale(longitudeC, 6), DoubleTools.scale(latitudeC, 6)};
            return coordinate;
        } catch (Exception e) {
            return null;
        }
    }

    public static String toGCJ02ByWebService(CoordinateSystem sourceCS, String locationsString) {
        try {
            if (sourceCS == null || sourceCS.getValue() == GCJ_02) {
                return locationsString;
            }
            String urlString = "https://restapi.amap.com/v3/assistant/coordinate/convert?locations="
                    + locationsString
                    + "&coordsys=" + sourceCS.gaodeConvertService()
                    + "&output=xml&key=" + AppVariables.getUserConfigValue("GaoDeMapServiceKey", CommonValues.GaoDeMapServiceKey);
            URL url = new URL(urlString);
            File xmlFile = FileTools.getTempFile(".xml");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            SSLContext sc = SSLContext.getInstance(CommonValues.HttpsProtocal);
            sc.init(null, trustAllManager(), new SecureRandom());
            connection.setSSLSocketFactory(sc.getSocketFactory());
            connection.setHostnameVerifier(trustAllVerifier());
            connection.connect();
            try ( BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(xmlFile))) {
                byte[] buf = new byte[CommonValues.IOBufferLength];
                int len;
                while ((len = inStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }
            }
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
            NodeList nodes = doc.getElementsByTagName("info");
            if (nodes == null || nodes.getLength() == 0) {
                return null;
            }
            String info = nodes.item(0).getTextContent();
            if (!"ok".equals(info.toLowerCase())) {
                return null;
            }

            nodes = doc.getElementsByTagName("locations");
            if (nodes == null || nodes.getLength() == 0) {
                return null;
            }
            return nodes.item(0).getTextContent();
        } catch (Exception e) {
            return null;
        }
    }

    /*
        Others
     */
    public static boolean validCoordinate(GeographyCode code) {
        if (code == null) {
            return false;
        }
        return (code.getLongitude() >= -180) && (code.getLongitude() <= 180)
                && (code.getLatitude() >= -90) && (code.getLatitude() <= 90);
    }

}
