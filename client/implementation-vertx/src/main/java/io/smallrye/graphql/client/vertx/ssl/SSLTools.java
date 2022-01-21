package io.smallrye.graphql.client.vertx.ssl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import io.vertx.core.buffer.Buffer;

public class SSLTools {

    public static KeyStore createKeyStore(String path, String type, String password) {
        try {
            KeyStore keyStore = KeyStore.getInstance(type != null ? type : "JKS");
            if (password == null || password.isEmpty()) {
                throw new IllegalArgumentException("No password provided for keystore");
            }
            try (InputStream input = locateStream(path)) {
                keyStore.load(input, password.toCharArray());
            } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
                throw new IllegalArgumentException("Failed to initialize key store from classpath resource " + path,
                        e);
            }
            return keyStore;
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public static Buffer asBuffer(KeyStore keyStore, char[] password) {
        if (keyStore != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                keyStore.store(out, password);
                return Buffer.buffer(out.toByteArray());
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
                throw new RuntimeException("Failed to translate keystore to vert.x keystore", e);
            }
        }
        return null;
    }

    private static InputStream locateStream(String path) throws FileNotFoundException {
        if (path.startsWith("classpath:")) {
            path = path.replaceFirst("classpath:", "");
            InputStream resultStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
            if (resultStream == null) {
                resultStream = SSLTools.class.getResourceAsStream(path);
            }
            if (resultStream == null) {
                throw new IllegalArgumentException(
                        "Classpath resource " + path + " not found for GraphQL Client SSL configuration");
            }
            return resultStream;
        } else {
            if (path.startsWith("file:")) {
                path = path.replaceFirst("file:", "");
            }
            File certificateFile = new File(path);
            if (!certificateFile.isFile()) {
                throw new IllegalArgumentException(
                        "Certificate file: " + path + " not found for GraphQL Client SSL configuration");
            }
            return new FileInputStream(certificateFile);
        }
    }
}
