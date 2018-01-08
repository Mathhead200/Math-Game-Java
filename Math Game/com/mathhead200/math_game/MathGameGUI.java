package com.mathhead200.math_game;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class MathGameGUI extends JFrame implements ActionListener, WindowListener {
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
	
	private ProblemSet problemSet = null;
	private long timestamp;
	private StringWriter writer = new StringWriter();
	private int savedChars = 0;
	private JFileChooser fileChooser = new JFileChooser();
	
	private Chalkboard chalkboard;
	private GameProgressBar progressBar = new GameProgressBar(100);
	private JLabel leftLabel = new JLabel();
	private JLabel rightLabel = new JLabel();
	
	private JMenuItem saveItem = new JMenuItem("Save");
	private JMenuItem exitItem = new JMenuItem("Exit");
	
	private JMenuItem additionItem = new JMenuItem("Addition (+)");
	private JMenuItem subtractionItem = new JMenuItem("Subtraction (-)");
	private JMenuItem level1Item = new JMenuItem("Level 1 (+|-)");
	private JMenuItem multiplicationItem = new JMenuItem("Multiplication (\u00D7)");
	private JMenuItem divisionItem = new JMenuItem("Division (\u00F7)");
	private JMenuItem level2Item = new JMenuItem("Level 2 (\u00D7|\u00F7|+|-)");
	
	private JRadioButtonMenuItem easyItem = new JRadioButtonMenuItem("Easy");
	private JRadioButtonMenuItem mediumItem = new JRadioButtonMenuItem("Medium");
	private JRadioButtonMenuItem hardItem = new JRadioButtonMenuItem("Hard");
	private JRadioButtonMenuItem veryHardItem = new JRadioButtonMenuItem("Very Hard");
	private JRadioButtonMenuItem ultimateItem = new JRadioButtonMenuItem("Ultimate");
	
	
	private String getIncompleteProblemSetFooter() {
		if( this.problemSet == null )
			return "";
		StringWriter writer = new StringWriter();
		long duration = System.currentTimeMillis() - timestamp;
		long sec = duration / 1000;
		long min = sec / 60;
		sec %= 60;
		writer.write( String.format("INCOMPLETE %.0f%% %d\u2713 %d\u2717 (duration: %d minutes and %d seconds)\n\n",
				progressBar.getPercentage() * 100, this.problemSet.getCorrectCount(), this.problemSet.getIncorrectCount(), min, sec) );
		return writer.toString();
	}
	
	private void nextProblem() {
		progressBar.setValue( problemSet.getPoints() );
		if( problemSet.hasNext() ) {
			MathProblem problem = problemSet.next();
			chalkboard.setGuess("");
			chalkboard.setTopOperand( Integer.toString(problem.operand1) );
			chalkboard.setOperator(problem.operator);
			chalkboard.setBottomOperand( Integer.toString(problem.operand2) );
			chalkboard.setResult( Integer.toString(problem.result) );
			rightLabel.setText( String.format("(%,d / %,d points)", progressBar.getValue(), progressBar.getMax()) );
		} else {
			timestamp = System.currentTimeMillis() - timestamp;
			long sec = timestamp / 1000;
			long min = sec / 60;
			sec %= 60;
			writer.write( String.format("COMPLETED 100%% %d\u2713 %d\u2717 (in %d minutes and %d seconds)\n\n",
					problemSet.getCorrectCount(), problemSet.getIncorrectCount(), min, sec) );
			
			problemSet = null;
			chalkboard.setGuess(min + ":" + sec);
			chalkboard.setTopOperand("Congrats");
			chalkboard.setBottomOperand("You Win!");
			chalkboard.setResult(null);
			rightLabel.setText( String.format("(%d / %d points)", progressBar.getValue(), progressBar.getMax()) );
		}
	}
	
	private void initProblemSet(ProblemSet problemSet, String message) {
		writer.write( getIncompleteProblemSetFooter() );
		timestamp = System.currentTimeMillis();
		writer.write(String.format( "%s (started %s)\n", message, DATE_FORMAT.format(new Date(timestamp)) ));
		
		this.problemSet = problemSet;
		progressBar.setMax( problemSet.getGoal() );
		progressBar.setValue(0);
		progressBar.setValue(0); // to remove the highlighting
		leftLabel.setText(message);
		nextProblem();
	}
	
	private void save() {
		if( fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION ) {
			File file = fileChooser.getSelectedFile();
			if( file.exists() && JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog( this,
					"File \"" + file + "\" already exists. Are you sure you want to overwrite it?",
					"Overwirte?", JOptionPane.YES_NO_OPTION) )
			{
				return; // do not save
			}
			try( Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) ) {
				StringBuffer buffer = writer.getBuffer();
				for( int i = 0; i < buffer.length(); i++ )
					fileWriter.write( buffer.charAt(i) );
				fileWriter.write( getIncompleteProblemSetFooter() );
				fileWriter.flush();
				savedChars = buffer.length();
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), ex.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	
	public MathGameGUI() {
		super("Math Game");
		JPanel panel = new JPanel( new BorderLayout() );
		
		chalkboard = new Chalkboard(8, 5, 16) {
			public void correct() {
				if( problemSet == null )
					return;
				MathProblem pr = problemSet.getCurrentProblem();
				String prStr = String.format("%d %c %d = %s", pr.operand1, pr.operator, pr.operand2, getGuess());
				writer.write( String.format("%3d.  %-30s  \u2713\n", problemSet.getCurrentProblemNumber(), prStr) );
				problemSet.correct();
				nextProblem();
			}
			
			public void incorrect() {
				if( problemSet == null || chalkboard.getGuess().trim().length() == 0 )
					return;
				MathProblem pr = problemSet.getCurrentProblem();
				String prStr = String.format("%d %c %d = %s", pr.operand1, pr.operator, pr.operand2, getGuess());
				writer.write( String.format("%3d.  %-30s  \u2717\n", problemSet.getCurrentProblemNumber(), prStr) );
				problemSet.incorrect();
				nextProblem();
			}
		};
		Border border = chalkboard.getBorder();
		chalkboard.setBorder(null);
		chalkboard.setTopOperand("Math");
		chalkboard.setOperator('+');
		chalkboard.setBottomOperand("Game");
		Dimension dim = new Dimension(64, 250);
		progressBar.setMinimumSize(dim);
		progressBar.setPreferredSize(dim);
		progressBar.setBorder( BorderFactory.createEmptyBorder(16, 16, 16, 16) );
		progressBar.setBackground( chalkboard.getBackground().darker() );
		JPanel labelPanel = new JPanel( new GridLayout(1, 2) );
		leftLabel.setHorizontalAlignment(JLabel.CENTER);
		leftLabel.setText("Choose a level to start.");
		leftLabel.setFont( chalkboard.getFont().deriveFont((float) 24) );
		leftLabel.setForeground( chalkboard.getForeground() );
		rightLabel.setHorizontalAlignment(JLabel.RIGHT);
		rightLabel.setFont( chalkboard.getFont().deriveFont((float) 20) );
		rightLabel.setForeground( chalkboard.getForeground() );
		labelPanel.setBorder( BorderFactory.createEmptyBorder(16, 16, 32, 16) );
		labelPanel.setOpaque(false);
		labelPanel.add(leftLabel);
		labelPanel.add(rightLabel);
		
		panel.setBorder(border);
		panel.setBackground( chalkboard.getBackground() );
		panel.add(labelPanel, BorderLayout.PAGE_START);
		panel.add(progressBar, BorderLayout.LINE_START);
		panel.add(chalkboard, BorderLayout.CENTER);
		
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu levelMenu = new JMenu("Level");
		JMenu difficultyMenu = new JMenu("Difficulty");
		menuBar.add(fileMenu);
		menuBar.add(levelMenu);
		menuBar.add(difficultyMenu);
		fileMenu.add(saveItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		levelMenu.add(additionItem);
		levelMenu.add(subtractionItem);
		levelMenu.add(level1Item);
		levelMenu.addSeparator();
		levelMenu.add(multiplicationItem);
		levelMenu.add(divisionItem);
		levelMenu.add(level2Item);
		difficultyMenu.add(easyItem);
		difficultyMenu.add(mediumItem);
		difficultyMenu.add(hardItem);
		difficultyMenu.add(veryHardItem);
		difficultyMenu.add(ultimateItem);
		saveItem.addActionListener(this);
		exitItem.addActionListener(this);
		additionItem.addActionListener(this);
		subtractionItem.addActionListener(this);
		level1Item.addActionListener(this);
		multiplicationItem.addActionListener(this);
		divisionItem.addActionListener(this);
		level2Item.addActionListener(this);
		easyItem.addActionListener(this);
		mediumItem.addActionListener(this);
		hardItem.addActionListener(this);
		ButtonGroup difficultyGroup = new ButtonGroup();
		difficultyGroup.add(easyItem);
		difficultyGroup.add(mediumItem);
		difficultyGroup.add(hardItem);
		difficultyGroup.add(veryHardItem);
		difficultyGroup.add(ultimateItem);
		easyItem.setSelected(true);
		
		setBackground( panel.getBackground() );
		setLayout( new GridLayout(1, 1) );
		add(panel);
		setJMenuBar(menuBar);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		panel.setPreferredSize( new Dimension(850, 425) );
		pack();
		setLocationRelativeTo(null);
	}

	
	public void actionPerformed(ActionEvent e) {
		if( e.getSource() == saveItem ) {
			
			save();
		
		} else if( e.getSource() == exitItem ) {
			
			dispatchEvent( new WindowEvent(this, WindowEvent.WINDOW_CLOSING) );
		
		} else if( e.getSource() == additionItem ) {
			
			if( easyItem.isSelected() )
				initProblemSet( new ProblemSet(1, 12, 1, 12, '+', new TreeMap<>(), 10, 1.25), "-- Addition, Easy --" );
			else if( mediumItem.isSelected() )
				initProblemSet( new ProblemSet(1, 99, 1, 99, '+', new TreeMap<>(), 20, 2.0), "-- Addition, Medium --" );
			else if( hardItem.isSelected() )
				initProblemSet( new ProblemSet(1, 999, 1, 999, '+', new TreeMap<>(), 25, 3.0), "-- Addition, Hard --" );
			else if( veryHardItem.isSelected() )
				initProblemSet( new ProblemSet(1, 9999, 1, 9999, '+', new TreeMap<>(), 20, 4.0), "-- Addition, Very Hard --" );
			else if( ultimateItem.isSelected() )
				initProblemSet( new ProblemSet(1, 9999999, 1, 9999999, '+', new TreeMap<>(), 10, 5.0), "-- Addition, Ultimate --" );
			
		} else if( e.getSource() == subtractionItem ) {
			
			if( easyItem.isSelected() )
				initProblemSet( new ProblemSet(1, 12, 1, 12, '-', new TreeMap<>(), 10, 1.25), "-- Subtraction, Easy --" );
			else if( mediumItem.isSelected() )
				initProblemSet( new ProblemSet(1, 99, 1, 99, '-', new TreeMap<>(), 20, 2.0), "-- Subtraction, Medium --" );
			else if( hardItem.isSelected() )
				initProblemSet( new ProblemSet(1, 999, 1, 999, '-', new TreeMap<>(), 25, 3.0), "-- Subtraction, Hard --" );
			else if( veryHardItem.isSelected() )
				initProblemSet( new ProblemSet(1, 9999, 1, 9999, '-', new TreeMap<>(), 20, 4.0), "-- Subtraction, Very Hard --" );
			else if( ultimateItem.isSelected() )
				initProblemSet( new ProblemSet(1, 9999999, 1, 9999999, '-', new TreeMap<>(), 10, 5.0), "-- Subtraction, Ultimate --" );
			
		} else if( e.getSource() == level1Item ) {
			
			TreeMap<Character, Double> weights = new TreeMap<>();
			weights.put('-', 0.5);
			
			if( easyItem.isSelected() )
				initProblemSet( new ProblemSet(1, 12, 1, 12, '+', weights, 10, 1.25), "-- Level 1, Easy --" );
			else if( mediumItem.isSelected() )
				initProblemSet( new ProblemSet(1, 99, 1, 99, '+', weights, 20, 2.0), "-- Level 1, Medium --" );
			else if( hardItem.isSelected() )
				initProblemSet( new ProblemSet(1, 999, 1, 999, '+', weights, 25, 3.0), "-- Level 1, Hard --" );
			else if( veryHardItem.isSelected() )
				initProblemSet( new ProblemSet(1, 9999, 1, 9999, '+', weights, 20, 4.0), "-- Level 1, Very Hard --" );
			else if( ultimateItem.isSelected() )
				initProblemSet( new ProblemSet(1, 9999999, 1, 9999999, '+', weights, 10, 5.0), "-- Level 1, Ultimate --" );
			
		} else if( e.getSource() == multiplicationItem ) {
			
			if( easyItem.isSelected() )
				initProblemSet( new ProblemSet(1, 6, 1, 6, '*', new TreeMap<>(), 10, 1.25), "-- Multiplication, Easy --" );
			else if( mediumItem.isSelected() )
				initProblemSet( new ProblemSet(1, 12, 1, 12, '*', new TreeMap<>(), 20, 2.0), "-- Multiplication, Medium --" );
			else if( hardItem.isSelected() )
				initProblemSet( new ProblemSet(1, 99, 1, 99, '*', new TreeMap<>(), 25, 3.0), "-- Multiplication, Hard --" );
			else if( veryHardItem.isSelected() )
				initProblemSet( new ProblemSet(1, 999, 1, 99, '*', new TreeMap<>(), 20, 4.0), "-- Multiplication, Very Hard --" );
			else if( ultimateItem.isSelected() )
				initProblemSet( new ProblemSet(1, 9999, 1, 9999, '*', new TreeMap<>(), 10, 5.0), "-- Multiplication, Ultimate --" );
			
		} else if( e.getSource() == divisionItem ) {
			
			if( easyItem.isSelected() )
				initProblemSet( new ProblemSet(1, 6, 1, 6, '/', new TreeMap<>(), 10, 2), "-- Division, Easy --" );
			else if( mediumItem.isSelected() )
				initProblemSet( new ProblemSet(1, 12, 1, 12, '/', new TreeMap<>(), 20, 2.0), "-- Division, Medium --" );
			else if( hardItem.isSelected() )
				initProblemSet( new ProblemSet(1, 99, 1, 99, '/', new TreeMap<>(), 25, 3.0), "-- Division, Hard --" );
			else if( veryHardItem.isSelected() )
				initProblemSet( new ProblemSet(1, 999, 1, 99, '/', new TreeMap<>(), 20, 4.0), "-- Division, Very Hard --" );
			else if( ultimateItem.isSelected() )
				initProblemSet( new ProblemSet(1, 9999, 1, 9999, '/', new TreeMap<>(), 10, 5.0), "-- Division, Ultimate --" );
			
			
		} else if( e.getSource() == level2Item ) {
			
			TreeMap<Character, Double> weights = new TreeMap<>();
			weights.put('-', 0.1);
			weights.put('*', 0.4);
			weights.put('/', 0.4);
			
			if( easyItem.isSelected() )
				initProblemSet( new ProblemSet(1, 6, 1, 6, '+', weights, 10, 2), "-- Level 2, Easy --" );
			else if( mediumItem.isSelected() )
				initProblemSet( new ProblemSet(1, 12, 1, 12, '+', weights, 20, 3), "-- Level 2, Medium --" );
			else if( hardItem.isSelected() )
				initProblemSet( new ProblemSet(1, 99, 1, 99, '+', weights, 30, 5), "-- Level 2, Hard --" );
			else if( veryHardItem.isSelected() )
				initProblemSet( new ProblemSet(1, 999, 1, 99, '+', weights, 30, 5), "-- Level 2, Very Hard --" );
			else if( ultimateItem.isSelected() )
				initProblemSet( new ProblemSet(1, 9999, 1, 9999, '+', weights, 10, 5), "-- Level 2, Ultimate --" );
			
		} else if( e.getSource() == easyItem ) {
			
			// do nothing
			
		} else if( e.getSource() == mediumItem ) {
			
			// do nothing
			
		} else if( e.getSource() == hardItem ) {
			
			// do nothing
			
		}
	}

	public void windowOpened(WindowEvent e) {
		// do nothing
	}

	public void windowClosing(WindowEvent e) {
		while( savedChars != writer.getBuffer().length() ) {
			int confirmResult = JOptionPane.showConfirmDialog( this,
					"Some of the problems you have done have not been saved. Would you like to save them now?",
					"Save before exiting?", JOptionPane.YES_NO_CANCEL_OPTION );
			if( confirmResult == JOptionPane.YES_OPTION ) {
				save();
			} else if( confirmResult == JOptionPane.NO_OPTION ) {
				break;
			} else /* if( confirmResult == JOptionPane.CANCEL_OPTION ) */ {
				return; // without disposing
			}
		}
		dispose();
	}

	public void windowClosed(WindowEvent e) {
		System.exit(0);
	}

	public void windowIconified(WindowEvent e) {
		// do nothing
	}

	public void windowDeiconified(WindowEvent e) {
		// do nothing
	}

	public void windowActivated(WindowEvent e) {
		// do nothing
	}

	public void windowDeactivated(WindowEvent e) {
		// do nothing
	}
	
}
