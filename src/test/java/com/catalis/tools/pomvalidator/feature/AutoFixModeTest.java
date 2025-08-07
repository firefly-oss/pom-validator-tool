package com.catalis.tools.pomvalidator.feature;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.apache.maven.model.Model;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Build;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.FileWriter;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class AutoFixModeTest {
    
    @TempDir
    Path tempDir;
    
    private Path testPom;
    private AutoFixMode autoFixMode;
    
    @BeforeEach
    void setUp() throws Exception {
        testPom = tempDir.resolve("pom.xml");
        autoFixMode = new AutoFixMode();
    }
    
    @Test
    void testFixMissingGroupId() throws Exception {
        // Create POM without groupId
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setArtifactId("test-project");
        model.setVersion("1.0.0");
        
        writePom(model);
        
        // Run auto-fix
        autoFixMode.runAutoFix(testPom, false);
        
        // Verify fix
        Model fixedModel = readPom();
        assertNotNull(fixedModel.getGroupId(), "GroupId should be added");
        assertEquals("com.example", fixedModel.getGroupId(), "Default groupId should be set");
    }
    
    @Test
    void testFixMissingProperties() throws Exception {
        // Create POM without encoding properties
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId("com.test");
        model.setArtifactId("test-project");
        model.setVersion("1.0.0");
        
        Properties props = new Properties();
        props.setProperty("java.version", "21");
        model.setProperties(props);
        
        writePom(model);
        
        // Run auto-fix
        autoFixMode.runAutoFix(testPom, false);
        
        // Verify properties were added
        Model fixedModel = readPom();
        Properties fixedProps = fixedModel.getProperties();
        
        assertEquals("UTF-8", fixedProps.getProperty("project.build.sourceEncoding"),
            "Source encoding should be added");
        assertEquals("UTF-8", fixedProps.getProperty("project.reporting.outputEncoding"),
            "Output encoding should be added");
        assertEquals("21", fixedProps.getProperty("maven.compiler.source"),
            "Compiler source should be added");
        assertEquals("21", fixedProps.getProperty("maven.compiler.target"),
            "Compiler target should be added");
    }
    
    @Test
    void testFixDuplicateDependencies() throws Exception {
        // Create POM with duplicate dependencies
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId("com.test");
        model.setArtifactId("test-project");
        model.setVersion("1.0.0");
        
        Dependency dep1 = new Dependency();
        dep1.setGroupId("org.springframework");
        dep1.setArtifactId("spring-core");
        dep1.setVersion("5.3.20");
        
        Dependency dep2 = new Dependency();
        dep2.setGroupId("org.springframework");
        dep2.setArtifactId("spring-core");
        dep2.setVersion("5.3.21");
        
        model.setDependencies(Arrays.asList(dep1, dep2));
        
        writePom(model);
        
        // Run auto-fix
        autoFixMode.runAutoFix(testPom, false);
        
        // Verify duplicates removed
        Model fixedModel = readPom();
        assertEquals(1, fixedModel.getDependencies().size(),
            "Duplicate dependency should be removed");
        assertEquals("5.3.20", fixedModel.getDependencies().get(0).getVersion(),
            "First version should be kept");
    }
    
    @Test
    void testFixTestScope() throws Exception {
        // Create POM with junit without test scope
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId("com.test");
        model.setArtifactId("test-project");
        model.setVersion("1.0.0");
        
        Dependency junit = new Dependency();
        junit.setGroupId("junit");
        junit.setArtifactId("junit");
        junit.setVersion("4.13.2");
        // No scope set
        
        model.setDependencies(Arrays.asList(junit));
        
        writePom(model);
        
        // Run auto-fix
        autoFixMode.runAutoFix(testPom, false);
        
        // Verify test scope added
        Model fixedModel = readPom();
        Dependency fixedJunit = fixedModel.getDependencies().get(0);
        assertEquals("test", fixedJunit.getScope(), "Test scope should be added to junit");
    }
    
    @Test
    void testFixPluginVersion() throws Exception {
        // Create POM with plugin without version
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId("com.test");
        model.setArtifactId("test-project");
        model.setVersion("1.0.0");
        
        Plugin plugin = new Plugin();
        plugin.setGroupId("org.apache.maven.plugins");
        plugin.setArtifactId("maven-compiler-plugin");
        // No version set
        
        Build build = new Build();
        build.setPlugins(Arrays.asList(plugin));
        model.setBuild(build);
        
        writePom(model);
        
        // Run auto-fix
        autoFixMode.runAutoFix(testPom, false);
        
        // Verify plugin version added
        Model fixedModel = readPom();
        Plugin fixedPlugin = fixedModel.getBuild().getPlugins().get(0);
        assertNotNull(fixedPlugin.getVersion(), "Plugin version should be added");
        assertEquals("3.11.0", fixedPlugin.getVersion(), "Compiler plugin version should be 3.11.0");
    }
    
    @Test
    void testBackupCreation() throws Exception {
        // Create simple POM
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId("com.test");
        model.setArtifactId("test-project");
        model.setVersion("1.0.0");
        
        writePom(model);
        
        // Run auto-fix with backup
        autoFixMode.runAutoFix(testPom, true);
        
        // Verify backup exists
        Path backupPath = testPom.resolveSibling(testPom.getFileName() + ".backup");
        assertTrue(Files.exists(backupPath), "Backup should be created");
    }
    
    private void writePom(Model model) throws Exception {
        MavenXpp3Writer writer = new MavenXpp3Writer();
        try (FileWriter fileWriter = new FileWriter(testPom.toFile())) {
            writer.write(fileWriter, model);
        }
    }
    
    private Model readPom() throws Exception {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try (FileReader fileReader = new FileReader(testPom.toFile())) {
            return reader.read(fileReader);
        }
    }
}
