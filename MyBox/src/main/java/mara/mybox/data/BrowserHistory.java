/*
 * Apache License Version 2.0
 */
package mara.mybox.data;

/**
 *
 * @author mara
 */
public class BrowserHistory {

    private String address, title, icon;
    private long visitTime;

    public BrowserHistory() {
    }

    /*
        get/set
     */
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(long visitTime) {
        this.visitTime = visitTime;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

}
