package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.EpidemicReport;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.NewCoronavirusPneumonia;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2020-2-4
 * @License Apache License Version 2.0
 */
public class EpidemicReportsChineseProvincesEditController extends EpidemicReportsEditController {

    public EpidemicReportsChineseProvincesEditController() {
        baseTitle = AppVariables.message("ChineseProvincesEpidemicReports");
    }

    @Override
    protected List<EpidemicReport> initData() {
        if (currentDataset == null) {
            return null;
        }
        List<EpidemicReport> data = new ArrayList();
        for (String name : GeographyCode.ChineseProvinces()) {
            data.add(NewCoronavirusPneumonia.ChinaProvinceReport(currentDataset, time.getTime(), name, 0, 0, 0, 0));
        }
        return data;
    }

}
