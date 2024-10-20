package com.undefined;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.*;

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

    public void cleanModpack() {
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

    public void check() {
        this.modList.check();
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
}
