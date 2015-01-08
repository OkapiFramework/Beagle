/*===========================================================================
  Copyright (C) 2015 by the Okapi Framework contributors
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
import java.util.HashMap;
import java.util.Map;

/**
 * Maintains a list of the context in which line-breaks are allowed,
 * and / or where a line-break need to be added at the end of the element.
 */
public class LBDefinition {

	private Map<String, String> map = new HashMap<>();
	
	/**
	 * Gets the information about preserving line-breaks for a given element.
	 * @param crumbs the crumbs to look at.
	 * @return true if line-break in the given element are to be preserved,
	 * false if they can be normalized, null if the element name is not specified.
	 */
	public Boolean preserveLb (String crumbs) {
		for ( String key : map.keySet() ) {
			if ( crumbs.endsWith(key) ) {
				switch ( map.get(key) ) {
				case "p":
				case "pa":
					return true;
				default:
					return false;
				}
			}
		}
		return null;
	}

	/**
	 * Indicates if a line-break must be added at the end of a given element.
	 * @param crumbs the crumbs to look at.
	 * @return true to add a line-break, false otherwise.
	 */
	public boolean addLb (String crumbs) {
		for ( String key : map.keySet() ) {
			if ( crumbs.endsWith(key) ) {
				switch ( map.get(key) ) {
				case "da":
				case "pa":
					return true;
				default:
					return false;
				}
			}
		}
		return false;
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
			// elem1/elem2 = pa, da, p, d
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
				switch ( value ) {
				case "p":
				case "pa":
				case "d":
				case "da":
					break;
				default:
					throw new RuntimeException("Invalid value (must be p, pa, d or da) in line: "+tmp);
				}
				map.put(key, value);
			}
			return true;
		}
		catch (Throwable e) {
			throw new RuntimeException("Error loading the line-break definition file.", e);
		}
	}
	
}
