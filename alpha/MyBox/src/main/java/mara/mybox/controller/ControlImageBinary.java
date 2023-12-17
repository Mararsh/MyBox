package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import mara.mybox.bufferedimage.ImageBinary;
import mara.mybox.bufferedimage.ImageBinary.BinaryAlgorithm;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ControlImageBinary extends BaseController {

    protected ImageView imageView;
    protected int threshold;

    @FXML
    protected ToggleGroup binaryGroup;
    @FXML
    protected RadioButton otsuRadio, defaultRadio, thresholdRadio;
    @FXML
    protected TextField thresholdInput;
    @FXML
    protected Button calculateButton;
    @FXML
    protected CheckBox ditherCheck;

    public void setParameters(ImageView imageView) {
        this.imageView = imageView;
        try {
            calculateButton.setVisible(imageView != null);

            binaryGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkThreshold();
                }
            });

            threshold = UserConfig.getInt(baseName + "Threadhold", 128);
            if (threshold < 0 || threshold > 255) {
                threshold = 128;
            }
            thresholdInput.setText(threshold + "");
            thresholdInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> vv, String ov, String nv) {
                    checkThreshold();
                }
            });
            NodeStyleTools.setTooltip(thresholdInput, new Tooltip(message("BWThresholdComments")));

            ditherCheck.setSelected(UserConfig.getBoolean(baseName + "Dither", false));
            ditherCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> vv, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "Dither", nv);
                }
            });

            checkThreshold();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    public boolean checkThreshold() {
        if (!thresholdRadio.isSelected()) {
            thresholdInput.setStyle(null);
            thresholdInput.setEditable(false);
            return true;
        }
        try {
            thresholdInput.setEditable(true);
            int v = Integer.parseInt(thresholdInput.getText());
            if (v >= 0 && v <= 255) {
                threshold = v;
                thresholdInput.setStyle(null);
                UserConfig.setInt(baseName + "Threadhold", threshold);
                return true;
            }
        } catch (Exception e) {
        }
        popError(message("InvalidParameter") + ": " + message("Threadhold"));
        thresholdInput.setStyle(UserConfig.badStyle());
        return false;
    }

    @FXML
    public void calculateAction() {
        if (imageView == null || imageView.getImage() == null) {
            popError(message("NoData"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            int v;

            @Override
            protected boolean handle() {
                v = ImageBinary.threshold(this, imageView.getImage());
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (isCancelled() || v < 0) {
                    return;
                }
                thresholdInput.setText(v + "");
            }

        };
        start(task);
    }

    public ImageBinary pickValues(int t) {
        if (t < 0 && !checkThreshold()) {
            return null;
        }
        ImageBinary imageBinary = new ImageBinary();
        imageBinary.setIntPara1(t <= 0 ? threshold : t)
                .setIsDithering(ditherCheck.isSelected());
        if (otsuRadio.isSelected()) {
            imageBinary.setAlgorithm(BinaryAlgorithm.OTSU);
        } else if (thresholdRadio.isSelected()) {
            imageBinary.setAlgorithm(BinaryAlgorithm.Threshold);
        } else {
            imageBinary.setAlgorithm(BinaryAlgorithm.Default);
        }
        return imageBinary;
    }

}
