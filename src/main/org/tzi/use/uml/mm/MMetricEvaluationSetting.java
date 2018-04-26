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

package org.tzi.use.uml.mm;

/**
 * TODO
 * @author KHANHHOANG
 *
 */
public class MMetricEvaluationSetting {
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
	
	public String createConditionExp()
	{
		String con;
		String tmp = scope =="Class"? "c.metrics." + name + "()" : "ModelMetrics." + name + "()";
		con = (minValue==-1 && maxValue==-1)?"1=1":minValue==-1?tmp + "<=" + maxValue:
			maxValue==-1?tmp + ">=" + minValue: tmp + ">=" + minValue + " and " + tmp + "<=" + maxValue;
		return con;
	}
	/*
	 * create a meta-invariant to evaluate the model based on the metric evaluation setting 
	 */
	public String createEvaluationInvariant()
	{
		if(scope =="Class") //if the setting is for class scope metric
		{
			return "Class.allInstances()->forAll(c|" + createConditionExp() + ")";
		}
		else //if the setting is for model scope metric
		{
			return createConditionExp();
		}
	}
	
	/*
	 * create a meta-query to select the violating classes that cause the setting false 
	 */
	public String createSelectQuery()
	{
		return "Class.allInstances()->select(c|not (" + createConditionExp() +"))";
	}
	public String toString()
	{
		return "Metric name:" + name + ";"+" Lower threshold:" + (minValue==-1.0?" ":minValue) 
				+ ";" + " Upper threshold:" + (maxValue==-1.0?" ":maxValue) + "\n" + "Evaluation result:" + satisfaction; 
	}
}
