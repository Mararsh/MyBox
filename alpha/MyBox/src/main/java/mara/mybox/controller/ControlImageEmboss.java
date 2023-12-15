package mara.mybox.controller;

import java.util.Arrays;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ControlImageEmboss extends BaseController {

    protected int direction, raduis;

    @FXML
    protected RadioButton topRadio, bottomRadio, leftRadio, rightRadio,
            leftTopRadio, rightBottomRadio, leftBottomRadio, rightTopRadio;
    @FXML
    protected ComboBox<String> raduisSelector;
    @FXML
    protected CheckBox greyCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

            topRadio.setSelected(true);

            raduis = UserConfig.getInt(baseName + "Raduis", 3);
            if (raduis <= 0) {
                raduis = 3;
            }
            raduisSelector.getItems().addAll(Arrays.asList("1", "3", "5"));
            raduisSelector.setValue(raduis + "");

            greyCheck.setSelected(UserConfig.getBoolean(baseName + "Grey", true));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected ConvolutionKernel pickValues() {
        try {
            int v;
            try {
                v = Integer.parseInt(raduisSelector.getValue());
            } catch (Exception e) {
                v = -1;
            }
            if (v > 0) {
                raduis = v;
                UserConfig.setInt(baseName + "Raduis", raduis);
                ValidationTools.setEditorNormal(raduisSelector);
            } else {
                popError(message("InvalidParameter") + ": " + message("Radius"));
                ValidationTools.setEditorBadStyle(raduisSelector);
                return null;
            }

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
            UserConfig.setBoolean(baseName + "Grey", greyCheck.isSelected());

            ConvolutionKernel kernel = ConvolutionKernel.makeEmbossKernel(
                    direction, raduis, greyCheck.isSelected());
            return kernel;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
