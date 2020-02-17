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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.gui.views.View;
import org.tzi.use.main.Session;
import org.tzi.use.uml.sys.MSystem;
/**
 * TODO
 * @author Khanh-Hoang Doan
 *
 */
public class QualityPropertiesEval extends JPanel implements View {

	private MSystem metaSystem;
	
	private JTable tblPropertiesEval;
	private JButton btnAddNew;
	private JButton btnRefresh;
	private PropertyEvaluationTableModel tableModel = new PropertyEvaluationTableModel();
	
	public QualityPropertiesEval(final MainWindow parent, final Session fSession) {
		metaSystem = fSession.metaSystem();
		
		setLayout(new BorderLayout());	
		
		//----Display list of the properties and the corresponding evaluation results on a JTable
		tblPropertiesEval = new JTable();
		tblPropertiesEval.setModel(tableModel);
		
		tblPropertiesEval.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = tblPropertiesEval.getSelectedRow();
				//get the corresponding selected row in the sorted Table model
				int dataRow = tblPropertiesEval.convertRowIndexToModel(row);
				Boolean evalValue = ((PropertyEvaluationTableModel)tblPropertiesEval.getModel()).getDataItem(dataRow).getEvaluation();
				
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && row >= 0 && evalValue){
					QualityEvaluationDetailView dlg = 
							new QualityEvaluationDetailView(fSession, parent, tableModel.getDataItem(dataRow));
		            dlg.setVisible(true);
				}
			}
		});
		
		List<QualityProperty> propertyList = MetricAPI.loadPropertyLibrary(metaSystem);
		tableModel.setList(propertyList);
		
		//A JTable must be in a JScrollPane so that the Header will be shown
		JScrollPane scrollPane = new JScrollPane(tblPropertiesEval, 
                                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		
		scrollPane.setPreferredSize(new Dimension(750,450));
		
		tblPropertiesEval.setPreferredScrollableViewportSize(new Dimension(350, 450));
		
		// center align the satisfiability column (column 3)
		TableColumn column = tblPropertiesEval.getColumnModel().getColumn(3);		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
      	renderer.setHorizontalAlignment(JLabel.CENTER);
      	column.setCellRenderer(renderer);
      	
      	//set the table column widths
      	int[] columnWidths = {300,200,100,150};
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
		
		//summary info panel
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS) );
		//print summary info, i.e., number of the unsatisfiable properties
		final JCheckBox ckViewUnsatisfiedOnly = new JCheckBox("View only violated smells");
		ckViewUnsatisfiedOnly.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(ckViewUnsatisfiedOnly.isSelected())
					sorter.setRowFilter(RowFilter.regexFilter("true", 3));
				else
					sorter.setRowFilter(null);
			}
		});
		infoPanel.add(ckViewUnsatisfiedOnly);
		infoPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		
		JLabel lblInfo = new JLabel();
		lblInfo.setText("<html>Model evaluation result: " + tableModel.getNumofFailures() + " quality smell(s) found. Double click on the corresponding smell row to see the detail.</html>");
		lblInfo.setForeground(Color.RED);
		infoPanel.add(lblInfo);
		
		
		bottomPanel.add(infoPanel,BorderLayout.NORTH);
		
		//button Addnew
		JPanel btnPanel = new JPanel(new FlowLayout());
		btnAddNew = new JButton("Add New Smell Definition");
		btnAddNew.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				AddNewQualityProperty dlg = new AddNewQualityProperty(fSession, parent);
				dlg.setVisible(true);
			}
		});
		btnAddNew.setBackground(Color.WHITE);
		btnPanel.add(btnAddNew);
		
		//button Refresh
		btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				List<QualityProperty> propertyList = MetricAPI.loadPropertyLibrary(metaSystem);
				tableModel.setList(propertyList);
			}
		});
		btnRefresh.setBackground(Color.WHITE);
		btnPanel.add(btnRefresh);
		bottomPanel.add(btnPanel,BorderLayout.CENTER);
				
		add(bottomPanel, BorderLayout.SOUTH);
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
 * The Table model for metrics evaluation
 */
@SuppressWarnings("serial")
class PropertyEvaluationTableModel extends AbstractTableModel {
    private List<QualityProperty> list = new ArrayList<QualityProperty>();
	private final String[] columnNames = { "Design Smell", "Type", "Severity", "Evaluation" };

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
        	return list.get(rowIndex).getSeverity();
        case 3:
        	StringBuilder text = new StringBuilder();
        	text.append("<html><font color='");
        	if(list.get(rowIndex).getIsValidOclExpression())
        	{
	        	if(!list.get(rowIndex).getEvaluation())
	        		text.append("green");
	        	else
	        		text.append("red");
	        	text.append("'>");
	        	text.append(list.get(rowIndex).getEvaluation().toString() + "</font></html>");
        	}
        	else
        	{
        		text.append("gray'>");
        		text.append("Can not evaluate the property!" + "</font></html>");
        	}
        		     	
			return text.toString();
        default:
            return null;
        }
    }
    //return the number of unsatisfiable properties
    public int getNumofFailures(){
    	int numOfFailures =0;
    	for(int rowIndex=0; rowIndex<list.size();rowIndex++)
    		if(getDataItem(rowIndex).getIsValidOclExpression() && getDataItem(rowIndex).getEvaluation())
    			numOfFailures++;
    	return numOfFailures;
    			
    }
    //return the number of invalid properties (invalid OCL exps or non-boolean exps)
    public int getNumofInvalidExps(){
    	int numOfInvalideExps =0;
    	for(int rowIndex=0; rowIndex<list.size();rowIndex++)
    		if(!getDataItem(rowIndex).getIsValidOclExpression())
    			numOfInvalideExps++;
    	return numOfInvalideExps;
    			
    }
}
