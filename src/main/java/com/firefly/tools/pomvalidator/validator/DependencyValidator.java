package com.firefly.tools.pomvalidator.validator;

import com.firefly.tools.pomvalidator.model.ValidationResult;
import com.firefly.tools.pomvalidator.model.ValidationIssue;
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
        ValidationResult.Builder result = ValidationResult.builder();
        
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
        
        result.info(ValidationIssue.of("Dependencies: " + directCount + " direct, " + managedCount + " managed"));
        
        return result.build();
    }
    
    private void validateDependencies(List<Dependency> dependencies, 
                                    ValidationResult.Builder result, 
                                    String type) {
        // Check for duplicate dependencies
        Map<String, List<Dependency>> groupedDeps = dependencies.stream()
            .collect(Collectors.groupingBy(dep -> dep.getGroupId() + ":" + dep.getArtifactId()));
        
        for (Map.Entry<String, List<Dependency>> entry : groupedDeps.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.error(ValidationIssue.of(
                    "Duplicate " + type + " dependency: " + entry.getKey(),
                    "Remove duplicate dependency declarations, keep only one version"
                ));
                // List all versions if they differ
                Set<String> versions = entry.getValue().stream()
                    .map(Dependency::getVersion)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
                if (versions.size() > 1) {
                    result.error(ValidationIssue.of(
                        "Version conflict for " + entry.getKey() + ": " + versions,
                        "Choose one version and remove others, or use dependencyManagement to control versions"
                    ));
                }
            }
        }
        
        // Check individual dependencies
        for (Dependency dep : dependencies) {
            validateSingleDependency(dep, result, type);
        }
    }
    
    private void validateSingleDependency(Dependency dep, 
                                        ValidationResult.Builder result, 
                                        String type) {
        String coords = dep.getGroupId() + ":" + dep.getArtifactId();
        
        // Check for missing groupId or artifactId
        if (isBlank(dep.getGroupId())) {
            result.error(ValidationIssue.of(
                "Missing groupId in " + type + " dependency: " + coords,
                "Add <groupId>org.example</groupId> element to the dependency"
            ));
        }
        
        if (isBlank(dep.getArtifactId())) {
            result.error(ValidationIssue.of(
                "Missing artifactId in " + type + " dependency: " + coords,
                "Add <artifactId>library-name</artifactId> element to the dependency"
            ));
        }
        
        // Check for version-related issues
        String version = dep.getVersion();
        if (!isBlank(version)) {
            // Check for SNAPSHOT versions in non-SNAPSHOT projects
            if (version.endsWith("-SNAPSHOT")) {
                result.warning(ValidationIssue.of(
                    "SNAPSHOT dependency in " + type + ": " + coords + ":" + version,
                    "Use stable release versions in production, SNAPSHOT versions are for development only"
                ));
            }
            
            // Check for version ranges (generally discouraged)
            if (version.contains("[") || version.contains("(") || version.contains(",")) {
                result.warning(ValidationIssue.of(
                    "Version range in " + type + " dependency: " + coords + ":" + version,
                    "Specify exact version numbers for reproducible builds: <version>1.2.3</version>"
                ));
            }
            
            // Check for LATEST or RELEASE versions (deprecated)
            if ("LATEST".equals(version) || "RELEASE".equals(version)) {
                result.error(ValidationIssue.of(
                    "Deprecated version keyword in " + type + " dependency: " + coords + ":" + version,
                    "Replace with specific version number: <version>1.2.3</version>"
                ));
            }
        }
        
        // Check scope
        String scope = dep.getScope();
        if (scope != null && !isValidScope(scope)) {
            result.warning(ValidationIssue.of(
                "Invalid scope in " + type + " dependency " + coords + ": " + scope,
                "Use valid scopes: compile, provided, runtime, test, system, or import"
            ));
        }
        
        // Check for common problematic dependencies
        checkProblematicDependencies(dep, result, type);
    }
    
    private void validateVersionManagement(List<Dependency> dependencies, 
                                         DependencyManagement depMgmt, 
                                         ValidationResult.Builder result) {
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
                    result.error(ValidationIssue.of(
                        "Direct dependency without version and not in dependency management: " + coords,
                        "Add <version>1.2.3</version> or define in <dependencyManagement> section"
                    ));
                }
            } else {
                if (managedDeps.contains(coords)) {
                    result.warning(ValidationIssue.of(
                        "Direct dependency specifies version but is managed: " + coords,
                        "Remove <version> tag from dependency to use managed version"
                    ));
                }
            }
        }
    }
    
    private void checkProblematicDependencies(Dependency dep, 
                                            ValidationResult.Builder result, 
                                            String type) {
        String coords = dep.getGroupId() + ":" + dep.getArtifactId();
        
        // Check for common issues
        if ("commons-logging:commons-logging".equals(coords)) {
            result.warning(ValidationIssue.of(
                "Consider using SLF4J instead of commons-logging: " + coords,
                "Replace with org.slf4j:slf4j-api and appropriate implementation"
            ));
        }
        
        if ("log4j:log4j".equals(coords) && 
            (dep.getVersion() == null || dep.getVersion().startsWith("1."))) {
            result.warning(ValidationIssue.of(
                "Log4j 1.x is end-of-life, consider upgrading: " + coords,
                "Upgrade to org.apache.logging.log4j:log4j-core version 2.x"
            ));
        }
        
        // Check for test dependencies in non-test scope
        if (isTestDependency(coords) && !"test".equals(dep.getScope())) {
            result.warning(ValidationIssue.of(
                "Test framework should have test scope: " + coords,
                "Add <scope>test</scope> to this dependency"
            ));
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
