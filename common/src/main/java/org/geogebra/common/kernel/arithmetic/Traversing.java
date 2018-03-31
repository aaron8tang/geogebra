package org.geogebra.common.kernel.arithmetic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.geogebra.common.gui.view.spreadsheet.RelativeCopy;
import org.geogebra.common.kernel.CASGenericInterface;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.arithmetic3D.MyVec3DNode;
import org.geogebra.common.kernel.commands.CommandProcessor;
import org.geogebra.common.kernel.geos.GeoDummyVariable;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoElementSpreadsheet;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.main.App;
import org.geogebra.common.plugin.Operation;
import org.geogebra.common.util.DoubleUtil;

import com.himamis.retex.editor.share.util.Unicode;

/**
 * Traversing objects are allowed to traverse through an Equation, MyList,
 * ExpressionNode and MyVecNode(3D) structure to perform some action, e.g.
 * replace one type of objects by another or just count some occurences of
 * certain types of objects.
 * 
 * Each public class in this file which implements Traversing solves a usual
 * task. To support transparency and good coding style, such tasks should be
 * called by the same convention.
 * 
 * Example code which collects GeoElements in an ExpressionNode with
 * multiplicities: {@code
 * ExpressionNode root = ...; 
 * HashMap<GeoElement, Integer> gSet = new HashMap<GeoElement, Integer>();
 * GeoCollector gc = GeoCollector.getCollector(gSet);
 * root.traverse(gc);
 * } Now the GeoElements are collected in {@code gSet}.
 * 
 * @author Zbynek Konecny
 */
public interface Traversing {
	/**
	 * Processes a value locally (no recursion)
	 * 
	 * @param ev
	 *            value to process
	 * @return processed value
	 */
	public ExpressionValue process(final ExpressionValue ev);

