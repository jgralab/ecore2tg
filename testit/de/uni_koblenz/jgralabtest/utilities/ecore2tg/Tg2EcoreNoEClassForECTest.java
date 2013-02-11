package de.uni_koblenz.jgralabtest.utilities.ecore2tg;

import java.util.ArrayList;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Tg2Ecore;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;

public class Tg2EcoreNoEClassForECTest {

	/**
	 * @param args
	 * @throws GraphIOException
	 */
	public static void main(String[] args) throws GraphIOException {

		Tg2Ecore.main(new String[] {
				"-nec",
				"-i",
				"testit/testdata/tg2ecore/edl_java_schema/graph01.tg",
				"-o",
				"testit/testdata/tg2ecore/edl_java_schema/gen/java-schema.rsa2.ecore",
				"-m",
				"testit/testdata/tg2ecore/edl_java_schema/gen/graph01_2.xmi" });
		t();
	}

	public static void t() throws GraphIOException {

		Schema s = GraphIO
				.loadSchemaFromFile("testit/testdata/tg2ecore/edl_java_schema/java-schema.rsa.tg");

		Schema2SchemaGraph s2sg = new Schema2SchemaGraph();
		SchemaGraph sg = s2sg.convert2SchemaGraph(s);

		Tg2Ecore tg2ec = new Tg2Ecore(sg);
		tg2ec.getConfiguration().setOption_noEClassForEdgeClasses(true);
		tg2ec.transform();

		EPackage ep = tg2ec.getTransformedMetamodel();
		tg2ec.saveEcoreMetamodel(ep,
				"testit/testdata/tg2ecore/edl_java_schema/gen/java-schema.rsa.ecore");

		Graph g = GraphIO.loadGraphFromFile(
				"testit/testdata/tg2ecore/edl_java_schema/test.tg", s,
				ImplementationType.GENERIC, null);
		System.out.println("Graph1: vertices: " + g.getVCount() + " edges: "
				+ g.getECount());

		Graph g_ex1 = GraphIO.loadGraphFromFile(
				"testit/testdata/tg2ecore/edl_java_schema/graph01.tg", s,
				ImplementationType.GENERIC, null);
		System.out.println("Graph2: vertices: " + g_ex1.getVCount()
				+ " edges: " + g_ex1.getECount());

		ArrayList<EObject> model = tg2ec.transformGraphToEcoreModel(g);
		tg2ec.saveEcoreModel(model,
				"testit/testdata/tg2ecore/edl_java_schema/gen/test.xmi");
		System.out.println("Model1: vertices: " + model.size());

		ArrayList<EObject> m_ex1 = tg2ec.transformGraphToEcoreModel(g_ex1);
		tg2ec.saveEcoreModel(m_ex1,
				"testit/testdata/tg2ecore/edl_java_schema/gen/graph01.xmi");
		System.out.println("Model2: vertices: " + m_ex1.size());

	}

}
