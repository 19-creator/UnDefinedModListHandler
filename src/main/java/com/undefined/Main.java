package com.undefined;


import java.io.IOException;

import static com.undefined.LogicalSide.*;


public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        ModList modList = new ModList("undefined");
        modList.setDirectory("C:/Users/q0xn7/AppData/Roaming/PrismLauncher/instances/UnDefined 1.20.1/.minecraft/mods");
        modList.setDeleteDirectory("C:/Users/q0xn7/Desktop/deletedMods");
        modList.setFileDeletionEnabled(true);

        ModpackManager modpack = new ModpackManager(
                "C:/Users/q0xn7/AppData/Roaming/PrismLauncher/instances/UnDefined 1.20.1/.minecraft",
                modList
        );

        ProblemLocator problemLocator = new ProblemLocator(modpack);
        problemLocator.run();

        modList.addMod(
                "",
                "",
                "",
                BOTH,
                "",
                false
        );


        //modList.save();
    }




}
