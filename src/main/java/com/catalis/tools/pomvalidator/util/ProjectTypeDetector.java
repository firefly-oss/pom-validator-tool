package com.catalis.tools.pomvalidator.util;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Utility class to detect the type of Maven project structure.
 */
public class ProjectTypeDetector {
    
    /**
     * Enum representing different types of Maven project structures.
     */
    public enum ProjectType {
        SINGLE_MODULE("Single Module Project", "📦"),
        MULTI_MODULE_PARENT("Multi-Module Parent Project", "📂"),
        MULTI_MODULE_CHILD("Multi-Module Child Module", "📄"),
        AGGREGATOR("Aggregator Project (POM-only)", "🔗"),
        BOM("Bill of Materials (BOM)", "📋"),
        STANDALONE_PARENT("Standalone Parent POM", "🎯"),
        UNKNOWN("Unknown Project Type", "❓");
        
        private final String description;
        private final String icon;
        
        ProjectType(String description, String icon) {
            this.description = description;
            this.icon = icon;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getIcon() {
            return icon;
        }
        
        public String getDisplayName() {
            return icon + " " + description;
        }
    }
    
    /**
     * Detailed information about the project structure.
     */
    public static class ProjectStructureInfo {
        private final ProjectType type;
        private final boolean hasParent;
        private final boolean hasModules;
        private final boolean hasDependencies;
        private final boolean hasDependencyManagement;
        private final boolean hasPluginManagement;
        private final String packaging;
        private final int moduleCount;
        private final int dependencyCount;
        private final String parentInfo;
        
        public ProjectStructureInfo(Model model, Path pomPath) {
            this.packaging = model.getPackaging() != null ? model.getPackaging() : "jar";
            this.hasParent = model.getParent() != null;
            this.hasModules = model.getModules() != null && !model.getModules().isEmpty();
            this.hasDependencies = model.getDependencies() != null && !model.getDependencies().isEmpty();
            this.hasDependencyManagement = model.getDependencyManagement() != null && 
                model.getDependencyManagement().getDependencies() != null && 
                !model.getDependencyManagement().getDependencies().isEmpty();
            this.hasPluginManagement = model.getBuild() != null && 
                model.getBuild().getPluginManagement() != null &&
                model.getBuild().getPluginManagement().getPlugins() != null &&
                !model.getBuild().getPluginManagement().getPlugins().isEmpty();
            
            this.moduleCount = hasModules ? model.getModules().size() : 0;
            this.dependencyCount = hasDependencies ? model.getDependencies().size() : 0;
            
            // Determine parent info
            if (hasParent) {
                Parent parent = model.getParent();
                this.parentInfo = parent.getGroupId() + ":" + parent.getArtifactId() + ":" + parent.getVersion();
            } else {
                this.parentInfo = null;
            }
            
            // Determine project type
            this.type = determineProjectType(model, pomPath);
        }
        
        private ProjectType determineProjectType(Model model, Path pomPath) {
            // Check if it's a BOM (Bill of Materials)
            if ("pom".equals(packaging) && !hasModules && hasDependencyManagement && !hasDependencies) {
                return ProjectType.BOM;
            }
            
            // Check if it's a multi-module parent
            if (hasModules) {
                return ProjectType.MULTI_MODULE_PARENT;
            }
            
            // Check if it's a child module in a multi-module project
            if (hasParent) {
                // Check if parent POM exists in parent directory
                Path parentDir = pomPath.getParent().getParent();
                if (parentDir != null) {
                    Path parentPom = parentDir.resolve("pom.xml");
                    if (Files.exists(parentPom)) {
                        return ProjectType.MULTI_MODULE_CHILD;
                    }
                }
                // Even without local parent, if it has a parent declaration, it's likely a child module
                return ProjectType.MULTI_MODULE_CHILD;
            }
            
            // Check if it's an aggregator (POM packaging without dependencies)
            if ("pom".equals(packaging) && !hasModules && !hasDependencies) {
                if (hasDependencyManagement || hasPluginManagement) {
                    return ProjectType.STANDALONE_PARENT;
                }
                return ProjectType.AGGREGATOR;
            }
            
            // Default to single module project
            if (!hasParent && !hasModules) {
                return ProjectType.SINGLE_MODULE;
            }
            
            return ProjectType.UNKNOWN;
        }
        
        // Getters
        public ProjectType getType() { return type; }
        public boolean hasParent() { return hasParent; }
        public boolean hasModules() { return hasModules; }
        public boolean hasDependencies() { return hasDependencies; }
        public boolean hasDependencyManagement() { return hasDependencyManagement; }
        public boolean hasPluginManagement() { return hasPluginManagement; }
        public String getPackaging() { return packaging; }
        public int getModuleCount() { return moduleCount; }
        public int getDependencyCount() { return dependencyCount; }
        public String getParentInfo() { return parentInfo; }
        
        /**
         * Get a detailed description of the project structure.
         */
        public String getDetailedDescription() {
            StringBuilder sb = new StringBuilder();
            sb.append(type.getDisplayName());
            
            if (hasModules) {
                sb.append(" (").append(moduleCount).append(" module");
                if (moduleCount > 1) sb.append("s");
                sb.append(")");
            }
            
            if (hasParent) {
                sb.append(" [Parent: ").append(parentInfo).append("]");
            }
            
            return sb.toString();
        }
        
        /**
         * Get structure characteristics as a list of strings.
         */
        public List<String> getCharacteristics() {
            List<String> chars = new java.util.ArrayList<>();
            
            chars.add("Packaging: " + packaging);
            
            if (hasParent) {
                chars.add("Has Parent: " + parentInfo);
            }
            
            if (hasModules) {
                chars.add("Modules: " + moduleCount);
            }
            
            if (hasDependencies) {
                chars.add("Direct Dependencies: " + dependencyCount);
            }
            
            if (hasDependencyManagement) {
                chars.add("Dependency Management: Yes");
            }
            
            if (hasPluginManagement) {
                chars.add("Plugin Management: Yes");
            }
            
            return chars;
        }
    }
    
    /**
     * Detect the project type and structure from a Maven model.
     */
    public static ProjectStructureInfo detectProjectStructure(Model model, Path pomPath) {
        return new ProjectStructureInfo(model, pomPath);
    }
    
    /**
     * Detect the project type from a Maven model (simple detection).
     */
    public static ProjectType detectProjectType(Model model, Path pomPath) {
        return detectProjectStructure(model, pomPath).getType();
    }
}
