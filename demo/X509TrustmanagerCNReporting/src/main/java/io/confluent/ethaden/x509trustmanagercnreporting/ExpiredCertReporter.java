package io.confluent.ethaden.x509trustmanagercnreporting;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public final class ExpiredCertReporter implements X509TrustManager {

    private X509TrustManager trustManager = null;

    public ExpiredCertReporter(KeyStore trustStore)
            throws KeyStoreException, NoSuchAlgorithmException {

        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance("PKIX");
        tmf.init(trustStore);
        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager)
                this.trustManager = (X509TrustManager) tm;
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        CertificateException invalidException = null;
        try {
            this.trustManager.checkClientTrusted(chain, authType);

        } catch (CertificateException e) {
            invalidException = e;
        }
        if (invalidException==null)
        {
            // Certificate is valid. Return.
            return;
        }
        // Certificate check is invalid. Check whether it is just expired
        X509Certificate wrappedCert = new EternalCertificate(chain[0]);
        X509Certificate[] wrappedCertChain = chain.clone();
        wrappedCertChain[0] = wrappedCert;
        try {
            this.trustManager.checkClientTrusted(wrappedCertChain, authType);
            // Certificate is invalid due to being expired. Grab the common name and report it back
            String commonName = wrappedCert.getSubjectX500Principal().getName();
            Date expiredDate = wrappedCert.getNotAfter();
            invalidException = new CertificateException("Certificate for common name \"" + commonName + "\" expired on "+expiredDate, invalidException);
        } catch (CertificateException f) {
            // Do nothing here. Throw original or updated exception below
        }
        throw invalidException;
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        this.trustManager.checkServerTrusted(chain, authType);
    }

    // The code for the following class has been copied from here:
    // https://gist.github.com/divergentdave/9a68d820e3610513bd4fcdc4ae5f91a1
    private class EternalCertificate extends X509Certificate {
        private final X509Certificate originalCertificate;

        public EternalCertificate(X509Certificate originalCertificate) {
            this.originalCertificate = originalCertificate;
        }

        @Override
        public void checkValidity()
                throws CertificateExpiredException, CertificateNotYetValidException {
            // Ignore notBefore/notAfter
        }

        @Override
        public void checkValidity(Date date)
                throws CertificateExpiredException, CertificateNotYetValidException {
            // Ignore notBefore/notAfter
        }

        @Override
        public int getVersion() {
            return originalCertificate.getVersion();
        }

        @Override
        public BigInteger getSerialNumber() {
            return originalCertificate.getSerialNumber();
        }

        @Override
        public Principal getIssuerDN() {
            return originalCertificate.getIssuerDN();
        }

        @Override
        public Principal getSubjectDN() {
            return originalCertificate.getSubjectDN();
        }

        @Override
        public Date getNotBefore() {
            return originalCertificate.getNotBefore();
        }

        @Override
        public Date getNotAfter() {
            return originalCertificate.getNotAfter();
        }

        @Override
        public byte[] getTBSCertificate() throws CertificateEncodingException {
            return originalCertificate.getTBSCertificate();
        }

        @Override
        public byte[] getSignature() {
            return originalCertificate.getSignature();
        }

        @Override
        public String getSigAlgName() {
            return originalCertificate.getSigAlgName();
        }

        @Override
        public String getSigAlgOID() {
            return originalCertificate.getSigAlgOID();
        }

        @Override
        public byte[] getSigAlgParams() {
            return originalCertificate.getSigAlgParams();
        }

        @Override
        public boolean[] getIssuerUniqueID() {
            return originalCertificate.getIssuerUniqueID();
        }

        @Override
        public boolean[] getSubjectUniqueID() {
            return originalCertificate.getSubjectUniqueID();
        }

        @Override
        public boolean[] getKeyUsage() {
            return originalCertificate.getKeyUsage();
        }

        @Override
        public int getBasicConstraints() {
            return originalCertificate.getBasicConstraints();
        }

        @Override
        public byte[] getEncoded() throws CertificateEncodingException {
            return originalCertificate.getEncoded();
        }

        @Override
        public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException, SignatureException {
            originalCertificate.verify(key);
        }

        @Override
        public void verify(PublicKey key, String sigProvider)
                throws CertificateException, NoSuchAlgorithmException, InvalidKeyException,
                NoSuchProviderException, SignatureException {
            originalCertificate.verify(key, sigProvider);
        }

        @Override
        public String toString() {
            return originalCertificate.toString();
        }

        @Override
        public PublicKey getPublicKey() {
            return originalCertificate.getPublicKey();
        }

        @Override
        public Set<String> getCriticalExtensionOIDs() {
            return originalCertificate.getCriticalExtensionOIDs();
        }

        @Override
        public byte[] getExtensionValue(String oid) {
            return originalCertificate.getExtensionValue(oid);
        }

        @Override
        public Set<String> getNonCriticalExtensionOIDs() {
            return originalCertificate.getNonCriticalExtensionOIDs();
        }

        @Override
        public boolean hasUnsupportedCriticalExtension() {
            return originalCertificate.hasUnsupportedCriticalExtension();
        }
    }
}
