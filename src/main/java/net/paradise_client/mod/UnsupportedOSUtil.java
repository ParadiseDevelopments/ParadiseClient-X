package net.paradise_client.mod;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import net.paradise_client.Constants;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * OS compatibility checker - requires Windows 11, macOS 12+, or Linux kernel 5.10+
 */
public class UnsupportedOSUtil {
  private static final AtomicBoolean checksPerformed = new AtomicBoolean(false);
  private static final int MIN_WINDOWS_BUILD = 22000;
  private static final int MIN_MACOS_VERSION = 12;
  private static final int MIN_LINUX_KERNEL_MAJOR = 5;
  private static final int MIN_LINUX_KERNEL_MINOR = 10;

  public static void performCheck() {
    if (checksPerformed.getAndSet(true)) {
      return;
    }

    try {
      Constants.LOGGER.info("Starting OS compatibility checks. Detected: {} {}",
        getProp("os.name"),
        getProp("os.version"));

      checkArchitecture();
      checkWindowsCompatibility();
      checkMacOSCompatibility();
      checkLinuxCompatibility();

      Constants.LOGGER.info("All OS compatibility checks passed.");
    } catch (UnsupportedOperationException e) {
      throw e;
    } catch (Exception e) {
      Constants.LOGGER.error("Unexpected error during compatibility check.", e);
      showError("An unexpected error occurred while checking system compatibility.\n\nError: " + e.getMessage(),
        "ParadiseClient – System Error");
      throw new UnsupportedOperationException("System compatibility check failed unexpectedly.", e);
    }
  }

  private static void checkArchitecture() {
    if (!getProp("os.name").toLowerCase().contains("windows")) {
      return;
    }

    String arch = getProp("os.arch").toLowerCase();
    if (!arch.contains("amd64") && !arch.contains("x86_64")) {
      Constants.LOGGER.error("32-bit Windows not supported.");
      showError(getAppPath() + " is not a valid Win32 application. (Requires 64-bit Windows.)",
        "ParadiseClient – System Error");
      System.exit(1);
    }
  }

  private static void checkWindowsCompatibility() {
    String os = getProp("os.name").toLowerCase();
    if (!os.contains("windows")) {
      return;
    }

    int build = getWindowsBuild();

    if (build >= MIN_WINDOWS_BUILD || os.contains("windows 11")) {
      Constants.LOGGER.info("Windows 11 and above confirmed (build: {})", build);
      return;
    }

    fail(getWindowsMessage(os), "ParadiseClient requires Windows 11 or higher");
  }

  private static void checkMacOSCompatibility() {
    if (!getProp("os.name").toLowerCase().contains("mac")) {
      return;
    }

    String ver = getProp("os.version");
    if (ver.isEmpty()) {
      return;
    }

    int major = Integer.parseInt(ver.split("\\.")[0]);
    if (major < MIN_MACOS_VERSION) {
      fail(String.format("You're running %s.\n\nParadiseClient requires macOS Monterey (12.0) or newer.\n\n" +
          "Please update your Mac's operating system.", getMacOSName(major, ver)),
        "ParadiseClient requires macOS Monterey or higher");
    }
    Constants.LOGGER.info("macOS version check passed: {}", ver);
  }

  private static void checkLinuxCompatibility() {
    if (!getProp("os.name").toLowerCase().contains("linux")) {
      return;
    }

    String ver = getProp("os.version");
    if (ver.isEmpty()) {
      return;
    }

    String[] parts = ver.split("[.\\-]");
    int major = Integer.parseInt(parts[0]);
    int minor = Integer.parseInt(parts[1]);

    if (major < MIN_LINUX_KERNEL_MAJOR || (major == MIN_LINUX_KERNEL_MAJOR && minor < MIN_LINUX_KERNEL_MINOR)) {
      fail(String.format("Your Linux kernel %d.%d is too old.\n\nParadiseClient requires kernel %d.%d+.\n\n" +
          "Update to Ubuntu 22.04+, Debian 11+, or Fedora 38+.",
        major,
        minor,
        MIN_LINUX_KERNEL_MAJOR,
        MIN_LINUX_KERNEL_MINOR), "ParadiseClient – Outdated Linux Kernel");
    }
    Constants.LOGGER.info("Linux kernel check passed: {}.{}", major, minor);
  }

