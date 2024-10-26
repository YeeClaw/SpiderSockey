package com.iliadstudios.spidersockey;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.PrivateKey;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.nio.file.Files;


public class Sockey extends WebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(Sockey.class);
    private final String TOKEN;

    public Sockey(InetSocketAddress address, SSLContext sslContext, String token) {
        super(address);
        this.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));

        this.TOKEN = token;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (!this.TOKEN.equals(handshake.getFieldValue("Authorization"))) {
            log.warn("Invalid token! Closing connection...");
            conn.close();
        } else {
            log.info("Connection opened with valid token! ({})", conn.getRemoteSocketAddress());
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        log.info("Connection closed! ({})", conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        log.info("Message received! ({}): {}", conn.getRemoteSocketAddress(), message);
        this.parseMessage(conn, message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        log.error("Error! ({}): {}", conn.getRemoteSocketAddress(), ex.getMessage());
    }

    @Override
    public void onStart() {
        log.info("Sockey server started! ({})", this.getAddress());
    }

    public void stopServer() {
        try {
            this.stop();
            log.info("Socky server stopped successfully!");
        } catch (Exception e) {
            System.out.println("Error stopping Socky server: " + e.getMessage());
        }
    }

    public void parseMessage(WebSocket conn, String message) {
        if ("Ping!".equals(message)) {
            conn.send("Pong!");
            log.info("Pong!");
        }

        if (message.startsWith("say ")) {
            executeCommand(message, conn);
        }
    }

    private void executeCommand(String command, WebSocket conn) {
        // Make better
        log.info("Executing command: {} from {}", command, conn.getRemoteSocketAddress());
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
            CommandSourceStack sourceStack = server.createCommandSourceStack();
            ParseResults<CommandSourceStack> parseResults = dispatcher.parse(command, sourceStack);
            try {
                dispatcher.execute(parseResults);
                conn.send("1");
            } catch (Exception e) {
                log.error("Error executing command: {}", e.getMessage());
                conn.send("0");
            }
        }
    }

    public static SSLContext createSSLContext(String certFile, String keyFile) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert;
        try (FileInputStream fis = new FileInputStream(certFile)) {
            caCert = (X509Certificate) cf.generateCertificate(fis);
        }

        String key = new String(Files.readAllBytes(Paths.get(keyFile)))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("caCert", caCert);
        ks.setKeyEntry("privateKey", privateKey, new char[0], new Certificate[]{caCert});

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, new char[0]);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        return sslContext;
    }
}
