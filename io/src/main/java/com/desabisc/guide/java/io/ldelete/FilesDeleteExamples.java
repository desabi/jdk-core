package com.desabisc.guide.java.io.ldelete;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.DosFileAttributeView;

/**
 * <h2>Files.delete(Path) -> Exhaustive Edge-Case Examples</h2>
 *
 * <p>This class demonstrates every significant edge case of {@link Files#delete(Path)},
 * the <em>strict</em> deletion method introduced in Java 7 as part of the NIO.2 API
 * ({@code java.nio.file}).</p>
 *
 * <h3>Contract summary</h3>
 * <pre>
 *  void Files.delete(Path path) throws IOException
 * </pre>
 * <ul>
 *   <li>Deletes the file or <em>empty</em> directory at {@code path}.</li>
 *   <li><strong>Always throws</strong> {@link NoSuchFileException} when the target
 *       does not exist -> it never silently succeeds.</li>
 *   <li>Throws {@link DirectoryNotEmptyException} when {@code path} is a directory
 *       that still contains entries.</li>
 *   <li>Throws {@link AccessDeniedException} when the process lacks the required
 *       OS permissions.</li>
 *   <li>For symbolic links, deletes the <em>link itself</em>, never the link target.</li>
 * </ul>
 *
 * <h3>When to choose {@code Files.delete} over {@code Files.deleteIfExists}</h3>
 * <p>Use {@code Files.delete} whenever the file <em>must</em> exist as a
 * precondition -> for example, after writing a temporary file you are sure was
 * created. The exception on absence is a <em>feature</em>: it surfaces
 * programming bugs or unexpected state immediately.</p>
 *
 * <h3>Working directory</h3>
 * <p>All examples operate under {@code C:\examples\java\io\deleteeg}. Call
 * {@link #setUp()} before running any example to guarantee the required folder
 * structure and fixture files exist.</p>
 *
 * @author  desabisc
 * @version 1.0
 * @see     Files#delete(Path)
 * @see     FilesDeleteIfExistsExamples
 */
@Slf4j
public class FilesDeleteExamples {

    /**
     * Entry point -> runs all examples in sequence.
     *
     * <p>Each example is self-contained and logs its own header and result.
     * Failures in one example do not prevent subsequent examples from running.</p>
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        setUp();

        example01_deleteRegularFile();
        example02_deleteEmptyDirectory();
        example03_deleteNonExistentFile_throwsNoSuchFileException();
        example04_deleteNonEmptyDirectory_showsException();
        example04b_recursiveTreeDeletion(ROOT.resolve("non-empty-dir"));
        example05_deleteReadOnlyFile();
        example06_deleteFileWithSpecialCharacters();
        example07_deleteSymbolicLink();
        example08_deleteVsDeleteIfExists_decisionGuide();

        log.info("-----------------------------------------------------------");
        log.info("  All FilesDeleteExamples finished.");
        log.info("-----------------------------------------------------------");
    }

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    /** Root working directory for all examples in this class. */
    private static final Path ROOT = Paths.get("C:\\examples\\java\\io\\deleteeg\\delete");
    
    // 0 -> SETUP
    
