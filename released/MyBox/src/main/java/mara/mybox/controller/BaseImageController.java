package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.FileExtensions;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @License Apache License Version 2.0
 *
 * BaseImageController < BaseImageController_Actions < BaseImageController_Image
 * < BaseImageController_MouseEvents < BaseImageController_Mask <
 * BaseImageController_Base
 */
public class BaseImageController extends BaseImageController_Actions {

    @FXML
    protected FlowPane buttonsPane;

    public BaseImageController() {
        baseTitle = message("Image");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            isPickingColor = imageChanged = false;
            loadWidth = defaultLoadWidth = -1;
            frameIndex = framesNumber = 0;
            sizeChangeAware = 1;
            zoomStep = xZoomStep = yZoomStep = 20;
            if (maskPane != null) {
                if (borderLine == null) {
                    borderLine = new Rectangle();
                    borderLine.setFill(Color.web("#ffffff00"));
                    borderLine.setStroke(Color.web("#cccccc"));
                    borderLine.setArcWidth(5);
                    borderLine.setArcHeight(5);
                    maskPane.getChildren().add(borderLine);
                }
                if (sizeText == null) {
                    sizeText = new Text();
                    sizeText.setFill(Color.web("#cccccc"));
                    sizeText.setStrokeWidth(0);
                    maskPane.getChildren().add(sizeText);
                }
                if (xyText == null) {
                    xyText = new Text();
                    maskPane.getChildren().add(xyText);
                }
            }
            if (mainAreaBox == null) {
                mainAreaBox = imageBox;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image);
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            if (zoomStepSelector != null) {
                NodeStyleTools.setTooltip(zoomStepSelector, new Tooltip(message("ZoomStep")));
            }
            if (pickColorCheck != null) {
                NodeStyleTools.setTooltip(pickColorCheck, new Tooltip(message("PickColor") + "\nCTRL+k"));
            }
            if (loadWidthSelector != null) {
                NodeStyleTools.setTooltip(loadWidthSelector, new Tooltip(message("ImageLoadWidthCommnets")));
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initImageView();
            initMaskPane();
            initCheckboxs();

            if (imageBox != null && imageView != null) {
                imageBox.disableProperty().bind(imageView.imageProperty().isNull());
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initImageView() {
        if (imageView == null) {
            return;
        }
        try {
            imageView.setPreserveRatio(true);

            imageView.fitWidthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    viewSizeChanged(Math.abs(new_val.doubleValue() - old_val.doubleValue()));
                }
            });

            imageView.fitHeightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    viewSizeChanged(Math.abs(new_val.doubleValue() - old_val.doubleValue()));
                }
            });

            if (scrollPane != null) {
                scrollPane.widthProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                        paneSizeChanged(Math.abs(new_val.doubleValue() - old_val.doubleValue()));
                    }
                });
                scrollPane.heightProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                        paneSizeChanged(Math.abs(new_val.doubleValue() - old_val.doubleValue()));
                    }
                });

            }

            zoomStep = UserConfig.getInt(baseName + "ZoomStep", 40);
            zoomStep = zoomStep <= 0 ? 40 : zoomStep;
            xZoomStep = zoomStep;
            yZoomStep = zoomStep;
            if (zoomStepSelector != null) {
                zoomStepSelector.getItems().addAll(
                        Arrays.asList("40", "20", "5", "1", "3", "15", "30", "50", "80", "100", "150", "200", "300", "500")
                );
                zoomStepSelector.setValue(zoomStep + "");
                zoomStepSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                        try {
                            int v = Integer.parseInt(newVal);
                            if (v > 0) {
                                zoomStep = v;
                                UserConfig.setInt(baseName + "ZoomStep", zoomStep);
                                zoomStepSelector.getEditor().setStyle(null);
                                xZoomStep = zoomStep;
                                yZoomStep = zoomStep;
                                zoomStepChanged();
                            } else {
                                zoomStepSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            zoomStepSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    }
                });
            }

            loadWidth = defaultLoadWidth;
            if (loadWidthSelector != null) {
                List<String> values = Arrays.asList(message("OriginalSize"),
                        "512", "1024", "256", "128", "2048", "100", "80", "4096");
                loadWidthSelector.getItems().addAll(values);
                int v = UserConfig.getInt(baseName + "LoadWidth", defaultLoadWidth);
                if (v <= 0) {
                    loadWidth = -1;
                    loadWidthSelector.getSelectionModel().select(0);
                } else {
                    loadWidth = v;
                    loadWidthSelector.setValue(v + "");
                }
                loadWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (message("OriginalSize").equals(newValue)) {
                            loadWidth = -1;
                        } else {
                            try {
                                loadWidth = Integer.parseInt(newValue);
                            } catch (Exception e) {
                                ValidationTools.setEditorBadStyle(loadWidthSelector);
                                return;
                            }
                        }
                        ValidationTools.setEditorNormal(loadWidthSelector);
                        setLoadWidth();
                    }
                });
            }

            if (imageView == null) {
                return;
            }
            if (buttonsPane != null) {
                buttonsPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }
            if (scrollPane != null) {
                scrollPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }

            if (saveButton != null) {
                saveButton.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initCheckboxs() {
        try {
            if (pickColorCheck != null) {
                pickColorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                        isPickingColor = pickColorCheck.isSelected();
                        checkPickingColor();
                    }
                });
            }

            if (rulerXCheck != null) {
                rulerXCheck.setSelected(UserConfig.getBoolean("ImageRulerXY", false));
                rulerXCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean("ImageRulerXY", rulerXCheck.isSelected());
                        drawMaskRulers();
                    }
                });
            }
            if (gridCheck != null) {
                gridCheck.setSelected(UserConfig.getBoolean("ImageGridLines", false));
                gridCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean("ImageGridLines", gridCheck.isSelected());
                        drawMaskGrid();
                    }
                });
            }

            if (coordinateCheck != null) {
                coordinateCheck.setSelected(UserConfig.getBoolean("ImagePopCooridnate", false));
                coordinateCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean("ImagePopCooridnate", coordinateCheck.isSelected());
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public List<MenuItem> fileMenuItems(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (sourceFile != null) {
                String fileFormat = FileNameTools.ext(sourceFile.getName()).toLowerCase();
                if (FileExtensions.MultiFramesImages.contains(fileFormat)) {
                    menu = new MenuItem(message("Frames"), StyleTools.getIconImageView("iconFrame.png"));
                    menu.setOnAction((ActionEvent menuItemEvent) -> {
                        ImageFramesController.open(this);
                    });
                    items.add(menu);
                }

                if (imageInformation != null) {
                    menu = new MenuItem(message("Information") + "    Ctrl+I " + message("Or") + " Alt+I",
                            StyleTools.getIconImageView("iconInfo.png"));
                    menu.setOnAction((ActionEvent menuItemEvent) -> {
                        infoAction();
                    });
                    items.add(menu);

                    menu = new MenuItem(message("MetaData"), StyleTools.getIconImageView("iconMeta.png"));
                    menu.setOnAction((ActionEvent menuItemEvent) -> {
                        metaAction();
                    });
                    items.add(menu);
                }

                menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
                menu.setOnAction((ActionEvent event) -> {
                    refreshAction();
                });
                items.add(menu);

                items.add(new SeparatorMenuItem());

            }

            menu = new MenuItem(message("Create") + "    Ctrl+N " + message("Or") + " Alt+N",
                    StyleTools.getIconImageView("iconAdd.png"));
            menu.setOnAction((ActionEvent event) -> {
                createAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Example"), StyleTools.getIconImageView("iconExamples.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                exampleAction();
            });
            items.add(menu);

            boolean imageShown = imageView != null && imageView.getImage() != null;

            menu = new MenuItem(message("LoadContentInSystemClipboard")
                    + (imageShown ? "" : ("    Ctrl+V " + message("Or") + " Alt+V")),
                    StyleTools.getIconImageView("iconImageSystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                loadContentInSystemClipboard();
            });
            items.add(menu);

            menu = new MenuItem(message("LoadWidth") + ": "
                    + (loadWidth <= 0 ? message("OriginalSize") : ("" + loadWidth)),
                    StyleTools.getIconImageView("iconInput.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageLoadWidthController.open(this);
            });
            items.add(menu);

            if (imageShown) {
                menu = new MenuItem(message("SaveAs") + "    Ctrl+B " + message("Or") + " Alt+B",
                        StyleTools.getIconImageView("iconSaveAs.png"));
                menu.setOnAction((ActionEvent event) -> {
                    saveAsAction();
                });
                items.add(menu);
            }

            if (sourceFile == null) {
                return items;
            }
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("OpenDirectory"), StyleTools.getIconImageView("iconOpenPath.png"));
            menu.setOnAction((ActionEvent event) -> {
                openSourcePath();
            });
            items.add(menu);

            menu = new MenuItem(message("ImagesBrowser"), StyleTools.getIconImageView("iconBrowse.png"));
            menu.setOnAction((ActionEvent event) -> {
                browseAction();
            });
            items.add(menu);

            menu = new MenuItem(message("BrowseFiles"), StyleTools.getIconImageView("iconList.png"));
            menu.setOnAction((ActionEvent event) -> {
                FileBrowseController.open(this);
            });
            items.add(menu);

            menu = new MenuItem(message("SystemMethod"), StyleTools.getIconImageView("iconSystemOpen.png"));
            menu.setOnAction((ActionEvent event) -> {
                systemMethod();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Rename"), StyleTools.getIconImageView("iconInput.png"));
            menu.setOnAction((ActionEvent event) -> {
                renameAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Delete") + "    Ctrl+D " + message("Or") + " Alt+D",
                    StyleTools.getIconImageView("iconDelete.png"));
            menu.setOnAction((ActionEvent event) -> {
                deleteAction();
            });
            items.add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public List<MenuItem> functionsMenuItems(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("Edit"), StyleTools.getIconImageView("iconEdit.png"));
            menu.setOnAction((ActionEvent event) -> {
                editAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Pop") + "    Ctrl+P " + message("Or") + " Alt+P",
                    StyleTools.getIconImageView("iconPop.png"));
            menu.setOnAction((ActionEvent event) -> {
                popAction();
            });
            items.add(menu);

            menu = new MenuItem(message("SelectPixels") + "    Ctrl+T " + message("Or") + " Alt+T",
                    StyleTools.getIconImageView("iconTarget.png"));
            menu.setOnAction((ActionEvent event) -> {
                selectPixels();
            });
            items.add(menu);

            menu = new MenuItem(message("Statistic"), StyleTools.getIconImageView("iconStatistic.png"));
            menu.setOnAction((ActionEvent event) -> {
                statisticAction();

            });
            items.add(menu);

            menu = new MenuItem(message("OCR"), StyleTools.getIconImageView("iconTxt.png"));
            menu.setOnAction((ActionEvent event) -> {
                ocrAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Split"), StyleTools.getIconImageView("iconSplit.png"));
            menu.setOnAction((ActionEvent event) -> {
                splitAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Sample"), StyleTools.getIconImageView("iconSample.png"));
            menu.setOnAction((ActionEvent event) -> {
                sampleAction();

            });
            items.add(menu);

            menu = new MenuItem(message("Repeat"), StyleTools.getIconImageView("iconRepeat.png"));
            menu.setOnAction((ActionEvent event) -> {
                repeatAction();
            });
            items.add(menu);

            if (sourceFile != null) {
                menu = new MenuItem("SVG", StyleTools.getIconImageView("iconSVG.png"));
                menu.setOnAction((ActionEvent event) -> {
                    svgAction();
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("ManageColors"), StyleTools.getIconImageView("iconPalette.png"));
            menu.setOnAction((ActionEvent event) -> {
                ColorsManageController.oneOpen();
            });
            items.add(menu);

            menu = new MenuItem(message("QueryColor"), StyleTools.getIconImageView("iconColor.png"));
            menu.setOnAction((ActionEvent event) -> {
                ColorQueryController.open();
            });
            items.add(menu);

            if (mainMenuController == null) {
                menu = new MenuItem(message("MainPageShortcut"), StyleTools.getIconImageView("iconMyBox.png"));
                menu.setOnAction((ActionEvent event) -> {
                    mybox();
                });
                items.add(menu);
            }

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public List<MenuItem> operationsMenuItems(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("Paste") + "    Ctrl+V " + message("Or") + " Alt+V",
                    StyleTools.getIconImageView("iconPaste.png"));
            menu.setOnAction((ActionEvent event) -> {
                controlAltV();
            });
            items.add(menu);

            menu = new MenuItem(message("RotateRight"), StyleTools.getIconImageView("iconRotateRight.png"));
            menu.setOnAction((ActionEvent event) -> {
                rotateRight();
            });
            items.add(menu);

            menu = new MenuItem(message("RotateLeft"), StyleTools.getIconImageView("iconRotateLeft.png"));
            menu.setOnAction((ActionEvent event) -> {
                rotateLeft();
            });
            items.add(menu);

            menu = new MenuItem(message("TurnOver"), StyleTools.getIconImageView("iconTurnOver.png"));
            menu.setOnAction((ActionEvent event) -> {
                turnOver();
            });
            items.add(menu);

            menu = new MenuItem(message("MirrorHorizontal"), StyleTools.getIconImageView("iconHorizontal.png"));
            menu.setOnAction((ActionEvent event) -> {
                horizontalAction();
            });
            items.add(menu);

            menu = new MenuItem(message("MirrorVertical"), StyleTools.getIconImageView("iconVertical.png"));
            menu.setOnAction((ActionEvent event) -> {
                verticalAction();
            });
            items.add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Menu copyMenu(Event fevent) {
        try {
            Menu copyMenu = new Menu(message("Copy"), StyleTools.getIconImageView("iconCopy.png"));

            MenuItem menu = new MenuItem(message("Copy"), StyleTools.getIconImageView("iconCopy.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageCopyController.open(this);
            });
            copyMenu.getItems().add(menu);

            menu = new MenuItem(message("CopyToSystemClipboard") + "    Ctrl+C " + message("Or") + " Alt+C",
                    StyleTools.getIconImageView("iconCopySystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                copyToSystemClipboard();
            });
            copyMenu.getItems().add(menu);

            menu = new MenuItem(message("CopyToMyBoxClipboard"), StyleTools.getIconImageView("iconCopy.png"));
            menu.setOnAction((ActionEvent event) -> {
                copyToMyBoxClipboard();
            });
            copyMenu.getItems().add(menu);

            menu = new MenuItem(message("ImagesInSystemClipboard"), StyleTools.getIconImageView("iconSystemClipboard.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageInSystemClipboardController.oneOpen();
            });
            copyMenu.getItems().add(menu);

            menu = new MenuItem(message("ImagesInMyBoxClipboard"), StyleTools.getIconImageView("iconClipboard.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImageInMyBoxClipboardController.oneOpen();

            });
            copyMenu.getItems().add(menu);

            return copyMenu;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public List<MenuItem> viewMenuItems(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("PickColors") + "    Ctrl+K " + message("Or") + " Alt+K",
                    StyleTools.getIconImageView("iconPickColor.png"));
            menu.setOnAction((ActionEvent event) -> {
                controlAltK();
            });
            items.add(menu);

            items.add(copyMenu(fevent));

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("LoadedSize") + "    Ctrl+1 " + message("Or") + " Alt+1",
                    StyleTools.getIconImageView("iconLoadSize.png"));
            menu.setOnAction((ActionEvent event) -> {
                loadedSize();
            });
            items.add(menu);

            menu = new MenuItem(message("PaneSize") + "    Ctrl+2 " + message("Or") + " Alt+2",
                    StyleTools.getIconImageView("iconPaneSize.png"));
            menu.setOnAction((ActionEvent event) -> {
                paneSize();
            });
            items.add(menu);

            menu = new MenuItem(message("ZoomIn") + "    Ctrl+3 " + message("Or") + " Alt+3",
                    StyleTools.getIconImageView("iconZoomIn.png"));
            menu.setOnAction((ActionEvent event) -> {
                zoomIn();
            });
            items.add(menu);

            menu = new MenuItem(message("ZoomOut") + "    Ctrl+4 " + message("Or") + " Alt+4",
                    StyleTools.getIconImageView("iconZoomOut.png"));
            menu.setOnAction((ActionEvent event) -> {
                zoomOut();
            });
            items.add(menu);

            CheckMenuItem reulersItem = new CheckMenuItem(message("Rulers"), StyleTools.getIconImageView("iconXRuler.png"));
            reulersItem.setSelected(UserConfig.getBoolean("ImageRulerXY", false));
            reulersItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent cevent) {
                    if (rulerXCheck != null) {
                        rulerXCheck.setSelected(!rulerXCheck.isSelected());
                    } else {
                        UserConfig.setBoolean("ImageRulerXY", reulersItem.isSelected());
                        drawMaskRulers();
                    }
                }
            });
            items.add(reulersItem);

            CheckMenuItem gridItem = new CheckMenuItem(message("GridLines"), StyleTools.getIconImageView("iconGrid.png"));
            gridItem.setSelected(UserConfig.getBoolean("ImageGridLines", false));
            gridItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent cevent) {
                    if (gridCheck != null) {
                        gridCheck.setSelected(!gridCheck.isSelected());
                    } else {
                        UserConfig.setBoolean("ImageGridLines", gridItem.isSelected());
                        drawMaskGrid();
                    }
                }
            });
            items.add(gridItem);

            CheckMenuItem coordItem = new CheckMenuItem(message("Coordinate"), StyleTools.getIconImageView("iconLocation.png"));
            coordItem.setSelected(UserConfig.getBoolean("ImagePopCooridnate", false));
            coordItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent cevent) {
                    if (coordinateCheck != null) {
                        coordinateCheck.setSelected(!coordinateCheck.isSelected());
                    } else {
                        UserConfig.setBoolean("ImagePopCooridnate", coordItem.isSelected());
                        drawMaskGrid();
                    }
                }
            });
            items.add(coordItem);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("ContextMenu") + "    F6", StyleTools.getIconImageView("iconMenu.png"));
            menu.setOnAction((ActionEvent event) -> {
                popContextMenu(event);
            });
            items.add(menu);

            menu = new MenuItem(message("Options"), StyleTools.getIconImageView("iconOptions.png"));
            menu.setOnAction((ActionEvent event) -> {
                options();
            });
            items.add(menu);

            if (TipsLabelKey != null) {
                menu = new MenuItem(message("Tips"), StyleTools.getIconImageView("iconTips.png"));
                menu.setOnAction((ActionEvent event) -> {
                    TextPopController.loadText(message(TipsLabelKey));
                });
                items.add(menu);
            }

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public boolean controlAltN() {
        createAction();
        return true;
    }

    @Override
    public boolean controlAltC() {
        if (imageView == null || imageView.getImage() == null
                || targetIsTextInput()) {
            return false;
        }
        copyToSystemClipboard();
        return true;

    }

    @Override
    public boolean controlAltV() {
        if (imageView == null || targetIsTextInput()) {
            return false;
        }
        if (imageView.getImage() != null) {
            pasteAction();
        } else {
            loadContentInSystemClipboard();
        }
        return true;
    }

    @Override
    public boolean controlAltS() {
        if (imageView == null || imageView.getImage() == null) {
            return false;
        }
        saveAction();
        return true;
    }

    @Override
    public boolean controlAltB() {
        if (imageView == null || imageView.getImage() == null) {
            return false;
        }
        saveAsAction();
        return true;
    }

    @Override
    public boolean controlAltK() {
        if (imageView == null || imageView.getImage() == null) {
            return false;
        }
        if (pickColorCheck != null) {
            pickColorCheck.setSelected(!pickColorCheck.isSelected());
            return true;
        } else if (imageView != null && imageView.getImage() != null) {
            isPickingColor = !isPickingColor;
            checkPickingColor();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean controlAltT() {
        if (imageView == null || imageView.getImage() == null) {
            return false;
        }
        selectPixels();
        return true;
    }

    @Override
    public boolean controlAltD() {
        if (imageView == null || imageView.getImage() == null
                || targetIsTextInput() || sourceFile == null) {
            return false;
        }
        deleteAction();
        return true;
    }

    @Override
    public boolean controlAlt1() {
        if (imageView == null || imageView.getImage() == null) {
            return false;
        }
        loadedSize();
        return true;
    }

    @Override
    public boolean controlAlt2() {
        if (imageView == null || imageView.getImage() == null) {
            return false;
        }
        paneSize();
        return true;
    }

    @Override
    public boolean controlAlt3() {
        if (imageView == null || imageView.getImage() == null) {
            return false;
        }
        zoomIn();
        return true;
    }

    @Override
    public boolean controlAlt4() {
        if (imageView == null || imageView.getImage() == null) {
            return false;
        }
        zoomOut();
        return true;
    }

    @Override
    public void cleanPane() {
        try {
            if (loadTask != null) {
                loadTask.cancel();
                loadTask = null;
            }
            if (paletteController != null) {
                paletteController.closeStage();
                paletteController = null;
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
