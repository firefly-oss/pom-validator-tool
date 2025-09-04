/*
 * Copyright 2025 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.firefly.tools.pomvalidator.util;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.FileReader;
import java.nio.file.Path;

/**
 * Utility class for parsing Maven POM files.
 */
public class PomParser {
    
    private final MavenXpp3Reader reader;
    
    public PomParser() {
        this.reader = new MavenXpp3Reader();
    }
    
    /**
     * Parses a POM file and returns the Maven Model.
     */
    public Model parsePom(Path pomPath) throws Exception {
        try (FileReader fileReader = new FileReader(pomPath.toFile())) {
            return reader.read(fileReader);
        }
    }
}
