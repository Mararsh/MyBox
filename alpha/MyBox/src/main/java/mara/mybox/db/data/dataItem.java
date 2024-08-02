package mara.mybox.db.data;

/**
 * @Author Mara
 * @CreateDate 2024-8-2
 * @License Apache License Version 2.0
 */
public class dataItem extends BaseData {

    protected long addrid;
    protected String address, icon;

    @Override
    public boolean valid() {
        return address != null && !address.isBlank();
    }

    /*
        Static methods
     */
    public static dataItem create() {
        return new dataItem();
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
