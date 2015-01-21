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

import java.io.File;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

public class HarvesterRewriter {
	
	final private XMLEventWriter writer;
	final private ILog log;
	final private XMLEventFactory evFact;
	final TagRenamer tagRen;
	final LBDefinition lbDef;
	
	private StringBuilder crumbs;
	private String basePath = null;

	public HarvesterRewriter (ILog log,
		XMLEventWriter writer,
		XMLEventFactory evFact,
		TagRenamer tagRen,
		LBDefinition lbDef)
	{
		this.log = log;
		this.writer = writer;
		this.evFact = evFact;
		this.tagRen = tagRen;
		this.lbDef = lbDef;
	}
	
	public void process (File inputFile)
		throws XMLStreamException
	{
		XMLEventReader reader = null;
		try {
			final XMLInputFactory xif = XMLInputFactory.newFactory();
			xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
			xif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
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
			StringBuilder tmp = new StringBuilder();
			boolean wasChars = false;
			crumbs = new StringBuilder();
			String newName;
			Stack<Boolean> preserveLB = new Stack<>();
			preserveLB.push(false);
			
//System.out.println("------: "+path);
			final String refElements = ";topicref;mapref;";
			String name;
			while ( reader.hasNext() ) {
				XMLEvent event = reader.nextEvent();
				
				if (( tmp.length() > 0 ) && !event.isCharacters() ) {
					String text = tmp.toString();
					if ( !preserveLB.peek() ) {
						text = text.replaceAll("\\n", " ");
					}
					writer.add(evFact.createCharacters(text));
					wasChars = false;
					tmp.setLength(0);
				}
				
				switch ( event.getEventType() ) {
				case XMLEvent.START_ELEMENT:
					StartElement se = event.asStartElement();
					name = se.getName().toString();
					crumbs.append("/"+name);
					
					Boolean res = lbDef.preserveLb(crumbs.toString());
					if ( res == null ) preserveLB.push(preserveLB.peek());
					else preserveLB.push(res);

					if ( refElements.indexOf(";"+name+";") > -1 ) {
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
					}
					else {
						newName = tagRen.getNewName(crumbs.toString());
						if ( newName != null ) {
							XMLEvent nse = evFact.createStartElement(
								se.getName().getPrefix(), se.getName().getNamespaceURI(), newName,
								se.getAttributes(), se.getNamespaces());
							writer.add(nse);
						}
						else {
							writer.add(event);
						}
					}
					break;
					
				case XMLEvent.END_ELEMENT:
					EndElement ee = event.asEndElement();
					name = ee.getName().toString();
					if ( refElements.indexOf(";"+name+";") > -1 ) {
						// Skip the output, but still need to pop the crumbs
					}
					else {
						newName = tagRen.getNewName(crumbs.toString());
						if ( newName != null ) {
							XMLEvent nee = evFact.createEndElement(
								ee.getName().getPrefix(), ee.getName().getNamespaceURI(), newName,
								ee.getNamespaces());
							writer.add(nee);
						}
						else {
							writer.add(event);
						}
						writer.flush();
					}
					preserveLB.pop();
					if ( lbDef.addLb(crumbs.toString()) ) {
						writer.add(evFact.createCharacters("\n"));
					}
					popCrumbs(); // Pop the crumbs
					break;
					
				case XMLEvent.CHARACTERS:
					Characters chars = event.asCharacters();
					if ( !wasChars ) {
						tmp.setLength(0);
						wasChars = true;
					}
					tmp.append(chars.getData());
					break;
					
				case XMLEvent.ENTITY_REFERENCE:
					writer.add(evFact.createCharacters(getEntity((EntityReference)event)));
					break;
					
				case XMLEvent.DTD:
				case XMLEvent.START_DOCUMENT:
				case XMLEvent.END_DOCUMENT:
					// No output
					break;
					
				default:
					writer.add(event);
					break;
				}
			}
		}
		finally {
			if ( reader != null ) {
				reader.close();
			}
		}
	}

	private void popCrumbs () {
		if ( crumbs.length() > 0 ) {
			int p = crumbs.lastIndexOf("/");
			crumbs.delete(p, crumbs.length());
		}
	}
	
	private String getEntity (EntityReference er) {
		switch ( er.getName() ) {
		case "nbsp": return "\u00a0";
		}
		log.log("Entity reference not found: "+er.getName());
		return "";
	}
	
	private void follow (String ref) throws XMLStreamException {
		String path = ref;
		File file = new File(path);
		if ( !file.isAbsolute() ) {
			path = basePath + File.separator + ref;
			file = new File(path);
		}
		if ( !file.exists() ) {
			log.log("File not found: "+file.getAbsolutePath());
			writer.add(evFact.createComment("FILE NOT FOUND: "+file.getAbsolutePath()));
			return;
		}
		log.log("Following: "+ref);
		HarvesterRewriter hw = new HarvesterRewriter(log, writer, evFact, tagRen, lbDef);
		writer.add(evFact.createComment("START "+file.getAbsolutePath()));
		hw.process(file);
		writer.add(evFact.createComment("END "+file.getAbsolutePath()));
	}

	private String getAttrValue (StartElement se,
		String name)
	{
		Attribute attr = se.getAttributeByName(new QName("", name));
		if ( attr == null ) return null;
		return attr.getValue();
	}

}
