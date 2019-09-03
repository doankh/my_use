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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.tzi.use.parser.ocl.OCLCompiler;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.ocl.expr.Expression;
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
public class MetricAPI {
	//**public static const**
	public static Path preDefinedMetricXMLFile = Paths.get(System.getProperty("user.dir")).resolve("metamodels").resolve("PreDefinedMetrics.xml");
	public static Path userDefinedMetricXMLFile = Paths.get(System.getProperty("user.dir")).resolve("metamodels").resolve("UserDefinedMetrics.xml");;
	public static Path designSmellXMLFile = Paths.get(System.getProperty("user.dir")).resolve("metamodels").resolve("DesignSmells.xml");;
	
	//**public static methods**
	public static Expression compileMetaOCLExpr(MSystem metaSystem, String oclExpression){
		String errFilename = Paths.get(System.getProperty("user.dir")).resolve("OCLEvaluationLog.txt").toAbsolutePath().toString();
		
		PrintWriter out;
		try {
			out = new PrintWriter(errFilename);
	  
	        // compile invariant
	        Expression expr = OCLCompiler.compileExpression(
	        		metaSystem.model(),
	        		metaSystem.state(),
	        		oclExpression, 
	                "Error", 
	                out, 
	                metaSystem.varBindings());
	        	        
	        out.flush();
	        return expr;
		} catch (IOException e) {
			return null;
		}
	}
	
	//**public static methods**
		public static Expression compileMetaOCLExpr1(MSystem metaSystem, String oclExpression){
			String errFilename = Paths.get(System.getProperty("user.dir")).resolve("OCLEvaluationLog.txt").toAbsolutePath().toString();
			
			PrintWriter out;
			try {
				out = new PrintWriter(errFilename);
		  
		        // compile invariant
		        Expression expr = OCLCompiler.compileExpression(
		        		metaSystem.model(),
		        		oclExpression, 
		                "Error", 
		                out, 
		                metaSystem.varBindings());
		        	        
		        out.flush();
		        return expr;
			} catch (IOException e) {
				return null;
			}
		}
	
	/**
	 * Read data of the pre-defined metrics from a XML file
	 */
	public static HashMap<String,Metric> loadMetricDatafromXMLFile(Path xmlFilePath) {
		File xmlFile = xmlFilePath.toFile();
		HashMap<String,Metric> metricData = new HashMap<String,Metric>();
		try {
			//Path homeDir = Paths.get(System.getProperty("user.dir")); 
			//File xmlFile = homeDir.resolve("metamodels").resolve("Metrics.xml").toFile();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			NodeList nList = doc.getElementsByTagName("Metric");
			for(int i=0; i<nList.getLength(); i++)
			{				
				Node nNode = nList.item(i);
				if(nNode.getNodeType() == Node.ELEMENT_NODE)
				{
					try{
						Element eElement = (Element) nNode;
						String shortName = eElement.getElementsByTagName("ShortName").item(0).getTextContent();
						String scope = eElement.getElementsByTagName("Scope").item(0).getTextContent();
						Metric metric = new Metric(shortName,
								eElement.getElementsByTagName("Name").item(0).getTextContent(),
								eElement.getElementsByTagName("Description").item(0).getTextContent(),
								eElement.getElementsByTagName("Type").item(0).getTextContent(),
								scope,
								eElement.getElementsByTagName("Definition").item(0).getTextContent());
						//Add 'c' or 'm' to the shortname of the metric to distinguish between class scope metrics and model scope metrics
						metricData.put((scope.equals("Model")?"m":"c") + shortName, metric);
					}
					catch(Exception e){}
				}
			}
			return metricData;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
		
	}
	//check whether a string is a numberic 
	public static boolean isNumeric(String str) { 
	  try {  
	    Double.parseDouble(str);  
	    return true;
	  } catch(NumberFormatException e){  
	    return false;  
	  }  
	}
	
	//get the list of all classes in the user model
	public static List<String> getUserModelClassList(MModel model){
		List<String> classes = new ArrayList<String>();
		for(MClass cls: model.classes())
			classes.add(cls.name());
		return classes;
	}
	
	
}
