package de.uni_koblenz.jgralab.utilities.ecore2tg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.riediger.plist.PList;
import org.riediger.plist.PListDict;
import org.riediger.plist.PListException;

import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.structure.Comment;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;

public class Ecore2TgConfiguration {
	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------
	// -------User Options------------------------------------------------------
	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------

	// //////////////////////////////////////////////////////////////////////////
	// #//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#/
	// //////////////////////////////////////////////////////////////////////////
	// #//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#/
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Enumeration, defines how the transformation will happen
	 * */
	public enum TransformParams {

		// EdgeClasses are searched automatically
		// --and treated as ones
		AUTOMATIC_TRANSFORMATION,

		// EdgeClasses are searched automatically
		// --but the results are only printed
		// --to the console
		PRINT_PROPOSALS,

		// No EdgeClasses are searched
		JUST_LIKE_ECORE;
	}

	/**
	 * This map includes qualified names of EReferences and maps them to a
	 * direction value {@link TO} or {@link FROM}
	 * */
	private final HashMap<String, Integer> referencesWithDirections = new HashMap<String, Integer>();

	/**
	 * This map includes qualified names of an EReference or an EClass that
	 * should become EdgeClass and maps them to a user chosen name
	 * */
	private final HashMap<String, String> edgeNames = new HashMap<String, String>();

	/**
	 * Defines whether missing role names should become generated
	 */
	private boolean generateRoleNames = false;

	/**
	 * Determines if EBigInterger/EBigDecimal attributes should be transformed
	 * to Long/Double. If there are really values in the model that don't fit,
	 * an exception will be thrown when transforming the model.
	 */
	private boolean convertBigNumbers = false;

	/**
	 * This map includes qualified names of an EReference or an EClass that
	 * should become EdgeClass and maps them to the qualified name of the
	 * Package, the user wants the resulting EdgeClass to place
	 * */
	private final HashMap<String, String> reference2packagenameMap = new HashMap<String, String>();

	/**
	 * This map includes qualified names of EReferences and maps them to the
	 * qualified names of that EReferences that are overwritten by the Key
	 * EReferences ones
	 * */
	private final HashMap<String, String> erefs2overwritteneref = new HashMap<String, String>();

	/**
	 * Defines if the aggregation value has influence on the direction of the
	 * resulting EdgeClass Possible values are
	 * {@link NO_DIRECTION_FROM_AGGREGATION} {@link DIRECTION_WHOLE_TO_PART} or
	 * {@link DIRECTION_PART_TO_WHOLE}
	 * */
	private int aggregationInfluenceOnDirection = Ecore2Tg.NO_DIRECTION_FROM_AGGREGATION;

	/**
	 * Saves name of user defined GraphClass if there is one
	 * */
	private String nameOfEClassThatIsGraphClass = "";

	/**
	 * User determined Name of GraphClass, should be set if no EClass is chosen
	 * to become the GraphClass
	 */
	private String graphclassName;

	/**
	 * Name of the Schema
	 */
	private String schemaName = "";

	/**
	 * List of qualified names of EClasses that the user wants to become
	 * EdgeClasses
	 * */
	private final ArrayList<String> eclassesThatAreEdgeClasses = new ArrayList<String>();

	private TransformParams transopt = TransformParams.AUTOMATIC_TRANSFORMATION;

	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------
	// -------Getter/Setter for user influence----------------------------------
	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * @return name of the GraphClass
	 */
	public String getGraphclassName() {
		return this.graphclassName;
	}

	/**
	 * Sets the name which the resulting GraphClass should have
	 * 
	 * @param graphclassName
	 */
	public void setGraphclassName(String graphclassName) {
		this.graphclassName = graphclassName;
	}

	/**
	 * @return the convertBigNumbers
	 */
	public boolean isConvertBigNumbers() {
		return this.convertBigNumbers;
	}

	/**
	 * Convert EBigInteger/EBigDecimal to Long/Double, if
	 * <code>convertBigNumbers</code> is true.
	 * 
	 * @param convertBigNumbers
	 *            the convertBigNumbers to set
	 */
	public void setConvertBigNumbers(boolean convertBigNumbers) {
		this.convertBigNumbers = convertBigNumbers;
	}

	/**
	 * Returns a list with the qualified names of all EClasses that should
	 * become EdgeClasses
	 * 
	 * @return ArrayList with qualified names of EClasses that are conceptual
	 *         EdgeClasses
	 * */
	public ArrayList<String> getEdgeClassesList() {
		return this.eclassesThatAreEdgeClasses;
	}

