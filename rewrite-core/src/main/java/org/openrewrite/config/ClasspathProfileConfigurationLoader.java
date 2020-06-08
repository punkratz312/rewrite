/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.config;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public class ClasspathProfileConfigurationLoader implements ProfileConfigurationLoader {
    private final Iterable<Path> compileClasspath;

    public ClasspathProfileConfigurationLoader(Iterable<Path> compileClasspath) {
        this.compileClasspath = compileClasspath;
    }

    @Override
    public Collection<ProfileConfiguration> load() {
        Collection<ProfileConfiguration> profiles = new ArrayList<>();

        try (ScanResult scanResult = new ClassGraph()
                .whitelistPaths("META-INF/rewrite-profiles")
                .enableMemoryMapping()
                .scan()) {
            loadScannedProfileYamls(profiles, scanResult);
        }

        try (ScanResult scanResult = new ClassGraph()
                .overrideClasspath(compileClasspath)
                .whitelistPaths("META-INF/rewrite-profiles")
                .enableMemoryMapping()
                .scan()) {
            loadScannedProfileYamls(profiles, scanResult);
        }

        return profiles;
    }

    private void loadScannedProfileYamls(Collection<ProfileConfiguration> profiles, ScanResult scanResult) {
        ResourceList yml = scanResult.getResourcesWithExtension("yml");
        yml.forEachInputStreamIgnoringIOException((res, ymlIn) -> {
            profiles.addAll(new YamlProfileConfigurationLoader(ymlIn).load());
        });
    }
}
