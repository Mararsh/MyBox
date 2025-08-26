package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.image.tools.BufferedImageTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ControlImageEmboss extends BaseController {

    protected int raduis;
    protected BufferedImageTools.Direction direction;

    @FXML
    protected RadioButton topRadio, bottomRadio, leftRadio, rightRadio,
            leftTopRadio, rightBottomRadio, leftBottomRadio, rightTopRadio,
            radius3Radio, radius5Radio;
    @FXML
    protected RadioButton keepRadio, greyRadio, bwRadio;

    @Override
    public void initControls() {
        try {
            super.initControls();

            topRadio.setSelected(true);

            raduis = UserConfig.getInt(baseName + "Raduis", 3);
            if (raduis == 3) {
                radius3Radio.setSelected(true);
            } else {
                radius5Radio.setSelected(true);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected ConvolutionKernel pickValues() {
        try {
            if (radius3Radio.isSelected()) {
                raduis = 3;
            } else {
                raduis = 5;
            }
            UserConfig.setInt(baseName + "Raduis", raduis);

            if (topRadio.isSelected()) {
                direction = BufferedImageTools.Direction.Top;
            } else if (bottomRadio.isSelected()) {
                direction = BufferedImageTools.Direction.Bottom;
            } else if (leftRadio.isSelected()) {
                direction = BufferedImageTools.Direction.Left;
            } else if (rightRadio.isSelected()) {
                direction = BufferedImageTools.Direction.Right;
            } else if (leftTopRadio.isSelected()) {
                direction = BufferedImageTools.Direction.LeftTop;
            } else if (rightBottomRadio.isSelected()) {
                direction = BufferedImageTools.Direction.RightBottom;
            } else if (leftBottomRadio.isSelected()) {
                direction = BufferedImageTools.Direction.LeftBottom;
            } else if (rightTopRadio.isSelected()) {
                direction = BufferedImageTools.Direction.RightTop;
            } else {
                direction = BufferedImageTools.Direction.Top;
            }

            int color;
            if (greyRadio.isSelected()) {
                color = ConvolutionKernel.Color.Grey;
            } else if (bwRadio.isSelected()) {
                color = ConvolutionKernel.Color.BlackWhite;
            } else {
                color = ConvolutionKernel.Color.Keep;
            }
            ConvolutionKernel kernel = ConvolutionKernel.makeEmbossKernel(
                    direction, raduis, color);

            return kernel;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
