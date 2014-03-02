/*
 * Created on Aug 27, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package es.org.chemi.games.snake.nls;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Chemi
 * 
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SnakeMessages {

    private static final String BUNDLE_NAME = "es.org.chemi.games.snake.nls.SnakeMessages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
	    .getBundle(BUNDLE_NAME);

    /**
	 * 
	 */
    private SnakeMessages() {
    }

    /**
     * @param key
     * @return
     */
    public static String getString(String key) {
	try {
	    return RESOURCE_BUNDLE.getString(key);
	} catch (MissingResourceException e) {
	    return '!' + key + '!';
	}
    }
}
