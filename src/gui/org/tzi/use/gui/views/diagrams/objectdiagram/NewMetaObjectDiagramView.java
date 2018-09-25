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

package org.tzi.use.gui.views.diagrams.objectdiagram;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.sys.MLink;
import org.tzi.use.uml.sys.MSystem;

/**
 * TODO
 * @author Khanh-Hoang Doan
 *
 */
@SuppressWarnings("serial")
public class NewMetaObjectDiagramView extends NewObjectDiagramView {
	public NewMetaObjectDiagramView(MainWindow mainWindow, MSystem system){
		super(mainWindow,system);
		
		hideDerivedLinks();
		
		hideMetricsObjects();
	}
	//Hide metrics meta objects
	private void hideMetricsObjects(){
		MClass cls;
		for(String str: new HashSet<String>(Arrays.asList("ClassMetrics","ModelMetrics")))
		{
			cls = system().model().getClass(str);
			if(cls != null)
				this.fObjectDiagram.hideObjects(system().state().objectsOfClass(cls));
		
		}
	}
	//Hide derived links
	private void hideDerivedLinks(){
		for (MAssociation assoc : system().model().associations()) {
			Set<MLink> links = system().state().linksOfAssociation(assoc).links();
			for (MLink link : links) {
				if (link.association().isDerived() || link.association().isUnion()) {
					fObjectDiagram.hideLink(link);
				}
			}
		}
		
	}
}
