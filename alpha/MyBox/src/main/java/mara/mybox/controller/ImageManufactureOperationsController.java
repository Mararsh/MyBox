package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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
public class ImageManufactureOperationsController extends BaseController {

    protected ImageManufactureController editor;

    protected ImageManufactureCopyController copyController;
    protected ImageManufactureClipboardController clipboardController;
    protected ImageManufactureCropController cropController;
    protected ImageManufactureScaleController scaleController;
    protected ImageManufactureColorController colorController;
    protected ImageManufactureEffectsController effectController;
    protected ImageManufactureEnhancementController enhancementController;
    protected ImageManufactureTextController textController;
    protected ImageManufactureShapeController shapeController;
    protected ImageManufactureEliminateController eliminateController;
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
            marginsPane, arcPane, shapePane, eliminatePane, textPane, richTextPane;

    public ImageManufactureOperationsController() {
        baseTitle = Languages.message("ImageManufacture");
    }

    public void setParameters(ImageManufactureController parent) {
        this.parentController = parent;
        editor = parent;
        baseName = editor.baseName;
        initPanes();
    }

    public void initPanes() {
        try {
            copyPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    ImageManufactureOperationController controller = loadPane(copyPane, Fxmls.ImageManufactureCopyFxml);
                    if (controller != null) {
                        copyController = (ImageManufactureCopyController) controller;
                    }
                    paneExpanded(copyController, n);
                }
            });

            clipboardPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    ImageManufactureOperationController controller = loadPane(clipboardPane, Fxmls.ImageManufactureClipboardFxml);
                    if (controller != null) {
                        clipboardController = (ImageManufactureClipboardController) controller;
                    }
                    paneExpanded(clipboardController, n);
                }
            });

            cropPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    ImageManufactureOperationController controller = loadPane(cropPane, Fxmls.ImageManufactureCropFxml);
                    if (controller != null) {
                        cropController = (ImageManufactureCropController) controller;
                    }
                    paneExpanded(cropController, n);
                }
            });

            colorPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    ImageManufactureOperationController controller = loadPane(colorPane, Fxmls.ImageManufactureColorFxml);
                    if (controller != null) {
                        colorController = (ImageManufactureColorController) controller;
                    }
                    paneExpanded(colorController, n);
                }
            });

            effectPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    ImageManufactureOperationController controller = loadPane(effectPane, Fxmls.ImageManufactureEffectsFxml);
                    if (controller != null) {
                        effectController = (ImageManufactureEffectsController) controller;
                    }
                    paneExpanded(effectController, n);
                }
            });

            enhancementPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    ImageManufactureOperationController controller = loadPane(enhancementPane, Fxmls.ImageManufactureEnhancementFxml);
                    if (controller != null) {
                        enhancementController = (ImageManufactureEnhancementController) controller;
                    }
                    paneExpanded(enhancementController, n);
                }
            });

            scalePane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    ImageManufactureOperationController controller = loadPane(scalePane, Fxmls.ImageManufactureScaleFxml);
                    if (controller != null) {
                        scaleController = (ImageManufactureScaleController) controller;
                    }
                    paneExpanded(scaleController, n);
                }
            });

            transformPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    ImageManufactureOperationController controller = loadPane(transformPane, Fxmls.ImageManufactureTransformFxml);
                    if (controller != null) {
                        transformController = (ImageManufactureTransformController) controller;
                    }
                    paneExpanded(transformController, n);
                }
            });

            shadowPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    ImageManufactureOperationController controller = loadPane(shadowPane, Fxmls.ImageManufactureShadowFxml);
                    if (controller != null) {
                        shadowController = (ImageManufactureShadowController) controller;
                    }
                    paneExpanded(shadowController, n);
                }
            });

            marginsPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    ImageManufactureOperationController controller = loadPane(marginsPane, Fxmls.ImageManufactureMarginsFxml);
                    if (controller != null) {
                        marginsController = (ImageManufactureMarginsController) controller;
                    }
                    paneExpanded(marginsController, n);
                }
            });

            arcPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    ImageManufactureOperationController controller = loadPane(arcPane, Fxmls.ImageManufactureArcFxml);
                    if (controller != null) {
                        arcController = (ImageManufactureArcController) controller;
                    }
                    paneExpanded(arcController, n);
                }
            });

            shapePane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    ImageManufactureOperationController controller = loadPane(shapePane, Fxmls.ImageManufactureShapeFxml);
                    if (controller != null) {
                        shapeController = (ImageManufactureShapeController) controller;
                    }
                    paneExpanded(shapeController, n);
                }
            });

            eliminatePane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    ImageManufactureOperationController controller = loadPane(eliminatePane, Fxmls.ImageManufactureEliminateFxml);
                    if (controller != null) {
                        eliminateController = (ImageManufactureEliminateController) controller;
                    }
                    paneExpanded(eliminateController, n);
                }
            });

            textPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    ImageManufactureOperationController controller = loadPane(textPane, Fxmls.ImageManufactureTextFxml);
                    if (controller != null) {
                        textController = (ImageManufactureTextController) controller;
                    }
                    paneExpanded(textController, n);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void paneExpanded(ImageManufactureOperationController controller, boolean expanded) {
        if (controller == null) {
            return;
        }
        if (expanded) {
            currentController = controller;
            controller.paneExpanded();
        } else {
            controller.paneUnexpanded();
        }
//        editor.adjustRightPane();
    }

    protected ImageManufactureOperationController loadPane(TitledPane titledPane, String fxml) {
        try {
            if (titledPane.getContent() == null) {
                ImageManufactureOperationController controller
                        = (ImageManufactureOperationController) WindowTools.loadFxml(fxml);
                controller.editor = editor;
                controller.scopeController = editor.scopeController;
                controller.operationsController = this;
                controller.imageView = editor.imageView;
                controller.maskView = editor.maskView;
                controller.maskPane = editor.maskPane;
                controller.baseName = editor.baseName;
                controller.baseTitle = editor.baseTitle;
                controller.initPane();

                VBox box = new VBox();
                box.getChildren().add(controller.getMyScene().getRoot());
                box.setMinHeight(Region.USE_PREF_SIZE);
                box.setPadding(new Insets(2));
                titledPane.setContent(box);
                controller.refreshStyle();
                box.applyCss();
                titledPane.applyCss();
                return controller;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    public void resetOperationPanes() {
        if (currentController == null) {
            return;
        }
        currentController.resetOperationPane();
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
    public void paneClicked(MouseEvent event, DoublePoint p) {
        if (currentController != null) {
            currentController.paneClicked(event, p);
        }
    }

    public void mousePressed(MouseEvent event) {
        if (currentController != null) {
            currentController.mousePressed(event);
        }
    }

    public void mouseDragged(MouseEvent event) {
        if (currentController != null) {
            currentController.mouseDragged(event);
        }
    }

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