	/**
	 * Sets the qualified name of that EClass that should become the GraphClass.
	 * 
	 * @param qualifiedEClassName
	 *            name of an EClass that is the conceptual GraphClass
	 * */
	public void setAsGraphClass(String qualifiedEClassName) {
		this.nameOfEClassThatIsGraphClass = qualifiedEClassName;
	}

	public String getAsGraphClass() {
		return this.nameOfEClassThatIsGraphClass;
	}

	/**
	 * Sets the qualified name of the schema
	 * 
	 * @param qualifiedEPackageName
	 *            name of an EPackage that is the root package of the metamodel
	 * */
	public void setSchemaName(String name) {
		this.schemaName = name;
	}

	public String getSchemaName() {
		return this.schemaName;
	}

	/**
	 * Sets the influence of aggregation on the direction of resulting
	 * EdgeClasses. Valid values are Ecore2Tg.NO_DIRECTION_FROM_AGGREGATION,
	 * Ecore2Tg.DIRECTION_PART_TO_WHOLE and Ecore2Tg.DIRECTION_WHOLE_TO_PART
	 * 
	 * @param i
	 *            integer value setting aggregation direction influence
	 * */
	public void setAggregationInfluenceOnDirection(int i) {
		if ((i != Ecore2Tg.DIRECTION_PART_TO_WHOLE)
				&& (i != Ecore2Tg.DIRECTION_WHOLE_TO_PART)
				&& (i != Ecore2Tg.NO_DIRECTION_FROM_AGGREGATION)) {
			System.err.println("Warning: Setting of aggregation influence on "
					+ "direction was not successful because " + i
					+ " is not a valid value. Valid values are"
					+ " Ecore2Tg.NO_DIRECTION_FROM_AGGREGATION, "
					+ "Ecore2Tg.DIRECTION_PART_TO_WHOLE and "
					+ "Ecore2Tg.DIRECTION_WHOLE_TO_PART.");
		} else {
			this.aggregationInfluenceOnDirection = i;
		}
	}

	public int getAggregationInfluenceOnDirection() {
		return this.aggregationInfluenceOnDirection;
	}

	/**
	 * Returns a map with pairs of qualified names of EReferences and EPackages.
	 * The EdgeClass resulting from transforming the specified EReference will
	 * become placed into the Package resulting from transforming the EPackage.
	 * 
	 * @return HashMap map with pairs of qualified names of EReferences and
	 *         EPackages
	 * */
	public HashMap<String, String> getDefinedPackagesOfEdgeClassesMap() {
		return this.reference2packagenameMap;
	}

	/**
	 * Returns a map with pairs of qualified names of EReferences and
	 * directions. This map defines which direction the EdgeClass resulting from
	 * the transformation of the EReference will be. As directions the constants
	 * Ecore2Tg.TO and Ecore2Tg.FROM are accepted.
	 * 
	 * @return HashMap map with pairs of EReferences and directions
	 * */
	public HashMap<String, Integer> getDirectionMap() {
		return this.referencesWithDirections;
	}

	/**
	 * Returns a map with pairs of qualified names. The key should be the
	 * qualified name of an EReference. The second name can become chosen
	 * freely. The EdgeClass resulting from the specified EReference will become
	 * the specified name from the value part.
	 * 
	 * @return HashMap map with pairs of qualified names
	 * */
	public HashMap<String, String> getNamesOfEdgeClassesMap() {
		return this.edgeNames;
	}

	/**
	 * Specifies whether missing role names should become generated.
	 * 
	 * @param b
	 *            defines whether missing role names should become generated
	 * */
	public void setGenerateRoleNames(boolean b) {
		this.generateRoleNames = b;
	}

	public boolean getGenerateRoleNames() {
		return this.generateRoleNames;
	}

	/**
	 * Returns a map with pairs of qualified names of EReferences. The
	 * EReference specified in the key part overwrites the EReference specified
	 * in the value part.
	 * 
	 * @return HashMap map with pairs of qualified EReference names
	 * */
	public HashMap<String, String> getPairsOfOverwritingEReferences() {
		return this.erefs2overwritteneref;
	}

	/**
	 * Specifies whether the program should look for conceptual EdgeClasses
	 * automatically.
	 * 
	 * @param t
	 *            valid values are Ecore2Tg.TransformParams.JUST_LIKE_ECORE
	 *            Ecore2Tg.TransformParams.PRINT_PROPOSALS and
	 *            Ecore2Tg.TransformParams.AUTOMATIC_TRANSFORMATION
	 * */
	public void setTransformationOption(TransformParams t) {
		this.transopt = t;
	}

