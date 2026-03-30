package com.desabisc.guide.java.io.mcompare;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/**
 * <h2>Files.mismatch() – Comprehensive Edge-Case Guide</h2>
 *
 * <p>{@link Files#mismatch(Path, Path)} was introduced in <strong>Java 12</strong>
 * (JEP 340). It compares the <em>byte-level content</em> of two files and returns
 * the position (0-based byte index) of the first byte where they differ.</p>
 *
 * <h3>Return-value contract</h3>
 * <table border="1" summary="Return value semantics">
 *   <tr><th>Scenario</th><th>Return value</th></tr>
 *   <tr><td>Files are byte-for-byte identical (including same size)</td>
 *       <td>{@code -1L}</td></tr>
 *   <tr><td>Files differ at byte index N</td>
 *       <td>{@code N} (0-based)</td></tr>
 *   <tr><td>Files share a common prefix but one is shorter (the shorter one is
 *           a strict prefix of the longer)</td>
 *       <td>size of the shorter file (= first "extra" byte position)</td></tr>
 * </table>
 *
 * <h3>Performance notes</h3>
 * <ul>
 *   <li>The JDK implementation reads both files using buffered channels, so it
 *       is efficient for large files.</li>
 *   <li>If the files differ very early (e.g., first byte), the method returns
 *       almost immediately without reading the rest.</li>
 *   <li>It is <strong>not</strong> a hashing approach: two large identical files
 *       require reading every byte from both files to confirm equality.</li>
 * </ul>
 *
 * <h3>Edge cases covered in this class</h3>
 * <ol>
 *   <li>Identical content → returns {@code -1}</li>
 *   <li>Same file compared to itself → returns {@code -1}</li>
 *   <li>Difference at the very first byte (byte 0)</li>
 *   <li>Difference in the middle of the file</li>
 *   <li>Difference at the very last byte</li>
 *   <li>Shorter file is a prefix of the longer (size mismatch only)</li>
 *   <li>Longer file is a superset (extra bytes at the end)</li>
 *   <li>Both files are empty → returns {@code -1}</li>
 *   <li>One file empty, other non-empty → returns {@code 0}</li>
 *   <li>Files identical in content but with different line endings (LF vs CRLF)</li>
 *   <li>Files identical in content but with leading/trailing whitespace difference</li>
 *   <li>Binary files (non-text byte sequences)</li>
 *   <li>Large file comparison (content equal)</li>
 * </ol>
 *
 * <h3>Working directory</h3>
 * <pre>{@code C:\examples\java\io\compareeg\mismatch\}</pre>
 *
 * @author  desabisc
 * @version 1.0
 * @since   JDK 12  ({@link Files#mismatch(Path, Path)})
 * @see     Files#mismatch(Path, Path)
 * @see     Files#isSameFile(Path, Path)
 */
@Slf4j
public class MismatchExamples {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    /** Root working directory for all examples in this class. */
    private static final Path BASE_DIR =
            Paths.get("C:\\examples\\java\\io\\compareeg\\mismatch");

    /**
     * Sentinel value returned by {@link Files#mismatch} when files are identical.
     * Declared as a named constant for clarity in log messages.
     */
    private static final long FILES_IDENTICAL = -1L;

    /** Size (in bytes) of the large file created for the large-file example. */
    private static final int LARGE_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    // -----------------------------------------------------------------------
    // Entry point
    // -----------------------------------------------------------------------

