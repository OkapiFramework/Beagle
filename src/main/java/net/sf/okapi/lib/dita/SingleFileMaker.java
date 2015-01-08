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

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

public class SingleFileMaker implements Closeable {
	
	final private XMLEventFactory evfact;
	final private ILog log;
	final private TagRenamer tagRen;
	final private LBDefinition lbDef;
	
	private XMLEventWriter writer = null;
	private File inputFile;
	private File tagRenFile;
	private File lbDefFile;
	private File outputFile;

	public SingleFileMaker (ILog log) {
		this.log = log;
		this.evfact = XMLEventFactory.newInstance();
		this.tagRen = new TagRenamer();
		this.lbDef = new LBDefinition();
	}

	public void setParameters (File inputFile,
		File tagRenFile,
		File lbDefFile,
		File outputFile)
	{
		this.inputFile = inputFile;
		this.tagRenFile = tagRenFile;
		this.lbDefFile = lbDefFile;
		this.outputFile = outputFile;
	}

	public void process () throws XMLStreamException, FactoryConfigurationError, FileNotFoundException {
		// DITA Map file
		log.log("DITAMAP file: "+inputFile);
		// Tag renaming file
		log.log("Tag renaming file: "+(tagRenFile==null ? "None" : tagRenFile.getAbsolutePath()));
		if ( tagRenFile != null ) {
			if ( !tagRen.loadFile(tagRenFile) ) {
				log.log("WARNING: tag-renaming file not found.");
			}
		}
		// Line-breaks file
		log.log("Line-breaks definition file: "+(lbDefFile==null ? "None" : lbDefFile.getAbsolutePath()));
		if ( lbDefFile != null ) {
			if ( !lbDef.loadFile(lbDefFile) ) {
				log.log("WARNING: line-breaks definition file not found.");
			}
		}
		
		// Output file
		log.log("Output: "+outputFile.getAbsolutePath());
		
		// Create the output
		writer = XMLOutputFactory.newInstance().createXMLEventWriter(
			new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8));
		writer.add(evfact.createStartDocument());
		writer.add(evfact.createCharacters("\n"));
		writer.add(evfact.createStartElement("", null, "ROOT"));
		
		HarvesterRewriter hw = new HarvesterRewriter(log, writer, evfact, tagRen, lbDef);
		hw.process(inputFile);
		
		writer.add(evfact.createEndElement("", null, "ROOT"));
		writer.add(evfact.createEndDocument());
		writer.flush();
	}
	
	@Override
	public void close () throws IOException {
		if ( writer != null ) {
			try {
				writer.close();
			}
			catch (XMLStreamException e) {
				throw new IOException(e);
			}
			writer = null;
		}
	}

}
