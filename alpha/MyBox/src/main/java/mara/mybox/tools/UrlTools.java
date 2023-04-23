package mara.mybox.tools;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class UrlTools {

    public static String checkURL(String value, Charset charset) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            String address = value;
            String addressS = address.toLowerCase();
            if (addressS.startsWith("file:/")
                    || addressS.startsWith("http://")
                    || addressS.startsWith("https://")) {

            } else if (address.startsWith("//")) {
                address = "http:" + value;
            } else {
                File file = new File(address);
                if (file.exists()) {
                    address = file.toURI().toString();
                } else {
                    address = "https://" + value;
                }
            }
            address = decodeURL(address, charset);
            return address;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return value;
        }
    }

    public static String decodeURL(String value, Charset charset) {
        try {
            if (value == null) {
                return null;
            }
            return URLDecoder.decode(value, charset);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return value;
        }
    }

    public static String decodeURL(File file, Charset charset) {
        try {
            if (file == null) {
                return null;
            }
            return decodeURL(file.toURI().toString(), charset);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static String encodeURL(String value, Charset charset) {
        try {
            if (value == null) {
                return null;
            }
            return URLEncoder.encode(value, charset);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return value;
        }
    }

    public static String encodeURL(File file, Charset charset) {
        try {
            if (file == null) {
                return null;
            }
            return encodeURL(file.toURI().toString(), charset);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }

    }

    public static String fullAddress(String baseAddress, String address) {
        try {
            URL url = fullUrl(new URI(baseAddress).toURL(), address);
            return url.toString();
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
            return address;
        }
    }

    public static URL fullUrl(URL baseURL, String address) {
        try {
            return new URL(baseURL, address);
        } catch (Exception e) {
            try {
                return new URI(address).toURL();
            } catch (Exception ex) {
                MyBoxLog.debug(ex);
                return null;
            }
        }
    }

    public static String filePrefix(URL url) {
        if (url == null) {
            return "";
        }
        String file = file(url);
        if (file == null) {
            return "";
        }
        int pos = file.lastIndexOf(".");
        file = pos < 0 ? file : file.substring(0, pos);
        return file;
    }

    public static URI uri(String address) {
        try {
            URI u;
            if (address.startsWith("file:")) {
                u = new URI(address);
            } else if (!address.startsWith("http")) {
                u = new URI("http://" + address);
            } else {
                u = new URI(address);
            }
            return u;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static String path(URL url) {
        if (url == null) {
            return null;
        }
        String urlPath = url.getPath();
        int pos = urlPath.lastIndexOf("/");
        String path = pos < 0 ? "" : urlPath.substring(0, pos + 1);
        return path;
    }

    public static String fullPath(URL url) {
        if (url == null) {
            return null;
        }
        String fullPath = url.getProtocol() + "://" + url.getHost() + path(url);
        return fullPath;
    }

    public static String fullPath(String address) {
        try {
            String path = fullPath(new URI(address).toURL());
            return path == null ? address : path;
        } catch (Exception e) {
            return address;
        }
    }

    public static String fileSuffix(URL url) {
        if (url == null) {
            return "";
        }
        String name = file(url);
        if (name == null) {
            return "";
        }
        int pos = name.lastIndexOf(".");
        name = pos < 0 ? "" : name.substring(pos);
        return name;
    }

    public static String file(URL url) {
        if (url == null) {
            return null;
        }
        try {
            String urlPath = url.getPath();
            int pos = urlPath.lastIndexOf("/");
            if (pos >= 0) {
                return pos < urlPath.length() - 1 ? urlPath.substring(pos + 1) : null;
            } else {
                return urlPath;
            }
        } catch (Exception e) {
            return null;
        }
    }

}
