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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.stream.XMLStreamException;

import net.sf.okapi.lib.dita.Entries;
import net.sf.okapi.lib.dita.Entry;
import net.sf.okapi.lib.dita.Harvester;

public class DocumentsPanel extends JPanel {

	private final static long serialVersionUID = 1L;
	
	private final JTextField edRoot;
	private final JTree tree;
	private final DefaultMutableTreeNode rootNode;
	private final DefaultTreeModel treeModel;
	
	private Entries entries;
	
	public DocumentsPanel () {
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		JLabel stRoot = new JLabel("Root:");
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 0; c.gridy = 0;
		add(stRoot, c);
		
		edRoot = new JTextField();
		edRoot.setText(new File("").getAbsolutePath());
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.PAGE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.75;
		c.gridx = 1; c.gridy = 0;
		add(edRoot, c);
		
		JButton btRoot = new JButton("...");
		btRoot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent event) {
				setRoot();
			}
		});
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridx = 2; c.gridy = 0;
		add(btRoot, c);
		
		rootNode = new DefaultMutableTreeNode("ROOT");
		tree = new JTree(rootNode);
		c = new GridBagConstraints();
		JScrollPane jsp = new JScrollPane(tree); 
		c.gridx = 0; c.gridy = 1; c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0; c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = GridBagConstraints.REMAINDER;
		add(jsp, c);
		
		treeModel = (DefaultTreeModel)tree.getModel();
		
		tree.setRootVisible(false);
		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)tree.getCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);		
	}
	
	private void setRoot () {
		// Select the folder to process
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
			fc.setDialogTitle("Select the Directory to Analyze");
		int option = fc.showOpenDialog(this);
		if ( option == JFileChooser.APPROVE_OPTION ) {
			edRoot.setText(fc.getSelectedFile().getAbsolutePath());
		}
	}

	public void scan () throws IOException, XMLStreamException {
		Harvester h = new Harvester();
		entries = new Entries();
		
		rootNode.removeAllChildren();
		treeModel.reload();

		// Get the list of all .ditamap and .dita files in the root (recursively)
		File rootDir = new File(edRoot.getText());
		edRoot.setText(rootDir.getAbsolutePath());
		
		Map<String, String> files = h.gatherFiles(rootDir);
		for ( String key : files.keySet() ) {
			entries.add(new Entry(key, files.get(key)));
		}
		// Get the direct references for each file
		for ( Entry entry : entries ) {
			Map<String, String> refs = h.gatherDirectReferences(new File(entry.getPath()));
			for ( String key : refs.keySet() ) {
				entry.add(new Entry(key, refs.get(key)));
			}
		}
		// Sort the list
		entries.sort();
		
		// Now update the tree
		rootNode.removeAllChildren();
		int len = edRoot.getText().length();
		for ( Entry entry : entries ) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(entry.getPath().substring(len));
			rootNode.add(node);
			for ( String key : entry ) {
				String refPath = entry.getPath(key);
				node.add(new DefaultMutableTreeNode(refPath.substring(len)));
			}
		}
		treeModel.reload();
		tree.setSelectionRow(0);
		tree.requestFocusInWindow();
	}

}
