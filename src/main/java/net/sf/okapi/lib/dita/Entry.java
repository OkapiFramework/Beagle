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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Entry implements Iterable<String>, Comparable<Entry> {

	private String key;
	private String path;
	private Map<String, Entry> children;

	static public String convURL (String path) {
		return path.replaceAll("%20", " ");
	}
	
	public Entry (String key,
		String path)
	{
		this.key = key;
		this.path = path;
		this.children = new HashMap<>(2);
	}

	public String getKey () {
		return key;
	}
	
	public String getPath () {
		return path;
	}

	public Entry add (Entry child) {
		children.put(child.getKey(), child);
		return child;
	}

	public void clear () {
		children.clear();
	}
	
	public String getPath (String key) {
		return children.get(key).path;
	}

	@Override
	public Iterator<String> iterator () {
		return children.keySet().iterator();
	}

	@Override
	public int compareTo (Entry o) {
		if ( o == null ) return -1;
		if ( !(o instanceof Entry) ) return -1;
		return this.key.compareTo(o.key);
	}
	
}
