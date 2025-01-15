package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.GeographyCode.AddressLevel;
import mara.mybox.data.GeographyCode.CoordinateSystem;
import mara.mybox.db.data.DataNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
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
        Coordinate System
     */
    public static String coordinateSystemMessageNames() {
        String s = "";
        for (CoordinateSystem v : CoordinateSystem.values()) {
            if (!s.isBlank()) {
                s += "\n";
            }
            s += message(v.name());
        }
        return s;
    }

    public static CoordinateSystem coordinateSystemByName(String name) {
        try {
            return CoordinateSystem.valueOf(name);
        } catch (Exception e) {
            try {
                return CoordinateSystem.valueOf(name);
            } catch (Exception ex) {
                return GeographyCode.defaultCoordinateSystem;
            }
        }
    }

    public static CoordinateSystem coordinateSystemByValue(short value) {
        try {
            return CoordinateSystem.values()[value];
        } catch (Exception e) {
            return GeographyCode.defaultCoordinateSystem;
        }
    }

    public static short coordinateSystemValue(CoordinateSystem cs) {
        try {
            return (short) cs.ordinal();
        } catch (Exception e) {
            return 0;
        }
    }

    public static String coordinateSystemName(short value) {
        try {
            return coordinateSystemByValue(value).name();
        } catch (Exception e) {
            return null;
        }
    }

    public static String coordinateSystemMessageName(short value) {
        try {
            return message(coordinateSystemName(value));
        } catch (Exception e) {
            return null;
        }
    }

    /*
        Address Level
     */
    public static String addressLevelMessageNames() {
        String s = "";
        for (AddressLevel v : AddressLevel.values()) {
            if (!s.isBlank()) {
                s += "\n";
            }
            s += message(v.name());
        }
        return s;
    }

    public static AddressLevel addressLevelByName(String name) {
        try {
            return AddressLevel.valueOf(name);
        } catch (Exception e) {
            return GeographyCode.defaultAddressLevel;
        }
    }

    public static AddressLevel addressLevelByValue(short value) {
        try {
            return AddressLevel.values()[value];
        } catch (Exception e) {
            return GeographyCode.defaultAddressLevel;
        }
    }

    public static short addressLevelValue(AddressLevel level) {
        try {
            return (short) level.ordinal();
        } catch (Exception e) {
            return 0;
        }
    }

    public static String addressLevelName(short value) {
        try {
            return addressLevelByValue(value).name();
        } catch (Exception e) {
            return null;
        }
    }

    public static String addressLevelMessageName(short value) {
        try {
            return message(addressLevelName(value));
        } catch (Exception e) {
            return null;
        }
    }

    /*
        map
     */
    public static String gaodeMap(int zoom) {
        try {
            File map = FxFileTools.getInternalFile("/js/GaoDeMap.html", "js", "GaoDeMap.html", true);
            String html = TextFileTools.readTexts(null, map);
            html = html.replace(AppValues.GaoDeMapJavascriptKey,
                    UserConfig.getString("GaoDeMapWebKey", AppValues.GaoDeMapJavascriptKey))
                    .replace("MyBoxMapZoom", zoom + "");
            return html;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return "";
        }
    }

    public static File tiandituFile(boolean geodetic, int zoom) {
        try {
            File map = FxFileTools.getInternalFile("/js/tianditu.html", "js", "tianditu.html", true);
            String html = TextFileTools.readTexts(null, map);
            html = html.replace(AppValues.TianDiTuWebKey,
                    UserConfig.getString("TianDiTuWebKey", AppValues.TianDiTuWebKey))
                    .replace("MyBoxMapZoom", zoom + "");
            if (geodetic) {
                html = html.replace("'EPSG:900913", "EPSG:4326");
            }
            TextFileTools.writeFile(map, html);
            return map;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    /*
        Query codes by web service
     */
    public static GeographyCode geoCode(CoordinateSystem coordinateSystem, String address) {
        try {
            if (coordinateSystem == null) {
                return null;
            }
            if (coordinateSystem == CoordinateSystem.GCJ_02) {
                // GaoDe Map only supports info codes of China
                String urlString = "https://restapi.amap.com/v3/geocode/geo?address="
                        + URLEncoder.encode(address, "UTF-8") + "&output=xml&key="
                        + UserConfig.getString("GaoDeMapServiceKey", AppValues.GaoDeMapWebServiceKey);
                GeographyCode geographyCode = new GeographyCode();
                geographyCode.setChineseName(address);
                geographyCode.setCoordinateSystem(coordinateSystem);
                return gaodeCode(urlString, geographyCode);
            } else if (coordinateSystem == CoordinateSystem.CGCS2000) {
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

                if (validCoordinate(longitude, latitude)) {
                    return GeographyCodeTools.geoCode(coordinateSystem, longitude, latitude);
                }

            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static GeographyCode geoCode(CoordinateSystem coordinateSystem, double longitude, double latitude) {
        try {
            if (coordinateSystem == null) {
                return null;
            }
            if (coordinateSystem == CoordinateSystem.GCJ_02) {
                String urlString = "https://restapi.amap.com/v3/geocode/regeo?location="
                        + longitude + "," + latitude + "&output=xml&key="
                        + UserConfig.getString("GaoDeMapServiceKey", AppValues.GaoDeMapWebServiceKey);
                GeographyCode geographyCode = new GeographyCode();
                geographyCode.setLongitude(longitude);
                geographyCode.setLatitude(latitude);
                geographyCode.setCoordinateSystem(coordinateSystem);
                return gaodeCode(urlString, geographyCode);
            } else if (coordinateSystem == CoordinateSystem.CGCS2000) {
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
                    String name = subData.substring(0, pos);
                    setName(geographyCode, name);
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
                    setName(geographyCode, name);
                }
            }
            flag = "\"city\":\"";
            pos = data.indexOf(flag);
            if (pos >= 0) {
                String subData = data.substring(pos + flag.length());
                flag = "\"";
                pos = subData.indexOf(flag);
                if (pos >= 0) {
                    geographyCode.setCity(subData.substring(0, pos));
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
                    geographyCode.setCountry(subData.substring(0, pos));
                }
            }
            flag = "\"county\":\"";
            pos = data.indexOf(flag);
            if (pos >= 0) {
                String subData = data.substring(pos + flag.length());
                flag = "\"";
                pos = subData.indexOf(flag);
                if (pos >= 0) {
                    geographyCode.setCounty(subData.substring(0, pos));
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
                    setName(geographyCode, name);
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
                    geographyCode.setProvince(subData.substring(0, pos));
                }
            }
            flag = "\"road\":\"";
            pos = data.indexOf(flag);
            if (pos >= 0) {
                String subData = data.substring(pos + flag.length());
                flag = "\"";
                pos = subData.indexOf(flag);
                if (pos >= 0) {
                    geographyCode.setVillage(subData.substring(0, pos));
                }
            }
//            if (!validCoordinate(geographyCode)) {
//                return null;
//            }
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
                String name = nodes.item(0).getTextContent();
                setName(geographyCode, name);
            }
            nodes = doc.getElementsByTagName("country");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setCountry(nodes.item(0).getTextContent());
            }
            nodes = doc.getElementsByTagName("province");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setProvince(nodes.item(0).getTextContent());
            }
            nodes = doc.getElementsByTagName("citycode");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setCode2(nodes.item(0).getTextContent());
            }
            nodes = doc.getElementsByTagName("city");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setCity(nodes.item(0).getTextContent());
            }
            nodes = doc.getElementsByTagName("district");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setCounty(nodes.item(0).getTextContent());
            }
            nodes = doc.getElementsByTagName("township");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setTown(nodes.item(0).getTextContent());
            }
            nodes = doc.getElementsByTagName("neighborhood");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setVillage(nodes.item(0).getFirstChild().getTextContent());
            }
            nodes = doc.getElementsByTagName("building");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setBuilding(nodes.item(0).getTextContent());
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
                    if (geographyCode.getBuilding() == null) {
                        geographyCode.setBuilding(s);
                    } else {
                        setName(geographyCode, s);
                    }
                }
            } else {
                nodes = doc.getElementsByTagName("street");
                if (nodes != null && nodes.getLength() > 0) {
                    String s = nodes.item(0).getTextContent();
                    if (geographyCode.getBuilding() == null) {
                        geographyCode.setBuilding(s);
                    } else {
                        setName(geographyCode, s);
                    }
                }
                nodes = doc.getElementsByTagName("number");
                if (nodes != null && nodes.getLength() > 0) {
                    geographyCode.setCode5(nodes.item(0).getTextContent());
                }
                if (!validCoordinate(geographyCode)) {
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
                    if (message("zh", "Country").equals(v)) {
                        geographyCode.setLevel(AddressLevel.Country);
                    } else if (message("zh", "Province").equals(v)) {
                        geographyCode.setLevel(AddressLevel.Province);
                    } else if (message("zh", "City").equals(v)) {
                        geographyCode.setLevel(AddressLevel.City);
                    } else if (message("zh", "County").equals(v)) {
                        geographyCode.setLevel(AddressLevel.County);
                    } else if (message("zh", "Town").equals(v)) {
                        geographyCode.setLevel(AddressLevel.Town);
                    } else if (message("zh", "Neighborhood").equals(v)) {
                        geographyCode.setLevel(AddressLevel.Village);
                    } else if (message("zh", "PointOfInterest").equals(v)) {
                        geographyCode.setLevel(AddressLevel.PointOfInterest);
                    } else if (message("zh", "Street").equals(v)) {
                        geographyCode.setLevel(AddressLevel.Village);
                    } else if (message("zh", "Building").equals(v)) {
                        geographyCode.setLevel(AddressLevel.Building);
                    } else {
                        geographyCode.setLevel(AddressLevel.PointOfInterest);
                    }
                }
            }
