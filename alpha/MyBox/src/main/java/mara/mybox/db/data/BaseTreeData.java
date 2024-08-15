package mara.mybox.db.data;

/**
 * @Author Mara
 * @CreateDate 2020-7-19
 * @License Apache License Version 2.0
 */
public abstract class BaseTreeData extends BaseData {


    /*
        abstract
     */
    public abstract String toText();

    public abstract String toXml(String prefix);

    public abstract String toHtml();

    public abstract String toJson(String prefix);

}
