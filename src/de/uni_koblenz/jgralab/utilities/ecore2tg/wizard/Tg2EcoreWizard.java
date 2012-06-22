package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard;

import java.util.ArrayList;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Tg2Ecore;
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.EcInfoStructure;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;

/**
 * Wizard with multiple options for transforming a JGraLab schema and graph to
 * an Ecore schema and model
 * 
 * @author kheckelmann
 * 
 */
public class Tg2EcoreWizard extends Wizard implements IImportWizard {

	/**
	 * Mandatory page for entering paths. Offers the possibility to choose the
	 * default options or load a configuration file.
	 */
	private Tg2EcoreWizardPage1Files page1Files;

	/**
	 * Optional page for deciding if EdgeClasses with only one role name should
	 * become unidirectional references, if the GraphClass should be transformed
	 * to an EClass and stated as root element. In addition the name, nxPrefix
	 * and nsURI of the schemas root package can be specified.
	 */
	private Tg2EcoreWizardPage2GenOptions page2GenOptions;

	/**
	 * Optional page for defining the additional from and to role names that are
	 * necessary to transform an EdgeClass to an EClass.
	 */
	private Tg2EcoreWizardPage3ECOptions page3ECOptions;

	public Tg2EcoreWizard() {
		super();
		this.setNeedsProgressMonitor(true);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public void addPages() {
		this.page1Files = new Tg2EcoreWizardPage1Files();
		this.page2GenOptions = new Tg2EcoreWizardPage2GenOptions();
		this.page3ECOptions = new Tg2EcoreWizardPage3ECOptions();
		this.addPage(this.page1Files);
		this.addPage(this.page2GenOptions);
		this.addPage(this.page3ECOptions);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		// First page: check default options
		if (page == this.page1Files) {
			if (this.page1Files.useDefaultOptions()
					|| this.page1Files.isBackToEcore()) {
				// no second page if default options should be used or it is a
				// back to Ecore transformation
				return null;
			}
		}
		// Second page: fill table of third page before showing
		else if (page == this.page2GenOptions) {
			this.fillEcTable();
		}

		return super.getNextPage(page);
	}

	@Override
	public boolean canFinish() {
		// Pressing the finish button is allowed if the mandatory informations
		// on the first page are given
		if (this.page1Files.isPageComplete()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean performFinish() {
		Schema2SchemaGraph s2sg = new Schema2SchemaGraph();
		SchemaGraph schemaGraph = s2sg.convert2SchemaGraph(this.page1Files
				.getOriginalSchema());

		Tg2Ecore tg2ecore = new Tg2Ecore(schemaGraph);

		if (this.page1Files.getConfigFilePath() != null
				&& !this.page1Files.getConfigFilePath().equals("")) {
			tg2ecore.fillWithConfigurationsFromFile(this.page1Files
					.getConfigFilePath());
		}

		if (!this.page1Files.useDefaultOptions()) {
			this.enterOptions(tg2ecore);
		}

		tg2ecore.transform();

		tg2ecore.saveEcoreMetamodel(tg2ecore.getTransformedMetamodel(),
				this.page1Files.getEcoreFilePath());

		// -- schema transformation finished --

		// if a model path is specified, try to load the graph and transform it
		if (this.page1Files.getXMIFilePath() != null
				&& !this.page1Files.getXMIFilePath().equals("")) {
			try {
				Graph g = GraphIO.loadGraphFromFile(
						this.page1Files.getTgFilePath(),
						this.page1Files.getOriginalSchema(),
						ImplementationType.GENERIC, null);

				ArrayList<EObject> model = tg2ecore
						.transformGraphToEcoreModel(g);
				tg2ecore.saveEcoreModel(model, this.page1Files.getXMIFilePath());
			} catch (GraphIOException ex) {
				ex.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/**
	 * Get the options from the pages and set them for the given
	 * {@link Tg2Ecore} transformation
	 * 
	 * @param tg2ecore
	 *            the transformation object
	 */
	private void enterOptions(Tg2Ecore tg2ecore) {
		// Option: Only one Rolename -> unidirectional references
		tg2ecore.setOption_oneroleToUni(this.page2GenOptions.getOneRoleToUni());

		// Option: transform GraphClass
		tg2ecore.setOption_transformGraphClass(this.page2GenOptions
				.getTransformGC());
		// Option: make GraphClass to root
		tg2ecore.setOption_makeGraphClassToRootElement(this.page2GenOptions
				.getMakeGCToRoot());

		// Option: root package name
		String rootName = this.page2GenOptions.getRootPackageName();
		if (rootName != null && !rootName.equals("")) {
			tg2ecore.setOption_rootpackageName(rootName);
		}
		// Option: root package nsPrefix
		String nsPrefix = this.page2GenOptions.getRootPackageNsPrefix();
		if (nsPrefix != null && !nsPrefix.equals("")) {
			tg2ecore.setOption_nsPrefix(nsPrefix);
		}
		// Option: root package nsURI
		String nsURI = this.page2GenOptions.getRootPackageNsURI();
		if (nsURI != null && !nsURI.equals("")) {
			tg2ecore.setOption_nsURI(nsURI);
		}

		// Option: additional rolenames
		EcInfoStructure[] infos = (EcInfoStructure[]) this.page3ECOptions
				.getEdgeClassTableViewer().getInput();
		if (infos == null) {
			return;
		}
		for (int i = 0; i < infos.length; i++) {
			if (infos[i].addToRoleName != null
					&& !infos[i].addToRoleName.equals("")) {
				tg2ecore.addOption_definerolenames(
						infos[i].edgeClass.getQualifiedName(),
						Tg2Ecore.EdgeDirection.To, infos[i].addToRoleName);
			}
			if (infos[i].addFromRoleName != null
					&& !infos[i].addFromRoleName.equals("")) {
				tg2ecore.addOption_definerolenames(
						infos[i].edgeClass.getQualifiedName(),
						Tg2Ecore.EdgeDirection.From, infos[i].addFromRoleName);
			}

		}
	}

	/**
	 * Sets the input of the table on page three that allows the definition of
	 * additional FROM and TO role names for the EdgeClasses that will be
	 * transformed to EClasses
	 */
	private void fillEcTable() {
		ArrayList<EcInfoStructure> ecInfos = new ArrayList<EcInfoStructure>();
		for (EdgeClass ec : this.page1Files.getOriginalSchema().getGraphClass()
				.getEdgeClasses()) {
			if (ec.hasAttributes() || !ec.getAllSuperClasses().isEmpty()
					|| !ec.getAllSubClasses().isEmpty()) {
				ecInfos.add(new EcInfoStructure(ec));
			}
		}
		this.page3ECOptions.getEdgeClassTableViewer().setInput(
				ecInfos.toArray(new EcInfoStructure[] {}));
	}
}
