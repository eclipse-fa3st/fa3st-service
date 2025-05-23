/**
 * Copyright (c) 2025 the Eclipse FA³ST Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.digitaltwin.fa3st.service.filestorage.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.fa3st.common.dataformat.DeserializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.ConfigurationInitializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidConfigurationException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.util.Ensure;
import org.eclipse.digitaltwin.fa3st.common.util.LambdaExceptionHelper;
import org.eclipse.digitaltwin.fa3st.common.util.StringHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.config.CoreConfig;
import org.eclipse.digitaltwin.fa3st.service.filestorage.FileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of {@link org.eclipse.digitaltwin.fa3st.service.filestorage.FileStorage} for file system storage.
 */
public class FileStorageFilesystem implements FileStorage<FileStorageFilesystemConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorageFilesystem.class);
    private static final String MSG_FILE_NOT_FOUND = "could not find file for path '%s'";
    private static final String PREFIX_LOCAL_FILE = "file:///";
    private Map<String, Path> existingFiles;
    private FileStorageFilesystemConfig config;

    public FileStorageFilesystem() {
        this.existingFiles = new HashMap<>();
    }


    @Override
    public void init(CoreConfig coreConfig, FileStorageFilesystemConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        Ensure.requireNonNull(config, "config must be non-null");
        this.config = config;
        ensurePath();
        loadFromExistingDataPath();
        loadFromEnvironment();
    }


    private void ensurePath() throws ConfigurationInitializationException {
        if (Objects.nonNull(config.getPath())
                && !StringHelper.isEmpty(config.getPath())) {
            try {
                Path path = Path.of(config.getPath());
                if (Files.exists(path) && !Files.isDirectory(path)) {
                    throw new ConfigurationInitializationException(
                            String.format("provided path is not a directory (path: %s)",
                                    config.getPath()));
                }
                if (Files.notExists(path)) {
                    LOGGER.info("provided path does not exist - it will be created (path: {})", config.getPath());
                    Files.createDirectories(path);
                }
                if (!Files.isDirectory(path)) {
                    Files.delete(path);
                    throw new ConfigurationInitializationException(
                            String.format("provided path is not a directory (path: %s)", config.getPath()));
                }

            }
            catch (InvalidPathException e) {
                throw new ConfigurationInitializationException(
                        String.format("error initializing FileStorageFilesystem - provided path is invalid (path: %s)", config.getPath()),
                        e);
            }
            catch (IOException e) {
                throw new ConfigurationInitializationException(
                        String.format("path could not be created (path: %s)", config.getPath()),
                        e);
            }
        }
    }


    private void loadFromExistingDataPath() {
        if (Objects.isNull(config.getExistingDataPath())) {
            return;
        }
        try {
            final Path existingDataPath = Path.of(config.getExistingDataPath());
            if (Files.exists(existingDataPath) && !Files.isDirectory(existingDataPath)) {
                LOGGER.warn("error initializing FileStorageFilesystem - existing data path is not a directory (path: {})", config.getExistingDataPath());
                return;
            }
            try (Stream<Path> files = Files.walk(existingDataPath)) {
                existingFiles = files.filter(file -> !Files.isDirectory(file))
                        .collect(Collectors.toMap(
                                x -> encodeFilePath(existingDataPath.relativize(x).toString()),
                                x -> x));
            }

        }
        catch (InvalidPathException e) {
            LOGGER.warn("error initializing FileStorageFilesystem - invalid existing data path (path: {})", config.getExistingDataPath(), e);
        }
        catch (IOException e) {
            LOGGER.warn("error initializing FileStorageFilesystem - unable to load existing data (path: {})", config.getExistingDataPath(), e);
        }
    }


    private void loadFromEnvironment() throws ConfigurationInitializationException {
        try {
            config.loadInitialModelAndFiles().getFiles().stream()
                    .forEach(LambdaExceptionHelper.rethrowConsumer(
                            x -> save(x.getPath(), x.getFileContent())));
        }
        catch (DeserializationException | InvalidConfigurationException | PersistenceException e) {
            throw new ConfigurationInitializationException("error initializing file-system file storage", e);
        }
    }


    @Override
    public byte[] get(String path) throws ResourceNotFoundException {
        String encodedFilePath = encodeFilePath(path);
        try {
            if (existingFiles.containsKey(encodedFilePath)) {
                return Files.readAllBytes(existingFiles.get(encodedFilePath));
            }
            return Files.readAllBytes(Path.of(config.getPath(), encodedFilePath));
        }
        catch (IOException e) {
            throw new ResourceNotFoundException(String.format(MSG_FILE_NOT_FOUND, path));
        }
    }


    private String encodeFilePath(String filePath) {
        return Base64.getUrlEncoder().encodeToString(localize(filePath).getBytes());
    }


    private String localize(String filePath) {
        if (Objects.isNull(filePath)) {
            return "";
        }
        if (filePath.startsWith(PREFIX_LOCAL_FILE)) {
            return filePath.substring(PREFIX_LOCAL_FILE.length());
        }
        return filePath;
    }


    @Override
    public void save(String path, byte[] content) throws PersistenceException {
        try {
            Files.write(
                    Path.of(config.getPath(), encodeFilePath(path)),
                    content);
        }
        catch (IOException e) {
            throw new PersistenceException(e);
        }
    }


    @Override
    public void delete(String path) throws ResourceNotFoundException, PersistenceException {
        String encodedFilePath = encodeFilePath(path);
        if (existingFiles.containsKey(encodedFilePath)) {
            existingFiles.remove(encodedFilePath);
        }
        else {
            try {
                if (!Files.deleteIfExists(Path.of(config.getPath(), encodedFilePath))) {
                    throw new ResourceNotFoundException(String.format("could not delete file for path '%s'", path));
                }
            }
            catch (IOException e) {
                throw new PersistenceException(e);
            }
        }
    }


    @Override
    public void deleteAll() throws PersistenceException {
        List<String> filesFailedToDelete = Arrays.stream(
                new File(config.getPath())
                        .listFiles(x -> x.isFile() && !existingFiles.values().contains(x.toPath())))
                .filter(x -> !x.delete())
                .map(File::getName)
                .toList();

        if (!filesFailedToDelete.isEmpty()) {
            throw new PersistenceException(String.format(
                    "Delete all files failed. Following files have not been deleted: %s%s",
                    System.lineSeparator(),
                    filesFailedToDelete.stream().collect(Collectors.joining(System.lineSeparator()))));
        }
    }


    @Override
    public boolean contains(String path) {
        return Files.exists(Path.of(config.getPath(), encodeFilePath(path)));
    }


    @Override
    public FileStorageFilesystemConfig asConfig() {
        return config;
    }

}
