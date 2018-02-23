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

// $Id: MMInstanceGenerator.java 5494 2015-02-05 12:59:25Z lhamann $

package org.tzi.use.uml.mm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.tzi.use.uml.mm.commonbehavior.communications.MSignal;
import org.tzi.use.uml.ocl.type.EnumType;
import org.tzi.use.uml.ocl.type.Type;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.util.StringUtil;

/**
 * A visitor for producing a sequence of commands for generating the
 * current model as an instance of the UML metamodel. The output can
 * be used as input commands for USE in combination with the
 * specification of the UML metamodel.
 *
 * @version     $ProjectVersion: 0.393 $
 * @author      Hoang Doan 
 */
public class MMInstanceGenerator implements MMVisitor {
    private MSystem fSystem;
    private LinkedList<String> soilCommands = new LinkedList<String>();
    private Set<Type> fDataTypes;
    private boolean fPass1;
//    private String fModelId;

    public MMInstanceGenerator(MSystem system) {
        fSystem = system;
        fDataTypes = new HashSet<Type>();
    }

    
    /**
     * Generates output for creating an instance of a model
     * element. This is common for all model elements.
     *
     * @return the name of the new instance 
     */
    private String genInstance(MModelElement e, String metaClass, String prefix) {
    	String eName = e.name();
        String qualifiedName = (( prefix != null ) ? prefix + "_" : "") + eName;
//      fOut.println("-- " + metaClass + " " + qualifiedName);
        String id = qualifiedName + metaClass;
        soilCommands.add("create " + id + " : " + metaClass);
        soilCommands.add("set " + id + ".name := '" + eName + "'");
        return id;
    }

    private String genInstance(MModelElement e, String metaClass) {
        return genInstance(e, metaClass, null);
    }

    public void visitAssociation(MAssociation e) {
    	String id = genInstance(e, "Association");
        soilCommands.add("set " + id + ".isDerived := " + e.isDerived());
        // visit association ends
        for (MAssociationEnd assocEnd : e.associationEnds()) {
            assocEnd.processWithVisitor(this);
        }
    }
    
    //------------------
    public void visitAssociationClass( MAssociationClass e ) {
        // prints information about associationclass
        if ( !fPass1 ) {
            String id = genInstance( e, "AssociationClass" );
            soilCommands.add("set " + id + ".isAbstract = " + e.isAbstract());
            soilCommands.add("set " + id + ".isDerived = " + e.isDerived());
        }

        // visit attributes
        for (MAttribute attr : e.attributes()) {
            attr.processWithVisitor( this );
        }

        // visit operations
        for (MOperation opc : e.operations()){
        	opc.processWithVisitor(this);
        }
        // visit association ends
        for (MAssociationEnd assocEnd : e.associationEnds()) {
            assocEnd.processWithVisitor( this );
        }

    }
    //in UML 2.4, AssociationEnd becomes Property
    public void visitAssociationEnd(MAssociationEnd e) {
        String id = genInstance(e, "Property", e.association().name());
        boolean isComposite = false;
        soilCommands.add("set " + id + ".isDerived := " + e.isDerived());
        soilCommands.add("set " + id + ".isDerivedUnion := " + e.isUnion());
        
        String soilCommand ="";
        soilCommand = "set " + id + ".aggregation := #";
        switch ( e.aggregationKind() ) {
        case MAggregationKind.NONE:
        	soilCommand = soilCommand + "none";
            break;
        case MAggregationKind.AGGREGATION:
        	soilCommand = soilCommand + "aggregate";
            break;
        case MAggregationKind.COMPOSITION:
        	soilCommand = soilCommand + "composite";
            isComposite = true;
            break;
        default: 
            throw new Error("Fatal error. Invalid multiplicity kind");            
        }
        soilCommands.add(soilCommand);
        soilCommands.add("set " + id + ".isComposite := " + isComposite);
        if(e.multiplicity().getRanges().size()>0)
        	soilCommands.add("set " + id + ".lower := " + e.multiplicity().getRanges().get(0).getLower());
        if(e.multiplicity().getRanges().size()>0)
        	soilCommands.add("set " + id + ".upper := " + e.multiplicity().getRanges().get(0).getUpper());
        
        // add AssociationEnd_Association links
        soilCommands.add("insert (" + e.association().name() + "Association, " + id + 
                     ") into A_Association_Association_Property_MemberEnd");
        soilCommands.add("insert (" + e.association().name() + "Association, " + id + 
                ") into C_Association_OwningAssociation_Property_OwnedEnd");
        soilCommands.add("insert (" + e.association().name() + "Association, " + id + 
                ") into A_Association_Association_Property_NavigableOwnedEnd");
        //add TypedElement(Property)_Type(Class) link
        soilCommands.add("insert (" + id + "," + e.cls() + "Class" + 
                ") into A_TypedElement_TypedElement_Type_Type");
    }

