package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2023-7-12
 * @License Apache License Version 2.0
 */
public class DoublePath extends DoubleRectangle {

    protected String content;

    public DoublePath() {
        content = null;
    }

    public DoublePath(String content) {
        this.content = content;
    }

    /*
        set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /*
    get
     */
    public String getContent() {
        return content;
    }

}
