package com.desabisc.guide.java.io.idirectories;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

/**
 * <h2>Guide: {@code Files.createDirectory()} and {@code Files.createDirectories()}</h2>
 *
 * <p>This guide explores the two main NIO.2 methods for directory creation in Java:
 * {@link Files#createDirectory(Path, FileAttribute[])} and
 * {@link Files#createDirectories(Path, FileAttribute[])}.
 * Both belong to the {@link Files} utility class introduced in Java 7 as part of the
 * {@code java.nio.file} package.</p>
 *
 * <hr/>
 *
 * <h3>Key Differences at a Glance</h3>
 * <pre>
 * ┌─────────────────────────────┬──────────────────────────────┬──────────────────────────────┐
 * │ Feature                     │ createDirectory()            │ createDirectories()          │
 * ├─────────────────────────────┼──────────────────────────────┼──────────────────────────────┤
 * │ Creates intermediate dirs   │ ✗ No                         │ ✓ Yes                        │
 * │ Fails if dir exists         │ ✓ Yes (FileAlreadyExists)    │ ✗ No (silently skips)        │
 * │ Fails if parent missing     │ ✓ Yes (NoSuchFileException)  │ ✗ No (creates all parents)   │
 * │ Use case                    │ One dir, parent must exist   │ Full nested path              │
 * └─────────────────────────────┴──────────────────────────────┴──────────────────────────────┘
 * </pre>
 *
 * <h3>Working Directory</h3>
 * <p>All examples in this guide use {@code C:\examples\java\io} as the base directory.</p>
 *
 * <h3>Dependencies</h3>
 * <ul>
 *   <li>Java 7+ (NIO.2)</li>
 *   <li>Lombok ({@code @Slf4j})</li>
 * </ul>
 *
 * @author  desabisc
 * @version 1.0
 * @see     Files#createDirectory(Path, FileAttribute[])
 * @see     Files#createDirectories(Path, FileAttribute[])
 * @see     Path
 */
@Slf4j
public class CreateDirectoriesGuide {

    /** Base working directory for all examples in this guide. */
    private static final Path BASE_DIR = Paths.get("C:\\examples\\java\\io");

    // ─────────────────────────────────────────────────────────────────────────
    // Entry Point
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Entry point. Runs all demonstration methods in sequence so you can read
     * the log output and understand the behavior of each edge case.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        CreateDirectoriesGuide guide = new CreateDirectoriesGuide();

        log.info("========================================================");
        log.info("  NIO.2 Guide – createDirectory & createDirectories");
        log.info("  Working directory: {}", BASE_DIR);
        log.info("========================================================");

        // ── createDirectory demos ──────────────────────────────────────────
        guide.demoCreateDirectorySuccess();
        guide.demoCreateDirectoryAlreadyExists();
        guide.demoCreateDirectoryParentMissing();
        guide.demoCreateDirectoryWithInvalidName();

        // ── createDirectories demos ────────────────────────────────────────
        guide.demoCreateDirectoriesNestedPath();
        guide.demoCreateDirectoriesAlreadyExists();
        guide.demoCreateDirectoriesDeepNesting();

        // ── Shared edge cases ──────────────────────────────────────────────
        guide.demoCreateDirectoryOnExistingFile();
        guide.demoCreateDirectoriesOnExistingFile();
        guide.demoPathWithSpecialCharacters();
        guide.demoReturnValueUsage();
        guide.demoConditionalCreation();

