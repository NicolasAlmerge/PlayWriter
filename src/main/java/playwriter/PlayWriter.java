package playwriter;


import static javax.swing.JOptionPane.DEFAULT_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.FileDialog;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFrame;


/**
 * Represents program entry point.
 */
public final class PlayWriter {
	private static final String WINDOW_TITLE = "PlayWriter Application";
	
	public static void main(String[] args) {
		// Display window
		FileDialog dialog = new FileDialog((JFrame) null, "Select Play File");
		dialog.setFilenameFilter((dir, fileName) -> fileName.toLowerCase().endsWith(".play"));
	    dialog.setVisible(true);
	    
	    // Get input file, if exists
	    String inputFile = dialog.getFile();
	    if (inputFile == null) System.exit(0);
	    
	    // Compute output file name
		int index = inputFile.lastIndexOf('.');
		String outputFile = (index == -1)? inputFile + ".pdf": inputFile.substring(0, index) + ".pdf";
		
		// Get directory to have absolute paths
		String dir = dialog.getDirectory();
		
		// File parser
		FileParser fp = null;
		
		try {
			// Parse play
			fp = new FileParser(new FileReader(dir + inputFile), dir + outputFile);
			fp.parseAll();
			fp.output();
		} catch (IOException | IllegalArgumentException | PlayCompileTimeError e) {
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
