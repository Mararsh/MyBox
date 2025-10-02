package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import mara.mybox.db.data.ConvolutionKernel;

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
    protected RadioButton zeroEdgeRadio, keepEdgeRadio, keepColorRadio, greyRadio, bwRadio;

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
            if (zeroEdgeRadio.isSelected()) {
                kernel.setEdge(ConvolutionKernel.Edge_Op.FILL_ZERO);
            } else {
                kernel.setEdge(ConvolutionKernel.Edge_Op.COPY);
            }
            if (greyRadio.isSelected()) {
                kernel.setColor(ConvolutionKernel.Color.Grey);
            } else if (bwRadio.isSelected()) {
                kernel.setColor(ConvolutionKernel.Color.BlackWhite);
            } else {
                kernel.setColor(ConvolutionKernel.Color.Keep);
            }
            return kernel;
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

}
