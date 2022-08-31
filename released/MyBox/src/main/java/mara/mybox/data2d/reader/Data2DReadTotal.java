package mara.mybox.data2d.reader;

import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DReadTotal extends Data2DOperator {

    public static Data2DReadTotal create(Data2D_Edit data) {
        Data2DReadTotal op = new Data2DReadTotal();
        return op.setData(data) ? op : null;
    }

    @Override
    public void handleData() {
        reader.readTotal();
    }

}
