package com.desabisc.guide.java.io.ldelete;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.List;

/**
 * <h2>Files.deleteIfExists(Path) — Exhaustive Edge-Case Examples</h2>
 *
 * <p>This class demonstrates every significant edge case of
 * {@link Files#deleteIfExists(Path)}, the <em>lenient</em> deletion method
 * introduced in Java 7 as part of the NIO.2 API ({@code java.nio.file}).</p>
 *
 * <h3>Contract summary</h3>
 * <pre>
 *  boolean Files.deleteIfExists(Path path) throws IOException
 * </pre>
 * <ul>
 *   <li>Deletes the file or <em>empty</em> directory at {@code path}.</li>
 *   <li>Returns {@code true} if the file was found and successfully deleted.</li>
 *   <li>Returns {@code false} if the file did <em>not</em> exist — no exception.</li>
 *   <li>Throws {@link DirectoryNotEmptyException} if {@code path} is a directory
 *       that still contains entries (same as {@link Files#delete(Path)}).</li>
 *   <li>Throws {@link AccessDeniedException} for permission failures.</li>
 *   <li>For symbolic links, deletes the <em>link itself</em>, never the target.</li>
 * </ul>
 *
 * <h3>When to choose {@code Files.deleteIfExists} over {@code Files.delete}</h3>
 * <ul>
 *   <li><strong>Idempotent cleanup</strong> — temp files, output artefacts, or
 *       any resource that may or may not have been created.</li>
 *   <li><strong>Retry logic</strong> — calling the same cleanup path multiple
 *       times must not throw on subsequent (no-op) invocations.</li>
 *   <li><strong>{@code finally} blocks</strong> — cleanup code that runs
 *       regardless of whether earlier steps succeeded.</li>
 *   <li><strong>Test teardown</strong> — {@code @AfterEach} methods that
 *       clean up files which tests may or may not have created.</li>
 * </ul>
 *
 * <h3>Working directory</h3>
 * <p>All examples operate under {@code C:\examples\java\io\deleteeg}. Call
 * {@link #setUp()} before running any example to guarantee the required folder
 * structure and fixture files exist.</p>
 *
 * @author  desabisc
 * @version 1.0
 * @see     Files#deleteIfExists(Path)
 * @see     FilesDeleteExamples
 */
@Slf4j
public class FilesDeleteIfExistsExamples {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    /** Root working directory for all examples in this class. */
    private static final Path ROOT =
            Paths.get("C:\\examples\\java\\io\\deleteeg\\deleteifexists");

    // ═══════════════════════════════════════════════════════════════════════
    // MAIN
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Entry point — runs all examples in sequence.
     *
     * <p>Each example is self-contained and logs its own header and result.
     * Failures in one example do not prevent subsequent examples from running.</p>
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        setUp();

        example01_deleteExistingFile_returnsTrue();
        example02_deleteNonExistentFile_returnsFalse();
        example03_deleteEmptyDirectory_returnsTrue();
        example04_deleteNonEmptyDirectory_throwsException();
        example05_deleteSymbolicLink();
        example06_deleteReadOnlyFile();
        example07_idempotentDeletion();
        example08_useReturnValueForConditionalLogic();
        example09_batchDeletion();
        example10_finallyBlockCleanupPattern();
        example11_recursiveTreeDeletion(ROOT.resolve("non-empty-dir"));

