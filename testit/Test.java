import java.io.IOException;

import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2Tg;

public class Test {

	/**
	 * @param args
	 * @throws GraphIOException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException, GraphIOException {
		Ecore2Tg.main(new String[] { "-i",
				"testit/testdata/ecore2tg_JaMoPP/java.ecore", "-i",
				"testit/testdata/ecore2tg_JaMoPP/layout.ecore", "-ii",
				"testit/testdata/ecore2tg_JaMoPP/layout.ecore", "-o",
				"testit/testdata/ecore2tg_JaMoPP/test.tg", "-sn",
				"test.test.Hugo", "-b", "true", "-gn", "TestGraph", "-m",
				"testit/testdata/ecore2tg_JaMoPP/javaVisitor.xmi" });

	}
}
