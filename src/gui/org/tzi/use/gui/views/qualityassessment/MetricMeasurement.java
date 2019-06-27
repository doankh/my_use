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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.main.Session;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MOperation;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.sys.MSystem;

/**
 * TODO
 * @author Khanh-Hoang Doan
 *
 */
public class MetricMeasurement extends JDialog {

	/**
	 * Create the panel.
	 */
	private MSystem metaSystem;
	private MSystem system;
	private Evaluator evaluator;
	private JTable tblMetricList;
	private JButton btnView;
	private final JComboBox fComboClassList;
	private MetricMeasurementTableModel tableModel = new MetricMeasurementTableModel();
	
	@SuppressWarnings("unchecked")
	public MetricMeasurement(final MainWindow parent, final Session fSession) {
		super(parent, "Class model metric measurement");
		metaSystem = fSession.metaSystem();
		system = fSession.system();
		
		setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		// Define the panel to hold the buttons 
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        JLabel lblclassList = new JLabel("Select a Class: ");
        topPanel.add(lblclassList);
        
        //populate items to the combobox
        fComboClassList = new JComboBox<Object>();
        fComboClassList.addItem("User Model");
        for(String cls: getClassList())
        	fComboClassList.addItem(cls + " Class");
        topPanel.add(fComboClassList);
        
        btnView = new JButton("View metrics");
        btnView.setMnemonic('V');
        btnView.setBackground(Color.WHITE);
        btnView.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				/*List<Metric> metrics = new ArrayList<Metric>();
				if(fComboClassList.getSelectedIndex()==0)
					metrics = loadModelMetrics();
				else
				{
					String clsName = fComboClassList.getSelectedItem().toString();
					MClass cls = system.model().getClass(clsName.substring(0, clsName.length()-5));
					if(cls != null) metrics = loadClassMetrics(cls);
				}
				tableModel.setList(metrics);*/
			}
		});
        topPanel.add(btnView);
        
        //tblMetricList.setModel(tableModel);
        
        JScrollPane scrollPane = new JScrollPane(tblMetricList, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        
        scrollPane.setPreferredSize(new Dimension(750,450));
        
        JComponent contentPane = (JComponent) getContentPane();
        contentPane.add(topPanel,BorderLayout.NORTH);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        getRootPane().setDefaultButton(btnView);
        
        pack();
        setSize(new Dimension(400, 400));
        setLocationRelativeTo(parent);
	}
	
	/**
	 * calculate class scope metrics of the class cls
	 * @return 
	 */
	private List<Metric> loadClassMetrics(MClass cls) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * calculate model scope metric of the user model
	 * @return
	 */
	private List<Metric> loadModelMetrics() {
		List<Metric> metrics = new ArrayList<Metric>();
		MClass cls = metaSystem.model().getClass("ModelMetrics");
		for(MOperation op: cls.operations())
		{
			Metric metric = new Metric();
			metric.shortName = op.name();
			metric.name = "";
			metric.description = "";
			metric.scope ="Model";
			metric.type ="";
		}
		return metrics;
	}

	//get the list of all classes in the user model
	private List<String> getClassList(){
		List<String> classes = new ArrayList<String>();
		for(MClass cls: system.model().classes())
			classes.add(cls.name());
		return classes;
	}
	
	/*
	 * The Table model to display the list of metrics
	 */ 
	@SuppressWarnings("serial")
	class MetricMeasurementTableModel extends AbstractTableModel {
	    private List<Metric> list = new ArrayList<Metric>();
		private final String[] columnNames = { "Design Smell", "Type", "Severity", "Evaluation" };

	    public void setList(List<Metric> data) {
	        this.list = data;
	        fireTableDataChanged();
	    }
	    
	    public Metric getDataItem(int row){
	    	return list.get(row);
	    }
	    
	    
	    @Override
		public String getColumnName(int col) {
	        return columnNames[col];
	    }
	    
	    public int getRowCount() {
	        return list.size();
	    }

	    public int getColumnCount() {
	        return columnNames.length;
	    }
	    
	    @Override
	    public Object getValueAt(int rowIndex, int columnIndex) {
	        switch (columnIndex) {
	        case 0:
	            return list.get(rowIndex).getShortName();
	        case 1:
	            return list.get(rowIndex).getName();
	        case 2:
	        	return list.get(rowIndex).getType();
	        
	        default:
	            return null;
	        }
	    }
	}
	/*
	 * data class for 
	 */
	class Metric{
		private String shortName;
		private String name;
		private String description;
		private String type;
		private String scope;
		private Double value;
		
		/**
		 * @return the shortName
		 */
		public String getShortName() {
			return shortName;
		}
		/**
		 * @param shortName the shortName to set
		 */
		public void setShortName(String shortName) {
			this.shortName = shortName;
		}
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}
		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}
		/**
		 * @param description the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}
		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}
		/**
		 * @param type the type to set
		 */
		public void setType(String type) {
			this.type = type;
		}
		/**
		 * @return the scope
		 */
		public String getScope() {
			return scope;
		}
		/**
		 * @param scope the scope to set
		 */
		public void setScope(String scope) {
			this.scope = scope;
		}
		/**
		 * @return the value
		 */
		public Double getValue() {
			return value;
		}
		/**
		 * @param value the value to set
		 */
		public void setValue(Double value) {
			this.value = value;
		}
		
	}
}
