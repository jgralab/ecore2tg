package de.uni_koblenz.jgralabtest.utilities.ecore2tg;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2Tg;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration.TransformParams;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Tg2Ecore;
import de.uni_koblenz.jgralab.utilities.rsa.Rsa2Tg;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.SchemaGraph2Schema;

public class Tg2EcoreMainTest {

	public static void jamoppTest(String root, String modelfile)
			throws GraphIOException, IOException {
		Ecore2Tg ec2tg = new Ecore2Tg(root + ".ecore");
		ec2tg.getConfiguration().setTransformationOption(
				TransformParams.AUTOMATIC_TRANSFORMATION);
		ec2tg.getConfiguration().setConvertBigNumbers(true);
		ec2tg.transform("emftext.JamoppJavaModel");
		SchemaGraph sg = ec2tg.getSchemaGraph();
		SchemaGraph2Schema sg2s = new SchemaGraph2Schema();
		Schema s = sg2s.convert(sg);
		GraphIO.saveSchemaToFile(s, root + ".tg");

		String[] r = { modelfile + ".xmi" };
		Graph g = ec2tg.transformModel(r);
		GraphIO.saveGraphToFile(g, modelfile + ".tg", null);
		Tg2Dot.convertGraph(g, modelfile + ".dot", false);

		Tg2Ecore tg2ec = new Tg2Ecore(sg);
		tg2ec.getConfiguration().setOption_backToEcore(true);
		tg2ec.transform();

		EPackage epack = tg2ec.getTransformedMetamodel();
		tg2ec.saveEcoreMetamodel(epack, root + "_back.ecore");

		ArrayList<EObject> model = tg2ec.transformGraphToEcoreModel(g);
		tg2ec.saveEcoreModel(model, modelfile + "_back.xmi");
	}

	public static void javatest(String root, String modelfile)
			throws GraphIOException, IOException {
		String file_java_modisco = root + ".ecore";

		Ecore2Tg ec2tg = new Ecore2Tg(file_java_modisco);
		ec2tg.getConfiguration().setTransformationOption(
				TransformParams.AUTOMATIC_TRANSFORMATION);
		ec2tg.transform("org.modisco.ModiscoJavaModel");
		SchemaGraph sg = ec2tg.getSchemaGraph();
		SchemaGraph2Schema sg2s = new SchemaGraph2Schema();
		Schema s = sg2s.convert(sg);
		GraphIO.saveSchemaToFile(s, root + ".tg");

		String[] r = { modelfile + ".javaxmi" };
		Graph g = ec2tg.transformModel(r);
		GraphIO.saveGraphToFile(g, modelfile + ".tg", null);
		Tg2Dot.convertGraph(g, modelfile + ".dot", false);

		Tg2Ecore tg2ec = new Tg2Ecore(sg);
		tg2ec.getConfiguration().setOption_backToEcore(true);
		tg2ec.transform();

		EPackage epack = tg2ec.getTransformedMetamodel();
		tg2ec.saveEcoreMetamodel(epack, root + "_back.ecore");

	}

	public static void greqlTest() throws GraphIOException {
		String modelfile = "tests/GrequlTest/greqltestgraph.tg";
		String[] a = { "tests/GrequlTest/greqltestgraph_trans_model.rootpackage" };

		Schema schema = GraphIO.loadSchemaFromFile(modelfile);
		schema.compile(CodeGeneratorConfiguration.MINIMAL);
		Schema2SchemaGraph s2sg = new Schema2SchemaGraph();
		SchemaGraph sg = s2sg.convert2SchemaGraph(schema);

		Tg2Ecore tg2ec = new Tg2Ecore(sg);
		tg2ec.transform();
		tg2ec.saveEcoreMetamodel(tg2ec.getTransformedMetamodel(),
				"tests/GrequlTest/greqltestgraph_trans_metamodel.ecore");

		Graph graph = GraphIO.loadGraphFromFile(modelfile, null);
		ArrayList<EObject> model = tg2ec.transformGraphToEcoreModel(graph);
		tg2ec.saveEcoreModel(model,
				"tests/GrequlTest/greqltestgraph_trans_model");

		Ecore2Tg ec2tg = new Ecore2Tg(
				"tests/GrequlTest/greqltestgraph_trans_metamodel.ecore");
		// Check if loadable
		ec2tg.loadModelFromXMIFile(a);
		// end check if loadable

		ec2tg.transform("de.uni_koblenz.jgralabtest.schemas.greqltestschema.rootpackage"
				+ "." + "RouteSchema");
		SchemaGraph2Schema sg2s = new SchemaGraph2Schema();
		Schema schem_res = sg2s.convert(ec2tg.getSchemaGraph());
		GraphIO.saveSchemaToFile(schem_res,
				"tests/GrequlTest/greqltestgraph_trans_back.tg");
		// Tg2Dot.convertGraph(ec2tg.getSchemaGraph(),
		// "tests/GrequlTest/greqltestgraph_trans_back.dot", false);

		ec2tg.transformModel(a);
	}

