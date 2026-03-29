package com.desabisc.guide.java.io.jcopy;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

/**
 * <h2>Files.copy() - Complete NIO.2 Guide</h2>
 *
 * <p>This guide class demonstrates every overload and scenario of the
 * {@link Files#copy(Path, Path, CopyOption...)} family, using the NIO.2
 * (non-blocking I/O) API introduced in Java 7 and refined in Java 8+.</p>
 *
 * <h3>Three official overloads of {@code Files.copy()}</h3>
 * <pre>
 *  ① Files.copy(Path source,   Path target,     CopyOption... options)  → Path
 *  ② Files.copy(InputStream in, Path target,    CopyOption... options)  → long
 *  ③ Files.copy(Path source,   OutputStream out)                        → long
 * </pre>
 *
 * <h3>Available {@link StandardCopyOption} flags</h3>
 * <ul>
 *   <li>{@link StandardCopyOption#REPLACE_EXISTING} – overwrite target if it exists.</li>
 *   <li>{@link StandardCopyOption#COPY_ATTRIBUTES}  – copy metadata (timestamps, permissions).</li>
 *   <li>{@link StandardCopyOption#ATOMIC_MOVE}      – only valid for {@code Files.move()},
 *       <em>not</em> for copy.</li>
 * </ul>
 *
 * <h3>Available {@link LinkOption} flags</h3>
 * <ul>
 *   <li>{@link LinkOption#NOFOLLOW_LINKS} – copy the symbolic link itself,
 *       not its target.</li>
 * </ul>
 *
 * <h3>Working directory</h3>
 * <p>All paths used in this guide are rooted at
 * {@code C:\examples\java\io\copyeg}. The {@code setUp()} method creates
 * the required folder structure before any example runs.</p>
 *
 * <h3>Key behavioral rules to remember</h3>
 * <ol>
 *   <li>{@code Files.copy(src, dst)} on a <em>directory</em> copies the
 *       directory entry only - it does <strong>not</strong> recurse into it.</li>
 *   <li>Without {@code REPLACE_EXISTING}, copying to an existing path throws
 *       {@link FileAlreadyExistsException}.</li>
 *   <li>Copying a non-empty directory over another directory that already
 *       exists throws {@link DirectoryNotEmptyException} even with
 *       {@code REPLACE_EXISTING}.</li>
 *   <li>A recursive deep-copy requires a manual {@link FileVisitor} walk.</li>
 * </ol>
 *
 * @author  desabisc
 * @version 1.0
 * @since   Java 17
 * @see     Files
 * @see     StandardCopyOption
 * @see     LinkOption
 * @see     FileVisitor
 */
@Slf4j
public class FilesCopyGuide {

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTANTS
    // ─────────────────────────────────────────────────────────────────────────

    /** Root working directory for all examples in this guide. */
    private static final Path ROOT = Path.of("C:\\examples\\java\\io\\copyeg");

    /** Sub-directory that holds the source files used as copy origins. */
    private static final Path SRC_DIR  = ROOT.resolve("source");

    /** Sub-directory that receives copied files in most examples. */
    private static final Path DEST_DIR = ROOT.resolve("destination");

    /** Sub-directory used exclusively for stream-based copy examples. */
    private static final Path STREAM_DIR = ROOT.resolve("stream");

    /** Sub-directory used for symbolic-link / edge-case examples. */
    private static final Path EDGE_DIR = ROOT.resolve("edgecases");

    // ─────────────────────────────────────────────────────────────────────────
    // ENTRY POINT
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Application entry point. Runs the complete guide in a safe, ordered
     * sequence: setup → examples → cleanup.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        FilesCopyGuide guide = new FilesCopyGuide();

        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║         Files.copy()  -  NIO.2 Complete Guide           ║");
        log.info("╚══════════════════════════════════════════════════════════╝");

        try {
            guide.setUp();

            // ── Overload ①: Path → Path ──────────────────────────────────
            guide.demo01_basicFileCopy();
            guide.demo02_copyFileAlreadyExists_noFlag();
            guide.demo03_copyFileWithReplaceExisting();
            guide.demo04_copyFileWithCopyAttributes();
            guide.demo05_copyFileReplaceAndAttributes();

            // ── Directories ───────────────────────────────────────────────
            guide.demo06_shallowDirectoryCopy();
            guide.demo07_deepRecursiveDirectoryCopy();
            guide.demo08_copyDirectoryAlreadyExists();

            // ── Symbolic links ────────────────────────────────────────────
            guide.demo09_copySymlinkFollowsTarget();
            guide.demo10_copySymlinkItself_noFollowLinks();

            // ── Overload ②: InputStream → Path ───────────────────────────
            guide.demo11_copyFromInputStream();
            guide.demo12_copyFromInputStream_replaceExisting();

            // ── Overload ③: Path → OutputStream ──────────────────────────
            guide.demo13_copyToOutputStream();

            // ── Edge cases ────────────────────────────────────────────────
            guide.demo14_copyNonExistentSource();
            guide.demo15_copyFileToItself();
            guide.demo16_copyEmptyDirectory();
            guide.demo17_copyIntoMissingParent();
            guide.demo18_copyPreservesNoAttributesByDefault();

        } catch (IOException e) {
            log.error("Fatal error during guide execution", e);
        } finally {
            //guide.tearDown();
        }