    public void visitAttribute(MAttribute e) {
        if (fPass1 ) {
            fDataTypes.add(e.type());
            return;
        }
        String id = genInstance(e, "Property",e.owner().name());
        soilCommands.add( "set " + id + ".isDerived := " + e.isDerived());
        soilCommands.add( "set " + id + ".isOrdered := false" );
        soilCommands.add( "set " + id + ".isUnique := false" );
        soilCommands.add( "set " + id + ".isDerivedUnion := false" );
        soilCommands.add( "set " + id + ".isReadOnly := false" );
        //fOut.println( "!set " + id + ".isID := false" );

        
        // add Class_Property link
        soilCommands.add("insert (" + e.owner().name() + "Class, " + 
                     id + ") into C_Class_Class_Property_OwnedAttribute");
        
        // add Property_Datatype link for type of attribute
        String s;
        if (e.owner().model().getClass(e.type().toString()) == null )
            s = "DataType";
        else 
            s = "Class";
        /*soilCommands.add("insert (" + e.type() + s + ", " + id +
                     ") into C_DataType_Datatype_Property_OwnedAttribute");*/
        soilCommands.add("insert (" + id +  ", " + e.type() + s +
                ") into A_TypedElement_TypedElement_Type_Type");
    }

    public void visitClass(MClass e) {
        //Generate class instances first and attributes and operations later, because a class might be
    	//the type of an attribute or the return type of an operation
    	if (fPass1 ) {
            String id = genInstance(e, "Class");
            
            soilCommands.add("set " + id + ".isAbstract := " + e.isAbstract());
            //generate a corresponding metrics object
            String id1 = id + "Metrics";
            soilCommands.add("create " + id1 + " : " + "ClassMetrics");
            //add A_ClassMetrics_metrics_Class_class assocition between Class and ClassMetrics
            soilCommands.add("insert (" + id + "," + id1 + ") into A_ClassMetrics_metrics_Class_class");
/*            // add to model namespace
            fOut.println("!insert (" + fModelId + ", " + id +
                         ") into Namespace_ModelElement");*/
            
        }

        // visit attributes
        for (MAttribute attr : e.attributes()) {
            attr.processWithVisitor(this);
        }
        // visit operations
        for (MOperation opc : e.operations()){
        	opc.processWithVisitor(this);
        }
    }

    public void visitClassInvariant(MClassInvariant e) {
        String id = genInstance(e, "Constraint", e.cls().name());
        soilCommands.add("set " + id + ".body := '" + 
                     StringUtil.escapeString(e.bodyExpression().toString(), '\'')
                     + "'");
        // connect to ModelElement
        soilCommands.add("insert (" + id + ", " +
                     e.cls().name() + 
                     "Class) into Constraint_ModelElement");
//        // add to model namespace
//        fOut.println("!insert (" + fModelId + ", " + id +
//                     ") into Namespace_ModelElement");
    }

    public void visitGeneralization(MGeneralization e) {
    	
		String id = e.name();
	    soilCommands.add("create " + id + " : " + "Generalization");
       //add A_Classifier_General_Generalization_Generalization between the general class and the generalization instance
	    soilCommands.add("insert ("+ e.parent().name() + "Class" + "," + id +") into A_Classifier_General_Generalization_Generalization");
       //add C_Classifier_Specific_Generalization_Generalization between the subclass and the generalization instance 
	    soilCommands.add("insert ("+ e.child().name() + "Class" + "," + id +") into C_Classifier_Specific_Generalization_Generalization");
       
        // add A_Class_Class_Class_SuperClass association between superclass and subclass
        soilCommands.add("insert (" + e.child().name() + "Class, " +
                     e.parent().name() + "Class) into A_Class_Class_Class_SuperClass");
        
    }