        log.info("═══════════════════════════════════════════════════════");
        log.info("  All FilesDeleteIfExistsExamples finished.");
        log.info("═══════════════════════════════════════════════════════");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 0 — SETUP
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Creates the complete folder structure and all fixture files required by
     * every example method in this class.
     *
     * <p>This method is <em>idempotent</em>: it is safe to call multiple times.
     * Any file or directory that already exists is simply skipped.</p>
     *
     * <p>Folder tree created under
     * {@code C:\examples\java\io\deleteeg\deleteifexists}:</p>
     * <pre>
     *  deleteifexists/
     *  ├── existing-file.txt         ← example 1: happy-path (returns true)
     *  ├── read-only-file.txt        ← example 6: read-only attribute set
     *  ├── locked-simulation.txt     ← example 7: simulated lock scenario
     *  ├── empty-dir/                ← example 3: empty directory (returns true)
     *  ├── non-empty-dir/            ← example 4: non-empty directory (exception)
     *  │   └── child.txt
     *  ├── idempotent-cleanup.tmp    ← example 8: safe multi-call pattern
     *  ├── batch-target-1.txt        ← example 9: batch deletion
     *  ├── batch-target-2.txt        ← example 9: batch deletion
     *  ├── batch-target-3.txt        ← example 9: batch deletion
     *  ├── symlink-target.txt        ← example 5: the actual target (kept alive)
     *  └── symlink-link.txt          ← example 5: the link itself (deleted)
     * </pre>
     *
     * <p><strong>Note on symbolic links:</strong> Creating symbolic links on
     * Windows requires either Developer Mode or Administrator privileges.
     * The symlink fixture is skipped gracefully if the privilege is absent.</p>
     */
    public static void setUp() {
        log.info("═══════════════════════════════════════════════════════");
        log.info("  setUp() — building fixture tree under: {}", ROOT);
        log.info("═══════════════════════════════════════════════════════");

        try {
            // Root
            Files.createDirectories(ROOT);

            // Regular file
            createIfAbsent(ROOT.resolve("existing-file.txt"),
                    "I exist — deleteIfExists will return true.");

            // Read-only file
            Path readOnly = ROOT.resolve("read-only-file.txt");
            if (Files.notExists(readOnly)) {
                Files.writeString(readOnly, "I am read-only.");
                DosFileAttributeView view =
                        Files.getFileAttributeView(readOnly, DosFileAttributeView.class);
                if (view != null) {
                    view.setReadOnly(true);
                }
                log.info("  [created + read-only] {}", readOnly);
            }

            // Locked-simulation file
            createIfAbsent(ROOT.resolve("locked-simulation.txt"),
                    "Simulating a locked file.");

            // Empty directory
            Files.createDirectories(ROOT.resolve("empty-dir"));
            log.info("  [created dir] {}", ROOT.resolve("empty-dir"));

            // Non-empty directory
            Path nonEmpty = ROOT.resolve("non-empty-dir");
            Files.createDirectories(nonEmpty);
            createIfAbsent(nonEmpty.resolve("child.txt"),
                    "I prevent my parent from being deleted.");

            // Idempotent cleanup temp file
            createIfAbsent(ROOT.resolve("idempotent-cleanup.tmp"),
                    "delete me once — or many times, safely.");

            // Batch targets
            for (int i = 1; i <= 3; i++) {
                createIfAbsent(ROOT.resolve("batch-target-" + i + ".txt"),
                        "Batch file " + i);
            }

            // Symbolic link (best-effort)
            Path target = ROOT.resolve("symlink-target.txt");
            Path link   = ROOT.resolve("symlink-link.txt");
            createIfAbsent(target, "I am the symlink target — I survive example 5.");
            if (Files.notExists(link)) {
                try {
                    Files.createSymbolicLink(link, target);
                    log.info("  [created symlink] {} → {}", link, target);
                } catch (UnsupportedOperationException | IOException e) {
                    log.warn("  [skip symlink] Requires Admin/Developer-Mode on Windows: {}",
                            e.getMessage());
                }
            }

            log.info("  setUp() complete.");

        } catch (IOException e) {
            log.error("setUp() failed: {}", e.getMessage(), e);
        }
    }

    // -----------------------------------------------------------------------
    // Private helper
    // -----------------------------------------------------------------------

