package com.mathhead200.math_game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;


@SuppressWarnings("serial")
public abstract class Chalkboard extends JComponent implements KeyListener {
	
	private int columns;
	private int lineThickness;
	private int padding;
	private String topOperand = "";
	private char operator = ' ';
	private String bottomOperand = "";
	private String result = null;
	private StringBuilder guess = new StringBuilder();
	private int caretPosition = -1; // negative values turn caret typing off
	
	public Chalkboard(int maxLength, int lineThinkness, int padding) {
		this.columns = maxLength + 1;
		this.lineThickness = lineThinkness;
		this.padding = padding;
		
		setOpaque(true);
		setBackground( new Color(53, 92, 63) );
		setForeground( new Color(0xF4FD8A) );
		setFont( new Font("Comic Sans MS", Font.PLAIN, 64) );
		setBorder( BorderFactory.createLineBorder(new Color(203, 160, 105), 12) );
		setSize(584, 361);
		
		addKeyListener(this);
		setFocusable(true);
	}
	
	public Chalkboard(int maxLength) {
		this(maxLength, 5, 10);
	}
	
	public Chalkboard() {
		this(7);
	}
	
	public int getMaxLength() {
		return columns - 1;
	}

	public void setMaxLength(int maxLength) {
		int minLength = Math.min( topOperand.length(), bottomOperand.length() );
		if( result != null )
			minLength = Math.min( minLength, result.length() );
		if( maxLength < minLength )
			throw new IllegalArgumentException("maxLength can not be set lower then the length of any current operand or result: " + maxLength);
		
		if( (this.caretPosition += maxLength - getMaxLength()) < 0 )
			this.caretPosition = 0;
		
		this.columns = maxLength + 1;
		repaint();
	}
	
	public int getLineThickness() {
		return lineThickness;
	}
	
	public void setLineThickness(int lineThickness) {
		this.lineThickness = lineThickness > 0 ? lineThickness : 0;
		repaint();
	}

	public String getTopOperand() {
		return topOperand;
	}
	
	public int getPadding() {
		return padding;
	}

	public void setPadding(int padding) {
		this.padding = padding > 0 ? padding : 0;
		repaint();
	}

	public void setTopOperand(String topOperand) {
		if( topOperand.length() > columns - 1 )
			throw new IllegalArgumentException("operands can not be longer then the maxLength: " + topOperand);
		this.topOperand = topOperand;
		repaint();
	}

	public char getOperator() {
		return operator;
	}

	public void setOperator(char operator) {
		this.operator = operator;
		repaint();
	}

	public String getBottomOperand() {
		return bottomOperand;
	}

	public void setBottomOperand(String bottomOperand) {
		if( topOperand.length() > columns - 1 )
			throw new IllegalArgumentException("operands can not be longer then the maxLength: " + bottomOperand);
		this.bottomOperand = bottomOperand;
		repaint();
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		if( topOperand.length() > columns - 1 )
			throw new IllegalArgumentException("result can not be longer then the maxLength: " + result);
		this.result = result;
	}
	
	public String getGuess() {
		return guess.toString();
	}
	
	public void setGuess(String guess) {
		synchronized (this.guess) {
			this.guess.replace(0, this.guess.length(), guess);
		}
		repaint();
	}
	
	public int getCaretPosition() {
		return caretPosition;
	}
	
	public void setCaretPosition(int caretPosition) {
		this.caretPosition = (0 <= caretPosition && caretPosition < getMaxLength() ? caretPosition : -1);
		repaint();
	}
	
	public abstract void correct();
	
	public abstract void incorrect();
	
