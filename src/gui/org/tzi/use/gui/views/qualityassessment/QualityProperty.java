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

/**
 * TODO
 * @author Khanh-Hoang Doan
 *
 */
/*
 * Data class for a quality property definition 
 */
public class QualityProperty{
	private String name;
	private String desc;
	private String type;
	private String severity;
	private String oclExpression;
	private String selectExpression;
	private String context;
	private Boolean evaluation;
	private Boolean isValidOclExpression;
	
	public QualityProperty(String sName, String sDecs, String sType, String sSeverity, String sOclExpression, String sSelectExpression, String sContext){
		this.name = sName;
		this.desc = sDecs;
		this.type = sType;
		this.severity = sSeverity;
		this.oclExpression = sOclExpression;
		this.selectExpression = sSelectExpression;
		this.context = sContext;
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
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
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
	 * @return the severity
	 */
	public String getSeverity() {
		return severity;
	}

	/**
	 * @param name the name to set
	 */
	public void setSeverity(String severity) {
		this.severity = severity;
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
	 * @return the selectExpression
	 */
	public String getSelectExpression() {
		return selectExpression;
	}

	/**
	 * @param selectExpression the selectExpression to set
	 */
	public void setSelectExpression(String selectExpression) {
		this.selectExpression = selectExpression;
	}

	/**
	 * @return the violatingElement
	 */
	public String getContext() {
		return context;
	}

	/**
	 * @param violatingElement the violatingElement to set
	 */
	public void setContext(String context) {
		this.context = context;
	}

	/**
	 * @return the satisfaction
	 */
	public Boolean getEvaluation() {
		return evaluation;
	}

	/**
	 * @param satisfaction the satisfaction to set
	 */
	public void setEvaluation(Boolean eval) {
		this.evaluation = eval;
	}

	/**
	 * @return the isValidOclExpression
	 */
	public Boolean getIsValidOclExpression() {
		return isValidOclExpression;
	}

	/**
	 * @param isValidOclExpression the isValidOclExpression to set
	 */
	public void setIsValidOclExpression(Boolean isValidOclExpression) {
		this.isValidOclExpression = isValidOclExpression;
	}
		
}
