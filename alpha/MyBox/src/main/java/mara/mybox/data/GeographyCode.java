package mara.mybox.data;

import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class GeographyCode implements Cloneable {

    protected double area;
    protected long population;
    protected AddressLevel level;
    protected String name, fullName, chineseName, englishName, levelName,
            code1, code2, code3, code4, code5, alias1, alias2, alias3, alias4, alias5,
            continent, country, province, city, county, town, village,
            building, poi, title, info, description;
    protected double longitude, latitude, altitude, precision;
    protected CoordinateSystem coordinateSystem;
    protected int markSize;

    public static CoordinateSystem defaultCoordinateSystem = CoordinateSystem.CGCS2000;
    public static AddressLevel defaultAddressLevel = AddressLevel.PointOfInterest;

    public static enum AddressLevel {
        Global, Continent, Country, Province, City, County,
        Town, Village, Building, PointOfInterest
    }

    public static enum CoordinateSystem {
        CGCS2000, GCJ_02, WGS_84, BD_09, Mapbar
    }

    public GeographyCode() {
        area = -1;
        population = -1;
        level = defaultAddressLevel;
        coordinateSystem = defaultCoordinateSystem;
        longitude = latitude = -200;
        altitude = precision = AppValues.InvalidDouble;
        markSize = -1;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            GeographyCode newCode = (GeographyCode) super.clone();
            newCode.setLevel(level);
            newCode.setCoordinateSystem(coordinateSystem);
            return newCode;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    /*
        custmized get/set
     */
    public String getName() {
        if (Languages.isChinese()) {
            name = chineseName != null ? chineseName : englishName;
        } else {
            name = englishName != null ? englishName : chineseName;
        }
        if (name == null) {
            return message(level.name());
        }
        return name;
    }

    public String getFullName() {
        getName();
        if (level == null || level.ordinal() <= 3) {
            fullName = name;
        } else {
            if (Languages.isChinese()) {
                if (country != null) {
                    fullName = country;
                } else {
                    fullName = "";
                }
                if (province != null && !province.isBlank()) {
                    fullName = fullName.isBlank() ? province : fullName + "-" + province;
                }
                if (city != null && !city.isBlank()) {
                    fullName = fullName.isBlank() ? city : fullName + "-" + city;
                }
                if (county != null && !county.isBlank()) {
                    fullName = fullName.isBlank() ? county : fullName + "-" + county;
                }
                if (town != null && !town.isBlank()) {
                    fullName = fullName.isBlank() ? town : fullName + "-" + town;
                }
                if (village != null && !village.isBlank()) {
                    fullName = fullName.isBlank() ? village : fullName + "-" + village;
                }
                if (building != null && !building.isBlank()) {
                    fullName = fullName.isBlank() ? building : fullName + "-" + building;
                }
                if (name != null && !name.isBlank()) {
                    fullName = fullName.isBlank() ? name : fullName + "-" + name;
                }
            } else {
                if (name != null) {
                    fullName = name;
                } else {
                    fullName = "";
                }
                if (building != null && !building.isBlank()) {
                    fullName = fullName.isBlank() ? building : fullName + "," + building;
                }
                if (village != null && !village.isBlank()) {
                    fullName = fullName.isBlank() ? village : fullName + "," + village;
                }
                if (town != null && !town.isBlank()) {
                    fullName = fullName.isBlank() ? town : fullName + "," + town;
                }
                if (county != null && !county.isBlank()) {
                    fullName = fullName.isBlank() ? county : fullName + "," + county;
                }
                if (city != null && !city.isBlank()) {
                    fullName = fullName.isBlank() ? city : fullName + "," + city;
                }
                if (province != null && !province.isBlank()) {
                    fullName = fullName.isBlank() ? province : fullName + "," + province;
                }
                if (country != null && !country.isBlank()) {
                    fullName = fullName.isBlank() ? country : fullName + "," + country;
                }
            }
        }
        return fullName;
    }

    public AddressLevel getLevel() {
        if (level == null) {
            level = defaultAddressLevel;
        }
        return level;
    }

    public String getLevelName() {
        levelName = message(getLevel().name());
        return levelName;
    }

    public CoordinateSystem getCoordinateSystem() {
        if (coordinateSystem == null) {
            coordinateSystem = defaultCoordinateSystem;
        }
        return coordinateSystem;
    }

    public String getTitle() {
        return title != null ? title : getName();
    }

    /*
        get/set
     */
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

    public String getCode1() {
        return code1;
    }

    public void setCode1(String code1) {
        this.code1 = code1;
    }

    public String getCode2() {
        return code2;
    }

    public void setCode2(String code2) {
        this.code2 = code2;
    }

    public String getCode3() {
        return code3;
    }

    public void setCode3(String code3) {
        this.code3 = code3;
    }

    public String getAlias1() {
        return alias1;
    }

    public void setAlias1(String alias1) {
        this.alias1 = alias1;
    }

    public String getAlias2() {
        return alias2;
    }

    public void setAlias2(String alias2) {
        this.alias2 = alias2;
    }

    public String getAlias3() {
        return alias3;
    }

    public void setAlias3(String alias3) {
        this.alias3 = alias3;
    }

    public double getLongitude() {
        return longitude;
    }

    public GeographyCode setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public GeographyCode setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public String getContinent() {
        return continent;
    }

    public String getCountry() {
        return country;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getCounty() {
        return county;
    }

    public String getTown() {
        return town;
    }

    public String getVillage() {
        return village;
    }

    public String getBuilding() {
        return building;
    }

    public String getPoi() {
        return poi;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getCode4() {
        return code4;
    }

    public void setCode4(String code4) {
        this.code4 = code4;
    }

    public String getCode5() {
        return code5;
    }

    public void setCode5(String code5) {
        this.code5 = code5;
    }

    public String getAlias4() {
        return alias4;
    }

    public void setAlias4(String alias4) {
        this.alias4 = alias4;
    }

    public String getAlias5() {
        return alias5;
    }

    public void setAlias5(String alias5) {
        this.alias5 = alias5;
    }

    public void setPoi(String poi) {
        this.poi = poi;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public void setLevel(AddressLevel level) {
        this.level = level;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public long getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        this.population = population;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public double getAltitude() {
        return altitude;
    }

    public GeographyCode setAltitude(double altitude) {
        this.altitude = altitude;
        return this;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public GeographyCode setCoordinateSystem(CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public GeographyCode setInfo(String info) {
        this.info = info;
        return this;
    }

    public GeographyCode setTitle(String label) {
        this.title = label;
        return this;
    }

    public int getMarkSize() {
        return markSize;
    }

    public GeographyCode setMarkSize(int markSize) {
        this.markSize = markSize;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public GeographyCode setDescription(String description) {
        this.description = description;
        return this;
    }

}
