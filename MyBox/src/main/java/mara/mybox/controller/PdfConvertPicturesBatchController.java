package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import static mara.mybox.controller.BaseController.logger;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-6-16
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfConvertPicturesBatchController extends PdfConvertPicturesController {

    public PdfConvertPicturesBatchController() {
    }

    @Override
    protected void initializeNext2() {
        try {
            startButton.disableProperty().bind(
                    Bindings.isEmpty(targetPathInput.textProperty())
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(imageAttributesController.getDensityInput().styleProperty().isEqualTo(badStyle))
                            .or(imageAttributesController.getQualityBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageAttributesController.getQualityInput().styleProperty().isEqualTo(badStyle)))
                            .or(imageAttributesController.getColorBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageAttributesController.getThresholdInput().styleProperty().isEqualTo(badStyle)))
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
