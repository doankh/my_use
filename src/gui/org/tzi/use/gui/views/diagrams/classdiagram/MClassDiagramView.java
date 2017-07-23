/*
 * USE - UML based specification environment
 * Copyright (C) 1999-2004 Mark Richters, University of Bremen
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

// $Id: ClassDiagramView.java 1050 2009-07-07 16:25:22Z lars $

package org.tzi.use.gui.views.diagrams.classdiagram;

import java.util.ArrayList;
import java.util.Collection;

import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.sys.MSystem;

/**
 * A graph showing an class diagram with all elements in the
 * current instance of MModel
 *
 * @version $ProjectVersion: 0.393 $
 * @author Hoang Doan
 * */
@SuppressWarnings("serial")
public class MClassDiagramView extends ClassDiagramView{
	Collection<MClass> relatedMMClasses;
	Collection<MAssociation> relatedMMAssociations;
    public MClassDiagramView( MainWindow mainWindow, MSystem system, MSystem metaSystem, boolean loadLayout, boolean isSimplied) { 
    	super(mainWindow,system,metaSystem,loadLayout,true);
    	MModel mModel = metaSystem.model();
    	//hide all elements when creating the metamodel
    	if(isSimplied)//Simplied Class Diagram Metamodel
    	{
    		classDiagram().hideAll();
    		getRelatedMMClassesAndAssos();
	        for(MClass cls: relatedMMClasses)
	        	classDiagram().showClass(cls);
	        //Hide the unused associations. Is there a possible method to recognize these associations?
	        //e.g. No redefined Property --> no A_Property_Property_Property_RedefinedProperty association
	        /*classDiagram().hideAssociation(mModel.getAssociation("A_Association_Association_Property_NavigableOwnedEnd"));
	        classDiagram().hideAssociation(mModel.getAssociation("C_Association_OwningAssociation_Property_OwnedEnd"));
	        classDiagram().hideAssociation(mModel.getAssociation("C_Property_AssociationEnd_Property_Qualifier"));
	        classDiagram().hideAssociation(mModel.getAssociation("A_Property_Property_Property_Opposite"));
	        classDiagram().hideAssociation(mModel.getAssociation("A_Property_Property_Property_SubsettedProperty"));
	        classDiagram().hideAssociation(mModel.getAssociation("A_Property_Property_Property_RedefinedProperty"));
	        classDiagram().hideAssociation(mModel.getAssociation("A_Operation_Operation_Operation_RedefinedOperation"));*/
	        
    	}
    }
    /*
     * 
     */
    private void getRelatedMMClassesAndAssos()
    {
		MModel mModel = system().model();
		MModel mmModel = metaSystem().model();
		relatedMMClasses = new ArrayList<MClass>();
//		relatedMMAssociations = new ArrayList<MAssociation>();
		relatedMMClasses.add(mmModel.getClass("Class"));
		relatedMMClasses.add(mmModel.getClass("DataType"));
		Collection<MClass> allClasses = mModel.classes();
		MClass mCls = null;
		MAssociation mAsso = null;
		for(MClass cls:allClasses)
			if(!cls.attributes().isEmpty())
			{
				mCls = mmModel.getClass("Property");
//				mAsso = mmModel.getAssociation("C_Class_Class_Property_OwnedAttribute");
				if(mCls !=null) relatedMMClasses.add(mCls);
//				if(mAsso!=null)	relatedMMAssociations.add(mAsso);
				break;
			}
		for(MClass cls:allClasses)
			if(!cls.operations().isEmpty())
			{
				mCls = mmModel.getClass("Operation");
				if(mCls !=null) relatedMMClasses.add(mCls);
				/*mAsso = mmModel.getAssociation("C_Class_Class_Operation_OwnedOperation");
				if(mAsso!=null) relatedMMAssociations.add(mAsso);*/
				break;
			}
		if(!mModel.associations().isEmpty())
		{
			mCls = mmModel.getClass("Association");
			if(mCls !=null) relatedMMClasses.add(mCls);
		}
		
		if(!mModel.getAssociationClassesOnly().isEmpty())
		{
			mCls = mmModel.getClass("AssociationClass");
			if(mCls !=null) relatedMMClasses.add(mCls);
		}
		/*Iterator<MGeneralization> it = mModel.generalizationGraph().edgeIterator();
		if(it.hasNext())
		{
			mCls = mmModel.getClass("Generalization");
			if(mCls !=null)
				relatedMMClasses.add(mCls);
		}*/

    	
    }
    
}