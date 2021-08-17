/*
 * Apache License Version 2.0
 */
package mara.mybox.data;

import java.util.List;

/**
 *
 * @author mara
 */
public class MediaList {

    protected String name;
    protected List<MediaInformation> medias;

    public MediaList() {
    }

    public static MediaList create() {
        return new MediaList();
    }

    public MediaList(String name) {
        this.name = name;
    }

    /*
        get/set
     */
    public String getName() {
        return name;
    }

    public MediaList setName(String name) {
        this.name = name;
        return this;
    }

    public List<MediaInformation> getMedias() {
        return medias;
    }

    public MediaList setMedias(List<MediaInformation> medias) {
        this.medias = medias;
        return this;
    }

}
