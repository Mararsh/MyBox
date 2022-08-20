package mara.mybox.data2d.reader;

import java.util.List;
import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DReadColumnNames extends Data2DOperator {

    public static Data2DReadColumnNames create(Data2D_Edit data) {
        Data2DReadColumnNames op = new Data2DReadColumnNames();
        return op.setData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        data2D.checkForLoad();
        return true;
    }

    @Override
    public void handleData() {
        reader.readColumnNames();
    }

    public List<String> getNames() {
        return reader.names;
    }

}
