package com.catalis.tools.pomvalidator;

import com.catalis.tools.pomvalidator.service.PomValidationService;
import com.catalis.tools.pomvalidator.model.ValidationResult;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main application class for the POM Validator Tool.
 * This tool validates Maven POM files for structure, dependencies, and common issues.
 */
public class PomValidatorApplication {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java -jar pom-validator-tool.jar <path-to-pom.xml>");
            System.err.println("Example: java -jar pom-validator-tool.jar /path/to/pom.xml");
            System.exit(1);
        }

        String pomFilePath = args[0];
        Path pomPath = Paths.get(pomFilePath);

        try {
            PomValidationService validationService = new PomValidationService();
            ValidationResult result = validationService.validatePom(pomPath);
            
            System.out.println("=== POM Validation Results ===");
            System.out.println("File: " + pomPath.toAbsolutePath());
            System.out.println("Status: " + (result.isValid() ? "VALID" : "INVALID"));
            System.out.println();
            
            if (!result.getErrors().isEmpty()) {
                System.out.println("ERRORS:");
                result.getErrors().forEach(error -> System.out.println("  ❌ " + error));
                System.out.println();
            }
            
            if (!result.getWarnings().isEmpty()) {
                System.out.println("WARNINGS:");
                result.getWarnings().forEach(warning -> System.out.println("  ⚠️  " + warning));
                System.out.println();
            }
            
            if (!result.getInfos().isEmpty()) {
                System.out.println("INFO:");
                result.getInfos().forEach(info -> System.out.println("  ℹ️  " + info));
                System.out.println();
            }
            
            System.out.println("Summary: " + result.getErrors().size() + " errors, " 
                             + result.getWarnings().size() + " warnings, " 
                             + result.getInfos().size() + " info messages");
            
            System.exit(result.isValid() ? 0 : 1);
            
        } catch (Exception e) {
            System.err.println("Error validating POM file: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
