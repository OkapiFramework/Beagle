/*===========================================================================
  Copyright (C) 2014 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  This library is free software; you can redistribute it and/or modify it 
  under the terms of the GNU Lesser General Public License as published by 
  the Free Software Foundation; either version 2.1 of the License, or (at 
  your option) any later version.

  This library is distributed in the hope that it will be useful, but 
  WITHOUT ANY WARRANTY; without even the implied warranty of 
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
  General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License 
  along with this library; if not, write to the Free Software Foundation, 
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

  See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
===========================================================================*/

package net.sf.okapi.applications.beagle;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import net.sf.okapi.lib.dita.Harvester1;
import net.sf.okapi.lib.dita.ILog;
import net.sf.okapi.lib.dita.SingleFileMaker;

public class MainDialog extends JFrame implements ILog {

	private static final long serialVersionUID = 1L;
	private static final String SETTING_PATH = ".beagle";
	private static final String TAB_START = "<html><body><table width='160'>";
	private static final String TAB_END = "</table></body></html>";

	private JTabbedPane tabPane;
	private DocumentsPanel docPane;
	private JTextArea edLog;
	private Properties config;

	public MainDialog () {
		initComponents();
		showInfo();
		edLog.requestFocusInWindow();
		loadSettings();
	}

	private void initComponents () {
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Okapi Beagle - DITA Localization Utility");

		WindowListener exitListener = new WindowAdapter() {
			@Override
			public void windowClosing (WindowEvent e) {
				saveSettings();
			}
		};
		addWindowListener(exitListener);

		// === Menu
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// --- File
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);
		