	public TransformParams getTransformationOption() {
		return this.transopt;
	}

	protected void addConfigurationAsComment(SchemaGraph schemagraph,
			GraphClass defpack) {

		// Transformation Option
		Comment co = schemagraph.createComment();
		co.set_text(EAnnotationKeys.ECORE_2_TG_CONFIG_FLAG
				+ " transformation mode: " + this.transopt.toString());
		defpack.add_comment(co);

		// root Package
		if ((this.schemaName != null) && !this.schemaName.equals("")) {
			Comment c = schemagraph.createComment();
			c.set_text(EAnnotationKeys.ECORE_2_TG_CONFIG_FLAG
					+ " name of taken root package: " + this.schemaName);
			defpack.add_comment(c);
		}

		// Graphclass
		if ((this.nameOfEClassThatIsGraphClass != null)
				&& !this.nameOfEClassThatIsGraphClass.equals("")) {
			Comment c = schemagraph.createComment();
			c.set_text(EAnnotationKeys.ECORE_2_TG_CONFIG_FLAG
					+ " name of eclass transformed to graphclass: "
					+ this.nameOfEClassThatIsGraphClass);
			defpack.add_comment(c);
		}

		// EdgeClass declaration
		if ((this.eclassesThatAreEdgeClasses != null)
				&& !this.eclassesThatAreEdgeClasses.isEmpty()) {
			Comment c = schemagraph.createComment();
			String text = EAnnotationKeys.ECORE_2_TG_CONFIG_FLAG
					+ " declared conceptual edgeclasses: ";
			for (String ec : this.eclassesThatAreEdgeClasses) {
				text += ec + ", ";
			}
			c.set_text(text.substring(0, text.length() - 2));
			defpack.add_comment(c);
		}

		// Aggregation Influence
		if (this.aggregationInfluenceOnDirection != Ecore2Tg.NO_DIRECTION_FROM_AGGREGATION) {
			Comment c = schemagraph.createComment();
			if (this.aggregationInfluenceOnDirection == Ecore2Tg.DIRECTION_PART_TO_WHOLE) {
				c.set_text(EAnnotationKeys.ECORE_2_TG_CONFIG_FLAG
						+ " aggregation influence DIRECTION_PART_TO_WHOLE");
			} else {
				c.set_text(EAnnotationKeys.ECORE_2_TG_CONFIG_FLAG
						+ " aggregation influence DIRECTION_WHOLE_TO_PART");
			}
			defpack.add_comment(c);
		}

		// Directions
		if ((this.referencesWithDirections != null)
				&& !this.referencesWithDirections.isEmpty()) {
			Comment c = schemagraph.createComment();
			String text = EAnnotationKeys.ECORE_2_TG_CONFIG_FLAG
					+ " direction of references : ";
			for (String re : this.referencesWithDirections.keySet()) {
				text += re;
				if (this.referencesWithDirections.get(re) == Ecore2Tg.TO) {
					text += " TO, ";
				} else {
					text += " FROM, ";
				}
			}
			c.set_text(text.substring(0, text.length() - 2));
			defpack.add_comment(c);
		}

		// names of edgeclasses
		if ((this.edgeNames != null) && !this.edgeNames.isEmpty()) {
			Comment c = schemagraph.createComment();
			String text = EAnnotationKeys.ECORE_2_TG_CONFIG_FLAG
					+ " names of edgeclasses"
					+ " resulting from the following ereferences: ";
			for (String str : this.edgeNames.keySet()) {
				text += str + " results in " + this.edgeNames.get(str) + ", ";
			}
			c.set_text(text.substring(0, text.length() - 2));
			defpack.add_comment(c);
		}

		// generate role names
		if (this.generateRoleNames) {
			Comment c = schemagraph.createComment();
			c.set_text(EAnnotationKeys.ECORE_2_TG_CONFIG_FLAG
					+ " rolenames are created");
			defpack.add_comment(c);
		}

		// declare packages
		if ((this.reference2packagenameMap != null)
				&& !this.reference2packagenameMap.isEmpty()) {
			Comment c = schemagraph.createComment();
			String text = EAnnotationKeys.ECORE_2_TG_CONFIG_FLAG
					+ " packages for edgeclasses: ";
			for (String str : this.reference2packagenameMap.keySet()) {
				text += str + " belongs to "
						+ this.reference2packagenameMap.get(str) + ", ";
			}
			c.set_text(text.substring(0, text.length() - 2));
			defpack.add_comment(c);
		}

		// overwriting references
		if ((this.erefs2overwritteneref != null)
				&& !this.erefs2overwritteneref.isEmpty()) {
			Comment c = schemagraph.createComment();
			String text = EAnnotationKeys.ECORE_2_TG_CONFIG_FLAG
					+ " overwriting ereferences: ";
			for (String str : this.erefs2overwritteneref.keySet()) {
				text += str + " overwrites "
						+ this.erefs2overwritteneref.get(str) + ", ";
			}
			c.set_text(text.substring(0, text.length() - 2));
			defpack.add_comment(c);
		}
	}

