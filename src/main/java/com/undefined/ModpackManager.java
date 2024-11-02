package com.undefined;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.*;

import static com.undefined.LogicalSide.BOTH;
import static com.undefined.LogicalSide.SERVER;

public class ModpackManager {

    private static final Logger LOGGER;

    private final Path modpackDirectory;
    private final ModList modList;


    static {
        LOGGER = Logger.getLogger(ModpackManager.class.getName());

        // Loop through all parent handlers (ConsoleHandler, etc.)
        for (Handler handler : LOGGER.getParent().getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                handler.setFormatter(new SimpleFormatter() {
                    @Override
                    public String format(LogRecord record) {
                        // Only print the log message content without timestamp or method name
                        return formatMessage(record) + "\n";
                    }
                });
            }
        }
    }

    public ModpackManager(String modpackDirectory, ModList list) {
        this.modpackDirectory = Paths.get(modpackDirectory);
        this.modList = list;
    }

    public void clean() {
        List<DeletionParameter> excludedFiles = new ArrayList<>(modList.allMods().stream()
                .map(m -> new DeletionParameter(filePath("mods/" + m.fileName() + ".jar"), false, false)).toList());

        excludedFiles.add(new DeletionParameter(filePath("config"), true, true));
        excludedFiles.add(new DeletionParameter(filePath("mods"), true, true));
        excludedFiles.add(new DeletionParameter(filePath("shaderpacks"), true, false));
        excludedFiles.add(new DeletionParameter(filePath("resourcepacks"), true, false));
        excludedFiles.add(new DeletionParameter(filePath("options.txt"), false, false));
        excludedFiles.add(new DeletionParameter(filePath("config/oculus.properties"), false, false));
        excludedFiles.add(new DeletionParameter(filePath("config/embeddium-options.json"), false, false));

        try {
            deleteFilesRecursive(modpackDirectory.toFile(), excludedFiles);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error cleaning modpack", e);
        }
    }

    public List<Mod> fetchMods(Predicate<Mod> filter) {
        return this.modList.allMods().stream().filter(filter).toList();
    }

    public List<Mod> fetchMods(String... tags) {
        return fetchMods(m -> m.hasTag(tags));
    }

    public List<Mod> fetchServerModpack() {
        return fetchMods(m -> m.getSide() == BOTH || m.getSide() == SERVER);
    }

    public double percentOfMods(Predicate<Mod> filter) {
        return Math.round(((double) fetchMods(filter).size() / this.modList.allMods().size()) * 100.0 * 100.0)/100.0;
    }

    public int numberOfModsWith(Predicate<Mod> filter) {
        return fetchMods(filter).size();
    }

    private void deleteFilesRecursive(File directory, List<DeletionParameter> deletionParameters) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            DeletionParameter parameter = getDeletionParameterForFile(file, deletionParameters);

            if (parameter != null) {
                if (parameter.isDirectory()) {
                    if (parameter.deleteContents()) {
                        LOGGER.log(Level.INFO, "Deleting contents of directory: {0}", file.getAbsolutePath());
                        deleteFilesRecursive(file, deletionParameters);
                        if (Objects.requireNonNull(file.list()).length == 0 && file.delete()) {
                            LOGGER.log(Level.INFO, "Deleted empty directory: {0}", file.getAbsolutePath());
                        }
                    }
                }
            } else {
                if (file.isDirectory()) {
                    deleteFilesRecursive(file, deletionParameters);
                    if (Objects.requireNonNull(file.list()).length == 0 && file.delete()) {
                        LOGGER.log(Level.INFO, "Deleted empty directory: {0}", file.getAbsolutePath());
                    }
                } else {
                    if (file.delete()) {
                        LOGGER.log(Level.INFO, "Deleted file: {0}", file.getAbsolutePath());
                    } else {
                        LOGGER.log(Level.WARNING, "Failed to delete file: {0}", file.getAbsolutePath());
                    }
                }
            }
        }
    }

    public void enableMod(String modid) {
        enableMod(this.modList.getMod(modid).get());
    }

    public void disableMod(String modid) {
        disableMod(this.modList.getMod(modid).get());
    }

    public void enableMod(Mod mod) {
        re_enableMod(getModFile(mod));
    }

    public void disableMod(Mod mod) {
        disableMod(getModFile(mod));
    }

    private File getModFile(Mod mod) {
        File file = new File(this.modList.directory() + "/" + mod.fileName() + ".jar");
        if(!file.exists()) {
            file = new File(this.modList.directory() + "/" + mod.fileName() + ".jar.disabled");
        }

        if(!file.exists()) {
            System.out.println("Could not find file for mod: " + mod.fileName());
        }

        return file;
    }

    private static void disableMod(File file) {
        if(file.getName().contains(".disabled")) return;

        if (file.renameTo(new File(file.getAbsolutePath() + ".disabled"))) {
            System.out.println("Disabled mod: " + file.getName());
        }
    }

    private static void re_enableMod(File file) {
        if(!file.getName().contains(".disabled")) return;
        file.renameTo(new File(file.getAbsolutePath().replace(".disabled", "")));
    }

    public void enableAllMods() {
        this.modList.allMods().forEach(this::enableMod);
    }

    public void check() {
        this.modList.check();
    }

    public ModList getModList() {
        return modList;
    }

    private DeletionParameter getDeletionParameterForFile(File file, List<DeletionParameter> deletionParameters) {
        for (DeletionParameter parameter : deletionParameters) {
            if (file.getAbsolutePath().equals(parameter.name())) {
                return parameter;
            }
        }
        return null;
    }

    private String filePath(String path) {
        return this.modpackDirectory.resolve(path).toString();
    }

    public record DeletionParameter(String name, boolean isDirectory, boolean deleteContents) {}

    public int size() {
        return this.modList.allMods().size();
    }
}
