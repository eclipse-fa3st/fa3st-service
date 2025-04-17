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
package org.eclipse.digitaltwin.fa3st.service.filestorage.memory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.fa3st.common.exception.ConfigurationException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.InMemoryFile;
import org.eclipse.digitaltwin.fa3st.service.config.CoreConfig;
import org.eclipse.digitaltwin.fa3st.service.filestorage.AbstractFileStorageTest;
import org.eclipse.digitaltwin.fa3st.service.filestorage.filesystem.FileStorageFilesystem;
import org.eclipse.digitaltwin.fa3st.service.filestorage.filesystem.FileStorageFilesystemConfig;
import org.junit.Assert;
import org.junit.Test;


public class FileStorageFilesystemTest extends AbstractFileStorageTest<FileStorageFilesystem, FileStorageFilesystemConfig> {

    @Override
    public FileStorageFilesystemConfig getFileStorageConfig() {
        return FileStorageFilesystemConfig.builder()
                .build();
    }


    @Test
    public void testCustomPath() throws ConfigurationException, IOException, ResourceNotFoundException, PersistenceException {
        Path rootPath = Path.of("foo/bar");
        Path filePath = Path.of("my/path/file.txt");
        rootPath.toFile().deleteOnExit();
        rootPath.getParent().toFile().deleteOnExit();
        byte[] content = "foo".getBytes();
        FileStorageFilesystemConfig config = FileStorageFilesystemConfig.builder()
                .path(rootPath.toString())
                .build();
        FileStorageFilesystem fileStorage = config.newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        InMemoryFile expected = InMemoryFile.builder()
                .path(filePath.toString())
                .content(content)
                .build();
        fileStorage.save(expected);
        Assert.assertTrue(Files.exists(rootPath));
        List<Path> files = Files.list(rootPath).collect(Collectors.toList());
        Assert.assertEquals(1, files.size());
        byte[] actual = Files.readAllBytes(files.get(0));
        Assert.assertArrayEquals(expected.getContent(), actual);
        fileStorage.delete(expected.getPath());
    }


    @Test
    public void testExistingFiles() throws ConfigurationException, IOException, ResourceNotFoundException {
        Path tempDir = Files.createTempDirectory("faast-filesystem-storage-test");
        Path nestedDir = Files.createDirectory(tempDir.resolve("foo"));
        Path tempFile = Files.createTempFile(nestedDir, "dummy-data-file", "");
        tempFile.toFile().deleteOnExit();
        nestedDir.toFile().deleteOnExit();
        tempDir.toFile().deleteOnExit();
        byte[] content = "foo".getBytes();
        Files.write(tempFile, content);
        FileStorageFilesystemConfig config = FileStorageFilesystemConfig.builder()
                .existingDataPath(tempDir.toString())
                .build();
        FileStorageFilesystem fileStorage = config.newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        byte[] actual = fileStorage.get(tempDir.relativize(tempFile).toString());
        Assert.assertArrayEquals(content, actual);
    }
}
