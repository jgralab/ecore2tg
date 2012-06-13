package de.uni_koblenz.jgralabtest.utilities.ecore2tg;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;

import org.eclipse.emf.ecore.EObject;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2Tg;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Tg2Ecore;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration.TransformParams;
import de.uni_koblenz.jgralab.utilities.rsa.SchemaGraph2XMI;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.SchemaGraph2Schema;

public class RoundtripTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			testEcoreTgEcore();
			testTgEcoreTg();
		} catch (GraphIOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void testTgEcoreTg() throws GraphIOException, IOException {

		System.out.println("#########################");
		System.out.println("Testcase: ExtendedLibrary");

		String graph_filename = "../../freshEcore2Tg/ecore2tg/tests/Tg2EcoreRoundTest/BibSeckGraph.tg";
		String metamodel_filename = "../../freshEcore2Tg/ecore2tg/tests/Tg2EcoreRoundTest/BibSeck_metamodel.ecore";
		String model_filename = "../../freshEcore2Tg/ecore2tg/tests/Tg2EcoreRoundTest/BibSeck";
		String graph_back_filename = "../../freshEcore2Tg/ecore2tg/tests/Tg2EcoreRoundTest/BibSeck_back.tg";
		String dot_filename = "../../freshEcore2Tg/ecore2tg/tests/Tg2EcoreRoundTest/BibSeck.dot";

		// --------------------------------------------
		// --- Tg -> Ecore ----------------------------
		// --------------------------------------------

		System.out.println("\nTg -> Ecore");

		Schema schema = GraphIO.loadSchemaFromFile(graph_filename);
		schema.compile(CodeGeneratorConfiguration.MINIMAL);
		schema.commit("../../GeneratedTest/",
				CodeGeneratorConfiguration.MINIMAL);
		Schema2SchemaGraph s2sg = new Schema2SchemaGraph();
		SchemaGraph sg = s2sg.convert2SchemaGraph(schema);

		SchemaGraph2XMI sg2xmi = new SchemaGraph2XMI();
		try {
			sg2xmi.process(sg,
					"../../freshEcore2Tg/ecore2tg/tests/Tg2EcoreRoundTest/BibSeckSchema.xmi");
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Tg2Ecore tg2ec = new Tg2Ecore(sg);
		tg2ec.fillWithConfigurationsFromFile("../../freshEcore2Tg/ecore2tg/tests/Tg2EcoreRoundTest/config.plist");
		tg2ec.transform();
		tg2ec.saveEcoreMetamodel(tg2ec.getTransformedMetamodel(),
				metamodel_filename);

		Graph g = GraphIO.loadGraphFromFile(graph_filename, null);
		Tg2Dot.convertGraph(g, dot_filename);

		ArrayList<EObject> model = tg2ec.transformGraphToEcoreModel(g);
		tg2ec.saveEcoreModel(model, model_filename);

		// --------------------------------------------
		// --- Ecore -> Tg ----------------------------
		// --------------------------------------------

		System.out.println("\nEcore -> Tg");

		Ecore2Tg ec2tg = new Ecore2Tg(metamodel_filename);
		ec2tg.getConfiguration().setTransformationOption(
				TransformParams.AUTOMATIC_TRANSFORMATION);
		ec2tg.transform("org.testschema.ExtendedLibrary");
		SchemaGraph2Schema sg2s = new SchemaGraph2Schema();
		GraphIO.saveSchemaToFile(sg2s.convert(ec2tg.getSchemaGraph()),
				graph_back_filename);

		Graph gr = ec2tg.transformModel(new String[] { model_filename + "."
				+ tg2ec.getOption_rootpackageName() });
		GraphIO.saveGraphToFile(gr, graph_back_filename, null);

		sg2xmi = new SchemaGraph2XMI();
		try {
			sg2xmi.process(s2sg.convert2SchemaGraph(sg2s.convert(ec2tg
					.getSchemaGraph())),
					"../../freshEcore2Tg/ecore2tg/tests/Tg2EcoreRoundTest/BibSeckSchema_back.xmi");
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

		System.out.println();
	}

	public static void testEcoreTgEcore() throws GraphIOException, IOException {
		System.out.println("############################");
		System.out.println("Testcase: ExtendedUniversity");

		// Filenames
		String metamodel_filename = "../../freshEcore2Tg/ecore2tg/tests/UniversityExtended/UniversityExtended.ecore";
		String[] model_filename = { "../../freshEcore2Tg/ecore2tg/tests/UniversityExtended/Model01.universityextended" };
		String graph_filename = "../../freshEcore2Tg/ecore2tg/tests/UniversityExtended/UniversityExtended_withModel.tg";
		String graph_dot_filename = "../../freshEcore2Tg/ecore2tg/tests/UniversityExtended/UniversityExtended.dot";
		String metamodel_back_filename = "../../freshEcore2Tg/ecore2tg/tests/UniversityExtended/UniversityExtended_back.ecore";
		String model_back_filename = "../../freshEcore2Tg/ecore2tg/tests/UniversityExtended/Model01_back";

		// --------------------------------------------
		// --- Ecore -> Tg ----------------------------
		// --------------------------------------------

		System.out.println("\nEcore -> Tg");

		Ecore2Tg ec2tg = new Ecore2Tg(metamodel_filename);
		ec2tg.getConfiguration().setTransformationOption(
				TransformParams.AUTOMATIC_TRANSFORMATION);
		ec2tg.transform("org.test.ExtendedUniversity");
		SchemaGraph2Schema sg2s_2 = new SchemaGraph2Schema();
		GraphIO.saveSchemaToFile(sg2s_2.convert(ec2tg.getSchemaGraph()),
				graph_filename);
		Graph graph = ec2tg.transformModel(model_filename);
		GraphIO.saveGraphToFile(graph, graph_filename, null);
		Tg2Dot.convertGraph(graph, graph_dot_filename);

		// --------------------------------------------
		// --- Tg -> Ecore ----------------------------
		// --------------------------------------------

		System.out.println("\nTg -> Ecore");

		Schema schema = GraphIO.loadSchemaFromFile(graph_filename);
		Schema2SchemaGraph s2sg = new Schema2SchemaGraph();
		SchemaGraph schemagraph = s2sg.convert2SchemaGraph(schema);

		Tg2Ecore tg2ec = new Tg2Ecore(schemagraph);
		tg2ec.setOption_backToEcore(true);
		tg2ec.transform();
		tg2ec.saveEcoreMetamodel(tg2ec.getTransformedMetamodel(),
				metamodel_back_filename);

		Graph g = GraphIO.loadGraphFromFile(graph_filename, null);
		ArrayList<EObject> model = tg2ec.transformGraphToEcoreModel(g);
		tg2ec.saveEcoreModel(model, model_back_filename);

		System.out.println();
	}

}
