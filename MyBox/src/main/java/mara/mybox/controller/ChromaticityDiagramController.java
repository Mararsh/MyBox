package mara.mybox.controller;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import mara.mybox.color.CIEColorSpace;
import mara.mybox.color.CIEData;
import mara.mybox.color.ChromaticityDiagram;
import mara.mybox.color.ChromaticityDiagram.DataType;
import mara.mybox.color.ColorValue;
import mara.mybox.color.SRGB;
import mara.mybox.data.VisitHistory;
import mara.mybox.data.tools.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.image.ImageValue;
import mara.mybox.image.file.ImageFileWriters;
import static mara.mybox.tools.DoubleTools.scale;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-5-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ChromaticityDiagramController extends ChromaticityBaseController {

    private boolean isLine, inputInit = true;
    private int dotSize, fontSize;
    private java.awt.Color bgColor, calculateColor;
    private final ObservableList<ColorValue> calculatedValues = FXCollections.observableArrayList();
    private ObservableList<CIEData> degree2nm1Data, degree10nm1Data, degree2nm5Data, degree10nm5Data;
    private double X, Y = 1, Z, x = 0.4, y = 0.5;

    @FXML
    private ComboBox<String> dotTypeBox, backgroundBox, fontSelector;
    @FXML
    private CheckBox cdProPhotoCheck, cdColorMatchCheck, cdNTSCCheck, cdPALCheck, cdAppleCheck, cdAdobeCheck,
            cdSRGBCheck, cdECICheck, cdCIECheck, cdSMPTECCheck, degree2Check, degree10Check,
            waveCheck, whitePointsCheck, gridCheck, calculateCheck, inputCheck;
    @FXML
    private ImageView cieDiagram;
    @FXML
    private ScrollPane cieDiagramScroll;
    @FXML
    private TextArea sourceInputArea, sourceDataArea,
            d2n1Area, d2n5Area, d10n1Area, d10n5Area;
    @FXML
    private SplitPane dataTablePane;
    @FXML
    private ToggleGroup dataGroup;
    @FXML
    private VBox tableBox, calculateBox, inputBox;
    @FXML
    private TableView<ColorValue> calculatedValuesTable;
    @FXML
    private TableColumn<ColorValue, String> colorSpaceColumn, conditionsColumn, valuesColumn;
    @FXML
    private TableView<CIEData> d2n1TableView, d2n5TableView, d10n1TableView, d10n5TableView, inputTableView;
    @FXML
    private TableColumn<CIEData, Integer> wave2n1Column, wave10n1Column, wave2n5Column, wave10n5Column;
    @FXML
    private TableColumn<CIEData, Double> tx2n1Column, ty2n1Column, tz2n1Column, nx2n1Column, ny2n1Column, nz2n1Column,
            rx2n1Column, ry2n1Column, rz2n1Column, r2n1Column, g2n1Column, b2n1Column,
            tx10n1Column, ty10n1Column, tz10n1Column, nx10n1Column, ny10n1Column, nz10n1Column,
            rx10n1Column, ry10n1Column, rz10n1Column, r10n1Column, g10n1Column, b10n1Column;
    @FXML
    private TableColumn<CIEData, Integer> ri2n1Column, gi2n1Column, bi2n1Column,
            ri10n1Column, gi10n1Column, bi10n1Column;
    @FXML
    private TableColumn<CIEData, Double> tx2n5Column, ty2n5Column, tz2n5Column, nx2n5Column, ny2n5Column, nz2n5Column,
            rx2n5Column, ry2n5Column, rz2n5Column, r2n5Column, g2n5Column, b2n5Column,
            tx10n5Column, ty10n5Column, tz10n5Column, nx10n5Column, ny10n5Column, nz10n5Column,
            rx10n5Column, ry10n5Column, rz10n5Column, r10n5Column, g10n5Column, b10n5Column;
    @FXML
    private TableColumn<CIEData, Integer> ri2n5Column, gi2n5Column, bi2n5Column,
            ri10n5Column, gi10n5Column, bi10n5Column;
    @FXML
    private ToolBar dataToolbar;
    @FXML
    private TextField XInput, YInput, ZInput, xInput, yInput;
    @FXML
    private Button calculateXYZButton, calculateXYButton, displayDataButton;
    @FXML
    private TabPane csPane, filePane;
    @FXML
    protected ColorSetController colorSetController;

    public ChromaticityDiagramController() {
        baseTitle = AppVariables.message("DrawChromaticityDiagram");

        TipsLabelKey = "ChromaticityDiagramTips";

        SourceFileType = VisitHistory.FileType.Text;
        SourcePathType = VisitHistory.FileType.Text;
        TargetPathType = VisitHistory.FileType.Text;
        TargetFileType = VisitHistory.FileType.Text;
        AddFileType = VisitHistory.FileType.Text;
        AddPathType = VisitHistory.FileType.Text;

        sourceExtensionFilter = CommonFxValues.TextExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initToolBar();
            initDataBox();
            initCIETables();
            initCIEData();
            initDiagram();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void initToolBar() {
        try {
            List<String> bgList = Arrays.asList(message("Transparent"),
                    message("White"), message("Black")
            );
            backgroundBox.getItems().addAll(bgList);
            backgroundBox.setVisibleRowCount(bgList.size());
            backgroundBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkBackground();
                }
            });

            List<String> opList = Arrays.asList(message("Line4px"),
                    message("Dot6px"), message("Dot10px"), message("Dot4px"),
                    message("Dot12px"), message("Line1px"), message("Line2px"),
                    message("Line6px"), message("Line10px")
            );
            dotTypeBox.getItems().addAll(opList);
            dotTypeBox.setVisibleRowCount(opList.size());
            dotTypeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkDotType();
                }
            });

            List<String> fontList = Arrays.asList("20", "24", "28", "30", "18", "16", "15", "14", "12", "10");
            fontSelector.getItems().addAll(fontList);
            fontSelector.setVisibleRowCount(fontList.size());
            fontSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkFontSize();
                }
            });

            calculateCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    if (!isSettingValues) {
                        displayChromaticityDiagram();
                    }
                }
            });
            inputCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            waveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            gridCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            whitePointsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            degree2Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            degree10Check.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdProPhotoCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdColorMatchCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdNTSCCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdPALCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdAppleCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdAdobeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdSRGBCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdECICheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdCIECheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });
            cdSMPTECCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    displayChromaticityDiagram();
                }
            });

            isSettingValues = true;
            backgroundBox.getSelectionModel().select(0);
            dotTypeBox.getSelectionModel().select(0);
            fontSelector.getSelectionModel().select(0);
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void initDiagram() {
        try {
            colorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    calculateColor();
                }
            });

            cieDiagram.fitWidthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov,
                        Number old_val, Number new_val) {
                    if (Math.abs(new_val.intValue() - old_val.intValue()) > 20) {
                        refinePane();
                    }
                }
            });
            cieDiagram.fitHeightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov,
                        Number old_val, Number new_val) {
                    if (Math.abs(new_val.intValue() - old_val.intValue()) > 20) {
                        refinePane();
                    }
                }
            });
            cieDiagramScroll.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov,
                        Number old_val, Number new_val) {
                    if (Math.abs(new_val.intValue() - old_val.intValue()) > 20) {
                        refinePane();
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    private void initDataBox() {
        try {
            dataGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    loadTableData();
                }
            });

            XInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        X = Double.parseDouble(newValue);
                        XInput.setStyle(null);
                    } catch (Exception e) {
                        XInput.setStyle(badStyle);
                    }
                }
            });
            YInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        Y = Double.parseDouble(newValue);
                        if (Y == 0) {
                            YInput.setStyle(badStyle);
                        } else {
                            YInput.setStyle(null);
                        }
                    } catch (Exception e) {
                        YInput.setStyle(badStyle);
                    }
                }
            });
            ZInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        Z = Double.parseDouble(newValue);
                        ZInput.setStyle(null);
                    } catch (Exception e) {
                        ZInput.setStyle(badStyle);
                    }
                }
            });
            FxmlControl.setTooltip(YInput, new Tooltip(message("1-based")));

            xInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        x = Double.parseDouble(newValue);
                        double z = 1 - x - y;
                        if (x > 1 || x < 0 || z < 0 || z > 1) {
                            xInput.setStyle(badStyle);
                        } else {
                            xInput.setStyle(null);
                        }
                    } catch (Exception e) {
                        xInput.setStyle(badStyle);
                    }
                }
            });
            yInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    try {
                        y = Double.parseDouble(newValue);
                        double z = 1 - x - y;
                        if (y > 1 || y <= 0 || z < 0 || z > 1) {
                            yInput.setStyle(badStyle);
                        } else {
                            yInput.setStyle(null);
                        }
                    } catch (Exception e) {
                        yInput.setStyle(badStyle);
                    }
                }
            });

            calculateXYZButton.disableProperty().bind(Bindings.isEmpty(XInput.textProperty())
                    .or(XInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(YInput.textProperty()))
                    .or(YInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(ZInput.textProperty()))
                    .or(ZInput.styleProperty().isEqualTo(badStyle))
            );

            calculateXYButton.disableProperty().bind(Bindings.isEmpty(xInput.textProperty())
                    .or(xInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(yInput.textProperty()))
                    .or(yInput.styleProperty().isEqualTo(badStyle))
            );

            displayDataButton.disableProperty().bind(Bindings.isEmpty(sourceDataArea.textProperty())
            );

            colorSpaceColumn.setCellValueFactory(new PropertyValueFactory<>("colorSpace"));
            conditionsColumn.setCellValueFactory(new PropertyValueFactory<>("conditions"));
            valuesColumn.setCellValueFactory(new PropertyValueFactory<>("values"));

            sourceInputArea.setStyle(" -fx-text-fill: gray;");
            sourceInputArea.setText(message("ChromaticityDiagramTips"));
            sourceInputArea.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(
                        ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) {
                    if (inputInit) {
                        sourceInputArea.clear();
                        sourceInputArea.setStyle(null);
                        inputInit = false;
                    }
                }
            });

            sourceInputArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    checkInputs();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void checkInputs() {
        String data = CIEData.cieString(sourceInputArea.getText());
        if (data != null) {
            sourceDataArea.setText(data);
        } else {
            popError(AppVariables.message("NoData"));
            sourceDataArea.clear();
        }
    }

    private void initCIETables() {
        wave2n1Column.setCellValueFactory(new PropertyValueFactory<>("waveLength"));
        tx2n1Column.setCellValueFactory(new PropertyValueFactory<>("X"));
        ty2n1Column.setCellValueFactory(new PropertyValueFactory<>("Y"));
        tz2n1Column.setCellValueFactory(new PropertyValueFactory<>("Z"));
        nx2n1Column.setCellValueFactory(new PropertyValueFactory<>("normalizedX"));
        ny2n1Column.setCellValueFactory(new PropertyValueFactory<>("normalizedY"));
        nz2n1Column.setCellValueFactory(new PropertyValueFactory<>("normalizedZ"));
        rx2n1Column.setCellValueFactory(new PropertyValueFactory<>("relativeX"));
        ry2n1Column.setCellValueFactory(new PropertyValueFactory<>("relativeY"));
        rz2n1Column.setCellValueFactory(new PropertyValueFactory<>("relativeZ"));
        r2n1Column.setCellValueFactory(new PropertyValueFactory<>("red"));
        g2n1Column.setCellValueFactory(new PropertyValueFactory<>("green"));
        b2n1Column.setCellValueFactory(new PropertyValueFactory<>("blue"));
        ri2n1Column.setCellValueFactory(new PropertyValueFactory<>("redi"));
        gi2n1Column.setCellValueFactory(new PropertyValueFactory<>("greeni"));
        bi2n1Column.setCellValueFactory(new PropertyValueFactory<>("bluei"));

        wave2n5Column.setCellValueFactory(new PropertyValueFactory<>("waveLength"));
        tx2n5Column.setCellValueFactory(new PropertyValueFactory<>("X"));
        ty2n5Column.setCellValueFactory(new PropertyValueFactory<>("Y"));
        tz2n5Column.setCellValueFactory(new PropertyValueFactory<>("Z"));
        nx2n5Column.setCellValueFactory(new PropertyValueFactory<>("normalizedX"));
        ny2n5Column.setCellValueFactory(new PropertyValueFactory<>("normalizedY"));
        nz2n5Column.setCellValueFactory(new PropertyValueFactory<>("normalizedZ"));
        rx2n5Column.setCellValueFactory(new PropertyValueFactory<>("relativeX"));
        ry2n5Column.setCellValueFactory(new PropertyValueFactory<>("relativeY"));
        rz2n5Column.setCellValueFactory(new PropertyValueFactory<>("relativeZ"));
        r2n5Column.setCellValueFactory(new PropertyValueFactory<>("red"));
        g2n5Column.setCellValueFactory(new PropertyValueFactory<>("green"));
        b2n5Column.setCellValueFactory(new PropertyValueFactory<>("blue"));
        ri2n5Column.setCellValueFactory(new PropertyValueFactory<>("redi"));
        gi2n5Column.setCellValueFactory(new PropertyValueFactory<>("greeni"));
        bi2n5Column.setCellValueFactory(new PropertyValueFactory<>("bluei"));

        wave10n1Column.setCellValueFactory(new PropertyValueFactory<>("waveLength"));
        tx10n1Column.setCellValueFactory(new PropertyValueFactory<>("X"));
        ty10n1Column.setCellValueFactory(new PropertyValueFactory<>("Y"));
        tz10n1Column.setCellValueFactory(new PropertyValueFactory<>("Z"));
        nx10n1Column.setCellValueFactory(new PropertyValueFactory<>("normalizedX"));
        ny10n1Column.setCellValueFactory(new PropertyValueFactory<>("normalizedY"));
        nz10n1Column.setCellValueFactory(new PropertyValueFactory<>("normalizedZ"));
        rx10n1Column.setCellValueFactory(new PropertyValueFactory<>("relativeX"));
        ry10n1Column.setCellValueFactory(new PropertyValueFactory<>("relativeY"));
        rz10n1Column.setCellValueFactory(new PropertyValueFactory<>("relativeZ"));
        r10n1Column.setCellValueFactory(new PropertyValueFactory<>("red"));
        g10n1Column.setCellValueFactory(new PropertyValueFactory<>("green"));
        b10n1Column.setCellValueFactory(new PropertyValueFactory<>("blue"));
        ri10n1Column.setCellValueFactory(new PropertyValueFactory<>("redi"));
        gi10n1Column.setCellValueFactory(new PropertyValueFactory<>("greeni"));
        bi10n1Column.setCellValueFactory(new PropertyValueFactory<>("bluei"));

        wave10n5Column.setCellValueFactory(new PropertyValueFactory<>("waveLength"));
        tx10n5Column.setCellValueFactory(new PropertyValueFactory<>("X"));
        ty10n5Column.setCellValueFactory(new PropertyValueFactory<>("Y"));
        tz10n5Column.setCellValueFactory(new PropertyValueFactory<>("Z"));
        nx10n5Column.setCellValueFactory(new PropertyValueFactory<>("normalizedX"));
        ny10n5Column.setCellValueFactory(new PropertyValueFactory<>("normalizedY"));
        nz10n5Column.setCellValueFactory(new PropertyValueFactory<>("normalizedZ"));
        rx10n5Column.setCellValueFactory(new PropertyValueFactory<>("relativeX"));
        ry10n5Column.setCellValueFactory(new PropertyValueFactory<>("relativeY"));
        rz10n5Column.setCellValueFactory(new PropertyValueFactory<>("relativeZ"));
        r10n5Column.setCellValueFactory(new PropertyValueFactory<>("red"));
        g10n5Column.setCellValueFactory(new PropertyValueFactory<>("green"));
        b10n5Column.setCellValueFactory(new PropertyValueFactory<>("blue"));
        ri10n5Column.setCellValueFactory(new PropertyValueFactory<>("redi"));
        gi10n5Column.setCellValueFactory(new PropertyValueFactory<>("greeni"));
        bi10n5Column.setCellValueFactory(new PropertyValueFactory<>("bluei"));

    }

    private void refinePane() {
        if (cieDiagram.getImage() == null) {
            return;
        }
        FxmlControl.paneSize(cieDiagramScroll, cieDiagram);
        cieDiagramScroll.setVvalue(cieDiagramScroll.getVmin());

    }

    private void checkDotType() {
        try {
            isLine = false;
            String type = dotTypeBox.getSelectionModel().getSelectedItem();
            if (message("Dot6px").equals(type)) {
                dotSize = 6;
            } else if (message("Dot10px").equals(type)) {
                dotSize = 10;
            } else if (message("Dot4px").equals(type)) {
                dotSize = 4;
            } else if (message("Dot12px").equals(type)) {
                dotSize = 12;
            } else if (message("Line2px").equals(type)) {
                isLine = true;
                dotSize = 2;
            } else if (message("Line1px").equals(type)) {
                isLine = true;
                dotSize = 1;
            } else if (message("Line4px").equals(type)) {
                isLine = true;
                dotSize = 4;
            } else if (message("Line6px").equals(type)) {
                isLine = true;
                dotSize = 6;
            } else if (message("Line10px").equals(type)) {
                isLine = true;
                dotSize = 10;
            } else {
                dotSize = 6;
            }
        } catch (Exception e) {
            dotSize = 6;
        }
        if (!isSettingValues) {
            displayChromaticityDiagram();
        }

    }

    private void checkBackground() {
        try {
            String type = backgroundBox.getSelectionModel().getSelectedItem();
            if (message("Transparent").equals(type)) {
                bgColor = null;
            } else if (message("White").equals(type)) {
                bgColor = java.awt.Color.WHITE;
            } else if (message("Black").equals(type)) {
                bgColor = java.awt.Color.BLACK;
            } else {
                bgColor = null;
            }
        } catch (Exception e) {
            bgColor = null;
        }
        if (!isSettingValues) {
            displayChromaticityDiagram();
        }

    }

    private void checkFontSize() {
        try {
            int v = Integer.parseInt(fontSelector.getValue());
            if (v > 0) {
                fontSize = v;
                fontSelector.getEditor().setStyle(null);
                if (!isSettingValues) {
                    displayChromaticityDiagram();
                }
            } else {
                fontSelector.getEditor().setStyle(badStyle);
            }
        } catch (Exception e) {
            fontSelector.getEditor().setStyle(badStyle);
        }
    }

    private void initCIEData() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private String degree2nm1String, degree10nm1String,
                        degree2nm5String, degree10nm5String;

                @Override
                protected boolean handle() {
                    CIEData cieData = new CIEData();
                    ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);

                    degree2nm1Data = FXCollections.observableArrayList();
                    degree2nm1Data.addAll(cieData.cie1931Observer2Degree1nmData(cs));
                    degree2nm1String = cieData.cie1931Observer2Degree1nmString(cs);

                    degree10nm1Data = FXCollections.observableArrayList();
                    degree10nm1Data.addAll(cieData.cie1964Observer10Degree1nmData(cs));
                    degree10nm1String = cieData.cie1964Observer10Degree1nmString(cs);

                    degree2nm5Data = FXCollections.observableArrayList();
                    degree2nm5Data.addAll(cieData.cie1931Observer2Degree5nmData(cs));
                    degree2nm5String = cieData.cie1931Observer2Degree5nmString(cs);

                    degree10nm5Data = FXCollections.observableArrayList();
                    degree10nm5Data.addAll(cieData.cie1964Observer10Degree5nmData(cs));
                    degree10nm5String = cieData.cie1964Observer10Degree5nmString(cs);

                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    d2n1TableView.setItems(degree2nm1Data);
                    d2n1Area.setText(degree2nm1String);

                    d10n1TableView.setItems(degree10nm1Data);
                    d10n1Area.setText(degree10nm1String);

                    d2n5TableView.setItems(degree2nm5Data);
                    d2n5Area.setText(degree2nm5String);

                    d10n5TableView.setItems(degree10nm5Data);
                    d10n5Area.setText(degree10nm5String);

                    afterInitCIEData();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    private void afterInitCIEData() {
        loadTableData();
        colorSetController.init(this, baseName + "Color", Color.THISTLE);
    }

    private void loadTableData() {
        try {
            tableBox.getChildren().clear();
            RadioButton selected = (RadioButton) dataGroup.getSelectedToggle();
            if (message("Calculate").equals(selected.getText())) {

                tableBox.getChildren().addAll(dataToolbar, calculateBox);

            } else if (message("Input").equals(selected.getText())) {

                tableBox.getChildren().addAll(dataToolbar, inputBox);

            }

            FxmlControl.refreshStyle(tableBox);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
        if (event.isControlDown() && event.getCode() != null) {
            switch (event.getCode()) {
                case DIGIT2:
                    paneSizeDiagram();
                    return;
                case DIGIT3:
                    zoomInDiagram();
                    return;
                case DIGIT4:
                    zoomOutDiagram();
                    return;
            }
        }
        super.keyEventsHandler(event);
    }

    @Override
    public void sourceFileChanged(final File file) {
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private String texts;

                @Override
                protected boolean handle() {
                    texts = FileTools.readTexts(file);
                    return texts != null;
                }

                @Override
                protected void whenSucceeded() {
                    sourceInputArea.setStyle(null);
                    inputInit = false;
//                            bottomLabel.setText(file.getAbsolutePath() + "\t" + AppVariables.getMessage("ChromaticityDiagramComments"));
                    isSettingValues = true;
                    sourceInputArea.setText(texts);
                    sourceInputArea.home();
                    isSettingValues = false;
                    checkInputs();
                }

                @Override
                protected void whenFailed() {
                    popError(AppVariables.message("NoData"));
                    sourceDataArea.clear();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void displayDataAction() {
        if (sourceDataArea.getText().isEmpty()) {
            return;
        }
        isSettingValues = true;
        inputCheck.setSelected(true);
        isSettingValues = false;
        displayChromaticityDiagram();
    }

    @FXML
    public void popDiagramPath(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistoryTools.getRecentPath(VisitHistory.FileType.Image);
            }

            @Override
            public void handleSelect() {
                saveAction();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                handleTargetPath(fname);
            }

        }.pop();
    }

    @FXML
    public void pop21Path(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistoryTools.getRecentPath(VisitHistory.FileType.Text);
            }

            @Override
            public void handleSelect() {
                export21Action();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                handleTargetPath(fname);
            }

        }.pop();
    }

    @FXML
    public void pop25Path(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistoryTools.getRecentPath(VisitHistory.FileType.Text);
            }

            @Override
            public void handleSelect() {
                export25Action();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                handleTargetPath(fname);
            }

        }.pop();
    }

    @FXML
    public void pop101Path(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistoryTools.getRecentPath(VisitHistory.FileType.Text);
            }

            @Override
            public void handleSelect() {
                export101Action();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                handleTargetPath(fname);
            }

        }.pop();
    }

    @FXML
    public void pop105Path(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistoryTools.getRecentPath(VisitHistory.FileType.Text);
            }

            @Override
            public void handleSelect() {
                export105Action();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                handleTargetPath(fname);
            }

        }.pop();
    }

    @FXML
    public void zoomInDiagram() {
        FxmlControl.zoomIn(cieDiagramScroll, cieDiagram, 20, 20);
    }

    @FXML
    public void zoomOutDiagram() {
        FxmlControl.zoomOut(cieDiagramScroll, cieDiagram, 20, 20);
    }

    @FXML
    public void paneSizeDiagram() {
        FxmlControl.paneSize(cieDiagramScroll, cieDiagram);
    }

    @FXML
    @Override
    public void clearAction() {
        isSettingValues = true;

        cdProPhotoCheck.setSelected(false);
        cdColorMatchCheck.setSelected(false);
        cdNTSCCheck.setSelected(false);
        cdPALCheck.setSelected(false);
        cdAppleCheck.setSelected(false);
        cdAdobeCheck.setSelected(false);
        cdSRGBCheck.setSelected(false);
        cdECICheck.setSelected(false);
        cdCIECheck.setSelected(false);
        cdSMPTECCheck.setSelected(false);
        degree2Check.setSelected(false);
        degree10Check.setSelected(false);
        waveCheck.setSelected(false);
        inputCheck.setSelected(false);
        calculateCheck.setSelected(false);
        whitePointsCheck.setSelected(false);

        isSettingValues = false;

        displayChromaticityDiagram();
    }

    @FXML
    @Override
    public void allAction() {
        isSettingValues = true;

        cdProPhotoCheck.setSelected(true);
        cdColorMatchCheck.setSelected(true);
        cdNTSCCheck.setSelected(true);
        cdPALCheck.setSelected(true);
        cdAppleCheck.setSelected(true);
        cdAdobeCheck.setSelected(true);
        cdSRGBCheck.setSelected(true);
        cdECICheck.setSelected(true);
        cdCIECheck.setSelected(true);
        cdSMPTECCheck.setSelected(true);
        degree2Check.setSelected(true);
        degree10Check.setSelected(true);
        waveCheck.setSelected(true);
        inputCheck.setSelected(true);
        calculateCheck.setSelected(true);
        whitePointsCheck.setSelected(true);

        isSettingValues = false;

        displayChromaticityDiagram();
    }

    protected void calculateColor() {
        CIEData d = new CIEData((Color) colorSetController.rect.getFill());
        isSettingValues = true;
        XInput.setText(scale(d.getX(), 8) + "");
        YInput.setText(scale(d.getY(), 8) + "");
        ZInput.setText(scale(d.getZ(), 8) + "");
        calculateXYZAction();
        isSettingValues = false;
        if (calculateCheck.isSelected()) {
            displayChromaticityDiagram();
        }
    }

    @FXML
    public void calculateXYZAction() {
        CIEData d = new CIEData(-1, X, Y, Z);
        xInput.setText(scale(d.getNormalizedX(), 8) + "");
        yInput.setText(scale(d.getNormalizedY(), 8) + "");
        displayCalculatedValued();
    }

    @FXML
    public void calculateXYAction() {
        CIEData d = new CIEData(x, y);
        XInput.setText(scale(d.getX(), 8) + "");
        YInput.setText(scale(d.getY(), 8) + "");
        ZInput.setText(scale(d.getZ(), 8) + "");
        displayCalculatedValued();
    }

    private void displayCalculatedValued() {
        if (x >= 0 && x <= 1 && y > 0 && y <= 1 && (x + y) <= 1) {
            double[] srgb = CIEColorSpace.XYZd50toSRGBd65(X, Y, Z);
            if (!isSettingValues) {
                isSettingValues = true;
                Color pColor = new Color((float) srgb[0], (float) srgb[1], (float) srgb[2], 1d);
                colorSetController.rect.setFill(pColor);
                isSettingValues = false;
            }
            Color pColor = (Color) colorSetController.rect.getFill();
            calculateColor = new java.awt.Color((float) pColor.getRed(), (float) pColor.getGreen(), (float) pColor.getBlue());

            List<ColorValue> values = new ArrayList<>();
            double[] cieLab = CIEColorSpace.XYZd50toCIELab(X, Y, Z);
            values.add(new ColorValue("CIE-L*ab", "D50", cieLab));

            double[] LCHab = CIEColorSpace.LabtoLCHab(cieLab);
            values.add(new ColorValue("LCH(ab)", "D50", LCHab));

            double[] cieLuv = CIEColorSpace.XYZd50toCIELuv(X, Y, Z);
            values.add(new ColorValue("CIE-L*uv", "D50", cieLuv));

            double[] LCHuv = CIEColorSpace.LuvtoLCHuv(cieLuv);
            values.add(new ColorValue("LCH(uv)", "D50", LCHuv));

            double[] hsb = {pColor.getHue(), pColor.getSaturation(),
                pColor.getBrightness()};
            values.add(new ColorValue("HSB", "D65", hsb));

            values.add(new ColorValue("sRGB", "D65 sRGB_Gamma", srgb, 255));
            double[] sRGBLinear = CIEColorSpace.XYZd50toSRGBd65Linear(X, Y, Z);
            values.add(new ColorValue("sRGB", "D65 Linear", sRGBLinear, 255));

            double[] adobeRGB = CIEColorSpace.XYZd50toAdobeRGBd65(X, Y, Z);
            values.add(new ColorValue("Adobe RGB", "D65 Gamma 2.2", adobeRGB, 255));

            double[] adobeRGBLinear = CIEColorSpace.XYZd50toAdobeRGBd65Linear(X, Y, Z);
            values.add(new ColorValue("Adobe RGB", "D65 Linear", adobeRGBLinear, 255));

            double[] appleRGB = CIEColorSpace.XYZd50toAppleRGBd65(X, Y, Z);
            values.add(new ColorValue("Apple RGB", "D65 Gamma 1.8", appleRGB, 255));

            double[] appleRGBLinear = CIEColorSpace.XYZd50toAppleRGBd65Linear(X, Y, Z);
            values.add(new ColorValue("Apple RGB", "D65 Linear", appleRGBLinear, 255));

            float[] eciRGB = SRGB.srgb2profile(ImageValue.eciRGBProfile(), pColor);
            values.add(new ColorValue("ECI RGB", "D50", FloatTools.toDouble(eciRGB), 255));

            double[] cmyk = SRGB.rgb2cmyk(pColor);
            values.add(new ColorValue("Calculated CMYK", "D65 sRGB_Gamma", cmyk, 100));

            float[] cmyk2 = SRGB.srgb2profile(ImageValue.eciCmykProfile(), pColor);
            values.add(new ColorValue("ECI CMYK", "D65 sRGB_Gamma", FloatTools.toDouble(cmyk2), 100));

            cmyk2 = SRGB.srgb2profile(ImageValue.adobeCmykProfile(), pColor);
            values.add(new ColorValue("Adobe CMYK Uncoated FOGRA29", "D65 sRGB_Gamma", FloatTools.toDouble(cmyk2), 100));

            calculatedValues.clear();
            calculatedValues.addAll(values);
            calculatedValuesTable.setItems(calculatedValues);
            calculatedValuesTable.refresh();

            if (calculateCheck.isSelected()) {
                displayChromaticityDiagram();
            }

        } else {
            calculateColor = null;
            calculatedValues.clear();
            calculatedValuesTable.refresh();
        }

    }

    private void displayChromaticityDiagram() {
        if (isSettingValues) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image image;

                @Override
                protected boolean handle() {
                    try {
                        LinkedHashMap<ChromaticityDiagram.DataType, Boolean> selections = new LinkedHashMap();
                        selections.put(DataType.CIE2Degree, degree2Check.isSelected());
                        selections.put(DataType.CIE10Degree, degree10Check.isSelected());
                        selections.put(DataType.CIEDataSource, inputCheck.isSelected());
                        selections.put(DataType.Calculate, calculateCheck.isSelected());
                        selections.put(DataType.Wave, waveCheck.isSelected());
                        selections.put(DataType.WhitePoints, whitePointsCheck.isSelected());
                        selections.put(DataType.Grid, gridCheck.isSelected());
                        selections.put(DataType.CIELines, cdCIECheck.isSelected());
                        selections.put(DataType.ECILines, cdECICheck.isSelected());
                        selections.put(DataType.sRGBLines, cdSRGBCheck.isSelected());
                        selections.put(DataType.AdobeLines, cdAdobeCheck.isSelected());
                        selections.put(DataType.AppleLines, cdAppleCheck.isSelected());
                        selections.put(DataType.PALLines, cdPALCheck.isSelected());
                        selections.put(DataType.NTSCLines, cdNTSCCheck.isSelected());
                        selections.put(DataType.ColorMatchLines, cdColorMatchCheck.isSelected());
                        selections.put(DataType.ProPhotoLines, cdProPhotoCheck.isSelected());
                        selections.put(DataType.SMPTECLines, cdSMPTECCheck.isSelected());

                        ChromaticityDiagram cd = new ChromaticityDiagram();
                        cd.setIsLine(isLine);
                        cd.setDotSize(dotSize);
                        cd.setBgColor(bgColor);
                        cd.setFontSize(fontSize);
                        cd.setDataSourceTexts(sourceDataArea.getText());
                        if (x >= 0 && x <= 1 && y > 0 && y <= 1) {
                            cd.setCalculateX(x);
                            cd.setCalculateY(y);
                            cd.setCalculateColor(calculateColor);
                        }
                        image = SwingFXUtils.toFXImage(cd.drawData(selections), null);

                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.debug(e.toString());
                    }
                    return image != null;
                }

                @Override
                protected void whenSucceeded() {
                    cieDiagram.setImage(image);
                    FxmlControl.paneSize(cieDiagramScroll, cieDiagram);
                    d2n1Area.home();
                    d2n5Area.home();
                    d10n1Area.home();
                    d10n5Area.home();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @FXML
    @Override
    public void saveAction() {
        final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                "ChromaticityDiagram", CommonFxValues.ImageExtensionFilter, true);
        if (file == null) {
            return;
        }
        recordFileWritten(file, VisitHistory.FileType.Image);

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    String format = FileTools.getFileSuffix(file.getName());
                    final BufferedImage bufferedImage = FxmlImageManufacture.bufferedImage(cieDiagram.getImage());
                    if (this == null || this.isCancelled()) {
                        return false;
                    }
                    ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    FxmlStage.openImageViewer(null, file);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void exportAction(String filename, TextArea textArea) {
        final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                filename, CommonFxValues.TextExtensionFilter, true);
        if (file == null) {
            return;
        }
        recordFileWritten(file, VisitHistory.FileType.Text);

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    return FileTools.writeFile(file, textArea.getText()) != null;
                }

                @Override
                protected void whenSucceeded() {
                    view(file);
                    popSuccessful();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void export21Action() {
        exportAction("CIE1931Observer2Degree1nm", d2n1Area);
    }

    @FXML
    public void export25Action() {
        exportAction("CIE1931Observer2Degree5nm", d2n5Area);
    }

    @FXML
    public void export101Action() {
        exportAction("CIE1964Observer10Degree1nm", d10n1Area);
    }

    @FXML
    public void export105Action() {
        exportAction("CIE1964Observer10Degree5nm", d10n5Area);
    }

}
