package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.image.ColorDemos;
import mara.mybox.image.data.ImageScope;
import mara.mybox.image.data.PixelsOperation;
import mara.mybox.image.data.PixelsOperationFactory;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-9-24
 * @License Apache License Version 2.0
 */
public class ImageReplaceColorBatchController extends BaseImageEditBatchController {

    protected PixelsOperation pixelsOperation;

    @FXML
    protected ControlColorMatch matchController;
    @FXML
    protected CheckBox excludeCheck, hueCheck, saturationCheck, brightnessCheck;
    @FXML
    protected ControlColorSet originalColorSetController, newColorSetController;

    public ImageReplaceColorBatchController() {
        baseTitle = message("ImageBatch") + " - " + message("ReplaceColor");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            originalColorSetController.init(this, baseName + "OriginalColor", Color.WHITE);
            originalColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue ov, Paint oldValue, Paint newValue) {
                    handleTransparentCheck.setVisible(originalColorSetController.awtColor().getRGB() != 0);
                }
            });
            handleTransparentCheck.setVisible(originalColorSetController.awtColor().getRGB() != 0);

            newColorSetController.init(this, baseName + "NewColor", Color.TRANSPARENT);

            hueCheck.setSelected(UserConfig.getBoolean(baseName + "ReplaceHue", true));
            hueCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                    UserConfig.setBoolean(baseName + "ReplaceHue", hueCheck.isSelected());
                }
            });

            saturationCheck.setSelected(UserConfig.getBoolean(baseName + "ReplaceSaturation", false));
            saturationCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                    UserConfig.setBoolean(baseName + "ReplaceSaturation", saturationCheck.isSelected());
                }
            });

            brightnessCheck.setSelected(UserConfig.getBoolean(baseName + "ReplaceBrightness", false));
            brightnessCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                    UserConfig.setBoolean(baseName + "ReplaceBrightness", brightnessCheck.isSelected());
                }
            });

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(tableView.getItems())
            );

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            if (((Color) originalColorSetController.rect.getFill())
                    .equals(((Color) newColorSetController.rect.getFill()))) {
                popError(message("OriginalNewSameColor"));
                return false;
            }

            if (!pickOperation()) {
                popError(message("InvalidParameter") + ": " + message("Distance"));
                return false;
            }
            return super.makeMoreParameters();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    protected boolean pickOperation() {
        try {
            boolean ok;
            java.awt.Color originalColor = originalColorSetController.awtColor();
            List<java.awt.Color> colors = new ArrayList();
            colors.add(originalColor);
            ImageScope scope = new ImageScope()
                    .setShapeType(ImageScope.ShapeType.Whole)
                    .setColors(colors)
                    .setColorExcluded(excludeCheck.isSelected());
            ok = matchController.pickValuesTo(scope);

            pixelsOperation = PixelsOperationFactory.create(null, scope,
                    PixelsOperation.OperationType.ReplaceColor,
                    PixelsOperation.ColorActionType.Set)
                    .setColorPara1(originalColor)
                    .setColorPara2(newColorSetController.awtColor())
                    .setSkipTransparent(originalColor.getRGB() != 0 && !handleTransparentCheck.isSelected())
                    .setBoolPara1(hueCheck.isSelected())
                    .setBoolPara2(saturationCheck.isSelected())
                    .setBoolPara3(brightnessCheck.isSelected());
            return ok;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        return pixelsOperation.setImage(source).setTask(currentTask).start();
    }

    @Override
    public void makeDemoFiles(FxTask currentTask, List<String> files, File demoFile, BufferedImage demoImage) {
        try {
            pickOperation();
            ColorDemos.replaceColor(currentTask, files,
                    pixelsOperation.setImage(demoImage), demoFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
