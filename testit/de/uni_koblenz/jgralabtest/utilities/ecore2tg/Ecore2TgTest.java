package de.uni_koblenz.jgralabtest.utilities.ecore2tg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2Tg;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration.TransformParams;

public class Ecore2TgTest {

	private static final String testdataFolder = "testit" + File.separator
			+ "testdata" + File.separator + "ecore2tg_";

	@Test
	public void testUserDefinesOverwritten() throws GraphIOException {
		String name = "UserDefinesOverwritten";
		String basefile = testdataFolder + name + File.separator;
		String ecorefile = basefile + name + ".ecore";

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

		conf.getDirectionMap().put("testuserdefinesoverwritten.E.end",
				Ecore2TgConfiguration.TO);

		// Start Transformation
		conf.setTransformationOption(TransformParams.AUTOMATIC_TRANSFORMATION);
		test.transform("test.OverwrittenRefs");

		Schema schem = test.getSchema();
		String tgfile = basefile + "gen" + File.separator + name + ".tg";
		GraphIO.saveSchemaToFile(schem, tgfile);

		assertTrue(schem.getAttributedElementClass("E") instanceof EdgeClass);
		assertTrue(schem.getAttributedElementClass("E1") instanceof EdgeClass);
		assertTrue(schem.getAttributedElementClass("E2") instanceof EdgeClass);
		assertTrue(schem.getAttributedElementClass("E3") instanceof EdgeClass);

		assertTrue(schem.getGraphClass().getEdgeClass("E1")
				.getDirectSuperClasses()
				.contains(schem.getGraphClass().getEdgeClass("E")));
		assertTrue(schem.getGraphClass().getEdgeClass("E2")
				.getDirectSuperClasses()
				.contains(schem.getGraphClass().getEdgeClass("E1")));
		assertTrue(schem.getGraphClass().getEdgeClass("E3")
				.getDirectSuperClasses()
				.contains(schem.getGraphClass().getEdgeClass("E2")));

		assertEquals("start", schem.getGraphClass().getEdgeClass("E").getFrom()
				.getRolename());
		assertEquals("end", schem.getGraphClass().getEdgeClass("E").getTo()
				.getRolename());
		assertEquals("end1", schem.getGraphClass().getEdgeClass("E1").getTo()
				.getRolename());
		assertEquals("end2", schem.getGraphClass().getEdgeClass("E2").getTo()
				.getRolename());
		assertEquals("end3", schem.getGraphClass().getEdgeClass("E3").getTo()
				.getRolename());

	}

	@Test
	public void testMultipleInheritanceEdgeClasses() throws GraphIOException {
		String name = "MultipleInheritanceEdgeClasses";
		String basefile = testdataFolder + name + File.separator;
		String ecorefile = basefile + name + ".ecore";

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

		// Start Transformation
		conf.setTransformationOption(TransformParams.AUTOMATIC_TRANSFORMATION);
		test.transform("test.MultipleInheritance");

		Schema schem = test.getSchema();
		String tgfile = basefile + "gen" + File.separator + name + ".tg";
		GraphIO.saveSchemaToFile(schem, tgfile);

		Schema schema = test.getSchema();
		assertTrue(schema.getAttributedElementClass("EdgeAB") instanceof EdgeClass);
		assertTrue(schema.getAttributedElementClass("EdgeCD") instanceof EdgeClass);
		assertTrue(schema.getAttributedElementClass("EdgeKL") instanceof EdgeClass);
		assertTrue(schema.getAttributedElementClass("BetterEdgeAB") instanceof EdgeClass);
		assertTrue(schema.getAttributedElementClass("BetterEdgeKL") instanceof EdgeClass);

		assertTrue(schema.getGraphClass().getEdgeClass("EdgeAB")
				.getDirectSuperClasses()
				.contains(schema.getGraphClass().getEdgeClass("EdgeCD")));
		assertTrue(schema.getGraphClass().getEdgeClass("BetterEdgeAB")
				.getDirectSuperClasses()
				.contains(schema.getGraphClass().getEdgeClass("EdgeAB")));
		assertTrue(schema.getGraphClass().getEdgeClass("BetterEdgeKL")
				.getDirectSuperClasses()
				.contains(schema.getGraphClass().getEdgeClass("EdgeKL")));
	}

