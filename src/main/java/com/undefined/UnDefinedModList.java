package com.undefined;

import java.util.Collections;
import java.util.List;

import static com.undefined.LogicalSide.BOTH;
import static com.undefined.LogicalSide.CLIENT;

public class UnDefinedModList {

    public static void run() {
        ModList modList = new ModList();
        modList.setDirectory("C:/Users/q0xn7/AppData/Roaming/PrismLauncher/instances/UnDefined 1.20.1/.minecraft/mods");
        modList.setDeleteDirectory("C:/Users/q0xn7/Desktop/deletedMods");
        modList.setFileDeletionEnabled(true);

        modList.addMod(
                "embeddium-0.3.31+mc1.20.1",
                "Embeddium",
                "embeddium",
                CLIENT,
                "(Performance Mod) Makes minecraft run better. It's a fork of sodium",
                false
        );

        modList.addMod(
                "oculus-mc1.20.1-1.7.0",
                "Oculus",
                "oculus",
                CLIENT,
                "Adds shaders",
                false
        );

        modList.addMod(
                "ferritecore-6.0.1-forge",
                "Ferrite Core",
                "ferritecore",
                BOTH,
                "(Performance Mod) Reduces minecraft's ram usage",
                false
        );

        modList.addMod(
                "noisium-forge-2.3.0+mc1.20-1.20.1",
                "Noisium",
                "noisium",
                BOTH,
                "(Performance Mod) Optimises worldgen performance for a better gameplay experience.",
                false
        );

        modList.addMod(
                "ImmediatelyFast-Forge-1.2.21+1.20.4",
                "ImmediatelyFast",
                "immediatelyfast",
                BOTH,
                "(Performance Mod) Speed up and optimize immediate mode rendering in Minecraft. It uses batch rendering to achieve this",
                false
        );

        modList.addMod(
                "modernfix-forge-5.19.4+mc1.20.1",
                "ModernFix",
                "modernfix",
                BOTH,
                "(Performance Mod) A performance mod for modern Minecraft that significantly improves launch times, world load times, memory usage",
                false
        );


        modList.addMod(
                "DynamicTrees-1.20.1-1.4.0-ALPHA02",
                "Dynamic Trees",
                "dynamictrees",
                BOTH,
                "Nice trees",
                false
        );

        modList.addMod(
                "DynamicTreesPlus-1.20.1-1.2.0-BETA3",
                "Dynamic Trees Plus",
                "dynamictreesplus",
                BOTH,
                "(Addon) Addon for dynamic trees",
                false,
                Collections.singletonList("dynamictrees")
        );

        modList.addMod(
                "memoryleakfix-forge-1.17+-1.1.5",
                "Memory Leak Fix",
                "memoryleakfix",
                BOTH,
                "(Performance Mod) A mod which fixes multiple memory leaks, both client-side & server-side",
                false
        );

        modList.addMod(
                "entityculling-forge-1.7.0-mc1.20.1",
                "EntityCulling",
                "entityculling",
                CLIENT,
                "(Performance Mod) This mod uses async path-tracing to hide Tiles/Entities that are not visible.",
                false
        );

        modList.addMod(
                "packetfixer-forge-1.4.2-1.19-to-1.20.1",
                "Packet Fixer",
                "packetfixer",
                BOTH,
                "A simple mod to solve various problems with packets/NBT's",
                false
        );

        modList.addMod(
                "BadOptimizations-2.1.4-1.20.1",
                "BadOptimizations",
                "badoptimizations",
                CLIENT,
                "(Performance Mod) Makes some rendering optimizations",
                false
        );

        modList.addMod(
                "fast-ip-ping-v1.0.5-mc1.20.4-forge",
                "Fast IP Ping",
                "fastipping",
                CLIENT,
                "(Performance Mod) Makes servers on server list load faster and makes loading to the server faster",
                false
        );

        modList.addMod(
                "entity_texture_features_forge_1.20.1-6.2.5",
                "Entity Texture Features",
                "entity_texture_features",
                CLIENT,
                "Adds support for resource-pack driven features for entity textures including some OptiFine features\n" +
                        "Supports OptiFine:\n" +
                        " - Random & Custom textures\n" +
                        " - Emissive textures\n" +
                        "With more features such as:\n" +
                        " - Blinking textures\n" +
                        " - Player Skin support\"",
                false
        );

        modList.addMod(
                "entity_model_features_forge_1.20.1-2.2.6",
                "Entity Model Features",
                "entity_model_features",
                CLIENT,
                "This is an expansion of the ETF mod, it adds support for OptiFine format Custom Entity Model (CEM) resource packs.\n" +
                        "While still allowing to you disable this to use a different model mod :)",
                false,
                Collections.singletonList("entity_texture_features")
        );

        modList.addMod(
                "notenoughanimations-forge-1.7.6-mc1.20.1",
                "NotEnoughAnimations",
                "notenoughanimations",
                CLIENT,
                "Adding and improving animations in Third-Person.",
                false
        );

        modList.addMod(
                "cloth-config-11.1.136-forge",
                "Cloth Config v10 API",
                "cloth_config",
                BOTH,
                "An API for config screens.",
                true
        );

        modList.addMod(
                "player-animation-lib-forge-1.0.2-rc1+1.20",
                "Player Animator",
                "playeranimator",
                BOTH,
                "Player animation api",
                true
        );

        modList.addMod(
                "bettercombat-forge-1.8.6+1.20.1",
                "Better Combat",
                "bettercombat",
                BOTH,
                "Easy, spectacular and fun melee combat system from Minecraft Dungeons",
                false,
                List.of(
                        "playeranimator",
                        "cloth_config"
                )
        );
    }



}
