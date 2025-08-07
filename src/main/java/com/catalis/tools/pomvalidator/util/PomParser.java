package com.catalis.tools.pomvalidator.util;

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
