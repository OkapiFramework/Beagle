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

package net.sf.okapi.lib.dita;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import net.sf.okapi.lib.dita.test.U;

import org.junit.Test;

public class HarvesterTest {

	private final String root = U.getParentDir(this, "/birds.ditamap");

	@Test
	public void testOneList () throws XMLStreamException, IOException {
		Harvester1 h = new Harvester1(null);
		File mainMap = new File(root+"/birds.ditamap");
		Map<String, String> list1 = h.gatherReferences(mainMap);
		assertNotNull(list1);
		assertEquals(3, list1.size());
		
		Map<String, String> list2 = h.gatherFiles(mainMap.getParentFile());
		assertNotNull(list2);
		
		for ( String path : list1.keySet() ) {
			if ( list2.containsKey(path) ) {
				list2.remove(path);
			}
		}
	}

	@Test
	public void testAllMaps () throws XMLStreamException, IOException {
		Harvester1 h = new Harvester1(null);
		File rootDir = new File(root);
		File[] files = rootDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept (File dir, String name) {
				return name.toLowerCase().endsWith(".ditamap");
			}
		});
		Map<String, String> list1 = new HashMap<>();
		for ( File mapFile : files ) {
			list1.putAll(h.gatherReferences(mapFile));
		}
		
		Map<String, String> list2 = h.gatherFiles(rootDir);
		assertNotNull(list2);
		
		for ( String path : list1.keySet() ) {
			if ( list2.containsKey(path) ) {
				list2.remove(path);
			}
		}
		
		System.out.println("DITA files not referenced in any other files:");
		for ( String path : list2.values() ) {
			if ( path.endsWith(".dita") ) {
				System.out.println(path);
			}
		}
		System.out.println("\nDITA MAP files not referenced in any other files:");
		for ( String path : list2.values() ) {
			if ( path.endsWith(".ditamap") ) {
				System.out.println(path);
			}
		}
	}
	
}