	public void paintComponent(Graphics graphics) {
		Graphics g = graphics.create();
		
		Rectangle rect = new Rectangle( 0, 0, getWidth(), getHeight() );
		Insets insets = getInsets();
		rect.x += insets.left;
		rect.y += insets.top;
		rect.width -= insets.left + insets.right;
		rect.height -= insets.top + insets.bottom;
		if( isOpaque() ) {
			g.setColor( getBackground() );
			g.fillRect(rect.x, rect.y, rect.width, rect.height);
		}
		rect.x += padding;
		rect.y += padding;
		rect.width -= 2 * padding;
		rect.height -= 2 * padding;
		
		g.setColor( getForeground() );
		g.setFont( getFont() );
		int dx = rect.width / columns;
		int dy = (rect.height - lineThickness - 2 * padding) / 3;
		
		int y = dy;
		// draw top operand
		for( int i = topOperand.length() - 1, x = rect.x + rect.width - dx; i >= 0; i--, x -= dx )
			g.drawString( Character.toString(topOperand.charAt(i)), x, y );
		
		y = 2 * dy;
		// draw operator
		g.drawString( Character.toString(operator), rect.x, y );
		// draw bottom operand
		for( int i = bottomOperand.length() - 1, x = rect.x + rect.width - dx; i >= 0; i--, x -= dx )
			g.drawString( Character.toString(bottomOperand.charAt(i)), x, y );
		
		y = 2 * dy + padding;
		// draw "equals" line
		g.fillRect(dx, y, rect.width - dx, lineThickness);
		
		y = 3 * dy + padding;
		// draw guess
		for( int i = guess.length() - 1, x = rect.x + rect.width - dx; i >= 0; i--, x -= dx )
			g.drawString( Character.toString(guess.charAt(i)), x, y );
		
		// draw caret
		if( caretPosition >= 0 ) {
			int x = dx * (caretPosition + 1);
			y = rect.height - padding;
			
			Polygon p = new Polygon();
			/*
			p.addPoint(  x + dx / 2,  y                            );
			p.addPoint(  x + dx,      y + padding - lineThickness  );
			p.addPoint(  x + dx,      y + padding                  );
			p.addPoint(  x + dx / 2,  y + lineThickness            );
			p.addPoint(  x,           y + padding                  );
			p.addPoint(  x,           y + padding - lineThickness  );
			*/
			p.addPoint(  x,                       y                            );
			p.addPoint(  x + lineThickness,       y                            );
			p.addPoint(  x + lineThickness,       y + padding - lineThickness  );
			p.addPoint(  x + dx - lineThickness,  y + padding - lineThickness  );
			p.addPoint(  x + dx - lineThickness,  y                            );
			p.addPoint(  x + dx,                  y                            );
			p.addPoint(  x + dx,                  y + padding                  );
			p.addPoint(  x,                       y + padding                  );
			
			// g.fillRect( x, y, dx, lineThickness);
			g.setColor( getBackground().darker() );
			g.fillPolygon(p);
		}
	}

	public void keyTyped(KeyEvent e) {
		if( Character.isAlphabetic(e.getKeyChar()) || Character.isDigit(e.getKeyChar()) ) {
			if( caretPosition >= 0 ) {
				synchronized(guess) {
					int pos;
					while( (pos = getRelativeCaretPosition()) < 0 )
						guess.insert(0, ' ');
					guess.setCharAt( pos, e.getKeyChar() );
					if( pos < guess.length() - 1 && guess.charAt(pos + 1) == ' ' )
						caretPosition++;
				}
			} else {
				synchronized(guess) {
					if( guess.length() < getMaxLength() )
						guess.append( e.getKeyChar() );
				}
			}
			repaint();
		}
	}

	private int getRelativeCaretPosition() {
		return caretPosition + guess.length() - getMaxLength();
	}
	
	public void keyPressed(KeyEvent e) {
		if( e.getKeyCode() == KeyEvent.VK_BACK_SPACE ) {
			
			if( caretPosition >= 0 ) {
				synchronized(guess) {
					int pos = getRelativeCaretPosition();
					if( pos >= 0 )
						guess.setCharAt(pos, ' ');
					while( guess.length() != 0 && guess.charAt(0) == ' ' )
						guess.deleteCharAt(0);
				}
			} else {
				synchronized(guess) {
					if( guess.length() != 0 )
						guess.deleteCharAt( guess.length() - 1 );
				}
			}
			repaint();
			
		} else if( e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
			
			caretPosition = -1;
			synchronized(guess) {
				if( guess.length() != 0 )
					guess.delete( 0, guess.length() );
			}
			repaint();
			
		} else if( e.getKeyCode() == KeyEvent.VK_LEFT ) {
			
			if( caretPosition >= 0 ) {
				if( (caretPosition = caretPosition - 1) < 0 )
					caretPosition = -1;
			} else {
				caretPosition = getMaxLength() - 1;
			}
			repaint();
			
		} else if( e.getKeyCode() == KeyEvent.VK_RIGHT ) {
			
			if( caretPosition >= 0 ) {
				if( (caretPosition = caretPosition + 1) >= getMaxLength() )
					caretPosition= -1;
			} else {
				caretPosition = 0;
			}
			repaint();
			
		} else if( e.getKeyChar() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE ) {
			
			String guess;
			synchronized(this.guess) {
				guess = this.guess.toString();
			}
			if( result != null && guess.equals(result) )
				correct();
			else
				incorrect();
			
		}
	}

	public void keyReleased(KeyEvent e) {
	}
	
}
