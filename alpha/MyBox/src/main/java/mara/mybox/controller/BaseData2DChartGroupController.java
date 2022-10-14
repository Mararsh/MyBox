package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-10-14
 * @License Apache License Version 2.0
 */
public class BaseData2DChartGroupController extends BaseData2DGroupController {

    @FXML
    protected ControlPlay playController;

    @Override
    public void initControls() {
        try {

            super.initControls();

            playController.setParameters(this);

            playController.frameNodify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
//                    displayFrame(playController.currentIndex);
                }
            });

            playController.stopNodify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
//                    closeFile();
                }
            });

            playController.intervalNodify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
//                    if (imageInfos != null) {
//                        for (ImageInformation info : imageInfos) {
//                            info.setDuration(playController.interval);
//                        }
//                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
