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
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.gui.views.View;
import org.tzi.use.main.Session;
import org.tzi.use.parser.ocl.OCLCompiler;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.MultiplicityViolationException;
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
public class QualityPropertiesEval extends JPanel implements View {

	private MSystem metaSystem;
	private Evaluator evaluator;
	private JTable tblPropertiesEval;
	private PropertyEvaluationTableModel tableModel = new PropertyEvaluationTableModel();
	
	public QualityPropertiesEval(final MainWindow parent, final Session fSession) {
		metaSystem = fSession.metaSystem();
		
		setLayout(new BorderLayout());		
		
		//Display list of the properties and the corresponding evaluation results on a JTable
		tblPropertiesEval = new JTable();
		tblPropertiesEval.setModel(tableModel);
		
		List<QualityProperty> propertyList = loadPropertyLibrary();
		tableModel.setList(propertyList);
		
		tblPropertiesEval.removeColumn(tblPropertiesEval.getColumnModel().getColumn(2));
		//A JTable must be in a JScrollPane so that the Header will be shown
		JScrollPane scrollPane = new JScrollPane(tblPropertiesEval, 
                                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		
		scrollPane.setPreferredSize(this.getPreferredSize());
		
		tblPropertiesEval.setPreferredScrollableViewportSize(new Dimension(350, 450));
		
		// center align the satisfiability column
		TableColumn column = tblPropertiesEval.getColumnModel().getColumn(2);		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
      	renderer.setHorizontalAlignment(JLabel.CENTER);
      	column.setCellRenderer(renderer);
      	
      	//set the table column widths
      	int[] columnWidths = {200,100,50,100};
      	setTableColumnWidths(columnWidths);
      	
      	//sort the list of properties
      	TableRowSorter<TableModel> sorter = new TableRowSorter<>(tblPropertiesEval.getModel());
      	tblPropertiesEval.setRowSorter(sorter);
      	List<RowSorter.SortKey> sortKeys = new ArrayList<>();      	 
      	sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
      	sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));    	 
      	sorter.setSortKeys(sortKeys);
      	sorter.sort();
      	//----------------
      	
		add(scrollPane, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel(new BorderLayout());
		//print summary info, i.e., number of the unsatisfiable properties
		JLabel lblInfo = new JLabel();
		lblInfo.setText("Model evaluation result: " + tableModel.getNumofFailures() + " property(ies) failed.");
		lblInfo.setForeground(Color.RED);
		bottomPanel.add(lblInfo,BorderLayout.CENTER);		
		add(bottomPanel, BorderLayout.SOUTH);
	}
	
	//Load property library defined in an XML file
	public List<QualityProperty> loadPropertyLibrary(){
		List<QualityProperty> result = new ArrayList<QualityProperty>();
		try {
			Path homeDir = Paths.get(System.getProperty("user.dir")); 
			File xmlFile = homeDir.resolve("metamodels").resolve("QualityProperties.xml").toFile();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			NodeList nList = doc.getElementsByTagName("Property");
			for(int i=0; i<nList.getLength(); i++)
			{
				QualityProperty property = new QualityProperty();
				Node nNode = nList.item(i);
				if(nNode.getNodeType() == Node.ELEMENT_NODE)
				{
					Element eElement = (Element) nNode;
					property.setName(eElement.getElementsByTagName("Name").item(0).getTextContent());
					property.setType(eElement.getElementsByTagName("Type").item(0).getTextContent());
					property.setOclExpression(eElement.getElementsByTagName("OClExpression").item(0).getTextContent());
					
					String errFilename = homeDir.resolve("metamodels").resolve("QualityPropertiesEvalLog.txt").toAbsolutePath().toString();
					
					PrintWriter out = new PrintWriter(errFilename);

			        
			        // compile invariant
			        Expression expr = OCLCompiler.compileExpression(
			        		metaSystem.model(),
			        		metaSystem.state(),
			        		property.getOclExpression(), 
			                "Error", 
			                out, 
			                metaSystem.varBindings());
			        	        
			        out.flush();
			        
			        try {
			            // evaluate it with current system state
			            evaluator = new Evaluator(true);
			            Value val = evaluator.eval(expr, metaSystem.state(), metaSystem.varBindings());
			            // print result
			            property.setSatisfaction(Boolean.parseBoolean(val.toString()));
			        } catch (MultiplicityViolationException e) {
			            
			        }
				}
				result.add(property);
			}
			
		} catch (Exception e) {
	         e.printStackTrace();}
		return result;
	}

	private void setTableColumnWidths(int[] columnWidths) {
	    TableColumnModel columnModel = tblPropertiesEval.getColumnModel();
	    for (int i = 0; i < columnModel.getColumnCount(); i++) {       
	            columnModel.getColumn(i).setPreferredWidth(columnWidths[i]);
	    }
	}
	@Override
	public void detachModel() {
		// TODO Auto-generated method stub
		
	}
}

/*
 * Data class for a quality property definition 
 */
class QualityProperty{
	private String name;
	private String type;
	private String oclExpression;
	private Boolean satisfaction;
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
	 * @return the oclExpression
	 */
	public String getOclExpression() {
		return oclExpression;
	}
	/**
	 * @param oclExpression the oclExpression to set
	 */
	public void setOclExpression(String oclExpression) {
		this.oclExpression = oclExpression;
	}
	/**
	 * @return the satisfaction
	 */
	public Boolean getSatisfaction() {
		return satisfaction;
	}
	/**
	 * @param satisfaction the satisfaction to set
	 */
	public void setSatisfaction(Boolean satisfaction) {
		this.satisfaction = satisfaction;
	}
	
}

/*
 * The Table model for metrics evaluation
 */
@SuppressWarnings("serial")
class PropertyEvaluationTableModel extends AbstractTableModel {
    private List<QualityProperty> list = new ArrayList<QualityProperty>();
	private final String[] columnNames = { "Name", "Type", "OCL Expression", "Satisfiability" };

    public void setList(List<QualityProperty> data) {
        this.list = data;
        fireTableDataChanged();
    }
    
    public QualityProperty getDataItem(int row){
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
            return list.get(rowIndex).getName();
        case 1:
            return list.get(rowIndex).getType();
        case 2:
        	return list.get(rowIndex).getOclExpression();
        case 3:
        	StringBuilder text = new StringBuilder();
        	text.append("<html><font color='");
        	if(list.get(rowIndex).getSatisfaction())
        		text.append("green");
        	else
        		text.append("red");
        	text.append("'>");
        	text.append(list.get(rowIndex).getSatisfaction().toString() + "</font></html>");
			return text.toString();
        default:
            return null;
        }
    }
    
    public int getNumofFailures(){
    	int numOfFailures =0;
    	for(int rowIndex=0; rowIndex<list.size();rowIndex++)
    		if(!list.get(rowIndex).getSatisfaction())
    			numOfFailures++;
    	return numOfFailures;
    			
    }
}
