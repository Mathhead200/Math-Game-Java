package com.mathhead200.math_game;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MathGame {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		} catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		MathGameGUI gui = new MathGameGUI();
		gui.setVisible(true);
	}

}
