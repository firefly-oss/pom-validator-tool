package com.catalis.tools.pomvalidator.validator;

import com.catalis.tools.pomvalidator.model.ValidationResult;
import com.catalis.tools.pomvalidator.model.ValidationIssue;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates multi-module Maven project structure including parent-child relationships
 * and module references.
 */
public class MultiModuleValidator implements PomValidator {
    
    @Override
    public ValidationResult validate(Model model, Path pomPath) {
        ValidationResult.Builder result = ValidationResult.builder();
        
        // Check if this is a parent POM (has modules)
        List<String> modules = model.getModules();
        if (modules != null && !modules.isEmpty()) {
            validateParentPom(model, pomPath, modules, result);
        }
        
        // Check if this POM has a parent
        Parent parent = model.getParent();
        if (parent != null) {
            validateChildPom(model, pomPath, parent, result);
        }
        
        return result.build();
    }
    
    private void validateParentPom(Model model, Path pomPath, List<String> modules, 
                                   ValidationResult.Builder result) {
        // Check packaging type for parent POM
        String packaging = model.getPackaging();
        if (!"pom".equals(packaging)) {
            result.warning(ValidationIssue.of(
                "Parent POM with modules should have packaging type 'pom', found: " + packaging,
                "Change <packaging>" + packaging + "</packaging> to <packaging>pom</packaging>"
            ));
        }
        
        // Validate each module reference
        Set<String> seenModules = new HashSet<>();
        Path parentDir = pomPath.getParent();
        
        for (String module : modules) {
            // Check for duplicate modules
            if (!seenModules.add(module)) {
                result.error(ValidationIssue.of(
                    "Duplicate module reference: " + module,
                    "Remove duplicate <module>" + module + "</module> entry"
                ));
                continue;
            }
            
            // Check if module directory exists
            Path modulePath = parentDir.resolve(module);
            if (!Files.exists(modulePath)) {
                result.error(ValidationIssue.of(
                    "Module directory does not exist: " + module,
                    "Create the module directory or remove the module reference"
                ));
                continue;
            }
            
            // Check if module has a pom.xml
            Path modulePom = modulePath.resolve("pom.xml");
            if (!Files.exists(modulePom)) {
                result.error(ValidationIssue.of(
                    "Module '" + module + "' does not contain a pom.xml",
                    "Add pom.xml to the module or remove the module reference"
                ));
            }
        }
        
        // Info about multi-module structure
        result.info(ValidationIssue.of(
            "Multi-module project with " + modules.size() + " modules"
        ));
        
        // Check for common parent POM properties
        if (model.getDependencyManagement() == null && 
            (model.getBuild() == null || model.getBuild().getPluginManagement() == null)) {
            result.info(ValidationIssue.of(
                "Consider adding dependencyManagement and/or pluginManagement sections",
                "Parent POMs typically manage versions centrally for child modules"
            ));
        }
    }
    
    private void validateChildPom(Model model, Path pomPath, Parent parent, 
                                  ValidationResult.Builder result) {
        // Check parent coordinates
        if (isBlank(parent.getGroupId())) {
            result.error(ValidationIssue.of(
                "Parent groupId is missing",
                "Add <groupId> to the <parent> section"
            ));
        }
        
        if (isBlank(parent.getArtifactId())) {
            result.error(ValidationIssue.of(
                "Parent artifactId is missing",
                "Add <artifactId> to the <parent> section"
            ));
        }
        
        if (isBlank(parent.getVersion())) {
            result.error(ValidationIssue.of(
                "Parent version is missing",
                "Add <version> to the <parent> section"
            ));
        }
        
        // Check relativePath
        String relativePath = parent.getRelativePath();
        if (relativePath != null && !relativePath.isEmpty()) {
            Path parentPomPath = pomPath.getParent().resolve(relativePath);
            
            // Normalize the path
            if (!relativePath.endsWith("pom.xml")) {
                parentPomPath = parentPomPath.resolve("pom.xml");
            }
            
            if (!Files.exists(parentPomPath)) {
                result.warning(ValidationIssue.of(
                    "Parent POM not found at relative path: " + relativePath,
                    "Verify the relativePath or remove it to use repository resolution"
                ));
            }
        } else if (relativePath == null) {
            // Default relative path is ../pom.xml
            Path defaultParentPath = pomPath.getParent().getParent();
            if (defaultParentPath != null) {
                Path defaultParentPom = defaultParentPath.resolve("pom.xml");
                if (Files.exists(defaultParentPom)) {
                    result.info(ValidationIssue.of(
                        "Using default parent relative path ../pom.xml",
                        "Consider explicitly setting <relativePath>../pom.xml</relativePath>"
                    ));
                }
            }
        }
        
        // Check if groupId is inherited
        if (model.getGroupId() == null && parent.getGroupId() != null) {
            result.info(ValidationIssue.of(
                "GroupId inherited from parent: " + parent.getGroupId()
            ));
        }
        
        // Check if version is inherited
        if (model.getVersion() == null && parent.getVersion() != null) {
            result.info(ValidationIssue.of(
                "Version inherited from parent: " + parent.getVersion()
            ));
        }
        
        // Warn about version alignment
        if (model.getVersion() != null && parent.getVersion() != null) {
            if (!model.getVersion().equals(parent.getVersion()) && 
                !model.getVersion().contains("${")) {
                result.warning(ValidationIssue.of(
                    "Module version (" + model.getVersion() + ") differs from parent version (" + parent.getVersion() + ")",
                    "Consider aligning versions or using ${project.parent.version}"
                ));
            }
        }
    }
    
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
