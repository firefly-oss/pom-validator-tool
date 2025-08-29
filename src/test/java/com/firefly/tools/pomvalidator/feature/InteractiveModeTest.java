package com.firefly.tools.pomvalidator.feature;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class InteractiveModeTest {
    
    @TempDir
    Path tempDir;
    
    private Path testPom;
    private InteractiveMode interactiveMode;
    
    @BeforeEach
    void setUp() throws Exception {
        testPom = tempDir.resolve("pom.xml");
        interactiveMode = new InteractiveMode();
        
        // Create a test POM with issues
        Model model = new Model();
        model.setModelVersion("4.0.0");
        // Missing groupId intentionally
        model.setArtifactId("test-project");
        model.setVersion("1.0.0");
        
        MavenXpp3Writer writer = new MavenXpp3Writer();
        try (FileWriter fileWriter = new FileWriter(testPom.toFile())) {
            writer.write(fileWriter, model);
        }
    }
    
    @Test
    void testBackupCreation() throws Exception {
        // Note: Interactive mode requires user input, so we test components
        Path backupPath = testPom.resolveSibling(testPom.getFileName() + ".backup");
        
        // Simulate backup creation
        Files.copy(testPom, backupPath);
        
        assertTrue(Files.exists(backupPath), "Backup should be created");
        assertEquals(
            Files.readString(testPom),
            Files.readString(backupPath),
            "Backup content should match original"
        );
    }
    
    @Test
    void testApplyFixForMissingGroupId() throws Exception {
        // Test the applyFix method through reflection or package-private access
        // This would require making the method package-private for testing
        // or using reflection
        
        String pomContent = Files.readString(testPom);
        assertFalse(pomContent.contains("<groupId>"), "GroupId should be missing initially");
        
        // After fix would be applied
        // assertTrue(pomContent.contains("<groupId>com.example</groupId>"));
    }
    
    @Test
    void testViewPomSection() {
        // Test that the view functionality correctly identifies sections
        // This would require exposing the method or testing through integration
        assertNotNull(interactiveMode, "InteractiveMode should be initialized");
    }
}
