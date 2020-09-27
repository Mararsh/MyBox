package mara.mybox.data;

import java.util.Collections;
import java.util.List;
import mara.mybox.db.TableBase;
import mara.mybox.db.TableEpidemicReport;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-2-2
 * @License Apache License Version 2.0
 */
public class EpidemicReport extends TableData {

    protected long epid, locationid = -1;
    protected String dataSet, locationFullName;
    protected GeographyCode location;
    protected double healedConfirmedPermillage, deadConfirmedPermillage,
            confirmedPopulationPermillage, deadPopulationPermillage, healedPopulationPermillage,
            confirmedAreaPermillage, deadAreaPermillage, healedAreaPermillage;
    protected long confirmed, healed, dead,
            increasedConfirmed, increasedHealed, increasedDead;
    protected long time = -1;
    protected int source;
    protected String sourceName;
    protected SourceType sourceType;
    public static String COVID19JHU = "COVID-19_JHU";
    public static String COVID19TIME = " 23:59:00";

    public enum ValueName {
        Confirmed, Healed, Dead,
        IncreasedConfirmed, IncreasedHealed, IncreasedDead
    }

    // 1:predefined 2:inputted 3:filled 4:statistic others:unknown
    public enum SourceType {
        PredefinedData, InputtedData, FilledData, StatisticData, Unknown
    }

    private void init() {
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

    public EpidemicReport() {
        init();
    }

    @Override
    public TableBase getTable() {
        if (table == null) {
            table = new TableEpidemicReport();
        }
        return table;
    }

    @Override
    public boolean valid() {
        return getLocationid() > 0
                && (confirmed > 0 || healed > 0 || dead > 0);
    }

    @Override
    public boolean setValue(String column, Object value) {
        if (column == null || value == null) {
            return false;
        }
        try {
            switch (column) {

            }
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public Object getValue(String column) {
        if (column == null) {
            return null;
        }
        try {
            switch (column) {

            }
        } catch (Exception e) {
        }
        return null;
    }

    // Only copy base attributes.
    public EpidemicReport copy() {
        try {
            EpidemicReport cloned = EpidemicReport.create()
                    .setDataSet(dataSet).setLocation(getLocation()).setLocationid(getLocationid())
                    .setConfirmed(confirmed).setHealed(healed).setDead(dead)
                    .setTime(time).setSource(source);
            return cloned;
        } catch (Exception e) {
            return null;
        }
    }

    public Number getNumber(String name) {
        try {
            if (name == null || name.isBlank()) {
                return null;
            }
            if (message("Confirmed").equals(name) || "confirmed".equals(name)) {
                return getConfirmed();

            } else if (message("Healed").equals(name) || "healed".equals(name)) {
                return getHealed();

            } else if (message("Dead").equals(name) || "dead".equals(name)) {
                return getDead();

            } else if (message("IncreasedConfirmed").equals(name) || "increased_confirmed".equals(name)) {
                return getIncreasedConfirmed();

            } else if (message("IncreasedHealed").equals(name) || "increased_healed".equals(name)) {
                return getIncreasedHealed();

            } else if (message("IncreasedDead").equals(name) || "increased_dead".equals(name)) {
                return getIncreasedDead();

            } else if (message("HealedConfirmedPermillage").equals(name) || "healed_confirmed_permillage".equals(name)) {
                return getHealedConfirmedPermillage();

            } else if (message("DeadConfirmedPermillage").equals(name) || "dead_confirmed_permillage".equals(name)) {
                return getDeadConfirmedPermillage();

            } else if (message("ConfirmedPopulationPermillage").equals(name) || "confirmed_population_permillage".equals(name)) {
                return getConfirmedPopulationPermillage();

            } else if (message("HealedPopulationPermillage").equals(name) || "healed_population_permillage".equals(name)) {
                return getHealedPopulationPermillage();

            } else if (message("DeadPopulationPermillage").equals(name) || "dead_population_permillage".equals(name)) {
                return getDeadPopulationPermillage();

            } else if (message("ConfirmedAreaPermillage").equals(name) || "confirmed_area_permillage".equals(name)) {
                return getConfirmedAreaPermillage();

            } else if (message("HealedAreaPermillage").equals(name) || "healed_area_permillage".equals(name)) {
                return getHealedAreaPermillage();

            } else if (message("DeadAreaPermillage").equals(name) || "dead_area_permillage".equals(name)) {
                return getDeadAreaPermillage();

            } else {
                return null;
            }

        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public String info(String lineBreak) {
        StringBuilder s = new StringBuilder();
        s.append(message("ID")).append(": ").append(epid).append(lineBreak);
        if (getDataSet() != null) {
            s.append(message("Dataset")).append(": ").append(getDataSet()).append(lineBreak);
        }
        if (getTime() > 0) {
            s.append(message("Time")).append(": ").append(DateTools.datetimeToString(getTime())).append(lineBreak);
        }
        s.append(message("Confirmed")).append(": ").append(confirmed).append(lineBreak);
        s.append(message("Healed")).append(": ").append(healed).append(lineBreak);
        s.append(message("Dead")).append(": ").append(dead).append(lineBreak);
        s.append(message("IncreasedConfirmed")).append(": ").append(increasedConfirmed).append(lineBreak);
        s.append(message("IncreasedHealed")).append(": ").append(increasedHealed).append(lineBreak);
        s.append(message("IncreasedDead")).append(": ").append(increasedDead).append(lineBreak);
        s.append(message("HealedConfirmedPermillage")).append(": ").append(healedConfirmedPermillage).append(lineBreak);
        s.append(message("DeadConfirmedPermillage")).append(": ").append(deadConfirmedPermillage).append(lineBreak);
        s.append(message("ConfirmedPopulationPermillage")).append(": ").append(confirmedPopulationPermillage).append(lineBreak);
        s.append(message("HealedPopulationPermillage")).append(": ").append(healedPopulationPermillage).append(lineBreak);
        s.append(message("DeadPopulationPermillage")).append(": ").append(deadPopulationPermillage).append(lineBreak);
        s.append(message("ConfirmedAreaPermillage")).append(": ").append(confirmedAreaPermillage).append(lineBreak);
        s.append(message("HealedAreaPermillage")).append(": ").append(healedAreaPermillage).append(lineBreak);
        s.append(message("DeadAreaPermillage")).append(": ").append(deadAreaPermillage).append(lineBreak);
        if (location != null) {
            s.append(location.info(lineBreak));
        }
        return s.toString();
    }

    /*
        Static methods
     */
    public static EpidemicReport create() {
        return new EpidemicReport();
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

    public static SourceType sourceType(int value) {
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

    public static String sourceName(int value) {
        return sourceType(value).name();
    }

    public static int source(SourceType source) {
        return sourceValue(source.name());
    }

    public static int sourceValue(String source) {
        return sourceValue(AppVariables.getLanguage(), source);
    }

    public static int sourceValue(String lang, String source) {
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

    public long getEpid() {
        return epid;
    }

    /*
    custmized get/set
     */
    public void setEpid(long epid) {
        this.epid = epid;
    }

    public long getLocationid() {
        if (locationid <= 0 && location != null) {
            locationid = location.getGcid();
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

    public int getSource() {
        return source;
    }

    public EpidemicReport setSource(int source) {
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
