package de.uni_koblenz.jgralab.utilities.ecore2tg;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.pcollections.ArrayPSet;
import org.pcollections.ArrayPVector;
import org.pcollections.PCollection;
import org.pcollections.PVector;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.ListDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.RecordDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.SetDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.AggregationKind;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.Comment;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.EndsAt;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.IncidenceClass;
import de.uni_koblenz.jgralab.grumlschema.structure.NamedElement;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration.TransformParams;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.SchemaGraph2Schema;

public class Ecore2Tg {

	/**
	 * Options for Command-Line-Tool
	 * */
	private static final String OPTION_FILENAME_METAMODEL = "i";
	private static final String OPTION_FILENAME_SCHEMA = "o";
	private static final String OPTION_FILENAME_CONFIG = "c";
	private static final String OPTION_FILENAME_MODEL = "m";

	private static final String OPTION_IS_GRAPHCLASS = "g";
	private static final String OPTION_IS_EDGECLASS = "e";

	private static final String OPTION_SEARCH_EDGECLASSES = "s";
	private static final String OPTION_PRINT_POSSIBLE_EDGECLASSES = "t";

	private static final String OPTION_AGGREGATION_INFLUENCE = "a";

	private static final String OPTION_PACKAGE_OF_EDGE_CLASS = "p";
	private static final String OPTION_EDGE_CLASS_DIRECTION = "d";
	private static final String OPTION_EDGE_CLASS_NAME = "n";

	private static final String OPTION_GENERATE_ROLE_NAME = "r";

	private static final String OPTION_OVERWRITES = "x";

	// private static final String OPTION_ROOT_PACKAGE = "w";

	private static final String OPTION_CONVERT_BIG_NUMBERS = "b";

	private static final String OPTION_GRAPHCLASS_NAME = "gn";
	private static final String OPTION_SCHEMA_NAME = "sn";

	/**
	 * Processes an Ecore-File to a TG-File as schema and optional a matching
	 * Model to a TG-File as graph
	 * 
	 * @param args
	 *            array of command line options
	 * @throws IOException
	 *             GraphIOException
	 * */
	public static void main(String[] args) throws IOException, GraphIOException {

		System.out.println("Ecore to TG");
		System.out.println("=========");
		JGraLab.setLogLevel(Level.OFF);

		// Retrieving all command line options
		CommandLine cli = processCommandLineOptions(args);

		assert cli != null : "No CommandLine object has been generated!";

		// Getting name of input file
		Ecore2Tg ecore2tg = new Ecore2Tg(
				cli.getOptionValue(OPTION_FILENAME_METAMODEL));

		// Getting name of config file
		String configfile = cli.getOptionValue(OPTION_FILENAME_CONFIG);
		if (configfile != null) {
			Ecore2TgConfiguration conf = Ecore2TgConfiguration
					.loadConfigurationFromFile(configfile);
			ecore2tg.setConfiguration(conf);
		}

		// Getting name of graphclass if an eclass is set
		String graphclassname = cli.getOptionValue(OPTION_IS_GRAPHCLASS);
		if (graphclassname != null) {
			ecore2tg.getConfiguration().setAsGraphClass(graphclassname);
		}

		// Getting name of graphclass
		String graphclassn = cli.getOptionValue(OPTION_GRAPHCLASS_NAME);
		if (graphclassn != null) {
			ecore2tg.getConfiguration().setGraphclassName(graphclassn);
		}

		// Getting names of EdgeClasses
		String[] edgenames = cli.getOptionValues(OPTION_EDGE_CLASS_NAME);
		if (edgenames != null) {
			for (int i = 0; i < edgenames.length; i += 2) {
				ecore2tg.getConfiguration().getNamesOfEdgeClassesMap()
						.put(edgenames[i], edgenames[i + 1]);
			}
		}

		if (cli.hasOption(OPTION_GENERATE_ROLE_NAME)) {
			ecore2tg.getConfiguration().setGenerateRoleNames(true);
		}
		/*
		 * String rootp = cli.getOptionValue(OPTION_ROOT_PACKAGE); if (rootp !=
		 * null) { ecore2tg.setRootPackageOfMetamodel(rootp); }
		 */

		// Aggregation Influence
		String aggVal = cli.getOptionValue(OPTION_AGGREGATION_INFLUENCE);
		if (aggVal != null) {
			int i = Integer.parseInt(aggVal);
			ecore2tg.getConfiguration().setAggregationInfluenceOnDirection(i);
		}

		// Directions of EdgeClasses
		String[] directOfEC = cli.getOptionValues(OPTION_EDGE_CLASS_DIRECTION);
		if (directOfEC != null) {
			for (int i = 0; i < directOfEC.length; i += 2) {
				String intval = directOfEC[i + 1];
				int direction = -1;
				if (intval.equals("FROM")) {
					direction = Ecore2TgConfiguration.FROM;
				} else if (intval.equals("TO")) {
					direction = Ecore2TgConfiguration.TO;
				} else {
					System.err
							.println("Warning: Direction determination failed for "
									+ directOfEC[i] + ".");
					continue;
				}
				ecore2tg.getConfiguration().getDirectionMap()
						.put(directOfEC[i], direction);
			}
		}

		// EdgeClasses
		String[] edgeCs = cli.getOptionValues(OPTION_IS_EDGECLASS);
		if (edgeCs != null) {
			for (String e : edgeCs) {
				ecore2tg.getConfiguration().getEdgeClassesList().add(e);
			}
		}

		// Packages of EdgeClasses
		String[] packs = cli.getOptionValues(OPTION_PACKAGE_OF_EDGE_CLASS);
		if (packs != null) {
			for (int i = 0; i < packs.length; i += 2) {
				ecore2tg.getConfiguration()
						.getDefinedPackagesOfEdgeClassesMap()
						.put(packs[i], packs[i + 1]);
			}
		}

		// Overwriting EReferences
		String[] over = cli.getOptionValues(OPTION_OVERWRITES);
		if (over != null) {
			for (int i = 0; i < over.length; i += 2) {
				ecore2tg.getConfiguration().getPairsOfOverwritingEReferences()
						.put(over[i], over[i + 1]);
			}
		}

		// Look if the program should search for EdgeClasses
		TransformParams para;
		if (cli.hasOption(OPTION_SEARCH_EDGECLASSES)) {
			para = TransformParams.AUTOMATIC_TRANSFORMATION;
		} else if (cli.hasOption(OPTION_PRINT_POSSIBLE_EDGECLASSES)) {
			para = TransformParams.PRINT_PROPOSALS;
		} else {
			para = TransformParams.JUST_LIKE_ECORE;
		}
		ecore2tg.getConfiguration().setTransformationOption(para);

		// Should we try to convert EBigInteger/EBigDecimal to Long/Double?
		ecore2tg.getConfiguration().setConvertBigNumbers(
				cli.hasOption(OPTION_CONVERT_BIG_NUMBERS));

		// Take Schemaname
		String schemNa = cli.getOptionValue(OPTION_SCHEMA_NAME);

		// Start transformation
		ecore2tg.transform(schemNa);

		// Getting name of output file
		String outputFile = cli.getOptionValue(OPTION_FILENAME_SCHEMA);
		// If there is a model, transform and save it with the schema
		String modelFiles[] = cli.getOptionValues(OPTION_FILENAME_MODEL);

		de.uni_koblenz.jgralab.schema.Schema tgSchema = ecore2tg.schem;

		if (outputFile != null) {
			if (modelFiles == null) {
				GraphIO.saveSchemaToFile(tgSchema, outputFile);
			} else if (modelFiles != null) {
				Graph g = ecore2tg.transformModel(modelFiles);
				GraphIO.saveGraphToFile(g, outputFile,
						new ConsoleProgressFunction());
			}
		}

		// Create a jar if demanded
		if (cli.hasOption('j')) {
			System.out.println("Saving schema classes to "
					+ cli.getOptionValue('j'));
			tgSchema.createJAR(CodeGeneratorConfiguration.NORMAL,
					cli.getOptionValue('j'));
		}

	}

	/**
	 * Processes all command line parameters and returns a {@link CommandLine}
	 * object, which holds all values included in the given {@link String}
	 * array.
	 * 
	 * @param args
	 *            {@link CommandLine} parameters.
	 * @return {@link CommandLine} object, which holds all necessary values.
	 */
	public static CommandLine processCommandLineOptions(String[] args) {

		// Creates a OptionHandler.
		String toolString = "java " + Ecore2Tg.class.getName();
		String versionString = JGraLab.getInfo(false);

		OptionHandler oh = new OptionHandler(toolString, versionString);

		// Several Options are declared.

		// OPTION_FILENAME_METAMODEL = "i";
		Option input = new Option(OPTION_FILENAME_METAMODEL, "input", true,
				"(required): Ecore Metamodel file of the Schema.");
		input.setRequired(true);
		input.setArgName("filename");
		oh.addOption(input);

		// OPTION_FILENAME_SCHEMA = "o";
		Option output = new Option(
				OPTION_FILENAME_SCHEMA,
				"output",
				true,
				"(optional): writes a TG-file of the Schema to the given filename. "
						+ "If a model is given, it saves the Graph with the Schema to the filename. "
						+ "Free naming, but should look like this: '<filename>.tg.'");
		output.setRequired(false);
		output.setArgName("filename");
		oh.addOption(output);

		// OPTION_FILENAME_CONFIG = "c";
		Option config = new Option(OPTION_FILENAME_CONFIG, "configuration",
				true,
				"(optional): loads configurations from the given filename. ");
		config.setRequired(false);
		config.setArgName("filename");
		oh.addOption(config);

		// OPTION_FILENAME_MODEL = "m";
		Option modelfile = new Option(OPTION_FILENAME_MODEL, "modelfilename",
				true,
				"(optional): filename of a corresponding model file that beomes transformed");
		modelfile.setRequired(false);
		modelfile.setArgName("filename");
		oh.addOption(modelfile);

		// OPTION_SCHEMA_NAME = "sn";
		Option schema_name = new Option(OPTION_SCHEMA_NAME, "schema-name",
				true,
				"(required): Name of the resulting schema in the form prefix.name");
		schema_name.setRequired(true);
		schema_name.setArgName("schema name");
		oh.addOption(schema_name);

		// OPTION_GRAPHCLASS_NAME = "gn";
		Option graphclass_name = new Option(
				OPTION_GRAPHCLASS_NAME,
				"graphclass-name",
				true,
				"(optional): Name of the resulting graphclass, required if graphclass-prototype is not set");
		graphclass_name.setArgName("name of graphclass");

		// OPTION_IS_GRAPHCLASS = "g";
		Option isGraphclass = new Option(
				OPTION_IS_GRAPHCLASS,
				"graphclass-prototype",
				true,
				"(optional): qualified name of EClass that should become the GraphClass, required if graphclass-name is not set");
		isGraphclass.setArgName("qualifiedEClassName");

		// OptionGroup
		OptionGroup gcnGroup = new OptionGroup();
		gcnGroup.addOption(graphclass_name);
		gcnGroup.addOption(isGraphclass);
		gcnGroup.setRequired(true);
		oh.addOptionGroup(gcnGroup);

		// OPTION_IS_EDGECLASS = "e";
		Option isEdgeclass = new Option(OPTION_IS_EDGECLASS,
				"edgeclass-prototype", true,
				"(optional): qualified name of EClass that should become an EdgeClass");
		isEdgeclass.setRequired(false);
		isEdgeclass.setArgName("qualifiedEClassName");
		oh.addOption(isEdgeclass);

		// OPTION_SEARCH_EDGECLASSES = "s";
		Option search = new Option(OPTION_SEARCH_EDGECLASSES,
				"searchAutomaticAfterEdgeClasses", false,
				"(optional): if this flag is set, EdgeClasses are automatically searched"
						+ "\n (defaults to false");
		// search.setRequired(false);
		// oh.addOption(search);

		// OPTION_PRINT_POSSIBLE_EDGECLASSES = "t";
		Option print = new Option(OPTION_PRINT_POSSIBLE_EDGECLASSES,
				"printFoundEdgeClasses", false,
				"(optional): if this flag is set, EdgeClasses proposals are printed"
						+ "\n (defaults to false)");
		// print.setRequired(false);
		// oh.addOption(print);

		// OptionGroup for Transformation Params
		OptionGroup transParamGroup = new OptionGroup();
		transParamGroup.setRequired(false);
		transParamGroup.addOption(search);
		transParamGroup.addOption(print);
		oh.addOptionGroup(transParamGroup);

		// OPTION_AGGREGATION_INFLUENCE = "a";
		Option aggreg = new Option(
				OPTION_AGGREGATION_INFLUENCE,
				"influenceOfAggregation",
				true,
				"(optional): 1 specifies direction whole to part, 2 specifies direction part to whole"
						+ "\n (defaults to NONE)");
		aggreg.setRequired(false);
		aggreg.setArgName("1or2");
		oh.addOption(aggreg);

		// OPTION_PACKAGE_OF_EDGE_CLASS ="p";
		Option packn = new Option(OPTION_PACKAGE_OF_EDGE_CLASS,
				"definePackageOfEdgeClass", true,
				"(optional): defines a package for an EdgeClass");
		packn.setRequired(false);
		packn.setArgs(2);
		packn.setValueSeparator(' ');
		packn.setArgName("qualifiedReferenceName qualifiedPackageName");
		oh.addOption(packn);

		// OPTION_EDGE_CLASS_DIRECTION = "d";
		Option direct = new Option(
				OPTION_EDGE_CLASS_DIRECTION,
				"directionOfEdgeClass",
				true,
				"(optional): defines the direction of the specified EReference, valid values for directionval are \"FROM\" and \"TO\"");
		direct.setRequired(false);
		direct.setArgs(2);
		direct.setValueSeparator(' ');
		direct.setArgName("qualifiedReferenceName directionvalue");
		oh.addOption(direct);

		// OPTION_EDGE_CLASS_NAME = "n";
		Option edgeNames = new Option(
				OPTION_EDGE_CLASS_NAME,
				"edgeclassname",
				true,
				"(optional): sets the EdgeClass, made frome the given EReference, the specified name");
		edgeNames.setRequired(false);
		edgeNames.setArgs(2);
		edgeNames.setValueSeparator(' ');
		edgeNames.setArgName("qualifiedReferenceName name");
		oh.addOption(edgeNames);

		// OPTION_GENERATE_ROLE_NAMES = "r"
		Option genR = new Option(OPTION_GENERATE_ROLE_NAME,
				"generateRoleNames", false,
				"(optional): defines that missing role names should become generated"
						+ "\n (defaults to false)");
		genR.setRequired(false);
		oh.addOption(genR);

		// OPTION_OVERWRITES = "v";
		Option overwrites = new Option(OPTION_OVERWRITES,
				"overwritingreferences", true,
				"(optional): defines which EReference overwrites which other EReference");
		overwrites.setRequired(false);
		overwrites.setArgs(2);
		overwrites.setValueSeparator(' ');
		overwrites.setArgName("overwritingRefname overwrittenRefname");
		oh.addOption(overwrites);
		/*
		 * // OPTION_ROOT_PACKAGE = "w" Option rootp = new
		 * Option(OPTION_ROOT_PACKAGE, "rootEPackage", true,
		 * "(optional): declares the root EPackage of the metamodel");
		 * rootp.setRequired(false); rootp.setArgName("epackagename");
		 * oh.addOption(rootp);
		 */

		// OPTION_CONVERT_BIG_NUMBERS = "b"
		Option convBigNumbers = new Option(OPTION_CONVERT_BIG_NUMBERS,
				"convert-bignums", false,
				"Convert EBigInteger/EBigDecimal to Long/Double.");
		convBigNumbers.setRequired(false);
		oh.addOption(convBigNumbers);

		Option createJar = new Option("j", "create-jar", true,
				"(optional) Compile classes for the schema an put them in the given jar.");
		createJar.setArgName("jar-file-name");
		createJar.setRequired(false);
		oh.addOption(createJar);

		// Parses the given command line parameters with all created Option.
		return oh.parse(args);
	}

	// //////////////////////////////////////////////////////////////////////////
	// #//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#/
	// //////////////////////////////////////////////////////////////////////////
	// #//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#//#/
	// //////////////////////////////////////////////////////////////////////////

	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------
	// -------Mappings-----------------------------------------------------------
	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * Remembers Mapping of EClass to VertexClasses
	 */
	private HashMap<EClass, VertexClass> vertexclassmap;

	/**
	 * Remembers Mapping of VertexClass to EClass Needed for compatibility check
	 * on directions of EdgeClasses
	 * */
	private HashMap<VertexClass, EClass> vertexclassmaprevers;

	/**
	 * Remembers Mapping of EPackages to Packages
	 * */
	private HashMap<EPackage, Package> packagemap;

	/**
	 * Remembers EnumDomainsMapping
	 * */
	private HashMap<String, EnumDomain> enumdomains;

	/**
	 * Remembers Mapping of EClasses to EdgeClasses
	 * */
	private HashMap<EClass, EdgeClass> edgeclassmap;

	/**
	 * Remembers Mapping of EdgeClasses to EClasses
	 * */
	private HashMap<EdgeClass, EClass> edgeclassmaprevers;

	/**
	 * Remembers to which EdgeClass an EReference becomes. Only includes
	 * EReferences that become an EdgeClass directly. EReferences from EClasses
	 * that are himself EdgeClasses are not in. Needed for model transformation.
	 * */
	private HashMap<EReference, EdgeClass> ereference2edgeclass_map;

	/**
	 * Remembers Mapping of EClasses to RecordDomains
	 * */
	private HashMap<EClass, RecordDomain> eclass2recorddomain_map;
	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------
	// -------Reminders for the algorithm---------------------------------------
	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * Remembers the EReferences that are already transformed Needed to prevent
	 * the program from transforming opposites twice or EReferences that belongs
	 * to EdgeClasses as autonomous EdgeClasses.
	 * */
	private HashSet<EReference> transformedEReferences;

