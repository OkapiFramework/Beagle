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

import java.awt.Font;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

public class Main {

	public static void main (String[] originalArgs) {

		// Set the look-and-feel and a larger font size
		try {
			NimbusLookAndFeel nlf = new NimbusLookAndFeel() {
				private static final long serialVersionUID = 1L;
				@Override
				public UIDefaults getDefaults () {
					UIDefaults ret = super.getDefaults();
					ret.put("defaultFont", new Font(Font.DIALOG, 0, 14));
					return ret;
				}
			};
			UIManager.setLookAndFeel(nlf);
			UIDefaults defs = nlf.getDefaults();
			defs.put("Tree.drawHorizontalLines", true);
			defs.put("Tree.drawVerticalLines", true);
		}
		catch (UnsupportedLookAndFeelException e) {
			// Use the default font
			e.printStackTrace();
		}

		MainDialog.start();
	}

}
