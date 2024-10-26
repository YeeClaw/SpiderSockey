package com.iliadstudios.spidersockey;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SpiderSockey.MOD_ID)
public class SpiderSockey
{
    public static final String MOD_ID = "spidersockey";
    private static final Logger LOGGER = LogUtils.getLogger();

    public SpiderSockey()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        // Create the config directory if it doesn't exist
        Path config = Paths.get("config/spidersockey/");
        if (!Files.exists(config)) {
            LOGGER.info("Config directory not found! Creating instead...");
            try {
                Files.createDirectories(config);
            } catch (Exception e) {
                LOGGER.error("Error creating config directory: {}", e.getMessage());
            }
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) throws Exception {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");

        // Write method `isReadyToStart() -> boolean` to make sure the server is ready to start

        Path certificatePath = Paths.get("config/spidersockey/server.crt");
        Path keyPath = Paths.get("config/spidersockey/server.key");

        if (!isReadyToStart(certificatePath, keyPath)) {
            LOGGER.error("Sockey server is not ready to start! Exiting...");
            return;
        }

        SSLContext sslContext = Sockey.createSSLContext(certificatePath.toString(), keyPath.toString());
        Sockey sockyJavaWeb = new Sockey(new InetSocketAddress(Config.ip, Config.port), sslContext, Config.token);
        sockyJavaWeb.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down... Stopping sockey server");
            sockyJavaWeb.stopServer();
        }));
    }

    private boolean isReadyToStart(Path certPath, Path privateKeyPath) {
        boolean customIP = !(Config.ip.equals("[your cool ip here]"));
        boolean customToken = !(Config.token.equals("[your cool token here]"));
        boolean certExists = Files.exists(certPath);
        boolean keyExists = Files.exists(privateKeyPath);

        boolean[] checks = {customIP, customToken, certExists, keyExists};

        if (!customIP) {
            LOGGER.error("Please set a custom IP in the config file!");
        }
        if (!customToken) {
            LOGGER.error("Please set a custom token in the config file!");
        }
        if (!certExists) {
            LOGGER.error("Please provide a server certificate at config/spidersockey/server.crt");
        }
        if (!keyExists) {
            LOGGER.error("Please provide a server key at config/spidersockey/server.key");
        }

        for (boolean check : checks) {
            if (!check) {
                return false;
            }
        }

        return true;
    }
}
