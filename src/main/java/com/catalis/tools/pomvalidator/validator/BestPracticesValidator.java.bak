package com.catalis.tools.pomvalidator.validator;

import com.catalis.tools.pomvalidator.model.ValidationResult;
import com.catalis.tools.pomvalidator.model.ValidationIssue;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;

import java.nio.file.Path;
import java.util.List;

/**
 * Validates general Maven best practices and common issues.
 */
public class BestPracticesValidator implements PomValidator {
    
    @Override
    public ValidationResult validate(Model model, Path pomPath) {
        ValidationResult.ValidationResultBuilder result = ValidationResult.builder();
        
        // Check for descriptive metadata
        checkProjectMetadata(model, result);
        
        // Check repositories
        checkRepositories(model, result);
        
        // Check project structure indicators
        checkProjectStructure(model, result);
        
        // Check for common anti-patterns
        checkAntiPatterns(model, result);
        
        return result.build();
    }
    
    private void checkProjectMetadata(Model model, ValidationResult.ValidationResultBuilder result) {
        // Check for project name
        if (isBlank(model.getName())) {
            result.warning("Missing project name - consider adding <name> element for better documentation");
        }
        
        // Check for project description
        if (isBlank(model.getDescription())) {
            result.warning("Missing project description - consider adding <description> element");
        }
        
        // Check for project URL
        if (isBlank(model.getUrl())) {
            result.info("Consider adding project URL for better documentation");
        }
        
        // Check for license information
        if (model.getLicenses() == null || model.getLicenses().isEmpty()) {
            result.info("Consider adding license information");
        }
        
        // Check for developer information
        if (model.getDevelopers() == null || model.getDevelopers().isEmpty()) {
            result.info("Consider adding developer information for better project documentation");
        }
        
        // Check for SCM information
        if (model.getScm() == null) {
            result.info("Consider adding SCM information for better traceability");
        }
    }
    
    private void checkRepositories(Model model, ValidationResult.ValidationResultBuilder result) {
        // Check regular repositories
        List<Repository> repositories = model.getRepositories();
        if (repositories != null && !repositories.isEmpty()) {
            result.warning("Custom repositories defined - ensure they are necessary and secure");
            
            for (Repository repo : repositories) {
                checkSingleRepository(repo, "repository", result);
            }
        }
        
        // Plugin repositories are less commonly used, so we skip detailed validation for now
        if (model.getPluginRepositories() != null && !model.getPluginRepositories().isEmpty()) {
            result.warning("Custom plugin repositories defined - ensure they are necessary and secure");
        }
    }
    
    private void checkSingleRepository(Repository repo, String type, 
                                     ValidationResult.ValidationResultBuilder result) {
        if (repo.getUrl() != null) {
            String url = repo.getUrl();
            
            // Check for HTTP URLs (security concern)
            if (url.startsWith("http://")) {
                result.warning("Repository uses HTTP instead of HTTPS: " + url);
            }
            
            // Check for snapshot repositories in releases
            if (repo.getReleases() != null && repo.getReleases().isEnabled() &&
                repo.getSnapshots() != null && repo.getSnapshots().isEnabled()) {
                result.info("Repository " + repo.getId() + " allows both releases and snapshots");
            }
            
            // Warn about common public repositories that might be unnecessary
            if (url.contains("repo1.maven.org") || url.contains("central")) {
                result.info("Central repository explicitly defined - this is usually unnecessary");
            }
        }
    }
    
    
    private void checkProjectStructure(Model model, ValidationResult.ValidationResultBuilder result) {
        // Check if this looks like a multi-module project
        if (model.getModules() != null && !model.getModules().isEmpty()) {
            result.info("Multi-module project detected with " + model.getModules().size() + " modules");
            
            // For multi-module projects, packaging should be pom
            if (!"pom".equals(model.getPackaging())) {
                result.warning("Multi-module project should have <packaging>pom</packaging>");
            }
        }
        
        // Check for profiles
        if (model.getProfiles() != null && !model.getProfiles().isEmpty()) {
            result.info("Project uses Maven profiles (" + model.getProfiles().size() + " defined)");
        }
    }
    
    private void checkAntiPatterns(Model model, ValidationResult.ValidationResultBuilder result) {
        // Check for very long artifact IDs (can cause issues)
        String artifactId = model.getArtifactId();
        if (artifactId != null && artifactId.length() > 50) {
            result.warning("Very long artifactId - consider shortening: " + artifactId);
        }
        
        // Check for spaces in coordinates (not allowed)
        if (artifactId != null && artifactId.contains(" ")) {
            result.error("ArtifactId contains spaces: " + artifactId);
        }
        
        String groupId = model.getGroupId();
        if (groupId != null && groupId.contains(" ")) {
            result.error("GroupId contains spaces: " + groupId);
        }
        
        // Check for uppercase in group/artifact IDs (convention is lowercase)
        if (groupId != null && !groupId.equals(groupId.toLowerCase())) {
            result.warning("GroupId should be lowercase by convention: " + groupId);
        }
        
        if (artifactId != null && !artifactId.equals(artifactId.toLowerCase())) {
            result.warning("ArtifactId should be lowercase by convention: " + artifactId);
        }
        
        // Check for Maven coordinates that might conflict with well-known ones
        if ("org.apache.maven".equals(groupId)) {
            result.warning("Using Apache Maven groupId - ensure this is intentional");
        }
        
        if ("junit".equals(artifactId) && !"junit".equals(groupId)) {
            result.warning("ArtifactId 'junit' with non-standard groupId - potential confusion");
        }
    }
    
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