    /**
     * Orchestrates every example in sequence.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        MismatchExamples runner = new MismatchExamples();

        try {
            runner.setUp();
        } catch (IOException e) {
            log.error("setUp() failed – cannot continue.", e);
            return;
        }

        runner.example01_identicalContent();
        runner.example02_sameFileObject();
        runner.example03_differenceAtFirstByte();
        runner.example04_differenceInMiddle();
        runner.example05_differenceAtLastByte();
        runner.example06_shorterIsPrefixOfLonger();
        runner.example07_longerHasExtraBytes();
        runner.example08_bothEmpty();
        runner.example09_oneEmptyOneNonEmpty();
        runner.example10_lineEndingDifference();
        runner.example11_whitespaceVariation();
        runner.example12_binaryFiles();
        runner.example13_largeFilesIdentical();
    }

    // -----------------------------------------------------------------------
    // setUp
    // -----------------------------------------------------------------------

    /**
     * Creates the folder structure and seed files required by every example.
     *
     * <p>This method is <em>idempotent</em>: running it twice produces the
     * same outcome as running it once. Existing files are left unchanged.</p>
     *
     * <h3>Files created</h3>
     * <pre>{@code
     * C:\examples\java\io\compareeg\mismatch\
     *   identical_a.txt       – "Hello, NIO.2 World!"
     *   identical_b.txt       – "Hello, NIO.2 World!"  (byte-for-byte copy)
     *   diff_first_a.txt      – "Aello"
     *   diff_first_b.txt      – "Bello"
     *   diff_middle_a.txt     – "Hello World"
     *   diff_middle_b.txt     – "Hello Earth"
     *   diff_last_a.txt       – "Hello!"
     *   diff_last_b.txt       – "HelloX"
     *   prefix.txt            – "Hello"
     *   prefix_extended.txt   – "Hello World"
     *   empty_a.txt           – (0 bytes)
     *   empty_b.txt           – (0 bytes)
     *   empty.txt             – (0 bytes)
     *   non_empty.txt         – "data"
     *   lf_file.txt           – "line1\nline2"     (Unix LF)
     *   crlf_file.txt         – "line1\r\nline2"   (Windows CRLF)
     *   spaces_a.txt          – "  hello  "
     *   spaces_b.txt          – "hello"
     *   binary_a.bin          – bytes [0x00, 0x01, 0x02, 0xFF]
     *   binary_b.bin          – bytes [0x00, 0x01, 0xFE, 0xFF]
     *   large_a.bin           – 5 MB of 0xAB bytes
     *   large_b.bin           – 5 MB of 0xAB bytes  (identical to large_a)
     * }</pre>
     *
     * @throws IOException if any directory or file cannot be created or written
     */
    private void setUp() throws IOException {
        log.info("=== setUp() – preparing working directory ===");

        Files.createDirectories(BASE_DIR);
        log.info("Working directory ready: {}", BASE_DIR);

        // Example 01 – identical content
        createUtf8IfAbsent("identical_a.txt", "Hello, NIO.2 World!");
        createUtf8IfAbsent("identical_b.txt", "Hello, NIO.2 World!");

        // Example 03 – difference at first byte
        createUtf8IfAbsent("diff_first_a.txt", "Aello");
        createUtf8IfAbsent("diff_first_b.txt", "Bello");

        // Example 04 – difference in the middle
        createUtf8IfAbsent("diff_middle_a.txt", "Hello World");
        createUtf8IfAbsent("diff_middle_b.txt", "Hello Earth");

        // Example 05 – difference at the last byte
        createUtf8IfAbsent("diff_last_a.txt", "Hello!");
        createUtf8IfAbsent("diff_last_b.txt", "HelloX");

        // Example 06 – prefix relationship
        createUtf8IfAbsent("prefix.txt",          "Hello");
        createUtf8IfAbsent("prefix_extended.txt", "Hello World");

        // Example 08 – both empty
        createUtf8IfAbsent("empty_a.txt", "");
        createUtf8IfAbsent("empty_b.txt", "");

        // Example 09 – one empty, one non-empty
        createUtf8IfAbsent("empty.txt",    "");
        createUtf8IfAbsent("non_empty.txt","data");

        // Example 10 – line-ending difference (LF vs CRLF)
        createBytesIfAbsent("lf_file.txt",   "line1\nline2".getBytes(StandardCharsets.UTF_8));
        createBytesIfAbsent("crlf_file.txt", "line1\r\nline2".getBytes(StandardCharsets.UTF_8));

        // Example 11 – whitespace variation
        createUtf8IfAbsent("spaces_a.txt", "  hello  ");
        createUtf8IfAbsent("spaces_b.txt", "hello");

        // Example 12 – binary files
        createBytesIfAbsent("binary_a.bin", new byte[]{0x00, 0x01, 0x02, (byte) 0xFF});
        createBytesIfAbsent("binary_b.bin", new byte[]{0x00, 0x01, (byte) 0xFE, (byte) 0xFF});

        // Example 13 – large files (5 MB identical content)
        createLargeFileIfAbsent("large_a.bin", LARGE_FILE_SIZE_BYTES, (byte) 0xAB);
        createLargeFileIfAbsent("large_b.bin", LARGE_FILE_SIZE_BYTES, (byte) 0xAB);

        log.info("setUp() complete.\n");
    }

