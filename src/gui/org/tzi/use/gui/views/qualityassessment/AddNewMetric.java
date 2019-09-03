/*
 * USE - UML based specification environment
 * Copyright (C) 1999-2010 Mark Richters, University of Bremen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

// $Id$

package org.tzi.use.gui.views.qualityassessment;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.tzi.use.config.Options;
import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.gui.util.CloseOnEscapeKeyListener;
import org.tzi.use.gui.util.TextComponentWriter;
import org.tzi.use.main.Session;
import org.tzi.use.parser.ocl.OCLCompiler;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MOperation;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.MultiplicityViolationException;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.util.StringUtil;
import org.tzi.use.util.TeeWriter;

/**
 * TODO
 * @author Khanh-Hoang Doan
 *
 */
public class AddNewMetric extends JDialog {

	private MSystem fMetaSystem;
	
	private final JTextArea fTextShortName;

	private final JTextArea fTextName;
	
	private final JTextArea fTextDecs;
	
	private final JComboBox<String> fComboType;
	
	private final JComboBox<String> fComboMetricList;
	
	private final JComboBox<String> fComboClassList;
	
	private final JComboBox<String> fComboScope;
	
    private final JTextArea fTextIn;

    private final JTextArea fTextOut;

    private Evaluator evaluator;

    private final JButton btnAdd;
    
    private final JButton btnEval;
    
    private JPanel p1;
    
    private final String classAlias = "_c";

