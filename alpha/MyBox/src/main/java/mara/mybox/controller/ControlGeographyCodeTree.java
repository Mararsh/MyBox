package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-2-3
 * @License Apache License Version 2.0
 */
public class ControlGeographyCodeTree extends ControlTreeView {

    protected ControlGeographyCodeView mapController;

    public void setPatrameters(GeographyCodeController controller) {
        try {
            parentController = controller;
            mapController = controller.mapController;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
