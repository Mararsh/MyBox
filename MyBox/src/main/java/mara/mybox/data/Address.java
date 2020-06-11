package mara.mybox.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
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
import mara.mybox.data.AddressExtension.AddressExtensionType;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.tools.FileTools.charset;
import mara.mybox.tools.LocationTools;
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

/**
 * @Author Mara
 * @CreateDate 2020-5-27
 * @License Apache License Version 2.0
 */
public class Address {

    protected long adid, owner, area, population;
    protected AddressLevel level;
    protected AddressSource source;
    protected String name, fullName, levelName, comments;
    protected double longitude, latitude, altitude, precision;
    protected CoordinatetSystem coordinatetSystem;
    protected String chineseName, englishName, chineseFullName, englishFullName, administrativeCode, postalCode;
    protected List<String> aliases, codes;

    public enum AddressLevel {
        Global, Continent, Country, Province, City, County, Town, Village, Building, InterestOfLocation
    }

    public enum AddressSource {
        Inputted, Predefined, Geonames, Data
    }

    public enum CoordinatetSystem {
        GPS, GaoDe, Baidu
    }

    public Address() {
        adid = owner = area = population = -1;
        level = null;
        source = null;
        longitude = latitude = altitude = -200;
        coordinatetSystem = null;
        aliases = null;
        codes = null;
    }

    public static Address create() {
        return new Address();
    }

    public void setLevel(short levelValue) {
        switch (levelValue) {
            case 1:
                level = AddressLevel.Global;
                break;
            case 2:
                level = AddressLevel.Continent;
                break;
            case 3:
                level = AddressLevel.Country;
                break;
            case 4:
                level = AddressLevel.Province;
                break;
            case 5:
                level = AddressLevel.City;
                break;
            case 6:
                level = AddressLevel.County;
                break;
            case 7:
                level = AddressLevel.Town;
                break;
            case 8:
                level = AddressLevel.Village;
                break;
            case 9:
                level = AddressLevel.Building;
                break;
            default:
                level = AddressLevel.InterestOfLocation;
                break;
        }

    }

    public void setSource(short sourceValue) {
        switch (sourceValue) {
            case 2:
                source = AddressSource.Predefined;
                break;
            case 3:
                source = AddressSource.Geonames;
                break;
            case 4:
                source = AddressSource.Data;
                break;
            default:
                source = AddressSource.Inputted;
                break;
        }
    }

    public void setCoordinatetSystem(short cs) {
        switch (cs) {
            case 2:
                coordinatetSystem = CoordinatetSystem.GaoDe;
                break;
            case 3:
                coordinatetSystem = CoordinatetSystem.Baidu;
                break;
            default:
                coordinatetSystem = CoordinatetSystem.GPS;
                break;
        }
    }

    /*
        Customized get
     */
    public String getName() {
        if (name == null) {
            if (AppVariables.isChinese() && chineseName != null) {
                name = chineseName;
            } else if (englishName != null) {
                name = englishName;
            } else if (aliases != null && !aliases.isEmpty()) {
                name = aliases.get(0);
            } else if (administrativeCode != null) {
                name = administrativeCode;
            } else if (postalCode != null) {
                name = postalCode;
            } else if (codes != null && !codes.isEmpty()) {
                name = codes.get(0);
            }
        }
        return name;
    }

    public String getFullName() {
        if (name == null) {
            if (AppVariables.isChinese() && chineseFullName != null) {
                name = chineseFullName;
            } else if (englishFullName != null) {
                name = englishFullName;
            } else {
                name = getName();
            }
        }
        return name;
    }

    /*
        Static values
     */
 /*
        Static methods
     */
 /*
        get/set
     */
    public long getAdid() {
        return adid;
    }

    public void setAdid(long adid) {
        this.adid = adid;
    }

    public long getOwner() {
        return owner;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }

    public long getArea() {
        return area;
    }

    public void setArea(long area) {
        this.area = area;
    }

    public long getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        this.population = population;
    }

    public AddressLevel getLevel() {
        return level;
    }

    public void setLevel(AddressLevel level) {
        this.level = level;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
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

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public CoordinatetSystem getCoordinatetSystem() {
        return coordinatetSystem;
    }

    public void setCoordinatetSystem(CoordinatetSystem coordinatetSystem) {
        this.coordinatetSystem = coordinatetSystem;
    }

    public AddressSource getSource() {
        return source;
    }

    public void setSource(AddressSource source) {
        this.source = source;
    }

    public String getChineseName() {
        return chineseName;
    }

    public void setChineseName(String chineseName) {
        this.chineseName = chineseName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getChineseFullName() {
        return chineseFullName;
    }

    public void setChineseFullName(String chineseFullName) {
        this.chineseFullName = chineseFullName;
    }

    public String getEnglishFullName() {
        return englishFullName;
    }

    public void setEnglishFullName(String englishFullName) {
        this.englishFullName = englishFullName;
    }

    public String getAdministrativeCode() {
        return administrativeCode;
    }

    public void setAdministrativeCode(String administrativeCode) {
        this.administrativeCode = administrativeCode;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public List<String> getCodes() {
        return codes;
    }

    public void setCodes(List<String> codes) {
        this.codes = codes;
    }

}
