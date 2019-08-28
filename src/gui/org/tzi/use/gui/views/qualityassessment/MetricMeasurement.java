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
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

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
	private Map<String, Metric> preDefinedMetricData;
	private Map<String, Metric> userDefinedMetricData;
	private MetricMeasurementTableModel tableModel = new MetricMeasurementTableModel();
	
	@SuppressWarnings("unchecked")
	public MetricMeasurement(final MainWindow parent, final Session fSession) {
		super(parent, "Class model metric measurement");
		metaSystem = fSession.metaSystem();
		system = fSession.system();
		
		Path homeDir = Paths.get(System.getProperty("user.dir")); 
		File xmlFile;
		xmlFile = homeDir.resolve("metamodels").resolve("PreDefinedMetrics.xml").toFile();
		preDefinedMetricData= Util.loadMetricDatafromXMLFile(xmlFile);
		
		xmlFile = homeDir.resolve("metamodels").resolve("UserDefinedMetrics.xml").toFile();
		userDefinedMetricData= Util.loadMetricDatafromXMLFile(xmlFile);
		
		
		setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		// Define the panel to hold the buttons 
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        JLabel lblClassList = new JLabel("Select a Class: ");
        topPanel.add(lblClassList);
        
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
				
				List<Metric> metrics = new ArrayList<Metric>();
				if(fComboClassList.getSelectedIndex()==0)
					metrics = loadModelMetrics();
				else
				{
					String clsName = fComboClassList.getSelectedItem().toString();
					MClass cls = system.model().getClass(clsName.substring(0, clsName.length()-6));
					if(cls != null) metrics = loadClassMetrics(cls);
				}
				tableModel.setList(metrics);
			}
		});
        topPanel.add(btnView);
        
        //set model and other properties for the JTable
        tblMetricList = new JTable();
        tblMetricList.setModel(tableModel);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		leftRenderer.setHorizontalAlignment(JLabel.LEFT);
		for(int i=0;i<tblMetricList.getColumnCount();i++)
		if(i==1)
			tblMetricList.getColumnModel().getColumn(i).setCellRenderer( leftRenderer );
		else
			tblMetricList.getColumnModel().getColumn(i).setCellRenderer( centerRenderer );
		
        
        JScrollPane scrollPane = new JScrollPane(tblMetricList, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        
        scrollPane.setPreferredSize(new Dimension(750,450));
        
       	//set the table column widths
       	int[] columnWidths = {70,330,120,80};
       	setTableColumnWidths(columnWidths);
        
        
        JComponent contentPane = (JComponent) getContentPane();
        contentPane.add(topPanel,BorderLayout.NORTH);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        getRootPane().setDefaultButton(btnView);
        
        pack();
        setSize(new Dimension(600, 500));
        setLocationRelativeTo(parent);
	}
	
	
	private void setTableColumnWidths(int[] columnWidths) {
	    TableColumnModel columnModel = tblMetricList.getColumnModel();
	    for (int i = 0; i < columnModel.getColumnCount(); i++) {       
	            columnModel.getColumn(i).setPreferredWidth(columnWidths[i]);
	    }
	}
	
	/**
	 * calculate class scope metrics of the class cls
	 * @return 
	 */
	private List<Metric> loadClassMetrics(MClass cls) {
		List<Metric> metrics = new ArrayList<Metric>();
		Metric metric;
		//get the meta class containing class scope pre-defined metric definitions
		MClass metricCls = metaSystem.model().getClass("ClassMetrics");
		if(metricCls != null)
		{
			for(MOperation op: metricCls.operations())
			{
				metric = preDefinedMetricData.get("c"+op.name());
				if(metric == null)
					metric = new Metric(op.name(),"","","","Class","",-1);
				else
				{
					metric.setShortName(op.name());
					metric.setScope("Class");
				}
				metric.setValue(metric.evaluate(metaSystem, cls));
				//add the metric to the result 
				metrics.add(metric);
			}
		}
		//get the meta class containing class scope pre-defined metric definitions
		for (Map.Entry<String, Metric> entry : userDefinedMetricData.entrySet())		
		{
			metric = entry.getValue();
			if(metric.getScope().equals("Class"))
				metric.setValue(metric.evaluate(metaSystem, cls));
		}
		return metrics;
	}
	
	/**
	 * calculate model scope metric of the user model
	 * @return
	 */
	private List<Metric> loadModelMetrics() {
		List<Metric> metrics = new ArrayList<Metric>();
		Metric metric;
		//get the meta class containing model scope metric definitions
		MClass metricCls = metaSystem.model().getClass("ModelMetrics");
		if(metricCls != null)
		{
			for(MOperation op: metricCls.operations())
			{
				metric = preDefinedMetricData.get("m"+op.name());
				if(metric == null)
					metric = new Metric(op.name(),"","","","Model","",-1);
				else
				{
					metric.setShortName(op.name());
					metric.setScope("Model");
				}
				metric.setValue(metric.evaluate(metaSystem, null));
				//add the metric to the result 
				metrics.add(metric);
			}
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
	
}

/*
 * The Table model to display the list of metrics
 */ 
@SuppressWarnings("serial")
class MetricMeasurementTableModel extends AbstractTableModel {
    private List<Metric> list = new ArrayList<Metric>();
	private final String[] columnNames = { "Short Name", "Name", "Type", "Value" };
	
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
        case 3:
        	if(list.get(rowIndex).getValue() == -1.0)//undefined
        		return "<html><i>Undefined</i></html>";
    		else
        			return String.format("%.2f", list.get(rowIndex).getValue());
        default:
            return null;
        }
    }
}