	/**
	 * Create the dialog.
	 */
	@SuppressWarnings("unchecked")
	public AddNewMetric(final MainWindow parent, final Session session) {
		super(parent, "Add new metric to the library");
    	
    	fMetaSystem = session.metaSystem();
    	
    	setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    	// Use font specified in the settings 
        Font evalFont = Font.getFont("use.gui.evalFont", getFont());
        
        // create text components and labels
        fTextIn = new JTextArea(10,50);
        fTextIn.setLineWrap(true);
        fTextIn.setFont(evalFont);
        fTextIn.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				btnAdd.setEnabled(false);				
			}
		});
        JLabel textInLabel = new JLabel("Enter metric definition:");
        textInLabel.setDisplayedMnemonic('O');
        textInLabel.setLabelFor(fTextIn);
        
        fTextShortName = new JTextArea();
        fTextShortName.setFont(evalFont);
        JLabel textShortNamelabel = new JLabel("Short name:");
        textShortNamelabel.setLabelFor(fTextShortName);
        
        fTextName = new JTextArea();
        fTextName.setFont(evalFont);
        JLabel textNamelabel = new JLabel("Full name:");
        textNamelabel.setLabelFor(fTextName);
        
        fTextDecs = new JTextArea(10,50);
        fTextDecs.setLineWrap(true);
        fTextDecs.setFont(evalFont);
        JLabel textDecslabel = new JLabel("Description:");
        textDecslabel.setLabelFor(fTextDecs);
        
        String[] types = {"Size","Complexity","OO features","Coupling"};
        fComboType = new JComboBox<String>(types);
        fComboType.setSelectedIndex(0);
        JLabel comboTypeLabel = new JLabel("Type:");
        comboTypeLabel.setLabelFor(fComboType);
        
        String[] scopes = {"Model","Class"};
        fComboScope = new JComboBox<String>(scopes);
        fComboScope.setSelectedIndex(0);
        fComboScope.addActionListener(new ActionListener() {
			//if change the scope
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
					populateMetricComboBoxbyScope(); //load the pre-defined metrics to Metric combobox
					String textInLabelText = "<html>Enter metric definition:";
					if(fComboScope.getSelectedItem().toString().equals("Class"))
						textInLabel.setText("<html>Enter metric definition:<br/><b>Context " + classAlias + ": Class </b><html>");
					else
						textInLabelText = "<html>Enter metric definition:";
					//show the combobox allow user choosing a class to validate if the scope of new metric is 'class' 
					p1.setVisible(fComboScope.getSelectedItem().toString().equals("Class"));
				}
		});
        JLabel comboScopeLabel = new JLabel("Scope:");
        comboScopeLabel.setLabelFor(fComboScope);
        
        fComboMetricList = new JComboBox<String>();
        populateMetricComboBoxbyScope();
        fComboMetricList.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(fComboMetricList.getSelectedIndex()>0) //if user select a pre-defined metric as a template
				{
					String metricName = fComboMetricList.getSelectedItem().toString();
					MClass metricCls = fMetaSystem.model().getClass(fComboScope.getSelectedItem().toString()+"Metrics");
					if(metricCls != null)
					{
						//load the ocl definition of the selected metric to fTextin text box
						MOperation metricOp = metricCls.operation(metricName, false);
						if(metricOp != null) fTextIn.setText(metricOp.expression().toString());
					}
				}
			}
		});

        JLabel comboMetricLabel = new JLabel("Choose a metric as template:");
        comboMetricLabel.setLabelFor(fComboMetricList);
        
        fComboClassList = new JComboBox<String>();
        for(String cls:MetricAPI.getUserModelClassList(session.system().model()))
        	fComboClassList.addItem(cls);
        JLabel comboClassLabel = new JLabel("Choose a class to evaluate:");
        comboClassLabel.setLabelFor(fComboClassList);
        
        fTextOut = new JTextArea(5,50);
        fTextOut.setEditable(false);
        fTextOut.setLineWrap(true);
        fTextOut.setFont(evalFont);
        
        JLabel textOutLabel = new JLabel("Result:");
        textOutLabel.setLabelFor(fTextOut);

        // create panel on the left and add text components
        JPanel textPane = new JPanel();    
        textPane.setLayout(new BoxLayout(textPane, BoxLayout.Y_AXIS));
        
        JPanel p = new JPanel(new BorderLayout());
        p.add(textShortNamelabel, BorderLayout.NORTH);
        p.add(new JScrollPane(fTextShortName), BorderLayout.CENTER);
        textPane.add(p);
        textPane.add(Box.createRigidArea(new Dimension(0, 5)));
        
        p = new JPanel(new BorderLayout());
        p.add(textNamelabel, BorderLayout.NORTH);
        p.add(new JScrollPane(fTextName), BorderLayout.CENTER);
        textPane.add(p);
        textPane.add(Box.createRigidArea(new Dimension(0, 5)));
        
        p = new JPanel(new BorderLayout());
        p.add(textDecslabel, BorderLayout.NORTH);
        p.add(new JScrollPane(fTextDecs), BorderLayout.CENTER);
        textPane.add(p);
        textPane.add(Box.createRigidArea(new Dimension(0, 5)));
        
        p = new JPanel(new BorderLayout());
        p.add(comboTypeLabel, BorderLayout.NORTH);
        p.add(fComboType);
        textPane.add(p);        
        textPane.add(Box.createRigidArea(new Dimension(0, 5)));
        
        p = new JPanel(new BorderLayout());
        p.add(comboScopeLabel, BorderLayout.NORTH);
        p.add(fComboScope);
        textPane.add(p);        
        textPane.add(Box.createRigidArea(new Dimension(0, 5)));
        
        p = new JPanel(new BorderLayout());
        p.add(comboMetricLabel, BorderLayout.NORTH);
        p.add(fComboMetricList);
        textPane.add(p);        
        textPane.add(Box.createRigidArea(new Dimension(0, 5)));
        
        p = new JPanel(new BorderLayout());;
        p.add(textInLabel, BorderLayout.NORTH);
        p.add(new JScrollPane(fTextIn), BorderLayout.CENTER);
        textPane.add(p);
        textPane.add(Box.createRigidArea(new Dimension(0, 5)));
        
        p1 = new JPanel(new BorderLayout());
        p1.add(comboClassLabel, BorderLayout.NORTH);
        p1.add(fComboClassList);
        textPane.add(p1);        
        textPane.add(Box.createRigidArea(new Dimension(0, 5)));
        p1.setVisible(false);
        
        p = new JPanel(new BorderLayout());
        p.add(textOutLabel, BorderLayout.NORTH);
        p.add(new JScrollPane(fTextOut), BorderLayout.CENTER);
        textPane.add(p);
        textPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
     // create panel on the right containing buttons
        JPanel btnPane = new JPanel();
        
        btnEval = new JButton("Evaluate");
        btnEval.setMnemonic('E');
        
        btnEval.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {         	
        		if(fComboScope.getSelectedItem().toString().equals("Class"))
        			btnAdd.setEnabled(evaluate(createClassMetricEvaluationExp(
        					fTextIn.getText(), fComboClassList.getSelectedItem().toString()+"Class") , false));
        		else
        			btnAdd.setEnabled(evaluate(fTextIn.getText(), false));
            }
        });
        Dimension dim = btnEval.getMaximumSize();
        dim.width = Short.MAX_VALUE;
        btnEval.setMaximumSize(dim);
        
        btnAdd = new JButton("Add to Library");
        btnAdd.setMnemonic('A');
        btnAdd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(fTextName.getText().trim().equals(""))
					JOptionPane.showMessageDialog(null,"The name of the property can be empty!");
				else
				{
					String scope = fComboScope.getSelectedItem().toString();
					String oclDefinition = scope.equals("Model")? fTextIn.getText(): createClassMetricDefinition(fTextIn.getText(),scope);
					Metric m = new Metric(fTextShortName.getText(), fTextName.getText(), fTextDecs.getText(), fComboType.getSelectedItem().toString(),
							scope, oclDefinition);
					m.saveMetrictoXMLFile(MetricAPI.userDefinedMetricXMLFile);
				}
							
			}
		});
        dim = btnAdd.getMaximumSize();
        dim.width = Short.MAX_VALUE;
        btnAdd.setMaximumSize(dim);
        btnAdd.setEnabled(false);
        
        JButton btnClear = new JButton("Clear");
        btnClear.setMnemonic('C');
        btnClear.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                fTextOut.setText(null);
                btnAdd.setEnabled(false);
            }
        });
        dim = btnClear.getMaximumSize();
        dim.width = Short.MAX_VALUE;
        btnClear.setMaximumSize(dim);
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                closeDialog();
            }
        });
        dim = btnClose.getMaximumSize();
        dim.width = Short.MAX_VALUE;
        btnClose.setMaximumSize(dim);

        btnPane.setLayout(new BoxLayout(btnPane, BoxLayout.Y_AXIS));
        btnPane.add(Box.createVerticalGlue());
        btnPane.add(btnEval);
        btnPane.add(Box.createRigidArea(new Dimension(0, 5)));
        btnPane.add(btnAdd);
        btnPane.add(Box.createRigidArea(new Dimension(0, 5)));
        btnPane.add(btnClear);
        btnPane.add(Box.createRigidArea(new Dimension(0, 5)));
        btnPane.add(btnClose);
        btnPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        JComponent contentPane = (JComponent) getContentPane();
        contentPane.add(textPane, BorderLayout.CENTER);
        contentPane.add(btnPane, BorderLayout.EAST);
        getRootPane().setDefaultButton(btnEval);

        pack();
        setSize(new Dimension(600, 600));
        setLocationRelativeTo(parent);
        fTextIn.requestFocus();

        // allow dialog close on escape key
        CloseOnEscapeKeyListener ekl = new CloseOnEscapeKeyListener(this);
        addKeyListener(ekl);
        fTextIn.addKeyListener(ekl);
        fTextOut.addKeyListener(ekl);
	}
	/*
	 * Evaluate the input OCL expression. 
	 * Return True only if the result of the evaluation is a numeric value.
	 */
	private boolean evaluate(String in, boolean evalTree) {
        //select the system will be evaluated
    	Boolean check = false;
		MSystem evalSystem = this.fMetaSystem;
    	if (evalSystem == null) {
        	fTextOut.setText("No system!");
        }
        
    	// clear previous results
        fTextOut.setText(null);

        // send error output to result window and msg stream
        StringWriter msgWriter = new StringWriter();
        PrintWriter out = new PrintWriter(new TeeWriter(
                new TextComponentWriter(fTextOut), msgWriter), true);
      
        // compile query
        Expression expr = OCLCompiler.compileExpression(
        		evalSystem.model(),
        		evalSystem.state(),
                in, 
                "Error", 
                out, 
                evalSystem.varBindings());
        
        
        out.flush();
        fTextIn.requestFocus();

        // compile errors?
        if (expr == null) {
            // try to parse error message and set caret to error position
            String msg = msgWriter.toString();
            int colon1 = msg.indexOf(':');
            if (colon1 != -1) {
                int colon2 = msg.indexOf(':', colon1 + 1);
                int colon3 = msg.indexOf(':', colon2 + 1);
                
                try {
                    int line = Integer.parseInt(msg.substring(colon1 + 1,
                            colon2));
                    int column = Integer.parseInt(msg.substring(colon2 + 1,
                            colon3));
                    int caret = 1 + StringUtil.nthIndexOf(in, line - 1,
                            Options.LINE_SEPARATOR);
                    caret += column;

                    // sanity check
                    caret = Math.min(caret, fTextIn.getText().length());
                    fTextIn.setCaretPosition(caret);
                } catch (NumberFormatException ex) { }
            }
        }

        try {
            // evaluate it with current system state
            evaluator = new Evaluator(evalTree);
            Value val = evaluator.eval(expr, evalSystem.state(), evalSystem
                    .varBindings());
            // print result
            fTextOut.setText(val.toStringWithType());
            if(val.isInteger()||val.isReal()) check = true;
        } catch (MultiplicityViolationException e) {
            fTextOut.setText("Could not evaluate. " + e.getMessage());
        }
        return check;
    }
	
	private void populateMetricComboBoxbyScope()
	{
		fComboMetricList.removeAllItems();
		fComboMetricList.addItem("--");
		for(String metric:getPreDefinedMetrics(fComboScope.getSelectedItem().toString()))
			fComboMetricList.addItem(metric);
		fComboMetricList.setSelectedIndex(0);
	}
	
	//get pre-defined metrics from metric classes (not from XML file)
	private List<String> getPreDefinedMetrics(String scope)
	{
		List<String> metrics = new ArrayList<String>();
		MClass metricCls = this.fMetaSystem.model().getClass(scope+"Metrics");
		if(metricCls != null)
		{
			for(MOperation op: metricCls.operations())
				metrics.add(op.name());
		}
		return metrics;
	}
	
	/**
	 * @param textIn: input as metric definition
	 * @return the ocl expression that can be validated on a chosen class
	 */
	private String createClassMetricEvaluationExp(String textIn, String evaluatedClass){
		return textIn.replaceAll(classAlias, evaluatedClass).replaceAll("self", evaluatedClass);
	}
	
	/**
	 * @param textIn: user input as metric definition
	 * @return the ocl definiton that can be save to the XML file
	 */
	private String createClassMetricDefinition(String textIn, String scope){
		return textIn.replaceAll("self", "self.class").replaceAll(classAlias, "self.class");
	}
	
	private void closeDialog() {
        setVisible(false);
        dispose();
    }

}
