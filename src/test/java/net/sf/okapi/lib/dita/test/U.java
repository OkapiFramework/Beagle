package net.sf.okapi.lib.dita.test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Provides a set of utility functions for testing.
 */
public class U {

	/**
	 * Gets the parent directory for a given path.
	 * @param obj the object where to get the class.
	 * @param filepath the path.
	 * @return the parent directory of the path.
	 */
	public static String getParentDir (Object obj,
		String filepath)
	{
        URL url = obj.getClass().getResource(filepath);
        String parentDir = null;
        if ( url != null ) {
			try {
				File file = new File(url.toURI());
				parentDir = file.getParent();
			}
			catch (URISyntaxException e) {
				return null;
			}
        }
        return parentDir;
    }

}
