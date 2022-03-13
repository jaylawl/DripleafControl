package de.jaylawl.dripleafcontrol.util;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class FileUtil {

    public enum FileCreationReport {
        FILE_EXISTS(),
        MKDIR_ERROR(),
        CREATE_FILE_ERROR(),
        CREATED_DIR_AND_FILE(),
        CREATED_FILE();
    }

    public record SubDirectory(String id, String path) {

        public SubDirectory(@NotNull String id, @NotNull String path) {
            this.id = id;
            this.path = path;
        }

    }

    public static final String[] YAML_FILE_EXTENSIONS = new String[]{".yml", ".yaml"};

    private final String baseDirectoryPath;
    private final HashMap<String, SubDirectory> subDirectories = new HashMap<>();

    public FileUtil(final @NotNull JavaPlugin pluginInstance) {
        this.baseDirectoryPath = pluginInstance.getDataFolder().getPath();
    }

    //

    public @NotNull String getBaseDirectoryPath() {
        return this.baseDirectoryPath;
    }

    public void registerSubDirectories(@NotNull SubDirectory... subDirectories) {
        for (SubDirectory subDirectory : subDirectories) {
            this.subDirectories.put(subDirectory.id, subDirectory);
        }
    }

    public SubDirectory getSubDirectory(@NotNull String subDirectoryId) {
        return this.subDirectories.get(subDirectoryId);
    }

    public File getSubDirectoryFile(@NotNull String subDirectoryId) {
        SubDirectory subDirectory = getSubDirectory(subDirectoryId);
        if (subDirectory != null) {
            return new File(String.join("/", this.baseDirectoryPath, subDirectory.path));
        }
        return null;
    }

    public boolean createDirectories() {
        final Collection<String> paths = new ArrayList<>();
        paths.add(this.baseDirectoryPath);
        this.subDirectories.values().forEach(subDirectory -> paths.add(this.baseDirectoryPath + "/" + subDirectory.path));

        int directionCreationFailures = 0;
        for (String directoryPath : paths) {
            File directory = new File(directoryPath);
            if (!directory.exists() || !directory.isDirectory()) {
                if (!directory.mkdirs()) {
                    directionCreationFailures++;
                }
            }
        }

        return directionCreationFailures == 0;
    }

    public @NotNull FileCreationReport ensureFileExists(final @NotNull File file) {
        if (!file.exists()) {
            final File pluginDirectory = file.getParentFile();
            boolean createdParentDirectory = false;
            if (!pluginDirectory.exists() || !pluginDirectory.isDirectory()) {
                if (!pluginDirectory.mkdirs()) {
                    return FileCreationReport.MKDIR_ERROR;
                } else {
                    createdParentDirectory = true;
                }
            }
            try {
                if (!file.createNewFile()) {
                    return FileCreationReport.CREATE_FILE_ERROR;
                } else {
                    return createdParentDirectory ? FileCreationReport.CREATED_DIR_AND_FILE : FileCreationReport.CREATED_FILE;
                }
            } catch (IOException ignored) {
                return FileCreationReport.CREATE_FILE_ERROR;
            }
        }
        return FileCreationReport.FILE_EXISTS;
    }

    //

    public static @NotNull File[] getFilesInDirectory(File directory) {
        return getFilesInDirectory(directory, false);
    }

    public static @NotNull File[] getFilesInDirectory(@NotNull File directory, boolean includeSubDirectories) {
        Collection<File> files = new ArrayList<>();
        File[] filesInDirectory = directory.listFiles();
        if (filesInDirectory != null) {
            for (File file : filesInDirectory) {
                if (file.isDirectory() && includeSubDirectories) {
                    files.addAll(Arrays.asList(getFilesInDirectory(file, true)));
                } else if (file.isFile()) {
                    files.add(file);
                }
            }
        }
        return files.toArray(new File[0]);
    }

    public static @NotNull File[] getFilesWithExtensionsInDirectory(@NotNull File directory, @NotNull String[] extensions) {
        return getFilesWithExtensionsInDirectory(directory, extensions, false);
    }

    public static @NotNull File[] getFilesWithExtensionsInDirectory(@NotNull File directory, @NotNull String[] extensions, boolean includeSubDirectories) {
        Collection<File> files = new ArrayList<>();
        File[] filesInDirectory = directory.listFiles();
        if (filesInDirectory != null) {
            for (File file : filesInDirectory) {
                if (file.isDirectory() && includeSubDirectories) {
                    files.addAll(Arrays.asList(getFilesWithExtensionsInDirectory(file, extensions, true)));
                } else if (file.isFile()) {
                    if (extensions == null || extensions.length == 0) {
                        files.add(file);
                    } else {
                        String fileName = file.getName().toLowerCase();
                        for (String extension : extensions) {
                            if (fileName.endsWith(extension.toLowerCase())) {
                                files.add(file);
                            }
                        }
                    }
                }
            }
        }
        return files.toArray(new File[0]);
    }

}
