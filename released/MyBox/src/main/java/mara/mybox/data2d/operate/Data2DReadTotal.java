package mara.mybox.data2d.operate;

import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DReadTotal extends Data2DOperate {

    public static Data2DReadTotal create(Data2D_Edit data) {
        Data2DReadTotal op = new Data2DReadTotal();
        return op.setSourceData(data) ? op : null;
    }

    @Override
    public boolean go() {
        return reader.start(true);
    }

    @Override
    public void handleData() {
        reader.readTotal();
    }

}
