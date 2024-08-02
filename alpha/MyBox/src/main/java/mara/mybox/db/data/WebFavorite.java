package mara.mybox.db.data;

/**
 * @Author Mara
 * @CreateDate 2024-8-2
 * @License Apache License Version 2.0
 */
public class WebFavorite extends TreeNode {

    protected long addrid;
    protected String address, icon;

    public WebFavorite() {
        tableName = "Web_Favorite";
        idName = "addrid";
    }

    @Override
    public boolean valid() {
        return address != null && !address.isBlank();
    }

    /*
        Static methods
     */
    public static WebFavorite create() {
        return new WebFavorite();
    }

    /*
        get/set
     */
    public long getAddrid() {
        return addrid;
    }

    public void setAddrid(long addrid) {
        this.addrid = addrid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

}