	public void saveConfigurationToFile(String uri) {
		PList x = new PList();
		PListDict ds = x.getDict();

		// GraphClass
		if (this.nameOfEClassThatIsGraphClass != null
				&& !this.nameOfEClassThatIsGraphClass.equals("")) {
			ds.put("graphclass", this.nameOfEClassThatIsGraphClass);
		}

		// EdgeClasses
		if (this.eclassesThatAreEdgeClasses != null
				&& !this.eclassesThatAreEdgeClasses.isEmpty()) {
			Vector<String> vec = new Vector<String>();
			vec.addAll(this.eclassesThatAreEdgeClasses);
			ds.put("edgeclasses", vec);
		}

		// Direction out of aggregation
		if (this.aggregationInfluenceOnDirection == Ecore2Tg.DIRECTION_PART_TO_WHOLE) {
			ds.put("edgeclassdirection_aggregation",
					"aggregation_part_to_wohle");
		} else if (this.aggregationInfluenceOnDirection == Ecore2Tg.DIRECTION_WHOLE_TO_PART) {
			ds.put("edgeclassdirection_aggregation",
					"aggregation_whole_to_part");
		}

		// Direction direct
		if (!this.referencesWithDirections.isEmpty()) {
			Vector<String> vec = new Vector<String>();
			for (String ref : this.referencesWithDirections.keySet()) {
				String dir = this.referencesWithDirections.get(ref) == Ecore2Tg.FROM ? "FROM"
						: "TO";
				vec.add(ref + "," + dir);
			}
			ds.put("edgeclassdirection_reference_specific", vec);

		}

		// EdgeClass names
		if (!this.edgeNames.isEmpty()) {
			Vector<String> vec = new Vector<String>();
			for (String refName : this.edgeNames.keySet()) {
				vec.add(refName + "," + this.edgeNames.get(refName));
			}
		}

		// Generate rolenames
		if (this.generateRoleNames) {
			ds.put("generate_role_names", true);
		}

		// Package names
		if (!this.reference2packagenameMap.isEmpty()) {
			Vector<String> vec = new Vector<String>();
			for (String refName : this.reference2packagenameMap.keySet()) {
				vec.add(refName + ","
						+ this.reference2packagenameMap.get(refName));
			}
			ds.put("reference_to_packagename", vec);
		}

		// Overwritten EReferences
		if (!this.erefs2overwritteneref.isEmpty()) {
			Vector<String> vec = new Vector<String>();
			for (String refName : this.erefs2overwritteneref.keySet()) {
				vec.add(refName + "," + this.erefs2overwritteneref.get(refName));
			}
			ds.put("reference_overwrites_reference", vec);
		}

		// Search for edgeclasses
		if (!this.transopt.equals(TransformParams.JUST_LIKE_ECORE)) {
			if (this.transopt.equals(TransformParams.AUTOMATIC_TRANSFORMATION)) {
				ds.put("search_for_edge_classes", "take_automatic");
			} else if (this.transopt.equals(TransformParams.PRINT_PROPOSALS)) {
				ds.put("search_for_edge_classes", "print_proposals");
			}
		}

		try {
			x.storeTo(uri);
		} catch (PListException e) {
			System.err.println("Error while saving configuration to " + uri
					+ ".");
			e.printStackTrace();
		}
	}

	// ------------------------------------------------------

