package versioning;

import java.io.*;
import java.nio.file.*;
import java.util.function.Consumer;
import java.util.zip.*;

import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.GlobalData;

public class WorldSaveEditor {
    private static final String WORLD_SAVE_PATH = GlobalData.appDataPath()+"saves"+System.getProperty("file.separator")+"worlds"+System.getProperty("file.separator");

    public static void processPlayerSaves(Consumer<File> fileProcessor) {
        File worldSaveDir = new File(WORLD_SAVE_PATH);
        if (!worldSaveDir.exists() || !worldSaveDir.isDirectory()) {
            DebugHelper.handleFormattedDebugMessage("World save directory not found at %s!", 25, MESSAGE_TYPE.ERROR, new Object[] {worldSaveDir.getAbsolutePath()});
 
            return;
        }

        // Loop through all world save ZIP files
        for (File zipFile : worldSaveDir.listFiles((dir, name) -> name.endsWith(".zip"))) {
            try {
                processZipFile(zipFile, fileProcessor);
            } catch (IOException e) {
                System.err.println("Failed to process zip file: " + zipFile.getName());
                e.printStackTrace();
            }
        }
    }

    private static void processZipFile(File zipFile, Consumer<File> fileProcessor) throws IOException {
        Path tempDir = Files.createTempDirectory("zip_extract_");
        File tempZipDir = tempDir.toFile();

        // Extract ZIP contents to temp directory
        try (ZipFile zip = new ZipFile(zipFile)) {
            for (ZipEntry entry : zip.stream().toList()) {
                File extractedFile = new File(tempZipDir, entry.getName());

                // Normalize path to avoid leading slashes or invalid names
                extractedFile = extractedFile.getCanonicalFile();

                // Ensure parent directories exist
                if (!extractedFile.getParentFile().exists() && !extractedFile.getParentFile().mkdirs()) {
                    throw new IOException("Failed to create parent directory: " + extractedFile.getParentFile());
                }

                // Skip directories
                if (entry.isDirectory()) {
                    continue;
                }

                // Extract all files
                extractFile(zip, entry, extractedFile);

                // Process only player save files
                if (entry.getName().contains("/players/") && entry.getName().endsWith(".dat")) {
                    fileProcessor.accept(extractedFile);
                }
            }
        }

        // Repackage the ZIP with all extracted (and possibly modified) contents
        repackageZip(zipFile, tempZipDir);

        // Cleanup temp directory
        deleteDirectory(tempZipDir);
    }

    private static void extractFile(ZipFile zip, ZipEntry entry, File outputFile) throws IOException {
        // Ensure the file's parent directories exist
        if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
            throw new IOException("Failed to create parent directory: " + outputFile.getParentFile());
        }

        try (InputStream is = zip.getInputStream(entry);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }


    private static void repackageZip(File zipFile, File sourceDir) throws IOException {
        File tempZip = new File(zipFile.getParent(), zipFile.getName() + ".tmp");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZip))) {
            Path basePath = sourceDir.toPath();
            Files.walk(sourceDir.toPath()).forEach(path -> {
                File file = path.toFile();
                if (!file.isDirectory()) {
                    String zipEntryName = basePath.relativize(path).toString().replace("\\", "/");
                    try (FileInputStream fis = new FileInputStream(file)) {
                        zos.putNextEntry(new ZipEntry(zipEntryName));
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            zos.write(buffer, 0, bytesRead);
                        }
                        zos.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        // Replace the old ZIP with the new one
        if (!zipFile.delete() || !tempZip.renameTo(zipFile)) {
            System.err.println("Failed to replace original zip: " + zipFile.getName());
        }
    }

    private static void deleteDirectory(File dir) {
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
            dir.delete();
        }
    }

}
