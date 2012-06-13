package de.uni_koblenz.jgralabtest.utilities.ecore2tg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.emf.ecore.EObject;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2Tg;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration.TransformParams;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Tg2Ecore;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralab.utilities.tg2dot.dot.GraphVizOutputFormat;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.SchemaGraph2Schema;

public class RoundtripTest {

	public static final String testfolder = "testit" + File.separator
			+ "testdata" + File.separator;

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

		String folder = testfolder + "tg2ecore2tg_library" + File.separator;
		String genfolder = folder + "gen" + File.separator;
		// IN
		String graph_filename = folder + "library_seck_graph.tg";
		String config_filename = folder + "config.plist";

		// OUT Tg2Ecore
		String metamodel_filename = genfolder + "library_metamodel.ecore";
		String model_filename = genfolder + "library_seck_model";

		// OUT Ecore2TG
		String graph_back_filename = genfolder + "library_seck_graph_back.tg";
		String graphPicture_filename = genfolder + "libary_seck_graph_back.png";

		// --------------------------------------------
		// --- Tg -> Ecore ----------------------------
		// --------------------------------------------

		System.out.println("\nTg -> Ecore");

		Schema schema = GraphIO.loadSchemaFromFile(graph_filename);

		Schema2SchemaGraph s2sg = new Schema2SchemaGraph();
		SchemaGraph sg = s2sg.convert2SchemaGraph(schema);

		Tg2Ecore tg2ec = new Tg2Ecore(sg);
		tg2ec.fillWithConfigurationsFromFile(config_filename);
		tg2ec.transform();
		tg2ec.saveEcoreMetamodel(tg2ec.getTransformedMetamodel(),
				metamodel_filename);

		Graph g = GraphIO.loadGraphFromFile(graph_filename, null);
		Tg2Dot.convertGraph(g, graphPicture_filename, GraphVizOutputFormat.PNG);

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

		System.out.println();
	}

	public static void testEcoreTgEcore() throws GraphIOException, IOException {
		System.out.println("############################");
		System.out.println("Testcase: ExtendedUniversity");

		// Filenames
		String folder = testfolder + "ecore2tg2ecore_university"
				+ File.separator;
		String genfolder = folder + "gen" + File.separator;

		// IN
		String metamodel_filename = folder + "UniversityExtended.ecore";
		String[] model_filename = { folder + "Model01.universityextended" };

		// OUT
		String graph_filename = genfolder + "UniversityExtended_graph.tg";
		String graph_dot_filename = genfolder + "UniversityExtended_graph.png";
		String metamodel_back_filename = genfolder
				+ "UniversityExtended_back.ecore";
		String model_back_filename = genfolder + "Model01_back";

		// --------------------------------------------
		// --- Ecore -> Tg ----------------------------
		// --------------------------------------------

		System.out.println("\nEcore -> Tg");

		Ecore2Tg ec2tg = new Ecore2Tg(metamodel_filename);
		ec2tg.getConfiguration().setTransformationOption(
				TransformParams.AUTOMATIC_TRANSFORMATION);
		ec2tg.transform("org.test.ExtendedUniversity");
		Graph graph = ec2tg.transformModel(model_filename);
		GraphIO.saveGraphToFile(graph, graph_filename, null);
		Tg2Dot.convertGraph(graph, graph_dot_filename, GraphVizOutputFormat.PNG);

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
