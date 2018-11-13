package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.imageio.ImageIO;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.db.TableImageHistory;
import mara.mybox.db.TableImageInit;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageHistory;
import mara.mybox.objects.ImageManufactureValues;
import mara.mybox.objects.ImageScope;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.fxml.FxmlTools;
import mara.mybox.fxml.FxmlImageTools;
import mara.mybox.fxml.FxmlScopeTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.objects.Rectangle;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class ImageManufactureController extends ImageViewerController {

    protected ScrollPane refPane, scopePane;
    protected ImageView refView, scopeView;
    protected Label refLabel;
    protected VBox refBox, scopeBox;

    protected ImageManufactureValues values;
    protected boolean isSettingValues, isSwitchingTab;

    protected String initTab;
    protected TextField scopeText;
    protected int stageWidth, stageHeight;
    protected String imageHistoriesPath;
    protected List<String> imageHistories;

    protected ImageScope scope;
    protected String scopeColorString, scopeAllString;

    public static class ImageOperationType {

        public static int Load = 0;
        public static int Arc = 1;
        public static int Color = 2;
        public static int Crop = 3;
        public static int Text = 4;
        public static int Effects = 5;
        public static int Filters = 6;
        public static int Replace_Color = 7;
        public static int Shadow = 8;
        public static int Size = 9;
        public static int Transform = 10;
        public static int Cut_Margins = 11;
        public static int Add_Margins = 12;
        public static int Cover = 13;
        public static int Convolution = 14;
    }

    protected class ImageManufactureParameters {

    }

    @FXML
    protected ToolBar hotBar;
    @FXML
    protected Tab fileTab, viewTab, colorTab, filtersTab, textTab, coverTab, cropTab,
            arcTab, shadowTab, effectsTab, convolutionTab, replaceColorTab, sizeTab, refTab,
            browseTab, transformTab, marginsTab;
    @FXML
    protected Label tipsLabel, scopeLeftLabel, scopeRightLabel, promptLabel;
    @FXML
    protected Button selectRefButton, saveButton, recoverButton, undoButton, redoButton, scopeClearButton;
    @FXML
    protected CheckBox showRefCheck, showScopeCheck;
    @FXML
    protected SplitPane splitPane;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected HBox hotBox, scopeSettingBox;
    @FXML
    protected VBox imageBox;
    @FXML
    protected ComboBox hisBox;
    @FXML
    protected ToggleGroup scopeGroup;
    @FXML
    protected TextField scopeLeftXInput, scopeLeftYInput, scopeRightXInput, scopeRightYInput;

    public ImageManufactureController() {
        sourcePathKey = "ImageSourcePathKey";

    }

    @Override
    protected void initializeNext2() {
        try {

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initCommon() {
        try {
            values = new ImageManufactureValues();
            values.setRefSync(true);

            browseTab.setDisable(true);
            viewTab.setDisable(true);
            colorTab.setDisable(true);
            effectsTab.setDisable(true);
            filtersTab.setDisable(true);
            convolutionTab.setDisable(true);
            replaceColorTab.setDisable(true);
            sizeTab.setDisable(true);
            refTab.setDisable(true);
            transformTab.setDisable(true);
            textTab.setDisable(true);
            coverTab.setDisable(true);
            arcTab.setDisable(true);
            shadowTab.setDisable(true);
            marginsTab.setDisable(true);
            cropTab.setDisable(true);

            hotBar.setDisable(true);

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue<? extends Tab> observable,
                        Tab oldTab, Tab newTab) {
                    if (isSettingValues) {
                        return;
                    }
                    hidePopup();
                    switchTab(newTab);
                }
            });

            Tooltip tips = new Tooltip(getMessage("ImageManufactureTips"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(tipsLabel, tips);

            tips = new Tooltip(getMessage("ImageRefTips"));
            tips.setFont(new Font(16));
            FxmlTools.setComments(showRefCheck, tips);
            showRefCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    checkReferenceImage();
                }
            });

//            tips = new Tooltip(getMessage("ImageHisComments"));
//            tips.setFont(new Font(16));
//            FxmlTools.setComments(hisBox, tips);
            hisBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    int index = newValue.intValue();
                    if (index < 0 || hisBox.getItems() == null) {
                        return;
                    }
                    if (index == hisBox.getItems().size() - 1) {
                        BaseController c = openStage(CommonValues.SettingsFxml, true);
                        c.setParentController(getMyController());
                        c.setParentFxml(getMyFxml());
                        return;
                    }
                    if (imageHistories == null || imageHistories.size() <= index) {
                        return;
                    }
//                    logger.debug(index + " " + imageHistories.get(index) + "  " + hisBox.getSelectionModel().getSelectedItem());
                    setImage(new File(imageHistories.get(index)));
                }
            });
            hisBox.setVisibleRowCount(15);
            hisBox.setDisable(!AppVaribles.getConfigBoolean("ImageHis"));

            tips = new Tooltip(getMessage("CTRL+s"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(saveButton, tips);

            tips = new Tooltip(getMessage("CTRL+r"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(recoverButton, tips);

            tips = new Tooltip(getMessage("CTRL+y"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(redoButton, tips);

            tips = new Tooltip(getMessage("CTRL+z"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(undoButton, tips);

            tips = new Tooltip(getMessage("CTRL+1"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(oButton, tips);

            tips = new Tooltip(getMessage("CTRL+2"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(wButton, tips);

            tips = new Tooltip(getMessage("CTRL+3"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(inButton, tips);

            tips = new Tooltip(getMessage("CTRL+4"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(outButton, tips);

            tips = new Tooltip(getMessage("CTRL+h"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(hisBox, tips);

            if (showScopeCheck != null && scopeGroup != null) {
                tips = new Tooltip(getMessage("CTRL+x"));
                tips.setFont(new Font(16));
                FxmlTools.quickTooltip(scopeClearButton, tips);

                tips = new Tooltip(getMessage("ShowScopeComments"));
                tips.setFont(new Font(16));
                FxmlTools.setComments(showScopeCheck, tips);
                showScopeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                        if (new_val) {
                            showScopePane();
                        } else {
                            hideScopePane();
                        }
                    }
                });
                initScopeBar();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initScopeBar() {
        try {
            scopeColorString = getMessage("ColorLabel");
            scopeAllString = "";

            scopeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkScope();
                }
            });

            scopeLeftXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if (!isSettingValues && values != null && scope != null) {
                        switch (scope.getScopeType()) {
                            case Matting:
                                checkMatting();
                                break;
                            case Rectangle:
                                checkRectangle();
                                break;
                            case Circle:
                                checkCircle();
                                break;
                            case Color:
                            case Hue:
                                checkDistance();
                                break;
                            default:
                                break;
                        }
                    }
                }
            });

            scopeLeftYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if (!isSettingValues && values != null && scope != null) {
                        switch (scope.getScopeType()) {
                            case Rectangle:
                                checkRectangle();
                                break;
                            case Circle:
                                checkCircle();
                                break;
                            default:
                                break;
                        }
                    }
                }
            });

            scopeRightXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if (!isSettingValues && values != null && scope != null) {
                        switch (scope.getScopeType()) {
                            case Rectangle:
                                checkRectangle();
                                break;
                            case Circle:
                                checkCircle();
                                break;
                            default:
                                break;
                        }
                    }
                }
            });

            scopeRightYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if (!isSettingValues && values != null && scope != null) {
                        switch (scope.getScopeType()) {
                            case Rectangle:
                                checkRectangle();
                                break;
                            default:
                                break;
                        }
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkScope() {
        try {
            if (showScopeCheck == null || values == null || scope == null) {
                return;
            }
            scope.setImage(values.getCurrentImage());
            imageView.setImage(values.getCurrentImage());
            scopeLeftXInput.setStyle(null);
            scopeLeftYInput.setStyle(null);
            scopeRightXInput.setStyle(null);
            scopeRightYInput.setStyle(null);
            scopeClearButton.setDisable(true);
            scope.clearColors();
            scope.clearPoints();

            RadioButton selected = (RadioButton) scopeGroup.getSelectedToggle();
            if (AppVaribles.getMessage("All").equals(selected.getText())) {
                scope.setScopeType(ImageScope.ScopeType.All);
                scope.setAreaScopeType(ImageScope.AreaScopeType.AllArea);
                scope.setColorScopeType(ImageScope.ColorScopeType.AllColor);

                scopeSettingBox.setDisable(true);
                showScopeCheck.setDisable(true);
                hideScopePane();
                promptLabel.setText(scopeAllString);

            } else {
                scopeSettingBox.setDisable(false);

                if (AppVaribles.getMessage("Settings").equals(selected.getText())) {
                    scope.setScopeType(ImageScope.ScopeType.Settings);
                    scope.setAreaScopeType(ImageScope.AreaScopeType.AllArea);
                    scope.setColorScopeType(ImageScope.ColorScopeType.AllColor);
                    promptLabel.setText(scopeAllString);

                    scopeSetting();

                } else if (AppVaribles.getMessage("Matting").equals(selected.getText())) {
                    scope.setScopeType(ImageScope.ScopeType.Matting);
                    scope.setAreaScopeType(ImageScope.AreaScopeType.AllArea);
                    scope.setColorScopeType(ImageScope.ColorScopeType.Color);

                    scopeLeftLabel.setText(getMessage("ColorDistance"));
                    scopeRightLabel.setText("");
                    scopeLeftXInput.setDisable(false);
                    scopeLeftYInput.setDisable(true);
                    scopeRightXInput.setDisable(true);
                    scopeRightYInput.setDisable(true);
                    isSettingValues = true;
                    scopeLeftXInput.setText("50");
                    isSettingValues = false;
                    checkMatting();

                    scopeClearButton.setDisable(false);
                    promptLabel.setText(getMessage("MattingComments"));

                } else if (AppVaribles.getMessage("Hue").equals(selected.getText())) {
                    scope.setScopeType(ImageScope.ScopeType.Hue);
                    scope.setAreaScopeType(ImageScope.AreaScopeType.AllArea);
                    scope.setColorScopeType(ImageScope.ColorScopeType.Hue);

                    scopeLeftLabel.setText(getMessage("HueDistance"));
                    scopeRightLabel.setText("");
                    scopeLeftXInput.setDisable(false);
                    scopeLeftYInput.setDisable(true);
                    scopeRightXInput.setDisable(true);
                    scopeRightYInput.setDisable(true);
                    isSettingValues = true;
                    scopeLeftXInput.setText("5");
                    isSettingValues = false;
                    checkDistance();

                    scopeClearButton.setDisable(false);
                    promptLabel.setText(scopeColorString);

                } else if (AppVaribles.getMessage("Color").equals(selected.getText())) {
                    scope.setScopeType(ImageScope.ScopeType.Color);
                    scope.setAreaScopeType(ImageScope.AreaScopeType.AllArea);
                    scope.setColorScopeType(ImageScope.ColorScopeType.Color);

                    scopeLeftLabel.setText(getMessage("ColorDistance"));
                    scopeRightLabel.setText("");
                    scopeLeftXInput.setDisable(false);
                    scopeLeftYInput.setDisable(true);
                    scopeRightXInput.setDisable(true);
                    scopeRightYInput.setDisable(true);
                    isSettingValues = true;
                    scopeLeftXInput.setText("50");
                    isSettingValues = false;
                    checkDistance();

                    scopeClearButton.setDisable(false);
                    promptLabel.setText(scopeColorString);

                } else if (AppVaribles.getMessage("Rectangle").equals(selected.getText())) {
                    scope.setScopeType(ImageScope.ScopeType.Rectangle);
                    scope.setAreaScopeType(ImageScope.AreaScopeType.Rectangle);
                    scope.setColorScopeType(ImageScope.ColorScopeType.AllColor);

                    scopeLeftLabel.setText(getMessage("LeftTop"));
                    scopeRightLabel.setText(getMessage("RightBottom"));
                    scopeLeftXInput.setDisable(false);
                    scopeLeftYInput.setDisable(false);
                    scopeRightXInput.setDisable(false);
                    scopeRightYInput.setDisable(false);
                    isSettingValues = true;
                    scopeLeftXInput.setText((int) (values.getCurrentImage().getWidth() / 4) + "");
                    scopeLeftYInput.setText((int) (values.getCurrentImage().getHeight() / 4) + "");
                    scopeRightXInput.setText((int) (values.getCurrentImage().getWidth() * 3 / 4) + "");
                    scopeRightYInput.setText((int) (values.getCurrentImage().getHeight() * 3 / 4) + "");
                    isSettingValues = false;
                    checkRectangle();

                    promptLabel.setText(getMessage("ScopeRectangleComments"));

                } else if (AppVaribles.getMessage("Circle").equals(selected.getText())) {
                    scope.setScopeType(ImageScope.ScopeType.Circle);
                    scope.setAreaScopeType(ImageScope.AreaScopeType.Circle);
                    scope.setColorScopeType(ImageScope.ColorScopeType.AllColor);

                    scopeLeftLabel.setText(getMessage("Center"));
                    scopeRightLabel.setText(getMessage("Radius"));
                    scopeLeftXInput.setDisable(false);
                    scopeLeftYInput.setDisable(false);
                    scopeRightXInput.setDisable(false);
                    scopeRightYInput.setDisable(true);
                    isSettingValues = true;
                    scopeLeftXInput.setText((int) (values.getCurrentImage().getWidth() / 2) + "");
                    scopeLeftYInput.setText((int) (values.getCurrentImage().getHeight() / 2) + "");
                    scopeRightXInput.setText((int) (values.getCurrentImage().getWidth() / 4) + "");
                    scopeRightYInput.setText("");
                    isSettingValues = false;
                    checkCircle();

                    promptLabel.setText(getMessage("ScopeCircleComments"));
                }

                showScopeCheck.setDisable(false);
                showScopeCheck.setSelected(true);
                showScopePane();
            }

            bottomLabel.setText(AppVaribles.getMessage("ScopeComments2"));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkDistance() {
        try {
            if (scopeLeftXInput.isDisable() || isSettingValues) {
                return;
            }
            int distance = Integer.valueOf(scopeLeftXInput.getText());
            if (distance >= 0 && distance <= 255) {
                scopeLeftXInput.setStyle(null);
                scope.setColorDistance(distance);
                scope.setHueDistance(distance);
                indicateColor();
            } else {
                scopeLeftXInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            scopeLeftXInput.setStyle(badStyle);
        }
    }

    protected void checkMatting() {
        try {
            if (scopeLeftXInput.isDisable() || isSettingValues) {
                return;
            }
            int distance = Integer.valueOf(scopeLeftXInput.getText());
            if (distance >= 0 && distance <= 255) {
                scopeLeftXInput.setStyle(null);
                scope.setColorDistance(distance);
                indicateMatting();
            } else {
                scopeLeftXInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            scopeLeftXInput.setStyle(badStyle);
        }
    }

    protected void checkRectangle() {
        if (scopeLeftXInput.isDisable() || isSettingValues) {
            return;
        }
        boolean areaValid = true;
        int leftX = -1, leftY = -1, rightX = -1, rightY = -1;
        try {
            leftX = Integer.valueOf(scopeLeftXInput.getText());
            if (leftX >= 0 && leftX <= values.getCurrentImage().getWidth()) {
                scopeLeftXInput.setStyle(null);
            } else {
                areaValid = false;
                scopeLeftXInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            areaValid = false;
            scopeLeftXInput.setStyle(badStyle);
        }
        try {
            leftY = Integer.valueOf(scopeLeftYInput.getText());
            if (leftY >= 0 && leftY <= values.getCurrentImage().getHeight()) {
                scopeLeftYInput.setStyle(null);
            } else {
                areaValid = false;
                scopeLeftYInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            areaValid = false;
            scopeLeftYInput.setStyle(badStyle);
        }

        try {
            rightX = Integer.valueOf(scopeRightXInput.getText());
            if (rightX >= 0 && rightX <= values.getCurrentImage().getWidth()) {
                scopeRightXInput.setStyle(null);
            } else {
                areaValid = false;
                scopeRightXInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            areaValid = false;
            scopeRightXInput.setStyle(badStyle);
        }
        try {
            rightY = Integer.valueOf(scopeRightYInput.getText());
            if (rightY >= 0 && rightY <= values.getCurrentImage().getHeight()) {
                scopeRightYInput.setStyle(null);
            } else {
                areaValid = false;
                scopeRightYInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            areaValid = false;
            scopeRightYInput.setStyle(badStyle);
        }

        if (leftX >= rightX) {
            scopeRightXInput.setStyle(badStyle);
            areaValid = false;
        }

        if (leftY >= rightY) {
            scopeRightYInput.setStyle(badStyle);
            areaValid = false;
        }

        if (areaValid) {
            scope.setRectangle(new Rectangle(leftX, leftY, rightX, rightY));
            indicateRectangle();
        } else {
            popError(getMessage("InvalidRectangle"));
        }

    }

    protected void checkCircle() {
        if (scopeLeftXInput.isDisable() || isSettingValues) {
            return;
        }
        boolean areaValid = true;
        int x = -1, y = -1, r = -1;

        try {
            x = Integer.valueOf(scopeLeftXInput.getText());
            if (x >= 0 && x <= values.getCurrentImage().getWidth()) {
                scopeLeftXInput.setStyle(null);
            } else {
                areaValid = false;
                scopeLeftXInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            areaValid = false;
            scopeLeftXInput.setStyle(badStyle);
        }
        try {
            y = Integer.valueOf(scopeLeftYInput.getText());
            if (y >= 0 && y <= values.getCurrentImage().getHeight()) {
                scopeLeftYInput.setStyle(null);
            } else {
                areaValid = false;
                scopeLeftYInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            areaValid = false;
            scopeLeftYInput.setStyle(badStyle);
        }

        if (areaValid) {
            scope.setCircleCenter(x, y);
        }

        try {
            r = Integer.valueOf(scopeRightXInput.getText());
            if (r > 0) {
                scopeRightXInput.setStyle(null);
                scope.setCircleRadius(r);
            } else {
                areaValid = false;
                scopeRightXInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            areaValid = false;
            scopeRightXInput.setStyle(badStyle);
        }

        if (areaValid) {
            indicateCircle();
        } else {
            popError(getMessage("InvalidCircle"));
        }

    }

    protected void indicateRectangle() {
        if (scope.getScopeType() != ImageScope.ScopeType.Rectangle) {
            return;
        }
        if (task != null && task.isRunning()) {
            return;
        }
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    int lineWidth = 1;
                    if (values.getCurrentImage().getWidth() >= 150) {
                        lineWidth = (int) values.getCurrentImage().getWidth() / 150;
                    }
                    final Image newImage = FxmlScopeTools.indicateRectangle(values.getCurrentImage(),
                            Color.RED, lineWidth, scope.getRectangle());
                    final Image scopeImage = FxmlScopeTools.scopeImage(values.getCurrentImage(), scope);
                    scope.setImage(scopeImage);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImage(newImage);
                            if (scopeView != null) {
                                scopeView.setImage(scopeImage);
                                scopeText.setText(scope.getScopeText());
                            }
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    protected void indicateCircle() {
        if (scope.getScopeType() != ImageScope.ScopeType.Circle) {
            return;
        }
        if (task != null && task.isRunning()) {
            return;
        }
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    int lineWidth = 1;
                    if (values.getCurrentImage().getWidth() >= 150) {
                        lineWidth = (int) values.getCurrentImage().getWidth() / 150;
                    }
                    final Image newImage = FxmlScopeTools.indicateCircle(values.getCurrentImage(),
                            Color.RED, lineWidth, scope.getCircle());
                    final Image scopeImage = FxmlScopeTools.scopeImage(values.getCurrentImage(), scope);
                    scope.setImage(scopeImage);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImage(newImage);
                            if (scopeView != null) {
                                scopeView.setImage(scopeImage);
                                scopeText.setText(scope.getScopeText());
                            }
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    protected void indicateMatting() {
        if (scope.getScopeType() != ImageScope.ScopeType.Matting) {
            return;
        }
        if (task != null && task.isRunning()) {
            return;
        }
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final Image scopeImage = FxmlScopeTools.scopeMatting(values.getCurrentImage(), scope);
                    scope.setImage(scopeImage);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (scopeView != null) {
                                scopeView.setImage(scopeImage);
                                scopeText.setText(scope.getScopeText());
                            }
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    protected void indicateColor() {
        if (scope.getScopeType() != ImageScope.ScopeType.Hue
                && scope.getScopeType() != ImageScope.ScopeType.Color) {
            return;
        }
        if (task != null && task.isRunning()) {
            return;
        }
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final Image scopeImage = FxmlScopeTools.scopeImage(values.getCurrentImage(), scope);
                    scope.setImage(scopeImage);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (scopeView != null) {
                                scopeView.setImage(scopeImage);
                                scopeText.setText(scope.getScopeText());
                            }
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void indicateSetting() {
        if (scope.getScopeType() != ImageScope.ScopeType.Settings) {
            return;
        }
        if (task != null && task.isRunning()) {
            return;
        }
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final Image scopeImage = FxmlScopeTools.scopeImage(values.getCurrentImage(), scope);
                    scope.setImage(scopeImage);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (scopeView != null) {
                                scopeView.setImage(scopeImage);
                                scopeText.setText(scope.getScopeText());
                            }
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void setImage(final File file) {
        Task setTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                BufferedImage bufferImage = ImageIO.read(file);
                final Image newImage = SwingFXUtils.toFXImage(bufferImage, null);
//                recordImageHistory(ImageOperationType.Load, newImage);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                        setBottomLabel();
                    }
                });
                return null;
            }
        };
        openHandlingStage(setTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(setTask);
        thread.setDaemon(true);
        thread.start();
    }

    public void showRef() {
        showRefCheck.setSelected(true);
    }

    protected void checkReferenceImage() {
        try {
            values.setShowRef(showRefCheck.isSelected());

            if (values.isShowRef()) {

                if (refPane == null) {
                    refPane = new ScrollPane();
                    refPane.setPannable(true);
                    refPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    VBox.setVgrow(refPane, Priority.ALWAYS);
                    HBox.setHgrow(refPane, Priority.ALWAYS);
                }
                if (refView == null) {
                    refView = new ImageView();
                    refView.setPreserveRatio(true);
                    refView.setOnMouseEntered(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            if (values.getRefInfo() == null) {
                                return;
                            }
                            String str = AppVaribles.getMessage("Format") + ":" + values.getRefInfo().getImageFormat() + "  "
                                    + AppVaribles.getMessage("Pixels") + ":" + values.getRefInfo().getxPixels() + "x" + values.getRefInfo().getyPixels();
                            if (values.getRefInfo().getFile() != null) {
                                str += "  " + AppVaribles.getMessage("Size") + ":" + FileTools.showFileSize(values.getRefInfo().getFile().length()) + "  "
                                        + AppVaribles.getMessage("ModifyTime") + ":" + DateTools.datetimeToString(values.getRefInfo().getFile().lastModified());
                            }
                            bottomLabel.setText(str);
                        }
                    });
                    refPane.setContent(refView);
                }

                if (refBox == null) {
                    refBox = new VBox();
                    VBox.setVgrow(refBox, Priority.ALWAYS);
                    HBox.setHgrow(refBox, Priority.ALWAYS);
                    refLabel = new Label();
                    refLabel.setText(getMessage("Reference"));
                    refLabel.setAlignment(Pos.CENTER);
                    VBox.setVgrow(refLabel, Priority.NEVER);
                    HBox.setHgrow(refLabel, Priority.ALWAYS);
                    refBox.getChildren().add(0, refLabel);
                    refBox.getChildren().add(1, refPane);
                }

                if (values.getRefFile() == null) {
                    values.setRefFile(values.getSourceFile());
                }
                if (values.getRefImage() == null) {
                    loadReferenceImage();
                } else {
                    refView.setImage(values.getRefImage());
                    if (values.getRefInfo() != null) {
//                            logger.debug(scrollPane.getHeight() + " " + refInfo.getyPixels());
                        if (scrollPane.getHeight() < values.getRefInfo().getyPixels()) {
                            refView.setFitHeight(scrollPane.getHeight() - 5); // use attributes of scrollPane but not refPane
//                                refView.setFitWidth(scrollPane.getWidth() - 1);
                        } else {
                            refView.setFitHeight(values.getRefInfo().getyPixels());
//                                refView.setFitWidth(refInfo.getxPixels());
                        }
                    }
                }

                if (!splitPane.getItems().contains(refBox)) {
                    splitPane.getItems().add(0, refBox);
                }

            } else {

                if (refBox != null && splitPane.getItems().contains(refBox)) {
                    splitPane.getItems().remove(refBox);
                }

            }

            adjustSplitPane();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void loadReferenceImage() {
        if (values.getRefFile() == null || values.getSourceFile() == null) {
            return;
        }
        if (values.getRefFile().getAbsolutePath().equals(values.getSourceFile().getAbsolutePath())) {
            values.setRefImage(image);
            values.setRefInfo(values.getImageInfo());
            refView.setImage(image);
            if (scrollPane.getHeight() < values.getImageInfo().getyPixels()) {
                refView.setFitHeight(scrollPane.getHeight() - 5); // use attributes of scrollPane but not refPane
                refView.setFitWidth(scrollPane.getWidth() - 1);
            } else {
                refView.setFitHeight(values.getImageInfo().getyPixels());
                refView.setFitWidth(values.getImageInfo().getxPixels());
            }
            return;
        }
        Task refTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                values.setRefInfo(ImageFileReaders.readImageMetaData(values.getRefFile().getAbsolutePath()));
                values.setRefImage(SwingFXUtils.toFXImage(ImageIO.read(values.getRefFile()), null));
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        refView.setImage(values.getRefImage());
                        if (refPane.getHeight() < values.getRefInfo().getyPixels()) {
                            refView.setFitHeight(refPane.getHeight() - 5);
                            refView.setFitWidth(refPane.getWidth() - 1);
                        } else {
                            refView.setFitHeight(values.getRefInfo().getyPixels());
                            refView.setFitWidth(values.getRefInfo().getxPixels());
                        }
                        setBottomLabel();
                    }
                });
                return null;
            }
        };
        openHandlingStage(refTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(refTask);
        thread.setDaemon(true);
        thread.start();
    }

    protected void setImageChanged(boolean imageChanged) {
        values.setImageChanged(imageChanged);
        if (imageChanged) {
            if (values.getSourceFile() != null) {
                getMyStage().setTitle(getBaseTitle() + "  " + values.getSourceFile().getAbsolutePath() + "*");
            }
            saveButton.setDisable(false);
            recoverButton.setDisable(false);
            undoButton.setDisable(false);
            redoButton.setDisable(true);

        } else {
            saveButton.setDisable(true);
            recoverButton.setDisable(true);
            if (values.getSourceFile() != null) {
                getMyStage().setTitle(getBaseTitle() + "  " + values.getSourceFile().getAbsolutePath());
            }
        }
        setBottomLabel();
        if (scopeView != null) {
            scope.setImage(imageView.getImage());
            switch (scope.getScopeType()) {
                case Matting:
                    indicateMatting();
                    break;
                case Rectangle:
                    indicateRectangle();
                    break;
                case Circle:
                    indicateCircle();
                    break;
                case Color:
                case Hue:
                    indicateColor();
                    break;
                case Settings:
                    indicateSetting();
                    break;
                default:
                    break;
            }
        }

    }

    protected void updateHisBox() {
        if (!AppVaribles.getConfigBoolean("ImageHis") || values.getSourceFile() == null) {
            hisBox.setDisable(true);
            return;
        }
        hisBox.setDisable(false);
        hisBox.getItems().clear();
        String fname = values.getSourceFile().getAbsolutePath();
        List<ImageHistory> his = TableImageHistory.read(fname);
        List<String> hisStrings = new ArrayList<>();
        imageHistories = new ArrayList<>();
        for (ImageHistory r : his) {
            String s;
            if (r.getUpdate_type() == ImageOperationType.Load) {
                s = AppVaribles.getMessage("Load");
            } else if (r.getUpdate_type() == ImageOperationType.Add_Margins) {
                s = AppVaribles.getMessage("AddMargins");
            } else if (r.getUpdate_type() == ImageOperationType.Arc) {
                s = AppVaribles.getMessage("Arc");
            } else if (r.getUpdate_type() == ImageOperationType.Color) {
                s = AppVaribles.getMessage("Color");
            } else if (r.getUpdate_type() == ImageOperationType.Crop) {
                s = AppVaribles.getMessage("Crop");
            } else if (r.getUpdate_type() == ImageOperationType.Cut_Margins) {
                s = AppVaribles.getMessage("CutMargins");
            } else if (r.getUpdate_type() == ImageOperationType.Effects) {
                s = AppVaribles.getMessage("Effects");
            } else if (r.getUpdate_type() == ImageOperationType.Filters) {
                s = AppVaribles.getMessage("Filters");
            } else if (r.getUpdate_type() == ImageOperationType.Convolution) {
                s = AppVaribles.getMessage("Convolution");
            } else if (r.getUpdate_type() == ImageOperationType.Replace_Color) {
                s = AppVaribles.getMessage("ReplaceColor");
            } else if (r.getUpdate_type() == ImageOperationType.Shadow) {
                s = AppVaribles.getMessage("Shadow");
            } else if (r.getUpdate_type() == ImageOperationType.Size) {
                s = AppVaribles.getMessage("Size");
            } else if (r.getUpdate_type() == ImageOperationType.Transform) {
                s = AppVaribles.getMessage("Transform");
            } else if (r.getUpdate_type() == ImageOperationType.Text) {
                s = AppVaribles.getMessage("Text");
            } else if (r.getUpdate_type() == ImageOperationType.Cover) {
                s = AppVaribles.getMessage("Cover");
            } else {
                continue;
            }
            s = DateTools.datetimeToString(r.getOperation_time()) + " " + s;
            hisStrings.add(s);
            imageHistories.add(r.getHistory_location());
        }
        ImageHistory init = TableImageInit.read(fname);
        if (init != null) {
            String s = DateTools.datetimeToString(init.getOperation_time()) + " " + AppVaribles.getMessage("Load");
            hisStrings.add(s);
            imageHistories.add(init.getHistory_location());
        }
        hisStrings.add(AppVaribles.getMessage("SettingsDot"));
        hisBox.getItems().addAll(hisStrings);
    }

    protected void recordImageHistory(final int updateType, final Image newImage) {
        if (values == null || values.getSourceFile() == null
                || !AppVaribles.getConfigBoolean("ImageHis")
                || updateType < 0 || newImage == null) {
            return;
        }
        if (imageHistoriesPath == null) {
            imageHistoriesPath = AppVaribles.getImageHisPath();
        }
        Task saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final BufferedImage bufferedImage = FxmlImageTools.getBufferedImage(newImage);
                    String filename = imageHistoriesPath + File.separator
                            + FileTools.getFilePrefix(values.getSourceFile().getName())
                            + "_" + (new Date().getTime()) + "_" + updateType
                            + "_" + new Random().nextInt(1000) + ".png";
                    while (new File(filename).exists()) {
                        filename = imageHistoriesPath + File.separator
                                + FileTools.getFilePrefix(values.getSourceFile().getName())
                                + "_" + (new Date().getTime()) + "_" + updateType
                                + "_" + new Random().nextInt(1000) + ".png";
                    }
                    filename = new File(filename).getAbsolutePath();
                    ImageFileWriters.writeImageFile(bufferedImage, "png", filename);
                    if (updateType == ImageOperationType.Load) {
                        TableImageInit.write(values.getSourceFile().getAbsolutePath(), filename);
                    } else {
                        TableImageHistory.add(values.getSourceFile().getAbsolutePath(), updateType, filename);
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            updateHisBox();
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        Thread thread = new Thread(saveTask);
        thread.setDaemon(true);
        thread.start();
    }

    protected void switchTab(Tab newTab) {

        String tabName = null;
        if (fileTab.equals(newTab)) {
            tabName = "file";
        } else if (sizeTab.equals(newTab)) {
            tabName = "size";
        } else if (cropTab.equals(newTab)) {
            tabName = "crop";
        } else if (filtersTab.equals(newTab)) {
            tabName = "filters";
        } else if (convolutionTab.equals(newTab)) {
            tabName = "convolution";
        } else if (effectsTab.equals(newTab)) {
            tabName = "effects";
        } else if (colorTab.equals(newTab)) {
            tabName = "color";
        } else if (replaceColorTab.equals(newTab)) {
            tabName = "replaceColor";
        } else if (textTab.equals(newTab)) {
            tabName = "text";
        } else if (coverTab.equals(newTab)) {
            tabName = "cover";
        } else if (arcTab.equals(newTab)) {
            tabName = "arc";
        } else if (shadowTab.equals(newTab)) {
            tabName = "shadow";
        } else if (transformTab.equals(newTab)) {
            tabName = "transform";
        } else if (marginsTab.equals(newTab)) {
            tabName = "margins";
        } else if (viewTab.equals(newTab)) {
            tabName = "view";
        } else if (refTab.equals(newTab)) {
            tabName = "ref";
        } else if (browseTab.equals(newTab)) {
            tabName = "browse";
        }
        switchTab(tabName);
    }

    protected void switchTab(String tabName) {
        try {
            isSwitchingTab = true;
            String fxml = null;
            switch (tabName) {
                case "file":
                    fxml = CommonValues.ImageManufactureFileFxml;
                    break;
                case "size":
                    fxml = CommonValues.ImageManufactureSizeFxml;
                    break;
                case "crop":
                    fxml = CommonValues.ImageManufactureCropFxml;
                    break;
                case "color":
                    fxml = CommonValues.ImageManufactureColorFxml;
                    break;
                case "filters":
                    fxml = CommonValues.ImageManufactureFiltersFxml;
                    break;
                case "convolution":
                    fxml = CommonValues.ImageManufactureConvolutionFxml;
                    break;
                case "effects":
                    fxml = CommonValues.ImageManufactureEffectsFxml;
                    break;
                case "replaceColor":
                    fxml = CommonValues.ImageManufactureReplaceColorFxml;
                    break;
                case "text":
                    fxml = CommonValues.ImageManufactureTextFxml;
                    break;
                case "cover":
                    fxml = CommonValues.ImageManufactureCoverFxml;
                    break;
                case "arc":
                    fxml = CommonValues.ImageManufactureArcFxml;
                    break;
                case "shadow":
                    fxml = CommonValues.ImageManufactureShadowFxml;
                    break;
                case "transform":
                    fxml = CommonValues.ImageManufactureTransformFxml;
                    break;
                case "margins":
                    fxml = CommonValues.ImageManufactureMarginsFxml;
                    break;
                case "view":
                    fxml = CommonValues.ImageManufactureViewFxml;
                    break;
                case "ref":
                    fxml = CommonValues.ImageManufactureRefFxml;
                    break;
                case "browse":
                    fxml = CommonValues.ImageManufactureBrowseFxml;
                    break;
            }

            if (fxml != null) {
                values.setStageWidth(stageWidth);
                values.setStageHeight(stageHeight);
                values.setImageViewWidth((int) imageView.getFitWidth());
                values.setImageViewHeight((int) imageView.getFitHeight());
                values.setScope(scope);
                ImageManufactureController controller
                        = (ImageManufactureController) reloadStage(fxml, AppVaribles.getMessage("ImageManufacture"));
                controller.setValues(values);
                controller.setTab(tabName);
                controller.initInterface();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void setTab(String tab) {
        try {
            if (tab == null) {
                return;
            }
            isSettingValues = true;
            switch (tab) {
                case "file":
                    tabPane.getSelectionModel().select(fileTab);
                    break;
                case "size":
                    tabPane.getSelectionModel().select(sizeTab);
                    break;
                case "crop":
                    tabPane.getSelectionModel().select(cropTab);
                    break;
                case "color":
                    tabPane.getSelectionModel().select(colorTab);
                    break;
                case "filters":
                    tabPane.getSelectionModel().select(filtersTab);
                    break;
                case "convolution":
                    tabPane.getSelectionModel().select(convolutionTab);
                    break;
                case "effects":
                    tabPane.getSelectionModel().select(effectsTab);
                    break;
                case "replaceColor":
                    tabPane.getSelectionModel().select(replaceColorTab);
                    break;
                case "text":
                    tabPane.getSelectionModel().select(textTab);
                    break;
                case "cover":
                    tabPane.getSelectionModel().select(coverTab);
                    break;
                case "arc":
                    tabPane.getSelectionModel().select(arcTab);
                    break;
                case "shadow":
                    tabPane.getSelectionModel().select(shadowTab);
                    break;
                case "transform":
                    tabPane.getSelectionModel().select(transformTab);
                    break;
                case "margins":
                    tabPane.getSelectionModel().select(marginsTab);
                    break;
                case "view":
                    tabPane.getSelectionModel().select(viewTab);
                    break;
                case "ref":
                    tabPane.getSelectionModel().select(refTab);
                    break;
                case "browse":
                    tabPane.getSelectionModel().select(browseTab);
                    break;
            }
            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void initInterface() {
        try {
            if (values == null || values.getImage() == null) {
                return;
            }
            isSettingValues = true;

            sourceFile = values.getSourceFile();
            image = values.getImage();
            imageInformation = values.getImageInfo();

            cropTab.setDisable(false);
            colorTab.setDisable(false);
            filtersTab.setDisable(false);
            convolutionTab.setDisable(false);
            effectsTab.setDisable(false);
            arcTab.setDisable(false);
            shadowTab.setDisable(false);
            replaceColorTab.setDisable(false);
            sizeTab.setDisable(false);
            refTab.setDisable(false);
            hotBar.setDisable(false);
            transformTab.setDisable(false);
            textTab.setDisable(false);
            coverTab.setDisable(false);
            marginsTab.setDisable(false);
            viewTab.setDisable(false);
            browseTab.setDisable(false);

            undoButton.setDisable(true);
            redoButton.setDisable(true);

            imageView.setImage(values.getCurrentImage());
            imageView.setCursor(Cursor.OPEN_HAND);
            setBottomLabel();
            setImageChanged(values.isImageChanged());
            updateHisBox();

            showRefCheck.setSelected(values.isShowRef());

            getMyStage().widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue,
                        Number oldWidth, Number newWidth) {
                    stageWidth = newWidth.intValue();
                }
            });
            getMyStage().heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue,
                        Number oldHeight, Number newHeight) {
                    stageHeight = newHeight.intValue();
                }
            });
            if (values.getStageWidth() > getMyStage().getWidth()) {
                getMyStage().setWidth(values.getStageWidth());
            }
            if (values.getStageHeight() > getMyStage().getHeight()) {
                getMyStage().setHeight(values.getStageHeight());
            }
            if (values.getImageViewHeight() > 0) {
                imageView.setFitHeight(values.getImageViewHeight());
                imageView.setFitWidth(values.getImageViewWidth());
            } else {
                fitSize();
            }

            isSettingValues = false;

            scope = values.getScope();
            if (scope != null) {
                checkScope();
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @Override
    public void afterImageLoaded() {
        try {
            super.afterImageLoaded();
            if (image == null) {
                return;
            }
            isSettingValues = true;

            values.setSourceFile(sourceFile);
            values.setImage(image);
            values.setImageInfo(imageInformation);
            values.setCurrentImage(image);
            values.setRefImage(image);
            setImageChanged(false);
            values.setScope(new ImageScope(image));

            recordImageHistory(ImageOperationType.Load, image);

            if (initTab != null) {
                switchTab(initTab);
            } else {
                initInterface();
            }

            isSettingValues = false;

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void scopeDetermined(ImageScope imageScope) {
        values.setScope(imageScope);
        scope = imageScope;
        showScopePane();
    }

    //  Hotbar Methods
    @FXML
    public void save() {
        if (saveButton.isDisabled()) {
            return;
        }
        if (values.getSourceFile() == null) {
            saveAs();
            return;
        }
        if (values.isIsConfirmBeforeSave()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("SureOverrideFile"));
            ButtonType buttonSave = new ButtonType(AppVaribles.getMessage("Save"));
            ButtonType buttonSaveAs = new ButtonType(AppVaribles.getMessage("SaveAs"));
            ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonCancel) {
                return;
            } else if (result.get() == buttonSaveAs) {
                saveAs();
                return;
            }

        }

        Task saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String format = values.getImageInfo().getImageFormat();
                final BufferedImage bufferedImage = FxmlImageTools.getBufferedImage(values.getCurrentImage());
                ImageFileWriters.writeImageFile(bufferedImage, format, values.getSourceFile().getAbsolutePath());
                imageInformation = ImageFileReaders.readImageMetaData(values.getSourceFile().getAbsolutePath());
                image = values.getCurrentImage();
                values.setImage(image);
                values.setImageInfo(imageInformation);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        setImageChanged(false);
                        setBottomLabel();
                    }
                });
                return null;
            }
        };
        openHandlingStage(saveTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(saveTask);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    public void saveAs() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(targetPathKey, CommonValues.UserFilePath));
            if (!path.isDirectory()) {
                path = new File(CommonValues.UserFilePath);
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setConfigValue(targetPathKey, file.getParent());

            Task saveTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    String format = FileTools.getFileSuffix(file.getName());
                    final BufferedImage bufferedImage = FxmlImageTools.getBufferedImage(values.getCurrentImage());
                    ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (values.getSourceFile() == null
                                    || values.getSaveAsType() == ImageManufactureFileController.SaveAsType.Load) {
                                sourceFileChanged(file);

                            } else if (values.getSaveAsType() == ImageManufactureFileController.SaveAsType.Open) {
                                openImageManufactureInNew(file.getAbsolutePath());
                            }
                        }
                    });
                    return null;
                }
            };
            openHandlingStage(saveTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(saveTask);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    @Override
    public void zoomIn() {
        try {
            super.zoomIn();
            if (values.isRefSync() && refView != null) {
                refView.setFitWidth(imageView.getFitWidth());
                refView.setFitHeight(imageView.getFitWidth());
            }
            if (scopeView != null) {
                scopeView.setFitWidth(imageView.getFitWidth());
                scopeView.setFitHeight(imageView.getFitHeight());
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void zoomOut() {
        super.zoomOut();
        if (values.isRefSync() && refView != null) {
            refView.setFitWidth(imageView.getFitWidth());
            refView.setFitHeight(imageView.getFitWidth());
        }
        if (scopeView != null) {
            scopeView.setFitWidth(imageView.getFitWidth());
            scopeView.setFitHeight(imageView.getFitHeight());
        }
    }

    @FXML
    @Override
    public void imageSize() {
        imageView.setFitHeight(-1);
        imageView.setFitWidth(-1);
        if (values.isRefSync() && refView != null) {
            refView.setFitHeight(-1);
            refView.setFitWidth(-1);
        }
        if (scopeView != null) {
            scopeView.setFitHeight(-1);
            scopeView.setFitWidth(-1);
        }
    }

    @FXML
    @Override
    public void paneSize() {
        imageView.setFitWidth(scrollPane.getWidth() - 5);
        imageView.setFitHeight(scrollPane.getHeight() - 5);
        if (values.isRefSync() && refView != null) {
            refView.setFitWidth(scrollPane.getWidth() - 5);
            refView.setFitHeight(scrollPane.getHeight() - 5);
        }
        if (scopeView != null) {
            scopeView.setFitWidth(scrollPane.getWidth() - 5);
            scopeView.setFitHeight(scrollPane.getHeight() - 5);
        }
    }

    // Common Methods
    @FXML
    public void setBottomLabel() {
        if (values == null || values.getCurrentImage() == null) {
            return;
        }
        String str;
        if (values.getImageInfo() == null) {
            str = AppVaribles.getMessage("CurrentPixels") + ":" + (int) values.getCurrentImage().getWidth() + "x" + (int) values.getCurrentImage().getHeight();
        } else {
            str = AppVaribles.getMessage("Format") + ":" + values.getImageInfo().getImageFormat() + "  "
                    + AppVaribles.getMessage("Pixels") + ":" + values.getImageInfo().getxPixels() + "x" + values.getImageInfo().getyPixels() + "  "
                    + AppVaribles.getMessage("Size") + ":" + FileTools.showFileSize(values.getImageInfo().getFile().length()) + "  "
                    + AppVaribles.getMessage("ModifyTime") + ":" + DateTools.datetimeToString(values.getImageInfo().getFile().lastModified()) + "  "
                    + AppVaribles.getMessage("CurrentPixels") + ":" + (int) values.getCurrentImage().getWidth() + "x" + (int) values.getCurrentImage().getHeight();
        }
        bottomLabel.setText(str);
    }

    @FXML
    public void clickImage(MouseEvent event) {
        handleClick(imageView, event);
    }

    public void clickImageForAll(MouseEvent event, Color color) {

    }

    public void clickImageForColor(MouseEvent event, Color color) {
        scope.addColor(color);
        indicateColor();
    }

    public void handleClick(ImageView view, MouseEvent event) {
        if (values == null || values.getCurrentImage() == null || scope == null) {
            view.setCursor(Cursor.OPEN_HAND);
            return;
        }
        view.setCursor(Cursor.HAND);

        int x = (int) Math.round(event.getX() * values.getCurrentImage().getWidth() / view.getBoundsInLocal().getWidth());
        int y = (int) Math.round(event.getY() * values.getCurrentImage().getHeight() / view.getBoundsInLocal().getHeight());
        PixelReader pixelReader = values.getCurrentImage().getPixelReader();
        Color color = pixelReader.getColor(x, y);

        switch (scope.getScopeType()) {
            case All:
            case Settings:
                clickImageForAll(event, color);
                break;

            case Color:
            case Hue:
                clickImageForColor(event, color);
                break;

            case Matting:
                scope.addPoints(x, y);
                indicateMatting();
                break;

            case Rectangle:
                if (event.getButton() == MouseButton.PRIMARY) {
                    isSettingValues = true;
                    scopeLeftXInput.setText(x + "");
                    scopeLeftYInput.setText(y + "");
                    isSettingValues = false;

                } else if (event.getButton() == MouseButton.SECONDARY) {
                    isSettingValues = true;
                    scopeRightXInput.setText(x + "");
                    scopeRightYInput.setText(y + "");
                    isSettingValues = false;
                }
                checkRectangle();
                break;

            case Circle:
                if (event.getButton() == MouseButton.PRIMARY) {
                    isSettingValues = true;
                    scopeLeftXInput.setText(x + "");
                    scopeLeftYInput.setText(y + "");
                    isSettingValues = false;

                } else if (event.getButton() == MouseButton.SECONDARY) {
                    isSettingValues = true;
                    int cx = scope.getCircle().getCenterX();
                    int cy = scope.getCircle().getCenterY();
                    long r = Math.round(Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy)));
                    scopeRightXInput.setText(r + "");
                    isSettingValues = false;

                }
                checkCircle();
                break;
            default:
                break;

        }
    }

    @FXML
    public void recovery() {
        imageView.setImage(values.getImage());
        values.setUndoImage(values.getCurrentImage());
        values.setCurrentImage(values.getImage());
        setImageChanged(false);
        undoButton.setDisable(false);
        redoButton.setDisable(true);

    }

    @FXML
    public void undoAction() {
        if (values.getUndoImage() == null) {
            undoButton.setDisable(true);
        }
        values.setRedoImage(values.getCurrentImage());
        values.setCurrentImage(values.getUndoImage());
        imageView.setImage(values.getUndoImage());
        setImageChanged(true);
        undoButton.setDisable(true);
        redoButton.setDisable(false);
    }

    @FXML
    public void redoAction() {
        if (values.getRedoImage() == null) {
            redoButton.setDisable(true);
        }
        values.setUndoImage(values.getCurrentImage());
        values.setCurrentImage(values.getRedoImage());
        imageView.setImage(values.getRedoImage());
        setImageChanged(true);
        undoButton.setDisable(false);
        redoButton.setDisable(true);
    }

    @FXML
    public void clearScope() {
        scope.clearColors();
        scope.clearPoints();
        switch (scope.getScopeType()) {
            case All:
            case Settings:

                break;

            case Color:
            case Hue:
                indicateColor();
                break;

            case Matting:
                indicateMatting();
                break;

            case Rectangle:
                indicateRectangle();
                break;

            case Circle:
                indicateCircle();
                break;

            default:
                break;
        }
    }

    @Override
    protected void keyEventsHandler(KeyEvent event) {
        super.keyEventsHandler(event);
        String key = event.getText();
        if (key == null || key.isEmpty()) {
            return;
        }
        if (event.isControlDown()) {
            switch (key) {
                case "s":
                case "S":
                    if (!saveButton.isDisabled()) {
                        save();
                    }
                    break;
                case "r":
                case "R":
                    if (!recoverButton.isDisabled()) {
                        recovery();
                    }
                    break;
                case "z":
                case "Z":
                    if (!undoButton.isDisabled()) {
                        undoAction();
                    }
                    break;
                case "y":
                case "Y":
                    if (!redoButton.isDisabled()) {
                        redoAction();
                    }
                    break;
                case "h":
                case "H":
                    if (!hisBox.isDisabled()) {
                        hisBox.show();
                    }
                    break;
                case "1":
                    if (!wButton.isDisabled()) {
                        paneSize();
                    }
                    break;
                case "2":
                    if (!oButton.isDisabled()) {
                        imageSize();
                    }
                    break;
                case "3":
                    if (!inButton.isDisabled()) {
                        zoomIn();
                    }
                    break;
                case "4":
                    if (!outButton.isDisabled()) {
                        zoomOut();
                    }
                    break;
                case "x":
                    if (!scopeClearButton.isDisabled()) {
                        clearScope();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void scopeSetting() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageScopeFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            final ImageScopeController controller = fxmlLoader.getController();
            Stage stage = new Stage();
            controller.setMyStage(stage);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    if (!controller.stageClosing()) {
                        event.consume();
                    }
                }
            });

            Scene scene = new Scene(pane);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(getMyStage());
            stage.getIcons().add(CommonValues.AppIcon);
            stage.setScene(scene);
            stage.show();

            String title = AppVaribles.getMessage("ImageManufactureScope");
            switch (scope.getOperationType()) {
                case Color:
                    title += " - " + AppVaribles.getMessage("Color");
                    break;
                case ReplaceColor:
                    title += " - " + AppVaribles.getMessage("ReplaceColor");
                    break;
                case Filters:
                    title += " - " + AppVaribles.getMessage("Filters");
                    break;
                case Crop:
                    title += " - " + AppVaribles.getMessage("Crop");
                    break;
                default:
                    break;
            }
            controller.loadImage(this, scope, title);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void showScopePane() {
        try {
            if (showScopeCheck == null || !showScopeCheck.isSelected()
                    || values == null || scope == null) {
                hideScopePane();
                return;
            }

            if (scopePane == null) {
                scopePane = new ScrollPane();
                scopePane.setPannable(true);
                scopePane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                VBox.setVgrow(scopePane, Priority.ALWAYS);
                HBox.setHgrow(scopePane, Priority.ALWAYS);
            }
            if (scopeView == null) {
                scopeView = new ImageView();
                scopeView.setPreserveRatio(true);
                scopeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        handleClick(scopeView, event);
                    }
                });
                scopePane.setContent(scopeView);
            }

            if (scopeBox == null) {
                scopeBox = new VBox();
                VBox.setVgrow(scopeBox, Priority.ALWAYS);
                HBox.setHgrow(scopeBox, Priority.ALWAYS);
                scopeText = new TextField();
                scopeText.setAlignment(Pos.CENTER_LEFT);
                scopeText.setEditable(false);
                scopeText.setStyle("-fx-text-fill: #2e598a; -fx-background: #f4f4f4;");
                VBox.setVgrow(scopeText, Priority.NEVER);
                HBox.setHgrow(scopeText, Priority.ALWAYS);
                scopeBox.getChildren().add(0, scopeText);
                scopeBox.getChildren().add(1, scopePane);
            }
            scopeText.setText(scope.getScopeText());

            Tooltip stips = new Tooltip(getMessage("ScopeImageComments"));
            stips.setFont(new Font(16));
            FxmlTools.quickTooltip(scopeBox, stips);

            scopeView.setImage(scope.getImage());

            if (!splitPane.getItems().contains(scopeBox)) {
                splitPane.getItems().add(0, scopeBox);
            }

            adjustSplitPane();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void hideScopePane() {
        try {
            if (showScopeCheck != null) {
                showScopeCheck.setSelected(false);
            }
            if (scopeBox != null && splitPane.getItems().contains(scopeBox)) {
                splitPane.getItems().remove(scopeBox);
            }
            adjustSplitPane();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void adjustSplitPane() {
        switch (splitPane.getItems().size()) {
            case 3:
                splitPane.getDividers().get(0).setPosition(0.33333);
                splitPane.getDividers().get(1).setPosition(0.66666);
//                splitPane.setDividerPositions(0.33, 0.33, 0.33); // This way not work!
                break;
            case 2:
                splitPane.getDividers().get(0).setPosition(0.5);
//               splitPane.setDividerPositions(0.5, 0.5); // This way not work!
                break;
            default:
                splitPane.setDividerPositions(1);
                break;
        }
        splitPane.layout();
        fitSize();
    }

    @Override
    public boolean stageReloading() {
        if (isSwitchingTab) {
            return true;
        }
        return checkSavingBeforeExit();
    }

    @Override
    public boolean stageClosing() {
        if (!checkSavingBeforeExit()) {
            return false;
        }
        return super.stageClosing();
    }

    public boolean checkSavingBeforeExit() {
        if (values.isImageChanged()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("ImageChanged"));
            ButtonType buttonSave = new ButtonType(AppVaribles.getMessage("Save"));
            ButtonType buttonSaveAs = new ButtonType(AppVaribles.getMessage("SaveAs"));
            ButtonType buttonNotSave = new ButtonType(AppVaribles.getMessage("NotSave"));
            ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonNotSave, buttonCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                save();
                return true;
            } else if (result.get() == buttonNotSave) {
                return true;
            } else if (result.get() == buttonSaveAs) {
                saveAs();
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public String getInitTab() {
        return initTab;
    }

    public void setInitTab(String initTab) {
        this.initTab = initTab;
    }

    public ImageManufactureValues getValues() {
        return values;
    }

    public void setValues(final ImageManufactureValues values) {
        this.values = values;
    }

}
