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
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import mara.mybox.color.IccHeader;
import mara.mybox.color.IccProfile;
import mara.mybox.color.IccTag;
import mara.mybox.color.IccTagType;
import mara.mybox.color.IccTags;
import mara.mybox.color.IccXML;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.ByteTools;
import static mara.mybox.tools.ByteTools.bytesToHexFormat;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileFilters;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-5-13
 * @Description
 * @License Apache License Version 2.0
 */
public class IccProfileEditorController extends ChromaticityBaseController {

    protected SourceType sourceType;
    protected String embedICCName, externalDataName;
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
            tagDisplay, tagNameDisplay, tagTypeDisplay, tagOffsetDisplay, tagSizeDisplay,
            maxDecodeInput;
    @FXML
    protected CheckBox embedCheck, independentCheck, subsetCheck,
            transparentcyCheck, matteCheck, negativeCheck, bwCheck, paperCheck, texturedCheck,
            isotropicCheck, selfLuminousCheck, idAutoCheck, lutNormalizeCheck, openExportCheck;
    @FXML
    protected Label infoLabel, cmmTypeMarkLabel, deviceClassMarkLabel, colorSpaceMarkLabel, PCSTypeMarkLabel,
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
    @FXML
    protected ControlFileBackup backupController;

    protected enum SourceType {
        Embed, Internal_File, External_File, External_Data
    };

    public IccProfileEditorController() {
        baseTitle = message("IccProfileEditor");
        TipsLabelKey = "IccProfileTips";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Icc);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            sourceFile = null;
            embedICCName = null;

            initToolbar();
            initHeaderControls();
            initTagsTable();
            initOptions();

