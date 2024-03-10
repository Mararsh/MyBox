package mara.mybox.data2d.operate;

import mara.mybox.data2d.operate.Data2DOperate;
import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DReadDefinition extends Data2DOperate {

    public static Data2DReadDefinition create(Data2D_Edit data) {
        Data2DReadDefinition op = new Data2DReadDefinition();
        return op.setSourceData(data) ? op : null;
    }

    @Override
    public void handleData() {
    }

}
