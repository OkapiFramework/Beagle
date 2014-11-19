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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GroupFilesInXMLDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final JTextField edDitaMap;
	private final JTextField edTagRen;
	private final JTextField edLbDef;
	
	private boolean cancelled = true;
	private String ditaMapPath;
	private String tagRenPath;
	private String lbDefPath;

	public GroupFilesInXMLDialog (JFrame owner,
		boolean modal,
		String ditaMapPath,
		String tagRenPath,
		String lbDefpPath)
	{
		super(owner, modal);
		
		setTitle("Group DITA Files into a Single XML");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		JPanel panel = new JPanel(new GridBagLayout());

		//--- DITA Map file
		GridBagConstraints c = new GridBagConstraints();
		final JLabel stDitaMap = new JLabel("DITA Map input file to process:");
		c.gridx = 0; c.gridy = 0; c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(stDitaMap, c);
		
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 1; c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		panel.add((edDitaMap = new JTextField()), c);
		if ( ditaMapPath != null ) edDitaMap.setText(ditaMapPath);

		final JButton btDitaMap = new JButton("...");
		btDitaMap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent event) {
				selectDitaMapFile();
			}
		});
		c = new GridBagConstraints();
		c.gridx = 1; c.gridy = 1;
		panel.add(btDitaMap, c);
		
		//--- Tag Renaming
		
		c = new GridBagConstraints();
		final JLabel stTagRen = new JLabel("Tag Renaming definition file (can be empty):");
		c.gridx = 0; c.gridy = 2; c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(stTagRen, c);
		
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 3; c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		panel.add((edTagRen = new JTextField()), c);
		if ( tagRenPath != null ) edTagRen.setText(tagRenPath);

		final JButton btTagRen = new JButton("...");
		btTagRen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent event) {
				selectTagRenFile();
			}
		});
		c = new GridBagConstraints();
		c.gridx = 1; c.gridy = 3;
		panel.add(btTagRen, c);

		//--- Line-break handling
		
		c = new GridBagConstraints();
		final JLabel stLbDef = new JLabel("Line-Breaks definition file (can be empty):");
		c.gridx = 0; c.gridy = 4; c.fill = GridBagConstraints.HORIZONTAL;
		//c.anchor = GridBagConstraints.PAGE_START;
		panel.add(stLbDef, c);
		
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 5; c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		panel.add((edLbDef = new JTextField()), c);
		if ( lbDefPath != null ) edLbDef.setText(lbDefPath);
		
		final JButton btLbDef = new JButton("...");
		btLbDef.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent event) {
				selectLbDefFile();
			}
		});
		c = new GridBagConstraints();
		c.gridx = 1; c.gridy = 5;
		panel.add(btLbDef, c);

		panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		add(panel, BorderLayout.PAGE_START);
		
		//--- Actions

		final JPanel pnlActions = new JPanel();
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 6;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add(pnlActions, c);

		final JButton btOk = new JButton("Execute");
		btOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent event) {
				doExecute();
			}
		});
		pnlActions.add(btOk);
		
		final JButton btCancel = new JButton("Cancel");
		btCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent event) {
				doCancel();
			}
		});
		pnlActions.add(btCancel);
		
		//--- Finalize the dialog box creation
		pack();
		Dimension dim = getPreferredSize(); 
		setMinimumSize(dim);
		setPreferredSize(new Dimension(800, dim.height));
		pack();
		
		setLocationRelativeTo(owner);
	}

	private void selectDitaMapFile () {
		File file = selectFile("DITA Map File", 
			new FileNameExtensionFilter("DITAMAP Files", "ditamap"),
			edDitaMap.getText());
		if ( file != null ) {
			edDitaMap.setText(file.getAbsolutePath());
		}
	}
	
	private void selectTagRenFile () {
		File file = selectFile("Tag Renaming Definition File", 
			new FileNameExtensionFilter("Tag-Renaming Definition Files", "txt"),
			edTagRen.getText());
		if ( file != null ) {
			edTagRen.setText(file.getAbsolutePath());
		}
	}
	
	private void selectLbDefFile () {
		File file = selectFile("Line-Breaks Definition File", 
			new FileNameExtensionFilter("Line-Breaks Definition Files", "txt"),
			edLbDef.getText());
		if ( file != null ) {
			edLbDef.setText(file.getAbsolutePath());
		}
	}
	
	private File selectFile (String title,
		FileNameExtensionFilter extFilter,
		String currentFile)
	{
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY); 
		fc.setDialogTitle(title);
    	fc.addChoosableFileFilter(extFilter);
		fc.setFileFilter(extFilter);
		if ( !currentFile.isEmpty() ) {
			File file = new File(currentFile);
			//fc.setCurrentDirectory(file.getParentFile());
			fc.ensureFileIsVisible(file);
			fc.setSelectedFile(file);
		}
		int option = fc.showOpenDialog(this);
		if ( option == JFileChooser.APPROVE_OPTION ) {
			return fc.getSelectedFile();
		}
		return null;
	}

	private void doExecute () {
		// DITA Map file
		String tmp = edDitaMap.getText().trim();
		if ( tmp.isEmpty() ) {
			
			return;
		}
		ditaMapPath = tmp;

		// Tag Renaming file
		tagRenPath = edTagRen.getText().trim();
		// Line-Breaks file
		lbDefPath = edLbDef.getText().trim();
		
		cancelled = false;
	}
	
	private void doCancel () {
		
	}
	
	public boolean isCancelled () {
		return cancelled;
	}

	public String getDitaMapPath () {
		return ditaMapPath;
	}
	
	public String getTagRenPath () {
		return tagRenPath;
	}
	
	public String getLbDefPath () {
		return lbDefPath;
	}

}
