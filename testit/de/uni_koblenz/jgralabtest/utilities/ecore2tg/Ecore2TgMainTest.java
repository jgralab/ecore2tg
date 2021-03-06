package de.uni_koblenz.jgralabtest.utilities.ecore2tg;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2Tg;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration.TransformParams;
import de.uni_koblenz.jgralab.utilities.rsa2tg.SchemaGraph2XMI;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.SchemaGraph2Schema;

public class Ecore2TgMainTest {

	public static void main(String[] args) {
		mainTestEdgeClassInheritance();
		/*
		 * String[] args2 = { "-i",
		 * "../../freshEcore2Tg/ecore2tg/tests/ComplexNodeAndWay/ComplexWayAndNode.ecore"
		 * , "-o",
		 * "../../freshEcore2Tg/ecore2tg/tests/ComplexNodeAndWay/ComplexWayAndNode.tg"
		 * };
		 * 
		 * try { Ecore2Tg.main(args2); } catch (IOException e) {
		 * e.printStackTrace(); } catch (GraphIOException e) {
		 * e.printStackTrace(); }
		 */
		boolean options = false;
		try {
			System.out.println("+++++++++++++++++\nLibrarySystem");
			main_LibrarySystem(options);

			System.out.println("+++++++++++++++++\nUniversityModel");
			main_UniversityCourses(options);

			System.out.println("+++++++++++++++++\nSimpleNodeAndWay");
			main_SimpleNodeAndWay(options);

			System.out.println("+++++++++++++++++\nComplexNodeAndWay");
			main_ComplexNodeAndWay(options);

			System.out.println("+++++++++++++++++\nUserManagement");
			main_UserManagement(options);

			System.out.println("+++++++++++++++++\nCatsWorld");
			main_CatsWorld(options);
			System.out.println("+++++++++++++++++\nSpecialCases");
			main_SpecialCases(options);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.gc();

		options = true;
		try {
			System.out.println("+++++++++++++++++\nTassilo");

			main_tassilo();

			System.out.println("+++++++++++++++++\nLibrarySystem");
			main_LibrarySystem(options);

			System.out.println("+++++++++++++++++\nUniversityModel");
			main_UniversityCourses(options);

			System.out.println("+++++++++++++++++\nSimpleNodeAndWay");
			main_SimpleNodeAndWay(options);

			System.out.println("+++++++++++++++++\nComplexNodeAndWay");
			main_ComplexNodeAndWay(options);

			System.out.println("+++++++++++++++++\nUserManagement");
			main_UserManagement(options);

			System.out.println("+++++++++++++++++\nCatsWorld");
			main_CatsWorld(options);

			System.out.println("+++++++++++++++++\nSpecialCases");
			main_SpecialCases(options);

			// System.gc();
			// mainTestMoreSpecialCases();
			// System.gc();
			// mainTestMoreSpecialCases2();
			// System.gc();
			// mainTestMoreSpecialCases5();
			System.out.println("+++++++++++++++++\nOverwriteTest");
			mainTestUserDefinesOverwrites();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Test for files from the Internet
		// String basefile =
		// "../../freshEcore2Tg/ecore2tg/tests/InetFileTest/di";

		// String basefile =
		// "../../freshEcore2Tg/ecore2tg/tests/InetFileTest/Grafcet_goodNs";
		// String[]p =
		// {"../../freshEcore2Tg/ecore2tg/tests/InetFileTest/Grafcet2PetriNet_model_goodNs.ecore"};

		String basefile = "../../freshEcore2Tg/ecore2tg/tests/InetFileTest/Grafcet";
		String[] p = { "../../freshEcore2Tg/ecore2tg/tests/InetFileTest/example.xmi" };

		// String basefile =
		// "../../freshEcore2Tg/ecore2tg/tests/InetFileTest/IEEE1471ConceptualModel";
		// String[]p =
		// {"../../freshEcore2Tg/ecore2tg/tests/InetFileTest/IEEE1471ConceptualModel_model.ecore"};
		simpleTrans(basefile, p, "org.Grafcet");
	}

	public static void simpleTrans(String basefile, String[] p, String schemName) {
		String ecorefile = basefile + ".ecore";
		Ecore2Tg test = new Ecore2Tg(ecorefile);
		Ecore2TgConfiguration conf = test.getConfiguration();
		conf.setTransformationOption(TransformParams.AUTOMATIC_TRANSFORMATION);
		test.transform(schemName);
		SchemaGraph schemaGraph = test.getSchemaGraph();
		produceOutput(schemaGraph, basefile);
		if (p != null) {
			Graph mg = test.transformModel(p);
			try {
				Tg2Dot.convertGraph(mg, basefile + "_model.dot", false);

				GraphIO.saveGraphToFile(mg, basefile + "_model.tg", null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void mainTestUserDefinesOverwrites() {
		String basefile = "../../freshEcore2Tg/ecore2tg/tests/TestUserDefinesOverwrites/TestUserDefinesOverwritten";
		String ecorefile = basefile + ".ecore";

		// Transformation
		Ecore2Tg test = new Ecore2Tg(ecorefile);
		Ecore2TgConfiguration conf = test.getConfiguration();
		// Options

		conf.getPairsOfOverwritingEReferences().put(
				"testuserdefinesoverwritten.V3.incoming3",
				"testuserdefinesoverwritten.V2.incoming2");
		conf.getPairsOfOverwritingEReferences().put(
				"testuserdefinesoverwritten.V2.incoming2",
				"testuserdefinesoverwritten.V1.incoming1");
		conf.getPairsOfOverwritingEReferences().put(
				"testuserdefinesoverwritten.V1.incoming1",
				"testuserdefinesoverwritten.V.incoming");

		conf.getPairsOfOverwritingEReferences().put(
				"testuserdefinesoverwritten.E3.end3",
				"testuserdefinesoverwritten.E2.end2");
		conf.getPairsOfOverwritingEReferences().put(
				"testuserdefinesoverwritten.E2.end2",
				"testuserdefinesoverwritten.E1.end1");
		conf.getPairsOfOverwritingEReferences().put(
				"testuserdefinesoverwritten.E1.end1",
				"testuserdefinesoverwritten.E.end");

		conf.getEdgeClassesList().add("testuserdefinesoverwritten.E");

		basefile += "Opt";

		// Start Transformation
		conf.setTransformationOption(TransformParams.AUTOMATIC_TRANSFORMATION);
		test.transform("test.OverwrittenRefs");
		SchemaGraph schemaGraph = test.getSchemaGraph();
		produceOutput(schemaGraph, basefile);
	}

	public static void mainTestEdgeClassInheritance() {
		String basefile = "../../freshEcore2Tg/ecore2tg/tests/TestMultipleEdgeClassInheritance/TestMultipleInheritanceEdgeClasses";
		String ecorefile = basefile + ".ecore";

		// Transformation
		Ecore2Tg test = new Ecore2Tg(ecorefile);
		Ecore2TgConfiguration conf = test.getConfiguration();
		// Options

		conf.getEdgeClassesList().add(
				"testmultipleinheritanceedgeclasses.EdgeAB");
		conf.getEdgeClassesList().add(
				"testmultipleinheritanceedgeclasses.EdgeCD");
		conf.getEdgeClassesList().add(
				"testmultipleinheritanceedgeclasses.EdgeKL");
		conf.getEdgeClassesList().add(
				"testmultipleinheritanceedgeclasses.BetterEdgeAB");
		conf.getEdgeClassesList().add(
				"testmultipleinheritanceedgeclasses.BetterEdgeKL");

		// test.getDirectionFromStartEClassMap().put(
		// "testmultipleinheritanceedgeclasses.EdgeAB",
		// "testmultipleinheritanceedgeclasses.A");

		basefile += "Opt";

		// Start Transformation
		conf.setTransformationOption(TransformParams.AUTOMATIC_TRANSFORMATION);

		test.transform("test.MultipleInheritance");
		SchemaGraph schemaGraph = test.getSchemaGraph();
		produceOutput(schemaGraph, basefile);
	}

	public static void mainTestMoreSpecialCases() {
		String basefile = "../../freshEcore2Tg/ecore2tg/tests/MoreSpecialCases/Malbuch";
		String ecorefile = basefile + ".ecore";

		// Transformation
		Ecore2Tg test = new Ecore2Tg(ecorefile);
		Ecore2TgConfiguration conf = test.getConfiguration();

		// Options

		// test.getEdgeClassesList().add("malbuch.Edge");
		// test.getEdgeClassesList().add("malbuch.EdgeCE");
		conf.setAsGraphClass("malbuch.G");
		basefile += "Opt";

		// Start Transformation
		conf.setTransformationOption(TransformParams.AUTOMATIC_TRANSFORMATION);
		test.transform("test.Malbuch");
		SchemaGraph schemaGraph = test.getSchemaGraph();
		produceOutput(schemaGraph, basefile);
	}

	public static void mainTestMoreSpecialCases2() {
		String basefile = "../../freshEcore2Tg/ecore2tg/tests/MoreSpecialCases/Malbuch2";
		String ecorefile = basefile + ".ecore";

		// Transformation
		Ecore2Tg test = new Ecore2Tg(ecorefile);
		Ecore2TgConfiguration conf = test.getConfiguration();
		// Options

		// test.getEdgeClassesList().add("malbuch2.E3");
		// test.getEdgeClassesList().add("malbuch2.E2");

		// basefile += "Opt";

		// Start Transformation
		conf.setTransformationOption(TransformParams.AUTOMATIC_TRANSFORMATION);
		test.transform("test.Malbuch2");
		SchemaGraph schemaGraph = test.getSchemaGraph();
		produceOutput(schemaGraph, basefile);
	}

	public static void mainTestMoreSpecialCases5() {
		String basefile = "../../freshEcore2Tg/ecore2tg/tests/MoreSpecialCases/Malbuch5";
		String ecorefile = basefile + ".ecore";

		// Transformation
		Ecore2Tg test = new Ecore2Tg(ecorefile);
		Ecore2TgConfiguration conf = test.getConfiguration();

		// Options

		// test.getEdgeClassesList().add("malbuch5.Edge");

		basefile += "Opt";

		// Start Transformation
		conf.setTransformationOption(TransformParams.AUTOMATIC_TRANSFORMATION);
		test.transform("test.Malbuch5");
		SchemaGraph schemaGraph = test.getSchemaGraph();
		produceOutput(schemaGraph, basefile);
	}

	public static void main_UserManagement(boolean options) throws IOException {

		String basefile = "../../freshEcore2Tg/ecore2tg/tests/UserManagement/UserManagement";
		String ecorefile = basefile + ".ecore";
		String modelBasefile = "../../freshEcore2Tg/ecore2tg/tests/UserManagement/Model1/Example1";
		String modelBasefileOut = modelBasefile;

		// Transformation
		Ecore2Tg test = new Ecore2Tg(ecorefile);
		Ecore2TgConfiguration conf = test.getConfiguration();

		// Options
		if (options) {
			conf.getEdgeClassesList().add("usermanagement.EntranceIntoGroup");

			conf.getNamesOfEdgeClassesMap().put(
					"usermanagement.Society.publishedPaper",
					"usermanagement.Publishes");
			conf.getNamesOfEdgeClassesMap()
					.put("usermanagement.User.writtenPaper",
							"usermanagement.Writes");

			basefile += "Opt";
			modelBasefileOut += "Opt";
		}

		// Start Transformation
		conf.setTransformationOption(TransformParams.AUTOMATIC_TRANSFORMATION);
		test.transform("test.Usermanagement");
		SchemaGraph schemaGraph = test.getSchemaGraph();
		produceOutput(schemaGraph, basefile);

		String paths[] = { modelBasefile + ".usermanagement" };
		Graph graph = test.transformModel(paths);
		String dotfile = modelBasefileOut + ".dot";
		Tg2Dot.convertGraph(graph, dotfile, false);

	}

	public static void main_ComplexNodeAndWay(boolean options)
			throws GraphIOException, IOException {

		String basefile = "../../freshEcore2Tg/ecore2tg/tests/ComplexNodeAndWay/ComplexWayAndNode";
		String ecorefile = basefile + ".ecore";

		// Transformation Object
		Ecore2Tg test = new Ecore2Tg(ecorefile);
		Ecore2TgConfiguration conf = test.getConfiguration();

		// Options
		if (options) {
			// test.getEdgeClassesList().add("complexwayandnode.structure.Way");
			conf.getDirectionMap().put(
					"complexwayandnode.structure.Node.outgoingWays",
					Ecore2TgConfiguration.TO);

			conf.getDefinedPackagesOfEdgeClassesMap().put(
					"complexwayandnode.carduse.Section.shownnodes",
					"complexwayandnode.structure");

			basefile += "Opt";

		}

		// Start Transformation
		conf.setTransformationOption(TransformParams.AUTOMATIC_TRANSFORMATION);
		test.transform("test.ComplexNodeAndWay");
		SchemaGraph schemaGraph = test.getSchemaGraph();
		produceOutput(schemaGraph, basefile);

		String[] a = new String[11];

		a[0] = "../../freshEcore2Tg/ecore2tg/tests/ComplexNodeAndWay/Model2/Bu1.structure";
		a[1] = "../../freshEcore2Tg/ecore2tg/tests/ComplexNodeAndWay/Model2/Bu2.structure";
		a[2] = "../../freshEcore2Tg/ecore2tg/tests/ComplexNodeAndWay/Model2/CR2.structure";
		a[3] = "../../freshEcore2Tg/ecore2tg/tests/ComplexNodeAndWay/Model2/HT1.structure";
		a[4] = "../../freshEcore2Tg/ecore2tg/tests/ComplexNodeAndWay/Model2/Int.structure";
		a[5] = "../../freshEcore2Tg/ecore2tg/tests/ComplexNodeAndWay/Model2/My.structure";
		a[6] = "../../freshEcore2Tg/ecore2tg/tests/ComplexNodeAndWay/Model2/Bu3.structure";
		a[7] = "../../freshEcore2Tg/ecore2tg/tests/ComplexNodeAndWay/Model2/CR3.structure";
		a[8] = "../../freshEcore2Tg/ecore2tg/tests/ComplexNodeAndWay/Model2/CR4.structure";
		a[9] = "../../freshEcore2Tg/ecore2tg/tests/ComplexNodeAndWay/Model2/CR5.structure";
		a[10] = "../../freshEcore2Tg/ecore2tg/tests/ComplexNodeAndWay/Model2/Int2.structure";

		Graph g = test.transformModel(a);
		String dotfile = "../../freshEcore2Tg/ecore2tg/tests/ComplexNodeAndWay/Model2/Example1.dot";
		Tg2Dot.convertGraph(g, dotfile, false);
		GraphIO.saveGraphToFile(
				g,
				"../../freshEcore2Tg/ecore2tg/tests/ComplexNodeAndWay/Model2/Example1.tg",
				null);
	}

	public static void main_SimpleNodeAndWay(boolean options)
			throws IOException {

		String basefile = "../../freshEcore2Tg/ecore2tg/tests/SimpleNodeAndWay/SimpleNodeAndWay";
		String ecorefile = basefile + ".ecore";
		String modelBasefile = "../../freshEcore2Tg/ecore2tg/tests/SimpleNodeAndWay/Model1/Example1";
		String modelBasefileOut = modelBasefile;

		// Transformation Object
		Ecore2Tg test = new Ecore2Tg(ecorefile);
		Ecore2TgConfiguration conf = test.getConfiguration();

		// Options
		if (options) {
			conf.getEdgeClassesList().add("simplenodeandway.Way");
			conf.getDirectionMap().put("simplenodeandway.Node.incomingWays",
					Ecore2TgConfiguration.TO);

			basefile += "Opt";
			modelBasefileOut += "Opt";
		}

		// Start Transformation
		conf.setTransformationOption(TransformParams.AUTOMATIC_TRANSFORMATION);
		test.transform("tests.test.SimpleNodeAndWay");
		SchemaGraph schemaGraph = test.getSchemaGraph();
		produceOutput(schemaGraph, basefile);

		String paths[] = { modelBasefile + ".simplenodeandway" };
		Graph graph = test.transformModel(paths);
		String dotfile = modelBasefileOut + ".dot";
		Tg2Dot.convertGraph(graph, dotfile, false);
	}

	public static void main_SpecialCases(boolean options) {

		String basefile = "../../freshEcore2Tg/ecore2tg/tests/SpecialCases/SpecialCases";
		String ecorefile = basefile + ".ecore";

		// Transformation Object
		Ecore2Tg test = new Ecore2Tg(ecorefile);
		Ecore2TgConfiguration conf = test.getConfiguration();

		// Options
		if (options) {
			conf.getEdgeClassesList().add("SpecialCases.EdgeAB");
			conf.getEdgeClassesList().add("SpecialCases.EdgeCD");
			conf.getEdgeClassesList().add("SpecialCases.EdgeEF");
			conf.getEdgeClassesList().add("SpecialCases.EdgeGH");
			conf.getEdgeClassesList().add("SpecialCases.EdgeIJ");
			conf.getEdgeClassesList().add("SpecialCases.EdgeKL");

			basefile += "Opt";
		}

		// Start Transformation
		conf.setTransformationOption(TransformParams.AUTOMATIC_TRANSFORMATION);
		test.transform("test.SpecialCases");
		SchemaGraph schemaGraph = test.getSchemaGraph();
		produceOutput(schemaGraph, basefile);
	}

	public static void main_LibrarySystem(boolean options)
			throws GraphIOException, IOException {

		String basefile = "../../freshEcore2Tg/ecore2tg/tests/LibrarySystem/LibrarySystem";
		String ecorefile = basefile + ".ecore";
		String modelBasefile = "../../freshEcore2Tg/ecore2tg/tests/LibrarySystem/Model1/Example1";
		String modelBasefileOut = modelBasefile;

		// Transformation Object
		Ecore2Tg test = new Ecore2Tg(ecorefile);
		Ecore2TgConfiguration conf = test.getConfiguration();

		// Options
		if (options) {
			conf.setAggregationInfluenceOnDirection(Ecore2TgConfiguration.DIRECTION_PART_TO_WHOLE);
			conf.getDirectionMap().put("librarysystem.Library.item",
					Ecore2TgConfiguration.TO);

			basefile += "Opt";
			modelBasefileOut += "Opt";
		}

		// Start Transformation
		conf.setTransformationOption(TransformParams.JUST_LIKE_ECORE);
		test.transform("test.LibrarySystem");
		SchemaGraph schemaGraph = test.getSchemaGraph();
		produceOutput(schemaGraph, basefile);

		String[] paths = { modelBasefile + ".librarysystem" };
		Graph graph = test.transformModel(paths);
		String dotfile = modelBasefileOut + ".dot";
		Tg2Dot.convertGraph(graph, dotfile, false);
		GraphIO.saveGraphToFile(graph, modelBasefile + ".tg", null);

	}

	public static void main_UniversityCourses(boolean options)
			throws GraphIOException, IOException {

		String basefile = "../../freshEcore2Tg/ecore2tg/tests/UniversityModel/UniversityCourses";
		String ecorefile = basefile + ".ecore";
		String modelBasefile = basefile.substring(0, basefile.lastIndexOf("/"))
				+ "/Model1/Example1";
		String modelBasefileOut = modelBasefile;
		String modelBasefile2 = basefile
				.substring(0, basefile.lastIndexOf("/")) + "/Model2/Example2";
		String modelBasefileOut2 = modelBasefile2;

		// Transformation Object
		Ecore2Tg test = new Ecore2Tg(ecorefile);
		Ecore2TgConfiguration conf = test.getConfiguration();

		// Options
		if (options) {
			conf.getNamesOfEdgeClassesMap().put(
					"universitycourses.work.Tutorial.belongingLecture",
					"universitycourses.work.BelongsToLecture");
			conf.getNamesOfEdgeClassesMap().put(
					"universitycourses.people.Assistant.tutorial",
					"universitycourses.HoldsTutorium");

			conf.getDefinedPackagesOfEdgeClassesMap().put(
					"universitycourses.people.Professor.lecture",
					"universitycourses");

			conf.getDirectionMap().put(
					"universitycourses.work.Lecture.lecturer",
					Ecore2TgConfiguration.TO);
			conf.getDirectionMap().put("universitycourses.work.Course.visitor",
					Ecore2TgConfiguration.TO);
			conf.setAsGraphClass("universitycourses.UniversityCoursesModel");
			conf.saveConfigurationToFile("../../freshEcore2Tg/ecore2tg/tests/UniversityModel/savedconfig.plist");

			basefile += "Opt";
			modelBasefileOut += "Opt";
			modelBasefileOut2 += "Opt";
		} else {
			test.setConfiguration(Ecore2TgConfiguration
					.loadConfigurationFromFile("../../freshEcore2Tg/ecore2tg/tests/UniversityModel/savedconfig.plist")); // PListTest.plist"));
			conf = test.getConfiguration();
		}

		// Start Transformation
		conf.setTransformationOption(TransformParams.JUST_LIKE_ECORE);
		test.transform("test.University");
		SchemaGraph schemaGraph = test.getSchemaGraph();
		produceOutput(schemaGraph, basefile);

		String[] paths = { modelBasefile + ".universitycourses" };
		Graph g = test.transformModel(paths);
		GraphIO.saveGraphToFile(g, modelBasefile + ".tg", null);
		Tg2Dot.convertGraph(g, modelBasefileOut + ".dot", false);

		String[] paths2 = { modelBasefile2 + ".universitycourses" };
		Graph g2 = test.transformModel(paths2);
		GraphIO.saveGraphToFile(g2, modelBasefile2 + ".tg", null);
		Tg2Dot.convertGraph(g2, modelBasefileOut2 + ".dot", false);
	}

	public static void main_CatsWorld(boolean options) {

		String basefile = "../../freshEcore2Tg/ecore2tg/tests/CatsWorld/CatWorld";
		String ecorefile = basefile + ".ecore";

		// Transformation Object
		Ecore2Tg test = new Ecore2Tg(ecorefile);
		Ecore2TgConfiguration conf = test.getConfiguration();

		// Options
		if (options) {
			conf.setAsGraphClass("catworld.CatsWorld");
			conf.getNamesOfEdgeClassesMap().put("catworld.cats.Cat.food",
					"catworld.cats.Eats");
			conf.getNamesOfEdgeClassesMap().put(
					"catworld.otherLivingBeings.TinOpener.catToOpenTinFor",
					"catworld.otherLivingBeings.OpensTinFor");
			conf.getNamesOfEdgeClassesMap().put(
					"catworld.cats.Cat.sleepingPlace", "catworld.cats.SleepAt");
			basefile += "Opt";
		}

		// Start Transformation
		conf.setTransformationOption(TransformParams.AUTOMATIC_TRANSFORMATION);
		test.transform("test.Catworld");
		SchemaGraph schemaGraph = test.getSchemaGraph();
		produceOutput(schemaGraph, basefile);
	}

	public static void main_tassilo() throws IOException {
		String basefile = "../../freshEcore2Tg/ecore2tg/tests/TassiloModel/original_minimal_metamodel";
		// String basefile =
		// "../../freshEcore2Tg/ecore2tg/tests/TassiloModel/evolved_minimal_metamodel";
		// String basefile =
		// "../../freshEcore2Tg/ecore2tg/tests/TassiloModel/evolved_metamodel";
		String ecorefile = basefile + ".ecore";
		Ecore2Tg test = new Ecore2Tg(ecorefile);
		test.getConfiguration().setTransformationOption(
				TransformParams.AUTOMATIC_TRANSFORMATION);
		test.transform("test.UMLstatemodel");
		String a[] = { "../../freshEcore2Tg/ecore2tg/tests/TassiloModel/original_modelNeu.xmi" };
		// String a[] =
		// {"../../freshEcore2Tg/ecore2tg/tests/TassiloModel/sample_minimal_migrated_model.xmi"};
		// String a[] =
		// {"../../freshEcore2Tg/ecore2tg/tests/TassiloModel/sample_migrated_model.xmi"};
		Graph mg = test.transformModel(a);
		produceOutput(test.getSchemaGraph(), basefile);
		try {
			GraphIO.saveGraphToFile(
					mg,
					"../../freshEcore2Tg/ecore2tg/tests/TassiloModel/original_modelNeu.tg",
					null);
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Tg2Dot.convertGraph(
				mg,
				"../../freshEcore2Tg/ecore2tg/tests/TassiloModel/original_modelNeu.dot",
				false);
	}

	public static void produceOutput(SchemaGraph schemaGraph, String basefile) {

		// Saving
		try {

			// Save as SchemaGraph Picture
			String dotfile = basefile + ".dot";
			Tg2Dot.convertGraph(schemaGraph, dotfile, false);

			// Save as tg-Schema
			String tgfile = basefile + ".tg";
			SchemaGraph2Schema transsg2s = new SchemaGraph2Schema();
			Schema schem = transsg2s.convert(schemaGraph);
			GraphIO.saveSchemaToFile(schem, tgfile);

			// Export to XMI
			String xmifile = basefile + ".xmi";
			SchemaGraph2XMI sg2xmi = new SchemaGraph2XMI();
			sg2xmi.process(schemaGraph, xmifile);

		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GraphIOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
