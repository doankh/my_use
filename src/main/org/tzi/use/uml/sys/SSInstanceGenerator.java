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

import org.tzi.use.main.Session;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MMInstanceGenerator;
import org.tzi.use.uml.ocl.value.Value;

/**
 * TODO
 * @author Khanh-Hoang Doan
 *
 */
public final class SSInstanceGenerator implements SSVisitor {
	private MSystemState systemState;
	private MSystem metaSystem;
	private MMInstanceGenerator mmInstanceGen;
	private LinkedList<String> soilCommands = new LinkedList<String>();
	private int idStringValueName;
	Map<String,Set<String>> mClassMapping = new HashMap<String, Set<String>>();
	Map<String,Set<String>> mAssociationMapping = new HashMap<String, Set<String>>();
	//store the map of <name of link, max index value>
	Map<String, Integer> mLinkId = new HashMap<String, Integer>();
	//store the map of <name of InstanceValue instance, max index value>
	Map<String, Integer> mInstanceValueId = new HashMap<String, Integer>();
	/**
	 * @param state
	 */

	public SSInstanceGenerator(Session fSession){
		systemState = fSession.system().state();
		metaSystem = fSession.metaSystem();
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
		idStringValueName =0;
		
		for(MObject obj: systemState.allObjects())
			visitObject(obj);
		
		for(MObject obj: systemState.allObjects())
			visitObjectforAttributes(obj);
		
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
//		String id1 = genInstance(obj.name(), null, "InstanceValue", "IV");
//		soilCommands.add(genInsertLink(id1, id, "A_InstanceValue_InstanceValue_InstanceSpecification_Instance"));
			
	}
		
	
	@Override
	public void visitObjectforAttributes(MObject obj) {
		//visit attribute value
		for(MAttribute attr: obj.cls().allAttributes()){
			String id = createInstanceName(obj.name(), null, "InstanceSpecification", "IS");
			visitAttribute(attr, id);
			visitValue(obj.state(systemState).attributeValue(attr), attr, id);
		};
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
		// TODO visit value with the type of user-defined Class (attr.type().isTypeofClass() == true)
		//visit value with Null value (metaClass = "LiteralNull")
		String metaClass, shortName = null;
		
		if(attr.type().isTypeOfInteger()) {metaClass = "LiteralInteger"; shortName = "Int" + value.toString() + "_";}
		else if(attr.type().isTypeOfReal()) {metaClass = "LiteralReal"; shortName = "Real" + value.toString().replace(".", "_") + "_";}
		else if(attr.type().isTypeOfBoolean()) {metaClass = "LiteralBoolean"; shortName = "Bool" + value.toString() + "_";}
		else if(attr.type().isTypeOfString()) {metaClass = "LiteralString"; shortName = "String" + (++idStringValueName);}
		else if(attr.type().isTypeOfClass())
		{
			String newInstanceValueName = getNewInstanceValueName(value.toString());
			String instanceValueID = genInstance(newInstanceValueName, null, "InstanceValue", "IV");
			//insert a link to the corresponding IS
			soilCommands.add(genInsertLink(instanceValueID, createInstanceName(value.toString(),null, "InstanceSpecification", "IS"), 
					"A_InstanceValue_InstanceValue_InstanceSpecification_Instance"));
			soilCommands.add(genInsertLink(createInstanceName(attr.name(), objectInstanceName, "Slot", "Slot"), instanceValueID,
					"C_Slot_OwningSlot_ValueSpecification_Value"));
			metaClass = null;
		}
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
		//an instance of InstanceValue corresponding to the object (ValueSpecification)
		//is created to connect to a the created role 
		String newInstanceValueName = getNewInstanceValueName(role.object().name());
		
		String instanceValueID = genInstance(newInstanceValueName, null, "InstanceValue", "IV");
		//insert a link to the corresponding IS
		soilCommands.add(genInsertLink(instanceValueID, createInstanceName(role.object().name(),null, "InstanceSpecification", "IS"), 
				"A_InstanceValue_InstanceValue_InstanceSpecification_Instance"));
		//insert a link to the corresponding slot (role)
		soilCommands.add(genInsertLink(id, instanceValueID, "C_Slot_OwningSlot_ValueSpecification_Value"));
	}
	
    public LinkedList<String> getGeneratedShellCommands(){
    	return soilCommands;
    }
    
    //delete all metainstantiation of the object model
    public void deleteMetaInstances(){
    	Set<String> metaClasseNames = new HashSet<String>(Arrays.asList("InstanceSpecification", "InstanceValue",
				"Slot", "LiteralInteger", "LiteralReal", "LiteralBoolean", "LiteralString", "LiteralReal", "LiteralNull"));
		Set<MObject> metaObjectperClass = new HashSet<MObject>();
		
		for(String metaClassName: metaClasseNames){
			MClass metaClass =  metaSystem.model().getClass(metaClassName);
			if(metaClass != null)
			{
				metaObjectperClass = metaSystem.state().objectsOfClass(metaClass);
				for(MObject obj: metaObjectperClass)
					metaSystem.state().deleteObject(obj);
			}
		}
    }
    
    private String getNewInstanceValueName(String objectName)
    {
    	int newInstanceValueId;
		if(!mInstanceValueId.containsKey(objectName))
		{
			newInstanceValueId = 1;
			mInstanceValueId.put(objectName, newInstanceValueId);
		}
		else
		{
			newInstanceValueId = mInstanceValueId.get(objectName);
			newInstanceValueId++;
			mInstanceValueId.replace(objectName, newInstanceValueId);
		}
		return objectName+newInstanceValueId;
    }
}
