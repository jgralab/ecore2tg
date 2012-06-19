package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2Tg;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration;
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.RefInfoStructure;
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.RefPackageEditingSupport;

/**
 * Wizard with multiple options for transforming an Ecore schema and model to a
 * JGraLab schema and graph
 * 
 * @author kheckelmann
 * 
 */
public class Ecore2TgWizard extends Wizard implements IImportWizard {

	/**
	 * Mandatory page for entering paths and the the qualified schema name.
	 * Offers the possibility to choose the default options or load a
	 * configuration file.
	 */
	private Ecore2TgFileWizardPage filePage;

	/**
	 * Optional page for deciding how the GraphClass is generated, if big
	 * numbers should become transformed and if the transformation should search
	 * for conceptual EdgeClasses.
	 */
	private Ecore2TgOptionWizardPage generalOptionsPage;

	/**
	 * Optional page for defining package, direction and name for the
	 * EdgeClasses resulting from a EReference or a pair of opposite
	 * EReferences.
	 */
	private Ecore2TgOptionReferencesWizardPage referenceOptionsPage;

	protected Ecore2TgConfiguration configuration;

	public Ecore2TgWizard() {
		super();
		this.setNeedsProgressMonitor(true);
		this.configuration = new Ecore2TgConfiguration();
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public void addPages() {
		this.filePage = new Ecore2TgFileWizardPage();
		this.generalOptionsPage = new Ecore2TgOptionWizardPage();
		this.referenceOptionsPage = new Ecore2TgOptionReferencesWizardPage();
		this.addPage(this.filePage);
		this.addPage(this.generalOptionsPage);
		this.addPage(this.referenceOptionsPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		// First page: fill list of second page before showing
		if (page instanceof Ecore2TgFileWizardPage) {
			if (this.filePage.useDefaultOptions()) {
				// no second page if default options should be used
				return null;
			}
			if (this.filePage.getConfigFilePath() != null
					&& !this.filePage.getConfigFilePath().equals("")) {
				this.configuration = Ecore2TgConfiguration
						.loadConfigurationFromFile(this.filePage
								.getConfigFilePath());
			}
			this.fillEClassesListWidget();
			this.generalOptionsPage.enterConfiguration(this.configuration);
		}
		// Second page: fill table of third page before showing
		else if (page instanceof Ecore2TgOptionWizardPage) {
			this.generalOptionsPage.saveConfiguration(this.configuration);
			this.fillRefTable();
			this.referenceOptionsPage.enterConfiguration(this.configuration);
		} else if (page instanceof Ecore2TgOptionReferencesWizardPage) {
			this.referenceOptionsPage.saveConfigurations(this.configuration);
		}

		return super.getNextPage(page);
	}

	@Override
	public boolean canFinish() {
		// Pressing the finish button is allowed if the mandatory informations
		// on the first page are given
		if (this.filePage.isPageComplete()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean performFinish() {

		this.referenceOptionsPage.saveConfigurations(this.configuration);

		Ecore2Tg ecore2tg = new Ecore2Tg(
				this.filePage.getEcoreSchemaResource(), this.configuration);

		HashMap<String, String> map = this.configuration
				.getNamesOfEdgeClassesMap();
		for (String s : map.keySet()) {
			System.err.println("DEBUG " + s + " " + map.get(s));
		}

		ecore2tg.transform(this.filePage.getQualifiedSchemaName());

		Schema schema = ecore2tg.getSchema();

		// -- schema transformation finished --

		try {
			// if there is a model specified, transform it to a graph
			if (this.filePage.getEcoreInstancePath() == null
					|| this.filePage.getEcoreInstancePath().equals("")) {
				schema.save(this.filePage.getTGTargetFilePath());
			} else {
				Graph g = ecore2tg.transformModel(new String[] { this.filePage
						.getEcoreInstancePath() });
				g.save(this.filePage.getTGTargetFilePath());
			}
		} catch (GraphIOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Fills the table on page three that allows the definition of name, package
	 * and direction of the EdgeClasses resulting from an EReference or a pair
	 * of EReferences
	 */
	private void fillRefTable() {
		System.err.println("DEBUG do the filling and setting");
		ArrayList<String> packageNames = new ArrayList<String>();
		packageNames.add("");
		ArrayList<EReference> refSet = new ArrayList<EReference>();
		TreeIterator<EObject> iter = this.filePage.getEcoreSchemaResource()
				.getAllContents();

		while (iter.hasNext()) {
			EObject ob = iter.next();
			if (ob instanceof EPackage) {
				EPackage pack = (EPackage) ob;
				String name = pack.getName();
				pack = pack.getESuperPackage();
				while (pack != null) {
					name = pack.getName() + "." + name;
					pack = pack.getESuperPackage();
				}
				packageNames.add(name);
			} else if (ob instanceof EReference) {
				EReference ref = (EReference) ob;
				if (ref.getEOpposite() == null) {
					refSet.add(ref);
				} else if (!refSet.contains(ref.getEOpposite())) {
					if (ref.getName().compareTo(ref.getEOpposite().getName()) < 0) {
						refSet.add(ref);
					} else {
						refSet.add(ref.getEOpposite());
					}
				}
			}

		}
		this.referenceOptionsPage.getPackageColumn().setEditingSupport(
				new RefPackageEditingSupport(this.referenceOptionsPage
						.getReferenceTableViewer(), packageNames
						.toArray(new String[] {})));

		RefInfoStructure[] refInfoArray = new RefInfoStructure[refSet.size()];
		for (int i = 0; i < refInfoArray.length; i++) {
			refInfoArray[i] = new RefInfoStructure(refSet.get(i));
		}

		this.referenceOptionsPage.getReferenceTableViewer().setInput(
				refInfoArray);
	}

	/**
	 * Fills the dropdown list on page two that allows the selection of an
	 * EClass as GraphClass
	 */
	private void fillEClassesListWidget() {
		Resource r = this.filePage.getEcoreSchemaResource();

		ArrayList<String> eclassList = new ArrayList<String>();
		TreeIterator<EObject> it = r.getAllContents();
		while (it.hasNext()) {
			EObject o = it.next();
			if (o instanceof EClass) {
				EClass eclass = (EClass) o;
				String name = eclass.getName();
				EPackage pack = eclass.getEPackage();
				while (pack != null) {
					name = pack.getName() + "." + name;
					pack = pack.getESuperPackage();
				}
				eclassList.add(name);
			}
		}
		this.generalOptionsPage.getListWidgetEClasses().setItems(
				eclassList.toArray(new String[] {}));
		this.generalOptionsPage.getListWidgetEClasses().setSelection(0);

		String schemaName = this.filePage.getQualifiedSchemaName();
		String gcname = schemaName.substring(schemaName.lastIndexOf(".") + 1);
		gcname += "Graph";
		this.generalOptionsPage.getTextGraphClassName().setText(gcname);

	}
}