	/**
	 * Needed for the transformation from EClasses to EdgeClasses, Containment
	 * EReferences that are not part of the Edge should not become transformed,
	 * they are ignored. The HashSet is needed during the Transformation from
	 * EClasses to EdgeClasses and later again, during the model transformation.
	 * */
	private HashSet<EReference> badEReferences;

	/**
	 * Remembers the EAttributes that can not become transformed because of an
	 * not transformable DataType or something. Needed to ignore those
	 * EAttributes during the model transformation.
	 * */
	private HashSet<EAttribute> badEAttributes;

	/**
	 * Remembers for an EdgeClass the EReferences that connect it to its alpha
	 * and omega
	 */
	private HashMap<EClass, ArrayList<EReference>> ereferencesOfEdgeClasses = new HashMap<EClass, ArrayList<EReference>>();

	/**
	 * Remembers for an EdgeClass whether alpha or omega is a subtype of another
	 */
	private HashMap<EClass, boolean[]> ereferencesOfEdgeClassesresult = new HashMap<EClass, boolean[]>();

	private ArrayList<EClass> edgeclasses;

	/**
	 * Set of all EReferences that define the direction for EReferences with or
	 * without opposites that become EdgeClasses Needed to remember direction
	 * for model transformation
	 * */
	private HashSet<EReference> definingDirectionEReferences;

	/**
	 * Set of all EReferences of EClasses that become EdgeClasses that point on
	 * the start of the resulting EdgeClass
	 * */
	private HashSet<EReference> ereferencesEdgeClass2start;

	/**
	 * Set of all EReferences of EClasses that become EdgeClasses that point on
	 * the end of the resulting EdgeClass
	 * */
	private HashSet<EReference> ereferencesEdgeClass2target;

	/**
	 * Saves for every EReference all EReferences that are overwritten by the
	 * Key EReference
	 * */
	private HashMap<EReference, ArrayList<EReference>> ereferenceWithOverwritten;

	/**
	 * Set of user defined EReferences with the direction FROM
	 * */
	private HashSet<EReference> fromERefererences;

	/**
	 * Set of user defined EReferences with the direction TO
	 * */
	private HashSet<EReference> toEReferences;

	private HashSet<EReference> recordDomainEReferences;
	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------
	// -------Basics-------------------------------------------------------------
	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * Complete Ecore metamodel represented as Resource all EPackages in that
	 * Resource will become transformed
	 */
	private final Resource metamodelResource;

	/**
	 * grUML SchemaGraph that represents the transformed Ecore metamodel
	 */
	private SchemaGraph schemagraph;

	/**
	 * GraphClass that belongs to the SchemaGraph Needed to change the qualified
	 * name if there is a GraphClass defined
	 * */
	private GraphClass graphclass;

	/**
	 * If the Ecore Model includes an EAttribute with the DataType Date, a
	 * RecordDomain is created for that
	 * */
	private RecordDomain dateDomain = null;

	private String schemaName;

	// ---
	// -- Analyzer
	// -----------

	private Ecore2TgAnalyzer analyzer;

	// ---------------------------
	// -- Configuration
	// ----------------------------

	private Ecore2TgConfiguration configuration;

	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------
	// -------Getter/Setter ----------------------------------------------------
	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * Returns the resulting SchemaGraph after the transformation.
	 * 
	 * @return SchemaGraph of the transformed metamodel
	 * */
	public SchemaGraph getSchemaGraph() {
		return this.schemagraph;
	}

	public de.uni_koblenz.jgralab.schema.Schema getSchema() {
		return this.schem;
	}

	public Ecore2TgConfiguration getConfiguration() {
		return this.configuration;
	}

	public void setConfiguration(Ecore2TgConfiguration conf) {
		this.configuration = conf;
	}

	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------
	// --Start of real code-----------------------------------------------------
	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * Constructor Loads the Ecore file
	 * 
	 * @param pathToEcoreFile
	 *            path to the ".ecore" File that should become transformed
	 */
	public Ecore2Tg(String pathToEcoreFile) {
		// Load the Ecore metamodel
		this.metamodelResource = loadMetaModelFromEcoreFile(pathToEcoreFile);
		this.configuration = new Ecore2TgConfiguration();
		this.analyzer = new Ecore2TgAnalyzer(this.metamodelResource);
	}

	public Ecore2Tg(Resource ecoreSchema) {
		this.metamodelResource = ecoreSchema;
		this.configuration = new Ecore2TgConfiguration();
		this.analyzer = new Ecore2TgAnalyzer(this.metamodelResource);
	}

	public Ecore2Tg(Ecore2TgAnalyzer anal) {
		this.metamodelResource = anal.getMetamodelResource();
		this.analyzer = anal;
		this.configuration = new Ecore2TgConfiguration();
	}

	public Ecore2Tg(String pathToEcoreFile, Ecore2TgConfiguration conf) {
		// Load the Ecore metamodel
		this.metamodelResource = loadMetaModelFromEcoreFile(pathToEcoreFile);
		this.configuration = conf;
		this.analyzer = new Ecore2TgAnalyzer(this.metamodelResource);
	}

	public Ecore2Tg(Resource ecoreSchema, Ecore2TgConfiguration conf) {
		this.metamodelResource = ecoreSchema;
		this.configuration = conf;
		this.analyzer = new Ecore2TgAnalyzer(this.metamodelResource);
	}

	public Ecore2Tg(Ecore2TgAnalyzer anal, Ecore2TgConfiguration conf) {
		this.metamodelResource = anal.getMetamodelResource();
		this.analyzer = anal;
		this.configuration = conf;
	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------
	// -------Schema Transformation---------------------------------------------
	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------

	/**
	 * Transform the Schema with the options set. After invoking this method,
	 * the SchemaGraph is ready
	 * */
	public void transform(String schemaName) {
		System.out.println("Transformation started...");
		long starttime = System.currentTimeMillis();

		// Initialize

		// Create an empty SchemaGraph
		this.schemagraph = GrumlSchema.instance().createSchemaGraph(
				ImplementationType.STANDARD);

		this.initializeMaps();

		// Start
		EPackage rootPackage = (EPackage) this.metamodelResource.getContents()
				.get(0);
		if (this.metamodelResource.getContents().size() > 1) {
			System.out.println("More than one root package in metamodel. ");
			for (EObject ob : this.metamodelResource.getContents()) {
				System.out.println("  Package: " + ((EPackage) ob).getName());
				if (((EPackage) ob).getNsURI() == null) {
					((EPackage) ob).setNsURI(((EPackage) ob).getName());
					System.err
							.println("  Warning: package has no nsURI - Set nsURI to: "
									+ ((EPackage) ob).getName());
				}
				if (((EPackage) ob).getNsPrefix() == null) {
					((EPackage) ob).setNsPrefix(((EPackage) ob).getName()
							.toLowerCase());
					System.err
							.println("  Warning: package has no nsPrefix - Set nsPrefix to: "
									+ ((EPackage) ob).getName().toLowerCase());
				}
			}
			for (EObject ob : this.metamodelResource.getContents()) {
				EPackage tp = (EPackage) ob;
				if (!tp.getName().contains("Type")) {
					rootPackage = tp;
					break;
				}
			}

		}

		// Determine names and nsprefix of Schema and GraphClass
		this.schemaName = schemaName;
		if (!this.schemaName.contains(".")
				|| !(this.schemaName.toLowerCase().charAt(0) == this.schemaName
						.charAt(0))
				|| !(this.schemaName.toUpperCase().charAt(
						this.schemaName.lastIndexOf(".") + 1) == this.schemaName
						.charAt(this.schemaName.lastIndexOf(".") + 1))) {
			System.err
					.println("Error: Transformation Ecore2Tg aborted. SchemaName "
							+ this.schemaName
							+ " is not valid. "
							+ "SchemaName must match nsprefix.Name. "
							+ "nsprefix must start with lower case, Name with upper case.");
			return;
		}
		String nsPrefixString = this.schemaName.substring(0,
				this.schemaName.lastIndexOf("."));
		String simpleSchemaName = this.schemaName.substring(this.schemaName
				.lastIndexOf(".") + 1);

		// Create the Schema
		Schema schema = this.schemagraph.createSchema();
		schema.set_name(simpleSchemaName);
		schema.set_packagePrefix(nsPrefixString);

		// Create the default GraphClass for the Schema
		this.graphclass = this.schemagraph.createGraphClass();
		// Check if an EClass is defined as GraphClass
		if ((this.getConfiguration().getAsGraphClass() != null)
				&& !this.getConfiguration().getAsGraphClass().equals("")) {
			this.graphclass.set_qualifiedName(this.getConfiguration()
					.getAsGraphClass() + "Graph");
		}
		// Check if a GraphClass name is defined
		else if ((this.getConfiguration().getGraphclassName() != null)
				&& !this.getConfiguration().getGraphclassName().equals("")) {
			this.graphclass.set_qualifiedName(this.getConfiguration()
					.getGraphclassName());
		}
		// If not, take the schema name
		else {
			this.graphclass.set_qualifiedName(simpleSchemaName + "Graph");
		}

		Comment gc = this.schemagraph.createComment();
		gc.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG);
		this.graphclass.add_comment(gc);
		schema.add_graphClass(this.graphclass);

		// Examine user input:
		this.examineUsersEdgeClassList();
		this.examineUsersDirectionMap();
		this.examineUsersOverwrittenEReferenceList();
		this.examineUserNamesAndPackages();

		// Does the user want the program to search for EdgeClasses?
		if (this.getConfiguration().getTransformationOption() != TransformParams.JUST_LIKE_ECORE) {
			// If they are not already searched
			if (this.analyzer.getFoundEdgeClasses() == null
					|| !this.ereferenceWithOverwritten.isEmpty()) {
				this.analyzer.searchForEdgeClasses(this.getConfiguration()
						.getTransformationOption());
			}
			if (this.getConfiguration().getTransformationOption() == TransformParams.AUTOMATIC_TRANSFORMATION) {
				this.badEReferences.addAll(this.analyzer
						.getIgnoredEReferences());
				this.ereferencesOfEdgeClasses.putAll(this.analyzer
						.getEreferencesOfEdgeClasses());
				this.ereferencesOfEdgeClassesresult.putAll(this.analyzer
						.getEreferencesOfEdgeClassesresult());
				this.edgeclasses.addAll(this.analyzer.getFoundEdgeClasses());
			}

		}
		Ecore2TgAnalyzer.sortEClasses(this.edgeclasses);

		// Add the 2 Attributes nsPrefix and nsURI to the GraphClass
		if (((EPackage) this.metamodelResource.getContents().get(0))
				.getEAnnotation(EAnnotationKeys.SOURCE_STRING) == null) {
			Attribute nsPrefix = this.schemagraph.createAttribute();
			nsPrefix.set_name("nsPrefix");
			nsPrefix.set_defaultValue("\"" + rootPackage.getNsPrefix() + "\"");
			Domain sd = this.schemagraph.createStringDomain();
			sd.set_qualifiedName("String");
			nsPrefix.add_domain(sd);
			Attribute nsURI = this.schemagraph.createAttribute();
			nsURI.set_name("nsURI");
			nsURI.set_defaultValue("\"" + rootPackage.getNsURI() + "\"");
			nsURI.add_domain(sd);
			this.graphclass.add_attribute(nsPrefix);
			this.graphclass.add_attribute(nsURI);
		}

		// Determine the default Package - if the Ecore metamodel has a root
		// EPackage, that becomes the default Package, if not a new default
		// package is created and the high level EPackages are added as
		// Subpackages
		Package defaultPackage;
		if (this.metamodelResource.getContents().size() > 1) {

			// Create the DefaultPackage for the Schema
			defaultPackage = this.schemagraph.createPackage();

			// Transform the EClasses skeletons and EPackages and put them into
			// the defaultPackage
			for (EObject ob : this.metamodelResource.getContents()) {
				EPackage p = (EPackage) ob;
				String qualname = p.getName().toLowerCase();
				defaultPackage.add_subpackage(this
						.transformEPackagesWithContent(p, qualname));
			}
		} else {
			defaultPackage = this.transformEPackagesWithContent(
					(EPackage) this.metamodelResource.getContents().get(0), "");
			Comment dwr = this.schemagraph.createComment();
			dwr.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
					+ EAnnotationKeys.DEFAULT_WAS_ROOT
					+ " "
					+ ((EPackage) this.metamodelResource.getContents().get(0))
							.getName());
			this.graphclass.add_comment(dwr);
		}
		defaultPackage.set_qualifiedName("");
		this.schemagraph.createContainsDefaultPackage(schema, defaultPackage);

