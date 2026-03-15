package com.desabisc.guide.java.io.htotrealpath;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * <h2>Comprehensive Guide: {@code Path.toRealPath()} - NIO.2 File API</h2>
 *
 * <p>This class demonstrates every significant behaviour and edge case of the
 * {@link Path#toRealPath(LinkOption...)} method introduced in Java 7 as part of
 * the NIO.2 (JSR-203) specification.</p>
 *
 * <h3>What is {@code toRealPath()}?</h3>
 * <p>{@code toRealPath()} returns the <em>real</em>, canonical, absolute path of
 * an existing file-system entry. It differs from the two superficially similar
 * alternatives in the following ways:</p>
 *
 * <table border="1" cellpadding="6">
 *   <thead>
 *     <tr>
 *       <th>Method</th>
 *       <th>Absolute?</th>
 *       <th>Normalised?</th>
 *       <th>Resolves symlinks?</th>
 *       <th>Requires file to exist?</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@code toAbsolutePath()}</td>
 *       <td>✔</td>
 *       <td>✘</td>
 *       <td>✘</td>
 *       <td>✘</td>
 *     </tr>
 *     <tr>
 *       <td>{@code normalize()}</td>
 *       <td>✘ (preserves relativity)</td>
 *       <td>✔</td>
 *       <td>✘</td>
 *       <td>✘</td>
 *     </tr>
 *     <tr>
 *       <td>{@code toRealPath()}</td>
 *       <td>✔</td>
 *       <td>✔</td>
 *       <td>✔ (by default)</td>
 *       <td>✔ - throws {@link IOException} otherwise</td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * <h3>Working directory assumed by all examples</h3>
 * <pre>C:\tests\java\io</pre>
 *
 * <h3>Prerequisites / test fixtures</h3>
 * <p>Before running this guide the helper method {@link #setupTestFixtures()}
 * creates every file, directory, and symbolic link that the examples need.
 * All artefacts live under {@code C:\tests\java\io}.</p>
 *
 * <h3>Dependencies</h3>
 * <ul>
 *   <li>JDK 11+ (NIO.2 has been stable since Java 7; records used for display only)</li>
 *   <li>Lombok {@code @Slf4j} for structured logging</li>
 * </ul>
 *
 * @author  desabisc
 * @version 1.0
 * @since   2026-03-15
 * @see     Path#toRealPath(LinkOption...)
 * @see     LinkOption#NOFOLLOW_LINKS
 * @see     Files
 */
@Slf4j
public class ToRealPathGuide {

    // -----------------------------------------------------------------------
    // Constants - working directory and fixture layout
    // -----------------------------------------------------------------------

    /** Root working directory for every example in this guide. */
    private static final Path WORK_DIR = Path.of("C:\\tests\\java\\io");

    /** A plain, always-present text file used as the canonical target. */
    private static final Path REAL_FILE = WORK_DIR.resolve("realFile.txt");

    /**
     * A directory that contains redundant {@code .} and {@code ..} segments so
     * we can showcase path normalisation performed by {@code toRealPath()}.
     */
    private static final Path DOTDOT_PATH =
            WORK_DIR.resolve("subdir\\..\\realFile.txt");

    /** A directory used in directory-resolution examples. */
    private static final Path SUB_DIR = WORK_DIR.resolve("subdir");

    /** A file that does <em>not</em> exist - used in the exception-handling example. */
    private static final Path GHOST_FILE = WORK_DIR.resolve("ghost.txt");

    /**
     * Symbolic link pointing to {@link #REAL_FILE}.
     * Created during {@link #setupTestFixtures()}.
     */
    private static final Path SYM_LINK = WORK_DIR.resolve("linkToRealFile.lnk");

    /**
     * Symbolic link that targets another symbolic link (chained), giving
     * {@code toRealPath()} the opportunity to chase multiple link hops.
     */
    private static final Path CHAINED_LINK = WORK_DIR.resolve("chainedLink.lnk");

    /**
     * A symbolic link whose target has been deleted after the link was created.
     * Used in the dangling-link edge case.
     */
    private static final Path DANGLING_LINK = WORK_DIR.resolve("danglingLink.lnk");

    /** Temporary target for the dangling-link scenario. */
    private static final Path DANGLING_TARGET = WORK_DIR.resolve("tempTarget.txt");

    // -----------------------------------------------------------------------
    // Entry point
    // -----------------------------------------------------------------------

    /**
     * Application entry point.
     *
     * <p>Runs every demonstration method in a logical order, preceded by fixture
     * setup and followed by cleanup.</p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("   toRealPath() Guide - NIO.2 File API");
        log.info("   Working directory: {}", WORK_DIR);
        log.info("═══════════════════════════════════════════════════════════");

        setupTestFixtures();

        demonstrateBasicRealPath();
        demonstrateDotDotNormalisation();
        demonstrateRelativePathResolution();
        demonstrateFollowLinksDefault();
        demonstrateNoFollowLinks();
        demonstrateChainedSymLinks();
        demonstrateNonExistentFile();
        demonstrateDanglingSymLink();
        demonstrateDirectoryRealPath();
        demonstrateWindowsCaseInsensitivity();
        demonstrateCompareWithToAbsolutePathAndNormalize();
        demonstratePathEqualityAfterRealPath();
        demonstrateAttributesViaRealPath();

        tearDownTestFixtures();

        log.info("═══════════════════════════════════════════════════════════");
        log.info("   Guide complete.");
        log.info("═══════════════════════════════════════════════════════════");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Test Fixture Helpers
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Creates the directory structure, plain files, and symbolic links required
     * by the demonstration methods.
     *
     * <p>Symbolic links on Windows require either:
     * <ul>
     *   <li>the <em>SeCreateSymbolicLinkPrivilege</em> privilege (Administrator), or</li>
     *   <li>Windows Developer Mode enabled (available since Windows 10 v1703).</li>
     * </ul>
     * If the privilege is absent the method logs a warning and skips symlink
     * creation; the affected demonstrations will catch an {@link IOException}
     * and explain the situation gracefully.</p>
     */
    private static void setupTestFixtures() {
        log.info("--- [SETUP] Creating test fixtures ---");
        try {
            Files.createDirectories(SUB_DIR);
            log.info("  Created directory : {}", SUB_DIR);

            if (Files.notExists(REAL_FILE)) {
                Files.writeString(REAL_FILE, "Hello from toRealPath() guide!\n");
                log.info("  Created file      : {}", REAL_FILE);
            }

            createSymLink(SYM_LINK, REAL_FILE, "symlink → realFile");
            createSymLink(CHAINED_LINK, SYM_LINK, "chainedLink → symlink → realFile");

            // Dangling link: create target, create link, then delete target
            if (Files.notExists(DANGLING_TARGET)) {
                Files.writeString(DANGLING_TARGET, "temporary");
            }
            createSymLink(DANGLING_LINK, DANGLING_TARGET, "danglingLink → tempTarget");
            Files.deleteIfExists(DANGLING_TARGET);
            log.info("  Dangling link ready (target deleted): {}", DANGLING_LINK);

        } catch (IOException e) {
            log.error("  [SETUP] Fixture creation failed: {}", e.getMessage());
        }
    }

    /**
     * Creates a symbolic link, logging a warning instead of throwing if the
     * operation is unsupported (e.g. insufficient OS privileges).
     *
     * @param link   path at which the symbolic link should be created
     * @param target path that the symbolic link points to
     * @param label  human-readable label used in log messages
     */
    private static void createSymLink(Path link, Path target, String label) {
        try {
            if (Files.notExists(link, LinkOption.NOFOLLOW_LINKS)) {
                Files.createSymbolicLink(link, target);
                log.info("  Created {}  :  {}", label, link);
            }
        } catch (UnsupportedOperationException | IOException e) {
            log.warn("  [SETUP] Cannot create {} - {}", label, e.getMessage());
        }
    }

    /**
     * Removes every file and symbolic link created by {@link #setupTestFixtures()}.
     *
     * <p>Directories are only removed when empty. The working directory itself
     * ({@link #WORK_DIR}) is intentionally left intact.</p>
     */
    private static void tearDownTestFixtures() {
        log.info("--- [TEARDOWN] Removing test fixtures ---");
        for (Path p : new Path[]{DANGLING_LINK, SYM_LINK, CHAINED_LINK, REAL_FILE}) {
            safeDelete(p);
        }
        safeDelete(SUB_DIR);
    }

    /**
     * Deletes a single path without throwing; logs the outcome instead.
     *
     * @param path the file-system entry to delete
     */
    private static void safeDelete(Path path) {
        try {
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("  Deleted: {}", path);
            }
        } catch (IOException e) {
            log.warn("  Could not delete {}: {}", path, e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Demonstration Methods
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * <b>Edge case 1 - Basic usage: resolve a known, existing file.</b>
     *
     * <p>{@code toRealPath()} on an already-absolute, clean path of an existing
     * file should return the same path, confirming that the entry really exists
     * on the file system and that the JVM's view is consistent with the OS.</p>
     *
     * <p>Key points:</p>
     * <ul>
     *   <li>The method performs a file-system round-trip - unlike pure string
     *       manipulation methods it actually checks the underlying FS.</li>
     *   <li>The returned path is always absolute.</li>
     *   <li>On case-insensitive file systems (NTFS) the casing is normalised to
     *       match what the OS stores, not what was passed in.</li>
     * </ul>
     *
     * <h4>Expected output</h4>
     * <pre>
     * Real path : C:\tests\java\io\realFile.txt
     * Is absolute : true
     * </pre>
     */
    private static void demonstrateBasicRealPath() {
        log.info("");
        log.info("┌─ [1] Basic toRealPath() on a plain existing file ──────────────");
        try {
            Path realPath = REAL_FILE.toRealPath();
            log.info("│  Input path  : {}", REAL_FILE);
            log.info("│  Real path   : {}", realPath);
            log.info("│  Is absolute : {}", realPath.isAbsolute());
        } catch (IOException e) {
            log.error("│  IOException - file does not exist or is inaccessible: {}", e.getMessage());
        }
        log.info("└────────────────────────────────────────────────────────────────");
    }

    /**
     * <b>Edge case 2 - Normalisation of {@code .} and {@code ..} segments.</b>
     *
     * <p>{@code toRealPath()} eliminates redundant path components such as the
     * current-directory token ({@code .}) and the parent-directory token
     * ({@code ..}).  It goes further than {@link Path#normalize()} because it
     * also validates that each intermediate component actually exists.</p>
     *
     * <p>Example input path:</p>
     * <pre>C:\tests\java\io\subdir\..\realFile.txt</pre>
     *
     * <p>After resolution this collapses to:</p>
     * <pre>C:\tests\java\io\realFile.txt</pre>
     *
     * <p>Important distinction from {@code normalize()}: if {@code subdir} did
     * not exist, {@code normalize()} would still succeed (it is purely lexical),
     * but {@code toRealPath()} would throw {@link NoSuchFileException}.</p>
     */
    private static void demonstrateDotDotNormalisation() {
        log.info("");
        log.info("┌─ [2] Normalisation of '..' segments ───────────────────────────");
        log.info("│  Input path   : {}", DOTDOT_PATH);
        try {
            Path real   = DOTDOT_PATH.toRealPath();
            Path norm   = DOTDOT_PATH.normalize();   // purely lexical - for comparison
            log.info("│  toRealPath() : {}", real);
            log.info("│  normalize()  : {}", norm);
            log.info("│  Both equal?  : {}", real.equals(norm.toAbsolutePath()));
        } catch (IOException e) {
            log.error("│  IOException: {}", e.getMessage());
        }
        log.info("└────────────────────────────────────────────────────────────────");
    }

    /**
     * <b>Edge case 3 - Relative path converted to canonical absolute path.</b>
     *
     * <p>When the JVM's current working directory is {@code C:\tests\java\io},
     * the relative path {@code realFile.txt} passed to {@code toRealPath()} is
     * expanded into its fully qualified form.</p>
     *
     * <p>This is the only method in the NIO.2 API that simultaneously:</p>
     * <ol>
     *   <li>Anchors a relative path against the JVM working directory.</li>
     *   <li>Resolves all symbolic-link hops.</li>
     *   <li>Eliminates redundant path elements.</li>
     *   <li>Verifies the file exists - failing with {@link IOException} otherwise.</li>
     * </ol>
     *
     * @implNote The JVM's working directory is typically the directory from
     *           which the JVM process was launched.  Inside an IDE it is often
     *           the project root.
     */
    private static void demonstrateRelativePathResolution() {
        log.info("");
        log.info("┌─ [3] Relative path → canonical absolute path ───────────────────");
        // Build a relative path. If the JVM was launched from WORK_DIR this
        // resolves correctly; otherwise the absolute form is used as a fallback.
        Path relativePath = Path.of("realFile.txt");
        log.info("│  Relative input: {}", relativePath);
        log.info("│  Is absolute?  : {}", relativePath.isAbsolute());
        try {
            Path realPath = relativePath.toRealPath();
            log.info("│  toRealPath()  : {}", realPath);
            log.info("│  Is absolute?  : {}", realPath.isAbsolute());
        } catch (IOException e) {
            // Very likely if the JVM cwd ≠ WORK_DIR; demonstrate the fall-back.
            log.warn("│  Relative resolution failed (cwd ≠ WORK_DIR): {}", e.getMessage());
            log.info("│  Retrying with explicit absolute path ...");
            try {
                Path realPath = REAL_FILE.toRealPath();
                log.info("│  toRealPath()  : {}", realPath);
            } catch (IOException ex) {
                log.error("│  Still failed: {}", ex.getMessage());
            }
        }
        log.info("└─────────────────────────────────────────────────────────────────");
    }

    /**
     * <b>Edge case 4 - Default behaviour: symbolic links ARE followed.</b>
     *
     * <p>Calling {@code toRealPath()} with no arguments (or equivalently with an
     * empty {@link LinkOption} array) instructs the method to follow symbolic
     * links.  The returned path therefore points to the <em>ultimate target</em>
     * of the link chain, not to the link itself.</p>
     *
     * <pre>
     *   SYM_LINK  →  REAL_FILE
     *   Input     :  C:\tests\java\io\linkToRealFile.lnk
     *   Expected  :  C:\tests\java\io\realFile.txt   (the real target)
     * </pre>
     *
     * <p>This is the mode you should use whenever you need the canonical on-disk
     * location of a resource, regardless of how many indirection layers exist
     * between the path you hold and the actual data.</p>
     */
    private static void demonstrateFollowLinksDefault() {
        log.info("");
        log.info("┌─ [4] Default mode - symbolic links ARE followed ───────────────");
        log.info("│  Symlink input : {}", SYM_LINK);
        try {
            // No LinkOption argument → follow links (default)
            Path realPath = SYM_LINK.toRealPath();
            log.info("│  toRealPath()  : {}", realPath);
            log.info("│  Points to actual file, NOT to the link itself.");
            log.info("│  Same as REAL_FILE? : {}", realPath.equals(REAL_FILE.toRealPath()));
        } catch (IOException e) {
            log.warn("│  Symlink unavailable (possibly no OS privilege): {}", e.getMessage());
        }
        log.info("└────────────────────────────────────────────────────────────────");
    }

    /**
     * <b>Edge case 5 - {@link LinkOption#NOFOLLOW_LINKS}: symbolic links are
     * NOT followed.</b>
     *
     * <p>Passing {@code LinkOption.NOFOLLOW_LINKS} makes {@code toRealPath()}
     * return the canonical path of the <em>link entry itself</em>, not of its
     * target.  This is vital in two scenarios:</p>
     * <ol>
     *   <li>You want to inspect, move, or delete the link - not its target.</li>
     *   <li>The link is <em>dangling</em> (target does not exist): with
     *       {@code NOFOLLOW_LINKS} the call succeeds; without it, an
     *       {@link IOException} is thrown because the target is missing.</li>
     * </ol>
     *
     * <pre>
     *   Input         :  C:\tests\java\io\linkToRealFile.lnk
     *   Expected      :  C:\tests\java\io\linkToRealFile.lnk  (link itself)
     *   Default mode  :  C:\tests\java\io\realFile.txt         (target)
     * </pre>
     */
    private static void demonstrateNoFollowLinks() {
        log.info("");
        log.info("┌─ [5] NOFOLLOW_LINKS - return the link's own real path ─────────");
        log.info("│  Symlink input  : {}", SYM_LINK);
        try {
            Path noFollow  = SYM_LINK.toRealPath(LinkOption.NOFOLLOW_LINKS);
            Path withFollow = SYM_LINK.toRealPath(); // for contrast
            log.info("│  NOFOLLOW_LINKS : {}", noFollow);
            log.info("│  Follow (default): {}", withFollow);
            log.info("│  Different paths? : {}", !noFollow.equals(withFollow));
        } catch (IOException e) {
            log.warn("│  Symlink unavailable: {}", e.getMessage());
        }
        log.info("└────────────────────────────────────────────────────────────────");
    }

    /**
     * <b>Edge case 6 - Chained (multi-hop) symbolic links.</b>
     *
     * <p>A symbolic link may itself point to another symbolic link.
     * {@code toRealPath()} (without {@code NOFOLLOW_LINKS}) resolves the entire
     * chain recursively until it reaches a real file-system entry.</p>
     *
     * <pre>
     *   chainedLink.lnk  →  linkToRealFile.lnk  →  realFile.txt
     * </pre>
     *
     * <p>The returned path skips all intermediate links and gives back the
     * canonical path of the final target ({@code realFile.txt}).</p>
     *
     * <p>On POSIX systems the kernel imposes a default maximum of 40 hops
     * ({@code MAXSYMLINKS}).  Exceeding this limit causes an {@link IOException}
     * with a "Too many levels of symbolic links" message.  Windows does not
     * impose a documented limit but typically resolves up to 63 reparse
     * points.</p>
     */
    private static void demonstrateChainedSymLinks() {
        log.info("");
        log.info("┌─ [6] Chained symlinks resolved recursively ─────────────────────");
        log.info("│  Chain : chainedLink → symlink → realFile");
        log.info("│  Input : {}", CHAINED_LINK);
        try {
            Path realPath = CHAINED_LINK.toRealPath();
            log.info("│  Resolved to : {}", realPath);
            log.info("│  Is the actual file? : {}",
                    realPath.equals(REAL_FILE.toRealPath()));
        } catch (IOException e) {
            log.warn("│  Chained link unavailable: {}", e.getMessage());
        }
        log.info("└─────────────────────────────────────────────────────────────────");
    }

    /**
     * <b>Edge case 7 - {@link NoSuchFileException} for non-existent paths.</b>
     *
     * <p>{@code toRealPath()} is one of the few NIO.2 {@code Path} methods that
     * <em>requires</em> the file to exist.  If it does not, the method throws a
     * {@link NoSuchFileException} (a subtype of {@link IOException}).  This
     * contrasts sharply with {@code toAbsolutePath()} and {@code normalize()},
     * which are purely lexical and succeed whether or not the path exists.</p>
     *
     * <p>Always wrap calls to {@code toRealPath()} in a try-catch block when the
     * existence of the file is not guaranteed.</p>
     *
     * <h4>Recommended pattern</h4>
     * <pre>{@code
     * try {
     *     Path real = suspectPath.toRealPath();
     *     // proceed with real
     * } catch (NoSuchFileException e) {
     *     log.warn("Path does not exist: {}", e.getFile());
     * } catch (IOException e) {
     *     log.error("I/O error resolving path: {}", e.getMessage());
     * }
     * }</pre>
     */
    private static void demonstrateNonExistentFile() {
        log.info("");
        log.info("┌─ [7] NoSuchFileException - file does not exist ─────────────────");
        log.info("│  Input path : {}", GHOST_FILE);
        log.info("│  Exists?    : {}", Files.exists(GHOST_FILE));
        try {
            Path realPath = GHOST_FILE.toRealPath();
            // This line is never reached:
            log.info("│  Real path  : {}", realPath);
        } catch (NoSuchFileException e) {
            log.warn("│  ✓ Caught NoSuchFileException - missing file: {}", e.getFile());
        } catch (IOException e) {
            log.error("│  Unexpected IOException: {}", e.getMessage());
        }
        log.info("└─────────────────────────────────────────────────────────────────");
    }

    /**
     * <b>Edge case 8 - Dangling symbolic link behaviour.</b>
     *
     * <p>A <em>dangling</em> link is one whose target has been deleted or moved.
     * The behaviour of {@code toRealPath()} depends on whether link-following
     * is enabled:</p>
     *
     * <ul>
     *   <li><b>Default (follow links)</b>: The method attempts to resolve the
     *       target, which no longer exists, so it throws {@link NoSuchFileException}
     *       pointing at the missing target.</li>
     *   <li><b>{@code NOFOLLOW_LINKS}</b>: The method resolves the link entry
     *       itself, which <em>does</em> exist, so it succeeds and returns the
     *       canonical path of the link file.</li>
     * </ul>
     *
     * <p>This asymmetry is extremely useful for detecting and cleaning up
     * dangling links in maintenance utilities.</p>
     */
    private static void demonstrateDanglingSymLink() {
        log.info("");
        log.info("┌─ [8] Dangling symlink - target has been deleted ────────────────");
        log.info("│  Dangling link : {}", DANGLING_LINK);
        log.info("│  Target exists?: {}", Files.exists(DANGLING_TARGET));

        // Attempt 1: default (follow links) - expected to throw
        log.info("│");
        log.info("│  [8a] Follow links (default) ...");
        try {
            Path realPath = DANGLING_LINK.toRealPath();
            log.info("│       Resolved to : {}", realPath);  // should not reach here
        } catch (NoSuchFileException e) {
            log.warn("│       ✓ NoSuchFileException - missing target: {}", e.getFile());
        } catch (IOException e) {
            log.warn("│       IOException: {}", e.getMessage());
        }

        // Attempt 2: NOFOLLOW_LINKS - expected to succeed
        log.info("│  [8b] NOFOLLOW_LINKS ...");
        try {
            Path realPath = DANGLING_LINK.toRealPath(LinkOption.NOFOLLOW_LINKS);
            log.info("│       ✓ Resolved link itself (not target): {}", realPath);
        } catch (IOException e) {
            log.warn("│       IOException: {}", e.getMessage());
        }
        log.info("└─────────────────────────────────────────────────────────────────");
    }

    /**
     * <b>Edge case 9 - Resolving a directory path (not just files).</b>
     *
     * <p>{@code toRealPath()} works identically for directories.  The method
     * does not distinguish between file and directory entries; it simply
     * validates that the path exists and returns its canonical form.</p>
     *
     * <p>Practical use cases:</p>
     * <ul>
     *   <li>Canonicalising a base directory before building child paths.</li>
     *   <li>Verifying that a configuration-supplied directory is reachable.</li>
     *   <li>Normalising plugin scan paths in framework bootstrapping code.</li>
     * </ul>
     */
    private static void demonstrateDirectoryRealPath() {
        log.info("");
        log.info("┌─ [9] Resolving a directory path ────────────────────────────────");
        log.info("│  Input directory : {}", SUB_DIR);
        try {
            Path realDir = SUB_DIR.toRealPath();
            log.info("│  Real path       : {}", realDir);
            log.info("│  Is directory?   : {}", Files.isDirectory(realDir));
        } catch (IOException e) {
            log.error("│  IOException: {}", e.getMessage());
        }
        log.info("└─────────────────────────────────────────────────────────────────");
    }

    /**
     * <b>Edge case 10 - Case-insensitive file systems (Windows / NTFS).</b>
     *
     * <p>On NTFS, file names are case-insensitive but case-preserving.  If the
     * file was created as {@code realFile.txt} but you pass
     * {@code REALFILE.TXT} or {@code realfile.txt} to {@code toRealPath()}, the
     * method normalises the casing to match the on-disk name.</p>
     *
     * <p>This is particularly important when:</p>
     * <ul>
     *   <li>Building cross-platform tools that must behave consistently on
     *       Windows and Linux.</li>
     *   <li>Comparing path objects: two {@code Path} objects with different
     *       casings are <em>not</em> equal, but after passing them both through
     *       {@code toRealPath()} they become equal (on NTFS).</li>
     * </ul>
     *
     * <p><b>Warning:</b> Do <em>not</em> rely on this normalisation when
     * writing cross-platform code.  On Linux (ext4, case-sensitive) a
     * differently-cased path to a real file throws {@link NoSuchFileException}.</p>
     */
    private static void demonstrateWindowsCaseInsensitivity() {
        log.info("");
        log.info("┌─ [10] Windows NTFS case-insensitivity normalisation ────────────");

        // All three represent the same file on NTFS:
        Path lowerCase = WORK_DIR.resolve("realfile.txt");
        Path upperCase = WORK_DIR.resolve("REALFILE.TXT");
        Path mixedCase = WORK_DIR.resolve("ReAlFiLe.TxT");

        log.info("│  lowerCase : {}", lowerCase);
        log.info("│  upperCase : {}", upperCase);
        log.info("│  mixedCase : {}", mixedCase);

        for (Path variant : new Path[]{lowerCase, upperCase, mixedCase}) {
            try {
                Path real = variant.toRealPath();
                log.info("│  → toRealPath({}) = {}", variant.getFileName(), real);
            } catch (IOException e) {
                log.warn("│  → toRealPath({}) threw {}: {}",
                        variant.getFileName(), e.getClass().getSimpleName(), e.getMessage());
            }
        }
        log.info("└─────────────────────────────────────────────────────────────────");
    }

    /**
     * <b>Edge case 11 - Comparing {@code toRealPath()} vs
     * {@code toAbsolutePath()} vs {@code normalize()}.</b>
     *
     * <p>These three methods are often confused.  This method shows their
     * differences side by side using the same dirty input path:</p>
     *
     * <pre>
     *   C:\tests\java\io\subdir\..\.\realFile.txt
     * </pre>
     *
     * <table border="1" cellpadding="6">
     *   <thead>
     *     <tr><th>Method</th><th>Result</th><th>Checks FS?</th></tr>
     *   </thead>
     *   <tbody>
     *     <tr>
     *       <td>{@code normalize()}</td>
     *       <td>{@code C:\tests\java\io\realFile.txt}</td>
     *       <td>No - lexical only</td>
     *     </tr>
     *     <tr>
     *       <td>{@code toAbsolutePath()}</td>
     *       <td>{@code C:\tests\java\io\subdir\..\.\realFile.txt}</td>
     *       <td>No - anchors CWD but keeps dirty segments</td>
     *     </tr>
     *     <tr>
     *       <td>{@code toRealPath()}</td>
     *       <td>{@code C:\tests\java\io\realFile.txt}</td>
     *       <td>Yes - validates existence + resolves links</td>
     *     </tr>
     *   </tbody>
     * </table>
     */
    private static void demonstrateCompareWithToAbsolutePathAndNormalize() {
        log.info("");
        log.info("┌─ [11] toRealPath() vs toAbsolutePath() vs normalize() ──────────");

        // Dirty path: mix of .., ., redundant separators
        Path dirty = WORK_DIR.resolve("subdir\\..\\.\\realFile.txt");
        log.info("│  Input (dirty) : {}", dirty);
        log.info("│");

        log.info("│  normalize()       : {}", dirty.normalize());
        log.info("│  toAbsolutePath()  : {}", dirty.toAbsolutePath());
        try {
            log.info("│  toRealPath()      : {}", dirty.toRealPath());
        } catch (IOException e) {
            log.error("│  toRealPath() threw: {}", e.getMessage());
        }
        log.info("│");
        log.info("│  Key insight: only toRealPath() guarantees the file exists AND");
        log.info("│  follows symlinks. The others are purely lexical transforms.");
        log.info("└─────────────────────────────────────────────────────────────────");
    }

    /**
     * <b>Edge case 12 - Path equality and {@code isSameFile()} after
     * {@code toRealPath()}.</b>
     *
     * <p>Two {@link Path} objects referring to the same file through different
     * representations (one via a symlink, one via the real path) are
     * <em>not</em> {@code equals()} to each other.  After applying
     * {@code toRealPath()} to both, they become {@code equals()}.
     * Alternatively, {@link Files#isSameFile(Path, Path)} can be used - it
     * performs an OS-level inode / file-ID comparison and correctly identifies
     * the same file even without calling {@code toRealPath()} first.</p>
     *
     * <pre>
     *   SYM_LINK.equals(REAL_FILE)               → false  (different path strings)
     *   SYM_LINK.toRealPath().equals(REAL_FILE.toRealPath())  → true
     *   Files.isSameFile(SYM_LINK, REAL_FILE)    → true
     * </pre>
     */
    private static void demonstratePathEqualityAfterRealPath() {
        log.info("");
        log.info("┌─ [12] Path equality: symlink vs real file ──────────────────────");
        log.info("│  SYM_LINK  : {}", SYM_LINK);
        log.info("│  REAL_FILE : {}", REAL_FILE);
        log.info("│");
        log.info("│  SYM_LINK.equals(REAL_FILE)                     : {}",
                SYM_LINK.equals(REAL_FILE));
        try {
            boolean equalAfterReal = SYM_LINK.toRealPath()
                    .equals(REAL_FILE.toRealPath());
            log.info("│  toRealPath(sym).equals(toRealPath(real))        : {}", equalAfterReal);
            log.info("│  Files.isSameFile(SYM_LINK, REAL_FILE)           : {}",
                    Files.isSameFile(SYM_LINK, REAL_FILE));
        } catch (IOException e) {
            log.warn("│  Symlink comparison unavailable: {}", e.getMessage());
            try {
                log.info("│  Files.isSameFile falls back gracefully: {}",
                        Files.isSameFile(REAL_FILE, REAL_FILE));
            } catch (IOException ex) {
                log.error("│  isSameFile also failed: {}", ex.getMessage());
            }
        }
        log.info("└─────────────────────────────────────────────────────────────────");
    }

    /**
     * <b>Edge case 13 - Reading file attributes via a real path.</b>
     *
     * <p>Combining {@code toRealPath()} with {@link Files#readAttributes(Path,
     * Class, LinkOption...)} is the safest pattern for reading metadata of an
     * existing file because:</p>
     * <ol>
     *   <li>You get the attributes of the <em>actual data</em>, not of the
     *       link entry (a symlink would report its own tiny size, not the
     *       target's size).</li>
     *   <li>The call throws {@link NoSuchFileException} early - before you
     *       build any business logic on a stale path.</li>
     * </ol>
     *
     * <p>This is the recommended practice in production back-end services that
     * process user-uploaded files, scan deployment artefacts, or audit log
     * directories.</p>
     */
    private static void demonstrateAttributesViaRealPath() {
        log.info("");
        log.info("┌─ [13] Reading file attributes through toRealPath() ────────────");
        log.info("│  Input : {}", REAL_FILE);
        try {
            Path real = REAL_FILE.toRealPath();
            BasicFileAttributes attrs =
                    Files.readAttributes(real, BasicFileAttributes.class);

            log.info("│  Real path        : {}", real);
            log.info("│  Size             : {} bytes", attrs.size());
            log.info("│  Is regular file? : {}", attrs.isRegularFile());
            log.info("│  Is directory?    : {}", attrs.isDirectory());
            log.info("│  Is symlink?      : {}", attrs.isSymbolicLink());
            log.info("│  Created          : {}", attrs.creationTime());
            log.info("│  Last modified    : {}", attrs.lastModifiedTime());
        } catch (IOException e) {
            log.error("│  IOException: {}", e.getMessage());
        }
        log.info("└─────────────────────────────────────────────────────────────────");
    }
}