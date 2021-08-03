/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.tools;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;

/**
 *
 * @author mara
 */
public class MessageDigestTools {

    public static byte[] SHA256(byte[] bytes) {
        return messageDigest(bytes, "SHA-256");
    }

    public static byte[] SHA256(File file) {
        return messageDigest(file, "SHA-256");
    }

    public static byte[] SHA256(BufferedImage image) {
        return messageDigest(BufferedImageTools.bytes(image), "SHA-256");
    }

    public static byte[] SHA1(byte[] bytes) {
        return messageDigest(bytes, "SHA-1");
    }

    public static byte[] SHA1(File file) {
        return messageDigest(file, "SHA-1");
    }

    public static byte[] SHA1(BufferedImage image) {
        return messageDigest(BufferedImageTools.bytes(image), "SHA-1");
    }

    public static void SignatureAlgorithms() {
        try {
            for (Provider provider : Security.getProviders()) {
                for (Provider.Service service : provider.getServices()) {
                    if (service.getType().equals("Signature")) {
                        MyBoxLog.debug(service.getAlgorithm());
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public static byte[] MD5(byte[] bytes) {
        return messageDigest(bytes, "MD5");
    }

    public static byte[] MD5(File file) {
        return messageDigest(file, "MD5");
    }

    public static byte[] MD5(BufferedImage image) {
        return messageDigest(BufferedImageTools.bytes(image), "MD5");
    }

    // https://docs.oracle.com/javase/10/docs/specs/security/standard-names.html#messagedigest-algorithms
    public static byte[] messageDigest(byte[] bytes, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] digest = md.digest(bytes);
            return digest;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static byte[] messageDigest(File file, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buf = new byte[AppValues.IOBufferLength];
                int len;
                while ((len = in.read(buf)) > 0) {
                    md.update(buf, 0, len);
                }
            }
            byte[] digest = md.digest();
            return digest;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

}
