package com.desabisc.guide.java.io.mcompare;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <h2>Files.isSameFile() – Comprehensive Edge-Case Guide</h2>
 *
 * <p>{@link Files#isSameFile(Path, Path)} answers one precise question:
 * <em>"Do these two {@link Path} objects refer to the same underlying file or directory
 * in the file system?"</em></p>
 *
 * <h3>How the JDK determines "sameness"</h3>
 * <ol>
 *   <li><strong>Reference equality shortcut</strong> – if both paths are {@code ==} equal,
 *       the method returns {@code true} immediately without any I/O.</li>
 *   <li><strong>Path equality shortcut</strong> – if {@code path1.equals(path2)} is
 *       {@code true} AND the provider is not a custom one, the method also returns
 *       {@code true} without I/O on most JDK implementations.</li>
 *   <li><strong>File-system probing</strong> – otherwise the JDK resolves both paths to
 *       their canonical form (following symlinks, resolving {@code .} / {@code ..}) and
 *       compares the underlying file-system object (inode on Linux/macOS; file ID on
 *       Windows). Two paths are the same file if and only if they point to the
 *       <em>same inode / file ID</em>, regardless of how different the string
 *       representations look.</li>
 * </ol>
 *
 * <h3>Checked exception contract</h3>
 * <ul>
 *   <li>Throws {@link IOException} if either path does not exist <em>or</em> if a
 *       file-system error occurs during resolution.</li>
 *   <li>There is <strong>one documented exception</strong> to the "both must exist" rule:
 *       if {@code path1.equals(path2)} is {@code true} some providers return {@code true}
 *       without verifying existence – but you should never rely on this.</li>
 * </ul>
 *
 * <h3>Edge cases covered in this class</h3>
 * <ol>
 *   <li>Identical {@link Path} object (same reference)</li>
 *   <li>Two distinct {@link Path} objects with identical string representation</li>
 *   <li>Absolute vs relative path pointing to the same file</li>
 *   <li>Path with redundant {@code .} and {@code ..} segments vs clean path</li>
 *   <li>Case variation on Windows (case-insensitive file system)</li>
 *   <li>Hard link pointing to the same inode</li>
 *   <li>Symbolic link resolved to its target</li>
 *   <li>Two completely different files (must return {@code false})</li>
 *   <li>One non-existent path (must throw {@link IOException})</li>
 *   <li>Both paths non-existent (must throw {@link IOException})</li>
 *   <li>Comparing a directory with itself</li>
 * </ol>
 *
 * <h3>Working directory</h3>
 * <pre>{@code C:\examples\java\io\compareeg\issamefile\}</pre>
 *
 * @author  desabisc
 * @version 1.0
 * @since   JDK 7  (NIO.2 – {@code java.nio.file})
 * @see     Files#isSameFile(Path, Path)
 */
@Slf4j
public class IsSameFileExamples {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    /** Root working directory for all examples in this class. */
    private static final Path BASE_DIR =
            Paths.get("C:\\examples\\java\\io\\compareeg\\issamefile");

    // -----------------------------------------------------------------------
    // Entry point
    // -----------------------------------------------------------------------

    /**
     * Orchestrates every example in sequence.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        IsSameFileExamples runner = new IsSameFileExamples();

        try {
            runner.setUp();
        } catch (IOException e) {
            log.error("setUp() failed – cannot continue.", e);
            return;
        }

        runner.example01_sameReference();
        runner.example02_equalPathObjects();
        runner.example03_absoluteVsRelative();
        runner.example04_redundantSegments();
        runner.example05_caseVariationWindows();
        runner.example06_hardLink();
        runner.example07_symbolicLink();
        runner.example08_differentFiles();
        runner.example09_onePathNonExistent();
        runner.example10_bothPathsNonExistent();
        runner.example11_directoryWithItself();
    }

    // -----------------------------------------------------------------------
    // setUp
    // -----------------------------------------------------------------------

    /**
     * Creates the folder structure and seed files required by every example.
     *
     * <p>This method is <em>idempotent</em>: running it twice in a row produces
     * the same result as running it once. Any directory or file that already
     * exists is silently skipped.</p>
     *
     * <h3>Files and directories created</h3>
     * <pre>{@code
     * C:\examples\java\io\compareeg\issamefile\
     *   alpha.txt          – plain text file ("Hello from Alpha")
     *   beta.txt           – plain text file ("Hello from Beta")
     *   subdir\            – sub-directory used in the directory example
     *     gamma.txt        – plain text file inside the sub-directory
     * }</pre>
     *
     * <p>Hard links and symbolic links are created at example runtime because
     * they depend on the seed files already existing and because symbolic-link
     * creation may require elevated privileges on Windows.</p>
     *
     * @throws IOException if a directory or file cannot be created
     */
    private void setUp() throws IOException {
        log.info("=== setUp() – preparing working directory ===");

        // Root directory
        Files.createDirectories(BASE_DIR);
        log.info("Working directory ready: {}", BASE_DIR);

        // Sub-directory
        Path subdir = BASE_DIR.resolve("subdir");
        Files.createDirectories(subdir);
        log.info("Sub-directory ready: {}", subdir);

        // Seed files
        createFileIfAbsent(BASE_DIR.resolve("alpha.txt"), "Hello from Alpha");
        createFileIfAbsent(BASE_DIR.resolve("beta.txt"),  "Hello from Beta");
        createFileIfAbsent(subdir.resolve("gamma.txt"),   "Hello from Gamma");

        log.info("setUp() complete.\n");
    }

    // -----------------------------------------------------------------------
    // Edge-case examples
    // -----------------------------------------------------------------------

    /**
     * <h3>Example 01 – Same {@link Path} reference</h3>
     *
     * <p>When you pass the <em>exact same object reference</em> as both arguments,
     * {@code isSameFile} returns {@code true} instantly. The JDK contains an
     * identity check ({@code path1 == path2}) as a fast-path optimisation, so no
     * file-system call is required.</p>
     *
     * <p><strong>Expected result:</strong> {@code true}</p>
     */
    private void example01_sameReference() {
        log.info("--- Example 01: same Path reference ---");
        Path path = BASE_DIR.resolve("alpha.txt");

        try {
            boolean result = Files.isSameFile(path, path);  // same object!
            log.info("isSameFile(path, path) → {}", result);
            // → true
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    /**
     * <h3>Example 02 – Two distinct {@link Path} objects with identical strings</h3>
     *
     * <p>Two separate calls to {@link Paths#get(String, String...)} with the same
     * string arguments produce two different Java objects, but they are
     * {@code equals()}-equal. Most NIO.2 providers short-circuit at this point
     * and return {@code true} without doing I/O, though the spec technically
     * requires the file to exist for some providers.</p>
     *
     * <p><strong>Expected result:</strong> {@code true}</p>
     */
    private void example02_equalPathObjects() {
        log.info("--- Example 02: equal Path objects, different references ---");
        Path path1 = Paths.get("C:\\examples\\java\\io\\compareeg\\issamefile\\alpha.txt");
        Path path2 = Paths.get("C:\\examples\\java\\io\\compareeg\\issamefile\\alpha.txt");

        log.info("path1 == path2  : {}", (path1 == path2));   // false – different objects
        log.info("path1.equals(p2): {}", path1.equals(path2)); // true  – same string

        try {
            boolean result = Files.isSameFile(path1, path2);
            log.info("isSameFile(path1, path2) → {}", result);
            // → true
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    /**
     * <h3>Example 03 – Absolute path vs relative path</h3>
     *
     * <p>A relative path like {@code "alpha.txt"} and a fully qualified absolute
     * path like {@code "C:\examples\java\io\compareeg\issamefile\alpha.txt"} are
     * <em>not</em> {@code equals()}-equal as Java objects, but they may resolve to
     * the very same file if the JVM's current working directory happens to match.
     * {@code isSameFile} resolves both paths before comparing, so it returns
     * {@code true} when they land on the same inode/file ID.</p>
     *
     * <p><strong>Note:</strong> This example works correctly only when the JVM is
     * launched from {@code C:\examples\java\io\compareeg\issamefile}. In any other
     * working directory the relative path resolves to a different (possibly absent)
     * file and the call throws an {@link IOException}.</p>
     *
     * <p><strong>Expected result:</strong> {@code true} (if cwd matches)</p>
     */
    private void example03_absoluteVsRelative() {
        log.info("--- Example 03: absolute path vs relative path ---");
        Path absolute = BASE_DIR.resolve("alpha.txt");
        Path relative = absolute.toAbsolutePath(); // make both absolute for portability

        // Demonstrate with a manually constructed "relative-style" string resolved
        // against BASE_DIR to keep the example self-contained regardless of cwd.
        Path alsoAbsolute = BASE_DIR.resolve(Paths.get(".", "alpha.txt")).normalize();

        log.info("absolute      : {}", absolute);
        log.info("also absolute : {}", alsoAbsolute);
        log.info("equals()      : {}", absolute.equals(alsoAbsolute)); // false – strings differ

        try {
            boolean result = Files.isSameFile(absolute, alsoAbsolute);
            log.info("isSameFile(absolute, alsoAbsolute) → {}", result);
            // → true  (both normalise to the same path)
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    /**
     * <h3>Example 04 – Paths with redundant {@code .} and {@code ..} segments</h3>
     *
     * <p>Path strings like {@code "issamefile\subdir\..\alpha.txt"} contain
     * navigation segments that make the Java string different from the clean
     * path {@code "issamefile\alpha.txt"}, yet they denote the same file after
     * resolution. {@code isSameFile} resolves (canonicalises) both paths before
     * comparing, so it returns {@code true} here.</p>
     *
     * <p>Contrast this with {@link Path#normalize()}, which only removes the
     * redundant segments lexically without consulting the file system – it would
     * fail to follow a real symlink, for example.</p>
     *
     * <p><strong>Expected result:</strong> {@code true}</p>
     */
    private void example04_redundantSegments() {
        log.info("--- Example 04: redundant . and .. segments ---");
        Path clean    = BASE_DIR.resolve("alpha.txt");
        Path dotDot   = BASE_DIR.resolve("subdir").resolve("..").resolve("alpha.txt");

        log.info("clean  : {}", clean);
        log.info("dotDot : {}", dotDot);
        log.info("equals(): {}", clean.equals(dotDot)); // false – string form differs

        try {
            boolean result = Files.isSameFile(clean, dotDot);
            log.info("isSameFile(clean, dotDot) → {}", result);
            // → true  (same file after resolution)
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    /**
     * <h3>Example 05 – Case variation on Windows (case-insensitive FS)</h3>
     *
     * <p>The Windows NTFS file system is case-insensitive by default, meaning
     * {@code ALPHA.TXT} and {@code alpha.txt} refer to the same file. The NIO.2
     * Windows provider delegates the comparison to the OS, so {@code isSameFile}
     * correctly returns {@code true} even though the Java {@code String} objects
     * differ in case.</p>
     *
     * <p>On Linux (ext4, case-sensitive by default) the same pair of paths would
     * point to two <em>different</em> (or non-existent) files and the behaviour
     * would be different.</p>
     *
     * <p><strong>Expected result on Windows:</strong> {@code true}<br>
     * <strong>Expected result on Linux (case-sensitive FS):</strong> {@code IOException}
     * if the upper-case variant does not exist</p>
     */
    private void example05_caseVariationWindows() {
        log.info("--- Example 05: case variation (Windows only) ---");
        Path lower = BASE_DIR.resolve("alpha.txt");
        Path upper = BASE_DIR.resolve("ALPHA.TXT");   // same file on NTFS

        log.info("lower : {}", lower);
        log.info("upper : {}", upper);
        log.info("equals(): {}", lower.equals(upper)); // false – case differs in Java

        try {
            boolean result = Files.isSameFile(lower, upper);
            log.info("isSameFile(lower, upper) → {} (Windows NTFS: case-insensitive)", result);
            // → true on Windows NTFS
        } catch (IOException e) {
            log.warn("IOException (expected on case-sensitive file systems): {}", e.getMessage());
        }
        log.info("");
    }

    /**
     * <h3>Example 06 – Hard link to the same inode</h3>
     *
     * <p>A <em>hard link</em> is a second directory entry pointing directly to
     * the same inode (or Windows file ID) as an existing file. Both the original
     * and the hard link share the same underlying data blocks and metadata. There
     * is no "primary" or "secondary" file – both are equal peers.</p>
     *
     * <p>{@code isSameFile} detects this by comparing inodes / file IDs and
     * therefore returns {@code true} for a file and any of its hard links,
     * regardless of how different the path strings look.</p>
     *
     * <p><strong>Expected result:</strong> {@code true}</p>
     *
     * @see Files#createLink(Path, Path)
     */
    private void example06_hardLink() {
        log.info("--- Example 06: hard link ---");
        Path original = BASE_DIR.resolve("alpha.txt");
        Path hardLink = BASE_DIR.resolve("alpha_hardlink.txt");

        try {
            // Create a hard link (idempotent guard)
            if (Files.notExists(hardLink)) {
                Files.createLink(hardLink, original);
                log.info("Hard link created: {}", hardLink);
            } else {
                log.info("Hard link already exists: {}", hardLink);
            }

            boolean result = Files.isSameFile(original, hardLink);
            log.info("isSameFile(original, hardLink) → {}", result);
            // → true – same inode / file ID

        } catch (IOException e) {
            log.error("Hard link example failed (may need NTFS & correct privileges): {}",
                    e.getMessage());
        }
        log.info("");
    }

    /**
     * <h3>Example 07 – Symbolic link resolved to its target</h3>
     *
     * <p>A <em>symbolic link</em> (symlink) is a special file containing a path
     * that the OS follows transparently. {@code isSameFile} always follows symbolic
     * links before comparing – it compares the <em>targets</em>, not the link files
     * themselves. This means:</p>
     * <ul>
     *   <li>{@code isSameFile(symlink, target)} → {@code true}</li>
     *   <li>{@code isSameFile(symlink, symlink)} → {@code true}</li>
     *   <li>Two different symlinks pointing to the same target → {@code true}</li>
     * </ul>
     *
     * <p><strong>Windows note:</strong> creating a symbolic link requires either
     * Developer Mode enabled or the {@code SeCreateSymbolicLinkPrivilege} privilege.
     * The example logs a warning and skips gracefully if the privilege is absent.</p>
     *
     * <p><strong>Expected result:</strong> {@code true}</p>
     *
     * @see Files#createSymbolicLink(Path, Path, java.nio.file.attribute.FileAttribute[])
     */
    private void example07_symbolicLink() {
        log.info("--- Example 07: symbolic link ---");
        Path target  = BASE_DIR.resolve("beta.txt");
        Path symlink = BASE_DIR.resolve("beta_symlink.txt");

        try {
            if (Files.notExists(symlink)) {
                Files.createSymbolicLink(symlink, target);
                log.info("Symlink created: {} → {}", symlink, target);
            } else {
                log.info("Symlink already exists: {}", symlink);
            }

            boolean result = Files.isSameFile(symlink, target);
            log.info("isSameFile(symlink, target) → {}", result);
            // → true – isSameFile follows the link and compares targets

        } catch (IOException e) {
            log.warn("Symlink example skipped (privilege or FS limitation): {}",
                    e.getMessage());
        }
        log.info("");
    }

    /**
     * <h3>Example 08 – Two completely different files</h3>
     *
     * <p>When both paths exist but refer to distinct files (different inodes /
     * file IDs), {@code isSameFile} returns {@code false}. The content of the
     * files is completely irrelevant to this check – even if both files contain
     * identical bytes, they are <em>not</em> the same file unless they share an
     * inode or the comparison is via a hard link.</p>
     *
     * <p>Content equality is the job of {@link Files#mismatch(Path, Path)},
     * covered in the companion class {@code MismatchExamples}.</p>
     *
     * <p><strong>Expected result:</strong> {@code false}</p>
     */
    private void example08_differentFiles() {
        log.info("--- Example 08: two completely different files ---");
        Path fileA = BASE_DIR.resolve("alpha.txt");
        Path fileB = BASE_DIR.resolve("beta.txt");

        try {
            boolean result = Files.isSameFile(fileA, fileB);
            log.info("isSameFile(alpha.txt, beta.txt) → {}", result);
            // → false
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    /**
     * <h3>Example 09 – One path does not exist</h3>
     *
     * <p>If either path does not exist on the file system, {@code isSameFile}
     * throws an {@link IOException} (specifically a
     * {@link java.nio.file.NoSuchFileException}). The method <em>cannot</em>
     * determine file identity for non-existent paths, so it fails fast with
     * a checked exception.</p>
     *
     * <p>This is a critical contract difference from {@link Path#equals}: two
     * {@code Path} objects can be equal (same string) even if neither file
     * exists, but {@code isSameFile} will still throw when they don't exist
     * (unless a specific provider implements the identity check without I/O).</p>
     *
     * <p><strong>Expected result:</strong> {@link java.nio.file.NoSuchFileException}
     * wrapped in {@link IOException}</p>
     */
    private void example09_onePathNonExistent() {
        log.info("--- Example 09: one path does not exist ---");
        Path existing    = BASE_DIR.resolve("alpha.txt");
        Path nonExistent = BASE_DIR.resolve("ghost_file_does_not_exist.txt");

        try {
            boolean result = Files.isSameFile(existing, nonExistent);
            log.info("isSameFile(existing, nonExistent) → {} [UNEXPECTED – expected exception]",
                    result);
        } catch (IOException e) {
            // This is the expected path
            log.info("IOException caught as expected → {}: {}",
                    e.getClass().getSimpleName(), e.getMessage());
        }
        log.info("");
    }

    /**
     * <h3>Example 10 – Both paths do not exist</h3>
     *
     * <p>When neither path exists, {@code isSameFile} is unable to resolve any
     * file-system identity and throws an {@link IOException} for the <em>first</em>
     * path it tries to look up. The exception is thrown before the second path is
     * even evaluated in most implementations.</p>
     *
     * <p>Developers sometimes expect {@code false} here (reasoning: "they can't be
     * the same if neither exists"). The spec disagrees: unknown identity → exception,
     * not a silent {@code false}.</p>
     *
     * <p><strong>Expected result:</strong> {@link java.nio.file.NoSuchFileException}</p>
     */
    private void example10_bothPathsNonExistent() {
        log.info("--- Example 10: both paths do not exist ---");
        Path ghost1 = BASE_DIR.resolve("phantom_one.txt");
        Path ghost2 = BASE_DIR.resolve("phantom_two.txt");

        try {
            boolean result = Files.isSameFile(ghost1, ghost2);
            log.info("isSameFile(ghost1, ghost2) → {} [UNEXPECTED – expected exception]",
                    result);
        } catch (IOException e) {
            log.info("IOException caught as expected → {}: {}",
                    e.getClass().getSimpleName(), e.getMessage());
        }
        log.info("");
    }

    /**
     * <h3>Example 11 – Comparing a directory with itself</h3>
     *
     * <p>{@code isSameFile} works for <em>directories</em> too, not just regular
     * files. The comparison logic is identical: the method resolves both paths and
     * compares their file-system identities. A directory compared to itself (or to
     * a path that resolves to the same directory) returns {@code true}.</p>
     *
     * <p>This is useful when you receive two {@code Path} objects pointing to a
     * project root from different sources (e.g., one resolved through an environment
     * variable, another relative to the classpath) and want to confirm they are the
     * same directory before performing a potentially destructive operation.</p>
     *
     * <p><strong>Expected result:</strong> {@code true}</p>
     */
    private void example11_directoryWithItself() {
        log.info("--- Example 11: directory compared with itself ---");
        Path dir1 = BASE_DIR;
        Path dir2 = BASE_DIR.resolve("subdir").resolve("..").normalize();

        log.info("dir1   : {}", dir1);
        log.info("dir2   : {}", dir2);
        log.info("equals(): {}", dir1.equals(dir2));  // may be false before normalisation

        try {
            boolean result = Files.isSameFile(dir1, dir2);
            log.info("isSameFile(dir1, dir2) → {}", result);
            // → true
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    // -----------------------------------------------------------------------
    // Utility helpers
    // -----------------------------------------------------------------------

    /**
     * Creates a UTF-8 text file at {@code path} containing {@code content} only if
     * the file does not already exist. This keeps {@link #setUp()} idempotent.
     *
     * @param path    the path where the file should be created
     * @param content the text to write into the file (UTF-8)
     * @throws IOException if the file cannot be created or written
     */
    private void createFileIfAbsent(Path path, String content) throws IOException {
        if (Files.notExists(path)) {
            Files.writeString(path, content);
            log.info("  Created: {}", path);
        } else {
            log.info("  Already exists: {}", path);
        }
    }
}