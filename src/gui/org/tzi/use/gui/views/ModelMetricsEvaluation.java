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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.parser.ocl.OCLCompiler;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.MultiplicityViolationException;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MSystem;

/**
 * TODO
 * @author KHANHHOANG
 *
 */
public class ModelMetricsEvaluation extends JPanel implements View{
	
	private Evaluator evaluator;
	private JTable tblMetricsEvaluation;
	MetricsEvaluationTableModel tableModel = new MetricsEvaluationTableModel();
	
	/**
	 * Create the panel.
	 */
	
	public ModelMetricsEvaluation(final MainWindow parent, MSystem metaSystem) {
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnLoadConfigFile = new JButton("Load the Setting");
		btnLoadConfigFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try{
					String readLine = null;
					String evaluationInv = "";
					
					
					File confFile = new File("E:\\test.txt");
					FileReader reader = new FileReader(confFile);
					@SuppressWarnings("resource")
					BufferedReader bufReader = new BufferedReader(reader);
					
					List<MetricEvaluation> configList = new ArrayList<MetricEvaluation>();
					while((readLine = bufReader.readLine()) != null)
					{
						String[] data = readLine.split("\\s+");
						MetricEvaluation config = new MetricEvaluation();
						config.setScope(data[0].trim().equals("0")?"Class":"Model");
						config.setName(data[1]);
						try{
							config.setMinValue(Double.parseDouble(data[2]));
							config.setMaxValue(Double.parseDouble(data[3]));
						}
						catch(NumberFormatException ex)
						{
							
						}
						evaluationInv = generateEvaluationInvariant(config);
						String errFilename = "E:\\err.txt";
						PrintWriter out = new PrintWriter(errFilename);

				        
				        // compile invariant
				        Expression expr = OCLCompiler.compileExpression(
				        		metaSystem.model(),
				        		metaSystem.state(),
				        		evaluationInv, 
				                "Error", 
				                out, 
				                metaSystem.varBindings());
				        	        
				        out.flush();
				        
				        try {
				            // evaluate it with current system state
				            evaluator = new Evaluator(true);
				            Value val = evaluator.eval(expr, metaSystem.state(), metaSystem.varBindings());
				            // print result
				            config.setSatisfaction(Boolean.parseBoolean(val.toString()));
				        } catch (MultiplicityViolationException e) {
				            
				        }
				        configList.add(config);
					}
					
					tableModel.setList(configList);
					
				}catch(IOException ex){}
			}
		});
		
		tblMetricsEvaluation = new JTable();
		
		tblMetricsEvaluation.setModel(tableModel);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		for(int i=0;i<tblMetricsEvaluation.getColumnCount();i++)
			tblMetricsEvaluation.getColumnModel().getColumn(i).setCellRenderer( centerRenderer );
		
		//A JTable must be in a JScrollPane so that the Header will be shown
		JScrollPane scrollPane = new JScrollPane(tblMetricsEvaluation, 
                                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(400, 300));
		add(scrollPane);

		add(btnLoadConfigFile);

	}
	private String generateEvaluationInvariant(MetricEvaluation config)
	{
		String inv = "";
		String con =""; //condition for every class
		String tmp;
		if(config.getScope()=="Class") //if the setting is for class scope metric
		{
			inv = "Class.allInstances()->forAll(c|";
			tmp =  "c.metrics." + config.getName() + "()";
			con = (config.getMinValue()==-1 && config.getMaxValue()==-1)?"1=1":config.getMinValue()==-1?tmp + "<=" + config.getMaxValue():
				config.getMaxValue()==-1?tmp + ">=" + config.getMinValue(): tmp + ">=" + config.getMinValue() + " and " + tmp + "<=" + config.getMaxValue();
			inv += con +")";
		}
		else //if the setting is for model scope metric
		{
			tmp = "ModelMetrics." + config.getName() + "()";
			inv = (config.getMinValue()==-1 && config.getMaxValue()==-1)?"1=1":config.getMinValue()==-1?tmp + "<=" + config.getMaxValue():
				config.getMaxValue()==-1?tmp + ">=" + config.getMinValue(): tmp + ">=" + config.getMinValue() + " and " + tmp + "<=" + config.getMaxValue();
		}
		return inv;
	}
	@Override
	public void detachModel() {
		// TODO Auto-generated method stub
		
	}
}
	
class MetricEvaluation{
	private String scope; //Class or Model
	private String name;
	private Double minValue; //-1 if no minimum value
	private Double maxValue;//-1 if no maximum value
	private Boolean satisfaction;
	
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
	 * @return the minValue
	 */
	public Double getMinValue() {
		return minValue;
	}
	/**
	 * @param minValue the minValue to set
	 */
	public void setMinValue(Double minValue) {
		this.minValue = minValue;
	}
	/**
	 * @return the maxValue
	 */
	public Double getMaxValue() {
		return maxValue;
	}
	/**
	 * @param maxValue the maxValue to set
	 */
	public void setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
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
class MetricsEvaluationTableModel extends AbstractTableModel {
    private List<MetricEvaluation> list = new ArrayList<MetricEvaluation>();
	private final String[] columnNames = { "Scope", "Metric", "Min Value", "Max Value", "Satisfied" };
    private final int[] columnWidths =   {  100,         100,       50,         50,          100};

    public void setList(List<MetricEvaluation> data) {
        this.list = data;
        fireTableDataChanged();
    }
    
    @Override
	public String getColumnName(int col) {
        return columnNames[col];
    }
    public int getColumnWidth(int col){
    	return columnWidths[col];
    }
    public int getRowCount() {
        return list.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0:
            return list.get(rowIndex).getScope();
        case 1:
            return list.get(rowIndex).getName();
        case 2:
            return list.get(rowIndex).getMinValue();
        case 3:
            return list.get(rowIndex).getMaxValue();
        case 4:
            return list.get(rowIndex).getSatisfaction();
        default:
            return null;
        }
    }
    
}
