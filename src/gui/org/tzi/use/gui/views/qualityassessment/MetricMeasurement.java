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
import java.util.HashMap;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.main.Session;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MOperation;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
	private Map<String, Metric> metricData;
	private MetricMeasurementTableModel tableModel = new MetricMeasurementTableModel();
	
	@SuppressWarnings("unchecked")
	public MetricMeasurement(final MainWindow parent, final Session fSession) {
		super(parent, "Class model metric measurement");
		metaSystem = fSession.metaSystem();
		system = fSession.system();
		
		initMetricData();
		
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
	
	/**
	 * Read data of the defined metrics from a XML file
	 */
	private void initMetricData() {
		metricData = new HashMap<String,Metric>();
		try {
			Path homeDir = Paths.get(System.getProperty("user.dir")); 
			File xmlFile = homeDir.resolve("metamodels").resolve("Metrics.xml").toFile();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			NodeList nList = doc.getElementsByTagName("Metric");
			for(int i=0; i<nList.getLength(); i++)
			{				
				Node nNode = nList.item(i);
				if(nNode.getNodeType() == Node.ELEMENT_NODE)
				{
					Element eElement = (Element) nNode;
					String shortName = eElement.getElementsByTagName("ShortName").item(0).getTextContent();
					String scope = eElement.getElementsByTagName("Scope").item(0).getTextContent();
					Metric metric = new Metric(shortName,
							eElement.getElementsByTagName("Name").item(0).getTextContent(),
							eElement.getElementsByTagName("Description").item(0).getTextContent(),
							eElement.getElementsByTagName("Type").item(0).getTextContent(),
							scope,
							-1.0);
					//Add 'c' or 'm' to the shortname of the metric to distinguish between class scope metrics and model scope metrics
					metricData.put((scope.equals("Model")?"m":"c") + shortName, metric);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
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
		//get the meta class containing class scope metric definitions
		MClass mettricCls = metaSystem.model().getClass("ClassMetrics");
		for(MOperation op: mettricCls.operations())
		{
			double value=-1.0;
			//the OCL expression to retrieve a class metric value
			String metricRetrievalExpr = cls.name()+ "Class.metrics." + op.name() + "()";
			Expression expr = Util.compileMetaOCLExpr(metaSystem, metricRetrievalExpr);
			//evaluate the expression on the metamode level to get the metric value
			if(expr != null)
			{
				evaluator = new Evaluator(true);
	            Value val = evaluator.eval(expr, metaSystem.state(),metaSystem.varBindings());
	            if(val.isInteger() || val.isReal())
	            	value = Double.parseDouble(val.toString());
			}	
			//get the additional info of the metric. 
			//Add 'c' to the shortname of the metric to distinguish between class scope metrics and model scope metrics
			Metric metric = metricData.get("c"+op.name());
			if(metric == null)
				metric = new Metric(op.name(),"","","","Class",value);
			else metric.setValue(value);
			//add the metric to the result
			metrics.add(metric);
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
		MClass mettricCls = metaSystem.model().getClass("ModelMetrics");
		for(MOperation op: mettricCls.operations())
		{
			double value=-1.0;
			//the OCL expression to retrieve a class metric value
			String metricRetrievalExpr = "ModelMetrics." + op.name() + "()";
			Expression expr = Util.compileMetaOCLExpr(metaSystem, metricRetrievalExpr);
			//evaluate the expression on the metamode level to get the metric value
			if(expr != null)
			{
				evaluator = new Evaluator(true);
	            Value val = evaluator.eval(expr, metaSystem.state(),metaSystem.varBindings());
	            if(val.isInteger() || val.isReal())
	            	value = Double.parseDouble(val.toString());
			}
			//get the additional info of the metric
			//Add 'm' to the shortname of the metric to distinguish between class scope metrics and model scope metrics
			metric = metricData.get("m"+op.name());
			if(metric == null)
				metric = new Metric(op.name(),"","","","Model",value);
			else metric.setValue(value);
			//add the metric to the result 
			metrics.add(metric);
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
/*
 * data class for Metric
 */
class Metric{
	/**
	 * @param shortName
	 * @param name
	 * @param description
	 * @param type
	 * @param scope
	 * @param value
	 */
	public Metric(String shortName, String name, String description, String type, String scope, double value) {
		super();
		this.shortName = shortName;
		this.name = name;
		this.description = description;
		this.type = type;
		this.scope = scope;
		this.value = value;
	}
	private String shortName;
	private String name;
	private String description;
	private String type;
	private String scope;
	private double value;
	
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
	public double getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}
	
}
