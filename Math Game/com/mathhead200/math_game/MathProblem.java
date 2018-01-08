package com.mathhead200.math_game;

public class MathProblem {
	public static final char PLUS = '+';
	public static final char MINUS = '-';
	public static final char TIMES = '\u00D7';
	public static final char DIVIDE = '\u00F7';
	
	public final int operand1;
	public final char operator;
	public final int operand2;
	public final int result;
	public final int points;
	
	public MathProblem(int operand1, char operator, int operand2) {
		this.operand1 = operand1;
		this.operator = (operator == '*' ? TIMES : operator == '/' ? DIVIDE : operator);
		this.operand2 = operand2;
		
		if( this.operator == PLUS ) {
			result = operand1 + operand2;
			points = result;
		} else if( this.operator == MINUS ) {
			if( operand2 > operand1 )
				throw new IllegalArgumentException("for '-', operand1 must be greater then or equal to operand2: " + operand1 + " - " + operand2);
			result = operand1 - operand2;
			points = operand1;
		} else if( this.operator == TIMES ) {
			result = operand1 * operand2;
			points = result;
		} else if( this.operator == DIVIDE ) {
			if( operand1 % operand2 != 0  )
				throw new IllegalArgumentException("for '/', operand2 must evenly divide operand1: " + operand1 + " / " + operand2);
			result = operand1 / operand2;
			points = operand1;
		} else {
			throw new IllegalArgumentException("unrecognized operator: " + operator);
		}
	}
	
	public String toString() {
		return String.format("%d %c %d = %d", operand1, operator, operand2, result);
	}
	
	public boolean equals(Object obj) {
		if( obj == this )
			return true;
		if( !(obj instanceof MathProblem) )
			return false;
		MathProblem prob = (MathProblem) obj;
		return operand1 == prob.operand1
		    && operator == prob.operator
		    && operand2 == prob.operand2;
	}
}
