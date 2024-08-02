package mara.mybox.db.data;

/**
 * @Author Mara
 * @CreateDate 2024-8-2
 * @License Apache License Version 2.0
 */
public class Note extends BaseData {

    protected long noteid;
    protected String note;

    @Override
    public boolean valid() {
        return note != null && !note.isBlank();
    }

    /*
        Static methods
     */
    public static Note create() {
        return new Note();
    }

    /*
        get/set
     */
    public long getNoteid() {
        return noteid;
    }

    public void setNoteid(long noteid) {
        this.noteid = noteid;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}