    /**
     * Creates the complete folder structure and all fixture files required by
     * every example method in this class.
     *
     * <p>This method is <em>idempotent</em>: it is safe to call multiple times.
     * Any file or directory that already exists is simply skipped.</p>
     *
     * <p>Folder tree created under {@code C:\examples\java\io\deleteeg\delete}:</p>
     * <pre>
     *  delete/
     *  ├── regular-file.txt          ← example 1: happy-path file deletion
     *  ├── read-only-file.txt        ← example 5: read-only attribute set
     *  ├── special chars&amp;file!.txt  ← example 6: non-ASCII / special chars
     *  ├── empty-dir/                ← example 2: empty directory deletion
     *  ├── non-empty-dir/            ← example 4: directory with children
     *  │   └── child.txt
     *  ├── symlink-target.txt        ← example 7: symlink target (kept alive)
     *  └── symlink-link.txt          ← example 7: the link itself (deleted)
     * </pre>
     *
     * <p><strong>Note on symbolic links:</strong> Creating symbolic links on
     * Windows requires either Developer Mode to be enabled or the process to be
     * run as Administrator. If the JVM lacks that privilege the symlink fixture
     * is skipped and example 7 logs a warning instead of failing.</p>
     */
    public static void setUp() {
        log.info("-----------------------------------------------------------");
        log.info("  setUp() -> building fixture tree under: {}", ROOT);
        log.info("-----------------------------------------------------------");

        try {
            // 1. Root directory
            Files.createDirectories(ROOT);

            // 2. Regular file
            Path regularFile = ROOT.resolve("regular-file.txt");
            if (Files.notExists(regularFile)) {
                Files.writeString(regularFile, "Hello, NIO.2!");
                log.info("  [created] {}", regularFile);
            }

            // 3. Read-only file
            Path readOnly = ROOT.resolve("read-only-file.txt");
            if (Files.notExists(readOnly)) {
                Files.writeString(readOnly, "I am read-only.");
                // Set the DOS read-only attribute (Windows-specific)
                DosFileAttributeView view =
                        Files.getFileAttributeView(readOnly, DosFileAttributeView.class);
                if (view != null) {
                    view.setReadOnly(true);
                }
                log.info("  [created + read-only] {}", readOnly);
            }

            // 4. File with special characters
            Path specialChars = ROOT.resolve("special chars&file!.txt");
            if (Files.notExists(specialChars)) {
                Files.writeString(specialChars, "Special chars are fine in NIO.2!");
                log.info("  [created] {}", specialChars);
            }

            // 5. Empty directory
            Path emptyDir = ROOT.resolve("empty-dir");
            Files.createDirectories(emptyDir);
            log.info("  [created dir] {}", emptyDir);

            // 6. Non-empty directory with a child file
            Path nonEmptyDir = ROOT.resolve("non-empty-dir");
            Files.createDirectories(nonEmptyDir);
            Path child = nonEmptyDir.resolve("child.txt");
            if (Files.notExists(child)) {
                Files.writeString(child, "I prevent my parent from being deleted.");
                log.info("  [created] {}", child);
            }

            // 7. Symbolic link (best-effort -> requires OS privileges on Windows)
            Path symlinkTarget = ROOT.resolve("symlink-target.txt");
            Path symlinkLink   = ROOT.resolve("symlink-link.txt");
            if (Files.notExists(symlinkTarget)) {
                Files.writeString(symlinkTarget, "I am the link target -> I survive.");
                log.info("  [created] {}", symlinkTarget);
            }
            if (Files.notExists(symlinkLink)) {
                try {
                    Files.createSymbolicLink(symlinkLink, symlinkTarget);
                    log.info("  [created symlink] {} → {}", symlinkLink, symlinkTarget);
                } catch (UnsupportedOperationException | IOException e) {
                    log.warn("  [skip symlink] Could not create symbolic link -> "
                            + "requires Admin/Developer-Mode on Windows: {}", e.getMessage());
                }
            }

            log.info("  setUp() complete.");

        } catch (IOException e) {
            log.error("setUp() failed: {}", e.getMessage(), e);
        }
    }
    
    // 1 -> HAPPY PATH: delete a regular file

    /**
     * <h3>Example 1 -> Deleting a regular file (happy path)</h3>
     *
     * <p>This is the most common use case: an existing, writable, plain file is
     * deleted with a single call. No exception is thrown on success.</p>
     *
     * <h4>Key behavior</h4>
     * <ul>
     *   <li>The method returns {@code void} -> there is no boolean result to check.</li>
     *   <li>After the call the path no longer exists in the file system.</li>
     *   <li>The operation is <em>atomic</em> on most operating systems: either the
     *       file is deleted or it is not -> there is no intermediate state.</li>
     * </ul>
     *
     * <h4>Checked exceptions that can propagate</h4>
     * <ul>
     *   <li>{@link NoSuchFileException} -> if the file disappeared between the
     *       existence check and the delete call (TOCTOU race).</li>
     *   <li>{@link AccessDeniedException} -> if the process lacks write permission
     *       on the parent directory or the file has the read-only attribute.</li>
     * </ul>
     */
    public static void example01_deleteRegularFile() {
        log.info("------------------------------------------------------------------");
        log.info("  Example 01 -> delete a regular file (happy path)");
        log.info("------------------------------------------------------------------");

        Path file = ROOT.resolve("regular-file.txt");
        log.info("  Target : {}", file);
        log.info("  Exists before delete? {}", Files.exists(file));

        try {
            Files.delete(file);
            log.info("  -> File deleted successfully.");
            log.info("  Exists after  delete? {}", Files.exists(file));
        } catch (IOException e) {
            log.error("  -> Unexpected error: {}", e.getMessage(), e);
        }
    }
    
