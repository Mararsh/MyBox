package mara.mybox.controller;

import java.util.ArrayList;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.FileFilters;

/**
 * @Author Mara
 * @CreateDate 2019-12-8
 * @Description
 * @License Apache License Version 2.0
 */
public class FFmpegMediasTableController extends ControlMediaTable {

    protected BaseBatchFFmpegController parent;

    public FFmpegMediasTableController() {
        sourceExtensionFilter = FileFilters.FFmpegMediaExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            examples = new ArrayList();
            examples.add("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");
            examples.add("http://download.oracle.com/otndocs/products/javafx/JavaRap/prog_index.m3u8");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