    /**
     * Creates {@code path} with {@code content} only if it does not already exist.
     * Logs the outcome for traceability.
     *
     * @param path    the file to create
     * @param content the text to write
     * @throws IOException if the file cannot be created
     */
    private static void createIfAbsent(Path path, String content) throws IOException {
        if (Files.notExists(path)) {
            Files.writeString(path, content);
            log.info("  [created] {}", path);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 1 — HAPPY PATH: delete existing file → returns true
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * <h3>Example 1 — Deleting an existing file (returns {@code true})</h3>
     *
     * <p>This is the primary success case: the file exists, the caller has
     * permission, and {@code deleteIfExists} returns {@code true} to confirm
     * that a deletion actually occurred.</p>
     *
     * <h4>Key behaviour</h4>
     * <ul>
     *   <li>Return value {@code true} — the file existed and was removed.</li>
     *   <li>The method delegates to the file-system provider's
     *       {@code delete} syscall under the hood.</li>
     *   <li>Same atomicity guarantees as {@link Files#delete(Path)}.</li>
     * </ul>
     *
     * <h4>Best practice: always check the return value</h4>
     * <p>Unlike {@link Files#delete(Path)}, the return value carries information.
     * Ignoring it means missing the distinction between "deleted now" and
     * "was already gone" — which matters for audit logs and idempotency tracking.</p>
     */
    public static void example01_deleteExistingFile_returnsTrue() {
        log.info("───────────────────────────────────────────────────────");
        log.info("  Example 01 — delete existing file → returns true");
        log.info("───────────────────────────────────────────────────────");

        Path file = ROOT.resolve("existing-file.txt");
        log.info("  Target : {}", file);
        log.info("  Exists before? {}", Files.exists(file));

        try {
            boolean deleted = Files.deleteIfExists(file);
            log.info("  deleteIfExists() returned: {}", deleted);     // true
            log.info("  ✔ File existed → deleted  → returned true.");
            log.info("  Exists after?  {}", Files.exists(file));
        } catch (IOException e) {
            log.error("  ✘ Unexpected IOException: {}", e.getMessage(), e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2 — File does NOT exist → returns false (no exception)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * <h3>Example 2 — File does not exist (returns {@code false}, no exception)</h3>
     *
     * <p>This is the defining behaviour that distinguishes
     * {@link Files#deleteIfExists(Path)} from {@link Files#delete(Path)}.
     * When the target is absent, {@code deleteIfExists} simply returns
     * {@code false} — it does <em>not</em> throw {@link NoSuchFileException}.</p>
     *
     * <h4>Why this is the correct design</h4>
     * <p>In many cleanup scenarios, the goal is to ensure the file is gone after
     * the call — not to confirm it was present before the call. The boolean
     * return provides both pieces of information without forcing the caller to
     * use try/catch for the "file was already absent" branch.</p>
     *
     * <h4>Idempotency</h4>
     * <p>Calling {@code deleteIfExists} on a non-existent path is perfectly safe
     * and has no side effects. This makes it ideal for test teardown and
     * retry-safe cleanup logic.</p>
     */
    public static void example02_deleteNonExistentFile_returnsFalse() {
        log.info("───────────────────────────────────────────────────────");
        log.info("  Example 02 — delete non-existent file → returns false");
        log.info("───────────────────────────────────────────────────────");

        Path ghost = ROOT.resolve("this-file-was-never-created.txt");
        log.info("  Target : {}", ghost);
        log.info("  Exists? {}", Files.exists(ghost));

        try {
            boolean deleted = Files.deleteIfExists(ghost);
            log.info("  deleteIfExists() returned: {}", deleted);     // false
            log.info("  ✔ File was absent → returned false → NO NoSuchFileException.");
            log.info("  Lesson: Files.deleteIfExists() is safe when the file is absent.");
        } catch (NoSuchFileException e) {
            // This block should NEVER be reached — documented for clarity
            log.error("  ✘ Unexpected NoSuchFileException (should not happen): {}",
                    e.getMessage());
        } catch (IOException e) {
            log.error("  ✘ Unexpected IOException: {}", e.getMessage(), e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 3 — Delete an EMPTY directory → returns true
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * <h3>Example 3 — Deleting an empty directory (returns {@code true})</h3>
     *
     * <p>{@link Files#deleteIfExists(Path)} handles empty directories identically
     * to {@link Files#delete(Path)}: if the directory exists and is empty, it is
     * removed and the method returns {@code true}.</p>
     *
     * <h4>Key behaviour</h4>
     * <ul>
     *   <li>Works the same as for regular files.</li>
     *   <li>Returns {@code false} if the directory never existed.</li>
     *   <li>Throws {@link DirectoryNotEmptyException} if children are present.</li>
     * </ul>
     *
     * <h4>Use case</h4>
     * <p>Cleaning up temporary build output directories that may or may not have
     * been created by an earlier build step — {@code deleteIfExists} avoids
     * wrapping every cleanup in an existence check.</p>
     */
    public static void example03_deleteEmptyDirectory_returnsTrue() {
        log.info("───────────────────────────────────────────────────────");
        log.info("  Example 03 — delete an empty directory → returns true");
        log.info("───────────────────────────────────────────────────────");

        Path dir = ROOT.resolve("empty-dir");
        log.info("  Target     : {}", dir);
        log.info("  isDirectory: {}", Files.isDirectory(dir));

        try {
            boolean deleted = Files.deleteIfExists(dir);
            log.info("  deleteIfExists() returned: {}", deleted);
            log.info("  ✔ Empty directory removed successfully.");
            log.info("  Exists after? {}", Files.exists(dir));
        } catch (DirectoryNotEmptyException e) {
            log.error("  ✘ DirectoryNotEmptyException: {}", e.getMessage());
        } catch (IOException e) {
            log.error("  ✘ IOException: {}", e.getMessage(), e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4 — Non-empty directory → DirectoryNotEmptyException
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * <h3>Example 4 — Non-empty directory ({@link DirectoryNotEmptyException})</h3>
     *
     * <p>{@link Files#deleteIfExists(Path)} shares the same directory-not-empty
     * constraint as {@link Files#delete(Path)}. The lenient "if exists" contract
     * only applies to the <em>absence</em> case; a directory with children is
     * always an error condition.</p>
     *
     * <h4>Important distinction</h4>
     * <ul>
     *   <li>{@code deleteIfExists} suppresses {@link NoSuchFileException}
     *       (absent file → {@code false}).</li>
     *   <li>{@code deleteIfExists} does <strong>NOT</strong> suppress
     *       {@link DirectoryNotEmptyException} — it still propagates.</li>
     * </ul>
     *
     * <h4>Recursive deletion pattern</h4>
     * <pre>{@code
     * try (var stream = Files.walk(dir)) {
     *     stream.sorted(Comparator.reverseOrder())
     *           .forEach(p -> {
     *               try { Files.deleteIfExists(p); }
     *               catch (IOException ex) { throw new UncheckedIOException(ex); }
     *           });
     * }
     * }</pre>
     */
    public static void example04_deleteNonEmptyDirectory_throwsException() {
        log.info("───────────────────────────────────────────────────────");
        log.info("  Example 04 — non-empty directory → DirectoryNotEmptyException");
        log.info("───────────────────────────────────────────────────────");

        Path dir = ROOT.resolve("non-empty-dir");
        log.info("  Target     : {}", dir);
        log.info("  isDirectory: {}", Files.isDirectory(dir));

        try {
            boolean deleted = Files.deleteIfExists(dir);
            log.info("  ✔ (unexpected success) deleted={}", deleted);
        } catch (DirectoryNotEmptyException e) {
            log.warn("  ✔ Caught expected DirectoryNotEmptyException: {}", e.getMessage());
            log.info("  Lesson: deleteIfExists() does NOT suppress DirectoryNotEmptyException.");
        } catch (IOException e) {
            log.error("  ✘ Unexpected IOException: {}", e.getMessage(), e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 5 — Symbolic links: delete the LINK, not the target
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * <h3>Example 5 — Deleting a symbolic link (link-not-target semantics)</h3>
     *
     * <p>Just like {@link Files#delete(Path)}, calling
     * {@link Files#deleteIfExists(Path)} on a symbolic link removes the
     * <em>link</em> entry from the directory, not the file the link points to.
     * This is consistent with POSIX {@code unlink(2)} semantics.</p>
     *
     * <h4>Three scenarios with symbolic links</h4>
     * <ol>
     *   <li><strong>Link exists, target exists</strong> — link is removed,
     *       target survives, method returns {@code true}.</li>
     *   <li><strong>Link exists, target is gone (dangling link)</strong> — link
     *       is still removed (acts on the link inode), returns {@code true}.</li>
     *   <li><strong>Link does not exist</strong> — returns {@code false},
     *       no exception.</li>
     * </ol>
     *
     * <h4>Platform note</h4>
     * <p>Creating symbolic links on Windows requires Developer Mode or
     * Administrator privileges. This example skips gracefully if the fixture
     * symlink was not created by {@link #setUp()}.</p>
     */
    public static void example05_deleteSymbolicLink() {
        log.info("───────────────────────────────────────────────────────");
        log.info("  Example 05 — delete a symbolic link (not the target)");
        log.info("───────────────────────────────────────────────────────");

        Path link   = ROOT.resolve("symlink-link.txt");
        Path target = ROOT.resolve("symlink-target.txt");

        if (Files.notExists(link) && !Files.isSymbolicLink(link)) {
            log.warn("  [skip] Symlink fixture not present — skipping example 05.");
            return;
        }

        log.info("  Link   : {} (isSymbolicLink? {})", link,   Files.isSymbolicLink(link));
        log.info("  Target : {} (exists?         {})", target, Files.exists(target));

        try {
            boolean deleted = Files.deleteIfExists(link);
            log.info("  deleteIfExists() returned: {}", deleted);
            log.info("  Link   still exists? {}", Files.exists(link));
            log.info("  Target still exists? {} ← target is UNTOUCHED", Files.exists(target));
            log.info("  ✔ Symbolic link removed; target survived.");
        } catch (IOException e) {
            log.error("  ✘ IOException: {}", e.getMessage(), e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 6 — AccessDeniedException: read-only file + remediation
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * <h3>Example 6 — Read-only file ({@link AccessDeniedException} + remediation)</h3>
     *
     * <p>On Windows, a file with the DOS {@code read-only} attribute set causes
     * {@link Files#deleteIfExists(Path)} to throw {@link AccessDeniedException},
     * even when the return-false contract would otherwise apply to absent files.</p>
     *
     * <h4>Important clarification</h4>
     * <p>{@code deleteIfExists} only suppresses {@link NoSuchFileException}.
     * It propagates every other {@link IOException}, including
     * {@link AccessDeniedException}. Catching {@link IOException} catches
     * all, but the recommended pattern is to catch specific subclasses first.</p>
     *
     * <h4>Remediation pattern (Windows)</h4>
     * <ol>
     *   <li>Catch {@link AccessDeniedException}.</li>
     *   <li>Clear the read-only attribute via {@link DosFileAttributeView}.</li>
     *   <li>Retry with {@code deleteIfExists} — returns {@code true} on success.</li>
     * </ol>
     */
    public static void example06_deleteReadOnlyFile() {
        log.info("───────────────────────────────────────────────────────");
        log.info("  Example 06 — read-only file (AccessDeniedException + remediation)");
        log.info("───────────────────────────────────────────────────────");

        Path readOnly = ROOT.resolve("read-only-file.txt");
        log.info("  Target : {}", readOnly);

        try {
            boolean deleted = Files.deleteIfExists(readOnly);
            log.info("  ✔ Deleted without access error (OS may not enforce attribute). "
                    + "returned={}", deleted);
        } catch (AccessDeniedException e) {
            log.warn("  ⚠ AccessDeniedException — clearing read-only attribute and retrying…");
            try {
                DosFileAttributeView view =
                        Files.getFileAttributeView(readOnly, DosFileAttributeView.class);
                if (view != null) {
                    view.setReadOnly(false);
                    log.info("  Read-only attribute cleared.");
                }
                boolean deleted = Files.deleteIfExists(readOnly);
                log.info("  ✔ deleteIfExists() returned {} after attribute fix.", deleted);
            } catch (IOException ex) {
                log.error("  ✘ Still could not delete: {}", ex.getMessage(), ex);
            }
        } catch (IOException e) {
            log.error("  ✘ Unexpected IOException: {}", e.getMessage(), e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 7 — Idempotent / multi-call safety pattern
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * <h3>Example 7 — Idempotent deletion (safe to call multiple times)</h3>
     *
     * <p>One of the most valuable properties of {@link Files#deleteIfExists(Path)}
     * is <em>idempotency</em>: calling it on the same path several times is
     * completely safe. After the first call the file is gone; subsequent calls
     * simply return {@code false} without side effects or exceptions.</p>
     *
     * <h4>Why idempotency matters</h4>
     * <ul>
     *   <li><strong>Retry loops</strong> — if deletion is part of a retry-able
     *       operation, subsequent retries must not fail because the file is
     *       already gone.</li>
     *   <li><strong>Parallel cleanup</strong> — two threads may both attempt to
     *       delete the same temp file; the second call returns {@code false}
     *       without throwing.</li>
     *   <li><strong>Test teardown</strong> — {@code @AfterEach} / {@code @AfterAll}
     *       methods run even if the test failed before creating the file.</li>
     * </ul>
     *
     * <h4>Contrast with {@code Files.delete}</h4>
     * <p>A second call to {@link Files#delete(Path)} on the same (now absent)
     * path throws {@link NoSuchFileException}, breaking retry loops and
     * requiring explicit existence checks.</p>
     */
    public static void example07_idempotentDeletion() {
        log.info("───────────────────────────────────────────────────────");
        log.info("  Example 07 — idempotent deletion (safe multi-call pattern)");
        log.info("───────────────────────────────────────────────────────");

        Path tmp = ROOT.resolve("idempotent-cleanup.tmp");
        log.info("  Target : {}", tmp);

        try {
            // Call 1 — file exists
            boolean first = Files.deleteIfExists(tmp);
            log.info("  Call 1 → returned {} (file existed → deleted).", first);

            // Call 2 — file is already gone
            boolean second = Files.deleteIfExists(tmp);
            log.info("  Call 2 → returned {} (file gone  → no exception).", second);

            // Call 3 — same safe no-op
            boolean third = Files.deleteIfExists(tmp);
            log.info("  Call 3 → returned {} (still gone → still no exception).", third);

            log.info("  ✔ All three calls succeeded without exception.");
            log.info("  Lesson: deleteIfExists() is fully idempotent — safe for retry loops.");

        } catch (IOException e) {
            log.error("  ✘ Unexpected IOException: {}", e.getMessage(), e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 8 — Using the return value for conditional logic
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * <h3>Example 8 — Using the boolean return value for conditional logic</h3>
     *
     * <p>The {@code boolean} returned by {@link Files#deleteIfExists(Path)} enables
     * branching without a prior existence check, eliminating a TOCTOU (time-of-check
     * / time-of-use) race condition.</p>
     *
     * <h4>Anti-pattern (TOCTOU race)</h4>
     * <pre>{@code
     * // WRONG — there is a window between the check and the delete
     * if (Files.exists(path)) {
     *     Files.deleteIfExists(path);   // file may have vanished here
     *     auditLog("Deleted: " + path);
     * } else {
     *     auditLog("Not found: " + path);
     * }
     * }</pre>
     *
     * <h4>Correct pattern (atomic check-and-delete)</h4>
     * <pre>{@code
     * boolean deleted = Files.deleteIfExists(path);
     * if (deleted) {
     *     auditLog("Deleted: " + path);
     * } else {
     *     auditLog("Not found: " + path);
     * }
     * }</pre>
     *
     * <p>The second pattern is race-free because the check and the delete
     * happen in a single atomic OS syscall.</p>
     */
    public static void example08_useReturnValueForConditionalLogic() {
        log.info("───────────────────────────────────────────────────────");
        log.info("  Example 08 — using return value for conditional logic");
        log.info("───────────────────────────────────────────────────────");

        // Scenario A: file exists → returns true
        Path existingFile = ROOT.resolve("existing-file-conditional.txt");
        try {
            // Recreate so the example is self-contained
            Files.writeString(existingFile, "Conditional delete target.");

            boolean deleted = Files.deleteIfExists(existingFile);
            if (deleted) {
                log.info("  [Scenario A] File was present → deleted → logged to audit trail.");
            } else {
                log.info("  [Scenario A] File was absent → skip audit entry (no orphan log).");
            }
        } catch (IOException e) {
            log.error("  [Scenario A] ✘ IOException: {}", e.getMessage(), e);
        }

        // Scenario B: file does not exist → returns false
        Path absentFile = ROOT.resolve("never-existed-conditional.txt");
        try {
            boolean deleted = Files.deleteIfExists(absentFile);
            if (deleted) {
                log.info("  [Scenario B] File was present → deleted.");
            } else {
                log.info("  [Scenario B] File was absent → returned false, no exception. "
                        + "No TOCTOU race.");
            }
        } catch (IOException e) {
            log.error("  [Scenario B] ✘ IOException: {}", e.getMessage(), e);
        }

        log.info("  Lesson: the boolean return IS meaningful — do not ignore it.");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 9 — Batch deletion of a list of files
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * <h3>Example 9 — Batch deletion of multiple files</h3>
     *
     * <p>A common real-world use case: deleting a dynamic list of output files
     * where some may already be absent (e.g., previous partial runs).
     * {@link Files#deleteIfExists(Path)} is the ideal choice because it does not
     * fail when a file in the list is missing.</p>
     *
     * <h4>Pattern</h4>
     * <pre>{@code
     * long deletedCount = paths.stream()
     *     .mapToLong(p -> {
     *         try { return Files.deleteIfExists(p) ? 1 : 0; }
     *         catch (IOException e) { log.error(...); return 0; }
     *     })
     *     .sum();
     * }</pre>
     *
     * <h4>Error handling strategy</h4>
     * <p>The stream continues even when one deletion fails (best-effort).
     * An alternative <em>fail-fast</em> strategy would propagate the first
     * exception immediately using {@link java.io.UncheckedIOException}.</p>
     */
    public static void example09_batchDeletion() {
        log.info("───────────────────────────────────────────────────────");
        log.info("  Example 09 — batch deletion of a file list");
        log.info("───────────────────────────────────────────────────────");

        List<Path> targets = List.of(
                ROOT.resolve("batch-target-1.txt"),
                ROOT.resolve("batch-target-2.txt"),
                ROOT.resolve("batch-target-3.txt"),
                ROOT.resolve("batch-target-ghost.txt")  // ← doesn't exist: safe
        );

        int deletedCount  = 0;
        int absentCount   = 0;
        int errorCount    = 0;

        for (Path path : targets) {
            try {
                boolean deleted = Files.deleteIfExists(path);
                if (deleted) {
                    log.info("  [deleted] {}", path);
                    deletedCount++;
                } else {
                    log.info("  [absent ] {} — no exception, batch continues.", path);
                    absentCount++;
                }
            } catch (IOException e) {
                log.error("  [error  ] {} — {}", path, e.getMessage());
                errorCount++;
            }
        }

        log.info("  ─────────────────────────────────────────────");
        log.info("  Batch summary → deleted={}, absent={}, errors={}",
                deletedCount, absentCount, errorCount);
        log.info("  ✔ Batch completed; absent files caused zero exceptions.");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 10 — finally-block cleanup pattern (temp-file lifecycle)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * <h3>Example 10 — {@code finally}-block cleanup pattern</h3>
     *
     * <p>The canonical use of {@link Files#deleteIfExists(Path)} in production
     * code: creating a temporary file, using it, and unconditionally cleaning it
     * up in a {@code finally} block — regardless of whether the usage step
     * succeeded or threw an exception.</p>
     *
     * <h4>Why {@code deleteIfExists} shines here</h4>
     * <ul>
     *   <li>If the file creation step fails, {@code path} is never populated and
     *       the cleanup call safely returns {@code false} without adding a
     *       secondary exception to the stack trace.</li>
     *   <li>If the usage step throws, the finally block still runs and the temp
     *       file is removed cleanly.</li>
     *   <li>The pattern is equivalent in safety to {@code try-with-resources}
     *       for {@link java.nio.file.Files#createTempFile} combined with a
     *       custom {@link AutoCloseable} wrapper.</li>
     * </ul>
     *
     * <h4>Preferred modern alternative</h4>
     * <pre>{@code
     * // Wrap the path in a custom AutoCloseable for try-with-resources
     * try (var temp = new DeleteOnClose(Files.createTempFile(ROOT, "tmp", ".tmp"))) {
     *     processFile(temp.path());
     * } // ← deleteIfExists called automatically in close()
     * }</pre>
     */
    public static void example10_finallyBlockCleanupPattern() {
        log.info("───────────────────────────────────────────────────────");
        log.info("  Example 10 — finally-block cleanup (temp-file lifecycle)");
        log.info("───────────────────────────────────────────────────────");

        Path tempFile = null;

        try {
            // Step 1 — create temp file
            tempFile = Files.createTempFile(ROOT, "tmp-", ".tmp");
            log.info("  Temp file created: {}", tempFile);

            // Step 2 — simulate some processing that might throw
            Files.writeString(tempFile, "Processing payload...");
            log.info("  Processing complete.");

            // (Intentionally no exception here — showing happy-path cleanup)

        } catch (IOException e) {
            log.error("  ✘ Processing failed: {}", e.getMessage(), e);
        } finally {
            // Step 3 — unconditional cleanup
            if (tempFile != null) {
                try {
                    boolean deleted = Files.deleteIfExists(tempFile);
                    log.info("  [finally] deleteIfExists() → {} (temp file cleaned up).", deleted);
                } catch (IOException e) {
                    log.error("  [finally] ✘ Could not delete temp file: {}", e.getMessage(), e);
                }
            } else {
                log.info("  [finally] tempFile was null (creation failed) — nothing to clean up.");
            }
        }

        log.info("  ✔ Temp-file lifecycle example complete.");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 11 — Recursive tree deletion using deleteIfExists (post-order walk)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * <h3>Example 11 — Recursive tree deletion using {@code deleteIfExists}</h3>
     *
     * <p>This method combines {@link Files#walk(Path, FileVisitOption...)} with
     * {@link Files#deleteIfExists(Path)} to remove an entire directory tree
     * in post-order (deepest entries first).</p>
     *
     * <h4>Advantage over using {@code delete} in the walk</h4>
     * <p>Using {@code deleteIfExists} inside the walk means that if a concurrent
     * process deletes a file between the walk's enumeration and our delete attempt,
     * the walk continues silently (returns {@code false}) instead of throwing
     * {@link NoSuchFileException} and aborting the stream.</p>
     *
     * <h4>Algorithm</h4>
     * <ol>
     *   <li>{@code Files.walk} streams all paths depth-first.</li>
     *   <li>{@code sorted(reverseOrder())} ensures children come before parents.</li>
     *   <li>{@code deleteIfExists} removes each entry; absent entries are skipped.</li>
     * </ol>
     *
     * @param treeRoot the root directory to delete recursively;
     *                 if absent, the method logs a warning and returns immediately
     */
    public static void example11_recursiveTreeDeletion(Path treeRoot) {
        log.info("───────────────────────────────────────────────────────");
        log.info("  Example 11 — recursive tree deletion with deleteIfExists");
        log.info("───────────────────────────────────────────────────────");
        log.info("  Root: {}", treeRoot);

        if (Files.notExists(treeRoot)) {
            log.warn("  [skip] Tree root does not exist — nothing to delete.");
            return;
        }

        int[] counters = {0, 0, 0}; // [deleted, absent, errors]

        try (var stream = Files.walk(treeRoot)) {
            stream
                .sorted(java.util.Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        boolean removed = Files.deleteIfExists(path);
                        if (removed) {
                            log.info("  [deleted] {}", path);
                            counters[0]++;
                        } else {
                            log.info("  [absent ] {} — already gone, continuing.", path);
                            counters[1]++;
                        }
                    } catch (DirectoryNotEmptyException e) {
                        // Unlikely in a sorted post-order walk, but guard anyway
                        log.error("  [error  ] Not empty (concurrent modification?): {}", path);
                        counters[2]++;
                    } catch (IOException e) {
                        log.error("  [error  ] {} — {}", path, e.getMessage());
                        counters[2]++;
                    }
                });

            log.info("  ─────────────────────────────────────────────");
            log.info("  Tree-walk summary → deleted={}, absent={}, errors={}",
                    counters[0], counters[1], counters[2]);
            log.info("  ✔ Recursive deletion complete.");

        } catch (IOException e) {
            log.error("  ✘ Failed to walk tree: {}", e.getMessage(), e);
        }
    }

}