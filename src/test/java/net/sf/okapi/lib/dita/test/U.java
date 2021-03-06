/*===========================================================================
  Copyright (C) 2014-2015 by the Okapi Framework contributors
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

package net.sf.okapi.lib.dita.test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Provides a set of utility functions for testing.
 */
public class U {

	/**
	 * Gets the parent directory for a given path.
	 * @param obj the object where to get the class.
	 * @param filepath the path.
	 * @return the parent directory of the path.
	 */
	public static String getParentDir (Object obj,
		String filepath)
	{
        URL url = obj.getClass().getResource(filepath);
        String parentDir = null;
        if ( url != null ) {
			try {
				File file = new File(url.toURI());
				parentDir = file.getParent();
			}
			catch (URISyntaxException e) {
				return null;
			}
        }
        return parentDir;
    }

}