	@Test
	public void testComplexWayAndNode() throws GraphIOException {
		String name = "ComplexWayAndNode";
		String basefile = testdataFolder + name + File.separator;
		String ecorefile = basefile + name + ".ecore";

		// Transformation
		Ecore2Tg test = new Ecore2Tg(ecorefile);
		Ecore2TgConfiguration conf = test.getConfiguration();

		// Options
		conf.getDirectionMap().put(
				"complexwayandnode.structure.Node.outgoingWays",
				Ecore2TgConfiguration.TO);

		conf.getDefinedPackagesOfEdgeClassesMap().put(
				"complexwayandnode.CardUse.Section.shownnodes",
				"complexwayandnode.structure");

		// Start Transformation
		conf.setTransformationOption(TransformParams.AUTOMATIC_TRANSFORMATION);
		test.transform("test.ComplexWayAndNode");

		Schema schem = test.getSchema();

		assertTrue(schem.getAttributedElementClass("structure.Way") instanceof EdgeClass);
		assertTrue(schem.getAttributedElementClass("structure.CarRoute") instanceof EdgeClass);
		assertTrue(schem.getAttributedElementClass("structure.HikingTrail") instanceof EdgeClass);

		assertTrue(schem.getGraphClass().getEdgeClass("structure.CarRoute")
				.getDirectSuperClasses()
				.contains(schem.getGraphClass().getEdgeClass("structure.Way")));
		assertTrue(schem.getGraphClass().getEdgeClass("structure.HikingTrail")
				.getDirectSuperClasses()
				.contains(schem.getGraphClass().getEdgeClass("structure.Way")));

		assertEquals("startOfWay",
				schem.getGraphClass().getEdgeClass("structure.Way").getFrom()
						.getRolename());
		assertEquals("endOfWay",
				schem.getGraphClass().getEdgeClass("structure.Way").getTo()
						.getRolename());
		assertEquals("structure",
				schem.getGraphClass().getVertexClass("carduse.Section")
						.getDirectedEdgeClassForFarEndRole("shownnodes")
						.getEdgeClass().getPackageName());

		String[] a = new String[11];

		a[0] = basefile + File.separator + "multiple_file_model"
				+ File.separator + "Bu1.structure";
		a[1] = basefile + File.separator + "multiple_file_model"
				+ File.separator + "Bu2.structure";
		a[2] = basefile + File.separator + "multiple_file_model"
				+ File.separator + "CR2.structure";
		a[3] = basefile + File.separator + "multiple_file_model"
				+ File.separator + "HT1.structure";
		a[4] = basefile + File.separator + "multiple_file_model"
				+ File.separator + "Int.structure";
		a[5] = basefile + File.separator + "multiple_file_model"
				+ File.separator + "My.structure";
		a[6] = basefile + File.separator + "multiple_file_model"
				+ File.separator + "Bu3.structure";
		a[7] = basefile + File.separator + "multiple_file_model"
				+ File.separator + "CR3.structure";
		a[8] = basefile + File.separator + "multiple_file_model"
				+ File.separator + "CR4.structure";
		a[9] = basefile + File.separator + "multiple_file_model"
				+ File.separator + "CR5.structure";
		a[10] = basefile + File.separator + "multiple_file_model"
				+ File.separator + "Int2.structure";

		Graph g = test.transformModel(a);

		String tgfile1 = basefile + "gen" + File.separator + name + ".tg";
		g.save(tgfile1);

		Graph g2 = test.transformModel(new String[] { basefile
				+ "Example1.complexwayandnode" });

		String tgfile2 = basefile + "gen" + File.separator + name + "2.tg";
		g2.save(tgfile2);
	}
}
