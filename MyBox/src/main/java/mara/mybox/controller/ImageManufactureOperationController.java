package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.data.ConvolutionKernel;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageManufactureOperationController extends ImageBaseController {

    protected ImageManufactureController parent;
    protected TitledPane myPane;
    protected ImageOperation operation;
    protected ImageManufacturePaneController imageController;
    protected ImageView maskView;

    @FXML
    protected Accordion accordionPane;
    @FXML
    protected TitledPane viewPane, clipboardPane, cropPane, scalePane, colorPane,
            effectPane, enhancementPane, transformPane, shadowPane,
            marginsPane, arcPane, penPane, textPane, richTextPane;

    public ImageManufactureOperationController() {
        baseTitle = AppVariables.message("ImageManufacture");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            accordionPane.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
                @Override
                public void changed(ObservableValue<? extends TitledPane> v, TitledPane o, TitledPane n) {
                    if (parent == null || n == null || (myPane != null && myPane.equals(n))) {
                        return;
                    }
                    expandPane(n);
                }
            });

            isPickingColor.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    if (imageController == null) {
                        return;
                    }
                    imageController.isPickingColor.set(newVal);
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
        //  do not accept event directly
//        logger.debug(event.getCode() + " " + event.getText());
    }

    // handle event passed from parent
    public void eventsHandler(KeyEvent event) {
//        logger.debug(event.getCode() + " " + event.getText() + " " + getClass());
        keyEventsHandlerDo(event);
    }

    public ImageManufactureOperationController expandPane(TitledPane thePane) {
        try {

            String newFxml;
            if (thePane.equals(viewPane)) {
                newFxml = CommonValues.ImageManufactureViewFxml;
            } else if (thePane.equals(clipboardPane)) {
                newFxml = CommonValues.ImageManufactureClipboardFxml;
            } else if (thePane.equals(cropPane)) {
                newFxml = CommonValues.ImageManufactureCropFxml;
            } else if (thePane.equals(colorPane)) {
                newFxml = CommonValues.ImageManufactureColorFxml;
            } else if (thePane.equals(effectPane)) {
                newFxml = CommonValues.ImageManufactureEffectsFxml;
            } else if (thePane.equals(enhancementPane)) {
                newFxml = CommonValues.ImageManufactureEnhancementFxml;
            } else if (thePane.equals(scalePane)) {
                newFxml = CommonValues.ImageManufactureScaleFxml;
            } else if (thePane.equals(transformPane)) {
                newFxml = CommonValues.ImageManufactureTransformFxml;
            } else if (thePane.equals(shadowPane)) {
                newFxml = CommonValues.ImageManufactureShadowFxml;
            } else if (thePane.equals(marginsPane)) {
                newFxml = CommonValues.ImageManufactureMarginsFxml;
            } else if (thePane.equals(arcPane)) {
                newFxml = CommonValues.ImageManufactureArcFxml;
            } else if (thePane.equals(penPane)) {
                newFxml = CommonValues.ImageManufacturePenFxml;
            } else if (thePane.equals(textPane)) {
                newFxml = CommonValues.ImageManufactureTextFxml;
            } else if (thePane.equals(richTextPane)) {
                newFxml = CommonValues.ImageManufactureRichTextFxml;
            } else {
                return null;
            }
            parent.currentImageController.clearOperating();

            FXMLLoader fxmlLoader = new FXMLLoader(FxmlStage.class.getResource(newFxml), AppVariables.currentBundle);
            Pane pane = fxmlLoader.load();
            parent.rightPaneBox.getChildren().clear();
            parent.rightPaneBox.getChildren().add(pane);
            ImageManufactureOperationController controller = (ImageManufactureOperationController) fxmlLoader.getController();
            controller.initPane(parent);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public void initPane(ImageManufactureController parent) {
        try {
            if (parent == null) {
                return;
            }
            this.parent = parent;
            if (parent.operationController != null) {
                parent.operationController.quitPane();
            }
            parent.operationController = this;
            imageController = parent.currentImageController;
            imageView = imageController.imageView;
            maskView = imageController.maskView;
            imageController.imageLabel.setText("");
            if (!parent.imageLoaded.get()) {
                return;
            }
            accordionPane.setExpandedPane(myPane);
            cropPane.disableProperty().bind(parent.editable.not());
            scalePane.disableProperty().bind(parent.editable.not());
            colorPane.disableProperty().bind(parent.editable.not());
            effectPane.disableProperty().bind(parent.editable.not());
            enhancementPane.disableProperty().bind(parent.editable.not());
            scalePane.disableProperty().bind(parent.editable.not());
            transformPane.disableProperty().bind(parent.editable.not());
            shadowPane.disableProperty().bind(parent.editable.not());
            marginsPane.disableProperty().bind(parent.editable.not());
            arcPane.disableProperty().bind(parent.editable.not());
            penPane.disableProperty().bind(parent.editable.not());
            textPane.disableProperty().bind(parent.editable.not());
            richTextPane.disableProperty().bind(parent.editable.not());

            refreshStyle();
            parent.operationChanged(this);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public ImageBaseController refresh() {
        return null;  //Bypass since this is part of frame
    }

    /*
        events passed from image pane
     */
    public void paneClicked(MouseEvent event) {

    }

//    public void colorPicked(Color color) {
//    }
    @FXML
    public void mousePressed(MouseEvent event) {

    }

    @FXML
    public void mouseDragged(MouseEvent event) {

    }

    @FXML
    public void mouseReleased(MouseEvent event) {

    }

    public void applyKernel(ConvolutionKernel kernel) {
        ImageManufactureEnhancementController controller
                = (ImageManufactureEnhancementController) expandPane(enhancementPane);
        if (controller == null) {
            return;
        }
        controller.applyKernel(kernel);
    }

    public void quitPane() {
        isPickingColor.unbind();
        isPickingColor.set(false);
        if (imageController != null) {
            imageController.clearOperating();
        }

    }

}
