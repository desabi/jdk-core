# 🕰️ What Is the Unix Epoch?

The **Unix epoch** is defined as:

> **00:00:00 UTC on January 1, 1970**

It serves as the **starting point** for timekeeping in many operating systems and programming languages, especially those based on Unix (like Linux, macOS, Java, JavaScript, Python, etc.).

Using a fixed reference point simplifies time calculations, comparisons, and storage across systems.

---

# 📏 How Is Time Measured Since the Epoch?

Time is measured as the **number of seconds or milliseconds** that have elapsed since the Unix epoch. This value is known as a **timestamp**.

- **In Java**: time is stored as a `long` representing **milliseconds** since the epoch.
- **In Unix systems**: time is typically stored as **seconds** since the epoch.

### Examples:
- `0` = Jan 1, 1970, 00:00:00 UTC
- `86400` = Jan 2, 1970, 00:00:00 UTC (since 86400 seconds = 1 day)