package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class SecurityTools {

    public static String keystore() {
        String jvm_cacerts = System.getProperty("java.home") + File.separator + "lib" + File.separator + "security" + File.separator + "cacerts";
        try {
            File file = myboxCacerts();
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                FileCopyTools.copyFile(new File(jvm_cacerts), file);
            }
            if (file.exists()) {
                return file.getAbsolutePath();
            } else {
                return jvm_cacerts;
            }
        } catch (Exception e) {
            return jvm_cacerts;
        }
    }

    public static String keystorePassword() {
        return "changeit";
    }

    public static void resetKeystore() {
        FileDeleteTools.delete(myboxCacerts());
        keystore();
    }

    public static File myboxCacerts() {
        return new File(AppVariables.MyboxDataPath + File.separator + "security" + File.separator + "cacerts_mybox");
    }

    public static void SSLServerSocketInfo() {
        try {
            SSLServerSocket sslServerSocket;
            SSLServerSocketFactory ssl = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            sslServerSocket = (SSLServerSocket) ssl.createServerSocket();
            String[] cipherSuites = sslServerSocket.getSupportedCipherSuites();
            for (String suite : cipherSuites) {
                MyBoxLog.debug(suite);
            }
            String[] protocols = sslServerSocket.getSupportedProtocols();
            for (String protocol : protocols) {
                MyBoxLog.debug(protocol);
            }
        } catch (Exception e) {
        }
    }

    public static boolean isHostCertificateInstalled(String host) {
        try {
            SSLContext context = SSLContext.getInstance(AppValues.HttpsProtocal);
            context.init(null, null, null);
            SSLSocket socket = (SSLSocket) context.getSocketFactory().createSocket(host, 443);
            socket.setSoTimeout(UserConfig.getUserConfigInt("WebConnectTimeout", 10000));
            try {
                socket.startHandshake();
                socket.close();
                return true;
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static String installCertificates(String keyStoreFile, String passwd, Certificate[] certs, String[] names) {
        try {
            if (keyStoreFile == null || certs == null || names == null || certs.length != names.length) {
                return Languages.message("InvalidData");
            }
            char[] passphrase = passwd.toCharArray();
            File cacerts = new File(keyStoreFile);
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(cacerts))) {
                keyStore.load(in, passphrase);
            } catch (Exception e) {
                return e.toString();
            }
            File tmpFile = TmpFileTools.getTempFile();
            try (final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                for (int i = 0; i < certs.length; i++) {
                    String alias = names[i].replaceAll(" ", "-");
                    keyStore.setCertificateEntry(alias, certs[i]);
                }
                keyStore.store(out, passphrase);
            } catch (Exception e) {
                return e.toString();
            }
            FileTools.rename(tmpFile, cacerts);
            return null;
        } catch (Exception e) {
            return e.toString();
        }
    }

    public static String installCertificates(Certificate[] certs, String[] names) {
        return installCertificates(SecurityTools.keystore(), SecurityTools.keystorePassword(), certs, names);
    }

    public static Certificate[] getCertificatesByFile(File certFile) {
        try (final FileInputStream inStream = new FileInputStream(certFile)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> data = cf.generateCertificates(inStream);
            if (data == null || data.isEmpty()) {
                return null;
            }
            Certificate[] certs = new Certificate[data.size()];
            int index = 0;
            for (Certificate cert : data) {
                certs[index++] = cert;
            }
            return certs;
        } catch (Exception e) {
            return null;
        }
    }

    public static String installCertificateByFile(String keyStoreFile, String passwd, File certFile, String alias, boolean wholeChain) {
        try {
            if (wholeChain) {
                Certificate[] certs = getCertificatesByFile(certFile);
                if (certs == null || certs.length == 0) {
                    return Languages.message("InvalidData");
                }
                String[] names = new String[certs.length];
                names[0] = alias;
                for (int i = 1; i < certs.length; i++) {
                    names[i] = alias + "-chain-" + i;
                }
                return installCertificates(keyStoreFile, passwd, certs, names);
            } else {
                Certificate cert = getCertificateByFile(certFile);
                if (cert == null) {
                    return Languages.message("InvalidData");
                }
                return installCertificate(keyStoreFile, passwd, cert, alias);
            }
        } catch (Exception e) {
            return e.toString();
        }
    }

    public static String installCertificateByFile(File certFile, String alias, boolean wholeChain) {
        return installCertificateByFile(SecurityTools.keystore(), SecurityTools.keystorePassword(), certFile, alias, wholeChain);
    }

    public static String installHostsCertificates(List<String> hosts, boolean wholeChain) {
        if (hosts == null || hosts.isEmpty()) {
            return Languages.message("InvalidData");
        }
        try {
            List<Certificate> certsList = new ArrayList<>();
            List<String> namesList = new ArrayList<>();
            for (String host : hosts) {
                if (wholeChain) {
                    Certificate[] hostCerts = getCertificatesByHost(host);
                    if (hostCerts == null) {
                        continue;
                    }
                    for (int i = 0; i < hostCerts.length; i++) {
                        certsList.add(hostCerts[i]);
                        namesList.add(host + (i == 0 ? "" : " chain " + i));
                    }
                } else {
                    Certificate hostCert = getCertificateByHost(host);
                    if (hostCert == null) {
                        continue;
                    }
                    certsList.add(hostCert);
                    namesList.add(host);
                }
            }
            if (certsList.isEmpty()) {
                return Languages.message("InvalidData");
            }
            String keyStoreFile = SecurityTools.keystore();
            String passwd = SecurityTools.keystorePassword();
            Certificate[] certs = new Certificate[certsList.size()];
            String[] names = new String[certsList.size()];
            for (int i = 0; i < namesList.size(); i++) {
                certs[i] = certsList.get(i);
                names[i] = namesList.get(i);
            }
            return installCertificates(keyStoreFile, passwd, certs, names);
        } catch (Exception e) {
            return e.toString();
        }
    }

    // https://github.com/escline/InstallCert/blob/master/InstallCert.java
    public static String installCertificateByHost(String keyStoreFile, String passwd, String host, String alias, boolean wholeChain) {
        try {
            if (wholeChain) {
                Certificate[] certs = getCertificatesByHost(host);
                if (certs == null || certs.length == 0) {
                    return Languages.message("InvalidData");
                }
                String[] names = new String[certs.length];
                names[0] = alias;
                for (int i = 1; i < certs.length; i++) {
                    names[i] = alias + "-chain-" + i;
                }
                return installCertificates(keyStoreFile, passwd, certs, names);
            } else {
                Certificate cert = getCertificateByHost(host);
                if (cert == null) {
                    return Languages.message("InvalidData");
                }
                return installCertificate(keyStoreFile, passwd, cert, alias);
            }
        } catch (Exception e) {
            return e.toString();
        }
    }

    public static String installCertificateByHost(String host, String alias, boolean wholeChain) {
        try {
            return installCertificateByHost(SecurityTools.keystore(), SecurityTools.keystorePassword(), host, alias, wholeChain);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
            return e.toString();
        }
    }

    public static String installCertificateByHost(String host, boolean wholeChain) {
        try {
            return installCertificateByHost(host, host, wholeChain);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
            return e.toString();
        }
    }

    public static String uninstallCertificate(String keyStoreFile, String passwd, List<String> aliases) throws Exception {
        try {
            char[] passphrase = passwd.toCharArray();
            File cacerts = new File(keyStoreFile);
            KeyStore keyStore;
            try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(cacerts))) {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(in, passphrase);
            } catch (Exception e) {
                return e.toString();
            }
            File tmpFile = TmpFileTools.getTempFile();
            try (final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                for (String alias : aliases) {
                    keyStore.deleteEntry(alias);
                }
                keyStore.store(out, passphrase);
            } catch (Exception e) {
                return e.toString();
            }
            FileTools.rename(tmpFile, cacerts);
            return null;
        } catch (Exception e) {
            return e.toString();
        }
    }

    public static Certificate getCertificateByHost(String host) {
        try {
            SSLContext context = SSLContext.getInstance(AppValues.HttpsProtocal);
            context.init(null, null, null);
            SSLSocket socket = (SSLSocket) context.getSocketFactory().createSocket(host, 443);
            socket.setSoTimeout(UserConfig.getUserConfigInt("WebConnectTimeout", 10000));
            try {
                socket.startHandshake();
                socket.close();
            } catch (Exception e) {
            }
            return socket.getSession().getPeerCertificates()[0];
        } catch (Exception e) {
            return null;
        }
    }

    public static Certificate[] getCertificatesByHost(String host) {
        try {
            SSLContext context = SSLContext.getInstance(AppValues.HttpsProtocal);
            context.init(null, null, null);
            SSLSocket socket = (SSLSocket) context.getSocketFactory().createSocket(host, 443);
            socket.setSoTimeout(UserConfig.getUserConfigInt("WebConnectTimeout", 10000));
            try {
                socket.startHandshake();
                socket.close();
            } catch (Exception e) {
            }
            return socket.getSession().getPeerCertificates();
        } catch (Exception e) {
            return null;
        }
    }

    public static Certificate getCertificateByFile(File certFile) {
        try (final FileInputStream inStream = new FileInputStream(certFile)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return cf.generateCertificate(inStream);
        } catch (Exception e) {
            return null;
        }
    }

    public static String installCertificate(String keyStoreFile, String passwd, Certificate cert, String name) {
        if (cert == null || name == null) {
            return Languages.message("InvalidData");
        }
        Certificate[] certs = {cert};
        String[] names = {name};
        return installCertificates(keyStoreFile, passwd, certs, names);
    }

}
