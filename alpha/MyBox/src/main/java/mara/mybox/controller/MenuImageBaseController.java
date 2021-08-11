package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
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

    @FXML
    protected Button imageSizeButton, paneSizeButton, zoomInButton, zoomOutButton,
            rotateLeftButton, rotateRightButton, turnOverButton, infoAction;
    @FXML
    protected CheckBox pickColorCheck, rulerXCheck, rulerYCheck, coordinateCheck, selectAreaCheck;
    @FXML
    protected ComboBox<String> zoomStepSelector, loadWidthBox;

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
            MyBoxLog.debug(e.toString());
        }
    }

    public void setParameters(BaseImageController imageController) {
        try {
            if (imageController == null || imageController.imageView == null
                    || imageController.imageView.getImage() == null) {
                this.closeStage();
                return;
            }
            this.imageController = imageController;
            parentController = imageController;
            baseName = imageController.baseName;

            if (imageController.imageInformation == null) {
                infoButton.setDisable(true);
                metaButton.setDisable(true);
            }

            if (imageController.pickColorCheck != null) {
                pickColorCheck.setSelected(imageController.pickColorCheck.isSelected());
                pickColorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        imageController.pickColorCheck.setSelected(newValue);
                    }
                });
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
                if (imageController.maskRectangleLine == null) {
                    selectAreaCheck.setDisable(true);
                } else if (imageController.selectAreaCheck != null) {
                    selectAreaCheck.setSelected(imageController.selectAreaCheck.isSelected());
                    selectAreaCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                            imageController.selectAreaCheck.setSelected(newValue);
                        }
                    });
                } else {
                    selectAreaCheck.setSelected(UserConfig.getBoolean(baseName + "SelectArea", false));
                    selectAreaCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                            UserConfig.setBoolean(baseName + "SelectArea", newValue);
                            imageController.checkSelect();
                        }
                    });
                }
            }

            if (imageController.coordinateCheck != null) {
                coordinateCheck.setSelected(imageController.coordinateCheck.isSelected());
                coordinateCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        imageController.coordinateCheck.setSelected(newValue);
                    }
                });
            } else {
                coordinateCheck.setSelected(UserConfig.getBoolean(baseName + "PopCooridnate", false));
                coordinateCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "PopCooridnate", coordinateCheck.isSelected());
                        imageController.checkCoordinate();
                    }
                });
            }

            if (imageController.rulerXCheck != null) {
                rulerXCheck.setSelected(imageController.rulerXCheck.isSelected());
                rulerXCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        imageController.rulerXCheck.setSelected(newValue);
                    }
                });
            } else {
                rulerXCheck.setSelected(UserConfig.getBoolean(baseName + "RulerX", false));
                rulerXCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "RulerX", newValue);
                        imageController.checkRulerX();
                    }
                });
            }

            if (imageController.rulerYCheck != null) {
                rulerYCheck.setSelected(imageController.rulerYCheck.isSelected());
                rulerYCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        imageController.rulerYCheck.setSelected(newValue);
                    }
                });
            } else {
                rulerYCheck.setSelected(UserConfig.getBoolean(baseName + "RulerY", false));
                rulerYCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "RulerY", newValue);
                        imageController.checkRulerY();
                    }
                });
            }

            zoomStepSelector.setValue(imageController.zoomStep + "");
            if (imageController.zoomStepSelector != null) {
                zoomStepSelector.getItems().addAll(imageController.zoomStepSelector.getItems());
            } else {
                zoomStepSelector.getItems().addAll(
                        Arrays.asList("40", "20", "5", "1", "3", "15", "30", "50", "80", "100", "150", "200", "300", "500")
                );
            }
            zoomStepSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                    try {
                        int v = Integer.valueOf(newVal);
                        if (v > 0) {
                            zoomStepSelector.getEditor().setStyle(null);
                            if (imageController.zoomStepSelector != null) {
                                imageController.zoomStepSelector.setValue(newVal);
                            } else {
                                imageController.zoomStep = v;
                                imageController.xZoomStep = v;
                                imageController.yZoomStep = v;
                                imageController.zoomStepChanged();
                            }
                        } else {
                            zoomStepSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                        }
                    } catch (Exception e) {
                        zoomStepSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                    }
                }
            });

            setControlsStyle();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        imageController.popFunctionsMenu(mouseEvent);
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
    public void popAction() {
        imageController.popAction();
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

    /*
        static methods
     */
    public static MenuImageBaseController open(BaseImageController imageController, double x, double y) {
        try {
            if (imageController == null) {
                return null;
            }
            Popup popup = PopTools.popWindow(imageController, Fxmls.MenuImageBaseFxml, imageController.imageView, x, y);
            if (popup == null) {
                return null;
            }
            Object object = popup.getUserData();
            if (object == null && !(object instanceof MenuController)) {
                return null;
            }
            MenuImageBaseController controller = (MenuImageBaseController) object;
            controller.setParameters(imageController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
