package de.uni_koblenz.jgralab.utilities.ecore2tg;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.BooleanDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.CollectionDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.DoubleDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.domains.IntegerDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.ListDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.LongDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.MapDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.RecordDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.SetDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.StringDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.AggregationKind;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Comment;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsGraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.IncidenceClass;
import de.uni_koblenz.jgralab.grumlschema.structure.NamedElement;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Tg2EcoreConfiguration.EdgeDirection;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;

public class Tg2Ecore {

	// #########################################################
	// ##### Command line tool #################################
	// #########################################################

	private static final String OPTION_FILENAME_SCHEMA = "i";
	private static final String OPTION_FILENAME_METAMODEL = "o";
	private static final String OPTION_FILENAME_CONFIG = "c";
	private static final String OPTION_FILENAME_MODEL = "m";

	private static final String OPTION_BACK2ECORE = "b";
	private static final String OPTION_ONEROLE2UNI = "u";
	private static final String OPTION_TRANSFORMGRAPHCLASS = "g";
	private static final String OPTION_MAKEGRAPHCLASS2ROOT = "r";
	private static final String OPTION_ROOTPACKAGENAME = "rn";
	private static final String OPTION_NSPREFIX = "p";
	private static final String OPTION_NSURI = "n";
	private static final String OPTION_DEFINEROLENAME = "d";
	private static final String OPTION_NOECLASSFOREDGECLASSES = "nec";

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
		String toolString = "java " + Tg2Ecore.class.getName();
		String versionString = JGraLab.getInfo(false);

		OptionHandler oh = new OptionHandler(toolString, versionString);

		// Declare Options

		// OPTION_FILENAME_SCHEMA = "i";
		Option input_tg = new Option(OPTION_FILENAME_SCHEMA, "input", true,
				"(required): tg-file with Schema and optional graph to transform ");
		input_tg.setRequired(true);
		input_tg.setArgName("filename");
		oh.addOption(input_tg);

		// OPTION_FILENAME_METAMODEL = "o";
		Option output_metamodel = new Option(OPTION_FILENAME_METAMODEL,
				"output", true,
				"(required): filename for the resulting Ecore Metamodel, "
						+ " must end with .ecore");
		output_metamodel.setRequired(true);
		output_metamodel.setArgName("filename");
		oh.addOption(output_metamodel);

		// OPTION_FILENAME_MODEL = "m";
		Option modelfile = new Option(OPTION_FILENAME_MODEL, "modelfilename",
				true, "(optional): filename for the resulting model,"
						+ " if the tg-file contains a graph. ");
		modelfile.setRequired(false);
		modelfile.setArgName("filename");
		oh.addOption(modelfile);

		// OPTION_FILENAME_CONFIG = "c";
		Option config = new Option(OPTION_FILENAME_CONFIG, "configuration",
				true,
				"(optional): loads configurations from the given filename. ");
		config.setRequired(false);
		config.setArgName("filename");
		oh.addOption(config);

		// OPTION_BACK2ECORE = "b";
		Option back2ecore = new Option(
				OPTION_BACK2ECORE,
				"back2ecoreflag",
				false,
				"(optional): the transformation should check comments set from Ecore2Tg"
						+ " and transform the schema back to the original metamodel.");
		back2ecore.setRequired(false);
		oh.addOption(back2ecore);

		// OPTION_ONEROLE2UNI = "u";
		Option onerole = new Option(OPTION_ONEROLE2UNI, "onerole2uniflag",
				false,
				"(optional): edges with only one rolename become unidirectional EReferences.");
		onerole.setRequired(false);
		oh.addOption(onerole);

		// OPTION_TRANSFORMGRAPHCLASS = "g";
		Option transGC = new Option(
				OPTION_TRANSFORMGRAPHCLASS,
				"transformGraphclass",
				false,
				"(optional): the GraphClass becomes an EClass, "
						+ "if not set the GraphClass doesn't become transformed.");
		transGC.setRequired(false);
		oh.addOption(transGC);

		// OPTION_MAKEGRAPHCLASS2ROOT = "r";
		Option gcroot = new Option(
				OPTION_MAKEGRAPHCLASS2ROOT,
				"graphclass_as_root",
				false,
				"(optional): the graphClass becomes the root element - "
						+ "only possible, if the transformGraphclass option is set.");
		gcroot.setRequired(false);
		oh.addOption(gcroot);

		Option noec = new Option(
				OPTION_NOECLASSFOREDGECLASSES,
				"no_eclasses_for_edgeclasses",
				false,
				"(optional): EdgeClasses are not transformed to EClasses. "
						+ "Attributes and inheritance relationships are lost. "
						+ "only top level EdgeClasses exist as EReferences after the transformation.");
		noec.setRequired(false);
		oh.addOption(noec);

		// OPTION_ROOTPACKAGENAME = "rn";
		Option rpn = new Option(OPTION_ROOTPACKAGENAME, "rootpackagename",
				true, "(optional): name of the rootpackage");
		rpn.setRequired(false);
		rpn.setArgName("rootpackagename");
		oh.addOption(rpn);

		// OPTION_NSPREFIX = "p";
		Option nspr = new Option(OPTION_NSPREFIX, "nsPrefix", true,
				"(optional): nsPrefix of rootpackage");
		nspr.setRequired(false);
		nspr.setArgName("nsPrefix");
		oh.addOption(nspr);

		// OPTION_NSURI = "n";
		Option nsuri = new Option(OPTION_NSURI, "nsURI", true,
				"(optional): nsURI of rootpackage");
		nsuri.setRequired(false);
		nsuri.setArgName("nsURI");
		oh.addOption(nsuri);

		// OPTION_DEFINEROLENAME = "d";
		Option drn = new Option(
				OPTION_DEFINEROLENAME,
				"defineRolename",
				true,
				"(optional): define additional rolenames for EdgeClass. direction must be \"from\" or \"to\"");
		drn.setRequired(false);
		drn.setArgs(3);
		drn.setValueSeparator(' ');
		drn.setArgName("edgeclassname");
		drn.setArgName("direction");
		drn.setArgName("rolename");
		oh.addOption(drn);

