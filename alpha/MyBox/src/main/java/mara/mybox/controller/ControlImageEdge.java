package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ControlImageEdge extends BaseController {

    @FXML
    protected RadioButton eightLaplaceRadio, eightLaplaceExcludedRadio,
            fourLaplaceRadio, fourLaplaceExcludedRadio;
    @FXML
    protected CheckBox greyCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

            greyCheck.setSelected(UserConfig.getBoolean(baseName + "Grey", true));
            greyCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Grey", greyCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public ConvolutionKernel pickValues() {
        try {
            ConvolutionKernel kernel;
            if (eightLaplaceRadio.isSelected()) {
                kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplace();
            } else if (eightLaplaceExcludedRadio.isSelected()) {
                kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplaceInvert();
            } else if (fourLaplaceRadio.isSelected()) {
                kernel = ConvolutionKernel.makeEdgeDetectionFourNeighborLaplace();
            } else if (fourLaplaceExcludedRadio.isSelected()) {
                kernel = ConvolutionKernel.makeEdgeDetectionFourNeighborLaplaceInvert();
            } else {
                return null;
            }
            kernel.setGray(greyCheck.isSelected());
            return kernel;
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

}
