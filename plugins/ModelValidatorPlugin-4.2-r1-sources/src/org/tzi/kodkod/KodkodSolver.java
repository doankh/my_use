package org.tzi.kodkod;

import java.util.LinkedHashSet;
import java.util.Set;

import kodkod.ast.Formula;
import kodkod.engine.Evaluator;
import kodkod.engine.Solution;
import kodkod.engine.Solution.Outcome;
import kodkod.engine.Solver;
import kodkod.instance.Bounds;
import kodkod.instance.Universe;

import org.apache.log4j.Logger;
import org.tzi.kodkod.helper.LogMessages;
import org.tzi.kodkod.model.iface.IClass;
import org.tzi.kodkod.model.iface.IModel;
import org.tzi.kodkod.model.type.IntegerType;
import org.tzi.kodkod.model.type.TypeAtoms;
import org.tzi.kodkod.model.visitor.BoundsVisitor;
import org.tzi.kodkod.model.visitor.ConstraintVisitor;
import org.tzi.use.config.Options;
import org.tzi.use.util.Log;

/**
 * Encapsulation of the base algorithm for the model validation with kodkod.
 * 
 * @author Hendrik Reitmann
 * 
 */
public class KodkodSolver {

	private static final Logger LOG = Logger.getLogger(KodkodSolver.class);

	private Evaluator evaluator;

	public Solution solve(IModel model) throws Exception {
		Bounds bounds = createBounds(model);
		Formula constraint = createConstraint(model);

		KodkodModelValidatorConfiguration configuration = KodkodModelValidatorConfiguration.INSTANCE;

		if(configuration.isDebugBoundsPrint()){
			LOG.info(bounds);
		}
		
		final Solver solver = new Solver();
		solver.options().setLogTranslation(1);
		
		LOG.info(LogMessages.searchSolution(configuration.satFactory().toString(), configuration.bitwidth()));

		solver.options().setSolver(configuration.satFactory());
		solver.options().setBitwidth(configuration.bitwidth());
		
		Solution solution = solver.solve(constraint, bounds);
		
		createEvaluator(solver, solution);

		if (Options.getDebug()) {
			Log.println();
			Log.println(solution.toString());
		}
		
		return solution;
	}

	/**
	 * Creates the constraint for kodkod.
	 * 
	 * @param model
	 * @return
	 */
	private Formula createConstraint(IModel model) {
		ConstraintVisitor constraintVisitor = new ConstraintVisitor();
		model.accept(constraintVisitor);
		Formula constraint = constraintVisitor.getFormula();

		// LOG.debug("\n" + PrintHelper.prettyKodkod(constraint));

		return constraint;
	}

	/**
	 * Sets the bounds for the relations.
	 * 
	 * @param model
	 * @return
	 */
	private Bounds createBounds(IModel model) {
		Universe universe = createUniverse(model);
		Bounds bounds = new Bounds(universe);
		model.accept(new BoundsVisitor(bounds, universe.factory()));

		LOG.debug("\n" + bounds);

		return bounds;
	}

	/**
	 * Creates the kodkod universe.
	 * 
	 * @param model
	 * @return
	 */
	private Universe createUniverse(IModel model) {
		Set<Object> atoms = new LinkedHashSet<Object>();

		Set<TypeAtoms> typeAtoms = new LinkedHashSet<TypeAtoms>(model.enumTypes());
		typeAtoms.addAll(model.typeFactory().typeAtoms());
		for (TypeAtoms typeAtom : typeAtoms) {
			atoms.addAll(typeAtom.atoms());
			if (typeAtom.isInteger()) {
				atoms.addAll(((IntegerType) typeAtom).toStringAtoms());
			}
		}
		for (IClass clazz : model.classes()) {
			atoms.addAll(clazz.objectType().atoms());
		}
		
		return new Universe(atoms);
	}

	private void createEvaluator(final Solver solver, Solution solution) {
		if (solution.outcome() == (Outcome.SATISFIABLE) || solution.outcome() == (Outcome.TRIVIALLY_SATISFIABLE)) {
			evaluator = new Evaluator(solution.instance(), solver.options());
		} else {
			evaluator = null;
		}
	}

	public Evaluator evaluator() {
		return evaluator;
	}
}
