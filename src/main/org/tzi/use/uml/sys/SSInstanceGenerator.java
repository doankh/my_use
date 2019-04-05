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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MMInstanceGenerator;
import org.tzi.use.uml.ocl.value.Value;

/**
 * TODO
 * @author Khanh-Hoang Doan
 *
 */
public final class SSInstanceGenerator implements SSVisitor {
	private MSystemState systemState;
	private MMInstanceGenerator mmInstanceGen;
	private LinkedList<String> soilCommands = new LinkedList<String>();
	Map<String,Set<String>> mClassMapping = new HashMap<String, Set<String>>();
	Map<String,Set<String>> mAssociationMapping = new HashMap<String, Set<String>>();
	//store the map of <name of link, max index value>
	Map<String, Integer> mLinkId = new HashMap<String, Integer>();
	
	/**
	 * @param state
	 */

	public SSInstanceGenerator(MSystemState _systemState){
		systemState = _systemState;
		mmInstanceGen = new MMInstanceGenerator();
	}
	
	void initMapping(){
		mClassMapping.clear();
		mAssociationMapping.clear();
		
		mClassMapping.put("Object",new HashSet<String>(Arrays.asList("InstanceSpecification","InstanceValue")));
		mClassMapping.put("Attribute",new HashSet<String>(Arrays.asList("Slot")));
		mClassMapping.put("Link",new HashSet<String>(Arrays.asList("InstanceSpecification")));
		mClassMapping.put("Role",new HashSet<String>(Arrays.asList("Slot")));
		mClassMapping.put("Real",new HashSet<String>(Arrays.asList("LiteralReal")));
		mClassMapping.put("Integer",new HashSet<String>(Arrays.asList("LiteralInteger")));
		mClassMapping.put("String",new HashSet<String>(Arrays.asList("LiteralString")));
		mClassMapping.put("Boolean",new HashSet<String>(Arrays.asList("LiteralBoolean")));
						
	}
	
	private String createInstanceName(String eName, String prefix, String metaClass, String metaClass_ShortName){
		String qualifiedName = (( prefix != null ) ? prefix + "_" : "") + eName;

        return qualifiedName + metaClass_ShortName;
	}
	
	private String genInstance(String eName, String prefix, String metaClass, String metaClass_ShortName) {
    	
        String id = createInstanceName(eName, prefix, metaClass, metaClass_ShortName);
        soilCommands.add("create " + id + " : " + metaClass);
        //soilCommands.add("set " + id + ".name := '" + eName + "'");
        
        return id;
    }
	
	private String genInsertLink(String obj1, String obj2, String assName){
		return "insert (" + obj1 + "," + obj2 + ") into " + assName;	
	}
	
	@Override
	public void visitSystemState() {
		// TODO Auto-generated method stub
		for(MObject obj: systemState.allObjects())
			visitObject(obj);
		
		for(MLink link: systemState.allLinks())
			visitLink(link);
	}

	@Override
	public void visitObject(MObject obj) {
		// TODO Auto-generated method stub
		String id = genInstance(obj.name(), null, "InstanceSpecification", "IS");
		soilCommands.add("set " + id + ".name := '" + obj.name() + "'");
		soilCommands.add(genInsertLink(id, mmInstanceGen.createInstanceName(obj.cls(), "Class", null),
				"A_InstanceSpecification_InstanceSpecification_Classifier_Classifier"));
		String id1 = genInstance(obj.name(), null, "InstanceValue", "IV");
		soilCommands.add(genInsertLink(id1, id, "A_InstanceValue_InstanceValue_InstanceSpecification_Instance"));
		
		//visit attribute value
		for(MAttribute attr: obj.cls().allAttributes()){
			visitAttribute(attr, id);
			visitValue(obj.state(systemState).attributeValue(attr), attr, id);
		}
		
	}

	@Override
	public void visitAttribute(MAttribute attribute, String objectInstanceName) {
		// TODO Auto-generated method stub
		String id = genInstance(attribute.name(), objectInstanceName, "Slot", "Slot");
		soilCommands.add(genInsertLink(objectInstanceName, id, "C_InstanceSpecification_OwningInctance_Slot_OwnedElement"));
		soilCommands.add(genInsertLink(id, mmInstanceGen.createInstanceName(attribute, "Property", attribute.owner().name()),
				"A_Slot_Slot_StructuralFeature_DefiningFeature"));		
		
	}

	@Override
	public void visitValue(Value value, MAttribute attr, String objectInstanceName) {
		// TODO Auto-generated method stub
		String metaClass, shortName = null;
		int idStringValueName = 0;
		if(attr.type().isTypeOfInteger()) {metaClass = "LiteralInteger"; shortName = "Int" + value.toString() + "_";}
		else if(attr.type().isTypeOfReal()) {metaClass = "LiteralReal"; shortName = "Real" + value.toString().replace(".", "_") + "_";}
		else if(attr.type().isTypeOfBoolean()) {metaClass = "LiteralBoolean"; shortName = "Bool" + value.toString() + "_";}
		else if(attr.type().isTypeOfString()) {metaClass = "LiteralString"; shortName = "String" + (++idStringValueName) + "_" + value.toString();}
		else metaClass = null;
		if (metaClass != null)
		{
			String id = genInstance(shortName, null, metaClass, metaClass);
			soilCommands.add("set " + id + ".value := " + value.toString());
			soilCommands.add(genInsertLink(createInstanceName(attr.name(), objectInstanceName, "Slot", "Slot"), id,
				"C_Slot_OwningSlot_ValueSpecification_Value"));
		}
	}

	@Override
	public void visitLink(MLink link) {
		// TODO Auto-generated method stub
		String assName = link.association().name();
		int linkId;
		if(!mLinkId.containsKey(assName))
		{
			linkId = 1;
			mLinkId.put(assName, linkId);
		}
		else
		{
			linkId = mLinkId.get(assName);
			linkId++;
			mLinkId.replace(assName, linkId);
		}
		String id = genInstance(assName + linkId, null, "InstanceSpecification", "IS");
		soilCommands.add(genInsertLink(id, mmInstanceGen.createInstanceName(link.association(), "Association", null),
				"A_InstanceSpecification_InstanceSpecification_Classifier_Classifier"));
		
		//visit roles
		for(MLinkEnd role: link.linkEnds())
			visitRole(role, id);
	}

	@Override
	public void visitRole(MLinkEnd role, String linkInstanceName) {
		// TODO Auto-generated method stub
		String id = genInstance(role.associationEnd().name(), linkInstanceName, "Slot", "Slot");
		soilCommands.add(genInsertLink(linkInstanceName, id, "C_InstanceSpecification_OwningInctance_Slot_OwnedElement"));
		soilCommands.add(genInsertLink(id, mmInstanceGen.createInstanceName(role.associationEnd(), "Property", role.associationEnd().association().name()),
				"A_Slot_Slot_StructuralFeature_DefiningFeature"));
		soilCommands.add(genInsertLink(id, createInstanceName(role.object().name(), null, "InstanceValue", "IV"), "C_Slot_OwningSlot_ValueSpecification_Value"));
	}

    public LinkedList<String> getGeneratedShellCommands(){
    	return soilCommands;
    }
}