	/**
	 * Replaces one object by another
	 */
	public class Replacer implements Traversing {
		private ExpressionValue oldObj;
		private ExpressionValue newObj;
		private static Replacer replacer = new Replacer();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev == oldObj) {
				return newObj;
			}
			return ev;
		}

		/**
		 * Creates a replacer
		 * 
		 * @param original
		 *            object to be replaced
		 * @param replacement
		 *            replacement
		 * @return replacer
		 */
		public static Replacer getReplacer(ExpressionValue original,
				ExpressionValue replacement) {
			replacer.oldObj = original;
			replacer.newObj = replacement;
			return replacer;
		}
	}

	/**
	 * Like replacer, but creates deep copies
	 *
	 */
	public class CopyReplacer implements Traversing {
		private ExpressionValue oldObj;
		private ExpressionValue newObj;
		private Kernel kernel;
		private static CopyReplacer replacer = new CopyReplacer();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev == oldObj) {
				newObj = newObj.deepCopy(kernel);
				return newObj;
			}
			return ev;
		}

		/**
		 * Creates a replacer
		 * 
		 * @param original
		 *            object to be replaced
		 * @param replacement
		 *            replacement
		 * @param kernel
		 *            kernel for copies
		 * @return replacer
		 */
		public static CopyReplacer getReplacer(ExpressionValue original,
				ExpressionValue replacement, Kernel kernel) {
			replacer.oldObj = original;
			replacer.newObj = replacement;
			replacer.kernel = kernel;
			return replacer;
		}
	}

	/**
	 * Replaces dummy variable with given name
	 *
	 */
	public class CommandReplacer implements Traversing {
		private Kernel kernel;
		private boolean cas;
		private static CommandReplacer replacer = new CommandReplacer();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev instanceof Command) {
				Command c = (Command) ev;
				String cmdName = kernel.getApplication()
						.getReverseCommand(c.getName());
				if (CommandProcessor.isCmdName(cmdName)
						|| kernel.getMacro(c.getName()) != null) {
					return ev;
				}
				MyList argList = new MyList(kernel);
				for (int i = 0; i < c.getArgumentNumber(); i++) {
					argList.addListElement(c.getItem(i).traverse(this));
				}
				ExpressionValue var = cas
						? new GeoDummyVariable(kernel.getConstruction(),
								c.getName())
						: new Variable(kernel, c.getName());
				return new ExpressionNode(kernel, var, Operation.FUNCTION_NVAR,
						argList);
			}
			return ev;
		}

		/**
		 * @param kernel
		 *            kernel in which resulting variables live (also needed to
		 *            check which commands are valid)
		 * @param cas
		 *            whether this is for CAS
		 * @return replacer
		 */
		public static CommandReplacer getReplacer(Kernel kernel, boolean cas) {
			replacer.kernel = kernel;
			replacer.cas = cas;
			return replacer;
		}
	}

	/**
	 * Replaces sin(15) with sin(15deg) GGB-2183
	 *
	 */
	public class DegreeReplacer implements Traversing {
		private Kernel kernel;
		private static DegreeReplacer replacer = new DegreeReplacer();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev instanceof ExpressionNode) {
				ExpressionNode en = (ExpressionNode) ev;

				Operation op = en.getOperation();
				if (Operation.isTrigDegrees(op)) {
					ExpressionValue arg = en.getLeft().unwrap();
					// Log.debug("arg " + arg.toString() + " " + arg.getClass()
					// + " " + arg.evaluateDouble());
					if (arg.isLeaf() && arg.isConstant() && !DoubleUtil
							.isInteger(180 * arg.evaluateDouble() / Math.PI)) {

						ExpressionNode argDegrees = new ExpressionNode(kernel,
								arg, Operation.MULTIPLY,
								new MySpecialDouble(kernel, Math.PI / 180.0,
										Unicode.DEGREE_CHAR + ""));

						return new ExpressionNode(kernel, argDegrees, op, null);
					}

				}

			}
			return ev;
		}

		/**
		 * @param kernel
		 *            kernel in which resulting variables live (also needed to
		 *            check which commands are valid)
		 * @return replacer
		 */
		public static DegreeReplacer getReplacer(Kernel kernel) {
			replacer.kernel = kernel;
			return replacer;
		}
	}

	/**
	 * Replaces asin(0.5) with asind(0.5)
	 *
	 */
	public class ArcTrigReplacer implements Traversing {

		private static ArcTrigReplacer replacer = new ArcTrigReplacer();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev instanceof ExpressionNode) {
				ExpressionNode en = (ExpressionNode) ev;
				Operation op = en.getOperation();
				Operation newOp = null;

				switch (op) {
				default:
					// do nothing
					break;
				case ARCSIN:
					newOp = Operation.ARCSIND;
					break;
				case ARCCOS:
					newOp = Operation.ARCCOSD;
					break;
				case ARCTAN:
					newOp = Operation.ARCTAND;
					break;
				}

				if (newOp != null) {
					en.setOperation(newOp);
				}
			}
			return ev;
		}

		/**
		 * @return replacer
		 */
		public static ArcTrigReplacer getReplacer() {
			return replacer;
		}
	}

	/**
	 * Replaces sin(x) with sin(x deg) GGB-2183 eg Solve(sin(x)=1/2)
	 *
	 */
	public class DegreeVariableReplacer implements Inspecting {
		private static DegreeVariableReplacer replacer = new DegreeVariableReplacer();

		@Override
		public boolean check(ExpressionValue ev) {
			if (ev instanceof ExpressionNode) {
				ExpressionNode en = (ExpressionNode) ev;

				Operation op = en.getOperation();
				ExpressionValue arg;

				if (Operation.isTrigDegrees(op) && (arg = en.getLeft()
						.unwrap()) instanceof FunctionVariable) {

					FunctionVariable fv = (FunctionVariable) arg;

					// change sin(x) to sin(x deg)
					if ("x".equals(fv.getSetVarString())) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * @param kernel
		 *            kernel in which resulting variables live (also needed to
		 *            check which commands are valid)
		 * @return replacer
		 */
		public static DegreeVariableReplacer getReplacer(Kernel kernel) {
			return replacer;
		}
	}

	/**
	 * Replaces dummy variable with given name
	 *
	 */
	public class CommandFunctionReplacer implements Traversing {
		private String fn;
		private GeoElement function;

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev.isGeoElement() && fn
					.equalsIgnoreCase(((GeoElement) ev).getLabelSimple())) {
				return function;
			}
			if (ev instanceof Command && fn.equals(((Command) ev).getName())) {

				Command c = (Command) ev;

				MyList argList = new MyList(c.getKernel());
				for (int i = 0; i < c.getArgumentNumber(); i++) {
					argList.addListElement(c.getItem(i).traverse(this));
				}
				return new ExpressionNode(c.getKernel(), function,
						Operation.FUNCTION_NVAR, argList);
			}
			return ev;
		}

		/**
		 * @param app
		 *            application (needed to check which commands are valid)
		 * @param fn
		 *            functionName
		 * @param function
		 *            function
		 * @return replacer
		 */
		public static CommandFunctionReplacer getReplacer(App app, String fn,
				GeoElement function) {
			CommandFunctionReplacer replacer = new CommandFunctionReplacer();
			replacer.fn = fn;
			replacer.function = function;
			return replacer;
		}
	}

	/**
	 * Replaces all commands "ggbvect" by their first argument, sets the CAS
	 * vector flag for future serialization
	 *
	 */
	public class GgbVectRemover implements Traversing {

		private static final GgbVectRemover remover = new GgbVectRemover();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev instanceof Command) {
				Command command = (Command) ev;
				if (command.getName().equals("ggbvect")) {
					ExpressionNode en = command.getArgument(0);
					ExpressionValue unwrapped = en.unwrap();
					if (unwrapped instanceof MyVecNode) {
						MyVecNode vecNode = (MyVecNode) unwrapped;
						vecNode.setCASVector();
						return vecNode;
					} else if (unwrapped instanceof MyVec3DNode) {
						MyVec3DNode vec3DNode = (MyVec3DNode) unwrapped;
						vec3DNode.setCASVector();
						return vec3DNode;
					}
				}

			}
			return ev;
		}

		/**
		 * @return instance of this traversing
		 */
		public static GgbVectRemover getInstance() {
			return remover;
		}
	}

	/**
	 * Replaces variables and polynomials
	 *
	 */
	public class VariablePolyReplacer implements Traversing {
		private FunctionVariable fv;
		private int replacements;
		private static VariablePolyReplacer replacer = new VariablePolyReplacer();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if ((ev instanceof Variable || ev instanceof FunctionVariable
					|| ev instanceof GeoDummyVariable)
					&& fv.toString(StringTemplate.defaultTemplate).equals(
							ev.toString(StringTemplate.defaultTemplate))) {
				replacements++;
				return fv;
			}

			return ev;
		}

		/**
		 * @return number of replacements since getReplacer was called
		 */
		public int getReplacements() {
			return replacements;
		}

		/**
		 * @param fv
		 *            function variable
		 * @return replacer
		 */
		public static VariablePolyReplacer getReplacer(FunctionVariable fv) {
			replacer.fv = fv;
			return replacer;
		}
	}

	/**
	 * Replaces dummy variable with given name
	 *
	 */
	public class GeoDummyReplacer implements Traversing {
		private String var;
		private ExpressionValue newObj;
		private boolean didReplacement;
		private boolean replaceFVs;
		private static GeoDummyReplacer replacer = new GeoDummyReplacer();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			boolean hitClass = ev instanceof GeoDummyVariable
					|| (replaceFVs && ev instanceof FunctionVariable);
			if (!hitClass || !var
					.equals(ev.toString(StringTemplate.defaultTemplate))) {
				return ev;
			}
			didReplacement = true;
			return newObj;
		}

		/**
		 * @param varStr
		 *            variable name
		 * @param replacement
		 *            replacement object
		 * @param replaceFVs
		 *            whether function variables should be replaced also
		 * @return replacer
		 */
		public static GeoDummyReplacer getReplacer(String varStr,
				ExpressionValue replacement, boolean replaceFVs) {
			replacer.var = varStr;
			replacer.newObj = replacement;
			replacer.didReplacement = false;
			replacer.replaceFVs = replaceFVs;
			return replacer;
		}

		/**
		 * @return true if a replacement was done since getReplacer() call
		 */
		public boolean didReplacement() {
			return didReplacement;
		}
	}

	/**
	 * Replaces Variables with given name by given object
	 * 
	 * @author Zbynek Konecny
	 *
	 */
	public class VariableReplacer implements Traversing {
		private List<String> vars = new ArrayList<>();
		private List<ExpressionValue> newObjs = new ArrayList<>();
		private int replacements;
		private Kernel kernel;
		private static VariableReplacer replacer = new VariableReplacer();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			ExpressionValue val;
			if ((val = contains(ev)) != null) {
				return new ExpressionNode(kernel, val);
			}
			if (!(ev instanceof Variable || ev instanceof FunctionVariable
					|| ev instanceof GeoDummyVariable)) {
				return ev;
			}
			if ((val = getVar(
					ev.toString(StringTemplate.defaultTemplate))) == null) {
				return ev;
			}
			replacements++;
			return val;
		}

		/**
		 * @return number of replacements since getReplacer was called
		 */
		public int getReplacements() {
			return replacements;
		}

		private static ExpressionValue contains(ExpressionValue ev) {
			for (int i = 0; i < replacer.newObjs.size(); i++) {
				if (replacer.newObjs.get(i) == ev) {
					return replacer.newObjs.get(i);
				}
			}
			return null;
		}

		private static ExpressionValue getVar(String var) {
			for (int i = 0; i < replacer.vars.size(); i++) {
				if (var.equals(replacer.vars.get(i))) {
					return replacer.newObjs.get(i);
				}
			}
			return null;
		}

		/**
		 * @param varStr
		 *            variable name
		 * @param replacement
		 *            replacement object
		 */
		public static void addVars(String varStr, ExpressionValue replacement) {
			replacer.vars.add(varStr);
			replacer.newObjs.add(replacement);
		}

		/**
		 * @param varStr
		 *            variable name
		 * @param replacement
		 *            replacement object
		 * @param kernel
		 *            kernel
		 * @return replacer
		 */
		public static VariableReplacer getReplacer(String varStr,
				ExpressionValue replacement, Kernel kernel) {
			replacer.vars.clear();
			replacer.newObjs.clear();

			replacer.vars.add(varStr);
			replacer.newObjs.add(replacement);

			replacer.replacements = 0;
			replacer.kernel = kernel;
			return replacer;
		}

		/**
		 * When calling this method, make sure you initialize the replacer with
		 * the {@link #addVars(String, ExpressionValue)} method
		 * 
		 * @param kernel1
		 *            kernel
		 * 
		 * @return replacer
		 */
		public static VariableReplacer getReplacer(Kernel kernel1) {
			replacer.kernel = kernel1;
			replacer.vars.clear();
			replacer.newObjs.clear();

			replacer.replacements = 0;
			return replacer;
		}
	}

	/**
	 * Replaces GeoNumerics with given expression
	 */
	public class GeoNumericReplacer implements Traversing {
		private List<GeoNumeric> geoNums = new ArrayList<>();
		private List<ExpressionValue> newExps = new ArrayList<>();
		private int replacements;
		private Kernel kernel;
		private static GeoNumericReplacer replacer = new GeoNumericReplacer();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			ExpressionValue val;
			if ((val = contains(ev)) != null) {
				return new ExpressionNode(kernel, val);
			}
			if (!(ev instanceof GeoNumeric)) {
				return ev;
			}
			if ((val = getGeoNum((GeoNumeric) ev)) == null) {
				return ev;
			}
			replacements++;
			return val;
		}

		/**
		 * @return number of replacements since getReplacer was called
		 */
		public int getReplacements() {
			return replacements;
		}

		private static ExpressionValue contains(ExpressionValue ev) {
			for (int i = 0; i < replacer.newExps.size(); i++) {
				if (replacer.newExps.get(i) == ev) {
					return replacer.newExps.get(i);
				}
			}
			return null;
		}

		private static ExpressionValue getGeoNum(GeoNumeric geoNum) {
			for (int i = 0; i < replacer.geoNums.size(); i++) {
				if (geoNum.equals(replacer.geoNums.get(i))) {
					return replacer.newExps.get(i);
				}
			}
			return null;
		}

		/**
		 * @param geoNum
		 *            geoNumeric to replace
		 * @param replacement
		 *            replacement object
		 */
		public static void addVars(GeoNumeric geoNum,
				ExpressionValue replacement) {
			replacer.geoNums.add(geoNum);
			replacer.newExps.add(replacement);
		}

		/**
		 * @param geoNum
		 *            geoNum to replace
		 * @param replacement
		 *            replacement object
		 * @param kernel
		 *            kernel
		 * @return replacer
		 */
		public static GeoNumericReplacer getReplacer(GeoNumeric geoNum,
				ExpressionValue replacement, Kernel kernel) {
			replacer.geoNums.clear();
			replacer.newExps.clear();

			replacer.geoNums.add(geoNum);
			replacer.newExps.add(replacement);

			replacer.replacements = 0;
			replacer.kernel = kernel;
			return replacer;
		}

		/**
		 * When calling this method, make sure you initialize the replacer with
		 * the {@link #addVars(GeoNumeric, ExpressionValue)} method
		 * 
		 * @param kernel1
		 *            kernel
		 * 
		 * @return replacer
		 */
		public static GeoNumericReplacer getReplacer(Kernel kernel1) {
			replacer.kernel = kernel1;
			replacer.geoNums.clear();
			replacer.newExps.clear();

			replacer.replacements = 0;
			return replacer;
		}
	}

	/**
	 * Renames Spreadsheet Variables with new name according to offset (dx,dy)
	 * 
	 * @author michael
	 *
	 */
	public class SpreadsheetVariableRenamer implements Traversing {
		private int dx;
		private int dy;
		private ArrayList<Variable> variables = new ArrayList<>();

		/**
		 * Renames Spreadsheet Variables with new name according to offset
		 * (dx,dy)
		 * 
		 * @param dx
		 *            x-offset
		 * @param dy
		 *            y-offset
		 */
		public SpreadsheetVariableRenamer(int dx, int dy) {
			this.dx = dx;
			this.dy = dy;
			variables.clear();
		}

		@Override
		public ExpressionValue process(ExpressionValue ev) {

			// check variables to avoid problem with updating twice
			// eg If[0 < A1 < 5, 0, 100] going to If[0 < A3 < 5, 0, 100]
			if (ev instanceof Variable && !variables.contains(ev)) {
				Variable v = (Variable) ev;

				String name = v.getName(StringTemplate.defaultTemplate);

				// Log.debug("found VARIABLE: "+name);
				if (GeoElementSpreadsheet.spreadsheetPattern.test(name)) {

					String newName = RelativeCopy.updateCellNameWithOffset(name,
							dx, dy);

					// Log.debug("FOUND SPREADSHEET VARIABLE: "+name + " -> " +
					// newName);

					// make sure new cell is autocreated if it doesn't exist
					// already
					v.getKernel().getConstruction().lookupLabel(newName, true);

					// Log.debug("setting new name to: "+newName);

					v.setName(newName);
					variables.add(v);

				}
			} else if (ev instanceof GeoElement) {

				GeoElement geo = (GeoElement) ev;

				String name = geo.getLabelSimple();

				if (GeoElementSpreadsheet.spreadsheetPattern.test(name)) {

					String newName = RelativeCopy.updateCellNameWithOffset(name,
							dx, dy);

					// make sure new cell is autocreated if it doesn't exist
					// already
					// and return it
					return geo.getConstruction().lookupLabel(newName, true);

				}

			}

			return ev;
		}

	}

	/**
	 * Replaces undefined variables by sliders
	 * 
	 * @author michael
	 *
	 */
	public class ReplaceUndefinedVariables implements Traversing {
		private final Kernel kernel;
		private String[] except;
		private TreeSet<GeoNumeric> undefined;

		/**
		 * Replaces undefined variables by sliders
		 * 
		 * @param kernel
		 *            kernel
		 * @param undefined
		 *            list of undefined vars (write only)
		 * @param skip
		 *            list of labels to skip
		 * 
		 */
		public ReplaceUndefinedVariables(Kernel kernel,
				TreeSet<GeoNumeric> undefined, String[] skip) {
			this.kernel = kernel;
			this.undefined = undefined;
			this.except = skip;
		}

		@Override
		public ExpressionValue process(ExpressionValue ev) {

			if (ev instanceof Variable) {
				Variable v = (Variable) ev;
				String name = v.getName(StringTemplate.defaultTemplate);
				ExpressionValue replace = kernel.lookupLabel(name, true,
						kernel.isResolveUnkownVarsAsDummyGeos());
				if (replace == null) {
					replace = Variable.replacement(kernel, name);
				}
				if (replace instanceof Variable
						&& !name.equals(kernel.getConstruction()
								.getRegisteredFunctionVariable())
						&& !isException(name)) {
					name = ((Variable) replace)
							.getName(StringTemplate.defaultTemplate);
					boolean old = kernel.getConstruction()
							.isSuppressLabelsActive();
					kernel.getConstruction().setSuppressLabelCreation(false);
					GeoNumeric slider = new GeoNumeric(kernel.getConstruction(),
							1);
					slider.setLabel(name);
					kernel.getConstruction().setSuppressLabelCreation(old);
					undefined.add(slider);
					boolean visible = !kernel.getApplication()
							.showView(App.VIEW_ALGEBRA)
							|| kernel.getApplication()
									.showAutoCreatedSlidersInEV();
					GeoNumeric.setSliderFromDefault(slider, false, visible);
					return ev;
				}
			}

			return ev;
		}

		private boolean isException(String name) {
			if (except == null) {
				return false;
			}
			for (int i = 0; i < except.length; i++) {
				if (except[i].equals(name)) {
					return true;
				}
			}
			return false;
		}

	}

	/**
	 * Collect undefined variables
	 * 
	 * @author michael
	 *
	 */
	public class CollectUndefinedVariables implements Traversing {

		private TreeSet<String> tree = new TreeSet<>();
		private TreeSet<String> localTree = new TreeSet<>();

		/**
		 * 
		 * @return list of undefined variables (repeats removed)
		 */
		public TreeSet<String> getResult() {
			tree.removeAll(localTree);
			return tree;
		}

		@Override
		public ExpressionValue process(ExpressionValue ev) {

			if (ev instanceof Variable) {
				Variable v = (Variable) ev;
				String name = v.getName(StringTemplate.defaultTemplate);
				if (v.getKernel().getApplication().getParserFunctions()
						.isReserved(name)) {
					return ev;
				}
				ExpressionValue ret;
				ret = v.getKernel().lookupLabel(name);
				if (ret == null) {
					ret = Variable.replacement(v.getKernel(), name);
				}

				if (ret instanceof Variable && !v.getKernel().getConstruction()
						.isRegistredFunctionVariable(name)) {
					// Log.debug("found undefined variable: "
					// + ((Variable) ret)
					// .getName(StringTemplate.defaultTemplate));
					tree.add(((Variable) ret)
							.getName(StringTemplate.defaultTemplate));
				}
			} else if (ev instanceof Command) { // Iteration[a+1, a, {1},4]

				Command com = (Command) ev;
				if (("Sequence".equals(com.getName())
						&& com.getArgumentNumber() > 2)
						|| "KeepIf".equals(com.getName())
						|| "CountIf".equals(com.getName())) {
					localTree.add(com.getArgument(1)
							.toString(StringTemplate.defaultTemplate));
				} else if ("Surface".equals(com.getName())) {
					int len = com.getArgumentNumber();
					if (len > 6) {
						localTree.add(com.getArgument(len - 3)
								.toString(StringTemplate.defaultTemplate));
						localTree.add(com.getArgument(len - 6)
								.toString(StringTemplate.defaultTemplate));
					}
				} else if ("CurveCartesian".equals(com.getName())) {
					int len = com.getArgumentNumber();
					localTree.add(com.getArgument(len - 3)
							.toString(StringTemplate.defaultTemplate));
				} else if (("IterationList".equals(com.getName())
						|| "Iteration".equals(com.getName()))
						&& com.getArgumentNumber() > 3) {

					for (int i = 1; i < com.getArgumentNumber() - 2; i++) {
						localTree.add(com.getArgument(i)
								.toString(StringTemplate.defaultTemplate));
					}
				} else if ("Zip".equals(com.getName())) {
					for (int i = 1; i < com.getArgumentNumber(); i += 2) {
						localTree.add(com.getArgument(i)
								.toString(StringTemplate.defaultTemplate));
					}
				} else if ("TriangleCurve".equals(com.getName())) {
					localTree.add("A");
					localTree.add("B");
					localTree.add("C");
				}
			}
			return ev;
		}
	}

	/**
	 * Collect FunctionVariables
	 * 
	 * @author michael
	 *
	 */
	public class CollectFunctionVariables implements Traversing {

		private ArrayList<FunctionVariable> al = new ArrayList<>();

		/**
		 * 
		 * @return list of undefined variables (repeats removed)
		 */
		public ArrayList<FunctionVariable> getResult() {
			return al;
		}

		@Override
		public ExpressionValue process(ExpressionValue ev) {

			if (ev instanceof FunctionVariable) {
				al.add((FunctionVariable) ev);
			}

			return ev;
		}

	}

	/**
	 * Replaces powers by roots or vice versa
	 */
	public class PowerRootReplacer implements Traversing {
		private boolean toRoot;
		/** functions with 100th root are numerically unstable */
		private static int MAX_ROOT = 99;
		private static PowerRootReplacer replacer = new PowerRootReplacer();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (!ev.isExpressionNode()) {
				return ev;
			}
			((ExpressionNode) ev).replacePowersRoots(toRoot, MAX_ROOT);
			return ev;
		}

		/**
		 * @param toRoot
		 *            true to replace exponents by roots
		 * @return replacer
		 */
		public static PowerRootReplacer getReplacer(boolean toRoot) {
			replacer.toRoot = toRoot;
			return replacer;
		}
	}

	/**
	 * Replaces diff function comming from GIAC
	 * 
	 * @author Zbynek Konecny
	 *
	 */
	public class DiffReplacer implements Traversing {
		/**
		 * Singleton instance
		 */
		public static final DiffReplacer INSTANCE = new DiffReplacer();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (!ev.isExpressionNode()) {
				return ev;
			}
			ExpressionNode en = (ExpressionNode) ev;

			if (en.getOperation() != Operation.DIFF) {
				return ev;
			}
			Kernel kernel = en.getKernel();
			ExpressionValue expr = en.getLeft();
			ExpressionValue var = en.getRight();
			ExpressionValue deg;
			if (expr instanceof MyNumberPair) {

				var = ((MyNumberPair) expr).y;
				expr = ((MyNumberPair) expr).x;
				deg = en.getRight();
			} else {
				deg = new MyDouble(kernel, 1);
			}
			String expStr = expr.toString(StringTemplate.defaultTemplate);
			int nameEnd = expStr.indexOf('(');
			if (expStr.indexOf('[') > 0) {
				nameEnd = nameEnd > 0 ? Math.min(nameEnd, expStr.indexOf('['))
						: expStr.indexOf('[');
			}
			String funLabel = nameEnd > 0 ? expStr.substring(0, nameEnd)
					: expStr;

			ExpressionValue diffArg = new MyDouble(kernel, Double.NaN);
			ExpressionValue mult = new MyDouble(kernel, 1);
			if (expr.unwrap() instanceof Command) {
				diffArg = ((Command) expr.unwrap()).getArgument(0);
				if (diffArg.unwrap() instanceof FunctionVariable && diffArg
						.toString(StringTemplate.defaultTemplate)
						.equals(var.toString(StringTemplate.defaultTemplate))) {
					// keep mult
				} else if (DoubleUtil.isEqual(deg.evaluateDouble(), 1)) {
					CASGenericInterface cas = kernel.getGeoGebraCAS()
							.getCurrentCAS();
					Command derivCommand = new Command(kernel, "Derivative",
							false);
					derivCommand.addArgument(diffArg.wrap());
					derivCommand.addArgument(var.wrap());
					derivCommand.addArgument(deg.wrap());
					mult = cas.evaluateToExpression(derivCommand, null, kernel);
				} else {
					mult = new MyDouble(kernel, Double.NaN);
				}
			}

			// derivative of f gives f'
			ExpressionNode derivative = new ExpressionNode(kernel,
					new Variable(kernel, funLabel), // function label "f"
					Operation.DERIVATIVE, deg);
			// function of given variable gives f'(t)
			return new ExpressionNode(kernel, derivative, Operation.FUNCTION,
					diffArg).multiplyR(mult); // Variable
		}

	}

	/**
	 * Goes through the ExpressionValue and collects all derivatives from
	 * expression nodes into arrays
	 */
	public class PrefixRemover implements Traversing {
		private static PrefixRemover collector = new PrefixRemover();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev instanceof Variable) {
				return new Variable(((Variable) ev).getKernel(),
						ev.toString(StringTemplate.defaultTemplate)
								.replace(Kernel.TMP_VARIABLE_PREFIX, ""));
			}
			return ev;
		}

		/**
		 * Resets and returns the collector
		 * 
		 * @return derivative collector
		 */
		public static PrefixRemover getCollector() {
			return collector;
		}

	}

	/**
	 * Goes through the ExpressionValue and collects all derivatives from
	 * expression nodes into arrays
	 */
	public class CommandCollector implements Traversing {
		private Set<Command> commands;
		private static CommandCollector collector = new CommandCollector();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev instanceof Command) {
				commands.add((Command) ev);
			}
			return ev;
		}

		/**
		 * Resets and returns the collector
		 * 
		 * @param commands
		 *            set into which we want to collect the commands
		 * @return derivative collector
		 */
		public static CommandCollector getCollector(Set<Command> commands) {
			collector.commands = commands;
			return collector;
		}
	}

	/**
	 * Collects all function variables
	 * 
	 * @author Zbynek Konecny
	 */
	public class FVarCollector implements Traversing {
		private Set<String> commands;
		private static FVarCollector collector = new FVarCollector();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev instanceof Equation) {
				return ev.wrap();
			}
			if (ev instanceof FunctionVariable) {
				commands.add(((FunctionVariable) ev).getSetVarString());
			}
			if (ev instanceof ExpressionNode) {
				ExpressionNode en = ev.wrap();
				if (en.getOperation() != Operation.FUNCTION
						&& en.getOperation() != Operation.FUNCTION_NVAR
						&& en.getOperation() != Operation.VEC_FUNCTION) {

					checkFunctional(en.getLeft());
					checkFunctional(en.getRight());
				}
			}
			return ev;
		}

		private void checkFunctional(ExpressionValue right) {
			if (right instanceof FunctionalNVar) {
				for (FunctionVariable fv : ((FunctionalNVar) right)
						.getFunctionVariables()) {
					commands.add(fv.getSetVarString());
				}
			}

		}

		/**
		 * Resets and returns the collector
		 * 
		 * @param commands
		 *            set into which we want to collect the commands
		 * @return derivative collector
		 */
		public static FVarCollector getCollector(Set<String> commands) {
			collector.commands = commands;
			return collector;
		}
	}

	/**
	 * Collects all geos with multiplicities
	 * 
	 * @author Zoltan Kovacs
	 */
	public class GeoCollector implements Traversing {
		private HashMap<GeoElement, Integer> commands;
		private static GeoCollector collector = new GeoCollector();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev instanceof Equation) {
				return ev.wrap();
			}
			if (ev instanceof GeoElement) {
				int occurrence = 0;
				if (commands.containsKey(ev)) {
					occurrence = commands.get(ev);
				}
				commands.put((GeoElement) ev, occurrence + 1);
			}
			return ev;
		}

		/**
		 * Resets and returns the collector
		 * 
		 * @param commands
		 *            set into which we want to collect the commands
		 * @return derivative collector
		 */
		public static GeoCollector getCollector(
				HashMap<GeoElement, Integer> commands) {
			collector.commands = commands;
			return collector;
		}
	}

	/**
	 * Collects all function variables
	 * 
	 * @author Zbynek Konecny
	 */
	public class NonFunctionCollector implements Traversing {
		private Set<String> commands;
		private static NonFunctionCollector collector = new NonFunctionCollector();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev instanceof ExpressionNode) {
				ExpressionNode en = (ExpressionNode) ev;
				if (en.getRight() instanceof GeoDummyVariable) {
					add(((GeoDummyVariable) en.getRight()));
				}
				if (en.getOperation() == Operation.FUNCTION
						|| en.getOperation() == Operation.FUNCTION_NVAR
						|| en.getOperation() == Operation.DERIVATIVE) {
					return en;
				}
				if (en.getLeft() instanceof GeoDummyVariable) {
					add(((GeoDummyVariable) en.getLeft()));
				}
			}
			return ev;
		}

		private void add(GeoDummyVariable dummy) {
			String str = dummy.toString(StringTemplate.defaultTemplate);
			if (dummy.getKernel().getApplication().getParserFunctions()
					.isReserved(str)) {
				return;
			}
			commands.add(str);

		}

		/**
		 * Resets and returns the collector
		 * 
		 * @param commands
		 *            set into which we want to collect the commands
		 * @return derivative collector
		 */
		public static NonFunctionCollector getCollector(Set<String> commands) {
			collector.commands = commands;
			return collector;
		}
	}

	/**
	 * Collects all dummy variables and function variables except those that are
	 * in the role of a function eg. for f(x) we will collect x, but not f
	 * 
	 * @author Balazs Bencze
	 */
	public class DummyVariableCollector implements Traversing {
		private Set<String> commands;
		private static DummyVariableCollector collector = new DummyVariableCollector();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev instanceof ExpressionNode) {
				ExpressionNode en = (ExpressionNode) ev;
				if (isVariable(en.getRight())) {
					add(en.getRight());
				}
				if (en.getOperation() == Operation.FUNCTION
						|| en.getOperation() == Operation.FUNCTION_NVAR
						|| en.getOperation() == Operation.DERIVATIVE) {
					return en;
				}
				if (isVariable(en.getLeft())) {
					add(en.getLeft());
				}
			}
			return ev;
		}

		private static boolean isVariable(ExpressionValue right) {
			return right instanceof GeoDummyVariable
					|| right instanceof FunctionVariable
					|| right instanceof Variable;
		}

		private void add(ExpressionValue dummy) {
			String str = dummy.toString(StringTemplate.defaultTemplate);
			commands.add(str);

		}

		/**
		 * Resets and returns the collector
		 * 
		 * @param commands
		 *            set into which we want to collect the commands
		 * @return derivative collector
		 */
		public static DummyVariableCollector getCollector(
				Set<String> commands) {
			collector.commands = commands;
			return collector;
		}
	}

	/**
	 * Collects all GeoNumeric labels
	 */
	public class GeoNumericLabelCollector implements Traversing {
		private Set<String> labels;
		private static GeoNumericLabelCollector collector = new GeoNumericLabelCollector();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev instanceof ExpressionNode) {
				ExpressionNode en = (ExpressionNode) ev;
				if (en.getRight() instanceof GeoNumeric) {
					add(en.getRight());
				}
				if (en.getLeft() instanceof GeoNumeric) {
					add(en.getLeft());
				}
			}
			return ev;
		}

		private void add(ExpressionValue geoNum) {
			String str = ((GeoNumeric) geoNum).getLabelSimple();
			if (str != null) {
				labels.add(str);
			}

		}

		/**
		 * Resets and returns the collector
		 * 
		 * @param labels
		 *            set into which we want to collect the geoNumeric labels
		 * @return derivative collector
		 */
		public static GeoNumericLabelCollector getCollector(
				Set<String> labels) {
			collector.labels = labels;
			return collector;
		}
	}

	/**
	 * Replaces function calls by multiplications in cases where left argument
	 * is clearly not a function (see NonFunctionCollector)
	 * 
	 * @author Zbynek Konecny
	 */
	public class NonFunctionReplacer implements Traversing {
		private Set<String> commands;
		private static NonFunctionReplacer collector = new NonFunctionReplacer();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev instanceof ExpressionNode) {
				ExpressionNode en = (ExpressionNode) ev;
				if (en.getOperation() == Operation.POWER
						&& en.getLeft() instanceof Command) {
					Command c = (Command) en.getLeft();
					if (commands.contains(c.getName())) {
						return new GeoDummyVariable(
								c.getKernel().getConstruction(), c.getName())
										.wrap()
										.multiply(c.getArgument(0)
												.traverse(this).wrap()
												.power(en.getRight()));
					}
				}
				if (en.getOperation() == Operation.FACTORIAL
						&& en.getLeft() instanceof Command) {
					Command c = (Command) en.getLeft();
					if (commands.contains(c.getName())) {
						return new GeoDummyVariable(
								c.getKernel().getConstruction(), c.getName())
										.wrap().multiply(
												c.getArgument(0).traverse(this)
														.wrap().factorial());
					}
				}
				if (en.getOperation() == Operation.SQRT_SHORT
						&& en.getLeft() instanceof Command) {
					Command c = (Command) en.getLeft();
					if (commands.contains(c.getName())) {
						return new GeoDummyVariable(
								c.getKernel().getConstruction(), c.getName())
										.wrap().sqrt().multiply(c.getArgument(0)
												.traverse(this));
					}
				}
			}
			if (ev instanceof Command) {
				Command c = (Command) ev;
				if (commands.contains(c.getName())
						&& c.getArgumentNumber() == 1) {
					return new GeoDummyVariable(c.getKernel().getConstruction(),
							c.getName()).wrap()
									.multiply(c.getArgument(0).traverse(this));
				}
			}
			return ev;
		}

		/**
		 * Resets and returns the collector
		 * 
		 * @param commands
		 *            set into which we want to collect the commands
		 * @return derivative collector
		 */
		public static NonFunctionReplacer getCollector(Set<String> commands) {
			collector.commands = commands;
			return collector;
		}
	}

	/**
	 * Returns the RHS side of an equation if LHS is y. E.g. y=x! returns x!.
	 * 
	 * @author Balazs Bencze
	 */
	public class FunctionCreator implements Traversing {
		private static FunctionCreator creator = new FunctionCreator();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev instanceof Equation) {
				Equation eq = (Equation) ev;
				if (eq.getLHS() != null
						&& eq.getLHS().getLeft() instanceof GeoDummyVariable) {
					GeoDummyVariable gdv = (GeoDummyVariable) eq.getLHS()
							.getLeft();
					if (gdv.toString(StringTemplate.defaultTemplate)
							.equals("y")) {
						return eq.getRHS().unwrap();
					}
				}
			}
			return ev;
		}

		/**
		 * @return instance of FunctionCreater
		 */
		public static FunctionCreator getCreator() {
			return creator;
		}
	}

	/**
	 * Removes commands from a given expression and returns the first argument
	 * of the command
	 */
	public class CommandRemover implements Traversing {
		private static CommandRemover remover = new CommandRemover();
		private static String[] commands;

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev instanceof Command) {
				Command ec = (Command) ev;
				for (int i = 0; i < commands.length; i++) {
					if (ec.getName().equals(commands[i])) {
						return ec.getArgument(0).unwrap();
					}
				}
			}
			return ev;
		}

		/**
		 * Get the remover
		 * 
		 * @param commands1
		 *            commands to be removed
		 * @return command remover
		 */
		public static CommandRemover getRemover(String... commands1) {
			commands = commands1;
			return remover;
		}
	}

	/**
	 * Replaces some of the unknown commands from CAS to known
	 * commands/expression node values to GeoGebra
	 * 
	 * @author Balazs Bencze
	 */
	public class CASCommandReplacer implements Traversing {
		/**
		 * Replacer object
		 */
		public final static CASCommandReplacer replacer = new CASCommandReplacer();

		@Override
		public ExpressionValue process(ExpressionValue ev) {
			if (ev instanceof Command) {
				Command ec = (Command) ev;
				if ("x".equals(ec.getName())) {
					return new ExpressionNode(ec.getKernel(), ec.getArgument(0),
							Operation.XCOORD, null);
				} else if ("y".equals(ec.getName())) {
					return new ExpressionNode(ec.getKernel(), ec.getArgument(0),
							Operation.YCOORD, null);
				} else if ("z".equals(ec.getName())) {
					return new ExpressionNode(ec.getKernel(), ec.getArgument(0),
							Operation.ZCOORD, null);
				}

			}
			return ev;
		}

	}
}
