package io.github.ayakkus.xmld;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

/**
 * The entry point for the program. 
 * Sets the UI to being the same as my website.
 * {@link http://a-yakkus.github.io/site/index.html}
 * @author Jack Stevenson (A-Yakkus)
 */
public class Launch {
	
	public static void main (String[] args) {
		//UIManager.put("OptionPane.background", new ColorUIResource (0x795548));
		//UIManager.put("Panel.background",new ColorUIResource (0x8d6e63));
		//UIManager.put("OptionPane.messageForeground",new ColorUIResource (0x00bcd4));
		//UIManager.put("Label.foreground", new ColorUIResource (0x00bcd4));
		
		SwingUtilities.invokeLater (new Runnable(){
			/**
			 * Overridden from java.lang.Runnable.
			 * */
			@Override
			public void run () {
				new OptionWindow();
			}
		});
	}

}