	/**
	 * Loads a configuration file from the give URI and adds the content to the
	 * options
	 * 
	 * @param uri
	 *            path to configuration file
	 * */
	@SuppressWarnings("unchecked")
	public static Ecore2TgConfiguration loadConfigurationFromFile(String uri) {
		PList x = new PList();
		try {
			x.loadFrom(uri);
		} catch (PListException e) {
			System.err
					.println("Error while loading the configuration file with uri "
							+ uri);
			e.printStackTrace();
			return null;
		}
		PListDict ds = x.getDict();

		Ecore2TgConfiguration conf = new Ecore2TgConfiguration();

		// GraphClass
		String name = (String) ds.get("graphclass");
		if (name != null) {
			conf.nameOfEClassThatIsGraphClass = name;
		}

		/*
		 * // Root Package String rpname = (String) ds.get("rootpackage"); if
		 * (rpname != null) { this.setRootPackageOfMetamodel(rpname); }
		 */

		// EdgeClasses
		Object conn = ds.get("edgeclasses");
		if (conn instanceof Vector) {
			Vector<String> con = (Vector<String>) conn;
			if (con != null) {
				conf.eclassesThatAreEdgeClasses.addAll(con);
			}
		}

		// Direction out of aggregation
		String aggr_dir = (String) ds.get("edgeclassdirection_aggregation");
		if (aggr_dir != null) {
			if (aggr_dir.equals("aggregation_part_to_wohle")) {
				conf.aggregationInfluenceOnDirection = Ecore2Tg.DIRECTION_PART_TO_WHOLE;
			} else if (aggr_dir.equals("aggregation_whole_to_part")) {
				conf.aggregationInfluenceOnDirection = Ecore2Tg.DIRECTION_WHOLE_TO_PART;
			} else {
				System.err
						.println("Configuration file "
								+ uri
								+ " contains invalid direction determination based on aggregation: "
								+ aggr_dir
								+ "\n Valid values are \"aggregation_part_to_wohle\" and \"aggregation_wohle_to_part\"");
			}
		}

		// Direction direct
		Object dirr = ds.get("edgeclassdirection_reference_specific");
		if (dirr instanceof Vector) {
			Vector<String> dir = (Vector<String>) dirr;
			if (dir != null) {
				for (String s : dir) {
					String intval = s.substring(s.indexOf(',') + 1);
					int direction = -1;
					if (intval.equals("FROM")) {
						direction = Ecore2Tg.FROM;
					} else if (intval.equals("TO")) {
						direction = Ecore2Tg.TO;
					} else {
						System.err
								.println("Configuration file "
										+ uri
										+ " contains invalid direction determination based on references: "
										+ intval + " instead of FROM or TO.");
						continue;
					}
					conf.getDirectionMap().put(s.substring(0, s.indexOf(',')),
							direction);
				}
			}
		}

		// Names
		Object ref2namess = ds.get("reference_to_edgeclassname");
		if (ref2namess instanceof Vector) {
			Vector<String> ref2names = (Vector<String>) ref2namess;
			if (ref2names != null) {
				for (String s : ref2names) {
					conf.edgeNames.put(s.substring(0, s.indexOf(',')),
							s.substring(s.indexOf(',') + 1));
				}
			}
		}

		// Rolenames
		if (ds.containsKey("generate_role_names")) {
			Boolean b = ds.getBoolean("generate_role_names");
			conf.generateRoleNames = b;
		}

		// Packages
		Object ref2paa = ds.get("reference_to_packagename");
		if (ref2paa instanceof Vector) {
			Vector<String> ref2pa = (Vector<String>) ref2paa;
			if (ref2pa != null) {
				for (String s : ref2pa) {
					conf.reference2packagenameMap.put(
							s.substring(0, s.indexOf(',')),
							s.substring(s.indexOf(',') + 1));
				}
			}
		}
		// Overwritten EReferences
		Object ref2reff = ds.get("reference_overwrites_reference");
		if (ref2reff instanceof Vector) {
			Vector<String> ref2ref = (Vector<String>) ref2reff;
			if (ref2ref != null) {
				for (String s : ref2ref) {
					conf.erefs2overwritteneref.put(
							s.substring(0, s.indexOf(',')),
							s.substring(s.indexOf(',') + 1));
				}
			}
		}
		// Transform Parameter
		String transStr = (String) ds.get("search_for_edge_classes");
		TransformParams tp = TransformParams.JUST_LIKE_ECORE;
		if (transStr != null) {
			if (transStr.equals("take_automatic")) {
				tp = TransformParams.AUTOMATIC_TRANSFORMATION;
			} else if (transStr.equals("print_proposals")) {
				tp = TransformParams.PRINT_PROPOSALS;
			} else {
				System.err
						.println("Configuration file "
								+ uri
								+ " contains invalid transformation parameter: "
								+ transStr
								+ "\n Valid values are \"take_automatic\" and \"print_proposals\"");

			}
			conf.transopt = tp;
		}
		return conf;
	}

}