    // 2 -> Delete an EMPTY directory

    /**
     * <h3>Example 2 -> Deleting an empty directory</h3>
     *
     * <p>{@link Files#delete(Path)} can remove an <em>empty</em> directory exactly
     * the same way it removes a file. On POSIX systems this wraps the {@code rmdir}
     * syscall; on Windows it calls {@code RemoveDirectory}.</p>
     *
     * <h4>Key behavior</h4>
     * <ul>
     *   <li>The directory must be completely empty -> even hidden OS metadata files
     *       (e.g., {@code .DS_Store} on macOS) will cause a failure.</li>
     *   <li>The method has no recursive deletion mode; for directory trees see
     *       {@link #example04_deleteNonEmptyDirectory_showsException()}.</li>
     * </ul>
     *
     * <h4>Checked exceptions that can propagate</h4>
     * <ul>
     *   <li>{@link DirectoryNotEmptyException} -> the directory has children.</li>
     *   <li>{@link NoSuchFileException} -> the directory does not exist.</li>
     * </ul>
     */
    public static void example02_deleteEmptyDirectory() {
        log.info("------------------------------------------------------------------");
        log.info("  Example 02 -> delete an empty directory");
        log.info("------------------------------------------------------------------");

        Path dir = ROOT.resolve("empty-dir");
        log.info("  Target : {}", dir);
        log.info("  Is directory? {}", Files.isDirectory(dir));

        try {
            Files.delete(dir);
            log.info("  -> Empty directory deleted successfully.");
            log.info("  Exists after delete? {}", Files.exists(dir));
        } catch (DirectoryNotEmptyException e) {
            log.error("  -> DirectoryNotEmptyException -> still has children: {}", e.getMessage());
        } catch (IOException e) {
            log.error("  -> IOException: {}", e.getMessage(), e);
        }
    }
    
    // 3 -> NoSuchFileException when file does NOT exist

    /**
     * <h3>Example 3 -> Deleting a non-existent file (throws {@link NoSuchFileException})</h3>
     *
     * <p>This is the most important behavioural difference between
     * {@link Files#delete(Path)} and {@link Files#deleteIfExists(Path)}.
     * {@code delete} <em>always</em> throws {@link NoSuchFileException} when the
     * target does not exist; it never silently succeeds.</p>
     *
     * <h4>Why this matters</h4>
     * <ul>
     *   <li>Use {@code delete} when absence is a <strong>bug</strong> -> the
     *       exception acts as a fast-fail assertion.</li>
     *   <li>Use {@code deleteIfExists} when absence is an expected,
     *       legitimate outcome (idempotent cleanup).</li>
     * </ul>
     *
     * <h4>TOCTOU warning</h4>
     * <p>Checking {@link Files#exists(Path, LinkOption...)} before calling {@code delete}
     * introduces a <em>time-of-check / time-of-use</em> (TOCTOU) race: another
     * process can delete or create the file between the check and the delete.
     * The correct pattern is to call {@code delete} directly and handle
     * {@link NoSuchFileException} explicitly when absence is acceptable.</p>
     */
    public static void example03_deleteNonExistentFile_throwsNoSuchFileException() {
        log.info("------------------------------------------------------------------");
        log.info("  Example 03 -> delete a non-existent file");
        log.info("------------------------------------------------------------------");

        Path ghost = ROOT.resolve("i-do-not-exist.txt");
        log.info("  Target : {}", ghost);
        log.info("  Exists? {}", Files.exists(ghost));

        try {
            Files.delete(ghost);
            log.info("  -> (should never reach here)");
        } catch (NoSuchFileException e) {
            // ← This is the expected outcome
            log.warn("  -> Caught expected NoSuchFileException: {}", e.getMessage());
            log.info("  Lesson: Files.delete() ALWAYS throws when the target is absent.");
            log.info("  Use Files.deleteIfExists() for idempotent / best-effort cleanup.");
        } catch (IOException e) {
            log.error("  -> Unexpected IOException: {}", e.getMessage(), e);
        }
    }

