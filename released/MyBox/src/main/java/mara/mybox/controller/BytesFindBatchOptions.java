package mara.mybox.controller;

/**
 * @Author Mara
 * @CreateDate 2023-5-10
 * @License Apache License Version 2.0
 */
public class BytesFindBatchOptions extends FindReplaceBatchOptions {

    public BytesFindBatchOptions() {
        TipsLabelKey = "BytesFindBatchTips";
    }

    @Override
    protected void checkFindInput(String string) {
        validateFindBytes(string);
    }

}
