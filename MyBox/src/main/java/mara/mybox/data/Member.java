package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2020-02-13
 * @License Apache License Version 2.0
 */
public class Member {

    protected String type, object, member;

    public Member(String type, String object, String member) {
        this.type = type;
        this.object = object;
        this.member = member;
    }

    /*
        get/set
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

}