        log.info("Guide finished.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SETUP & TEARDOWN
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates the entire folder / file scaffold required by all examples.
     *
     * <p>Structure created under {@code C:\examples\java\io\copyeg}:</p>
     * <pre>
     *  copyeg/
     *  ├── source/
     *  │   ├── hello.txt          ("Hello, NIO.2!")
     *  │   ├── config.properties  ("app.name=guide")
     *  │   └── subdir/
     *  │       └── nested.txt     ("Nested file content")
     *  ├── destination/           (empty - receives copies)
     *  ├── stream/                (empty - for stream demos)
     *  └── edgecases/             (empty - for symlink / edge demos)
     * </pre>
     *
     * @throws IOException if any directory or file cannot be created
     */
    private void setUp() throws IOException {
        log.info("──────────────────────────────────────────────────────────");
        log.info("[SETUP] Creating working directory scaffold ...");

        // Create all top-level directories
        for (Path dir : List.of(SRC_DIR, DEST_DIR, STREAM_DIR, EDGE_DIR)) {
            Files.createDirectories(dir);
        }

        // Source files
        Files.writeString(SRC_DIR.resolve("hello.txt"),         "Hello, NIO.2!");
        Files.writeString(SRC_DIR.resolve("config.properties"), "app.name=guide");

        // Nested sub-directory inside source
        Path subDir = SRC_DIR.resolve("subdir");
        Files.createDirectories(subDir);
        Files.writeString(subDir.resolve("nested.txt"), "Nested file content");

        log.info("[SETUP] Scaffold ready under: {}", ROOT);
        log.info("──────────────────────────────────────────────────────────");
    }

    /**
     * Removes the entire working directory tree created by {@link #setUp()}.
     *
     * <p>Uses a depth-first {@link Files#walk(Path, FileVisitOption...)} so
     * that files are deleted before their parent directories.</p>
     */
    private void tearDown() {
        log.info("──────────────────────────────────────────────────────────");
        log.info("[TEARDOWN] Cleaning up working directory …");
        try (Stream<Path> walk = Files.walk(ROOT)) {
            walk.sorted(java.util.Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        log.warn("[TEARDOWN] Could not delete: {} - {}", path, e.getMessage());
                    }
                });
            log.info("[TEARDOWN] Cleanup complete.");
        } catch (IOException e) {
            log.warn("[TEARDOWN] Walk failed: {}", e.getMessage());
        }
    }

    // =========================================================================
    // DEMO 01 - Basic file copy (Path → Path, no options)
    // =========================================================================

    /**
     * <b>Demo 01 - Basic file copy (no {@link CopyOption}s).</b>
     *
     * <p>The simplest overload: copy a single regular file from a source path
     * to a target path. When no options are supplied:</p>
     * <ul>
     *   <li>The target file is created atomically.</li>
     *   <li>File <em>metadata</em> (timestamps, permissions) is <strong>not</strong>
     *       copied - the target gets the current time as its creation date.</li>
     *   <li>If the target already exists the JVM throws
     *       {@link FileAlreadyExistsException}.</li>
     * </ul>
     *
     * <pre>{@code
     *  Path copied = Files.copy(source, target);
     *  // copied == target (the method returns the target path)
     * }</pre>
     *
     * @throws IOException if the source cannot be read or the target cannot
     *                     be created
     */
    private void demo01_basicFileCopy() throws IOException {
        log.info("=== Demo 01 - Basic file copy (no options) ===");

        Path source = SRC_DIR.resolve("hello.txt");
        Path target = DEST_DIR.resolve("hello_copy.txt");

        Path result = Files.copy(source, target);

        log.info("  Source  : {}", source);
        log.info("  Target  : {}", result);
        log.info("  Content : {}", Files.readString(result));
        log.info("  Exists  : {}", Files.exists(result));
        log.info("  Size    : {} bytes", Files.size(result));
        log.info("  RULE    : No options -> metadata NOT copied, target must not exist.");
    }

    // =========================================================================
    // DEMO 02 - Copy when target already exists, without REPLACE_EXISTING
    // =========================================================================

    /**
     * <b>Demo 02 - Target already exists, no {@link StandardCopyOption#REPLACE_EXISTING}.</b>
     *
     * <p>Demonstrates the {@link FileAlreadyExistsException} that the JVM
     * throws when the destination path already contains a file and the
     * {@code REPLACE_EXISTING} option was <em>not</em> provided.</p>
     *
     * <p>This is the most common beginner mistake with {@code Files.copy()}.
     * Always check whether the target exists beforehand, or supply
     * {@code REPLACE_EXISTING} when overwriting is intentional.</p>
     *
     * <pre>{@code
     *  // ✗ Throws FileAlreadyExistsException if target exists
     *  Files.copy(source, existingTarget);
     *
     *  // ✓ Safe - overwrites silently
     *  Files.copy(source, existingTarget, StandardCopyOption.REPLACE_EXISTING);
     * }</pre>
     *
     * @throws IOException if an unexpected I/O error occurs
     */
    private void demo02_copyFileAlreadyExists_noFlag() throws IOException {
        log.info("=== Demo 02 - Copy to existing target WITHOUT REPLACE_EXISTING ===");

        Path source = SRC_DIR.resolve("hello.txt");
        Path target = DEST_DIR.resolve("hello_copy.txt"); // created in Demo 01

        log.info("  Target already exists: {}", Files.exists(target));

        try {
            Files.copy(source, target); // ← no options supplied
            log.warn("  [UNEXPECTED] Copy succeeded - this should not happen!");
        } catch (FileAlreadyExistsException e) {
            log.info("  [EXPECTED] FileAlreadyExistsException: {}", e.getFile());
            log.info("  FIX -> supply StandardCopyOption.REPLACE_EXISTING to overwrite safely.");
        }
    }

    // =========================================================================
    // DEMO 03 - Copy with REPLACE_EXISTING
    // =========================================================================

    /**
     * <b>Demo 03 - Overwriting an existing target with
     * {@link StandardCopyOption#REPLACE_EXISTING}.</b>
     *
     * <p>{@code REPLACE_EXISTING} instructs the JVM to:</p>
     * <ul>
     *   <li>Silently delete the existing target and write the new copy.</li>
     *   <li>If the target is a <em>symbolic link</em>, replace the link itself
     *       (not the link's target file).</li>
     *   <li>If the target is a <em>non-empty directory</em>, still throw
     *       {@link DirectoryNotEmptyException} - use a recursive walk to
     *       delete it first (see {@link #demo07_deepRecursiveDirectoryCopy()}).</li>
     * </ul>
     *
     * @throws IOException if the copy operation fails
     */
    private void demo03_copyFileWithReplaceExisting() throws IOException {
        log.info("=== Demo 03 - Copy with REPLACE_EXISTING ===");

        Path source = SRC_DIR.resolve("config.properties");
        Path target = DEST_DIR.resolve("hello_copy.txt"); // already exists from Demo 01

        log.info("  Target before copy : {}", Files.readString(target));

        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

        log.info("  Target after copy  : {}", Files.readString(target));
        log.info("  RULE: REPLACE_EXISTING silently overwrites regular files and symlinks.");
    }

    // =========================================================================
    // DEMO 04 - Copy with COPY_ATTRIBUTES
    // =========================================================================

    /**
     * <b>Demo 04 - Preserving metadata with
     * {@link StandardCopyOption#COPY_ATTRIBUTES}.</b>
     *
     * <p>By default, {@code Files.copy()} creates the target with the current
     * system time as its last-modified timestamp. When {@code COPY_ATTRIBUTES}
     * is specified, the JVM attempts to transfer the following metadata:</p>
     * <ul>
     *   <li>Last-modified time ({@link BasicFileAttributes#lastModifiedTime()}).</li>
     *   <li>Creation time (platform-dependent).</li>
     *   <li>POSIX permissions (on UNIX/Linux/macOS).</li>
     *   <li>DOS attributes (read-only, hidden) on Windows.</li>
     * </ul>
     *
     * <p><b>Platform note:</b> Not all attributes are portable. Attributes
     * unsupported by the target file system are silently skipped.</p>
     *
     * <pre>{@code
     *  Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
     * }</pre>
     *
     * @throws IOException if the copy or attribute read fails
     */
    private void demo04_copyFileWithCopyAttributes() throws IOException {
        log.info("=== Demo 04 - Copy with COPY_ATTRIBUTES ===");

        Path source = SRC_DIR.resolve("hello.txt");
        Path target = DEST_DIR.resolve("hello_with_attrs.txt");

        // Record source timestamp before copying
        FileTime srcModified = Files.getLastModifiedTime(source);
        log.info("  Source last-modified : {}", srcModified);

        Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);

        FileTime dstModified = Files.getLastModifiedTime(target);
        log.info("  Target last-modified : {}", dstModified);

        boolean timesMatch = srcModified.equals(dstModified);
        log.info("  Timestamps match     : {}", timesMatch);
        log.info("  RULE: COPY_ATTRIBUTES preserves metadata; exact attributes depend on the OS.");
    }

    // =========================================================================
    // DEMO 05 - Combining REPLACE_EXISTING + COPY_ATTRIBUTES
    // =========================================================================

    /**
     * <b>Demo 05 - Combining multiple {@link CopyOption}s.</b>
     *
     * <p>{@code CopyOption} arguments are variadic - you can supply any
     * combination as a comma-separated list. Combining
     * {@link StandardCopyOption#REPLACE_EXISTING} and
     * {@link StandardCopyOption#COPY_ATTRIBUTES} is the safest, most complete
     * form of a file copy in production code:</p>
     *
     * <pre>{@code
     *  Files.copy(source, target,
     *      StandardCopyOption.REPLACE_EXISTING,
     *      StandardCopyOption.COPY_ATTRIBUTES);
     * }</pre>
     *
     * <p>This pattern is recommended for backup utilities, file synchronisation
     * tools, and any scenario where both idempotence and metadata fidelity
     * matter.</p>
     *
     * @throws IOException if the copy fails
     */
    private void demo05_copyFileReplaceAndAttributes() throws IOException {
        log.info("=== Demo 05 - REPLACE_EXISTING + COPY_ATTRIBUTES combined ===");

        Path source = SRC_DIR.resolve("hello.txt");
        Path target = DEST_DIR.resolve("hello_with_attrs.txt"); // already exists

        // Force a known old timestamp on the target to make the demo observable
        Files.setLastModifiedTime(target, FileTime.from(Instant.parse("2000-01-01T00:00:00Z")));
        log.info("  Target timestamp set to: {}", Files.getLastModifiedTime(target));

        Files.copy(source, target,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES);

        log.info("  Source timestamp: {}", Files.getLastModifiedTime(source));
        log.info("  Target timestamp: {}", Files.getLastModifiedTime(target));
        log.info("  RULE: Multiple options -> safe overwrite + full metadata transfer.");
    }

    // =========================================================================
    // DEMO 06 - Shallow directory copy (directory entry only)
    // =========================================================================

    /**
     * <b>Demo 06 - Shallow directory copy.</b>
     *
     * <p>{@code Files.copy()} applied to a <em>directory</em> copies
     * <strong>only the directory entry</strong> - it creates an empty
     * directory at the destination and stops there. The children (files and
     * subdirectories) inside the source directory are <em>not</em> copied.</p>
     *
     * <p>This is by design and often surprises developers coming from
     * {@code File.renameTo()} or shell {@code cp -r}. To perform a deep
     * (recursive) copy, see {@link #demo07_deepRecursiveDirectoryCopy()}.</p>
     *
     * <pre>{@code
     *  // Copies the directory shell - does NOT recurse
     *  Files.copy(srcDir, destDir);
     * }</pre>
     *
     * @throws IOException if the directory cannot be created at the target
     */
    private void demo06_shallowDirectoryCopy() throws IOException {
        log.info("=== Demo 06 - Shallow directory copy (entry only) ===");

        Path source = SRC_DIR.resolve("subdir");
        Path target = DEST_DIR.resolve("subdir_shallow");

        Files.copy(source, target);

        log.info("  Source children : {}", listChildren(source));
        log.info("  Target children : {}", listChildren(target));   // <- empty!
        log.info("  RULE: Files.copy() on a directory is ALWAYS shallow - children are ignored.");
    }

    // =========================================================================
    // DEMO 07 - Deep recursive directory copy using FileVisitor
    // =========================================================================

    /**
     * <b>Demo 07 - Deep (recursive) directory copy using
     * {@link Files#walkFileTree(Path, FileVisitor)}.</b>
     *
     * <p>Because {@code Files.copy()} is inherently shallow for directories,
     * a recursive copy requires visiting every node of the source tree via a
     * {@link FileVisitor}. The strategy is:</p>
     * <ol>
     *   <li>{@link FileVisitor#preVisitDirectory} - recreate the directory at
     *       the corresponding target path.</li>
     *   <li>{@link FileVisitor#visitFile} - copy each file with
     *       {@code COPY_ATTRIBUTES} and {@code REPLACE_EXISTING}.</li>
     *   <li>{@link FileVisitor#visitFileFailed} - log the error and
     *       {@link FileVisitResult#CONTINUE} (fail-soft).</li>
     * </ol>
     *
     * <p>The helper {@link #buildDeepCopyVisitor(Path, Path)} constructs this
     * visitor as an anonymous {@link SimpleFileVisitor} subclass.</p>
     *
     * @throws IOException if any part of the tree walk fails critically
     */
    private void demo07_deepRecursiveDirectoryCopy() throws IOException {
        log.info("=== Demo 07 - Deep recursive directory copy ===");

        Path source = SRC_DIR;
        Path target = DEST_DIR.resolve("source_deep_copy");

        Files.walkFileTree(source, buildDeepCopyVisitor(source, target));

        log.info("  Deep copy complete. Target tree:");
        try (Stream<Path> walk = Files.walk(target)) {
            walk.forEach(p -> log.info("    {}", p));
        }
        log.info("  RULE: Recursive copy requires FileVisitor - Files.copy() alone is not enough.");
    }

    /**
     * Builds a {@link SimpleFileVisitor} that performs a deep copy from
     * {@code srcRoot} to {@code dstRoot}.
     *
     * <p>The visitor resolves each visited path <em>relative</em> to
     * {@code srcRoot} and constructs the matching path under {@code dstRoot},
     * preserving the entire directory structure.</p>
     *
     * <p><b>Design note</b> - {@link StandardCopyOption#REPLACE_EXISTING} is
     * always included so that re-running the copy is idempotent (safe to call
     * multiple times without failing on the second run).</p>
     *
     * @param srcRoot the root of the source tree
     * @param dstRoot the root of the destination tree (created if absent)
     * @return a configured {@link SimpleFileVisitor} ready for
     *         {@link Files#walkFileTree(Path, FileVisitor)}
     */
    private SimpleFileVisitor<Path> buildDeepCopyVisitor(Path srcRoot, Path dstRoot) {
        return new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir,
                                                     BasicFileAttributes attrs)
                    throws IOException {
                Path targetDir = dstRoot.resolve(srcRoot.relativize(dir));
                Files.createDirectories(targetDir);
                log.info("    [DIR ] Created : {}", targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file,
                                             BasicFileAttributes attrs)
                    throws IOException {
                Path targetFile = dstRoot.resolve(srcRoot.relativize(file));
                Files.copy(file, targetFile,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES);
                log.info("    [FILE] Copied  : {}", targetFile);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                log.warn("    [WARN] Could not copy: {} - {}", file, exc.getMessage());
                return FileVisitResult.CONTINUE; // fail-soft: keep copying other files
            }
        };
    }

    // =========================================================================
    // DEMO 08 - Copy directory when target already exists
    // =========================================================================

    /**
     * <b>Demo 08 - Copying a directory whose target already exists.</b>
     *
     * <p>Two distinct exceptions can arise depending on the state of the
     * target directory:</p>
     * <ul>
     *   <li>{@link FileAlreadyExistsException} - target exists and
     *       {@code REPLACE_EXISTING} was <em>not</em> supplied.</li>
     *   <li>{@link DirectoryNotEmptyException} - target is a
     *       <em>non-empty</em> directory and {@code REPLACE_EXISTING}
     *       <em>was</em> supplied. The JVM refuses to delete a non-empty
     *       directory automatically.</li>
     * </ul>
     *
     * <p><b>Solution for non-empty target directories:</b> delete the target
     * tree recursively first (with {@link Files#walkFileTree}), then copy.</p>
     *
     * @throws IOException if an unexpected I/O error occurs
     */
    private void demo08_copyDirectoryAlreadyExists() throws IOException {
        log.info("=== Demo 08 - Copy directory over existing non-empty target ===");

        Path source = SRC_DIR.resolve("subdir");
        Path target = DEST_DIR.resolve("subdir_shallow"); // created (non-empty) in Demo 06

        // Add a child so the target is definitively non-empty
        Files.writeString(target.resolve("occupied.txt"), "I am here");

        // ── Case A: no REPLACE_EXISTING ──────────────────────────────────────
        try {
            Files.copy(source, target);
        } catch (FileAlreadyExistsException e) {
            log.info("  [Case A] FileAlreadyExistsException (no REPLACE_EXISTING): {}",
                    e.getFile());
        }

        // ── Case B: with REPLACE_EXISTING but target is non-empty ─────────────
        try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (DirectoryNotEmptyException e) {
            log.info("  [Case B] DirectoryNotEmptyException (non-empty target): {}",
                    e.getFile());
            log.info("  FIX -> delete the target tree first, then copy.");
        }
    }

    // =========================================================================
    // DEMO 09 - Copy a symbolic link (follows target by default)
    // =========================================================================

    /**
     * <b>Demo 09 - Copying a symbolic link - default behaviour
     * (link is followed).</b>
     *
     * <p>By default, {@code Files.copy()} <em>follows</em> symbolic links.
     * This means:</p>
     * <ul>
     *   <li>The <em>content</em> of the link's target file is copied.</li>
     *   <li>The new file at the destination is a regular file, <strong>not</strong>
     *       a symbolic link.</li>
     * </ul>
     *
     * <p><b>Platform note:</b> Symbolic link creation requires elevated
     * privileges on Windows unless Developer Mode is enabled. If the link
     * cannot be created, this demo logs a warning and returns gracefully.</p>
     *
     * @throws IOException if an unexpected I/O error occurs
     */
    private void demo09_copySymlinkFollowsTarget() throws IOException {
        log.info("=== Demo 09 - Copy symbolic link (follows target) ===");

        Path realFile = SRC_DIR.resolve("hello.txt");
        Path link     = EDGE_DIR.resolve("hello_link.txt");
        Path target   = EDGE_DIR.resolve("followed_copy.txt");

        try {
            Files.deleteIfExists(link);
            Files.createSymbolicLink(link, realFile);
            log.info("  Symlink created: {} → {}", link, realFile);
        } catch (UnsupportedOperationException | IOException e) {
            log.warn("  [SKIP] Symlink creation not supported on this OS/user: {}", e.getMessage());
            return;
        }

        Files.copy(link, target, StandardCopyOption.REPLACE_EXISTING);

        log.info("  Target is symlink  : {}", Files.isSymbolicLink(target));  // false
        log.info("  Target content     : {}", Files.readString(target));
        log.info("  RULE: Default → link is followed; target is a regular file copy of the link's data.");
    }

    // =========================================================================
    // DEMO 10 - Copy the symbolic link itself (NOFOLLOW_LINKS)
    // =========================================================================

    /**
     * <b>Demo 10 - Copying the symbolic link <em>itself</em> with
     * {@link LinkOption#NOFOLLOW_LINKS}.</b>
     *
     * <p>When {@code NOFOLLOW_LINKS} is supplied, the JVM copies the symbolic
     * link entry rather than its target. The resulting destination path is
     * itself a symbolic link pointing to the same destination as the source
     * link.</p>
     *
     * <p>Use this option when you need to replicate a directory tree that
     * contains symlinks and want to preserve the link structure (e.g., backup
     * tools, installers).</p>
     *
     * <pre>{@code
     *  Files.copy(symlink, target,
     *      LinkOption.NOFOLLOW_LINKS,
     *      StandardCopyOption.REPLACE_EXISTING);
     * }</pre>
     *
     * @throws IOException if an unexpected I/O error occurs
     */
    private void demo10_copySymlinkItself_noFollowLinks() throws IOException {
        log.info("=== Demo 10 - Copy the symlink itself (NOFOLLOW_LINKS) ===");

        Path link   = EDGE_DIR.resolve("hello_link.txt");
        Path target = EDGE_DIR.resolve("link_copy.txt");

        if (!Files.exists(link, LinkOption.NOFOLLOW_LINKS)) {
            log.warn("  [SKIP] Symlink from Demo 09 not available; skipping.");
            return;
        }

        Files.copy(link, target,
                LinkOption.NOFOLLOW_LINKS,
                StandardCopyOption.REPLACE_EXISTING);

        log.info("  Target is symlink    : {}", Files.isSymbolicLink(target));  // true
        log.info("  Target link target   : {}", Files.readSymbolicLink(target));
        log.info("  RULE: NOFOLLOW_LINKS → copies the link itself, not the data it points to.");
    }

    // =========================================================================
    // DEMO 11 - InputStream → Path (overload ②)
    // =========================================================================

    /**
     * <b>Demo 11 - Copying an {@link InputStream} into a file
     * (overload ②).</b>
     *
     * <p>Signature:
     * <pre>{@code
     *  long Files.copy(InputStream in, Path target, CopyOption... options)
     * }</pre>
     *
     * <p>This overload is ideal for:</p>
     * <ul>
     *   <li>Saving HTTP response bodies to disk.</li>
     *   <li>Writing class-path resources (e.g., {@code getResourceAsStream()})
     *       to the file system.</li>
     *   <li>Any scenario where data arrives as a stream rather than a file.</li>
     * </ul>
     *
     * <p>The method reads the stream until EOF and returns the total number of
     * bytes written. <strong>The stream is NOT closed by this method</strong>
     * - always wrap in try-with-resources.</p>
     *
     * @throws IOException if the stream cannot be read or the file created
     */
    private void demo11_copyFromInputStream() throws IOException {
        log.info("=== Demo 11 - InputStream → Path (overload ②) ===");

        Path target = STREAM_DIR.resolve("from_stream.txt");

        // Simulate a network / resource stream with a plain byte array
        byte[] data = "Data arriving from a stream source".getBytes();

        try (InputStream in = new java.io.ByteArrayInputStream(data)) {
            long bytesWritten = Files.copy(in, target);
            log.info("  Target   : {}", target);
            log.info("  Written  : {} bytes", bytesWritten);
            log.info("  Content  : {}", Files.readString(target));
        }
        log.info("  RULE: Stream is NOT closed by Files.copy() - always use try-with-resources.");
    }

    // =========================================================================
    // DEMO 12 - InputStream → Path with REPLACE_EXISTING
    // =========================================================================

    /**
     * <b>Demo 12 - {@link InputStream} → {@link Path} overwrite with
     * {@link StandardCopyOption#REPLACE_EXISTING}.</b>
     *
     * <p>Just as with the {@code Path → Path} overload, supplying
     * {@code REPLACE_EXISTING} prevents {@link FileAlreadyExistsException}
     * when the target already exists.</p>
     *
     * <p>Only {@link StandardCopyOption#REPLACE_EXISTING} is valid for this
     * overload. {@link StandardCopyOption#COPY_ATTRIBUTES} and
     * {@link LinkOption#NOFOLLOW_LINKS} are <strong>not</strong> accepted
     * and will throw {@link UnsupportedOperationException} at runtime.</p>
     *
     * @throws IOException if the copy fails
     */
    private void demo12_copyFromInputStream_replaceExisting() throws IOException {
        log.info("=== Demo 12 - InputStream → Path with REPLACE_EXISTING ===");

        Path target = STREAM_DIR.resolve("from_stream.txt"); // already exists from Demo 11

        byte[] newData = "UPDATED content from a new stream".getBytes();

        try (InputStream in = new java.io.ByteArrayInputStream(newData)) {
            long bytes = Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("  Overwritten with {} bytes", bytes);
            log.info("  New content: {}", Files.readString(target));
        }
        log.info("  RULE: For InputStream overload, only REPLACE_EXISTING is a valid option.");
    }

    // =========================================================================
    // DEMO 13 - Path → OutputStream (overload ③)
    // =========================================================================

    /**
     * <b>Demo 13 - Copying a file's content to an {@link OutputStream}
     * (overload ③).</b>
     *
     * <p>Signature:
     * <pre>{@code
     *  long Files.copy(Path source, OutputStream out)
     * }</pre>
     *
     * <p>Use cases:</p>
     * <ul>
     *   <li>Streaming a file as an HTTP response body
     *       ({@code response.getOutputStream()}).</li>
     *   <li>Piping file content into a compression or encryption stream.</li>
     *   <li>Writing file bytes into an in-memory buffer for unit testing.</li>
     * </ul>
     *
     * <p>This overload accepts <strong>no</strong> {@link CopyOption}
     * arguments. <strong>The stream is NOT closed by this method.</strong></p>
     *
     * @throws IOException if the file cannot be read or the stream written
     */
    private void demo13_copyToOutputStream() throws IOException {
        log.info("=== Demo 13 - Path -> OutputStream (overload 3) ===");

        Path source = SRC_DIR.resolve("hello.txt");

        try (OutputStream out = new java.io.ByteArrayOutputStream()) {
            long bytes = Files.copy(source, out);
            log.info("  Source  : {}", source);
            log.info("  Flushed : {} bytes to OutputStream", bytes);
            log.info("  Buffer  : {}", out.toString());
        }
        log.info("  RULE: No CopyOption supported; stream is NOT closed by Files.copy().");
    }

    // =========================================================================
    // DEMO 14 - Source does not exist
    // =========================================================================

    /**
     * <b>Demo 14 - Source path does not exist
     * ({@link NoSuchFileException}).</b>
     *
     * <p>When the source path does not resolve to an existing file or
     * directory, {@code Files.copy()} throws {@link NoSuchFileException}.
     * This is distinct from {@link java.io.FileNotFoundException}
     * (the old {@code java.io} API) - always catch the NIO.2 variant when
     * using {@link Files}.</p>
     *
     * <pre>{@code
     *  // ✗ Source missing → NoSuchFileException
     *  Files.copy(Path.of("ghost.txt"), target);
     * }</pre>
     *
     * @throws IOException if an unexpected error occurs
     */
    private void demo14_copyNonExistentSource() throws IOException {
        log.info("=== Demo 14 - Source does not exist (NoSuchFileException) ===");

        Path ghost  = SRC_DIR.resolve("ghost.txt"); // intentionally absent
        Path target = DEST_DIR.resolve("ghost_copy.txt");

        try {
            Files.copy(ghost, target);
        } catch (NoSuchFileException e) {
            log.info("  [EXPECTED] NoSuchFileException: {}", e.getFile());
            log.info("  FIX -> validate with Files.exists(source) before copying.");
        }
    }

    // =========================================================================
    // DEMO 15 - Copy a file to itself
    // =========================================================================

    /**
     * <b>Demo 15 - Copying a file to itself.</b>
     *
     * <p>When source and target resolve to the same file (detected via
     * {@link Files#isSameFile(Path, Path)}), the JVM's behaviour is
     * implementation-defined but typically a no-op or throws an exception.
     * Always guard against this in production utilities.</p>
     *
     * <p>The safeguard shown below uses {@link Files#isSameFile(Path, Path)}
     * which compares the underlying file-system inodes, so it catches
     * aliased paths (e.g., {@code ./foo.txt} vs {@code foo.txt}).</p>
     *
     * <pre>{@code
     *  if (Files.isSameFile(source, target)) {
     *      log.warn("Source and target are the same file - copy skipped.");
     *      return;
     *  }
     * }</pre>
     *
     * @throws IOException if the same-file check fails
     */
    private void demo15_copyFileToItself() throws IOException {
        log.info("=== Demo 15 - Copy a file to itself ===");

        Path source = SRC_DIR.resolve("hello.txt");
        Path target = SRC_DIR.resolve("hello.txt"); // same path

        if (Files.isSameFile(source, target)) {
            log.info("  [GUARDED] Source == Target (isSameFile). Copy skipped.");
            log.info("  RULE: Always guard with Files.isSameFile() in copy utilities.");
            return;
        }

        // Reaching here means different physical files - proceed normally
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        log.info("  Copy completed (paths are different physical files).");
    }

    // =========================================================================
    // DEMO 16 - Copy an empty directory
    // =========================================================================

    /**
     * <b>Demo 16 - Copying an empty directory.</b>
     *
     * <p>An empty directory is the one case where {@code Files.copy()} on a
     * directory produces a fully equivalent result at the target - because
     * there are no children to miss. The call creates an empty directory at
     * the destination.</p>
     *
     * <p>This is also the foundation of the recursive-copy visitor in
     * {@link #buildDeepCopyVisitor(Path, Path)}: {@code preVisitDirectory}
     * always meets an effectively "empty" target because children are handled
     * sequentially afterward by {@code visitFile}.</p>
     *
     * @throws IOException if the directory cannot be created
     */
    private void demo16_copyEmptyDirectory() throws IOException {
        log.info("=== Demo 16 - Copy an empty directory ===");

        Path emptySource = EDGE_DIR.resolve("empty_src");
        Path target      = EDGE_DIR.resolve("empty_dst");

        Files.createDirectories(emptySource);

        Files.copy(emptySource, target, StandardCopyOption.REPLACE_EXISTING);

        log.info("  Target is directory : {}", Files.isDirectory(target));
        log.info("  Target children     : {}", listChildren(target));
        log.info("  RULE: Copying an empty directory is the only shallow copy that is complete.");
    }

    // =========================================================================
    // DEMO 17 - Target parent directory does not exist
    // =========================================================================

    /**
     * <b>Demo 17 - Target parent directory does not exist
     * ({@link NoSuchFileException}).</b>
     *
     * <p>Unlike {@code mkdir -p} or {@code File.mkdirs()}, {@code Files.copy()}
     * does <strong>not</strong> create missing intermediate directories. If the
     * parent of the target path does not exist, the method throws
     * {@link NoSuchFileException}.</p>
     *
     * <p><b>Fix pattern:</b> always call
     * {@link Files#createDirectories(Path, java.nio.file.attribute.FileAttribute[])}
     * on the target's parent before copying.</p>
     *
     * <pre>{@code
     *  Files.createDirectories(target.getParent());
     *  Files.copy(source, target);
     * }</pre>
     *
     * @throws IOException if an unexpected error occurs
     */
    private void demo17_copyIntoMissingParent() throws IOException {
        log.info("=== Demo 17 - Target parent does not exist (NoSuchFileException) ===");

        Path source = SRC_DIR.resolve("hello.txt");
        Path target = DEST_DIR.resolve("nonexistent_dir").resolve("child").resolve("deep.txt");

        // ── Attempt without creating parent ──────────────────────────────────
        try {
            Files.copy(source, target);
        } catch (NoSuchFileException e) {
            log.info("  [EXPECTED] NoSuchFileException: missing parent '{}'", e.getFile());
        }

        // ── Correct approach: ensure parent exists first ──────────────────────
        Files.createDirectories(target.getParent());
        Files.copy(source, target);
        log.info("  [FIXED] File copied after createDirectories(): {}", Files.exists(target));
        log.info("  RULE: Files.copy() never creates missing parent directories - do it yourself.");
    }

    // =========================================================================
    // DEMO 18 - Default copy does NOT preserve timestamps
    // =========================================================================

    /**
     * <b>Demo 18 - Confirming that default copy does <em>not</em> preserve
     * timestamps.</b>
     *
     * <p>This demo intentionally sets the source file's last-modified time to
     * a fixed historical date, performs a plain {@code Files.copy()} with no
     * options, and then confirms that the target's timestamp is <em>different</em>
     * (it reflects the time of the copy operation, not the original file).</p>
     *
     * <p>Contrasted with {@link #demo04_copyFileWithCopyAttributes()}, this
     * demonstrates exactly what attribute information is <em>lost</em> when
     * {@link StandardCopyOption#COPY_ATTRIBUTES} is omitted.</p>
     *
     * @throws IOException if the copy or attribute inspection fails
     */
    private void demo18_copyPreservesNoAttributesByDefault() throws IOException {
        log.info("=== Demo 18 - Default copy does NOT preserve timestamps ===");

        Path source = SRC_DIR.resolve("config.properties");
        Path target = DEST_DIR.resolve("config_no_attrs.properties");

        // Force source to a known historical timestamp
        FileTime historicalTime = FileTime.from(Instant.parse("1999-12-31T23:59:59Z"));
        Files.setLastModifiedTime(source, historicalTime);
        log.info("  Source last-modified (forced): {}", Files.getLastModifiedTime(source));

        Files.copy(source, target); // no COPY_ATTRIBUTES

        FileTime targetTime = Files.getLastModifiedTime(target);
        log.info("  Target last-modified (after copy): {}", targetTime);

        boolean preserved = historicalTime.equals(targetTime);
        log.info("  Timestamp preserved: {} (expected false)", preserved);
        log.info("  RULE: Without COPY_ATTRIBUTES, the target receives the current system time.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE UTILITY
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a list of the <em>direct</em> children of {@code dir}, or an
     * empty list if the directory does not exist or an I/O error occurs.
     *
     * <p>Intended only for logging within this guide class - not a
     * production-grade directory listing utility.</p>
     *
     * @param dir the directory to inspect
     * @return a {@link List} of direct child {@link Path}s (names only)
     */
    private List<String> listChildren(Path dir) {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.map(p -> p.getFileName().toString()).toList();
        } catch (IOException e) {
            return List.of("<error: " + e.getMessage() + ">");
        }
    }
}