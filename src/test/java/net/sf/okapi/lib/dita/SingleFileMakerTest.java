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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import net.sf.okapi.lib.dita.test.ConsoleLog;
import net.sf.okapi.lib.dita.test.U;

import org.junit.Test;

public class SingleFileMakerTest {

	private final String root = U.getParentDir(this, "/birds.ditamap");

	@Test
	public void testSimple ()
		throws IOException, XMLStreamException, FactoryConfigurationError
	{
		try (SingleFileMaker sfm = new SingleFileMaker(new ConsoleLog())) {
			File inputFile = new File(root + "/birds.ditamap");
			File tagRenFile = new File(root + "/tag-renaming.txt");
			File lbDefFile = new File(root + "/lb-definition.txt");
			File outputFile = new File(inputFile.getAbsolutePath()
					+ ".single.xml");
			outputFile.delete();
			sfm.setParameters(inputFile, tagRenFile, lbDefFile, outputFile);
			sfm.process();
			assertTrue(outputFile.exists());
		}
	}

}
