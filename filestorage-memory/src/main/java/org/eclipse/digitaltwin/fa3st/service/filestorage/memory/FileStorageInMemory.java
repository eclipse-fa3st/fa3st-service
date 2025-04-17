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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.digitaltwin.fa3st.common.dataformat.DeserializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.ConfigurationInitializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidConfigurationException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.EnvironmentContext;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.config.CoreConfig;
import org.eclipse.digitaltwin.fa3st.service.filestorage.FileStorage;


/**
 * Implementation of {@link org.eclipse.digitaltwin.fa3st.service.filestorage.FileStorage} for in memory storage.
 */
public class FileStorageInMemory implements FileStorage<FileStorageInMemoryConfig> {

    private FileStorageInMemoryConfig config;
    private final Map<String, byte[]> files;

    public FileStorageInMemory() {
        files = new ConcurrentHashMap<>();
    }


    @Override
    public void init(CoreConfig coreConfig, FileStorageInMemoryConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
        EnvironmentContext environmentContext = null;
        try {
            environmentContext = config.loadInitialModelAndFiles();
            environmentContext.getFiles().stream().forEach(v -> save(v.getPath(), v.getFileContent()));
        }
        catch (DeserializationException | InvalidConfigurationException e) {
            throw new ConfigurationInitializationException("error initializing in-memory file storage", e);
        }
    }


    @Override
    public byte[] get(String path) throws ResourceNotFoundException {
        if (!files.containsKey(path)) {
            throw new ResourceNotFoundException(String.format("could not find file for path '%s'", path));
        }
        return files.get(path);
    }


    @Override
    public boolean contains(String path) {
        return this.files.containsKey(path);
    }


    @Override
    public void save(String path, byte[] content) {
        files.put(path, content);
    }


    @Override
    public void delete(String path) throws ResourceNotFoundException {
        if (!files.containsKey(path)) {
            throw new ResourceNotFoundException(String.format("could not find file for path '%s'", path));
        }
        files.remove(path);
    }


    @Override
    public void deleteAll() throws PersistenceException {
        files.clear();
    }


    @Override
    public FileStorageInMemoryConfig asConfig() {
        return config;
    }

}
