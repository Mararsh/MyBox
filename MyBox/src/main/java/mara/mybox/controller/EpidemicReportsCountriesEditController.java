package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.EpidemicReport;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.NewCoronavirusPneumonia;
import mara.mybox.db.TableEpidemicReport;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-2-4
 * @License Apache License Version 2.0
 */
public class EpidemicReportsCountriesEditController extends EpidemicReportsEditController {

    public EpidemicReportsCountriesEditController() {
        baseTitle = AppVariables.message("GlobalEpidemicReports");
    }

    @Override
    protected List<EpidemicReport> initData() {
        if (currentDataset == null) {
            return null;
        }
        List<String> countries = TableEpidemicReport.countries(currentDataset);
        List<String> allCountries = GeographyCode.countries();
        if (countries == null || countries.isEmpty()) {
            countries = allCountries;
        } else {
            for (String c : allCountries) {
                if (!countries.contains(c)) {
                    countries.add(c);
                }
            }
        }
        countries.remove(message("China"));
        List<EpidemicReport> data = new ArrayList();
        for (String name : countries) {
            data.add(NewCoronavirusPneumonia.CountryReport(currentDataset, time.getTime(), name, 0, 0, 0, 0));
        }
        return data;
    }

}
