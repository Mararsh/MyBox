package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-9
 * @License Apache License Version 2.0
 */
public class MenuImageBaseController extends MenuController {

    protected BaseImageController imageController;
    protected ChangeListener<Boolean> colorListener, areaListener, coordinateListener,
            rulersListener, gridListener, loadListener;
    protected ChangeListener<String> widthListener, zoomListener;

    @FXML
    protected Button imageSizeButton, paneSizeButton, zoomInButton, zoomOutButton,
            rotateLeftButton, rotateRightButton, turnOverButton, infoAction;
    @FXML
    protected CheckBox pickColorCheck, rulerXCheck, gridCheck, coordinateCheck, selectAreaCheck;
    @FXML
    protected ComboBox<String> zoomStepSelector, loadWidthSelector;

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(zoomStepSelector, new Tooltip(message("ZoomStep")));
            if (selectAreaCheck != null) {
                NodeStyleTools.setTooltip(selectAreaCheck, new Tooltip(message("SelectArea") + "\nCTRL+t"));
            }
            NodeStyleTools.setTooltip(pickColorCheck, new Tooltip(message("PickColor") + "\nCTRL+k"));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setParameters(BaseImageController imageController, double x, double y) {
        try {
            if (imageController == null || imageController.imageView == null
                    || imageController.imageView.getImage() == null) {
                this.closeStage();
                return;
            }
            this.imageController = imageController;
            parentController = imageController;
            baseName = imageController.baseName;

            if (!imageController.canPickColor()) {
                pickColorCheck.setDisable(true);
            } else if (imageController.pickColorCheck != null) {
                pickColorCheck.setSelected(imageController.pickColorCheck.isSelected());
                pickColorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        isSettingValues = true;
                        imageController.pickColorCheck.setSelected(newValue);
                        isSettingValues = false;
                    }
                });
                colorListener = new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        isSettingValues = true;
                        pickColorCheck.setSelected(newValue);
                        isSettingValues = false;
                    }
                };
                imageController.pickColorCheck.selectedProperty().addListener(colorListener);
            } else {
                pickColorCheck.setSelected(imageController.isPickingColor);
                pickColorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        imageController.isPickingColor = newValue;
                        imageController.checkPickingColor();
                    }
                });
            }

            if (selectAreaCheck != null) {
                if (!imageController.canSelect()) {
                    selectAreaCheck.setDisable(true);
                } else if (imageController.selectAreaCheck != null) {
                    selectAreaCheck.setSelected(imageController.selectAreaCheck.isSelected());
                    selectAreaCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                            if (isSettingValues) {
                                return;
                            }
                            isSettingValues = true;
                            imageController.selectAreaCheck.setSelected(newValue);
                            isSettingValues = false;
                        }
                    });
                    areaListener = new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                            if (isSettingValues) {
                                return;
                            }
                            isSettingValues = true;
                            selectAreaCheck.setSelected(newValue);
                            isSettingValues = false;
                        }
                    };
                    imageController.selectAreaCheck.selectedProperty().addListener(areaListener);
                } else {
                    selectAreaCheck.setSelected(UserConfig.getBoolean(baseName + "SelectArea", false));
                    selectAreaCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                            UserConfig.setBoolean(baseName + "SelectArea", newValue);
                            imageController.finalRefineView();
                        }
                    });
                }
            }

            if (imageController.coordinateCheck != null) {
                coordinateCheck.setSelected(imageController.coordinateCheck.isSelected());
                coordinateCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        isSettingValues = true;
                        imageController.coordinateCheck.setSelected(newValue);
                        isSettingValues = false;
                    }
                });
                coordinateListener = new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        isSettingValues = true;
                        coordinateCheck.setSelected(newValue);
                        isSettingValues = false;
                    }
                };
                imageController.coordinateCheck.selectedProperty().addListener(coordinateListener);
            } else {
                coordinateCheck.setSelected(UserConfig.getBoolean("ImagePopCooridnate", false));
                coordinateCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean("ImagePopCooridnate", coordinateCheck.isSelected());
                        imageController.checkCoordinate();
                    }
                });
            }

            if (imageController.rulerXCheck != null) {
                rulerXCheck.setSelected(imageController.rulerXCheck.isSelected());
                rulerXCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        isSettingValues = true;
                        imageController.rulerXCheck.setSelected(newValue);
                        isSettingValues = false;
                    }
                });
                rulersListener = new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        isSettingValues = true;
                        rulerXCheck.setSelected(newValue);
                        isSettingValues = false;
                    }
                };
                imageController.rulerXCheck.selectedProperty().addListener(rulersListener);
            } else {
                rulerXCheck.setSelected(UserConfig.getBoolean("ImageRulerXY", false));
                rulerXCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean("ImageRulerXY", newValue);
                        imageController.drawMaskRulers();
                    }
                });
            }

            if (imageController.gridCheck != null) {
                gridCheck.setSelected(imageController.gridCheck.isSelected());
                gridCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        isSettingValues = true;
                        imageController.gridCheck.setSelected(newValue);
                        isSettingValues = false;
                    }
                });
                gridListener = new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        isSettingValues = true;
                        gridCheck.setSelected(newValue);
                        isSettingValues = false;
                    }
                };
                imageController.gridCheck.selectedProperty().addListener(gridListener);
            } else {
                gridCheck.setSelected(UserConfig.getBoolean("ImageGridLines", false));
                gridCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean("ImageGridLines", newValue);
                        imageController.drawMaskGrid();
                    }
                });
            }

            if (imageController.zoomStepSelector != null) {
                zoomStepSelector.getItems().addAll(imageController.zoomStepSelector.getItems());
                zoomListener = new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        isSettingValues = true;
                        zoomStepSelector.setValue(imageController.zoomStepSelector.getValue());
                        isSettingValues = false;
                    }
                };
                imageController.zoomStepSelector.getSelectionModel().selectedItemProperty().addListener(zoomListener);
            } else {
                zoomStepSelector.getItems().addAll(
                        Arrays.asList("40", "20", "5", "1", "3", "15", "30", "50", "80", "100", "150", "200", "300", "500")
                );
            }
            zoomStepSelector.setValue(imageController.zoomStep + "");
            zoomStepSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        int v = Integer.parseInt(newVal);
                        if (v > 0) {
                            zoomStepSelector.getEditor().setStyle(null);
                            isSettingValues = true;
                            if (imageController.zoomStepSelector != null) {
                                imageController.zoomStepSelector.setValue(newVal);
                            } else {
                                imageController.zoomStep = v;
                                imageController.xZoomStep = v;
                                imageController.yZoomStep = v;
                                imageController.zoomStepChanged();
                            }
                            isSettingValues = false;
                        } else {
                            zoomStepSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        zoomStepSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            if (loadWidthSelector != null) {
                if (imageController.loadWidthSelector != null) {
                    loadWidthSelector.getItems().addAll(imageController.loadWidthSelector.getItems());
                    loadWidthSelector.setValue(imageController.loadWidthSelector.getValue());
                    widthListener = new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue ov, String oldValue, String newValue) {
                            if (isSettingValues) {
                                return;
                            }
                            isSettingValues = true;
                            loadWidthSelector.setValue(imageController.loadWidthSelector.getValue());
                            isSettingValues = false;
                        }
                    };
                    imageController.loadWidthSelector.getSelectionModel().selectedItemProperty().addListener(widthListener);
                } else {
                    loadWidthSelector.getItems().addAll(Arrays.asList(message("OriginalSize"),
                            "512", "1024", "256", "128", "2048", "100", "80", "4096")
                    );
                    loadWidthSelector.setValue(imageController.loadWidth > 0 ? imageController.loadWidth + "" : message("OriginalSize"));
                }
                loadWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        int v = -1;
                        if (!message("OriginalSize").equals(newValue)) {
                            try {
                                v = Integer.parseInt(newValue);
                                ValidationTools.setEditorNormal(loadWidthSelector);
                            } catch (Exception e) {
                                ValidationTools.setEditorBadStyle(loadWidthSelector);
                                return;
                            }
                        }
                        isSettingValues = true;
                        if (imageController.loadWidthSelector != null) {
                            imageController.loadWidthSelector.setValue(newValue);
                        } else {
                            imageController.setLoadWidth(v);
                        }
                        isSettingValues = false;
                    }
                });
            }

            super.setParameters(imageController, imageController.imageView, x, y);

            loadListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    updateImage();
                }
            };
            imageController.loadNotify.addListener(loadListener);
            updateImage();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void updateImage() {
        sourceFile = imageController.sourceFile;
        boolean noInfo = imageController.imageInformation == null;
        if (infoButton != null) {
            infoButton.setDisable(noInfo);
        }
        if (metaButton != null) {
            metaButton.setDisable(noInfo);
        }
        if (renameButton != null) {
            renameButton.setDisable(noInfo);
        }
        if (openSourceButton != null) {
            openSourceButton.setDisable(sourceFile == null || !sourceFile.exists());
        }
        if (getMyStage() != null) {
            myStage.setTitle(imageController.getTitle());
        }
    }

    @FXML
    public void popFunctionsMenu(Event event) {
        if (UserConfig.getBoolean("ImageFunctionsPopWhenMouseHovering", true)) {
            showFunctionsMenu(event);
        }
    }

    @FXML
    public void showFunctionsMenu(Event event) {
        imageController.showFunctionsMenu(event);
    }

    @FXML
    @Override
    public void selectSourceFile() {
        imageController.selectSourceFile();
    }

    @FXML
    @Override
    public void popSourceFile(Event event) {
        imageController.popSourceFile(event);
    }

    @FXML
    public void zoomOut() {
        imageController.zoomOut();
    }

    @FXML
    public void zoomIn() {
        imageController.zoomIn();
    }

    @FXML
    public void paneSize() {
        imageController.paneSize();
    }

    @FXML
    public void loadedSize() {
        imageController.loadedSize();
    }

    @FXML
    @Override
    public void copyToSystemClipboard() {
        imageController.copyToSystemClipboard();
    }

    @FXML
    @Override
    public void copyToMyBoxClipboard() {
        imageController.copyToMyBoxClipboard();
    }

    @FXML
    @Override
    public void systemClipBoard() {
        imageController.systemClipBoard();
    }

    @FXML
    @Override
    public void myBoxClipBoard() {
        imageController.myBoxClipBoard();
    }

    @FXML
    @Override
    public boolean popAction() {
        return imageController.popAction();
    }

    @FXML
    public void manufactureAction() {
        imageController.manufactureAction();
    }

    @FXML
    @Override
    public void saveAsAction() {
        imageController.saveAsAction();
    }

    @FXML
    @Override
    public void infoAction() {
        imageController.infoAction();
    }

    @FXML
    public void metaAction() {
        imageController.metaAction();
    }

    @FXML
    public void settings() {
        imageController.settings();
    }

    @Override
    public void cleanPane() {
        try {
            if (imageController != null) {
                imageController.loadNotify.removeListener(loadListener);
                imageController.loadWidthSelector.getSelectionModel().selectedItemProperty().removeListener(widthListener);
                imageController.zoomStepSelector.getSelectionModel().selectedItemProperty().removeListener(zoomListener);
                imageController.gridCheck.selectedProperty().removeListener(gridListener);
                imageController.rulerXCheck.selectedProperty().removeListener(rulersListener);
                imageController.coordinateCheck.selectedProperty().removeListener(coordinateListener);
                imageController.selectAreaCheck.selectedProperty().removeListener(areaListener);
                imageController.pickColorCheck.selectedProperty().removeListener(colorListener);
            }
            loadListener = null;
            widthListener = null;
            zoomListener = null;
            gridListener = null;
            rulersListener = null;
            coordinateListener = null;
            areaListener = null;
            colorListener = null;
            imageController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static methods
     */
    public static MenuImageBaseController open(BaseImageController imageController, double x, double y) {
        try {
            if (imageController == null) {
                return null;
            }
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                Object object = window.getUserData();
                if (object != null && object instanceof MenuImageBaseController) {
                    try {
                        MenuImageBaseController controller = (MenuImageBaseController) object;
                        if (controller.imageController.equals(imageController)) {
                            controller.close();
                        }
                    } catch (Exception e) {
                    }
                }
            }
            MenuImageBaseController controller = (MenuImageBaseController) WindowTools.openChildStage(
                    imageController.getMyWindow(), Fxmls.MenuImageBaseFxml, false);
            controller.setParameters(imageController, x, y);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
