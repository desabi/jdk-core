package com.desabisc.guide.java.io.frelativize;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <h2>PathRelativizeGuide - Comprehensive Guide to {@link Path#relativize(Path)}</h2>
 *
 * <p>This class demonstrates every significant use-case and edge case of the NIO.2
 * {@link Path#relativize(Path)} method, which constructs a <em>relative</em> path
 * from one location to another.</p>
 *
 * <h3>Contract of {@code relativize()}</h3>
 * <pre>
 *   Given:  base.resolve( base.relativize(target) )
 *   Result: a path that locates the same file as {@code target}
 * </pre>
 *
 * <p>In other words, {@code relativize} is the <strong>inverse</strong> of
 * {@link Path#resolve(Path)}.</p>
 *
 * <h3>Key Rules</h3>
 * <ul>
 *   <li>Both paths must be of the same type: <em>both absolute</em> or
 *       <em>both relative</em>. Mixing types throws
 *       {@link IllegalArgumentException}.</li>
 *   <li>On Windows, both absolute paths must share the same <em>root</em>
 *       (e.g. {@code C:\}). Different roots throw
 *       {@link IllegalArgumentException}.</li>
 *   <li>If {@code base.equals(target)}, the result is an <em>empty path</em>
 *       ({@code ""}).</li>
 *   <li>Each step "up" the directory tree is expressed as {@code ..}.</li>
 * </ul>
 *
 * <h3>Working Directory</h3>
 * <p>All examples use {@code C:\tests\java\io} as the conceptual working root.</p>
 *
 * <p><strong>Dependencies:</strong> Lombok {@code @Slf4j} (SLF4J + implementation
 * on the runtime classpath, e.g. Logback).</p>
 *
 * @author  desabisc
 * @version 1.0
 * @since   JDK 17+
 * @see     Path#relativize(Path)
 * @see     Path#resolve(Path)
 */
@Slf4j
public class PathRelativizeGuide {

    // -----------------------------------------------------------------------
    // Constants - working root and commonly shared sub-directories
    // -----------------------------------------------------------------------

    /** Root working directory used throughout this guide. */
    private static final Path WORKING_ROOT = Paths.get("C:\\tests\\java\\io");

    /** Sub-directory: {@code C:\tests\java\io\reports} */
    private static final Path DIR_REPORTS  = WORKING_ROOT.resolve("reports");

    /** Sub-directory: {@code C:\tests\java\io\docs} */
    private static final Path DIR_DOCS     = WORKING_ROOT.resolve("docs");

    /** Sub-directory: {@code C:\tests\java\io\data\csv} */
    private static final Path DIR_DATA_CSV = WORKING_ROOT.resolve("data").resolve("csv");

    /** Sub-directory: {@code C:\tests\java\io\data\json} */
    private static final Path DIR_DATA_JSON = WORKING_ROOT.resolve("data").resolve("json");

    /** Sub-directory: {@code C:\tests\java\io\archive\2024\q1} */
    private static final Path DIR_ARCHIVE_Q1 = WORKING_ROOT
            .resolve("archive").resolve("2024").resolve("q1");

    // -----------------------------------------------------------------------
    // Entry Point
    // -----------------------------------------------------------------------

    /**
     * Application entry point.
     *
     * <p>Executes all demonstration methods sequentially. Each method is
     * self-contained and independently illustrates one aspect of
     * {@code relativize()}.</p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        PathRelativizeGuide guide = new PathRelativizeGuide();

        log.info("========================================================");
        log.info("  Path.relativize() - Complete Edge-Case Guide");
        log.info("  Working root: {}", WORKING_ROOT);
        log.info("========================================================");

        guide.demonstrateBasicRelativize();
        guide.demonstrateRelativizeToChild();
        guide.demonstrateRelativizeToParent();
        guide.demonstrateRelativizeSiblingDirectories();
        guide.demonstrateRelativizeToSamePath();
        guide.demonstrateRelativizeWithDeepNesting();
        guide.demonstrateRelativizeTwoRelativePaths();
        guide.demonstrateRelativizeRelativeWithGoingUp();
        guide.demonstrateRoundTripWithResolve();
        guide.demonstrateRelativizeWithFileNames();
        guide.demonstrateIllegalMixAbsoluteRelative();
        guide.demonstrateIllegalDifferentRoots();
        guide.demonstrateNormalizationBeforeRelativize();

        log.info("========================================================");
        log.info("  Guide complete.");
        log.info("========================================================");
    }

    // -----------------------------------------------------------------------
    // Case 1 - Basic: base and target are siblings under the same parent
    // -----------------------------------------------------------------------

    /**
     * <h3>Case 1 - Basic Sibling Relativize (same depth, same parent)</h3>
     *
     * <p>When both paths share the same immediate parent, {@code relativize}
     * produces a single-level relative path: one {@code ..} step up and one
     * step into the target.</p>
     *
     * <pre>{@code
     *   base   : C:\tests\java\io\reports
     *   target : C:\tests\java\io\docs
     *   result : ..\docs
     * }</pre>
     *
     * <p>Interpretation: "from {@code reports}, go one level up, then enter
     * {@code docs}."</p>
     */
    private void demonstrateBasicRelativize() {
        log.info("--- Case 1: Basic sibling relativize ---");

        Path base   = DIR_REPORTS;
        Path target = DIR_DOCS;
        Path result = base.relativize(target);

        log.info("  base   : {}", base);
        log.info("  target : {}", target);
        log.info("  result : {}", result);
        // result → ..\docs  (Windows separator)

        log.info("  name-count of result: {}", result.getNameCount());
        // 2 elements: ".." and "docs"
    }

    // -----------------------------------------------------------------------
    // Case 2 - Relativize from parent to child
    // -----------------------------------------------------------------------

    /**
     * <h3>Case 2 - Parent → Child (target is a descendant of base)</h3>
     *
     * <p>When the target is strictly beneath the base, {@code relativize}
     * yields a <em>purely descending</em> relative path - no {@code ..}
     * segments are needed.</p>
     *
     * <pre>{@code
     *   base   : C:\tests\java\io
     *   target : C:\tests\java\io\data\csv
     *   result : data\csv
     * }</pre>
     *
     * <p>Equivalent to calling {@code base.relativize(target)} only when you
     * know {@code target} starts with {@code base}.</p>
     */
    private void demonstrateRelativizeToChild() {
        log.info("--- Case 2: Parent-to-child relativize ---");

        Path base   = WORKING_ROOT;
        Path target = DIR_DATA_CSV;
        Path result = base.relativize(target);

        log.info("  base   : {}", base);
        log.info("  target : {}", target);
        log.info("  result : {}", result);
        // result → data\csv

        log.info("  No '..' segments - target is a descendant of base: {}",
                !result.toString().contains(".."));
    }

    // -----------------------------------------------------------------------
    // Case 3 - Relativize from child to parent (going up)
    // -----------------------------------------------------------------------

    /**
     * <h3>Case 3 - Child → Parent (target is an ancestor of base)</h3>
     *
     * <p>When the target is an ancestor of the base, every level between
     * base and the common root is represented by one {@code ..} segment.</p>
     *
     * <pre>{@code
     *   base   : C:\tests\java\io\data\csv
     *   target : C:\tests\java\io
     *   result : ..\..
     * }</pre>
     *
     * <p>Two {@code ..} segments because {@code data\csv} is two levels below
     * {@code C:\tests\java\io}.</p>
     */
    private void demonstrateRelativizeToParent() {
        log.info("--- Case 3: Child-to-parent relativize ---");

        Path base   = DIR_DATA_CSV;
        Path target = WORKING_ROOT;
        Path result = base.relativize(target);

        log.info("  base   : {}", base);
        log.info("  target : {}", target);
        log.info("  result : {}", result);
        // result → ..\..

        log.info("  Number of '..' steps: {}", result.getNameCount());
        // Every name element is ".." here
    }

    // -----------------------------------------------------------------------
    // Case 4 - Relativize between siblings at different sub-depths
    // -----------------------------------------------------------------------

    /**
     * <h3>Case 4 - Cross-Branch Siblings (different sub-depths)</h3>
     *
     * <p>When base and target share a common ancestor but differ in depth,
     * the result combines {@code ..} steps (to reach the common ancestor)
     * with descending steps (to reach the target).</p>
     *
     * <pre>{@code
     *   base   : C:\tests\java\io\data\csv
     *   target : C:\tests\java\io\data\json
     *   result : ..\json
     *
     *   base   : C:\tests\java\io\data\csv
     *   target : C:\tests\java\io\archive\2024\q1
     *   result : ..\..\archive\2024\q1
     * }</pre>
     *
     * <p>Visualisation of the path tree:</p>
     * <pre>
     *   C:\tests\java\io\
     *   ├── data\
     *   │   ├── csv\   ← base (example A)
     *   │   └── json\  ← target (example A)
     *   └── archive\
     *       └── 2024\
     *           └── q1\ ← target (example B)
     * </pre>
     */
    private void demonstrateRelativizeSiblingDirectories() {
        log.info("--- Case 4: Cross-branch siblings ---");

        // Example A: same parent (data\)
        Path baseA   = DIR_DATA_CSV;
        Path targetA = DIR_DATA_JSON;
        Path resultA = baseA.relativize(targetA);
        log.info("  [A] base   : {}", baseA);
        log.info("  [A] target : {}", targetA);
        log.info("  [A] result : {}", resultA);
        // result → ..\json

        log.info("  ---");

        // Example B: common ancestor is WORKING_ROOT (two levels up from csv)
        Path baseB   = DIR_DATA_CSV;
        Path targetB = DIR_ARCHIVE_Q1;
        Path resultB = baseB.relativize(targetB);
        log.info("  [B] base   : {}", baseB);
        log.info("  [B] target : {}", targetB);
        log.info("  [B] result : {}", resultB);
        // result → ..\..\archive\2024\q1
    }

    // -----------------------------------------------------------------------
    // Case 5 - Relativize identical paths (empty result)
    // -----------------------------------------------------------------------

    /**
     * <h3>Case 5 - Identical Paths (empty relative path)</h3>
     *
     * <p>When base and target represent the <em>same</em> path,
     * {@code relativize} returns an <strong>empty path</strong> - a path
     * whose {@link Path#toString()} yields {@code ""} and whose
     * {@link Path#getNameCount()} is {@code 0}.</p>
     *
     * <pre>{@code
     *   base   : C:\tests\java\io\reports
     *   target : C:\tests\java\io\reports
     *   result : ""  (empty path)
     * }</pre>
     *
     * <p><strong>Important:</strong> An empty path and {@code "."} are
     * <em>not</em> the same object, but resolving either against {@code base}
     * returns {@code base}.</p>
     */
    private void demonstrateRelativizeToSamePath() {
        log.info("--- Case 5: Identical paths → empty result ---");

        Path base   = DIR_REPORTS;
        Path target = DIR_REPORTS;   // same reference, same path
        Path result = base.relativize(target);

        log.info("  base            : {}", base);
        log.info("  target          : {}", target);
        log.info("  result          : '{}'", result);      // → ''
        log.info("  isEmpty (toString): {}", result.toString().isEmpty());
        log.info("  getNameCount   : {}", result.getNameCount());    // → 0

        // Verify round-trip: base.resolve("") == base
        Path roundTrip = base.resolve(result);
        log.info("  base.resolve(result): {}", roundTrip);
        log.info("  round-trip equals base: {}", roundTrip.equals(base));
    }

    // -----------------------------------------------------------------------
    // Case 6 - Deep nesting (many levels)
    // -----------------------------------------------------------------------

    /**
     * <h3>Case 6 - Deep Nesting (many directory levels)</h3>
     *
     * <p>Demonstrates that {@code relativize} scales linearly with depth.
     * Each extra level of base depth beyond the common ancestor adds one
     * {@code ..} segment.</p>
     *
     * <pre>{@code
     *   base   : C:\tests\java\io\archive\2024\q1
     *   target : C:\tests\java\io\reports
     *   result : ..\..\..\reports    (3 × ".." to reach io\, then "reports")
     * }</pre>
     *
     * <p>The number of {@code ..} segments equals the distance from
     * {@code base} to the nearest common ancestor with {@code target}.</p>
     */
    private void demonstrateRelativizeWithDeepNesting() {
        log.info("--- Case 6: Deep nesting ---");

        Path base   = DIR_ARCHIVE_Q1;       // io\archive\2024\q1  (depth 4)
        Path target = DIR_REPORTS;          // io\reports           (depth 1 from io)
        Path result = base.relativize(target);

        log.info("  base         : {}", base);
        log.info("  target       : {}", target);
        log.info("  result       : {}", result);
        // result → ..\..\..\reports

        log.info("  segment count: {}", result.getNameCount());
        // "..","..","..","reports" → 4 segments

        for (int i = 0; i < result.getNameCount(); i++) {
            log.info("    segment[{}] = {}", i, result.getName(i));
        }
    }

    // -----------------------------------------------------------------------
    // Case 7 - Two purely relative paths
    // -----------------------------------------------------------------------

    /**
     * <h3>Case 7 - Both Paths Are Relative</h3>
     *
     * <p>{@code relativize} works equally well with <em>relative</em> paths
     * as long as <em>both</em> are relative (no absolute root component).</p>
     *
     * <pre>{@code
     *   base   : logs\app
     *   target : logs\archive\2024
     *   result : ..\archive\2024
     * }</pre>
     *
     * <p>This is useful when constructing portable, OS-agnostic relative
     * references without anchoring to a specific drive or mount point.</p>
     */
    private void demonstrateRelativizeTwoRelativePaths() {
        log.info("--- Case 7: Both paths are relative ---");

        Path base   = Paths.get("logs", "app");
        Path target = Paths.get("logs", "archive", "2024");
        Path result = base.relativize(target);

        log.info("  base   : {}", base);
        log.info("  target : {}", target);
        log.info("  result : {}", result);
        // result → ..\archive\2024

        log.info("  base is absolute  : {}", base.isAbsolute());
        log.info("  target is absolute: {}", target.isAbsolute());
    }

    // -----------------------------------------------------------------------
    // Case 8 - Relative paths with only up-navigation
    // -----------------------------------------------------------------------

    /**
     * <h3>Case 8 - Relative Path: Target Is Above Base</h3>
     *
     * <p>Even with relative paths, if the target is "above" the base (i.e.
     * fewer components on the shared leading path), the result consists
     * exclusively of {@code ..} segments.</p>
     *
     * <pre>{@code
     *   base   : reports\monthly\january
     *   target : reports
     *   result : ..\..
     * }</pre>
     *
     * <p>This mirrors the same logic as Case 3 but without an absolute root.</p>
     */
    private void demonstrateRelativizeRelativeWithGoingUp() {
        log.info("--- Case 8: Relative path going up only ---");

        Path base   = Paths.get("reports", "monthly", "january");
        Path target = Paths.get("reports");
        Path result = base.relativize(target);

        log.info("  base   : {}", base);
        log.info("  target : {}", target);
        log.info("  result : {}", result);
        // result → ..\..

        log.info("  name count: {}", result.getNameCount());
    }

    // -----------------------------------------------------------------------
    // Case 9 - Round-trip verification: relativize + resolve
    // -----------------------------------------------------------------------

    /**
     * <h3>Case 9 - Round-Trip Verification: {@code relativize} + {@code resolve}</h3>
     *
     * <p>The JDK contract guarantees:</p>
     * <pre>
     *   base.resolve( base.relativize(target) )
     *     .normalize()
     *     .equals( target.normalize() )
     * </pre>
     *
     * <p>This method systematically verifies that contract for several
     * different (base, target) pairs, and logs whether each round-trip
     * succeeds. Calling {@link Path#normalize()} is important because the
     * resolved path may contain redundant {@code ..} segments before
     * normalization.</p>
     *
     * <pre>{@code
     *   base   : C:\tests\java\io\data\csv
     *   target : C:\tests\java\io\archive\2024\q1
     *   rel    : ..\..\archive\2024\q1
     *   resolved (before normalize) : C:\tests\java\io\data\csv\..\..\archive\2024\q1
     *   resolved (after  normalize) : C:\tests\java\io\archive\2024\q1    ← equals target ✓
     * }</pre>
     */
    private void demonstrateRoundTripWithResolve() {
        log.info("--- Case 9: Round-trip (relativize + resolve) ---");

        record TestPair(Path base, Path target) {}

        TestPair[] pairs = {
                new TestPair(DIR_DATA_CSV,    DIR_ARCHIVE_Q1),
                new TestPair(WORKING_ROOT,    DIR_DATA_JSON),
                new TestPair(DIR_ARCHIVE_Q1,  DIR_REPORTS),
                new TestPair(DIR_DOCS,        DIR_DOCS),        // same path → empty
        };

        for (TestPair pair : pairs) {
            Path relative   = pair.base().relativize(pair.target());
            Path resolved   = pair.base().resolve(relative);
            Path normalized = resolved.normalize();
            boolean matches = normalized.equals(pair.target().normalize());

            log.info("  base     : {}", pair.base());
            log.info("  target   : {}", pair.target());
            log.info("  relative : {}", relative);
            log.info("  resolved : {}", resolved);
            log.info("  normalized: {} - round-trip OK: {}", normalized, matches);
            log.info("  ---");
        }
    }

    // -----------------------------------------------------------------------
    // Case 10 - Relativize paths that end with file names
    // -----------------------------------------------------------------------

    /**
     * <h3>Case 10 - Paths Including File Names</h3>
     *
     * <p>{@link Path#relativize(Path)} is <em>file-system agnostic</em>: it
     * does not distinguish between paths that represent directories and those
     * that represent files. The last component is treated as just another
     * name segment.</p>
     *
     * <p>This case models a common real-world scenario: computing a relative
     * path from one file to another, as needed when writing HTML {@code href}
     * or CSS {@code url()} references.</p>
     *
     * <pre>{@code
     *   base   : C:\tests\java\io\docs\index.html
     *   target : C:\tests\java\io\reports\summary.pdf
     *   result : ..\reports\summary.pdf
     * }</pre>
     *
     * <p><strong>Caveat:</strong> if {@code base} is a <em>file</em> (not a
     * directory), the caller must pass the <em>parent</em> of the base file
     * to get the intended relative path from that file's <em>containing
     * directory</em>. See example B below.</p>
     */
    private void demonstrateRelativizeWithFileNames() {
        log.info("--- Case 10: Paths including file names ---");

        Path baseFile   = DIR_DOCS.resolve("index.html");
        Path targetFile = DIR_REPORTS.resolve("summary.pdf");

        // Example A - relativize file-to-file (raw, without considering parent)
        Path resultA = baseFile.relativize(targetFile);
        log.info("  [A] base (file)   : {}", baseFile);
        log.info("  [A] target (file) : {}", targetFile);
        log.info("  [A] result        : {}", resultA);
        // → ..\reports\summary.pdf  (treats index.html as another directory segment)

        log.info("  ---");

        // Example B - correct approach: relativize from the PARENT of base-file
        Path baseParent = baseFile.getParent();   // C:\tests\java\io\docs
        Path resultB    = baseParent.relativize(targetFile);
        log.info("  [B] base (parent of file) : {}", baseParent);
        log.info("  [B] target (file)         : {}", targetFile);
        log.info("  [B] result                : {}", resultB);
        // → ..\reports\summary.pdf  (now semantically correct for an href)
    }

    // -----------------------------------------------------------------------
    // Case 11 - IllegalArgumentException: mixing absolute and relative
    // -----------------------------------------------------------------------

    /**
     * <h3>Case 11 - {@link IllegalArgumentException}: Mixing Absolute and Relative</h3>
     *
     * <p>The JDK specification mandates that both paths be of the same
     * <em>kind</em> (both absolute or both relative). Passing one of each
     * immediately throws {@link IllegalArgumentException} with no path
     * resolution performed.</p>
     *
     * <p>Two sub-cases are demonstrated:</p>
     * <ol>
     *   <li><strong>Absolute base + relative target</strong></li>
     *   <li><strong>Relative base + absolute target</strong></li>
     * </ol>
     *
     * <pre>{@code
     *   // Sub-case A
     *   base   : C:\tests\java\io         ← absolute
     *   target : reports\summary.pdf      ← relative
     *   → throws IllegalArgumentException
     *
     *   // Sub-case B
     *   base   : data\csv                 ← relative
     *   target : C:\tests\java\io\reports ← absolute
     *   → throws IllegalArgumentException
     * }</pre>
     *
     * <p><strong>Best practice:</strong> always validate
     * {@link Path#isAbsolute()} on both paths before calling
     * {@code relativize} in production code.</p>
     */
    private void demonstrateIllegalMixAbsoluteRelative() {
        log.info("--- Case 11: IllegalArgumentException - mixed absolute/relative ---");

        // Sub-case A: absolute base, relative target
        Path absoluteBase   = WORKING_ROOT;
        Path relativeTarget = Paths.get("reports", "summary.pdf");
        log.info("  [A] base   (absolute): {}", absoluteBase);
        log.info("  [A] target (relative): {}", relativeTarget);

        try {
            Path result = absoluteBase.relativize(relativeTarget);
            log.info("  [A] result (unexpected): {}", result);
        } catch (IllegalArgumentException ex) {
            log.warn("  [A] Caught expected IllegalArgumentException: {}", ex.getMessage());
        }

        log.info("  ---");

        // Sub-case B: relative base, absolute target
        Path relativeBase   = Paths.get("data", "csv");
        Path absoluteTarget = DIR_REPORTS;
        log.info("  [B] base   (relative): {}", relativeBase);
        log.info("  [B] target (absolute): {}", absoluteTarget);

        try {
            Path result = relativeBase.relativize(absoluteTarget);
            log.info("  [B] result (unexpected): {}", result);
        } catch (IllegalArgumentException ex) {
            log.warn("  [B] Caught expected IllegalArgumentException: {}", ex.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Case 12 - IllegalArgumentException: different Windows roots (C:\ vs D:\)
    // -----------------------------------------------------------------------

    /**
     * <h3>Case 12 - {@link IllegalArgumentException}: Different Windows Roots</h3>
     *
     * <p>On Windows, two absolute paths that start from <em>different drive
     * letters</em> (e.g. {@code C:\} and {@code D:\}) share no common root
     * and therefore cannot be relativized against each other. The JDK
     * implementation detects this and throws {@link IllegalArgumentException}.</p>
     *
     * <pre>{@code
     *   base   : C:\tests\java\io\reports   ← root C:\
     *   target : D:\projects\output         ← root D:\
     *   → throws IllegalArgumentException
     * }</pre>
     *
     * <p><strong>Detection tip:</strong> compare
     * {@link Path#getRoot()} on both paths before calling {@code relativize}
     * when handling user-supplied paths on Windows.</p>
     *
     * <pre>{@code
     *   if (!Objects.equals(base.getRoot(), target.getRoot())) {
     *       throw new IllegalStateException("Cannot relativize across different roots");
     *   }
     * }</pre>
     */
    private void demonstrateIllegalDifferentRoots() {
        log.info("--- Case 12: IllegalArgumentException - different Windows roots ---");

        Path baseOnC    = DIR_REPORTS;                          // C:\tests\java\io\reports
        Path targetOnD  = Paths.get("D:\\projects\\output");   // D:\projects\output

        log.info("  base   : {} (root: {})", baseOnC,   baseOnC.getRoot());
        log.info("  target : {} (root: {})", targetOnD, targetOnD.getRoot());

        try {
            Path result = baseOnC.relativize(targetOnD);
            log.info("  result (unexpected): {}", result);
        } catch (IllegalArgumentException ex) {
            log.warn("  Caught expected IllegalArgumentException - different roots: {}",
                    ex.getMessage());
        }

        // Defensive guard pattern
        boolean sameRoot = java.util.Objects.equals(baseOnC.getRoot(), targetOnD.getRoot());
        log.info("  Defensive check - same root: {}", sameRoot);
        if (!sameRoot) {
            log.info("  → Skipping relativize: roots differ ({} vs {})",
                    baseOnC.getRoot(), targetOnD.getRoot());
        }
    }

    // -----------------------------------------------------------------------
    // Case 13 - Normalization before relativize (redundant segments)
    // -----------------------------------------------------------------------

    /**
     * <h3>Case 13 - Normalize Before Relativizing (Redundant Segments)</h3>
     *
     * <p>{@code relativize} works on paths <em>as supplied</em> - it does
     * <strong>not</strong> internally normalize them. Paths containing
     * redundant {@code .} (current-dir) or {@code ..} (parent-dir) segments
     * will produce unexpected results unless normalized first.</p>
     *
     * <p>Three sub-cases are demonstrated:</p>
     * <ol>
     *   <li><strong>No normalize</strong> - raw paths with {@code ..}
     *       segments yield a longer, harder-to-read result.</li>
     *   <li><strong>With normalize</strong> - redundant segments are
     *       collapsed first, producing the clean expected result.</li>
     *   <li><strong>Dot (current-directory) segment</strong> - a {@code .}
     *       inside a path must be normalized before relativizing.</li>
     * </ol>
     *
     * <pre>{@code
     *   // Without normalize
     *   base   : C:\tests\java\io\data\..\reports
     *   target : C:\tests\java\io\docs
     *   result : ..\docs   ← logically correct but path contains ".."
     *
     *   // With normalize
     *   base   : C:\tests\java\io\reports    (normalized from above)
     *   target : C:\tests\java\io\docs
     *   result : ..\docs   ← clean and canonical
     * }</pre>
     *
     * <p><strong>Best practice:</strong> always call {@link Path#normalize()}
     * on both paths before passing them to {@code relativize}.</p>
     */
    private void demonstrateNormalizationBeforeRelativize() {
        log.info("--- Case 13: Normalization before relativize ---");

        // Sub-case A: base has a redundant ".." segment (same logical location as DIR_REPORTS)
        Path baseRaw      = WORKING_ROOT.resolve("data").resolve("..").resolve("reports");
        Path targetClean  = DIR_DOCS;

        log.info("  [A] base (raw)    : {}", baseRaw);
        log.info("  [A] target        : {}", targetClean);

        Path resultWithout = baseRaw.relativize(targetClean);
        log.info("  [A] result WITHOUT normalize : '{}'", resultWithout);
        // May include an extra ".." component due to raw ".." in base

        Path resultWith = baseRaw.normalize().relativize(targetClean.normalize());
        log.info("  [A] result WITH    normalize : '{}'", resultWith);
        // → ..\docs  (clean and correct)

        log.info("  ---");

        // Sub-case B: path with a "." (current-directory) segment
        Path baseWithDot = WORKING_ROOT.resolve("reports").resolve(".").resolve("subdir");
        Path targetB     = DIR_DOCS;

        log.info("  [B] base (with dot) : {}", baseWithDot);
        log.info("  [B] target          : {}", targetB);

        Path resultDotRaw  = baseWithDot.relativize(targetB);
        Path resultDotNorm = baseWithDot.normalize().relativize(targetB.normalize());

        log.info("  [B] result WITHOUT normalize : '{}'", resultDotRaw);
        log.info("  [B] result WITH    normalize : '{}'", resultDotNorm);
        // After normalize: base becomes io\reports\subdir  → result: ..\..\docs
    }
}