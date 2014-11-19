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

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

public class Harvester {
	
	private final XMLInputFactory xif;

	private Map<String, String> refs;
	private XMLEventReader reader = null;
	private String basePath = null;

	public Harvester () {
		xif = XMLInputFactory.newFactory();
	}

	public Map<String, String> gatherFiles (File rootDir)
		throws IOException
	{
		Path dir = FileSystems.getDefault().getPath(rootDir.getAbsolutePath());
		Map<String, String> files = new HashMap<>();
		return getFileNames(files, dir);
	}
	
	private Map<String, String> getFileNames (Map<String, String> fileNames,
		Path dir)
		throws IOException
	{
		DirectoryStream<Path> stream = null;
		try {
			stream = Files.newDirectoryStream(dir);
			for ( Path path : stream ) {
				if ( path.toFile().isDirectory() ) {
					getFileNames(fileNames, path);
				}
				else {
					String key = path.toAbsolutePath().toString().toLowerCase().replace('\\', '/');
					if ( key.endsWith(".ditamap") || key.endsWith(".dita")	) {
						fileNames.put(key, path.toAbsolutePath().toString());
					}
				}
			}
		}
		finally {
			if ( stream != null ) stream.close();
		}
		return fileNames;
	}
	
	public Map<String, String> gatherDirectReferences (File inputFile)
		throws XMLStreamException
	{
		try {
			refs = new HashMap<>();
			String path = inputFile.getAbsolutePath();
			int p = path.lastIndexOf('\\'); 
			if ( p == -1 ) p = path.lastIndexOf('/');
			if ( p != -1 ) {
				basePath = path.substring(0, p);
			}
			else {
				basePath = "";
			}
			
			StreamSource inputSource = new StreamSource(inputFile);
			reader = xif.createXMLEventReader(inputSource);
			
			while ( reader.hasNext() ) {
				XMLEvent event = reader.nextEvent();
				
				if ( event.isStartElement() ) {
					StartElement se = event.asStartElement();
					String name = se.getName().toString();
					switch ( name ) {
					case "xref":
						// Reference for those elements are not imports
						break;
					default:
						process(getAttrValue(se, "href"));
						process(getAttrValue(se, "conref"));
						break;
					}
				}
			}
		}
		finally {
			close();
		}
		return refs;
	}

	public void close ()
		throws XMLStreamException
	{
		if ( reader != null ) {
			reader.close();
		}
	}

	private void process (String ref) {
		if ( ref == null ) return;
		// Remove any fragment identifier
		int p = ref.lastIndexOf('#');
		if ( p > -1 ) ref = ref.substring(0, p);
		// Skip fragment-id-only reference
		if ( ref.isEmpty() ) return;
		// Add the new reference
		String key = ref;
		key = key.toLowerCase();
		if ( key.endsWith(".dita")
			|| key.endsWith(".ditamap")
		) {
			File file = new File(key);
			if ( !file.isAbsolute() ) {
				ref = basePath + File.separator + ref;
				key = ref;
			}
			refs.put(key.toLowerCase().replace('\\', '/'), ref);
		}
	}

	private String getAttrValue (StartElement se,
		String name)
	{
		Attribute attr = se.getAttributeByName(new QName("", name));
		if ( attr == null ) return null;
		return attr.getValue();
	}

}
