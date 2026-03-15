package com.desabisc.guide.java.io.gnormalize;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <h2>PathNormalizeGuide - Comprehensive Guide to {@link Path#normalize()}</h2>
 *
 * <p>This class is a self-contained, runnable guide that demonstrates every significant
 * edge case of the {@link Path#normalize()} method from the Java NIO.2 file API
 * ({@code java.nio.file}, introduced in Java 7).</p>
 *
 * <h3>What does {@code normalize()} do?</h3>
 * <p>{@code normalize()} returns a path that is equivalent to the original but with
 * <em>redundant name elements eliminated</em>:</p>
 * <ul>
 *   <li>Single-dot ({@code .}) components are removed - they mean "current directory"
 *       and add no positional information.</li>
 *   <li>Double-dot ({@code ..}) components are resolved against their preceding element
 *       - they mean "go up one level" and cancel the preceding name element.</li>
 *   <li>The root component (e.g., {@code C:\} or {@code /}) is <strong>never</strong>
 *       removed, even if leading {@code ..} would logically climb above it.</li>
 * </ul>
 *
 * <h3>What does {@code normalize()} NOT do?</h3>
 * <ul>
 *   <li>It does <strong>not</strong> access the file system.</li>
 *   <li>It does <strong>not</strong> resolve symbolic links (use {@link Files#toRealPath}
 *       for that).</li>
 *   <li>It does <strong>not</strong> make a relative path absolute (use
 *       {@link Path#toAbsolutePath()} first if needed).</li>
 * </ul>
 *
 * <h3>Working Directory Convention</h3>
 * <p>All examples use {@code C:\tests\java\io} as the logical working/base directory,
 * consistent with a Windows NIO.2 development environment.</p>
 *
 * <p><strong>Note on cross-platform execution:</strong> Windows-style absolute paths
 * ({@code C:\...}) are constructed via {@link Paths#get(String, String...)} and
 * wrapped safely so that the class can also compile and run on Linux/macOS - in those
 * environments the Windows-absolute-path examples are demonstrated as
 * {@link InvalidPathException}-safe strings shown in the log output only.</p>
 *
 * <h3>Dependencies</h3>
 * <ul>
 *   <li>Java 11+ (NIO.2 has been stable since Java 7; {@code var} patterns used
 *       internally require Java 11).</li>
 *   <li>Project Lombok ({@code lombok.extern.slf4j.Slf4j}) for structured logging.</li>
 * </ul>
 *
 * @author  Abi
 * @version 1.0
 * @since   2026-03-15
 * @see     java.nio.file.Path
 * @see     java.nio.file.Paths
 * @see     java.nio.file.Files
 */
@Slf4j
public class PathNormalizeGuide {

    // -----------------------------------------------------------------------
    //  Constants - base working directory on Windows
    // -----------------------------------------------------------------------

    /** Root working directory used as the anchor for all examples. */
    private static final String BASE_DIR = "C:\\tests\\java\\io";

    /** Separator line for log readability. */
    private static final String SEPARATOR =
            "=".repeat(70);

    // -----------------------------------------------------------------------
    //  Entry Point
    // -----------------------------------------------------------------------

    /**
     * Application entry point. Runs every edge-case demonstration in sequence.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        PathNormalizeGuide guide = new PathNormalizeGuide();

        log.info(SEPARATOR);
        log.info("  Path.normalize() - Complete Edge-Case Guide");
        log.info("  Working directory : {}", BASE_DIR);
        log.info(SEPARATOR);

        guide.demonstrateAlreadyNormalized();
        guide.demonstrateSingleDotRemoval();
        guide.demonstrateDoubleDotResolution();
        guide.demonstrateMultipleConsecutiveDoubleDots();
        guide.demonstrateDoubleDotAtRoot();
        guide.demonstrateRelativePathNormalization();
        guide.demonstrateMixedDotCombinations();
        guide.demonstrateTrailingSeparator();
        guide.demonstrateEmptyPath();
        guide.demonstratePathWithSpaces();
        guide.demonstrateResolveAndNormalize();
        guide.demonstrateNormalizeVsToRealPath();
        guide.demonstrateUncPath();
        guide.demonstrateDeepNesting();
        guide.demonstrateNormalizationPreservesRoot();
        guide.demonstratePathEqualityAfterNormalize();
        guide.demonstrateFileSystemOperationWithNormalization();

        log.info(SEPARATOR);
        log.info("  Guide complete.");
        log.info(SEPARATOR);
    }

    // -----------------------------------------------------------------------
    //  Edge Case 1 - Already-normalized path
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that calling {@code normalize()} on a path that contains no
     * redundant elements returns a path equal to (and typically the same object as)
     * the original.
     *
     * <pre>
     * Input  : C:\tests\java\io\data\file.txt
     * Output : C:\tests\java\io\data\file.txt   (unchanged)
     * </pre>
     *
     * <p><strong>Key insight:</strong> {@code normalize()} is idempotent - calling it
     * repeatedly on an already-clean path is safe and produces the same result.</p>
     */
    private void demonstrateAlreadyNormalized() {
        printSection("EDGE CASE 1 - Already-normalized path");

        Path original = buildWindowsPath(BASE_DIR, "data", "file.txt");
        Path normalized = original.normalize();

        log.info("  Original   : {}", original);
        log.info("  Normalized : {}", normalized);
        log.info("  Equal?     : {}", original.equals(normalized));
        log.info("  Idempotent check (normalize twice) : {}",
                normalized.normalize().equals(normalized));
    }

    // -----------------------------------------------------------------------
    //  Edge Case 2 - Single-dot (.) removal
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that single-dot ({@code .}) components - which mean
     * <em>current directory</em> - are completely removed by {@code normalize()}.
     *
     * <pre>
     * Input  : C:\tests\java\io\.\data\.\file.txt
     * Output : C:\tests\java\io\data\file.txt
     * </pre>
     *
     * <p>A single dot in a path is syntactically valid but semantically a no-op.
     * {@code normalize()} strips every occurrence regardless of position.</p>
     */
    private void demonstrateSingleDotRemoval() {
        printSection("EDGE CASE 2 - Single-dot (.) removal");

        // Build path with embedded '.' components
        Path withDots = Paths.get(BASE_DIR, ".", "data", ".", "file.txt");
        Path normalized = withDots.normalize();

        log.info("  Input  : {}", withDots);
        log.info("  Output : {}", normalized);
        log.info("  Name count before : {}", withDots.getNameCount());
        log.info("  Name count after  : {}", normalized.getNameCount());
    }

    // -----------------------------------------------------------------------
    //  Edge Case 3 - Double-dot (..) resolution
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that double-dot ({@code ..}) components - which mean
     * <em>go up one directory level</em> - are resolved by removing both the
     * {@code ..} component and the immediately preceding name element.
     *
     * <pre>
     * Input  : C:\tests\java\io\data\..\config\app.properties
     * Output : C:\tests\java\io\config\app.properties
     * </pre>
     *
     * <p>This is the most common use-case: cleaning up a path constructed
     * programmatically that may contain "up-one-level" navigation jumps.</p>
     */
    private void demonstrateDoubleDotResolution() {
        printSection("EDGE CASE 3 - Double-dot (..) resolution");

        Path raw = Paths.get(BASE_DIR, "data", "..", "config", "app.properties");
        Path normalized = raw.normalize();

        log.info("  Input  : {}", raw);
        log.info("  Output : {}", normalized);

        // Show what each name element is before normalization
        log.info("  Name elements before normalization:");
        for (int i = 0; i < raw.getNameCount(); i++) {
            log.info("    [{}] -> {}", i, raw.getName(i));
        }

        log.info("  Name elements after normalization:");
        for (int i = 0; i < normalized.getNameCount(); i++) {
            log.info("    [{}] -> {}", i, normalized.getName(i));
        }
    }

    // -----------------------------------------------------------------------
    //  Edge Case 4 - Multiple consecutive double-dots
    // -----------------------------------------------------------------------

    /**
     * Demonstrates resolution of multiple consecutive {@code ..} components,
     * each climbing one level higher than the last.
     *
     * <pre>
     * Input  : C:\tests\java\io\a\b\c\..\..\..
     * Output : C:\tests\java\io
     * </pre>
     *
     * <p>Three consecutive {@code ..} cancel out three name elements ({@code c},
     * {@code b}, {@code a}), leaving just the base directory.</p>
     */
    private void demonstrateMultipleConsecutiveDoubleDots() {
        printSection("EDGE CASE 4 - Multiple consecutive (..) elements");

        Path raw = Paths.get(BASE_DIR, "a", "b", "c", "..", "..", "..");
        Path normalized = raw.normalize();

        log.info("  Input  : {}", raw);
        log.info("  Output : {}", normalized);
        log.info("  Matches BASE_DIR? : {}", normalized.equals(Paths.get(BASE_DIR)));

        // One extra '..' than name elements climbs to parent of BASE_DIR
        Path tooMany = Paths.get(BASE_DIR, "a", "..", "..");
        Path normalizedTooMany = tooMany.normalize();
        log.info("  Extra '..' Input  : {}", tooMany);
        log.info("  Extra '..' Output : {}", normalizedTooMany);  // C:\tests\java
    }

    // -----------------------------------------------------------------------
    //  Edge Case 5 - Double-dot at root (cannot climb above root)
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that {@code ..} components cannot navigate above the root of an
     * absolute path. When a {@code ..} would logically go above the root, it is
     * simply discarded - the root is preserved.
     *
     * <pre>
     * Input  : C:\..\..\file.txt
     * Output : C:\file.txt
     * </pre>
     *
     * <p><strong>Important:</strong> This is purely lexicographic resolution.
     * {@code normalize()} makes no file-system access and therefore cannot detect
     * that the resulting path does not actually exist.</p>
     */
    private void demonstrateDoubleDotAtRoot() {
        printSection("EDGE CASE 5 - Double-dot (..) at/above root (clamped)");

        // On Windows the root is "C:\". The '..' cannot escape it.
        Path climbingAboveRoot = buildWindowsPath("C:\\", "..", "..", "file.txt");
        Path normalized = climbingAboveRoot.normalize();

        log.info("  Input  : {}", climbingAboveRoot);
        log.info("  Output : {}", normalized);
        log.info("  Root preserved? : {}", normalized.getRoot() != null);
        log.info("  Root value      : {}", normalized.getRoot());
    }

    // -----------------------------------------------------------------------
    //  Edge Case 6 - Relative path normalization
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that {@code normalize()} works on <em>relative</em> paths too,
     * not just absolute ones. When there are more leading {@code ..} components than
     * available name elements, the surplus {@code ..} elements are retained at the
     * front of the result - there is no root to clamp against.
     *
     * <pre>
     * Input  : data\..\config\..\..\shared\\util.jar
     * Output : ..\shared\\util.jar
     * </pre>
     *
     * <p>Unlike absolute paths, a relative path <em>can</em> start with {@code ..}
     * in the normalized form - it is valid and indicates "look in an ancestor of
     * the current working directory".</p>
     */
    private void demonstrateRelativePathNormalization() {
        printSection("EDGE CASE 6 - Relative path normalization");

        // Relative - no root
        Path relative = Paths.get("data", "..", "config", "..", "..", "shared", "util.jar");
        Path normalized = relative.normalize();

        log.info("  Input    : {}", relative);
        log.info("  Output   : {}", normalized);
        log.info("  Absolute?: {}", relative.isAbsolute());
        log.info("  Starts with '..'? : {}",
                normalized.getNameCount() > 0 &&
                        normalized.getName(0).toString().equals(".."));

        // Purely upward-only relative path
        Path purelyUp = Paths.get("..", "..", "..");
        Path normalizedUp = purelyUp.normalize();
        log.info("  Pure '..' Input  : {}", purelyUp);
        log.info("  Pure '..' Output : {}", normalizedUp);   // remains ..\..\..
    }

    // -----------------------------------------------------------------------
    //  Edge Case 7 - Mixed single-dot and double-dot combinations
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that {@code normalize()} handles arbitrary mixes of {@code .}
     * and {@code ..} components in a single path, resolving them all in one pass
     * from left to right.
     *
     * <pre>
     * Input  : C:\tests\java\io\.\a\..\.\b\..\c\.\file.txt
     * Output : C:\tests\java\io\c\file.txt
     * </pre>
     *
     * <p>Resolution order matters: {@code .} is removed first (conceptually), and
     * each {@code ..} eliminates the immediately preceding resolved name element.</p>
     */
    private void demonstrateMixedDotCombinations() {
        printSection("EDGE CASE 7 - Mixed (.) and (..) combinations");

        Path messy = Paths.get(BASE_DIR, ".", "a", "..", ".", "b", "..", "c", ".", "file.txt");
        Path normalized = messy.normalize();

        log.info("  Input  : {}", messy);
        log.info("  Output : {}", normalized);
    }

    // -----------------------------------------------------------------------
    //  Edge Case 8 - Trailing separator / trailing dot
    // -----------------------------------------------------------------------

    /**
     * Demonstrates the behaviour of {@code normalize()} when a path ends with a
     * directory separator or with a trailing dot.
     *
     * <p>Java's {@link Paths#get(String)} normalizes trailing separators at parse
     * time on most file-system implementations (the trailing separator is dropped
     * before {@code normalize()} is even called). A trailing {@code .} is treated as
     * a name element and is removed by {@code normalize()}.</p>
     *
     * <pre>
     * Input  : C:\tests\java\io\data\.
     * Output : C:\tests\java\io\data
     * </pre>
     */
    private void demonstrateTrailingSeparator() {
        printSection("EDGE CASE 8 - Trailing separator / trailing dot");

        // Trailing dot acts as a '.' name element
        Path withTrailingDot = Paths.get(BASE_DIR, "data", ".");
        Path normalized = withTrailingDot.normalize();

        log.info("  Input (trailing dot)  : {}", withTrailingDot);
        log.info("  Output                : {}", normalized);

        // String with trailing backslash - parsed cleanly by Paths.get
        Path withTrailingSep = Paths.get(BASE_DIR + "\\data\\");
        Path normalizedSep = withTrailingSep.normalize();
        log.info("  Input (trailing '\\') : {}", withTrailingSep);
        log.info("  Output                : {}", normalizedSep);
    }

    // -----------------------------------------------------------------------
    //  Edge Case 9 - Empty path
    // -----------------------------------------------------------------------

    /**
     * Demonstrates the behaviour of {@code normalize()} on an <em>empty path</em>
     * ({@code ""} or {@code "."}). An empty path represents the current directory
     * and is treated by {@code Paths.get("")} as a single-element path whose name
     * is the empty string.
     *
     * <pre>
     * Paths.get("")   ->  ""   (empty relative path)
     * Paths.get(".") .normalize() -> ""
     * </pre>
     *
     * <p>After normalizing a single {@code "."} the implementation typically returns
     * an empty path on Windows ({@code ""}) rather than {@code "."}. Code that
     * constructs paths with {@code "."} should call {@code normalize()} before
     * using the result in comparisons.</p>
     */
    private void demonstrateEmptyPath() {
        printSection("EDGE CASE 9 - Empty path and single '.'");

        Path emptyPath = Paths.get("");
        Path normalizedEmpty = emptyPath.normalize();
        log.info("  Paths.get(\"\")       : '{}'", emptyPath);
        log.info("  After normalize()   : '{}'", normalizedEmpty);
        log.info("  Name count          : {}", normalizedEmpty.getNameCount());

        Path singleDot = Paths.get(".");
        Path normalizedDot = singleDot.normalize();
        log.info("  Paths.get(\".\")      : '{}'", singleDot);
        log.info("  After normalize()   : '{}'", normalizedDot);
        log.info("  Empty path equals? : {}", normalizedDot.equals(emptyPath));
    }

    // -----------------------------------------------------------------------
    //  Edge Case 10 - Paths containing spaces
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that spaces and other special characters in directory/file names
     * are treated as ordinary characters by {@code normalize()} - they have no
     * special meaning and are never stripped or encoded.
     *
     * <pre>
     * Input  : C:\tests\java\io\my data\..\my config\app file.properties
     * Output : C:\tests\java\io\my config\app file.properties
     * </pre>
     */
    private void demonstratePathWithSpaces() {
        printSection("EDGE CASE 10 - Paths with spaces in names");

        Path withSpaces = Paths.get(BASE_DIR, "my data", "..", "my config", "app file.properties");
        Path normalized = withSpaces.normalize();

        log.info("  Input  : {}", withSpaces);
        log.info("  Output : {}", normalized);
        log.info("  File name: {}", normalized.getFileName());
    }

    // -----------------------------------------------------------------------
    //  Edge Case 11 - resolve() + normalize() (the canonical pattern)
    // -----------------------------------------------------------------------

    /**
     * Demonstrates the canonical NIO.2 idiom of chaining {@link Path#resolve(String)}
     * with {@code normalize()} to safely construct a child path from a potentially
     * untrusted or relative string.
     *
     * <pre>
     * base.resolve("../secret/passwords.txt").normalize()
     *   ->  C:\tests\java\secret\passwords.txt
     * </pre>
     *
     * <p><strong>Security note (path-traversal):</strong> If the base directory is a
     * trusted root and the resolved+normalized path no longer starts with that root,
     * it means a {@code ..} attack has escaped the sandbox. Always verify that the
     * normalized path {@linkplain Path#startsWith(Path) starts with} the trusted
     * base after normalization.</p>
     *
     * @see Path#resolve(String)
     * @see Path#startsWith(Path)
     */
    private void demonstrateResolveAndNormalize() {
        printSection("EDGE CASE 11 - resolve() + normalize() (canonical pattern)");

        Path base = Paths.get(BASE_DIR);

        // Legitimate relative input
        String userInput = "reports\\2026\\q1.csv";
        Path safe = base.resolve(userInput).normalize();
        log.info("  [Safe]   resolve('{}') -> {}", userInput, safe);
        log.info("  [Safe]   Within base? : {}", safe.startsWith(base));

        // Malicious traversal input
        String maliciousInput = "..\\..\\secret\\passwords.txt";
        Path suspicious = base.resolve(maliciousInput).normalize();
        log.info("  [Attack] resolve('{}') -> {}", maliciousInput, suspicious);
        log.info("  [Attack] Within base? : {}", suspicious.startsWith(base));

        if (!suspicious.startsWith(base)) {
            log.warn("  PATH TRAVERSAL DETECTED - rejecting '{}'", maliciousInput);
        }
    }

    // -----------------------------------------------------------------------
    //  Edge Case 12 - normalize() vs toRealPath()
    // -----------------------------------------------------------------------

    /**
     * Highlights the critical difference between {@code normalize()} and
     * {@link Files#toRealPath(java.nio.file.LinkOption...)}:
     *
     * <ul>
     *   <li>{@code normalize()} - <em>lexicographic</em> only; no file-system access;
     *       works on non-existent paths; does <strong>not</strong> resolve symlinks.</li>
     *   <li>{@code toRealPath()} - accesses the file system; requires the path to
     *       actually exist; resolves all symlinks; also normalizes implicitly.</li>
     * </ul>
     *
     * <pre>
     * // Assume: C:\tests\java\io\link -> C:\tests\java\io\real
     * Paths.get("C:\tests\java\io\link\..\data").normalize()
     *   ->  C:\tests\java\io\data          (symlink NOT followed)
     * Files.toRealPath(Paths.get("C:\tests\java\io\link\..\data"))
     *   ->  C:\tests\java\io\data          (symlink followed AND filesystem verified)
     * </pre>
     */
    private void demonstrateNormalizeVsToRealPath() {
        printSection("EDGE CASE 12 - normalize() vs toRealPath()");

        Path hypotheticalSymlink = Paths.get(BASE_DIR, "link", "..", "data", "file.txt");
        Path normalized = hypotheticalSymlink.normalize();

        log.info("  normalize() result (no FS access) : {}", normalized);
        log.info("  normalize() is purely lexicographic - symlinks are NOT followed.");

        // toRealPath() requires the path to exist - use it safely
        try {
            Path real = normalized.toRealPath();
            log.info("  toRealPath() result               : {}", real);
        } catch (IOException e) {
            log.info("  toRealPath() threw IOException (expected if path doesn't exist): {}",
                    e.getMessage());
        }

        log.info("  Rule: normalize() for construction/cleaning; " +
                "toRealPath() for filesystem truth.");
    }

    // -----------------------------------------------------------------------
    //  Edge Case 13 - UNC (Universal Naming Convention) paths on Windows
    // -----------------------------------------------------------------------

    /**
     * Demonstrates {@code normalize()} behaviour on Windows UNC paths
     * ({@code \\server\share\...}). The UNC root ({@code \\server\share}) is treated
     * as the root component and can never be navigated above with {@code ..}.
     *
     * <pre>
     * Input  : \\myserver\share\data\..\config\app.yml
     * Output : \\myserver\share\config\app.yml
     * </pre>
     *
     * <p>Note: {@code Paths.get("\\\\server\\share")} requires escaped backslashes
     * in Java string literals.</p>
     */
    private void demonstrateUncPath() {
        printSection("EDGE CASE 13 - UNC path normalization (Windows)");

        Path unc = buildWindowsPath("\\\\myserver\\share", "data", "..", "config", "app.yml");
        Path normalized = unc.normalize();

        log.info("  Input  : {}", unc);
        log.info("  Output : {}", normalized);
        log.info("  Root   : {}", normalized.getRoot());
    }

    // -----------------------------------------------------------------------
    //  Edge Case 14 - Very deep nesting
    // -----------------------------------------------------------------------

    /**
     * Demonstrates that {@code normalize()} can handle arbitrarily deep nesting -
     * paths with many levels of directories interleaved with {@code ..} components
     * are fully resolved in a single call with no stack-overflow risk.
     *
     * <pre>
     * Input  : C:\tests\java\io\a\b\c\d\e\f\g\..\..\..\..\..
     * Output : C:\tests\java\io\a\b
     * </pre>
     */
    private void demonstrateDeepNesting() {
        printSection("EDGE CASE 14 - Deeply nested path with many (..) elements");

        Path deep = Paths.get(BASE_DIR, "a", "b", "c", "d", "e", "f", "g",
                "..", "..", "..", "..", "..");
        Path normalized = deep.normalize();

        log.info("  Input  : {}", deep);
        log.info("  Output : {}", normalized);
        log.info("  Depth before: {}", deep.getNameCount());
        log.info("  Depth after : {}", normalized.getNameCount());
    }

    // -----------------------------------------------------------------------
    //  Edge Case 15 - Normalization preserves the root on absolute paths
    // -----------------------------------------------------------------------

    /**
     * Confirms that for any absolute path, {@code normalize()} <strong>always</strong>
     * preserves the root component. Even if all name elements are cancelled out by
     * {@code ..} components, the root remains.
     *
     * <pre>
     * Input  : C:\a\..\b\..\c\..
     * Output : C:\         (root only - all name elements cancelled)
     * </pre>
     */
    private void demonstrateNormalizationPreservesRoot() {
        printSection("EDGE CASE 15 - Root always preserved (absolute paths)");

        // All name elements cancelled - only root should remain
        Path allCancelled = buildWindowsPath("C:\\", "a", "..", "b", "..", "c", "..");
        Path normalized = allCancelled.normalize();

        log.info("  Input     : {}", allCancelled);
        log.info("  Output    : {}", normalized);
        log.info("  Root      : '{}'", normalized.getRoot());
        log.info("  Name count: {}", normalized.getNameCount()); // expected: 0
    }

    // -----------------------------------------------------------------------
    //  Edge Case 16 - Path equality: two different strings, same logical path
    // -----------------------------------------------------------------------

    /**
     * Demonstrates how {@code normalize()} enables accurate equality comparisons
     * between paths that are logically identical but syntactically different.
     *
     * <p>Without normalization, {@link Path#equals(Object)} performs a purely
     * syntactic comparison - two paths pointing to the same location but written
     * differently will <em>not</em> be equal.</p>
     *
     * <pre>
     * pathA  = C:\tests\java\io\data\file.txt
     * pathB  = C:\tests\java\io\.\data\..\data\file.txt
     * pathA.equals(pathB)               -> false
     * pathA.normalize().equals(pathB.normalize()) -> true
     * </pre>
     */
    private void demonstratePathEqualityAfterNormalize() {
        printSection("EDGE CASE 16 - Equality: logically identical, syntactically different");

        Path pathA = Paths.get(BASE_DIR, "data", "file.txt");
        Path pathB = Paths.get(BASE_DIR, ".", "data", "..", "data", "file.txt");

        log.info("  pathA : {}", pathA);
        log.info("  pathB : {}", pathB);
        log.info("  pathA.equals(pathB)                         : {}", pathA.equals(pathB));
        log.info("  pathA.normalize().equals(pathB.normalize()) : {}",
                pathA.normalize().equals(pathB.normalize()));
    }

    // -----------------------------------------------------------------------
    //  Edge Case 17 - Real file-system operation with normalization
    // -----------------------------------------------------------------------

    /**
     * Demonstrates a complete, realistic NIO.2 workflow where a path with redundant
     * components is first <em>normalized</em> before being passed to
     * {@link Files#exists(Path, java.nio.file.LinkOption...)},
     * {@link Files#createDirectories(Path, java.nio.file.attribute.FileAttribute[])},
     * and {@link Files#writeString(Path, CharSequence, java.nio.file.OpenOption...)}.
     *
     * <p>Always normalize before any {@code Files.*} operation when the path was
     * constructed programmatically from user input or concatenated strings - this
     * prevents subtle bugs where two different string representations of the same
     * directory are treated as distinct locations.</p>
     *
     * <p>This method creates a temporary file under the system temp directory
     * (not under {@code C:\tests\java\io}) to remain cross-platform runnable.</p>
     */
    private void demonstrateFileSystemOperationWithNormalization() {
        printSection("EDGE CASE 17 - Real NIO.2 file-system operation using normalize()");

        // Build a messy path via the system temp directory (cross-platform)
        Path tempBase = Path.of(System.getProperty("java.io.tmpdir"));
        Path messyDir  = tempBase.resolve("desabisc").resolve(".").resolve("guide")
                .resolve("..").resolve("guide").resolve("output");
        Path normalDir = messyDir.normalize();

        log.info("  Messy directory path : {}", messyDir);
        log.info("  Normalized directory : {}", normalDir);

        try {
            // Always use the NORMALIZED path for file-system I/O
            Files.createDirectories(normalDir);
            log.info("  Directory created    : {}", Files.exists(normalDir));

            Path outputFile = normalDir.resolve("normalize-guide.txt");
            Files.writeString(outputFile,
                    "Written using a normalized NIO.2 path.\n" +
                    "Original (messy) path: " + messyDir + "\n" +
                    "Normalized path      : " + normalDir);

            log.info("  File written to      : {}", outputFile);
            log.info("  File exists?         : {}", Files.exists(outputFile));
            log.info("  File size (bytes)    : {}", Files.size(outputFile));

            // Clean up
            Files.delete(outputFile);
            Files.delete(normalDir);
            Files.delete(normalDir.getParent()); // 'desabisc'
            log.info("  Cleanup complete.");

        } catch (IOException e) {
            log.error("  I/O error during file-system demo: {}", e.getMessage(), e);
        }
    }

    // -----------------------------------------------------------------------
    //  Utility Helpers
    // -----------------------------------------------------------------------

    /**
     * Constructs a {@link Path} from a Windows-style root string and subsequent
     * path components. Catches {@link InvalidPathException} gracefully and returns
     * a fallback relative path when running on a non-Windows file system where
     * Windows-style drive letters are rejected by the default provider.
     *
     * @param root       the root component (e.g., {@code "C:\\"} or {@code "\\\\server\\share"})
     * @param components additional path name elements appended after the root
     * @return a {@link Path} built from the root and components; falls back to a
     *         relative path if the root is invalid on the current file system
     */
    private Path buildWindowsPath(String root, String... components) {
        try {
            return Paths.get(root, components);
        } catch (InvalidPathException e) {
            // Fallback: construct a relative demo path for non-Windows environments
            log.debug("  [buildWindowsPath] Windows path '{}' invalid on this OS, " +
                    "using relative fallback.", root);
            return Paths.get("C_drive_root", components);
        }
    }

    /**
     * Prints a clearly delimited section header to the log for readability.
     * Uses {@code @Slf4j}'s {@code log} instance at INFO level.
     *
     * @param title the section title to display between separator lines
     */
    private void printSection(String title) {
        log.info("");
        log.info("-".repeat(70));
        log.info("  >> {}", title);
        log.info("-".repeat(70));
    }
}