    // -----------------------------------------------------------------------
    // Edge-case examples
    // -----------------------------------------------------------------------

    /**
     * <h3>Example 01 – Byte-for-byte identical files</h3>
     *
     * <p>When two files have exactly the same size <em>and</em> the same byte
     * sequence, {@code mismatch} returns {@code -1L} to signal that no mismatch
     * was found. The sentinel {@code -1} is used instead of throwing an exception
     * so that callers can handle equality without a try/catch.</p>
     *
     * <p>Note that the files are <em>different</em> file-system objects (different
     * inodes); only their content is equal. This is the key distinction between
     * {@code mismatch} (content comparison) and {@code isSameFile}
     * (identity comparison).</p>
     *
     * <p><strong>Expected result:</strong> {@code -1} (files are identical)</p>
     */
    private void example01_identicalContent() {
        log.info("--- Example 01: identical content ---");
        Path a = BASE_DIR.resolve("identical_a.txt");
        Path b = BASE_DIR.resolve("identical_b.txt");

        try {
            long pos = Files.mismatch(a, b);
            if (pos == FILES_IDENTICAL) {
                log.info("Files are byte-for-byte identical → mismatch() = {}", pos);
            } else {
                log.info("Unexpected mismatch at byte index {}", pos);
            }
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    /**
     * <h3>Example 02 – Same {@link Path} object (or same file path) compared to itself</h3>
     *
     * <p>Comparing a file to <em>itself</em> is a guaranteed no-mismatch scenario.
     * Both sides of the comparison point to the same byte stream, so every byte
     * read from the left side equals the corresponding byte read from the right
     * side. The return value is {@code -1}.</p>
     *
     * <p>The JDK does <strong>not</strong> short-circuit here the way
     * {@code isSameFile} does (it still reads the file). If you want to avoid
     * I/O, call {@code isSameFile} first and skip {@code mismatch} when it
     * returns {@code true}.</p>
     *
     * <p><strong>Expected result:</strong> {@code -1}</p>
     */
    private void example02_sameFileObject() {
        log.info("--- Example 02: same file compared to itself ---");
        Path path = BASE_DIR.resolve("identical_a.txt");

        try {
            long pos = Files.mismatch(path, path);
            log.info("mismatch(file, file) → {} (identical, as expected)", pos);
            // → -1
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    /**
     * <h3>Example 03 – Difference at byte index 0 (first byte)</h3>
     *
     * <p>Files {@code "Aello"} and {@code "Bello"} differ at their very first byte:
     * {@code 'A'} (0x41) vs {@code 'B'} (0x42). The method detects this immediately
     * and returns {@code 0} – the 0-based index of the first differing byte.</p>
     *
     * <p>This is the fastest possible mismatch detection: only one byte per file
     * needs to be read before the result is known.</p>
     *
     * <p><strong>Expected result:</strong> {@code 0}</p>
     */
    private void example03_differenceAtFirstByte() {
        log.info("--- Example 03: difference at byte 0 (first byte) ---");
        Path a = BASE_DIR.resolve("diff_first_a.txt");  // "Aello"
        Path b = BASE_DIR.resolve("diff_first_b.txt");  // "Bello"

        try {
            long pos = Files.mismatch(a, b);
            log.info("'Aello' vs 'Bello' → first mismatch at byte index: {}", pos);
            // → 0  (0x41 'A' ≠ 0x42 'B')
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    /**
     * <h3>Example 04 – Difference in the middle of the file</h3>
     *
     * <p>Files {@code "Hello World"} (11 bytes) and {@code "Hello Earth"} (11
     * bytes) share the prefix {@code "Hello "} (6 bytes) and then diverge.
     * The first differing byte is at index 6: {@code 'W'} (0x57) vs
     * {@code 'E'} (0x45).</p>
     *
     * <p>This example illustrates that the method reads bytes in order from
     * the beginning and returns as soon as a difference is found – it does not
     * scan from both ends.</p>
     *
     * <p><strong>Expected result:</strong> {@code 6}</p>
     */
    private void example04_differenceInMiddle() {
        log.info("--- Example 04: difference in the middle ---");
        Path a = BASE_DIR.resolve("diff_middle_a.txt");  // "Hello World"
        Path b = BASE_DIR.resolve("diff_middle_b.txt");  // "Hello Earth"

        try {
            long pos = Files.mismatch(a, b);
            //   H  e  l  l  o  ' ' W/E ...
            //   0  1  2  3  4   5  6
            log.info("'Hello World' vs 'Hello Earth' → first mismatch at byte index: {}", pos);
            // → 6
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    /**
     * <h3>Example 05 – Difference at the very last byte</h3>
     *
     * <p>Files {@code "Hello!"} and {@code "HelloX"} are 6 bytes each. They share
     * 5 identical bytes ({@code "Hello"}) and differ only at byte index 5:
     * {@code '!'} (0x21) vs {@code 'X'} (0x58).</p>
     *
     * <p>This is the worst-case scenario for performance: the method must read
     * every byte of both files before finding the difference. For large files
     * that are almost identical, consider hashing approaches if performance
     * matters.</p>
     *
     * <p><strong>Expected result:</strong> {@code 5}</p>
     */
    private void example05_differenceAtLastByte() {
        log.info("--- Example 05: difference at the last byte ---");
        Path a = BASE_DIR.resolve("diff_last_a.txt");  // "Hello!"  (6 bytes)
        Path b = BASE_DIR.resolve("diff_last_b.txt");  // "HelloX"  (6 bytes)

        try {
            long pos = Files.mismatch(a, b);
            //   H  e  l  l  o  !/X
            //   0  1  2  3  4   5
            log.info("'Hello!' vs 'HelloX' → first mismatch at byte index: {}", pos);
            // → 5
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    /**
     * <h3>Example 06 – Shorter file is a strict prefix of the longer file</h3>
     *
     * <p>File {@code "Hello"} (5 bytes) is a byte-level prefix of
     * {@code "Hello World"} (11 bytes). They share the same 5 bytes, after which
     * the shorter file ends but the longer one continues.</p>
     *
     * <p>In this situation {@code mismatch} returns the <em>size of the shorter
     * file</em> (5), because that is the index of the first "extra" byte in the
     * longer file – the position where the streams first differ (one has data,
     * the other is exhausted).</p>
     *
     * <p><strong>Key insight:</strong> the return value equals the length of the
     * shorter file when the shorter file is a complete prefix of the longer one.
     * You can detect this case with:
     * {@code long shorterSize = Math.min(Files.size(a), Files.size(b));}</p>
     *
     * <p><strong>Expected result:</strong> {@code 5} (size of "Hello")</p>
     */
    private void example06_shorterIsPrefixOfLonger() {
        log.info("--- Example 06: shorter file is a prefix of the longer ---");
        Path shorter = BASE_DIR.resolve("prefix.txt");           // "Hello"       → 5 bytes
        Path longer  = BASE_DIR.resolve("prefix_extended.txt");  // "Hello World" → 11 bytes

        try {
            long pos = Files.mismatch(shorter, longer);
            log.info("'Hello' vs 'Hello World' → first mismatch at byte index: {}", pos);
            // → 5  (= size of the shorter file; it ends here, longer continues)
            log.info("Size of shorter file: {}", Files.size(shorter));
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    /**
     * <h3>Example 07 – Order of arguments matters when sizes differ</h3>
     *
     * <p>Using the same two files as Example 06 but with the arguments swapped,
     * we confirm that the return value is the same regardless of argument order.
     * The first extra byte in the longer file is still at index 5 whether the
     * longer file is the first or the second argument.</p>
     *
     * <p>This symmetry holds because {@code mismatch} is defined as
     * "the position of the first byte where the two files differ", which is
     * independent of argument order when the shorter file is a prefix.</p>
     *
     * <p><strong>Expected result:</strong> {@code 5} (same as Example 06)</p>
     */
    private void example07_longerHasExtraBytes() {
        log.info("--- Example 07: argument order swap (longer first) ---");
        Path shorter = BASE_DIR.resolve("prefix.txt");           // "Hello"
        Path longer  = BASE_DIR.resolve("prefix_extended.txt");  // "Hello World"

        try {
            long pos1 = Files.mismatch(shorter, longer);
            long pos2 = Files.mismatch(longer,  shorter);

            log.info("mismatch(shorter, longer) → {}", pos1); // → 5
            log.info("mismatch(longer, shorter) → {}", pos2); // → 5
            log.info("Result is symmetric: {}", (pos1 == pos2));
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    /**
     * <h3>Example 08 – Both files are empty</h3>
     *
     * <p>Two empty files (0 bytes each) are trivially identical: there are no
     * bytes to compare, so no mismatch can exist. The method returns {@code -1}.</p>
     *
     * <p>This is a boundary condition worth testing explicitly because some
     * naive implementations might handle the empty case differently (e.g.,
     * returning 0 instead of -1).</p>
     *
     * <p><strong>Expected result:</strong> {@code -1}</p>
     */
    private void example08_bothEmpty() {
        log.info("--- Example 08: both files are empty ---");
        Path a = BASE_DIR.resolve("empty_a.txt");
        Path b = BASE_DIR.resolve("empty_b.txt");

        try {
            long pos = Files.mismatch(a, b);
            log.info("mismatch(empty, empty) → {} (identical – both empty)", pos);
            // → -1
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    /**
     * <h3>Example 09 – One file empty, one non-empty</h3>
     *
     * <p>An empty file (0 bytes) compared to a non-empty file differs at the
     * very first byte: the empty file has no byte at index 0, while the non-empty
     * file does. The mismatch is reported at index {@code 0}.</p>
     *
     * <p>Symmetrically to Example 06, the size of the shorter file (0) equals
     * the return value (0), because the empty file is technically a prefix of
     * every non-empty file.</p>
     *
     * <p><strong>Expected result:</strong> {@code 0}</p>
     */
    private void example09_oneEmptyOneNonEmpty() {
        log.info("--- Example 09: one empty, one non-empty ---");
        Path empty    = BASE_DIR.resolve("empty.txt");     // 0 bytes
        Path nonEmpty = BASE_DIR.resolve("non_empty.txt"); // "data" → 4 bytes

        try {
            long pos = Files.mismatch(empty, nonEmpty);
            log.info("mismatch(empty, 'data') → {} (empty ends immediately at byte 0)", pos);
            // → 0
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    /**
     * <h3>Example 10 – Line-ending difference: Unix LF vs Windows CRLF</h3>
     *
     * <p>A common source of subtle file differences when exchanging files between
     * operating systems is the line-ending convention:</p>
     * <ul>
     *   <li>Unix / macOS: {@code LF} ({@code \n}, 0x0A) – 1 byte per line end</li>
     *   <li>Windows: {@code CRLF} ({@code \r\n}, 0x0D 0x0A) – 2 bytes per line end</li>
     * </ul>
     *
     * <p>Files {@code "line1\nline2"} (10 bytes) and {@code "line1\r\nline2"}
     * (11 bytes) look identical when opened in a text editor that normalises line
     * endings, but at the byte level they differ at index 5:
     * {@code 0x0A} ({@code \n}) vs {@code 0x0D} ({@code \r}).</p>
     *
     * <p>{@code mismatch} operates at the raw byte level and therefore catches
     * this difference precisely at the position of the first {@code \r} byte.</p>
     *
     * <p><strong>Expected result:</strong> {@code 5}</p>
     */
    private void example10_lineEndingDifference() {
        log.info("--- Example 10: LF vs CRLF line endings ---");
        Path lf   = BASE_DIR.resolve("lf_file.txt");    // "line1\nline2"   (10 bytes)
        Path crlf = BASE_DIR.resolve("crlf_file.txt");  // "line1\r\nline2" (11 bytes)

        try {
            long pos = Files.mismatch(lf, crlf);
            //   l  i  n  e  1  \n/\r ...
            //   0  1  2  3  4   5
            log.info("LF vs CRLF → first mismatch at byte index: {}", pos);
            // → 5  (\n 0x0A ≠ \r 0x0D)
            log.info("Size of LF file  : {}", Files.size(lf));
            log.info("Size of CRLF file: {}", Files.size(crlf));
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    /**
     * <h3>Example 11 – Whitespace variation (leading / trailing spaces)</h3>
     *
     * <p>Files {@code "  hello  "} (9 bytes) and {@code "hello"} (5 bytes) differ
     * at their very first byte: space {@code 0x20} vs {@code 'h'} (0x68). The
     * method reports the mismatch at index {@code 0}.</p>
     *
     * <p>This example highlights that {@code mismatch} is <em>not</em> a semantic
     * comparison – it does not trim whitespace or perform any normalisation. If you
     * need trim-aware comparison, pre-process the content before writing it to the
     * files (or compare strings in memory instead).</p>
     *
     * <p><strong>Expected result:</strong> {@code 0}</p>
     */
    private void example11_whitespaceVariation() {
        log.info("--- Example 11: whitespace variation (leading / trailing spaces) ---");
        Path withSpaces    = BASE_DIR.resolve("spaces_a.txt");  // "  hello  " (9 bytes)
        Path withoutSpaces = BASE_DIR.resolve("spaces_b.txt");  // "hello"     (5 bytes)

        try {
            long pos = Files.mismatch(withSpaces, withoutSpaces);
            log.info("'  hello  ' vs 'hello' → first mismatch at byte index: {}", pos);
            // → 0  (space 0x20 ≠ 'h' 0x68)
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    /**
     * <h3>Example 12 – Binary files (non-text byte sequences)</h3>
     *
     * <p>{@code mismatch} is completely agnostic about file encoding – it works
     * on raw bytes. Binary files (images, class files, serialised objects, etc.)
     * are compared byte-by-byte the same way as text files.</p>
     *
     * <p>File A contains bytes: {@code [0x00, 0x01, 0x02, 0xFF]}<br>
     * File B contains bytes: {@code [0x00, 0x01, 0xFE, 0xFF]}<br>
     * They agree on bytes 0 and 1, and first differ at byte 2:
     * {@code 0x02} vs {@code 0xFE}.</p>
     *
     * <p><strong>Expected result:</strong> {@code 2}</p>
     */
    private void example12_binaryFiles() {
        log.info("--- Example 12: binary files ---");
        Path binaryA = BASE_DIR.resolve("binary_a.bin");  // [0x00, 0x01, 0x02, 0xFF]
        Path binaryB = BASE_DIR.resolve("binary_b.bin");  // [0x00, 0x01, 0xFE, 0xFF]

        try {
            long pos = Files.mismatch(binaryA, binaryB);
            //   0x00  0x01  0x02/0xFE  0xFF
            //   idx0  idx1  idx2       idx3
            log.info("Binary [00 01 02 FF] vs [00 01 FE FF] → first mismatch at byte index: {}",
                    pos);
            // → 2

            // Log raw bytes for verification
            byte[] bytesA = Files.readAllBytes(binaryA);
            byte[] bytesB = Files.readAllBytes(binaryB);
            log.info("File A bytes: {}", bytesToHex(bytesA));
            log.info("File B bytes: {}", bytesToHex(bytesB));
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    /**
     * <h3>Example 13 – Large files (5 MB) with identical content</h3>
     *
     * <p>This example demonstrates that {@code mismatch} scales to large files.
     * Both files are 5 MB of the same byte value ({@code 0xAB}), so the method
     * must read all 5 × 1024 × 1024 bytes from each file before concluding that
     * they are identical and returning {@code -1}.</p>
     *
     * <p>Elapsed time is logged to illustrate the performance of the NIO.2
     * channel-based implementation against a non-trivial file size.</p>
     *
     * <p><strong>Expected result:</strong> {@code -1}</p>
     */
    private void example13_largeFilesIdentical() {
        log.info("--- Example 13: large files (5 MB) – identical content ---");
        Path largeA = BASE_DIR.resolve("large_a.bin");
        Path largeB = BASE_DIR.resolve("large_b.bin");

        try {
            log.info("File sizes: {} MB each",
                    LARGE_FILE_SIZE_BYTES / (1024 * 1024));

            long start = System.currentTimeMillis();
            long pos   = Files.mismatch(largeA, largeB);
            long elapsed = System.currentTimeMillis() - start;

            if (pos == FILES_IDENTICAL) {
                log.info("5 MB files are identical → mismatch() = {} | elapsed: {} ms",
                        pos, elapsed);
            } else {
                log.info("Unexpected mismatch at byte index {} | elapsed: {} ms",
                        pos, elapsed);
            }
        } catch (IOException e) {
            log.error("Unexpected IOException", e);
        }
        log.info("");
    }

    // -----------------------------------------------------------------------
    // Utility helpers
    // -----------------------------------------------------------------------

    /**
     * Creates a UTF-8 text file at {@code BASE_DIR/filename} with the given
     * {@code content} only if the file does not already exist.
     *
     * @param filename relative file name inside {@link #BASE_DIR}
     * @param content  the text to write (UTF-8 encoded)
     * @throws IOException if the file cannot be created or written
     */
    private void createUtf8IfAbsent(String filename, String content) throws IOException {
        Path path = BASE_DIR.resolve(filename);
        if (Files.notExists(path)) {
            Files.writeString(path, content, StandardCharsets.UTF_8);
            log.info("  Created: {} ({} byte(s))", path, content.getBytes(StandardCharsets.UTF_8).length);
        } else {
            log.info("  Already exists: {}", path);
        }
    }

    /**
     * Creates a binary file at {@code BASE_DIR/filename} containing the given
     * raw {@code bytes} only if the file does not already exist.
     *
     * @param filename relative file name inside {@link #BASE_DIR}
     * @param bytes    the raw byte content to write
     * @throws IOException if the file cannot be created or written
     */
    private void createBytesIfAbsent(String filename, byte[] bytes) throws IOException {
        Path path = BASE_DIR.resolve(filename);
        if (Files.notExists(path)) {
            Files.write(path, bytes, StandardOpenOption.CREATE_NEW);
            log.info("  Created: {} ({} byte(s))", path, bytes.length);
        } else {
            log.info("  Already exists: {}", path);
        }
    }

    /**
     * Creates a large binary file at {@code BASE_DIR/filename} filled with
     * {@code size} repetitions of {@code fillByte} only if the file does not
     * already exist.
     *
     * <p>The file is written in 64 KB chunks to avoid allocating the entire
     * content in a single in-memory byte array, keeping heap usage bounded.</p>
     *
     * @param filename the relative file name inside {@link #BASE_DIR}
     * @param size     total number of bytes to write
     * @param fillByte the byte value to repeat throughout the file
     * @throws IOException if the file cannot be created or written
     */
    private void createLargeFileIfAbsent(String filename, int size, byte fillByte)
            throws IOException {

        Path path = BASE_DIR.resolve(filename);
        if (Files.notExists(path)) {
            int chunkSize = 64 * 1024; // 64 KB chunks
            byte[] chunk  = new byte[chunkSize];
            Arrays.fill(chunk, fillByte);

            try (var out = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
                int remaining = size;
                while (remaining > 0) {
                    int toWrite = Math.min(chunkSize, remaining);
                    out.write(chunk, 0, toWrite);
                    remaining -= toWrite;
                }
            }
            log.info("  Created large file: {} ({} byte(s))", path, size);
        } else {
            log.info("  Already exists: {}", path);
        }
    }

    /**
     * Converts a byte array to a hexadecimal string for human-readable logging.
     *
     * <p>Example: {@code [0x00, 0xFF]} → {@code "00 FF"}</p>
     *
     * @param bytes the byte array to convert; must not be {@code null}
     * @return a space-separated uppercase hexadecimal string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}