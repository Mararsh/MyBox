package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2020-5-27
 * @License Apache License Version 2.0
 */
public class AddressExtension {

    protected long adid;
    protected AddressExtensionType type;
    protected String value;

    public enum AddressExtensionType {
        ChineseName, EnglishName, ChineseFullName, EnglishFullName, Alias,
        AdministrativeCode, PostalCode, AddressCode
    }

    public static AddressExtension create() {
        return new AddressExtension();
    }

    public void setType(short typeValue) {
        switch (typeValue) {
            case 1:
                type = AddressExtensionType.ChineseName;
                break;
            case 2:
                type = AddressExtensionType.EnglishName;
                break;
            case 3:
                type = AddressExtensionType.ChineseFullName;
                break;
            case 4:
                type = AddressExtensionType.EnglishFullName;
                break;
            case 100:
                type = AddressExtensionType.Alias;
                break;
            case 500:
                type = AddressExtensionType.AdministrativeCode;
                break;
            case 501:
                type = AddressExtensionType.PostalCode;
                break;
            default:
                type = AddressExtensionType.AddressCode;
        }
    }

    /*
        get/set
     */
    public long getAdid() {
        return adid;
    }

    public AddressExtension setAdid(long adid) {
        this.adid = adid;
        return this;
    }

    public AddressExtensionType getType() {
        return type;
    }

    public AddressExtension setType(AddressExtensionType type) {
        this.type = type;
        return this;
    }

    public String getValue() {
        return value;
    }

    public AddressExtension setValue(String value) {
        this.value = value;
        return this;
    }

}
