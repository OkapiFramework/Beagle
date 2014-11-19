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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Entries implements Iterable<Entry> {

	private class ExtensionComparator implements Comparator<Entry> {
		@Override
		public int compare (Entry o1,
			Entry o2)
		{
			String x1 = null;
			String x2 = null;
			int p = o1.getKey().lastIndexOf('.');
			if ( p > -1 ) x1 = o1.getKey().substring(p);
			p = o2.getKey().lastIndexOf('.');
			if ( p > -1 ) x2 = o2.getKey().substring(p);
			if (( x1 == null ) || ( x2 == null )) {
				return -1;
			}
			// .ditamap first
			return x2.compareTo(x1);
		}
	}

	private final ExtensionComparator comp = new ExtensionComparator();
	
	private List<Entry> entries;
	
	public Entries () {
		entries = new ArrayList<>();
	}

	public Entry add (Entry entry) {
		entries.add(entry);
		return entry;
	}

	@Override
	public Iterator<Entry> iterator () {
		return entries.iterator();
	}

	public void sort () {
		// Sort by path (key)
		Collections.sort(entries);
		// then sort by extensions
		Collections.sort(entries, comp);
	}
}
