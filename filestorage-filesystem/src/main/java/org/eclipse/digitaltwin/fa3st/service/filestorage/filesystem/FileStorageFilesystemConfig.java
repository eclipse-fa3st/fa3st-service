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

import java.util.Objects;
import org.eclipse.digitaltwin.fa3st.service.filestorage.FileStorageConfig;


/**
 * Configuration class for {@link FileStorageFilesystem}.
 */
public class FileStorageFilesystemConfig extends FileStorageConfig<FileStorageFilesystem> {

    private static final String DEFAULT_PATH = ".";
    private String path;
    private String existingDataPath;

    public FileStorageFilesystemConfig() {
        this.path = DEFAULT_PATH;
    }


    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }


    public String getExistingDataPath() {
        return existingDataPath;
    }


    public void setExistingDataPath(String existingDataPath) {
        this.existingDataPath = existingDataPath;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileStorageFilesystemConfig that = (FileStorageFilesystemConfig) o;
        return Objects.equals(path, that.path)
                && Objects.equals(existingDataPath, that.existingDataPath);
    }


    @Override
    public int hashCode() {
        return Objects.hash(path, existingDataPath);
    }


    public static Builder builder() {
        return new Builder();
    }

    private abstract static class AbstractBuilder<T extends FileStorageFilesystemConfig, B extends AbstractBuilder<T, B>>
            extends FileStorageConfig.AbstractBuilder<FileStorageFilesystem, T, B> {

        public B path(String value) {
            getBuildingInstance().setPath(value);
            return getSelf();
        }


        public B existingDataPath(String value) {
            getBuildingInstance().setExistingDataPath(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<FileStorageFilesystemConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected FileStorageFilesystemConfig newBuildingInstance() {
            return new FileStorageFilesystemConfig();
        }
    }
}
