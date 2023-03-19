package playwriter;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.FileDialog;
import javax.swing.JFrame;

/**
 * Represents the program's entry point.
 *
 * @author Nicolas Almerge
 * @since 1.0
 */
public final class PlayWriter {
  private static final String WINDOW_TITLE = "PlayWriter Application";

  /**
   * Private constructor.
   */
  private PlayWriter() {
  }

  /**
   * Main function.
   *
   * @param args Not used.
   */
  public static void main(String[] args) {
    // Display window
    FileDialog dialog = new FileDialog((JFrame) null, "Select Play File");
    dialog.setFilenameFilter((dir, f) -> {
      f = f.toLowerCase();
      return f.endsWith(".docx") || f.endsWith(".doc") || f.endsWith(".play") || f.endsWith(".txt");
    });
    dialog.setVisible(true);

    // Get input file, if exists
    String inputFile = dialog.getFile();
    if (inputFile == null) {
      System.exit(0);
    }

    // Compute output file name
    int index = inputFile.lastIndexOf('.');
    String outputFile;
    boolean isPlainText;

    if (index == -1) {
      outputFile = inputFile + ".pdf";
      isPlainText = true;
    } else {
      outputFile = inputFile.substring(0, index) + ".pdf";
      String extension = inputFile.substring(index + 1).toLowerCase();
      isPlainText = (extension.equals("txt") || extension.equals("play"));
    }

    // Get directory to have absolute paths
    String dir = dialog.getDirectory();

    // File parser
    FileParser fp = null;

    try {
      // Parse play
      fp = new FileParser(dir + inputFile, dir + outputFile, isPlainText);
      fp.parseAll();
      fp.output();
    } catch (Exception e) {
      // Show error message
      if (fp != null) {
        fp.closePlayWithFailMessage();
      }
      showMessageDialog(null, e.getMessage(), WINDOW_TITLE, ERROR_MESSAGE);
      return;
    }

    // Show confirmation message
    showMessageDialog(null, "Program successfully completed!", WINDOW_TITLE, INFORMATION_MESSAGE);
  }
}
