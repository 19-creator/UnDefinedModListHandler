package com.undefined;

import java.io.*;
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

    public ModList(String filename) throws IOException, ClassNotFoundException {
        loadMods(filename);
        this.name = filename;
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
        Mod mod = new Mod(fileName, name, modid, side, description, isApi, dependents, dependencies, this);
        this.mods.add(mod);
        mod.initializeDependenciesAndDependents(this);
    }

    public void removeMod(String modid) {
        removeMod(modid, false);
    }

    public void removeMod(String modid, boolean removeDependents) {
        removeMod(modid, removeDependents,0);
    }

    private void removeMod(String modid, boolean removeDependents, int iterations) {
        Optional<Mod> optional = getMod(modid);
        if(optional.isEmpty()) {
            System.out.println("Did not remove mod '" + modid + "' " + "because it does not exist");
            return;
        }
        Mod modToRemove = optional.get();
        if(!removeDependents && !modToRemove.dependents().isEmpty()) {
            System.out.println("Cannot remove mod '" + modToRemove.modid() + "'" + " because the following mods depend on it");

            for(String dependent : modToRemove.dependents()) {
                System.out.println("- " + dependent);
            }
            return;
        }

        mods.remove(modToRemove); // Remove the mod from the modlist before we start removing other things
        if(iterations == 0) System.out.println("Removed '" + modToRemove.modid() + "' Iterations: " + iterations);

        if(removeDependents && !modToRemove.dependents().isEmpty()) {
            for(String dependent : modToRemove.dependents()) {
                modToRemove.removeDependent(dependent, this); // This removes mod from our dependent list and the mod's dependencies list
                removeMod(dependent, true, iterations + 1); // Remove all the mod dependents from modlist too if flag is true
            }
        }

        // Remove our dependencies from the mod list if they are just an unused API
        if (!modToRemove.dependencies().isEmpty()) {
            for (String dependency : modToRemove.dependencies()) {
                Optional<Mod> modOptional = getMod(dependency);

                if (modOptional.isPresent()) {
                    Mod dependencyMod = modOptional.get();
                    boolean isApi = dependencyMod.isApi();
                    boolean hasNoOtherDependents = dependencyMod.dependents().size() == 1;
                    boolean isDependentOfModToRemove = dependencyMod.dependents().contains(modid);

                    if (isApi && hasNoOtherDependents && isDependentOfModToRemove) {
                        modToRemove.removeDependency(dependency, this);
                        removeMod(dependency, false, iterations + 1);
                    } else {
                        modToRemove.removeDependency(dependency, this);
                    }
                }
            }
        }

        if(iterations > 0) System.out.println("Removed '" + modToRemove.modid() + "' Iterations: " + iterations);
        if(isFileDeletionEnabled) {
            moveJarFile(this.srcDirectory, modToRemove.fileName(), this.deleteDirectory);
        }
    }

    public void addDependencyToMod(String dependencyId, String modId) {
        boolean dependencyPresent = containsMod(dependencyId);
        Optional<Mod> mod = getMod(modId);

        if (!dependencyPresent && mod.isEmpty()) {
            System.out.println("Dependency '" + dependencyId + "' and mod '" + modId + "' were not found");
            return;
        }

        if (!dependencyPresent) {
            System.out.println("Dependency '" + dependencyId + "' not found");
            return;
        }

        if (mod.isEmpty()) {
            System.out.println("Mod '" + modId + "' not found");
            return;
        }

        mod.get().addDependency(dependencyId, this);
    }

    public void removeDependencyFromMod(String dependencyId, String modId) {
        boolean dependencyPresent = containsMod(dependencyId);
        Optional<Mod> mod = getMod(modId);

        if (!dependencyPresent && mod.isEmpty()) {
            System.out.println("Dependency '" + dependencyId + "' and mod '" + modId + "' were not found");
            return;
        }

        if (!dependencyPresent) {
            System.out.println("Dependency '" + dependencyId + "' not found");
            return;
        }

        if (mod.isEmpty()) {
            System.out.println("Mod '" + modId + "' not found");
            return;
        }

        mod.get().removeDependency(dependencyId, this);
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

    public Optional<Mod> getMod(String modid) {
        for(Mod mod : mods) {
            if(mod.modid().equals(modid)) {
                return Optional.of(mod);
            }
        }
        return Optional.empty();
    }

    public void save() throws IOException {
        save(this.name);
    }

    public void save(String name) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(Paths.get("src", "main", "resources", name + ".mods").toFile()))) {
            oos.writeObject(mods);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadMods(String name) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Paths.get("src", "main", "resources", name + ".mods").toFile()))) {
            this.mods = (List<Mod>) ois.readObject();
        } catch (FileNotFoundException e) {
            this.mods =  new ArrayList<>();
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
            new File(this.srcDirectory + m).delete();
            System.out.println("Deleted '" + m + "'");
        });
    }

    public void moveExtraMods(String moveDirectory) {
        listExtraMods().forEach(m -> {
            moveJarFile(this.srcDirectory, m, moveDirectory);
        });
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

}
