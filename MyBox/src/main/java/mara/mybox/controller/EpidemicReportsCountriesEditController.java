package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.EpidemicReport;
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
        List<EpidemicReport> data = new ArrayList();
        List<String> countries = TableEpidemicReport.countries(currentDataset);
        countries.remove(message("China"));
        for (String name : countries) {
            data.add(EpidemicReport.CountryReport(currentDataset, time.getTime(), name, 0, 0, 0, 0));
        }
        return data;
    }

}
