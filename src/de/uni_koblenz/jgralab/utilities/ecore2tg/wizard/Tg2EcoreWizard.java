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
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Tg2Ecore;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Tg2EcoreConfiguration;
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

	/**
	 * Optional page to save the configuration
	 */
	private Tg2EcoreWizardPage4SaveOptions page4SaveOptions;

	// ---------------------------------------------------------------------------

	public Tg2EcoreWizard() {
		super();
		this.setNeedsProgressMonitor(true);
	}

	private Tg2EcoreConfiguration config = new Tg2EcoreConfiguration();

	private Schema schema;

	public Tg2EcoreConfiguration getConfiguration() {
		return this.config;
	}

	public Schema getSchema() {
		return this.schema;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public void addPages() {
		this.page1Files = new Tg2EcoreWizardPage1Files();
		this.page2GenOptions = new Tg2EcoreWizardPage2GenOptions();
		this.page3ECOptions = new Tg2EcoreWizardPage3ECOptions();
		this.page4SaveOptions = new Tg2EcoreWizardPage4SaveOptions();
		this.addPage(this.page1Files);
		this.addPage(this.page2GenOptions);
		this.addPage(this.page3ECOptions);
		this.addPage(this.page4SaveOptions);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		// First page: check default options
		if (page == this.page1Files) {
			this.schema = this.page1Files.getOriginalSchema();
			if (this.page1Files.useDefaultOptions()
					|| this.page1Files.isBackToEcore()) {
				// no second page if default options should be used or it is a
				// back to Ecore transformation
				return null;
			}
			if (this.page1Files.getConfigFilePath() != null
					&& !this.page1Files.getConfigFilePath().equals("")) {
				this.config = Tg2EcoreConfiguration
						.loadConfigurationFromFile(this.page1Files
								.getConfigFilePath());
				this.page4SaveOptions.setFileNameText(this.page1Files
						.getConfigFilePath());
			}
			this.page2GenOptions.enterConfiguration(this.config);
		}
		// Second page: fill table of third page before showing
		else if (page == this.page2GenOptions) {
			this.page2GenOptions.saveConfiguration(this.config);
			this.page3ECOptions.enterConfiguration(this.config);
		} else if (page == this.page3ECOptions) {
			this.page3ECOptions.saveConfiguration(this.config);
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
		this.page3ECOptions.saveConfiguration(this.config);

		Schema2SchemaGraph s2sg = new Schema2SchemaGraph();
		SchemaGraph schemaGraph = s2sg.convert2SchemaGraph(this.page1Files
				.getOriginalSchema());

		Tg2Ecore tg2ecore;
		if (!this.page1Files.useDefaultOptions()) {
			tg2ecore = new Tg2Ecore(schemaGraph, this.config);
		} else {
			tg2ecore = new Tg2Ecore(schemaGraph);
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

}
