/*
 * Apache License Version 2.0
 */

module mara.mybox {
    requires java.base;
    requires java.sql;
    requires java.management;
    requires jdk.management;
    requires javafx.controls;
    requires javafx.graphicsEmpty;
    requires javafx.fxml;
    requires javafx.controlsEmpty;
    requires javafx.web;
    requires javafx.mediaEmpty;
    requires javafx.media;
    requires javafx.graphics;
    requires javafx.baseEmpty;
    requires javafx.base;
    requires javafx.swing;
    requires pdfbox;
    requires fontbox;
    requires commons.logging;
    requires jai.imageio.core;
    requires jai.imageio.jpeg2000;
    requires jlayer;
    requires mp3spi;
    requires junit;
    requires tritonus.share;
    requires org.apache.logging.log4j.core;
    requires org.apache.logging.log4j;
    requires derby;

    exports mara.mybox;
    exports thridparty;
}
