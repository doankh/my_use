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

package org.tzi.use.uml.sys;

import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.ocl.value.Value;

/**
 * TODO
 * @author Khanh-Hoang Doan
 *
 */
public interface SSVisitor {
	void visitSystemState();
	void visitObject(MObject object);
	void visitAttribute(MAttribute attribute, String parentObjectInstanceName);
	void visitLink(MLink link);
	void visitRole(MLinkEnd role, String linkInstanceName);
	/**
	 * @param attr
	 * @param value
	 */
	void visitValue(Value value, MAttribute attr, String parentObjectInstanceName);
}
