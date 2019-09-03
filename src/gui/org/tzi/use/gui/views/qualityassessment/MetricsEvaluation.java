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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.tzi.use.config.Options;
import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.gui.util.ExtFileFilter;
import org.tzi.use.gui.views.View;
import org.tzi.use.main.Session;
import org.tzi.use.uml.mm.MMetricEvaluationSetting;
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
public class MetricsEvaluation extends JPanel implements View{
	
	private MSystem metaSystem;
	private Evaluator evaluator;
	private JTable tblMetricsEvaluation;
	private JLabel lblInfo;
	private JButton btnLoadConfigFile;
	MetricsEvaluationTableModel tableModel = new MetricsEvaluationTableModel();
	
	/**
	 * Create the panel.
	 */
	
	public MetricsEvaluation(final MainWindow parent, final Session fSession) {
		
		metaSystem = fSession.metaSystem();
		setLayout(new BorderLayout());
		
		btnLoadConfigFile = new JButton("Load the Setting");
		btnLoadConfigFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try{
					String readLine = null;
					String evaluationInv = "";
					//Open the file browser dialog
					JFileChooser fChooser = new JFileChooser(Options.getLastDirectory().toFile());
		            ExtFileFilter filter = new ExtFileFilter("met", "Metrics configuration");
		            fChooser.setFileFilter(filter);
		            fChooser.setDialogTitle("Open the metric configuration file ...");
		            		            
		            int returnVal = fChooser.showOpenDialog(MetricsEvaluation.this);
		            if (returnVal != JFileChooser.APPROVE_OPTION)
		                return;

		            Path path = fChooser.getCurrentDirectory().toPath();
		            Options.setLastDirectory(path);
		            Path f = fChooser.getSelectedFile().toPath();
		            File confFile = new File(f.toAbsolutePath().toString());
		            
					//File confFile = new File("E:\\test.txt");
					FileReader reader = new FileReader(confFile);
					@SuppressWarnings("resource")
					BufferedReader bufReader = new BufferedReader(reader);
					
					List<MMetricEvaluationSetting> configList = new ArrayList<MMetricEvaluationSetting>();
					int settingNo=0;
					
					//check error while reading the configuration file. print the info to the log window
					boolean checkError = true;
					parent.logWriter().flush();
					parent.logWriter().println("---------------------------------------");
					parent.logWriter().println("Read the metric threshold configuration: " + confFile.getName());
					while((readLine = bufReader.readLine()) != null)
					{
						String[] data = readLine.split("\\s+");
						//Check the validity of the setting
						settingNo++;
						if(checkMetricSetting(settingNo, data, parent.logWriter()))
						{
							MMetricEvaluationSetting config = new MMetricEvaluationSetting();
							config.setScope(data[0].trim().equals("0")?"Class":"Model");
							config.setName(data[1]);
							try{
								config.setMinValue(Double.parseDouble(data[2]));
								config.setMaxValue(Double.parseDouble(data[3]));
							}
							catch(NumberFormatException ex)
							{
								
							}
							evaluationInv = config.createEvaluationInvariant();
							
							Expression expr = MetricAPI.compileMetaOCLExpr(metaSystem, evaluationInv);
							if(expr!= null)
						        try {
						            // evaluate it with current system state
						            evaluator = new Evaluator(true);
						            Value val = evaluator.eval(expr, metaSystem.state(), metaSystem.varBindings());
						            // print result
						            config.setSatisfaction(Boolean.parseBoolean(val.toString()));
						            configList.add(config);
						        } catch (MultiplicityViolationException e) {
						        	 
						        }
							else
							{
								checkError = false;
								parent.logWriter().println("Setting " + settingNo + ": unable to evaluate!" );
							}
						}
						else
							checkError = false;
					}
					tableModel.setList(configList);
					lblInfo.setText("<html>Model evaluation result: " + tableModel.getNumofFailures() + " setting failed. </html>");
					if(!checkError)
						JOptionPane.showMessageDialog(null,"Errors while loading the configuration file. See the log window for details!");
					else
						parent.logWriter().println("no errors!");
										
				}catch(IOException ex){}
			}
		});
		
		tblMetricsEvaluation = new JTable();
		
		tblMetricsEvaluation.setModel(tableModel);
		
		tblMetricsEvaluation.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = tblMetricsEvaluation.getSelectedRow();
				Boolean value = ((MetricsEvaluationTableModel)tblMetricsEvaluation.getModel()).getDataItem(row).getSatisfaction();
				String scope = tblMetricsEvaluation.getModel().getValueAt(row, 0).toString();
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && row >= 0 && scope.equals("Class") && !value){
					MetricEvaluationDetailedView dlg = 
							new MetricEvaluationDetailedView(fSession, parent, tableModel.getDataItem(row));
		            dlg.setVisible(true);
				}
			}
		});
		
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		for(int i=0;i<tblMetricsEvaluation.getColumnCount();i++)
			tblMetricsEvaluation.getColumnModel().getColumn(i).setCellRenderer( centerRenderer );
		
		//A JTable must be in a JScrollPane so that the Header will be shown
		JScrollPane scrollPane = new JScrollPane(tblMetricsEvaluation, 
                                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(500, 500));
		
		add(scrollPane, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel(new BorderLayout());
		//print summary info, i.e., number of the unsatisfiable setting
		
		//lblInfo.setText("<html>Model evaluation result: " + tableModel.getNumofFailures() + " setting failed. </html>");
		lblInfo = new JLabel();
		lblInfo.setForeground(Color.RED);
		bottomPanel.add(lblInfo,BorderLayout.NORTH);
		
		bottomPanel.add(btnLoadConfigFile,BorderLayout.EAST);
		btnLoadConfigFile.setBackground(Color.WHITE);
		add(bottomPanel, BorderLayout.SOUTH);

	}
	
	//check the error while reading a metric threshold setting
	boolean checkMetricSetting(int settingNo, String[] setting, PrintWriter fLogWriter)
	{
		boolean result = true;
		String prefix = "Setting " + settingNo + ": ";
		if(setting.length != 4)
		{
			result = false;
			fLogWriter.println(prefix + "Must include 4 parameters: Metric scope(0/1), Name of the Metric, Lower threshold, Upper threshold. Parameters are separated by a space");
		}
		else
		{
			if(!setting[0].trim().equals("0") && !setting[0].trim().equals("1"))
			{
				result = false;
				fLogWriter.println(prefix + "Metric scope parameter must be 0 or 1");
			}
			if(!MetricAPI.isNumeric(setting[2]))
			{
				result = false;
				fLogWriter.println(prefix + "The lower threshold must be a number");
			}
			if(!MetricAPI.isNumeric(setting[3]))
			{
				result = false;
				fLogWriter.println(prefix + "The upper threshold must be a number");
			}
		}
		return result;
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
class MetricsEvaluationTableModel extends AbstractTableModel {
    private List<MMetricEvaluationSetting> list = new ArrayList<MMetricEvaluationSetting>();
	private final String[] columnNames = { "Scope", "Metric", "Min Value", "Max Value", "Satisfiability" };
    private final int[] columnWidths =   {  80,         80,       50,         50,          80};

    public void setList(List<MMetricEvaluationSetting> data) {
        this.list = data;
        fireTableDataChanged();
    }
    
    public MMetricEvaluationSetting getDataItem(int row){
    	return list.get(row);
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
    @Override
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
    //return the number of unsatisfiable properties
    public int getNumofFailures(){
    	int numOfFailures =0;
    	for(int rowIndex=0; rowIndex<list.size();rowIndex++)
    		if(!list.get(rowIndex).getSatisfaction())
    			numOfFailures++;
    	return numOfFailures;
    			
    }
}
