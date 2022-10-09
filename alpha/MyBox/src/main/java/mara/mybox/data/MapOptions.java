package mara.mybox.data;

import java.io.File;

/**
 * @Author Mara
 * @CreateDate 2022-10-9
 * @License Apache License Version 2.0
 */
public class MapOptions {

    protected MapType mapType;
    protected GeoCoordinateSystem coordinateSystem;
    protected String markerImage;
    protected int markerSize, textSize, mapSize, dataMax;
    protected File markerImageFile;

    public enum MapType {
        TianDiTu, GaoDe
    }

}
