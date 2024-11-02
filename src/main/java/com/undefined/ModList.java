package com.undefined;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ModList {

    private String name;
    private List<Mod> mods;
    private String srcDirectory;
    private String deleteDirectory;
    private boolean isFileDeletionEnabled;

    public ModList() {
        this.mods = new ArrayList<>();
    }

    public ModList(String filename) {
        this.name = filename;
        loadMods();
    }

    public void setDirectory(String dir) {
        this.srcDirectory = dir;
    }

    public void setDeleteDirectory(String dir) {
        this.deleteDirectory = dir;
    }

    public void setFileDeletionEnabled(boolean isFileDeletionEnabled) {
        this.isFileDeletionEnabled = isFileDeletionEnabled;
    }

    public void addMod(String fileName, String name, String modId, LogicalSide side, String description, boolean isApi) {
        addMod(fileName, name, modId, side, description, isApi, null, null);
    }

    public void addMod(String fileName, String name, String modId, LogicalSide side, String description, boolean isApi, List<String> dependencies) {
        addMod(fileName, name, modId, side, description, isApi, null, dependencies);
    }

    public void addMod(String fileName, String name, String modid, LogicalSide side, String description, boolean isApi, List<String> dependents, List<String> dependencies) {
        if(fileName.isEmpty() || name.isEmpty() || modid.isEmpty()) return;
        if(this.containsMod(modid)) return;
        Mod mod = new Mod(fileName, name, modid, side, description, isApi, dependents, dependencies);
        this.mods.add(mod);
        mod.initializeDependenciesAndDependents(this);
    }

    public void removeModByFileName(String fileName) {
        if(fileName.isEmpty()) return;
        removeMod(this.getModByFile(fileName).modid());
    }

    public void removeModByFileName(String fileName, boolean removeDependents) {
        if(fileName.isEmpty()) return;
        removeMod(this.getModByFile(fileName).modid(), removeDependents);
    }

    public void removeMod(String modid) {
        removeMod(modid, false);
    }

    public void removeMod(String modid, boolean removeDependents) {
        removeMod(modid, removeDependents,0);
    }

    private void removeMod(String modid, boolean removeDependents, int iterations) {
        Optional<Mod> optionalMod = getMod(modid);
        if (optionalMod.isEmpty()) {
            System.out.println("Did not remove mod '" + modid + "' because it does not exist");
            return;
        }

        Mod modToRemove = optionalMod.get();

        // Check if removal is blocked due to active dependents and the flag
        if (!removeDependents && !modToRemove.dependents().isEmpty()) {
            System.out.println("Cannot remove mod '" + modToRemove.modid() + "' because the following mods depend on it:");
            modToRemove.dependents().forEach(dependent -> System.out.println("- " + dependent));
            return;
        }

        // Remove mod before processing dependents or dependencies
        mods.remove(modToRemove);
        System.out.println("Removed '" + modToRemove.modid() + "' Iterations: " + iterations);

        // Recursively remove dependents if allowed
        if (removeDependents) {
            modToRemove.dependents().forEach(dependent -> {
                modToRemove.removeDependent(dependent, this);
                removeMod(dependent, true, iterations + 1);
            });
        }

        // Remove unused API dependencies
        modToRemove.dependencies().forEach(dependency -> {
            getMod(dependency).ifPresent(dependencyMod -> {
                if (shouldRemoveApiDependency(dependencyMod, modid)) {
                    modToRemove.removeDependency(dependency, this);
                    removeMod(dependency, false, iterations + 1);
                }
            });
        });

        // Optionally delete mod file
        if (isFileDeletionEnabled) {
            moveJarFile(this.srcDirectory, modToRemove.fileName(), this.deleteDirectory);
        }
    }

    private boolean shouldRemoveApiDependency(Mod dependencyMod, String modid) {
        return dependencyMod.isApi()
                && dependencyMod.dependents().size() == 1
                && dependencyMod.dependents().contains(modid);
    }


    public void addDependencyToMod(String dependencyId, String modId) {
        validateDependencyAndMod(dependencyId, modId).ifPresent(value -> value.addDependency(dependencyId, this));
    }

    public void removeDependencyFromMod(String dependencyId, String modId) {
        validateDependencyAndMod(dependencyId, modId).ifPresent(value -> value.removeDependency(dependencyId, this));
    }

    private Optional<Mod> validateDependencyAndMod(String dependencyId, String modId) {
        boolean dependencyPresent = containsMod(dependencyId);
        Optional<Mod> mod = getMod(modId);

        if (!dependencyPresent && mod.isEmpty()) {
            System.out.println("Dependency '" + dependencyId + "' and mod '" + modId + "' were not found");
            return Optional.empty();
        }

        if (!dependencyPresent) {
            System.out.println("Dependency '" + dependencyId + "' not found");
            return Optional.empty();
        }

        if (mod.isEmpty()) {
            System.out.println("Mod '" + modId + "' not found");
            return Optional.empty();
        }

        return mod;
    }


    public boolean containsMod(String modid) {
        for(Mod mod : mods) {
            if(mod.modid().equals(modid)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsModByFileName(String filename) {
        for(Mod mod : mods) {
            if((mod.fileName() + ".jar").equals(filename)) {
                return true;
            }
        }
        return false;
    }

    public Mod getModByFile(String filename) {
        for(Mod mod : mods) {
            if(mod.fileName().equals(filename)) {
                return mod;
            }
        }
        throw new RuntimeException("Mod not found " + filename);
    }

    public Optional<Mod> getMod(String modid) {
        for(Mod mod : mods) {
            if(mod.modid().equals(modid)) {
                return Optional.of(mod);
            }
        }
        return Optional.empty();
    }

    public void save() {
        save(this.name);
    }


    public void save(String name) {
        Gson gson = new Gson();
        try (Writer writer = new FileWriter(Paths.get("src", "main", "resources", name + ".mods").toFile())) {
            gson.toJson(mods, writer); // Serialize the mods list to JSON
        } catch (IOException e) {
            System.err.println("Error saving mods: " + e.getMessage());
        }
    }

    // Method to load mods from a file in JSON format
    public void loadMods() {
        Gson gson = new Gson();
        try (Reader reader = new FileReader(Paths.get("src", "main", "resources", name + ".mods").toFile())) {
            Type modListType = new TypeToken<List<Mod>>(){}.getType();
            mods = gson.fromJson(reader, modListType); // Deserialize JSON to mods list
        } catch (IOException e) {
            System.err.println("Error loading mods: " + e.getMessage());
        }
    }

    /**
     * @return List of files that exist in the mod directory but not on the modlist
     */
    public List<String> listExtraMods() {
        List<String> extraMods = new ArrayList<>();
        try {
            // Get a list of all files in the source directory
            File folder = new File(this.srcDirectory);
            File[] listOfFiles = folder.listFiles();

            if (listOfFiles != null) {
                for (File file : listOfFiles) {

                    boolean isInModList = false;
                    for(Mod mod : this.mods) {
                        String modFile = mod.fileName() + ".jar";
                        String fileName = file.getName();
                        if((modFile).equals(fileName)) {
                            isInModList = true;
                            break;
                        }
                    }
                    if(!isInModList) {
                        extraMods.add(file.getName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return extraMods;
    }

    public void deleteExtraMods() {
        listExtraMods().forEach(m -> {
            File mod = new File(this.srcDirectory + "/" + m);
            if (mod.isDirectory()) {
                deleteDirectory(mod);
            } else {
                mod.delete();
            }
            System.out.println("Deleted '" + m + "'");
        });
    }

    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) { // listFiles can return null
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        directory.delete();
    }

    public void moveExtraMods() {
        moveExtraMods(this.deleteDirectory);
    }

    public void moveExtraMods(String moveDirectory) {
        listExtraMods().forEach(m -> moveJarFile(this.srcDirectory, m, moveDirectory));
    }

    public void check() {
        List<String> missingMods = missingMods();
        List<String> extraMods = listExtraMods();
        boolean missingModsEmpty = missingMods().isEmpty();
        boolean extraModsEmpty = extraMods.isEmpty();
        if(missingModsEmpty && extraModsEmpty) {
            System.out.println("All mods on list are present and no extra mods");
        }else {
            if(!missingModsEmpty) {
                System.out.println("Missing " + missingMods.size() + " mods: ");
                Collections.sort(missingMods);
                missingMods.forEach(m -> System.out.println("- " + m));
            }
            if(!extraModsEmpty) {
                if(!missingModsEmpty) {
                    System.out.println("------------------------");
                }
                System.out.println("Extra " + extraMods.size() + " mods: ");
                Collections.sort(extraMods);
                extraMods.forEach(e -> System.out.println("- " + e));
            }
        }
    }

    private List<String> missingMods() {
        ArrayList<String> missingMods = new ArrayList<>();
        try {
            // Get a list of all files in the source directory
            File folder = new File(this.srcDirectory);
            if(folder.listFiles() == null) return missingMods;
            List<File> listOfFiles = List.of(Objects.requireNonNull(folder.listFiles()));
            List<String> fileNames = listOfFiles.stream().map(File::getName).toList();

            for (Mod mod : this.mods) {
                if(!fileNames.contains(mod.fileName() + ".jar")) {
                    missingMods.add(mod.fileName());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return missingMods;
    }

    public List<Mod> initializeExtraMods() {
        List<Mod> extraMods = new ArrayList<>();

        List<String> extraModFiles = listExtraMods();
        if(extraModFiles.isEmpty()) return extraMods;

        for(String modFile : extraModFiles) {
            if(modFile.endsWith(".jar")) {
                String modFilePath = this.srcDirectory + "/" + modFile;

                String fileName = modFile.replace(".jar", "");
                String modName = ModJarReader.extractModName(modFilePath);
                String modid = ModJarReader.extractModId(modFilePath);
                String description = ModJarReader.extractDescription(modFilePath);
                List<String> dependencies = ModJarReader.extractMandatoryDependencies(modFilePath);
                Mod mod = new Mod(fileName, modName, modid, LogicalSide.BOTH, description, false, null, dependencies);
                extraMods.add(mod);
                if(!this.containsMod(modid)) this.mods.add(mod);
            }
        }

        for(Mod mod : extraMods) {
            mod.initializeDependenciesAndDependents(this);
        }

        return extraMods;
    }

    public void printModInfo(String filename) {
        extractModInfoByFileName(filename);
    }

    public void extractModInfoByFile(String filename) {
        Mod mod = getModByFile(filename);
        try {
            ModJarReader.extractModInfo(ModJarReader.readModsTomlFromJar(this.srcDirectory + "/" + mod.fileName() + ".jar"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void extractModInfoByID(String modid) {
        getMod(modid).ifPresent(m -> {
            try {
                ModJarReader.extractModInfo(ModJarReader.readModsTomlFromJar(this.srcDirectory + "/" + getMod(modid).get().fileName() + ".jar"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void extractModInfoByFileName(String fileName) {
        try {
            System.out.println("---------------------------------------------------");
            ModJarReader.extractModInfo(ModJarReader.readModsTomlFromJar(this.srcDirectory + "/" + fileName + ".jar"));
            System.out.println("---------------------------------------------------");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int modCount() {
        return this.mods.size() + 1;
    }

    public List<Mod> allMods() {
        return this.mods;
    }

    public static void delete(String filename) {
        try {
            if(Paths.get("src", "main", "resources", filename + ".mods").toFile().delete()) System.out.println("Deleted " + filename);
        } catch (Exception ignored) {}
    }

    public static ModList combineModList(ModList first, ModList second) {
        List<Mod> list1 = first.allMods();
        List<Mod> list2 = second.allMods();
        for(Mod mod : list2) {
            if(!first.containsMod(mod.modid())) {
                list1.add(mod);
            }
        }
        return first;
    }

    public static void moveJarFile(String sourceDirectoryPath, String jarFileName, String destinationDirectoryPath) {
        // Ensure the .jar extension is present
        if (!jarFileName.endsWith(".jar")) {
            jarFileName += ".jar";
        }

        Path sourceJarPath = Paths.get(sourceDirectoryPath, jarFileName);
        Path destinationJarPath = Paths.get(destinationDirectoryPath, jarFileName);

        try {
            if (Files.exists(sourceJarPath) && sourceJarPath.toString().endsWith(".jar")) {
                // Ensure the destination directory exists, or create it
                Files.createDirectories(Paths.get(destinationDirectoryPath));

                // Move the file
                Files.move(sourceJarPath, destinationJarPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Successfully deleted: " + jarFileName);
            } else {
                System.out.println("Failed to delete: '" + jarFileName + "' because it does not exist");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to delete: '" + jarFileName + "'" + e.getMessage());
        }
    }

    public String directory() {
        return srcDirectory;
    }

}
