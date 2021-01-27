package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2020-12-05
 * @License Apache License Version 2.0
 */
public class ControlTTFSelecter extends BaseController {

    protected String ttfFile;

    @FXML
    protected ComboBox<String> ttfSelector;

    public ControlTTFSelecter() {
        SourceFileType = VisitHistory.FileType.TTF;
        SourceFileType = VisitHistory.FileType.TTF;
        SourcePathType = VisitHistory.FileType.TTF;
        AddFileType = VisitHistory.FileType.TTF;
        AddPathType = VisitHistory.FileType.TTF;
        TargetPathType = VisitHistory.FileType.TTF;
        TargetFileType = VisitHistory.FileType.TTF;
        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.TTF);
        targetPathKey = sourcePathKey;
        sourceExtensionFilter = CommonFxValues.TTFExtensionFilter;
        targetExtensionFilter = CommonFxValues.TTFExtensionFilter;
    }

    public static ControlTTFSelecter create() {
        return new ControlTTFSelecter();
    }

    public ControlTTFSelecter name(String baseName) {
        this.baseName = baseName;

        ttfSelector.getItems().addAll(SystemTools.ttfList());
        ttfSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue == null || newValue.isBlank()) {
                    return;
                }
                ttfFile = newValue;
                AppVariables.setUserConfigValue(baseName + "TTF", newValue);
            }
        });
        ttfFile = AppVariables.getUserConfigValue(baseName + "TTF", null);
        if (ttfFile == null) {
            if (!ttfSelector.getItems().isEmpty()) {
                ttfSelector.getSelectionModel().select(0);
            }
        } else {
            ttfSelector.setValue(ttfFile);
        }
        return this;
    }

    @Override
    public void sourceFileChanged(File file) {
        ttfSelector.getSelectionModel().select(file.getAbsolutePath());
    }

}
