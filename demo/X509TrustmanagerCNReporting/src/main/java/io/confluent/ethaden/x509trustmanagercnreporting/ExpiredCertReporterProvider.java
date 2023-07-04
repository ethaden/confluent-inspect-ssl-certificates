package io.confluent.ethaden.x509trustmanagercnreporting;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

public class ExpiredCertReporterProvider extends java.security.Provider {

    private final static String NAME = "ExpiredCertReporter";
    private final static String INFO =
            "X509TrustManagerFactorySpiWithCNReporting JSSE Provider (implements trust factory with output of common name for expired certificates)";
    private final static String VERSION = "1.0";

    public ExpiredCertReporterProvider() {
        super(NAME, VERSION, INFO);
        // AccessController.doPrivileged(new PrivilegedAction<Void>() {
        // public Void run() {
        // put("TrustManagerFactory." + X509TrustManagerFactorySpiWithCNReporting.getAlgorithm(),
        // X509TrustManagerFactorySpiWithCNReporting.class.getName());
        // return null;
        // }
        // });
        put("TrustManagerFactory." + X509TrustManagerFactorySpiWithCNReporting.getAlgorithm(),
                X509TrustManagerFactorySpiWithCNReporting.class.getName());
    }

    public final static class X509TrustManagerFactorySpiWithCNReporting
            extends TrustManagerFactorySpi {

        KeyStore truststore = null;

        public static String getAlgorithm() {
            return "ExpiredCertReporter";
        }

        @Override
        protected void engineInit(KeyStore truststore) throws KeyStoreException {
            this.truststore = truststore;
        }

        @Override
        protected void engineInit(ManagerFactoryParameters params)
                throws InvalidAlgorithmParameterException {
            // Nothing to do here
        }

        @Override
        protected TrustManager[] engineGetTrustManagers() {
            try {
                return new TrustManager[] {new ExpiredCertReporter(this.truststore)};
            } catch (KeyStoreException e) {
                throw new IllegalStateException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
