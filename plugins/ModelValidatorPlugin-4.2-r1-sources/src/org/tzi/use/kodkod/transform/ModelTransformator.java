package org.tzi.use.kodkod.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import kodkod.ast.Node;
import kodkod.ast.Relation;
import kodkod.ast.Variable;

import org.apache.log4j.Logger;
import org.tzi.kodkod.helper.LogMessages;
import org.tzi.kodkod.model.iface.IAssociation;
import org.tzi.kodkod.model.iface.IAssociationClass;
import org.tzi.kodkod.model.iface.IAssociationEnd;
import org.tzi.kodkod.model.iface.IAttribute;
import org.tzi.kodkod.model.iface.IClass;
import org.tzi.kodkod.model.iface.IModel;
import org.tzi.kodkod.model.iface.IModelFactory;
import org.tzi.kodkod.model.type.Type;
import org.tzi.kodkod.model.type.TypeFactory;
import org.tzi.use.graph.DirectedGraph;
import org.tzi.use.kodkod.transform.ocl.DefaultExpressionVisitor;
import org.tzi.use.uml.mm.MAggregationKind;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MAssociationClass;
import org.tzi.use.uml.mm.MAssociationEnd;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MClassInvariant;
import org.tzi.use.uml.mm.MClassifier;
import org.tzi.use.uml.mm.MGeneralization;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.type.EnumType;
import org.tzi.use.util.uml.sorting.UseFileOrderComparator;

/**
 * Class to transform the use model in a model of the model validator.
 * 
 * @author Hendrik Reitmann
 * 
 */
public class ModelTransformator {

	private static final Logger LOG = Logger.getLogger(ModelTransformator.class);

	private final IModelFactory factory;
	private final TypeFactory typeFactory;

	public ModelTransformator(final IModelFactory factory, final TypeFactory typeFactory) {
		this.factory = factory;
		this.typeFactory = typeFactory;
	}

	public IModel transform(MModel mModel) {
		long startTime = System.currentTimeMillis();

		LOG.info(LogMessages.startModelTransform(mModel.name()));

		IModel model = factory.createModel(mModel.name(), factory, typeFactory);

		try {
			transformEnums(model, mModel.enumTypes());

			List<MAssociationClass> associationClasses = new ArrayList<MAssociationClass>(mModel.getAssociationClassesOnly());

			List<MClass> classes = new ArrayList<MClass>(mModel.classes());
			classes.removeAll(associationClasses);

			List<MAssociation> simpleAssociations = new ArrayList<MAssociation>(mModel.associations());
			simpleAssociations.removeAll(associationClasses);

			Collections.sort(associationClasses, new UseFileOrderComparator());
			Collections.sort(classes, new UseFileOrderComparator());
			Collections.sort(simpleAssociations, new UseFileOrderComparator());
			
			transformClasses(model, classes);

			transformAssociationClasses(model, associationClasses);

			transformGeneralization(model, mModel.generalizationGraph());

			transformAttributes(model, classes);

			transformSimpleAssociations(model, simpleAssociations);

			transformDerivedAttributes(model, classes);
			transformDerivedAttributes(model, associationClasses);
			
			InvariantTransformator invariantTransformator = new InvariantTransformator(factory, typeFactory);
			List<MClassInvariant> classInvariants = new ArrayList<MClassInvariant>(mModel.classInvariants());
			Collections.sort(classInvariants, new UseFileOrderComparator());
			
			invariantTransformator.transformAndAdd(model, classInvariants);

			LOG.info(LogMessages.modelTransformSuccessful);
			LOG.info(LogMessages.modelTransformTime((System.currentTimeMillis() - startTime)));

		} catch (Exception exception) {
			if (LOG.isDebugEnabled()) {
				LOG.error(LogMessages.modelTransformError, exception);
			} else {
				LOG.error(LogMessages.modelTransformError);
			}
		}

		return model;
	}

	private void transformDerivedAttributes(IModel model, List<? extends MClass> classes) {
		for(MClass cls : classes){
			for(MAttribute attr : cls.attributes()){
				transformDerivedAttribute(model, attr);
			}
		}
	}

