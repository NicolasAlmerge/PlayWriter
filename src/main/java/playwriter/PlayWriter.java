package playwriter;


import static javax.swing.JOptionPane.DEFAULT_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.FileDialog;
import javax.swing.JFrame;


/**
 * Represents program entry point.
 */
public final class PlayWriter {
	private static final String WINDOW_TITLE = "PlayWriter Application";
	
	public static void main(String[] args) {
		// Display window
		FileDialog dialog = new FileDialog((JFrame) null, "Select Play File");
		dialog.setFilenameFilter((dir, fileName) -> {
			fileName = fileName.toLowerCase();
			return (
				fileName.endsWith(".docx") ||
				fileName.endsWith(".doc") ||
				fileName.endsWith(".play") ||
				fileName.endsWith(".txt")
			);
		});
	    dialog.setVisible(true);
	    
	    // Get input file, if exists
	    String inputFile = dialog.getFile();
	    if (inputFile == null) System.exit(0);
	    
	    // Compute output file name
		int index = inputFile.lastIndexOf('.');
		String outputFile;
		boolean isPlainText;

		if (index == -1) {
			outputFile = inputFile + ".pdf";
			isPlainText = true;
		} else {
			outputFile = inputFile.substring(0, index) + ".pdf";
			String extension = inputFile.substring(index+1);
			isPlainText = (extension.equals("txt") || extension.equals("play"));
		}
		
		// Get directory to have absolute paths
		String dir = dialog.getDirectory();
		
		// File parser
		FileParser fp = null;
		
		try {
			// Parse play
			fp = new FileParser(dir+inputFile, dir+outputFile, isPlainText);
			fp.parseAll();
			fp.output();
		} catch (Exception e) {
			// Show error message
			if (fp != null) fp.closePlayWithFailMessage();
			showMessageDialog(null, e.getMessage(), WINDOW_TITLE, ERROR_MESSAGE);
			return;
		}
		
		// Show confirmation message
		showMessageDialog(null, "Program successfully completed!", WINDOW_TITLE, DEFAULT_OPTION);
	}
	
	private PlayWriter() {}
}
