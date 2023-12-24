package mara.mybox.controller;

/**
 * @Author Mara
 * @CreateDate 2020-9-7
 * @License Apache License Version 2.0
 */
public class ControlTargetPath extends ControlTargetFile {

    public ControlTargetPath() {
        initPathSelecter();
    }

    public final ControlTargetPath initPathSelecter() {
        initSelecter();
        isSource = false;
        isDirectory = true;
        checkQuit = false;
        permitNull = false;
        mustExist = true;
        return this;
    }

}