//            if (!validCoordinate(geographyCode)) {
//                return null;
//            }
            return geographyCode;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static boolean setName(GeographyCode geographyCode, String name) {
        if (geographyCode.getChineseName() == null) {
            geographyCode.setChineseName(name);
            return true;
        } else if (!geographyCode.getChineseName().equals(name)) {
            if (geographyCode.getAlias1() == null) {
                geographyCode.setAlias1(geographyCode.getChineseName());
                geographyCode.setChineseName(name);
                return true;
            } else if (!geographyCode.getAlias1().equals(name)) {
                if (geographyCode.getAlias2() == null) {
                    geographyCode.setAlias2(geographyCode.getChineseName());
                    geographyCode.setChineseName(name);
                    return true;
                } else if (!geographyCode.getAlias2().equals(name)) {
                    if (geographyCode.getAlias3() == null) {
                        geographyCode.setAlias3(geographyCode.getChineseName());
                        geographyCode.setChineseName(name);
                        return true;
                    } else if (!geographyCode.getAlias3().equals(name)) {
                        if (geographyCode.getAlias4() == null) {
                            geographyCode.setAlias4(geographyCode.getChineseName());
                            geographyCode.setChineseName(name);
                            return true;
                        } else if (!geographyCode.getAlias4().equals(name)) {
                            geographyCode.setAlias5(geographyCode.getChineseName());
                            geographyCode.setChineseName(name);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static String gaodeConvertService(CoordinateSystem cs) {
        if (cs == null) {
            cs = GeographyCode.defaultCoordinateSystem;
        }
        switch (cs) {
            case CGCS2000:
                return "gps";
            case GCJ_02:
                return "autonavi";
            case WGS_84:
                return "gps";
            case BD_09:
                return "baidu";
            case Mapbar:
                return "mapbar";
            default:
                return "autonavi";
        }
    }

    /*
        Convert
     */
    public static GeographyCode fromNode(DataNode node) {
        if (node == null) {
            return null;
        }
        GeographyCode code = new GeographyCode();
        code.setTitle(node.getTitle());
        code.setLevel(addressLevelByValue(node.getShortValue("level")));
        code.setCoordinateSystem(coordinateSystemByValue(node.getShortValue("coordinate_system")));
        double d = node.getDoubleValue("longitude");
        code.setLongitude((DoubleTools.invalidDouble(d) || d > 180 || d < -180) ? -200 : d);
        d = node.getDoubleValue("latitude");
        code.setLatitude((DoubleTools.invalidDouble(d) || d > 90 || d < -90) ? -200 : d);
        code.setAltitude(node.getDoubleValue("altitude"));
        code.setPrecision(node.getDoubleValue("precision"));
        code.setChineseName(node.getStringValue("chinese_name"));
        code.setEnglishName(node.getStringValue("english_name"));
        code.setContinent(node.getStringValue("continent"));
        code.setCountry(node.getStringValue("country"));
        code.setProvince(node.getStringValue("province"));
        code.setCity(node.getStringValue("city"));
        code.setCounty(node.getStringValue("county"));
        code.setTown(node.getStringValue("town"));
        code.setVillage(node.getStringValue("village"));
        code.setBuilding(node.getStringValue("building"));
        code.setPoi(node.getStringValue("poi"));
        code.setAlias1(node.getStringValue("alias1"));
        code.setAlias2(node.getStringValue("alias2"));
        code.setAlias3(node.getStringValue("alias3"));
        code.setAlias4(node.getStringValue("alias4"));
        code.setAlias5(node.getStringValue("alias5"));
        code.setCode1(node.getStringValue("code1"));
        code.setCode2(node.getStringValue("code2"));
        code.setCode3(node.getStringValue("code3"));
        code.setCode4(node.getStringValue("code4"));
        code.setCode5(node.getStringValue("code5"));
        code.setDescription(node.getStringValue("description"));
        d = node.getDoubleValue("area");
        code.setArea(DoubleTools.invalidDouble(d) || d <= 0 ? -1 : d);
        long p = node.getLongValue("population");
        code.setPopulation(LongTools.invalidLong(p) || p <= 0 ? -1 : p);
        return code;
    }

    public static DataNode toNode(GeographyCode code) {
        if (code == null) {
            return null;
        }
        DataNode node = new DataNode();
        node.setTitle(code.getTitle());
        node.setValue("level", addressLevelValue(code.getLevel()));
        node.setValue("coordinate_system", coordinateSystemValue(code.getCoordinateSystem()));
        node.setValue("longitude", code.getLongitude());
        node.setValue("latitude", code.getLatitude());
        node.setValue("precision", code.getPrecision());
        node.setValue("chinese_name", code.getChineseName());
        node.setValue("english_name", code.getEnglishName());
        node.setValue("continent", code.getContinent());
        node.setValue("country", code.getCountry());
        node.setValue("province", code.getProvince());
        node.setValue("city", code.getCity());
        node.setValue("county", code.getCounty());
        node.setValue("town", code.getTown());
        node.setValue("village", code.getVillage());
        node.setValue("building", code.getBuilding());
        node.setValue("poi", code.getPoi());
        node.setValue("alias1", code.getAlias1());
        node.setValue("alias1", code.getAlias1());
        node.setValue("alias1", code.getAlias1());
        node.setValue("alias1", code.getAlias1());
        node.setValue("alias1", code.getAlias1());
        node.setValue("code1", code.getCode1());
        node.setValue("code1", code.getCode1());
        node.setValue("code1", code.getCode1());
        node.setValue("code1", code.getCode1());
        node.setValue("code1", code.getCode1());
        node.setValue("description", code.getDescription());
        node.setValue("area", code.getArea());
        node.setValue("population", code.getPopulation());
        return node;
    }

    public static GeographyCode toCGCS2000(GeographyCode code, boolean setCS) {
        GeographyCode converted = toWGS84(code);
        if (converted != null && setCS) {
            converted.setCoordinateSystem(CoordinateSystem.CGCS2000);
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
            CoordinateSystem cs = code.getCoordinateSystem();
            double[] coordinate;
            switch (cs) {
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
            newCode.setCoordinateSystem(CoordinateSystem.WGS_84);
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
            CoordinateSystem cs = code.getCoordinateSystem();
            double[] coordinate;
            switch (cs) {
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
            newCode.setCoordinateSystem(CoordinateSystem.GCJ_02);
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

    public static List<GeographyCode> toGCJ02ByWebService(CoordinateSystem sourceCS,
            List<GeographyCode> codes) {
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
                for (int i = 0; i < locationsValues.length; i++) {
                    String locationValue = locationsValues[i];
                    String[] values = locationValue.split(",");
                    double longitudeC = Double.parseDouble(values[0]);
                    double latitudeC = Double.parseDouble(values[1]);
                    GeographyCode newCode = (GeographyCode) codes.get(i).clone();
                    newCode.setLongitude(longitudeC);
                    newCode.setLatitude(latitudeC);
                    newCode.setCoordinateSystem(CoordinateSystem.GCJ_02);
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

            if (sourceCS == null || sourceCS == CoordinateSystem.GCJ_02) {
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

    public static String toGCJ02ByWebService(CoordinateSystem sourceCS, String locationsString) {
        try {
            if (sourceCS == null || sourceCS == CoordinateSystem.GCJ_02) {
                return locationsString;
            }
            String urlString = "https://restapi.amap.com/v3/assistant/coordinate/convert?locations="
                    + locationsString
                    + "&coordsys=" + gaodeConvertService(sourceCS)
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

    public static double DMS2Coordinate(double degrees, double minutes, double seconds) {
        return DMS2Coordinate(degrees >= 0, degrees, minutes, seconds);
    }

    public static double DMS2Coordinate(boolean negitive, double degrees, double minutes, double seconds) {
        double value = Math.abs(degrees) + minutes / 60.0 + seconds / 3600.0;
        return negitive ? -value : value;
    }

    public static double[] coordinate2DMS(double value) {
        double[] dms = new double[3];
        int i = (int) value;
        dms[0] = i;
        double d = (Math.abs(value) - Math.abs(i)) * 60.0;
        i = (int) d;
        dms[1] = i;
        dms[2] = (d - i) * 60.0;
        return dms;
    }

    public static String dmsString(double degrees, double minutes, double seconds) {
        return (int) degrees + "\u00b0" + (int) minutes + "'" + DoubleTools.scale(seconds, 4) + "\"";
    }

    public static String latitudeToDmsString(double value) {
        double[] dms = coordinate2DMS(value);
        int degrees = (int) dms[0];
        String s = Math.abs(degrees) + "\u00b0" + (int) dms[1] + "'" + DoubleTools.scale(dms[2], 4) + "\"";
        return s + (degrees >= 0 ? "N" : "S");
    }

    public static String coordinateToDmsString(double value) {
        double[] dms = coordinate2DMS(value);
        return dmsString(dms[0], dms[1], dms[2]);
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

    public static double[] parseDMS(String value) {
        double[] dms = {-200.0, 0.0, 0.0, 0.0};
        if (value == null || value.trim().isBlank()) {
            return dms;
        }
        try {
            String s = value.trim().toLowerCase();
            boolean negative = false;
            boolean latitude = false;
            if (s.startsWith("s")) {
                negative = true;
                latitude = true;
                s = s.substring(1);
            } else if (s.startsWith("\u5357\u7eac")) {
                negative = true;
                latitude = true;
                s = s.substring(2);
            } else if (s.startsWith("\u5357")) {
                negative = true;
                latitude = true;
                s = s.substring(1);
            } else if (s.endsWith("s")) {
                negative = true;
                latitude = true;
                s = s.substring(0, s.length() - 1);
            } else if (s.startsWith("n")) {
                negative = false;
                latitude = true;
                s = s.substring(1);
            } else if (s.startsWith("\u5317\u7eac")) {
                negative = false;
                latitude = true;
                s = s.substring(2);
            } else if (s.startsWith("\u5317")) {
                negative = false;
                latitude = true;
                s = s.substring(1);
            } else if (s.endsWith("n")) {
                negative = false;
                latitude = true;
                s = s.substring(0, s.length() - 1);
            } else if (s.startsWith("w")) {
                negative = true;
                s = s.substring(1);
            } else if (s.startsWith("\u897f\u7ecf")) {
                negative = true;
                s = s.substring(2);
            } else if (s.startsWith("\u897f")) {
                negative = true;
                s = s.substring(1);
            } else if (s.endsWith("w")) {
                negative = true;
                s = s.substring(0, s.length() - 1);
            } else if (s.startsWith("e")) {
                negative = false;
                s = s.substring(1);
            } else if (s.startsWith("\u4e1c\u7ecf")) {
                negative = false;
                s = s.substring(2);
            } else if (s.startsWith("\u4e1c")) {
                negative = false;
                s = s.substring(1);
            } else if (s.endsWith("e")) {
                negative = false;
                s = s.substring(0, s.length() - 1);
            } else if (s.startsWith("\u7eac\u5ea6")) {
                latitude = true;
                s = s.substring(2);
            } else if (s.startsWith("latitude")) {
                latitude = true;
                s = s.substring("latitude".length());
            } else if (s.startsWith("\u7ecf\u5ea6")) {
                latitude = false;
                s = s.substring(2);
            } else if (s.startsWith("longitude")) {
                latitude = false;
                s = s.substring("longitude".length());
            }
            s = s.trim();
            if (s.startsWith("-")) {
                negative = true;
                s = s.substring(1);
            } else if (s.startsWith("+")) {
                negative = false;
                s = s.substring(1);
            }
            s = s.trim();
            int pos = s.indexOf("\u5ea6");
            if (pos < 0) {
                pos = s.indexOf("\u00b0");
            }
            if (pos >= 0) {
                try {
                    int v = Integer.parseInt(s.substring(0, pos).trim());
                    if (latitude) {
                        if (v >= -90 && v <= 90) {
                            dms[1] = v;
                        } else {
                            dms[0] = -200;
                            return dms;
                        }
                    } else {
                        if (v >= -180 && v <= 180) {
                            dms[1] = v;
                        } else {
                            dms[0] = -200;
                            return dms;
                        }
                    }
                    if (pos == s.length() - 1) {
                        dms[2] = 0;
                        dms[3] = 0;
                        dms[0] = DMS2Coordinate(negative, dms[1], dms[2], dms[3]);
                        return dms;
                    }
                    s = s.substring(pos + 1).trim();
                } catch (Exception e) {
                    dms[0] = -200;
                    return dms;
                }
            } else {
                dms[1] = 0;
            }
            pos = s.indexOf("\u5206");
            if (pos < 0) {
                pos = s.indexOf("'");
            }
            if (pos >= 0) {
                try {
                    int v = Integer.parseInt(s.substring(0, pos).trim());
                    if (v >= 0 && v < 60) {
                        dms[2] = v;
                    } else {
                        dms[0] = -200;
                        return dms;
                    }
                    if (pos == s.length() - 1) {
                        dms[3] = 0;
                        dms[0] = DMS2Coordinate(negative, dms[1], dms[2], dms[3]);
                        return dms;
                    }
                    s = s.substring(pos + 1).trim();
                } catch (Exception e) {
                    dms[0] = -200;
                    return dms;
                }
            } else {
                dms[2] = 0;
            }
            if (s.endsWith("\"") || s.endsWith("\u79d2")) {
                s = s.substring(0, s.length() - 1);
            }
            try {
                double v = Double.parseDouble(s.trim());
                if (v >= 0 && v < 60) {
                    dms[3] = v;
                    dms[0] = DMS2Coordinate(negative, dms[1], dms[2], dms[3]);
                    return dms;
                } else {
                    dms[0] = -200;
                    return dms;
                }
            } catch (Exception e) {
                dms[0] = -200;
                return dms;
            }
        } catch (Exception e) {
            dms[0] = -200;
            return dms;
        }
    }

    public static String longitudeToDmsString(double value) {
        double[] dms = coordinate2DMS(value);
        int degrees = (int) dms[0];
        String s = Math.abs(degrees) + "\u00b0" + (int) dms[1] + "'" + DoubleTools.scale(dms[2], 4) + "\"";
        return s + (degrees >= 0 ? "E" : "W");
    }

}
