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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.tzi.use.uml.sys.MSystem;

/**
 * TODO
 * @author KHANHHOANG
 *
 */
@SuppressWarnings("serial")
class MetricEvaluationDetailedView extends JDialog {

	private final JPanel contentPanel = new JPanel();
	
	private final JTextArea fTextIn;

    private final JTextArea fTextOut;

    private final JButton btnClose;

	/**
	 * Create the dialog.
	 */
	MetricEvaluationDetailedView(final MSystem system, JFrame parent) {
		super(parent, "Further Inspectation of Metric Evaluation");
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		// Use font specified in the settings 
        Font evalFont = Font.getFont("use.gui.evalFont", getFont());
        
        // create text components and labels
        fTextIn = new JTextArea();
        fTextIn.setEditable(false);
        fTextIn.setFont(evalFont);
        JLabel textInLabel = new JLabel("Metric evaluation:");
        textInLabel.setDisplayedMnemonic('O');
        textInLabel.setLabelFor(fTextIn);
        
        fTextOut = new JTextArea();
        fTextOut.setEditable(false);
        fTextOut.setLineWrap(true);
        fTextOut.setFont(evalFont);
        
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
