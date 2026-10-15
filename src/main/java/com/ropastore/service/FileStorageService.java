package com.ropastore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private final Path rootLocation;
    private final Path productosLocation;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.productosLocation = this.rootLocation.resolve("productos").normalize();
        init();
    }

    private void init() {
        try {
            Files.createDirectories(this.productosLocation);
            log.info("Directorio de almacenamiento de imágenes inicializado en: {}", this.productosLocation);
        } catch (IOException e) {
            log.error("No se pudo inicializar el directorio de almacenamiento de imágenes", e);
            throw new RuntimeException("No se pudo inicializar el almacenamiento de archivos", e);
        }
    }

    /**
     * Guarda un archivo de imagen en el directorio de productos y retorna la URL relativa accesible vía web.
     *
     * @param file Archivo subido por el usuario
     * @return Ruta pública web (ej. /uploads/productos/uuid.jpg)
     */
    public String storeProductoImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Formato de imagen no permitido. Solo se aceptan JPEG, PNG, WebP o GIF.");
        }

        String originalFilename = StringUtils.cleanPath(
            file.getOriginalFilename() != null ? file.getOriginalFilename() : "imagen.jpg"
        );
        String extension = getFileExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Extensión de archivo no permitida: " + extension);
        }

        String uniqueFilename = UUID.randomUUID().toString() + extension.toLowerCase();

        try {
            Path destinationFile = this.productosLocation.resolve(uniqueFilename).normalize();

            // Protección contra Path Traversal
            if (!destinationFile.getParent().equals(this.productosLocation)) {
                throw new SecurityException("No se puede almacenar el archivo fuera del directorio designado.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Imagen de producto almacenada con éxito: {}", uniqueFilename);
            return "/uploads/productos/" + uniqueFilename;
        } catch (IOException e) {
            log.error("Error al guardar archivo de imagen de producto", e);
            throw new RuntimeException("Error al procesar y guardar la imagen: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina una imagen previamente subida si se encuentra dentro de /uploads/productos/.
     */
    public void deleteProductoImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/productos/")) {
            return;
        }

        try {
            String filename = imageUrl.substring("/uploads/productos/".length());
            Path targetFile = this.productosLocation.resolve(filename).normalize();
            if (Files.exists(targetFile) && targetFile.getParent().equals(this.productosLocation)) {
                Files.delete(targetFile);
                log.info("Imagen anterior eliminada: {}", filename);
            }
        } catch (IOException e) {
            log.warn("No se pudo eliminar la imagen previa: {}", imageUrl, e);
        }
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1 || lastIndex == filename.length() - 1) {
            return ".jpg";
        }
        return filename.substring(lastIndex);
    }

    public Path getRootLocation() {
        return rootLocation;
    }
}
