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


package com.firefly.tools.pomvalidator.feature;

import com.firefly.tools.pomvalidator.service.PomValidationService;
import com.firefly.tools.pomvalidator.model.ValidationResult;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Watch mode for continuous monitoring of POM files.
 * Automatically validates POMs when they change.
 */
public class WatchMode {
    
    private final PomValidationService validationService;
    private final Map<Path, Long> lastModified;
    private final AtomicBoolean running;
    
    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";
    
    public WatchMode() {
        this.validationService = new PomValidationService();
        this.lastModified = new HashMap<>();
        this.running = new AtomicBoolean(false);
    }
    
    public void watch(Path rootPath, boolean recursive) throws IOException, InterruptedException {
        running.set(true);
        
        // Clear screen and show header
        clearScreen();
        System.out.println(BOLD + PURPLE + "🔍 POM Validator - Watch Mode" + RESET);
        System.out.println(CYAN + "Monitoring: " + rootPath.toAbsolutePath() + RESET);
        System.out.println(CYAN + "Mode: " + (recursive ? "Recursive" : "Single directory") + RESET);
        System.out.println(YELLOW + "Press Ctrl+C to stop watching..." + RESET);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        // Initial validation
        validateAllPoms(rootPath, recursive);
        
        // Set up file watcher
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            
            // Register directories
            if (recursive) {
                registerRecursive(rootPath, watchService);
            } else {
                rootPath.register(watchService, 
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
            }
            
            // Watch loop
            while (running.get()) {
                WatchKey key = watchService.poll(1, java.util.concurrent.TimeUnit.SECONDS);
                if (key == null) {
                    continue;
                }
                
                Path dir = (Path) key.watchable();
                
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    
                    Path fileName = (Path) event.context();
                    Path fullPath = dir.resolve(fileName);
                    
                    if (fileName.toString().equals("pom.xml")) {
                        handlePomChange(fullPath, kind);
                    } else if (kind == StandardWatchEventKinds.ENTRY_CREATE && 
                               Files.isDirectory(fullPath) && recursive) {
                        // Register new directory if in recursive mode
                        fullPath.register(watchService,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_MODIFY,
                            StandardWatchEventKinds.ENTRY_DELETE);
                    }
                }
                
                key.reset();
            }
        }
    }
    
    private void registerRecursive(Path rootPath, WatchService watchService) throws IOException {
        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // Skip hidden directories and target
                String dirName = dir.getFileName() != null ? dir.getFileName().toString() : "";
                if (dirName.startsWith(".") || dirName.equals("target") || dirName.equals("node_modules")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                
                dir.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    private void validateAllPoms(Path rootPath, boolean recursive) throws IOException {
        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.getFileName().toString().equals("pom.xml")) {
                    validatePom(file, false);
                }
                return recursive ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
            }
            
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String dirName = dir.getFileName() != null ? dir.getFileName().toString() : "";
                if (dirName.startsWith(".") || dirName.equals("target") || dirName.equals("node_modules")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    private void handlePomChange(Path pomPath, WatchEvent.Kind<?> kind) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            System.out.println(YELLOW + "[" + timestamp + "] " + "POM deleted: " + pomPath + RESET);
            lastModified.remove(pomPath);
        } else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            System.out.println(GREEN + "[" + timestamp + "] " + "New POM detected: " + pomPath + RESET);
            validatePom(pomPath, true);
        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            // Check if file actually changed (avoid duplicate events)
            try {
                long currentMod = Files.getLastModifiedTime(pomPath).toMillis();
                Long lastMod = lastModified.get(pomPath);
                if (lastMod == null || currentMod > lastMod) {
                    System.out.println(BLUE + "[" + timestamp + "] " + "POM modified: " + pomPath + RESET);
                    validatePom(pomPath, true);
                }
            } catch (IOException e) {
                // File might have been deleted
            }
        }
    }
    
    private void validatePom(Path pomPath, boolean showTimestamp) {
        try {
            // Update last modified time
            lastModified.put(pomPath, Files.getLastModifiedTime(pomPath).toMillis());
            
            // Validate
            ValidationResult result = validationService.validatePom(pomPath);
            
            // Display result
            String timestamp = showTimestamp ? 
                "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " : "";
            
            if (result.isValid() && result.getWarnings().isEmpty()) {
                System.out.println(GREEN + timestamp + "✅ " + pomPath.getFileName() + 
                    " - Valid (no issues)" + RESET);
            } else if (result.isValid()) {
                System.out.println(YELLOW + timestamp + "⚠️  " + pomPath.getFileName() + 
                    " - Valid with " + result.getWarnings().size() + " warnings" + RESET);
            } else {
                System.out.println(RED + timestamp + "❌ " + pomPath.getFileName() + 
                    " - Invalid (" + result.getErrors().size() + " errors, " + 
                    result.getWarnings().size() + " warnings)" + RESET);
                
                // Show first error
                if (!result.getErrors().isEmpty()) {
                    System.out.println("    └─ " + result.getErrors().get(0).getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println(RED + "Error validating " + pomPath + ": " + e.getMessage() + RESET);
        }
    }
    
    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    public void stop() {
        running.set(false);
    }
}
