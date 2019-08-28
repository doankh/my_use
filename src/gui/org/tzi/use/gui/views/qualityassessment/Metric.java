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

import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MSystem;

/**
 * TODO
 * @author Khanh-Hoang Doan
 *
 */
/*
 * data class for Metric
 */
public class Metric{
	/**
	 * @param shortName
	 * @param name
	 * @param description
	 * @param type
	 * @param scope
	 * @param value
	 */
	public Metric(String shortName, String name, String description, String type, String scope, String oclDefinition, double value) {
		super();
		this.shortName = shortName;
		this.name = name;
		this.description = description;
		this.type = type;
		this.scope = scope;
		this.oclDefinition = oclDefinition;
		this.value = value;
	}
	private String shortName;
	private String name;
	private String description;
	private String type;
	private String scope;
	private String oclDefinition;
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
	 * @return the description
	 */
	public String getOcldefinition() {
		return oclDefinition;
	}
	/**
	 * @param description the description to set
	 */
	public void setOcldefinition(String definition) {
		this.oclDefinition = definition;
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
	
	public double evaluate(MSystem metaSystem, MClass cls){
		double value=-1.0;
		Evaluator evaluator;
		//the OCL expression to retrieve a class metric value
		String metricRetrievalExpr;
		metricRetrievalExpr = this.scope.equals("Model")?
				"ModelMetrics." + this.shortName + "()"
				: cls.name()+ "Class.metrics." + this.shortName + "()";
		Expression expr = Util.compileMetaOCLExpr(metaSystem, metricRetrievalExpr);
		//evaluate the expression on the metamode level to get the metric value
		if(expr != null)
		{
			evaluator = new Evaluator(true);
            Value val = evaluator.eval(expr, metaSystem.state(),metaSystem.varBindings());
            if(val.isInteger() || val.isReal())
            	value = Double.parseDouble(val.toString());
		}
		
		return value;
	}
}
