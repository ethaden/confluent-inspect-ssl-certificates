package io.confluent.ethaden.testjavassl.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

class TestJavaSSLServer {
    public static void main(String args[]) {
        System.out.println("USAGE: java -jar <server-jar-file>");

        int port = 1234;

        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        SSLServerSocketFactory ssf = TestJavaSSLServer.getServerSocketFactory();
        //ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
        try (ServerSocket listener = ssf.createServerSocket(port);
                SSLServerSocket sslListener = (SSLServerSocket) listener) {
            sslListener.setEnabledCipherSuites(
                    new String[] {"TLS_DHE_DSS_WITH_AES_256_CBC_SHA256", "TLS_AES_256_GCM_SHA384"});
            // sslListener.setEnabledProtocols(new String[] {"TLSv1.2"});
            sslListener.setEnabledProtocols(new String[] {"TLSv1.2", "TLSv1.3"});
            // sslListener.setNeedClientAuth(false);

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
                }
            }
        } catch (IOException e) {
            System.out.println("Unable to initiate connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static SSLServerSocketFactory getServerSocketFactory() {
        SSLServerSocketFactory ssf = null;
        try {
            // set up key manager to do server authentication
            // Open keystore
            char[] ksPassphrase = "password".toCharArray();
            // KeyStore kmf = KeyManagerFactory.getInstance("SunX509");
            KeyStore ksKeys = KeyStore.getInstance("JKS");
            ksKeys.load(new FileInputStream("./ssl/server.jks"), ksPassphrase);
            // Initialize key manager
            // Open trust store
            char[] tsPassPhrase = "password".toCharArray();
            KeyStore ksTrust = KeyStore.getInstance("JKS");
            ksTrust.load(new FileInputStream("./ssl/truststore.jks"), tsPassPhrase);

            KeyManagerFactory kmf =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ksKeys, ksPassphrase);
            TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ksTrust);
            // Build SSL context
            SSLContext ctx;
            ctx = SSLContext.getInstance("TLS");
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            // ctx.init(kmf.getKeyManagers(), null, null);

            ssf = ctx.getServerSocketFactory();
            return ssf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
