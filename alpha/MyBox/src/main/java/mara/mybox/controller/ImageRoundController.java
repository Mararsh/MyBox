package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageRoundController extends BaseImageEditController {

    protected int round;

    @FXML
    protected ComboBox roundSelector;
    @FXML
    protected ControlColorSet colorController;

    public ImageRoundController() {
        baseTitle = message("Round");
    }

    @Override
    protected void initMore() {
        try {
            operation = message("Round");

            colorController.init(this, baseName + "Color");
            roundSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            round = v;
                            UserConfig.setInt(baseName + "Round", round);
                            ValidationTools.setEditorNormal(roundSelector);
                        } else {
                            ValidationTools.setEditorBadStyle(roundSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(roundSelector);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || image == null) {
                return false;
            }
            round = UserConfig.getInt(baseName + "Round", 20);
            if (round < 0) {
                round = 0;
            }
            int width = (int) imageView.getImage().getWidth();
            List<String> values = new ArrayList<>();
            values.addAll(Arrays.asList(
                    width / 6 + "", width / 8 + "", width / 4 + "", width / 10 + "",
                    "0", "15", "30", "50", "150", "300", "10", "3"));
            String v = round + "";
            if (!values.contains(v)) {
                values.add(0, v);
            }
            isSettingValues = true;
            roundSelector.getItems().setAll(values);
            roundSelector.setValue(v);
            isSettingValues = false;

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @Override
    protected void handleImage() {
        Color c = colorController.color();
        opInfo = round + " " + c;
        handledImage = FxImageTools.setRound(srcImage(), round, c);
    }

    /*
        static methods
     */
    public static ImageRoundController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageRoundController controller = (ImageRoundController) WindowTools.branch(
                    parent.getMyWindow(), Fxmls.ImageRoundFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
