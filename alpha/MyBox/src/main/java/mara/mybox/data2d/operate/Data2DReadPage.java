package mara.mybox.data2d.operate;

import java.sql.Connection;
import java.util.List;
import mara.mybox.data2d.Data2D_Edit;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DReadPage extends Data2DOperate {

    public static Data2DReadPage create(Data2D_Edit data) {
        Data2DReadPage op = new Data2DReadPage();
        return op.setSourceData(data) ? op : null;
    }

    @Override
    public void handleData() {
        reader.readPage();
    }

    public List<List<String>> getRows() {
        return reader.getRows();
    }

    /*
        set
     */
    public Data2DReadPage setConn(Connection conn) {
        this.conn = conn;
        return this;
    }

}
