package de.johannes.cloud;

import de.johannes.cloud.server.Server;
import de.johannes.cloud.server.ServerConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;

public class FileManager {

    public static boolean hasTemplate(String template) {
        File tf = new File("templates", template);
        return tf.exists() && tf.isDirectory();
    }

    public static String[] listTemplates() {
        File templates = new File("templates");
        if (!templates.isDirectory()) {
            return new String[]{};
        } else {
            return templates.list();
        }
    }

    public static File runtimeDirectory(ServerConfig server) {
        File directory = new File("runtime/" + server.id());
        if (!directory.exists() || !directory.isDirectory()) {
            directory.mkdirs();
        }
        return directory;
    }

    public static boolean copyRuntime(Server server) {
        File template = new File("templates/" + server.config().template());
        File runtime = server.config().directory();
        try {
            copyDirectory(template.toPath(), runtime.toPath());
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    public static boolean deleteRuntime(Server server) {
        Path dir = server.config().directory().toPath();
        if (!Files.exists(dir)) return false;

        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                        throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;

        } catch (IOException e) {
            return false;
        }
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetFile = target.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
