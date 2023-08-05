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
            accordionPane.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
                @Override
                public void changed(ObservableValue<? extends TitledPane> v, TitledPane o, TitledPane n) {
                    paneSwitched(n);
                }
            });

            copyPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    if (!n && copyController != null) {
                        copyController.paneUnexpanded();
                    }
                }
            });

            clipboardPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    if (!n && clipboardController != null) {
                        clipboardController.paneUnexpanded();
                    }
                }
            });

            cropPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    if (!n && cropController != null) {
                        cropController.paneUnexpanded();
                    }
                }
            });

            colorPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    if (!n && colorController != null) {
                        colorController.paneUnexpanded();
                    }
                }
            });

            effectPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    if (!n && effectController != null) {
                        effectController.paneUnexpanded();
                    }
                }
            });

            enhancementPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    if (!n && enhancementController != null) {
                        enhancementController.paneUnexpanded();
                    }
                }
            });

            scalePane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    if (!n && scaleController != null) {
                        scaleController.paneUnexpanded();
                    }
                }
            });

            transformPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    if (!n && transformController != null) {
                        transformController.paneUnexpanded();
                    }
                }
            });

            shadowPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    if (!n && shadowController != null) {
                        shadowController.paneUnexpanded();
                    }
                }
            });

            marginsPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    if (!n && marginsController != null) {
                        marginsController.paneUnexpanded();
                    }
                }
            });

            arcPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    if (!n && arcController != null) {
                        arcController.paneUnexpanded();
                    }
                }
            });

            shapePane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    if (!n && shapeController != null) {
                        shapeController.paneUnexpanded();
                    }
                }
            });

            eliminatePane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    if (!n && eliminateController != null) {
                        eliminateController.paneUnexpanded();
                    }
                }
            });

            textPane.expandedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean o, Boolean n) {
                    if (!n && textController != null) {
                        textController.paneUnexpanded();
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void paneSwitched(TitledPane currentPane) {
        try {
            if (currentPane == null) {
                editor.resetImagePane();

            } else if (currentPane == copyPane) {
                if (copyController == null) {
                    copyController = (ImageManufactureCopyController) loadPane(
                            copyPane, Fxmls.ImageManufactureCopyFxml);
                }
                copyController.paneExpanded();

            } else if (currentPane == clipboardPane) {
                if (clipboardController == null) {
                    clipboardController = (ImageManufactureClipboardController) loadPane(
                            clipboardPane, Fxmls.ImageManufactureClipboardFxml);
                }
                clipboardController.paneExpanded();

            } else if (currentPane == cropPane) {
                if (cropController == null) {
                    cropController = (ImageManufactureCropController) loadPane(
                            cropPane, Fxmls.ImageManufactureCropFxml);
                }
                cropController.paneExpanded();

            } else if (currentPane == scalePane) {
                if (scaleController == null) {
                    scaleController = (ImageManufactureScaleController) loadPane(
                            scalePane, Fxmls.ImageManufactureScaleFxml);
                }
                scaleController.paneExpanded();

            } else if (currentPane == colorPane) {
                if (colorController == null) {
                    colorController = (ImageManufactureColorController) loadPane(
                            colorPane, Fxmls.ImageManufactureColorFxml);
                }
                colorController.paneExpanded();

            } else if (currentPane == effectPane) {
                if (effectController == null) {
                    effectController = (ImageManufactureEffectsController) loadPane(
                            effectPane, Fxmls.ImageManufactureEffectsFxml);
                }
                effectController.paneExpanded();

            } else if (currentPane == enhancementPane) {
                if (enhancementController == null) {
                    enhancementController = (ImageManufactureEnhancementController) loadPane(
                            enhancementPane, Fxmls.ImageManufactureEnhancementFxml);
                }
                enhancementController.paneExpanded();

            } else if (currentPane == transformPane) {
                if (transformController == null) {
                    transformController = (ImageManufactureTransformController) loadPane(
                            transformPane, Fxmls.ImageManufactureTransformFxml);
                }
                transformController.paneExpanded();

            } else if (currentPane == shadowPane) {
                if (shadowController == null) {
                    shadowController = (ImageManufactureShadowController) loadPane(
                            shadowPane, Fxmls.ImageManufactureShadowFxml);
                }
                shadowController.paneExpanded();

            } else if (currentPane == marginsPane) {
                if (marginsController == null) {
                    marginsController = (ImageManufactureMarginsController) loadPane(
                            marginsPane, Fxmls.ImageManufactureMarginsFxml);
                }
                marginsController.paneExpanded();

            } else if (currentPane == arcPane) {
                if (arcController == null) {
                    arcController = (ImageManufactureArcController) loadPane(
                            arcPane, Fxmls.ImageManufactureArcFxml);
                }
                arcController.paneExpanded();

            } else if (currentPane == shapePane) {
                if (shapeController == null) {
                    shapeController = (ImageManufactureShapeController) loadPane(
                            shapePane, Fxmls.ImageManufactureShapeFxml);

                }
                shapeController.paneExpanded();

            } else if (currentPane == eliminatePane) {
                if (eliminateController == null) {
                    eliminateController = (ImageManufactureEliminateController) loadPane(
                            eliminatePane, Fxmls.ImageManufactureEliminateFxml);
                }
                eliminateController.paneExpanded();

            } else if (currentPane == textPane) {
                if (textController == null) {
                    textController = (ImageManufactureTextController) loadPane(
                            textPane, Fxmls.ImageManufactureTextFxml);
                }
                textController.paneExpanded();

            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
