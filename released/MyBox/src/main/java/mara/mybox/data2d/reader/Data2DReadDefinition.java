package mara.mybox.data2d.reader;

import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DReadDefinition extends Data2DOperator {

    public static Data2DReadDefinition create(Data2D_Edit data) {
        Data2DReadDefinition op = new Data2DReadDefinition();
        return op.setData(data) ? op : null;
    }

    @Override
    public void handleData() {
    }

}
