package com.undefined;


import java.io.IOException;


public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        // Make a mod removal system where you explain why you removed a mod so you
        // can look up in the future reason why mods were removed

        ModList modList = new ModList("test");
        modList.setDirectory("C:/Users/q0xn7/AppData/Roaming/PrismLauncher/instances/UnDefined 1.20.1/.minecraft/mods");
        modList.setDeleteDirectory("C:/Users/q0xn7/Desktop/deletedMods");
        modList.setFileDeletionEnabled(true);

        ModpackManager modpack = new ModpackManager(
                "C:/Users/q0xn7/AppData/Roaming/PrismLauncher/instances/UnDefined 1.20.1/.minecraft",
                modList
        );

        //ProblemLocator problemLocator = new ProblemLocator(modpack, "diagonalwalls");
        //problemLocator.run();

        //modpack.check();
        //modList.updateMods();
        //var s = modList.initializeExtraMods();

        // Make sure when removing mod to use this so dependencies
        // are successfully unregistered
        //modList.removeMod("");

        //modpack.clean();

        int t = 0;
        //modList.save();
    }




}
