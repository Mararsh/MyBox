package mara.mybox.db.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import mara.mybox.controller.LoadingController;
import mara.mybox.data.GeoCoordinateSystem;
import static mara.mybox.data.GeoCoordinateSystem.Value.GCJ_02;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.UrlTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
    public static GeographyCode geoCode(GeoCoordinateSystem coordinateSystem, String address) {
        try {
            if (coordinateSystem == null) {
                return null;
            }
            if (coordinateSystem.getValue() == GeoCoordinateSystem.Value.GCJ_02) {
                // GaoDe Map only supports info codes of China
                String urlString = "https://restapi.amap.com/v3/geocode/geo?address="
                        + URLEncoder.encode(address, "UTF-8") + "&output=xml&key="
                        + UserConfig.getString("GaoDeMapServiceKey", AppValues.GaoDeMapWebServiceKey);
                GeographyCode geographyCode = new GeographyCode();
                geographyCode.setChineseName(address);
                geographyCode.setCoordinateSystem(coordinateSystem);
                return gaodeCode(urlString, geographyCode);
            } else if (coordinateSystem.getValue() == GeoCoordinateSystem.Value.CGCS2000) {
                //  http://api.tianditu.gov.cn/geocoder?ds={"keyWord":"泰山"}&tk=0ddeb917def62b4691500526cc30a9b1
                String urlString = "http://api.tianditu.gov.cn/geocoder?ds="
                        + URLEncoder.encode("{\"keyWord\":\""
                                + address
                                + "\"}", "UTF-8") + "&tk="
                        + UserConfig.getString("TianDiTuWebKey", AppValues.TianDiTuWebKey);
                URL url = UrlTools.url(urlString);
                if (url == null) {
                    return null;
                }
                File jsonFile = FileTmpTools.getTempFile(".json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                connection.connect();
                try (final BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                        final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(jsonFile))) {
                    byte[] buf = new byte[AppValues.IOBufferLength];
                    int len;
                    while ((len = inStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                }
                String data = TextFileTools.readTexts(null, jsonFile);
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

    public static GeographyCode geoCode(GeoCoordinateSystem coordinateSystem, double longitude, double latitude, boolean decodeAncestors) {
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

    public static GeographyCode geoCode(GeoCoordinateSystem coordinateSystem, double longitude, double latitude) {
        try {
            if (coordinateSystem == null) {
                return null;
            }
            if (coordinateSystem.getValue() == GeoCoordinateSystem.Value.GCJ_02) {
                String urlString = "https://restapi.amap.com/v3/geocode/regeo?location="
                        + longitude + "," + latitude + "&output=xml&key="
                        + UserConfig.getString("GaoDeMapServiceKey", AppValues.GaoDeMapWebServiceKey);
                GeographyCode geographyCode = new GeographyCode();
                geographyCode.setLongitude(longitude);
                geographyCode.setLatitude(latitude);
                geographyCode.setCoordinateSystem(coordinateSystem);
                return gaodeCode(urlString, geographyCode);
            } else if (coordinateSystem.getValue() == GeoCoordinateSystem.Value.CGCS2000) {
                String urlString = "http://api.tianditu.gov.cn/geocoder?postStr={'lon':"
                        + longitude + ",'lat':" + latitude
                        + ",'ver':1}&type=geocode&tk="
                        + UserConfig.getString("TianDiTuWebKey", AppValues.TianDiTuWebKey);
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
            URL url = UrlTools.url(urlString);
            if (url == null) {
                return null;
            }
            File jsonFile = FileTmpTools.getTempFile(".json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            connection.connect();
            try (final BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                    final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(jsonFile))) {
                byte[] buf = new byte[AppValues.IOBufferLength];
                int len;
                while ((len = inStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
            }
            String data = TextFileTools.readTexts(null, jsonFile);
//            MyBoxLog.debug(data);
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
            MyBoxLog.debug(e);
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
            URL url = UrlTools.url(urlString);
            if (url == null) {
                return null;
            }
            File xmlFile = HtmlReadTools.download(null, url.toString());
//            MyBoxLog.debug(FileTools.readTexts(xmlFile));
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
                        geographyCode.setLongitude(Double.parseDouble(values[0].trim()));
                        geographyCode.setLatitude(Double.parseDouble(values[1].trim()));
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
                    if (Languages.message("zh", "Country").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("Country"));
                    } else if (Languages.message("zh", "Province").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("Province"));
                    } else if (Languages.message("zh", "City").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("City"));
                    } else if (Languages.message("zh", "County").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("County"));
                    } else if (Languages.message("zh", "Town").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("Town"));
                    } else if (Languages.message("zh", "Neighborhood").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("Village"));
                    } else if (Languages.message("zh", "PointOfInterest").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("Point Of Interest"));
                    } else if (Languages.message("zh", "Street").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("Village"));
                    } else if (Languages.message("zh", "Building").equals(v)) {
                        geographyCode.setLevelCode(new GeographyCodeLevel("Building"));
                    } else {
                        geographyCode.setLevelCode(new GeographyCodeLevel("Point Of Interest"));
                    }
                }
            }
            if (!validCoordinate(geographyCode)) {
                return null;
            }
            return geographyCode;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static GeographyCode encode(FxTask task, GeographyCode code) {
        if (code == null) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection();
                PreparedStatement geoInsert = conn.prepareStatement(TableGeographyCode.Insert)) {
            return encode(task, conn, geoInsert, code, true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return null;
    }

    public static GeographyCode encode(FxTask task, Connection conn, PreparedStatement geoInsert,
            GeographyCode code, boolean decodeAncestors) {
        if (code == null) {
            return null;
        }
        try {
            Map<String, Object> codeRet = encode(task, conn, geoInsert, code.getLevel(),
                    code.getLongitude(), code.getLatitude(),
                    code.getContinentName(), code.getCountryName(),
                    code.getProvinceName(), code.getCityName(), code.getCountyName(),
                    code.getTownName(), code.getVillageName(), code.getBuildingName(),
                    code.getName(), true, decodeAncestors);
            conn.commit();
//            if (codeRet.get("message") != null) {
//                MyBoxLog.error((String) codeRet.get("message"));
//            }
            if (codeRet.get("code") != null) {
                GeographyCode encoded = (GeographyCode) codeRet.get("code");
                return encoded;
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return null;
    }

    // !! Caller responses for committing the update
    public static Map<String, Object> encode(FxTask task, Connection conn, PreparedStatement geoInsert,
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
                    importPredefined(task, conn, null);
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
            MyBoxLog.debug(e);
            ret.put("message", e.toString());
            return ret;
        }
    }

    /*
        Import
     */
    public static void importPredefined(FxTask task) {
        importPredefined(task, null, null);
    }

    public static void importPredefined(FxTask task, Connection conn) {
        importPredefined(task, conn, null);
    }

    public static void importPredefined(FxTask task, Connection conn, LoadingController loading) {
        if (conn == null) {
            try (final Connection conn1 = DerbyBase.getConnection()) {
                importPredefined(task, conn1, loading);
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            return;
        }
        try {
            conn.setAutoCommit(false);
            File file = mara.mybox.fxml.FxFileTools.getInternalFile("/data/examples/Geography_Code_global_internal.csv", "data", "Geography_Code_global_internal.csv");
            importInternalCSV(task, conn, loading, file, true);
            file = mara.mybox.fxml.FxFileTools.getInternalFile("/data/examples/Geography_Code_countries_internal.csv", "data", "Geography_Code_countries_internal.csv");
            importInternalCSV(task, conn, loading, file, true);
            if (task != null && !task.isWorking()) {
                return;
            }
            if (!Languages.isChinese()) {
                try {
                    String sql = "UPDATE Geography_Code SET comments=null WHERE level=3 AND predefined=1";
                    conn.prepareStatement(sql).executeUpdate();
                } catch (Exception e) {
                    MyBoxLog.debug(e);
                }
            }
            file = mara.mybox.fxml.FxFileTools.getInternalFile("/data/examples/Geography_Code_china_provinces_internal.csv", "data", "Geography_Code_china_provinces_internal.csv");
            importInternalCSV(task, conn, loading, file, true);
            if (task != null && !task.isWorking()) {
                return;
            }
            file = mara.mybox.fxml.FxFileTools.getInternalFile("/data/examples/Geography_Code_china_cities_internal.csv", "data", "Geography_Code_china_cities_internal.csv");
            importInternalCSV(task, conn, loading, file, true);
            if (task != null && !task.isWorking()) {
                return;
            }
            file = mara.mybox.fxml.FxFileTools.getInternalFile("/data/examples/Geography_Code_china_counties_internal.csv", "data", "Geography_Code_china_counties_internal.csv");
            importInternalCSV(task, conn, loading, file, true);
            if (task != null && !task.isWorking()) {
                return;
            }
            file = mara.mybox.fxml.FxFileTools.getInternalFile("/data/examples/Geography_Code_special.csv", "data", "Geography_Code_special.csv");
            importInternalCSV(task, conn, loading, file, true);
            conn.commit();
        } catch (Exception e) {
            MyBoxLog.debug(e);
            if (loading != null) {
                loading.setInfo(e.toString());
            }
        }
    }

    // gcid,levelid,longitude,latitude,chinese_name,english_name,code1,code2,code3,code4,code5,alias1,alias2,alias3,alias4,alias5,
    // area,population,continentid,countryid,provinceid,cityid,countyid,townid,villageid,buildingid,comments
    public static void importInternalCSV(FxTask task, LoadingController loading, File file, boolean predefined) {
        try (final Connection conn = DerbyBase.getConnection()) {
            importInternalCSV(task, conn, loading, file, predefined);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public static void importInternalCSV(FxTask task, Connection conn, LoadingController loading, File file, boolean predefined) {
        long importCount = 0;
        long insertCount = 0;
        long updateCount = 0;
        long failedCount = 0;
        File validFile = FileTools.removeBOM(task, file);
        try (final CSVParser parser = CSVParser.parse(validFile, StandardCharsets.UTF_8, CsvTools.csvFormat())) {
            conn.setAutoCommit(false);
            List<String> names = parser.getHeaderNames();
            if (loading != null) {
                loading.setInfo(Languages.message("Importing") + " " + file.getAbsolutePath());
            }
            try (final PreparedStatement gcidQeury = conn.prepareStatement(TableGeographyCode.GCidQeury);
                    final PreparedStatement insert = conn.prepareStatement(TableGeographyCode.Insert);
                    final PreparedStatement update = conn.prepareStatement(TableGeographyCode.Update)) {
                gcidQeury.setMaxRows(1);
                boolean exist;
                for (CSVRecord record : parser) {
                    if (task != null && !task.isWorking()) {
                        parser.close();
                        return;
                    }
                    GeographyCode code = GeographyCodeTools.readIntenalRecord(names, record);
                    if (predefined) {
                        code.setSource(GeographyCode.AddressSource.PredefinedData);
                    } else {
                        code.setSource(GeographyCode.AddressSource.ImportedData);
                    }
                    gcidQeury.setLong(1, code.getGcid());
                    try (final ResultSet results = gcidQeury.executeQuery()) {
                        exist = results.next();
                    }
                    if (exist) {
                        if (TableGeographyCode.update(conn, update, code)) {
                            updateCount++;
                            importCount++;
                            if (loading != null && (importCount % 20 == 0)) {
                                loading.setInfo(Languages.message("Update") + ": " + updateCount + " " + code.getLevelCode().getName()
                                        + " " + code.getName() + " " + code.getLongitude() + " " + code.getLatitude());
                            }
                        } else {
                            ++failedCount;
                            if (loading != null) {
                                loading.setInfo(Languages.message("Failed") + ": " + failedCount + " " + code.getLevelCode().getName()
                                        + " " + code.getName() + " " + code.getLongitude() + " " + code.getLatitude());
                            }
                        }
                    } else {
                        if (TableGeographyCode.insert(conn, insert, code)) {
                            insertCount++;
                            importCount++;
                            if (loading != null && (importCount % 20 == 0)) {
                                loading.setInfo(Languages.message("Insert") + ": " + insertCount + " " + code.getLevelCode().getName()
                                        + " " + code.getName() + " " + code.getLongitude() + " " + code.getLatitude());
                            }
                        } else {
                            ++failedCount;
                            if (loading != null) {
                                loading.setInfo(Languages.message("Failed") + ": " + failedCount + " " + code.getLevelCode().getName()
                                        + " " + code.getName() + " " + code.getLongitude() + " " + code.getLatitude());
                            }
                        }
                    }
                }
                conn.commit();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public static List<GeographyCode> readInternalCSV(FxTask task, File file) {
        List<GeographyCode> codes = new ArrayList();
        File validFile = FileTools.removeBOM(task, file);
        if (validFile == null || (task != null && !task.isWorking())) {
            return null;
        }
        try (final CSVParser parser = CSVParser.parse(validFile, StandardCharsets.UTF_8, CsvTools.csvFormat())) {
            List<String> names = parser.getHeaderNames();
            for (CSVRecord record : parser) {
                GeographyCode code = GeographyCodeTools.readIntenalRecord(names, record);
                if (code != null) {
                    codes.add(code);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return codes;
    }

    public static GeographyCode readIntenalRecord(List<String> names, CSVRecord record) {
        try {
            GeographyCode code = new GeographyCode();
            if (names.contains("gcid")) {
                code.setGcid(Long.parseLong(record.get("gcid")));
            } else {
                code.setGcid(Long.parseLong(record.get("dataid")));
            }
            code.setLevelCode(new GeographyCodeLevel(Short.parseShort(record.get("levelid"))));
            try {
                code.setLongitude(Double.parseDouble(record.get("longitude")));
                code.setLatitude(Double.parseDouble(record.get("latitude")));
            } catch (Exception e) {
            }
            if (names.contains("altitude")) {
                try {
                    code.setAltitude(Double.parseDouble(record.get("altitude")));
                } catch (Exception e) {
                }
            }
            if (names.contains("precision")) {
                try {
                    code.setPrecision(Double.parseDouble(record.get("precision")));
                } catch (Exception e) {
                }
            }
            if (names.contains("coordinate_system")) {
                code.setCoordinateSystem(new GeoCoordinateSystem(record.get("coordinate_system")));
            } else {
                code.setCoordinateSystem(GeoCoordinateSystem.defaultCode());
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
                code.setArea(Long.parseLong(record.get("area")));
            }
            if (names.contains("population") && record.get("population") != null) {
                code.setPopulation(Long.parseLong(record.get("population")));
            }
            try {
                if (record.get("continentid") != null) {
                    code.setContinent(Long.parseLong(record.get("continentid")));
                }
            } catch (Exception e) {
            }
            try {
                if (record.get("countryid") != null) {
                    code.setCountry(Long.parseLong(record.get("countryid")));
                }
            } catch (Exception e) {
            }
            try {
                if (record.get("provinceid") != null) {
                    code.setProvince(Long.parseLong(record.get("provinceid")));
                }
            } catch (Exception e) {
            }
            try {
                if (record.get("cityid") != null) {
                    code.setCity(Long.parseLong(record.get("cityid")));
                }
            } catch (Exception e) {
            }
            try {
                if (record.get("countyid") != null) {
                    code.setCounty(Long.parseLong(record.get("countyid")));
                }
            } catch (Exception e) {
            }
            try {
                if (record.get("townid") != null) {
                    code.setTown(Long.parseLong(record.get("townid")));
                }
            } catch (Exception e) {
            }
            try {
                if (record.get("villageid") != null) {
                    code.setVillage(Long.parseLong(record.get("villageid")));
                }
            } catch (Exception e) {
            }
            try {
                if (record.get("buildingid") != null) {
                    code.setBuilding(Long.parseLong(record.get("buildingid")));
                }
            } catch (Exception e) {
            }
            if (names.contains("comments")) {
                code.setComments(record.get("comments"));
            }
            return code;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static GeographyCode readExtenalRecord(Connection conn, List<String> names, CSVRecord record) {
        try {
            String lang = names.contains(Languages.message("zh", "Level")) ? "zh" : "en";
            GeographyCode code = new GeographyCode();
            code.setSource(GeographyCode.AddressSource.ImportedData);
            if (names.contains("level")) {
                code.setLevelCode(new GeographyCodeLevel(record.get("level")));
            } else if (names.contains(Languages.message(lang, "Level"))) {
                code.setLevelCode(new GeographyCodeLevel(record.get(Languages.message(lang, "Level"))));
            } else {
                return null;
            }
            if (names.contains("longitude")) {
                try {
                    code.setLongitude(Double.parseDouble(record.get("longitude")));
                } catch (Exception e) {
                }
            } else if (names.contains(Languages.message(lang, "Longitude"))) {
                try {
                    code.setLongitude(Double.parseDouble(record.get(Languages.message(lang, "Longitude"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("latitude")) {
                try {
                    code.setLatitude(Double.parseDouble(record.get("latitude")));
                } catch (Exception e) {
                }
            } else if (names.contains(Languages.message(lang, "Latitude"))) {
                try {
                    code.setLatitude(Double.parseDouble(record.get(Languages.message(lang, "Latitude"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("altitude")) {
                try {
                    code.setAltitude(Double.parseDouble(record.get("altitude")));
                } catch (Exception e) {
                }
            } else if (names.contains(Languages.message(lang, "Altitude"))) {
                try {
                    code.setAltitude(Double.parseDouble(record.get(Languages.message(lang, "Altitude"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("precision")) {
                try {
                    code.setPrecision(Double.parseDouble(record.get("precision")));
                } catch (Exception e) {
                }
            } else if (names.contains(Languages.message(lang, "Precision"))) {
                try {
                    code.setPrecision(Double.parseDouble(record.get(Languages.message(lang, "Precision"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("coordinate_system")) {
                code.setCoordinateSystem(new GeoCoordinateSystem(record.get("coordinate_system")));
            } else if (names.contains(Languages.message(lang, "CoordinateSystem"))) {
                code.setCoordinateSystem(new GeoCoordinateSystem(record.get(Languages.message(lang, "CoordinateSystem"))));
            } else {
                code.setCoordinateSystem(GeoCoordinateSystem.defaultCode());
            }
            if (names.contains("chinese_name")) {
                code.setChineseName(record.get("chinese_name"));
            } else if (names.contains(Languages.message(lang, "ChineseName"))) {
                code.setChineseName(record.get(Languages.message(lang, "ChineseName")));
            }
            if (names.contains("english_name")) {
                code.setEnglishName(record.get("english_name"));
            } else if (names.contains(Languages.message(lang, "EnglishName"))) {
                code.setEnglishName(record.get(Languages.message(lang, "EnglishName")));
            }
            if (!GeographyCode.valid(code)) {
                return null;
            }
            if (names.contains("code1")) {
                code.setCode1(record.get("code1"));
            } else if (names.contains(Languages.message(lang, "Code1"))) {
                code.setCode1(record.get(Languages.message(lang, "Code1")));
            }
            if (names.contains("code2")) {
                code.setCode2(record.get("code2"));
            } else if (names.contains(Languages.message(lang, "Code2"))) {
                code.setCode2(record.get(Languages.message(lang, "Code2")));
            }
            if (names.contains("code3")) {
                code.setCode3(record.get("code3"));
            } else if (names.contains(Languages.message(lang, "Code3"))) {
                code.setCode3(record.get(Languages.message(lang, "Code3")));
            }
            if (names.contains("code4")) {
                code.setCode4(record.get("code4"));
            } else if (names.contains(Languages.message(lang, "Code4"))) {
                code.setCode4(record.get(Languages.message(lang, "Code4")));
            }
            if (names.contains("code5")) {
                code.setCode5(record.get("code5"));
            } else if (names.contains(Languages.message(lang, "Code5"))) {
                code.setCode5(record.get(Languages.message(lang, "Code5")));
            }
            if (names.contains("alias1")) {
                code.setAlias1(record.get("alias1"));
            } else if (names.contains(Languages.message(lang, "Alias1"))) {
                code.setAlias1(record.get(Languages.message(lang, "Alias1")));
            }
            if (names.contains("alias2")) {
                code.setAlias2(record.get("alias2"));
            } else if (names.contains(Languages.message(lang, "Alias2"))) {
                code.setAlias2(record.get(Languages.message(lang, "Alias2")));
            }
            if (names.contains("alias3")) {
                code.setAlias3(record.get("alias3"));
            } else if (names.contains(Languages.message(lang, "Alias3"))) {
                code.setAlias3(record.get(Languages.message(lang, "Alias3")));
            }
            if (names.contains("alias4")) {
                code.setAlias4(record.get("alias4"));
            } else if (names.contains(Languages.message(lang, "Alias4"))) {
                code.setAlias4(record.get(Languages.message(lang, "Alias4")));
            }
            if (names.contains("alias5")) {
                code.setAlias5(record.get("alias5"));
            } else if (names.contains(Languages.message(lang, "Alias5"))) {
                code.setAlias5(record.get(Languages.message(lang, "Alias5")));
            }
            if (names.contains("area") && record.get("area") != null) {
                try {
                    code.setArea(1000000 * Long.parseLong(record.get("area")));
                } catch (Exception e) {
                }
            } else if (names.contains(Languages.message(lang, "SquareMeters")) && record.get(Languages.message(lang, "SquareMeters")) != null) {
                try {
                    code.setArea(Long.parseLong(record.get(Languages.message(lang, "SquareMeters"))));
                } catch (Exception e) {
                }
            } else if (names.contains(Languages.message(lang, "SquareKilometers")) && record.get(Languages.message(lang, "SquareKilometers")) != null) {
                try {
                    code.setArea(1000000 * Long.parseLong(record.get(Languages.message(lang, "SquareKilometers"))));
                } catch (Exception e) {
                }
            }
            if (names.contains("population") && record.get("population") != null) {
                try {
                    code.setPopulation(Long.parseLong(record.get("population")));
                } catch (Exception e) {
                }
            } else if (names.contains(Languages.message(lang, "Population")) && record.get(Languages.message(lang, "Population")) != null) {
                try {
                    code.setArea(Long.parseLong(record.get(Languages.message(lang, "Population"))));
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
            String continent = names.contains(Languages.message(lang, "Continent"))
                    ? record.get(Languages.message(lang, "Continent")) : (names.contains("continent") ? record.get("continent") : null);
            if (continent != null && !continent.isBlank()) {
                continentC = TableGeographyCode.readCode(conn, 2, continent, false);
            }
            if (continentC != null) {
                continentid = continentC.getGcid();
            } else {
                continentid = -1;
            }
            String country = names.contains(Languages.message(lang, "Country"))
                    ? record.get(Languages.message(lang, "Country")) : (names.contains("country") ? record.get("country") : null);
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
            String province = names.contains(Languages.message(lang, "Province"))
                    ? record.get(Languages.message(lang, "Province")) : (names.contains("province") ? record.get("province") : null);
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
            String city = names.contains(Languages.message(lang, "City"))
                    ? record.get(Languages.message(lang, "City")) : (names.contains("city") ? record.get("city") : null);
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
            String county = names.contains(Languages.message(lang, "County"))
                    ? record.get(Languages.message(lang, "County")) : (names.contains("county") ? record.get("county") : null);
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
            String town = names.contains(Languages.message(lang, "Town"))
                    ? record.get(Languages.message(lang, "Town")) : (names.contains("town") ? record.get("town") : null);
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
            String village = names.contains(Languages.message(lang, "Village"))
                    ? record.get(Languages.message(lang, "Village")) : (names.contains("village") ? record.get("village") : null);
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
            String building = names.contains(Languages.message(lang, "Building"))
                    ? record.get(Languages.message(lang, "Building")) : (names.contains("building") ? record.get("building") : null);
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
            MyBoxLog.debug(e);
            return null;
        }
    }

    /*
        Convert
     */
    public static GeographyCode toCGCS2000(GeographyCode code, boolean setCS) {
        GeographyCode converted = toWGS84(code);
        if (converted != null && setCS) {
            converted.setCoordinateSystem(GeoCoordinateSystem.CGCS2000());
        }
        return converted;
    }

    public static List<GeographyCode> toCGCS2000(List<GeographyCode> codes, boolean setCS) {
        if (codes == null) {
            return codes;
        }
        List<GeographyCode> newCodes = new ArrayList<>();
        for (GeographyCode code : codes) {
            if (!validCoordinate(code)) {
                continue;
            }
            GeographyCode newCode = toCGCS2000(code, setCS);
            if (newCode != null) {
                newCodes.add(newCode);
            }
        }
        return newCodes;
    }

    public static GeographyCode toWGS84(GeographyCode code) {
        try {
            if (code == null || !validCoordinate(code)
                    || code.getCoordinateSystem() == null) {
                return code;
            }
            GeoCoordinateSystem cs = code.getCoordinateSystem();
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
            newCode.setGcid(-1);
            newCode.setLongitude(coordinate[0]);
            newCode.setLatitude(coordinate[1]);
            newCode.setCoordinateSystem(GeoCoordinateSystem.WGS84());
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
            if (!validCoordinate(code)) {
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
            if (code == null || !validCoordinate(code)
                    || code.getCoordinateSystem() == null) {
                return code;
            }
            GeoCoordinateSystem cs = code.getCoordinateSystem();
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
            newCode.setGcid(-1);
            newCode.setLongitude(coordinate[0]);
            newCode.setLatitude(coordinate[1]);
            newCode.setCoordinateSystem(GeoCoordinateSystem.GCJ02());
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
            if (!validCoordinate(code)) {
                continue;
            }
            GeographyCode newCode = toGCJ02(code);
            if (newCode != null) {
                newCodes.add(newCode);
            }
        }
        return newCodes;
    }

    public static List<GeographyCode> toGCJ02ByWebService(GeoCoordinateSystem sourceCS, List<GeographyCode> codes) {
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
                GeoCoordinateSystem GCJ02 = GeoCoordinateSystem.GCJ02();
                for (int i = 0; i < locationsValues.length; i++) {
                    String locationValue = locationsValues[i];
                    String[] values = locationValue.split(",");
                    double longitudeC = Double.parseDouble(values[0]);
                    double latitudeC = Double.parseDouble(values[1]);
                    GeographyCode newCode = (GeographyCode) codes.get(i).clone();
                    newCode.setGcid(-1);
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

    public static double[] toGCJ02ByWebService(GeoCoordinateSystem sourceCS, double longitude, double latitude) {
        try {

            if (sourceCS == null || sourceCS.getValue() == GCJ_02) {
                double[] coordinate = {DoubleTools.scale(longitude, 6),
                    DoubleTools.scale(latitude, 6)};
                return coordinate;
            }
            String results = toGCJ02ByWebService(sourceCS, DoubleTools.scale(longitude, 6) + "," + DoubleTools.scale(latitude, 6));
            String[] values = results.split(",");
            double longitudeC = Double.parseDouble(values[0]);
            double latitudeC = Double.parseDouble(values[1]);
            double[] coordinate = {DoubleTools.scale(longitudeC, 6),
                DoubleTools.scale(latitudeC, 6)};
            return coordinate;
        } catch (Exception e) {
            return null;
        }
    }

    public static String toGCJ02ByWebService(GeoCoordinateSystem sourceCS, String locationsString) {
        try {
            if (sourceCS == null || sourceCS.getValue() == GCJ_02) {
                return locationsString;
            }
            String urlString = "https://restapi.amap.com/v3/assistant/coordinate/convert?locations="
                    + locationsString
                    + "&coordsys=" + sourceCS.gaodeConvertService()
                    + "&output=xml&key=" + UserConfig.getString("GaoDeMapServiceKey", AppValues.GaoDeMapWebServiceKey);
            URL url = UrlTools.url(urlString);
            if (url == null) {
                return null;
            }
            File xmlFile = HtmlReadTools.download(null, url.toString());
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
        return code != null && validCoordinate(code.getLongitude(), code.getLatitude());
    }

    public static boolean validCoordinate(double longitude, double latitude) {
        return (longitude >= -180) && (longitude <= 180)
                && (latitude >= -90) && (latitude <= 90);
    }

}