            backupController.setParameters(this, baseName);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(maxDecodeInput, new Tooltip(message("MaxDecodeComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e);
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
//            embedBox.getSelectionModel().select(0);

//            saveButton.disableProperty().bind(profileVersionInput.styleProperty().isEqualTo(badStyle)
//                    .or(createTimeInput.styleProperty().isEqualTo(badStyle))
//                    .or(xInput.styleProperty().isEqualTo(badStyle))
//                    .or(yInput.styleProperty().isEqualTo(badStyle))
//                    .or(zInput.styleProperty().isEqualTo(badStyle))
//            );
//            saveAsButton.disableProperty().bind(saveButton.disableProperty()
//            );
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initHeaderControls() {
        try {
            List<String> manuList = new ArrayList<>();
            for (String[] item : IccHeader.DeviceManufacturers) {
                manuList.add(item[0] + AppValues.Indent + item[1]);
            }
            cmmTypeBox.getItems().addAll(manuList);
            cmmTypeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (!cmmTypeBox.getItems().contains(newValue)) {
                        ValidationTools.setEditorWarnStyle(cmmTypeBox);
                    } else {
                        ValidationTools.setEditorNormal(cmmTypeBox);
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
                    if (isSettingValues) {
                        return;
                    }
                    profileVersionMarkLabel.setText("*");
                    profileChanged();
                }
            });

            List<String> classList = new ArrayList<>();
            for (String[] item : IccHeader.ProfileDeviceClasses) {
                classList.add(item[0] + AppValues.Indent + message(item[1]));
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
                        ValidationTools.setEditorWarnStyle(colorSpaceBox);
                    } else {
                        ValidationTools.setEditorNormal(colorSpaceBox);
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
                        createTimeInput.setStyle(UserConfig.badStyle());
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

            List<String> platformsList = new ArrayList<>();
            for (String[] item : IccHeader.PrimaryPlatforms) {
                platformsList.add(item[0] + AppValues.Indent + item[1]);
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
                    if (isSettingValues) {
                        return;
                    }
                    if (!manufacturerBox.getItems().contains(newValue)) {
                        ValidationTools.setEditorWarnStyle(manufacturerBox);
                    } else {
                        ValidationTools.setEditorNormal(manufacturerBox);
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

            List<String> intents = new ArrayList<>();
            for (String item : IccHeader.RenderingIntents) {
                intents.add(message(item));
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
                        xInput.setStyle(UserConfig.badStyle());
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
                        yInput.setStyle(UserConfig.badStyle());
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
                        zInput.setStyle(UserConfig.badStyle());
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
                        ValidationTools.setEditorWarnStyle(creatorBox);
                    } else {
                        ValidationTools.setEditorNormal(creatorBox);
                    }
                    if (isSettingValues) {
                        return;
                    }
                    creatorMarkLabel.setText("*");
                    profileChanged();
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initTagsTable() {
        tagColumn.setCellValueFactory(new PropertyValueFactory<>("tag"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        offsetColumn.setCellValueFactory(new PropertyValueFactory<>("offset"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));

        tagsTableView.setItems(tagsTable);
        tagsTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object t, Object t1) {
                IccTag selected = tagsTableView.getSelectionModel().getSelectedItem();
                displayTagData(selected);
            }
        });
    }

    @Override
    public void initOptions() {
        maxDecodeInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                checkMaxDecode();
            }
        });
        maxDecodeInput.setText(UserConfig.getInt("ICCMaxDecodeNumber", 500) + "");

        lutNormalizeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean("LutNormalize", newValue);
            }
        });
        lutNormalizeCheck.setSelected(UserConfig.getBoolean("LutNormalize", true));
    }

    private void checkMaxDecode() {
        try {
            String s = maxDecodeInput.getText().trim();
            if (s.isEmpty()) {
                xInput.setStyle(null);
                UserConfig.setInt("ICCMaxDecodeNumber", Integer.MAX_VALUE);
                return;
            }
            int v = Integer.parseInt(s);
            if (v > 0) {
                UserConfig.setInt("ICCMaxDecodeNumber", v);
                xInput.setStyle(null);
            } else {
                xInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            xInput.setStyle(UserConfig.badStyle());
        }
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
        if (null != sourceType) {
            switch (sourceType) {
                case Internal_File:
                case External_File:
                    if (sourceFile != null) {
                        return sourceFile.getAbsolutePath();
                    }
                    break;
                case Embed:
                    if (embedICCName != null) {
                        return message("JavaEmbeddedColorModel") + ": " + embedICCName;
                    }
                    break;
                case External_Data:
                    if (externalDataName != null) {
                        return externalDataName;
                    }
                    break;
                default:
                    break;
            }
        }
        return "";
    }

    @Override
    public void selectSourceFileDo(File file) {
        if (file == null) {
            return;
        }
        recordFileOpened(file);
        sourceType = SourceType.External_File;
        openProfile(file.getAbsolutePath());

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
                sourceType = SourceType.Embed;
                openProfile(name);
                break;
            default:
                sourceType = SourceType.Internal_File;
                openProfile(name);
        }

    }

    public void externalData(String name, byte[] data) {
        try {
            if (data == null) {
                return;
            }
            sourceType = SourceType.External_Data;
            sourceFile = null;
            externalDataName = name;
            embedBox.getSelectionModel().clearSelection();
            profile = new IccProfile(data);
            displayProfileData();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void openProfile(final String name) {
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        if (task != null) {
            task.cancel();
        }
        final String inputName;
        if (sourceType == SourceType.Embed) {
            inputName = message("JavaEmbeddedColorModel") + ": " + name;
        } else {
            inputName = message("File") + ": " + name;
        }
        task = new FxSingletonTask<Void>(this) {

            private File file;
            private IccProfile p;

            @Override
            protected boolean handle() {
                try {
                    switch (sourceType) {
                        case Embed:
                            p = new IccProfile(name);
                            break;
                        case Internal_File:
                            file = mara.mybox.fxml.FxFileTools.getInternalFile("/data/ICC/" + name, "ICC", name);
                            p = new IccProfile(file);
                            break;
                        case External_File:
                            file = new File(name);
                            p = new IccProfile(file);
                    }
                } catch (Exception e) {
                    error = e.toString();
                }
                return p != null && p.getHeader() != null;
            }

            @Override
            protected void whenSucceeded() {
                isIccFile = sourceType != SourceType.Embed;
                if (isIccFile) {
                    sourceFile = file;
                    isSettingValues = true;
                    embedICCName = null;
                    isSettingValues = false;
                } else {
                    embedICCName = name;
                    sourceFile = null;
                }
                if (sourceType == SourceType.External_File) {
                    embedBox.getSelectionModel().clearSelection();
                }
                profile = p;
                displayProfileData();
                backupController.loadBackups(sourceFile);
            }

            @Override
            protected void whenFailed() {
                if (error == null) {
                    if (p != null && p.getError() != null) {
                        error = p.getError();
                    } else {
                        error = message("Invalid");
                    }
                }
                popError(inputName + " " + error);
            }

        };
        start(task, inputName + " " + message("Loading..."));
    }

    private void displayProfileData() {
        if (profile == null || !profile.isIsValid()) {
            popError(message("IccInvalid"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        profile.setNormalizeLut(lutNormalizeCheck.isSelected());
        infoLabel.setText(getCurrentName());
        if (myStage != null) {
            myStage.setTitle(getBaseTitle() + "  " + getCurrentName());
        }
        resetMarkLabel(headerBox);
        inputsValid = true;
        task = new FxSingletonTask<Void>(this) {

            private String xml;

            @Override
            protected boolean handle() {
                header = profile.getHeader();
                header.readFields();
                tags = profile.getTags();
                tags.readTags();
                xml = IccXML.iccXML(header, tags);
                if (xml == null) {
                    error = message("IccInvalid");
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            protected void whenSucceeded() {
                displaySummary();
                initHeaderInputs();
                makeTagsInputs();
                displayTagsTable();
                displayXML(xml);
            }

        };
        start(task);
    }

    private void displaySummary() {
        summaryArea.clear();
        if (header == null) {
            return;
        }

        try {
            LinkedHashMap<String, IccTag> fields = header.getFields();
            String s = message("ProfileSize") + ": " + header.value("ProfileSize");
            List<IccTag> tagsList = tags.getTags();
            if (tagsList != null) {
                s += "   " + message("TagsNumber") + ": " + tagsList.size();
            }
            String name = getCurrentName();
            infoLabel.setText(name + "\n" + message("ProfileSize") + ": " + header.value("ProfileSize")
                    + ((tagsList != null) ? "\n" + message("TagsNumber") + ": " + tagsList.size() : ""));

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
                        s += message("ProfileFlags") + ": ";
                        s += ((boolean) header.value("ProfileFlagEmbedded") ? message("NotEmbedded") : message("NotEmbedded")) + "  ";
                        s += ((boolean) header.value("ProfileFlagIndependently") ? message("Independent") : message("NotIndependent")) + "  ";
                        s += ((boolean) header.value("ProfileFlagMCSSubset") ? message("MCSSubset") : message("MCSNotSubset"));
                        s += " (" + header.hex(44, 4) + ")\n";
                        break;
                    case "DeviceAttributeTransparency":
                        s += message("DeviceAttributes") + ": ";
                        s += ((boolean) header.value("DeviceAttributeTransparency") ? message("Transparency") : message("Reflective")) + "  ";
                        s += ((boolean) header.value("DeviceAttributeMatte") ? message("Matte") : message("Glossy")) + "  ";
                        s += ((boolean) header.value("DeviceAttributeNegative") ? message("Negative2") : message("Positive2")) + "  ";
                        s += ((boolean) header.value("DeviceAttributeBlackOrWhite") ? message("BlackOrWhite") : message("Colorful")) + "  ";
                        s += ((boolean) header.value("DeviceAttributePaperBased") ? message("PaperBased") : message("NonPaperBased")) + "  ";
                        s += ((boolean) header.value("DeviceAttributeTextured") ? message("Textured") : message("NonTextured")) + "  ";
                        s += ((boolean) header.value("DeviceAttributeIsotropic") ? message("Isotropic") : message("NonIsotropic")) + "  ";
                        s += ((boolean) header.value("DeviceAttributeSelfLuminous") ? message("SelfLuminous") : message("NonSelfLuminous"));
                        s += " (" + header.hex(56, 8) + ")\n";
                        break;
                    case "PCCIlluminantX":
                        s += message("ConnectionSpaceIlluminant") + ": ";
                        s += header.value("PCCIlluminantX") + "  ";
                        s += header.value("PCCIlluminantY") + "  ";
                        s += header.value("PCCIlluminantZ");
                        s += " (" + header.hex(68, 12) + ")\n";
                        break;
                    default:
                        s += message(field.getTag()) + ": " + field.getValue();
                        if (field.getType() != IccTag.TagType.Bytes) {
                            s += " (" + bytesToHexFormat(field.getBytes()) + ")";
                        }
                        s += "\n";
                        break;
                }

            }

            s += "\n\n" + message("HeaderBytes") + ": \n" + ByteTools.bytesToHexFormat(header.getHeader());
            summaryArea.setText(s);

        } catch (Exception e) {
            MyBoxLog.debug(e);

        }

    }

    private void initHeaderInputs() {
        if (header == null) {
            return;
        }
        try {
            isSettingValues = true;
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
            manufacturerBox.setValue((String) header.value("DeviceManufacturer"));
            deviceModelInput.setText((String) header.value("DeviceModel"));
            transparentcyCheck.setSelected((boolean) header.value("DeviceAttributeTransparency"));
            matteCheck.setSelected((boolean) header.value("DeviceAttributeMatte"));
            negativeCheck.setSelected((boolean) header.value("DeviceAttributeNegative"));
            bwCheck.setSelected((boolean) header.value("DeviceAttributeBlackOrWhite"));
            paperCheck.setSelected((boolean) header.value("DeviceAttributePaperBased"));
            texturedCheck.setSelected((boolean) header.value("DeviceAttributeTextured"));
            isotropicCheck.setSelected((boolean) header.value("DeviceAttributeIsotropic"));
            selfLuminousCheck.setSelected((boolean) header.value("DeviceAttributeSelfLuminous"));
            intentBox.setValue((String) header.value("RenderingIntent"));
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
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.debug(e);
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
            MyBoxLog.debug(e);

        }
    }

    private void displayXML(String xml) {
        if (xml != null) {
            xmlArea.setText(xml);
        } else {
            xmlArea.clear();
            popError(message("InvalidData"));
        }
    }

    private void makeTagsInputs() {
        try {
            tagDataBox.getChildren().clear();
            if (tags == null) {
                return;
            }
            isSettingValues = true;
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

                Label label = new Label(message(tag.getName()));
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
                        Label label2 = new Label(message("NotSupportEditCurrently"));
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
            refreshStyle(tagDataBox);
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.debug(e);

        }
    }

    private void profileChanged() {
        myStage.setTitle(getBaseTitle() + "  " + getCurrentName() + " " + "*");
    }

    private boolean tagValueChanged(final IccTag tag, final String key, final String value) {
        if (isSettingValues || tag.getType() == null) {
            return true;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                return tag.update(key, value);
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        String name;
                        if (key != null) {
                            name = tag.getTag() + key;
                        } else {
                            name = tag.getTag();
                        }
                        if (ok) {
                            try {
                                Label label = (Label) NodeTools.findNode(thisPane, name + "MarkLabel");
                                label.setText("*");
                                label.setStyle("-fx-text-fill: blue; -fx-font-weight: bolder;");
                                profileChanged();
                            } catch (Exception e) {
                            }
                            NodeStyleTools.setStyle(thisPane, name + "Input", null);
                        } else {
                            NodeStyleTools.setStyle(thisPane, name + "Input", UserConfig.badStyle());
                        }
                    }
                });
            }

        };
        start(task);
        return true;
    }

    private boolean tagValueChanged(IccTag tag, String value) {
        return tagValueChanged(tag, null, value);
    }

    private void makeTagTextInput(Pane parent, final IccTag tag) {
        try {
            if (tag.getValueSelection() != null) {
                final ComboBox<String> valuesListbox = new ComboBox<>();
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
                            valuesListbox.getEditor().setStyle(UserConfig.badStyle());
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
                            valueInput.setStyle(UserConfig.badStyle());
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
                            valueInput.setStyle(UserConfig.badStyle());
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
                                valueInput.setStyle(UserConfig.badStyle());
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
                                valueInput.setStyle(UserConfig.badStyle());
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
                            valueInput.setStyle(UserConfig.badStyle());
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
                                valueInput.setStyle(UserConfig.badStyle());
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
                                valueInput.setStyle(UserConfig.badStyle());
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
            Label illuminantLabel = new Label(message("Illuminant"));
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
                        illuminantInput.setStyle(UserConfig.badStyle());
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
            Label surroundLabel = new Label(message("Surround"));
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
                        surroundInput.setStyle(UserConfig.badStyle());
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
            Label typeLabel = new Label(message("IlluminantType"));
            typeLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
            typeLabel.wrapTextProperty().setValue(true);
            final ComboBox<String> typeListbox = new ComboBox<>();
            typeListbox.setId(tag.getTag() + "IlluminantTypeInput");
            typeListbox.getItems().addAll(IccTagType.illuminantTypes());
            typeListbox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (tagValueChanged(tag, "IlluminantType", newValue)) {
                        typeListbox.getEditor().setStyle(null);
                    } else {
                        typeListbox.getEditor().setStyle(UserConfig.badStyle());
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
            Label observerLabel = new Label(message("StandardObserver"));
            observerLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
            observerLabel.wrapTextProperty().setValue(true);
            final ComboBox<String> observerListbox = new ComboBox<>();
            observerListbox.setId(tag.getTag() + "ObserverInput");
            observerListbox.getItems().addAll(IccTagType.observerTypes());
            observerListbox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (tagValueChanged(tag, "Observer", newValue)) {
                        observerListbox.getEditor().setStyle(null);
                    } else {
                        observerListbox.getEditor().setStyle(UserConfig.badStyle());
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
            Label tristimulusLabel = new Label(message("Tristimulus"));
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
                        tristimulusInput.setStyle(UserConfig.badStyle());
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
            Label geometryLabel = new Label(message("Geometry"));
            geometryLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
            geometryLabel.wrapTextProperty().setValue(true);
            final ComboBox<String> geometryListbox = new ComboBox<>();
            geometryListbox.setId(tag.getTag() + "GeometryInput");
            geometryListbox.getItems().addAll(IccTagType.geometryTypes());
            geometryListbox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (tagValueChanged(tag, "Geometry", newValue)) {
                        geometryListbox.getEditor().setStyle(null);
                    } else {
                        geometryListbox.getEditor().setStyle(UserConfig.badStyle());
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
            Label flareLabel = new Label(message("Flare"));
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
                        flareInput.setStyle(UserConfig.badStyle());
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
            Label typeLabel = new Label(message("IlluminantType"));
            typeLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
            typeLabel.wrapTextProperty().setValue(true);
            final ComboBox<String> typeListbox = new ComboBox<>();
            typeListbox.setId(tag.getTag() + "IlluminantTypeInput");
            typeListbox.getItems().addAll(IccTagType.illuminantTypes());
            typeListbox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (tagValueChanged(tag, "IlluminantType", newValue)) {
                        typeListbox.getEditor().setStyle(null);
                    } else {
                        typeListbox.getEditor().setStyle(UserConfig.badStyle());
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
                            valueInput.setStyle(UserConfig.badStyle());
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
                                valueInput.setStyle(UserConfig.badStyle());
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
                                valueInput.setStyle(UserConfig.badStyle());
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

        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private String display, bytes;

            @Override
            protected boolean handle() {
                bytes = bytesToHexFormat(tag.getBytes());
                if (tag.getType() == null || tag.getValue() == null) {
                    return false;
                }
                if (tag.getType() == IccTag.TagType.MultiLocalizedUnicode) {
                    display = IccTagType.textDescriptionFullDisplay(tag);
                } else {
                    display = tag.display();
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (tag.getType() == null) {
                    tagTypeDisplay.setText(message("NotDecoded"));
                    tagDataDisplay.setText(message("NotDecoded"));
                } else {
                    tagTypeDisplay.setText(tag.getType() + "");
                    if (display != null) {
                        tagDataDisplay.setText(display);
                    }
                }
                tagBytesDisplay.setText(bytes);
            }

        };
        start(task);
    }

    @FXML
    public void popSave(MouseEvent event) {
        if (isIccFile) {
            return;
        }
        super.popSaveAs(event);
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

                openProfile(sourceFile.getAbsolutePath());
            }
        } else {
            if (embedICCName != null) {
                openProfile(embedICCName);
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
            popError(message("InvalidData"));
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
        if (UserConfig.badStyle().equals(node.getStyle())) {
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
    @Override
    public void refreshAction() {
        if (!validateInputs()) {
            return;
        }

        final byte[] newHeaderData = encodeHeaderUpdate();
        if (encodeHeaderUpdate() == null) {
            popError(message("InvalidData"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                return profile.update(newHeaderData);
            }

            @Override
            protected void whenSucceeded() {
                displayProfileData();
                popSuccessful();
            }

        };
        start(task);
    }

    @FXML
    public void showXmlPathMenu(Event event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event, true) {

            @Override
            public List<VisitHistory> recentPaths() {
                int pathNumber = AppVariables.fileRecentNumber;
                return VisitHistoryTools.getRecentPathWrite(VisitHistory.FileType.XML, pathNumber);
            }

            @Override
            public void handleSelect() {
                exportXmlAction();
            }

            @Override
            public void handlePath(String fname) {
                handleTargetPath(fname);
            }

        }.pop();
    }

    @FXML
    public void pickXmlPath(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)
                || AppVariables.fileRecentNumber <= 0) {
            exportXmlAction();
        } else {
            showXmlPathMenu(event);
        }
    }

    @FXML
    public void popXmlPath(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)) {
            showXmlPathMenu(event);
        }
    }

    @FXML
    public void exportXmlAction() {
//        if (!validateInputs()) {
//            return;
//        }
        String name;
        if (isIccFile) {
            name = FileNameTools.prefix(sourceFile.getName());
        } else {
            name = embedICCName;
        }
        final File file = chooseSaveFile(UserConfig.getPath(VisitHistoryTools.getPathKey(VisitHistory.FileType.XML)),
                name, FileFilters.XMLExtensionFilter);
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                if (TextFileTools.writeFile(file, xmlArea.getText()) == null) {
                    return false;
                }
                recordFileWritten(file, VisitHistory.FileType.XML);
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (openExportCheck.isSelected()) {
                    browseURI(file.toURI());
                }
                popSuccessful();
            }

        };
        start(task);
    }

    private byte[] encodeHeaderUpdate() {
        if (profileVersionInput.getStyle().equals(UserConfig.badStyle())
                || createTimeInput.getStyle().equals(UserConfig.badStyle())
                || xInput.getStyle().equals(UserConfig.badStyle())
                || yInput.getStyle().equals(UserConfig.badStyle())
                || zInput.getStyle().equals(UserConfig.badStyle())) {
            popError(message("InvalidData"));
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
            popError(message("InvalidData"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                if (backupController.needBackup()) {
                    backupController.addBackup(task, file);
                }
                return profile.write(file, newHeaderData);
            }

            @Override
            protected void whenSucceeded() {
                sourceFile = file;
                sourceType = SourceType.External_File;
                openProfile(file.getAbsolutePath());
                popSuccessful();

            }

        };
        start(task);
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

        String name;
        if (isIccFile) {
            name = FileNameTools.prefix(sourceFile.getName());
        } else {
            name = embedICCName;
        }
        final File file = chooseSaveFile(UserConfig.getPath(baseName + "TargetPath"),
                name, targetExtensionFilter);
        if (file == null) {
            return;
        }
        recordFileWritten(file);
        saveAsFile(file);

    }

}
