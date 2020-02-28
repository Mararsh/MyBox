package mara.mybox.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.ExcelTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.tools.FileTools.charset;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-2-2
 * @License Apache License Version 2.0
 */
public class EpidemicReport {

    protected long dataid = -1;
    protected String dataSet, dataLabel, comments, country, province, city,
            district, township, neighborhood, level;
    protected double longitude, latitude, healedRatio, deadRatio;
    protected int confirmed, suspected, healed, dead,
            increasedConfirmed, increasedSuspected, increasedHealed, increasedDead;
    protected long time = -1;

    public EpidemicReport() {
        dataid = -1;
        dataSet = dataLabel = comments = country = province = city
                = district = township = neighborhood = level = null;
        longitude = latitude = -200;
        healedRatio = deadRatio = 0;
        confirmed = suspected = healed = dead = increasedConfirmed
                = increasedSuspected = increasedHealed = increasedDead = 0;
        time = -1;
    }

    public static EpidemicReport create() {
        return new EpidemicReport();
    }

    public static boolean validCoordinate(EpidemicReport report) {
        return report.getLongitude() >= -180 && report.getLongitude() <= 180
                && report.getLatitude() >= -90 && report.getLatitude() <= 90;
    }

    public static List<EpidemicReport> readTxt(File file) {
        List<EpidemicReport> reports = new ArrayList<>();
        if (file == null || !file.exists()) {
            return reports;
        }
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("//")) {
                    continue;
                }
                String[] values = line.split(",");
                if (values.length != 21 && values.length != 22) {
                    continue;
                }
                try {
                    int offset = values.length == 22 ? 1 : 0;
                    EpidemicReport report = new EpidemicReport();
                    report.setDataSet(readTxtString(values[offset + 0]));
                    report.setDataLabel(readTxtString(values[offset + 1]));
                    report.setLongitude(Double.valueOf(values[offset + 2]));
                    report.setLatitude(Double.valueOf(values[offset + 3]));
                    report.setLevel(readTxtString(values[offset + 4]));
                    report.setCountry(readTxtString(values[offset + 5]));
                    report.setProvince(readTxtString(values[offset + 6]));
                    report.setCity(readTxtString(values[offset + 7]));
                    report.setDistrict(readTxtString(values[offset + 8]));
                    report.setTownship(readTxtString(values[offset + 9]));
                    report.setNeighborhood(readTxtString(values[offset + 10]));
                    report.setConfirmed(Integer.valueOf(values[offset + 11]));
                    report.setSuspected(Integer.valueOf(values[offset + 12]));
                    report.setHealed(Integer.valueOf(values[offset + 13]));
                    report.setDead(Integer.valueOf(values[offset + 14]));
                    report.setIncreasedConfirmed(Integer.valueOf(values[offset + 15]));
                    report.setIncreasedSuspected(Integer.valueOf(values[offset + 16]));
                    report.setIncreasedHealed(Integer.valueOf(values[offset + 17]));
                    report.setIncreasedDead(Integer.valueOf(values[offset + 18]));
                    report.setComments(readTxtString(values[offset + 19]));
                    report.setTime(DateTools.stringToDatetime(values[offset + 20].substring(1, 20)).getTime());
                    reports.add(report);

                } catch (Exception e) {
                    logger.debug(e.toString());
                    break;
                }
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return reports;
    }

    private static String readTxtString(String value) {
        if (value == null || value.isBlank() || !value.startsWith("\"")) {
            return value;
        }
        return value.substring(1, value.length() - 1);
    }

    public static void writeTxt(File file, List<EpidemicReport> reports) {
        try {
            if (file == null || reports == null || reports.isEmpty()) {
                return;
            }
            StringBuilder s = new StringBuilder();
            s.append("//").append(message("DataSet")).append(",")
                    .append(message("Label")).append(",")
                    .append(message("Longitude")).append(",").append(message("Latitude")).append(",").
                    append(message("Level")).append(",").append(message("Country")).append(",").
                    append(message("Province")).append(",").append(message("City")).append(",")
                    .append(message("District")).append(",").append(message("Township")).append(",")
                    .append(message("Neighborhood")).append(",").
                    append(message("Confirmed")).append(",").append(message("Suspected")).append(",")
                    .append(message("Healed")).append(",").append(message("Dead")).append(",").
                    append(message("IncreasedConfirmed")).append(",").append(message("IncreasedSuspected")).append(",")
                    .append(message("IncreasedHealed")).append(",").append(message("IncreasedDead")).append(",").
                    append(message("Comments")).append(",").append(message("Time")).append("\n");

            for (EpidemicReport report : reports) {
                s.append("\"").append(report.getDataSet()).append("\",")
                        .append(report.getDataLabel() != null ? "\"" + report.getDataLabel() + "\"" : "").append(",")
                        .append(report.getLongitude()).append(",").append(report.getLatitude()).append(",")
                        .append(report.getLevel() != null ? "\"" + report.getLevel() + "\"" : "").append(",")
                        .append(report.getCountry() != null ? "\"" + report.getCountry() + "\"" : "").append(",")
                        .append(report.getProvince() != null ? "\"" + report.getProvince() + "\"" : "").append(",")
                        .append(report.getCity() != null ? "\"" + report.getCity() + "\"" : "").append(",")
                        .append(report.getDistrict() != null ? "\"" + report.getDistrict() + "\"" : "").append(",")
                        .append(report.getTownship() != null ? "\"" + report.getTownship() + "\"" : "").append(",")
                        .append(report.getNeighborhood() != null ? "\"" + report.getNeighborhood() + "\"" : "").append(",")
                        .append(report.getConfirmed()).append(",")
                        .append(report.getSuspected()).append(",")
                        .append(report.getHealed()).append(",")
                        .append(report.getDead()).append(",")
                        .append(report.getIncreasedConfirmed()).append(",")
                        .append(report.getIncreasedSuspected()).append(",")
                        .append(report.getIncreasedHealed()).append(",")
                        .append(report.getIncreasedDead()).append(",")
                        .append(report.getComments() != null ? "\"" + report.getComments() + "\"" : "").append(",\"")
                        .append(DateTools.datetimeToString(report.getTime())).append("\"\n");
            }
            FileTools.writeFile(file, s.toString());

        } catch (Exception e) {

        }
    }

    public static void writeExcel(File file, List<EpidemicReport> reports) {
        try {
            if (file == null || reports == null || reports.isEmpty()) {
                return;
            }
            List<String> columns = new ArrayList<>();
            columns.addAll(Arrays.asList(
                    //                    message("Dataid"),
                    message("DataSet"), message("Time"),
                    message("Level"), message("Country"), message("Province"), message("City"),
                    message("Confirmed"),
                    message("IncreasedConfirmed"),
                    message("Healed"),
                    message("IncreasedHealed"),
                    message("HealedRatio"),
                    message("Dead"),
                    message("IncreasedDead"),
                    message("DeadRatio"),
                    message("Longitude"), message("Latitude")
            ));
            List<List<String>> rows = new ArrayList<>();
            for (EpidemicReport report : reports) {
                List<String> row = new ArrayList<>();
                boolean valid = validCoordinate(report);
                row.addAll(Arrays.asList(
                        //                        report.getDataid() + "",
                        report.getDataSet(), DateTools.datetimeToString(report.getTime()),
                        (report.getLevel() == null ? "" : report.getLevel()),
                        (report.getCountry() == null ? "" : report.getCountry()),
                        (report.getProvince() == null ? "" : report.getProvince()),
                        (report.getCity() == null ? "" : report.getCity()),
                        (report.getConfirmed() + ""),
                        (report.getIncreasedConfirmed() + ""),
                        (report.getHealed() + ""),
                        (report.getIncreasedHealed() + ""),
                        (report.getHealedRatio() + ""),
                        (report.getDead() + ""),
                        (report.getIncreasedDead() + ""),
                        (report.getDeadRatio() + ""),
                        (valid ? report.getLongitude() + "" : ""),
                        (valid ? report.getLatitude() + "" : "")
                )
                );
                rows.add(row);
            }
            ExcelTools.createXLSX(file, columns, rows);

        } catch (Exception e) {

        }
    }

    public static void writeJson(File file, List<EpidemicReport> reports) {
        try {
            if (file == null || reports == null || reports.isEmpty()) {
                return;
            }
            StringBuilder s = new StringBuilder();
            String indent = "    ";
            s.append("{\"EpidemicReports\": [\n");
            for (EpidemicReport report : reports) {
                s.append(indent).append("{\"dataset\":\"").append(report.getDataSet()).append("\",")
                        .append("\"time\":\"").append(DateTools.datetimeToString(report.getTime())).append("\",")
                        .append("\"level\":\"").append(report.getLevel()).append("\"");
                if (report.getCountry() != null) {
                    s.append(",\"country\":\"").append(report.getCountry()).append("\"");
                }
                if (report.getProvince() != null) {
                    s.append(",\"province\":\"").append(report.getProvince()).append("\"");
                }
                if (report.getCity() != null) {
                    s.append(",\"city\":\"").append(report.getCity()).append("\"");
                }
                s.append(",\"confirmed\":").append(report.getConfirmed())
                        .append(",\"increasedConfirmed\":").append(report.getIncreasedConfirmed())
                        .append(",\"healed\":").append(report.getHealed())
                        .append(",\"increasedHealed\":").append(report.getIncreasedHealed())
                        .append(",\"healedRatio\":").append(report.getHealedRatio())
                        .append(",\"increasedDead\":").append(report.getIncreasedDead())
                        .append(",\"deadRatio\":").append(report.getDeadRatio());
                if (report.getLongitude() >= -180) {
                    s.append(",\"longtitude\":").append(report.getLongitude())
                            .append(",\"latitude\":").append(report.getLatitude());
                }
                s.append(indent).append("}\n");
            }
            s.append("]}");
            FileTools.writeFile(file, s.toString());
        } catch (Exception e) {
        }
    }

    public static void writeXml(File file, List<EpidemicReport> reports) {
        try {
            if (file == null || reports == null || reports.isEmpty()) {
                return;
            }
            StringBuilder s = new StringBuilder();
            String indent = "    ";
            s.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n").
                    append("<EpidemicReports>\n");
            for (EpidemicReport report : reports) {
                s.append(indent).append("<EpidemicReport ")
                        .append(" dataset=\"").append(report.getDataSet()).append("\" ")
                        .append(" time=\"").append(DateTools.datetimeToString(report.getTime())).append("\" ")
                        .append(" level=\"").append(report.getLevel()).append("\" ");
                if (report.getCountry() != null) {
                    s.append(" country=\"").append(report.getCountry()).append("\" ");
                }
                if (report.getProvince() != null) {
                    s.append(" province=\"").append(report.getProvince()).append("\" ");
                }
                if (report.getCity() != null) {
                    s.append(" city=\"").append(report.getCity()).append("\" ");
                }
                s.append(" confirmed=").append(report.getConfirmed())
                        .append(" increasedConfirmed=").append(report.getIncreasedConfirmed())
                        .append(" healed=").append(report.getHealed())
                        .append(" increasedHealed=").append(report.getIncreasedHealed())
                        .append(" healedRatio=").append(report.getHealedRatio())
                        .append(" increasedDead=").append(report.getIncreasedDead())
                        .append(" deadRatio=").append(report.getDeadRatio());
                if (report.getLongitude() >= -180) {
                    s.append(" longtitude=").append(report.getLongitude())
                            .append(" latitude=").append(report.getLatitude());
                }
                s.append(indent).append(" />\n");
            }
            s.append("</EpidemicReports>\n");
            FileTools.writeFile(file, s.toString());
        } catch (Exception e) {
        }
    }

    public static void writeHtml(File file, List<EpidemicReport> reports) {
        try {
            if (file == null || reports == null || reports.isEmpty()) {
                return;
            }
            List<String> names = new ArrayList<>();
            String title = message("EpidemicReport") + " " + message("NewCoronavirusPneumonia");
            names.addAll(Arrays.asList(
                    message("Dataset"), message("Time"), message("Level"),
                    message("Country"), message("Province"), message("City"),
                    message("Confirmed"), message("IncreasedConfirmed"),
                    message("Healed"), message("IncreasedHealed"), message("HealedRatio"),
                    message("Dead"), message("IncreasedDead"), message("DeadRatio"),
                    message("Longitude"), message("Latitude")
            ));
            StringTable table = new StringTable(names, title);
            for (EpidemicReport report : reports) {
                List<String> row = new ArrayList<>();
                row.addAll(Arrays.asList(
                        report.getDataSet(), report.getLevel(), DateTools.datetimeToString(report.getTime()),
                        report.getCountry() != null ? report.getCountry() : "",
                        report.getProvince() != null ? report.getProvince() : "",
                        report.getCity() != null ? report.getCity() : "",
                        report.getConfirmed() + "", report.getIncreasedConfirmed() + "",
                        report.getHealed() + "", report.getIncreasedHealed() + "", report.getHealedRatio() + "",
                        report.getDead() + "", report.getIncreasedDead() + "", report.getDeadRatio() + "",
                        report.getLongitude() >= -180 ? report.getLongitude() + "" : "",
                        report.getLatitude() >= -90 ? report.getLatitude() + "" : ""
                ));
                table.add(row);
            }
            FileTools.writeFile(file, StringTable.tableHtml(table));
        } catch (Exception e) {
        }
    }

    // Only copy base attributes.
    public EpidemicReport copy() {
        try {
            EpidemicReport cloned = EpidemicReport.create()
                    .setDataSet(dataSet).setLevel(level).
                    setCountry(country).setProvince(province).setCity(city)
                    .setDistrict(district).setTownship(township).setNeighborhood(neighborhood)
                    .setLongitude(longitude).setLatitude(latitude)
                    .setConfirmed(confirmed).setSuspected(suspected).setHealed(healed).setDead(dead)
                    .setTime(time).setDataLabel(dataLabel).setComments(comments);
            return cloned;
        } catch (Exception e) {
            return null;
        }
    }

    /*
        get/set
     */
    public long getDataid() {
        return dataid;
    }

    public EpidemicReport setDataid(long dataid) {
        this.dataid = dataid;
        return this;
    }

    public String getDataSet() {
        return dataSet;
    }

    public EpidemicReport setDataSet(String dataSet) {
        this.dataSet = dataSet;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public EpidemicReport setCountry(String country) {
        this.country = country;
        return this;
    }

    public String getProvince() {
        return province;
    }

    public EpidemicReport setProvince(String province) {
        this.province = province;
        return this;
    }

    public String getCity() {
        return city;
    }

    public EpidemicReport setCity(String city) {
        this.city = city;
        return this;
    }

    public String getDistrict() {
        return district;
    }

    public EpidemicReport setDistrict(String district) {
        this.district = district;
        return this;
    }

    public String getTownship() {
        return township;
    }

    public EpidemicReport setTownship(String township) {
        this.township = township;
        return this;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public EpidemicReport setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
        return this;
    }

    public String getLevel() {
        return level;
    }

    public EpidemicReport setLevel(String level) {
        this.level = level;
        return this;
    }

    public String getDataLabel() {
        return dataLabel;
    }

    public EpidemicReport setDataLabel(String dataLabel) {
        this.dataLabel = dataLabel;
        return this;
    }

    public String getComments() {
        return comments;
    }

    public EpidemicReport setComments(String comments) {
        this.comments = comments;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public EpidemicReport setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public EpidemicReport setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public int getConfirmed() {
        return confirmed;
    }

    public EpidemicReport setConfirmed(int confirmed) {
        this.confirmed = confirmed;
        return this;
    }

    public int getSuspected() {
        return suspected;
    }

    public EpidemicReport setSuspected(int suspected) {
        this.suspected = suspected;
        return this;
    }

    public int getHealed() {
        return healed;
    }

    public EpidemicReport setHealed(int healed) {
        this.healed = healed;
        return this;
    }

    public int getDead() {
        return dead;
    }

    public EpidemicReport setDead(int dead) {
        this.dead = dead;
        return this;
    }

    public long getTime() {
        return time;
    }

    public EpidemicReport setTime(long time) {
        this.time = time;
        return this;
    }

    public double getHealedRatio() {
        if (confirmed <= 0) {
            healedRatio = 0;
        } else {
            healedRatio = DoubleTools.scale(healed * 100.0d / confirmed, 2);
        }
        return healedRatio;
    }

    public EpidemicReport setHealedRatio(double healedRatio) {
        this.healedRatio = healedRatio;
        return this;
    }

    public double getDeadRatio() {
        if (confirmed <= 0) {
            deadRatio = 0;
        } else {
            deadRatio = DoubleTools.scale(dead * 100.0d / confirmed, 2);
        }
        return deadRatio;
    }

    public EpidemicReport setDeadRatio(double deadRatio) {
        this.deadRatio = deadRatio;
        return this;
    }

    public int getIncreasedConfirmed() {
        return increasedConfirmed;
    }

    public void setIncreasedConfirmed(int increasedConfirmed) {
        this.increasedConfirmed = increasedConfirmed;
    }

    public int getIncreasedSuspected() {
        return increasedSuspected;
    }

    public void setIncreasedSuspected(int increasedSuspected) {
        this.increasedSuspected = increasedSuspected;
    }

    public int getIncreasedHealed() {
        return increasedHealed;
    }

    public void setIncreasedHealed(int increasedHealed) {
        this.increasedHealed = increasedHealed;
    }

    public int getIncreasedDead() {
        return increasedDead;
    }

    public void setIncreasedDead(int increasedDead) {
        this.increasedDead = increasedDead;
    }

}
