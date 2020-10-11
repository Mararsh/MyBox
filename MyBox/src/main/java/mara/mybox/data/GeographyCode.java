package mara.mybox.data;

import mara.mybox.data.tools.GeographyCodeTools;
import mara.mybox.db.TableBase;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class GeographyCode extends TableData {

    protected long gcid, owner, continent, country, province, city, county, town, village, building,
            area, population;
    protected int level;
    protected GeographyCodeLevel levelCode;
    protected String name, fullName, chineseName, englishName, levelName,
            code1, code2, code3, code4, code5, alias1, alias2, alias3, alias4, alias5, comments,
            continentName, countryName, provinceName, cityName, countyName, townName, villageName,
            buildingName, poiName;
    protected double longitude, latitude, altitude, precision;
    protected GeographyCode ownerCode, continentCode, countryCode, provinceCode, cityCode,
            countyCode, townCode, villageCode, buildingCode;
    protected CoordinateSystem coordinateSystem;
    protected AddressSource source;
    protected String sourceName;

    public enum AddressLevel {
        Global, Continent, Country, Province, City, County, Town, Village, Building, InterestOfLocation
    }

    public enum AddressSource {
        PredefinedData, InputtedData, Geonames, ImportedData, Unknown
    }

    public GeographyCode() {
        gcid = continent = country = province = city = county = village = town = building
                = area = population = -1;
        level = 10;
        levelCode = null;
        source = AddressSource.InputtedData;
        longitude = latitude = -200;
        altitude = precision = CommonValues.InvalidDouble;
        continentCode = null;
        countryCode = null;
        provinceCode = null;
        cityCode = null;
        countyCode = null;
        townCode = null;
        villageCode = null;
        buildingCode = null;

    }

    @Override
    public TableBase getTable() {
        if (table == null) {
            table = new TableGeographyCode();
        }
        return table;
    }

    @Override
    public boolean valid() {
        return (chineseName != null && !chineseName.isBlank())
                || (englishName != null && !englishName.isBlank());
    }

    @Override
    public boolean setValue(String column, Object value) {
        if (column == null) {
            return false;
        }
        try {
            switch (column) {
                case "gcid":
                    gcid = value == null ? -1 : (long) value;
                    return true;
                case "level":
                    level = value == null ? -1 : (int) value;
                    levelCode = new GeographyCodeLevel(level);
                    return true;
                case "chinese_name":
                    chineseName = value == null ? null : (String) value;
                    return true;
                case "english_name":
                    englishName = value == null ? null : (String) value;
                    return true;
                case "longitude":
                    longitude = value == null ? CommonValues.InvalidDouble : (Double) value;
                    return true;
                case "latitude":
                    latitude = value == null ? CommonValues.InvalidDouble : (Double) value;
                    return true;
                case "altitude":
                    altitude = value == null ? CommonValues.InvalidDouble : (Double) value;
                    return true;
                case "precision":
                    precision = value == null ? CommonValues.InvalidDouble : (Double) value;
                    return true;
                case "coordinate_system":
                    coordinateSystem = value == null
                            ? CoordinateSystem.defaultCode() : new CoordinateSystem((short) value);
                    return true;
                case "area":
                    area = value == null ? CommonValues.InvalidLong : (long) value;
                    return true;
                case "population":
                    population = value == null ? CommonValues.InvalidLong : (long) value;
                    return true;
                case "code1":
                    code1 = value == null ? null : (String) value;
                    return true;
                case "code2":
                    code2 = value == null ? null : (String) value;
                    return true;
                case "code3":
                    code3 = value == null ? null : (String) value;
                    return true;
                case "code4":
                    code4 = value == null ? null : (String) value;
                    return true;
                case "code5":
                    code5 = value == null ? null : (String) value;
                    return true;
                case "alias1":
                    code1 = value == null ? null : (String) value;
                    return true;
                case "alias2":
                    alias2 = value == null ? null : (String) value;
                    return true;
                case "alias3":
                    alias3 = value == null ? null : (String) value;
                    return true;
                case "alias4":
                    alias4 = value == null ? null : (String) value;
                    return true;
                case "alias5":
                    alias5 = value == null ? null : (String) value;
                    return true;
                case "owner":
                    owner = value == null ? null : (long) value;
                    return true;
                case "continent":
                    continent = value == null ? null : (long) value;
                    return true;
                case "country":
                    country = value == null ? null : (long) value;
                    return true;
                case "province":
                    province = value == null ? null : (long) value;
                    return true;
                case "city":
                    city = value == null ? null : (long) value;
                    return true;
                case "county":
                    county = value == null ? null : (long) value;
                    return true;
                case "town":
                    town = value == null ? null : (long) value;
                    return true;
                case "village":
                    village = value == null ? null : (long) value;
                    return true;
                case "building":
                    building = value == null ? null : (long) value;
                    return true;
                case "comments":
                    comments = value == null ? null : (String) value;
                    return true;
                case "gcsource":
                    short s = value == null ? -1 : (short) value;
                    source = source(s);
                    return true;
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return false;
    }

    @Override
    public Object getValue(String column) {
        if (column == null) {
            return null;
        }
        switch (column) {
            case "gcid":
                return gcid;
            case "level":
                return getLevel();
            case "longitude":
                return longitude;
            case "latitude":
                return latitude;
            case "altitude":
                return altitude;
            case "precision":
                return precision;
            case "coordinate_system":
                return coordinateSystem == null
                        ? CoordinateSystem.defaultCode().intValue()
                        : coordinateSystem.intValue();
            case "chinese_name":
                return chineseName;
            case "english_name":
                return englishName;
            case "code1":
                return code1;
            case "code2":
                return code2;
            case "code3":
                return code3;
            case "code4":
                return code4;
            case "code5":
                return code5;
            case "alias1":
                return alias1;
            case "alias2":
                return alias2;
            case "alias3":
                return alias3;
            case "alias4":
                return alias4;
            case "alias5":
                return alias5;
            case "owner":
                return owner;
            case "continent":
                return this.getContinent();
            case "country":
                return this.getCountry();
            case "province":
                return this.getProvince();
            case "city":
                return this.getCity();
            case "county":
                return this.getCounty();
            case "town":
                return this.getTown();
            case "village":
                return this.getVillage();
            case "building":
                return this.getBuilding();
            case "area":
                return area;
            case "population":
                return population;
            case "comments":
                return comments;
            case "gcsource":
                return source(source);
        }
        return null;
    }

    public String info(String lineBreak) {
        StringBuilder s = new StringBuilder();

        s.append(message("Name")).append(": ").append(getName()).append(lineBreak);
        if (!getName().equals(getFullName())) {
            s.append(message("Address")).append(": ").append(fullName).append(lineBreak);
        }
        if (longitude >= -180 && longitude <= 180) {
            s.append(message("Longitude")).append(": ").append(longitude).append(lineBreak);
        }
        if (latitude >= -90 && latitude <= 90) {
            s.append(message("Latitude")).append(": ").append(latitude).append(lineBreak);
        }
        if (altitude != CommonValues.InvalidDouble) {
            s.append(message("Altitude")).append(": ").append(altitude).append(lineBreak);
        }
        if (precision != CommonValues.InvalidDouble) {
            s.append(message("Precision")).append(": ").append(precision).append(lineBreak);
        }
        if (coordinateSystem != null) {
            s.append(message("CoordinateSystem")).append(": ").append(coordinateSystem.name()).append(lineBreak);
        }
        if (getLevelCode() != null) {
            s.append(message("Level")).append(": ").append(message(levelCode.getName())).append(lineBreak);
        }
        if (getCountryName() != null && !countryName.isBlank()) {
            s.append(message("Country")).append(": ").append(countryName).append(lineBreak);
        }
        if (getProvinceName() != null && !provinceName.isBlank()) {
            s.append(message("Province")).append(": ").append(provinceName).append(lineBreak);
        }
        if (getCityName() != null && !cityName.isBlank()) {
            s.append(message("City")).append(": ").append(cityName).append(lineBreak);
        }
        if (getCountyName() != null && !countyName.isBlank()) {
            s.append(message("County")).append(": ").append(countyName).append(lineBreak);
        }
        if (getTownName() != null && !townName.isBlank()) {
            s.append(message("Town")).append(": ").append(townName).append(lineBreak);
        }
        if (getVillageName() != null && !villageName.isBlank()) {
            s.append(message("Village")).append(": ").append(villageName).append(lineBreak);
        }
        if (getBuildingName() != null && !buildingName.isBlank()) {
            s.append(message("Building")).append(": ").append(buildingName).append(lineBreak);
        }
        if (getCode1() != null && !getCode1().isBlank()) {
            s.append(message("Code")).append(": ").append(getCode1()).append(lineBreak);
        }
        if (getCode2() != null && !getCode2().isBlank()) {
            s.append(message("Code")).append(": ").append(getCode2()).append(lineBreak);
        }
        if (getCode3() != null && !getCode3().isBlank()) {
            s.append(message("Code")).append(": ").append(getCode3()).append(lineBreak);
        }
        if (getCode4() != null && !getCode4().isBlank()) {
            s.append(message("Code")).append(": ").append(getCode4()).append(lineBreak);
        }
        if (getCode5() != null && !getCode5().isBlank()) {
            s.append(message("Code")).append(": ").append(getCode5()).append(lineBreak);
        }
        if (getAlias1() != null && !getAlias1().isBlank()) {
            s.append(message("Alias")).append(": ").append(getAlias1()).append(lineBreak);
        }
        if (getAlias2() != null && !getAlias2().isBlank()) {
            s.append(message("Alias")).append(": ").append(getAlias2()).append(lineBreak);
        }
        if (getAlias3() != null && !getAlias3().isBlank()) {
            s.append(message("Alias")).append(": ").append(getAlias3()).append(lineBreak);
        }
        if (getAlias4() != null && !getAlias4().isBlank()) {
            s.append(message("Alias")).append(": ").append(getAlias4()).append(lineBreak);
        }
        if (getAlias5() != null && !getAlias5().isBlank()) {
            s.append(message("Alias")).append(": ").append(getAlias5()).append(lineBreak);
        }
        if (area > 0) {
            s.append(message("SquareMeters")).append(": ").append(area).append(lineBreak);
        }
        if (population > 0) {
            s.append(message("Population")).append(": ").append(population).append(lineBreak);
        }
        if (getComments() != null) {
            String c = getComments().trim();
            if (!c.isBlank()) {
                s.append(message("Comments")).append(": ").append(c).append(lineBreak);
            }
        }

        return s.toString();
    }

    public boolean validCoordinate() {
        return GeographyCodeTools.validCoordinate(this);
    }

    public int mapSize() {
        if (levelCode == null) {
            return 3;
        }
        switch (levelCode.getLevel()) {
            case 3:
                return 4;
            case 4:
                return 6;
            case 5:
                return 9;
            case 6:
                return 11;
            case 7:
                return 13;
            case 8:
                return 16;
            case 9:
            case 10:
                return 18;
            default:
                return 3;
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            GeographyCode newCode = (GeographyCode) super.clone();
            if (levelCode != null) {
                newCode.setLevelCode((GeographyCodeLevel) levelCode.clone());
            }
            if (coordinateSystem != null) {
                newCode.setCoordinateSystem((CoordinateSystem) coordinateSystem.clone());
            }
            if (ownerCode != null) {
                newCode.setOwnerCode((GeographyCode) ownerCode.clone());
            }
            if (continentCode != null) {
                newCode.setContinentCode((GeographyCode) continentCode.clone());
            }
            if (countryCode != null) {
                newCode.setCountryCode((GeographyCode) countryCode.clone());
            }
            if (provinceCode != null) {
                newCode.setProvinceCode((GeographyCode) provinceCode.clone());
            }
            if (cityCode != null) {
                newCode.setCityCode((GeographyCode) cityCode.clone());
            }
            if (townCode != null) {
                newCode.setTownCode((GeographyCode) townCode.clone());
            }
            if (villageCode != null) {
                newCode.setVillageCode((GeographyCode) villageCode.clone());
            }
            if (buildingCode != null) {
                newCode.setBuildingCode((GeographyCode) buildingCode.clone());
            }
            return newCode;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public void setSource(short value) {
        this.source = source(value);
    }

    public short getSourceValue() {
        return source(source);
    }

    public boolean isPredefined() {
        return source == AddressSource.PredefinedData;
    }

    /*
        Static externalValues
     */
    public static GeographyCode create() {
        return new GeographyCode();
    }

    public static String name(GeographyCode code) {
        if (code != null) {
            if (AppVariables.isChinese()) {
                if (code.getChineseName() != null) {
                    return code.getChineseName();
                } else {
                    return code.getEnglishName();
                }
            } else {
                if (code.getEnglishName() != null) {
                    return code.getEnglishName();
                } else {
                    return code.getChineseName();
                }
            }
        }
        return null;
    }

    public static GeographyCode none() {
        GeographyCode none = new GeographyCode();
        none.setName(message("None"));
        return none;
    }

    public static int mapSize(GeographyCode code) {
        if (code == null) {
            return 3;
        }
        return code.mapSize();
    }

    public static AddressSource source(short value) {
        switch (value) {
            case 1:
                return AddressSource.InputtedData;
            case 2:
                return AddressSource.PredefinedData;
            case 3:
                return AddressSource.Geonames;
            case 4:
                return AddressSource.ImportedData;
            default:
                return AddressSource.Unknown;
        }
    }

    public static short source(AddressSource source) {
        return sourceValue(source.name());
    }

    public static short sourceValue(String source) {
        return sourceValue(AppVariables.getLanguage(), source);
    }

    public static short sourceValue(String lang, String source) {
        if (message(lang, "InputtedData").equals(source) || "InputtedData".equals(source)) {
            return 1;
        } else if (message(lang, "PredefinedData").equals(source) || "PredefinedData".equals(source)) {
            return 2;
        } else if ("Geonames".equals(source)) {
            return 3;
        } else if (message(lang, "ImportedData").equals(source) || "ImportedData".equals(source)) {
            return 4;
        } else {
            return 0;
        }
    }


    /*
        custmized get/set
     */
    public String getName() {
        name = name(this);
        if (name == null) {
            switch (level) {
                case 1:
                    return message("Earth");
                case 2:
                    return continentName;
                case 3:
                    return countryName;
                case 4:
                    return provinceName;
                case 5:
                    return cityName;
                case 6:
                    return countyName;
                case 7:
                    return townName;
                case 8:
                    return villageName;
                case 9:
                    return buildingName;
                case 10:
                    return poiName;
            }
        }
        return name;
    }

    public String getFullName() {
        getName();
        if (levelCode == null || getLevel() <= 3) {
            fullName = name;
        } else {
            continentName = getContinentName();
            countryName = getCountryName();
            provinceName = getProvinceName();
            cityName = getCityName();
            countyName = getCountyName();
            townName = getTownName();
            villageName = getVillageName();
            buildingName = getBuildingName();
            if (AppVariables.isChinese()) {
                if (countryName != null) {
                    fullName = countryName;
                } else {
                    fullName = "";
                }
                if (provinceName != null && !provinceName.isBlank()) {
                    fullName = fullName.isBlank() ? provinceName : fullName + "-" + provinceName;
                }
                if (cityName != null && !cityName.isBlank()) {
                    fullName = fullName.isBlank() ? cityName : fullName + "-" + cityName;
                }
                if (countyName != null && !countyName.isBlank()) {
                    fullName = fullName.isBlank() ? countyName : fullName + "-" + countyName;
                }
                if (townName != null && !townName.isBlank()) {
                    fullName = fullName.isBlank() ? townName : fullName + "-" + townName;
                }
                if (villageName != null && !villageName.isBlank()) {
                    fullName = fullName.isBlank() ? villageName : fullName + "-" + villageName;
                }
                if (buildingName != null && !buildingName.isBlank()) {
                    fullName = fullName.isBlank() ? buildingName : fullName + "-" + buildingName;
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
                if (buildingName != null && !buildingName.isBlank()) {
                    fullName = fullName.isBlank() ? buildingName : fullName + "," + buildingName;
                }
                if (villageName != null && !villageName.isBlank()) {
                    fullName = fullName.isBlank() ? villageName : fullName + "," + villageName;
                }
                if (townName != null && !townName.isBlank()) {
                    fullName = fullName.isBlank() ? townName : fullName + "," + townName;
                }
                if (countyName != null && !countyName.isBlank()) {
                    fullName = fullName.isBlank() ? countyName : fullName + "," + countyName;
                }
                if (cityName != null && !cityName.isBlank()) {
                    fullName = fullName.isBlank() ? cityName : fullName + "," + cityName;
                }
                if (provinceName != null && !provinceName.isBlank()) {
                    fullName = fullName.isBlank() ? provinceName : fullName + "," + provinceName;
                }
                if (countryName != null && !countryName.isBlank()) {
                    fullName = fullName.isBlank() ? countryName : fullName + "," + countryName;
                }
            }
        }
        return fullName;
    }

    public String getContinentName() {
        if (continentName == null) {
            if (level == 2) {
                continentName = getName();
            } else if (level > 2 && getContinentCode() != null) {
                continentName = name(getContinentCode());
            }
        }
        return continentName;
    }

    public String getCountryName() {
        if (countryName == null) {
            if (level == 3) {
                countryName = getName();
            } else if (level > 3 && getCountryCode() != null) {
                countryName = name(getCountryCode());
            }
        }
        return countryName;
    }

    public String getProvinceName() {
        if (provinceName == null) {
            if (level == 4) {
                provinceName = getName();
            } else if (level > 4 && getProvinceCode() != null) {
                provinceName = name(getProvinceCode());
            }
        }
        return provinceName;
    }

    public String getCityName() {
        if (cityName == null) {
            if (level == 5) {
                cityName = getName();
            } else if (level > 5 && getCityCode() != null) {
                cityName = name(getCityCode());
            }
        }
        return cityName;
    }

    public String getCountyName() {
        if (countyName == null) {
            if (level == 6) {
                countyName = getName();
            } else if (level > 6 && getCountyCode() != null) {
                countyName = name(getCountyCode());
            }
        }
        return countyName;
    }

    public String getTownName() {
        if (townName == null) {
            if (level == 7) {
                townName = getName();
            } else if (level > 7 && getTownCode() != null) {
                townName = name(getTownCode());
            }
        }
        return townName;
    }

    public String getVillageName() {
        if (villageName == null) {
            if (level == 8) {
                villageName = getName();
            } else if (level > 8 && getVillageCode() != null) {
                villageName = name(getVillageCode());
            }
        }
        return villageName;
    }

    public String getBuildingName() {
        if (buildingName == null) {
            if (level == 9) {
                buildingName = getName();
            } else if (level > 9 && getBuildingCode() != null) {
                buildingName = name(getBuildingCode());
            }
        }
        return buildingName;
    }

    public String getPoiName() {
        if (poiName == null) {
            if (level == 10) {
                poiName = getName();
            }
        }
        return poiName;
    }

    public long getContinent() {
        if (continentCode != null) {
            continent = continentCode.getGcid();
        }
        return continent;
    }

    public long getCountry() {
        if (countryCode != null) {
            country = countryCode.getGcid();
        }
        return country;
    }

    public long getProvince() {
        if (provinceCode != null) {
            province = provinceCode.getGcid();
        }
        return province;
    }

    public long getCity() {
        if (cityCode != null) {
            city = cityCode.getGcid();
        }
        return city;
    }

    public long getCounty() {
        if (countyCode != null) {
            county = countyCode.getGcid();
        }
        return county;
    }

    public long getVillage() {
        if (villageCode != null) {
            village = villageCode.getGcid();
        }
        return village;
    }

    public long getTown() {
        if (townCode != null) {
            town = townCode.getGcid();
        }
        return town;
    }

    public long getBuilding() {
        if (buildingCode != null) {
            building = buildingCode.getGcid();
        }
        return building;
    }

    public GeographyCode getContinentCode() {
//        if (level > 2 && continentCode == null) {
//            TableGeographyCode.decodeAncestors(this);
//        }
        return continentCode;
    }

    public GeographyCode getCountryCode() {
//        if (level > 3 && countryCode == null) {
//            TableGeographyCode.decodeAncestors(this);
//        }
        return countryCode;
    }

    public GeographyCode getProvinceCode() {
//        if (level > 4 && provinceCode == null) {
//            TableGeographyCode.decodeAncestors(this);
//        }
        return provinceCode;
    }

    public GeographyCode getCityCode() {
//        if (level > 5 && cityCode == null) {
//            TableGeographyCode.decodeAncestors(this);
//        }
        return cityCode;
    }

    public GeographyCode getCountyCode() {
//        if (level > 7 && countyCode == null) {
//            TableGeographyCode.decodeAncestors(this);
//        }
        return countyCode;
    }

    public GeographyCode getTownCode() {
//        if (level > 8 && townCode == null) {
//            TableGeographyCode.decodeAncestors(this);
//        }
        return townCode;
    }

    public GeographyCode getVillageCode() {
//        if (level > 9 && villageCode == null) {
//            TableGeographyCode.decodeAncestors(this);
//        }
        return villageCode;
    }

    public GeographyCode getBuildingCode() {
//        if (level > 9 && buildingCode == null) {
//            TableGeographyCode.decodeAncestors(this);
//        }
        return buildingCode;
    }

    public GeographyCodeLevel getLevelCode() {
        if (levelCode == null && level > 0) {
            levelCode = new GeographyCodeLevel(level);
        }
        return levelCode;
    }

    public int getLevel() {
        if (levelCode != null) {
            level = levelCode.getLevel();
        }
        if (level > 10 || level < 1) {
            level = 10;
        }
        return level;
    }

    public String getLevelName() {
        if (getLevelCode() != null) {
            levelName = levelCode.getName();
        }
        return levelName;
    }

    public CoordinateSystem getCoordinateSystem() {
        if (coordinateSystem == null) {
            coordinateSystem = CoordinateSystem.defaultCode();
        }
        return coordinateSystem;
    }

    public String getSourceName() {
        sourceName = message(source.name());
        return sourceName;
    }


    /*
        get/set
     */
    public long getGcid() {
        return gcid;
    }

    public void setGcid(long gcid) {
        this.gcid = gcid;
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

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
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

    public void setContinent(long continent) {
        this.continent = continent;
    }

    public void setCountry(long country) {
        this.country = country;
    }

    public void setProvince(long province) {
        this.province = province;
    }

    public void setCity(long city) {
        this.city = city;
    }

    public void setCounty(long county) {
        this.county = county;
    }

    public void setVillage(long village) {
        this.village = village;
    }

    public void setTown(long town) {
        this.town = town;
    }

    public void setBuilding(long building) {
        this.building = building;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
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

    public void setContinentCode(GeographyCode continentCode) {
        this.continentCode = continentCode;
    }

    public void setCountryCode(GeographyCode countryCode) {
        this.countryCode = countryCode;
    }

    public void setProvinceCode(GeographyCode provinceCode) {
        this.provinceCode = provinceCode;
    }

    public void setCityCode(GeographyCode cityCode) {
        this.cityCode = cityCode;
    }

    public void setCountyCode(GeographyCode countyCode) {
        this.countyCode = countyCode;
    }

    public void setTownCode(GeographyCode townCode) {
        this.townCode = townCode;
    }

    public void setVillageCode(GeographyCode villageCode) {
        this.villageCode = villageCode;
    }

    public void setBuildingCode(GeographyCode buildingCode) {
        this.buildingCode = buildingCode;
    }

    public void setPoiName(String poiName) {
        this.poiName = poiName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setContinentName(String continentName) {
        this.continentName = continentName;
    }

    public AddressSource getSource() {
        return source;
    }

    public void setSource(AddressSource source) {
        this.source = source;
    }

    public void setLevelCode(GeographyCodeLevel levelCode) {
        this.levelCode = levelCode;
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

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getOwner() {
        return owner;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public GeographyCode getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(GeographyCode ownerCode) {
        this.ownerCode = ownerCode;
    }

    public void setCoordinateSystem(CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

}
