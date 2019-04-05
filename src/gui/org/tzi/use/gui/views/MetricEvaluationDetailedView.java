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

package org.tzi.use.gui.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.gui.views.diagrams.classdiagram.ClassDiagram;
import org.tzi.use.gui.views.diagrams.classdiagram.ClassDiagramData;
import org.tzi.use.gui.views.diagrams.classdiagram.ClassDiagramView;
import org.tzi.use.gui.views.diagrams.classdiagram.ClassNode;
import org.tzi.use.main.Session;
import org.tzi.use.parser.ocl.OCLCompiler;
import org.tzi.use.uml.mm.MMetricEvaluationSetting;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.MultiplicityViolationException;
import org.tzi.use.uml.ocl.value.Value;

/**
 * TODO
 * @author KHANHHOANG
 *
 */
@SuppressWarnings("serial")
class MetricEvaluationDetailedView extends JDialog {

	private Evaluator evaluator;
	
	private final JPanel contentPanel = new JPanel();
	
	private final JTextArea fTextIn;

    private final JTextArea fTextOut;

    private final JButton btnClose;

	/**
	 * Create the dialog.
	 */
	MetricEvaluationDetailedView(final Session fSession, final MainWindow parent, MMetricEvaluationSetting config) {
		super(parent, "Metric evaluation detail");
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		// Use font specified in the settings 
        Font evalFont = Font.getFont("use.gui.evalFont", getFont());
        
        // create text components and labels
        fTextIn = new JTextArea();
        fTextIn.setEditable(false);
        fTextIn.setText(config.toString());
        fTextIn.setFont(evalFont);
        JLabel textInLabel = new JLabel("Metric evaluation:");
        textInLabel.setDisplayedMnemonic('O');
        textInLabel.setLabelFor(fTextIn);
        
        fTextOut = new JTextArea();
        fTextOut.setEditable(false);
        fTextOut.setLineWrap(true);
        fTextOut.setFont(evalFont);
        
        try
        {
	        //Evaluate the auto generated meta-query and show result in fTextOut
	        String evaluationInv = config.createSelectQuery();
			String errFilename = Paths.get(System.getProperty("user.dir")).resolve("OCLEvaluationLog.txt").toAbsolutePath().toString();
					
			PrintWriter out = new PrintWriter(errFilename);
	
	        
	        // compile invariant
	        Expression expr = OCLCompiler.compileExpression(
	        		fSession.metaSystem().model(),
	        		fSession.metaSystem().state(),
	        		evaluationInv, 
	                "Error", 
	                out, 
	                fSession.metaSystem().varBindings());
	        	        
	        out.flush();
	        
	        try {
	            // evaluate it with current system state
	            evaluator = new Evaluator(true);
	            Value val = evaluator.eval(expr, fSession.metaSystem().state(), fSession.metaSystem().varBindings());
	            // print result
	            String result = val.toStringWithType();
	            fTextOut.setText(result);
	            //highlight the violating classes on the class diagram by setting it selected
	            if(val.isSet()) 
	            {
	            	String[] classMetaObectsNames = val.toString().substring(4, val.toString().length()-1).split(",");
	            	String className;
	            	//get list of current class diagram views
	            	List<ClassDiagramView> cdviews = MainWindow.instance().getClassDiagrams();
	            	for(ClassDiagramView cdview : cdviews) {
	            		ClassDiagram cd = cdview.getClassDiagram();
	            		//deselect all elements in the class diagram
	            		cd.deSelectAllElement();
	            		for(int i=0; i<classMetaObectsNames.length;i++) {
	            			className =classMetaObectsNames[i].substring(0,classMetaObectsNames[i].length()-5);
							ClassNode n = ((ClassDiagramData)cd.getVisibleData()).fClassToNodeMap.get(fSession.system().model().getClass(className));
							if(n != null) cd.getNodeSelection().add(n);
						}
	            		cd.invalidateContent(true);
					}
	            	
	            }
	        } catch (MultiplicityViolationException e) {
	        } 
        } catch(IOException ex){}
        
        JLabel textOutLabel = new JLabel("Violating Classes:");
        textOutLabel.setLabelFor(fTextOut);

        // create panel on the left and add text components
        JPanel textPane = new JPanel();
        textPane.setLayout(new BoxLayout(textPane, BoxLayout.Y_AXIS));

        JPanel p = new JPanel(new BorderLayout());
        p.add(textInLabel, BorderLayout.NORTH);
        p.add(new JScrollPane(fTextIn), BorderLayout.CENTER);
        textPane.add(p);
        textPane.add(Box.createRigidArea(new Dimension(0, 5)));

        p = new JPanel(new BorderLayout());
        p.add(textOutLabel, BorderLayout.NORTH);
        p.add(new JScrollPane(fTextOut), BorderLayout.CENTER);
        textPane.add(p);
        textPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // create panel on the right containing buttons
        JPanel btnPane = new JPanel();
        
        btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                closeDialog();
            }
        });

        Dimension dim = btnClose.getMaximumSize();
        dim.width = Short.MAX_VALUE;
        btnClose.setMaximumSize(dim);

        btnPane.setLayout(new BoxLayout(btnPane, BoxLayout.Y_AXIS));
        //btnPane.add(Box.createVerticalGlue());
        btnPane.add(btnClose);
        btnPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        JComponent contentPane = (JComponent) getContentPane();
        contentPane.add(textPane, BorderLayout.CENTER);
        contentPane.add(btnPane, BorderLayout.EAST);

        pack();
        setSize(new Dimension(500, 200));
        setLocationRelativeTo(parent);
	}
	private void closeDialog() {
        setVisible(false);
        dispose();
    }

}
