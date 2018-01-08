package com.mathhead200.math_game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class GameProgressBar extends JComponent {
	
	private int min;
	private int max;
	private int value = 0;
	private int prevValue = 0;
	
	private Color incForeground = null; // foreground color to use when an increase in value has occurred
	private Color decForeground = null; // foreground color to use when a decrease in value has occurred
	
	public GameProgressBar(int min, int max, int value) {
		setMinMax(min, max);
		setValue(value);
		prevValue = this.value;
		
		setOpaque(true);
		setBackground( new Color(53, 92, 63) );
		setForeground( new Color(0x94C0CC) );
		setIncForeground( new Color(0xBCDF8A) );
		setDecForeground( new Color(0xED7777) );
	}
	
	public GameProgressBar(int min, int max) {
		this(min, max, 0);
	}
	
	public GameProgressBar(int max) {
		this(0, max);
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		if( min >= max )
			throw new IllegalArgumentException("min must be less then max: " + min);
		this.min = min;
		repaint();
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		if( min >= max )
			throw new IllegalArgumentException("max must be greater then min: " + max);
		this.max = max;
		repaint();
	}

	public void setMinMax(int min, int max) {
		if( min >= max )
			throw new IllegalArgumentException("min must be less then max: " + min + ", " + max);
		this.min = min;
		this.max = max;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.prevValue = this.value;
		this.value = value < min ? min : value > max ? max : value;
		repaint();
	}
	
	public void addValue(int dv) {
		setValue( getValue() + dv );
	}
	
	public double getPercentage() {
		return (double) getValue() / (getMax() - getMin());
	}
	
	public Color getIncForeground() {
		return incForeground;
	}

	public void setIncForeground(Color incForeground) {
		this.incForeground = incForeground;
		repaint();
	}

	public Color getDecForeground() {
		return decForeground;
	}

	public void setDecForeground(Color decForeground) {
		this.decForeground = decForeground;
		repaint();
	}

	public void paintComponent(Graphics graphics) {
		Graphics g = graphics.create();
		
		Rectangle rect = new Rectangle( 0, 0, getWidth(), getHeight() );
		Insets insets = getInsets();
		rect.x += insets.left;
		rect.y += insets.top;
		rect.width -= insets.left + insets.right;
		rect.height -= insets.top + insets.bottom;
		
		int heightPrev = (int) Math.round((double) prevValue / (max - min) * rect.height);	
		g.setColor( getForeground() );
		g.fillRect(rect.x, rect.y + rect.height - heightPrev, rect.width, heightPrev);
		
		if( isOpaque() ) {
			g.setColor( getBackground() );
			g.fillRect(rect.x, rect.y, rect.width, rect.height - heightPrev);
		}
		
		if( value > prevValue ) {
			int heightChange = (int) Math.round((double) (value - prevValue) / (max - min) * rect.height);
			g.setColor( incForeground != null ? incForeground : getForeground().brighter() );
			g.fillRect(rect.x, rect.y + rect.height - heightPrev - heightChange, rect.width, heightChange);
		} else {
			int heightChange = (int) ((double) (prevValue - value) / (max - min) * rect.height);
			g.setColor( decForeground != null ? decForeground : getForeground().darker() );
			g.fillRect(rect.x, rect.y + rect.height - heightPrev, rect.width, heightChange);
		}
	}
}
