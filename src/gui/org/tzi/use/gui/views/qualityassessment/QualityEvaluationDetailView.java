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
import org.tzi.use.gui.views.diagrams.elements.edges.EdgeBase;
import org.tzi.use.main.Session;
import org.tzi.use.parser.ocl.OCLCompiler;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MAssociationClass;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MGeneralization;
import org.tzi.use.uml.mm.MMInstanceGenerator;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.MModelElement;
import org.tzi.use.uml.mm.MOperation;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.MultiplicityViolationException;
import org.tzi.use.uml.ocl.type.CollectionType;
import org.tzi.use.uml.ocl.value.Value;

/**
 * TODO
 * @author Khanh-Hoang Doan
 *
 */
public class QualityEvaluationDetailView extends JDialog {

private Evaluator evaluator;
	
	private final JPanel contentPanel = new JPanel();
	
	private final JTextArea fTextName;
	private final JTextArea fTextDecs;

    private final JTextArea fTextOut;

    private final JButton btnClose;

	/**
	 * Create the dialog.
	 */
    QualityEvaluationDetailView(final Session fSession, final MainWindow parent, QualityProperty designSmell) {
		super(parent, "Metric evaluation detail");
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		// Use font specified in the settings 
        Font evalFont = Font.getFont("use.gui.evalFont", getFont());
        
        // create text components and labels
        fTextName = new JTextArea();
        fTextName.setEditable(false);
        fTextName.setText(designSmell.getName());
        fTextName.setFont(evalFont);
        JLabel textNameLabel = new JLabel("Name of design smell:");
        textNameLabel.setDisplayedMnemonic('O');
        textNameLabel.setLabelFor(fTextName);
        
        fTextDecs = new JTextArea();
        fTextDecs.setEditable(false);
        fTextDecs.setLineWrap(true);
        fTextDecs.setText(designSmell.getDesc());
        fTextDecs.setFont(evalFont);
        JLabel textDecsLabel = new JLabel("Description:");
        textDecsLabel.setDisplayedMnemonic('O');
        textDecsLabel.setLabelFor(fTextDecs);
        
        fTextOut = new JTextArea();
        fTextOut.setEditable(false);
        fTextOut.setLineWrap(true);
        fTextOut.setFont(evalFont);
        
        try
        {
	        //Evaluate the auto generated meta-query and show result in fTextOut
	        String evaluationInv = designSmell.getSelectExpression();
			String errFilename = Paths.get(System.getProperty("user.dir")).resolve("OCLEvaluationLog.txt").toAbsolutePath().toString();
			String violatingElementKind = designSmell.getViolatingElement();	
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
	            
	            //highlight the violating classes on the class diagram by setting it selected
	            if(val.isSet()) 
	            {
	            	org.tzi.use.uml.ocl.type.Type t = ((CollectionType)val.type()).elemType();
	            	String[] metaObectsNames = val.toString().substring(4, val.toString().length()-1).split(",");
	            	highlightViolatingElements(fSession.system().model(), metaObectsNames, t.shortName());
	            	fTextOut.setText(generateOutput(fSession.system().model(), metaObectsNames, t.shortName(), violatingElementKind));
	            }
	            else
	            {
	            	// print result
		            String result = val.toStringWithType();
		            fTextOut.setText(result);
	            }
	        } catch (MultiplicityViolationException e) {
	        } 
        } catch(IOException ex){}
        
        JLabel textOutLabel = new JLabel("Violating elements:");
        textOutLabel.setLabelFor(fTextOut);

        // create panel on the left and add text components
        JPanel textPane = new JPanel();
        textPane.setLayout(new BoxLayout(textPane, BoxLayout.Y_AXIS));

        JPanel p = new JPanel(new BorderLayout());
        p.add(textNameLabel, BorderLayout.NORTH);
        p.add(new JScrollPane(fTextName), BorderLayout.CENTER);
        textPane.add(p);
        textPane.add(Box.createRigidArea(new Dimension(0, 5)));

        p = new JPanel(new BorderLayout());
        p.add(textDecsLabel, BorderLayout.NORTH);
        p.add(new JScrollPane(fTextDecs), BorderLayout.CENTER);
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
    
    //highlight the violating classes on the class diagram by setting it selected
    private void highlightViolatingElements(MModel model, String[] violatingMetaInstances, String elementKind)
    {
    	MModelElement modelElement;
    	//get list of current class diagram views
    	List<ClassDiagramView> cdviews = MainWindow.instance().getClassDiagrams();
    	for(ClassDiagramView cdview : cdviews) {
    		ClassDiagram cd = cdview.getClassDiagram();
    		//deselect all elements in the class diagram
    		cd.deSelectAllElement();
    		//set elements selected
    		for(int i=0; i<violatingMetaInstances.length;i++) {
    			modelElement = (new MMInstanceGenerator().getModelElementfromMetaInstanceName(model, violatingMetaInstances[i], elementKind));
    			if(modelElement instanceof MClass || modelElement instanceof MAssociationClass)
    			{
    				ClassNode n = ((ClassDiagramData)cd.getVisibleData()).fClassToNodeMap.get(modelElement);
    				if(n != null) cd.getNodeSelection().add(n);
    			}
    			else if(modelElement instanceof MAssociation)
    			{
    				
    				EdgeBase e = ((ClassDiagramData)cd.getVisibleData()).fBinaryAssocToEdgeMap.get(modelElement);
    				if(e != null) cd.getEdgeSelection().add(e);
    			}
    			else if(modelElement instanceof MGeneralization)
    			{
    				
    				EdgeBase e = ((ClassDiagramData)cd.getVisibleData()).fGenToGeneralizationEdge.get(modelElement);
    				if(e != null) cd.getEdgeSelection().add(e);
    			}
    			else if(modelElement instanceof MAttribute)
    			{
    				MClass cls = ((MAttribute)modelElement).owner();
    				ClassNode n = ((ClassDiagramData)cd.getVisibleData()).fClassToNodeMap.get(cls);
    				if(n != null) cd.getNodeSelection().add(n);
    			}
    			else if(modelElement instanceof MOperation)
    			{
    				MClass cls = ((MOperation)modelElement).cls();
    				ClassNode n = ((ClassDiagramData)cd.getVisibleData()).fClassToNodeMap.get(cls);
    				if(n != null) cd.getNodeSelection().add(n);
    			}
    				
			}
    		cd.invalidateContent(true);
		}  	
    }
    
    //generate output reporting the violating elements
    private String generateOutput(MModel model, String[] violatingMetaInstances, String metaClass, String elementKind){
    	String output ="";
    	int i = violatingMetaInstances.length;
    	MModelElement modelElement;
    	output += elementKind + (i>1?(elementKind.equals("Class")?"es":"s"):"") + ": ";
    	for(String metaInstance: violatingMetaInstances) {
			modelElement = (new MMInstanceGenerator().getModelElementfromMetaInstanceName(model, metaInstance, metaClass));
			if(modelElement instanceof MOperation)
				output += ((MOperation)modelElement).qualifiedName() + "; ";
			else if (modelElement instanceof MAttribute)
				output += ((MAttribute)modelElement).qualifiedName() + "; ";
			else
				output += modelElement.toString() + "; "; 
    	}
    	
    	return output;
    }
    
	private void closeDialog() {
        setVisible(false);
        dispose();
    }

}
