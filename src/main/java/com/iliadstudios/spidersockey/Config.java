package com.iliadstudios.spidersockey;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = SpiderSockey.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<String> TOKEN = BUILDER
            .comment("The secret token for authentication")
            .define("token", "[your cool token here]");

    public static final ForgeConfigSpec.ConfigValue<String> IP = BUILDER
            .comment("The IP address of the websocket server")
            .define("ip", "[your cool ip here]");

    public static final ForgeConfigSpec.IntValue PORT = BUILDER
            .comment("The port of the websocket server. This default port doesn't explicitly need to be changed.")
            .defineInRange("port", 8443, 0, 65535);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static String token;
    public static String ip;
    public static int port;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        token = TOKEN.get();
        ip = IP.get();
        port = PORT.get();
    }
}