    // 4 -> DirectoryNotEmptyException for non-empty directories

    /**
     * <h3>Example 4 -> Attempting to delete a non-empty directory
     * ({@link DirectoryNotEmptyException})</h3>
     *
     * <p>{@link Files#delete(Path)} <strong>cannot</strong> delete a directory
     * that still contains files or subdirectories. It throws
     * {@link DirectoryNotEmptyException} immediately -> it does <em>not</em>
     * perform any partial cleanup.</p>
     *
     * <h4>Safe recursive deletion pattern</h4>
     * <p>To delete an entire directory tree, walk it in <em>post-order</em>
     * (deepest entries first) using {@link Files#walk(Path, FileVisitOption...)}
     * combined with a terminal delete on each entry:</p>
     * <pre>{@code
     * Files.walk(dirToDelete)
     *      .sorted(Comparator.reverseOrder())   // children before parents
     *      .forEach(p -> {
     *          try { Files.delete(p); }
     *          catch (IOException ex) { throw new UncheckedIOException(ex); }
     *      });
     * }</pre>
     *
     * <h4>Checked exceptions that can propagate</h4>
     * <ul>
     *   <li>{@link DirectoryNotEmptyException} -> directory has children (shown here).</li>
     *   <li>{@link NoSuchFileException} -> directory doesn't exist.</li>
     * </ul>
     */
    public static void example04_deleteNonEmptyDirectory_showsException() {
        log.info("------------------------------------------------------------------");
        log.info("  Example 04 -> delete a non-empty directory (exception demo)");
        log.info("------------------------------------------------------------------");

        Path dir = ROOT.resolve("non-empty-dir");
        log.info("  Target : {}", dir);

        try {
            Files.delete(dir);
            log.info("  -> (should never reach here -> directory is NOT empty)");
        } catch (DirectoryNotEmptyException e) {
            // ← This is the expected outcome
            log.warn("  -> Caught expected DirectoryNotEmptyException: {}", e.getMessage());
            log.info("  Lesson: Files.delete() refuses to delete non-empty directories.");
            log.info("  Use Files.walk() + reverseOrder() for recursive tree deletion.");
        } catch (IOException e) {
            log.error("  ✘ Unexpected IOException: {}", e.getMessage(), e);
        }
    }

    // 4b -> BONUS: recursive directory tree deletion using Files.walk()

