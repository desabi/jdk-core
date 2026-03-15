package com.desabisc.guide.java.io.eresolve;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <h2>Path.resolve() - Complete Field Guide</h2>
 *
 * <p><b>Used to concatenate paths in a manner similar to how we concatenate strings.</b></p>
 *
 * <p>This class is a comprehensive, executable guide to the {@link Path#resolve(Path)} and
 * {@link Path#resolve(String)} methods from the Java NIO.2 API ({@code java.nio.file}).
 * Every method in this class isolates one specific behavior or edge case, and is designed
 * to be read sequentially as a learning resource.</p>
 *
 * <h3>Contract of {@code resolve()} in one sentence</h3>
 * <blockquote>
 *   Given a <em>base</em> path {@code B} and an <em>other</em> path {@code O},
 *   {@code B.resolve(O)} returns:
 *   <ul>
 *     <li>{@code O}      - if {@code O} is <strong>absolute</strong></li>
 *     <li>{@code B}      - if {@code O} is <strong>empty</strong></li>
 *     <li>{@code B / O}  - in every other case (relative {@code O})</li>
 *   </ul>
 * </blockquote>
 *
 * <h3>Working directory used throughout</h3>
 * <pre>  C:\tests\java\io</pre>
 *
 * <h3>NIO.2 specification references</h3>
 * <ul>
 *   <li>{@code java.nio.file.Path#resolve(Path)}</li>
 *   <li>{@code java.nio.file.Path#resolve(String)}</li>
 *   <li>JEP 203 / JSR-203 (NIO.2)</li>
 * </ul>
 *
 * <p><strong>All methods are intentionally {@code private}</strong> so that each
 * example is a self-contained unit of education accessible only through {@code main}.</p>
 *
 * @author  Senior Java Developer - NIO.2 Field Guide Series
 * @version 1.0
 * @since   Java 7 (NIO.2)
 */
public final class PathResolveGuide {

    // -------------------------------------------------------------------------
    // Constants - anchor paths used across all examples
    // -------------------------------------------------------------------------

    /** Absolute working-directory path used as the typical <em>base</em>. */
    private static final Path WORKING_DIR = Paths.get("C:\\tests\\java\\io");

    /** Section divider width for console output. */
    private static final String DIVIDER = "-".repeat(70);

    // -------------------------------------------------------------------------
    // Constructor - utility class: prevent instantiation
    // -------------------------------------------------------------------------

    /** Prevents instantiation of this utility / guide class. */
    private PathResolveGuide() {
        throw new UnsupportedOperationException("Guide class - not instantiable.");
    }

    // =========================================================================
    // ENTRY POINT
    // =========================================================================

    /**
     * Entry point.  Calls every example method in logical learning order:
     *
     * <ol>
     *   <li>Overload overview ({@code Path} vs {@code String})</li>
     *   <li>Core rule 1 - absolute {@code other} replaces base</li>
     *   <li>Core rule 2 - empty {@code other} returns base</li>
     *   <li>Core rule 3 - relative {@code other} is appended</li>
     *   <li>Multi-segment relative resolution</li>
     *   <li>Dot-segment handling ({@code .} and {@code ..})</li>
     *   <li>Normalise after resolve</li>
     *   <li>Resolve on a relative base</li>
     *   <li>Resolve from the filesystem root</li>
     *   <li>Resolve vs {@code resolveSibling}</li>
     *   <li>Cross-root resolution (Windows drives)</li>
     *   <li>Chaining resolve calls</li>
     *   <li>{@code null} argument guard</li>
     * </ol>
     *
     * @param args command-line arguments (ignored).
     */
    public static void main(String[] args) {
        printHeader("Path.resolve() - Complete Field Guide");
        printSection("C:\\tests\\java\\io  ←  working directory");

        example01_overloadOverview();
        example02_absoluteOtherReplacesBase();
        example03_emptyOtherReturnsBase();
        example04_relativeOtherAppendedToBase();
        example05_multiSegmentRelative();
        example06_dotSegmentSingleDot();
        example07_dotSegmentDoubleDot();
        example08_normalizeAfterResolve();
        example09_relativeBase();
        example10_rootBase();
        example11_resolveVsResolveSibling();
        example12_crossRootWindowsDrives();
        example13_chainingResolve();
        example14_nullArgumentGuard();

        printFooter();
    }

    // =========================================================================
    // EXAMPLE 01 - Overload overview: Path vs String
    // =========================================================================

    /**
     * <h3>Example 01 - Two overloads: {@code resolve(Path)} and {@code resolve(String)}</h3>
     *
     * <p>{@link Path} exposes two {@code resolve} overloads:</p>
     * <pre>
     *   Path  resolve(Path   other)   // JDK 7+
     *   Path  resolve(String other)   // Convenience - calls Paths.get(other) internally
     * </pre>
     *
     * <p>The {@code String} overload is purely a convenience wrapper.
     * Both produce identical results when the string represents the same path.</p>
     *
     * <p><strong>Output expected:</strong></p>
     * <pre>
     *   resolve(Path)   -> C:\tests\java\io\output\result.txt
     *   resolve(String) -> C:\tests\java\io\output\result.txt
     *   Equal? true
     * </pre>
     */
    private static void example01_overloadOverview() {
        printExample("01", "Two overloads - resolve(Path) vs resolve(String)");

        Path base = WORKING_DIR;                                // C:\tests\java\io
        String relativeStr = "output\\result.txt";

        Path viaPathOverload   = base.resolve(Paths.get(relativeStr));
        Path viaStringOverload = base.resolve(relativeStr);

        print("base               = " + base);
        print("other (string)     = " + relativeStr);
        print("resolve(Path)      -> " + viaPathOverload);
        print("resolve(String)    -> " + viaStringOverload);
        print("Both equal?        -> " + viaPathOverload.equals(viaStringOverload));

        note("The String overload is syntactic sugar. Prefer resolve(String) for "
                + "literals; resolve(Path) when composing programmatic paths.");
    }

    // =========================================================================
    // EXAMPLE 02 - Absolute other replaces the base entirely
    // =========================================================================

    /**
     * <h3>Example 02 - Core Rule 1: Absolute {@code other} replaces the base</h3>
     *
     * <p>This is the most important rule and the one most often misunderstood.</p>
     *
     * <blockquote>
     * <em>"If {@code other} is an absolute path, this method trivially returns {@code other}."</em>
     * - {@code Path.resolve()} Javadoc
     * </blockquote>
     *
     * <p>This mirrors the behaviour of POSIX's {@code realpath} and is consistent with
     * how most operating-system path APIs handle absolute path arguments.</p>
     *
     * <p><strong>Practical implication:</strong> This is intentional by design - it lets
     * callers pass either an absolute override or a relative fragment without changing
     * the call site:
     * <pre>
     *   // Caller can always do this - it "just works" regardless of user input:
     *   Path resolved = basePath.resolve(userSuppliedPath);
     * </pre>
     * </p>
     *
     * <p><strong>Output expected:</strong></p>
     * <pre>
     *   base            = C:\tests\java\io
     *   absolute other  = C:\logs\app.log
     *   resolved        -> C:\logs\app.log   ← base is completely discarded
     * </pre>
     *
     * <p><strong>Warning:</strong> This rule makes it trivial to introduce a path-traversal
     * vulnerability if user-supplied paths are resolved without validation.
     * Always validate or sanitise input before calling {@code resolve()}.</p>
     */
    private static void example02_absoluteOtherReplacesBase() {
        printExample("02", "Core Rule 1 - absolute 'other' replaces the entire base");

        Path base          = WORKING_DIR;                       // C:\tests\java\io
        Path absoluteOther = Paths.get("C:\\logs\\app.log");    // fully absolute

        Path resolved = base.resolve(absoluteOther);

        print("base            = " + base);
        print("absolute other  = " + absoluteOther);
        print("resolved        -> " + resolved);
        print("base discarded? -> " + !resolved.startsWith(base));

        note("Security note: never call resolve() with untrusted user input "
                + "without first checking 'other.isAbsolute()' or validating the result "
                + "starts with your intended root via 'resolved.startsWith(trustedRoot)'.");
    }

    // =========================================================================
    // EXAMPLE 03 - Empty other returns the base unchanged
    // =========================================================================

    /**
     * <h3>Example 03 - Core Rule 2: Empty {@code other} returns the base unchanged</h3>
     *
     * <p>When {@code other} is an <em>empty path</em> (a path with zero name elements
     * and no root component), {@code resolve()} returns the base without modification.
     * Conceptually this is equivalent to appending nothing.</p>
     *
     * <p>An empty path is obtained via:
     * <pre>
     *   Paths.get("")     // empty-string constructor
     * </pre>
     * Note that this is <strong>NOT</strong> the same as {@code null} - passing
     * {@code null} throws {@link NullPointerException}.</p>
     *
     * <p><strong>Output expected:</strong></p>
     * <pre>
     *   base          = C:\tests\java\io
     *   empty other   = (empty)
     *   resolved      -> C:\tests\java\io    ← base returned as-is
     *   Same object?  -> false  (new Path instance, but equal)
     *   Equal?        -> true
     * </pre>
     */
    private static void example03_emptyOtherReturnsBase() {
        printExample("03", "Core Rule 2 - empty 'other' returns the base unchanged");

        Path base       = WORKING_DIR;        // C:\tests\java\io
        Path emptyOther = Paths.get("");      // zero-element path

        Path resolved = base.resolve(emptyOther);

        print("base           = " + base);
        print("empty other    = '" + emptyOther + "'");
        print("resolved       -> " + resolved);
        print("Same instance? -> " + (base == resolved));
        print("Equal value?   -> " + base.equals(resolved));

        note("Paths.get(\"\") represents the 'current directory' token in many "
                + "file-system operations but is treated as 'no-op' by resolve().");
    }

    // =========================================================================
    // EXAMPLE 04 - Relative other is appended to the base
    // =========================================================================

    /**
     * <h3>Example 04 - Core Rule 3: Relative {@code other} is appended to the base</h3>
     *
     * <p>This is the everyday happy-path use of {@code resolve()}. When {@code other}
     * is a relative path, it is appended to the base path using the platform's
     * name separator ({@code \} on Windows, {@code /} on Unix).</p>
     *
     * <p>The resulting path is <em>not necessarily normalised</em> - it is a syntactic
     * concatenation. Call {@link Path#normalize()} if you need to collapse redundant
     * segments (see Example 08).</p>
     *
     * <p><strong>Output expected:</strong></p>
     * <pre>
     *   base     = C:\tests\java\io
     *   other    = reports\2024\summary.csv
     *   resolved -> C:\tests\java\io\reports\2024\summary.csv
     * </pre>
     */
    private static void example04_relativeOtherAppendedToBase() {
        printExample("04", "Core Rule 3 - relative 'other' is appended to the base");

        Path base  = WORKING_DIR;                               // C:\tests\java\io
        Path other = Paths.get("reports", "2024", "summary.csv");

        Path resolved = base.resolve(other);

        print("base       = " + base);
        print("other      = " + other);
        print("resolved   -> " + resolved);
        print("Is absolute? -> " + resolved.isAbsolute());

        note("resolve() performs a pure syntactic operation - no file-system access "
                + "occurs. The returned path may or may not exist on disk.");
    }

    // =========================================================================
    // EXAMPLE 05 - Multi-segment relative resolution
    // =========================================================================

    /**
     * <h3>Example 05 - Multi-segment relative path with {@code Paths.get()}</h3>
     *
     * <p>Demonstrates that {@code other} can carry arbitrarily deep path hierarchies
     * in a single call. The convenience factory {@link Paths#get(String, String...)}
     * joins segments using the platform separator before resolve is called.</p>
     *
     * <p><strong>Two equivalent ways to express the same thing:</strong></p>
     * <pre>
     *   // Option A: single composite string
     *   base.resolve("modules\\core\\src\\main\\java\\App.java");
     *
     *   // Option B: varargs - preferred: platform-independent, no hard-coded separators
     *   base.resolve(Paths.get("modules", "core", "src", "main", "java", "App.java"));
     * </pre>
     *
     * <p><strong>Output expected:</strong></p>
     * <pre>
     *   resolved -> C:\tests\java\io\modules\core\src\main\java\App.java
     * </pre>
     */
    private static void example05_multiSegmentRelative() {
        printExample("05", "Multi-segment relative path");

        Path base = WORKING_DIR;

        // Option A - embedded separator (platform-specific string literal)
        Path resolvedA = base.resolve("modules\\core\\src\\main\\java\\App.java");

        // Option B - varargs (platform-independent, recommended)
        Path resolvedB = base.resolve(
                Paths.get("modules", "core", "src", "main", "java", "App.java"));

        print("base       = " + base);
        print("Option A   -> " + resolvedA);
        print("Option B   -> " + resolvedB);
        print("Equal?     -> " + resolvedA.equals(resolvedB));

        note("Prefer Paths.get(String, String...) varargs - it uses the correct "
                + "platform separator automatically, making code portable across OSes.");
    }

    // =========================================================================
    // EXAMPLE 06 - Dot-segment: single dot "."
    // =========================================================================

    /**
     * <h3>Example 06 - Dot-segment: single dot ({@code .})</h3>
     *
     * <p>A single {@code .} is a valid path name element meaning "current directory".
     * {@code resolve()} is a <em>purely syntactic</em> operation - it does <strong>not</strong>
     * interpret or eliminate the dot.  The result therefore contains the literal {@code .}
     * segment.</p>
     *
     * <p>To eliminate it, call {@link Path#normalize()} afterwards.</p>
     *
     * <p><strong>Output expected:</strong></p>
     * <pre>
     *   resolved             -> C:\tests\java\io\.\config
     *   after normalize()    -> C:\tests\java\io\config
     * </pre>
     *
     * @see Path#normalize()
     */
    private static void example06_dotSegmentSingleDot() {
        printExample("06", "Dot segment '.' - resolve does NOT eliminate it");

        Path base  = WORKING_DIR;
        Path other = Paths.get(".", "config");   // .  is kept literally

        Path resolved   = base.resolve(other);
        Path normalised = resolved.normalize();

        print("base              = " + base);
        print("other             = " + other);
        print("resolved          -> " + resolved);
        print("after normalize() -> " + normalised);

        note("resolve() is syntactic - it never accesses the file system and "
                + "never interprets '.' or '..'. Always normalise if you need a "
                + "canonical representation.");
    }

    // =========================================================================
    // EXAMPLE 07 - Dot-segment: double dot ".."
    // =========================================================================

    /**
     * <h3>Example 07 - Dot-segment: double dot ({@code ..})</h3>
     *
     * <p>A {@code ..} segment means "parent directory".  Again, {@code resolve()} is
     * syntactic: the double-dot is appended verbatim.  The resulting path literally
     * contains {@code ..}, which may look surprising until {@link Path#normalize()} is
     * called.</p>
     *
     * <p><strong>Three levels up, then back down - step by step:</strong></p>
     * <pre>
     *   base     = C:\tests\java\io
     *   other    = ..\..\shared\lib
     *
     *   raw      = C:\tests\java\io\..\..\shared\lib
     *   normalised = C:\tests\shared\lib          ← two levels removed
     * </pre>
     *
     * <p><strong>Warning - root escape:</strong> it is possible to construct a path that
     * escapes a trusted root via {@code ..} traversal.  After resolving, verify the result
     * still starts with the intended root:</p>
     * <pre>
     *   if (!resolved.normalize().startsWith(trustedRoot)) {
     *       throw new SecurityException("Path traversal detected!");
     *   }
     * </pre>
     *
     * @see Path#normalize()
     */
    private static void example07_dotSegmentDoubleDot() {
        printExample("07", "Dot-dot segment '..' - path traversal, raw vs normalised");

        Path base  = WORKING_DIR;                             // C:\tests\java\io
        Path other = Paths.get("..", "..", "shared", "lib");  // go up 2, then down

        Path resolved   = base.resolve(other);
        Path normalised = resolved.normalize();

        print("base              = " + base);
        print("other             = " + other);
        print("raw resolved      -> " + resolved);
        print("after normalize() -> " + normalised);
        print("Name count (raw)  -> " + resolved.getNameCount());
        print("Name count (norm) -> " + normalised.getNameCount());

        // Security demonstration - detecting root escape
        Path trustedRoot = WORKING_DIR;
        boolean safe = normalised.startsWith(trustedRoot);
        print("Still inside trusted root? -> " + safe + "  ← traversal escaped root!");

        note("Never skip the startsWith(trustedRoot) guard when resolving "
                + "paths that may contain user-supplied '..' segments.");
    }

    // =========================================================================
    // EXAMPLE 08 - Normalize after resolve (canonical path)
    // =========================================================================

    /**
     * <h3>Example 08 - {@code normalize()} after {@code resolve()}</h3>
     *
     * <p>This example consolidates the normalize idiom.  A realistic scenario:
     * a configuration file reader receives a relative path fragment that may contain
     * redundant segments.  The two-step idiom is:
     * <pre>
     *   Path result = base.resolve(fragment).normalize();
     * </pre>
     * </p>
     *
     * <p>{@code normalize()} removes redundant {@code .} and resolves {@code ..} elements
     * <em>syntactically</em> (no filesystem access). For a filesystem-backed canonical path
     * (resolving symlinks, verifying existence) use {@link java.nio.file.Path#toRealPath}
     * instead.</p>
     *
     * <p><strong>Comparison:</strong></p>
     * <pre>
     *   normalize() - syntactic, no I/O, path may not exist
     *   toRealPath() - semantic, requires I/O, path MUST exist
     * </pre>
     *
     * @see Path#normalize()
     * @see Path#toRealPath(java.nio.file.LinkOption...)
     */
    private static void example08_normalizeAfterResolve() {
        printExample("08", "normalize() after resolve() - clean canonical form");

        Path base     = WORKING_DIR;
        String messy  = "reports\\.\\2024\\..\\2025\\final.xlsx";  // redundant dots

        Path raw        = base.resolve(messy);
        Path normalised = raw.normalize();

        print("base            = " + base);
        print("messy fragment  = " + messy);
        print("raw resolved    -> " + raw);
        print("normalised      -> " + normalised);

        note("Rule of thumb: always normalise when the fragment comes from external "
                + "input (config files, CLI args, HTTP query params). "
                + "For symlink resolution or existence checks, escalate to toRealPath().");
    }

    // =========================================================================
    // EXAMPLE 09 - Resolve on a relative base
    // =========================================================================

    /**
     * <h3>Example 09 - Base is itself a relative path</h3>
     *
     * <p>The base path does not need to be absolute.  When the base is relative,
     * the result is also relative - the same three rules apply.  This is useful
     * when constructing sub-paths within a module or building portable relative
     * references that are anchored later.</p>
     *
     * <p>Use cases:</p>
     * <ul>
     *   <li>Building paths inside a portable ZIP or JAR layout.</li>
     *   <li>Unit-testing path logic without referencing real OS roots.</li>
     *   <li>Constructing Maven/Gradle relative source-set paths.</li>
     * </ul>
     *
     * <p><strong>Output expected:</strong></p>
     * <pre>
     *   relative base = src\main\java
     *   other         = com\example\App.java
     *   resolved      -> src\main\java\com\example\App.java
     *   Is absolute?  -> false
     * </pre>
     */
    private static void example09_relativeBase() {
        printExample("09", "Relative base - result is also relative");

        Path relativeBase = Paths.get("src", "main", "java");
        Path other        = Paths.get("com", "example", "App.java");

        Path resolved = relativeBase.resolve(other);

        print("relative base  = " + relativeBase);
        print("other          = " + other);
        print("resolved       -> " + resolved);
        print("Is absolute?   -> " + resolved.isAbsolute());
        print("Name count     -> " + resolved.getNameCount());

        note("A relative base gives a relative result. To anchor it to the JVM's "
                + "current working directory, call: "
                + "Path.of(\"\").toAbsolutePath().resolve(relativeBase).resolve(other)");
    }

    // =========================================================================
    // EXAMPLE 10 - Resolve from the filesystem root
    // =========================================================================

    /**
     * <h3>Example 10 - Base is the filesystem root ({@code C:\})</h3>
     *
     * <p>The root component of a Windows filesystem ({@code C:\}) is a valid
     * {@link Path}.  Resolving from the root produces an absolute path anchored
     * directly to the drive root.</p>
     *
     * <p>This is handy when building absolute paths from a known drive letter without
     * hard-coding the full prefix in every call site.</p>
     *
     * <p><strong>Output expected:</strong></p>
     * <pre>
     *   root base  = C:\
     *   other      = tests\java\io\data.bin
     *   resolved   -> C:\tests\java\io\data.bin
     * </pre>
     */
    private static void example10_rootBase() {
        printExample("10", "Resolve from filesystem root (C:\\)");

        // Obtain the root of the working directory's filesystem
        Path rootBase = WORKING_DIR.getRoot();   // C:\  (Windows)
        Path other    = Paths.get("tests", "java", "io", "data.bin");

        Path resolved = rootBase.resolve(other);

        print("root base  = " + rootBase);
        print("other      = " + other);
        print("resolved   -> " + resolved);
        print("Root of result -> " + resolved.getRoot());

        note("getRoot() returns null for relative paths, so guard with a null "
                + "check before using it as a base.");
    }

    // =========================================================================
    // EXAMPLE 11 - resolve() vs resolveSibling()
    // =========================================================================

    /**
     * <h3>Example 11 - {@code resolve()} vs {@code resolveSibling()}</h3>
     *
     * <p>A common point of confusion: what is the difference between
     * {@link Path#resolve(String)} and {@link Path#resolveSibling(String)}?</p>
     *
     * <table border="1" cellpadding="5">
     *   <caption>Behavioural comparison</caption>
     *   <tr>
     *     <th>Method</th>
     *     <th>Appends to</th>
     *     <th>Analogy</th>
     *   </tr>
     *   <tr>
     *     <td>{@code resolve(other)}</td>
     *     <td>the full base path</td>
     *     <td>step <em>into</em> a child</td>
     *   </tr>
     *   <tr>
     *     <td>{@code resolveSibling(other)}</td>
     *     <td>the <em>parent</em> of base</td>
     *     <td>rename / swap the last element</td>
     *   </tr>
     * </table>
     *
     * <p>{@code resolveSibling(s)} is equivalent to {@code base.getParent().resolve(s)},
     * with the added convenience that it handles the case where {@code base} has no
     * parent (returns {@code other}).</p>
     *
     * <p><strong>Output expected:</strong></p>
     * <pre>
     *   base file         = C:\tests\java\io\input.txt
     *   resolve(other)    -> C:\tests\java\io\input.txt\output.txt   ← child
     *   resolveSibling(o) -> C:\tests\java\io\output.txt             ← sibling
     * </pre>
     */
    private static void example11_resolveVsResolveSibling() {
        printExample("11", "resolve() vs resolveSibling() - child vs sibling");

        Path baseFile = WORKING_DIR.resolve("input.txt"); // C:\tests\java\io\input.txt
        String other  = "output.txt";

        Path childResult   = baseFile.resolve(other);          // treats input.txt as dir
        Path siblingResult = baseFile.resolveSibling(other);   // replaces input.txt

        print("base file            = " + baseFile);
        print("resolve(other)       -> " + childResult);
        print("resolveSibling(other)-> " + siblingResult);

        // Equivalent long-hand for resolveSibling
        Path manualSibling = baseFile.getParent().resolve(other);
        print("getParent().resolve()-> " + manualSibling);
        print("sibling == manual?   -> " + siblingResult.equals(manualSibling));

        note("When working with file paths (leaf = filename), use resolveSibling() "
                + "to swap the filename. Use resolve() only when treating the base "
                + "path as a directory.");
    }

    // =========================================================================
    // EXAMPLE 12 - Cross-root resolution (Windows multi-drive)
    // =========================================================================

    /**
     * <h3>Example 12 - Cross-root resolution on Windows (different drive letters)</h3>
     *
     * <p>On Windows, two paths can have different <em>roots</em> (drive letters: {@code C:\},
     * {@code D:\}, etc.).  The NIO.2 specification leaves the behaviour of resolving across
     * different roots as implementation-defined.  On the Windows {@code WindowsFileSystem}
     * provider the rule is:</p>
     * <ul>
     *   <li>If {@code other} has a root component ({@code D:\...}), it is treated as
     *       absolute -> Core Rule 1 applies: {@code other} is returned.</li>
     *   <li>If {@code other} has a root but no leading separator (e.g., {@code D:relative}),
     *       behaviour is drive-relative and may differ from what you expect.</li>
     * </ul>
     *
     * <p><strong>Output expected (Windows JVM):</strong></p>
     * <pre>
     *   base (C drive)            = C:\tests\java\io
     *   absolute other (D drive)  = D:\data\archive
     *   resolved                  -> D:\data\archive  ← C: base discarded (absolute rule)
     * </pre>
     *
     * <p><strong>Note:</strong> On a non-Windows JVM, paths created with {@code C:\} are
     * treated as relative paths with a name element {@code "C:\tests\java\io"} - the
     * results will differ.  This example is intended for Windows-deployed code.</p>
     */
    private static void example12_crossRootWindowsDrives() {
        printExample("12", "Cross-root (different Windows drive letters)");

        Path baseCDrive    = WORKING_DIR;                          // C:\tests\java\io
        Path absoluteDrive = Paths.get("D:\\data\\archive");      // fully absolute, D:

        Path resolved = baseCDrive.resolve(absoluteDrive);

        print("base (C:\\)      = " + baseCDrive);
        print("other (D:\\)     = " + absoluteDrive);
        print("resolved        -> " + resolved);
        print("Base discarded? -> " + !resolved.startsWith(baseCDrive));
        print("Root of result  -> " + resolved.getRoot());

        note("Windows-specific: 'D:relative' (root-only, no leading backslash) creates "
                + "a drive-relative path, whose resolution against a different-drive base "
                + "is OS-implementation-specific. Prefer always-absolute or always-relative "
                + "paths to avoid ambiguity.");
    }

    // =========================================================================
    // EXAMPLE 13 - Chaining resolve calls
    // =========================================================================

    /**
     * <h3>Example 13 - Chaining multiple {@code resolve()} calls</h3>
     *
     * <p>Because {@code resolve()} returns a {@link Path}, calls can be chained
     * in a fluent style.  This is idiomatic Java NIO.2 and is particularly readable
     * when constructing deep directory hierarchies step by step.</p>
     *
     * <p><strong>Equivalences:</strong></p>
     * <pre>
     *   // One call with compound path
     *   base.resolve(Paths.get("a", "b", "c", "file.txt"))
     *
     *   // Chained calls - identical result
     *   base.resolve("a").resolve("b").resolve("c").resolve("file.txt")
     * </pre>
     *
     * <p>The chained form can be clearer when each segment is computed dynamically
     * (e.g., from variables or method calls).</p>
     *
     * <p><strong>Output expected:</strong></p>
     * <pre>
     *   compound -> C:\tests\java\io\2025\Q1\January\report.pdf
     *   chained  -> C:\tests\java\io\2025\Q1\January\report.pdf
     *   Equal?   -> true
     * </pre>
     */
    private static void example13_chainingResolve() {
        printExample("13", "Chaining resolve() calls - fluent path construction");

        String year    = "2025";
        String quarter = "Q1";
        String month   = "January";
        String file    = "report.pdf";

        // Single compound call
        Path compound = WORKING_DIR.resolve(
                Paths.get(year, quarter, month, file));

        // Equivalent chained calls - useful when segments are dynamic
        Path chained = WORKING_DIR
                .resolve(year)
                .resolve(quarter)
                .resolve(month)
                .resolve(file);

        print("base     = " + WORKING_DIR);
        print("compound -> " + compound);
        print("chained  -> " + chained);
        print("Equal?   -> " + compound.equals(chained));

        note("Both styles produce the same result. Choose the style that best "
                + "communicates intent: compound for static structures, "
                + "chained for dynamic or conditional path construction.");
    }

    // =========================================================================
    // EXAMPLE 14 - Null argument guard
    // =========================================================================

    /**
     * <h3>Example 14 - {@code null} argument throws {@link NullPointerException}</h3>
     *
     * <p>Passing {@code null} to either overload of {@code resolve()} throws
     * {@link NullPointerException} immediately. The JDK contract is explicit:</p>
     * <blockquote>
     *   <em>"Throws: NullPointerException – if other is null."</em>
     * </blockquote>
     *
     * <p>This is the standard JDK {@code null}-contract for NIO.2 path operations.
     * Always validate externally sourced paths before passing them to {@code resolve()}.</p>
     *
     * <p><strong>Recommended guard pattern:</strong></p>
     * <pre>
     *   Objects.requireNonNull(fragment, "path fragment must not be null");
     *   Path resolved = base.resolve(fragment);
     * </pre>
     *
     * <p><strong>Output expected:</strong></p>
     * <pre>
     *   Caught NullPointerException - as specified by the JDK contract.
     * </pre>
     */
    private static void example14_nullArgumentGuard() {
        printExample("14", "null argument - NullPointerException guard");

        Path base = WORKING_DIR;

        // --- resolve(Path) with null ---
        try {
            Path nullPath = null;
            base.resolve(nullPath);               // must throw NPE
            print("ERROR: expected NullPointerException was NOT thrown!");
        } catch (NullPointerException npe) {
            print("resolve(Path null)   -> caught NullPointerException ✓ (expected)");
        }

        // --- resolve(String) with null ---
        try {
            String nullString = null;
            base.resolve(nullString);             // must throw NPE
            print("ERROR: expected NullPointerException was NOT thrown!");
        } catch (NullPointerException npe) {
            print("resolve(String null) -> caught NullPointerException ✓ (expected)");
        }

        note("Use Objects.requireNonNull() or Optional to guard inputs before "
                + "calling resolve(). Fail fast with a meaningful message rather "
                + "than letting the NPE surface deep in library code.");
    }

    // =========================================================================
    // CONSOLE OUTPUT HELPERS
    // =========================================================================

    private static void printHeader(String title) {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.printf("  %s%n", title);
        System.out.println("=".repeat(70));
    }

    private static void printSection(String text) {
        System.out.printf("  =  %s%n%n", text);
    }

    private static void printExample(String number, String title) {
        System.out.println();
        System.out.println(DIVIDER);
        System.out.printf("  [Example %s]  %s%n", number, title);
        System.out.println(DIVIDER);
    }

    private static void print(String message) {
        System.out.printf("    %s%n", message);
    }

    private static void note(String message) {
        System.out.println();
        // Wrap note text at ~65 chars for readability
        String prefix = "  ✎ NOTE: ";
        String indent = "           ";
        String[] words = message.split(" ");
        StringBuilder line = new StringBuilder(prefix);
        for (String word : words) {
            if (line.length() + word.length() + 1 > 70 && line.length() > prefix.length()) {
                System.out.println(line);
                line = new StringBuilder(indent);
            }
            line.append(word).append(" ");
        }
        System.out.println(line);
    }

    private static void printFooter() {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("  End of Path.resolve() Field Guide");
        System.out.printf("  FileSystem provider : %s%n",
                FileSystems.getDefault().provider().getScheme());
        System.out.printf("  Separator           : '%s'%n",
                FileSystems.getDefault().getSeparator());
        System.out.println("=".repeat(70));
        System.out.println();
    }
}