		// Check on a user defined GraphClass
		if ((this.getConfiguration().getAsGraphClass() == null)
				|| this.getConfiguration().getAsGraphClass().equals("")) {
			Comment gccom = this.schemagraph.createComment();
			gccom.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
					+ EAnnotationKeys.GENERATED_GRAPHCLASS);
			this.graphclass.add_comment(gccom);
		}

		// Transform the EClasses that are EdgeClasses
		this.transformEClassesIntoEdgeClasses();

		// Transform the SuperType Structures, Attributes and References
		this.transformSuperTypeConnectionsAndEStructuralFeatures();

		// Create Names for those EdgeClasses, that hat none until now
		this.createEdgeClassNames();

		// Save configurations as comments
		this.configuration.addConfigurationAsComment(this.schemagraph,
				this.graphclass);

		for (EPackage old : this.packagemap.keySet()) {
			if (!this.packagemap.get(old).get_subpackage().iterator().hasNext()
					&& !this.packagemap.get(old)
							.getContainsGraphElementClassIncidences()
							.iterator().hasNext()
					&& !this.packagemap.get(old).getContainsDomainIncidences()
							.iterator().hasNext()) {
				// package empty
				this.packagemap.get(old).delete();
			}
		}

		// Get the Schema and compile it
		SchemaGraph2Schema transsg2s = new SchemaGraph2Schema();
		this.schem = transsg2s.convert(this.schemagraph);
		this.schem.finish();
		// this.schem.compile(CodeGeneratorConfiguration.MINIMAL);

		// Transformation ready
		long endtime = System.currentTimeMillis();
		System.out.println("Transformation finished. It took "
				+ (endtime - starttime) + " milliseconds.");
	}

	private void initializeMaps() {
		// Initializing core mappings
		this.vertexclassmap = new HashMap<EClass, VertexClass>();
		this.vertexclassmaprevers = new HashMap<VertexClass, EClass>();
		this.packagemap = new HashMap<EPackage, Package>();
		this.enumdomains = new HashMap<String, EnumDomain>();
		this.edgeclassmap = new HashMap<EClass, EdgeClass>();
		this.edgeclassmaprevers = new HashMap<EdgeClass, EClass>();
		this.ereference2edgeclass_map = new HashMap<EReference, EdgeClass>();
		this.eclass2recorddomain_map = new HashMap<EClass, RecordDomain>();

		// Initializing maps and sets for the algorithms
		this.transformedEReferences = new HashSet<EReference>();
		this.badEReferences = new HashSet<EReference>();
		this.badEAttributes = new HashSet<EAttribute>();
		this.edgeclasses = new ArrayList<EClass>();
		this.ereferencesOfEdgeClasses = new HashMap<EClass, ArrayList<EReference>>();
		this.ereferencesOfEdgeClassesresult = new HashMap<EClass, boolean[]>();
		this.definingDirectionEReferences = new HashSet<EReference>();
		this.ereferencesEdgeClass2start = new HashSet<EReference>();
		this.ereferencesEdgeClass2target = new HashSet<EReference>();
		this.ereferenceWithOverwritten = new HashMap<EReference, ArrayList<EReference>>();
		this.fromERefererences = new HashSet<EReference>();
		this.toEReferences = new HashSet<EReference>();
		this.recordDomainEReferences = new HashSet<EReference>();
	}

	// -------------------------------------------------------------------------
	// --------Examine user inputs----------------------------------------------
	// -------------------------------------------------------------------------

	/**
	 * Iterates over the list with the qualified names of the EClasses, the user
	 * wants to become EdgeClasses. It looks for the EClasses and throws it with
	 * its subclasses into the {@link edgeclasses} list
	 * */
	private void examineUsersEdgeClassList() {
		for (String name : this.getConfiguration().getEdgeClassesList()) {
			EClass eclass = Ecore2TgAnalyzer.getEClassByName(name,
					this.metamodelResource);
			if (eclass == null) {
				System.err.println("Invalid user input: Can not declare "
						+ name
						+ " as EdgeClass, because the EClass is not found.");
				throw new RuntimeException(
						"Invalid user input: Can not declare "
								+ name
								+ " as EdgeClass, because the EClass is not found.");
			}
			this.edgeclasses.add(eclass);
		}
		// Check for Subclasses
		ArrayList<EClass> childs = Ecore2TgAnalyzer.getSubclassesOfEClasses(
				this.metamodelResource, this.edgeclasses);
		this.edgeclasses.addAll(childs);
	}

	/**
	 * Iterates over the user specified directions Fills the Sets
	 * {@link fromEReferences} and {@link toEReferences}
	 * */
	private void examineUsersDirectionMap() {
		for (String name : this.configuration.getDirectionMap().keySet()) {
			EReference ref = Ecore2TgAnalyzer.getEReferenceByName(name,
					this.metamodelResource);
			if (ref != null) {
				if (this.configuration.getDirectionMap().get(name) == Ecore2TgConfiguration.FROM) {
					this.fromERefererences.add(ref);
				} else if (this.configuration.getDirectionMap().get(name) == Ecore2TgConfiguration.TO) {
					this.toEReferences.add(ref);
				} else {
					System.err
							.println("Invalid user input: Setting direction for "
									+ name
									+ " is not possible. As arguments are 0 or 1 expected. User entered "
									+ this.configuration.getDirectionMap().get(
											name) + " instead.");
				}
			} else {
				System.err.println("Invalid user input: Setting direction for "
						+ name
						+ " is not possible. The EReference does not exist.");
			}
		}
	}

	/**
	 * Iterates over the user specified overwriting EReferences Fills the Map
	 * {@link erefs2overwrittenerefs}
	 * */
	private void examineUsersOverwrittenEReferenceList() {
		for (String word : this.configuration
				.getPairsOfOverwritingEReferences().keySet()) {
			EReference keyref = Ecore2TgAnalyzer.getEReferenceByName(word,
					this.metamodelResource);
			EReference valref = Ecore2TgAnalyzer.getEReferenceByName(
					this.configuration.getPairsOfOverwritingEReferences().get(
							word), this.metamodelResource);
			if ((keyref == null) || (valref == null)) {
				System.err
						.println("Invalid user input: "
								+ word
								+ " should overwrite "
								+ this.configuration
										.getPairsOfOverwritingEReferences()
										.get(word)
								+ " but at least one of the EReferences does not exist.");
				continue;
			}
			if (this.ereferenceWithOverwritten.containsKey(keyref)) {
				this.ereferenceWithOverwritten.get(keyref).add(valref);
				;
			} else {
				ArrayList<EReference> st = new ArrayList<EReference>();
				st.add(valref);
				this.ereferenceWithOverwritten.put(keyref, st);
			}
		}
		boolean changed = true;
		while (changed) {
			changed = false;
			for (EReference keyref : this.ereferenceWithOverwritten.keySet()) {
				for (EReference keyrefInner : this.ereferenceWithOverwritten
						.keySet()) {
					if (this.ereferenceWithOverwritten.get(keyref).contains(
							keyrefInner)
							&& !this.ereferenceWithOverwritten.get(keyref)
									.containsAll(
											this.ereferenceWithOverwritten
													.get(keyrefInner))) {
						this.ereferenceWithOverwritten.get(keyref)
								.addAll(this.ereferenceWithOverwritten
										.get(keyrefInner));
						changed = true;
					}
				}

			}
		}
	}

	/**
	 * Iterates over the name and package defining maps to check whether the
	 * user has given correct input.
	 * */
	private void examineUserNamesAndPackages() {
		for (String key : this.configuration.getNamesOfEdgeClassesMap()
				.keySet()) {
			if ((Ecore2TgAnalyzer.getEReferenceByName(key,
					this.metamodelResource) == null)
					&& (Ecore2TgAnalyzer.getEClassByName(key,
							this.metamodelResource) == null)) {
				System.err.println("Invalid user input: "
						+ key
						+ " does not exist. Name "
						+ this.configuration.getNamesOfEdgeClassesMap()
								.get(key) + " is not set.");
			}
		}
		for (String key : this.configuration
				.getDefinedPackagesOfEdgeClassesMap().keySet()) {
			if (Ecore2TgAnalyzer.getEReferenceByName(key,
					this.metamodelResource) == null) {
				System.err.println("Invalid user input: EReference "
						+ key
						+ " does not exist. Package "
						+ this.configuration
								.getDefinedPackagesOfEdgeClassesMap().get(key)
						+ " is not set.");
			} else {
				if (this.getEPackageByName(this.configuration
						.getDefinedPackagesOfEdgeClassesMap().get(key)) == null) {
					System.err.println("Invalid user input: EPackage "
							+ this.configuration
									.getDefinedPackagesOfEdgeClassesMap().get(
											key)
							+ " does not exist. Package for " + key
							+ " is not set.");
				}
			}
		}
	}

	// --------------------------------------------------------------------------
	// -------Transformation methods--------------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * Iterative method that transforms the EPackage structure into a Package
	 * Structure Calls the methods to transform the different contents of the
	 * Package Contents are - DataTypes: Enumerations and Others - EClasses : to
	 * be GraphClass, EdgeClasses, VertexClasses
	 * 
	 * @param pack
	 *            EPackage that should become transformed
	 * @param qualifiedPackageName
	 *            name for the new Package
	 * 
	 * @return the transformed Package
	 * */
	private Package transformEPackagesWithContent(EPackage pack,
			String qualifiedPackageName) {

		// Create a new empty Package
		Package activePackage = this.schemagraph.createPackage();
		activePackage.set_qualifiedName(qualifiedPackageName);
		if (!activePackage.get_qualifiedName().equals("")) {
			Comment c = this.schemagraph.createComment();
			c.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
					+ EAnnotationKeys.EPACKAGE_NSPREFIX + pack.getNsPrefix());
			activePackage.add_comment(c);
			c = this.schemagraph.createComment();
			c.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
					+ EAnnotationKeys.EPACKAGE_NSURI + pack.getNsURI());
			activePackage.add_comment(c);
		}
		this.transformEAnnotations(pack.getEAnnotations(), activePackage);

		this.packagemap.put(pack, activePackage);

		String packageprefixForFurtherCalls;
		if (activePackage.get_qualifiedName().equals("")) {
			packageprefixForFurtherCalls = "";
		} else {
			packageprefixForFurtherCalls = activePackage.get_qualifiedName()
					+ ".";
		}

		// Iterate over all Classifiers of the EPackage
		for (EClassifier classifier : pack.getEClassifiers()) {

			EAnnotation grUML_EAnnotation = classifier
					.getEAnnotation(EAnnotationKeys.SOURCE_STRING);

			// The Classifier is an Enumeration
			if (classifier instanceof EEnum) {
				EnumDomain en = this.transformEEnum((EEnum) classifier,
						packageprefixForFurtherCalls);
				this.enumdomains.put(((EEnum) classifier).getName(), en);
				this.schemagraph.createContainsDomain(activePackage, en);
			}

			// The Classifier is a not transformable EDataType
			else if (classifier instanceof EDataType) {
				if (classifier.getName().equals("Boolean")) {
					((EDataType) classifier)
							.setInstanceClassName("java.lang.Boolean");
				} else if (classifier.getName().equals("Integer")) {
					((EDataType) classifier)
							.setInstanceClassName("java.lang.Integer");
				} else if (classifier.getName().equals("Byte")) {
					((EDataType) classifier)
							.setInstanceClassName("java.lang.Byte");
				} else if (classifier.getName().equals("Short")) {
					((EDataType) classifier)
							.setInstanceClassName("java.lang.Short");
				} else if (classifier.getName().equals("Long")) {
					((EDataType) classifier)
							.setInstanceClassName("java.lang.Long");
				} else if (classifier.getName().equals("String")) {
					((EDataType) classifier)
							.setInstanceClassName("java.lang.String");
				} else if (classifier.getName().equals("Float")) {
					((EDataType) classifier)
							.setInstanceClassName("java.lang.Float");
				} else if (classifier.getName().equals("Double")) {
					((EDataType) classifier)
							.setInstanceClassName("java.lang.Double");
				} else if (classifier.getName().equals("UnlimitedNatural")) {
					((EDataType) classifier)
							.setInstanceClassName("java.lang.Long");
				} else {
					System.err.println("Program can not transform EDataType "
							+ classifier.getName() + ".");
				}
			}

			// The Classifier is an EClass
			else {
				// --User wants the EClass to become the GraphClass

				if (this.configuration.getAsGraphClass().endsWith(
						(packageprefixForFurtherCalls + classifier.getName()))) {
					this.graphclass.set_qualifiedName(((EClass) classifier)
							.getName());
					// Transform EAttributes
					for (EAttribute eatt : ((EClass) classifier)
							.getEAttributes()) {
						Attribute activeAttribute = this.transformEAttribute(
								eatt, this.graphclass);
						if (activeAttribute != null) {
							this.graphclass.add_attribute(activeAttribute);
						}
					}
				}
				// --User wants the EClass to become an EdgeClass
				else if (this.edgeclasses.contains(classifier)) {
					EdgeClass ec = this.schemagraph.createEdgeClass();
					this.edgeclassmap.put((EClass) classifier, ec);
					this.edgeclassmaprevers.put(ec, (EClass) classifier);
				}

				// --No User Specification -- look for EAnnotations
				// Graphclass
				else if ((grUML_EAnnotation != null)
						&& grUML_EAnnotation.getDetails().containsKey(
								EAnnotationKeys.KEY_IS_GRAPHCLASS)) {

					this.graphclass.set_qualifiedName(((EClass) classifier)
							.getName());
					if ((this.configuration.getAsGraphClass() == null)
							|| this.configuration.getAsGraphClass().equals("")) {
						this.configuration.setAsGraphClass(this.graphclass
								.get_qualifiedName());

					}
					// Transform EAttributes
					for (EAttribute eatt : ((EClass) classifier)
							.getEAttributes()) {
						Attribute activeAttribute = this.transformEAttribute(
								eatt, this.graphclass);
						if (activeAttribute != null) {
							this.graphclass.add_attribute(activeAttribute);
						}
					}
				}

				// EAnnotation EdgeClass
				else if ((grUML_EAnnotation != null)
						&& grUML_EAnnotation.getDetails().containsKey(
								EAnnotationKeys.KEY_IS_EDGECLASS)) {
					// if it is an EAnnotation, it can not be
					// guaranteed,
					// that the subclasses are transformed after the
					// superclasses
					// but EAnnotations are only created full, so that
					// the direction
					// is clear too
					EdgeClass ec = this.schemagraph.createEdgeClass();
					this.edgeclasses.add((EClass) classifier);
					this.edgeclassmap.put((EClass) classifier, ec);
					this.edgeclassmaprevers.put(ec, (EClass) classifier);
				}

				// EAnnotation RecordDomain
				else if ((grUML_EAnnotation != null)
						&& grUML_EAnnotation.getDetails().containsKey(
								EAnnotationKeys.KEY_IS_RECORDDOMAIN)) {
					EClass ecl = (EClass) classifier;
					RecordDomain red = this.transformEClass2RecordDomain(ecl,
							packageprefixForFurtherCalls);
					this.eclass2recorddomain_map.put(ecl, red);
					this.schemagraph.createContainsDomain(activePackage, red);
				}

				// --Normal EClass Transformation
				else {
					VertexClass activeVertexClass = this
							.transformEClassIntoVertexClass(
									(EClass) classifier,
									packageprefixForFurtherCalls);
					this.vertexclassmap.put((EClass) classifier,
							activeVertexClass);
					this.vertexclassmaprevers.put(activeVertexClass,
							(EClass) classifier);
					this.schemagraph.createContainsGraphElementClass(
							activePackage, activeVertexClass);
				}

			}
		}

		// Iteration over all SubPackage of the EPackage to transform them
		for (EPackage subEPackage : pack.getESubpackages()) {
			String name = subEPackage.getName().toLowerCase();
			Package subPackage = this.transformEPackagesWithContent(
					subEPackage, packageprefixForFurtherCalls + name);
			activePackage.add_subpackage(subPackage);
		}

		return activePackage;
	}

	/**
	 * Transforms an eclass that was a former RecordDomain back to a
	 * RecordDomain
	 * 
	 * @param eclass
	 *            represents the RecordDomain in Ecore
	 * @param packagePrefix
	 *            packages of RecordDomain for setting the qualified name,
	 *            ending with "."
	 * @return the resulting RecordDomain
	 */
	private RecordDomain transformEClass2RecordDomain(EClass eclass,
			String packagePrefix) {
		RecordDomain rd;
		if (this.eclass2recorddomain_map.containsKey(eclass)) {
			rd = this.eclass2recorddomain_map.get(eclass);
		} else {
			rd = this.schemagraph.createRecordDomain();
		}
		rd.set_qualifiedName(packagePrefix + eclass.getName());

		for (EAttribute ea : eclass.getEAllAttributes()) {
			Domain component = this.transformEDataType(ea.getEAttributeType(),
					rd, ea.getName());

			if ((ea.getUpperBound() != 1) && ea.isOrdered()) {
				ListDomain lc = this.schemagraph.createListDomain();
				lc.add_basedomain(component);
				rd.add_componentdomain(lc).set_name(ea.getName());
			} else if ((ea.getUpperBound() != 1) && ea.isUnique()) {
				SetDomain sc = this.schemagraph.createSetDomain();
				sc.add_basedomain(component);
				rd.add_componentdomain(sc).set_name(ea.getName());
			} else {
				rd.add_componentdomain(component).set_name(ea.getName());
			}
		}
		for (EReference eref : eclass.getEAllReferences()) {
			RecordDomain component;
			if (this.eclass2recorddomain_map.containsKey(eref
					.getEReferenceType())) {
				component = this.eclass2recorddomain_map.get(eref
						.getEReferenceType());
			} else {
				component = this.schemagraph.createRecordDomain();
				this.eclass2recorddomain_map.put(eref.getEReferenceType(),
						component);
			}

			if ((eref.getUpperBound() != 1) && eref.isOrdered()) {
				ListDomain lc = this.schemagraph.createListDomain();
				lc.add_basedomain(component);
				rd.add_componentdomain(lc).set_name(eref.getName());
			} else if ((eref.getUpperBound() != 1) && eref.isUnique()) {
				SetDomain sc = this.schemagraph.createSetDomain();
				sc.add_basedomain(component);
				rd.add_componentdomain(sc).set_name(eref.getName());
			} else {
				rd.add_componentdomain(component).set_name(eref.getName());
			}
		}
		return rd;
	}

	/**
	 * Transforms an EEnum into an EnumDomain
	 * 
	 * @param eenum
	 *            the EEnum to transform
	 * @param packagePrefix
	 *            the package prefix of the EEnum to determine the qualified
	 *            name of the resulting EnumDomain, it ends with a point, if
	 *            it's not the default package
	 * */
	private EnumDomain transformEEnum(EEnum eenum, String packagePrefix) {
		EnumDomain en = this.schemagraph.createEnumDomain();
		en.set_qualifiedName(packagePrefix + eenum.getName());
		PVector<String> constants = ArrayPVector.empty();
		for (EEnumLiteral eliteral : eenum.getELiterals()) {
			String name = eliteral.getLiteral();
			String goodname = name.toUpperCase().replace("-", "_");
			if (!Character.isLetterOrDigit(goodname.charAt(0))) {
				goodname = eliteral.getName().toUpperCase();
			}
			if (!name.equals(goodname)) {
				Comment com = this.schemagraph.createComment();
				com.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
						+ EAnnotationKeys.CHANGED_ENUM_LITERAL + goodname + " "
						+ name);
				en.add_comment(com);
			}
			constants = constants.plus(goodname);
		}
		en.set_enumConstants(constants);

		return en;
	}

	/**
	 * Transforms the Skeleton of an EClass into a VertexClass No Attributes,
	 * References or SuperTypeConnections are here transformed, the method just
	 * creates an empty VertexClass with the correct name and abstract value
	 * 
	 * @param eclass
	 *            the EClass to transform
	 * @param packagePrefix
	 *            the package prefix of the EClass to determine the qualified
	 *            name with point at the end
	 * 
	 * @return the resulting VertexClass
	 * */
	private VertexClass transformEClassIntoVertexClass(EClass eclass,
			String packagePrefix) {
		VertexClass activeVertexClass = this.schemagraph.createVertexClass();
		if (eclass.getName().equals("Vertex")
				|| eclass.getName().equals("Edge")
				|| eclass.getName().equals("Graph")) {
			activeVertexClass.set_qualifiedName(packagePrefix
					+ eclass.getName() + "_");
		} else {
			activeVertexClass.set_qualifiedName(packagePrefix
					+ eclass.getName());
		}
		if (eclass.isAbstract()) {
			activeVertexClass.set_abstract(true);
		} else if (eclass.isInterface()) {
			activeVertexClass.set_abstract(true);
			Comment com = this.schemagraph.createComment();
			com.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
					+ EAnnotationKeys.INTERFACE);
			activeVertexClass.add_comment(com);
		} else {
			activeVertexClass.set_abstract(false);
		}
		this.transformEAnnotations(eclass.getEAnnotations(), activeVertexClass);
		return activeVertexClass;
	}

	private void transformEAnnotations(EList<EAnnotation> eannotations,
			NamedElement target) {
		for (EAnnotation ean : eannotations) {
			String commentString = ean.getSource();
			if (commentString.equals(EAnnotationKeys.SOURCE_STRING)) {
				continue;
			}
			if (commentString.equals(EAnnotationKeys.SOURCE_STRING_COMMENTS)) {
				for (String k : ean.getDetails().keySet()) {
					Comment comment = this.schemagraph.createComment();
					comment.set_text(k);
					target.add_comment(comment);
				}
			} else {
				commentString = EAnnotationKeys.ECORE_EANNOTATION_FLAG + " "
						+ commentString + ";";
				for (String str : ean.getDetails().keySet()) {
					commentString = commentString + str + " : "
							+ ean.getDetails().get(str) + ";";
				}

				Comment comment = this.schemagraph.createComment();
				comment.set_text(commentString);
				target.add_comment(comment);
			}
		}
	}

	/**
	 * Method to transform an EAttribute into an Attribute
	 * 
	 * @param eatt
	 *            the EAttribute to transform
	 * @param elForComment
	 *            NamedElement containing the Attribute, needed for possible
	 *            comments
	 * 
	 * @return the resulting Attribute, null if a transformation is not possible
	 * */
	private Attribute transformEAttribute(EAttribute eatt,
			NamedElement elForComment) {

		// Transform the name and defaultValue
		Attribute activeAttribute = this.schemagraph.createAttribute();
		activeAttribute.set_name(eatt.getName());
		activeAttribute.set_defaultValue(eatt.getDefaultValueLiteral());

		// Transform the Domain
		Domain activeDomain = this.transformEDataType(eatt.getEAttributeType(),
				elForComment, eatt.getName());

		// If the resulting Domain is null, it can not become transformed
		// --in this case, the EAttribute shouldn't become transformed
		if (activeDomain == null) {
			this.badEAttributes.add(eatt);
			System.err.println("Program can not transform Attribute "
					+ eatt.getName()
					+ " of EClass "
					+ Ecore2TgAnalyzer.getQualifiedEClassName(eatt
							.getEContainingClass()) + " because its Domain "
					+ eatt.getEAttributeType().getName()
					+ " can not become transformed.");
			return null;
		}

		// Test the Muliplicity of the EAttribute
		// --If it is 0 or 1, it is just an Attribute
		if ((eatt.getUpperBound() < 2) && (eatt.getUpperBound() > 0)) {
			activeAttribute.add_domain(activeDomain);
		}

		// --If not, it is a List or Set
		else {
			if (eatt.isOrdered()) {
				ListDomain temp = this.schemagraph.createListDomain();
				temp.add_basedomain(activeDomain);
				temp.set_qualifiedName("List<"
						+ activeDomain.get_qualifiedName() + ">");
				activeAttribute.add_domain(temp);
			} else {
				SetDomain temp = this.schemagraph.createSetDomain();
				temp.add_basedomain(activeDomain);
				temp.set_qualifiedName("Set<"
						+ activeDomain.get_qualifiedName() + ">");
				activeAttribute.add_domain(temp);
			}
		}
		String def = activeAttribute.get_defaultValue();

		if ((def != null) && activeDomain.get_qualifiedName().equals("Boolean")) {
			if (def.equals("false")) {
				activeAttribute.set_defaultValue("f");
			} else if (def.equals("true")) {
				activeAttribute.set_defaultValue("t");
			}
		}

		return activeAttribute;
	}

	/**
	 * Method that transforms an EDataType into a Domain, if possible
	 * 
	 * @param ed
	 *            the EDataType to transform
	 * @param elForComment
	 *            NamedElement that contains an Attribute or a Component of that
	 *            type - necessary for the comments in case of BigInteger and
	 *            BigDecimal
	 * @param atOrCompName
	 *            name of the Attribute or Component, the Domain is for
	 * 
	 * @return the resulting Domain or null, if there is no
	 * */
	private Domain transformEDataType(EDataType ed, NamedElement elForComment,
			String atOrCompName) {
		String typename = ed.getName();
		if (typename.equals("EBoolean") || typename.equals("EBooleanObject")
				|| typename.equals("Boolean")) {
			Domain dom = this.schemagraph.createBooleanDomain();
			dom.set_qualifiedName("Boolean");
			return dom;
		}
		if (typename.equals("EDouble") || typename.equals("EFloat")
				|| typename.equals("EDoubleObject")
				|| typename.equals("EFloatObject") || typename.equals("Double")
				|| typename.equals("Float")) {
			Domain dom = this.schemagraph.createDoubleDomain();
			dom.set_qualifiedName("Double");
			return dom;
		}
		if (typename.equals("EInt") || typename.equals("EIntegerObject")
				|| typename.equals("EShort") || typename.equals("EShortObject")
				|| typename.equals("EByte") || typename.equals("EByteObject")
				|| typename.equals("Integer") || typename.equals("Short")
				|| typename.equals("Byte")) {
			Domain dom = this.schemagraph.createIntegerDomain();
			dom.set_qualifiedName("Integer");
			return dom;
		}
		if (typename.equals("ELong") || typename.equals("ELongObject")
				|| typename.equals("Long")
				|| typename.equals("UnlimitedNatural")) {
			Domain dom = this.schemagraph.createLongDomain();
			dom.set_qualifiedName("Long");
			return dom;
		}
		if (typename.equals("EString") || typename.equals("EChar")
				|| typename.equals("ECharacterObject")
				|| typename.equals("String") || typename.equals("Character")) {
			Domain dom = this.schemagraph.createStringDomain();
			dom.set_qualifiedName("String");
			return dom;
		}
		if (typename.equals("EDate")) {
			if (this.dateDomain == null) {
				RecordDomain rec = this.schemagraph.createRecordDomain();
				String[] compNames = { "day", "month", "year", "hour",
						"minute", "second" };
				for (int i = 0; i < 6; i++) {
					Domain component = this.schemagraph.createIntegerDomain();
					component.set_qualifiedName("Integer");
					rec.add_componentdomain(component).set_name(compNames[i]);
				}
				rec.set_qualifiedName(/*
									 * this.packagemap.get(
									 * this.metamodelResource
									 * .getContents().get(0))
									 * .get_qualifiedName() +
									 */"Date");
				this.schemagraph.createContainsDomain(this.packagemap
						.get(this.metamodelResource.getContents().get(0)), rec);
				this.dateDomain = rec;
			}
			return this.dateDomain;
		}
		if (ed instanceof EEnum) {
			return this.enumdomains.get(typename);
		}

		// Maybe try to convert EBigInteger/EBigDecimal to Long/Double
		if (this.configuration.isConvertBigNumbers()
				&& typename.equals("EBigInteger")) {
			System.err.println("Info: Converting " + typename
					+ " to Long as requested by -" + OPTION_CONVERT_BIG_NUMBERS
					+ " option.");
			Domain dom = this.schemagraph.createLongDomain();
			dom.set_qualifiedName("Long");
			Comment c = this.schemagraph.createComment();
			c.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
					+ EAnnotationKeys.WAS_BIG_INTEGER + " " + atOrCompName);
			elForComment.add_comment(c);
			return dom;
		}
		if (this.configuration.isConvertBigNumbers()
				&& typename.equals("EBigDecimal")) {
			System.err.println("Info: Converting " + typename
					+ " to Double as requested by -"
					+ OPTION_CONVERT_BIG_NUMBERS + " option.");
			Domain dom = this.schemagraph.createDoubleDomain();
			dom.set_qualifiedName("Double");
			return dom;
		}

		// If no transformable Domain fits, the result is null
		return null;
	}

	// Variable for setting which of the four Rolename of an Conceptual
	// EdgeClass would be taken
	private boolean takeRolenameOfToEdge = false;

	/**
	 * Transforms all EClasses in the eclassmap into EdgeClasses Puts the
	 * belonging EReferences into the transformedEReferences set
	 * */
	private void transformEClassesIntoEdgeClasses() {
		for (EClass eclass : this.edgeclasses) {

			// Take the empty edgeclass from the map
			EdgeClass edgeclass = this.edgeclassmap.get(eclass);

			// EdgeClasses can have Supertypes
			for (EClass parent : eclass.getESuperTypes()) {
				edgeclass.add_superclass(this.edgeclassmap.get(parent));
			}

			// EdgeClasses can have Attributes
			for (EAttribute eatt : eclass.getEAttributes()) {
				Attribute activeAttribute = this.transformEAttribute(eatt,
						edgeclass);
				if (activeAttribute != null) {
					edgeclass.add_attribute(activeAttribute);
				}
			}

			// Check for former RecordDomains
			// Exclude refs to RecordDomains
			for (EReference ed : eclass.getEReferences()) {
				if (!(ed.getEAnnotation(EAnnotationKeys.SOURCE_STRING) == null)
						&& ed.getEAnnotation(EAnnotationKeys.SOURCE_STRING)
								.getDetails()
								.containsKey(
										EAnnotationKeys.KEY_FOR_REF_TO_RECORD)) {
					this.transformUnidirectionalEReferenceToRecordRef(ed);
				}
			}

			// Package
			this.schemagraph.createContainsGraphElementClass(
					this.packagemap.get(eclass.getEPackage()), edgeclass);

			// Looking, if the user wants the EdgeClass' name different
			String qualName = Ecore2TgAnalyzer.getQualifiedEClassName(eclass);
			String key = this.getKeyIfPossible(qualName,
					this.configuration.getNamesOfEdgeClassesMap());
			if (key != null) {
				String na = this.configuration.getNamesOfEdgeClassesMap().get(
						key);
				// name is qualified
				if (na.contains(".")) {
					edgeclass.set_qualifiedName(na);
				}
				// name is not qualified
				else {
					if (this.packagemap.get(eclass.getEPackage())
							.get_qualifiedName().equals("")) {
						edgeclass.set_qualifiedName(na);
					} else {
						edgeclass.set_qualifiedName(this.packagemap.get(eclass
								.getEPackage()) + "." + na);
					}
				}
			} else {
				if (this.packagemap.get(eclass.getEPackage())
						.get_qualifiedName().equals("")) {
					edgeclass.set_qualifiedName(eclass.getName());
				} else {
					edgeclass.set_qualifiedName(this.packagemap.get(
							eclass.getEPackage()).get_qualifiedName()
							+ "." + eclass.getName());
				}
			}

			// Annotations
			this.transformEAnnotations(eclass.getEAnnotations(), edgeclass);

			EClass eclass1 = null;
			EClass eclass2 = null;
			IncidenceClass inc1 = this.schemagraph.createIncidenceClass();
			IncidenceClass inc2 = this.schemagraph.createIncidenceClass();

			// ------------------------------------------------------------------
			// ------Incidences--------------------------------------------------
			// ------------------------------------------------------------------

			ArrayList<EReference> resultlist = new ArrayList<EReference>();
			boolean[] subtypes = Ecore2TgAnalyzer.getEdgesEReferences(
					this.metamodelResource, eclass,
					this.ereferenceWithOverwritten, resultlist,
					this.badEReferences, this.ereferencesOfEdgeClasses,
					this.ereferencesOfEdgeClassesresult);

			EReference erefFromEdgeToEClass1 = resultlist.get(0);
			EReference erefFromEClass1ToEdge = resultlist.get(1);
			EReference erefFromEdgeToEClass2 = resultlist.get(2);
			EReference erefFromEClass2ToEdge = resultlist.get(3);
			this.transformedEReferences.add(erefFromEdgeToEClass1);
			this.transformedEReferences.add(erefFromEClass1ToEdge);
			this.transformedEReferences.add(erefFromEdgeToEClass2);
			this.transformedEReferences.add(erefFromEClass2ToEdge);

			// Remember additonal containment EReferences of EdgeClasses
			if (((EPackage) this.metamodelResource.getContents().get(0))
					.getEAnnotation(EAnnotationKeys.SOURCE_STRING) == null) {
				if (resultlist.size() == 6) {
					EReference fromCont = resultlist.get(5);
					EReference toCont = resultlist.get(4);
					if (fromCont != null) {
						Comment com = this.schemagraph.createComment();
						VertexClass containerVC = this.vertexclassmap
								.get(fromCont.getEContainingClass());
						String contName;
						if (containerVC != null) {
							contName = containerVC.get_qualifiedName();
						} else {
							contName = this.graphclass.get_qualifiedName();
						}
						com.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
								+ EAnnotationKeys.CONTAINMENT_EXISTS
								+ fromCont.getName() + " " + contName + " TO");
						edgeclass.add_comment(com);
					}
					if (toCont != null) {
						Comment com = this.schemagraph.createComment();
						com.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
								+ EAnnotationKeys.CONTAINMENT_EXISTS
								+ toCont.getName()
								+ " "
								+ this.vertexclassmap.get(
										toCont.getEReferenceType())
										.get_qualifiedName() + " FROM");
						edgeclass.add_comment(com);

					}
				}
			}

			boolean cont1 = false;
			boolean cont2 = false;

			HashSet<EdgeClass> supers = new HashSet<EdgeClass>();
			supers.add(edgeclass);
			while (!supers.isEmpty()) {
				EdgeClass current = supers.iterator().next();
				for (EdgeClass parent : current.get_superclass()) {
					supers.add(parent);
					if (this.takeRolenameOfToEdge) {
						if ((erefFromEClass1ToEdge != null)
								&& (erefFromEClass1ToEdge.getName().equals(
										parent.get_from().get_roleName()) || erefFromEClass1ToEdge
										.getName().equals(
												parent.get_to().get_roleName()))) {
							subtypes[0] = true;
						}
						if ((erefFromEClass2ToEdge != null)
								&& (erefFromEClass2ToEdge.getName().equals(
										parent.get_from().get_roleName()) || erefFromEClass2ToEdge
										.getName().equals(
												parent.get_to().get_roleName()))) {
							subtypes[1] = true;
						}
					} else {
						if ((erefFromEdgeToEClass1 != null)
								&& (erefFromEdgeToEClass1.getName().equals(
										parent.get_from().get_roleName()) || erefFromEdgeToEClass1
										.getName().equals(
												parent.get_to().get_roleName()))) {
							subtypes[1] = true;
						}
						if ((erefFromEdgeToEClass2 != null)
								&& (erefFromEdgeToEClass2.getName().equals(
										parent.get_from().get_roleName()) || erefFromEdgeToEClass2
										.getName().equals(
												parent.get_to().get_roleName()))) {
							subtypes[0] = true;
						}
					}
				}
				supers.remove(current);
			}

			// Set EClasses
			if (erefFromEdgeToEClass1 != null) {
				eclass1 = erefFromEdgeToEClass1.getEReferenceType();
				if (erefFromEdgeToEClass1.isContainment()) {
					cont1 = true;
				}
			}
			if (erefFromEClass1ToEdge != null) {
				eclass1 = erefFromEClass1ToEdge.getEContainingClass();
				if (erefFromEClass1ToEdge.isContainment()) {
					cont2 = true;
				}
			}
			if (erefFromEdgeToEClass2 != null) {
				eclass2 = erefFromEdgeToEClass2.getEReferenceType();
				if (erefFromEdgeToEClass2.isContainment()) {
					cont2 = true;
				}
			}
			if (erefFromEClass2ToEdge != null) {
				eclass2 = erefFromEClass2ToEdge.getEContainingClass();
				if (erefFromEClass2ToEdge.isContainment()) {
					cont1 = true;
				}
			}

			// Fill Incidences
			if ((erefFromEClass2ToEdge != null)
					&& (erefFromEdgeToEClass1 != null)) {
				this.fillIncidenceWith2EReference(inc1, erefFromEClass2ToEdge,
						erefFromEdgeToEClass1, cont2, subtypes[1],
						this.takeRolenameOfToEdge);
			} else if (erefFromEClass2ToEdge != null) {
				this.fillIncidenceWithToEdgeClassEReference(inc1,
						erefFromEClass2ToEdge, cont2, subtypes[1],
						this.takeRolenameOfToEdge);
			} else if (erefFromEdgeToEClass1 != null) {
				this.fillIncidenceWithFromEdgeClassEReference(inc1,
						erefFromEdgeToEClass1, cont2, subtypes[1],
						!this.takeRolenameOfToEdge);
			} else {
				this.fillIncidenceDefault(inc1, cont2);
			}

			if ((erefFromEClass1ToEdge != null)
					&& (erefFromEdgeToEClass2 != null)) {
				this.fillIncidenceWith2EReference(inc2, erefFromEClass1ToEdge,
						erefFromEdgeToEClass2, cont1, subtypes[0],
						this.takeRolenameOfToEdge);
			} else if (erefFromEClass1ToEdge != null) {
				this.fillIncidenceWithToEdgeClassEReference(inc2,
						erefFromEClass1ToEdge, cont1, subtypes[0],
						this.takeRolenameOfToEdge);
			} else if (erefFromEdgeToEClass2 != null) {
				this.fillIncidenceWithFromEdgeClassEReference(inc2,
						erefFromEdgeToEClass2, cont1, subtypes[0],
						!this.takeRolenameOfToEdge);
			} else {
				this.fillIncidenceDefault(inc2, cont1);
			}

			// Direction Determination
			EClass start = null;
			EClass end = null;
			IncidenceClass startOfEdge;
			IncidenceClass endOfEdge;
			EReference toStart;
			EReference fromStart;
			EReference toEnd;
			EReference fromEnd;
			boolean direction1to2;
			boolean directionUserDetermined = false;
			boolean directionAnnotated = false;
			// Look for User Preferences
			if (this.toEReferences.contains(erefFromEClass1ToEdge)
					|| this.toEReferences.contains(erefFromEdgeToEClass2)
					|| this.fromERefererences.contains(erefFromEClass2ToEdge)
					|| this.fromERefererences.contains(erefFromEdgeToEClass1)) {
				// eclass1 is start
				direction1to2 = true;
				directionUserDetermined = true;
			} else if (this.toEReferences.contains(erefFromEClass2ToEdge)
					|| this.toEReferences.contains(erefFromEdgeToEClass1)
					|| this.fromERefererences.contains(erefFromEClass1ToEdge)
					|| this.fromERefererences.contains(erefFromEdgeToEClass2)) {
				// eclass2 is start
				direction1to2 = false;
				directionUserDetermined = true;
			} else if ((erefFromEClass1ToEdge != null)
					&& (erefFromEClass1ToEdge
							.getEAnnotation(EAnnotationKeys.SOURCE_STRING) != null)
					&& erefFromEClass1ToEdge
							.getEAnnotation(EAnnotationKeys.SOURCE_STRING)
							.getDetails()
							.containsKey(EAnnotationKeys.KEY_FOR_DIRECTION)) {
				EAnnotation ean = erefFromEClass1ToEdge
						.getEAnnotation(EAnnotationKeys.SOURCE_STRING);
				String dir = ean.getDetails().get(
						EAnnotationKeys.KEY_FOR_DIRECTION);
				directionUserDetermined = true;
				directionAnnotated = true;
				// EClass1 is start
				if (dir.equalsIgnoreCase("TO")) {
					direction1to2 = true;
				}
				// EClass2 is start
				else {
					direction1to2 = false;
				}
			} else if ((erefFromEdgeToEClass2 != null)
					&& (erefFromEdgeToEClass2
							.getEAnnotation(EAnnotationKeys.SOURCE_STRING) != null)
					&& erefFromEdgeToEClass2
							.getEAnnotation(EAnnotationKeys.SOURCE_STRING)
							.getDetails()
							.containsKey(EAnnotationKeys.KEY_FOR_DIRECTION)) {
				EAnnotation ean = erefFromEdgeToEClass2
						.getEAnnotation(EAnnotationKeys.SOURCE_STRING);
				String dir = ean.getDetails().get(
						EAnnotationKeys.KEY_FOR_DIRECTION);
				directionUserDetermined = true;
				directionAnnotated = true;
				// EClass1 is start
				if (dir.equalsIgnoreCase("TO")) {
					direction1to2 = true;
				}
				// EClass2 is start
				else {
					direction1to2 = false;
				}
			} else if ((erefFromEClass2ToEdge != null)
					&& (erefFromEClass2ToEdge
							.getEAnnotation(EAnnotationKeys.SOURCE_STRING) != null)
					&& erefFromEClass2ToEdge
							.getEAnnotation(EAnnotationKeys.SOURCE_STRING)
							.getDetails()
							.containsKey(EAnnotationKeys.KEY_FOR_DIRECTION)) {
				EAnnotation ean = erefFromEClass2ToEdge
						.getEAnnotation(EAnnotationKeys.SOURCE_STRING);
				String dir = ean.getDetails().get(
						EAnnotationKeys.KEY_FOR_DIRECTION);
				directionUserDetermined = true;
				directionAnnotated = true;
				// EClass2 is start
				if (dir.equalsIgnoreCase("TO")) {
					direction1to2 = false;
				}
				// EClass1 is start
				else {
					direction1to2 = true;
				}
			} else if ((erefFromEdgeToEClass1 != null)
					&& (erefFromEdgeToEClass1
							.getEAnnotation(EAnnotationKeys.SOURCE_STRING) != null)
					&& erefFromEdgeToEClass1
							.getEAnnotation(EAnnotationKeys.SOURCE_STRING)
							.getDetails()
							.containsKey(EAnnotationKeys.KEY_FOR_DIRECTION)) {
				EAnnotation ean = erefFromEdgeToEClass1
						.getEAnnotation(EAnnotationKeys.SOURCE_STRING);
				String dir = ean.getDetails().get(
						EAnnotationKeys.KEY_FOR_DIRECTION);
				directionUserDetermined = true;
				directionAnnotated = true;
				// EClass2 is start
				if (dir.equalsIgnoreCase("TO")) {
					direction1to2 = false;
				}
				// EClass1 is start
				else {
					direction1to2 = true;
				}
			}
			// Look for Containment Preferences
			else if (((inc1.get_aggregation() == AggregationKind.COMPOSITE) && (this.configuration
					.getAggregationInfluenceOnDirection() == Ecore2TgConfiguration.DIRECTION_PART_TO_WHOLE))
					|| ((inc2.get_aggregation() == AggregationKind.COMPOSITE) && (this.configuration
							.getAggregationInfluenceOnDirection() == Ecore2TgConfiguration.DIRECTION_WHOLE_TO_PART))) {
				direction1to2 = true;
			} else if (((inc2.get_aggregation() == AggregationKind.COMPOSITE) && (this.configuration
					.getAggregationInfluenceOnDirection() == Ecore2TgConfiguration.DIRECTION_PART_TO_WHOLE))
					|| ((inc1.get_aggregation() == AggregationKind.COMPOSITE) && (this.configuration
							.getAggregationInfluenceOnDirection() == Ecore2TgConfiguration.DIRECTION_WHOLE_TO_PART))) {
				direction1to2 = false;
			}
			/*
			 * //Only []<-->[]<-->[] has no preference else if(startPointer ==
			 * 1){ start = eclass1; startOfEdge=inc1; end = eclass2;
			 * endOfEdge=inc2; } else if(startPointer == 2){ start = eclass2;
			 * startOfEdge = inc2; end = eclass1; endOfEdge = inc1; }
			 */
			// Default alphabetic Order
			else {
				if (inc1.get_roleName()
						.compareToIgnoreCase(inc2.get_roleName()) < 0) {
					direction1to2 = true;
				} else {
					direction1to2 = false;
				}
			}
			// Setting direction
			if (direction1to2) {
				start = eclass1;
				startOfEdge = inc1;
				end = eclass2;
				endOfEdge = inc2;
				toStart = erefFromEdgeToEClass1;
				fromStart = erefFromEClass1ToEdge;
				toEnd = erefFromEdgeToEClass2;
				fromEnd = erefFromEClass2ToEdge;
			} else {
				start = eclass2;
				startOfEdge = inc2;
				end = eclass1;
				endOfEdge = inc1;
				toEnd = erefFromEdgeToEClass1;
				fromEnd = erefFromEClass1ToEdge;
				toStart = erefFromEdgeToEClass2;
				fromStart = erefFromEClass2ToEdge;
			}

			if (!directionAnnotated) {
				// Check on supertypes
				if (eclass.getESuperTypes().size() != 0) {
					// Iterate over all Supertypes
					for (EClass parent : eclass.getEAllSuperTypes()) {
						// Get start and end eclass from supertype
						EClass parentstarteclass = this.vertexclassmaprevers
								.get(this.edgeclassmap.get(parent).get_from()
										.get_targetclass());
						EClass parentendeclass = this.vertexclassmaprevers
								.get(this.edgeclassmap.get(parent).get_to()
										.get_targetclass());
						// Check if the start nodes are compatible
						if ((start == parentstarteclass)
								|| start.getEAllSuperTypes().contains(
										parentstarteclass)) {
							// if the end nodes are compatible too, the
							// direction
							// can be semantically wrong
							if (((start == parentendeclass) || start
									.getEAllSuperTypes().contains(
											parentendeclass))
									&& (eclass.getEReferences().size() != 0)) {
								// Try.......
								ArrayList<EReference> res = new ArrayList<EReference>();
								Ecore2TgAnalyzer.getEdgesEReferences(
										this.metamodelResource, parent,
										this.ereferenceWithOverwritten, res,
										this.badEReferences,
										this.ereferencesOfEdgeClasses,
										this.ereferencesOfEdgeClassesresult);

								EReference erefPFromEdgeToEClass1 = res.get(0);
								EReference erefPFromEdgeToEClass2 = res.get(2);
								boolean st = false;

								if ((erefFromEdgeToEClass1 != null)
										&& (erefPFromEdgeToEClass1 != null)
										&& (this.ereferenceWithOverwritten
												.get(erefFromEdgeToEClass1) != null)
										&& this.ereferenceWithOverwritten.get(
												erefFromEdgeToEClass1)
												.contains(
														erefPFromEdgeToEClass1)) {
									st = this.ereferencesEdgeClass2start
											.contains(erefPFromEdgeToEClass1)
											&& (toEnd == erefFromEdgeToEClass1);
									st = st
											|| (this.ereferencesEdgeClass2target
													.contains(erefPFromEdgeToEClass1) && (toStart == erefFromEdgeToEClass1));
								} else if ((erefFromEdgeToEClass1 != null)
										&& (erefPFromEdgeToEClass2 != null)
										&& (this.ereferenceWithOverwritten
												.get(erefFromEdgeToEClass1) != null)
										&& this.ereferenceWithOverwritten.get(
												erefFromEdgeToEClass1)
												.contains(
														erefPFromEdgeToEClass2)) {
									st = this.ereferencesEdgeClass2start
											.contains(erefPFromEdgeToEClass2)
											&& (toEnd == erefFromEdgeToEClass1);
									st = st
											|| (this.ereferencesEdgeClass2target
													.contains(erefPFromEdgeToEClass2) && (toStart == erefFromEdgeToEClass1));
								} else if ((erefFromEdgeToEClass2 != null)
										&& (erefPFromEdgeToEClass2 != null)
										&& (this.ereferenceWithOverwritten
												.get(erefFromEdgeToEClass2) != null)
										&& this.ereferenceWithOverwritten.get(
												erefFromEdgeToEClass2)
												.contains(
														erefPFromEdgeToEClass2)) {
									st = this.ereferencesEdgeClass2start
											.contains(erefPFromEdgeToEClass2)
											&& (toEnd == erefFromEdgeToEClass2);
									st = st
											|| (this.ereferencesEdgeClass2target
													.contains(erefPFromEdgeToEClass2) && (toStart == erefFromEdgeToEClass2));
								} else if ((erefFromEdgeToEClass2 != null)
										&& (erefPFromEdgeToEClass1 != null)
										&& (this.ereferenceWithOverwritten
												.get(erefFromEdgeToEClass2) != null)
										&& this.ereferenceWithOverwritten.get(
												erefFromEdgeToEClass2)
												.contains(
														erefPFromEdgeToEClass1)) {
									st = this.ereferencesEdgeClass2start
											.contains(erefPFromEdgeToEClass1)
											&& (toEnd == erefFromEdgeToEClass2);
									st = st
											|| (this.ereferencesEdgeClass2target
													.contains(erefPFromEdgeToEClass1) && (toStart == erefFromEdgeToEClass2));
								} else {
									System.err
											.println("Warning: "
													+ parent.getName()
													+ " is parent of "
													+ eclass.getName()
													+ ", but the directions of the EdgeClasses are not savely set.");
								}
								if (st) {
									// Direction false
									EClass temp = start;
									start = end;
									end = temp;
									IncidenceClass tempin = startOfEdge;
									startOfEdge = endOfEdge;
									endOfEdge = tempin;
									EReference tempref = toStart;
									toStart = toEnd;
									toEnd = tempref;
									tempref = fromStart;
									fromStart = fromEnd;
									fromEnd = tempref;
								}
								// END Try
							}
						}
						// If the start nodes are not compatible, try the
						// endnode,
						// if ok swap start and end
						else if ((end == parentstarteclass)
								|| end.getEAllSuperTypes().contains(
										parentstarteclass)) {
							if (directionUserDetermined) {
								System.err
										.println("EdgeClass "
												+ eclass.getName()
												+ " had a user determined or annotated direction that was not used for transformation, because the Supertype direction was different.");
							}
							EClass temp = start;
							start = end;
							end = temp;
							IncidenceClass tempin = startOfEdge;
							startOfEdge = endOfEdge;
							endOfEdge = tempin;
							EReference tempref = toStart;
							toStart = toEnd;
							toEnd = tempref;
							tempref = fromStart;
							fromStart = fromEnd;
							fromEnd = tempref;
						} else {
							System.err
									.println("The EdgeClasses "
											+ eclass.getName()
											+ " and "
											+ parent.getName()
											+ " are in supertype relationship, but the incidences are not compatible.");
							System.exit(1);
						}
					}
				}
			}
			// Add the incidences to the created EdgeClass
			edgeclass.add_from(startOfEdge);
			edgeclass.add_to(endOfEdge);

			// Add the incidences to the VertexClasses
			this.vertexclassmap.get(start).add_incidence(startOfEdge);
			this.vertexclassmap.get(end).add_incidence(endOfEdge);

			if (toStart != null) {
				this.ereferencesEdgeClass2start.add(toStart);
			}
			if (toEnd != null) {
				this.ereferencesEdgeClass2target.add(toEnd);
			}

			if (((eclass1 == start) && subtypes[0])
					|| ((eclass2 == start) && subtypes[1])) {
				toStart = null;
				fromStart = null;
			}
			if (((eclass1 == end) && subtypes[0])
					|| ((eclass2 == end) && subtypes[1])) {
				toEnd = null;
				fromEnd = null;
			}
			// Save in comments which EReferences exists and which are generated
			// by Ecore2Tg
			if (toStart == null) {
				Comment c = this.schemagraph.createComment();
				c.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
						+ EAnnotationKeys.GENERATE_DIRECTION_TO_START);
				edgeclass.add_comment(c);
			} else if (this.takeRolenameOfToEdge) {
				Comment c = this.schemagraph.createComment();
				c.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
						+ EAnnotationKeys.REFERENCE_NAME_TO_START_WAS
						+ toStart.getName());
				edgeclass.add_comment(c);
			}
			if (fromStart == null) {
				Comment c = this.schemagraph.createComment();
				c.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
						+ EAnnotationKeys.GENERATE_DIRECTION_FROM_START);
				edgeclass.add_comment(c);
			} else if (!this.takeRolenameOfToEdge) {
				Comment c = this.schemagraph.createComment();
				c.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
						+ EAnnotationKeys.REFERENCE_NAME_TO_TARGET_WAS
						+ fromStart.getName());
				edgeclass.add_comment(c);
			}
			if (toEnd == null) {
				Comment c = this.schemagraph.createComment();
				c.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
						+ EAnnotationKeys.GENERATE_DIRECTION_TO_END);
				edgeclass.add_comment(c);
			} else if (this.takeRolenameOfToEdge) {
				Comment c = this.schemagraph.createComment();
				c.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
						+ EAnnotationKeys.REFERENCE_NAME_TO_TARGET_WAS
						+ toEnd.getName());
				edgeclass.add_comment(c);
			}
			if (fromEnd == null) {
				Comment c = this.schemagraph.createComment();
				c.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
						+ EAnnotationKeys.GENERATE_DIRECTION_FROM_END);
				edgeclass.add_comment(c);
			} else if (!this.takeRolenameOfToEdge) { // @TODO wenns so bleiben
				// soll, annotation key
				// ändern
				Comment c = this.schemagraph.createComment();
				c.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
						+ EAnnotationKeys.REFERENCE_NAME_TO_START_WAS
						+ fromEnd.getName());
				edgeclass.add_comment(c);
			}

		}// End Iteration over all EClasses that are EdgeClasses

	}

	/**
	 * Transforms 1. the SuperTypeConnections between EClasses into
	 * SuperTypeConnections between VertexClasses 2. the EAttributes of EClasses
	 * into Attributes of VertexClasses 3. the EReferences of EClasses into
	 * EdgeClasses
	 * */
	private void transformSuperTypeConnectionsAndEStructuralFeatures() {
		// Iterate over all transformed EClasses
		for (EClass eclass : this.vertexclassmap.keySet()) {
			VertexClass vertexClass = this.vertexclassmap.get(eclass);

			// Transform SuperType Connections
			for (EClass supereclass : eclass.getESuperTypes()) {
				VertexClass supervertex = this.vertexclassmap.get(supereclass);
				if (supervertex != null) {
					vertexClass.add_superclass(supervertex);
				} else {
					System.err.println("Program can not add supertype "
							+ supereclass + " to "
							+ vertexClass.get_qualifiedName());
				}
			}

			// Transform EAttributes
			for (EAttribute eatt : eclass.getEAttributes()) {
				Attribute activeAttribute = this.transformEAttribute(eatt,
						vertexClass);
				if (activeAttribute != null) {
					vertexClass.add_attribute(activeAttribute);
				}
			}

			// Transform EReferences as if they aren't transformed yet
			for (EReference ereference : eclass.getEReferences()) {
				if (this.transformedEReferences.contains(ereference)) {
					continue;
				}

				this.transformEReference(ereference);
			}
		}
	}

	/**
	 * Transforms an EReference into an EdgeClass, if the EReference has an
	 * Opposite, that becomes transformed too
	 * 
	 * @param ereference
	 *            the EReference to transform
	 * */
	private void transformEReference(EReference ereference) {
		if (ereference.getEOpposite() != null) {
			this.transformBidirectionalEReferences(ereference);
		} else if ((ereference.getEAnnotation(EAnnotationKeys.SOURCE_STRING) != null)
				&& ereference.getEAnnotation(EAnnotationKeys.SOURCE_STRING)
						.getDetails()
						.containsKey(EAnnotationKeys.KEY_FOR_REF_TO_RECORD)) {
			this.transformUnidirectionalEReferenceToRecordRef(ereference);
		} else {
			this.transformUnidirectionalEReferences(ereference);
		}
	}

	private void transformUnidirectionalEReferenceToRecordRef(EReference eref) {
		this.recordDomainEReferences.add(eref);
		this.badEReferences.add(eref);// for the instance transformation
		EClass target = eref.getEReferenceType();
		RecordDomain rd = this.eclass2recorddomain_map.get(target);
		Attribute at = this.schemagraph.createAttribute();
		EClass start = eref.getEContainingClass();

		at.set_name(eref.getName());
		if (eref.getUpperBound() != 1) {
			if (eref.isOrdered()) {
				// List
				ListDomain d = this.schemagraph.createListDomain();
				d.set_qualifiedName("ListDomain");
				d.add_basedomain(rd);
				at.add_domain(d);
			} else {
				// Set
				SetDomain d = this.schemagraph.createSetDomain();
				d.set_qualifiedName("SetDomain");
				d.add_basedomain(rd);
				at.add_domain(d);
			}

		} else {
			at.add_domain(rd);
		}

		GraphElementClass gec = null;
		if (this.vertexclassmap.containsKey(start)) {
			gec = this.vertexclassmap.get(start);
		} else if (this.edgeclassmap.containsKey(start)) {
			gec = this.edgeclassmap.get(start);
		}
		gec.add_attribute(at);

	}

	/**
	 * Transforms an unidirectional EReference into an EdgeClass,
	 * 
	 * @param ereference
	 *            the EReference to transform
	 * */
	private void transformUnidirectionalEReferences(EReference ereference) {

		// The EReference should not become transformed more than ones
		this.transformedEReferences.add(ereference);

		String refName = Ecore2TgAnalyzer.getQualifiedReferenceName(ereference);

		EClass eclass1 = ereference.getEContainingClass();
		EClass eclass2 = ereference.getEReferenceType();

		// If target is graphclass, don't transform
		if (!this.vertexclassmap.containsKey(eclass2)) {
			return;
		}

		// Creation of the IncidenceClasses of the EdgeClass
		IncidenceClass inc1 = this.schemagraph.createIncidenceClass();
		IncidenceClass inc2 = this.schemagraph.createIncidenceClass();

		// Fill Incidences
		this.fillIncidenceDefault(inc1, ereference.isContainment());
		this.fillIncidenceWith1EReference(inc2, ereference);

		// Add the incidences to the VertexClasses
		this.vertexclassmap.get(eclass1).add_incidence(inc1);
		this.vertexclassmap.get(eclass2).add_incidence(inc2);

		// Create a new EdgeClass
		EdgeClass activeEdgeClass = this.schemagraph.createEdgeClass();

		// Define start
		EClass start;
		int direction;
		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// Direction
		// --Check if user has determined direction
		if (this.toEReferences.contains(ereference)) {
			start = eclass1;
			direction = Ecore2TgConfiguration.TO;
		} else if (this.fromERefererences.contains(ereference)) {
			start = eclass2;
			direction = Ecore2TgConfiguration.FROM;
		}
		// --Check if Composition changes the direction
		else if (ereference.isContainment()
				&& (this.configuration.getAggregationInfluenceOnDirection() == Ecore2TgConfiguration.DIRECTION_PART_TO_WHOLE)) {
			start = eclass2;
			direction = Ecore2TgConfiguration.FROM;
		}
		// --Else, take the ereference direction
		else {
			start = eclass1;
			direction = Ecore2TgConfiguration.TO;
		}

		// Add the incidences to the created EdgeClass
		Comment c = this.schemagraph.createComment();
		if (direction == Ecore2TgConfiguration.TO) {
			activeEdgeClass.add_from(inc1);
			activeEdgeClass.add_to(inc2);
			this.definingDirectionEReferences.add(ereference);
			c.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
					+ EAnnotationKeys.GENERATE_DIRECTION_FROM);

		} else {
			activeEdgeClass.add_from(inc2);
			activeEdgeClass.add_to(inc1);
			c.set_text(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
					+ EAnnotationKeys.GENERATE_DIRECTION_FROM);
		}
		activeEdgeClass.add_comment(c);

		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// Determine the Name of the EdgeClass

		EAnnotation ean = ereference
				.getEAnnotation(EAnnotationKeys.SOURCE_STRING);
		String name = "";// Set an empty Name

		// ---Test, if the user has determined a name for the EReference
		String keystring = this.getKeyIfPossible(refName,
				this.configuration.getNamesOfEdgeClassesMap());
		if (keystring != null) {
			name = this.configuration.getNamesOfEdgeClassesMap().get(keystring);
		}

		// ---Test if there is an EAnnotation for the name
		else if (ean != null) {
			name = ean.getDetails().get(EAnnotationKeys.KEY_FOR_EDGECLASS_NAME);
			if (name == null) {
				name = "";
			}
		}

		// Put into Package
		Package targetpack = null;
		// -- if the user determined the name, the package is determined too
		if (name.contains(".")) {
			targetpack = this.getPackageByName(name.substring(0,
					name.lastIndexOf(".")));
		}

		// --If there is an user entry for ereference, take it
		if (targetpack == null) {
			targetpack = this.lookUpPackage(refName);
		}

		// --If there is an EAnnotation, use it
		if ((targetpack == null) && (ean != null)) {
			String packname = ean.getDetails().get(
					EAnnotationKeys.KEY_FOR_PACKAGE_NAME);
			if (packname != null) {
				targetpack = this.packagemap.get(this
						.getPackageByName(packname));
			}
		}

		// --else, take the ClassPackage
		if (targetpack == null) {
			targetpack = this.packagemap.get(start.getEPackage());
		}
		this.schemagraph.createContainsGraphElementClass(targetpack,
				activeEdgeClass);

		// Check if user determined a simple name
		// --If yes, make a qualified name out of it
		if (!name.equals("") && !name.contains(".")
				&& !targetpack.get_qualifiedName().equals("")) {

			name = targetpack.get_qualifiedName() + "." + name;
		}

		// Set Name
		activeEdgeClass.set_qualifiedName(name);

		// Annotations
		this.transformEAnnotations(ereference.getEAnnotations(),
				activeEdgeClass);

		// Save for Model Transformation
		this.ereference2edgeclass_map.put(ereference, activeEdgeClass);
	}

	/**
	 * Transforms an bidirectional EReference into an EdgeClass,
	 * 
	 * @param ereference
	 *            the EReference to transform
	 * */
	private void transformBidirectionalEReferences(EReference ereference) {

		// Take the opposite
		EReference opposite = ereference.getEOpposite();

		// The EReference should not become transformed more than ones
		this.transformedEReferences.add(ereference);
		this.transformedEReferences.add(opposite);

		// Take the qualified names for further investigation
		String refName = Ecore2TgAnalyzer.getQualifiedReferenceName(ereference);
		String oppName = Ecore2TgAnalyzer.getQualifiedReferenceName(opposite);
		int helpvar = 0;

		EClass eclass1 = ereference.getEContainingClass();
		EClass eclass2 = ereference.getEReferenceType();

		// If target is graphclass, don't transform
		if (!this.vertexclassmap.containsKey(eclass2)) {
			return;
		}

		EClass start;

		// Create a new EdgeClass
		EdgeClass activeEdgeClass = this.schemagraph.createEdgeClass();

		// Creation of the IncidenceClasses of the EdgeClass
		IncidenceClass inc1 = this.schemagraph.createIncidenceClass();
		IncidenceClass inc2 = this.schemagraph.createIncidenceClass();

		// Fill Incidences
		this.fillIncidenceWith1EReference(inc1, opposite);
		this.fillIncidenceWith1EReference(inc2, ereference);

		// Add the incidences to the VertexClasses
		this.vertexclassmap.get(eclass1).add_incidence(inc1);
		this.vertexclassmap.get(eclass2).add_incidence(inc2);

		// Annotations
		this.transformEAnnotations(ereference.getEAnnotations(),
				activeEdgeClass);
		this.transformEAnnotations(opposite.getEAnnotations(), activeEdgeClass);

		// Take the grUML Annotations
		EAnnotation ean = ereference
				.getEAnnotation(EAnnotationKeys.SOURCE_STRING);
		EAnnotation eanOpp = opposite
				.getEAnnotation(EAnnotationKeys.SOURCE_STRING);

		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// Name

		// Determine the Name of the EdgeClass
		activeEdgeClass.set_qualifiedName(""); // Set an empty Name
		String name = "";
		// ---Test, if the user has determined a name for the EReference
		String keystring = this.getKeyIfPossible(refName,
				this.configuration.getNamesOfEdgeClassesMap());
		if (keystring != null) {
			name = this.configuration.getNamesOfEdgeClassesMap().get(keystring);
			helpvar = 1;
		}
		// ---If not, test, if the user has determined a name for the Opposite
		else {
			keystring = this.getKeyIfPossible(oppName,
					this.configuration.getNamesOfEdgeClassesMap());
			if (keystring != null) {
				name = this.configuration.getNamesOfEdgeClassesMap().get(
						keystring);
				helpvar = 2;
			}
		}

		// ---Test, if there is an Annotation for the name on ereference
		if (ean != null) {
			keystring = ean.getDetails().get(
					EAnnotationKeys.KEY_FOR_EDGECLASS_NAME);
			if (keystring != null) {
				name = keystring;
				helpvar = 1;
			}
		}
		// Test if there is Annotation for the name on opposite
		else if (eanOpp != null) {
			keystring = eanOpp.getDetails().get(
					EAnnotationKeys.KEY_FOR_EDGECLASS_NAME);
			if (keystring != null) {
				name = keystring;
				helpvar = 2;
			}
		}

		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

		// Direction
		int direction;
		if (this.toEReferences.contains(ereference)) {
			start = eclass1;
			direction = Ecore2TgConfiguration.TO;
		} else if (this.toEReferences.contains(opposite)) {
			start = eclass2;
			direction = Ecore2TgConfiguration.FROM;
		} else if (this.fromERefererences.contains(ereference)) {
			start = eclass2;
			direction = Ecore2TgConfiguration.FROM;
		} else if (this.fromERefererences.contains(opposite)) {
			start = eclass1;
			direction = Ecore2TgConfiguration.TO;
		}
		// User hasn't determined Direction
		// Check if a EReference is annotated
		else if ((ean != null)
				&& (ean.getDetails().get(EAnnotationKeys.KEY_FOR_DIRECTION) != null)) {
			String s = ean.getDetails().get(EAnnotationKeys.KEY_FOR_DIRECTION);
			if (s.equals("TO")) {
				start = eclass1;
				direction = Ecore2TgConfiguration.TO;
			} else {
				start = eclass2;
				direction = Ecore2TgConfiguration.FROM;
			}
		} else if ((eanOpp != null)
				&& (eanOpp.getDetails().get(EAnnotationKeys.KEY_FOR_DIRECTION) != null)) {
			String s = eanOpp.getDetails().get(
					EAnnotationKeys.KEY_FOR_DIRECTION);
			if (s.equals("FROM")) {
				start = eclass1;
				direction = Ecore2TgConfiguration.TO;
			} else {
				start = eclass2;
				direction = Ecore2TgConfiguration.FROM;
			}
		}
		// Check if user has named and indirectly defined
		else if (helpvar == 1) {
			start = eclass1;
			direction = Ecore2TgConfiguration.TO;
		} else if (helpvar == 2) {
			start = eclass2;
			direction = Ecore2TgConfiguration.FROM;
		}
		// Check if Composition changes the direction
		else if ((ereference.isContainment() && (this.configuration
				.getAggregationInfluenceOnDirection() == Ecore2TgConfiguration.DIRECTION_WHOLE_TO_PART))
				|| (opposite.isContainment() && (this.configuration
						.getAggregationInfluenceOnDirection() == Ecore2TgConfiguration.DIRECTION_PART_TO_WHOLE))) {
			start = eclass1;
			direction = Ecore2TgConfiguration.TO;
		} else if ((opposite.isContainment() && (this.configuration
				.getAggregationInfluenceOnDirection() == Ecore2TgConfiguration.DIRECTION_WHOLE_TO_PART))
				|| (ereference.isContainment() && (this.configuration
						.getAggregationInfluenceOnDirection() == Ecore2TgConfiguration.DIRECTION_PART_TO_WHOLE))) {
			start = eclass2;
			direction = Ecore2TgConfiguration.FROM;
		}
		// Take Default Alphabetical Order
		else {
			if (ereference.getName().compareTo(opposite.getName()) < 0) {
				start = eclass1;
				direction = Ecore2TgConfiguration.TO;
			} else {
				start = eclass2;
				direction = Ecore2TgConfiguration.FROM;
			}
		}

		// Add the incidences to the created EdgeClass
		if (direction == Ecore2TgConfiguration.TO) {
			activeEdgeClass.add_from(inc1);
			activeEdgeClass.add_to(inc2);
			this.definingDirectionEReferences.add(ereference);
		} else {
			activeEdgeClass.add_from(inc2);
			activeEdgeClass.add_to(inc1);
			this.definingDirectionEReferences.add(opposite);
		}

		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// Put into Package
		Package targetpack = null;
		// Look, if the name has determined the package too
		if (name.contains(".")) {
			targetpack = this.getPackageByName(name.substring(0,
					name.lastIndexOf(".")));
		}
		// --If there is an user entry for ereference, take it
		if (targetpack == null) {
			targetpack = this.lookUpPackage(refName);
		}
		// --If it is not, try the opposite, if it exists
		if ((targetpack == null) && (ereference.getEOpposite() != null)) {
			targetpack = this.lookUpPackage(oppName);
		}
		// --If there is an EAnnotation, use it
		// EAnnotation ean =
		// ereference.getEAnnotation(EAnnotationKeys.SOURCE_STRING);
		if ((targetpack == null) && (ean != null)) {
			String packname = ean.getDetails().get(
					EAnnotationKeys.KEY_FOR_PACKAGE_NAME);
			if (packname != null) {
				targetpack = this.getPackageByName(packname);
			}
		}
		if ((targetpack == null) && (eanOpp != null)) {
			String packname = ean.getDetails().get(
					EAnnotationKeys.KEY_FOR_PACKAGE_NAME);
			if (packname != null) {
				targetpack = this.getPackageByName(packname);
			}
		}
		// --else, take the ClassPackage
		if (targetpack == null) {
			targetpack = this.packagemap.get(start.getEPackage());
		}
		this.schemagraph.createContainsGraphElementClass(targetpack,
				activeEdgeClass);

		if (!name.equals("") && !name.contains(".")) {
			name = targetpack.get_qualifiedName() + "." + name;
		}
		// Set name
		activeEdgeClass.set_qualifiedName(name);

		// Save for Model Transformation
		this.ereference2edgeclass_map.put(ereference, activeEdgeClass);
		this.ereference2edgeclass_map.put(opposite, activeEdgeClass);
	}

	// --------------------------------------------------------------------------
	// -------Fill Incidences methods-------------------------------------------
	// --------------------------------------------------------------------------

	private void fillIncidenceDefault(IncidenceClass ic, boolean cont) {
		if (!cont) {
			ic.set_min(0);
			ic.set_max(Integer.MAX_VALUE);
		} else {
			ic.set_min(0);
			ic.set_max(1);
		}
		ic.set_roleName("");
		ic.set_aggregation(AggregationKind.NONE);
	}

	private void fillIncidenceWith1EReference(IncidenceClass ic, EReference e) {
		ic.set_min(e.getLowerBound());

		if (e.getUpperBound() != -1) {
			ic.set_max(e.getUpperBound());
		} else {
			ic.set_max(Integer.MAX_VALUE);
		}

		ic.set_roleName(e.getName());
		if (e.isContainment()) {
			ic.set_aggregation(AggregationKind.COMPOSITE);
		} else {
			// Look for Annotations
			EAnnotation ean = e.getEAnnotation(EAnnotationKeys.SOURCE_STRING);
			if ((ean != null)
					&& (ean.getDetails()
							.get(EAnnotationKeys.KEY_IS_AGGREGATION) != null)) {
				ic.set_aggregation(AggregationKind.SHARED);
			} else {
				ic.set_aggregation(AggregationKind.NONE);
			}
		}
	}

	private void fillIncidenceWithFromEdgeClassEReference(IncidenceClass ic,
			EReference acceptref, boolean cont, boolean subtype,
			boolean takeFromEdgeClassRoleName) {
		if (!cont) {
			ic.set_min(0);
			ic.set_max(Integer.MAX_VALUE);
		} else {
			ic.set_min(0);
			ic.set_max(1);
		}
		if (subtype || !takeFromEdgeClassRoleName) {
			ic.set_roleName("");
		} else {
			ic.set_roleName(acceptref.getName());
		}
		if (acceptref.isContainment()) {
			ic.set_aggregation(AggregationKind.COMPOSITE);
			System.err.println("Transformation of the EReferences "
					+ acceptref.getName() + " has an composition problem.");
		} else {
			ic.set_aggregation(AggregationKind.NONE);
		}
	}

	/**
	 * Fills the IncidenceClass ic with the multiplicity of the EReference e
	 * 
	 * @param ic
	 *            IncidenceClass to fill
	 * @param e
	 *            EReference to get multiplicity and rolename
	 * @param cont
	 *            boolean, if the EReference is the opposite of a containment
	 *            one
	 * @param subtype
	 *            boolean, if the EReference redefines another EReference
	 */
	private void fillIncidenceWithToEdgeClassEReference(IncidenceClass ic,
			EReference e, boolean cont, boolean subtype,
			boolean takeToEdgeRoleName) {
		if (!cont) {
			ic.set_min(e.getLowerBound());
			if (e.getUpperBound() != -1) {
				ic.set_max(e.getUpperBound());
			} else {
				ic.set_max(Integer.MAX_VALUE);
			}
		} else {
			ic.set_min(0);
			ic.set_max(1);
		}
		if (subtype || !takeToEdgeRoleName) {
			ic.set_roleName("");
		} else {
			ic.set_roleName(e.getName());
		}
		if (e.isContainment()) {
			ic.set_aggregation(AggregationKind.COMPOSITE);
		} else {
			// Look for Annotations
			EAnnotation ean = e.getEAnnotation(EAnnotationKeys.SOURCE_STRING);
			if ((ean != null)
					&& (ean.getDetails()
							.get(EAnnotationKeys.KEY_IS_AGGREGATION) != null)) {
				ic.set_aggregation(AggregationKind.SHARED);
			} else {
				ic.set_aggregation(AggregationKind.NONE);
			}
		}
	}

	private void fillIncidenceWith2EReference(IncidenceClass ic,
			EReference eref, EReference acceptref, boolean cont,
			boolean subtype, boolean takeToEdgeAsRoleName) {
		if (!cont) {
			ic.set_min(eref.getLowerBound());
			if (eref.getUpperBound() != -1) {
				ic.set_max(eref.getUpperBound());
			} else {
				ic.set_max(Integer.MAX_VALUE);
			}
		} else {
			ic.set_min(0);
			ic.set_max(1);
		}
		if (subtype) {
			ic.set_roleName("");
		} else if (takeToEdgeAsRoleName) {
			ic.set_roleName(eref.getName());
		} else {
			ic.set_roleName(acceptref.getName());
		}
		if (eref.isContainment() && acceptref.isContainment()) {
			ic.set_aggregation(AggregationKind.COMPOSITE);
		} else if (eref.isContainment() || acceptref.isContainment()) {
			ic.set_aggregation(AggregationKind.COMPOSITE);
			System.err.println("Transformation of the EReferences "
					+ eref.getName() + " and " + acceptref.getName()
					+ " has an composition problem.");
		} else {
			// Look for Annotations
			EAnnotation ean = eref
					.getEAnnotation(EAnnotationKeys.SOURCE_STRING);
			if ((ean != null)
					&& (ean.getDetails()
							.get(EAnnotationKeys.KEY_IS_AGGREGATION) != null)) {
				ic.set_aggregation(AggregationKind.SHARED);
			} else {
				ic.set_aggregation(AggregationKind.NONE);
			}
		}
	}

	// --------------------------------------------------------------------------
	// -------Convenience methods-----------------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * Searchs in a Resource for the EPackage with the qualified name
	 * 
	 * @param qualname
	 *            qualified name of an EPackage
	 * 
	 * @return EPackage that is found or null if it does not exist
	 * */
	private EPackage getEPackageByName(String qualname) {
		for (EObject ob : this.metamodelResource.getContents()) {
			EPackage p = (EPackage) ob;
			if (p.getName().equalsIgnoreCase(qualname)) {
				return p;
			}
			EPackage res = this.getEPackageByName(qualname, p, "");
			if (res != null) {
				return res;
			}
		}
		return null;
	}

	/**
	 * Searchs in a Resource for the EPackage with the specified name
	 * 
	 * @param qualname
	 *            qualified name of an EPackage
	 * @param pack
	 *            EPackage to compare
	 * @param packprefix
	 *            qualified name of pack
	 * 
	 * @return EPackage that is found or null if it does not exist
	 * */
	private EPackage getEPackageByName(String qualname, EPackage pack,
			String packprefix) {
		if ((packprefix + "." + pack.getName()).equalsIgnoreCase(qualname)) {
			return pack;
		}
		for (EPackage child : pack.getESubpackages()) {
			EPackage res = this.getEPackageByName(qualname, child, packprefix
					+ pack.getName());
			if (res != null) {
				return res;
			}
		}
		return null;
	}

	/**
	 * Searches for a Package with a given qualified name
	 * 
	 * @param qualName
	 *            The qualified name of the Package
	 * @return the Package with the given qualified name or null if there is no
	 *         one
	 * */
	private Package getPackageByName(String qualName) {
		Iterator<Package> targetPackages = this.schemagraph
				.getPackageVertices().iterator();
		while (targetPackages.hasNext()) {
			Package targetpack = targetPackages.next();
			if (targetpack.get_qualifiedName().equals(qualName)) {
				return targetpack;
			}
		}
		return null;
	}

	private Package lookUpPackage(String name) {
		String packagekey = this.getKeyIfPossible(name,
				this.configuration.getDefinedPackagesOfEdgeClassesMap());
		if (packagekey == null) {
			return null;
		}
		String packagename = this.configuration
				.getDefinedPackagesOfEdgeClassesMap().get(packagekey);
		return this.getPackageByName(packagename);
	}

	/**
	 * Checks if in a map is a key equal to a String If there is, it returns the
	 * key
	 * 
	 * @param equalToKey
	 *            String to look for
	 * @return the key equal to the given String or null if there is no
	 * */
	private String getKeyIfPossible(String equalToKey,
			HashMap<String, String> map) {
		for (String str : map.keySet()) {
			if (str.equalsIgnoreCase(equalToKey)) {
				return str;
			}
		}
		return null;
	}

	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------
	// -------Model transformation----------------------------------------------
	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * Maps the EObjects that corresponds to EClasses that have been transformed
	 * to VertexClasses to the created Vertexes
	 * */
	private HashMap<EObject, Vertex> objectsToVertexes;

	/**
	 * Set with all EObjects that corresponds to EClasses that have been
	 * transformed to EdgeClasses
	 * */
	private HashSet<EObject> edges;

	/**
	 * Schema
	 * */
	private de.uni_koblenz.jgralab.schema.Schema schem;

	/**
	 * Complicated map to remember what links are already transformed, necessary
	 * because of opposites and so on A link is saved as a triple of the EObject
	 * it belongs to, the EReference the link is an instance of and the EObject
	 * it links to.
	 * */
	private HashMap<EObject, HashMap<EReference, ArrayList<EObject>>> transformedLinks;

	public Graph transformModel(EList<EObject> objects, String[] paths) {
		// Take the time
		System.out.println("Transformation of Model started...");
		long starttime = System.currentTimeMillis();

		// Initialize Maps
		this.objectsToVertexes = new HashMap<EObject, Vertex>();
		this.edges = new HashSet<EObject>();
		this.transformedLinks = new HashMap<EObject, HashMap<EReference, ArrayList<EObject>>>();

		return this.startModelTransformation(objects, paths, starttime);
	}

	private Graph startModelTransformation(EList<EObject> objects,
			String[] paths, long starttime) {
		Graph graph;
		try {
			graph = this.schem.createGraph(ImplementationType.GENERIC,
					paths[0].substring(paths[0].lastIndexOf(".")) + "Graph",
					40, 50);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			System.out
					.println("GraphClass can not become created. Transformation of model aborted");
			return null;
		}

		// Transform Objects
		this.examineEObjects(objects, graph);
		this.transformEdgesFromEClasses(objects, graph);
		this.transformVertices(graph);

		// Transformation ready
		long endtime = System.currentTimeMillis();
		System.out.println("Transformation of Model finished. It took "
				+ (endtime - starttime) + " milliseconds.");

		return graph;
	}

	/**
	 * Transforms a model fitting to the metamodel
	 * 
	 * @param paths
	 *            Paths to model files
	 * @return Graph that represents the transformed model
	 * */
	public Graph transformModel(String[] paths) {

		// Take the time
		System.out.println("Transformation of Model started...");
		long starttime = System.currentTimeMillis();

		// Initialize Maps
		this.objectsToVertexes = new HashMap<EObject, Vertex>();
		this.edges = new HashSet<EObject>();
		this.transformedLinks = new HashMap<EObject, HashMap<EReference, ArrayList<EObject>>>();

		// Load the Model
		EList<EObject> objects = this.loadModelFromXMIFile(paths);
		long loadtime = System.currentTimeMillis();
		System.out.println("Model loaded. It took " + (loadtime - starttime)
				+ " milliseconds");

		return this.startModelTransformation(objects, paths, starttime);
	}

	/**
	 * Iterates over all EObjects, creates empty Vertexes for the instances of
	 * VertexClasses and collects the instances of EdgeClasses in a Set
	 * 
	 * @param objectlist
	 *            EList<EObject> list with all EObjects of the model
	 * @param graph
	 *            Graph the created Graph to the model
	 * @param modelSchema
	 *            Schema to the model
	 * */
	private void examineEObjects(EList<EObject> objectlist, Graph graph) {
		for (EObject eob : objectlist) {

			// Get the EClass
			EClass eclass = eob.eClass();
			if ((eclass.getName().equals("EEnumLiteral"))) {
				continue;
			}

			// Get the transformed VertexClass
			VertexClass transVC = this.vertexclassmap.get(eclass);

			// EObject is a Vertex
			if (transVC != null) {
				de.uni_koblenz.jgralab.schema.VertexClass vertexclass = (de.uni_koblenz.jgralab.schema.VertexClass) this.schem
						.getAttributedElementClass((transVC.get_qualifiedName()));

				Vertex activeVertex = graph.createVertex(vertexclass);
				this.objectsToVertexes.put(eob, activeVertex);
			}
			// EObject is an Edge
			else if (this.edgeclassmap.get(eclass) != null) {
				EdgeClass transEC = this.edgeclassmap.get(eclass);
				if (transEC != null) {
					this.edges.add(eob);
				}
			}
			// EObject is a RecordDomain instance
			else if (this.eclass2recorddomain_map.get(eclass) != null) {
				// ok, do nothing
			}
			// EObject is an GraphClassInstance
			else {
				// Graphclass
				this.transformAttributeValues(eob, graph, graph);
			}

			// Examine further
			this.examineEObjects(eob.eContents(), graph);
		}
	}

	/**
	 * Transforms all created Vertexes wit Attributes and EReferences
	 * 
	 * @param graph
	 *            Graph of the model
	 * @param schem
	 *            Schema of the model
	 * */
	private void transformVertices(Graph graph) {
		for (EObject eob : this.objectsToVertexes.keySet()) {
			Vertex v = this.objectsToVertexes.get(eob);
			this.transformAttributeValues(eob, v, graph);
			this.transformReferenceValues(graph, eob, v);
			this.transformRecordDomainValues(graph, eob, v);
		}
	}

	/**
	 * Transforms links to former RecordDomain instances into Attribute values
	 * 
	 * @param graph
	 *            current graph
	 * @param eob
	 *            EObject to check links for
	 * @param graphElement
	 *            resulting grUML representation of eob
	 */
	private void transformRecordDomainValues(Graph graph, EObject eob,
			AttributedElement<?, ?> graphElement) {

		// Iterate over EReferences of the given Object
		for (EReference er : eob.eClass().getEAllReferences()) {
			// if there is a reference, that links to a former RecordDomain,
			// transform it
			if (this.recordDomainEReferences.contains(er)) {
				RecordDomain recd = this.eclass2recorddomain_map.get(er
						.getEReferenceType());

				de.uni_koblenz.jgralab.schema.RecordDomain realDomain = (de.uni_koblenz.jgralab.schema.RecordDomain) this.schem
						.getDomain(recd.get_qualifiedName());

				Object rec_inst = eob.eGet(er);

				Object rec = this.getRecVal(er.getEReferenceType(), graph,
						rec_inst, realDomain);
				graphElement.setAttribute(er.getName(), rec);
			}
		}
	}

	/**
	 * Transforms an Ecore EObject to a Record or a List of Records
	 * 
	 * @param eclassRecordDomain
	 *            EClass that is Ecore representation of RecordDomain
	 * @param graph
	 *            current graph
	 * @param record_instance
	 *            EObject representing record
	 * @param realDomain
	 *            RecordDomain to create an instance for
	 * @return Record or List of Records
	 */
	private Object getRecVal(EClass eclassRecordDomain, Graph graph,
			Object record_instance,
			de.uni_koblenz.jgralab.schema.RecordDomain realDomain) {

		HashMap<String, Object> fields = new HashMap<String, Object>();

		// Only one Record as Attribute
		if (record_instance instanceof EObject) {
			// Attributes of records
			for (EAttribute eat : eclassRecordDomain.getEAttributes()) {

				fields.put(eat.getName(), this.getAttributeValue(
						((EObject) record_instance).eGet(eat), graph, eat));
			}
			// Further record references of records
			for (EReference erefToAnotherRecD : eclassRecordDomain
					.getEReferences()) {

				RecordDomain recd2 = this.eclass2recorddomain_map
						.get(erefToAnotherRecD.getEReferenceType());

				de.uni_koblenz.jgralab.schema.RecordDomain realDomain2 = (de.uni_koblenz.jgralab.schema.RecordDomain) this.schem
						.getDomain(recd2.get_qualifiedName());

				fields.put(erefToAnotherRecD.getName(), this.getRecVal(
						erefToAnotherRecD.getEReferenceType(), graph,
						((EObject) record_instance).eGet(erefToAnotherRecD),
						realDomain2));

			}
			// Record rec = this.createRecord(realDomain, fields);
			Record rec = graph.createRecord(realDomain, fields);
			return rec;

		}
		// List of Records
		else {
			@SuppressWarnings("unchecked")
			List<EObject> rl = (List<EObject>) record_instance;
			PVector<Record> recl = ArrayPVector.empty();
			for (EObject reo : rl) {
				fields.clear();
				// Attributes of records
				for (EAttribute eat : eclassRecordDomain.getEAttributes()) {
					fields.put(eat.getName(),
							this.getAttributeValue((reo).eGet(eat), graph, eat));
				}
				// Further record references of records
				for (EReference ereef : eclassRecordDomain.getEReferences()) {
					fields.put(ereef.getName(), this.getRecVal(
							ereef.getEReferenceType(), graph,
							(reo).eGet(ereef), realDomain));
				}
				// Record rec = this.createRecord(realDomain, fields);
				Record rec = graph.createRecord(realDomain, fields);
				recl = recl.plus(rec);
			}
			return recl;
		}

	}

	// private Record createRecord(
	// de.uni_koblenz.jgralab.schema.RecordDomain domain,
	// Map<String, Object> fields) {
	// try {
	// Class<?> myRecord = Class.forName(domain.getSchema()
	// .getPackagePrefix() + "." + domain.getQualifiedName(),
	// false, SchemaClassManager.instance(domain.getSchema()
	// .getQualifiedName()));
	// Constructor<?> constr = myRecord.getConstructor(Map.class);
	//
	// Record newRec = (Record) constr.newInstance(fields);
	// return newRec;
	// } catch (Exception e) {
	// System.err.println("Can not create Record");
	// e.printStackTrace();
	// }
	// return null;
	// }

	/**
	 * Transforms the Values of EAttributes for the given Vertex
	 * 
	 * @param eob
	 *            EObject with the EAttributes
	 * @param v
	 *            AttributedElement (Vertex,Edge,GraphClassInstance)
	 * @param graph
	 *            Graph of model
	 * @param schem
	 *            Schema of model
	 * */
	private void transformAttributeValues(EObject eob,
			AttributedElement<?, ?> v, Graph graph) {
		for (EAttribute at : eob.eClass().getEAllAttributes()) {
			if (this.badEAttributes.contains(at)) {
				// EAttribute was not transformable
				continue;
			}
			// Get the attributes content
			Object atContent = eob.eGet(at);
			if ((atContent == null) && (at.getLowerBound() > 0)) {
				System.err.println("Warning: Attribute '"
						+ eob.eClass().getName() + " : " + at.getName()
						+ "' has a LowerBound > 0 but the object " + v
						+ " has no value for it.");
				continue;
			}

			v.setAttribute(at.getName(),
					this.getAttributeValue(atContent, graph, at));
		}
	}

	/**
	 * Transforms an Ecore Attribute Value into a grUML one
	 * 
	 * @param atContent
	 *            Ecore Object to transform
	 * @param schem
	 *            Schema of model
	 * @param graph
	 *            Graph of model
	 * */
	private Object getAttributeValue(Object atContent, Graph graph,
			EAttribute ecoreEat) {
		// if it is an EEnumLiteral, find the matching grUML one
		if (atContent instanceof EEnumLiteral) {

			EEnumLiteral el = ((EEnumLiteral) atContent);

			String literal = el.getLiteral().toUpperCase();
			char[] array = literal.toCharArray();
			boolean ok = true;
			for (int i = 0; i < array.length; i++) {
				if (!Character.isLetterOrDigit(array[i])) {
					ok = false;
				}
			}

			String literal_name;
			if (ok) {
				literal_name = literal;
			} else {
				literal_name = el.getName().toUpperCase();
			}
			// EnumDomain ed = this.enumdomains.get(el.getEEnum().getName());
			return literal_name;

		}
		// if it is a Date, create a Record
		else if (atContent instanceof Date) {
			Date ecoredate = (Date) atContent;
			de.uni_koblenz.jgralab.schema.RecordDomain realDomain = (de.uni_koblenz.jgralab.schema.RecordDomain) this.schem
					.getDomain(this.dateDomain.get_qualifiedName());
			Calendar cal = Calendar.getInstance();
			cal.setTime(ecoredate);
			HashMap<String, Object> fields = new HashMap<String, Object>();
			fields.put("day", cal.get(Calendar.DAY_OF_MONTH));
			fields.put("month", cal.get(Calendar.MONTH) + 1);
			fields.put("year", cal.get(Calendar.YEAR));
			fields.put("hour", cal.get(Calendar.HOUR_OF_DAY));
			fields.put("minute", cal.get(Calendar.MINUTE));
			fields.put("second", cal.get(Calendar.SECOND));
			// Object rec = this.createRecord(realDomain, fields);
			Object rec = graph.createRecord(realDomain, fields);
			return rec;
		}
		// if it is a BigInteger and they should become transformed to Long
		else if (this.configuration.isConvertBigNumbers()
				&& (atContent instanceof BigInteger)) {
			BigInteger bi = (BigInteger) atContent;
			BigInteger min = BigInteger.valueOf(Long.MIN_VALUE);
			BigInteger max = BigInteger.valueOf(Long.MAX_VALUE);
			long l = bi.longValue();
			if ((bi.compareTo(min) < 0) || (bi.compareTo(max) > 0)) {
				System.err.println("Warning: BigInteger " + bi
						+ " doesn't fit into the Long domain!");
			} else {
				System.out.println("Info: Successfully converted BigInteger "
						+ l + " to long.");
			}
			return l;
		}
		// if it is a BigDecimal and they should become transformed to Double
		else if (this.configuration.isConvertBigNumbers()
				&& (atContent instanceof BigDecimal)) {
			BigDecimal bd = (BigDecimal) atContent;
			double d = bd.doubleValue();
			System.err.println("Warning: Converted BigDecimal " + bd
					+ " to double " + d
					+ " which might have been caused some loss of precision.");
			return d;
		}
		// if it is a Character
		else if (atContent instanceof Character) {
			Character c = (Character) atContent;
			return c.toString();
		}
		// if its an List or Set
		else if (atContent instanceof EList) {
			PCollection<Object> contents;
			if (ecoreEat.isUnique() && !ecoreEat.isOrdered()) {
				contents = ArrayPSet.empty();
			} else {
				contents = ArrayPVector.empty();
			}
			for (Object a : (EList<?>) atContent) {
				contents = contents.plus(this.getAttributeValue(a, graph,
						ecoreEat));
			}
			return contents;
		}
		// if it is int, long, double, String, just return the value, it is not
		// necessary to transform
		else {
			return atContent;
		}
	}

	/**
	 * Transform that EReferenceValues, that don't fit to the EdgeClasses out of
	 * EClasses
	 * 
	 * @param graph
	 *            Graph of model
	 * @param eob
	 *            EObject to transform the EReferences for
	 * @param v
	 *            the Vertex created for the EObject
	 * @param schem
	 *            Schema of model
	 * */
	private void transformReferenceValues(Graph graph, EObject eob, Vertex v) {
		// Iterate over all EReferences
		for (EReference eref : eob.eClass().getEAllReferences()) {
			// Take the links to the EReference
			Object links = eob.eGet(eref);
			// If there is only one link, create an edge for it
			if (links instanceof EObject) {
				this.createEdge(v, eob, eref, (EObject) links, graph);
			}
			// If there are more links, create an edge for every link
			else if (links instanceof EList) {
				for (Object o : (EList<?>) links) {
					if (o instanceof EObject) {
						this.createEdge(v, eob, eref, (EObject) o, graph);
					} else {
						System.out.println("That should never appear. What is "
								+ o + "?");
					}
				}
			}
		}
	}

	/**
	 * Creates an edge between v and link
	 * 
	 * @param v
	 *            Vertex that is created for eob
	 * @param eob
	 *            EObject that contains eref
	 * @param eref
	 *            EReference where link is an instance of
	 * @param link
	 *            EObject that is referenced by eob
	 * @param graph
	 *            Graph of model
	 * @param schem
	 *            Schema of model
	 * */
	private void createEdge(Vertex v, EObject eob, EReference eref,
			EObject link, Graph graph) {
		// Test if the link is alread transformed
		if (this.transformedLinks.get(eob) != null) {
			if (this.transformedLinks.get(eob).get(eref) != null) {
				if (this.transformedLinks.get(eob).get(eref).contains(link)) {
					// Link already transformed
					return;
				}
			}
		}

		// Take the other end of the edge from map
		Vertex oneend = this.objectsToVertexes.get(link);
		if (oneend == null) {
			// link goes to an EdgeClass or the GraphClass
			// so, don't transform it
			return;
		}

		// Get the EdgeClass
		EdgeClass transEC = this.ereference2edgeclass_map.get(eref);
		de.uni_koblenz.jgralab.schema.EdgeClass edgeclass = (de.uni_koblenz.jgralab.schema.EdgeClass) this.schem
				.getAttributedElementClass(transEC.get_qualifiedName());
		// Create the Edge in the right direction
		if (this.definingDirectionEReferences.contains(eref)) {
			graph.createEdge(edgeclass, v, oneend);
		} else {
			graph.createEdge(edgeclass, oneend, v);
		}

		// Add the links to the transformed links
		this.addToTransformedLinks(eob, eref, link);
		if (eref.getEOpposite() != null) {
			this.addToTransformedLinks(link, eref.getEOpposite(), eob);
		}
	}

	/**
	 * Adds an link for an EReference and an EObject to the transformedLinks Map
	 * 
	 * @param start
	 *            EObject that contains the link
	 * @param ref
	 *            EReference that link is an instance of
	 * @param end
	 *            EObject that is referenced by link
	 * */
	private void addToTransformedLinks(EObject start, EReference ref,
			EObject end) {
		if (this.transformedLinks.get(start) != null) {
			if (this.transformedLinks.get(start).get(ref) != null) {
				this.transformedLinks.get(start).get(ref).add(end);
			} else {
				ArrayList<EObject> transformedTargets = new ArrayList<EObject>();
				transformedTargets.add(end);
				this.transformedLinks.get(start).put(ref, transformedTargets);
			}
		} else {
			ArrayList<EObject> transformedTargets = new ArrayList<EObject>();
			transformedTargets.add(end);
			HashMap<EReference, ArrayList<EObject>> ref2targets = new HashMap<EReference, ArrayList<EObject>>();
			ref2targets.put(ref, transformedTargets);
			this.transformedLinks.put(start, ref2targets);
		}
	}

	/**
	 * Transforms instances of EClasses that have become EdgeClasses
	 * 
	 * @param objectlist
	 *            Complete Model
	 * @param graph
	 *            Graph of model
	 * @param schema
	 *            Schema of model
	 * */
	private void transformEdgesFromEClasses(EList<EObject> objectlist,
			Graph graph) {

		// Iterate over edge instances
		for (EObject eob : this.edges) {
			EClass eclass = eob.eClass();

			// Transform EReferences
			ArrayList<EObject> edgeEnds = new ArrayList<EObject>();
			ArrayList<EReference> actualERefs = new ArrayList<EReference>();

			// Iterate over all EReferences from EClass
			// it may be more than 2 because of inheritance
			for (EReference ref : eclass.getEAllReferences()) {
				if (this.badEReferences.contains(ref)) {
					continue;
				}
				if (eob.eGet(ref) != null) {
					Object links = eob.eGet(ref);
					edgeEnds.add((EObject) links);
					actualERefs.add(ref);
				}
			}

			// Find the direction
			EObject start = null;
			EObject end = null;

			// If there is only one edge reachable through the EReferences
			// search for the other end
			if (edgeEnds.size() == 1) {

				ArrayList<EObject> linkingEnds = new ArrayList<EObject>();
				this.getLinksToEObject(objectlist, eob, linkingEnds);

				if (linkingEnds.size() == 1) {
					edgeEnds.add(linkingEnds.get(0));
				} else if (linkingEnds.size() == 2) {
					if (linkingEnds.get(0) != start) {
						edgeEnds.add(linkingEnds.get(0));
					} else {
						edgeEnds.add(linkingEnds.get(1));
					}
				}
			}

			// Determine what end is start and what is end
			if (this.ereferencesEdgeClass2start.contains(actualERefs.get(0))) {
				start = edgeEnds.get(0);
				end = edgeEnds.get(1);
			} else {
				end = edgeEnds.get(0);
				start = edgeEnds.get(1);
			}

			// Get the EdgeClass
			EdgeClass transEC = this.edgeclassmap.get(eclass);
			de.uni_koblenz.jgralab.schema.EdgeClass edgeclass = (de.uni_koblenz.jgralab.schema.EdgeClass) this.schem
					.getAttributedElementClass(transEC.get_qualifiedName());

			// Create the Edge
			Vertex alpha = this.objectsToVertexes.get(start);
			Vertex omega = this.objectsToVertexes.get(end);
			if (alpha == null || omega == null) {
				System.err.println("Hlp " + start + " " + end);
			}
			Edge edge = graph.createEdge(edgeclass, alpha, omega);

			// Transform Attributes
			this.transformAttributeValues(eob, edge, graph);
			// Transform RecorDomains
			this.transformRecordDomainValues(graph, eob, edge);
		}
	}

	/**
	 * Finds all links that refer to a given EObject in a field
	 * 
	 * @param field
	 *            list to search in
	 * @param eob
	 *            EObject to search links to
	 * @param startEObjects
	 *            results
	 * */
	private void getLinksToEObject(EList<EObject> field, EObject eob,
			ArrayList<EObject> startEObjects) {
		for (EObject o : field) {
			for (EReference er : o.eClass().getEAllReferences()) {
				if (this.badEReferences.contains(er)) {
					continue;
				}
				if (o.eGet(er) != null) {
					if (o.eGet(er) instanceof EList) {
						for (Object i : (EList<?>) o.eGet(er)) {
							if (i == eob) {
								startEObjects.add((EObject) i);
							}
						}
					} else {
						if (o == eob) {
							startEObjects.add(o);
						}
					}
				}
			}
			this.getLinksToEObject(o.eContents(), eob, startEObjects);
		}
	}

	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------
	// -------Loader for ecore models and metamodels----------------------------
	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * Loader for .ecore Files Loads the Ecore Model out of the .ecore File, the
	 * resulting root Element is an EPackage
	 * 
	 * @param path
	 *            Path to the .ecore file that should become loaded
	 * @return Resource with the Ecore Metamodel
	 * */
	public static Resource loadMetaModelFromEcoreFile(String path) {
		try {
			// URI from the .ecore File
			URI fileURI = URI.createFileURI(path);

			ResourceSet resSet = new ResourceSetImpl();

			// Register the Metamodel to verify the namespace
			resSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
					.put("xmi", new XMIResourceFactoryImpl());
			resSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
					.put("ecore", new EcoreResourceFactoryImpl());
			resSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
					.put("javaxmi", new XMIResourceFactoryImpl());

			Resource xmiResource = resSet.getResource(fileURI, true);

			for (EObject o : xmiResource.getContents()) {
				EPackage p = (EPackage) o;
				registerEPackagesAndExtensions(p, resSet);
			}

			// Return it as result
			return xmiResource;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Loader for model files with .xmi ending Loads an ecore model that
	 * corresponds to the transformed ecore metamodel and returns it as EList of
	 * EObjects.
	 * 
	 * @param paths
	 *            Array with uris of all model files belonging to a model
	 * @return model as EList of EObjects
	 * */
	public EList<EObject> loadModelFromXMIFile(String[] paths) {
		// Load the model
		EList<EObject> list = new BasicEList<EObject>();

		// Get the ResourceSet
		ResourceSet resSet = this.metamodelResource.getResourceSet();

		ArrayList<Resource> modelResources = new ArrayList<Resource>();
		for (String path : paths) {
			// The URI to load
			URI uri = URI.createFileURI(path);

			// Load the resource
			Resource res = resSet.getResource(uri, true);
			list.addAll(res.getContents());
			modelResources.add(res);
		}

		// Reset the URI for more than 1 model file
		if (modelResources.size() > 1) {
			int index = 0;
			for (int i = 0; i < modelResources.get(0).getURI().segments().length; i++) {
				String segment = modelResources.get(0).getURI().segments()[i];
				boolean equal = true;
				for (Resource r : modelResources) {
					if (!r.getURI().segments()[i].equals(segment)) {
						equal = false;
					}
				}
				if (equal) {
					index++;
					;
				} else {
					break;
				}
			}
			for (Resource r : modelResources) {
				String newURIstring = "";
				for (int i = index; i < r.getURI().segments().length; i++) {
					newURIstring = newURIstring + "."
							+ r.getURI().segments()[i];
				}
				r.setURI(URI.createURI(newURIstring.substring(1)));
			}
		}

		return list;
	}

	/**
	 * Registers all {@link EPackage} objects of the metamodel to the
	 * ResourceSet of the model. For every EPackage there is an extension that
	 * must although become registered with an {@link XMIResourceFactory}
	 * 
	 * @param pack
	 *            root EPackage to register
	 * @param resSet
	 *            ResourceSet to Register all EPackage namespaces
	 * */
	private static void registerEPackagesAndExtensions(EPackage pack,
			ResourceSet resSet) {
		EPackage.Registry.INSTANCE.put(pack.getNsURI(), pack);
		if (pack.getName() != null) {
			resSet.getResourceFactoryRegistry()
					.getExtensionToFactoryMap()
					.put(pack.getName().toLowerCase(),
							new XMIResourceFactoryImpl());
		}
		for (EPackage childpack : pack.getESubpackages()) {
			registerEPackagesAndExtensions(childpack, resSet);
		}
	}

	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------
	// ---------Copied from Rsa2Tg with minor changes---------------------------
	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * Creates {@link EdgeClass} names for all EdgeClass objects, which do have
	 * an empty String or a String, which ends with a '.'.
	 */
	private void createEdgeClassNames() {
		for (EdgeClass ec : this.schemagraph.getEdgeClassVertices()) {

			boolean errorGenToRolename = false;
			boolean errorGenFromRolename = false;

			String name = ec.get_qualifiedName().trim();

			if (name.equals("Edge") || name.equals("Vertex")
					|| name.equals("Graph")) {
				name = name + "_";
			}
			if (!name.equals("") && !name.endsWith(".")) {
				continue;
			}

			IncidenceClass to = (IncidenceClass) ec.getFirstGoesToIncidence()
					.getThat();
			IncidenceClass from = (IncidenceClass) ec
					.getFirstComesFromIncidence().getThat();

			// invent an edgeclass name
			String ecName = null;

			String toRole = to.get_roleName();
			if ((toRole == null) || toRole.equals("")) {
				toRole = ((VertexClass) to.getFirstEndsAtIncidence().getThat())
						.get_qualifiedName();
				int p = toRole.lastIndexOf('.');
				if (p >= 0) {
					toRole = toRole.substring(p + 1);
				}
				if (this.configuration.getGenerateRoleNames()) {

					VertexClass fromVC = (VertexClass) from
							.getFirstEndsAtIncidence().getThat();
					VertexClass toVC = (VertexClass) to
							.getFirstEndsAtIncidence().getThat();

					String simpleNameOfToVC = this
							.lowercaseFirst(toVC.get_qualifiedName()
									.substring(
											toVC.get_qualifiedName()
													.lastIndexOf(".") + 1,
											toVC.get_qualifiedName().length()));

					// Collect Inheritance Hierarchy
					HashSet<VertexClass> vcset = new HashSet<VertexClass>();
					this.fillSetWithFamliy(fromVC, vcset);
					ArrayList<VertexClass> vcList = new ArrayList<VertexClass>();
					vcList.addAll(vcset);
					boolean duplicated = this.isPossibleRolename(
							simpleNameOfToVC, vcList);
					if (duplicated) {
						errorGenToRolename = true;
					} else {
						to.set_roleName(simpleNameOfToVC);
					}

				}
			} else {
				toRole = Character.toUpperCase(toRole.charAt(0))
						+ toRole.substring(1);
			}

			// There must be a 'to' role name, which is different than null and
			// not empty.
			if ((toRole == null) || (toRole.length() <= 0)) {
				throw new RuntimeException(
						"There is no role name 'to' for the edge '" + name
								+ "' defined.");
			}

			if ((to.get_aggregation() != AggregationKind.NONE)
					|| (from.get_aggregation() != AggregationKind.NONE)) {
				if (from.get_aggregation() != AggregationKind.NONE) {
					// -->Hier tausch ich mal Contains und IsPartOf
					ecName = "IsPartOf" + toRole;
				} else {
					ecName = "Contains" + toRole;
				}
			} else {
				ecName = "LinksTo" + toRole;
			}

			if (/* isUseFromRole() */from.get_roleName() != null) {
				String fromRole = from.get_roleName();
				if ((fromRole == null) || fromRole.equals("")) {
					fromRole = ((VertexClass) from.getFirstEndsAtIncidence()
							.getThat()).get_qualifiedName();
					int p = fromRole.lastIndexOf('.');
					if (p >= 0) {
						fromRole = fromRole.substring(p + 1);
					}

					if (this.configuration.getGenerateRoleNames()) {

						VertexClass toVC = (VertexClass) to
								.getFirstEndsAtIncidence().getThat();
						VertexClass fromVC = (VertexClass) from
								.getFirstEndsAtIncidence().getThat();

						// Role Name should be simple name of class if possible
						String simpleFromVCName = this.lowercaseFirst(fromVC
								.get_qualifiedName().substring(
										fromVC.get_qualifiedName().lastIndexOf(
												".") + 1,
										fromVC.get_qualifiedName().length()));

						// Collect Inheritance Hierarchy
						HashSet<VertexClass> vcset = new HashSet<VertexClass>();
						this.fillSetWithFamliy(toVC, vcset);
						ArrayList<VertexClass> vcList = new ArrayList<VertexClass>();
						vcList.addAll(vcset);

						boolean duplicated = this.isPossibleRolename(
								simpleFromVCName, vcList);
						if (duplicated) {
							errorGenFromRolename = true;
						} else {

							from.set_roleName(simpleFromVCName);
						}
					}
				} else {
					fromRole = Character.toUpperCase(fromRole.charAt(0))
							+ fromRole.substring(1);
				}

				// There must be a 'from' role name, which is different than
				// null and not empty.
				if ((fromRole == null) || (fromRole.length() <= 0)) {
					throw new RuntimeException(
							"There is no role name of 'from' for the edge '"
									+ name + "' defined.");
				}
				name += fromRole;
			}

			assert (ecName != null) && (ecName.length() > 0);

			// Einschub - find ich nötig, oder ich stelle mich hier einfach nur
			// dumm an
			String packagePrefixName = ((Package) ec
					.getFirstContainsGraphElementClassIncidence().getThat())
					.get_qualifiedName();
			if (!packagePrefixName.equals("")) {
				packagePrefixName = packagePrefixName + ".";
			}

			for (EdgeClass ec2 : this.schemagraph.getEdgeClassVertices()) {
				if (ec2.get_qualifiedName().equals(
						(packagePrefixName + name + ecName))) {
					ecName += "2";
				}
			}

			while (this.schemaGraphContainsEdgeClassName(packagePrefixName
					+ name + ecName)) {
				ecName += "I";
			}

			// Einschub ende

			ec.set_qualifiedName(packagePrefixName + name + ecName);

			if (errorGenToRolename) {
				System.err
						.println("WARNING: Generation of TO role name for EdgeClass "
								+ ec.get_qualifiedName() + " failed.");
			}
			if (errorGenFromRolename) {
				System.err
						.println("WARNING: Generation of FROM role name for EdgeClass "
								+ ec.get_qualifiedName() + " failed.");
			}
		}
	}

	private String lowercaseFirst(String str) {
		if (str.length() < 2) {
			return str.toLowerCase();
		}
		return str.substring(0, 1).toLowerCase() + str.substring(1);
	}

	private void fillSetWithFamliy(VertexClass vc, Set<VertexClass> list) {
		// Put element in
		list.add(vc);

		// Put all parents in
		Iterator<? extends VertexClass> it = vc.get_superclass().iterator();
		while (it.hasNext()) {
			VertexClass parent = it.next();
			this.fillSetWithParents(parent, list);
		}

		// Put all childs in
		it = vc.get_subclass().iterator();
		while (it.hasNext()) {
			VertexClass child = it.next();
			this.fillSetWithChildren(child, list);
		}
	}

	private void fillSetWithParents(VertexClass vc, Set<VertexClass> list) {
		// Put element in
		list.add(vc);

		// Put all parents in
		Iterator<? extends VertexClass> it = vc.get_superclass().iterator();
		while (it.hasNext()) {
			VertexClass parent = it.next();
			this.fillSetWithParents(parent, list);
		}
	}

	private void fillSetWithChildren(VertexClass vc, Set<VertexClass> list) {
		// Put element in
		list.add(vc);

		// Put all childs in
		Iterator<? extends VertexClass> it = vc.get_subclass().iterator();
		while (it.hasNext()) {
			VertexClass child = it.next();
			this.fillSetWithChildren(child, list);
		}
	}

	private boolean isPossibleRolename(String rolename, List<VertexClass> vcList) {
		boolean duplicated = false;
		for (VertexClass fromVC : vcList) {

			for (EndsAt ea : fromVC.getEndsAtIncidences()) {
				IncidenceClass ic = (IncidenceClass) ea.getAlpha();
				if (ic.getFirstGoesToIncidence() != null) {
					String rn = (((IncidenceClass) ((EdgeClass) ic
							.getFirstGoesToIncidence().getAlpha())
							.getFirstComesFromIncidence().getOmega()))
							.get_roleName();
					if (rn.equals(rolename)) {
						duplicated = true;
					}
				} else if (ic.getFirstComesFromIncidence() != null) {
					String rn = (((IncidenceClass) ((EdgeClass) ic
							.getFirstComesFromIncidence().getAlpha())
							.getFirstGoesToIncidence().getOmega()))
							.get_roleName();
					if (rn.equals(rolename)) {
						duplicated = true;
					}
				}
			}
		}
		return duplicated;
		// tt

	}

	/**
	 * Checks if the SchemaGraph already contains an EdgeClass with the
	 * specified name
	 * 
	 * @param qalifiedName
	 *            name to search for
	 * @return true if the SchemaGraph contains an EdgeClass with the qualified
	 *         name false if not
	 * */
	private boolean schemaGraphContainsEdgeClassName(String qualifiedName) {
		for (EdgeClass ec : this.schemagraph.getEdgeClassVertices()) {
			if (ec.get_qualifiedName().equals(qualifiedName)) {
				return true;
			}
		}
		return false;
	}
}