  private static int getWindowsBuild() {
    try {
      Process p = Runtime.getRuntime()
        .exec("cmd /c reg query \"HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\" /v CurrentBuild");
      try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
        String line;
        while ((line = r.readLine()) != null) {
          if (line.contains("CurrentBuild")) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length > 2) {
              return Integer.parseInt(parts[parts.length - 1]);
            }
          }
        }
      }
    } catch (Exception e) {
      Constants.LOGGER.warn("Failed to query Windows build", e);
    }
    return 0;
  }

  private static String getMacOSName(int major, String full) {
    if (major == 11) {
      return "macOS Big Sur (11.x)";
    }
    if (major == 10) {
      if (full.startsWith("10.15")) {
        return "macOS Catalina (10.15)";
      }
      if (full.startsWith("10.14")) {
        return "macOS Mojave (10.14)";
      }
      if (full.startsWith("10.13")) {
        return "macOS High Sierra (10.13)";
      }
      return "macOS 10.x";
    }
    return "macOS " + major;
  }

  private static String getWindowsMessage(String os) {
    String ver = "unknown Windows";
    if (os.contains("xp")) {
      ver = "Windows XP";
    } else if (os.contains("vista")) {
      ver = "Windows Vista";
    } else if (os.contains("7")) {
      ver = "Windows 7";
    } else if (os.contains("8.1")) {
      ver = "Windows 8.1";
    } else if (os.contains("8")) {
      ver = "Windows 8";
    } else if (os.contains("windows 10")) {
      ver = "Windows 10";
    }

    if (!ver.equals("unknown Windows")) {
      return String.format("You're running %s.\n\nThis version is no longer supported by Microsoft " +
        "and cannot run ParadiseClient.\n\nUpgrade to Windows 11.", ver);
    }
    return String.format("Your Windows version is too old.\n\nParadiseClient requires Windows 11 (Build %d+).\n\n" +
      "Please upgrade your OS.", MIN_WINDOWS_BUILD);
  }

  private static void fail(String msg, String exception) {
    Constants.LOGGER.error("========================================");
    Constants.LOGGER.error("FATAL: UNSUPPORTED OS");
    Constants.LOGGER.error(msg.replaceAll("<[^>]*>", "").replace("\n", " "));
    Constants.LOGGER.error("========================================");
    showError(msg, "ParadiseClient – System Compatibility Failure");
    throw new UnsupportedOperationException(exception);
  }

  private static void showError(String msg, String title) {
    String os = getProp("os.name").toLowerCase();
    if (os.contains("windows")) {
      showWindowsError(msg, title);
    } else if (os.contains("mac")) {
      showMacError(msg, title);
    } else if (os.contains("linux")) {
      showLinuxError(msg, title);
    } else {
      Constants.LOGGER.error("ERROR: {}\n{}", title, msg);
    }
  }

  private static void showWarning(String msg, String title) {
    String os = getProp("os.name").toLowerCase();
    if (os.contains("windows")) {
      showWindowsWarning(msg, title);
    } else if (os.contains("mac")) {
      showMacWarning(msg, title);
    } else if (os.contains("linux")) {
      showLinuxWarning(msg, title);
    } else {
      Constants.LOGGER.warn("WARNING: {}\n{}", title, msg);
    }
  }

  private static void showWindowsError(String msg, String title) {
    try {
      User32.INSTANCE.MessageBoxA(null, msg, title, 0x10); // MB_ICONERROR
    } catch (Exception e) {
      Constants.LOGGER.error("Native dialog failed: {}\n{}", title, msg);
    }
  }

  private static void showWindowsWarning(String msg, String title) {
    try {
      User32.INSTANCE.MessageBoxA(null, msg, title, 0x30); // MB_ICONWARNING
    } catch (Exception e) {
      Constants.LOGGER.warn("Native dialog failed: {}\n{}", title, msg);
    }
  }

  private static void showMacError(String msg, String title) {
    try {
      String clean = msg.replaceAll("<[^>]*>", "");
      Process p = Runtime.getRuntime()
        .exec(new String[]{"osascript",
          "-e",
          String.format("display dialog \"%s\" with title \"%s\" with icon stop buttons {\"OK\"} default button \"OK\"",
            clean.replace("\\", "\\\\").replace("\"", "\\\""),
            title.replace("\\", "\\\\").replace("\"", "\\\""))
        });
      p.waitFor();
    } catch (Exception e) {
      Constants.LOGGER.error("Native dialog failed: {}\n{}", title, msg);
    }
  }

  private static void showMacWarning(String msg, String title) {
    try {
      String clean = msg.replaceAll("<[^>]*>", "");
      Process p = Runtime.getRuntime()
        .exec(new String[]{"osascript",
          "-e",
          String.format(
            "display dialog \"%s\" with title \"%s\" with icon caution buttons {\"OK\"} default button \"OK\"",
            clean.replace("\\", "\\\\").replace("\"", "\\\""),
            title.replace("\\", "\\\\").replace("\"", "\\\""))
        });
      p.waitFor();
    } catch (Exception e) {
      Constants.LOGGER.warn("Native dialog failed: {}\n{}", title, msg);
    }
  }

  private static void showLinuxError(String msg, String title) {
    try {
      String clean = msg.replaceAll("<[^>]*>", "");
      if (cmdExists("zenity")) {
        Runtime.getRuntime()
          .exec(new String[]{"zenity", "--error", "--title=" + title, "--text=" + clean, "--width=400"})
          .waitFor();
      } else if (cmdExists("kdialog")) {
        Runtime.getRuntime().exec(new String[]{"kdialog", "--error", clean, "--title", title}).waitFor();
      } else {
        Constants.LOGGER.error("No dialog tool found: {}\n{}", title, clean);
      }
    } catch (Exception e) {
      Constants.LOGGER.error("Native dialog failed: {}\n{}", title, msg);
    }
  }

  private static void showLinuxWarning(String msg, String title) {
    try {
      String clean = msg.replaceAll("<[^>]*>", "");
      if (cmdExists("zenity")) {
        Runtime.getRuntime()
          .exec(new String[]{"zenity", "--warning", "--title=" + title, "--text=" + clean, "--width=400"})
          .waitFor();
      } else if (cmdExists("kdialog")) {
        Runtime.getRuntime().exec(new String[]{"kdialog", "--sorry", clean, "--title", title}).waitFor();
      } else {
        Constants.LOGGER.warn("No dialog tool found: {}\n{}", title, clean);
      }
    } catch (Exception e) {
      Constants.LOGGER.warn("Native dialog failed: {}\n{}", title, msg);
    }
  }

  private static boolean cmdExists(String cmd) {
    try {
      return Runtime.getRuntime().exec(new String[]{"which", cmd}).waitFor() == 0;
    } catch (Exception e) {
      return false;
    }
  }

  private static String getProp(String key) {
    try {
      String val = System.getProperty(key);
      return val != null ? val : "";
    } catch (SecurityException e) {
      return "";
    }
  }

  private static String getAppPath() {
    try {
      String path = UnsupportedOSUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
      return new File(URLDecoder.decode(path, StandardCharsets.UTF_8)).getAbsolutePath();
    } catch (Exception e) {
      return "ParadiseClient";
    }
  }

  private interface User32 extends StdCallLibrary {
    User32 INSTANCE = Native.loadLibrary("user32", User32.class);

    int MessageBoxA(WinDef.HWND hWnd, String text, String caption, int type);
  }
}