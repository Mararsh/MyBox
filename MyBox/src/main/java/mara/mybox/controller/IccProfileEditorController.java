package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import mara.mybox.controller.base.BaseController;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.color.IccTagType;
import mara.mybox.color.IccHeader;
import mara.mybox.color.IccProfile;
import mara.mybox.color.IccTag;
import mara.mybox.color.IccTags;
import mara.mybox.color.IccXML;
import mara.mybox.tools.ByteTools;
import static mara.mybox.tools.ByteTools.bytesToHexFormat;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.CommonValues;
import static mara.mybox.value.CommonValues.Indent;

/**
 * @Author Mara
 * @CreateDate 2019-5-13
 * @Description
 * @License Apache License Version 2.0
 */
public class IccProfileEditorController extends BaseController {

    protected String embedICC;
    protected boolean isIccFile, inputsValid;
    protected IccProfile profile;
    private IccHeader header;
    private IccTags tags;
    protected ObservableList<IccTag> tagsTable = FXCollections.observableArrayList();

    @FXML
    protected ComboBox<String> embedBox, cmmTypeBox, deviceClassBox, colorSpaceBox, PCSTypeBox,
            platformBox, manufacturerBox, intentBox, creatorBox;
    @FXML
    protected TextField profileVersionInput, createTimeInput, profileFileInput, deviceModelInput,
            xInput, yInput, zInput, profileIdInput, spectralPCSInput, spectralPCSRangeInput, bispectralPCSRangeInput,
            mcsInput, subClassInput, subclassVersionInput,
            xOutput, yOutput, zOutput,
            tagDisplay, tagNameDisplay, tagTypeDisplay, tagOffsetDisplay, tagSizeDisplay;
    @FXML
    protected CheckBox saveConfirmCheck, embedCheck, independentCheck, subsetCheck,
            transparentcyCheck, matteCheck, negativeCheck, bwCheck, paperCheck, texturedCheck,
            isotropicCheck, selfLuminousCheck, idAutoCheck, lutNormalizeCheck, openExportCheck;
    @FXML
    protected Label cmmTypeMarkLabel, deviceClassMarkLabel, colorSpaceMarkLabel, PCSTypeMarkLabel,
            platformMarkLabel, manufacturerMarkLabel, intentMarkLabel, creatorMarkLabel,
            profileVersionMarkLabel, createTimeMarkLabel, profileFileMarkLabel, deviceModelMarkLabel,
            xMarkLabel, yMarkLabel, zMarkLabel, embedMarkLabel, independentMarkLabel, subsetMarkLabel,
            transparentcyMarkLabel, matteMarkLabel, negativeMarkLabel, bwMarkLabel, paperMarkLabel, texturedMarkLabel,
            isotropicMarkLabel, selfLuminousMarkLabel, cieDataLabel;
    @FXML
    protected TextArea summaryArea, xmlArea, tagDescDisplay, tagDataDisplay, tagBytesDisplay;
    @FXML
    protected TableView<IccTag> tagsTableView;
    @FXML
    protected TableColumn<IccTag, String> tagColumn, nameColumn, typeColumn;
    @FXML
    protected TableColumn<IccTag, Integer> offsetColumn, sizeColumn;
    @FXML
    protected VBox headerBox, tagDataBox, csInputBox, chromaticDiagramBox;
    @FXML
    protected TabPane displayPane;
    @FXML
    protected Tab tagDataTab;
    @FXML
    protected Button refreshHeaderButton, refreshXmlButton, exportXmlButton;

    private enum SourceType {
        Embed, Internal_File, External_File
    };

    public IccProfileEditorController() {
        baseTitle = AppVaribles.getMessage("IccProfileEditor");

        TipsLabelKey = "IccProfileTips";

        SourceFileType = VisitHistory.FileType.Icc;
        SourcePathType = VisitHistory.FileType.Icc;
        TargetPathType = VisitHistory.FileType.Icc;
        TargetFileType = VisitHistory.FileType.Icc;
        AddFileType = VisitHistory.FileType.Icc;
        AddPathType = VisitHistory.FileType.Icc;

        defaultPathKey = SystemTools.IccProfilePath();

        fileExtensionFilter = CommonValues.IccProfileExtensionFilter;
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();
            sourceFile = null;
            embedICC = null;

            initToolbar();
            initHeaderControls();
            initTagsTable();
            initDataTable();
            initOptions();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initToolbar() {
        try {
            embedBox.getItems().addAll(Arrays.asList(
                    "sRGB", "XYZ", "PYCC", "GRAY", "LINEAR_RGB",
                    "ECI_RGB_v2_ICCv4.icc", "ECI_CMYK.icc",
                    "AdobeRGB_1998.icc", "Adobe_AppleRGB.icc", "Adobe_ColorMatchRGB.icc",
                    "AdobeCMYK_CoatedFOGRA27.icc", "AdobeCMYK_CoatedFOGRA39.icc", "AdobeCMYK_UncoatedFOGRA29.icc", "AdobeCMYK_WebCoatedFOGRA28.icc",
                    "AdobeCMYK_JapanColor2001Coated.icc", "AdobeCMYK_JapanColor2001Uncoated.icc", "AdobeCMYK_JapanColor2002Newspaper.icc",
                    "AdobeCMYK_JapanWebCoated.icc", "AdobeCMYK_USSheetfedCoated.icc", "AdobeCMYK_USSheetfedUncoated.icc",
                    "AdobeCMYK_USWebCoatedSWOP.icc", "AdobeCMYK_USWebUncoated.icc"
            ));
            embedBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    iccChanged();
                }
            });
            embedBox.getSelectionModel().select(0);

            saveConfirmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean oldValue, Boolean newValue) {
                    AppVaribles.setUserConfigValue("IccEditerConfirmSave", newValue);
                }
            });
            saveConfirmCheck.setSelected(AppVaribles.getUserConfigBoolean("IccEditerConfirmSave", true));

