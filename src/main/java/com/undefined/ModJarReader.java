package com.undefined;

import com.moandjiezana.toml.Toml;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ModJarReader {

    // Method to extract mods.toml and return it as a String
    public static String readModsTomlFromJar(String jarFilePath) throws IOException {
        ZipFile jarFile = new ZipFile(jarFilePath);
        ZipEntry modsTomlEntry = jarFile.getEntry("META-INF/mods.toml");

        if (modsTomlEntry == null) {
            System.out.println("Could not find META-INF/mods.toml for jar " + jarFilePath);
            return null;
        }

        // Reading the mods.toml file content
        try (InputStream inputStream = jarFile.getInputStream(modsTomlEntry);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            return stringBuilder.toString();
        }
    }

    // Method to extract and print mod information from mods.toml
    public static void extractModInfo(String tomlContent) {
        if(tomlContent == null) return;
        Toml toml = new Toml().read(tomlContent);

        // Handle modLoader as both a table and a string
        try {
            Toml modLoaderTable = toml.getTable("modLoader");
            if (modLoaderTable != null) {
                String loaderVersion = modLoaderTable.getString("loaderVersion");
                System.out.println("Mod Loader Version: " + loaderVersion);
            }
        } catch (ClassCastException e) {
            // Handle modLoader as a string if it's not a table
            String modLoaderString = toml.getString("modLoader");
            if (modLoaderString != null) {
                System.out.println("Mod Loader: " + modLoaderString);
            } else {
                System.out.println("modLoader section is missing.");
            }
        }

        // Loop through mods defined in the file
        List<Toml> mods = toml.getTables("mods");
        if (mods != null) {
            for (Toml mod : mods) {
                String modId = mod.getString("modId");
                String modName = mod.getString("displayName");
                String description = mod.getString("description");

                System.out.println("\nMod ID: " + modId);
                System.out.println("Mod Name: " + modName);
                System.out.println("Description: " + description);

                // Extract dependencies for this mod
                String dependenciesKey = "dependencies." + modId;
                List<Toml> dependencies = toml.getTables(dependenciesKey);

                if (dependencies != null && !dependencies.isEmpty()) {
                    System.out.println("Dependencies:");
                    for (Toml dependency : dependencies) {
                        String depModId = dependency.getString("modId");
                        String versionRange = dependency.getString("versionRange");
                        Boolean mandatory = dependency.getBoolean("mandatory");
                        String ordering = dependency.getString("ordering");
                        String side = dependency.getString("side");

                        System.out.println("  Dependency Mod ID: " + depModId);
                        System.out.println("  Version Range: " + versionRange);
                        System.out.println("  Mandatory: " + mandatory);
                        System.out.println("  Ordering: " + ordering);
                        System.out.println("  Side: " + side);
                    }
                } else {
                    System.out.println("No dependencies.");
                }
            }
        }
    }


    public static String extractModName(String path) {
        try {
            String tomlContent = readModsTomlFromJar(path);
            if(tomlContent == null) return null;
            Toml toml = new Toml().read(tomlContent);
            List<Toml> mods = toml.getTables("mods");
            if (mods != null && !mods.isEmpty()) {
                return mods.get(0).getString("displayName");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Return null if not found or error occurs
    }

    // Method to extract mod ID
    public static String extractModId(String path) {
        try {
            String tomlContent = readModsTomlFromJar(path);
            if(tomlContent == null) return null;
            Toml toml = new Toml().read(tomlContent);
            List<Toml> mods = toml.getTables("mods");
            if (mods != null && !mods.isEmpty()) {
                return mods.get(0).getString("modId");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Return null if not found or error occurs
    }

    // Method to extract mandatory dependencies excluding "minecraft" and "forge"
    public static List<String> extractMandatoryDependencies(String path) {
        List<String> mandatoryDeps = new ArrayList<>();
        try {
            String tomlContent = readModsTomlFromJar(path);
            if(tomlContent == null) return null;
            Toml toml = new Toml().read(tomlContent);

            // Get all mods
            List<Toml> mods = toml.getTables("mods");
            if (mods != null && !mods.isEmpty()) {
                for (Toml mod : mods) {
                    String modId = mod.getString("modId");

                    // Try both quoted and unquoted keys
                    List<Toml> dependencies = toml.getTables("dependencies." + modId);
                    if (dependencies == null) {
                        dependencies = toml.getTables("dependencies.\"" + modId + "\"");
                    }

                    if (dependencies != null) {
                        for (Toml dependency : dependencies) {
                            String depModId = dependency.getString("modId");
                            Boolean mandatory = dependency.getBoolean("mandatory");

                            // Add only mandatory dependencies, excluding "minecraft" and "forge"
                            if (mandatory != null && mandatory && !depModId.equals("minecraft") && !depModId.equals("forge")) {
                                mandatoryDeps.add(depModId);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mandatoryDeps; // Return list of mandatory dependencies
    }




    // Method to extract mod description
    public static String extractDescription(String path) {
        try {
            String tomlContent = readModsTomlFromJar(path);
            if(tomlContent == null) return null;
            Toml toml = new Toml().read(tomlContent);
            List<Toml> mods = toml.getTables("mods");
            if (mods != null && !mods.isEmpty()) {
                return mods.get(0).getString("description");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Return null if not found or error occurs
    }


}


