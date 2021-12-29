package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-9-7
 * @License Apache License Version 2.0
 */
public class ControlTargetPath extends ControlTargetFile {

    public ControlTargetPath() {
        isSource = false;
        isDirectory = true;
        checkQuit = false;
        permitNull = false;
        mustExist = true;
        notify = new SimpleBooleanProperty(false);
    }

    @Override
    public ControlTargetFile init() {
        String v = null;
        if (savedName != null) {
            v = UserConfig.getString(savedName, null);
        }
        if (v == null || v.isBlank()) {
            v = defaultFile != null ? defaultFile.getAbsolutePath() : null;
        }
        fileInput.setText(v);
        initTargetExistType();
        return this;
    }

}