    /**
     * <h3>Example 4b -> Recursive directory tree deletion with {@code Files.walk()}</h3>
     *
     * <p>This method builds on example 4 to show the canonical NIO.2 pattern for
     * deleting an entire subtree safely in post-order (deepest entries first,
     * root last). The approach uses a single pipeline without any external
     * libraries.</p>
     *
     * <h4>How it works</h4>
     * <ol>
     *   <li>{@link Files#walk(Path, FileVisitOption...)} performs a depth-first
     *       traversal and streams every {@link Path} in the tree, including the
     *       root itself.</li>
     *   <li>{@code sorted(Comparator.reverseOrder())} puts the longest
     *       (deepest) paths first, so children are always deleted before their
     *       parent directories.</li>
     *   <li>Each {@link Path} is passed to {@link Files#delete(Path)}.  Because
     *       every child has already been removed by this point, no
     *       {@link DirectoryNotEmptyException} can occur.</li>
     * </ol>
     *
     * <h4>Error strategy</h4>
     * <p>The {@code forEach} wraps each delete in try/catch so that one failing
     * entry does not abort the entire walk -> it logs the error and continues.
     * Adjust this strategy to your requirements (fail-fast vs. best-effort).</p>
     *
     * @param treeRoot the root of the directory tree to remove recursively
     */
    public static void example04b_recursiveTreeDeletion(Path treeRoot) {
        log.info("------------------------------------------------------------------");
        log.info("  Example 04b -> recursive tree deletion via Files.walk()");
        log.info("------------------------------------------------------------------");
        log.info("  Root   : {}", treeRoot);

        if (Files.notExists(treeRoot)) {
            log.warn("  [skip] Tree root does not exist: {}", treeRoot);
            return;
        }

        try (var stream = Files.walk(treeRoot)) {
            stream
                .sorted(java.util.Comparator.reverseOrder()) // deepest first
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        log.info("  [deleted] {}", path);
                    } catch (IOException ex) {
                        log.error("  [FAILED ] {} -> {}", path, ex.getMessage());
                    }
                });
            log.info("  -> Recursive deletion complete.");
        } catch (IOException e) {
            log.error("  -> Failed to walk tree: {}", e.getMessage(), e);
        }
    }

    // 5 -> AccessDeniedException: read-only file on Windows

    /**
     * <h3>Example 5 -> Deleting a read-only file ({@link AccessDeniedException})</h3>
     *
     * <p>On Windows, a file with the {@code read-only} DOS attribute set causes
     * {@code Files.delete} to throw {@link AccessDeniedException}, even if the
     * current user owns the file.</p>
     *
     * <h4>Correct remediation pattern</h4>
     * <ol>
     *   <li>Catch {@link AccessDeniedException}.</li>
     *   <li>Clear the read-only attribute via {@link DosFileAttributeView}.</li>
     *   <li>Retry to delete.</li>
     * </ol>
     *
     * <p>This is the same technique build tools like Maven and Gradle apply when
     * cleaning {@code .m2} cache files on Windows.</p>
     *
     * <h4>POSIX note</h4>
     * <p>On Linux/macOS the read-only permission is controlled at the file-mode
     * level ({@code chmod}). The correct fix on POSIX is to grant write permission
     * with {@link Files#setAttribute(Path, String, Object, LinkOption...)} using
     * the attribute name {@code "posix:permissions"}.</p>
     */
    public static void example05_deleteReadOnlyFile() {
        log.info("------------------------------------------------------------------");
        log.info("  Example 05 -> delete a read-only file (AccessDeniedException)");
        log.info("------------------------------------------------------------------");

        Path readOnly = ROOT.resolve("read-only-file.txt");
        log.info("  Target : {}", readOnly);

        try {
            Files.delete(readOnly);
            // If it succeeds without error the OS didn't enforce read-only
            log.info("  ✔ Deleted without error (OS may not enforce read-only attribute).");
        } catch (AccessDeniedException e) {
            log.warn("  ⚠ AccessDeniedException caught -> clearing read-only attribute and retrying…");
            try {
                // Step 1: clear the read-only attribute
                DosFileAttributeView view =
                        Files.getFileAttributeView(readOnly, DosFileAttributeView.class);
                if (view != null) {
                    view.setReadOnly(false);
                    log.info("  Read-only attribute cleared.");
                }
                // Step 2: retry
                Files.delete(readOnly);
                log.info("  ✔ File deleted successfully after clearing read-only attribute.");
            } catch (IOException ex) {
                log.error("  ✘ Still could not delete: {}", ex.getMessage(), ex);
            }
        } catch (NoSuchFileException e) {
            log.warn("  ⚠ File does not exist -> run setUp() first: {}", e.getMessage());
        } catch (IOException e) {
            log.error("  ✘ Unexpected IOException: {}", e.getMessage(), e);
        }
    }

    // 6 -> Files with special characters in their names

    /**
     * <h3>Example 6 -> Deleting a file whose name contains special characters</h3>
     *
     * <p>The NIO.2 {@link Path} API operates on the raw file-system path and
     * handles special characters transparently. There is no need to escape spaces,
     * ampersands, exclamation marks, or Unicode code points -> unlike legacy
     * {@link java.io.File} with shell-based tools.</p>
     *
     * <h4>Characters that are valid in Windows file names</h4>
     * <p>Spaces, {@code &}, {@code !}, {@code @}, {@code #}, {@code (}, {@code )},
     * {@code -}, {@code +}, and many Unicode characters are all valid.
     * Windows only forbids: {@code \ / : * ? " < > |} and control characters.</p>
     *
     * <h4>Key takeaway</h4>
     * <p>Always prefer {@link Path#resolve(String)} over string concatenation when
     * building paths -> {@code resolve} delegates encoding concerns to the underlying
     * file-system provider.</p>
     */
    public static void example06_deleteFileWithSpecialCharacters() {
        log.info("------------------------------------------------------------------");
        log.info("  Example 06 -> delete a file with special characters in name");
        log.info("------------------------------------------------------------------");

        Path special = ROOT.resolve("special chars&file!.txt");
        log.info("  Target : {}", special);
        log.info("  Exists? {}", Files.exists(special));

        try {
            Files.delete(special);
            log.info("  ✔ File with special characters deleted successfully.");
            log.info("  Lesson: NIO.2 handles special characters transparently.");
        } catch (NoSuchFileException e) {
            log.warn("  ⚠ File not found -> run setUp() first: {}", e.getMessage());
        } catch (IOException e) {
            log.error("  ✘ Unexpected IOException: {}", e.getMessage(), e);
        }
    }

    // 7 -> Symbolic links: delete the LINK, not the TARGET

    /**
     * <h3>Example 7 -> Deleting a symbolic link (link-not-target semantics)</h3>
     *
     * <p>When the path passed to {@link Files#delete(Path)} is a symbolic link,
     * <em>only the link itself is deleted</em>. The link target is completely
     * unaffected. This is called <em>not following</em> the link -> consistent
     * with POSIX {@code unlink(2)} semantics.</p>
     *
     * <h4>Contrast with {@code Files.delete} on a regular file via a link</h4>
     * <pre>
     *  symlink → target.txt
     *
     *  Files.delete(symlink)   → removes symlink, target.txt still exists ✔
     *  Files.delete(target)    → removes target.txt, symlink becomes dangling ✔
     * </pre>
     *
     * <h4>Dangling links</h4>
     * <p>A dangling (broken) symbolic link -> one whose target no longer exists ->
     * can still be deleted with {@code Files.delete} because the operation acts
     * on the link inode itself, not the target. This is a common cleanup scenario
     * after a target file has been independently removed.</p>
     *
     * <h4>Platform note</h4>
     * <p>Creating symbolic links on Windows requires either
     * <em>Developer Mode</em> or <em>Administrator privileges</em>.
     * This example skips gracefully if the symlink fixture was not created during
     * {@link #setUp()}.</p>
     */
    public static void example07_deleteSymbolicLink() {
        log.info("------------------------------------------------------------------");
        log.info("  Example 07 -> delete a symbolic link (not the target)");
        log.info("------------------------------------------------------------------");

        Path link   = ROOT.resolve("symlink-link.txt");
        Path target = ROOT.resolve("symlink-target.txt");

        if (Files.notExists(link) && !Files.isSymbolicLink(link)) {
            log.warn("  [skip] Symlink fixture not present -> skipping example 07.");
            log.warn("  To enable: run with Admin/Developer-Mode on Windows.");
            return;
        }

        log.info("  Link   : {} (is symlink? {})", link,   Files.isSymbolicLink(link));
        log.info("  Target : {} (exists?     {})", target, Files.exists(target));

        try {
            Files.delete(link);
            log.info("  ✔ Symbolic link deleted.");
            log.info("  Link   still exists? {} ", Files.exists(link));
            log.info("  Target still exists? {} ← target is untouched", Files.exists(target));
            log.info("  Lesson: Files.delete() on a symlink removes the LINK, not the TARGET.");
        } catch (NoSuchFileException e) {
            log.warn("  ⚠ Link not found: {}", e.getMessage());
        } catch (IOException e) {
            log.error("  ✘ IOException: {}", e.getMessage(), e);
        }
    }

    // 8 -> delete() vs. deleteIfExists() side-by-side

    /**
     * <h3>Example 8 -> {@code delete()} vs {@code deleteIfExists()} decision guide</h3>
     *
     * <p>This method does <em>not</em> perform any file operation. It serves as an
     * in-code reference summarizing when to choose each method, demonstrated
     * through contrasting log messages and code comments.</p>
     *
     * <table border="1" cellpadding="6">
     *   <caption>Comparison: delete vs deleteIfExists</caption>
     *   <tr>
     *     <th>Scenario</th>
     *     <th>Preferred method</th>
     *     <th>Reason</th>
     *   </tr>
     *   <tr>
     *     <td>File was just created -> guaranteed to exist</td>
     *     <td>{@code Files.delete()}</td>
     *     <td>Exception on absence is a programming bug signal</td>
     *   </tr>
     *   <tr>
     *     <td>Temp-file cleanup in {@code finally} block</td>
     *     <td>{@code Files.deleteIfExists()}</td>
     *     <td>File may not have been created if earlier step failed</td>
     *   </tr>
     *   <tr>
     *     <td>Idempotent cleanup / retry logic</td>
     *     <td>{@code Files.deleteIfExists()}</td>
     *     <td>Safely callable multiple times with no exception</td>
     *   </tr>
     *   <tr>
     *     <td>Deletion whose absence must be audited</td>
     *     <td>{@code Files.delete()}</td>
     *     <td>Forces caller to acknowledge and handle the missing-file case</td>
     *   </tr>
     * </table>
     */
    public static void example08_deleteVsDeleteIfExists_decisionGuide() {
        log.info("------------------------------------------------------------------");
        log.info("  Example 08 -> Files.delete() vs Files.deleteIfExists()");
        log.info("------------------------------------------------------------------");

        log.info("  ┌------------------------------------------------------------------┐");
        log.info("  │  Files.delete(path)                                              │");
        log.info("  │  • Returns void                                                  │");
        log.info("  │  • THROWS NoSuchFileException when path does not exist           │");
        log.info("  │  • Use when absence is a BUG (guaranteed-to-exist precondition)  │");
        log.info("  └------------------------------------------------------------------┘");
        log.info("  ┌------------------------------------------------------------------┐");
        log.info("  │  Files.deleteIfExists(path)                                      │");
        log.info("  │  • Returns boolean (true = deleted, false = wasn't there)        │");
        log.info("  │  • DOES NOT throw when path does not exist                       │");
        log.info("  │  • Use for idempotent / best-effort cleanup                      │");
        log.info("  └------------------------------------------------------------------┘");

        // ----------------------------------------------------------------
        // Pattern A: use delete() when the file MUST exist
        // ----------------------------------------------------------------
        Path mustExist = ROOT.resolve("regular-file.txt");
        // Re-create it so the demo works even if example01 already ran
        try {
            if (Files.notExists(mustExist)) {
                Files.writeString(mustExist, "recreated for example 08");
            }
            Files.delete(mustExist);              // ← strict: throws if absent
            log.info("  [Pattern A] ✔ delete()  -> file removed (existence was guaranteed).");
        } catch (IOException e) {
            log.error("  [Pattern A] ✘ {}", e.getMessage());
        }

        // ----------------------------------------------------------------
        // Pattern B: use deleteIfExists() in finally / cleanup scenarios
        // ----------------------------------------------------------------
        Path mayOrMayNotExist = ROOT.resolve("i-was-never-created.tmp");
        try {
            boolean deleted = Files.deleteIfExists(mayOrMayNotExist);
            if (deleted) {
                log.info("  [Pattern B] ✔ deleteIfExists() -> file found and removed.");
            } else {
                log.info("  [Pattern B] ✔ deleteIfExists() -> file was absent; no exception thrown.");
            }
        } catch (IOException e) {
            log.error("  [Pattern B] ✘ {}", e.getMessage());
        }
    }

    
    // MAIN
    

}