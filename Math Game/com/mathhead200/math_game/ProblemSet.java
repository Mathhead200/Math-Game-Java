package com.mathhead200.math_game;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.TreeMap;

public class ProblemSet implements Iterator<MathProblem> {
	
	private static final int MIN_GOAL = 50;
	
	private int min1;
	private int range1;
	private int min2;
	private int range2;
	private char defaultOp;
	private TreeMap<Character, Double> weights;
	private int goal;
	
	private double approxPointsPer;
	private int loss;
	
	private Random random = new Random();
	private int points = 0;
	private int correctCount = 0;
	private int incorrectCount = 0;
	private MathProblem currProb = null;
	private int currProbNum = 0;
	
	public ProblemSet(int min1, int max1, int min2, int max2, char defaultOp, Map<Character, Double> weights, int approxSize, double failMultiplier) {
		this.min1 = min1;
		this.range1 = max1 - min1 + 1;
		this.min2 = min2;
		this.range2 = max2 - min2 + 1;
		
		this.defaultOp = defaultOp;
		this.weights = new TreeMap<Character, Double>();
		double weightSum = 0;
		for( char op : weights.keySet() )
			this.weights.put( op, weightSum += weights.get(op) );
		if( weightSum > 1 )
			throw new IllegalArgumentException("sum of weights must not be greater then 1: " + weightSum);
		
		double freqAdd = 0.0;
		if( defaultOp == '+' || defaultOp == '-' )
			freqAdd += 1.0 - weightSum;
		if( weights.containsKey('+') )
			freqAdd += weights.get('+');
		if( weights.containsKey('-') )
			freqAdd += weights.get('-');
		double freqMul = 0.0;
		if( defaultOp == '*' || defaultOp == '/' )
			freqMul += 1.0 - weightSum;
		if( weights.containsKey('*') )
			freqMul += weights.get('*');
		if( weights.containsKey('/') )
			freqMul += weights.get('/');
		double approxPointsPerAdd = (min1 + max1 + min2 + max2) / 2.0;
		double approxPointsPerMul = (min1 + max1) * (min2 + max2) / 4.0;
		approxPointsPer = freqAdd * approxPointsPerAdd + freqMul * approxPointsPerMul;
		double expectedSize = approxSize * approxPointsPer;
		int goalResolution = MIN_GOAL * (int) Math.ceil( 2 * approxPointsPer / MIN_GOAL );
		this.goal = goalResolution * (int) Math.ceil(expectedSize / goalResolution);
		
		loss = (int) Math.round(failMultiplier * approxPointsPer);
		int mostSigPlace = (int) Math.pow( 10, (int) Math.log10(loss) );
		loss = (int) Math.round((double) loss / mostSigPlace) * mostSigPlace;
	}

	public boolean hasNext() {
		return points < goal;
	}

	public MathProblem next() {
		if( !hasNext() )
			throw new NoSuchElementException("this problem set has been completed");
		
		int num1 = min1 + random.nextInt(range1);
		int num2 = min2 + random.nextInt(range2);
		
		Character operator = null;
		double omega = random.nextDouble();
		for( char op : weights.keySet() )
			if( omega < weights.get(op) ) {
				operator = op;
				break;
			}
		if( operator == null )
			operator = defaultOp;
		
		if( operator == '-' )
			currProb = new MathProblem(num1 + num2, operator, num1);
		else if( operator == '/')
			currProb = new MathProblem(num1 * num2, operator, num1);
		else
			currProb = new MathProblem(num1, operator, num2);
		
		currProbNum++;
		return currProb;
	}
	
	public int getGoal() {
		return goal;
	}
	
	public int getPoints() {
		return points;
	}
	
	public double getApproxPointsPer() {
		return approxPointsPer;
	}

	public int getLoss() {
		return loss;
	}
	
	public int getCorrectCount() {
		return correctCount;
	}
	
	public int getIncorrectCount() {
		return incorrectCount;
	}

	public void correct() {
		if( currProb == null )
			throw new IllegalStateException("the current problem has already been completed, or no problem has been given");
		points += currProb.points;
		correctCount++;
		currProb = null;
	}
	
	public void incorrect() {
		if( currProb == null )
			throw new IllegalStateException("the current problem has already been completed, or no problem has been given");
		if( (points -= loss) < 0 )
			points = 0;
		incorrectCount++;
		currProb = null;
	}
	
	public MathProblem getCurrentProblem() {
		return currProb;
	}
	
	/**
	 * @return The number in the problem set the current problem is, or the size
	 *         of the problem size if there are no more problems.
	 */
	public int getCurrentProblemNumber() {
		return currProbNum;
	}
}