    public void visitModel(MModel e) {
        // create Model
        /*fOut.println("-- UML metamodel instance generated by USE " + 
                     Options.RELEASE_VERSION);
        fOut.println();*/
    	//generate the diagram mstric class
    	soilCommands.add("create ModelMetrics : " + "ModelMetrics");
        fPass1 = true;

        // visit classes in first pass only to gather required
        // DataTypes
        for (MClass cls : e.classes()) {
            cls.processWithVisitor(this);
        }

        // create all DataTypes that are required later
        /*fOut.println("-- DataTypes");*/
        
        for (Type t : fDataTypes) {
            if (e.getClass(t.toString()) == null ) {
                String id = t.toString() + "DataType";
                soilCommands.add("create " + id + " : DataType");
                soilCommands.add("set " + id + ".name := '" + t.toString() + "'");
            }
        }

        fPass1 = false;
        
        // visit classes
        for (MClass cls : e.classes()) {
            cls.processWithVisitor(this);
        }
        
        // visit associationclasses
        for (MAssociationClass cls : e.getAssociationClassesOnly()) {
            cls.processWithVisitor(this);
        }

        // visit associations
        for (MAssociation assoc : e.associations()) {
            assoc.processWithVisitor(this);
        }

        // visit generalizations
        Iterator<MGeneralization> it = e.generalizationGraph().edgeIterator();
        
        while (it.hasNext() ) {
            MGeneralization gen = it.next();
            gen.processWithVisitor(this);
        }

        // visit constraints
//        for (MClassInvariant inv : e.classInvariants()) {
//            inv.processWithVisitor(this);
//        }
    }

    public void visitOperation(MOperation e) {
    	
        if (fPass1 ) {
            if (e.hasResultType()) fDataTypes.add(e.resultType());
            return;
        }
    	if (!fPass1 ) {
	        String id = genInstance(e, "Operation", e.cls().name());
	        soilCommands.add( "set " + id + ".isQuery := false");
	        soilCommands.add( "set " + id + ".isOrdered := false" );
	        soilCommands.add( "set " + id + ".isUnique := false" );
	        
	        // add Class_Operation link
	        soilCommands.add("insert (" + e.cls().name() + "Class, " + 
	                     id + ") into C_Class_Class_Operation_OwnedOperation");

	        // add Operation_Datatype link for return type of the operation
	        String s;
	        if (e.hasResultType())
	        {
		        if (e.cls().model().getClass(e.resultType().toString()) == null )
		            s = "DataType";
		        //FIXME: no association when the resultType of a operation is a class 
		        else 
		            s = "Class";
		        soilCommands.add("insert (" + e.resultType() + s + ", " + id +
		                     ") into C_DataType_Datatype_Operation_OwnedOperation");
	        }
	        //if the operation redefine another operation from a super class
//	        if(e.getRedefinedOperation() != null)
//	        {
//	        	MOperation mOp = e.getRedefinedOperation();
//	        	soilCommands.add("insert (" + id + "," + mOp.cls().name() + "_" + mOp.name() + "Operation" 
//	                      + ") into A_Operation_Operation_Operation_RedefinedOperation");
//	        }
	        	
    	}
    }

    public LinkedList<String> getGeneratedShellCommands(){
    	return soilCommands;
    }
    /*public LinkedList<MStatement> getGeneratedStatements(){
    	LinkedList<MStatement> genStatements = new LinkedList<MStatement>();
    	for (int i = 0; i < soilCommands.size(); i++)
    	{
    		MStatement statement = translateSoilCommandtoStatement(soilCommands.get(i));
    		if (statement != null) genStatements.add(statement); 
    	}
    	return genStatements;
    }*/
    
    public void visitPrePostCondition(MPrePostCondition e) {
        //FIXME: implement
    }

	@Override
	public void visitAnnotation(MElementAnnotation a) {
		// NoOp		
	}

	@Override
	public void visitSignal(MSignal mSignalImpl) {
		//FIXME: implement		
	}

	@Override
	public void visitEnum(EnumType enumType) {
		// NoOp		
	}	
}
