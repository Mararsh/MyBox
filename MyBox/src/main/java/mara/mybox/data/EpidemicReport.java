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
    protected String dataSet, dataLabel, comments, country, province, city;
    protected double longitude = -200, latitude = -200, healedRatio, deadRatio;
    protected int confirmed, suspected, healed, dead;
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
        data.addAll(NCP20200122());
        data.addAll(NCP20200131());
        data.addAll(NCP20200202());
        data.addAll(NCP20200203());
        data.addAll(NCP20200204());
        data.addAll(NCP20200205());
        data.addAll(NCP20200207());
        data.addAll(NCP20200208());
        data.addAll(NCP20200209());
        data.addAll(NCP20200210());
        data.addAll(NCP20200211());

        if (TableEpidemicReport.write(data)) {
            TableEpidemicReport.summary(message("NewCoronavirusPneumonia"));
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
                .setCountry(message(country))
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
        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
        if (data != null && !data.isEmpty()) {
            return data;
        }
        data = new ArrayList<>();
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHubei", 27, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityBeijing", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangdong", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityShanghai", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceZhejiang", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceYunnan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceSichuan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShandong", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangxi", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuizhou", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceAnhui", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHainan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceNingxia", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceFujian", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangxi", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityTianjin", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHenan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityChongqing", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi2", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "Taiwan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHunan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceLiaoning", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "Macau", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHebei", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangsu", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHeilongjiang", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "InnerMongolia", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiLin", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXinjiang", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "HongKong", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceQinghai", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXizang", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGansu", 0, 0, 0, 0));

        return data;
    }

    public static List<EpidemicReport> NCP20200103() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-01-03 17:00:00").getTime();
        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
        if (data != null && !data.isEmpty()) {
            return data;
        }
        data = new ArrayList<>();
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHubei", 44, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityBeijing", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangdong", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityShanghai", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceZhejiang", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceYunnan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceSichuan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShandong", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangxi", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuizhou", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceAnhui", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHainan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceNingxia", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceFujian", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangxi", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityTianjin", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHenan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityChongqing", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi2", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "Taiwan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHunan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceLiaoning", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "Macau", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHebei", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangsu", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHeilongjiang", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "InnerMongolia", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiLin", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXinjiang", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "HongKong", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceQinghai", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXizang", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGansu", 0, 0, 0, 0));

        return data;
    }

    public static List<EpidemicReport> NCP20200110() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-01-10 24:00:00").getTime();
        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
        if (data != null && !data.isEmpty()) {
            return data;
        }
        data = new ArrayList<>();
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHubei", 41, 0, 2, 1));
        data.add(ChinaProvinceReport(dataset, time, "CityBeijing", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangdong", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityShanghai", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceZhejiang", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceYunnan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceSichuan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShandong", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangxi", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuizhou", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceAnhui", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHainan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceNingxia", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceFujian", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangxi", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityTianjin", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHenan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityChongqing", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi2", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "Taiwan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHunan", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceLiaoning", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "Macau", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHebei", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangsu", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHeilongjiang", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "InnerMongolia", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiLin", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXinjiang", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "HongKong", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceQinghai", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXizang", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGansu", 0, 0, 0, 0));

        return data;
    }

    public static List<EpidemicReport> NCP20200122() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-01-22 22:45:00").getTime();
        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
        if (data != null && !data.isEmpty()) {
            return data;
        }
        data = new ArrayList<>();
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHubei", 444, 0, 0, 17));
        data.add(ChinaProvinceReport(dataset, time, "CityBeijing", 10, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangdong", 26, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityShanghai", 9, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceZhejiang", 10, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceYunnan", 1, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceSichuan", 5, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShandong", 2, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangxi", 2, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuizhou", 1, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceAnhui", 1, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHainan", 4, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceNingxia", 1, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceFujian", 1, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangxi", 2, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityTianjin", 4, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHenan", 5, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityChongqing", 6, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi2", 1, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "Taiwan", 1, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHunan", 4, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceLiaoning", 2, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "Macau", 1, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHebei", 1, 0, 0, 0));

        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangsu", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHeilongjiang", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "InnerMongolia", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiLin", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXinjiang", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "HongKong", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceQinghai", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXizang", 0, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGansu", 0, 0, 0, 0));

        return data;
    }

    public static List<EpidemicReport> NCP20200131() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-01-31 11:00:00").getTime();
        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
        if (data != null && !data.isEmpty()) {
            return data;
        }
        data = new ArrayList<>();
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHubei", 5806, 0, 116, 204));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceZhejiang", 537, 0, 9, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangdong", 393, 0, 9, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHenan", 352, 0, 3, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHunan", 332, 0, 2, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceAnhui", 237, 0, 3, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangxi", 240, 0, 7, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityChongqing", 206, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangsu", 168, 0, 2, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceSichuan", 177, 0, 1, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShandong", 178, 0, 2, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityBeijing", 121, 0, 5, 1));
        data.add(ChinaProvinceReport(dataset, time, "CityShanghai", 128, 0, 5, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceFujian", 101, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi", 87, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangxi", 87, 0, 2, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHebei", 82, 0, 0, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceYunnan", 76, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHeilongjiang", 59, 0, 0, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceLiaoning", 45, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHainan", 50, 0, 1, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi2", 39, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityTianjin", 31, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGansu", 29, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuizhou", 15, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceNingxia", 21, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "InnerMongolia", 22, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiLin", 14, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXinjiang", 17, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "HongKong", 12, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceQinghai", 8, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "Taiwan", 9, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "Macau", 7, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXizang", 1, 0, 0, 0));

        return data;
    }

    public static List<EpidemicReport> NCP20200202() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-02-02 17:51:17").getTime();
        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
        if (data != null && !data.isEmpty()) {
            return data;
        }
        data = new ArrayList<>();
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHubei", 9074, 0, 221, 294));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceZhejiang", 661, 0, 23, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangdong", 632, 0, 12, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHenan", 493, 0, 5, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHunan", 463, 0, 12, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceAnhui", 340, 0, 6, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangxi", 333, 0, 10, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityChongqing", 275, 0, 7, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangsu", 236, 0, 6, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceSichuan", 231, 0, 4, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShandong", 230, 0, 5, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityBeijing", 191, 0, 9, 1));
        data.add(ChinaProvinceReport(dataset, time, "CityShanghai", 182, 0, 10, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceFujian", 159, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi", 116, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangxi", 111, 0, 2, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHebei", 104, 0, 6, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceYunnan", 99, 0, 2, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHeilongjiang", 95, 0, 2, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceLiaoning", 69, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHainan", 63, 0, 2, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi2", 56, 0, 2, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityTianjin", 40, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGansu", 40, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuizhou", 38, 0, 2, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceNingxia", 28, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "InnerMongolia", 27, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiLin", 23, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXinjiang", 21, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "HongKong", 14, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceQinghai", 11, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "Taiwan", 10, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "Macau", 8, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXizang", 1, 0, 0, 0));

        return data;
    }

    public static List<EpidemicReport> NCP20200203() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-02-03 22:42:17").getTime();
        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
        if (data != null && !data.isEmpty()) {
            return data;
        }
        data = new ArrayList<>();
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHubei", 11177, 0, 320, 350));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceZhejiang", 724, 0, 43, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangdong", 725, 0, 20, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHenan", 566, 0, 15, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHunan", 521, 0, 21, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceAnhui", 408, 0, 14, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangxi", 391, 0, 18, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityChongqing", 312, 0, 9, 3));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangsu", 271, 0, 8, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceSichuan", 254, 0, 14, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShandong", 230, 0, 5, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityBeijing", 212, 0, 12, 1));
        data.add(ChinaProvinceReport(dataset, time, "CityShanghai", 203, 0, 10, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceFujian", 179, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi", 128, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangxi", 127, 0, 7, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHebei", 113, 0, 3, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceYunnan", 114, 0, 5, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHeilongjiang", 121, 0, 3, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceLiaoning", 74, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHainan", 72, 0, 4, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi2", 66, 0, 2, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityTianjin", 60, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGansu", 51, 0, 3, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuizhou", 46, 0, 2, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceNingxia", 31, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "InnerMongolia", 34, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiLin", 31, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXinjiang", 24, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "HongKong", 15, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceQinghai", 13, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "Taiwan", 10, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "Macau", 8, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXizang", 1, 0, 0, 0));

        return data;
    }

    public static List<EpidemicReport> NCP20200204() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-02-04 20:30:17").getTime();
        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
        if (data != null && !data.isEmpty()) {
            return data;
        }

        data = new ArrayList<>();
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHubei", 13522, 0, 398, 414));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceZhejiang", 829, 0, 60, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangdong", 813, 0, 27, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHenan", 675, 0, 35, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHunan", 593, 0, 32, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceAnhui", 480, 0, 20, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangxi", 476, 0, 19, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityChongqing", 344, 0, 14, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangsu", 308, 0, 12, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceSichuan", 282, 0, 15, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShandong", 275, 0, 11, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityBeijing", 228, 0, 23, 1));
        data.add(ChinaProvinceReport(dataset, time, "CityShanghai", 219, 0, 12, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceFujian", 194, 0, 7, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi", 142, 0, 2, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangxi", 139, 0, 10, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHebei", 126, 0, 4, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceYunnan", 119, 0, 5, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHeilongjiang", 155, 0, 2, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceLiaoning", 77, 0, 2, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHainan", 80, 0, 6, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi2", 74, 0, 2, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityTianjin", 67, 0, 2, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGansu", 55, 0, 3, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuizhou", 58, 0, 2, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceNingxia", 34, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "InnerMongolia", 35, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiLin", 42, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXinjiang", 29, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "HongKong", 17, 0, 0, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceQinghai", 15, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "Taiwan", 10, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "Macau", 10, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXizang", 1, 0, 0, 0));

        return data;
    }

    public static List<EpidemicReport> NCP20200205() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-02-05 22:34:17").getTime();
        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
        if (data != null && !data.isEmpty()) {
            return data;
        }

        data = new ArrayList<>();
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHubei", 16678, 0, 533, 479));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceZhejiang", 895, 0, 77, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangdong", 895, 0, 41, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHenan", 764, 0, 50, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHunan", 661, 0, 48, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceAnhui", 530, 0, 23, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangxi", 548, 0, 28, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityChongqing", 376, 0, 15, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangsu", 341, 0, 22, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceSichuan", 301, 0, 24, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShandong", 307, 0, 15, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityBeijing", 253, 0, 24, 1));
        data.add(ChinaProvinceReport(dataset, time, "CityShanghai", 243, 0, 15, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceFujian", 205, 0, 11, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi", 165, 0, 6, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangxi", 150, 0, 14, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHebei", 135, 0, 6, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceYunnan", 124, 0, 5, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHeilongjiang", 190, 0, 7, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceLiaoning", 88, 0, 4, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHainan", 99, 0, 5, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi2", 81, 0, 5, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityTianjin", 69, 0, 2, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGansu", 57, 0, 6, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuizhou", 64, 0, 8, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceNingxia", 34, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "InnerMongolia", 42, 0, 3, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiLin", 54, 0, 2, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXinjiang", 32, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "HongKong", 21, 0, 0, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceQinghai", 17, 0, 3, 0));
        data.add(ChinaProvinceReport(dataset, time, "Taiwan", 11, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "Macau", 10, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXizang", 1, 0, 0, 0));

        return data;
    }

    public static List<EpidemicReport> NCP20200207() {
        String dataset = message("NewCoronavirusPneumonia");
        long time = DateTools.stringToDatetime("2020-02-07 22:28:17").getTime();
        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
        if (data != null && !data.isEmpty()) {
            return data;
        }

        data = new ArrayList<>();
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHubei", 22112, 0, 832, 618));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceZhejiang", 1006, 0, 122, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangdong", 1034, 0, 86, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHenan", 914, 0, 87, 3));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHunan", 772, 0, 111, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceAnhui", 665, 0, 47, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangxi", 661, 0, 45, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityChongqing", 415, 0, 31, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangsu", 408, 0, 42, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceSichuan", 344, 0, 42, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShandong", 386, 0, 36, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityBeijing", 297, 0, 33, 1));
        data.add(ChinaProvinceReport(dataset, time, "CityShanghai", 277, 0, 30, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceFujian", 224, 0, 20, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi", 184, 0, 17, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangxi", 172, 0, 17, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHebei", 172, 0, 21, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceYunnan", 136, 0, 15, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHeilongjiang", 277, 0, 12, 3));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceLiaoning", 96, 0, 7, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHainan", 99, 0, 5, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi2", 96, 0, 15, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityTianjin", 81, 0, 2, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGansu", 67, 0, 9, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuizhou", 81, 0, 6, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceNingxia", 43, 0, 3, 0));
        data.add(ChinaProvinceReport(dataset, time, "InnerMongolia", 50, 0, 5, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiLin", 65, 0, 4, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXinjiang", 39, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "HongKong", 25, 0, 0, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceQinghai", 18, 0, 3, 0));
        data.add(ChinaProvinceReport(dataset, time, "Taiwan", 16, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "Macau", 10, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXizang", 1, 0, 0, 0));

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
        long time = DateTools.stringToDatetime("2020-02-08 22:28:17").getTime();
        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
        if (data != null && !data.isEmpty()) {
            return data;
        }

        data = new ArrayList<>();
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHubei", 24953, 0, 1159, 699));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceZhejiang", 1048, 0, 173, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangdong", 1095, 0, 115, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHenan", 981, 0, 105, 4));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHunan", 803, 0, 146, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceAnhui", 733, 0, 59, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangxi", 698, 0, 55, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityChongqing", 428, 0, 39, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangsu", 439, 0, 51, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceSichuan", 363, 0, 59, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShandong", 416, 0, 42, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityBeijing", 315, 0, 34, 2));
        data.add(ChinaProvinceReport(dataset, time, "CityShanghai", 286, 0, 41, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceFujian", 239, 0, 24, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi", 195, 0, 19, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangxi", 183, 0, 17, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHebei", 195, 0, 28, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceYunnan", 138, 0, 21, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHeilongjiang", 295, 0, 13, 5));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceLiaoning", 105, 0, 8, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHainan", 124, 0, 14, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi2", 104, 0, 20, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityTianjin", 88, 0, 4, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGansu", 71, 0, 9, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuizhou", 89, 0, 7, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceNingxia", 45, 0, 13, 0));
        data.add(ChinaProvinceReport(dataset, time, "InnerMongolia", 52, 0, 5, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiLin", 69, 0, 4, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXinjiang", 42, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "HongKong", 26, 0, 0, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceQinghai", 18, 0, 3, 0));
        data.add(ChinaProvinceReport(dataset, time, "Taiwan", 17, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "Macau", 10, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXizang", 1, 0, 0, 0));

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
        long time = DateTools.stringToDatetime("2020-02-09 22:28:17").getTime();
        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
        if (data != null && !data.isEmpty()) {
            return data;
        }

        data = new ArrayList<>();
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHubei", 27100, 0, 1471, 780));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceZhejiang", 1075, 0, 201, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangdong", 1131, 0, 143, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHenan", 1033, 0, 155, 6));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHunan", 838, 0, 183, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceAnhui", 779, 0, 72, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangxi", 740, 0, 72, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityChongqing", 450, 0, 50, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangsu", 468, 0, 74, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceSichuan", 386, 0, 71, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShandong", 444, 0, 61, 1));
        data.add(ChinaProvinceReport(dataset, time, "CityBeijing", 326, 0, 37, 2));
        data.add(ChinaProvinceReport(dataset, time, "CityShanghai", 293, 0, 44, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceFujian", 250, 0, 35, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi", 208, 0, 24, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangxi", 195, 0, 18, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHebei", 206, 0, 32, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceYunnan", 141, 0, 18, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHeilongjiang", 307, 0, 14, 6));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceLiaoning", 107, 0, 12, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHainan", 130, 0, 19, 3));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi2", 115, 0, 22, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityTianjin", 90, 0, 4, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGansu", 79, 0, 14, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuizhou", 99, 0, 7, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceNingxia", 45, 0, 13, 0));
        data.add(ChinaProvinceReport(dataset, time, "InnerMongolia", 54, 0, 5, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiLin", 78, 0, 12, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXinjiang", 45, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "HongKong", 29, 0, 1, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceQinghai", 18, 0, 3, 0));
        data.add(ChinaProvinceReport(dataset, time, "Taiwan", 18, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "Macau", 10, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXizang", 1, 0, 0, 0));

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
        long time = DateTools.stringToDatetime("2020-02-10 22:28:17").getTime();
        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
        if (data != null && !data.isEmpty()) {
            return data;
        }

        data = new ArrayList<>();
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHubei", 29631, 0, 1829, 871));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceZhejiang", 1092, 0, 241, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangdong", 1159, 0, 166, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHenan", 1073, 0, 185, 6));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHunan", 879, 0, 208, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceAnhui", 830, 0, 89, 3));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangxi", 771, 0, 102, 1));
        data.add(ChinaProvinceReport(dataset, time, "CityChongqing", 473, 0, 72, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangsu", 492, 0, 80, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceSichuan", 405, 0, 80, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShandong", 466, 0, 66, 1));
        data.add(ChinaProvinceReport(dataset, time, "CityBeijing", 337, 0, 44, 2));
        data.add(ChinaProvinceReport(dataset, time, "CityShanghai", 299, 0, 48, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceFujian", 261, 0, 37, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi", 213, 0, 32, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangxi", 210, 0, 18, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHebei", 218, 0, 39, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceYunnan", 149, 0, 19, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHeilongjiang", 331, 0, 30, 8));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceLiaoning", 108, 0, 13, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHainan", 138, 0, 19, 3));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi2", 119, 0, 25, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityTianjin", 95, 0, 8, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGansu", 83, 0, 17, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuizhou", 109, 0, 10, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceNingxia", 49, 0, 14, 0));
        data.add(ChinaProvinceReport(dataset, time, "InnerMongolia", 58, 0, 5, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiLin", 80, 0, 12, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXinjiang", 49, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "HongKong", 38, 0, 1, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceQinghai", 18, 0, 3, 0));
        data.add(ChinaProvinceReport(dataset, time, "Taiwan", 18, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "Macau", 10, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXizang", 1, 0, 0, 0));

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
        long time = DateTools.stringToDatetime("2020-02-11 13:58:17").getTime();
        List<EpidemicReport> data = TableEpidemicReport.read(dataset, time, 1);
        if (data != null && !data.isEmpty()) {
            return data;
        }

        data = new ArrayList<>();
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHubei", 31728, 0, 2222, 974));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceZhejiang", 1117, 0, 250, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangdong", 1177, 0, 185, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHenan", 1105, 0, 198, 7));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHunan", 912, 0, 222, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceAnhui", 860, 0, 88, 4));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangxi", 804, 0, 127, 1));
        data.add(ChinaProvinceReport(dataset, time, "CityChongqing", 486, 0, 66, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiangsu", 515, 0, 87, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceSichuan", 417, 0, 82, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShandong", 486, 0, 72, 1));
        data.add(ChinaProvinceReport(dataset, time, "CityBeijing", 342, 0, 48, 3));
        data.add(ChinaProvinceReport(dataset, time, "CityShanghai", 303, 0, 52, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceFujian", 267, 0, 39, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi", 219, 0, 32, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuangxi", 215, 0, 30, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHebei", 239, 0, 41, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceYunnan", 149, 0, 19, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHeilongjiang", 360, 0, 27, 8));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceLiaoning", 108, 0, 14, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceHainan", 142, 0, 20, 3));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceShanxi2", 122, 0, 26, 0));
        data.add(ChinaProvinceReport(dataset, time, "CityTianjin", 102, 0, 8, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGansu", 86, 0, 21, 2));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceGuizhou", 118, 0, 10, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceNingxia", 53, 0, 14, 0));
        data.add(ChinaProvinceReport(dataset, time, "InnerMongolia", 58, 0, 5, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceJiLin", 81, 0, 13, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXinjiang", 55, 0, 0, 0));
        data.add(ChinaProvinceReport(dataset, time, "HongKong", 42, 0, 1, 1));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceQinghai", 18, 0, 3, 0));
        data.add(ChinaProvinceReport(dataset, time, "Taiwan", 18, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "Macau", 10, 0, 1, 0));
        data.add(ChinaProvinceReport(dataset, time, "ProvinceXizang", 1, 0, 0, 0));

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

    public static void importNCPs() {
        if (TableGeographyCode.read(message("China")) == null) {
            GeographyCode.importCodes();
        }
        File file;
        if ("zh".equals(AppVariables.getLanguage())) {
            file = FxmlControl.getInternalFile("/data/db/EpidemicReport_zh.del",
                    "AppTemp", "EpidemicReport_zh.del");
        } else {
            file = FxmlControl.getInternalFile("/data/db/EpidemicReport_en.del",
                    "AppTemp", "EpidemicReport_en.del");
        }
        DerbyBase.importData("Epidemic_Report", file.getAbsolutePath(), false);

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

}
