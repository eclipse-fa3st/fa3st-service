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
package org.eclipse.digitaltwin.fa3st.service.filestorage;

import org.eclipse.digitaltwin.fa3st.common.exception.ConfigurationException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.InMemoryFile;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.config.CoreConfig;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * A test class for a file storage implementation should inherit from this abstract class. This class provides basic
 * tests for all methods defined in {@link org.eclipse.digitaltwin.fa3st.service.filestorage.FileStorage}.
 *
 * @param <T> type of the file storage to test
 * @param <C> type of the config matching the file storage to test
 */
public abstract class AbstractFileStorageTest<T extends FileStorage<C>, C extends FileStorageConfig<T>> {

    protected static final ServiceContext SERVICE_CONTEXT = Mockito.mock(ServiceContext.class);
    private T fileStorage;

    /**
     * Gets an instance of the concrete file storage config to use.
     *
     * @return the file storage configuration
     * @throws org.eclipse.digitaltwin.fa3st.service.exception.ConfigurationException if configuration fails
     */
    public abstract C getFileStorageConfig() throws ConfigurationException;


    @Test
    public void saveAndDelete() throws ResourceNotFoundException, ConfigurationException, PersistenceException {
        FileStorageConfig<T> config = getFileStorageConfig();
        fileStorage = config.newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        InMemoryFile expected = InMemoryFile.builder()
                .path("my/path/file.txt")
                .content("foo".getBytes())
                .build();
        fileStorage.save(expected);
        byte[] actual = fileStorage.get(expected.getPath());
        Assert.assertArrayEquals(expected.getContent(), actual);
        fileStorage.delete(expected.getPath());
        Assert.assertThrows(ResourceNotFoundException.class, () -> fileStorage.get(expected.getPath()));
    }
}