//            saveButton.disableProperty().bind(profileVersionInput.styleProperty().isEqualTo(badStyle)
//                    .or(createTimeInput.styleProperty().isEqualTo(badStyle))
//                    .or(xInput.styleProperty().isEqualTo(badStyle))
//                    .or(yInput.styleProperty().isEqualTo(badStyle))
//                    .or(zInput.styleProperty().isEqualTo(badStyle))
//            );
//            saveAsButton.disableProperty().bind(saveButton.disableProperty()
//            );
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initHeaderControls() {
        try {
            List<String> manuList = new ArrayList();
            for (String[] item : IccHeader.DeviceManufacturers) {
                manuList.add(item[0] + Indent + item[1]);
            }
            cmmTypeBox.getItems().addAll(manuList);
            cmmTypeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (!cmmTypeBox.getItems().contains(newValue)) {
                        FxmlControl.setEditorWarnStyle(cmmTypeBox);
                    } else {
                        FxmlControl.setEditorNormal(cmmTypeBox);
                    }
                    if (isSettingValues) {
                        return;
                    }
                    cmmTypeMarkLabel.setText("*");
                    profileChanged();
                }
            });

            profileVersionInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        float v = Float.parseFloat(newValue);
                        profileVersionInput.setStyle(null);
                    } catch (Exception e) {
                        profileVersionInput.setStyle(badStyle);
                    }
                    if (isSettingValues) {
                        return;
                    }
                    profileVersionMarkLabel.setText("*");
                    profileChanged();
                }
            });

            List<String> classList = new ArrayList();
            for (String[] item : IccHeader.ProfileDeviceClasses) {
                classList.add(item[0] + Indent + getMessage(item[1]));
            }
            deviceClassBox.getItems().addAll(classList);
            deviceClassBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    deviceClassMarkLabel.setText("*");
                    profileChanged();
                }
            });

            List<String> csList = Arrays.asList(IccHeader.ColorSpaceTypes);
            colorSpaceBox.getItems().addAll(csList);
            colorSpaceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (!colorSpaceBox.getItems().contains(newValue)) {
                        FxmlControl.setEditorWarnStyle(colorSpaceBox);
                    } else {
                        FxmlControl.setEditorNormal(colorSpaceBox);
                    }
                    if (isSettingValues) {
                        return;
                    }
                    colorSpaceMarkLabel.setText("*");
                    profileChanged();
                }
            });

            PCSTypeBox.getItems().addAll(Arrays.asList(IccHeader.PCSTypes));
            PCSTypeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    PCSTypeMarkLabel.setText("*");
                    profileChanged();
                }
            });

            createTimeInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (IccTagType.dateTimeBytes(newValue) != null) {
                        createTimeInput.setStyle(null);
                    } else {
                        createTimeInput.setStyle(badStyle);
                    }
                    if (isSettingValues) {
                        return;
                    }
                    createTimeMarkLabel.setText("*");
                    profileChanged();
                }
            });

            profileFileInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    profileFileMarkLabel.setText("*");
                    profileChanged();
                }
            });

            List<String> platformsList = new ArrayList();
            for (String[] item : IccHeader.PrimaryPlatforms) {
                platformsList.add(item[0] + Indent + item[1]);
            }
            platformBox.getItems().addAll(platformsList);
            platformBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    platformMarkLabel.setText("*");
                    profileChanged();
                }
            });

            embedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    embedMarkLabel.setText("*");
                    profileChanged();
                }
            });

            independentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    independentMarkLabel.setText("*");
                    profileChanged();
                }
            });

            subsetCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    subsetMarkLabel.setText("*");
                    profileChanged();
                }
            });

            manufacturerBox.getItems().addAll(manuList);
            manufacturerBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (!manufacturerBox.getItems().contains(newValue)) {
                        FxmlControl.setEditorWarnStyle(manufacturerBox);
                    } else {
                        FxmlControl.setEditorNormal(manufacturerBox);
                    }
                    if (isSettingValues) {
                        return;
                    }
                    manufacturerMarkLabel.setText("*");
                    profileChanged();
                }
            });

            deviceModelInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    deviceModelMarkLabel.setText("*");
                    profileChanged();
                }
            });

            transparentcyCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    transparentcyMarkLabel.setText("*");
                    profileChanged();
                }
            });

            matteCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    matteMarkLabel.setText("*");
                    profileChanged();
                }
            });

            negativeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    negativeMarkLabel.setText("*");
                    profileChanged();
                }
            });

            bwCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    bwMarkLabel.setText("*");
                    profileChanged();
                }
            });

            paperCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    paperMarkLabel.setText("*");
                    profileChanged();
                }
            });

            texturedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    texturedMarkLabel.setText("*");
                    profileChanged();
                }
            });

            isotropicCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    isotropicMarkLabel.setText("*");
                    profileChanged();
                }
            });

            selfLuminousCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    selfLuminousMarkLabel.setText("*");
                    profileChanged();
                }
            });

            List<String> intents = new ArrayList();
            for (String item : IccHeader.RenderingIntents) {
                intents.add(getMessage(item));
            }
            intentBox.getItems().addAll(intents);
            intentBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    intentMarkLabel.setText("*");
                    profileChanged();
                }
            });

            xInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        double v = Double.parseDouble(newValue);
                        xInput.setStyle(null);
                    } catch (Exception e) {
                        xInput.setStyle(badStyle);
                    }
                    if (isSettingValues) {
                        return;
                    }
                    xMarkLabel.setText("*");
                    profileChanged();
                }
            });

            yInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        double v = Double.parseDouble(newValue);
                        yInput.setStyle(null);
                    } catch (Exception e) {
                        yInput.setStyle(badStyle);
                    }
                    if (isSettingValues) {
                        return;
                    }
                    yMarkLabel.setText("*");
                    profileChanged();
                }
            });

            zInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        double v = Double.parseDouble(newValue);
                        zInput.setStyle(null);
                    } catch (Exception e) {
                        zInput.setStyle(badStyle);
                    }
                    if (isSettingValues) {
                        return;
                    }
                    zMarkLabel.setText("*");
                    profileChanged();
                }
            });

            creatorBox.getItems().addAll(manuList);
            creatorBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (!creatorBox.getItems().contains(newValue)) {
                        FxmlControl.setEditorWarnStyle(creatorBox);
                    } else {
                        FxmlControl.setEditorNormal(creatorBox);
                    }
                    if (isSettingValues) {
                        return;
                    }
                    creatorMarkLabel.setText("*");
                    profileChanged();
                }
            });
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initTagsTable() {
        tagColumn.setCellValueFactory(new PropertyValueFactory<IccTag, String>("tag"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<IccTag, String>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<IccTag, String>("type"));
        offsetColumn.setCellValueFactory(new PropertyValueFactory<IccTag, Integer>("offset"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<IccTag, Integer>("size"));

        tagsTableView.setItems(tagsTable);
        tagsTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object t, Object t1) {
                IccTag selected = tagsTableView.getSelectionModel().getSelectedItem();
                displayTagData(selected);
            }
        });
    }

    protected void initDataTable() {

    }

    protected void initOptions() {
        lutNormalizeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                AppVaribles.setUserConfigValue("LutNormalize", newValue);
            }
        });
        lutNormalizeCheck.setSelected(AppVaribles.getUserConfigBoolean("LutNormalize", true));
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        String name = getCurrentName();
        if (name != null) {
            myStage.setTitle(getBaseTitle() + "  " + name);
        }
    }

    private String getCurrentName() {
        if (isIccFile) {
            if (sourceFile != null) {
                return sourceFile.getAbsolutePath();
            }
        } else {
            if (embedICC != null) {
                return getMessage("JavaEmbeddedColorModel") + ": " + embedICC;
            }
        }
        return null;
    }

    @Override
    public void selectSourceFileDo(File file) {
        if (file == null) {
            return;
        }
        recordFileOpened(file);
        openProfile(SourceType.External_File, file.getAbsolutePath());
    }

    private void iccChanged() {
        if (isSettingValues) {
            return;
        }
        String name = embedBox.getSelectionModel().getSelectedItem();
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        String fname = null;
        switch (name) {
            case "sRGB":
            case "XYZ":
            case "PYCC":
            case "GRAY":
            case "LINEAR_RGB":
                openProfile(SourceType.Embed, name);
                break;
            default:
                openProfile(SourceType.Internal_File, name);
        }

    }

    private void openProfile(final SourceType type, final String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                return;
            }
            final String inputName;
            if (type == SourceType.Embed) {
                inputName = getMessage("JavaEmbeddedColorModel") + ": " + name;
            } else {
                inputName = getMessage("File") + ": " + name;
            }
            if (task != null && task.isRunning()) {
                task.cancel();
            }
            task = new Task<Void>() {
                private boolean ok;
                private String error;
                private File file;
                private IccProfile p;
                private byte[] data;

                @Override
                protected Void call() throws Exception {
                    try {
                        data = null;
                        switch (type) {
                            case Embed:
                                p = new IccProfile(name);
                                data = p.getData();
                                break;
                            case Internal_File:
                                file = FxmlControl.getUserFile("/data/ICC/" + name, name);
                                p = new IccProfile(file);
                                data = FileTools.readBytes(file);
                                p.setData(data);
                                break;
                            case External_File:
                                file = new File(name);
                                p = new IccProfile(file);
                                data = FileTools.readBytes(file);
                                p.setData(data);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                    }
                    ok = p != null && p.getHeader() != null;
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (ok) {
                                String displayName;
                                isIccFile = type != SourceType.Embed;
                                if (isIccFile) {
                                    sourceFile = file;
                                    isSettingValues = true;
                                    embedICC = null;
                                    isSettingValues = false;
                                    displayName = AppVaribles.getMessage("File") + ": " + name;
                                } else {
                                    embedICC = name;
                                    sourceFile = null;
                                    displayName = AppVaribles.getMessage("JavaEmbeddedColorModel") + ": " + name;
                                }
                                if (type == SourceType.External_File) {
                                    embedBox.getSelectionModel().clearSelection();
                                }
                                profile = p;
                                profile.setNormalizeLut(lutNormalizeCheck.isSelected());
                                header = profile.getHeader();
                                tags = profile.getTags();
                                bottomLabel.setStyle(null);
                                bottomLabel.setText(displayName);
                                if (myStage != null) {
                                    myStage.setTitle(getBaseTitle() + "  " + getCurrentName());
                                }
                                resetMarkLabel(headerBox);
                                inputsValid = true;
                                loadProfileData();
                                if (!profile.isIsValid()) {
                                    popError(AppVaribles.getMessage("IccInvalid"), 6000);
                                }
                            } else {
                                if (error == null) {
                                    if (p != null && p.getError() != null) {
                                        error = p.getError();
                                    } else {
                                        error = AppVaribles.getMessage("Invalid");
                                    }
                                }
                                popError(inputName + " " + error);
                            }
                        }
                    });

                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL, inputName + " " + getMessage("Loading..."));
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void loadProfileData() {
        if (header == null) {
            return;
        }

        LoadingController contoller = openHandlingStage(Modality.WINDOW_MODAL, getMessage("Loading..."));
        isSettingValues = true;
        try {

            displaySummary();

            initHeaderInputs();

            makeTagsInputs();

            displayXML();

            displayTagsTable();

        } catch (Exception e) {
            logger.debug(e.toString());

        }
        isSettingValues = false;

        contoller.closeStage();

//        displayColorSpace();
    }

    private void displaySummary() {
        summaryArea.clear();
        if (header == null) {
            return;
        }
        try {
            LinkedHashMap<String, IccTag> fields = header.getFields();
            String s = getMessage("ProfileSize") + ": " + header.value("ProfileSize");
            List<IccTag> tagsList = tags.getTags();
            if (tagsList != null) {
                s += "   " + getMessage("TagsNumber") + ": " + tagsList.size();
            }
            String name = getCurrentName();
            bottomLabel.setText(name + "   " + s);

            s = name + "\n" + s + "\n\n";
            for (String key : fields.keySet()) {
                IccTag field = fields.get(key);
                switch (key) {
                    case "ProfileFlagIndependently":
                    case "ProfileFlagMCSSubset":
                    case "DeviceAttributeMatte":
                    case "DeviceAttributeNegative":
                    case "DeviceAttributeBlackOrWhite":
                    case "DeviceAttributePaperBased":
                    case "DeviceAttributeTextured":
                    case "DeviceAttributeIsotropic":
                    case "DeviceAttributeSelfLuminous":
                    case "PCCIlluminantY":
                    case "PCCIlluminantZ":
                        continue;
                    case "ProfileFlagEmbedded":
                        s += getMessage("ProfileFlags") + ": ";
                        s += ((boolean) header.value("ProfileFlagEmbedded") ? getMessage("NotEmbedded") : getMessage("NotEmbedded")) + "  ";
                        s += ((boolean) header.value("ProfileFlagIndependently") ? getMessage("Independent") : getMessage("NotIndependent")) + "  ";
                        s += ((boolean) header.value("ProfileFlagMCSSubset") ? getMessage("MCSSubset") : getMessage("MCSNotSubset"));
                        s += " (" + header.hex(44, 4) + ")\n";
                        break;
                    case "DeviceAttributeTransparency":
                        s += getMessage("DeviceAttributes") + ": ";
                        s += ((boolean) header.value("DeviceAttributeTransparency") ? getMessage("Transparency") : getMessage("Reflective")) + "  ";
                        s += ((boolean) header.value("DeviceAttributeMatte") ? getMessage("Matte") : getMessage("Glossy")) + "  ";
                        s += ((boolean) header.value("DeviceAttributeNegative") ? getMessage("Negative") : getMessage("Positive")) + "  ";
                        s += ((boolean) header.value("DeviceAttributeBlackOrWhite") ? getMessage("BlackOrWhite") : getMessage("Colorful")) + "  ";
                        s += ((boolean) header.value("DeviceAttributePaperBased") ? getMessage("PaperBased") : getMessage("NonPaperBased")) + "  ";
                        s += ((boolean) header.value("DeviceAttributeTextured") ? getMessage("Textured") : getMessage("NonTextured")) + "  ";
                        s += ((boolean) header.value("DeviceAttributeIsotropic") ? getMessage("Isotropic") : getMessage("NonIsotropic")) + "  ";
                        s += ((boolean) header.value("DeviceAttributeSelfLuminous") ? getMessage("SelfLuminous") : getMessage("NonSelfLuminous"));
                        s += " (" + header.hex(56, 8) + ")\n";
                        break;
                    case "PCCIlluminantX":
                        s += getMessage("ConnectionSpaceIlluminant") + ": ";
                        s += header.value("PCCIlluminantX") + "  ";
                        s += header.value("PCCIlluminantY") + "  ";
                        s += header.value("PCCIlluminantZ");
                        s += " (" + header.hex(68, 12) + ")\n";
                        break;
                    default:
                        s += getMessage(field.getTag()) + ": " + field.getValue();
                        if (field.getType() != IccTag.TagType.Bytes) {
                            s += " (" + bytesToHexFormat(field.getBytes()) + ")";
                        }
                        s += "\n";
                        break;
                }

            }

            s += "\n\n" + getMessage("HeaderBytes") + ": \n" + ByteTools.bytesToHexFormat(header.getHeader());
            summaryArea.setText(s);

        } catch (Exception e) {
            logger.debug(e.toString());

        }

    }

    private void initHeaderInputs() {
        if (header == null) {
            return;
        }
        try {
            cmmTypeBox.getSelectionModel().select((String) header.value("CMMType"));
            profileVersionInput.setText(header.value("ProfileVersion") + "");
            deviceClassBox.getSelectionModel().select((String) header.value("ProfileDeviceClass"));
            colorSpaceBox.getSelectionModel().select((String) header.value("ColorSpaceType"));
            PCSTypeBox.getSelectionModel().select((String) header.value("PCSType"));
            createTimeInput.setText((String) header.value("CreateTime"));
            profileFileInput.setText((String) header.value("ProfileFile"));
            platformBox.getSelectionModel().select((String) header.value("PrimaryPlatform"));
            embedCheck.setSelected((boolean) header.value("ProfileFlagEmbedded"));
            independentCheck.setSelected((boolean) header.value("ProfileFlagIndependently"));
            subsetCheck.setSelected((boolean) header.value("ProfileFlagMCSSubset"));
            manufacturerBox.getSelectionModel().select((String) header.value("DeviceManufacturer"));
            deviceModelInput.setText((String) header.value("DeviceModel"));
            transparentcyCheck.setSelected((boolean) header.value("DeviceAttributeTransparency"));
            matteCheck.setSelected((boolean) header.value("DeviceAttributeMatte"));
            negativeCheck.setSelected((boolean) header.value("DeviceAttributeNegative"));
            bwCheck.setSelected((boolean) header.value("DeviceAttributeBlackOrWhite"));
            paperCheck.setSelected((boolean) header.value("DeviceAttributePaperBased"));
            texturedCheck.setSelected((boolean) header.value("DeviceAttributeTextured"));
            isotropicCheck.setSelected((boolean) header.value("DeviceAttributeIsotropic"));
            selfLuminousCheck.setSelected((boolean) header.value("DeviceAttributeSelfLuminous"));
            intentBox.getSelectionModel().select((String) header.value("RenderingIntent"));
            xInput.setText(header.value("PCCIlluminantX") + "");
            yInput.setText(header.value("PCCIlluminantY") + "");
            zInput.setText(header.value("PCCIlluminantZ") + "");
            creatorBox.getSelectionModel().select((String) header.value("Creator"));
            profileIdInput.setText((String) header.value("ProfileID"));
            spectralPCSInput.setText((String) header.value("SpectralPCS"));
            spectralPCSRangeInput.setText((String) header.value("SpectralPCSWaveLengthRange"));
            bispectralPCSRangeInput.setText((String) header.value("BispectralPCSWaveLengthRange"));
            mcsInput.setText((String) header.value("MCS"));
            subClassInput.setText((String) header.value("ProfileDeviceSubclass"));
            subclassVersionInput.setText(header.value("ProfileSubclassVersion") + "");
        } catch (Exception e) {
            logger.debug(e.toString());

        }
    }

    private void displayTagsTable() {
        try {
            tagsTable.clear();
            if (tags == null) {
                return;
            }
            tagsTable.addAll(tags.getTags());
            tagsTableView.refresh();
        } catch (Exception e) {
            logger.debug(e.toString());

        }
    }

    private void displayXML() {
        if (header == null) {
            return;
        }
        try {
            String xml = IccXML.iccXML(header, tags);
            if (xml != null) {
                xmlArea.setText(xml);
            } else {
                xmlArea.clear();
                popError(getMessage("InvalidData"));
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            xmlArea.clear();
            popError(getMessage("InvalidData"));

        }

    }

    private void makeTagsInputs() {
        try {
            tagDataBox.getChildren().clear();
            if (tags == null) {
                return;
            }
            for (IccTag tag : tags.getTags()) {
                if (tag.getType() == null) {
                    continue;
                }

                HBox tagBox = new HBox();
                tagBox.setAlignment(Pos.CENTER_LEFT);
                tagBox.setMaxSize(Double.MAX_VALUE, Region.USE_COMPUTED_SIZE);
                tagBox.setSpacing(5);
                VBox.setVgrow(tagBox, Priority.NEVER);
                HBox.setHgrow(tagBox, Priority.ALWAYS);

                Label label = new Label(getMessage(tag.getName()));
                label.setPrefWidth(Region.USE_COMPUTED_SIZE);
                label.wrapTextProperty().setValue(true);
                tagBox.getChildren().add(label);

                switch (tag.getType()) {
                    case Text:
                    case MultiLocalizedUnicode:
                    case Signature:
                    case DateTime:
                        makeTagTextInput(tagBox, tag);
                        break;

                    case XYZ:
                        makeTagXYZNumberInput(tagBox, tag);
                        break;

                    case Curve:
                        makeTagCurveInput(tagBox, tag);
                        break;

                    case ViewingConditions:
                        makeTagViewingConditionsInput(tagBox, tag);
                        break;

                    case Measurement:
                        makeTagMeasurementInput(tagBox, tag);
                        break;

                    case S15Fixed16Array:
                        makeTagS15Fixed16ArrayInput(tagBox, tag);
                        break;

                    case LUT: {
                        Label label2 = new Label(getMessage("NotSupportEditCurrently"));
                        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
                        label.wrapTextProperty().setValue(true);
                        tagBox.getChildren().add(label2);
                    }
                    break;

                    default:
                        break;

                }

                tagDataBox.getChildren().add(tagBox);
            }
        } catch (Exception e) {
            logger.debug(e.toString());

        }
    }

    private void profileChanged() {
        myStage.setTitle(getBaseTitle() + "  " + getCurrentName() + " " + "*");
    }

    private boolean tagValueChanged(final IccTag tag, final String key, final String value) {
        if (isSettingValues || tag.getType() == null) {
            return true;
        }
        final String name;
        if (key != null) {
            name = tag.getTag() + key;
        } else {
            name = tag.getTag();
        }
        try {
            Label label = (Label) FxmlControl.findNode(thisPane, name + "MarkLabel");
            label.setText("*");
            label.setStyle("-fx-text-fill: blue; -fx-font-weight: bolder;");
            profileChanged();
        } catch (Exception e) {
        }

        if (task != null && task.isRunning()) {
            task.cancel();
        }
        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                ok = tag.update(key, value);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok) {
                            FxmlControl.setStyle(thisPane, name + "Input", null);
                        } else {
                            FxmlControl.setStyle(thisPane, name + "Input", badStyle);
                        }
                    }
                });
            }

        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    private boolean tagValueChanged(IccTag tag, String value) {
        return tagValueChanged(tag, null, value);
    }

    private void makeTagTextInput(Pane parent, final IccTag tag) {
        try {
            if (tag.getValueSelection() != null) {
                final ComboBox<String> valuesListbox = new ComboBox();
                valuesListbox.setId(tag.getTag() + "Input");
                valuesListbox.getItems().addAll(tag.getValueSelection());
                if (tag.getValue() != null) {
                    valuesListbox.getSelectionModel().select(tag.display());
                }
                valuesListbox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (tagValueChanged(tag, newValue)) {
                            valuesListbox.getEditor().setStyle(null);
                        } else {
                            valuesListbox.getEditor().setStyle(badStyle);
                        }
                    }
                });
                valuesListbox.setPrefWidth(300);
                parent.getChildren().add(valuesListbox);
            } else {
                final TextField valueInput = new TextField();
                valueInput.setId(tag.getTag() + "Input");
                valueInput.setMaxSize(Double.MAX_VALUE, Region.USE_COMPUTED_SIZE);
                HBox.setHgrow(valueInput, Priority.ALWAYS);
                if (tag.getValue() != null) {
                    valueInput.setText(tag.display());
                }
                valueInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (tagValueChanged(tag, newValue)) {
                            valueInput.setStyle(null);
                        } else {
                            valueInput.setStyle(badStyle);
                        }
                    }
                });
                parent.getChildren().add(valueInput);
            }
            Label label = new Label();
            label.setId(tag.getTag() + "MarkLabel");
            parent.getChildren().add(label);
        } catch (Exception e) {
            popError(e.toString());
        }
    }

    private void makeTagXYZNumberInput(Pane parent, final IccTag tag) {
        try {
            if (tag.getValue() == null) {
                final TextArea valueInput = new TextArea();
                HBox.setHgrow(valueInput, Priority.ALWAYS);
                valueInput.setWrapText(true);
                valueInput.setId(tag.getTag() + "Input");
                valueInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (tagValueChanged(tag, newValue)) {
                            valueInput.setStyle(null);
                        } else {
                            valueInput.setStyle(badStyle);
                        }
                    }
                });
                parent.getChildren().add(valueInput);
            } else {
                double[][] values = (double[][]) tag.getValue();
                if (tag.getValue() == null || values.length <= 1) {
                    final TextField valueInput = new TextField();
                    valueInput.setId(tag.getTag() + "Input");
                    HBox.setHgrow(valueInput, Priority.ALWAYS);
                    VBox.setVgrow(valueInput, Priority.NEVER);
                    valueInput.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue ov, String oldValue, String newValue) {
                            if (tagValueChanged(tag, newValue)) {
                                valueInput.setStyle(null);
                            } else {
                                valueInput.setStyle(badStyle);
                            }
                        }
                    });
                    valueInput.setText(tag.display());
                    valueInput.setMaxSize(Double.MAX_VALUE, Region.USE_COMPUTED_SIZE);
                    valueInput.setPrefHeight(Region.USE_COMPUTED_SIZE);
                    parent.getChildren().add(valueInput);
                } else {
                    final TextArea valueInput = new TextArea();
                    HBox.setHgrow(valueInput, Priority.ALWAYS);
                    valueInput.setWrapText(true);
                    valueInput.setId(tag.getTag() + "Input");
                    valueInput.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue ov, String oldValue, String newValue) {
                            if (tagValueChanged(tag, newValue)) {
                                valueInput.setStyle(null);
                            } else {
                                valueInput.setStyle(badStyle);
                            }
                        }
                    });
                    valueInput.setText(tag.display());
                    parent.getChildren().add(valueInput);
                }
            }
            Label label = new Label();
            label.setId(tag.getTag() + "MarkLabel");
            parent.getChildren().add(label);

        } catch (Exception e) {
            popError(e.toString());
        }

    }

    private void makeTagCurveInput(Pane parent, final IccTag tag) {
        try {
            if (tag.getValue() == null) {
                final TextArea valueInput = new TextArea();
                HBox.setHgrow(valueInput, Priority.ALWAYS);
                valueInput.setWrapText(true);
                valueInput.setId(tag.getTag() + "Input");
                valueInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (tagValueChanged(tag, newValue)) {
                            valueInput.setStyle(null);
                        } else {
                            valueInput.setStyle(badStyle);
                        }
                    }
                });
                parent.getChildren().add(valueInput);
            } else {
                double[] values = (double[]) tag.getValue();
                if (values.length <= 2) {
                    final TextField valueInput = new TextField();
                    VBox.setVgrow(valueInput, Priority.NEVER);
                    valueInput.setId(tag.getTag() + "Input");
                    HBox.setHgrow(valueInput, Priority.ALWAYS);
                    valueInput.setText(tag.display());
                    valueInput.setPrefHeight(Region.USE_COMPUTED_SIZE);
                    valueInput.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue ov, String oldValue, String newValue) {
                            if (tagValueChanged(tag, newValue)) {
                                valueInput.setStyle(null);
                            } else {
                                valueInput.setStyle(badStyle);
                            }
                        }
                    });
                    parent.getChildren().add(valueInput);
                } else {
                    final TextArea valueInput = new TextArea();
                    HBox.setHgrow(valueInput, Priority.ALWAYS);
                    valueInput.setWrapText(true);
                    valueInput.setId(tag.getTag() + "Input");
                    valueInput.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue ov, String oldValue, String newValue) {
                            if (tagValueChanged(tag, newValue)) {
                                valueInput.setStyle(null);
                            } else {
                                valueInput.setStyle(badStyle);
                            }
                        }
                    });
                    valueInput.setText(tag.display());
                    parent.getChildren().add(valueInput);
                }
            }
            Label label = new Label();
            label.setId(tag.getTag() + "MarkLabel");
            parent.getChildren().add(label);

        } catch (Exception e) {
            popError(e.toString());
        }
    }

    private void makeTagViewingConditionsInput(Pane parent, final IccTag tag) {
        try {
            VBox viewBox = new VBox();
            HBox.setHgrow(viewBox, Priority.ALWAYS);
            viewBox.setPrefHeight(Region.USE_COMPUTED_SIZE);
            Map<String, Object> values = null;
            if (tag.getValue() != null) {
                values = (Map<String, Object>) tag.getValue();
            }
            HBox illuminantBox = new HBox();
            HBox.setHgrow(illuminantBox, Priority.ALWAYS);
            VBox.setVgrow(illuminantBox, Priority.NEVER);
            illuminantBox.setSpacing(5);
            illuminantBox.setAlignment(Pos.CENTER_LEFT);
            Label illuminantLabel = new Label(getMessage("Illuminant"));
            illuminantLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
            illuminantLabel.wrapTextProperty().setValue(true);
            final TextField illuminantInput = new TextField();
            illuminantInput.setId(tag.getTag() + "IlluminantInput");
            illuminantInput.setMaxSize(Double.MAX_VALUE, Region.USE_COMPUTED_SIZE);
            illuminantInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (tagValueChanged(tag, "Illuminant", newValue)) {
                        illuminantInput.setStyle(null);
                    } else {
                        illuminantInput.setStyle(badStyle);
                    }
                }
            });
            HBox.setHgrow(illuminantInput, Priority.ALWAYS);
            if (values != null) {
                double[] illuminant = (double[]) values.get("illuminant");
                illuminantInput.setText(illuminant[0] + "  " + illuminant[1] + "  " + illuminant[2]);
            }
            Label label = new Label();
            label.setId(tag.getTag() + "IlluminantMarkLabel");
            illuminantBox.getChildren().addAll(illuminantLabel, illuminantInput, label);
            viewBox.getChildren().add(illuminantBox);

            HBox surroundBox = new HBox();
            HBox.setHgrow(surroundBox, Priority.ALWAYS);
            VBox.setVgrow(surroundBox, Priority.NEVER);
            surroundBox.setSpacing(5);
            surroundBox.setAlignment(Pos.CENTER_LEFT);
            Label surroundLabel = new Label(getMessage("Surround"));
            surroundLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
            surroundLabel.wrapTextProperty().setValue(true);
            final TextField surroundInput = new TextField();
            surroundInput.setId(tag.getTag() + "SurroundInput");
            surroundInput.setMaxSize(Double.MAX_VALUE, Region.USE_COMPUTED_SIZE);
            surroundInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (tagValueChanged(tag, "Surround", newValue)) {
                        surroundInput.setStyle(null);
                    } else {
                        surroundInput.setStyle(badStyle);
                    }
                }
            });
            HBox.setHgrow(surroundInput, Priority.ALWAYS);
            if (values != null) {
                double[] surround = (double[]) values.get("surround");
                surroundInput.setText(surround[0] + "  " + surround[1] + "  " + surround[2]);
            }
            label = new Label();
            label.setId(tag.getTag() + "SurroundMarkLabel");
            surroundBox.getChildren().addAll(surroundLabel, surroundInput, label);
            viewBox.getChildren().add(surroundBox);

            HBox typeBox = new HBox();
            HBox.setHgrow(typeBox, Priority.ALWAYS);
            VBox.setVgrow(typeBox, Priority.NEVER);
            typeBox.setSpacing(5);
            typeBox.setAlignment(Pos.CENTER_LEFT);
            Label typeLabel = new Label(getMessage("IlluminantType"));
            typeLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
            typeLabel.wrapTextProperty().setValue(true);
            final ComboBox<String> typeListbox = new ComboBox();
            typeListbox.setId(tag.getTag() + "IlluminantTypeInput");
            typeListbox.getItems().addAll(IccTagType.illuminantTypes());
            typeListbox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (tagValueChanged(tag, "IlluminantType", newValue)) {
                        typeListbox.getEditor().setStyle(null);
                    } else {
                        typeListbox.getEditor().setStyle(badStyle);
                    }
                }
            });
            if (values != null) {
                String type = (String) values.get("illuminantType");
                typeListbox.getSelectionModel().select(type);
            }
            typeListbox.setPrefWidth(300);
            label = new Label();
            label.setId(tag.getTag() + "IlluminantTypeMarkLabel");
            typeBox.getChildren().addAll(typeLabel, typeListbox, label);
            viewBox.getChildren().add(typeBox);

            parent.getChildren().add(viewBox);

        } catch (Exception e) {
            popError(e.toString());
        }

    }

    private void makeTagMeasurementInput(Pane parent, final IccTag tag) {
        try {
            VBox measurementBox = new VBox();
            HBox.setHgrow(measurementBox, Priority.ALWAYS);
            measurementBox.setPrefHeight(Region.USE_COMPUTED_SIZE);
            Map<String, Object> values = null;
            if (tag.getValue() != null) {
                values = (Map<String, Object>) tag.getValue();
            }

            HBox observerBox = new HBox();
            HBox.setHgrow(observerBox, Priority.ALWAYS);
            VBox.setVgrow(observerBox, Priority.NEVER);
            observerBox.setSpacing(5);
            observerBox.setAlignment(Pos.CENTER_LEFT);
            Label observerLabel = new Label(getMessage("StandardObserver"));
            observerLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
            observerLabel.wrapTextProperty().setValue(true);
            final ComboBox<String> observerListbox = new ComboBox();
            observerListbox.setId(tag.getTag() + "ObserverInput");
            observerListbox.getItems().addAll(IccTagType.observerTypes());
            observerListbox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (tagValueChanged(tag, "Observer", newValue)) {
                        observerListbox.getEditor().setStyle(null);
                    } else {
                        observerListbox.getEditor().setStyle(badStyle);
                    }
                }
            });
            if (values != null) {
                observerListbox.getSelectionModel().select((String) values.get("observer"));
            }
            observerListbox.setPrefWidth(300);
            Label label = new Label();
            label.setId(tag.getTag() + "ObserverMarkLabel");
            observerBox.getChildren().addAll(observerLabel, observerListbox, label);
            measurementBox.getChildren().add(observerBox);

            HBox tristimulusBox = new HBox();
            HBox.setHgrow(tristimulusBox, Priority.ALWAYS);
            VBox.setVgrow(tristimulusBox, Priority.NEVER);
            tristimulusBox.setSpacing(5);
            tristimulusBox.setAlignment(Pos.CENTER_LEFT);
            Label tristimulusLabel = new Label(getMessage("Tristimulus"));
            tristimulusLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
            tristimulusLabel.wrapTextProperty().setValue(true);
            final TextField tristimulusInput = new TextField();
            tristimulusInput.setId(tag.getTag() + "TristimulusInput");
            tristimulusInput.setMaxSize(Double.MAX_VALUE, Region.USE_COMPUTED_SIZE);
            tristimulusInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (tagValueChanged(tag, "Tristimulus", newValue)) {
                        tristimulusInput.setStyle(null);
                    } else {
                        tristimulusInput.setStyle(badStyle);
                    }
                }
            });
            HBox.setHgrow(tristimulusInput, Priority.ALWAYS);
            if (values != null) {
                double[] tristimulus = (double[]) values.get("tristimulus");
                tristimulusInput.setText(tristimulus[0] + "  " + tristimulus[1] + "  " + tristimulus[2]);
            }
            label = new Label();
            label.setId(tag.getTag() + "TristimulusMarkLabel");
            tristimulusBox.getChildren().addAll(tristimulusLabel, tristimulusInput, label);
            measurementBox.getChildren().add(tristimulusBox);

            HBox geometryBox = new HBox();
            HBox.setHgrow(geometryBox, Priority.ALWAYS);
            VBox.setVgrow(geometryBox, Priority.NEVER);
            geometryBox.setSpacing(5);
            geometryBox.setAlignment(Pos.CENTER_LEFT);
            Label geometryLabel = new Label(getMessage("Geometry"));
            geometryLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
            geometryLabel.wrapTextProperty().setValue(true);
            final ComboBox<String> geometryListbox = new ComboBox();
            geometryListbox.setId(tag.getTag() + "GeometryInput");
            geometryListbox.getItems().addAll(IccTagType.geometryTypes());
            geometryListbox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (tagValueChanged(tag, "Geometry", newValue)) {
                        geometryListbox.getEditor().setStyle(null);
                    } else {
                        geometryListbox.getEditor().setStyle(badStyle);
                    }
                }
            });
            if (values != null) {
                geometryListbox.getSelectionModel().select((String) values.get("geometry"));
            }
            geometryListbox.setPrefWidth(200);
            label = new Label();
            label.setId(tag.getTag() + "GeometryMarkLabel");
            geometryBox.getChildren().addAll(geometryLabel, geometryListbox, label);
            measurementBox.getChildren().add(geometryBox);

            HBox flareBox = new HBox();
            HBox.setHgrow(flareBox, Priority.ALWAYS);
            VBox.setVgrow(flareBox, Priority.NEVER);
            flareBox.setSpacing(5);
            flareBox.setAlignment(Pos.CENTER_LEFT);
            Label flareLabel = new Label(getMessage("Flare"));
            flareLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
            flareLabel.wrapTextProperty().setValue(true);
            final TextField flareInput = new TextField();
            flareInput.setId(tag.getTag() + "FlareInput");
            flareInput.setMaxSize(Double.MAX_VALUE, Region.USE_COMPUTED_SIZE);
            flareInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (tagValueChanged(tag, "Flare", newValue)) {
                        flareInput.setStyle(null);
                    } else {
                        flareInput.setStyle(badStyle);
                    }
                }
            });
            HBox.setHgrow(flareInput, Priority.ALWAYS);
            if (values != null) {
                flareInput.setText((double) values.get("flare") + "");
            }
            label = new Label();
            label.setId(tag.getTag() + "FlareMarkLabel");
            flareBox.getChildren().addAll(flareLabel, flareInput, label);
            measurementBox.getChildren().add(flareBox);

            HBox typeBox = new HBox();
            HBox.setHgrow(typeBox, Priority.ALWAYS);
            VBox.setVgrow(typeBox, Priority.NEVER);
            typeBox.setSpacing(5);
            typeBox.setAlignment(Pos.CENTER_LEFT);
            Label typeLabel = new Label(getMessage("IlluminantType"));
            typeLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
            typeLabel.wrapTextProperty().setValue(true);
            final ComboBox<String> typeListbox = new ComboBox();
            typeListbox.setId(tag.getTag() + "IlluminantTypeInput");
            typeListbox.getItems().addAll(IccTagType.illuminantTypes());
            typeListbox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (tagValueChanged(tag, "IlluminantType", newValue)) {
                        typeListbox.getEditor().setStyle(null);
                    } else {
                        typeListbox.getEditor().setStyle(badStyle);
                    }
                }
            });
            if (values != null) {
                typeListbox.getSelectionModel().select((String) values.get("illuminantType"));
            }
            typeListbox.setPrefWidth(150);
            label = new Label();
            label.setId(tag.getTag() + "IlluminantTypeMarkLabel");
            typeBox.getChildren().addAll(typeLabel, typeListbox, label);
            measurementBox.getChildren().add(typeBox);

            parent.getChildren().add(measurementBox);
        } catch (Exception e) {
            popError(e.toString());
        }
    }

    private void makeTagS15Fixed16ArrayInput(Pane parent, final IccTag tag) {
        try {
            if (tag.getValue() == null) {
                final TextArea valueInput = new TextArea();
                HBox.setHgrow(valueInput, Priority.ALWAYS);
                valueInput.setWrapText(true);
                valueInput.setId(tag.getTag() + "Input");
                valueInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (tagValueChanged(tag, newValue)) {
                            valueInput.setStyle(null);
                        } else {
                            valueInput.setStyle(badStyle);
                        }
                    }
                });
                parent.getChildren().add(valueInput);
            } else {
                double[] values = (double[]) tag.getValue();
                if (values.length <= 2) {
                    final TextField valueInput = new TextField();
                    VBox.setVgrow(valueInput, Priority.NEVER);
                    valueInput.setId(tag.getTag() + "Input");
                    HBox.setHgrow(valueInput, Priority.ALWAYS);
                    valueInput.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue ov, String oldValue, String newValue) {
                            if (tagValueChanged(tag, newValue)) {
                                valueInput.setStyle(null);
                            } else {
                                valueInput.setStyle(badStyle);
                            }
                        }
                    });
                    valueInput.setText(tag.display());
                    valueInput.setPrefHeight(Region.USE_COMPUTED_SIZE);
                    parent.getChildren().add(valueInput);
                } else {
                    final TextArea valueInput = new TextArea();
                    HBox.setHgrow(valueInput, Priority.ALWAYS);
                    valueInput.setWrapText(true);
                    valueInput.setId(tag.getTag() + "Input");
                    valueInput.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue ov, String oldValue, String newValue) {
                            if (tagValueChanged(tag, newValue)) {
                                valueInput.setStyle(null);
                            } else {
                                valueInput.setStyle(badStyle);
                            }
                        }
                    });
                    valueInput.setText(tag.display());
                    parent.getChildren().add(valueInput);
                }
            }
            Label label = new Label();
            label.setId(tag.getTag() + "MarkLabel");
            parent.getChildren().add(label);
        } catch (Exception e) {
            popError(e.toString());
        }
    }

    private void displayTagData(final IccTag tag) {
        tagDisplay.clear();
        tagNameDisplay.clear();
        tagTypeDisplay.clear();
        tagOffsetDisplay.clear();
        tagSizeDisplay.clear();
        tagDescDisplay.clear();
        tagDataDisplay.clear();
        tagBytesDisplay.clear();
        if (tag == null) {
            return;
        }
        displayPane.getSelectionModel().select(tagDataTab);
        tagDisplay.setText(tag.getTag());
        tagNameDisplay.setText(tag.getName());
        tagOffsetDisplay.setText(tag.getOffset() + "");
        tagSizeDisplay.setText(tag.getBytes().length + "");
        tagDescDisplay.setText(tag.getDescription());

        if (task != null && task.isRunning()) {
            task.cancel();
        }
        task = new Task<Void>() {
            private boolean ok;
            private String display, bytes;

            @Override
            protected Void call() throws Exception {
                bytes = bytesToHexFormat(tag.getBytes());
                if (tag.getType() == null || tag.getValue() == null) {
                    return null;
                }
                if (tag.getType() == IccTag.TagType.MultiLocalizedUnicode) {
                    display = IccTagType.textDescriptionFullDisplay(tag);
                } else {
                    display = tag.display();
                }

                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (tag.getType() == null) {
                            tagTypeDisplay.setText(AppVaribles.getMessage("NotDecoded"));
                            tagDataDisplay.setText(AppVaribles.getMessage("NotDecoded"));
                        } else {
                            tagTypeDisplay.setText(tag.getType() + "");
                            if (display != null) {
                                tagDataDisplay.setText(display);
                            }
                        }
                        tagBytesDisplay.setText(bytes);
                    }
                });

            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    public void popSave(MouseEvent event) {
        if (isIccFile) {
            return;
        }
        super.popSaveAs(event);
    }

    @FXML
    public void openLinks() {
        openStage(CommonValues.ChromaticLinksFxml);
    }

    @FXML
    public void calculateXYZ() {

//        String colorSpace = (String) profile.getHeaderValues().get("ColorSpaceType");
//        float[] xyz = null;
//        switch (colorSpace) {
//            case "RGB ": {
//                try {
//                    float[] vs = new float[3];
////                    vs[0] = (int) inputs.get("Red") / 255f;
////                    vs[1] = (int) inputs.get("Green") / 255f;
////                    vs[2] = (int) inputs.get("Blue") / 255f;
//                    xyz = profile.calculateXYZ(vs);
//                } catch (Exception e) {
//                    popError(e.toString());
//                    return;
//                }
//                break;
//            }
//            case "CMYK": {
//                makeInput(csInputBox, getMessage("Cyan"), "", "Cyan");
//                makeInput(csInputBox, getMessage("Magenta"), "", "Magenta");
//                makeInput(csInputBox, getMessage("Yellow"), "", "Yellow");
//                makeInput(csInputBox, getMessage("Black"), "", "Black");
//                break;
//            }
//
//        }
//        if (xyz == null) {
//            return;
//        }
//        xOutput.setText(xyz[0] + "");
//        yOutput.setText(xyz[1] + "");
//        zOutput.setText(xyz[2] + "");
//        for (String key : inputs.keySet()) {
//
//            if (key.equals("Red") || key.equals("Green") || key.equals("Blue") || key.equals("Gray")) {
//                try {
//                    int v = (int) inputs.get(key);
//                } catch (Exception e) {
//
//                }
//            }
//
//        }
    }

    @FXML
    public void nowCreateTime() {
        createTimeInput.setText(DateTools.datetimeToString(new Date()));
    }

    @FXML
    @Override
    public void recoverAction() {
        if (isIccFile) {
            if (sourceFile != null) {
                openProfile(SourceType.External_File, sourceFile.getAbsolutePath());
            }
        } else {
            if (embedICC != null) {
                openProfile(SourceType.Embed, embedICC);
            }
        }
    }

    public void resetMarkLabel(Node node) {
        if (node == null) {
            return;
        }
        if (node.getId() != null && node.getId().endsWith("MarkLabel")) {
            Label label = (Label) node;
            label.setText("");
        }
        if (node instanceof Parent) {
            for (Node c : ((Parent) node).getChildrenUnmodifiable()) {
                resetMarkLabel(c);
            }
        }
    }

    public boolean validateInputs() {
        inputsValid = true;
        validateInputs(thisPane);
        if (!inputsValid) {
            popError(getMessage("InvalidData"));
        }

//        saveButton.setDisable(!inputsValid);
//        saveAsButton.setDisable(!inputsValid);
//        refreshHeaderButton.setDisable(!inputsValid);
//        refreshXmlButton.setDisable(!inputsValid);
//        exportButton.setDisable(!inputsValid);
        return inputsValid;
    }

    public void validateInputs(Node node) {
        if (node == null || !inputsValid) {
            return;
        }
        if (badStyle.equals(node.getStyle())) {
            inputsValid = false;
            return;
        }
        if (node instanceof Parent) {
            for (Node c : ((Parent) node).getChildrenUnmodifiable()) {
                validateInputs(c);
            }
        }
    }

    @FXML
    public void refreshAction() {
        if (!validateInputs()) {
            return;
        }

        final byte[] newHeaderData = encodeHeaderUpdate();
        if (encodeHeaderUpdate() == null) {
            popError(getMessage("InvalidData"));
            return;
        }

        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {

                ok = profile.update(newHeaderData);

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok) {
                            loadProfileData();
                            popInformation(AppVaribles.getMessage("Successful"));
                        } else {
                            popInformation(AppVaribles.getMessage("failed"));
                        }
                    }
                });
            }

        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    public void popXmlPath(MouseEvent event) { //
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        List<VisitHistory> his = VisitHistory.getRecentPath(VisitHistory.FileType.Xml);
        List<String> paths = new ArrayList();
        if (his != null) {
            for (VisitHistory h : his) {
                String pathname = h.getResourceValue();
                paths.add(pathname);
            }
        }
        if (paths.isEmpty()) {
            return;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        MenuItem imenu = new MenuItem(getMessage("RecentAccessedDirectories"));
        imenu.setStyle("-fx-text-fill: #2e598a;");
        popMenu.getItems().add(imenu);
        MenuItem menu;
        for (String path : paths) {
            menu = new MenuItem(path);
            final String p = path;
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    AppVaribles.setUserConfigValue(targetPathKey, p);
                    exportXmlAction();
                }
            });
            popMenu.getItems().add(menu);
        }

        popMenu.getItems().add(new SeparatorMenuItem());
        menu = new MenuItem(getMessage("MenuClose"));
        imenu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                popMenu.hide();
                popMenu = null;
            }
        });
        popMenu.getItems().add(menu);

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    @FXML
    public void exportXmlAction() {
//        if (!validateInputs()) {
//            return;
//        }

        final FileChooser fileChooser = new FileChooser();
        File path = AppVaribles.getUserConfigPath(targetPathKey);
        if (path.exists()) {
            fileChooser.setInitialDirectory(path);
        }
        if (isIccFile) {
            fileChooser.setInitialFileName(FileTools.getFilePrefix(sourceFile.getName()));
        } else {
            fileChooser.setInitialFileName(embedICC);
        }
        fileChooser.getExtensionFilters().addAll(CommonValues.XmlExtensionFilter);
        final File file = fileChooser.showSaveDialog(getMyStage());
        if (file == null) {
            return;
        }
        recordFileWritten(file, targetPathKey,
                VisitHistory.FileType.Xml, VisitHistory.FileType.Xml);

        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                ok = FileTools.writeFile(file, xmlArea.getText());
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok) {
                            if (openExportCheck.isSelected()) {
                                browseURI(file.toURI());
                            }
                            popInformation(AppVaribles.getMessage("Successful"));
                        } else {
                            popInformation(AppVaribles.getMessage("failed"));
                        }
                    }
                });
            }

        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private byte[] encodeHeaderUpdate() {
        if (profileVersionInput.getStyle().equals(badStyle)
                || createTimeInput.getStyle().equals(badStyle)
                || xInput.getStyle().equals(badStyle)
                || yInput.getStyle().equals(badStyle)
                || zInput.getStyle().equals(badStyle)) {
            popError(getMessage("InvalidData"));
            return null;
        }
        try {
            byte[] newHeaderBytes = new byte[128];
            byte[] headerBytes = header.getHeader();
            System.arraycopy(headerBytes, 0, newHeaderBytes, 0, 84);

            byte[] bytes = IccProfile.first4ASCII(cmmTypeBox.getSelectionModel().getSelectedItem());
            System.arraycopy(bytes, 0, newHeaderBytes, 4, 4);

            bytes = IccHeader.profileVersion(profileVersionInput.getText().trim());
            System.arraycopy(bytes, 0, newHeaderBytes, 8, 4);

            bytes = IccProfile.first4ASCII(deviceClassBox.getSelectionModel().getSelectedItem());
            System.arraycopy(bytes, 0, newHeaderBytes, 12, 4);

            bytes = IccHeader.colorSpaceType(colorSpaceBox.getSelectionModel().getSelectedItem());
            System.arraycopy(bytes, 0, newHeaderBytes, 16, 4);

            bytes = IccProfile.first4ASCII(PCSTypeBox.getSelectionModel().getSelectedItem());
            System.arraycopy(bytes, 0, newHeaderBytes, 20, 4);

            bytes = IccTagType.dateTimeBytes(createTimeInput.getText());
            System.arraycopy(bytes, 0, newHeaderBytes, 24, 12);

            bytes = IccProfile.first4ASCII(profileFileInput.getText());
            System.arraycopy(bytes, 0, newHeaderBytes, 36, 4);

            bytes = IccProfile.first4ASCII(platformBox.getSelectionModel().getSelectedItem());
            System.arraycopy(bytes, 0, newHeaderBytes, 40, 4);

            bytes = IccHeader.profileFlags(embedCheck.isSelected(), independentCheck.isSelected(), subsetCheck.isSelected());
            System.arraycopy(bytes, 0, newHeaderBytes, 44, 4);

            bytes = IccProfile.first4ASCII(manufacturerBox.getSelectionModel().getSelectedItem());
            System.arraycopy(bytes, 0, newHeaderBytes, 48, 4);

            bytes = IccProfile.first4ASCII(deviceModelInput.getText());
            System.arraycopy(bytes, 0, newHeaderBytes, 52, 4);

            bytes = IccHeader.deviceAttributes(transparentcyCheck.isSelected(), matteCheck.isSelected(),
                    negativeCheck.isSelected(), bwCheck.isSelected(),
                    paperCheck.isSelected(), texturedCheck.isSelected(),
                    isotropicCheck.isSelected(), selfLuminousCheck.isSelected());
            System.arraycopy(bytes, 0, newHeaderBytes, 56, 8);

            bytes = IccHeader.RenderingIntent(intentBox.getSelectionModel().getSelectedItem());
            System.arraycopy(bytes, 0, newHeaderBytes, 64, 4);

            bytes = IccTagType.s15Fixed16Number(Double.parseDouble(xInput.getText()));
            System.arraycopy(bytes, 0, newHeaderBytes, 68, 4);

            bytes = IccTagType.s15Fixed16Number(Double.parseDouble(yInput.getText()));
            System.arraycopy(bytes, 0, newHeaderBytes, 72, 4);

            bytes = IccTagType.s15Fixed16Number(Double.parseDouble(zInput.getText()));
            System.arraycopy(bytes, 0, newHeaderBytes, 76, 4);

            bytes = IccProfile.first4ASCII(creatorBox.getSelectionModel().getSelectedItem());
            System.arraycopy(bytes, 0, newHeaderBytes, 80, 4);

//        tagDataDisplay.setText(ByteTools.bytesToHexFormat(newHeaderBytes));
//        spectralPCSInput.setText((String) headerValues.get("SpectralPCS"));
//        spectralPCSRangeInput.setText((String) headerValues.get("SpectralPCSWaveLengthRange"));
//        bispectralPCSRangeInput.setText((String) headerValues.get("BispectralPCSWaveLengthRange"));
//        mcsInput.setText((String) headerValues.get("MCS"));
//        subClassInput.setText((String) headerValues.get("ProfileDeviceSubclass"));
//        subclassVersionInput.setText((String) headerValues.get("ProfileSubclassVersion"));
            return newHeaderBytes;
        } catch (Exception e) {
            return null;
        }
    }

    public void saveAsFile(final File file) {
        if (file == null) {
            return;
        }
        final byte[] newHeaderData = encodeHeaderUpdate();
        if (encodeHeaderUpdate() == null) {
            popError(getMessage("InvalidData"));
            return;
        }

        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {

                ok = profile.write(file, newHeaderData);

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok) {
                            sourceFile = file;
                            openProfile(SourceType.External_File, file.getAbsolutePath());
                            popInformation(AppVaribles.getMessage("Successful"));
                        } else {
                            popInformation(AppVaribles.getMessage("failed"));
                        }
                    }
                });
            }

        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    @Override
    public void saveAction() {
        if (!validateInputs()) {
            return;
        }

        if (!isIccFile || sourceFile == null) {
            saveAsAction();
            return;
        }
        saveAsFile(sourceFile);

    }

    @FXML
    @Override
    public void saveAsAction() {
        if (!validateInputs()) {
            return;
        }

        final FileChooser fileChooser = new FileChooser();
        File path = AppVaribles.getUserConfigPath(targetPathKey);
        if (path.exists()) {
            fileChooser.setInitialDirectory(path);
        }
        if (isIccFile) {
            fileChooser.setInitialFileName(FileTools.getFilePrefix(sourceFile.getName()));
        } else {
            fileChooser.setInitialFileName(embedICC);
        }
        fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
        final File file = fileChooser.showSaveDialog(getMyStage());
        if (file == null) {
            return;
        }
        recordFileWritten(file);
        saveAsFile(file);

    }

}
