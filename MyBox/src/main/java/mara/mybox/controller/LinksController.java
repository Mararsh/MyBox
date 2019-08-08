package mara.mybox.controller;

import mara.mybox.controller.base.BaseController;
import mara.mybox.value.AppVaribles;

/**
 * @Author Mara
 * @CreateDate 2018-6-13 8:14:06
 * @Description
 * @License Apache License Version 2.0
 */
public class LinksController extends BaseController {

    public LinksController() {
        baseTitle = AppVaribles.message("Links");
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        myStage.toFront();
    }

}
