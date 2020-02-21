package mara.mybox.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.TableEpidemicReport;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.AppVariables;
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
    protected double longitude = -200, latitude = -200, healedRatio, deadRatio;
    protected int confirmed, suspected, healed, dead,
            increasedConfirmed, increasedSuspected, increasedHealed, increasedDead;
    protected long time = -1;

    public static EpidemicReport create() {
        return new EpidemicReport();
    }

    public static List<EpidemicReport> writeNCPs() {
        if (TableGeographyCode.read(message("China")) == null) {
            GeographyCode.importCodes();
        }

        List<EpidemicReport> data = new ArrayList<>();
        data.addAll(NCP20200101());
        data.addAll(NCP20200103());
        data.addAll(NCP20200110());
        data.addAll(NCP20200207());
        data.addAll(NCP20200208());
        data.addAll(NCP20200209());
        data.addAll(NCP20200210());
        data.addAll(NCP20200211());
        data.addAll(NCP20200213());
        data.addAll(NCP20200218());
        data.addAll(NCP20200219());

        if (TableEpidemicReport.write(data)) {
//            TableEpidemicReport.statistic(message("NewCoronavirusPneumonia"));
            return data;
        } else {
            return null;
        }
    }

    public static EpidemicReport ChinaProvinceReport(
            String dataset, long time, String province,
            int confirmed, int suspected, int healed, int dead) {
        GeographyCode code = GeographyCode.query(province);
        EpidemicReport report = create().setDataSet(dataset)
                .setCountry(message("China")).setProvince(message(province))
                .setLevel(message("Province"))
                .setConfirmed(confirmed).setSuspected(suspected)
                .setHealed(healed).setDead(dead)
                .setTime(time);
        if (code != null) {
            report.setLongitude(code.longitude).setLatitude(code.latitude);
        } else {
            report.setLongitude(-200).setLatitude(-200); // GaoDe Map only supports geography codes of China
        }
        return report;
    }

    public static EpidemicReport CountryReport(
            String dataset, long time, String country,
            int confirmed, int suspected, int healed, int dead) {
        GeographyCode code = TableGeographyCode.read(message(country));
        EpidemicReport report = create().setDataSet(dataset)
                .setCountry(message(country)).setLevel(message("Country"))
                .setConfirmed(confirmed).setSuspected(suspected)
                .setHealed(healed).setDead(dead)
                .setTime(time);
        if (code != null) {
            report.setLongitude(code.longitude).setLatitude(code.latitude);
        } else {
            report.setLongitude(-200).setLatitude(-200); // GaoDe Map only supports geography codes of China
        }
        return report;
    }

    public static List<EpidemicReport> NCP20200101() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-01-01 11:50:00").getTime();
        //        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
//        if (data != null && !data.isEmpty()) {
//            return data;
//        }

        List<EpidemicReport> data = new ArrayList<>();
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHubei", 27, 0, 0, 0));
        return data;
    }

    public static List<EpidemicReport> NCP20200103() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-01-03 17:00:00").getTime();
        //        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
//        if (data != null && !data.isEmpty()) {
//            return data;
//        }

        List<EpidemicReport> data = new ArrayList<>();
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHubei", 44, 0, 0, 0));
        return data;
    }

    public static List<EpidemicReport> NCP20200110() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-01-10 23:00:00").getTime();
        //        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
//        if (data != null && !data.isEmpty()) {
//            return data;
//        }

        List<EpidemicReport> data = new ArrayList<>();
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHubei", 41, 0, 2, 1));
        return data;
    }

    public static List<EpidemicReport> NCP20200207() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-02-07 13:00:00").getTime();
        //        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
