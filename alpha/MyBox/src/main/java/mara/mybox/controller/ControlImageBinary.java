package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
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
    protected SimpleBooleanProperty notify;

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

    public ControlImageBinary() {
        notify = new SimpleBooleanProperty(false);
    }

    public void notifyChange() {
        notify.set(!notify.get());
    }

    public void setParameters(BaseController parent, ImageView imageView) {
        parentController = parent;
        this.imageView = imageView;
        try {
            calculateButton.setVisible(imageView != null);

            binaryGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkThreshold();
                    notifyChange();
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
                    notifyChange();
                }
            });
            NodeStyleTools.setTooltip(thresholdInput, new Tooltip(message("BWThresholdComments")));

            ditherCheck.setSelected(UserConfig.getBoolean(baseName + "Dither", false));
            ditherCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> vv, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "Dither", nv);
                    notifyChange();
                }
            });

            checkThreshold();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void checkThreshold() {
        try {
            if (!thresholdRadio.isSelected()) {
                thresholdInput.setStyle(null);
                thresholdInput.setEditable(false);
                return;
            }
            thresholdInput.setEditable(true);
            int v = Integer.parseInt(thresholdInput.getText());
            if (v >= 0 && v <= 255) {
                threshold = v;
                thresholdInput.setStyle(null);
                UserConfig.setInt(baseName + "Threadhold", threshold);
            } else {
                thresholdInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            thresholdInput.setStyle(UserConfig.badStyle());
        }
    }

    @FXML
    public void calculateAction() {
        try {
            if (!(parentController instanceof BaseImageController)) {
                return;
            }
            if (imageView == null || imageView.getImage() == null) {
                popError(message("NoData"));
                return;
            }
            int v = ImageBinary.calculateThreshold(((BaseImageController) parentController).imageView.getImage());
            thresholdInput.setText(v + "");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public BinaryAlgorithm algorithm() {
        if (otsuRadio.isSelected()) {
            return BinaryAlgorithm.OTSU;
        } else if (thresholdRadio.isSelected()) {
            return BinaryAlgorithm.Threshold;
        } else {
            return BinaryAlgorithm.Default;
        }
    }

    public int threshold(BufferedImage image) {
        if (otsuRadio.isSelected() && image != null) {
            return ImageBinary.calculateThreshold(image);
        } else if (thresholdRadio.isSelected()) {
            return threshold;
        } else {
            return -1;
        }
    }

    public int threshold() {
        if (imageView != null && imageView.getImage() != null) {
            return threshold(SwingFXUtils.fromFXImage(imageView.getImage(), null));
        } else {
            return threshold(null);
        }
    }

    public boolean dither() {
        return ditherCheck.isSelected();
    }

}
