package mara.mybox.db.data;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-2-2
 * @License Apache License Version 2.0
 */
public class EpidemicReport extends BaseData {

    protected long epid, locationid = -1;
    protected String dataSet, locationFullName;
    protected GeographyCode location;
    protected double healedConfirmedPermillage, deadConfirmedPermillage,
            confirmedPopulationPermillage, deadPopulationPermillage, healedPopulationPermillage,
            confirmedAreaPermillage, deadAreaPermillage, healedAreaPermillage;
    protected long confirmed, healed, dead,
            increasedConfirmed, increasedHealed, increasedDead;
    protected long time = -1;
    protected short source;
    protected String sourceName;
    protected SourceType sourceType;
    public static String COVID19JHU = "COVID-19_JHU";
    public static String COVID19TIME = " 23:59:00";

    public static enum ValueName {
        Confirmed, Healed, Dead,
        IncreasedConfirmed, IncreasedHealed, IncreasedDead
    }

    // 1:predefined 2:inputted 3:filled 4:statistic others:unknown
    public static enum SourceType {
        PredefinedData, InputtedData, FilledData, StatisticData, Unknown
    }

    public EpidemicReport() {
        epid = -1;
        locationid = -1;
        dataSet = null;
        location = null;
        healedConfirmedPermillage = deadConfirmedPermillage = 0;
        confirmed = healed = dead = increasedConfirmed
                = increasedHealed = increasedDead = 0;
        healedConfirmedPermillage = deadConfirmedPermillage
                = confirmedPopulationPermillage = deadPopulationPermillage = healedPopulationPermillage
                = confirmedAreaPermillage = deadAreaPermillage = healedAreaPermillage = 0;
        time = -1;
        source = 2;
    }

    /*
        Static methods
     */
    public static EpidemicReport create() {
        return new EpidemicReport();
    }

