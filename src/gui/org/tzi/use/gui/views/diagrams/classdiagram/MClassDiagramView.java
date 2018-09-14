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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tzi.use.gui.main.MainWindow;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MAssociationEnd;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MGeneralization;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.MOperation;
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
	Map<String, Set<String>> relatedMetaClasses = new HashMap<String, Set<String>>();
	Map<String, Set<String>> relatedMetaAssociations = new HashMap<String, Set<String>>();
	Map<String, Set<String>> subDiagramClasses = new HashMap<String, Set<String>>();
	Path layoutFile;
    public MClassDiagramView( MainWindow mainWindow, MSystem system, MSystem metaSystem, boolean loadLayout, boolean isSimplied, String diagramName) { 
    	super(mainWindow,metaSystem,loadLayout);
    	MModel mModel = metaSystem.model();
    	Set<String> modelElements = getModelElements(system.model());
    	
    	try{
			layoutFile = this.fClassDiagram.getOptions().getDirectory().resolve(diagramName + ".clt");
			//load corresponding layout
			this.fClassDiagram.loadLayout(layoutFile);
			}
			catch (Exception e)
			{
				//this.fClassDiagram.getLog().println("Error loading default Meta-Sub diagram. Load full diagram");
				this.fClassDiagram.resetLayout();
			}
    	
    	if(isSimplied)//Simplified Class Diagram Metamodel
    	{
			
    		initMappings();
    		Set<String> metaClassNames = new HashSet<String>();
    		Set<String> metaAssociationNames = new HashSet<String>();
    		List<MAssociation> directMetaAssociations = new ArrayList<MAssociation>();
    		Set<MClass> directMetaClasses = new HashSet<MClass>();
        	//hide all elements
    		//this.fClassDiagram.hideAll();
    		//get related meta classes/associations need to be shown
	        for (String str : modelElements)
        	{
	        	metaClassNames = relatedMetaClasses.get(str);
	        	metaAssociationNames = relatedMetaAssociations.get(str);
	        	if(metaAssociationNames != null)
	        		for(String assName : metaAssociationNames)
	        		{
	        			MAssociation tmp = mModel.getAssociation(assName);
	        			if (tmp != null)directMetaAssociations.add(tmp);
	        		}
	        			
	        	if(metaClassNames != null)
	        		for(String clsName : metaClassNames)
	        		{
	        			MClass tmp = mModel.getClass(clsName);
	        			if (tmp != null)directMetaClasses.add(tmp);
	        		}
        	}
	        
	      //Hide unneeded associations
	        Collection<MClass> allMetaClasses = mModel.classes();
	        for(MClass cls : allMetaClasses)
	        {
	        	if(!directMetaClasses.contains(cls))
	        		this.fClassDiagram.hideClass(cls);
	        }
	        
	        //Hide unneeded associations
	        Collection<MAssociation> allMetaAssociations = mModel.associations();
	        for(MAssociation ass : allMetaAssociations)
	        {
	        	if(!directMetaAssociations.contains(ass))
	        		this.fClassDiagram.hideAssociation(ass);
	        }
	        //Hide unused associations
	        hideUnusedAssociation(mModel);
		}
    }
    
    private void initSubdiagramClasses()
    {
    	subDiagramClasses.clear();
    	subDiagramClasses.put("Namespaces diagram", new HashSet<String>(Arrays.asList("Element", "NamedElement", "Namespace", "PackageableElement", "DirectedRelationship",
    										"ElementImport", "PackageableImport", "Package")));
    	subDiagramClasses.put("Classifiers diagram", new HashSet<String>(Arrays.asList("Classifier", "NamedElement", "Namespace", "RedefinableElement", "Type",
				"Feature", "StructuralFeature", "Property","Generalization","DirectedRelationship")));
    	subDiagramClasses.put("Features diagram", new HashSet<String>(Arrays.asList("Classifier", "NamedElement", "Namespace", "RedefinableElement", "Type", "TypedElement",
				"Feature", "StructuralFeature", "MultiplicityElement","Parameter", "BehavioralFeature")));
    	subDiagramClasses.put("Operations diagram", new HashSet<String>(Arrays.asList("Constraint", "NamedElement", "Namespace", "ValueSpecification", "Type", "TypedElement",
				"Parameter", "BehavioralFeature", "Operation")));
    	subDiagramClasses.put("Classes diagram", new HashSet<String>(Arrays.asList("Class", "Classifier", "Type", "Operation", "Association", "Relationship",
				"StructuralFeature", "Property","ValueSpecification")));
    	subDiagramClasses.put("DataTypes diagram", new HashSet<String>(Arrays.asList("DataType", "InstanceSpecification", "Classifier", "BehavioralFeature", "Operation",
    			"Feature", "StructuralFeature", "Property","PrimitiveType", "Enumeration","EnumerationLiteral")));
    }
    
    private void initMappings()
    {
    	relatedMetaClasses.clear();
    	relatedMetaAssociations.clear();
    	
    	relatedMetaClasses.put("Class", new HashSet<String>(Arrays.asList("Class", "Classifier")));
    	relatedMetaClasses.put("Attribute", new HashSet<String>(Arrays.asList("Property", "DataType", "StructuralFeature", "TypedElement", "Type")));
    	relatedMetaClasses.put("Association", new HashSet<String>(Arrays.asList("Association")));
    	relatedMetaClasses.put("AssociationEnd", new HashSet<String>(Arrays.asList("Property")));
    	relatedMetaClasses.put("Operation", new HashSet<String>(Arrays.asList("Operation", "Type")));
    	relatedMetaClasses.put("Parameter", new HashSet<String>(Arrays.asList("Parameter", "DataType", "StructuralFeature", "TypedElement", "Type")));
    	relatedMetaClasses.put("AssociationClass", new HashSet<String>(Arrays.asList("AssociationClass")));
    	relatedMetaClasses.put("Generalization", new HashSet<String>(Arrays.asList("Generalization")));
    	//End of meta classes mapping
    	
    	relatedMetaAssociations.put("Attribute", new HashSet<String>(Arrays.asList("C_Class_Class_Property_OwnedAttribute",
    			"A_TypedElement_TypedElement_Type_Type")));
    	relatedMetaAssociations.put("Association", new HashSet<String>(Arrays.asList("A_Association_Association_Type_EndType")));
    	relatedMetaAssociations.put("AssociationEnd", new HashSet<String>(Arrays.asList("C_Association_OwningAssociation_Property_OwnedEnd")));
    	relatedMetaAssociations.put("Operation", new HashSet<String>(Arrays.asList("C_Class_Class_Operation_OwnedOperation",
    			"A_Operation_Operation_Type_Type")));
    	relatedMetaAssociations.put("Parameter", new HashSet<String>(Arrays.asList("C_Operation_Operation_Parameter_OwnedParameter", "A_TypedElement_TypedElement_Type_Type"))); 	
    	relatedMetaAssociations.put("Generalization", new HashSet<String>(Arrays.asList("A_Class_Class_Class_SuperClass",
    			"C_Classifier_Specific_Generalization_Generalization","A_Classifier_General_Generalization_Generalization")));
    	relatedMetaAssociations.put("Redefined Attribute", new HashSet<String>(Arrays.asList("A_Property_Property_Property_RedefinedProperty")));
    	relatedMetaAssociations.put("Redefined AssociationEnd", new HashSet<String>(Arrays.asList("A_Property_Property_Property_RedefinedProperty")));
    	relatedMetaAssociations.put("Subsetted Attribute", new HashSet<String>(Arrays.asList("A_Property_Property_Property_SubsettedProperty")));
    	relatedMetaAssociations.put("Subsetted AssociationEnd", new HashSet<String>(Arrays.asList("A_Property_Property_Property_SubsettedProperty")));
    }
    
    //get all elements in the model, e.g., class, attributes, operations, . ..  
    private Set<String> getModelElements(MModel mModel)
    {
		Set<String> modelElements = new HashSet<String>();
		modelElements.add("Class");
		Collection<MClass> allClasses = mModel.classes();
		for (MClass cls : allClasses)
			if (!cls.attributes().isEmpty())
			{
				modelElements.add("Attribute");
				break;
			}
		
		Boolean ck1 = false, ck2=false, ck3 = false;
		for (MClass cls : allClasses)
			if (!cls.operations().isEmpty())
			{
				ck1=true;
				for (MOperation opc : cls.operations())
					if(!opc.paramList().isEmpty())
					{
						ck2=true;
						break;
					}
				if(ck2) break;
			}
		if(ck1)modelElements.add("Operation");
		if(ck2)modelElements.add("Parameter");
		
		ck1 = false; ck2=false; ck3 = false;
		
		if(!mModel.associations().isEmpty())
		{
			modelElements.add("Association");
			for (MAssociation ass: mModel.associations())
				if (!ass.associationEnds().isEmpty())
				{
					ck1=true;
					for (MAssociationEnd assEnd : ass.associationEnds())
					{
						if (!assEnd.getRedefiningEnds().isEmpty()) ck2 = true;
						if (!assEnd.getSubsettingEnds().isEmpty()) ck3 = true;	
					}
					if (ck2&&ck3)break;
				}
		}
		if (ck1)modelElements.add("AssociationEnd");
		if (ck2)modelElements.add("Redefined AssociationEnd");
		if (ck3)modelElements.add("Subsetted AssociationEnd");
		
		if (!mModel.getAssociationClassesOnly().isEmpty())
			modelElements.add("AssociationClass");
		
		
		Iterator<MGeneralization> it = mModel.generalizationGraph().edgeIterator();
		if (it.hasNext())
			modelElements.add("Generalization");
		
		
		return modelElements;
    }
    
    private void hideUnusedAssociation(MModel mModel)
    {
    	Set<String> unUsedAssoc = new HashSet<String>(Arrays.asList("A_Property_Property_Property_Opposite",
    			"A_Operation_Operation_Operation_RedefinedOperation", "C_Property_AssociationEnd_Property_Qualifier"));
    	for(String assocName : unUsedAssoc)
    		if(mModel.getAssociation(assocName) != null) this.fClassDiagram.hideAssociation(mModel.getAssociation(assocName));
    }
}