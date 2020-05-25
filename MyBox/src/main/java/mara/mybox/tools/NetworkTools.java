package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.web.WebEngine;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import mara.mybox.data.CertificateBypass;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-9 7:46:58
 * @Description
 * @License Apache License Version 2.0
 */
public class NetworkTools {

    public static Map<String, String> readCookie(WebEngine webEngine) {
        try {
            String s = (String) webEngine.executeScript("document.cookie;");
            String[] vs = s.split(";");
            Map<String, String> m = new HashMap<>();
            for (String v : vs) {
                String[] vv = v.split("=");
                if (vv.length < 2) {
                    continue;
                }
                m.put(vv[0].trim(), vv[1].trim());
            }
            return m;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static int findFreePort(int port) {
        int p;
        try {
            Socket socket = new Socket(InetAddress.getLocalHost(), port);
            socket.close();
            p = port;
        } catch (Exception e) {
            try {
                try ( ServerSocket serverSocket = new ServerSocket(0)) {
                    p = serverSocket.getLocalPort();
                }
            } catch (Exception ex) {
                p = -1;
            }
        }
        return p;
    }

    public static boolean isPortUsed(int port) {
        try {
            Socket socket = new Socket(InetAddress.getLocalHost(), port);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void myBoxSSL() {
        HttpsURLConnection.setDefaultSSLSocketFactory(NetworkTools.MyBoxSSLSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(NetworkTools.MyBoxHostnameVerifier());
    }

    public static class MyBoxTrustManager implements X509TrustManager {

        public final X509TrustManager tm;
        public X509Certificate[] chain;

        public MyBoxTrustManager() throws Exception {
            char[] passphrase = SystemTools.keystorePassword().toCharArray();
            File cacerts = new File(SystemTools.keystore());
            KeyStore keyStore;
            try ( BufferedInputStream in = new BufferedInputStream(new FileInputStream(cacerts))) {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(in, passphrase);
            } catch (Exception e) {
                throw e;
            }
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init(keyStore);
            TrustManager[] trustmanagers = factory.getTrustManagers();
            if (trustmanagers.length == 0) {
                throw new Exception("no trust manager found");
            }
            tm = (X509TrustManager) trustmanagers[0];
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return tm.getAcceptedIssuers();
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            tm.checkClientTrusted(chain, authType);

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            this.chain = chain;
            if (chain == null || chain.length == 0) {
                return;
            }
            List<String> bypass = CertificateBypass.bypass();
            for (X509Certificate cert : chain) {
                String[] certlines = cert.toString().split("\n");
                for (String line : certlines) {
                    if (!line.contains("DNSName")) {
                        continue;
                    }
                    for (String host : bypass) {
                        if (line.contains(" " + host)
                                || line.contains("www." + host)
                                || line.contains("*." + host)) {
//                            logger.debug(host + "  " + line);
                            return;
                        }
                    }
                }
            }
            tm.checkServerTrusted(chain, authType);
        }
    }

    public static SSLSocketFactory MyBoxSSLSocketFactory() {
        try {
            SSLContext sc = SSLContext.getInstance(CommonValues.HttpsProtocal);
            sc.init(null, new TrustManager[]{new MyBoxTrustManager()}, new SecureRandom());
            return sc.getSocketFactory();
        } catch (Exception e) {
            logger.debug(e.toString());
            return AppVariables.defaultSSLSocketFactory;
        }

    }

    public static SSLSocketFactory DefaultSSLSocketFactory() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream keyStoreFile = new FileInputStream(new File(SystemTools.keystore()));
            String keyStorePassword = SystemTools.keystorePassword();
            keyStore.load(keyStoreFile, keyStorePassword.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keyStorePassword.toCharArray());
            KeyManager[] keyManagers = kmf.getKeyManagers();

            SSLContext ctx = SSLContext.getInstance(CommonValues.HttpsProtocal);
            ctx.init(keyManagers, null, new SecureRandom());

            return ctx.getSocketFactory();
        } catch (Exception e) {
            logger.debug(e.toString());
            return AppVariables.defaultSSLSocketFactory;
        }

    }

    public static HostnameVerifier MyBoxHostnameVerifier() {
        HostnameVerifier verifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                List<String> bypass = CertificateBypass.bypass();
//                logger.debug(hostname);
                for (String host : bypass) {
                    if (hostname.equals(" " + host)
                            || hostname.equals("www." + host)
                            || hostname.equals("*." + host)) {
//                        logger.debug(hostname + "  " + session.getPeerHost() + "  " + session.getProtocol() + "  " + session.getCipherSuite());
                        return true;
                    }
                }
                return AppVariables.defaultHostnameVerifier.verify(hostname, session);
            }
        };
        return verifier;
    }

    public static TrustManager[] trustAllManager() {
        TrustManager[] trustAllManager = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                    return new X509Certificate[0];
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs,
                        String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs,
                        String authType) {
                }
            }
        };
        return trustAllManager;
    }

    public static HostnameVerifier trustAllVerifier() {
        HostnameVerifier allHostsValid = (String hostname, SSLSession session) -> {
//            logger.debug(hostname + "  " + session.getPeerHost() + "  " + session.getProtocol() + "  " + session.getCipherSuite());
            return true;
        };
        return allHostsValid;
    }

    // https://nakov.com/blog/2009/07/16/disable-certificate-validation-in-java-ssl-connections/
    public static void trustAll() {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllManager(), new SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(trustAllVerifier());
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    // https://stackoverflow.com/questions/20050742/access-denied-to-setfactory-by-executing-httpsurlconnection-setdefaultsslsocketf
    public static void trust(URLConnection urlConnection) {
        try {
            if (urlConnection == null || !(urlConnection instanceof HttpsURLConnection)) {
                return;
            }
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllManager(), new java.security.SecureRandom());

            HttpsURLConnection uc = (HttpsURLConnection) urlConnection;
            uc.setSSLSocketFactory(sc.getSocketFactory());
            uc.setHostnameVerifier(trustAllVerifier());

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public static void trustAllHosts() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(trustAllVerifier());
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public static void trustHosts(List<String> hosts) {
        try {
            HostnameVerifier verifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    if (hosts.contains(hostname)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(verifier);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public static void defaultSSL() {
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(AppVariables.defaultSSLSocketFactory);
            HttpsURLConnection.setDefaultHostnameVerifier(AppVariables.defaultHostnameVerifier);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public static Certificate getCertificateByFile(File certFile) throws
            Exception {
        try ( FileInputStream inStream = new FileInputStream(certFile)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
            return cert;
        } catch (Exception e) {
            return null;
        }
    }

    public static Certificate getCertificateByHost(String keyStoreFile,
            String passwprd, String host)
            throws Exception {
        try {
            int port = 443;
            char[] passphrase = passwprd.toCharArray();
            File cacerts = new File(keyStoreFile);
            KeyStore keyStore;
            //        logger.debug("Loading KeyStore " + cacerts + "...");
            try ( BufferedInputStream in = new BufferedInputStream(new FileInputStream(cacerts))) {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(in, passphrase);
            } catch (Exception e) {
                return null;
            }
            SavingTrustManager tm = new SavingTrustManager(keyStore);
            SSLContext context = SSLContext.getInstance(CommonValues.HttpsProtocal);
            context.init(null, new TrustManager[]{tm}, new SecureRandom());
            SSLSocket socket = (SSLSocket) context.getSocketFactory().createSocket(host, port);
            socket.setSoTimeout(10000);
            try {
                socket.startHandshake();
                socket.close();
            } catch (SSLException e) {
            }
            X509Certificate[] chain = tm.chain;
            if (chain == null) {
                return null;
            }
            MessageDigest sha384 = MessageDigest.getInstance("SHA-384");
            X509Certificate cert = chain[0];
            sha384.update(cert.getEncoded());
            return cert;
        } catch (Exception e) {
            return null;
        }
    }

    public static String installCertificateByFile(String keyStoreFile,
            String passwprd,
            File certFile, String alias) throws Exception {
        try {
            char[] passphrase = passwprd.toCharArray();
            File cacerts = new File(keyStoreFile);
            KeyStore keyStore;
            try ( BufferedInputStream in = new BufferedInputStream(new FileInputStream(cacerts))) {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(in, passphrase);
            } catch (Exception e) {
                return e.toString();
            }

            try ( FileInputStream inStream = new FileInputStream(certFile);
                     BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(cacerts))) {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
                keyStore.setCertificateEntry(alias, cert);
                keyStore.store(out, passphrase);
                return null;
            } catch (Exception e) {
                return e.toString();
            }
        } catch (Exception e) {
//            logger.debug(e.toString());
            return e.toString();
        }
    }

    public static class SavingTrustManager implements X509TrustManager {

        public final X509TrustManager tm;
        public X509Certificate[] chain;

        public SavingTrustManager(KeyStore keystore) throws Exception {
            super();
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init(keystore);
            TrustManager[] trustmanagers = factory.getTrustManagers();
            if (trustmanagers.length == 0) {
                throw new Exception("no trust manager found");
            }
            tm = (X509TrustManager) trustmanagers[0];
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return tm.getAcceptedIssuers();
//            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            tm.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            this.chain = chain;
            tm.checkServerTrusted(chain, authType);
        }
    }

    public static String installCertificateByHost(String host, String alias)
            throws Exception {
        return installCertificateByHost(SystemTools.keystore(), SystemTools.keystorePassword(), host, alias);
    }

    // https://github.com/escline/InstallCert/blob/master/InstallCert.java
    public static String installCertificateByHost(String keyStoreFile,
            String passwprd,
            String host, String alias) throws Exception {
        try {
            int port = 443;
            char[] passphrase = passwprd.toCharArray();
            File cacerts = new File(keyStoreFile);
            KeyStore keyStore;
            try ( BufferedInputStream in = new BufferedInputStream(new FileInputStream(cacerts))) {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(in, passphrase);
            } catch (Exception e) {
                return e.toString();
            }
            SavingTrustManager tm = new SavingTrustManager(keyStore);
            SSLContext context = SSLContext.getInstance(CommonValues.HttpsProtocal);
            context.init(null, new TrustManager[]{tm}, new SecureRandom());
            SSLSocket socket = (SSLSocket) context.getSocketFactory().createSocket(host, port);
            socket.setSoTimeout(10000);
            try {
                socket.startHandshake();
                socket.close();
                logger.debug(host + " AlreadyInstalled");
                return "AlreadyInstalled";
            } catch (SSLException e) {
//                logger.debug(e.toString());
            }
            X509Certificate[] chain = tm.chain;
            if (chain == null) {
                return "Could not obtain server certificate chain";
            }
            MessageDigest md = MessageDigest.getInstance("SHA-384");
            for (X509Certificate cert : chain) {
                md.update(cert.getEncoded());
                keyStore.setCertificateEntry(alias, cert);
            }

            try ( BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(cacerts))) {
                keyStore.store(out, passphrase);
                return null;
            } catch (Exception e) {
                return e.toString();
            }

        } catch (Exception e) {
            logger.debug(e.toString());
            return e.toString();
        }

    }

    public static boolean isHostCertificateInstalled(String host)
            throws Exception {
        return isHostCertificateInstalled(SystemTools.keystore(), SystemTools.keystorePassword(), host);
    }

    public static boolean isHostCertificateInstalled(String keyStoreFile,
            String passwprd, String host)
            throws Exception {
        try {
            int port = 443;
            char[] passphrase = passwprd.toCharArray();
            File cacerts = new File(keyStoreFile);
            KeyStore keyStore;
            try ( BufferedInputStream in = new BufferedInputStream(new FileInputStream(cacerts))) {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(in, passphrase);
            } catch (Exception e) {
                return false;
            }
            SSLContext context = SSLContext.getInstance(CommonValues.HttpsProtocal);
            context.init(null, new TrustManager[]{
                new SavingTrustManager(keyStore)}, null);
            SSLSocket socket = (SSLSocket) context.getSocketFactory().createSocket(host, port);
            socket.setSoTimeout(10000);
            try {
                socket.startHandshake();
                socket.close();
                return true;
            } catch (SSLException e) {
//                logger.debug(e.toString());
                return false;
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }

    }

    public static String uninstallCertificate(String keyStoreFile,
            String passwprd,
            List<String> aliases) throws Exception {
        try {
            char[] passphrase = passwprd.toCharArray();
            File cacerts = new File(keyStoreFile);
            KeyStore keyStore;
            try ( BufferedInputStream in = new BufferedInputStream(new FileInputStream(cacerts))) {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(in, passphrase);
            } catch (Exception e) {
                return e.toString();
            }

            try ( BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(cacerts))) {
                for (String alias : aliases) {
                    keyStore.deleteEntry(alias);
                }
                keyStore.store(out, passphrase);
                return null;
            } catch (Exception e) {
                return e.toString();
            }
        } catch (Exception e) {
//            logger.debug(e.toString());
            return e.toString();
        }
    }

    public static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int b : bytes) {
            b &= 0xff;
            sb.append(HEXDIGITS[b >> 4]);
            sb.append(HEXDIGITS[b & 15]);
            sb.append(' ');
        }
        return sb.toString();
    }

    // https://www.cnblogs.com/starcrm/p/7071227.html
    public static InetAddress localHost() {
        try {
            InetAddress candidateAddress = null;
            logger.debug("InetAddress.getLocalHost():" + InetAddress.getLocalHost());
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces();
                    ifaces.hasMoreElements();) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                for (Enumeration inetAddrs = iface.getInetAddresses();
                        inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    logger.debug("inetAddr.getHostAddress:" + inetAddr.getHostAddress());
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                return InetAddress.getByName("localhost");
            }
            return jdkSuppliedAddress;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean download(String address, File targetFile) {
        try {
            if (targetFile == null || address == null) {
                return false;
            }
            File tmpFile = FileTools.getTempFile();
            URL url = new URL(address);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllManager(), new SecureRandom());
            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier(trustAllVerifier());
            InputStream inStream = conn.getInputStream();
            FileOutputStream fs = new FileOutputStream(tmpFile);
            byte[] buf = new byte[1204];
            int len;
            while ((len = inStream.read(buf)) != -1) {
                fs.write(buf, 0, len);
            }
            if (targetFile.exists()) {
                targetFile.delete();
            }
            tmpFile.renameTo(targetFile);
            return targetFile.exists();
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    public static String urlFileName(URL url) {
        try {
            if (url == null || url.getFile() == null) {
                return null;
            }
            String filename = url.getFile().substring(url.getFile().lastIndexOf('/'));
            String validname = "";
            for (int i = 0; i < filename.length(); ++i) {
                char c = filename.charAt(i);
                if (c >= 'a' && c <= 'z'
                        || c >= 'A' && c <= 'Z'
                        || c >= '0' && c <= '9'
                        || c == '_' || c == '-' || c == '.') {
                    validname += c;
                } else {
                    validname += "-";
                }
            }
            return validname;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public static File httpsPage(String address) {
        try {
            URL url = new URL(address);
            File pageFile = FileTools.getTempFile(".htm");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllManager(), new SecureRandom());
            connection.setSSLSocketFactory(sc.getSocketFactory());
            connection.setHostnameVerifier(trustAllVerifier());
            connection.connect();
            try ( BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(pageFile))) {
                byte[] buf = new byte[CommonValues.IOBufferLength];
                int len;
                while ((len = inStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }
            }
            return pageFile;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