    public static boolean setValue(EpidemicReport data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "epid":
                    data.setEpid(value == null ? -1 : (long) value);
                    return true;
                case "data_set":
                    data.setDataSet(value == null ? null : (String) value);
                    return true;
                case "time":
                    data.setTime(value == null ? -1 : ((Date) value).getTime());
                    return true;
                case "confirmed":
                    data.setConfirmed(value == null ? -1 : (long) value);
                    return true;
                case "healed":
                    data.setHealed(value == null ? -1 : (long) value);
                    return true;
                case "dead":
                    data.setDead(value == null ? -1 : (long) value);
                    return true;
                case "increased_confirmed":
                    data.setIncreasedConfirmed(value == null ? -1 : (long) value);
                    return true;
                case "increased_healed":
                    data.setIncreasedHealed(value == null ? -1 : (long) value);
                    return true;
                case "increased_dead":
                    data.setIncreasedDead(value == null ? -1 : (long) value);
                    return true;
                case "source":
                    data.setSource(value == null ? -1 : (short) value);
                    return true;
                case "locationid":
                    data.setLocationid(value == null ? -1 : (long) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(EpidemicReport data, String column) {
        if (data == null || column == null) {
            return null;
        }
        try {
            switch (column) {
                case "epid":
                    return data.getEpid();
                case "data_set":
                    return data.getDataSet();
                case "time":
                    return new Date(data.getTime());
                case "confirmed":
                    return data.getConfirmed();
                case "healed":
                    return data.getHealed();
                case "dead":
                    return data.getDead();
                case "increased_confirmed":
                    return data.getIncreasedConfirmed();
                case "increased_healed":
                    return data.getIncreasedHealed();
                case "increased_dead":
                    return data.getIncreasedDead();
                case "source":
                    return data.getSource();
                case "locationid":
                    return data.getLocationid();
                case "healed_confirmed_permillage":
                    return data.getHealedConfirmedPermillage();
                case "dead_confirmed_permillage":
                    return data.getDeadConfirmedPermillage();
                case "confirmed_population_permillage":
                    return data.getConfirmedPopulationPermillage();
                case "healed_population_permillage":
                    return data.getHealedPopulationPermillage();
                case "dead_population_permillage":
                    return data.getDeadPopulationPermillage();
                case "confirmed_area_permillage":
                    return data.getConfirmedAreaPermillage();
                case "healed_area_permillage":
                    return data.getHealedAreaPermillage();
                case "dead_area_permillage":
                    return data.getDeadAreaPermillage();
            }
            if (data.getLocation() != null) {
                return GeographyCode.getValue(data.getLocation(), column);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return null;
    }

    public static String displayColumn(EpidemicReport data, ColumnDefinition column, Object value) {
        if (data == null || column == null || value == null) {
            return null;
        }
        switch (column.getName()) {
            case "source":
                return data.getSourceName();
        }
        if ((column.getType() == ColumnType.Long && (long) value <= 0)
                || (column.getType() == ColumnType.Double && (double) value <= 0)) {
            return null;
        }
        if (data.getLocation() != null) {
            String display = GeographyCode.displayColumn(data.getLocation(), column, value);
            if (display != null) {
                return display;
            }
        }
        return BaseDataTools.displayColumnBase(data, column, value);
    }

    public static boolean valid(EpidemicReport data) {
        return data != null
                && data.getLocationid() >= 0
                && (data.getConfirmed() > 0 || data.getHealed() > 0 || data.getDead() > 0);
    }

    public static void sortAsTimeAscent(List<EpidemicReport> reports) {
        if (reports == null || reports.isEmpty()) {
            return;
        }
        Collections.sort(reports, (EpidemicReport r1, EpidemicReport r2) -> {
            long diff = r1.getTime() - r2.getTime();
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else {
                return 0;
            }
        });
    }

    public static SourceType sourceType(short value) {
        switch (value) {
            case 1:
                return SourceType.PredefinedData;
            case 2:
                return SourceType.InputtedData;
            case 3:
                return SourceType.FilledData;
            case 4:
                return SourceType.StatisticData;
            default:
                return SourceType.Unknown;
        }
    }

    public static String sourceName(short value) {
        return sourceType(value).name();
    }

    public static short source(SourceType source) {
        return sourceValue(source.name());
    }

    public static short sourceValue(String source) {
        return sourceValue(AppVariables.getLanguage(), source);
    }

    public static short sourceValue(String lang, String source) {
        if (message(lang, "PredefinedData").equals(source) || "PredefinedData".equals(source)) {
            return 1;
        } else if (message(lang, "InputtedData").equals(source) || "InputtedData".equals(source)) {
            return 2;
        } else if (message(lang, "FilledData").equals(source) || "FilledData".equals(source)) {
            return 3;
        } else if (message(lang, "StatisticData").equals(source) || "StatisticData".equals(source)) {
            return 4;
        } else {
            return 0;
        }
    }

    public static int PredefinedData() {
        return source(SourceType.PredefinedData);
    }

    public static int InputtedData() {
        return source(SourceType.InputtedData);
    }

    public static int FilledData() {
        return source(SourceType.FilledData);
    }

    public static int StatisticData() {
        return source(SourceType.StatisticData);
    }

    /*
    custmized get/set
     */
    public void setEpid(long epid) {
        this.epid = epid;
    }

    public long getLocationid() {
        if (locationid <= 0 && location != null) {
            locationid = location.getId();
        }
        return locationid;
    }

    public GeographyCode getLocation() {
//        if (location == null && locationid > 0) {
//            location = TableGeographyCode.readCode(locationid, false);
//        }
        return location;
    }

    public String getLocationFullName() {
        if (locationFullName == null && getLocation() != null) {
            locationFullName = location.getFullName();
        }
        return locationFullName;
    }

    public String getSourceName() {
        sourceName = sourceName(source);
        return message(sourceName);
    }

    public SourceType getSourceType() {
        sourceType = sourceType(source);
        return sourceType;
    }

    /*
        get/set
     */
    public long getEpid() {
        return epid;
    }

    public EpidemicReport setLocationid(long locationid) {
        this.locationid = locationid;
        return this;
    }

    public String getDataSet() {
        return dataSet;
    }

    public EpidemicReport setDataSet(String dataSet) {
        this.dataSet = dataSet;
        return this;
    }

    public EpidemicReport setLocation(GeographyCode location) {
        this.location = location;
        return this;
    }

    public long getConfirmed() {
        return confirmed;
    }

    public EpidemicReport setConfirmed(long confirmed) {
        this.confirmed = confirmed;
        return this;
    }

    public long getHealed() {
        return healed;
    }

    public EpidemicReport setHealed(long healed) {
        this.healed = healed;
        return this;
    }

    public long getDead() {
        return dead;
    }

    public EpidemicReport setDead(long dead) {
        this.dead = dead;
        return this;
    }

    public long getIncreasedConfirmed() {
        return increasedConfirmed;
    }

    public EpidemicReport setIncreasedConfirmed(long increasedConfirmed) {
        this.increasedConfirmed = increasedConfirmed;
        return this;
    }

    public long getIncreasedHealed() {
        return increasedHealed;
    }

    public EpidemicReport setIncreasedHealed(long increasedHealed) {
        this.increasedHealed = increasedHealed;
        return this;
    }

    public long getIncreasedDead() {
        return increasedDead;
    }

    public EpidemicReport setIncreasedDead(long increasedDead) {
        this.increasedDead = increasedDead;
        return this;
    }

    public long getTime() {
        return time;
    }

    public EpidemicReport setTime(long time) {
        this.time = time;
        return this;
    }

    public void setLocationFullName(String locationFullName) {
        this.locationFullName = locationFullName;
    }

    public void setHealedConfirmedPermillage(double healedConfirmedPermillage) {
        this.healedConfirmedPermillage = healedConfirmedPermillage;
    }

    public void setDeadConfirmedPermillage(double deadConfirmedPermillage) {
        this.deadConfirmedPermillage = deadConfirmedPermillage;
    }

    public short getSource() {
        return source;
    }

    public EpidemicReport setSource(short source) {
        this.source = source;
        return this;
    }

    public EpidemicReport setSourceName(String sourceName) {
        this.sourceName = sourceName;
        return this;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public void setConfirmedPopulationPermillage(double confirmedPopulationPermillage) {
        this.confirmedPopulationPermillage = confirmedPopulationPermillage;
    }

    public void setDeadPopulationPermillage(double deadPopulationPermillage) {
        this.deadPopulationPermillage = deadPopulationPermillage;
    }

    public void setHealedPopulationPermillage(double healedPopulationPermillage) {
        this.healedPopulationPermillage = healedPopulationPermillage;
    }

    public void setConfirmedAreaPermillage(double confirmedAreaPermillage) {
        this.confirmedAreaPermillage = confirmedAreaPermillage;
    }

    public void setDeadAreaPermillage(double deadAreaPermillage) {
        this.deadAreaPermillage = deadAreaPermillage;
    }

    public void setHealedAreaPermillage(double healedAreaPermillage) {
        this.healedAreaPermillage = healedAreaPermillage;
    }

    public double getHealedConfirmedPermillage() {
        return healedConfirmedPermillage;
    }

    public double getDeadConfirmedPermillage() {
        return deadConfirmedPermillage;
    }

    public double getConfirmedPopulationPermillage() {
        return confirmedPopulationPermillage;
    }

    public double getDeadPopulationPermillage() {
        return deadPopulationPermillage;
    }

    public double getHealedPopulationPermillage() {
        return healedPopulationPermillage;
    }

    public double getConfirmedAreaPermillage() {
        return confirmedAreaPermillage;
    }

    public double getDeadAreaPermillage() {
        return deadAreaPermillage;
    }

    public double getHealedAreaPermillage() {
        return healedAreaPermillage;
    }

}
