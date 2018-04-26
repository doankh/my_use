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

import org.tzi.use.uml.ocl.expr.VarDecl;
import org.tzi.use.uml.ocl.type.Type;

/**
 * A parameter of a class operation
 * @author KHANHHOANG
 *
 */
public class MParameter extends MModelElementImpl {
	
	private MOperation fOwner;
	
	private VarDecl decl;
	/**
	 * @param name
	 */
	protected MParameter(VarDecl dec, MOperation owner) {
		super(dec.name());
		decl = dec;
		fOwner = owner;
	}
	
	public VarDecl getDeclaration()
	{
		return decl;
	}
	
    public Type type() {
        return decl.type();
    }
	
	public MOperation owner()
	{
		return fOwner;
	}
	
    public String qualifiedName(){
    	return fOwner.cls().name() + "::" + fOwner.name() + "::" + name();
    }
	
	@Override
	public void processWithVisitor(MMVisitor v) {
        v.visitParameter(this);
    }	
}
