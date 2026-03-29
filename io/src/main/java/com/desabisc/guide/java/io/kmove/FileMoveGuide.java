package com.desabisc.guide.java.io.kmove;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * <h2>Files.move() — Complete NIO.2 Guide</h2>
 *
 * <p>This class is a self-contained, runnable reference guide that demonstrates every
 * meaningful behavior of {@link Files#move(Path, Path, CopyOption...)} from the
 * Java NIO.2 API ({@code java.nio.file} package, introduced in Java 7).</p>
 *
 * <h3>What is {@code Files.move()}?</h3>
 * <p>{@code Files.move(source, target, options...)} attempts to move or rename the file
 * or directory at {@code source} so that it ends up at {@code target}.  "Move" here
 * means <em>one atomic logical operation</em> — the JVM delegates to the underlying OS
 * whenever possible, so an intra-filesystem move is typically just a metadata update
 * (like a shell {@code mv}) rather than a copy-then-delete.</p>
 *
 * <h3>Copy-options explained</h3>
 * <ul>
 *   <li>{@link StandardCopyOption#REPLACE_EXISTING} — if {@code target} already exists,
 *       overwrite it.  Without this flag the method throws
 *       {@link FileAlreadyExistsException}.</li>
 *   <li>{@link StandardCopyOption#ATOMIC_MOVE} — asks the OS to perform the move as a
 *       single, indivisible operation.  If the OS cannot guarantee atomicity it throws
 *       {@link AtomicMoveNotSupportedException}.  Useful for configuration hot-swaps.</li>
 * </ul>
 *
 * <h3>Working directory</h3>
 * <pre>
 *   C:\examples\java\io\moveeg\
 *   └── (all sub-folders are created by setUp())
 * </pre>
 *
 * <h3>How to run</h3>
 * <ol>
 *   <li>Call {@link #setUp()} once to create the required folder structure.</li>
 *   <li>Call the individual example methods in any order; each one is idempotent
 *       because {@code setUp()} can always be called again to reset state.</li>
 * </ol>
 *
 * <p><strong>Dependencies:</strong> Lombok (for {@code @Slf4j}), JDK 17+.</p>
 *
 * @author  desabisc
 * @version 1.0
 * @see     Files#move(Path, Path, CopyOption...)
 * @see     StandardCopyOption
 * @see     Path
 */
@Slf4j
public class FileMoveGuide {

    // -------------------------------------------------------------------------
    // Root working directory — every path in this guide lives under here.
    // -------------------------------------------------------------------------

    /** Root working directory for all examples. */
    private static final Path ROOT = Path.of("C:\\examples\\java\\io\\moveeg");

    // -------------------------------------------------------------------------
    // main
    // -------------------------------------------------------------------------

    /**
     * Entry point — runs every example in sequence.
     *
     * <p>A fresh call to {@link #setUp()} precedes the examples so the folder
     * structure is always in a known state when the program starts.</p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        FileMoveGuide guide = new FileMoveGuide();

        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║         Files.move() — NIO.2 Complete Guide          ║");
        log.info("╚══════════════════════════════════════════════════════╝");

        guide.setUp();

        // ── File operations ──────────────────────────────────────────────────
        guide.example01_moveFileToAnotherDirectory();
        guide.example02_renameFileInPlace();
        guide.example03_moveAndRenameFileSimultaneously();
        guide.example04_moveFileReplaceExisting();
        guide.example05_moveFileWithoutReplaceThrowsException();
        guide.example06_moveNonExistentFileThrowsException();
        guide.example07_moveFileToNonExistentDirectoryThrowsException();
        guide.example08_moveFileToSameLocationIsNoOp();
        guide.example09_moveFileAtomically();
        guide.example10_atomicMoveUnsupportedHandling();

        // ── Directory operations ─────────────────────────────────────────────
        guide.example11_moveEmptyDirectory();
        guide.example12_renameDirectoryInPlace();
        guide.example13_moveNonEmptyDirectoryIntraFilesystem();
        guide.example14_moveNonEmptyDirectoryReplaceExisting();
        guide.example15_moveDirectoryToNonExistentParentThrowsException();
        guide.example16_moveDirectoryWhenTargetIsNonEmptyDirectoryThrowsException();

        // ── Advanced / real-world patterns ───────────────────────────────────
        guide.example17_safeSwapViaAtomicMove();
        guide.example18_moveWithCustomFileNameTimestamp();

        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║                  All examples done.                  ║");
        log.info("╚══════════════════════════════════════════════════════╝");
    }

    // =========================================================================
    // setUp
    // =========================================================================

    /**
     * Creates the complete folder structure required by all examples.
     *
     * <p>This method is designed to be <em>idempotent</em>: it can be called
     * multiple times without error because it uses
     * {@link Files#createDirectories(Path, java.nio.file.attribute.FileAttribute[])}
     * (which does nothing if the directory already exists) and skips file
     * creation when the file is already present.</p>
     *
     * <p>Folder layout after this method returns:</p>
     * <pre>
     * C:\examples\java\io\moveeg\
     * ├── source\
     * │   ├── report.txt          ← plain source file used by multiple examples
     * │   ├── invoice.txt         ← file used for rename-in-place examples
     * │   ├── config.properties   ← file used for ATOMIC_MOVE examples
     * │   └── existing_target.txt ← pre-existing file used for REPLACE_EXISTING examples
     * ├── destination\            ← target directory for move operations
     * ├── backup\                 ← secondary target directory
     * ├── dir_source\             ← non-empty directory used in directory move examples
     * │   ├── child.txt
     * │   └── sub\
     * │       └── nested.txt
     * ├── dir_empty\              ← empty directory moved in example 11
     * └── atomic_staging\         ← staging area for atomic-swap examples
     *     └── new_config.properties
     * </pre>
     */
    private void setUp() {
        log.info("──────────────────────────────────────────────────────");
        log.info("[setUp] Initialising working directory structure …");

        try {
            // ── Directories ───────────────────────────────────────────────
            Path source        = ROOT.resolve("source");
            Path destination   = ROOT.resolve("destination");
            Path backup        = ROOT.resolve("backup");
            Path dirSource     = ROOT.resolve("dir_source");
            Path dirSourceSub  = dirSource.resolve("sub");
            Path dirEmpty      = ROOT.resolve("dir_empty");
            Path atomicStaging = ROOT.resolve("atomic_staging");

            for (Path dir : new Path[]{
                    source, destination, backup,
                    dirSource, dirSourceSub, dirEmpty, atomicStaging}) {
                Files.createDirectories(dir);
                log.info("[setUp]   created directory: {}", dir);
            }

            // ── Files ─────────────────────────────────────────────────────
            createFileIfAbsent(source.resolve("report.txt"),
                    "Q1 Sales Report\nTotal: $142,000\n");

            createFileIfAbsent(source.resolve("invoice.txt"),
                    "Invoice #0042\nAmount due: $500\n");

            createFileIfAbsent(source.resolve("config.properties"),
                    "app.env=production\napp.port=8080\n");

            createFileIfAbsent(source.resolve("existing_target.txt"),
                    "I am the pre-existing target file.\n");

            createFileIfAbsent(dirSource.resolve("child.txt"),
                    "child file inside dir_source\n");

            createFileIfAbsent(dirSource.resolve("sub").resolve("nested.txt"),
                    "nested file inside dir_source/sub\n");

            createFileIfAbsent(atomicStaging.resolve("new_config.properties"),
                    "app.env=production\napp.port=9090\n");

            // Pre-place a target file in /destination for the REPLACE_EXISTING example
            createFileIfAbsent(destination.resolve("existing_target.txt"),
                    "I was already in /destination — I should be overwritten.\n");

            log.info("[setUp] ✔ Setup complete. Root = {}", ROOT);

        } catch (IOException e) {
            log.error("[setUp] ✘ Failed to create folder structure: {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    // =========================================================================
    // FILE examples (01 – 10)
    // =========================================================================

    /**
     * <h3>Example 01 — Move a file to another directory (no rename)</h3>
     *
     * <p>The simplest use-case: relocate {@code report.txt} from {@code /source}
     * to {@code /destination} keeping the same file name.</p>
     *
     * <p>Key points:</p>
     * <ul>
     *   <li>The <em>target path must include the file name</em> — unlike a shell
     *       {@code mv}, NIO.2 does <strong>not</strong> automatically append the
     *       source name when the target is an existing directory.</li>
     *   <li>No {@link CopyOption}s are used; if the target already exists the
     *       method throws {@link FileAlreadyExistsException}.</li>
     * </ul>
     *
     * <p>After this method the file lives at
     * {@code …\moveeg\destination\report.txt}.</p>
     */
    private void example01_moveFileToAnotherDirectory() {
        log.info("── Example 01: Move file to another directory ──────────");

        Path source = ROOT.resolve("source").resolve("report.txt");
        // ✱ Target = destination directory + SAME file name
        Path target = ROOT.resolve("destination").resolve("report.txt");

        try {
            ensureFileExists(source, "Q1 Sales Report\nTotal: $142,000\n");

            log.info("[01] Moving: {} -> {}", source, target);
            Path result = Files.move(source, target);
            log.info("[01] ✔ Success. File now at: {}", result);
            log.info("[01]   source exists? {}", Files.exists(source));   // false
            log.info("[01]   target exists? {}", Files.exists(target));   // true

        } catch (IOException e) {
            log.error("[01] ✘ {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    /**
     * <h3>Example 02 — Rename a file in place</h3>
     *
     * <p>A rename is just a move where source and target share the <em>same
     * parent directory</em> but have different names.  From the OS's perspective
     * this is an extremely cheap directory-metadata update.</p>
     *
     * <p>Renaming {@code invoice.txt} -> {@code invoice_final.txt} inside
     * {@code /source}.</p>
     *
     * <p>After this method the file lives at
     * {@code …\moveeg\source\invoice_final.txt}.</p>
     */
    private void example02_renameFileInPlace() {
        log.info("── Example 02: Rename file in place ────────────────────");

        Path source = ROOT.resolve("source").resolve("invoice.txt");
        Path target = ROOT.resolve("source").resolve("invoice_final.txt");   // ← same dir, new name

        try {
            ensureFileExists(source, "Invoice #0042\nAmount due: $500\n");

            log.info("[02] Renaming: {} -> {}", source.getFileName(), target.getFileName());
            Files.move(source, target);
            log.info("[02] ✔ Renamed successfully.");
            log.info("[02]   Old name exists? {}", Files.exists(source));   // false
            log.info("[02]   New name exists? {}", Files.exists(target));   // true

        } catch (IOException e) {
            log.error("[02] ✘ {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    /**
     * <h3>Example 03 — Move AND rename in a single call</h3>
     *
     * <p>{@code Files.move()} happily changes both the parent directory and the
     * file name simultaneously.  There is no need for two separate operations.</p>
     *
     * <p>Moves {@code source/config.properties} -> {@code backup/config_bak.properties}
     * (different directory, different name).</p>
     */
    private void example03_moveAndRenameFileSimultaneously() {
        log.info("── Example 03: Move + rename in one call ───────────────");

        Path source = ROOT.resolve("source").resolve("config.properties");
        Path target = ROOT.resolve("backup").resolve("config_bak.properties");

        try {
            ensureFileExists(source, "app.env=production\napp.port=8080\n");

            log.info("[03] source : {}", source);
            log.info("[03] target : {}", target);
            Files.move(source, target);
            log.info("[03] ✔ Move + rename done in one Files.move() call.");

        } catch (IOException e) {
            log.error("[03] ✘ {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    /**
     * <h3>Example 04 — Move a file with {@code REPLACE_EXISTING}</h3>
     *
     * <p>Without this option, moving to a path that is already occupied throws
     * {@link FileAlreadyExistsException}.  {@link StandardCopyOption#REPLACE_EXISTING}
     * instructs the JVM to silently overwrite the target.</p>
     *
     * <p>Use case: nightly batch jobs that always produce a file with the same name
     * (e.g. {@code report.csv}) and overwrite the previous version.</p>
     *
     * <p>Moves {@code source/existing_target.txt} -> {@code destination/existing_target.txt}
     * even though the target already exists.</p>
     */
    private void example04_moveFileReplaceExisting() {
        log.info("── Example 04: REPLACE_EXISTING ────────────────────────");

        Path source = ROOT.resolve("source").resolve("existing_target.txt");
        Path target = ROOT.resolve("destination").resolve("existing_target.txt");

        try {
            // Ensure both exist so the conflict is real
            ensureFileExists(source, "NEW content — this should overwrite the old file.\n");
            ensureFileExists(target, "OLD content — should be replaced.\n");

            log.info("[04] Target already exists: {}", Files.exists(target));
            log.info("[04] Moving with REPLACE_EXISTING …");

            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

            log.info("[04] ✔ Overwrite succeeded.");
            log.info("[04]   Target content: {}", Files.readString(target));

        } catch (IOException e) {
            log.error("[04] ✘ {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    /**
     * <h3>Example 05 — Move without {@code REPLACE_EXISTING} throws an exception</h3>
     *
     * <p>This is the <em>default-fail-fast</em> behaviour: if the caller did not
     * explicitly request an overwrite and the target path is occupied,
     * {@link Files#move} throws {@link FileAlreadyExistsException}.</p>
     *
     * <p>This example intentionally triggers that exception and handles it
     * gracefully, illustrating the correct exception-handling pattern.</p>
     */
    private void example05_moveFileWithoutReplaceThrowsException() {
        log.info("── Example 05: FileAlreadyExistsException (no REPLACE) ─");

        Path source = ROOT.resolve("source").resolve("report.txt");
        Path target = ROOT.resolve("destination").resolve("report.txt");

        try {
            // Guarantee both paths are occupied
            ensureFileExists(source, "Source report\n");
            ensureFileExists(target, "Target report — already here!\n");

            log.info("[05] Attempting move WITHOUT REPLACE_EXISTING …");
            Files.move(source, target);   // ← no options -> expected to throw

            log.warn("[05] ✘ Expected exception was NOT thrown!");

        } catch (FileAlreadyExistsException e) {
            // ✔ This is the expected outcome
            log.info("[05] ✔ Caught FileAlreadyExistsException as expected.");
            log.info("[05]   Conflicting path: {}", e.getFile());
            log.info("[05]   Tip: pass StandardCopyOption.REPLACE_EXISTING to overwrite.");

        } catch (IOException e) {
            log.error("[05] ✘ Unexpected IOException: {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    /**
     * <h3>Example 06 — Moving a non-existent file throws {@link NoSuchFileException}</h3>
     *
     * <p>{@code Files.move()} always validates that the source exists before
     * attempting the operation.  Passing a path that does not exist triggers
     * {@link NoSuchFileException}.</p>
     *
     * <p>Edge case relevance: this is easy to hit in concurrent applications where
     * another thread or process deletes the file between the existence-check and the
     * move call — always handle {@link NoSuchFileException} when the source can
     * disappear concurrently.</p>
     */
    private void example06_moveNonExistentFileThrowsException() {
        log.info("── Example 06: NoSuchFileException (missing source) ────");

        Path ghost  = ROOT.resolve("source").resolve("ghost_file.txt");   // does not exist
        Path target = ROOT.resolve("destination").resolve("ghost_file.txt");

        try {
            log.info("[06] source exists? {}", Files.exists(ghost));   // false
            log.info("[06] Attempting to move non-existent file …");
            Files.move(ghost, target);

            log.warn("[06] ✘ Expected exception was NOT thrown!");

        } catch (NoSuchFileException e) {
            log.info("[06] ✔ Caught NoSuchFileException: missing path -> {}", e.getFile());

        } catch (IOException e) {
            log.error("[06] ✘ Unexpected IOException: {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    /**
     * <h3>Example 07 — Moving to a non-existent parent directory throws an exception</h3>
     *
     * <p>{@code Files.move()} will <strong>not</strong> create missing intermediate
     * directories.  If the parent of the target path does not exist, the operation
     * throws {@link NoSuchFileException}.</p>
     *
     * <p>The fix is to call {@link Files#createDirectories(Path,
     * java.nio.file.attribute.FileAttribute[])} on the target's parent <em>before</em>
     * invoking {@code Files.move()}.</p>
     */
    private void example07_moveFileToNonExistentDirectoryThrowsException() {
        log.info("── Example 07: Target parent dir missing -> exception ───");

        Path source = ROOT.resolve("source").resolve("report.txt");
        // ✱ "nowhere" does not exist on disk
        Path target = ROOT.resolve("nowhere").resolve("report.txt");

        try {
            ensureFileExists(source, "Report content\n");

            log.info("[07] Target parent exists? {}", Files.exists(target.getParent()));   // false
            log.info("[07] Attempting move to non-existent directory …");
            Files.move(source, target);

            log.warn("[07] ✘ Expected exception was NOT thrown!");

        } catch (NoSuchFileException e) {
            log.info("[07] ✔ Caught NoSuchFileException — parent dir is missing.");
            log.info("[07]   Fix: call Files.createDirectories(target.getParent()) first.");

        } catch (IOException e) {
            log.error("[07] ✘ Unexpected IOException: {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    /**
     * <h3>Example 08 — Moving a file to its current location is a no-op</h3>
     *
     * <p>When source and target resolve to the <em>exact same</em> path,
     * {@code Files.move()} succeeds silently and returns the target path without
     * modifying anything.  The JVM spec mandates this behaviour (JEP 203).</p>
     *
     * <p>This edge case is easy to overlook in code that computes paths
     * dynamically — for example, when a user picks "current directory" as the
     * destination in a GUI.</p>
     */
    private void example08_moveFileToSameLocationIsNoOp() {
        log.info("── Example 08: Source == target -> no-op ────────────────");

        Path source = ROOT.resolve("source").resolve("report.txt");

        try {
            ensureFileExists(source, "Report content\n");

            log.info("[08] source : {}", source);
            log.info("[08] target : {} (same path)", source);

            Path result = Files.move(source, source);   // source == target

            log.info("[08] ✔ No-op completed. Returned path: {}", result);
            log.info("[08]   File still exists: {}", Files.exists(source));

        } catch (IOException e) {
            log.error("[08] ✘ {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    /**
     * <h3>Example 09 — Atomic move with {@code ATOMIC_MOVE}</h3>
     *
     * <p>{@link StandardCopyOption#ATOMIC_MOVE} instructs the JVM to delegate the
     * move to the OS as a single, indivisible operation.  Other processes watching
     * the target path will see either the old file or the new one — never a
     * partially-written intermediate state.</p>
     *
     * <p>This is the standard pattern for <em>configuration hot-swap</em>:
     * write the new config to a temp file in the same filesystem, then atomically
     * replace the live config with a single rename.</p>
     *
     * <p>Constraints:</p>
     * <ul>
     *   <li>Source and target <strong>must</strong> be on the same filesystem
     *       (same drive letter on Windows, same mount point on Linux/macOS).</li>
     *   <li>Combining {@code ATOMIC_MOVE} with {@code REPLACE_EXISTING} is
     *       implementation-defined; on most JVMs it works, but the spec does not
     *       guarantee it.</li>
     * </ul>
     */
    private void example09_moveFileAtomically() {
        log.info("── Example 09: ATOMIC_MOVE (config hot-swap) ───────────");

        // Staging area must be on the SAME filesystem as the live config
        Path stagingNewConfig = ROOT.resolve("atomic_staging").resolve("new_config.properties");
        Path liveConfig       = ROOT.resolve("source").resolve("config.properties");

        try {
            ensureFileExists(stagingNewConfig, "app.env=production\napp.port=9090\n");
            ensureFileExists(liveConfig,       "app.env=production\napp.port=8080\n");

            log.info("[09] Live config BEFORE: {}", Files.readString(liveConfig).strip());

            Files.move(stagingNewConfig, liveConfig,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);

            log.info("[09] ✔ Atomic swap done.");
            log.info("[09] Live config AFTER : {}", Files.readString(liveConfig).strip());

        } catch (AtomicMoveNotSupportedException e) {
            // Cross-filesystem or OS limitation -> degrade gracefully
            log.warn("[09] ⚠ ATOMIC_MOVE not supported on this filesystem: {}", e.getMessage());
            log.warn("[09]   Consider using a non-atomic REPLACE_EXISTING fallback.");

        } catch (IOException e) {
            log.error("[09] ✘ {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    /**
     * <h3>Example 10 — Graceful fallback when {@code ATOMIC_MOVE} is unsupported</h3>
     *
     * <p>Cross-filesystem moves (e.g. from a network share to a local drive, or from
     * a RAM disk to an SSD) cannot be atomic.  The JVM throws
     * {@link AtomicMoveNotSupportedException} to signal this.</p>
     *
     * <p>The recommended fallback is a <em>non-atomic</em> move with
     * {@code REPLACE_EXISTING}.  You should then document in your system design
     * that there is a brief window during which the target might be unavailable.</p>
     *
     * <p>This example simulates the exception by catching and logging it,
     * then executing the fallback path.</p>
     */
    private void example10_atomicMoveUnsupportedHandling() {
        log.info("── Example 10: ATOMIC_MOVE fallback pattern ────────────");

        Path source = ROOT.resolve("atomic_staging").resolve("new_config.properties");
        Path target = ROOT.resolve("source").resolve("config.properties");

        try {
            ensureFileExists(source, "app.env=production\napp.port=9090\n");

            try {
                log.info("[10] Trying ATOMIC_MOVE first …");
                Files.move(source, target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
                log.info("[10] ✔ Atomic move succeeded.");

            } catch (AtomicMoveNotSupportedException e) {
                log.warn("[10] ⚠ ATOMIC_MOVE unsupported — degrading to non-atomic move.");
                log.warn("[10]   Reason: {}", e.getMessage());

                // Fallback: non-atomic but still safe for most use-cases
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                log.info("[10] ✔ Non-atomic fallback move completed.");
            }

        } catch (IOException e) {
            log.error("[10] ✘ Move failed entirely: {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    // =========================================================================
    // DIRECTORY examples (11 – 16)
    // =========================================================================

    /**
     * <h3>Example 11 — Move an empty directory</h3>
     *
     * <p>Moving an empty directory works identically to moving a regular file:
     * the OS renames the directory entry.  It is fast and does not touch the
     * contents (there are none).</p>
     *
     * <p>Moves {@code dir_empty\} -> {@code destination\dir_empty_moved\}.</p>
     */
    private void example11_moveEmptyDirectory() {
        log.info("── Example 11: Move empty directory ────────────────────");

        Path source = ROOT.resolve("dir_empty");
        Path target = ROOT.resolve("destination").resolve("dir_empty_moved");

        try {
            Files.createDirectories(source);   // idempotent
            // Guarantee it is empty
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(source)) {
                for (Path entry : stream) {
                    Files.deleteIfExists(entry);
                }
            }

            log.info("[11] source is empty? {}", isDirEmpty(source));

            if (Files.exists(target)) {
                Files.delete(target);   // reset from previous run
            }

            Files.move(source, target);
            log.info("[11] ✔ Empty directory moved to: {}", target);
            log.info("[11]   source exists? {}", Files.exists(source));
            log.info("[11]   target is dir? {}", Files.isDirectory(target));

        } catch (IOException e) {
            log.error("[11] ✘ {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    /**
     * <h3>Example 12 — Rename a directory in place</h3>
     *
     * <p>Exactly like renaming a file: same parent, new name.  The OS performs a
     * metadata-only update — on most filesystems this is {@code O(1)} regardless
     * of how many files are inside the directory.</p>
     *
     * <p>Renames {@code dir_source\} -> {@code dir_source_renamed\}.</p>
     */
    private void example12_renameDirectoryInPlace() {
        log.info("── Example 12: Rename directory in place ───────────────");

        Path source = ROOT.resolve("dir_source");
        Path target = ROOT.resolve("dir_source_renamed");

        try {
            Files.createDirectories(source.resolve("sub"));
            ensureFileExists(source.resolve("child.txt"), "child\n");
            ensureFileExists(source.resolve("sub").resolve("nested.txt"), "nested\n");

            if (Files.exists(target)) {
                deleteRecursively(target);
            }

            log.info("[12] Renaming directory: {} -> {}", source.getFileName(), target.getFileName());
            Files.move(source, target);
            log.info("[12] ✔ Directory renamed.");
            log.info("[12]   Old name exists? {}", Files.exists(source));
            log.info("[12]   New name exists? {}", Files.exists(target));
            log.info("[12]   Child still there? {}",
                    Files.exists(target.resolve("child.txt")));

            // Rename back so other examples can still use dir_source
            Files.move(target, source);
            log.info("[12]   (restored to dir_source for subsequent examples)");

        } catch (IOException e) {
            log.error("[12] ✘ {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    /**
     * <h3>Example 13 — Move a non-empty directory (intra-filesystem)</h3>
     *
     * <p>When source and target are on the <em>same filesystem</em>, the OS can
     * move a non-empty directory as a single rename — all child entries come
     * along for free without being individually touched.</p>
     *
     * <p>⚠ If source and target are on <em>different</em> filesystems (e.g. C: -> D:),
     * the JVM throws {@link IOException} because cross-filesystem directory moves
     * require a recursive copy-then-delete, which is not what {@code Files.move()}
     * does.  Use a custom recursive walk in that case (see the Javadoc for
     * {@link Files#walkFileTree}).</p>
     *
     * <p>Moves {@code dir_source\} (with children) -> {@code destination\dir_source_moved\}.</p>
     */
    private void example13_moveNonEmptyDirectoryIntraFilesystem() {
        log.info("── Example 13: Move non-empty directory (same FS) ──────");

        Path source = ROOT.resolve("dir_source");
        Path target = ROOT.resolve("destination").resolve("dir_source_moved");

        try {
            // Ensure source is populated
            Files.createDirectories(source.resolve("sub"));
            ensureFileExists(source.resolve("child.txt"), "child\n");
            ensureFileExists(source.resolve("sub").resolve("nested.txt"), "nested\n");

            if (Files.exists(target)) {
                deleteRecursively(target);
            }

            log.info("[13] Moving non-empty directory …");
            Files.move(source, target);
            log.info("[13] ✔ Directory moved.");
            log.info("[13]   target/child.txt exists?        {}",
                    Files.exists(target.resolve("child.txt")));
            log.info("[13]   target/sub/nested.txt exists?   {}",
                    Files.exists(target.resolve("sub").resolve("nested.txt")));

            // Restore dir_source for later examples
            Files.move(target, source);
            log.info("[13]   (dir_source restored)");

        } catch (IOException e) {
            log.error("[13] ✘ {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    /**
     * <h3>Example 14 — Move a directory with {@code REPLACE_EXISTING}</h3>
     *
     * <p>When moving a <em>directory</em> to a target that is also a directory,
     * {@code REPLACE_EXISTING} only works if the target directory is <strong>empty</strong>.
     * If the target contains any entries, the OS refuses to overwrite it and
     * {@link DirectoryNotEmptyException} is thrown even with {@code REPLACE_EXISTING}.</p>
     *
     * <p>This example shows the happy path (target is empty) and documents the
     * non-empty target constraint.</p>
     */
    private void example14_moveNonEmptyDirectoryReplaceExisting() {
        log.info("── Example 14: Directory REPLACE_EXISTING (empty target) ");

        Path source = ROOT.resolve("dir_source");
        Path target = ROOT.resolve("destination").resolve("dir_replace_target");

        try {
            Files.createDirectories(source.resolve("sub"));
            ensureFileExists(source.resolve("child.txt"), "child\n");

            // Target exists but is EMPTY -> REPLACE_EXISTING succeeds
            Files.createDirectories(target);
            log.info("[14] Target is empty dir: {}", isDirEmpty(target));

            log.info("[14] Moving with REPLACE_EXISTING …");
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("[14] ✔ Directory moved over the empty target.");
            log.info("[14]   target/child.txt exists? {}",
                    Files.exists(target.resolve("child.txt")));

            // Restore
            Files.move(target, source);
            log.info("[14]   (dir_source restored)");

        } catch (DirectoryNotEmptyException e) {
            log.warn("[14] ⚠ DirectoryNotEmptyException — target has entries.");
            log.warn("[14]   Tip: empty (or recursively delete) the target first.");

        } catch (IOException e) {
            log.error("[14] ✘ {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    /**
     * <h3>Example 15 — Move directory to a non-existent parent throws an exception</h3>
     *
     * <p>Just like files, {@code Files.move()} will <strong>not</strong> create missing
     * ancestor directories for the target.  The immediate parent of the target path
     * must already exist.</p>
     */
    private void example15_moveDirectoryToNonExistentParentThrowsException() {
        log.info("── Example 15: Missing target parent -> exception ───────");

        Path source = ROOT.resolve("dir_source");
        // ✱ "phantom_parent" does not exist
        Path target = ROOT.resolve("phantom_parent").resolve("dir_source");

        try {
            Files.createDirectories(source);

            log.info("[15] Target parent exists? {}", Files.exists(target.getParent()));
            Files.move(source, target);

            log.warn("[15] ✘ Expected exception was NOT thrown!");

        } catch (NoSuchFileException e) {
            log.info("[15] ✔ NoSuchFileException — target parent is missing.");
            log.info("[15]   Missing path: {}", e.getFile());
            log.info("[15]   Fix: Files.createDirectories(target.getParent()) first.");

        } catch (IOException e) {
            log.error("[15] ✘ Unexpected IOException: {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    /**
     * <h3>Example 16 — Moving a directory when target is a non-empty directory</h3>
     *
     * <p>{@link DirectoryNotEmptyException} is thrown when:</p>
     * <ul>
     *   <li>The target path is an existing directory that contains entries, AND</li>
     *   <li>{@code REPLACE_EXISTING} is set (if it were not set,
     *       {@link FileAlreadyExistsException} would be thrown instead).</li>
     * </ul>
     *
     * <p>The only correct fix is to empty or recursively delete the target before
     * calling {@code Files.move()}.</p>
     */
    private void example16_moveDirectoryWhenTargetIsNonEmptyDirectoryThrowsException() {
        log.info("── Example 16: DirectoryNotEmptyException ───────────────");

        Path source = ROOT.resolve("dir_source");
        Path target = ROOT.resolve("destination").resolve("non_empty_target");

        try {
            Files.createDirectories(source);
            ensureFileExists(source.resolve("child.txt"), "child\n");

            // Make target non-empty
            Files.createDirectories(target);
            ensureFileExists(target.resolve("occupant.txt"), "I am here!\n");

            log.info("[16] target is non-empty: {}", !isDirEmpty(target));
            log.info("[16] Attempting move with REPLACE_EXISTING …");
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

            log.warn("[16] ✘ Expected exception was NOT thrown!");

        } catch (DirectoryNotEmptyException e) {
            log.info("[16] ✔ DirectoryNotEmptyException — target dir is occupied.");
            log.info("[16]   Conflicting path: {}", e.getFile());
            log.info("[16]   Fix: recursively delete the target first, then move.");

        } catch (IOException e) {
            log.error("[16] ✘ Unexpected IOException: {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    // =========================================================================
    // Advanced / real-world patterns (17 – 18)
    // =========================================================================

    /**
     * <h3>Example 17 — Safe config swap via {@code ATOMIC_MOVE}</h3>
     *
     * <p>This is the canonical <em>write-then-swap</em> pattern used in production
     * systems to update a live configuration file without any consumer ever seeing
     * a half-written file:</p>
     *
     * <ol>
     *   <li>Write the new content to a <em>staging</em> (temp) file on the
     *       <strong>same filesystem</strong> as the live file.</li>
     *   <li>Atomically rename staging -> live, replacing the old content.</li>
     * </ol>
     *
     * <p>Because the rename is atomic, any reader that opens the live config will
     * either read the old version or the new version — never a mix.</p>
     *
     * <p>Implementation note: the staging file is placed in the same directory as
     * the target to guarantee same-filesystem placement (a temp file in the OS
     * temp directory might be on a different drive).</p>
     */
    private void example17_safeSwapViaAtomicMove() {
        log.info("── Example 17: Safe write-then-swap (production pattern) ");

        Path liveConfig = ROOT.resolve("source").resolve("config.properties");
        // Stage in the same directory as liveConfig to guarantee same filesystem
        Path stagingConfig = ROOT.resolve("source").resolve(".config.properties.tmp");

        try {
            ensureFileExists(liveConfig, "app.env=production\napp.port=8080\n");

            String newContent = "app.env=production\napp.port=9090\napp.feature.dark_mode=true\n";

            // Step 1 — Write new content to staging file
            Files.writeString(stagingConfig, newContent);
            log.info("[17] Staging file written: {}", stagingConfig.getFileName());

            // Step 2 — Atomic swap
            Files.move(stagingConfig, liveConfig,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);

            log.info("[17] ✔ Atomic swap complete.");
            log.info("[17]   Live config is now:\n{}", Files.readString(liveConfig));

        } catch (AtomicMoveNotSupportedException e) {
            log.warn("[17] ⚠ Atomic move not supported — using non-atomic fallback.");
            try {
                Files.move(ROOT.resolve("source").resolve(".config.properties.tmp"),
                        liveConfig, StandardCopyOption.REPLACE_EXISTING);
                log.info("[17] ✔ Non-atomic fallback completed.");
            } catch (IOException ex) {
                log.error("[17] ✘ Fallback also failed: {}", ex.getMessage(), ex);
            }

        } catch (IOException e) {
            log.error("[17] ✘ {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    /**
     * <h3>Example 18 — Move a file and rename it with a timestamp suffix</h3>
     *
     * <p>A common archival pattern: when a process finishes producing a file,
     * move it to an archive directory and append a timestamp to the name so that
     * multiple versions can coexist without overwriting each other.</p>
     *
     * <p>Example target name: {@code report_2024-07-15T10-30-00.txt}</p>
     *
     * <p>This example also demonstrates how to safely build a target name
     * programmatically using {@link Path#getFileName()} and string manipulation
     * without hard-coding anything.</p>
     */
    private void example18_moveWithCustomFileNameTimestamp() {
        log.info("── Example 18: Move + timestamp rename (archival pattern) ");

        Path source = ROOT.resolve("source").resolve("report.txt");
        Path archiveDir = ROOT.resolve("backup");

        try {
            ensureFileExists(source, "Final Q1 report\n");
            Files.createDirectories(archiveDir);

            // Build a timestamp-suffixed name
            String originalName = source.getFileName().toString();
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss"));

            int dotIndex = originalName.lastIndexOf('.');
            String archivedName = (dotIndex == -1)
                    ? originalName + "_" + timestamp
                    : originalName.substring(0, dotIndex) + "_" + timestamp
                    + originalName.substring(dotIndex);

            Path target = archiveDir.resolve(archivedName);

            log.info("[18] Source name : {}", originalName);
            log.info("[18] Archive name: {}", archivedName);

            Files.move(source, target);

            log.info("[18] ✔ Archived successfully: {}", target);

        } catch (IOException e) {
            log.error("[18] ✘ {}", e.getMessage(), e);
        }

        log.info("──────────────────────────────────────────────────────");
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Creates {@code file} with the given {@code content} if and only if the file
     * does not already exist.
     *
     * <p>Uses {@link Files#writeString(Path, CharSequence, OpenOption...)} with
     * {@link StandardOpenOption#CREATE_NEW} so the operation is atomic with respect
     * to file creation — it will not silently truncate an existing file.</p>
     *
     * @param file    path of the file to create
     * @param content initial text content written to the file
     * @throws IOException if an I/O error occurs
     */
    private void createFileIfAbsent(Path file, String content) throws IOException {
        if (!Files.exists(file)) {
            Files.writeString(file, content, StandardOpenOption.CREATE_NEW);
            log.debug("[helper] created file: {}", file);
        }
    }

    /**
     * Creates {@code file} with the given {@code content}, overwriting it if it
     * already exists.
     *
     * <p>Unlike {@link #createFileIfAbsent}, this method guarantees the file
     * exists with the exact content provided — useful inside individual examples
     * to reset a file to a known state before demonstrating behaviour.</p>
     *
     * @param file    path of the file to create or overwrite
     * @param content text content to write
     * @throws IOException if an I/O error occurs
     */
    private void ensureFileExists(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Returns {@code true} if the directory at {@code dir} contains no entries.
     *
     * <p>Uses a {@link DirectoryStream} and reads only the first entry to avoid
     * iterating over potentially large directories.</p>
     *
     * @param dir the directory to inspect
     * @return {@code true} if the directory is empty, {@code false} otherwise
     * @throws IOException if an I/O error occurs while opening the stream
     */
    private boolean isDirEmpty(Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            return !stream.iterator().hasNext();
        }
    }

    /**
     * Recursively deletes a directory tree rooted at {@code root}.
     *
     * <p>Uses {@link Files#walkFileTree(Path, FileVisitor)} with a
     * {@link SimpleFileVisitor} that deletes each file on
     * {@link SimpleFileVisitor#visitFile visitFile} and each (now-empty) directory
     * on {@link SimpleFileVisitor#postVisitDirectory postVisitDirectory}.</p>
     *
     * <p>This helper is used in examples to clean up target directories created
     * by previous test runs so each example starts from a known state.</p>
     *
     * @param root the directory tree to delete (must be a directory)
     * @throws IOException if any file or directory could not be deleted
     */
    private void deleteRecursively(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                if (exc != null) throw exc;
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}