		JMenuItem menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
		menuItem.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent event) {
				closeApplication();
			}
		});
		
		// --- Commands
		
		menu = new JMenu("Commands");
		menu.setMnemonic(KeyEvent.VK_C);
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Scan Directory", KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent event) {
				scanDirectory();
			}
		});
		
		menu.addSeparator();
		
		menuItem = new JMenuItem("Analyze References in a Directory...", KeyEvent.VK_A);
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent event) {
				analyzeReferences();
			}
		});
		
		menuItem = new JMenuItem("Group DITA Files into a Single XML...", KeyEvent.VK_G);
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent event) {
				groupFilesIntoSingleXML();
			}
		});

		// --- Commands
		
		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Help Topics", KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent event) {
				openHelp();
			}
		});
		
		menu.addSeparator();

		menuItem = new JMenuItem("About Beagle", KeyEvent.VK_A);
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent event) {
				showInfo();
			}
		});

		
		//=== Panels
		
		tabPane = new JTabbedPane();

		// Add the Log tab
		edLog = new JTextArea();
		JScrollPane jsp = new JScrollPane(edLog);
		edLog.setLineWrap(true);
		edLog.setWrapStyleWord(true);
		edLog.setFont(new Font("Courier New", 0, 14));
		tabPane.addTab(TAB_START+"Log"+TAB_END, jsp);
		
		// Add the Documents tab
		docPane = new DocumentsPanel();
		tabPane.addTab(TAB_START+"Scan Result"+TAB_END, docPane);
		
		add(tabPane);

		setPreferredSize(new Dimension(1000, 600));
		pack();
		
		// Center the dialog
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dim.width - getSize().width) / 2,
			(dim.height - getSize().height) / 2);
	}

	private void closeApplication () {
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	
	private void openHelp () {
		try {
			// Re-use or create the help file (needed because it's stored in a jar)
			File file = new File(System.getProperty("java.io.tmpdir"), "beagleHelp.html");
			if ( !file.exists() ) {
				try ( InputStream is = getClass().getResourceAsStream("help.html") ) {
					Files.copy(is, file.toPath());
					file.deleteOnExit();
				}
			}
			// Open the help
			java.awt.Desktop.getDesktop().browse(file.toURI());
		}
		catch (Throwable e) {
			log(e);
			showLog();
		}
	}
	
	@Override
	public void log (String text) {
		edLog.setText(edLog.getText()+text+"\n");
	}
	
	@Override
	public void showLog () {
		tabPane.setSelectedIndex(0);
	}
	
	@Override
	public void log (Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw); 
		e.printStackTrace(pw);
		log(sw.toString());
	}

	@Override
	public void clearLog () {
		edLog.setText("");
	}
	
	private void showInfo () {
		clearLog();
		log("-------------------------------------------------------------------------------");
		log("Okapi Beagle - DITA Localization Utility");
		log("Version: "+getClass().getPackage().getImplementationVersion());
		log("-------------------------------------------------------------------------------");
		Runtime rt = Runtime.getRuntime();
		rt.runFinalization();
		rt.gc();
        log("Java version: " + System.getProperty("java.version"));
        log(String.format("Platform: %s, %s, %s",
			System.getProperty("os.name"), //$NON-NLS-1$ 
			System.getProperty("os.arch"), //$NON-NLS-1$
			System.getProperty("os.version")));
		NumberFormat nf = NumberFormat.getInstance();
        log(String.format("Java VM memory: free=%s KB, total=%s KB",
			nf.format(rt.freeMemory()/1024),
			nf.format(rt.totalMemory()/1024)));
		log("-------------------------------------------------------------------------------");
		log("Press F1 for help.");
	}
	
	private void scanDirectory () {
		clearLog();
		log("===== Scan Directory");
		try {
			docPane.scan();
		}
		catch ( Throwable e ) {
    		log(e);
    		showLog();
		}
	}
	
	private void groupFilesIntoSingleXML () {
		clearLog();
		log("===== Group DITA Files into Single XML");
		
    	try {
    		GroupFilesInXMLDialog dlg = new GroupFilesInXMLDialog(this, true,
    			config.getProperty("ditaMapPath", ""),
    			config.getProperty("tagRenPath", ""),
    			config.getProperty("lbDefPath", ""));
    		dlg.setVisible(true);
    		if ( dlg.isCancelled() ) {
    			log("Cancelled by user.");
    			return;
    		}

    		File ditaMapFile = new File(dlg.getDitaMapPath());
    		config.setProperty("ditaMapPath", ditaMapFile.getAbsolutePath());
    		File tagRenFile = (dlg.getTagRenPath() == null ? null : new File(dlg.getTagRenPath()));
    		config.setProperty("tagRenPath", tagRenFile.getAbsolutePath());
    		File lbDefFile = (dlg.getLbDefPath() == null ? null : new File(dlg.getLbDefPath()));
    		config.setProperty("lbDefPath", lbDefFile.getAbsolutePath());
    		File outFile = new File(ditaMapFile.getAbsolutePath()+".single-out.xml");

    		try ( SingleFileMaker sfm = new SingleFileMaker(this) ) {
    			sfm.setParameters(ditaMapFile, tagRenFile, lbDefFile, outFile);
        		sfm.process();
    		}
			log("Done.");
    	}
    	catch ( Throwable e ) {
    		log(e);
    		showLog();
    	}
	}
	
	private void analyzeReferences () {
		clearLog();
		log("===== Reference Analysis");
		
    	try {
    		File rootDir;
    		// Select the folder to process
    		JFileChooser fc = new JFileChooser();
    		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
   			fc.setDialogTitle("Select the Directory to Analyze");
   			fc.setCurrentDirectory(new File(config.getProperty("defaultDir", "")));
    		int option = fc.showOpenDialog(this);
    		if ( option == JFileChooser.APPROVE_OPTION ) {
   				rootDir = fc.getSelectedFile();
   				config.setProperty("defaultDir", fc.getCurrentDirectory().getAbsolutePath());
   				log("Directory: "+rootDir+"\n");
    		}
    		else {
    			log("Cancelled by user.");
    			return;
    		}
    		
    		// Gather the ditamap files
    		File[] files = rootDir.listFiles(new FilenameFilter() {
    			@Override
    			public boolean accept (File dir, String name) {
    				return name.toLowerCase().endsWith(".ditamap");
    			}
    		});
    		if ( files.length == 0 ) {
    			log("No ditamap file found in this directory.");
    			return;
    		}
    		
    		// Harvest the references from each ditamap file
    		Harvester1 h = new Harvester1(this);
    		Map<String, String> list1 = new HashMap<>();
    		for ( File mapFile : files ) {
    			list1.putAll(h.gatherReferences(mapFile));
    		}
    		// Gather all the dita-related files
    		Map<String, String> list2 = h.gatherFiles(rootDir);
    		// Remove referenced files
    		for ( String path : list1.keySet() ) {
    			if ( list2.containsKey(path) ) {
    				list2.remove(path);
    			}
    		}
    		// Output the result
    		log("Referenced DITA and DITA MAP files:");
    		int count = 0;
    		for ( String path : list1.values() ) {
   				log(path); count++;
    		}
    		log("Count = "+count);
    		
    		log("\nDITA files not referenced in any other DITA files:");
    		count = 0;
    		for ( String path : list2.values() ) {
    			if ( path.endsWith(".dita") ) {
    				log(path); count++;
    			}
    		}
    		log("Count = "+count);
    		
    		log("\nDITA MAP files not referenced in any other files\n"
    			+ "(these files are either unused or top-level DITA MAP files):");
    		count = 0;
    		for ( String path : list2.values() ) {
    			if ( path.endsWith(".ditamap") ) {
    				log(path); count++;
    			}
    		}
    		log("Count = "+count);
			log("Done.");
    	}
    	catch ( Throwable e ) {
    		log(e);
    		showLog();
    	}
	}

	private void saveSettings () {
		OutputStream output = null;
		try {
			output = new FileOutputStream(SETTING_PATH);
			config.store(output, "Settings for Okapi Beagle (This file is re-created when leaving Beagle)");
		}
		catch ( Throwable e) {
			log(e);
		}
		finally {
			if ( output != null ) {
				try {
					output.close();
				}
				catch (IOException e) {
					log(e);
				}
			};
		}
	}
	
	private void loadSettings () {
		InputStream input = null;
		try {
			File file = new File(SETTING_PATH);
			if ( !file.exists() ) return;
			config = new Properties();
			config.load(new FileInputStream(file));
		}
		catch ( Throwable e) {
			log(e);
		}
		finally {
			if ( input != null ) {
				try {
					input.close();
				}
				catch (IOException e) {
					log(e);
				}
			};
		}
	}
	
	public static void start () {
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run () {
				new MainDialog().setVisible(true);
			}
		});
	}

}
