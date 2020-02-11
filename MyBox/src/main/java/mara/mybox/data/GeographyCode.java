package mara.mybox.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.xml.parsers.DocumentBuilderFactory;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.FileTools;
import static mara.mybox.tools.NetworkTools.trustAllManager;
import static mara.mybox.tools.NetworkTools.trustAllVerifier;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class GeographyCode {

    private static List<String> ChineseProvinces;

    protected String address, fullAddress, country, province, citycode, city, district, township,
            neighborhood, building, AdministrativeCode, street, number, level;
    protected double longitude = -200, latitude = -200;

    public String geography(String lineBreak) {
        StringBuilder s = new StringBuilder();
        if (getAddress() != null && !getAddress().isBlank()) {
            s.append(message("Address")).append(message(": ")).append(getAddress()).append(lineBreak);
        }
        s.append(message("Longitude")).append(message(": ")).append(getLongitude()).append(lineBreak);
        s.append(message("Latitude")).append(message(": ")).append(getLatitude()).append(lineBreak);
        if (getFullAddress() != null && !getFullAddress().isBlank()) {
            s.append(message("FullAddress")).append(message(": ")).append(getFullAddress()).append(lineBreak);
        }
        if (getCountry() != null && !getCountry().isBlank()) {
            s.append(message("Country")).append(message(": ")).append(getCountry()).append(lineBreak);
        }
        if (getProvince() != null && !getProvince().isBlank()) {
            s.append(message("Province")).append(message(": ")).append(getProvince()).append(lineBreak);
        }
        if (getCitycode() != null && !getCitycode().isBlank()) {
            s.append(message("Citycode")).append(message(": ")).append(getCitycode()).append(lineBreak);
        }
        if (getCity() != null && !getCity().isBlank()) {
            s.append(message("City")).append(message(": ")).append(getCity()).append(lineBreak);
        }
        if (getDistrict() != null && !getDistrict().isBlank()) {
            s.append(message("District")).append(message(": ")).append(getDistrict()).append(lineBreak);
        }
        if (getTownship() != null && !getTownship().isBlank()) {
            s.append(message("Township")).append(message(": ")).append(getTownship()).append(lineBreak);
        }
        if (getNeighborhood() != null && !getNeighborhood().isBlank()) {
            s.append(message("Neighborhood")).append(message(": ")).append(getNeighborhood()).append(lineBreak);
        }
        if (getBuilding() != null && !getBuilding().isBlank()) {
            s.append(message("Building")).append(message(": ")).append(getBuilding()).append(lineBreak);
        }
        if (getAdministrativeCode() != null && !getAdministrativeCode().isBlank()) {
            s.append(message("AdministrativeCode")).append(message(": ")).append(getAdministrativeCode()).append(lineBreak);
        }
        if (getStreet() != null && !getStreet().isBlank()) {
            s.append(message("Street")).append(message(": ")).append(getStreet()).append(lineBreak);
        }
        if (getNumber() != null && !getNumber().isBlank()) {
            s.append(message("Number")).append(message(": ")).append(getNumber()).append(lineBreak);
        }
        if (getLevel() != null && !getLevel().isBlank()) {
            s.append(message("Level")).append(message(": ")).append(getLevel()).append(lineBreak);
        }
        return s.toString();
    }

    public static GeographyCode query(String address) {
        try {
            GeographyCode geographyCode = TableGeographyCode.read(message(address));
            if (geographyCode != null) {
                return geographyCode;
            }
            // GaoDe Map only supports geography codes of China
            String urlString = "https://restapi.amap.com/v3/geocode/geo?address="
                    + URLEncoder.encode(message("zh", address), "UTF-8")
                    + "&output=xml&key=" + CommonValues.GaoDeWebKey;
            geographyCode = new GeographyCode();
            geographyCode.setAddress(message(address));
            return read(urlString, geographyCode);
        } catch (Exception e) {
            return null;
        }
    }

    public static GeographyCode query(double longtitude, double latitude) {
        try {
            GeographyCode geographyCode = TableGeographyCode.read(longtitude, latitude);
            if (geographyCode != null) {
                return geographyCode;
            }
            String urlString = "https://restapi.amap.com/v3/geocode/regeo?location="
                    + longtitude + "," + latitude
                    + "&output=xml&key=" + CommonValues.GaoDeWebKey;
            geographyCode = new GeographyCode();
            geographyCode.setLongitude(longtitude);
            geographyCode.setLatitude(latitude);
            return read(urlString, geographyCode);
        } catch (Exception e) {
            return null;
        }
    }

    public static GeographyCode read(String urlString,
            GeographyCode geographyCode) {
        try {
            URL url = new URL(urlString);
            File xmlFile = FileTools.getTempFile(".xml");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            SSLContext sc = SSLContext.getInstance("SSL");
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
            Document doc = DocumentBuilderFactory.newInstance().
                    newDocumentBuilder().parse(xmlFile);

            NodeList nodes = doc.getElementsByTagName("formatted_address");
            if (nodes != null && nodes.getLength() > 0) {
                String fulladdress = nodes.item(0).getTextContent();
                geographyCode.setFullAddress(fulladdress);
                if (geographyCode.getAddress() == null) {
                    geographyCode.setAddress(fulladdress);
                }
            } else {
                if (geographyCode.getAddress() != null) {
                    geographyCode.setFullAddress(geographyCode.getAddress());
                }
            }

            geographyCode.setCountry(message("China"));
//            nodes = doc.getElementsByTagName("country");
//            if (nodes != null && nodes.getLength() > 0) {
//                geographyCode.setCountry(nodes.item(0).getTextContent());
//
//            } else {
//                geographyCode.setCountry("");
//            }
            nodes = doc.getElementsByTagName("province");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setProvince(nodes.item(0).getTextContent());

            } else {
                geographyCode.setProvince("");
            }

            nodes = doc.getElementsByTagName("citycode");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setCitycode(nodes.item(0).getTextContent());

            } else {
                geographyCode.setCitycode("");
            }
            nodes = doc.getElementsByTagName("city");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setCity(nodes.item(0).getTextContent());

            } else {
                geographyCode.setCity("");
            }
            nodes = doc.getElementsByTagName("district");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setDistrict(nodes.item(0).getTextContent());

            } else {
                geographyCode.setDistrict("");
            }
            nodes = doc.getElementsByTagName("township");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setTownship(nodes.item(0).getTextContent());

            } else {
                geographyCode.setTownship("");
            }
            nodes = doc.getElementsByTagName("neighborhood");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setNeighborhood(nodes.item(0).getFirstChild().getTextContent());

            } else {
                geographyCode.setNeighborhood("");
            }
            nodes = doc.getElementsByTagName("building");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setBuilding(nodes.item(0).getTextContent());

            } else {
                geographyCode.setBuilding("");
            }
            nodes = doc.getElementsByTagName("adcode");
            if (nodes != null && nodes.getLength() > 0) {
                geographyCode.setAdministrativeCode(nodes.item(0).getTextContent());

            } else {
                geographyCode.setAdministrativeCode("");
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
                    geographyCode.setStreet(s);
                } else {
                    geographyCode.setStreet("");
                }
                geographyCode.setNumber("");
                geographyCode.setLevel("");
            } else {
                nodes = doc.getElementsByTagName("street");
                if (nodes != null && nodes.getLength() > 0) {
                    geographyCode.setStreet(nodes.item(0).getTextContent());
                } else {
                    geographyCode.setStreet("");
                }

                nodes = doc.getElementsByTagName("number");
                if (nodes != null && nodes.getLength() > 0) {
                    geographyCode.setNumber(nodes.item(0).getTextContent());
                } else {
                    geographyCode.setNumber("");
                }
                if (geographyCode.getLongitude() < -180) {
                    nodes = doc.getElementsByTagName("location");
                    if (nodes != null && nodes.getLength() > 0) {
                        String[] values = nodes.item(0).getFirstChild().getTextContent().split(",");
                        geographyCode.setLongitude(Double.valueOf(values[0].trim()));
                        geographyCode.setLatitude(Double.valueOf(values[1].trim()));
                    } else {
                        geographyCode.setLongitude(-1);
                        geographyCode.setLatitude(-1);
                    }
                }
                if (geographyCode.getAddress() == null) {
                    geographyCode.setAddress(geographyCode.getLongitude() + "," + geographyCode.getLatitude());
                }
                nodes = doc.getElementsByTagName("level");
                if (nodes != null && nodes.getLength() > 0) {
                    String v = nodes.item(0).getTextContent();
                    if (message("zh", "Province").equals(v)) {
                        geographyCode.setLevel(message("Province"));
                    } else if (message("zh", "Country").equals(v)) {
                        geographyCode.setLevel(message("Country"));
                    } else if (message("zh", "City").equals(v)) {
                        geographyCode.setLevel(message("City"));
                    } else if (message("zh", "Township").equals(v)) {
                        geographyCode.setLevel(message("Township"));
                    } else if (message("zh", "Street").equals(v)) {
                        geographyCode.setLevel(message("Street"));
                    } else {
                        geographyCode.setLevel(v);
                    }
                } else {
                    geographyCode.setLevel("");
                }
            }
            TableGeographyCode.write(geographyCode);
        } catch (Exception e) {
//            logger.debug(e.toString());
        }
        return geographyCode;
    }

    public static List<String> ChineseProvinces() {
        if (ChineseProvinces != null) {
            return ChineseProvinces;
        }
        ChineseProvinces = new ArrayList();
        ChineseProvinces = new ArrayList();
        ChineseProvinces.add("ProvinceHubei");
        ChineseProvinces.add("ProvinceZhejiang");
        ChineseProvinces.add("ProvinceGuangdong");
        ChineseProvinces.add("ProvinceHenan");
        ChineseProvinces.add("ProvinceHunan");
        ChineseProvinces.add("ProvinceAnhui");
        ChineseProvinces.add("ProvinceJiangxi");
        ChineseProvinces.add("CityChongqing");
        ChineseProvinces.add("ProvinceJiangsu");
        ChineseProvinces.add("ProvinceSichuan");
        ChineseProvinces.add("ProvinceShandong");
        ChineseProvinces.add("CityBeijing");
        ChineseProvinces.add("CityShanghai");
        ChineseProvinces.add("ProvinceFujian");
        ChineseProvinces.add("ProvinceShanxi");
        ChineseProvinces.add("ProvinceGuangxi");
        ChineseProvinces.add("ProvinceHebei");
        ChineseProvinces.add("ProvinceYunnan");
        ChineseProvinces.add("ProvinceHeilongjiang");
        ChineseProvinces.add("ProvinceLiaoning");
        ChineseProvinces.add("ProvinceHainan");
        ChineseProvinces.add("ProvinceShanxi2");
        ChineseProvinces.add("CityTianjin");
        ChineseProvinces.add("ProvinceGansu");
        ChineseProvinces.add("ProvinceGuizhou");
        ChineseProvinces.add("ProvinceNingxia");
        ChineseProvinces.add("InnerMongolia");
        ChineseProvinces.add("ProvinceJiLin");
        ChineseProvinces.add("ProvinceXinjiang");
        ChineseProvinces.add("HongKong");
        ChineseProvinces.add("ProvinceQinghai");
        ChineseProvinces.add("Taiwan");
        ChineseProvinces.add("Macau");
        ChineseProvinces.add("ProvinceXizang");
        return ChineseProvinces;
    }

    public static void initChineseProvincesCodes() {
        try {
            for (String name : ChineseProvinces()) {
                query(name);
            }
        } catch (Exception e) {
//            logger.debug(e.toString());
        }
    }

    public static GeographyCode countryCode(String country,
            double longtitude, double latitude) {
        GeographyCode code = new GeographyCode();
        code.setAddress(message(country));
        code.setCountry(message(country));
        code.setLongitude(longtitude);
        code.setLatitude(latitude);
        code.setLevel(message("Country"));
        return code;
    }

    public static void initCountriesCodes() {
        List<GeographyCode> codes = new ArrayList();
        try {
            codes.add(countryCode("Angola", 17.87, -11.20));
            codes.add(countryCode("Afghanistan", 67.71, 33.94));
            codes.add(countryCode("Albania", 20.17, 41.15));
            codes.add(countryCode("Algeria", 1.66, 28.03));
            codes.add(countryCode("Andorra", 1.52, 42.51));
            codes.add(countryCode("Anguilla", -63.07, 18.22));
            codes.add(countryCode("Argentina", -63.62, -38.42));
            codes.add(countryCode("Armenia", 45.04, 40.07));
            codes.add(countryCode("Ascension", -90.94, 30.20));
            codes.add(countryCode("Australia", 133.78, -25.27));
            codes.add(countryCode("Austria", 14.55, 47.52));
            codes.add(countryCode("Azerbaijan", 47.58, 40.14));
            codes.add(countryCode("Bahamas", -77.40, 25.03));
            codes.add(countryCode("Bahrain", 50.56, 26.07));
            codes.add(countryCode("Bangladesh", 90.36, 23.68));
            codes.add(countryCode("Barbados", -59.54, 13.19));
            codes.add(countryCode("Belarus", 27.95, 53.71));
            codes.add(countryCode("Belgium", 4.47, 50.50));
            codes.add(countryCode("Belize", -88.50, 17.19));
            codes.add(countryCode("Benin", 2.32, 9.31));
            codes.add(countryCode("BermudaIs", -64.75, 32.31));
            codes.add(countryCode("Bolivia", -63.59, -16.29));
            codes.add(countryCode("Botswana", 24.68, -22.33));
            codes.add(countryCode("Brazil", -51.93, -14.24));
            codes.add(countryCode("Brunei", 114.73, 4.54));
            codes.add(countryCode("Bulgaria", 25.49, 42.73));
            codes.add(countryCode("Burkina-faso", -1.56, 12.24));
            codes.add(countryCode("Burma", 95.96, 21.92));
            codes.add(countryCode("Burundi", 29.92, -3.37));
            codes.add(countryCode("Cameroon", 12.35, 7.37));
            codes.add(countryCode("Canada", -106.35, 56.13));
            codes.add(countryCode("CaymanIs", -117.64, 33.64));
            codes.add(countryCode("CentralAfricanRepublic", 20.94, 6.61));
            codes.add(countryCode("Chad", 18.73, 15.45));
            codes.add(countryCode("Chile", -71.54, -35.68));
            codes.add(countryCode("China", 104.20, 35.86));
            codes.add(countryCode("Colombia", -74.30, 4.57));
            codes.add(countryCode("CookIs", -90.49, 47.61));
            codes.add(countryCode("CostaRica", -83.75, 9.75));
            codes.add(countryCode("Croatia", 15.2, 45.1));
            codes.add(countryCode("Cuba", -77.78, 21.52));
            codes.add(countryCode("Cyprus", 33.43, 35.13));
            codes.add(countryCode("CzechRepublic", 15.47, 49.82));
            codes.add(countryCode("Denmark", 9.50, 56.26));
            codes.add(countryCode("Djibouti", 42.59, 11.83));
            codes.add(countryCode("DominicaRep", -0.19, 51.52));
            codes.add(countryCode("Ecuador", -78.18, -1.83));
            codes.add(countryCode("Egypt", 30.80, 26.82));
            codes.add(countryCode("EISalvador", -88.90, 13.79));
            codes.add(countryCode("Estonia", 25.01, 58.60));
            codes.add(countryCode("Ethiopia", 40.49, 9.15));
            codes.add(countryCode("Fiji", 178.07, -17.71));
            codes.add(countryCode("Finland", 25.75, 61.92));
            codes.add(countryCode("France", 2.21, 46.23));
            codes.add(countryCode("FrenchGuiana", -53.13, 3.93));
            codes.add(countryCode("Gabon", 11.61, -0.80));
            codes.add(countryCode("Gambia", -15.31, 13.44));
            codes.add(countryCode("Georgia", -82.90, 32.17));
            codes.add(countryCode("Germany", 10.45, 51.17));
            codes.add(countryCode("Ghana", -1.02, 7.95));
            codes.add(countryCode("Gibraltar", -5.35, 36.14));
            codes.add(countryCode("Greece", 21.82, 39.07));
            codes.add(countryCode("Grenada", -61.68, 12.12));
            codes.add(countryCode("Guam", 144.79, 13.44));
            codes.add(countryCode("Guatemala", -90.23, 15.78));
            codes.add(countryCode("Guinea", -9.70, 9.95));
            codes.add(countryCode("Guyana", -58.93, 4.86));
            codes.add(countryCode("Haiti", -72.29, 18.97));
            codes.add(countryCode("Honduras", -86.24, 15.20));
            codes.add(countryCode("Hungary", 19.50, 47.16));
            codes.add(countryCode("Iceland", -19.02, 64.96));
            codes.add(countryCode("India", 78.96, 20.59));
            codes.add(countryCode("Indonesia", 113.92, -0.79));
            codes.add(countryCode("Iran", 53.69, 32.43));
            codes.add(countryCode("Iraq", 43.68, 33.22));
            codes.add(countryCode("Ireland", -8.24, 53.41));
            codes.add(countryCode("Israel", 34.85, 31.05));
            codes.add(countryCode("Italy", 12.57, 41.87));
            codes.add(countryCode("IvoryCoast", -5.55, 7.54));
            codes.add(countryCode("Japan", 138.25, 36.20));
            codes.add(countryCode("Jordan", 36.24, 30.59));
            codes.add(countryCode("Kampuchea", 105.46, 12.00));
            codes.add(countryCode("Kazakstan", 66.92, 48.02));
            codes.add(countryCode("Kenya", 37.91, -0.02));
            codes.add(countryCode("Korea", 127.98, 37.66));
            codes.add(countryCode("Kuwait", 47.48, 29.31));
            codes.add(countryCode("Kyrgyzstan", 74.77, 41.20));
            codes.add(countryCode("Laos", 102.50, 19.86));
            codes.add(countryCode("Latvia", 24.60, 56.88));
            codes.add(countryCode("Lebanon", 35.86, 33.85));
            codes.add(countryCode("Lesotho", 28.23, -29.61));
            codes.add(countryCode("Liberia", -9.43, 6.43));
            codes.add(countryCode("Libya", 17.23, 26.34));
            codes.add(countryCode("Liechtenstein", 9.56, 47.17));
            codes.add(countryCode("Lithuania", 23.88, 55.17));
            codes.add(countryCode("Luxembourg", 6.13, 49.82));
            codes.add(countryCode("Macao", 113.54, 22.20));
            codes.add(countryCode("Madagascar", 46.87, -18.77));
            codes.add(countryCode("Malawi", 34.30, -13.25));
            codes.add(countryCode("Malaysia", 101.98, 4.21));
            codes.add(countryCode("Maldives", 73.54, 1.98));
            codes.add(countryCode("Mali", -4.00, 17.57));
            codes.add(countryCode("Malta", 14.38, 35.94));
            codes.add(countryCode("MarianaIs", -43.41, -20.37));
            codes.add(countryCode("Martinique", -61.02, 14.64));
            codes.add(countryCode("Mauritius", 57.55, -20.35));
            codes.add(countryCode("Mexico", -102.55, 23.63));
            codes.add(countryCode("Monaco", 7.42, 43.74));
            codes.add(countryCode("Mongolia", 103.85, 46.86));
            codes.add(countryCode("MontserratIs", -62.19, 16.74));
            codes.add(countryCode("Morocco", -7.09, 31.79));
            codes.add(countryCode("Mozambique", 35.53, -18.67));
            codes.add(countryCode("Namibia", 18.49, -22.96));
            codes.add(countryCode("Nauru", 166.93, -0.52));
            codes.add(countryCode("Nepal", 84.12, 28.39));
            codes.add(countryCode("NetheriAndsAntilles", -68.26, 12.20));
            codes.add(countryCode("Netherlands", 5.29, 52.13));
            codes.add(countryCode("NewZealand", 174.89, -40.90));
            codes.add(countryCode("Nicaragua", -85.21, 12.87));
            codes.add(countryCode("Niger", 8.08, 17.61));
            codes.add(countryCode("Nigeria", 8.68, 9.08));
            codes.add(countryCode("NorthKorea", 127.51, 40.34));
            codes.add(countryCode("Oman", 55.98, 21.47));
            codes.add(countryCode("Pakistan", 69.35, 30.38));
            codes.add(countryCode("Panama", -80.78, 8.54));
            codes.add(countryCode("PapuaNewCuinea", 143.96, -6.31));
            codes.add(countryCode("Paraguay", -58.44, -23.44));
            codes.add(countryCode("Peru", -75.02, -9.19));
            codes.add(countryCode("Philippines", 121.77, 12.88));
            codes.add(countryCode("Poland", 19.15, 51.92));
            codes.add(countryCode("FrenchPolynesia", -149.41, -17.68));
            codes.add(countryCode("Portugal", -8.22, 39.40));
            codes.add(countryCode("PuertoRico", -66.59, 18.22));
            codes.add(countryCode("Qatar", 51.18, 25.35));
            codes.add(countryCode("Reunion", 55.54, -21.12));
            codes.add(countryCode("Romania", 24.97, 45.94));
            codes.add(countryCode("Russia", 105.32, 61.52));
            codes.add(countryCode("SaintLueia", -60.98, 13.91));
            codes.add(countryCode("SaintVincent", 7.64, 45.75));
            codes.add(countryCode("SamoaEastern", -121.83, 37.35));
            codes.add(countryCode("SamoaWestern", -124.15, 40.80));
            codes.add(countryCode("SanMarino", 12.46, 43.94));
            codes.add(countryCode("SaoTomeAndPrincipe", 6.61, 0.19));
            codes.add(countryCode("SaudiArabia", 45.08, 23.89));
            codes.add(countryCode("Senegal", -14.45, 14.50));
            codes.add(countryCode("Seychelles", 55.49, -4.68));
            codes.add(countryCode("SierraLeone", -11.78, 8.46));
            codes.add(countryCode("Singapore", 103.82, 1.35));
            codes.add(countryCode("Slovakia", 19.70, 48.67));
            codes.add(countryCode("Slovenia", 15.00, 46.15));
            codes.add(countryCode("SolomonIs", -97.37, 38.92));
            codes.add(countryCode("Somali", 46.20, 5.15));
            codes.add(countryCode("SouthAfrica", 22.94, -30.56));
            codes.add(countryCode("Spain", -3.75, 40.46));
            codes.add(countryCode("SriLanka", 80.77, 7.87));
            codes.add(countryCode("St.Lucia", -60.98, 13.91));
            codes.add(countryCode("St.Vincent", -91.06, 29.99));
            codes.add(countryCode("Sudan", 30.22, 12.86));
            codes.add(countryCode("Suriname", -56.03, 3.92));
            codes.add(countryCode("Swaziland", 31.47, -26.52));
            codes.add(countryCode("Sweden", 18.64, 60.13));
            codes.add(countryCode("Switzerland", 8.23, 46.82));
            codes.add(countryCode("Syria", 39.00, 34.80));
            codes.add(countryCode("Tajikstan", 71.28, 38.86));
            codes.add(countryCode("Thailand", 100.99, 15.87));
            codes.add(countryCode("Togo", 0.82, 8.62));
            codes.add(countryCode("Tonga", -175.20, -21.18));
            codes.add(countryCode("TrinidadAndTobago", -61.22, 10.69));
            codes.add(countryCode("Tunisia", 9.54, 33.89));
            codes.add(countryCode("Turkey", 35.24, 38.96));
            codes.add(countryCode("Turkmenistan", 59.56, 38.97));
            codes.add(countryCode("Uganda", 32.29, 1.37));
            codes.add(countryCode("Ukraine", 31.17, 48.38));
            codes.add(countryCode("UnitedArabEmirates", 53.85, 23.42));
            codes.add(countryCode("UnitedKingdom", -3.44, 55.38));
            codes.add(countryCode("UnitedStates", -95.71, 37.09));
            codes.add(countryCode("Uruguay", -55.77, -32.52));
            codes.add(countryCode("Uzbekistan", 64.59, 41.38));
            codes.add(countryCode("Venezuela", -66.59, 6.42));
            codes.add(countryCode("Vietnam", 108.28, 14.06));
            codes.add(countryCode("Yemen", 48.52, 15.55));
            codes.add(countryCode("Yugoslavia", 121.02, 14.48));
            codes.add(countryCode("Zimbabwe", 29.15, -19.02));
            codes.add(countryCode("Zaire", 21.76, -4.04));
            codes.add(countryCode("Zambia", 27.85, -13.13));

            TableGeographyCode.write(codes);
        } catch (Exception e) {
//            logger.debug(e.toString());        }
        }

    }

    public static void importCodes() {
        File file;
        if ("zh".equals(AppVariables.getLanguage())) {
            file = FxmlControl.getInternalFile("/data/db/GeographyCodes_zh.del",
                    "AppTemp", "GeographyCodes_zh.del");
        } else {
            file = FxmlControl.getInternalFile("/data/db/GeographyCodes_en.del",
                    "AppTemp", "GeographyCodes_en.del");
        }
        DerbyBase.importData("Geography_Code", file.getAbsolutePath(), false);

    }

    /*
        get/set
     */
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCitycode() {
        return citycode;
    }

    public void setCitycode(String citycode) {
        this.citycode = citycode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getTownship() {
        return township;
    }

    public void setTownship(String township) {
        this.township = township;
    }

    public String getAdministrativeCode() {
        return AdministrativeCode;
    }

    public void setAdministrativeCode(String AdministrativeCode) {
        this.AdministrativeCode = AdministrativeCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

}