		return oh.parse(args);
	}

	public static void main(String[] args) {
		System.out.println("Tg to Ecore");
		System.out.println("===========");
		JGraLab.setLogLevel(Level.OFF);

		// Retrieving all command line options
		CommandLine cli = processCommandLineOptions(args);

		assert cli != null : "No CommandLine object has been generated!";

		// Get the schema filename
		String schemafilename = cli.getOptionValue(OPTION_FILENAME_SCHEMA);
		Schema schema = null;
		try {
			schema = GraphIO.loadSchemaFromFile(schemafilename);
		} catch (GraphIOException e) {
			System.out.println("Schema file " + schemafilename
					+ " does not exist.");
			e.printStackTrace();
		}
		schema.compile(CodeGeneratorConfiguration.MINIMAL);
		Schema2SchemaGraph s2sg = new Schema2SchemaGraph();
		SchemaGraph sg = s2sg.convert2SchemaGraph(schema);

		// Instanciate Tg2Ecore
		Tg2EcoreConfiguration config = new Tg2EcoreConfiguration();

		String cfilename = cli.getOptionValue(OPTION_FILENAME_CONFIG);
		if (cfilename != null) {
			config = Tg2EcoreConfiguration.loadConfigurationFromFile(cfilename);
		}
		Tg2Ecore tg2ecore = new Tg2Ecore(sg, config);

		config.setOption_backToEcore(cli.hasOption(OPTION_BACK2ECORE));
		config.setOption_oneroleToUni(cli.hasOption(OPTION_ONEROLE2UNI));
		config.setOption_transformGraphClass(cli
				.hasOption(OPTION_TRANSFORMGRAPHCLASS));
		config.setOption_makeGraphClassToRootElement(cli
				.hasOption(OPTION_MAKEGRAPHCLASS2ROOT));
		config.setOption_noEClassForEdgeClasses(cli
				.hasOption(OPTION_NOECLASSFOREDGECLASSES));

		String rn = cli.getOptionValue(OPTION_ROOTPACKAGENAME);
		if (rn != null) {
			config.setOption_rootpackageName(rn);
		}
		String nsPrefix = cli.getOptionValue(OPTION_NSPREFIX);
		if (nsPrefix != null) {
			config.setOption_nsPrefix(nsPrefix);
		}
		String nsURI = cli.getOptionValue(OPTION_NSURI);
		if (nsURI != null) {
			config.setOption_nsURI(nsURI);
		}

		String[] rolenames = cli.getOptionValues(OPTION_DEFINEROLENAME);
		if (rolenames != null) {
			for (String en : rolenames) {
				String[] ar = en.split(" ");
				assert ar.length == 3;
				Tg2EcoreConfiguration.EdgeDirection dir = Tg2EcoreConfiguration.EdgeDirection.From;
				if (ar[1].equalsIgnoreCase("To")) {
					dir = EdgeDirection.To;
				}
				config.addOption_definerolenames(ar[0], dir, ar[2]);
			}
		}

		// Start the transformation:
		tg2ecore.transform();

		// Save the result;
		String path = cli.getOptionValue(OPTION_FILENAME_METAMODEL);
		tg2ecore.saveEcoreMetamodel(tg2ecore.getTransformedMetamodel(), path);

		// test if a graph is there
		if (cli.hasOption(OPTION_FILENAME_MODEL)) {
			Graph g;
			try {
				g = GraphIO.loadGraphFromFile(schemafilename, null);
			} catch (GraphIOException e) {
				System.out.println("There is no graph in " + schemafilename
						+ " but a model filename is given.");
				e.printStackTrace();
				return;
			}

			ArrayList<EObject> model = tg2ecore.transformGraphToEcoreModel(g);

			String modelfilename = cli.getOptionValue(OPTION_FILENAME_MODEL);
			tg2ecore.saveEcoreModel(model, modelfilename);
		}
	}

	private Tg2EcoreConfiguration config;

	public Tg2EcoreConfiguration getConfiguration() {
		return this.config;
	}

	// #########################################################
	// ##### Transformation Basic ##############################
	// #########################################################

	/**
	 * SchemaGraph to transform.
	 */
	private final SchemaGraph schemagraph;

	/**
	 * GraphClass of the Schema that should become transformed.
	 */
	private EClass graphClassOfSchema;

	/**
	 * Resulting Ecore Metamodel represented by its rootpackage.
	 */
	private EPackage rootpackage;

	/**
	 * Method to get the result of the Transformation.
	 * 
	 * @return the resulting metamodel as EPackage instance
	 */
	public EPackage getTransformedMetamodel() {
		return this.rootpackage;
	}

	// #########################################################
	// ##### Transformation Memory #############################
	// #########################################################

	/**
	 * Maps all EdgeClasses that become transformed to EClasses to that result.
	 * Necessary to save the empty EdgeClass first.
	 */
	private HashMap<EdgeClass, EClass> edgeclasses2eclasses;

	/**
	 * Maps all EdgeClasses that become transformed to EdgeClasses to the
	 * resulting ConceptualEdgeClasses.
	 */
	private HashMap<EdgeClass, ConceptualEdgeClass> edgeclasses2concepts;

	/**
	 * Maps all EdgeClasses that become EReferences to that results. The size of
	 * that array is 2. At position 0 the "from-EReference" is saved and at
	 * position 1 the "to-EReference".
	 */
	private HashMap<EdgeClass, EReference[]> edgeclasses2ereferen;

	/**
	 * Maps grUML Packages to the resulting Ecore EPackages.
	 */
	private HashMap<Package, EPackage> packagemap;

	/**
	 * Maps grUML VertexClasses to the resulting EClasses.
	 */
	private HashMap<VertexClass, EClass> vertexclass2eclass;

	/**
	 * Maps grUML EnumDomains to the resulting Ecore EEnums
	 */
	private HashMap<EnumDomain, EEnum> enums;

	/**
	 * Maps the grUML RecordDomains to the resulting EClassifier - that is
	 * "EDate" if the record was instantiated by Ecore2Tg to capsulate EDate and
	 * an EClass, that has the records parts as EAttributes otherwise.
	 */
	private HashMap<RecordDomain, EClassifier> records;

	/**
	 * Remembers which EClasses are references by a containment EReference -
	 * necessary if the Option {@link}makeGraphClassToRootElement is set.
	 */
	private HashSet<EClass> alreadyContainedEClasses;

	/**
	 * Attributes that can not become transformed
	 */
	private HashSet<Attribute> badAttributes;

	/**
	 * Additional containment EReferences from comments
	 */
	private HashSet<EReference> additionalReferencesForEdges;

	/**
	 * Structural Class to describe a conceptual EdgeClass as aggregation of
	 * four EReferences.
	 */
	private class ConceptualEdgeClass {

		EReference erefFromStartToEC;
		EReference erefFromECToStart;
		EReference erefFromTargetToEC;
		EReference erefFromECToTarget;

		ConceptualEdgeClass(EReference ese, EReference ees, EReference ete,
				EReference eet) {
			this.erefFromStartToEC = ese;
			this.erefFromECToStart = ees;
			this.erefFromTargetToEC = ete;
			this.erefFromECToTarget = eet;
		}
	}

	// #########################################################
	// ##### Start of schema transformation ####################
	// #########################################################

	/**
	 * Initializes a new Tg2Ecore instance. After invoking, options can be set.
	 * 
	 * @param s
	 *            SchemaGraph to transform
	 * */
	public Tg2Ecore(SchemaGraph s) {
		this.schemagraph = s;
		this.config = new Tg2EcoreConfiguration();
	}

	public Tg2Ecore(SchemaGraph s, Tg2EcoreConfiguration c) {
		this.schemagraph = s;
		this.config = c;
	}

	/**
	 * Starts the transformation of the SchemaGraph to the Ecore rootpackage
	 * with the set options. After invoking transform, the rootpackage property
	 * is set.
	 * */
	public void transform() {

		System.out.println("Transformation of Schema started ...");
		long starttime = System.currentTimeMillis();

		// Initializing memory for transformation
		this.initializeMembers();

		// Get the default package out of the SchemaGraph
		de.uni_koblenz.jgralab.grumlschema.structure.Package defaultpackage = null;
		Vertex tempvertex = this.schemagraph.getFirstContainsDefaultPackage()
				.getOmega();
		if (tempvertex instanceof de.uni_koblenz.jgralab.grumlschema.structure.Package) {
			defaultpackage = this.schemagraph.getFirstContainsDefaultPackage()
					.getOmega();
		} else {
			System.out.println("No default package in SchemaGraph - Error.");
		}

		// Start the Transformation of all Packages, initialize objects for
		// later VertexClass and EdgeClass Transformation
		this.rootpackage = this.transformPackagesWithContent(defaultpackage);

		// Set name, nxPrefix and nsURI
		this.determineEPackageProperties();

		// Transform all elements of SchemaGraph
		this.transformVertexClasses();
		this.transformEdgeClassesToEReferences();
		this.transformEdgeClassesToEClasses();
		this.transformGraphClass(this.schemagraph.getFirstGraphClass());

		// If it is a back transformation, the defaultPackage was wrapped around
		// the old rootpackage during Ecore2Tg. Because of that the new
		// rootpackage is deleted and the old rootpackage takes its place.
		if (this.config.isOption_backToEcore()
				&& (this.rootpackage.getESubpackages().size() == 1)
				&& (this.rootpackage.getEClassifiers().size() == 0)) {
			EPackage oldEpack = this.rootpackage;
			this.rootpackage = this.rootpackage.getESubpackages().get(0);
			oldEpack.getESubpackages().remove(this.rootpackage);
			defaultpackage = defaultpackage.get_subpackages().iterator().next();
		}

		// Generate nsPrefix and nsURI for all SubPackages
		this.generatePackageNS(defaultpackage);

		// Flag to indicate the Ecore metamodel as former TG-Schema
		EAnnotation ean_tg2ecore = EcoreFactory.eINSTANCE.createEAnnotation();
		ean_tg2ecore.setSource(EAnnotationKeys.SOURCE_STRING);
		ean_tg2ecore.getDetails().put(EAnnotationKeys.KEY_COMES_FROM_TG2ECORE,
				"true");
		this.rootpackage.getEAnnotations().add(ean_tg2ecore);

		// Save configuration as EAnnotation
		this.rootpackage.getEAnnotations().add(this.getOptionsAsEAnnotation());

		long endtime = System.currentTimeMillis();
		System.out.println("Transformation of Schema finished. It took "
				+ (endtime - starttime) + " milliseconds.");
	}

	/**
	 * Initialize the transformation memory before transformation
	 */
	private void initializeMembers() {
		this.edgeclasses2eclasses = new HashMap<EdgeClass, EClass>();
		this.edgeclasses2concepts = new HashMap<EdgeClass, ConceptualEdgeClass>();
		this.edgeclasses2ereferen = new HashMap<EdgeClass, EReference[]>();
		this.packagemap = new HashMap<Package, EPackage>();
		this.vertexclass2eclass = new HashMap<VertexClass, EClass>();
		this.enums = new HashMap<EnumDomain, EEnum>();
		this.records = new HashMap<RecordDomain, EClassifier>();
		this.alreadyContainedEClasses = new HashSet<EClass>();
		this.badAttributes = new HashSet<Attribute>();
		this.additionalReferencesForEdges = new HashSet<EReference>();
	}

	/**
	 * Determine name, nsPrefix and nsURI of the Ecore rootpackage
	 */
	private void determineEPackageProperties() {
		// Determine nsPrefix, nsURI and name of the rootpackage
		if ((this.config.getOption_nsPrefix() == null)
				|| this.config.getOption_nsPrefix().equals("")) {
			this.config.setOption_nsPrefix(((String) this.schemagraph
					.getFirstDefinesGraphClass().getOmega()
					.getAttribute("qualifiedName")).toLowerCase());
		}
		if ((this.config.getOption_nsURI() == null)
				|| this.config.getOption_nsURI().equals("")) {
			this.config.setOption_nsURI("http://"
					+ this.config.getOption_nsPrefix() + "/1.0/");
		}

		if ((this.config.getOption_rootpackageName() == null)
				|| this.config.getOption_rootpackageName().equals("")) {
			this.config.setOption_rootpackageName("rootpackage");
		}

		this.rootpackage.setNsPrefix(this.config.getOption_nsPrefix());
		this.rootpackage.setNsURI(this.config.getOption_nsURI());
		this.rootpackage.setName(this.config.getOption_rootpackageName());

		if (this.config.isOption_backToEcore()) {
			Iterator<? extends Comment> ic = this.schemagraph
					.getGraphClassVertices().iterator().next().get_comments()
					.iterator();
			while (ic.hasNext()) {
				String comText = ic.next().get_text();
				if (comText.startsWith(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
						+ EAnnotationKeys.DEFAULT_WAS_ROOT)) {
					String packPref = comText.substring(comText
							.lastIndexOf(" ") + 1);
					this.rootpackage.setName(packPref);
				}
			}
		}
	}

	/**
	 * Puts all user defined options as String into an EAnnotation with
	 * specified source String.
	 * 
	 * @return resulting EAnnotation
	 */
	private EAnnotation getOptionsAsEAnnotation() {
		EAnnotation ean = EcoreFactory.eINSTANCE.createEAnnotation();
		ean.setSource(EAnnotationKeys.SOURCE_CONFIG);
		ean.getDetails().put("Option backtransformation",
				this.config.isOption_backToEcore() + "");
		ean.getDetails()
				.put("Option transform Edges with only one rolename to unidirectional references",
						this.config.isOption_oneroleToUni() + "");
		ean.getDetails().put("Option transform GraphClass:",
				this.config.isOption_transformGraphClass() + "");
		ean.getDetails().put("Option make GraphClass to root element",
				this.config.isOption_makeGraphClassToRootElement() + "");
		if (!this.config.getOption_rootpackageName().equals("rootpackage")) {
			ean.getDetails().put("Option name of rootpackage",
					this.config.getOption_rootpackageName());
		}
		ean.getDetails().put("Option nsPrefix",
				this.config.getOption_nsPrefix());
		ean.getDetails().put("Option nsURI", this.config.getOption_nsURI());
		for (String key : this.config.getOption_definerolenames().keySet()) {
			for (EdgeDirection ekey : this.config.getOption_definerolenames()
					.get(key).keySet()) {
				ean.getDetails().put(
						"Option define missing rolenames",
						key
								+ " "
								+ ekey
								+ " "
								+ this.config.getOption_definerolenames()
										.get(key).get(ekey));
			}
		}
		return ean;
	}

	/**
	 * Iterates over the grUML package structure and generates an ecore one for
	 * it It fills the {@link}vertexclass2eclass, the {@link}packagemap, the
	 * {@link}enums, the {@link}records maps and the {@link}
	 * edgeclasses_for_eclasses, {@link}edgeclasses_for_ereferen sets.
	 * 
	 * @param pack
	 *            Package to transform
	 * @return EPackage resulting from the input Package
	 * */
	private EPackage transformPackagesWithContent(Package pack) {
		EPackage epack = EcoreFactory.eINSTANCE.createEPackage();
		String qualname = pack.get_qualifiedName();
		String simplename = qualname.substring(qualname.lastIndexOf(".") + 1);
		epack.setName(simplename);

		this.packagemap.put(pack, epack);
		this.transformCommentsToEAnnotations(pack, epack);

		// Vertexes and Edges
		for (ContainsGraphElementClass gec : pack
				.getContainsGraphElementClassIncidences()) {
			Vertex v = gec.getOmega();

			// GraphElementClass is a VertexClass
			if (v instanceof VertexClass) {
				VertexClass vc = (VertexClass) v;
				EClass eclass = EcoreFactory.eINSTANCE.createEClass();
				epack.getEClassifiers().add(eclass);
				this.vertexclass2eclass.put(vc, eclass);
			}
			// GraphElementClass is an EdgeClass
			else if (v instanceof EdgeClass) {
				EdgeClass ec = (EdgeClass) v;
				// If the EdgeClass has sub-, superclasses or attributes,
				// transform it to an EClass
				if ((ec.get_subclasses().iterator().hasNext()
						|| ec.get_superclasses().iterator().hasNext() || ec
						.get_attributes().iterator().hasNext())) {
					if (this.config.isOption_noEClassForEdgeClasses()) {
						if (!ec.get_superclasses().iterator().hasNext()) {
							EReference[] refs = {
									EcoreFactory.eINSTANCE.createEReference(),
									EcoreFactory.eINSTANCE.createEReference() };
							this.edgeclasses2ereferen.put(ec, refs);
						}
					} else {
						EClass eclass = EcoreFactory.eINSTANCE.createEClass();
						this.edgeclasses2eclasses.put(ec, eclass);
						epack.getEClassifiers().add(eclass);
					}
				}
				// If not, just make two EReferences out of it
				else {
					EReference[] refs = {
							EcoreFactory.eINSTANCE.createEReference(),
							EcoreFactory.eINSTANCE.createEReference() };
					this.edgeclasses2ereferen.put(ec, refs);
				}
			}
		}
		// Enumerations and Records
		for (ContainsDomain cond : pack.getContainsDomainIncidences()) {
			if (cond.getOmega() instanceof EnumDomain) {
				EnumDomain end = (EnumDomain) cond.getOmega();
				EEnum value = this.transformEnum(end);
				this.enums.put(end, value);
			} else if (cond.getOmega() instanceof RecordDomain) {
				RecordDomain recd = (RecordDomain) cond.getOmega();
				EClassifier recec = this.transformRecordDomain(recd);
				this.records.put(recd, recec);
				epack.getEClassifiers().add(recec);
			}
		}
		// Subpackages
		for (Package sub : pack.get_subpackages()) {
			EPackage esub = this.transformPackagesWithContent(sub);
			epack.getESubpackages().add(esub);
		}
		return epack;
	}

	/**
	 * Determines the SupPackages nsURI and nsPrefix. If it is a back
	 * transformation, the nsPrefix and the nsURI of the rootpackage become
	 * overwritten.
	 * 
	 * @param p
	 *            the current package to determine nsPrefix and nsURI for
	 */
	private void generatePackageNS(Package p) {
		EPackage pack = this.packagemap.get(p);
		boolean found = false;
		// Look for comments, if it is a back transformation
		if (this.config.isOption_backToEcore()) {
			for (Comment c : p.get_comments()) {
				if (c.get_text().startsWith(
						EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
								+ EAnnotationKeys.EPACKAGE_NSPREFIX)) {
					pack.setNsPrefix(c
							.get_text()
							.replaceAll(
									EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
											+ EAnnotationKeys.EPACKAGE_NSPREFIX,
									"").replace("\"", ""));
					found = true;
				}
				if (c.get_text().startsWith(
						EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
								+ EAnnotationKeys.EPACKAGE_NSURI)) {
					pack.setNsURI(c
							.get_text()
							.replaceAll(
									EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
											+ EAnnotationKeys.EPACKAGE_NSURI,
									"").replace("\"", ""));
					found = true;
				}
			}
		}
		// If there are no comments and the Package is a SubPackage, generate
		// nsPrefix and nsURI:
		// nsPrefix: parentsNsPrefix + "."+ name
		// nsURI: parentsNsURI+"/"+nsPrefix
		if (!found && (pack.getESuperPackage() != null)) {
			pack.setNsPrefix(pack.getESuperPackage().getNsPrefix() + "."
					+ pack.getName().toLowerCase());
			pack.setNsURI(pack.getESuperPackage().getNsURI()
					+ "/"
					+ pack.getNsPrefix().substring(
							pack.getNsPrefix().lastIndexOf(".") + 1));
		}

		// Iterate over all child packages
		for (Package sub : p.get_subpackages()) {
			this.generatePackageNS(sub);
		}

	}

	/**
	 * Transforms the GraphClass into an EClass
	 * 
	 * @param GraphClass
	 * */
	private void transformGraphClass(GraphClass graphClass) {

		// If the not transform GraphClass Option is set and it is no back
		// transformation, then return - if the option_backToEcore is set
		// transformation of the GraphClass depends on the generated comments
		if (!this.config.isOption_transformGraphClass()
				&& !this.config.isOption_backToEcore()) {
			return;
		}

		// Look for comments
		boolean returncausecomment = false;
		if (this.config.isOption_backToEcore()) {
			for (Comment com : graphClass.get_comments()) {
				// Check if GraphClass is generated
				if (com.get_text().equals(
						EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
								+ EAnnotationKeys.GENERATED_GRAPHCLASS)) {
					// GraphClass is generated from Ecore2Tg -> abort
					// Transformation
					returncausecomment = true;
				}
				// Check the nsPrefix and nsURI that are saved in Attributes
				// from Ecore2Tg
				if (com.get_text().startsWith(
						EAnnotationKeys.ECORE_2_TG_METADATA_FLAG)) {
					for (Attribute at : graphClass.get_attributes()) {
						if (at.get_name().equals("nsPrefix")) {
							this.rootpackage.setNsPrefix(at.get_defaultValue()
									.replace("\"", ""));
							this.badAttributes.add(at);
						} else if (at.get_name().equals("nsURI")) {
							this.rootpackage.setNsURI(at.get_defaultValue()
									.replace("\"", ""));
							this.badAttributes.add(at);
						}
					}
				}
			}
		}
		// Return if the GraphClass was generated during Ecore2Tg
		if (returncausecomment) {
			return;
		}

		// Now, an EClass is generated for the GraphClass
		EClass gc_eclass = EcoreFactory.eINSTANCE.createEClass();
		gc_eclass.setName(graphClass.get_qualifiedName());
		for (Attribute at : graphClass.get_attributes()) {
			if (!at.get_name().equals("nsPrefix")
					&& !at.get_name().equals("nsURI")) {
				EStructuralFeature eat = this.transformAttribute(at);
				if (eat != null) {
					gc_eclass.getEStructuralFeatures().add(eat);
				}
			}
		}
		EAnnotation ean = EcoreFactory.eINSTANCE.createEAnnotation();
		ean.setSource(EAnnotationKeys.SOURCE_STRING);
		ean.getDetails().put(EAnnotationKeys.KEY_IS_GRAPHCLASS, "true");
		gc_eclass.getEAnnotations().add(ean);

		this.transformCommentsToEAnnotationForGraphClass(graphClass, gc_eclass);

		if (this.config.isOption_makeGraphClassToRootElement()) {
			this.addContainmentsToGraphClass(this.rootpackage, gc_eclass);
		}
		this.rootpackage.getEClassifiers().add(gc_eclass);
		this.graphClassOfSchema = gc_eclass;

	}

	/**
	 * Creates a containment EReference for all EClasses, that are not contained
	 * by another EClass to the GraphClass
	 * 
	 * @param pack
	 *            EPackage to search for
	 * @param gc_eclass
	 *            EClass that represents the GraphClass
	 */
	private void addContainmentsToGraphClass(EPackage pack, EClass gc_eclass) {
		for (EClassifier eclassi : pack.getEClassifiers()) {
			if (!(eclassi instanceof EClass)) {
				continue;
			}
			EClass eclass = (EClass) eclassi;
			if (!this.alreadyContainedEClasses.contains(eclass)
					&& eclass.getESuperTypes().isEmpty()) {
				EReference cont = EcoreFactory.eINSTANCE.createEReference();
				cont.setLowerBound(0);
				cont.setUpperBound(-1);
				cont.setContainment(true);
				cont.setEType(eclass);
				cont.setName(eclass.getName().toLowerCase() + "s");
				gc_eclass.getEStructuralFeatures().add(cont);
			}
		}
		for (EPackage subp : pack.getESubpackages()) {
			this.addContainmentsToGraphClass(subp, gc_eclass);
		}
	}

	/**
	 * Finishes the transformation from VertexClasses to EClasses by adding the
	 * name, super types and attributes
	 * */
	private void transformVertexClasses() {
		for (VertexClass vc : this.vertexclass2eclass.keySet()) {
			EClass eclass = this.vertexclass2eclass.get(vc);
			String qualname = vc.get_qualifiedName();
			String simplename = qualname
					.substring(qualname.lastIndexOf(".") + 1);
			if (simplename.equals("Edge_") || simplename.equals("Vertex_")
					|| simplename.equals("Graph_")) {
				simplename = simplename.substring(0, simplename.length() - 1);
			}
			eclass.setName(simplename);
			for (VertexClass supervc : vc.get_superclasses()) {
				eclass.getESuperTypes().add(
						this.vertexclass2eclass.get(supervc));
			}
			for (Attribute at : vc.get_attributes()) {
				EStructuralFeature transAt = this.transformAttribute(at);
				if (transAt != null) {
					eclass.getEStructuralFeatures().add(transAt);
				}
			}
			// Test if VertexClass is abstract
			if (vc.is_abstract()) {
				eclass.setAbstract(true);
			}
			// Test if VertexClass is interface
			for (Comment com : vc.get_comments()) {
				if (com.get_text().equals(
						EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
								+ EAnnotationKeys.INTERFACE)) {
					eclass.setInterface(true);
				}
			}
			this.transformCommentsToEAnnotations(vc, eclass);

		}
	}

	/**
	 * Transforms EdgeClasses without super types and attributes into
	 * EReferences.
	 * */
	private void transformEdgeClassesToEReferences() {
		for (EdgeClass ec : this.edgeclasses2ereferen.keySet()) {

			// Get prepared EReferences
			EReference eref_to = this.edgeclasses2ereferen.get(ec)[1];
			IncidenceClass toinc = ec.get_to();

			EReference eref_from = this.edgeclasses2ereferen.get(ec)[0];
			IncidenceClass frominc = ec.get_from();

			// Get endpoints
			EClass start = this.vertexclass2eclass.get(frominc
					.get_targetclass());
			EClass target = this.vertexclass2eclass
					.get(toinc.get_targetclass());

			// Check on transform former unidirectional EReferences again to
			// unidirectional EReferences
			boolean badFrom = false;
			boolean badTo = false;
			if (this.config.isOption_backToEcore()) {
				for (Comment c : ec.get_comments()) {
					if (c.get_text().equals(
							EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
									+ EAnnotationKeys.GENERATE_DIRECTION_FROM)) {
						badFrom = true;
					}
					if (c.get_text().equals(
							EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
									+ EAnnotationKeys.GENERATE_DIRECTION_TO)) {
						badTo = true;
					}
				}
			}
			// Check whether Incidences without role_names should become
			// transformed
			else if (this.config.isOption_oneroleToUni()) {
				if ((frominc.get_roleName() == null)
						|| frominc.get_roleName().equals("")) {
					badFrom = true;
				}
				if ((toinc.get_roleName() == null)
						|| toinc.get_roleName().equals("")) {
					badTo = true;
				}
			}

			// Add the EReferences to start and target if they are not generated
			if (!badTo) {
				start.getEStructuralFeatures().add(eref_to);
				eref_to.setEType(target);
			}
			if (!badFrom) {
				target.getEStructuralFeatures().add(eref_from);
				eref_from.setEType(start);
			}

			// To-Direction
			this.fillWithIncidences(eref_to, toinc, frominc, target);
			// From-Direction
			this.fillWithIncidences(eref_from, frominc, toinc, start);

			// /----

			EAnnotation an_to = EcoreFactory.eINSTANCE.createEAnnotation();
			EAnnotation an_from = EcoreFactory.eINSTANCE.createEAnnotation();

			this.addBasicEAnnotationsToEReferences(ec, an_to, an_from);

			// Determine containment or aggregation
			if (toinc.get_aggregation().equals(AggregationKind.COMPOSITE)) {
				eref_to.setContainment(true);
				this.alreadyContainedEClasses.add(eref_to.getEReferenceType());
			} else if (toinc.get_aggregation().equals(AggregationKind.SHARED)) {
				an_to.getDetails().put(EAnnotationKeys.KEY_IS_AGGREGATION,
						"true");
			}
			if (frominc.get_aggregation().equals(AggregationKind.COMPOSITE)) {
				eref_from.setContainment(true);
				this.alreadyContainedEClasses
						.add(eref_from.getEReferenceType());
			} else if (frominc.get_aggregation().equals(AggregationKind.SHARED)) {
				an_from.getDetails().put(EAnnotationKeys.KEY_IS_AGGREGATION,
						"true");
			}

			// Adding as Opposite
			if (badFrom) {
				this.edgeclasses2ereferen.get(ec)[0] = null;
			} else if (badTo) {
				this.edgeclasses2ereferen.get(ec)[1] = null;
			} else /* (!generateFrom && !generateTo) */{
				eref_to.setEOpposite(eref_from);
				eref_from.setEOpposite(eref_to);
			}

			eref_to.getEAnnotations().add(an_to);
			eref_from.getEAnnotations().add(an_from);

			this.transformCommentsToEAnnotations(ec, eref_to);

		}
	}

	/**
	 * Adds the basic metadata to the EReferences that represent an EdgeClass
	 * 
	 * @param ec
	 *            the former EdgeClass
	 * @param an_to
	 *            EReference representing the to direction of the EdgeClass
	 * @param an_from
	 *            EReference representing the from direction of the EdgeClass
	 */
	private void addBasicEAnnotationsToEReferences(EdgeClass ec,
			EAnnotation an_to, EAnnotation an_from) {
		an_to.setSource(EAnnotationKeys.SOURCE_STRING);
		an_to.getDetails().put(EAnnotationKeys.KEY_FOR_DIRECTION, "TO");
		an_to.getDetails().put(EAnnotationKeys.KEY_FOR_EDGECLASS_NAME,
				ec.get_qualifiedName());
		an_to.getDetails().put(
				EAnnotationKeys.KEY_FOR_PACKAGE_NAME,
				((NamedElement) ec.getFirstContainsGraphElementClassIncidence()
						.getAlpha()).get_qualifiedName());

		an_from.setSource(EAnnotationKeys.SOURCE_STRING);
		an_from.getDetails().put(EAnnotationKeys.KEY_FOR_DIRECTION, "FROM");
		an_from.getDetails().put(EAnnotationKeys.KEY_FOR_EDGECLASS_NAME,
				ec.get_qualifiedName());
		an_from.getDetails().put(
				EAnnotationKeys.KEY_FOR_PACKAGE_NAME,
				((NamedElement) ec.getFirstContainsGraphElementClassIncidence()
						.getAlpha()).get_qualifiedName());
	}

	/**
	 * Fills an EReference with the correct Incidences from an EdgeClass
	 * 
	 * @param ereference
	 *            to fill
	 * @param incidence
	 *            representing direction of ereference
	 * @param opposite
	 *            representing the other direction
	 * @param target
	 *            of the ereference
	 */
	private void fillWithIncidences(EReference ereference,
			IncidenceClass incidence, IncidenceClass opposite, EClass target) {
		// To-Direction
		if ((incidence.get_roleName() != null)
				&& !incidence.get_roleName().equals("")) {
			ereference.setName(incidence.get_roleName());
		} else if ((opposite.get_roleName() != null)
				&& !opposite.get_roleName().equals("")) {
			ereference.setName(opposite.get_roleName() + "Opposite");
		} else {
			ereference.setName(target.getName().toLowerCase());
		}
		ereference.setLowerBound(incidence.get_min());
		ereference.setUpperBound(incidence.get_max() == Integer.MAX_VALUE ? -1
				: incidence.get_max());
	}

	/**
	 * Sorts an EdgeClass list in a way, that all parent classes are before
	 * their children. Ugly, but seems to work.
	 * 
	 * @param eclist
	 *            EdgeClass list to sort
	 */
	private void sortEdgeClassList(ArrayList<EdgeClass> eclist) {
		for (int i = 0; i < eclist.size(); i++) {
			int position = i;
			EdgeClass element = eclist.get(i);
			for (EdgeClass superec : element.get_superclasses()) {
				int j = eclist.indexOf(superec);
				if (j > position) {
					eclist.set(position, superec);
					eclist.set(j, element);
				}
			}
		}
	}

	private boolean takeRoleNamesAsInnerRefs = false;

	/**
	 * Transforms EdgeClasses with super types and attributes into EClasses
	 * 
	 * */
	private void transformEdgeClassesToEClasses() {

		// Sort the EdgeClasses so, that SuperClasses are transformed before
		// SubClasses
		Set<EdgeClass> ecset = this.edgeclasses2eclasses.keySet();
		ArrayList<EdgeClass> eclist = new ArrayList<EdgeClass>();
		eclist.addAll(ecset);
		this.sortEdgeClassList(eclist);

		for (EdgeClass edgeclass : eclist) {

			EClass eclass = this.edgeclasses2eclasses.get(edgeclass);

			// Name of conceptual EdgeClass
			String qualname = edgeclass.get_qualifiedName();
			String simplename = qualname
					.substring(qualname.lastIndexOf(".") + 1);
			if (simplename.equals("Edge_") || simplename.equals("Vertex_")
					|| simplename.equals("Graph_")) {
				simplename = simplename.substring(0, simplename.length() - 2);
			}
			eclass.setName(simplename);

			// Attributes
			for (Attribute at : edgeclass.get_attributes()) {
				EStructuralFeature transAt = this.transformAttribute(at);
				eclass.getEStructuralFeatures().add(transAt);
			}

			// Supertypes
			for (EdgeClass sup : edgeclass.get_superclasses()) {
				eclass.getESuperTypes().add(this.edgeclasses2eclasses.get(sup));
			}

			// Annotate as EdgeClass
			EAnnotation anno_eclass = EcoreFactory.eINSTANCE
					.createEAnnotation();
			anno_eclass.setSource(EAnnotationKeys.SOURCE_STRING);
			anno_eclass.getDetails().put(EAnnotationKeys.KEY_IS_EDGECLASS,
					"true");
			eclass.getEAnnotations().add(anno_eclass);

			// Get Start of EdgeClass
			IncidenceClass from = edgeclass.get_from();
			VertexClass startvertex = from.get_targetclass();
			EClass starteclass = this.vertexclass2eclass.get(startvertex);

			// Get End of EdgeClass
			IncidenceClass to = edgeclass.get_to();
			VertexClass targetvertex = to.get_targetclass();
			EClass targeteclass = this.vertexclass2eclass.get(targetvertex);

			// Create EReferences
			EReference to_start_ref = EcoreFactory.eINSTANCE.createEReference();
			EReference from_start_ref = EcoreFactory.eINSTANCE
					.createEReference();
			EReference to_target_ref = EcoreFactory.eINSTANCE
					.createEReference();
			EReference from_target_ref = EcoreFactory.eINSTANCE
					.createEReference();

			// Determine which EReferences shouldn't become filled;
			boolean[] tempArray = this
					.checkWhichEReferencesToGenerateForEdgeClass(edgeclass,
							to_start_ref, from_target_ref, to_target_ref,
							from_start_ref, eclass, from, to);
			boolean generate_from_end = tempArray[0];
			boolean generate_to_end = tempArray[1];
			boolean generate_from_start = tempArray[2];
			boolean generate_to_start = tempArray[3];

			// Set names and incidences for the EReferences
			this.setNamesAndIncidencesToEReferencesOfEdgeClass(edgeclass,
					eclass, from, to, to_start_ref, from_start_ref,
					to_target_ref, from_target_ref);

			// Create EAnnotations for EReferences
			EAnnotation anno_to_start_ref = EcoreFactory.eINSTANCE
					.createEAnnotation();
			EAnnotation anno_from_start_ref = EcoreFactory.eINSTANCE
					.createEAnnotation();
			EAnnotation anno_to_target_ref = EcoreFactory.eINSTANCE
					.createEAnnotation();
			EAnnotation anno_from_target_ref = EcoreFactory.eINSTANCE
					.createEAnnotation();

			// Annotate EReferences with Directions
			this.annotateEReferencesOfEdgeClassWithDirections(
					anno_to_start_ref, anno_from_start_ref, anno_to_target_ref,
					anno_from_target_ref);

			// Set the Containment property if necessary or save a Aggregation
			// as EAnnotation
			this.setContainmentPropertyForEReferencesOfEdgeClass(from, to,
					to_start_ref, from_start_ref, to_target_ref,
					from_target_ref, anno_to_start_ref, anno_from_start_ref,
					anno_to_target_ref, anno_from_target_ref);

			// Add the EAnnotations to the EReferences
			to_start_ref.getEAnnotations().add(anno_to_start_ref);
			from_start_ref.getEAnnotations().add(anno_from_start_ref);
			to_target_ref.getEAnnotations().add(anno_to_target_ref);
			from_target_ref.getEAnnotations().add(anno_from_target_ref);

			// Transform Comments of EdgeClass
			this.transformCommentsToEAnnotations(edgeclass, eclass);

			this.saveEReferencesIfTeyAreOk(edgeclass, eclass, starteclass,
					targeteclass, to_start_ref, from_start_ref, to_target_ref,
					from_target_ref, generate_from_end, generate_to_end,
					generate_from_start, generate_to_start);
		}
	}

	/**
	 * Examine the EReferences if they are the same as the parent ones, in that
	 * case the parent ones where taken. When the right EReferences are
	 * determined, they become connected to the start, end and eclass and
	 * finally added to the edgeclass2concept memory
	 * 
	 * @param edgeclass
	 *            EdgeClass that should become transformed
	 * @param eclass
	 *            EClass representing the EdgeClass
	 * @param starteclass
	 *            Ecore start of EdgeClass
	 * @param targeteclass
	 *            Ecore target of EdgeClass
	 * @param to_start_ref
	 *            EReference from EdgeClass to start
	 * @param from_start_ref
	 *            EReference from start to EdgeClass
	 * @param to_target_ref
	 *            EReference from EdgeClass to target
	 * @param from_target_ref
	 *            EReference from target to EdgeClass
	 * @param generate_from_end
	 *            if the from_target_ref shouldn't become transformed
	 * @param generate_to_end
	 *            if the to_target_ref shouldn't become transformed
	 * @param generate_from_start
	 *            if the from_start_ref shoudn't become transformed
	 * @param generate_to_start
	 *            if the to_start_ref shoudn't become transformed
	 */
	private void saveEReferencesIfTeyAreOk(EdgeClass edgeclass, EClass eclass,
			EClass starteclass, EClass targeteclass, EReference to_start_ref,
			EReference from_start_ref, EReference to_target_ref,
			EReference from_target_ref, boolean generate_from_end,
			boolean generate_to_end, boolean generate_from_start,
			boolean generate_to_start) {
		// Check on already there EReferences of SuperClasses
		// If there are EReferences that occur twice, use
		// the EReferences from the SuperClass
		boolean same_as_parent_to_start = false;
		boolean same_as_parent_to_target = false;
		boolean same_as_parent_from_start = false;
		boolean same_as_parent_from_target = false;

		for (EdgeClass parec : edgeclass.get_superclasses()) {
			ConceptualEdgeClass ecconc = this.edgeclasses2concepts.get(parec);
			if (from_start_ref.getName().equals(
					"outgoing_" + eclass.getName().toLowerCase())) {
				if (ecconc.erefFromECToStart.getEType() == starteclass) {
					// Same EReference as parent - don't add
					same_as_parent_to_start = true;
					same_as_parent_from_start = true;
					// Set for the concept map to parent
					to_start_ref = ecconc.erefFromECToStart;
					from_start_ref = ecconc.erefFromStartToEC;
				}
			}
			if (from_target_ref.getName().equals(
					"incoming_" + eclass.getName().toLowerCase())) {
				if (ecconc.erefFromECToTarget.getEType() == targeteclass) {
					// Same EReference as parent - don't add
					same_as_parent_to_target = true;
					same_as_parent_from_target = true;
					// Set for the concept map to parent
					to_target_ref = ecconc.erefFromECToTarget;
					from_target_ref = ecconc.erefFromTargetToEC;
				}
			}

		}

		// ---------------------------------------------------
		// Put the not generated EReferences on their place
		// in dependency from the generate... and same_as_...
		// flags
		// ---------------------------------------------------
		if (!generate_to_start && !same_as_parent_to_start) {
			eclass.getEStructuralFeatures().add(to_start_ref);
			to_start_ref.setEType(starteclass);
		} else if (generate_to_start) {
			to_start_ref = null;
		}
		if (!generate_from_start && !same_as_parent_from_start) {
			starteclass.getEStructuralFeatures().add(from_start_ref);
			from_start_ref.setEType(eclass);
		} else if (generate_from_start) {
			from_start_ref = null;
		}
		if (!generate_to_end && !same_as_parent_to_target) {
			eclass.getEStructuralFeatures().add(to_target_ref);
			to_target_ref.setEType(targeteclass);
		} else if (generate_to_end) {
			to_target_ref = null;
		}
		if (!generate_from_end && !same_as_parent_from_target) {
			targeteclass.getEStructuralFeatures().add(from_target_ref);
			from_target_ref.setEType(eclass);
		} else if (generate_from_end) {
			from_target_ref = null;
		}
		if (!generate_to_start && !generate_from_start
				&& !same_as_parent_to_start && !same_as_parent_from_start) {
			to_start_ref.setEOpposite(from_start_ref);
			from_start_ref.setEOpposite(to_start_ref);
		}
		if (!generate_to_end && !generate_from_end && !same_as_parent_to_target
				&& !same_as_parent_from_target) {
			to_target_ref.setEOpposite(from_target_ref);
			from_target_ref.setEOpposite(to_target_ref);
		}

		// Get the inherited incidences
		if (generate_to_start && generate_from_start) {
			for (EdgeClass parec : edgeclass.get_superclasses()) {
				ConceptualEdgeClass parentconcept = this.edgeclasses2concepts
						.get(parec);
				if ((parentconcept.erefFromECToStart != null)
						|| (parentconcept.erefFromStartToEC != null)) {
					to_start_ref = parentconcept.erefFromECToStart;
					from_start_ref = parentconcept.erefFromStartToEC;
				}
			}
		}
		// Get the inherited incidences for the concept - necessary for the
		// model later
		if (generate_to_end && generate_from_end) {
			for (EdgeClass parec : edgeclass.get_superclasses()) {
				ConceptualEdgeClass parentconcept = this.edgeclasses2concepts
						.get(parec);
				if ((parentconcept.erefFromECToTarget != null)
						|| (parentconcept.erefFromTargetToEC != null)) {
					to_target_ref = parentconcept.erefFromECToTarget;
					from_target_ref = parentconcept.erefFromTargetToEC;
				}
			}
		}

		// Save
		this.edgeclasses2concepts.put(edgeclass, new ConceptualEdgeClass(
				from_start_ref, to_start_ref, from_target_ref, to_target_ref));
	}

	/**
	 * Fills the 4 EReferences of an EdgeClass with name and multiplicity
	 * 
	 * @param edgeclass
	 *            former EdgeClass
	 * @param eclass
	 *            conceptual EdgeClass
	 * @param from
	 *            Incidence of EdgeClass
	 * @param to
	 *            Incidence of EdgeClass
	 * @param to_start_ref
	 *            EReference from the conceptual EdgeClass to its start
	 * @param from_start_ref
	 *            EReference from the start to the conceptual EdgeClass
	 * @param to_target_ref
	 *            EReference from the conceptual EdgeClass to its target
	 * @param from_target_ref
	 *            EReference from the target to the conceptual EdgeClass
	 */
	private void setNamesAndIncidencesToEReferencesOfEdgeClass(
			EdgeClass edgeclass, EClass eclass, IncidenceClass from,
			IncidenceClass to, EReference to_start_ref,
			EReference from_start_ref, EReference to_target_ref,
			EReference from_target_ref) {
		// Setting names
		// Check if TO rolename exists
		if ((to.get_roleName() != null) && !to.get_roleName().equals("")) {
			// Check if the TO rolename should be taken for the from_start
			if ((from_start_ref.getName() == null)
					&& this.takeRoleNamesAsInnerRefs) {
				from_start_ref.setName(to.get_roleName());
			}
			// Otherwise take the TO rolename for to_target
			else if (to_target_ref.getName() == null) {
				to_target_ref.setName(to.get_roleName());
			}
		}
		// Check if FROM rolename exists
		if ((from.get_roleName() != null) && !from.get_roleName().equals("")) {
			// Check if the from rolename should be taken for the
			// from_target
			if ((from_target_ref.getName() == null)
					&& this.takeRoleNamesAsInnerRefs) {
				from_target_ref.setName(from.get_roleName());
			}
			// Otherwise take the FROM rolename for to_start
			else if (to_start_ref.getName() == null) {
				to_start_ref.setName(from.get_roleName());
			}
		}

		// Initalizing EReference to_start
		if (to_start_ref.getName() == null) {
			if (this.config.getOption_definerolenames().containsKey(
					edgeclass.get_qualifiedName())
					&& this.config.getOption_definerolenames()
							.get(edgeclass.get_qualifiedName())
							.containsKey(EdgeDirection.From)) {
				to_start_ref.setName(this.config.getOption_definerolenames()
						.get(edgeclass.get_qualifiedName())
						.get(EdgeDirection.From));
			} else {
				to_start_ref.setName("startOf" + eclass.getName());
			}
		}
		to_start_ref.setLowerBound(1);
		to_start_ref.setUpperBound(1);

		// Initializing EReference from_start
		if (from_start_ref.getName() == null) {
			if (this.config.getOption_definerolenames().containsKey(
					edgeclass.get_qualifiedName())
					&& this.config.getOption_definerolenames()
							.get(edgeclass.get_qualifiedName())
							.containsKey(EdgeDirection.To)) {
				from_start_ref.setName(this.config.getOption_definerolenames()
						.get(edgeclass.get_qualifiedName())
						.get(EdgeDirection.To));
			} else {
				from_start_ref.setName("outgoing" + eclass.getName());
			}
		}
		from_start_ref.setLowerBound(to.get_min());
		from_start_ref.setUpperBound(to.get_max() == Integer.MAX_VALUE ? -1
				: to.get_max());

		// Initializing EReference to_target
		if (to_target_ref.getName() == null) {
			if (this.config.getOption_definerolenames().containsKey(
					edgeclass.get_qualifiedName())
					&& this.config.getOption_definerolenames()
							.get(edgeclass.get_qualifiedName())
							.containsKey(EdgeDirection.To)) {
				to_target_ref.setName(this.config.getOption_definerolenames()
						.get(edgeclass.get_qualifiedName())
						.get(EdgeDirection.To));
			} else {
				to_target_ref.setName("targetOf" + eclass.getName());
			}
		}
		to_target_ref.setLowerBound(1);
		to_target_ref.setUpperBound(1);

		// Initializing EReference from_target
		if (from_target_ref.getName() == null) {
			if (this.config.getOption_definerolenames().containsKey(
					edgeclass.get_qualifiedName())
					&& this.config.getOption_definerolenames()
							.get(edgeclass.get_qualifiedName())
							.containsKey(EdgeDirection.From)) {
				from_target_ref.setName(this.config.getOption_definerolenames()
						.get(edgeclass.get_qualifiedName())
						.get(EdgeDirection.From));
			} else {
				from_target_ref.setName("incoming" + eclass.getName());
			}
		}
		from_target_ref.setLowerBound(from.get_min());
		from_target_ref.setUpperBound(from.get_max() == Integer.MAX_VALUE ? -1
				: from.get_max());
	}

	/**
	 * Checks on containment or aggregation - if the EdgeClass was a
	 * containment, the containment property is set for the EReferences, if the
	 * EdgeClass was an aggregation, that is saved into EAnnotations
	 * 
	 * @param from
	 *            Incidence from of EdgeClass
	 * @param to
	 *            Incidence to of EdgeClass
	 * @param to_start_ref
	 *            EReference from conceptual EdgeClass to its start EClass
	 * @param from_start_ref
	 *            EReference from the start EClass to the conceptual EdgeClass
	 * @param to_target_ref
	 *            EReference from conceptual EdgeClass to its end EClass
	 * @param from_target_ref
	 *            EReference from the end EClass to the conceptual EdgeClass
	 * @param anno_to_start_ref
	 *            EAnnotation for to_start_ref
	 * @param anno_from_start_ref
	 *            EAnnotation for from_start_ref
	 * @param anno_to_target_ref
	 *            EAnnotation for to_target_ref
	 * @param anno_from_target_ref
	 *            EAnnotation for From_target_ref
	 */
	private void setContainmentPropertyForEReferencesOfEdgeClass(
			IncidenceClass from, IncidenceClass to, EReference to_start_ref,
			EReference from_start_ref, EReference to_target_ref,
			EReference from_target_ref, EAnnotation anno_to_start_ref,
			EAnnotation anno_from_start_ref, EAnnotation anno_to_target_ref,
			EAnnotation anno_from_target_ref) {
		// Set Containment Property to EReferences
		if (to.get_aggregation().equals(AggregationKind.COMPOSITE)) {
			to_target_ref.setContainment(true);
			from_start_ref.setContainment(true);
			this.alreadyContainedEClasses
					.add(to_target_ref.getEReferenceType());
			this.alreadyContainedEClasses.add(from_start_ref
					.getEReferenceType());
		} else if (to.get_aggregation().equals(AggregationKind.SHARED)) {
			anno_to_target_ref.getDetails().put(
					EAnnotationKeys.KEY_IS_AGGREGATION, "true");
			anno_from_start_ref.getDetails().put(
					EAnnotationKeys.KEY_IS_AGGREGATION, "true");
		}
		if (from.get_aggregation().equals(AggregationKind.COMPOSITE)) {
			from_target_ref.setContainment(true);
			to_start_ref.setContainment(true);
			this.alreadyContainedEClasses.add(from_target_ref
					.getEReferenceType());
			this.alreadyContainedEClasses.add(to_start_ref.getEReferenceType());
		} else if (from.get_aggregation().equals(AggregationKind.SHARED)) {
			anno_to_start_ref.getDetails().put(
					EAnnotationKeys.KEY_IS_AGGREGATION, "true");
			anno_from_target_ref.getDetails().put(
					EAnnotationKeys.KEY_IS_AGGREGATION, "true");
		}
	}

	/**
	 * Saves the direction of an EdgeClass into the EAnnotations of its
	 * EReferences
	 * 
	 * @param anno_to_start_ref
	 *            EAnnotation for EReference from EdgeClass to start
	 * @param anno_from_start_ref
	 *            EAnnotation for EReference from start to EdgeClass
	 * @param anno_to_target_ref
	 *            EAnnotation for EReference from EdgeClass to target
	 * @param anno_from_target_ref
	 *            EAnnotation for EReference from target to EdgeClass
	 */
	private void annotateEReferencesOfEdgeClassWithDirections(
			EAnnotation anno_to_start_ref, EAnnotation anno_from_start_ref,
			EAnnotation anno_to_target_ref, EAnnotation anno_from_target_ref) {
		anno_to_start_ref.setSource(EAnnotationKeys.SOURCE_STRING);
		anno_to_start_ref.getDetails().put(EAnnotationKeys.KEY_FOR_DIRECTION,
				"FROM");
		anno_from_start_ref.setSource(EAnnotationKeys.SOURCE_STRING);
		anno_from_start_ref.getDetails().put(EAnnotationKeys.SOURCE_STRING,
				"TO");
		anno_to_target_ref.setSource(EAnnotationKeys.SOURCE_STRING);
		anno_to_target_ref.getDetails().put(EAnnotationKeys.KEY_FOR_DIRECTION,
				"TO");
		anno_from_target_ref.setSource(EAnnotationKeys.SOURCE_STRING);
		anno_from_target_ref.getDetails().put(
				EAnnotationKeys.KEY_FOR_DIRECTION, "FROM");
	}

	/**
	 * Checks which EReferences should become instanciated
	 * 
	 * @param edgeclass
	 * @param to_start_ref
	 * @param from_target_ref
	 * @param to_target_ref
	 * @param from_start_ref
	 * @param eclass
	 *            representing the edgeclass in ecore
	 * @param from
	 *            from incidence
	 * @param to
	 *            to incidence
	 * @return boolean array of {generate_from_end, generate_to_end,
	 *         generate_from_start, generate_to_start }
	 */
	private boolean[] checkWhichEReferencesToGenerateForEdgeClass(
			EdgeClass edgeclass, ENamedElement to_start_ref,
			ENamedElement from_target_ref, ENamedElement to_target_ref,
			ENamedElement from_start_ref, EClass eclass, IncidenceClass from,
			IncidenceClass to) {
		// Determine which EReferences shouldn't become filled;
		boolean generate_from_end = false;
		boolean generate_to_end = false;
		boolean generate_from_start = false;
		boolean generate_to_start = false;
		if (this.config.isOption_backToEcore()) {
			// if option_backToEcore is chosen, check on Comments to
			// determine which EReferences are generated ones
			// and shouldn't rest because of that
			EReference tempHelp = null;
			for (Comment c : edgeclass.get_comments()) {
				if (c.get_text().equals(
						EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
								+ EAnnotationKeys.GENERATE_DIRECTION_FROM_END)) {
					generate_from_end = true;
				} else if (c
						.get_text()
						.equals(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
								+ EAnnotationKeys.GENERATE_DIRECTION_FROM_START)) {
					generate_from_start = true;
				} else if (c.get_text().equals(
						EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
								+ EAnnotationKeys.GENERATE_DIRECTION_TO_END)) {
					generate_to_end = true;
				} else if (c.get_text().equals(
						EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
								+ EAnnotationKeys.GENERATE_DIRECTION_TO_START)) {
					generate_to_start = true;
				}
				// Get missing role names
				else if (c.get_text().startsWith(
						EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
								+ EAnnotationKeys.REFERENCE_NAME_TO_START_WAS)) {
					String oldrolename = c
							.get_text()
							.replace(
									EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
											+ EAnnotationKeys.REFERENCE_NAME_TO_START_WAS,
									"");
					if (this.takeRoleNamesAsInnerRefs) {
						to_start_ref.setName(oldrolename);
					} else {
						from_target_ref.setName(oldrolename);
					}
				} else if (c.get_text().startsWith(
						EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
								+ EAnnotationKeys.REFERENCE_NAME_TO_TARGET_WAS)) {
					String oldrolename = c
							.get_text()
							.replace(
									EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
											+ EAnnotationKeys.REFERENCE_NAME_TO_TARGET_WAS,
									"");
					if (this.takeRoleNamesAsInnerRefs) {
						to_target_ref.setName(oldrolename);
					} else {
						from_start_ref.setName(oldrolename);
					}
				}
				// Check on lost containment EReferences
				else if (c.get_text().startsWith(
						EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
								+ EAnnotationKeys.CONTAINMENT_EXISTS)) {
					String val = c.get_text().replace(
							EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
									+ EAnnotationKeys.CONTAINMENT_EXISTS, "");
					if (val.endsWith(" TO")) {
						val = val.replace(" TO", "");
						String[] info = val.split(" ");
						EReference contToEdge = EcoreFactory.eINSTANCE
								.createEReference();
						contToEdge.setName(info[0]);
						contToEdge.setContainment(true);
						contToEdge.setUpperBound(-1);
						contToEdge.setLowerBound(0);
						contToEdge.setEType(eclass);
						EClass container = this.vertexclass2eclass.get(this
								.getVertexClassByName(info[1]));
						container.getEStructuralFeatures().add(contToEdge);
						this.additionalReferencesForEdges.add(contToEdge);
						if (tempHelp == null) {
							tempHelp = contToEdge;
						} else {
							tempHelp.setEOpposite(contToEdge);
						}
					}
					if (val.endsWith(" FROM")) {
						val = val.replace(" FROM", "");
						String[] info = val.split(" ");
						EReference contFromEdge = EcoreFactory.eINSTANCE
								.createEReference();
						contFromEdge.setName(info[0]);
						contFromEdge.setUpperBound(1);
						contFromEdge.setLowerBound(1);
						EClass container = this.vertexclass2eclass.get(this
								.getVertexClassByName(info[1]));
						contFromEdge.setEType(container);
						eclass.getEStructuralFeatures().add(contFromEdge);
						if (tempHelp == null) {
							tempHelp = contFromEdge;
						} else {
							tempHelp.setEOpposite(contFromEdge);
						}
					}
				}
			}
		} else if (this.config.isOption_oneroleToUni()) {
			if ((from.get_roleName() == null) || from.get_roleName().equals("")) {
				generate_from_start = true;
			} else if ((to.get_roleName() == null)
					|| to.get_roleName().equals("")) {
				generate_from_end = true;
			}
		}
		return new boolean[] { generate_from_end, generate_to_end,
				generate_from_start, generate_to_start };
	}

	/**
	 * Transforms an EStructuralFeature into an EAttribute or an EReference, in
	 * case of a RecordDomain
	 * 
	 * @param at
	 *            the Attribute to transform
	 * @return EStructuralFeature EAttribute or EReference
	 * */
	private EStructuralFeature transformAttribute(Attribute at) {
		String name = at.get_name();
		String defVal = at.get_defaultValue();
		Domain dom = at.get_domain();
		boolean isBig = false;
		// Check on BigInteger or BigDecimal - that's not nice, but I don't see
		// a better way than doing it here
		if (this.config.isOption_backToEcore()) {
			if ((dom instanceof LongDomain)
					|| ((dom instanceof CollectionDomain) && (((CollectionDomain) dom)
							.get_basedomain() instanceof LongDomain))) {
				Iterator<? extends Comment> it = ((NamedElement) at
						.getFirstHasAttributeIncidence().getAlpha())
						.get_comments().iterator();
				while (it.hasNext()) {
					String c = it.next().get_text();
					if (c.startsWith(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
							+ EAnnotationKeys.WAS_BIG_INTEGER + " ")) {
						if (c.replace(
								EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
										+ EAnnotationKeys.WAS_BIG_INTEGER + " ",
								"").equals(at.get_name())) {
							isBig = true;
						}
					}
				}
			} else if ((dom instanceof DoubleDomain)
					|| ((dom instanceof CollectionDomain) && (((CollectionDomain) dom)
							.get_basedomain() instanceof DoubleDomain))) {
				Iterator<? extends Comment> it = ((NamedElement) at
						.getFirstHasAttributeIncidence().getAlpha())
						.get_comments().iterator();
				while (it.hasNext()) {
					String c = it.next().get_text();
					if (c.startsWith(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
							+ EAnnotationKeys.WAS_BIG_DECIMAL + " ")) {
						if (c.replace(
								EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
										+ EAnnotationKeys.WAS_BIG_DECIMAL + " ",
								"").equals(at.get_name())) {
							isBig = true;
						}
					}
				}
			} else if ((dom instanceof ListDomain)
					|| (dom instanceof SetDomain)) {

			}
		}
		// Create the attribute
		EStructuralFeature f = this.createEAttributeWith(name, defVal, dom,
				isBig);
		if (f == null) {
			this.badAttributes.add(at);
		}
		return f;

	}

	/**
	 * Creates an EAttribute or an EReference with a given name, default value
	 * depending on the domain
	 * 
	 * @param name
	 *            name of EStructuralFeature
	 * @param defVal
	 *            defaultValue of EStructuralFeature
	 * @param dom
	 *            Domain of EStructuralFeature
	 * @return the resulting EStructuralFeature
	 */
	private EStructuralFeature createEAttributeWith(String name, String defVal,
			Domain dom, boolean isBig) {
		// Attribute is a Collection, List or Set
		if (dom instanceof CollectionDomain) {
			CollectionDomain list = (CollectionDomain) dom;
			EStructuralFeature res = null;
			// -- Collection of Records?
			if (list.get_basedomain() instanceof RecordDomain) {
				RecordDomain recd = (RecordDomain) list.get_basedomain();
				res = this.transformRecordDomainAttribute(recd, name, defVal);
			}
			// -- Collection of Collection ?
			else if (list.get_basedomain() instanceof CollectionDomain) {
				System.err.println("Attribute " + name
						+ " is a Collection of a Collection."
						+ " Ecore doesn't support such a structure,"
						+ " so the Attribute is not transformed.");
				return null;
			}
			// -- Other Collection
			else {
				res = this.createEAttribute(name, defVal, dom, isBig);
			}
			if (list instanceof ListDomain) {
				// List behavior
				res.setLowerBound(0);
				res.setUpperBound(-1);
				res.setOrdered(true);
				res.setUnique(false);
			} else if (list instanceof SetDomain) {
				// Set behavior
				res.setLowerBound(0);
				res.setUpperBound(-1);
				res.setOrdered(false);
				res.setUnique(true);
			}
			return res;

		}
		// Attribute is a RecordDomain
		else if (dom instanceof RecordDomain) {
			RecordDomain recd = (RecordDomain) dom;
			// -- RecordDomain is a transformed EDate
			EStructuralFeature ref = this.transformRecordDomainAttribute(recd,
					name, defVal);
			ref.setLowerBound(1);
			ref.setUpperBound(1);
			return ref;
		}
		// Attribute is a MapDomain
		else if (dom instanceof MapDomain) {
			System.err.println("Attribute " + name + " is a MapDomain."
					+ " Transformation is not supported.");
			return null;
		}
		// Attribute is not a recursive Domain - BasicDomain or EnumDomain
		else {
			EAttribute eat = this.createEAttribute(name, defVal, dom, isBig);
			eat.setLowerBound(1);
			eat.setUpperBound(1);
			return eat;
		}
	}

	/**
	 * Transforms a RecordDomain Attribute to an EAttribute. If the RecordDomain
	 * is a former EDate, it returns an EAttribute. If its's a real
	 * RecordDomain, an EReference to the EClass, representing the Record is the
	 * result.
	 * 
	 * @param recd
	 *            RecordDomain of the Attribute
	 * @param at
	 *            Attribute to transform
	 * @return transformed Attribute as EStructuralFeature
	 */
	private EStructuralFeature transformRecordDomainAttribute(
			RecordDomain recd, String name, String defValue) {
		// Record is forme EDate?
		if (this.records.get(recd) == EcorePackage.eINSTANCE.getEDate()) {
			EAttribute eat = EcoreFactory.eINSTANCE.createEAttribute();
			eat.setName(name);
			eat.setDefaultValueLiteral(defValue);
			EClassifier datatype = EcorePackage.eINSTANCE.getEDate();
			eat.setEType(datatype);
			return eat;
		}
		// Real RecordDomain
		else {
			EClass recd_eclass = (EClass) this.records.get(recd);
			EReference ref = EcoreFactory.eINSTANCE.createEReference();
			// ref.setName("has" + recd_eclass.getName());
			ref.setName(name);
			ref.setContainment(true);
			ref.setEType(recd_eclass);
			ref.setUnique(true);
			// Save that this EReference goes to a former Record for
			// Ecore2Tg
			EAnnotation ean = EcoreFactory.eINSTANCE.createEAnnotation();
			ean.setSource(EAnnotationKeys.SOURCE_STRING);
			ean.getDetails().put(EAnnotationKeys.KEY_FOR_REF_TO_RECORD, "true");
			ref.getEAnnotations().add(ean);
			return ref;
		}
	}

	/**
	 * Returns a new {@link EAttribute} with the given parameters
	 * 
	 * @param name
	 *            name of the Attribute
	 * @param defVal
	 *            defaultValue of the Attribute
	 * @param dom
	 *            Domain of the Attribute
	 * @param isBig
	 *            indicator if it should become a BigInteger or BigDecimal
	 * */
	private EAttribute createEAttribute(String name, String defVal, Domain dom,
			boolean isBig) {
		EAttribute eat = EcoreFactory.eINSTANCE.createEAttribute();
		eat.setName(name);
		eat.setDefaultValueLiteral(defVal);
		EClassifier datatype = this.transformDomain(dom, isBig);
		eat.setEType(datatype);
		return eat;
	}

	/**
	 * Transforms an EnumDomain
	 * 
	 * @param domain
	 *            EnumDomain to transform
	 * @return resulting EEnum
	 * */
	private EEnum transformEnum(EnumDomain domain) {
		EEnum eenum = EcoreFactory.eINSTANCE.createEEnum();
		String qualname = domain.get_qualifiedName();
		String simplename = qualname.substring(qualname.lastIndexOf(".") + 1);
		eenum.setName(simplename);

		(this.packagemap.get(domain.getFirstContainsDomainIncidence()
				.getAlpha())).getEClassifiers().add(eenum);
		int i = 1;
		for (String con : domain.get_enumConstants()) {

			EEnumLiteral elit = EcoreFactory.eINSTANCE.createEEnumLiteral();
			String litname = con;
			for (Comment com : domain.get_comments()) {
				if (com.get_text().startsWith(
						EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
								+ EAnnotationKeys.CHANGED_ENUM_LITERAL + con
								+ " ")) {
					litname = com.get_text().replace(
							EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
									+ EAnnotationKeys.CHANGED_ENUM_LITERAL
									+ con + " ", "");
				}
			}
			elit.setLiteral(litname);
			elit.setName(litname);
			elit.setValue(i);
			i++;
			eenum.getELiterals().add(elit);
		}
		return eenum;

	}

	/**
	 * Transforms a grUML domain into an Ecore DataType
	 * */
	private EClassifier transformDomain(Domain domain, boolean isBig) {
		EClassifier datatype = null;

		if (domain instanceof BooleanDomain) {
			datatype = EcorePackage.eINSTANCE.getEBoolean();
		} else if (domain instanceof StringDomain) {
			datatype = EcorePackage.eINSTANCE.getEString();
		} else if (domain instanceof IntegerDomain) {
			datatype = EcorePackage.eINSTANCE.getEInt();
		} else if (domain instanceof LongDomain) {
			if (isBig) {
				datatype = EcorePackage.eINSTANCE.getEBigInteger();
			} else {
				datatype = EcorePackage.eINSTANCE.getELong();
			}
		} else if (domain instanceof DoubleDomain) {
			if (isBig) {
				datatype = EcorePackage.eINSTANCE.getEBigDecimal();
			} else {
				datatype = EcorePackage.eINSTANCE.getEDouble();
			}
		} else if (domain instanceof ListDomain) {
			Domain realDomain = ((ListDomain) domain).get_basedomain();
			datatype = this.transformDomain(realDomain, isBig);
		} else if (domain instanceof SetDomain) {
			Domain realDomain = ((SetDomain) domain).get_basedomain();
			datatype = this.transformDomain(realDomain, isBig);
		} else if (domain instanceof EnumDomain) {
			datatype = this.enums.get(domain);
		} else {
			System.err.println("Transformation of " + domain
					+ " not supported.");
		}

		return datatype;
	}

	/**
	 * Transforms a RecordDomain into an EClass
	 * 
	 * @param domain
	 *            RecordDomain to transform
	 * @return resulting EClass
	 * */
	private EClassifier transformRecordDomain(RecordDomain domain) {
		// Check whether the RecordDomain is a former DateDomain
		if (domain.get_qualifiedName().endsWith("Date")) {
			boolean istransformedDate = true;
			int numberOfComponents = 0;
			for (HasRecordDomainComponent comp : domain
					.getHasRecordDomainComponentIncidences()) {
				if (!comp.get_name().equals("day")
						&& !comp.get_name().equals("month")
						&& !comp.get_name().equals("year")
						&& !comp.get_name().equals("hour")
						&& !comp.get_name().equals("minute")
						&& !comp.get_name().equals("second")) {
					istransformedDate = false;
				}
				numberOfComponents++;
			}
			if (numberOfComponents != 6) {
				istransformedDate = false;
			}
			if (istransformedDate) {
				return EcorePackage.eINSTANCE.getEDate();
			}
		}

		// If it is not a former DateDomain, create an EClass as Ecore
		// representation
		EClass eclass = EcoreFactory.eINSTANCE.createEClass();
		String qualname = domain.get_qualifiedName();
		String simplename = qualname.substring(qualname.lastIndexOf(".") + 1);
		eclass.setName(simplename);
		for (HasRecordDomainComponent d : domain
				.getHasRecordDomainComponentIncidences()) {
			EStructuralFeature eat = this.createEAttributeWith(d.get_name(),
					null, d.getOmega(), false);
			if (eat != null) {
				eclass.getEStructuralFeatures().add(eat);
			}
		}

		// Mark as RecordDomain with EAnnotation
		EAnnotation ean = EcoreFactory.eINSTANCE.createEAnnotation();
		ean.setSource(EAnnotationKeys.SOURCE_STRING);
		ean.getDetails().put(EAnnotationKeys.KEY_IS_RECORDDOMAIN, "true");
		eclass.getEAnnotations().add(ean);

		return eclass;
	}

	/**
	 * Transform grUML comments to Ecore EAnnotations
	 * 
	 * @param source
	 *            commented Element
	 * @param target
	 *            element to annotate
	 */
	private void transformCommentsToEAnnotations(NamedElement source,
			EModelElement target) {
		for (Comment comment : source.get_comments()) {
			String text = comment.get_text();
			this.transformSingleComment(target, text);
		}
	}

	/**
	 * Transform grUML comments to Ecore EAnnotations for the GraphClass.
	 * Important is to ignore the ECORE_2_TG_CONFIG_FLAG in comments, because
	 * they are just a convenience help and shouldn't become transformed to a
	 * real EAnnotation.
	 * 
	 * @param source
	 *            commented Element
	 * @param target
	 *            element to annotate
	 */
	private void transformCommentsToEAnnotationForGraphClass(GraphClass source,
			EClass target) {
		for (Comment comment : source.get_comments()) {
			String text = comment.get_text();
			if (text.startsWith(EAnnotationKeys.ECORE_2_TG_CONFIG_FLAG)) {
				// Comment contains the Configurations of Ecore2Tg -> no
				// Transformation
				continue;
			}
			this.transformSingleComment(target, text);
		}
	}

	/**
	 * Transform a single Comment into an EAnnotation and add it to the target
	 * element
	 * 
	 * @param target
	 *            element to create the EAnnotation for
	 * @param text
	 *            text of the Comment
	 */
	private void transformSingleComment(EModelElement target, String text) {
		if (text.startsWith(EAnnotationKeys.ECORE_2_TG_METADATA_FLAG)) {
			// Comment contains Metadata from Ecore2Tg -> no Transformation
			return;
		}
		EAnnotation ean = EcoreFactory.eINSTANCE.createEAnnotation();
		if (text.startsWith(EAnnotationKeys.ECORE_EANNOTATION_FLAG)) {
			String ecoretext = text.replaceFirst(
					EAnnotationKeys.ECORE_EANNOTATION_FLAG + " ", "");
			ean.setSource(ecoretext.substring(0, ecoretext.indexOf(";")));
			ecoretext = ecoretext.substring(ecoretext.indexOf(";") + 1);
			String[] c = ecoretext.split(";");
			for (String element : c) {
				ean.getDetails().put(
						element.substring(0, element.indexOf(":")),
						element.substring(element.indexOf(":") + 1));
			}
		} else {
			ean.setSource(EAnnotationKeys.SOURCE_STRING_COMMENTS);
			ean.getDetails().put(text, "");
		}
		target.getEAnnotations().add(ean);
	}

	// #########################################################
	// ##### Start of graph transformation #####################
	// #########################################################

	/**
	 * Graph corresponding to the transformed schema
	 */
	private Graph currentGraph;

	/**
	 * List of EObjects representing the model corresponding to the graph
	 */
	private ArrayList<EObject> model;

	/**
	 * Memory of vertices and their resulting EObjects
	 */
	private HashMap<Vertex, EObject> vertices2eobjects;

	/**
	 * Memory of edges and their resulting EObjects
	 */
	private HashMap<EObject, Edge> eobjects2edges;

	/**
	 * Transforms the given Graph to an Ecore model. The Graph must correspond
	 * to the given Schema.
	 * 
	 * @param g
	 *            Graph to transform
	 * @return resulting Ecore model
	 */
	public ArrayList<EObject> transformGraphToEcoreModel(Graph g) {
		System.out.println("Transformation of Graph started ...");
		long starttime = System.currentTimeMillis();

		this.model = new ArrayList<EObject>();
		this.vertices2eobjects = new HashMap<Vertex, EObject>();
		this.eobjects2edges = new HashMap<EObject, Edge>();
		this.currentGraph = g;
		this.transformVertexesToEObjects();
		this.transformEdges();
		this.transformGraphToEObject();
		this.recoverContainerOfEdges();

		long endtime = System.currentTimeMillis();
		System.out.println("Transformation of Graph finished. It took "
				+ (endtime - starttime) + "milliseconds.");

		return this.model;
	}

	/**
	 * Recovers containment EReferences that points on an conceptual EdgeClass -
	 * necessary if they become deleted during Ecore2Tg
	 */
	private void recoverContainerOfEdges() {
		for (EReference addiref : this.additionalReferencesForEdges) {
			for (EObject posedge : this.model) {
				if (posedge.eClass().equals(addiref.getEReferenceType())) {
					// Found Edge referred
					Edge origEdge = this.eobjects2edges.get(posedge);
					EObject startob = this.vertices2eobjects.get(origEdge
							.getAlpha());
					EObject endob = this.vertices2eobjects.get(origEdge
							.getOmega());
					if ((startob.eContainer() != null)
							&& (endob.eContainer() != null)
							&& startob.eContainer().eClass()
									.equals(addiref.getEContainingClass())
							&& (startob.eContainer() == endob.eContainer())) {
						this.setEdgeForEObject(startob.eContainer(), posedge,
								addiref);

					} else {
						throw new RuntimeException(
								"Invalid Model. Vertexes of contained Edges have no compatible Containers.");
					}
				}
			}
		}
	}

	/**
	 * Transforms the Vertices to EObjects corresponding to their VertexClasses
	 */
	private void transformVertexesToEObjects() {
		for (Vertex vertex : this.currentGraph.vertices()) {
			// Extract correct VertexClass
			de.uni_koblenz.jgralab.schema.VertexClass attributedElemCl = vertex
					.getAttributedElementClass();
			VertexClass vertexClass = this
					.getVertexClassByName(attributedElemCl.getQualifiedName());
			// Extract matching EClass and EPackage
			EClass eClass = this.vertexclass2eclass.get(vertexClass);
			EPackage epack = this.packagemap.get(vertexClass
					.getFirstContainsGraphElementClassIncidence().getAlpha());
			// Create EObject and transform Attributes
			EObject eob = epack.getEFactoryInstance().create(eClass);
			this.transformAttributeValues(vertex, vertexClass, eob);

			this.model.add(eob);
			this.vertices2eobjects.put(vertex, eob);
		}
	}

	/**
	 * Transforms the Graph object to an EObject.
	 */
	private void transformGraphToEObject() {
		if (this.graphClassOfSchema == null) {
			// The Graph object should not become transformed
			return;
		}
		EClass eClass = this.graphClassOfSchema;
		EObject eob = this.rootpackage.getEFactoryInstance().create(eClass);
		this.transformAttributeValues(this.currentGraph,
				this.schemagraph.getFirstGraphClass(), eob);

		if (this.config.isOption_makeGraphClassToRootElement()) {
			// Iterate over all elements in model to put them to root
			for (EObject element : this.model) {
				EClass part = element.eClass();
				// Look for the correct EReference
				EReference contRef = null;
				for (EReference eref : eClass.getEAllReferences()) {
					if (eref.getEReferenceType().equals(part)
							|| part.getEAllSuperTypes().contains(
									eref.getEReferenceType())) {
						contRef = eref;
						break;
					}
				}
				if (contRef == null) {
					// EClass not contained by root - maybe contained by another
					// eclass
					continue;
				}
				this.setEdgeForEObject(eob, element, contRef);
			}
		}
		this.model.add(eob);
	}

	/**
	 * Transform the Edges of the graph to links and EObjects
	 */
	private void transformEdges() {
		for (Edge ed : this.currentGraph.edges()) {
			de.uni_koblenz.jgralab.schema.EdgeClass eec = ed
					.getAttributedElementClass();
			EdgeClass edgeclass = this.getEdgeClassByName(eec
					.getQualifiedName());

			Vertex start = ed.getAlpha();
			Vertex end = ed.getOmega();
			EObject startob = this.vertices2eobjects.get(start);
			EObject endob = this.vertices2eobjects.get(end);
			// transform to EObject
			if (this.edgeclasses2eclasses.containsKey(edgeclass)) {
				this.transformEdgeToEObject(ed, edgeclass, startob, endob);
			}
			// transform to links
			else {
				if (this.edgeclasses2ereferen.containsKey(edgeclass)) {
					this.transformEdgeToLinks(edgeclass, startob, endob);
				} else if (this.config.isOption_noEClassForEdgeClasses()) {
					ArrayList<EdgeClass> topSuperClasses = new ArrayList<EdgeClass>();
					this.getTopSuperEdgeClasses(edgeclass, topSuperClasses);
					for (EdgeClass ec : topSuperClasses) {
						this.transformEdgeToLinks(ec, startob, endob);
					}
				} else {
					throw new RuntimeException("Should never happen.");
				}
			}
		}
	}

	private void getTopSuperEdgeClasses(EdgeClass ec, List<EdgeClass> tops) {
		if (ec.get_superclasses().iterator().hasNext()) {
			for (EdgeClass sup : ec.get_superclasses()) {
				this.getTopSuperEdgeClasses(sup, tops);
			}
		} else {
			tops.add(ec);
		}
	}

	/**
	 * Transforms an Edge into one or two Links
	 * 
	 * @param edgeclass
	 *            Class of the Edge
	 * @param startob
	 *            EObject where the Edge starts
	 * @param endob
	 *            EObject where the Edge ends
	 */
	private void transformEdgeToLinks(EdgeClass edgeclass, EObject startob,
			EObject endob) {
		EReference from = this.edgeclasses2ereferen.get(edgeclass)[0];
		EReference to = this.edgeclasses2ereferen.get(edgeclass)[1];

		if (to != null) {
			this.setEdgeForEObject(startob, endob, to);
		}
		if (from != null) {
			this.setEdgeForEObject(endob, startob, from);
		}
	}

	/**
	 * Transforms an Edge into an EObject
	 * 
	 * @param ed
	 *            Edge to become transformed
	 * @param edgeclass
	 *            Class of the Edge
	 * @param startob
	 *            EObject where the Edge starts
	 * @param endob
	 *            EObject where the Edge ends
	 */
	private void transformEdgeToEObject(Edge ed, EdgeClass edgeclass,
			EObject startob, EObject endob) {
		EClass eclassedgeclass = this.edgeclasses2eclasses.get(edgeclass);
		ConceptualEdgeClass conc = this.edgeclasses2concepts.get(edgeclass);
		EObject edgeob = eclassedgeclass.getEPackage().getEFactoryInstance()
				.create(eclassedgeclass);
		this.model.add(edgeob);
		this.eobjects2edges.put(edgeob, ed);

		if (conc.erefFromECToStart != null) {
			this.setEdgeForEObject(edgeob, startob, conc.erefFromECToStart);
		}
		if (conc.erefFromECToTarget != null) {
			this.setEdgeForEObject(edgeob, endob, conc.erefFromECToTarget);
		}
		if (conc.erefFromTargetToEC != null) {
			this.setEdgeForEObject(endob, edgeob, conc.erefFromTargetToEC);
		}
		if (conc.erefFromStartToEC != null) {
			this.setEdgeForEObject(startob, edgeob, conc.erefFromStartToEC);
		}
		this.transformAttributeValues(ed, edgeclass, edgeob);
	}

	/**
	 * Creates a link corresponding to the EReference ref between eob and target
	 * 
	 * @param eob
	 *            EObject to create the link for
	 * @param target
	 *            EObject target for the link
	 * @param ref
	 *            EReference the link should be an instance of
	 */
	private void setEdgeForEObject(EObject eob, EObject target, EReference ref) {
		// If upper bound is 1, just set the value
		if (ref.getUpperBound() == 1) {// =0 is not possible, -1 is *
			eob.eSet(ref, target);
		}
		// Else add the new link to a list of those links - create one if it is
		// the first one
		else {
			Object o = eob.eGet(ref);
			if (o == null) {
				EList<EObject> list = new BasicEList<EObject>();
				list.add(target);
				eob.eSet(ref, list);
			} else {
				@SuppressWarnings("unchecked")
				EList<EObject> eList = (EList<EObject>) o;
				eList.add(target);
			}
		}
	}

	/**
	 * Transforms the Attribute values of a given AttributedElement to
	 * EAttribute value of the corresponding EObject eob
	 * 
	 * @param ae
	 *            AttributedElement with attribute values
	 * @param vc
	 *            AttributedElementClass - schema for AttributedElement
	 * @param eob
	 *            EObject to add the EAttribute values to
	 */
	private void transformAttributeValues(AttributedElement<?, ?> ae,
			AttributedElementClass vc, EObject eob) {
		for (Attribute at : vc.get_attributes()) {
			this.transformAttributeValue(ae, eob, at);
		}
		if (vc instanceof VertexClass) {
			this.transformAttributeValueForSuperVertexClassesAttributes(
					(VertexClass) vc, (Vertex) ae, eob);
		}
		if (vc instanceof EdgeClass) {
			this.transformAttributeValueForSuperEdgeClassesAttributes(
					(EdgeClass) vc, (Edge) ae, eob);
		}
	}

	private void transformAttributeValueForSuperVertexClassesAttributes(
			VertexClass vc, Vertex ae, EObject eob) {
		for (AttributedElementClass par : vc.get_superclasses()) {
			for (Attribute at : par.get_attributes()) {
				this.transformAttributeValue(ae, eob, at);

			}
			this.transformAttributeValueForSuperVertexClassesAttributes(
					(VertexClass) par, ae, eob);
		}
	}

	private void transformAttributeValueForSuperEdgeClassesAttributes(
			EdgeClass vc, Edge ae, EObject eob) {
		for (AttributedElementClass par : vc.get_superclasses()) {
			for (Attribute at : par.get_attributes()) {
				this.transformAttributeValue(ae, eob, at);

			}
			this.transformAttributeValueForSuperEdgeClassesAttributes(
					(EdgeClass) par, ae, eob);
		}
	}

	/**
	 * Transform a single Attribute of an AttributedElement to an EAttribute of
	 * an EObject
	 * 
	 * @param element
	 *            AttributedElement to get the Attribute value
	 * @param eob
	 *            EObject to to set the Attribute value for
	 * @param tgAttribute
	 *            the current Attribute
	 */
	private void transformAttributeValue(AttributedElement<?, ?> element,
			EObject eob, Attribute tgAttribute) {
		if (this.badAttributes.contains(tgAttribute)) {
			return;
		}
		// Get the value
		Object tgAttributeValue = element.getAttribute(tgAttribute.get_name());
		if (tgAttributeValue == null) {
			return;
		}

		// Find the Ecore represenation of the Attribute
		EStructuralFeature ecoreAttribute = null;
		boolean isBig = false;
		for (EAttribute eat : eob.eClass().getEAllAttributes()) {
			if (eat.getName().equals(tgAttribute.get_name())) {
				ecoreAttribute = eat;
				if (eat.getEAttributeType().getName().equals("EBigInteger")
						|| eat.getEAttributeType().getName()
								.equals("EBigDecimal")) {
					isBig = true;
				}
				break;
			}
		}
		if (ecoreAttribute == null) { // for records it could be although an
										// EReference
			for (EReference eref : eob.eClass().getEAllReferences()) {
				if (eref.getName().equals(tgAttribute.get_name())) {
					ecoreAttribute = eref;
					break;
				}
			}
		}

		Object ecoreAttributeValue = this.getAttributeValueOf(tgAttributeValue,
				tgAttribute.get_domain(), isBig);

		eob.eSet(ecoreAttribute, ecoreAttributeValue);
	}

	/**
	 * Creates the Ecore Attribute value from the grUML one
	 * 
	 * @param atvalue
	 *            the grUML Attribute value
	 * @param dom
	 *            the Domain of the grUML Attribute value
	 * @param isBig
	 *            indicates whether the Attribute was a former BigInteger or
	 *            BigDecimal - necessary here because Attributes can't have
	 *            Comments
	 * @return the Ecore Attribute value
	 */
	private Object getAttributeValueOf(Object atvalue, Domain dom, boolean isBig) {
		// Determine the domain of the Attribute
		// ++++++++++++++++
		// ++RecordDomain++
		// ++++++++++++++++
		if (dom instanceof RecordDomain) {
			RecordDomain rd = (RecordDomain) dom;
			EClassifier recClass = this.records.get(rd);
			// Real RecordDomain
			if (recClass instanceof EClass) {
				EClass eclass = (EClass) recClass;
				EObject atrObject = eclass.getEPackage().getEFactoryInstance()
						.create(eclass);
				for (EStructuralFeature receat : eclass
						.getEAllStructuralFeatures()) {
					Record rec = (Record) atvalue;
					Object newValue = rec.getComponent(receat.getName());
					// Search for right domain
					Domain d = null;
					Iterator<HasRecordDomainComponent> it = rd
							.getHasRecordDomainComponentIncidences().iterator();
					while (it.hasNext()) {
						HasRecordDomainComponent hrdc = it.next();
						if (hrdc.get_name().equals(receat.getName())) {
							d = hrdc.getOmega();
						}
					}
					Object transrecval = this.getAttributeValueOf(newValue, d,
							false);
					atrObject.eSet(receat, transrecval);
				}
				return atrObject;
			}
			// EDate
			else {
				Record daterec = (Record) atvalue;
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.DAY_OF_MONTH,
						(Integer) daterec.getComponent("day"));
				cal.set(Calendar.MONTH,
						(Integer) daterec.getComponent("month") - 1);
				cal.set(Calendar.YEAR, (Integer) daterec.getComponent("year"));
				cal.set(Calendar.HOUR_OF_DAY,
						(Integer) daterec.getComponent("hour"));
				cal.set(Calendar.MINUTE,
						(Integer) daterec.getComponent("minute"));
				cal.set(Calendar.SECOND,
						(Integer) daterec.getComponent("second"));
				return cal.getTime();
			}
		}
		// ++++++++++++++
		// ++ListDomain++
		// ++++++++++++++
		else if (dom instanceof ListDomain) {
			ListDomain ld = (ListDomain) dom;
			if (ld.get_basedomain() instanceof RecordDomain) {
				ArrayList<Object> list = new ArrayList<Object>();
				List<?> grList = (List<?>) atvalue;
				for (Object o : grList) {
					list.add(this.getAttributeValueOf(o, ld.get_basedomain(),
							isBig));
				}
				return list;
			} else {
				// eob.eSet(eattr, atvalue);

				return atvalue;
			}
		}
		// ++++++++++++++
		// ++SetDomain+++
		// ++++++++++++++
		else if (dom instanceof SetDomain) {
			SetDomain sd = (SetDomain) dom;
			if (sd.get_basedomain() instanceof RecordDomain) {
				ArrayList<Object> list = new ArrayList<Object>();
				@SuppressWarnings("unchecked")
				List<Object> grList = (List<Object>) atvalue;
				for (Object o : grList) {
					list.add(this.getAttributeValueOf(o, sd.get_basedomain(),
							isBig));
				}
				return list;
			} else {
				// eob.eSet(eattr, atvalue);
				return new ArrayList<Object>(((Set<?>) atvalue));
			}
		}
		// ++++++++++++++
		// ++EnumDomain++
		// ++++++++++++++
		else if (dom instanceof EnumDomain) {
			EnumDomain domain = (EnumDomain) dom;

			EEnum eenum = this.enums.get(dom);
			EPackage pack = eenum.getEPackage();

			String literal = atvalue.toString();

			for (Comment com : domain.get_comments()) {
				if (com.get_text().startsWith(
						EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
								+ EAnnotationKeys.CHANGED_ENUM_LITERAL
								+ literal + " ")) {
					String oldLit = com.get_text().replace(
							EAnnotationKeys.ECORE_2_TG_METADATA_FLAG
									+ EAnnotationKeys.CHANGED_ENUM_LITERAL
									+ literal + " ", "");
					literal = oldLit;
					break;
				}
			}

			Object val = pack.getEFactoryInstance().createFromString(eenum,
					literal);
			return val;
		}

		// ++++++++++++++++++++++++++++++
		// ++String, Integer, Float ...++
		// ++++++++++++++++++++++++++++++
		else {
			if (isBig && (atvalue instanceof Long)) {
				return BigInteger.valueOf((Long) atvalue);
			}
			if (isBig && (atvalue instanceof Double)) {
				return BigDecimal.valueOf((Double) atvalue);
			}
			return atvalue;
		}
	}

	/**
	 * Returns the VertexClass with the given name
	 * 
	 * @param vcname
	 *            name of VertexClass
	 * @return found VertexClass, <code>null</code> if no VertexClass with that
	 *         name is found
	 */
	private VertexClass getVertexClassByName(String vcname) {
		for (VertexClass vc : this.schemagraph.getVertexClassVertices()) {
			if (vc.get_qualifiedName().equals(vcname)) {
				return vc;
			}
		}
		return null;
	}

	/**
	 * Returns the EdgeClass with the given name
	 * 
	 * @param ecname
	 *            name of EdgeClass
	 * @return found EdgeClass, <code>null</code> if no EdgeClass with that name
	 *         is found
	 */
	private EdgeClass getEdgeClassByName(String ecname) {
		for (EdgeClass ec : this.schemagraph.getEdgeClassVertices()) {
			if (ec.get_qualifiedName().equals(ecname)) {
				return ec;
			}
		}
		return null;
	}

	// #########################################################
	// ##### Ecore save methods ################################
	// #########################################################

	/**
	 * Saves an Ecore metamodel represented by an EPackage to a file
	 * 
	 * @param pack
	 *            EPackage that contains the metamodel
	 * */
	public void saveEcoreMetamodel(EPackage pack, String path) {
		URI filename = URI.createFileURI(path);
		XMIResource xmiResource = new XMIResourceImpl(filename);
		xmiResource.getContents().add(pack);
		try {
			xmiResource.save(null);
		} catch (IOException e) {
			System.err.println("Failed to save Ecore metamodel to " + filename);
			e.printStackTrace();
		}
	}

	/**
	 * Takes an Array of EObjects representing an Ecore model corresponding to
	 * the transformed metamodel and saves it to a given location
	 * 
	 * @param eobs
	 *            model
	 * @param path
	 *            to save place
	 */
	public void saveEcoreModel(ArrayList<EObject> eobs, String path) {
		if (path.substring(path.lastIndexOf(File.separator)).contains(".")) {
			this.saveEcoreModel(eobs, path, false);
		} else {
			this.saveEcoreModel(eobs, path, true);
		}
	}

	public void saveEcoreModel(ArrayList<EObject> eobs, String path,
			boolean addExtension) {
		ResourceSet resset = new ResourceSetImpl();
		resset.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put("xmi", new XMIResourceFactoryImpl());
		if (addExtension) {
			String extension = this.rootpackage.getName();
			extension = extension.substring(extension.lastIndexOf(".") + 1);
			resset.getResourceFactoryRegistry().getExtensionToFactoryMap()
					.put(extension, new XMIResourceFactoryImpl());
			path = path + "." + extension;
		}
		URI uri = URI.createFileURI(path);
		Resource res = resset.createResource(uri);
		if (this.config.isOption_makeGraphClassToRootElement()) {
			res.getContents().add(eobs.get(eobs.size() - 1));
		}

		for (EObject eob : eobs) {
			if (eob.eResource() != res) {
				res.getContents().add(eob);
			}
		}

		try {
			res.save(null);
		} catch (IOException e) {
			System.err.println("Failed to save Ecore model to " + path);
			e.printStackTrace();
		}
	}
}
