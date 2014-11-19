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

public class Harvester1 {
	
	final private ILog log;
	private Map<String, String> refs;
	private XMLEventReader reader = null;
	private String basePath = null;

	public Harvester1 (ILog log) {
		this.log = log;
	}
	
	public Map<String, String> gatherFiles (File inputFile)
		throws IOException
	{
		Path dir = FileSystems.getDefault().getPath(inputFile.getAbsolutePath());
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
					if ( key.endsWith(".ditamap") || key.endsWith(".dita") ) {
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
	
	public Map<String, String> gatherReferences (File inputFile)
		throws XMLStreamException
	{
		try {
			final XMLInputFactory xif = XMLInputFactory.newFactory();
			refs = new HashMap<>();
			if ( !inputFile.exists() ) {
				log.log("File not found: "+inputFile.getAbsolutePath());
				return refs;
			}
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
						break; // Do not follow
					default:
						String href = getAttrValue(se, "href");
						if ( href != null ) {
							p = href.lastIndexOf('#');
							if ( p > -1 ) {
								href = href.substring(0, p);
							}
							if ( !href.isEmpty() ) {
								href = href.toLowerCase();
								href = Entry.convURL(href);
								if ( href.endsWith(".dita") || href.endsWith(".ditamap") ) {
									follow(href);
								}
							}
						}
						String conref = getAttrValue(se, "conref");
						if ( conref != null ) {
							p = conref.lastIndexOf('#');
							if ( p > -1 ) {
								conref = conref.substring(0, p);
							}
							if ( !conref.isEmpty() ) {
								conref = conref.toLowerCase();
								conref = Entry.convURL(conref);
								if ( conref.endsWith(".dita") || conref.endsWith(".ditamap") ) {
									follow(conref);
								}
							}
						}
						
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
	
	private void follow (String ref) throws XMLStreamException {
		String path = ref;
		File file = new File(path);
		if ( !file.isAbsolute() ) {
			path = basePath + File.separator + ref;
			file = new File(path);
		}
		refs.put(path.replace('\\', '/').toLowerCase(), path);
		Harvester1 h = new Harvester1(log);
		Map<String, String> res = h.gatherReferences(file);
		refs.putAll(res);
	}

	private String getAttrValue (StartElement se,
		String name)
	{
		Attribute attr = se.getAttributeByName(new QName("", name));
		if ( attr == null ) return null;
		return attr.getValue();
	}

}
