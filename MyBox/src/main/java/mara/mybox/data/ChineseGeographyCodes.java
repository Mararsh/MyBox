package mara.mybox.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonValues;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2020-3-8
 * @License Apache License Version 2.0
 */
/**
 * // http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/
 * Till 2019-12-30. China has following geography levels for statistic
 * Level 1: province (省/直辖市/自治州)
 * Level 2: city (一级市/盟)
 * Level 3: county (区/县/二级市/自治县/自治旗)
 * Level 4: town (街道/镇/乡)
 * Level 5: village (居委会/村委会)
 */
public class ChineseGeographyCodes {

    private static final String addressBase = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2019/";
    private static final long asiaid = 2, chinaid = 100;

    private StringBuilder currentLinks;
    private List<GeographyCode> provinces, cities, counties, towns, villages;
    private long cityid, countyid, townid, villageid, failedCount;
    private String provinceName, cityName, countyName, townName, villageName;
    private GeographyCode provinceCode, cityCode, countyCode, townCode, villageCode;

    public void make() {
        try {
            provinces = new ArrayList<>();
            cities = new ArrayList<>();
            cityid = 1200;
            countyid = 2000;
            townid = 10000;
            villageid = 100000;

//            provinces();
//            coordinatesCitiesRefer();
//            coordinatesCitiesGeoCode();
//            cities();
//            counties();
//            coordinatesCountiesGeoCode();
//            citiesAll();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public List<GeographyCode> fetch(
            String level, String address, String flagStart, String flagEnd) {
        try {
            URL url = new URL(address);
            String path = address.substring(0, address.lastIndexOf("/") + 1);
            File pageFile = FileTools.getTempFile(".txt");
            boolean ok = false;
            for (int i = 0; i < 10; i++) {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0");
                connection.connect();
                try ( BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                         BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(pageFile))) {
                    byte[] buf = new byte[CommonValues.IOBufferLength];
                    int len;
                    while ((len = inStream.read(buf)) != -1) {
                        outputStream.write(buf, 0, len);
                    }
                    ok = true;
                    break;
                } catch (Exception e) {
                    logger.debug((++failedCount) + "  " + e.toString());
                    Thread.sleep(500);
                }
            }
            if (!ok || !pageFile.exists()) {
                logger.debug(address);
                return null;
            }
            boolean started = false;
            StringBuilder s = new StringBuilder();
            try ( BufferedReader reader = new BufferedReader(new FileReader(pageFile, Charset.forName("gbk")))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!started) {
                        if (line.contains(flagStart)) {
                            started = true;
                            s.append(line);
                        }
                    } else {
                        s.append(line);
                        if (line.contains(flagEnd)) {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug(e.toString());
                return null;
            }
            String data = s.toString();
            List<GeographyCode> codes = new ArrayList<>();
            while (data.length() > 0) {
                GeographyCode code = new GeographyCode();
                switch (level) {
                    case "village":
                        data = fetchVillage(data, code);
                        break;
                    case "province":
                        data = fetchProvince(path, data, code);
                        break;
                    default:
                        data = fetchOther(path, data, code, level);
                        break;
                }
                if (data == null) {
                    break;
                }
                codes.add(code);
            }
            return codes;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public void testCharset() {
        try {
            String link = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2019/34/08/25/340825108.html";
            List<GeographyCode> villageData = fetch("village", link, "<tr class='villagetr'>", "</table>");
//            for (GeographyCode town : townsData) {
//                townCode = town;
//                town(false);
//                towns.add(townCode);
//            }
//            FileTools.writeFile(new File("D:\\玛瑞\\Mybox\\地理代码\\counties-Links.txt"), currentLinks.toString());
//            GeographyCode.exportText(new File("D:\\玛瑞\\Mybox\\地理代码\\counties.txt"), counties, true);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public String fetchProvince(String path, String data, GeographyCode code) {
        String link, name;
        int pos1 = data.indexOf("a href='");
        if (pos1 < 0) {
            return null;
        }
        data = data.substring(pos1 + "a href='".length());
        int pos2 = data.indexOf("'");
        link = path + data.substring(0, pos2).trim();
        pos1 = data.indexOf(">");
        if (pos1 < 0) {
            return null;
        }
        pos2 = data.indexOf("<");
        if (pos2 < 0) {
            return null;
        }
        name = data.substring(pos1 + 1, pos2).trim();
        code.setChineseName(name);
        code.setComments(link);
        currentLinks.append(name).append("\t").append(link).append("\n");
        return data;
    }

    public String fetchOther(String path, String data, GeographyCode code, String level) {
        String link, code1, name;
        int pos1 = data.indexOf("a href='");
        if (pos1 < 0) {
            return null;
        }
        data = data.substring(pos1 + "a href='".length());
        int pos2 = data.indexOf("'");
        link = path + data.substring(0, pos2).trim();
        pos1 = data.indexOf(">");
        if (pos1 < 0) {
            return null;
        }
        pos2 = data.indexOf("<");
        if (pos2 < 0) {
            return null;
        }
        code1 = data.substring(pos1 + 1, pos2).trim();
        data = data.substring(pos2);
        pos1 = data.indexOf("html'>");
        if (pos1 < 0) {
            return null;
        }
        data = data.substring(pos1 + "html'>".length());
        pos2 = data.indexOf("<");
        if (pos2 < 0) {
            return null;
        }
        name = data.substring(0, pos2).trim();
        code.setChineseName(name);
        code.setCode3(code1);
        code.setComments(link);
        switch (level) {
            case "city":
                currentLinks.append(provinceName);
                break;
            case "county":
                currentLinks.append(cityCode.getFullName());
                break;
            case "town":
                currentLinks.append(countyCode.getFullName());
                break;
            case "village":
                currentLinks.append(townCode.getFullName());
                break;
            default:
                break;
        }
        currentLinks.append("\t").append(name).append("\t")
                .append(link).append("\t").append(code1).append("\n");
        return data;
    }

    public String fetchVillage(String data, GeographyCode code) {
        String code1, code2, name;

        int pos1 = data.indexOf("<td>");
        if (pos1 < 0) {
            return null;
        }
        data = data.substring(pos1 + "<td>".length());
        int pos2 = data.indexOf("</td>");
        code1 = data.substring(0, pos2).trim();

        pos1 = data.indexOf("<td>");
        if (pos1 < 0) {
            return null;
        }
        data = data.substring(pos1 + "<td>".length());
        pos2 = data.indexOf("</td>");
        code2 = data.substring(0, pos2).trim();

        pos1 = data.indexOf("<td>");
        if (pos1 < 0) {
            return null;
        }
        data = data.substring(pos1 + "<td>".length());
        pos2 = data.indexOf("</td>");
        name = data.substring(0, pos2).trim();

        code.setChineseName(name);
        code.setCode3(code1);
        code.setCode4(code2);

        return data;
    }

    public void provinces() {
        try {
            cityid = 1200;
            currentLinks = new StringBuilder();
            List<GeographyCode> provincesData = fetch("province", addressBase + "index.html", "<tr class='provincetr'>", "</table>");
            FileTools.writeFile(new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\provincesLinks.txt"), currentLinks.toString(), Charset.forName("utf-8"));

            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
                currentLinks = new StringBuilder();
                provinces = new ArrayList();
                cities = new ArrayList();
                for (GeographyCode province : provincesData) {
                    String link = province.getComments();
                    provinceName = province.getChineseName();
                    String sql = "SELECT * FROM Geography_Code WHERE "
                            + " level=4 AND country=" + chinaid + " AND ( "
                            + TableGeographyCode.nameEqual(provinceName) + " ) "
                            + " ORDER BY gcid ";

                    provinceCode = TableGeographyCode.queryCode(conn, sql, true);
                    provinces.add(provinceCode);

                    List<GeographyCode> citiesData = fetch("city", link, "<tr class='citytr'>", "</table>");
                    for (GeographyCode city : citiesData) {
                        cityCode = city;
                        city();
                        cities.add(cityCode);
                    }
                }
                GeographyCode.writeInternalCSV(new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\provinces_internal.csv"), provinces);
                GeographyCode.writeExternalCSV(new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\provinces_external.csv"), provinces);
                GeographyCode.writeInternalCSV(new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\cities_internal.csv"), cities);
                GeographyCode.writeExternalCSV(new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\cities_external.csv"), cities);
                FileTools.writeFile(new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\citiesLinks.txt"), currentLinks.toString(), Charset.forName("utf-8"));
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void city() {
        try {
            cityName = cityCode.getChineseName();
            cityCode.setGcid(++cityid);
            cityCode.setLevelCode(new GeographyCodeLevel("City"));
            cityCode.setFullName(provinceCode.getFullName() + cityName);
            cityCode.setContinentName(provinceCode.getContinentName());
            cityCode.setCountry(provinceCode.getCountry());
            cityCode.setCountryName(provinceCode.getCountryName());
            cityCode.setProvince(provinceCode.getGcid());
            cityCode.setProvinceName(provinceCode.getChineseName());
            cityCode.setComments(null);
            cityCode.setChineseName(cityName);
            String sname = cityName;
            if (cityName.endsWith("自治州") || cityName.endsWith("自治县")) {
                sname = cityName.substring(0, cityName.length() - 5);
            } else if (cityName.endsWith("地区")) {
                sname = cityName.substring(0, cityName.length() - 2);
            } else if (cityName.endsWith("市") || cityName.endsWith("盟") || cityName.endsWith("县")) {
                sname = cityName.substring(0, cityName.length() - 1);
            }
//            logger.debug(cityid + " " + cityName + " " + sname);
            if (!sname.equals(cityName)) {
                cityCode.setAlias1(sname);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    public void citiesAll() {
        try {
            File file = new File("D:\\玛瑞\\Mybox\\地理代码\\citiesLinks.txt");
            Map<String, String> links = new HashMap<>();
            try ( BufferedReader reader = new BufferedReader(new FileReader(file, Charset.forName("utf-8")))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split("\t");
                    if (fields.length < 3) {
                        continue;
                    }
                    links.put(fields[0] + fields[1], fields[2]);
                }
            }

            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
                conn.setReadOnly(true);
                String sql = "SELECT * FROM Geography_Code WHERE "
                        + " level=5 AND continent=2 AND country=" + chinaid
                        + " ORDER BY gcid ";
                cities = TableGeographyCode.queryCodes(conn, sql, true);
            } catch (Exception e) {
                return;
            }
            StringBuilder s = new StringBuilder();
            for (GeographyCode city : cities) {
                cityCode = city;
                cityName = city.getFullName();
                long start = new Date().getTime();
                String link = links.get(city.getFullName());
                currentLinks = new StringBuilder();
                List<GeographyCode> countiesData = fetch("county", link, "<tr class='countytr'>", "</table>");
                counties = new ArrayList<>();
                towns = new ArrayList<>();
                villages = new ArrayList<>();
                for (GeographyCode county : countiesData) {
                    countyCode = county;
                    county(true, true);
                    counties.add(countyCode);
                }
                String d = cityName + " counties:" + counties.size()
                        + " towns:" + towns.size() + " villages:" + villages.size()
                        + " cost:" + (new Date().getTime() - start) / 60000;
                s.append(d).append("\n");
                logger.debug(d);
                FileTools.writeFile(new File("D:\\玛瑞\\Mybox\\地理代码\\links\\" + cityName + "-Links.txt"), currentLinks.toString());
                GeographyCode.exportText(new File("D:\\玛瑞\\Mybox\\地理代码\\city\\" + cityName + "-counties.txt"), counties, true);
                GeographyCode.exportText(new File("D:\\玛瑞\\Mybox\\地理代码\\city\\" + cityName + "-towns.txt"), towns, true);
                GeographyCode.exportText(new File("D:\\玛瑞\\Mybox\\地理代码\\city\\" + cityName + "-villages.txt"), villages, true);
            }
            FileTools.writeFile(new File("D:\\玛瑞\\Mybox\\地理代码\\costs.txt"), s.toString());
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void counties() {
        try {
            countyid = 2000;
            File file = new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\citiesLinks.txt");
            Map<String, String> links = new HashMap<>();
            try ( BufferedReader reader = new BufferedReader(new FileReader(file, Charset.forName("utf-8")))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split("\t");
                    if (fields.length < 3) {
                        continue;
                    }
                    links.put(fields[0] + fields[1], fields[2]);
                }
            }
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
                String sql = "SELECT * FROM Geography_Code WHERE "
                        + " level=5  AND country=" + chinaid
                        + "  ORDER BY gcid ";
                cities = TableGeographyCode.queryCodes(conn, sql, true);
                StringBuilder s = new StringBuilder();
                currentLinks = new StringBuilder();
                counties = new ArrayList<>();
                for (GeographyCode city : cities) {
                    cityCode = city;
                    cityName = city.getProvinceCode().getAlias1();
                    if (cityCode.getAlias1() != null) {
                        cityName += cityCode.getAlias1();
                    } else {
                        cityName += cityCode.getChineseName();
                    }
                    String link = links.get(cityName);
                    if (link == null) {
                        logger.debug(cityName);
                        continue;
                    }
                    List<GeographyCode> countiesData = fetch("county", link, "<tr class='countytr'>", "</table>");
                    for (GeographyCode county : countiesData) {
                        countyCode = county;
                        county(false, false);
                        counties.add(countyCode);
                    }
                }
                GeographyCode.writeInternalCSV(new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\counties_internal.csv"), counties);
                GeographyCode.writeExternalCSV(new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\counties_external.csv"), counties);
                FileTools.writeFile(new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\countiesLinks.txt"), currentLinks.toString(), Charset.forName("utf-8"));

            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void county(boolean fetchTown, boolean fetchVillage) {
        try {
            String link = countyCode.getComments();
            countyName = countyCode.getChineseName();
            countyCode.setChineseName(countyName);
            countyCode.setGcid(countyid);
            countyCode.setLevelCode(new GeographyCodeLevel("County"));
            countyCode.setFullName(cityCode.getFullName() + countyName);
            countyCode.setContinentName(cityCode.getContinentName());
            countyCode.setCountry(cityCode.getCountry());
            countyCode.setCountryName(cityCode.getCountryName());
            countyCode.setProvince(cityCode.getProvince());
            countyCode.setProvinceName(cityCode.getProvinceName());
            countyCode.setCity(cityCode.getGcid());
            countyCode.setCityName(cityCode.getChineseName());
            countyCode.setComments(null);
            if (!fetchTown) {
                return;
            }
//            logger.debug(countyid + " " + countyCode.getFullName());
            List<GeographyCode> townsData = fetch("town", link, "<tr class='towntr'>", "</table>");
            for (GeographyCode town : townsData) {
                townCode = town;
                town(fetchVillage);
                towns.add(townCode);
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    public void town(boolean fetchVillage) {
        try {
            String link = townCode.getComments();
            townName = townCode.getChineseName();
            townCode.setGcid(townid);
            townCode.setLevelCode(new GeographyCodeLevel("Town"));
            townCode.setFullName(countyCode.getFullName() + townName);
            townCode.setContinent(countyCode.getContinent());
            townCode.setContinentName(countyCode.getContinentName());
            townCode.setCountry(countyCode.getCountry());
            townCode.setCountryName(countyCode.getCountryName());
            townCode.setProvince(countyCode.getProvince());
            townCode.setProvinceName(countyCode.getProvinceName());
            townCode.setCity(countyCode.getCity());
            townCode.setCityName(countyCode.getCityName());
            townCode.setCounty(countyCode.getGcid());
            townCode.setCountyName(countyCode.getChineseName());
            townCode.setComments(null);
//            logger.debug(townid + " " + townCode.getFullName());
            if (!fetchVillage) {
                return;
            }
            List<GeographyCode> villagesData = fetch("village", link, "<tr class='villagetr'>", "</table>");
            for (GeographyCode village : villagesData) {
                villageCode = village;
                village();
                villages.add(villageCode);
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    public void village() {
        try {
            villageName = villageCode.getChineseName();
            villageCode.setGcid(villageid);
            villageCode.setLevelCode(new GeographyCodeLevel("Village"));
            villageCode.setFullName(townCode.getFullName() + villageName);
            villageCode.setContinent(townCode.getContinent());
            villageCode.setContinentName(townCode.getContinentName());
            villageCode.setCountry(townCode.getCountry());
            villageCode.setCountryName(townCode.getCountryName());
            villageCode.setProvince(townCode.getProvince());
            villageCode.setProvinceName(townCode.getProvinceName());
            villageCode.setCity(townCode.getCity());
            villageCode.setCityName(townCode.getCityName());
            villageCode.setCounty(townCode.getCounty());
            villageCode.setCountyName(townCode.getCountyName());
            villageCode.setTown(townCode.getGcid());
            villageCode.setTownName(townCode.getChineseName());
            villageCode.setComments(null);
//            logger.debug(villageid + " " + villageCode.getFullName());

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    public void towns() {
        try {
            townid = 10000;
            File file = new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\countiesLinks.txt");
            Map<String, String> links = new HashMap<>();
            try ( BufferedReader reader = new BufferedReader(new FileReader(file, Charset.forName("utf-8")))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split("\t");
                    if (fields.length < 3) {
                        continue;
                    }
                    links.put(fields[0] + fields[1], fields[2]);
                }
            }
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
                String sql = "SELECT * FROM Geography_Code WHERE "
                        + " level=4  AND country=" + chinaid
                        + "  ORDER BY gcid ";
                provinces = TableGeographyCode.queryCodes(conn, sql, true);
                for (GeographyCode province : provinces) {
                    provinceCode = province;
                    provinceName = province.getChineseName();
                    long start = new Date().getTime();
                    currentLinks = new StringBuilder();
                    towns = new ArrayList<>();
                    sql = "SELECT * FROM Geography_Code WHERE "
                            + " level=6 AND country=" + chinaid + " AND "
                            + " province=" + provinceCode.getGcid()
                            + "  ORDER BY gcid ";
                    counties = TableGeographyCode.queryCodes(conn, sql, true);
                    String link;
                    for (GeographyCode county : counties) {
                        countyCode = county;
                        countyName = countyCode.getFullName();
                        link = links.get(countyName);
                        if (link == null) {
                            logger.debug(countyName);
                            continue;
                        }
//                        logger.debug(countyName + "  " + link);
                        List<GeographyCode> townsData = fetch("town", link, "<tr class='towntr'>", "</table>");
                        if (townsData == null || townsData.isEmpty()) {
                            logger.debug(countyName);
                            continue;
                        }
                        for (GeographyCode town : townsData) {
                            townCode = town;
                            town(false);
                            towns.add(townCode);
                        }
                    }
                    GeographyCode.writeInternalCSV(new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\internal\\" + provinceName + "_towns_internal.csv"), towns);
                    GeographyCode.writeExternalCSV(new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\external\\" + provinceName + "_towns_external.csv"), towns);
                    FileTools.writeFile(new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\" + provinceName + "_towns_Links.txt"), currentLinks.toString());
                    logger.debug(provinceName + " " + towns.size() + " 花费:" + ((new Date().getTime() - start) / 1000) + "秒");
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void villages() {
        try {
//            villageid = 100000;
            villageid = 738406;
            failedCount = 0;
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
                String sql = "SELECT * FROM Geography_Code WHERE "
                        + " level=4 AND country=" + chinaid
                        + " AND province > 1028 AND province < 1031 "
                        + "  ORDER BY gcid ";
                provinces = TableGeographyCode.queryCodes(conn, sql, true);
                for (GeographyCode province : provinces) {
                    provinceCode = province;
                    provinceName = province.getChineseName();
                    File file = new File("D:\\玛瑞\\Mybox\\地理代码\\中国国家统计局\\towns\\townLinks\\" + provinceName + "_towns_Links.txt");
                    if (!file.exists()) {
                        continue;
                    }
                    Map<String, String> links = new HashMap<>();
                    try ( BufferedReader reader = new BufferedReader(new FileReader(file, Charset.forName("utf-8")))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] fields = line.split("\t");
                            if (fields.length < 3) {
                                continue;
                            }
                            links.put(fields[0] + fields[1], fields[2]);
                        }
                    }
                    long start = new Date().getTime();
                    currentLinks = new StringBuilder();
                    villages = new ArrayList<>();
                    sql = "SELECT * FROM Geography_Code WHERE "
                            + " level=7  AND country=" + chinaid + " AND "
                            + " province=" + provinceCode.getGcid()
                            + " ORDER BY gcid ";
                    towns = TableGeographyCode.queryCodes(conn, sql, true);
                    String link;
                    logger.debug(provinceName + " towns:" + towns.size());
                    for (GeographyCode town : towns) {
                        townCode = town;
                        townName = townCode.getFullName();
                        link = links.get(townName);
                        if (link == null) {
                            logger.debug("NoLink  " + townName);
                            continue;
                        }
                        List<GeographyCode> villagesData = fetch("village", link, "<tr class='villagetr'>", "</table>");
                        if (villagesData == null) {
                            logger.debug("NoData  " + townName);
                            continue;
                        }
                        for (GeographyCode village : villagesData) {
                            villageCode = village;
                            village();
                            villages.add(villageCode);
                        }
                    }
                    GeographyCode.writeInternalCSV(
                            new File("D:\\玛瑞\\Mybox\\地理代码\\中国国家统计局\\villages\\internal\\" + provinceName + "_villages_internal.csv"), villages);
                    GeographyCode.writeExternalCSV(
                            new File("D:\\玛瑞\\Mybox\\地理代码\\中国国家统计局\\villages\\external\\" + provinceName + "_villages_external.csv"), villages);
                    logger.debug(provinceName + " towns:" + towns.size() + " villages:" + villages.size() + " 花费:" + ((new Date().getTime() - start) / 1000) + "秒");
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void fixCounties() {
        try {
            File file = new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\counties_internal.csv");
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                     CSVParser parser = CSVParser.parse(file, StandardCharsets.UTF_8,
                            CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(',').withTrim().withNullString(""))) {
                List<String> names = parser.getHeaderNames();
                for (CSVRecord record : parser) {
                    GeographyCode code = GeographyCode.readIntenalRecord(names, record);
                    String sql = "SELECT * FROM Geography_Code WHERE "
                            + " level=6  AND country=" + chinaid + " AND gcid=" + code.getGcid()
                            + "  ORDER BY gcid ";
                    GeographyCode dbcode = TableGeographyCode.queryCode(conn, sql, true);
                    if (dbcode == null) {
                        logger.debug(code.getFullName() + "  " + code.getGcid());
                        GeographyCode query = GeographyCode.geoCode(code.getFullName());
                        if (query == null || query.getLongitude() < -180) {
                            logger.debug(code.getFullName() + "  " + code.getGcid());
                        } else {
                            code.setLongitude(query.getLongitude());
                            code.setLatitude(query.getLatitude());
                            code.setCode1(query.getCode1());
                            code.setCode2(query.getCode2());
                            code.setCode5(query.getCode5());
                            TableGeographyCode.write(conn, code);
                            logger.debug(code.getFullName() + "  " + code.getGcid());
                        }
                    } else if (!code.getChineseName().equals(dbcode.getChineseName())) {
                        logger.debug(code.getGcid() + "  " + code.getFullName() + "  " + dbcode.getChineseName());
                    }
                }

            } catch (Exception e) {
                logger.debug(e.toString());
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void fixTowns() {
        try {
            File file = new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\internal\\towns_internal.csv");
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                     Statement statement = conn.createStatement();
                     CSVParser parser = CSVParser.parse(file, StandardCharsets.UTF_8,
                            CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(',').withTrim().withNullString(""))) {
                List<String> names = parser.getHeaderNames();
                for (CSVRecord record : parser) {
                    GeographyCode code = GeographyCode.readIntenalRecord(names, record);
                    String sql = "SELECT * FROM Geography_Code WHERE "
                            + " country=" + chinaid + " AND gcid=" + code.getGcid() + "  AND "
                            + " level=7  ORDER BY gcid ";
                    GeographyCode dbcode = TableGeographyCode.queryCode(conn, sql, true);
                    if (dbcode == null) {
                        logger.debug(code.getFullName() + "  " + code.getGcid());
                        GeographyCode query = GeographyCode.geoCode(code.getFullName());
                        if (query == null || query.getLongitude() < -180) {
                            logger.debug(code.getFullName() + "  " + code.getGcid());
                        } else {
                            code.setLongitude(query.getLongitude());
                            code.setLatitude(query.getLatitude());
                            code.setCode1(query.getCode1());
                            code.setCode2(query.getCode2());
                            code.setCode5(query.getCode5());
                            TableGeographyCode.write(conn, code);
                            logger.debug(code.getFullName() + "  " + code.getGcid());
                        }
                    } else if (!code.getChineseName().equals(dbcode.getChineseName())) {
                        logger.debug(code.getGcid() + "  " + code.getFullName() + "  " + dbcode.getChineseName());
                    }
                }

            } catch (Exception e) {
                logger.debug(e.toString());
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void provinceCoordinates() {
        try {
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                     Statement statement = conn.createStatement()) {
                String sql = "SELECT * FROM Geography_Code WHERE "
                        + " country=" + chinaid + " AND "
                        + " level=4  ORDER BY gcid ";
                provinces = TableGeographyCode.queryCodes(conn, sql, true);
                for (GeographyCode province : provinces) {
                    provinceName = province.getChineseName();
                    GeographyCode gcode = GeographyCode.geoCode(provinceName);
                    if (gcode == null || gcode.getLongitude() < -180) {
                        logger.debug(province.getGcid() + " " + provinceName);
                    } else {
                        if (province.getLongitude() != gcode.getLongitude()
                                || province.getLatitude() != gcode.getLatitude()) {
                            logger.debug(provinceName + "  " + province.getGcid()
                                    + "  db " + province.getLongitude() + " " + province.getLatitude()
                                    + "  geo " + gcode.getLongitude() + " " + gcode.getLatitude()
                            );
                            province.setLongitude(gcode.getLongitude());
                            province.setLatitude(gcode.getLatitude());
                            province.setCode1(gcode.getCode1());
                            province.setCode2(gcode.getCode2());
                            province.setCode5(gcode.getCode5());
                            TableGeographyCode.update(conn, province);
                        }
                    }
                }
            }
            GeographyCode.writeInternalCSV(new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\Geography_Code_China\\internal_csv\\Geography_Code_china_provinces_internal.csv"), provinces);
            GeographyCode.writeExternalCSV(new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\Geography_Code_China\\external_csv\\Geography_Code_china_provinces_external.csv"), provinces);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void cityCoordinates() {
        try {
            List<GeographyCode> fixed = new ArrayList<>();
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                     Statement statement = conn.createStatement()) {
                cities = GeographyCode.readInternalCSV(new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\internal\\cities_internal.csv"));
                for (GeographyCode city : cities) {
                    String sql = "SELECT * FROM Geography_Code WHERE "
                            + " country=" + chinaid + " AND  "
                            + " province=" + city.getProvince() + " AND  "
                            + " gcid=" + city.getGcid() + " AND  "
                            + " level=5  ";
                    cityCode = TableGeographyCode.queryCode(conn, sql, true);
                    if (!city.getChineseName().equals(cityCode.getChineseName()) && !city.getChineseName().equals(cityCode.getAlias1())) {
                        logger.debug(city.getGcid() + " " + city.getChineseName() + "  " + cityCode.getChineseName());
                        continue;
                    }
                    String fullname = city.getFullName();
                    GeographyCode gcode = GeographyCode.geoCode(fullname);
//                    logger.debug(fullname + "  " + city.getDataid()
//                            + "  db " + cityCode.getLongitude() + " " + cityCode.getLatitude());
                    if (gcode == null || gcode.getLongitude() < -180) {
                        logger.debug(city.getGcid() + " " + fullname);
                    } else {
                        if (cityCode.getLongitude() != gcode.getLongitude()
                                || cityCode.getLatitude() != gcode.getLatitude()) {
                            logger.debug(fullname + "  " + city.getGcid()
                                    + "  db " + cityCode.getLongitude() + " " + cityCode.getLatitude()
                                    + "  geo " + gcode.getLongitude() + " " + gcode.getLatitude()
                            );
                        }
                        cityCode.setLongitude(gcode.getLongitude());
                        cityCode.setLatitude(gcode.getLatitude());
                        cityCode.setCode1(gcode.getCode1());
                        cityCode.setCode2(gcode.getCode2());
                        cityCode.setCode5(gcode.getCode5());
                        TableGeographyCode.update(conn, cityCode);
                    }
                    fixed.add(cityCode);
                }
            }
            GeographyCode.writeInternalCSV(new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\Geography_Code_China\\internal_csv\\Geography_Code_china_cities_internal.csv"), fixed);
            GeographyCode.writeExternalCSV(new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\Geography_Code_China\\external_csv\\Geography_Code_china_cities_external.csv"), fixed);

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void countyCoordinates() {
        try {
            List<GeographyCode> fixed = new ArrayList<>();
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                     Statement statement = conn.createStatement()) {
                counties = GeographyCode.readInternalCSV(new File("D:\\玛瑞\\Mybox\\地理代码\\ChinaNationalBureauOfStatistic\\internal\\counties_internal.csv"));
                for (GeographyCode county : counties) {
                    String sql = "SELECT * FROM Geography_Code WHERE "
                            + " country=" + chinaid + " AND  "
                            + " province=" + county.getProvince() + " AND  "
                            + " city=" + county.getCity() + " AND  "
                            + " gcid=" + county.getGcid() + " AND  "
                            + " level=6  ";
                    countyCode = TableGeographyCode.queryCode(conn, sql, true);
                    if (!county.getChineseName().equals(countyCode.getChineseName())) {
                        logger.debug(county.getGcid() + " " + county.getChineseName() + "  " + countyCode.getChineseName());
                        continue;
                    }
                    String fullname = countyCode.getProvinceName();
                    if (countyCode.getCityCode().getAlias1() == null) {
                        fullname += countyCode.getCityName();
                    } else {
                        fullname += countyCode.getCityCode().getAlias1();
                    }
                    fullname += countyCode.getChineseName();
                    GeographyCode gcode = GeographyCode.geoCode("a389d47ae369e57e0c2c7e32e845d1b0", fullname);
//                    logger.debug(fullname + "  " + city.getDataid()
//                            + "  db " + cityCode.getLongitude() + " " + cityCode.getLatitude());
                    if (gcode == null || gcode.getLongitude() < -180) {
                        logger.debug(countyCode.getGcid() + " " + fullname);
                    } else {
                        if (countyCode.getLongitude() != gcode.getLongitude()
                                || countyCode.getLatitude() != gcode.getLatitude()) {
                            logger.debug(fullname + "  " + countyCode.getGcid()
                                    + "  db " + countyCode.getLongitude() + " " + countyCode.getLatitude()
                                    + "  geo " + gcode.getLongitude() + " " + gcode.getLatitude()
                            );
                        }
                        countyCode.setLongitude(gcode.getLongitude());
                        countyCode.setLatitude(gcode.getLatitude());
                        countyCode.setCode1(gcode.getCode1());
                        countyCode.setCode2(gcode.getCode2());
                        countyCode.setCode5(gcode.getCode5());
                        TableGeographyCode.update(conn, countyCode);
                    }
                    fixed.add(countyCode);
                }
            }
            GeographyCode.writeInternalCSV(new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\Geography_Code_China\\internal_csv\\Geography_Code_china_counties_internal.csv"), fixed);
            GeographyCode.writeExternalCSV(new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\Geography_Code_China\\external_csv\\Geography_Code_china_counties_external.csv"), fixed);

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void townCoordinates() {
        try {
            List<String> keys = new ArrayList();
            keys.add("98a4164af224b27303c43050a4548a72");
            keys.add("6e06097442fa572d894332bb50fd0f47");
            keys.add("6417170734205c4058e673cd6cc1b57e");
            keys.add("d4f56f6eea366c3eacdb587cfabaac70");
            keys.add("b9c55a022a9f3c2fed85029d4a6d3af7");
            keys.add("49262401445c9785f1fdd290d120a15a");
            keys.add("bc2d6788683ad5850854cdfa2bdeb723");
            keys.add("3803347cfcc48402ac2cfdbf140242ec");
//            keys.add("d7444d9a7fae01fa850236d909ad4450");  // 63
            keys.add("a389d47ae369e57e0c2c7e32e845d1b0"); // 621
            int count = 0;
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                     Statement statement = conn.createStatement()) {
                String sql = "SELECT * FROM Geography_Code WHERE "
                        + " country=100 AND"
                        + " level=4 ORDER BY gcid ";
                provinces = TableGeographyCode.queryCodes(conn, sql, true);
                for (GeographyCode province : provinces) {
                    provinceName = province.getChineseName();
                    sql = "SELECT * FROM Geography_Code WHERE "
                            + " country=100 AND province=" + province.getGcid()
                            + "AND level=7 ORDER BY gcid ";
                    towns = TableGeographyCode.queryCodes(conn, sql, true);
                    logger.debug(provinceName + "  " + towns.size());
                    for (GeographyCode town : towns) {
                        String fullname = town.getProvinceName();
                        if (town.getCityCode().getAlias1() == null) {
                            fullname += town.getCityName();
                        } else {
                            fullname += town.getCityCode().getAlias1();
                        }
                        fullname += town.getCountyName() + town.getChineseName();
//                        logger.debug(town.getDataid() + " " + town.getCity() + " " + town.getCityName()
//                                + " " + (town.getCityCode() != null) + " " + town.getCityCode().getAlias1() + " " + fullname);
                        String key = keys.get((count++) / 5500);
                        GeographyCode gcode = GeographyCode.geoCode(key, fullname);

                        if (count % 500 == 0) {
                            logger.debug(town.getGcid() + " " + fullname);
                        }
                        if (gcode == null || gcode.getLongitude() < -180) {
                            logger.debug(town.getGcid() + " " + town.getCity() + " " + fullname);
                        } else {
//                            logger.debug(fullname + "  " + town.getDataid()
//                                    + "  geo " + gcode.getLongitude() + " " + gcode.getLatitude()
//                            );
//                            if (town.getLongitude() != gcode.getLongitude()
//                                    || town.getLatitude() != gcode.getLatitude()) {
//                                logger.debug(fullname + "  " + town.getDataid()
//                                        + "  db " + town.getLongitude() + " " + town.getLatitude()
//                                        + "  geo " + gcode.getLongitude() + " " + gcode.getLatitude()
//                                );
//                            }
                            town.setLongitude(gcode.getLongitude());
                            town.setLatitude(gcode.getLatitude());
                            town.setCode1(gcode.getCode1());
                            town.setCode2(gcode.getCode2());
                            town.setCode5(gcode.getCode5());
                            TableGeographyCode.update(conn, town);
                        }
                    }
                    GeographyCode.writeInternalCSV(
                            new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\Geography_Code_China\\internal_csv\\china_province_towns_internal\\"
                                    + provinceName + "_towns_internal.csv"), towns);
                    GeographyCode.writeExternalCSV(
                            new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\Geography_Code_China\\external_csv\\china_province_towns_external\\"
                                    + provinceName + "_towns_external.csv"), towns);
                }
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void villageCoordinates() {
        try {
            List<String> keys = new ArrayList();
            keys.add("6e06097442fa572d894332bb50fd0f47");
            keys.add("6417170734205c4058e673cd6cc1b57e");
            keys.add("d4f56f6eea366c3eacdb587cfabaac70");
            keys.add("b9c55a022a9f3c2fed85029d4a6d3af7");
            keys.add("49262401445c9785f1fdd290d120a15a");
            keys.add("bc2d6788683ad5850854cdfa2bdeb723");
            keys.add("3803347cfcc48402ac2cfdbf140242ec");
            keys.add("45d0e6f82451b61239b17667d8a58737");
            keys.add("4b71c22eb36ce6876be0fcbf375e674a");
            keys.add("4db0775ad414bec39257919332e81845");
            keys.add("6eef08f627d0bc0537fb9845d09b3c3a");
            keys.add("fc6e6d5e402f6f92db4cb445c831dc9f");
            keys.add("b7747f5bb89f02edb928a4420d1a2bb7");
            keys.add("43d49dbf38a32f53598623d68c0cea67");
            keys.add("49401b102a6b3ade534ec523b34ca864");
            keys.add("cf0e5d81f3e8193fa7dc110678bd3af4");
            keys.add("9b744f018266c0251c130e72f632a71b");
            keys.add("98a4164af224b27303c43050a4548a72");
//            keys.add("d7444d9a7fae01fa850236d909ad4450");  // 63
//            keys.add("a389d47ae369e57e0c2c7e32e845d1b0"); // 621

            int count = 0;
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                     Statement statement = conn.createStatement()) {
                String sql = "SELECT * FROM Geography_Code WHERE "
                        + " country=100 AND  province > 1023 AND "
                        + " level=4 ORDER BY gcid ";
                provinces = TableGeographyCode.queryCodes(conn, sql, true);
                List<GeographyCode> coordinates;
                int keyIndex = 0;
                int lastKey = -1;
                String key = "";
                for (GeographyCode province : provinces) {
                    provinceName = province.getChineseName();
                    villages = GeographyCode.readInternalCSV(
                            new File("D:\\玛瑞\\Mybox\\地理代码\\中国国家统计局\\villages\\internal\\" + provinceName + "_villages_internal.csv"));
                    logger.debug(provinceName + "  " + villages.size());
                    long start = new Date().getTime();
                    coordinates = new ArrayList();
                    for (GeographyCode village : villages) {
                        if (village.getGcid() <= 676587) {
                            continue;
                        }
                        String fullname = village.getFullName();
                        keyIndex = (count++) / 5800;
                        if (lastKey != keyIndex) {
                            if (keyIndex > keys.size() - 1) {
                                logger.debug("out fo keys: " + count);
                                return;
                            }
                            key = keys.get(keyIndex);
                            logger.debug(count + "  " + keyIndex + "  " + key);
                            lastKey = keyIndex;
                            if (!coordinates.isEmpty()) {
                                int filelIndex = keyIndex + 34;
                                GeographyCode.writeInternalCSV(
                                        new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\中文\\中国村庄\\china_province_villages_internal\\"
                                                + provinceName + "_villages_internal_" + filelIndex + ".csv"), coordinates, false);
                                GeographyCode.writeExternalCSV(
                                        new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\中文\\中国村庄\\china_province_villages_external\\"
                                                + provinceName + "_villages_external_" + filelIndex + ".csv"), coordinates, false);
                                logger.debug(provinceName + " villages: " + filelIndex + " " + coordinates.size() + " 花费:" + ((new Date().getTime() - start) / 1000) + "秒");
                                coordinates = new ArrayList();
                                start = new Date().getTime();
                            }

                        }
                        GeographyCode gcode = GeographyCode.geoCode(key, fullname);
                        if (gcode == null || gcode.getLongitude() < -180) {
                            logger.debug(village.getGcid() + " " + fullname);
                        } else {
                            village.setLongitude(gcode.getLongitude());
                            village.setLatitude(gcode.getLatitude());
                            village.setCode1(gcode.getCode1());
                            village.setCode2(gcode.getCode2());
                            village.setCode5(gcode.getCode5());
//                            logger.debug(village.getDataid() + " " + fullname + " " + gcode.getLongitude() + " " + gcode.getLatitude());
                        }
                        coordinates.add(village);
                    }
                    if (!coordinates.isEmpty()) {
                        int filelIndex = keyIndex + 35;
                        GeographyCode.writeInternalCSV(
                                new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\中文\\中国村庄\\china_province_villages_internal\\"
                                        + provinceName + "_villages_internal_" + filelIndex + ".csv"), coordinates, false);
                        GeographyCode.writeExternalCSV(
                                new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\中文\\中国村庄\\china_province_villages_external\\"
                                        + provinceName + "_villages_external_" + filelIndex + ".csv"), coordinates, false);
                        logger.debug(provinceName + " villages: " + filelIndex + " " + coordinates.size() + " 花费:" + ((new Date().getTime() - start) / 1000) + "秒");
                    }
                }
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void fixVillageCoordinates() {
        try {
            List<String> keys = new ArrayList();
//            keys.add("6e06097442fa572d894332bb50fd0f47");
//            keys.add("6417170734205c4058e673cd6cc1b57e");
//            keys.add("d4f56f6eea366c3eacdb587cfabaac70");
//            keys.add("b9c55a022a9f3c2fed85029d4a6d3af7");
//            keys.add("49262401445c9785f1fdd290d120a15a");
//            keys.add("bc2d6788683ad5850854cdfa2bdeb723");
//            keys.add("3803347cfcc48402ac2cfdbf140242ec");
//            keys.add("45d0e6f82451b61239b17667d8a58737");
//            keys.add("4b71c22eb36ce6876be0fcbf375e674a");
//            keys.add("4db0775ad414bec39257919332e81845");
//            keys.add("6eef08f627d0bc0537fb9845d09b3c3a");
//            keys.add("fc6e6d5e402f6f92db4cb445c831dc9f");
//            keys.add("b7747f5bb89f02edb928a4420d1a2bb7");
//            keys.add("43d49dbf38a32f53598623d68c0cea67");
//            keys.add("49401b102a6b3ade534ec523b34ca864");
            keys.add("cf0e5d81f3e8193fa7dc110678bd3af4");
            keys.add("9b744f018266c0251c130e72f632a71b");
            keys.add("98a4164af224b27303c43050a4548a72");
//            keys.add("d7444d9a7fae01fa850236d909ad4450");  // 63
//            keys.add("a389d47ae369e57e0c2c7e32e845d1b0"); // 621

            int count = 0;
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                     Statement statement = conn.createStatement()) {
                String sql = "SELECT * FROM Geography_Code WHERE "
                        + " country=100 AND level=4 ORDER BY gcid ";
                provinces = TableGeographyCode.queryCodes(conn, sql, true);
                int keyIndex = 0;
                int lastKey = -1;
                String key = "";
                boolean missed = false, fixed = true;
                for (GeographyCode province : provinces) {
                    provinceName = province.getChineseName();
                    File internalFile = new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\中文\\中国村庄miss\\china_province_villages_internal\\"
                            + provinceName + "_villages_internal.csv");
                    if (!internalFile.exists()) {
                        continue;
                    }
                    long start = new Date().getTime();
                    villages = GeographyCode.readInternalCSV(internalFile);
                    logger.debug(provinceName + "  " + villages.size());
                    missed = false;
                    for (GeographyCode village : villages) {
                        if (village.validCoordinate()) {
                            continue;
                        }
                        missed = true;
                        count++;
                        String fullname = village.getFullName().substring(4);
                        logger.debug(count + " miss: " + village.getGcid() + " " + fullname);
                        keyIndex = count / 5800;
                        if (lastKey != keyIndex) {
                            if (keyIndex > keys.size() - 1) {
                                logger.debug("out fo keys: " + count);
                                return;
                            }
                            key = keys.get(keyIndex);
                            logger.debug(count + "  " + keyIndex + "  " + key);
                            lastKey = keyIndex;
                        }
                        int retry = 0;
                        fixed = false;
                        while (retry < 5) {
                            GeographyCode gcode = GeographyCode.geoCode(key, fullname);
                            if (gcode == null || !gcode.validCoordinate()) {
                                logger.debug("failed: " + village.getGcid() + " " + fullname);
                                Thread.sleep(1000);
                                retry++;
                            } else {
                                village.setLongitude(gcode.getLongitude());
                                village.setLatitude(gcode.getLatitude());
                                village.setCode1(gcode.getCode1());
                                village.setCode2(gcode.getCode2());
                                village.setCode5(gcode.getCode5());
                                fixed = true;
                                logger.debug(village.getGcid() + " " + fullname + " " + gcode.getLongitude() + " " + gcode.getLatitude());
                                break;
                            }
                        }
                        if (!fixed) {
                            break;
                        }
                    }
                    if (!missed) {
                        new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\中文\\中国村庄miss\\china_province_villages_internal\\"
                                + provinceName + "_villages_internal.csv").
                                renameTo(new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\中文\\中国村庄\\内部格式\\"
                                        + provinceName + "_villages_internal.csv"));
                        new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\中文\\中国村庄miss\\china_province_villages_external\\"
                                + provinceName + "_villages_external.csv").
                                renameTo(new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\中文\\中国村庄\\外部格式\\"
                                        + provinceName + "_villages_external.csv"));
                        logger.debug("Rename:  " + provinceName + " villages: " + villages.size() + " 花费:" + ((new Date().getTime() - start) / 1000) + "秒");
                    } else {
                        if (fixed) {
                            GeographyCode.writeInternalCSV(
                                    new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\中文\\中国村庄\\内部格式\\"
                                            + provinceName + "_villages_internal.csv"), villages, false);
                            GeographyCode.writeExternalCSV(
                                    new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\中文\\中国村庄\\外部格式\\"
                                            + provinceName + "_villages_external.csv"), villages, false);
                            logger.debug("Fixed:  " + provinceName + " villages: " + villages.size() + " 花费:" + ((new Date().getTime() - start) / 1000) + "秒");
                            new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\中文\\中国村庄miss\\china_province_villages_internal\\"
                                    + provinceName + "_villages_internal.csv").delete();
                            new File("D:\\玛瑞\\Mybox\\地理代码\\mybox地理代码\\中文\\中国村庄miss\\china_province_villages_external\\"
                                    + provinceName + "_villages_external.csv").delete();
                        } else {
                            logger.debug("Not Fixed:  " + provinceName + " villages: " + villages.size() + " 花费:" + ((new Date().getTime() - start) / 1000) + "秒");
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

}
