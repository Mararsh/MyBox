package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.tools.SystemTools;
import mara.mybox.tools.TTFTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.UserConfig;

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
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.TTF);
    }

    public static ControlTTFSelecter create() {
        return new ControlTTFSelecter();
    }

    public ControlTTFSelecter name(String baseName) {
        this.baseName = baseName;

        ttfSelector.getItems().addAll(TTFTools.ttfList());
        ttfSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue == null || newValue.isBlank()) {
                    return;
                }
                ttfFile = newValue;
                UserConfig.setUserConfigString(baseName + "TTF", newValue);
            }
        });
        ttfFile = UserConfig.getUserConfigString(baseName + "TTF", null);
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