	public static void modeltest() throws GraphIOException {
		// String schemafilename = "tests/UniversityModel/UniversityCourses.tg";
		// String modelfilename = "tests/UniversityModel/Model1/Example1.tg";
		String schemafilename = "tests/LibrarySystem/LibrarySystemOpt.tg";
		String modelfilename = "tests/LibrarySystem/Model1/Example1.tg";

		// String schemafilename =
		// "tests/ComplexNodeAndWay/ComplexWayAndNode.tg";
		// String modelfilename = "tests/ComplexNodeAndWay/Model2/Example1.tg";

		Schema schema = GraphIO.loadSchemaFromFile(schemafilename);
		schema.compile(CodeGeneratorConfiguration.MINIMAL);
		Schema2SchemaGraph s2sg = new Schema2SchemaGraph();
		SchemaGraph sg = s2sg.convert2SchemaGraph(schema);
		Tg2Ecore tg2ec = new Tg2Ecore(sg);
		tg2ec.getConfiguration().setOption_backToEcore(true);
		tg2ec.transform();
		tg2ec.saveEcoreMetamodel(tg2ec.getTransformedMetamodel(),
		// "tests/ComplexNodeAndWay/ComplexWayAndNode_back.ecore");
		// "tests/UniversityModel/UniversityCourses_back.ecore");
				"tests/LibrarySystem/LibrarySystemOpt_back.ecore");
		Graph g = GraphIO.loadGraphFromFile(modelfilename, schema,
				ImplementationType.STANDARD, null);
		ArrayList<EObject> eobs = tg2ec.transformGraphToEcoreModel(g);
		// tg2ec.saveEcoreModel(eobs,
		// "tests/LibrarySystem/Model1/Example1_back");
		tg2ec.saveEcoreModel(eobs,
		// "tests/ComplexNodeAndWay/Model2/Example1_back");
				"tests/LibrarySystem/Model1/Example1_back");
		// tg2ec
		// .saveEcoreModel(eobs,
		// "tests/UniversityModel/Model1/Example1_back");

	}

	public static void main(String[] args) {
		try {

			/*
			 * String[] a = { "tests/tastest/target_model.tg",
			 * "tests/tastest/StateMachine_back.ecore",
			 * "tests/tastest/trans_model" }; Tg2Ecore.main(a);
			 */

			// modeltest();
			// greqlTest();
			// Ecore2TgMainTest.main_UniversityCourses(false);
			// jamoppTest("tests/jamoppTest/java", "tests/jamoppTest/Test");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			javatest("tests/java_models/modisco_first_test_metamodel",
					"tests/java_models/modisco_class_with_method");
			javatest("tests/java_models/modisco_first_test_metamodel",
					"tests/java_models/modisco_first_test_model");
			javatest("tests/java_models/modisco_first_test_metamodel",
					"tests/java_models/modisco_empty_class_model");
			javatest("tests/java_models/modisco_first_test_metamodel",
					"tests/java_models/modisco_class_with_member_model");
			javatest("tests/java_models/modisco_first_test_metamodel",
					"tests/java_models/modisco_class_with_parent_and_package");
			javatest("tests/java_models/modisco_first_test_metamodel",
					"tests/java_models/modisco_class_with_field_and_getter");

			Rsa2Tg rsa2tg = new Rsa2Tg();
			rsa2tg.setUseFromRole(true);
			rsa2tg.setUseNavigability(true);
			rsa2tg.setFilenameDot("tests/TGBack/Maps.dot");
			rsa2tg.setFilenameSchema("tests/TGBack/Maps.tg");
			rsa2tg.process("tests/TGBack/Blank Package.xmi");

			Schema schema = GraphIO //
					// .loadSchemaFromFile("tests/LibrarySystem/LibrarySystem.tg"
					// ); //
					.loadSchemaFromFile("tests/TGBack/Maps.tg");
			// "tests/SimpleNodeAndWay/SimpleNodeAndWay.tg"); //
			// .loadSchemaFromFile( //
			// "tests/UniversityModel/UniversityCourses.tg");
			// .loadSchemaFromFile("tests/UserManagement/UserManagement.tg");
			// .
			// loadSchemaFromFile("tests/SimpleNodeAndWay/SimpleNodeAndWay.tg");
			Schema2SchemaGraph s2sg = new Schema2SchemaGraph();
			SchemaGraph sg = s2sg.convert2SchemaGraph(schema);
			Tg2Ecore tg2ecore = new Tg2Ecore(sg);
			tg2ecore.transform();
			tg2ecore.saveEcoreMetamodel(tg2ecore.getTransformedMetamodel(),
					"tests/TGBack/Maps.ecore");
			// "tests/UniversityModel/UniversityCourses_back.ecore");
			// "tests/UserManagement/UserManagement_back.ecore"); //
			// "tests/SimpleNodeAndWay/SimpleNodeAndWay_back.ecore");
			Ecore2Tg e2tg = new Ecore2Tg("tests/TGBack/Maps.ecore");
			e2tg.transform("maps.Maps");
			SchemaGraph sg_back = e2tg.getSchemaGraph();
			SchemaGraph2Schema sg2s = new SchemaGraph2Schema();
			Schema schema_back = sg2s.convert(sg_back);
			GraphIO.saveSchemaToFile(schema_back, "tests/TGBack/Maps_back.tg");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
