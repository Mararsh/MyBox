package mara.mybox.data;

import java.security.cert.Certificate;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class CertificateEntry {

    protected String alias;
    protected long createTime;
    protected Certificate[] certificateChain;

    public static CertificateEntry create() {
        return new CertificateEntry();
    }

    public String getCertificates() {
        if (certificateChain == null) {
            return "";
        }
        String s = "";
        for (Certificate cert : certificateChain) {
            s += cert + "\n\n";
        }
        return s;
    }

    public String getAlias() {
        return alias;
    }

    public CertificateEntry setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public long getCreateTime() {
        return createTime;
    }

    public CertificateEntry setCreateTime(long createTime) {
        this.createTime = createTime;
        return this;
    }

    public Certificate[] getCertificateChain() {
        return certificateChain;
    }

    public CertificateEntry setCertificateChain(Certificate[] certificateChain) {
        this.certificateChain = certificateChain;
        return this;
    }

}
