package io.confluent.ethaden.testjavassl.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.confluent.ethaden.x509trustmanagercnreporting.ExpiredCertReporter;

class TestJavaSSLServer {

    private static final Logger logger = LoggerFactory.getLogger(TestJavaSSLServer.class);

    public static void main(String args[]) {
        System.out.println("Usage: java -jar <jar file> [-e]");
        System.out.println("  -e: Report common name of expired client certificate in execption");
        int port = 1234;
        System.out.println(String.format("Starting server on port %d", port));

        boolean reportCommonNameIfExpired = false;
        if (args.length >= 1) {
            if (args[0].equals("-e")) {
                reportCommonNameIfExpired = true;
            }
        }

        SSLServerSocketFactory ssf =
                TestJavaSSLServer.getServerSocketFactory(reportCommonNameIfExpired);
        while (true) {
            try (ServerSocket listener = ssf.createServerSocket(port);
                    SSLServerSocket sslListener = (SSLServerSocket) listener) {
                sslListener.setEnabledCipherSuites(new String[] {
                        "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256", "TLS_AES_256_GCM_SHA384"});
                // sslListener.setEnabledProtocols(new String[] {"TLSv1.2"});
                sslListener.setEnabledProtocols(new String[] {"TLSv1.2", "TLSv1.3"});
                sslListener.setNeedClientAuth(true);

                String inputLine;
                while (true) {
                    try (Socket clientSocket = sslListener.accept()) {
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(clientSocket.getInputStream()));
                        while ((inputLine = in.readLine()) != null) {
                            System.out.println(inputLine);
                            out.println(inputLine);
                        }
                    } catch (IOException e) {
                        System.out.println("Unable to initiate connection: " + e.getMessage());
                        e.printStackTrace();
                        break;
                    }
                }
            } catch (javax.net.ssl.SSLHandshakeException e) {
                System.out.println("An SSLHandshakeException occurred: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Unable to initiate connection: " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }
        // } catch (javax.net.ssl.SSLHandshakeException e) {
        // System.out.println("An SSLHandshakeException occurred. Continuing...");
        // e.printStackTrace();
        // } catch (IOException e) {
        // System.out.println("Unable to initiate connection: " + e.getMessage());
        // e.printStackTrace();
        // break;
        // }


    }

    private static SSLServerSocketFactory getServerSocketFactory(
            boolean reportCommonNameIfExpired) {
        SSLServerSocketFactory ssf = null;
        try {
            // set up key manager to do server authentication
            // Open keystore
            String ksPass = System.getProperty("javax.net.ssl.keyStorePassword");
            if (ksPass == null) {
                ksPass = "password";
            }
            char[] ksPassphrase = ksPass.toCharArray();
            KeyStore ksKeys = KeyStore.getInstance("JKS");
            String keyStoreFilename = System.getProperty("javax.net.ssl.keyStore");
            if (keyStoreFilename == null) {
                keyStoreFilename = "./ssl/server.jks";
            }
            ksKeys.load(new FileInputStream(keyStoreFilename), ksPassphrase);
            KeyManagerFactory kmf =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ksKeys, ksPassphrase);
            // Initialize key manager
            // Open trust store
            String tsPass = System.getProperty("javax.net.ssl.trustStorePassword");
            if (tsPass == null) {
                tsPass = "password";
            }
            char[] tsPassPhrase = tsPass.toCharArray();
            KeyStore ksTrust = KeyStore.getInstance("JKS");
            String trustStoreFilename = System.getProperty("javax.net.ssl.trustStore");
            if (trustStoreFilename == null) {
                trustStoreFilename = "./ssl/truststore.jks";
            }
            String trustStoreType = System.getProperty("javax.net.ssl.trustStoreType");
            if (trustStoreType == null) {
                trustStoreType = TrustManagerFactory.getDefaultAlgorithm();
            }
            ksTrust.load(new FileInputStream(trustStoreFilename), tsPassPhrase);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(trustStoreType);
            tmf.init(ksTrust);
            TrustManager[] byPassTrustManagers = tmf.getTrustManagers();
            if (reportCommonNameIfExpired) {
                byPassTrustManagers = new TrustManager[] {new ExpiredCertReporter(ksTrust)};
            }

            // Build SSL context
            SSLContext ctx;
            ctx = SSLContext.getInstance("TLS");
            ctx.init(kmf.getKeyManagers(), byPassTrustManagers, null);
            // ctx.init(kmf.getKeyManagers(), null, null);

            ssf = ctx.getServerSocketFactory();
            return ssf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
