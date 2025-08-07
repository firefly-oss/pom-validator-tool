package com.catalis.tools.pomvalidator.validator;

import com.catalis.tools.pomvalidator.model.ValidationResult;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Validates dependency-related issues like duplicate dependencies, version conflicts, etc.
 */
public class DependencyValidator implements PomValidator {
    
    @Override
    public ValidationResult validate(Model model, Path pomPath) {
        ValidationResult.ValidationResultBuilder result = ValidationResult.builder();
        
        // Check direct dependencies
        List<Dependency> dependencies = model.getDependencies();
        if (dependencies != null && !dependencies.isEmpty()) {
            validateDependencies(dependencies, result, "direct");
        }
        
        // Check managed dependencies
        DependencyManagement depMgmt = model.getDependencyManagement();
        if (depMgmt != null && depMgmt.getDependencies() != null) {
            validateDependencies(depMgmt.getDependencies(), result, "managed");
        }
        
        // Check for dependencies without versions (should use dependency management)
        if (dependencies != null) {
            validateVersionManagement(dependencies, depMgmt, result);
        }
        
        // Info about dependencies
        int directCount = dependencies != null ? dependencies.size() : 0;
        int managedCount = depMgmt != null && depMgmt.getDependencies() != null ? 
            depMgmt.getDependencies().size() : 0;
        
        result.info("Dependencies: " + directCount + " direct, " + managedCount + " managed");
        
        return result.build();
    }
    
    private void validateDependencies(List<Dependency> dependencies, 
                                    ValidationResult.ValidationResultBuilder result, 
                                    String type) {
        // Check for duplicate dependencies
        Map<String, List<Dependency>> groupedDeps = dependencies.stream()
            .collect(Collectors.groupingBy(dep -> dep.getGroupId() + ":" + dep.getArtifactId()));
        
        for (Map.Entry<String, List<Dependency>> entry : groupedDeps.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.error("Duplicate " + type + " dependency: " + entry.getKey());
                // List all versions if they differ
                Set<String> versions = entry.getValue().stream()
                    .map(Dependency::getVersion)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
                if (versions.size() > 1) {
                    result.error("Version conflict for " + entry.getKey() + ": " + versions);
                }
            }
        }
        
        // Check individual dependencies
        for (Dependency dep : dependencies) {
            validateSingleDependency(dep, result, type);
        }
    }
    
    private void validateSingleDependency(Dependency dep, 
                                        ValidationResult.ValidationResultBuilder result, 
                                        String type) {
        String coords = dep.getGroupId() + ":" + dep.getArtifactId();
        
        // Check for missing groupId or artifactId
        if (isBlank(dep.getGroupId())) {
            result.error("Missing groupId in " + type + " dependency: " + coords);
        }
        
        if (isBlank(dep.getArtifactId())) {
            result.error("Missing artifactId in " + type + " dependency: " + coords);
        }
        
        // Check for version-related issues
        String version = dep.getVersion();
        if (!isBlank(version)) {
            // Check for SNAPSHOT versions in non-SNAPSHOT projects
            if (version.endsWith("-SNAPSHOT")) {
                result.warning("SNAPSHOT dependency in " + type + ": " + coords + ":" + version);
            }
            
            // Check for version ranges (generally discouraged)
            if (version.contains("[") || version.contains("(") || version.contains(",")) {
                result.warning("Version range in " + type + " dependency: " + coords + ":" + version);
            }
            
            // Check for LATEST or RELEASE versions (deprecated)
            if ("LATEST".equals(version) || "RELEASE".equals(version)) {
                result.error("Deprecated version keyword in " + type + " dependency: " + coords + ":" + version);
            }
        }
        
        // Check scope
        String scope = dep.getScope();
        if (scope != null && !isValidScope(scope)) {
            result.warning("Invalid scope in " + type + " dependency " + coords + ": " + scope);
        }
        
        // Check for common problematic dependencies
        checkProblematicDependencies(dep, result, type);
    }
    
    private void validateVersionManagement(List<Dependency> dependencies, 
                                         DependencyManagement depMgmt, 
                                         ValidationResult.ValidationResultBuilder result) {
        if (depMgmt == null || depMgmt.getDependencies() == null) {
            return;
        }
        
        Set<String> managedDeps = depMgmt.getDependencies().stream()
            .map(dep -> dep.getGroupId() + ":" + dep.getArtifactId())
            .collect(Collectors.toSet());
        
        for (Dependency dep : dependencies) {
            String coords = dep.getGroupId() + ":" + dep.getArtifactId();
            
            if (isBlank(dep.getVersion())) {
                if (!managedDeps.contains(coords)) {
                    result.error("Direct dependency without version and not in dependency management: " + coords);
                }
            } else {
                if (managedDeps.contains(coords)) {
                    result.warning("Direct dependency specifies version but is managed: " + coords);
                }
            }
        }
    }
    
    private void checkProblematicDependencies(Dependency dep, 
                                            ValidationResult.ValidationResultBuilder result, 
                                            String type) {
        String coords = dep.getGroupId() + ":" + dep.getArtifactId();
        
        // Check for common issues
        if ("commons-logging:commons-logging".equals(coords)) {
            result.warning("Consider using SLF4J instead of commons-logging: " + coords);
        }
        
        if ("log4j:log4j".equals(coords) && 
            (dep.getVersion() == null || dep.getVersion().startsWith("1."))) {
            result.warning("Log4j 1.x is end-of-life, consider upgrading: " + coords);
        }
        
        // Check for test dependencies in non-test scope
        if (isTestDependency(coords) && !"test".equals(dep.getScope())) {
            result.warning("Test framework should have test scope: " + coords);
        }
    }
    
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    private boolean isValidScope(String scope) {
        return Set.of("compile", "provided", "runtime", "test", "system", "import").contains(scope);
    }
    
    private boolean isTestDependency(String coords) {
        return coords.contains("junit") || 
               coords.contains("testng") || 
               coords.contains("mockito") ||
               coords.contains("assertj") ||
               coords.startsWith("org.springframework:spring-test");
    }
}
