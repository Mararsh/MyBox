package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.tools.TTFTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-05
 * @License Apache License Version 2.0
 */
public class ControlTTFSelector extends BaseController {

    protected String ttfFile;

    @FXML
    protected ComboBox<String> ttfSelector;

    public ControlTTFSelector() {
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.TTF);
    }

    public static ControlTTFSelector create() {
        return new ControlTTFSelector();
    }

    public ControlTTFSelector name(String baseName) {
        this.baseName = baseName;

        List<String> files = TTFTools.ttfList();
        ttfSelector.getItems().addAll(files);
        ttfFile = UserConfig.getString(baseName + "TTF", null);
        if (ttfFile == null) {
            if (!files.isEmpty()) {
                ttfFile = files.get(0);
            }
        }
        ttfSelector.setValue(ttfFile);
        ttfSelector.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> s, String ov, String nv) {
                if (nv == null || nv.isBlank()) {
                    return;
                }
                ttfFile = nv;
                UserConfig.setString(baseName + "TTF", nv);
            }
        });

        return this;
    }

    @Override
    public void sourceFileChanged(File file) {
        ttfSelector.setValue(file.getAbsolutePath());
    }

    public String getTtfFile() {
        return ttfFile;
    }

}
