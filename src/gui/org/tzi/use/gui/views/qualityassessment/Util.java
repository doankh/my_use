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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import org.tzi.use.parser.ocl.OCLCompiler;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.sys.MSystem;

/**
 * TODO
 * @author Khanh-Hoang Doan
 *
 */
public class Util {
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
}