	private void transformDerivedAttribute(IModel model, MAttribute mAttr) {
		if(!mAttr.isDerived()){
			return;
		}
		
		IClass kodkodCls = model.getClass(mAttr.owner().name());
		IAttribute attr = kodkodCls.getAttribute(mAttr.name());
		
		// transform derived expression
		// <owner>.allInstances()->forAll( self | self.<attribute> = derivedExpr )
		Expression derivedExpr = mAttr.getDeriveExpression();
		if(derivedExpr != null){
			Relation ownerRel;
			if(kodkodCls.existsInheritance()){
				ownerRel = kodkodCls.inheritanceRelation();
			} else {
				ownerRel = kodkodCls.relation();
			}
			
			Variable var = Variable.unary("self");
			
			Map<String, Node> variables = new TreeMap<String, Node>();
			variables.put("self", var);
			
			DefaultExpressionVisitor dev = new DefaultExpressionVisitor(model, variables,
					new HashMap<String, IClass>(), new HashMap<String, Variable>(), new ArrayList<String>());
			derivedExpr.processWithVisitor(dev);
			kodkod.ast.Expression derExpr = (kodkod.ast.Expression) dev.getObject();
			attr.setDerivedConstraint(var.join(attr.relation()).eq(derExpr).forAll(var.oneOf(ownerRel)));
		}
	}

	private void transformEnums(IModel model, Set<EnumType> enumTypes) {
		List<String> literals;
		for (EnumType enumType : enumTypes) {
			literals = new ArrayList<String>();
			for (String literal : enumType.getLiterals()) {
				literals.add(literal);
			}
			model.addEnumType((org.tzi.kodkod.model.type.EnumType) typeFactory.enumType(enumType.name(), literals));
		}
	}

	private void transformClasses(IModel model, List<MClass> mClasses) {
		for (MClass mClass : mClasses) {
			if (!mClass.name().startsWith("$")) {
				IClass kClass = factory.createClass(model, mClass.name(), mClass.isAbstract());
				model.addClass(kClass);
			} else {
				LOG.error(LogMessages.className$Error);
			}
		}
	}

	private void transformAttributes(IModel model, List<MClass> mClasses) {
		for (MClass mClass : mClasses) {
			IClass kClass = model.getClass(mClass.name());
			transformClassAttributes(model, kClass, mClass.attributes());
		}
	}

	private void transformClassAttributes(IModel model, IClass clazz, List<MAttribute> attributes) {
		TypeConverter typeConverter = new TypeConverter(model);
		for (MAttribute mAttribute : attributes) {

			Type type = typeConverter.convert(mAttribute.type());
			if (type != null) {
				clazz.addAttribute(factory.createAttribute(model, mAttribute.name(), type, clazz));
			}
		}
	}

	private void transformSimpleAssociations(IModel model, List<MAssociation> mAssociations) {
		MultiplicityTransformator multTransformator = new MultiplicityTransformator();
		for (MAssociation mAssociation : mAssociations) {
			model.addAssociation(transformAssociation(multTransformator, model, mAssociation));
		}
	}

	private IAssociation transformAssociation(MultiplicityTransformator multTransformator, IModel model, MAssociation mAssociation) {
		IAssociation association = factory.createAssociation(model, mAssociation.name());
		for (MAssociationEnd mAssociationEnd : mAssociation.associationEnds()) {

			IAssociationEnd end = factory.createAssociationEnd(mAssociationEnd.name(),
					multTransformator.transform(mAssociationEnd.multiplicity()),
					transformAggregationKind(mAssociationEnd.aggregationKind()),
					model.getClass(mAssociationEnd.cls().name()));
			association.addAssociationEnd(end);
			end.associatedClass().addAssociation(association);
		}
		return association;
	}

	private int transformAggregationKind(int useAggregationKind) {
		if(useAggregationKind == MAggregationKind.COMPOSITION){
			return IAssociationEnd.COMPOSITION;
		} else if(useAggregationKind == MAggregationKind.AGGREGATION){
			return IAssociationEnd.AGGREGATION;
		} else {
			return IAssociationEnd.REGULAR;
		}
	}

	private void transformAssociationClasses(IModel model, List<MAssociationClass> mAssociationClasses) {
		MultiplicityTransformator multTransformator = new MultiplicityTransformator();
		for (MAssociationClass mAssociationClass : mAssociationClasses) {

			IAssociationClass associationClass = factory.createAssociationClass(model, mAssociationClass.name(), mAssociationClass.isAbstract());
			transformClassAttributes(model, associationClass, mAssociationClass.attributes());
			model.addClass(associationClass);

			IAssociation association = transformAssociation(multTransformator, model, mAssociationClass);
			association.setAssociationClass(associationClass);

			model.addAssociation(association);
		}
	}

	private void transformGeneralization(IModel model, DirectedGraph<MClassifier, MGeneralization> mGeneralizationGraph) {
		IClass source, target;

		Iterator<MGeneralization> iterator = mGeneralizationGraph.edgeIterator();
		while (iterator.hasNext()) {
			MGeneralization next = iterator.next();
			if (!(next.source() instanceof MClass))  continue;
			
			source = model.getClass(next.source().name());
			target = model.getClass(next.target().name());
			source.addParent(target);
			target.addChild(source);
		}
	}
}
