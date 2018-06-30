/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import static mara.mybox.controller.BaseController.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-28
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageViewerIController extends ImageViewerController {

    @Override
    protected void initializeNext2() {
        try {
            setTips();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
