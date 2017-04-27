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

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.tzi.use.config.Options;
import org.tzi.use.uml.mm.commonbehavior.communications.MSignal;
import org.tzi.use.uml.ocl.type.EnumType;
import org.tzi.use.uml.ocl.type.Type;
import org.tzi.use.util.StringUtil;

/**
 * A visitor for producing a sequence of commands for generating the
 * current model as an instance of the UML metamodel. The output can
 * be used as input commands for USE in combination with the
 * specification of the UML metamodel.
 *
 * @version     $ProjectVersion: 0.393 $
 * @author      Mark Richters 
 */
public class MMInstanceGenerator implements MMVisitor {
    protected PrintWriter fOut;
    private Set<Type> fDataTypes;
    private boolean fPass1;
    private String fModelId;

    public MMInstanceGenerator(PrintWriter out) {
        fOut = out;
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
        fOut.println("-- " + metaClass + " " + qualifiedName);
        String id = qualifiedName + metaClass;
        fOut.println("!create " + id + " : " + metaClass);
        fOut.println("!set " + id + ".name := '" + eName + "'");
        return id;
    }

    private String genInstance(MModelElement e, String metaClass) {
        return genInstance(e, metaClass, null);
    }

    public void visitAssociation(MAssociation e) {
        String id = genInstance(e, "Association");
        fOut.println( "!set " + id + ".isDerived := " + e.isDerived());
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
            fOut.println( "!set " + id + ".isAbstract = " + e.isAbstract() );
            fOut.println( "!set " + id + ".isDerived = " + e.isDerived());
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
        fOut.println( "!set " + id + ".isDerived := " + e.isDerived());
        fOut.println( "!set " + id + ".isDerivedUnion := " + e.isUnion());

        fOut.print("!set " + id + ".aggregation := #");
        switch ( e.aggregationKind() ) {
        case MAggregationKind.NONE:
            fOut.println("none");
            break;
        case MAggregationKind.AGGREGATION:
            fOut.println("aggregate");
            break;
        case MAggregationKind.COMPOSITION:
            fOut.println("composite");
            isComposite = true;
            break;
        default: 
            throw new Error("Fatal error. Invalid multiplicity kind");            
        }
        
        fOut.println( "!set " + id + ".isComposite := " + isComposite );
        if(e.multiplicity().getRanges().size()>0)
        	fOut.println( "!set " + id + ".lower := " + e.multiplicity().getRanges().get(0).getLower());
        if(e.multiplicity().getRanges().size()>0)
        	fOut.println( "!set " + id + ".upper := " + e.multiplicity().getRanges().get(0).getUpper());
        
        // add AssociationEnd_Association link
        fOut.println("!insert (" + e.association().name() + "Association, " + id + 
                     ") into A_Association_Association_Property_MemberEnd");
        fOut.println();
    }

    public void visitAttribute(MAttribute e) {
        if (fPass1 ) {
            fDataTypes.add(e.type());
            return;
        }
        String id = genInstance(e, "Property",e.owner().name());
        fOut.println( "!set " + id + ".isDerived := " + e.isDerived());
        fOut.println( "!set " + id + ".isOrdered := false" );
        fOut.println( "!set " + id + ".isUnique := false" );
        fOut.println( "!set " + id + ".isDerivedUnion := false" );
        fOut.println( "!set " + id + ".isReadOnly := false" );
        //fOut.println( "!set " + id + ".isID := false" );

        
        // add Class_Property link
        fOut.println("!insert (" + e.owner().name() + "Class, " + 
                     id + ") into C_Class_Class_Property_OwnedAttribute");

        // add Property_Datatype link for type of attribute
        String s;
        if (e.owner().model().getClass(e.type().toString()) == null )
            s = "DataType";
        else 
            s = "Class";
        fOut.println("!insert (" + e.type() + s + ", " + id +
                     ") into C_DataType_Datatype_Property_OwnedAttribute");
        fOut.println();
    }

    public void visitClass(MClass e) {
        if (! fPass1 ) {
            String id = genInstance(e, "Class");
            fOut.println("!set " + id + ".isAbstract := " + e.isAbstract());

/*            // add to model namespace
            fOut.println("!insert (" + fModelId + ", " + id +
                         ") into Namespace_ModelElement");*/
            fOut.println();
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
        fOut.println("!set " + id + ".body := '" + 
                     StringUtil.escapeString(e.bodyExpression().toString(), '\'')
                     + "'");
        // connect to ModelElement
        fOut.println("!insert (" + id + ", " +
                     e.cls().name() + 
                     "Class) into Constraint_ModelElement");
//        // add to model namespace
//        fOut.println("!insert (" + fModelId + ", " + id +
//                     ") into Namespace_ModelElement");
        fOut.println();
    }

    public void visitGeneralization(MGeneralization e) {
        String id = genInstance(e, "Generalization");
        fOut.println("!set " + id + ".discriminator := ''");
        // connect to child
        fOut.println("!insert (" + id + ", " +
                     e.child().name() + 
                     "Class) into Generalization_GeneralizableElement1");
        // connect to parent
        fOut.println("!insert (" + id + ", " +
                     e.parent().name() + 
                     "Class) into Generalization_GeneralizableElement2");
//        // add to model namespace
//        fOut.println("!insert (" + fModelId + ", " + id +
//                     ") into Namespace_ModelElement");
        fOut.println();
    }

    public void visitModel(MModel e) {
        // create Model
        fOut.println("-- UML metamodel instance generated by USE " + 
                     Options.RELEASE_VERSION);
        fOut.println();

        fPass1 = true;

        // visit classes in first pass only to gather required
        // DataTypes
        for (MClass cls : e.classes()) {
            cls.processWithVisitor(this);
        }

        // create all DataTypes that are required later
        fOut.println("-- DataTypes");
        
        for (Type t : fDataTypes) {
            if (e.getClass(t.toString()) == null ) {
                String id = t.toString() + "DataType";
                fOut.println("!create " + id + " : DataType");
                fOut.println("!set " + id + ".name := '" + t.toString() + "'");
            }
        }
        fOut.println();

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
        //FIXME: implement --> HoangDK 18.08
    	
        if (fPass1 ) {
            if (e.hasResultType()) fDataTypes.add(e.resultType());
            return;
        }
    	if (!fPass1 ) {
	        String id = genInstance(e, "Operation", e.cls().name());
	        fOut.println( "!set " + id + ".isQuery := false");
	        fOut.println( "!set " + id + ".isOrdered := false" );
	        fOut.println( "!set " + id + ".isUnique := false" );
	        
	        // add Class_Operation link
	        fOut.println("!insert (" + e.cls().name() + "Class, " + 
	                     id + ") into C_Class_Class_Operation_OwnedOperation");

	        // add Operation_Datatype link for return type of the operation
	        String s;
	        if (e.hasResultType())
	        {
		        if (e.cls().model().getClass(e.resultType().toString()) == null )
		            s = "DataType";
		        //FIXME: no association when the resultType of a operation is a class 
//		        else 
//		            s = "Class";
//		        fOut.println("!insert (" + e.resultType() + s + ", " + id +
//		                     ") into C_DataType_Datatype_Operation_OwnedOperation");
	        }
	        
	        fOut.println();
    	}
    }

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
