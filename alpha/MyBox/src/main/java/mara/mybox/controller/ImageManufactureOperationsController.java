package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-9-5
 * @License Apache License Version 2.0
 */
public class ImageManufactureOperationsController extends ImageViewerController {

    protected ImageManufactureController imageController;

    protected ImageManufactureCopyController copyController;
    protected ImageManufactureClipboardController clipboardController;
    protected ImageManufactureCropController cropController;
    protected ImageManufactureScaleController scaleController;
    protected ImageManufactureColorController colorController;
    protected ImageManufactureEffectsController effectController;
    protected ImageManufactureEnhancementController enhancementController;
    protected ImageManufactureTextController textController;
    protected ImageManufacturePenController penController;
    protected ImageManufactureTransformController transformController;
    protected ImageManufactureArcController arcController;
    protected ImageManufactureShadowController shadowController;
    protected ImageManufactureMarginsController marginsController;

    protected ImageManufactureOperationController currentController;

    @FXML
    protected Accordion accordionPane;
    @FXML
    protected TitledPane copyPane, clipboardPane, cropPane, scalePane, colorPane,
            effectPane, enhancementPane, transformPane, shadowPane,
            marginsPane, arcPane, penPane, textPane, richTextPane;

    public ImageManufactureOperationsController() {
        baseTitle = Languages.message("ImageManufacture");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            accordionPane.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
                @Override
                public void changed(ObservableValue<? extends TitledPane> v, TitledPane o, TitledPane n) {
                    checkPaneStatus();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(ImageManufactureController parent) {
        this.parentController = parent;
        imageController = parent;
        imageView = imageController.imageView;
        baseName = imageController.baseName;

    }

    public void checkPaneStatus() {
        try {
            TitledPane currentPane = accordionPane.getExpandedPane();
            if (imageController == null || currentPane == null) {
                return;
            }
            imageController.showRightPane();
            imageController.resetImagePane();
            ImageManufactureOperationController controller;
            if (currentPane.equals(copyPane)) {
                controller = checkPaneStatus(currentPane, Fxmls.ImageManufactureCopyFxml);
                if (controller != null) {
                    copyController = (ImageManufactureCopyController) controller;
                }
                copyController.paneExpanded();
                currentController = copyController;
            } else if (currentPane.equals(clipboardPane)) {
                controller = checkPaneStatus(currentPane, Fxmls.ImageManufactureClipboardFxml);
                if (controller != null) {
                    clipboardController = (ImageManufactureClipboardController) controller;
                }
                clipboardController.paneExpanded();
                currentController = clipboardController;
            } else if (currentPane.equals(cropPane)) {
                controller = checkPaneStatus(currentPane, Fxmls.ImageManufactureCropFxml);
                if (controller != null) {
                    cropController = (ImageManufactureCropController) controller;
                }
                cropController.paneExpanded();
                currentController = cropController;
            } else if (currentPane.equals(colorPane)) {
                controller = checkPaneStatus(currentPane, Fxmls.ImageManufactureColorFxml);
                if (controller != null) {
                    colorController = (ImageManufactureColorController) controller;
                }
                colorController.paneExpanded();
                currentController = colorController;
            } else if (currentPane.equals(effectPane)) {
                controller = checkPaneStatus(currentPane, Fxmls.ImageManufactureEffectsFxml);
                if (controller != null) {
                    effectController = (ImageManufactureEffectsController) controller;
                }
                effectController.paneExpanded();
                currentController = effectController;
            } else if (currentPane.equals(enhancementPane)) {
                controller = checkPaneStatus(currentPane, Fxmls.ImageManufactureEnhancementFxml);
                if (controller != null) {
                    enhancementController = (ImageManufactureEnhancementController) controller;
                }
                enhancementController.paneExpanded();
                currentController = enhancementController;
            } else if (currentPane.equals(scalePane)) {
                controller = checkPaneStatus(currentPane, Fxmls.ImageManufactureScaleFxml);
                if (controller != null) {
                    scaleController = (ImageManufactureScaleController) controller;
                }
                scaleController.paneExpanded();
                currentController = scaleController;
            } else if (currentPane.equals(transformPane)) {
                controller = checkPaneStatus(currentPane, Fxmls.ImageManufactureTransformFxml);
                if (controller != null) {
                    transformController = (ImageManufactureTransformController) controller;
                }
                transformController.paneExpanded();
                currentController = transformController;
            } else if (currentPane.equals(shadowPane)) {
                controller = checkPaneStatus(currentPane, Fxmls.ImageManufactureShadowFxml);
                if (controller != null) {
                    shadowController = (ImageManufactureShadowController) controller;
                }
                shadowController.paneExpanded();
                currentController = shadowController;
            } else if (currentPane.equals(marginsPane)) {
                controller = checkPaneStatus(currentPane, Fxmls.ImageManufactureMarginsFxml);
                if (controller != null) {
                    marginsController = (ImageManufactureMarginsController) controller;
                }
                marginsController.paneExpanded();
                currentController = marginsController;
            } else if (currentPane.equals(arcPane)) {
                controller = checkPaneStatus(currentPane, Fxmls.ImageManufactureArcFxml);
                if (controller != null) {
                    arcController = (ImageManufactureArcController) controller;
                }
                arcController.paneExpanded();
                currentController = arcController;
            } else if (currentPane.equals(penPane)) {
                controller = checkPaneStatus(currentPane, Fxmls.ImageManufacturePenFxml);
                if (controller != null) {
                    penController = (ImageManufacturePenController) controller;
                }
                penController.paneExpanded();
                currentController = penController;
            } else if (currentPane.equals(textPane)) {
                controller = checkPaneStatus(currentPane, Fxmls.ImageManufactureTextFxml);
                if (controller != null) {
                    textController = (ImageManufactureTextController) controller;
                }
                textController.paneExpanded();
                currentController = textController;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected ImageManufactureOperationController checkPaneStatus(TitledPane titledPane, String fxml) {
        try {
            if (titledPane.getContent() == null) {
                ImageManufactureOperationController controller
                        = (ImageManufactureOperationController) WindowTools.loadFxml(fxml);
                titledPane.setContent(controller.getMyScene().getRoot());
                controller.imageController = imageController;
                controller.scopeController = imageController.scopeController;
                controller.operationsController = this;
                controller.imageView = imageController.imageView;
                controller.maskView = imageController.maskView;
                controller.maskPane = imageController.maskPane;
                controller.baseName = imageController.baseName;
                controller.baseTitle = imageController.baseTitle;
                controller.initPane();
                controller.refreshStyle();
                return controller;
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return null;
    }

    public void resetOperationPanes() {
        if (currentController == null) {
            return;
        }
        if (copyController == currentController) {
            copyController.resetOperationPane();
        } else if (clipboardController == currentController) {
            clipboardController.resetOperationPane();
        } else if (cropController == currentController) {
            cropController.resetOperationPane();
        } else if (scaleController == currentController) {
            scaleController.resetOperationPane();
        } else if (colorController == currentController) {
            colorController.resetOperationPane();
        } else if (effectController == currentController) {
            effectController.resetOperationPane();
        } else if (enhancementController == currentController) {
            enhancementController.resetOperationPane();
        } else if (textController == currentController) {
            textController.resetOperationPane();
        } else if (penController == currentController) {
            penController.resetOperationPane();
        } else if (transformController == currentController) {
            transformController.resetOperationPane();
        } else if (arcController == currentController) {
            arcController.resetOperationPane();
        } else if (shadowController == currentController) {
            shadowController.resetOperationPane();
        } else if (marginsController == currentController) {
            marginsController.resetOperationPane();
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (currentController != null) {
            return currentController.keyEventsFilter(event);
        }
        return false;
    }

    @Override
    public BaseImageController refreshInterfaceAndFile() {
        return null;  //Bypass since this is part of frame
    }

    /*
        events passed from image pane
     */
    @Override
    public void imageClicked(MouseEvent event, DoublePoint p) {
        if (currentController != null) {
            currentController.imageClicked(event, p);
        }
    }

    @FXML
    @Override
    public void mousePressed(MouseEvent event) {
        if (currentController != null) {
            currentController.mousePressed(event);
        }
    }

    @FXML
    @Override
    public void mouseDragged(MouseEvent event) {
        if (currentController != null) {
            currentController.mouseDragged(event);
        }
    }

    @FXML
    @Override
    public void mouseReleased(MouseEvent event) {
        if (currentController != null) {
            currentController.mouseReleased(event);
        }
    }

    /*
        get/set
     */
    public ImageManufactureClipboardController getClipboardController() {
        return clipboardController;
    }

    public void setClipboardController(ImageManufactureClipboardController clipboardController) {
        this.clipboardController = clipboardController;
    }

}
