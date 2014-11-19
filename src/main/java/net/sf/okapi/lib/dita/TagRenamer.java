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

package net.sf.okapi.lib.dita;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class TagRenamer {

	Map<String, String> map = new LinkedHashMap<>();
	
	/**
	 * Indicates if a given crumbs should trigger a renaming.
	 * @param crumbs the crumbs to look at.
	 * @return true if a renaming is needed, false otherwise.
	 */
	public boolean needsRenaming (String crumbs) {
		return (getNewName(crumbs) != null);
	}
	
	/**
	 * Gets the new name for the given last name of the crumbs.
	 * @param crumbs the crumbs.
	 * @return the new name, or null if no new name was found.
	 */
	public String getNewName (String crumbs) {
		for ( String key : map.keySet() ) {
			if ( crumbs.endsWith(key) ) return map.get(key);
		}
		return null;
	}

	public boolean loadFile (File input) {
		if ( !input.exists() ) {
			return false;
		}
		try (BufferedReader in = new BufferedReader(
			new InputStreamReader(
				new FileInputStream(input), StandardCharsets.UTF_8))
		) {
			map.clear();
			String tmp;
			while ((tmp = in.readLine()) != null) {
				tmp = tmp.trim();
				if ( tmp.isEmpty() || tmp.startsWith("#") ) continue;
				// Parse the entry
				String[] parts = tmp.split("=", -1);
				if ( parts.length != 2 ) {
					throw new RuntimeException("Syntax error in line: "+tmp);
				}
				String key = parts[0].trim();
				String value = parts[1].trim();
				if ( key.isEmpty() || value.isEmpty() ) {
					throw new RuntimeException("Syntax error in line: "+tmp);
				}
				map.put(key, value);
			}
			return true;
		}
		catch (Throwable e) {
			throw new RuntimeException("Error loading the tag-renaming file.", e);
		}
	}
	
}