//        if (data != null && !data.isEmpty()) {
//            return data;
//        }

        List<EpidemicReport> data = new ArrayList<>();

        data.add(CountryReport(dataset, time, "Japan", 86, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Korea", 24, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Singapore", 33, 0, 2, 0));
        data.add(CountryReport(dataset, time, "Thailand", 25, 0, 8, 0));
        data.add(CountryReport(dataset, time, "Australia", 15, 0, 3, 0));
        data.add(CountryReport(dataset, time, "Malaysia", 15, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Germany", 13, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Vietnam", 12, 0, 3, 0));
        data.add(CountryReport(dataset, time, "UnitedStates", 12, 0, 1, 0));
        data.add(CountryReport(dataset, time, "France", 6, 0, 0, 0));
        data.add(CountryReport(dataset, time, "UnitedArabEmirates", 5, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Canada", 5, 0, 1, 0));
        data.add(CountryReport(dataset, time, "UnitedKingdom", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Italy", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "India", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Philippines", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Russia", 2, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Belgium", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Spain", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Sweden", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Finland", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "SriLanka", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Kampuchea", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Nepal", 1, 0, 0, 0));

        return data;
    }

    public static List<EpidemicReport> NCP20200208() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-02-08 13:00:00").getTime();
        //        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
//        if (data != null && !data.isEmpty()) {
//            return data;
//        }

        List<EpidemicReport> data = new ArrayList<>();

        data.add(CountryReport(dataset, time, "Japan", 89, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Korea", 24, 0, 2, 0));
        data.add(CountryReport(dataset, time, "Singapore", 33, 0, 2, 0));
        data.add(CountryReport(dataset, time, "Thailand", 32, 0, 8, 0));
        data.add(CountryReport(dataset, time, "Australia", 15, 0, 3, 0));
        data.add(CountryReport(dataset, time, "Malaysia", 16, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Germany", 13, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Vietnam", 13, 0, 3, 0));
        data.add(CountryReport(dataset, time, "UnitedStates", 12, 0, 3, 0));
        data.add(CountryReport(dataset, time, "France", 11, 0, 0, 0));
        data.add(CountryReport(dataset, time, "UnitedArabEmirates", 7, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Canada", 5, 0, 1, 0));
        data.add(CountryReport(dataset, time, "UnitedKingdom", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Italy", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "India", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Philippines", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Russia", 2, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Belgium", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Spain", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Sweden", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Finland", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "SriLanka", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Kampuchea", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Nepal", 1, 0, 0, 0));

        return data;
    }

    public static List<EpidemicReport> NCP20200209() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-02-09 13:00:00").getTime();
        //        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
//        if (data != null && !data.isEmpty()) {
//            return data;
//        }

        List<EpidemicReport> data = new ArrayList<>();
        data.add(CountryReport(dataset, time, "Japan", 89, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Korea", 27, 0, 3, 0));
        data.add(CountryReport(dataset, time, "Singapore", 40, 0, 2, 0));
        data.add(CountryReport(dataset, time, "Thailand", 32, 0, 8, 0));
        data.add(CountryReport(dataset, time, "Australia", 15, 0, 3, 0));
        data.add(CountryReport(dataset, time, "Malaysia", 17, 0, 2, 0));
        data.add(CountryReport(dataset, time, "Germany", 14, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Vietnam", 14, 0, 3, 0));
        data.add(CountryReport(dataset, time, "UnitedStates", 12, 0, 3, 0));
        data.add(CountryReport(dataset, time, "France", 11, 0, 0, 0));
        data.add(CountryReport(dataset, time, "UnitedArabEmirates", 7, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Canada", 7, 0, 1, 0));
        data.add(CountryReport(dataset, time, "UnitedKingdom", 4, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Italy", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "India", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Philippines", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Russia", 2, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Belgium", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Spain", 2, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Sweden", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Finland", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "SriLanka", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Kampuchea", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Nepal", 1, 0, 0, 0));

        return data;
    }

    public static List<EpidemicReport> NCP20200210() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-02-10 13:00:00").getTime();
        //        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
//        if (data != null && !data.isEmpty()) {
//            return data;
//        }

        List<EpidemicReport> data = new ArrayList<>();
        data.add(CountryReport(dataset, time, "Japan", 162, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Korea", 27, 0, 3, 0));
        data.add(CountryReport(dataset, time, "Singapore", 45, 0, 7, 0));
        data.add(CountryReport(dataset, time, "Thailand", 32, 0, 10, 0));
        data.add(CountryReport(dataset, time, "Australia", 15, 0, 3, 0));
        data.add(CountryReport(dataset, time, "Malaysia", 18, 0, 2, 0));
        data.add(CountryReport(dataset, time, "Germany", 14, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Vietnam", 14, 0, 6, 0));
        data.add(CountryReport(dataset, time, "UnitedStates", 12, 0, 3, 0));
        data.add(CountryReport(dataset, time, "France", 11, 0, 0, 0));
        data.add(CountryReport(dataset, time, "UnitedArabEmirates", 7, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Canada", 7, 0, 1, 0));
        data.add(CountryReport(dataset, time, "UnitedKingdom", 8, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Italy", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "India", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Philippines", 3, 0, 0, 1));
        data.add(CountryReport(dataset, time, "Russia", 2, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Brazil", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Belgium", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Spain", 2, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Sweden", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Finland", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "SriLanka", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Kampuchea", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Nepal", 1, 0, 0, 0));

        return data;
    }

    public static List<EpidemicReport> NCP20200211() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-02-11 13:00:00").getTime();
//        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
//        if (data != null && !data.isEmpty()) {
//            return data;
//        }

        List<EpidemicReport> data = new ArrayList<>();
        data.add(CountryReport(dataset, time, "Japan", 162, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Korea", 28, 0, 3, 0));
        data.add(CountryReport(dataset, time, "Singapore", 45, 0, 7, 0));
        data.add(CountryReport(dataset, time, "Thailand", 32, 0, 10, 0));
        data.add(CountryReport(dataset, time, "Australia", 15, 0, 3, 0));
        data.add(CountryReport(dataset, time, "Malaysia", 18, 0, 3, 0));
        data.add(CountryReport(dataset, time, "Germany", 14, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Vietnam", 15, 0, 6, 0));
        data.add(CountryReport(dataset, time, "UnitedStates", 13, 0, 3, 0));
        data.add(CountryReport(dataset, time, "France", 11, 0, 0, 0));
        data.add(CountryReport(dataset, time, "UnitedArabEmirates", 8, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Canada", 7, 0, 1, 0));
        data.add(CountryReport(dataset, time, "UnitedKingdom", 8, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Italy", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "India", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Philippines", 3, 0, 0, 1));
        data.add(CountryReport(dataset, time, "Russia", 2, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Brazil", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Belgium", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Spain", 2, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Sweden", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Finland", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "SriLanka", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Kampuchea", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Nepal", 1, 0, 0, 0));

        return data;
    }

    public static List<EpidemicReport> NCP20200213() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-02-13 13:00:00").getTime();
//        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
//        if (data != null && !data.isEmpty()) {
//            return data;
//        }

        List<EpidemicReport> data = new ArrayList<>();
        data.add(CountryReport(dataset, time, "Japan", 251, 0, 1, 1));
        data.add(CountryReport(dataset, time, "Korea", 28, 0, 7, 0));
        data.add(CountryReport(dataset, time, "Singapore", 58, 0, 15, 0));
        data.add(CountryReport(dataset, time, "Thailand", 33, 0, 10, 0));
        data.add(CountryReport(dataset, time, "Australia", 15, 0, 5, 0));
        data.add(CountryReport(dataset, time, "Malaysia", 19, 0, 3, 0));
        data.add(CountryReport(dataset, time, "Germany", 16, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Vietnam", 16, 0, 7, 0));
        data.add(CountryReport(dataset, time, "UnitedStates", 14, 0, 3, 0));
        data.add(CountryReport(dataset, time, "France", 11, 0, 0, 0));
        data.add(CountryReport(dataset, time, "UnitedArabEmirates", 8, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Canada", 7, 0, 1, 0));
        data.add(CountryReport(dataset, time, "UnitedKingdom", 9, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Italy", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "India", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Philippines", 3, 0, 1, 1));
        data.add(CountryReport(dataset, time, "Russia", 2, 0, 2, 0));
        data.add(CountryReport(dataset, time, "Brazil", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Belgium", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Spain", 2, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Sweden", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Finland", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "SriLanka", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Kampuchea", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Nepal", 1, 0, 1, 0));

        return data;
    }

    public static List<EpidemicReport> NCP20200218() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-02-18 13:00:00").getTime();
//        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
//        if (data != null && !data.isEmpty()) {
//            return data;
//        }

        List<EpidemicReport> data = new ArrayList<>();
        data.add(CountryReport(dataset, time, "Japan", 616, 0, 1, 1));
        data.add(CountryReport(dataset, time, "Korea", 31, 0, 12, 0));
        data.add(CountryReport(dataset, time, "Singapore", 81, 0, 29, 0));
        data.add(CountryReport(dataset, time, "Thailand", 33, 0, 10, 0));
        data.add(CountryReport(dataset, time, "Australia", 15, 0, 5, 0));
        data.add(CountryReport(dataset, time, "Malaysia", 22, 0, 9, 0));
        data.add(CountryReport(dataset, time, "Germany", 16, 0, 9, 0));
        data.add(CountryReport(dataset, time, "Vietnam", 16, 0, 9, 0));
        data.add(CountryReport(dataset, time, "UnitedStates", 15, 0, 3, 0));
        data.add(CountryReport(dataset, time, "France", 12, 0, 4, 1));
        data.add(CountryReport(dataset, time, "UnitedArabEmirates", 9, 0, 4, 0));
        data.add(CountryReport(dataset, time, "Canada", 8, 0, 1, 0));
        data.add(CountryReport(dataset, time, "UnitedKingdom", 9, 0, 8, 0));
        data.add(CountryReport(dataset, time, "Italy", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "India", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Philippines", 3, 0, 1, 1));
        data.add(CountryReport(dataset, time, "Russia", 2, 0, 2, 0));
        data.add(CountryReport(dataset, time, "Brazil", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Belgium", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Spain", 2, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Sweden", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Finland", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "SriLanka", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Kampuchea", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Nepal", 1, 0, 1, 0));

        return data;
    }

    public static List<EpidemicReport> NCP20200219() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-02-19 13:00:00").getTime();
//        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
//        if (data != null && !data.isEmpty()) {
//            return data;
//        }

        List<EpidemicReport> data = new ArrayList<>();
        data.add(CountryReport(dataset, time, "Japan", 621, 0, 1, 1));
        data.add(CountryReport(dataset, time, "Singapore", 84, 0, 29, 0));
        data.add(CountryReport(dataset, time, "Korea", 51, 0, 12, 0));
        data.add(CountryReport(dataset, time, "Thailand", 33, 0, 10, 0));
        data.add(CountryReport(dataset, time, "Australia", 15, 0, 5, 0));
        data.add(CountryReport(dataset, time, "Malaysia", 22, 0, 15, 0));
        data.add(CountryReport(dataset, time, "Germany", 16, 0, 9, 0));
        data.add(CountryReport(dataset, time, "Vietnam", 16, 0, 9, 0));
        data.add(CountryReport(dataset, time, "UnitedStates", 15, 0, 3, 0));
        data.add(CountryReport(dataset, time, "France", 12, 0, 4, 1));
        data.add(CountryReport(dataset, time, "UnitedArabEmirates", 9, 0, 4, 0));
        data.add(CountryReport(dataset, time, "Canada", 8, 0, 1, 0));
        data.add(CountryReport(dataset, time, "UnitedKingdom", 9, 0, 8, 0));
        data.add(CountryReport(dataset, time, "Italy", 3, 0, 0, 0));
        data.add(CountryReport(dataset, time, "India", 3, 0, 3, 0));
        data.add(CountryReport(dataset, time, "Philippines", 3, 0, 1, 1));
        data.add(CountryReport(dataset, time, "Russia", 2, 0, 2, 0));
        data.add(CountryReport(dataset, time, "Brazil", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Belgium", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Spain", 2, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Sweden", 1, 0, 0, 0));
        data.add(CountryReport(dataset, time, "Finland", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "SriLanka", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Kampuchea", 1, 0, 1, 0));
        data.add(CountryReport(dataset, time, "Nepal", 1, 0, 1, 0));

        return data;
    }

    public static void importNCPs() {
        if (TableGeographyCode.read(message("China")) == null) {
            GeographyCode.importCodes();
        }
        File file;
        if ("zh".equals(AppVariables.getLanguage())) {
            file = FxmlControl.getInternalFile("/data/db/Epidemic_Report_zh.del",
                    "AppTemp", "EpidemicReport_zh.del");
        } else {
            file = FxmlControl.getInternalFile("/data/db/Epidemic_Report_en.del",
                    "AppTemp", "Epidemic_Report_en.del");
        }
        DerbyBase.importData("Epidemic_Report", file.getAbsolutePath(), false);
        TableEpidemicReport.moveDataid();

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
