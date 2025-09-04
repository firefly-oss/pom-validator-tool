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


package com.firefly.tools.pomvalidator.validator;

import com.firefly.tools.pomvalidator.model.ValidationResult;
import com.firefly.tools.pomvalidator.model.ValidationIssue;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginManagement;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates plugin-related issues like missing versions, duplicate plugins, etc.
 */
public class PluginValidator implements PomValidator {
    
    private static final Set<String> CORE_MAVEN_PLUGINS = Set.of(
        "maven-clean-plugin",
        "maven-compiler-plugin", 
        "maven-deploy-plugin",
        "maven-install-plugin",
        "maven-resources-plugin",
        "maven-site-plugin",
        "maven-surefire-plugin"
    );
    
    @Override
    public ValidationResult validate(Model model, Path pomPath) {
        ValidationResult.Builder result = ValidationResult.builder();
        
        Build build = model.getBuild();
        if (build == null) {
        result.info(ValidationIssue.of("No build section defined"));
            return result.build();
        }
        
        // Check direct plugins
        List<Plugin> plugins = build.getPlugins();
        if (plugins != null && !plugins.isEmpty()) {
            validatePlugins(plugins, result, "direct");
        }
        
        // Check managed plugins
        PluginManagement pluginMgmt = build.getPluginManagement();
        if (pluginMgmt != null && pluginMgmt.getPlugins() != null) {
            validatePlugins(pluginMgmt.getPlugins(), result, "managed");
        }
        
        // Check for plugins without versions (should use plugin management)
        if (plugins != null) {
            validateVersionManagement(plugins, pluginMgmt, result);
        }
        
        // Check for recommended plugins
        checkRecommendedPlugins(plugins, result);
        
        // Info about plugins
        int directCount = plugins != null ? plugins.size() : 0;
        int managedCount = pluginMgmt != null && pluginMgmt.getPlugins() != null ? 
            pluginMgmt.getPlugins().size() : 0;
        
        result.info(ValidationIssue.of("Plugins: " + directCount + " direct, " + managedCount + " managed"));
        
        return result.build();
    }
    
    private void validatePlugins(List<Plugin> plugins, 
                               ValidationResult.Builder result, 
                               String type) {
        // Check for duplicate plugins
        Map<String, List<Plugin>> groupedPlugins = plugins.stream()
            .collect(Collectors.groupingBy(plugin -> 
                plugin.getGroupId() + ":" + plugin.getArtifactId()));
        
        for (Map.Entry<String, List<Plugin>> entry : groupedPlugins.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.error(ValidationIssue.of("Duplicate " + type + " plugin: " + entry.getKey(), "Remove duplicate plugin declarations"));
            }
        }
        
        // Check individual plugins
        for (Plugin plugin : plugins) {
            validateSinglePlugin(plugin, result, type);
        }
    }
    
    private void validateSinglePlugin(Plugin plugin, 
                                    ValidationResult.Builder result, 
                                    String type) {
        String coords = getPluginCoords(plugin);
        
        // Check for missing artifactId
        if (isBlank(plugin.getArtifactId())) {
            result.error(ValidationIssue.of("Missing artifactId in " + type + " plugin: " + coords, "Add <artifactId>plugin-name</artifactId>"));
        }
        
        // Check version
        String version = plugin.getVersion();
        if (!isBlank(version)) {
            // Check for SNAPSHOT versions
            if (version.endsWith("-SNAPSHOT")) {
                result.warning(ValidationIssue.of("SNAPSHOT plugin version in " + type + ": " + coords + ":" + version, "Use stable release versions"));
            }
            
            // Check for deprecated version keywords
            if ("LATEST".equals(version) || "RELEASE".equals(version)) {
                result.error(ValidationIssue.of("Deprecated version keyword in " + type + " plugin: " + coords + ":" + version, "Use specific version number"));
            }
        }
        
        // Check for specific plugin configurations
        checkSpecificPluginIssues(plugin, result, type);
    }
    
    private void validateVersionManagement(List<Plugin> plugins, 
                                         PluginManagement pluginMgmt, 
                                         ValidationResult.Builder result) {
        if (pluginMgmt == null || pluginMgmt.getPlugins() == null) {
            // Check if core Maven plugins should have versions specified
            for (Plugin plugin : plugins) {
                String artifactId = plugin.getArtifactId();
                if (CORE_MAVEN_PLUGINS.contains(artifactId) && isBlank(plugin.getVersion())) {
                    result.warning(ValidationIssue.of("Core Maven plugin without version: " + getPluginCoords(plugin), "Add version or use pluginManagement"));
                }
            }
            return;
        }
        
        Set<String> managedPlugins = pluginMgmt.getPlugins().stream()
            .map(this::getPluginCoords)
            .collect(Collectors.toSet());
        
        for (Plugin plugin : plugins) {
            String coords = getPluginCoords(plugin);
            
            if (isBlank(plugin.getVersion())) {
                if (!managedPlugins.contains(coords)) {
                    result.warning(ValidationIssue.of("Direct plugin without version and not in plugin management: " + coords, "Add version or define in pluginManagement"));
                }
            } else {
                if (managedPlugins.contains(coords)) {
                    result.warning(ValidationIssue.of("Direct plugin specifies version but is managed: " + coords, "Remove version to use managed version"));
                }
            }
        }
    }
    
    private void checkRecommendedPlugins(List<Plugin> plugins, 
                                       ValidationResult.Builder result) {
        if (plugins == null) {
            return;
        }
        
        Set<String> pluginArtifacts = plugins.stream()
            .map(Plugin::getArtifactId)
            .collect(Collectors.toSet());
        
        // Check for compiler plugin (essential for Java projects)
        if (!pluginArtifacts.contains("maven-compiler-plugin")) {
            result.warning(ValidationIssue.of("Consider explicitly configuring maven-compiler-plugin", "Add maven-compiler-plugin with Java version"));
        }
        
        // Check for surefire plugin (for testing)
        if (!pluginArtifacts.contains("maven-surefire-plugin")) {
            result.info(ValidationIssue.of("Consider configuring maven-surefire-plugin for test execution"));
        }
    }
    
    private void checkSpecificPluginIssues(Plugin plugin, 
                                         ValidationResult.Builder result, 
                                         String type) {
        String artifactId = plugin.getArtifactId();
        
        // Check maven-compiler-plugin configuration
        if ("maven-compiler-plugin".equals(artifactId)) {
            // This would require parsing the configuration, which is complex
            // For now, just note that it's present
            result.info(ValidationIssue.of("Maven compiler plugin configured"));
        }
        
        // Check for outdated plugins
        if ("cobertura-maven-plugin".equals(artifactId)) {
            result.warning(ValidationIssue.of("Cobertura plugin is deprecated, consider JaCoCo instead: " + getPluginCoords(plugin), "Replace with org.jacoco:jacoco-maven-plugin"));
        }
        
        if ("findbugs-maven-plugin".equals(artifactId)) {
            result.warning(ValidationIssue.of("FindBugs plugin is deprecated, consider SpotBugs instead: " + getPluginCoords(plugin), "Replace with com.github.spotbugs:spotbugs-maven-plugin"));
        }
    }
    
    private String getPluginCoords(Plugin plugin) {
        String groupId = plugin.getGroupId();
        if (isBlank(groupId)) {
            groupId = "org.apache.maven.plugins"; // Default group ID for Maven plugins
        }
        return groupId + ":" + plugin.getArtifactId();
    }
    
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