        log.info("========================================================");
        log.info("  Guide complete.");
        log.info("========================================================");
    }

    // =========================================================================
    // createDirectory() -> Single-Level Directory Creation
    // =========================================================================

    /**
     * <h3>Happy Path – {@code createDirectory()} with an existing parent</h3>
     *
     * <p>{@link Files#createDirectory(Path, FileAttribute[])} creates <strong>exactly one</strong>
     * new directory. The parent directory <strong>must</strong> already exist; otherwise a
     * {@link NoSuchFileException} is thrown (see {@link #demoCreateDirectoryParentMissing()}).</p>
     *
     * <p><b>Use this method when:</b></p>
     * <ul>
     *   <li>You are certain the parent directory already exists.</li>
     *   <li>You want the operation to <em>fail</em> if the directory already exists
     *       (strict creation semantics).</li>
     * </ul>
     *
     * <p><b>Expected path created:</b> {@code C:\examples\java\io\output}</p>
     *
     * <pre>
     *  C:\examples\java\io\          ← must exist (parent)
     *  └── output\                   ← created by this call
     * </pre>
     *
     * <p><b>Equivalent shell command:</b></p>
     * <pre>{@code mkdir C:\examples\java\io\output}</pre>
     */
    private void demoCreateDirectorySuccess() {
        log.info("--- [createDirectory] Happy Path ---");

        Path target = BASE_DIR.resolve("output");

        try {
            Path created = Files.createDirectory(target);
            log.info("Directory created successfully: {}", created.toAbsolutePath());
            log.info("Is directory? {}", Files.isDirectory(created));
        } catch (IOException e) {
            log.error("Failed to create directory '{}': {}", target, e.getMessage());
        }
    }

    /**
     * <h3>Edge Case – {@code createDirectory()} when the directory already exists</h3>
     *
     * <p>If the target path already exists (whether as a directory or a file),
     * {@link Files#createDirectory(Path, FileAttribute[])} throws
     * {@link FileAlreadyExistsException}. This is by design: the method promises
     * <em>atomic creation</em>, not idempotency.</p>
     *
     * <p><b>Key insight:</b> Unlike shell {@code mkdir}, which silently succeeds when the
     * directory already exists, {@code createDirectory()} is strict. This makes it useful
     * as a lightweight <em>mutex</em> -> only one thread/process can create the directory.</p>
     *
     * <p><b>How to handle it:</b></p>
     * <ul>
     *   <li>Catch {@link FileAlreadyExistsException} and treat it as a no-op if idempotency
     *       is acceptable.</li>
     *   <li>Use {@link Files#exists(Path, java.nio.file.LinkOption...)} before calling, but be
     *       aware of TOCTOU (time-of-check/time-of-use) race conditions in multi-threaded
     *       contexts.</li>
     * </ul>
     *
     * <pre>
     *  Scenario:
     *    C:\examples\java\io\output\  ← already exists
     *    → createDirectory(output)    ← throws FileAlreadyExistsException
     * </pre>
     */
    private void demoCreateDirectoryAlreadyExists() {
        log.info("--- [createDirectory] Edge Case: Directory already exists ---");

        // Assume "output" was created in the previous demo
        Path target = BASE_DIR.resolve("output");

        try {
            Files.createDirectory(target);
            log.info("Directory created: {}", target);
        } catch (FileAlreadyExistsException e) {
            // This is the expected exception -> NOT a generic IOException
            log.warn("Cannot create '{}' -> it already exists. Caught: {}",
                    target.getFileName(), e.getClass().getSimpleName());
            log.info("Tip: Use Files.exists(path) to check before creating, or catch this "
                    + "exception explicitly and treat it as acceptable.");
        } catch (IOException e) {
            log.error("Unexpected IO error: {}", e.getMessage());
        }
    }

    /**
     * <h3>Edge Case – {@code createDirectory()} when the parent directory is missing</h3>
     *
     * <p>{@link Files#createDirectory(Path, FileAttribute[])} does <strong>not</strong> create
     * intermediate (parent) directories. If any ancestor in the path does not exist, a
     * {@link NoSuchFileException} is thrown immediately.</p>
     *
     * <p><b>Contrast with {@code createDirectories()}:</b>
     * {@link Files#createDirectories(Path, FileAttribute[])} would succeed in this same scenario
     * because it creates all missing ancestors automatically.</p>
     *
     * <pre>
     *  Scenario:
     *    C:\examples\java\io\        ← exists
     *    C:\examples\java\io\a\      ← MISSING (parent of "b")
     *    → createDirectory(a\b)      ← throws NoSuchFileException
     *
     *  Fix: use createDirectories(a\b) instead.
     * </pre>
     *
     * @see #demoCreateDirectoriesNestedPath()
     */
    private void demoCreateDirectoryParentMissing() {
        log.info("--- [createDirectory] Edge Case: Parent directory missing ---");

        // "missing-parent" does not exist, so "child" cannot be created
        Path target = BASE_DIR.resolve("missing-parent").resolve("child");

        try {
            Files.createDirectory(target);
            log.info("Directory created: {}", target);
        } catch (NoSuchFileException e) {
            log.warn("Cannot create '{}' -> parent path does not exist. Caught: {}",
                    target, e.getClass().getSimpleName());
            log.info("Fix: call Files.createDirectories(path) to create all missing parents.");
        } catch (IOException e) {
            log.error("Unexpected IO error: {}", e.getMessage());
        }
    }

    /**
     * <h3>Edge Case – {@code createDirectory()} with an invalid directory name</h3>
     *
     * <p>On Windows, certain characters are forbidden in file and directory names:
     * {@code \ / : * ? " < > |}. Attempting to create a path containing these characters
     * causes an {@link IOException} (often wrapped as an
     * {@code InvalidPathException} at path construction time, before the call even reaches
     * the filesystem).</p>
     *
     * <p><b>Cross-platform note:</b> On Linux/macOS only {@code /} and the null byte are
     * forbidden. Windows is far more restrictive. When writing portable code, validate
     * directory names against the target OS's rules or use a sanitisation utility.</p>
     *
     * <pre>
     *  Windows forbidden characters: \ / : * ? " &lt; &gt; |
     *  Example invalid name:  "my:folder"  → InvalidPathException at Paths.get()
     * </pre>
     */
    private void demoCreateDirectoryWithInvalidName() {
        log.info("--- [createDirectory] Edge Case: Invalid directory name (Windows) ---");

        // Colon (':') is illegal in Windows path components (after the drive letter)
        try {
            Path target = BASE_DIR.resolve("my:folder");
            Files.createDirectory(target);
            log.info("Directory created: {}", target);
        } catch (java.nio.file.InvalidPathException e) {
            log.warn("Path rejected by the OS before reaching the filesystem. "
                    + "Invalid character detected. Caught: {}", e.getClass().getSimpleName());
            log.info("Tip: Sanitise user-supplied names -> strip or replace forbidden characters.");
        } catch (IOException e) {
            log.error("IO error while creating directory: {}", e.getMessage());
        }
    }

    // =========================================================================
    // createDirectories() -> Multi-Level / Recursive Directory Creation
    // =========================================================================

    /**
     * <h3>Happy Path – {@code createDirectories()} with a nested (non-existent) path</h3>
     *
     * <p>{@link Files#createDirectories(Path, FileAttribute[])} is the NIO.2 equivalent of
     * shell {@code mkdir -p}. It traverses the path from top to bottom and creates
     * <em>every missing segment</em>, including all intermediate directories.</p>
     *
     * <p><b>Use this method when:</b></p>
     * <ul>
     *   <li>You need to ensure a full nested path exists before writing a file.</li>
     *   <li>You do not know which intermediate directories are missing.</li>
     *   <li>Idempotency is required (calling it multiple times is safe).</li>
     * </ul>
     *
     * <p><b>Expected path created:</b></p>
     * <pre>
     *  C:\examples\java\io\
     *  └── reports\                  ← created (1st segment)
     *      └── 2024\                 ← created (2nd segment)
     *          └── quarterly\        ← created (3rd segment)
     * </pre>
     *
     * <p><b>Equivalent shell command:</b></p>
     * <pre>{@code mkdir -p C:\examples\java\io\reports\2024\quarterly}</pre>
     */
    private void demoCreateDirectoriesNestedPath() {
        log.info("--- [createDirectories] Happy Path: Full nested path ---");

        Path target = BASE_DIR.resolve("reports").resolve("2024").resolve("quarterly");

        try {
            Path created = Files.createDirectories(target);
            log.info("All directories created successfully: {}", created.toAbsolutePath());
            log.info("Is directory? {}", Files.isDirectory(created));
        } catch (IOException e) {
            log.error("Failed to create directory tree '{}': {}", target, e.getMessage());
        }
    }

    /**
     * <h3>Edge Case – {@code createDirectories()} when the directory already exists</h3>
     *
     * <p>Unlike {@link Files#createDirectory(Path, FileAttribute[])},
     * {@link Files#createDirectories(Path, FileAttribute[])} does <strong>not</strong> throw
     * {@link FileAlreadyExistsException} when the target already exists as a directory.
     * It simply returns the path silently, making the method <em>idempotent</em>.</p>
     *
     * <p><b>Idempotency</b> means you can call {@code createDirectories()} unconditionally
     * before every file write without wrapping it in an existence check. This is the
     * recommended pattern for ensuring a directory exists:</p>
     *
     * <pre>{@code
     *  // Safe, idempotent -> no existence check needed
     *  Files.createDirectories(outputDir);
     *  Files.writeString(outputDir.resolve("result.txt"), data);
     * }</pre>
     *
     * <p><b>Exception:</b> If the path exists but is a <em>regular file</em> (not a directory),
     * the method <em>does</em> throw {@link FileAlreadyExistsException}. See
     * {@link #demoCreateDirectoriesOnExistingFile()}.</p>
     */
    private void demoCreateDirectoriesAlreadyExists() {
        log.info("--- [createDirectories] Edge Case: Directory already exists (idempotent) ---");

        // "reports/2024/quarterly" was created in the previous demo
        Path target = BASE_DIR.resolve("reports").resolve("2024").resolve("quarterly");

        try {
            Path result = Files.createDirectories(target);
            // No exception -> the call is a no-op when the directory already exists
            log.info("createDirectories() succeeded silently. Path already existed: {}", result);
            log.info("This is the idempotent behaviour -> safe to call multiple times.");
        } catch (FileAlreadyExistsException e) {
            log.warn("Unexpected: path exists as a file, not a directory. Caught: {}",
                    e.getClass().getSimpleName());
        } catch (IOException e) {
            log.error("Unexpected IO error: {}", e.getMessage());
        }
    }

    /**
     * <h3>Edge Case – {@code createDirectories()} with a deeply nested path</h3>
     *
     * <p>There is no built-in depth limit enforced by NIO.2, but the underlying OS and
     * filesystem impose their own constraints (e.g., {@code MAX_PATH = 260} characters on
     * Windows without long-path support enabled).</p>
     *
     * <p><b>Windows long-path support:</b> Since Windows 10 (version 1607) you can opt in to
     * paths longer than 260 characters by setting the registry key
     * {@code HKLM\SYSTEM\CurrentControlSet\Control\FileSystem\LongPathsEnabled = 1} and adding
     * {@code <longPathAware>true</longPathAware>} to your application manifest.</p>
     *
     * <p>This demo creates a 5-level deep structure under the base directory:</p>
     * <pre>
     *  C:\examples\java\io\
     *  └── deep\
     *      └── level-1\
     *          └── level-2\
     *              └── level-3\
     *                  └── level-4\
     * </pre>
     */
    private void demoCreateDirectoriesDeepNesting() {
        log.info("--- [createDirectories] Edge Case: Deeply nested path ---");

        Path target = BASE_DIR
                .resolve("deep")
                .resolve("level-1")
                .resolve("level-2")
                .resolve("level-3")
                .resolve("level-4");

        try {
            Files.createDirectories(target);
            log.info("Deep path created: {}", target.toAbsolutePath());
            log.info("Total path length (chars): {}", target.toAbsolutePath().toString().length());
        } catch (IOException e) {
            log.error("Failed to create deep path: {}. Possible cause: path length exceeds OS "
                    + "limit (260 chars on Windows without long-path support). Error: {}",
                    target, e.getMessage());
        }
    }

    // =========================================================================
    // Shared Edge Cases -> Both Methods
    // =========================================================================

    /**
     * <h3>Edge Case – {@code createDirectory()} when path exists as a regular file</h3>
     *
     * <p>If the target path already exists as a <em>regular file</em> (not a directory),
     * {@link Files#createDirectory(Path, FileAttribute[])} throws
     * {@link FileAlreadyExistsException}. You cannot overwrite a file with a directory using
     * this API.</p>
     *
     * <p><b>Resolution strategy:</b></p>
     * <ol>
     *   <li>Delete the conflicting file with {@link Files#delete(Path)}.</li>
     *   <li>Rename/move the file with {@link Files#move(Path, Path,
     *       java.nio.file.CopyOption...)} before creating the directory.</li>
     * </ol>
     *
     * <pre>
     *  Scenario:
     *    C:\examples\java\io\conflict.txt  ← regular file (already exists)
     *    → createDirectory(conflict.txt)   ← throws FileAlreadyExistsException
     * </pre>
     */
    private void demoCreateDirectoryOnExistingFile() {
        log.info("--- [createDirectory] Edge Case: Path exists as a file ---");

        Path conflictingFile = BASE_DIR.resolve("conflict.txt");

        try {
            // First, create a file at the target path to simulate the conflict
            if (!Files.exists(conflictingFile)) {
                Files.createFile(conflictingFile);
                log.info("Setup: created file '{}' to simulate conflict.", conflictingFile.getFileName());
            }

            // Now attempt to create a directory at the same path
            Files.createDirectory(conflictingFile);
            log.info("Directory created: {}", conflictingFile);

        } catch (FileAlreadyExistsException e) {
            log.warn("Cannot create directory -> '{}' already exists as a regular file. "
                    + "Caught: {}", conflictingFile.getFileName(), e.getClass().getSimpleName());
            log.info("Resolution: delete or move the file first, then call createDirectory().");
        } catch (IOException e) {
            log.error("Unexpected IO error: {}", e.getMessage());
        }
    }

    /**
     * <h3>Edge Case – {@code createDirectories()} when the leaf path exists as a regular file</h3>
     *
     * <p>{@link Files#createDirectories(Path, FileAttribute[])} is idempotent for directories,
     * but it is <em>not</em> idempotent when the leaf segment (the final component of the path)
     * exists as a regular file. In that case it throws {@link FileAlreadyExistsException}.</p>
     *
     * <p><b>Important distinction:</b> Only the <em>leaf</em> triggers the exception. If an
     * intermediate segment is a file (e.g., {@code C:\io\output} where {@code output} is a
     * file, not a directory), a different {@link IOException} is thrown because the traversal
     * fails trying to descend into a file.</p>
     *
     * <pre>
     *  Scenario A -> leaf is a file:
     *    C:\examples\java\io\leaf.txt     ← regular file
     *    → createDirectories(leaf.txt)    ← throws FileAlreadyExistsException
     *
     *  Scenario B -> intermediate is a file:
     *    C:\examples\java\io\blocker.txt  ← regular file (not a dir)
     *    → createDirectories(blocker\sub) ← throws IOException (not a directory)
     * </pre>
     */
    private void demoCreateDirectoriesOnExistingFile() {
        log.info("--- [createDirectories] Edge Case: Leaf path exists as a file ---");

        Path conflictingFile = BASE_DIR.resolve("leaf.txt");

        try {
            // Setup: ensure the file exists
            if (!Files.exists(conflictingFile)) {
                Files.createFile(conflictingFile);
                log.info("Setup: created file '{}' to simulate conflict.", conflictingFile.getFileName());
            }

            // Attempt to treat the file path as a directory
            Files.createDirectories(conflictingFile);
            log.info("Directories created: {}", conflictingFile);

        } catch (FileAlreadyExistsException e) {
            log.warn("createDirectories() failed -> leaf path '{}' is a regular file. "
                    + "Caught: {}", conflictingFile.getFileName(), e.getClass().getSimpleName());
            log.info("Tip: Verify that none of the path segments is an existing regular file "
                    + "before calling createDirectories().");
        } catch (IOException e) {
            log.error("Unexpected IO error: {}", e.getMessage());
        }
    }

    /**
     * <h3>Edge Case – Paths with spaces and Unicode characters</h3>
     *
     * <p>NIO.2 {@link Path} objects handle spaces and Unicode characters transparently on
     * modern JVMs (Java 11+, UTF-8 by default via {@code -Dfile.encoding=UTF-8} or Java 17+
     * {@code -Dstdout.encoding}). However, be aware of:</p>
     *
     * <ul>
     *   <li><b>Filesystem encoding:</b> Older NTFS/FAT32 setups may mis-encode non-ASCII names
     *       if the JVM's default charset is not UTF-8.</li>
     *   <li><b>Shell portability:</b> Generated curl/shell commands referencing the path must
     *       quote it properly (e.g., {@code "C:\examples\java\io\año 2024"}).</li>
     *   <li><b>Path.of() vs Paths.get():</b> Both are equivalent since Java 11; prefer the
     *       {@code Path.of()} factory method in modern code for brevity.</li>
     * </ul>
     *
     * <pre>
     *  Created paths:
     *    C:\examples\java\io\año 2024\
     *    C:\examples\java\io\año 2024\datos estadísticos\
     * </pre>
     */
    private void demoPathWithSpecialCharacters() {
        log.info("--- [createDirectories] Edge Case: Paths with spaces and Unicode ---");

        // Path with accented characters and spaces -> common in Spanish-locale environments
        Path target = BASE_DIR.resolve("año 2024").resolve("datos estadísticos");

        try {
            Files.createDirectories(target);
            log.info("Unicode path created successfully: {}", target.toAbsolutePath());
            log.info("Directory exists and is accessible: {}", Files.isDirectory(target));
        } catch (IOException e) {
            log.error("Failed to create Unicode path '{}': {}", target, e.getMessage());
            log.info("Tip: Ensure your JVM runs with -Dfile.encoding=UTF-8 on Windows.");
        }
    }

    /**
     * <h3>Return Value – Using the returned {@link Path} reference</h3>
     *
     * <p>Both {@link Files#createDirectory(Path, FileAttribute[])} and
     * {@link Files#createDirectories(Path, FileAttribute[])} return the {@link Path}
     * that was passed in (the same object, not a copy). The return value is primarily
     * useful for:</p>
     *
     * <ul>
     *   <li><b>Method chaining:</b> Immediately resolve a child path or pass to another
     *       method without storing intermediate variables.</li>
     *   <li><b>Clarity:</b> Explicitly capturing the result makes the intent clear in
     *       code review -> "I created this directory, and now I'm using it."</li>
     * </ul>
     *
     * <p><b>Chain example -> create directory and immediately write a file into it:</b></p>
     * <pre>{@code
     *  Path logFile = Files.createDirectories(BASE_DIR.resolve("logs"))
     *                      .resolve("app.log");
     *  Files.writeString(logFile, "Application started\n");
     * }</pre>
     *
     * <p>This demo demonstrates the chaining pattern directly.</p>
     */
    private void demoReturnValueUsage() {
        log.info("--- Return Value: Chaining the result of createDirectories() ---");

        try {
            // Chain: create directory → resolve child file → write to it
            Path logFile = Files.createDirectories(BASE_DIR.resolve("logs"))
                                .resolve("app.log");

            Files.writeString(logFile, "Guide demo started at " + java.time.LocalDateTime.now());

            log.info("Log file written via chaining: {}", logFile.toAbsolutePath());
            log.info("File size: {} bytes", Files.size(logFile));

        } catch (IOException e) {
            log.error("Chain operation failed: {}", e.getMessage());
        }
    }

    /**
     * <h3>Best Practice – Conditional directory creation pattern</h3>
     *
     * <p>A common pattern in production code is to create a directory only when it does not
     * yet exist, without letting {@link FileAlreadyExistsException} propagate as an error.
     * There are two idiomatic approaches:</p>
     *
     * <p><b>Approach A -> Use {@code createDirectories()} (preferred):</b><br>
     * Idempotent by design. No existence check required. Works for nested paths.</p>
     *
     * <pre>{@code
     *  Files.createDirectories(target); // safe to call unconditionally
     * }</pre>
     *
     * <p><b>Approach B -> Check-then-create with {@code createDirectory()} (use carefully):</b><br>
     * Explicit check with {@link Files#exists(Path, java.nio.file.LinkOption...)} before
     * calling. Subject to TOCTOU race conditions in concurrent contexts; prefer Approach A
     * unless you explicitly need single-level semantics.</p>
     *
     * <pre>{@code
     *  if (!Files.exists(target)) {
     *      Files.createDirectory(target);
     *  }
     * }</pre>
     *
     * <p><b>Approach C -> Catch-and-ignore {@code FileAlreadyExistsException}:</b><br>
     * Useful when you need {@code createDirectory()}'s single-level guarantee but also
     * want idempotency.</p>
     *
     * <pre>{@code
     *  try {
     *      Files.createDirectory(target);
     *  } catch (FileAlreadyExistsException ignored) {
     *      // acceptable -> directory already present
     *  }
     * }</pre>
     *
     * <p>This demo runs all three approaches against the same target path so you can observe
     * their behavior in sequence.</p>
     */
    private void demoConditionalCreation() {
        log.info("--- Best Practice: Conditional directory creation patterns ---");

        Path target = BASE_DIR.resolve("conditional-demo");

        // ── Approach A: createDirectories() -> always safe ─────────────────
        log.info("[Approach A] createDirectories() -> idempotent, no check needed.");
        try {
            Files.createDirectories(target);
            log.info("  Result: directory ensured at {}", target);
        } catch (IOException e) {
            log.error("  Approach A failed: {}", e.getMessage());
        }

        // ── Approach B: check-then-create ──────────────────────────────────
        log.info("[Approach B] Files.exists() guard before createDirectory().");
        try {
            if (!Files.exists(target)) {
                Files.createDirectory(target);
                log.info("  Result: directory created.");
            } else {
                log.info("  Result: directory already existed -> skipped creation.");
            }
        } catch (IOException e) {
            log.error("  Approach B failed: {}", e.getMessage());
        }

        // ── Approach C: catch-and-ignore ───────────────────────────────────
        log.info("[Approach C] createDirectory() + catch FileAlreadyExistsException.");
        try {
            Files.createDirectory(target);
            log.info("  Result: directory created on first call.");
        } catch (FileAlreadyExistsException ignored) {
            log.info("  Result: FileAlreadyExistsException suppressed -> directory already existed.");
        } catch (IOException e) {
            log.error("  Approach C failed: {}", e.getMessage());
        }

        log.info("Recommendation: prefer Approach A (createDirectories) in most production code.");
    }
}