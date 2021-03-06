package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2Tg;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgAnalyzer;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration.TransformParams;

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
	private Ecore2TgWizardPage1Files page1Files;

	/**
	 * Optional page for deciding how the GraphClass is generated, if big
	 * numbers should become transformed and if the transformation should search
	 * for conceptual EdgeClasses.
	 */
	private Ecore2TgWizardPage2GenOptions page2GenOptions;

	/**
	 * Optional page for deciding if all found conceptual EdgeClasses should be
	 * transformed to real EdgeClasses
	 */
	private Ecore2TgWizardPage3ChooseECs page3ChooseECs;

	/**
	 * Optional page for deciding which EReferences should override which
	 */
	private Ecore2TgWizardPage4OverwrittenRefs page4OverwrittenRefs;

	/**
	 * Optional page for defining package, direction and name for the
	 * EdgeClasses resulting from a EReference or a pair of opposite
	 * EReferences.
	 */
	private Ecore2TgWizardPage5ReferenceOptions page5ReferenceOptions;

	/**
	 * Optional page to save the configuration
	 */
	private Ecore2TgWizardPage6SaveOptions page6SaveOptions;

	// -------------------------------------------------------------

	/**
	 * Configuration to fill
	 */
	private Ecore2TgConfiguration configuration;

	/**
	 * Analyzer for the metamodel, used for EdgeClass search
	 */
	private Ecore2TgAnalyzer anal;

	/**
	 * Currently chosen metamodel
	 */
	private List<Resource> metamodelResource;

	/**
	 * Constructor
	 */
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
		this.page1Files = new Ecore2TgWizardPage1Files();
		this.page2GenOptions = new Ecore2TgWizardPage2GenOptions();
		this.page3ChooseECs = new Ecore2TgWizardPage3ChooseECs();
		this.page4OverwrittenRefs = new Ecore2TgWizardPage4OverwrittenRefs();
		this.page5ReferenceOptions = new Ecore2TgWizardPage5ReferenceOptions();
		this.page6SaveOptions = new Ecore2TgWizardPage6SaveOptions();
		this.addPage(this.page1Files);
		this.addPage(this.page2GenOptions);
		this.addPage(this.page3ChooseECs);
		this.addPage(this.page4OverwrittenRefs);
		this.addPage(this.page5ReferenceOptions);
		this.addPage(this.page6SaveOptions);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		// First page: fill list of second page before showing
		if (page instanceof Ecore2TgWizardPage1Files) {
			this.anal = new Ecore2TgAnalyzer(this.metamodelResource);
			if (this.page1Files.useDefaultOptions()) {
				// no second page if default options should be used
				return null;
			}
			if (this.page1Files.getConfigFilePath() != null
					&& !this.page1Files.getConfigFilePath().equals("")) {
				this.configuration = Ecore2TgConfiguration
						.loadConfigurationFromFile(this.page1Files
								.getConfigFilePath());
				this.page6SaveOptions.setFileNameText(this.page1Files
						.getConfigFilePath());
			}
			this.page2GenOptions.enterConfiguration(this.getConfiguration());
		}
		// Second page: fill table of third page before showing
		else if (page instanceof Ecore2TgWizardPage2GenOptions) {
			this.page2GenOptions.saveConfiguration(this.getConfiguration());
			this.anal
					.searchForEdgeClasses(TransformParams.AUTOMATIC_TRANSFORMATION);
			// If look for EdgeClasses, show result
			if (this.page2GenOptions.getSearchForEdgeClasses()) {

				if (this.anal.getFoundEdgeClasses().isEmpty()
						&& this.anal
								.getEdgeClassCandidatesWithUnclearEReferences()
								.isEmpty()) {
					this.page5ReferenceOptions.fillRefTable(
							this.metamodelResource,
							this.anal.getFoundEdgeClasses());
					this.page5ReferenceOptions.enterConfiguration(this
							.getConfiguration());
					return this.page5ReferenceOptions;
				} else {
					this.page3ChooseECs.fillTable(this.anal
							.getFoundEdgeClasses(), this.anal
							.getEdgeClassCandidatesWithUnclearEReferences());
				}
			}
			// Don't look for EdgeClasses, show what is in config
			else {
				// If nothing is in config -> next page
				if (this.getConfiguration().getEdgeClassesList().isEmpty()) {
					this.page5ReferenceOptions.fillRefTable(
							this.metamodelResource, this.getEClassesByName(this
									.getConfiguration().getEdgeClassesList()));
					this.page5ReferenceOptions.enterConfiguration(this
							.getConfiguration());
					return this.page5ReferenceOptions;
				} else {
					ArrayList<EClass> savedEdgeClasses = this
							.getEClassesByName(this.getConfiguration()
									.getEdgeClassesList());
					ArrayList<EClass> okECs = new ArrayList<EClass>();
					ArrayList<EClass> prECs = new ArrayList<EClass>();
					for (EClass ec : savedEdgeClasses) {
						if (this.anal
								.getEdgeClassCandidatesWithUnclearEReferences()
								.contains(ec)) {
							prECs.add(ec);
						} else {
							okECs.add(ec);
						}
					}
					this.page3ChooseECs.fillTable(okECs, prECs);
				}
			}
		}
		// Third page
		else if (page instanceof Ecore2TgWizardPage3ChooseECs) {
			this.page3ChooseECs.saveConfiguration(this.getConfiguration());
			if (this.anal.getEdgeClassCandidatesWithUnclearEReferences()
					.isEmpty()
					|| this.testConfigContainsunclear(this.anal
							.getEdgeClassCandidatesWithUnclearEReferences(),
							this.getConfiguration())) {
				if (this.getConfiguration().getTransformationOption()
						.equals(TransformParams.AUTOMATIC_TRANSFORMATION)) {
					this.page5ReferenceOptions.fillRefTable(
							this.metamodelResource,
							this.anal.getFoundEdgeClasses());
				} else {
					this.page5ReferenceOptions.fillRefTable(
							this.metamodelResource, this.getEClassesByName(this
									.getConfiguration().getEdgeClassesList()));
				}
				this.page5ReferenceOptions.enterConfiguration(this
						.getConfiguration());
				return this.page5ReferenceOptions;
			}
			this.page4OverwrittenRefs.fillListViewer(this.anal
					.getEReferenceToOverwriteCandidatesMap());
			this.page4OverwrittenRefs.enterConfiguration(this
					.getConfiguration());
		}
		// Fourth page
		else if (page instanceof Ecore2TgWizardPage4OverwrittenRefs) {
			this.page4OverwrittenRefs
					.saveConfiguration(this.getConfiguration());
			if (this.getConfiguration().getTransformationOption()
					.equals(TransformParams.AUTOMATIC_TRANSFORMATION)) {
				this.page5ReferenceOptions.fillRefTable(this.metamodelResource,
						this.anal.getFoundEdgeClasses());
			} else {
				this.page5ReferenceOptions.fillRefTable(this.metamodelResource,
						this.getEClassesByName(this.getConfiguration()
								.getEdgeClassesList()));
			}
			this.page5ReferenceOptions.enterConfiguration(this
					.getConfiguration());
		}
		// Fifth page
		else if (page instanceof Ecore2TgWizardPage5ReferenceOptions) {
			this.page5ReferenceOptions.saveConfiguration(this
					.getConfiguration());
		}

		return super.getNextPage(page);
	}

	/**
	 * Test if there are conceptual EdgeClasses with unclear EReferences in the
	 * users wishlist
	 * 
	 * @param col
	 * @param configuration
	 * @return
	 */
	private boolean testConfigContainsunclear(Collection<EClass> col,
			Ecore2TgConfiguration configuration) {
		boolean found = false;
		for (EClass ec : col) {
			if (configuration.getEdgeClassesList().contains(
					Ecore2TgAnalyzer.getQualifiedEClassName(ec))) {
				found = true;
			}
		}
		return !found;
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
		this.page2GenOptions.saveConfiguration(this.configuration);
		this.page3ChooseECs.saveConfiguration(this.configuration);
		this.page4OverwrittenRefs.saveConfiguration(this.configuration);
		this.page5ReferenceOptions.saveConfiguration(this.getConfiguration());

		Ecore2Tg ecore2tg = new Ecore2Tg(this.anal, this.getConfiguration());
		ecore2tg.transform(this.page1Files.getQualifiedSchemaName());

		Schema schema = ecore2tg.getSchema();

		// -- schema transformation finished --

		try {
			// if there is a model specified, transform it to a graph
			if (this.page1Files.getEcoreInstancePath() == null
					|| this.page1Files.getEcoreInstancePath().equals("")) {
				schema.save(this.page1Files.getTGTargetFilePath());
			} else {
				Graph g = ecore2tg
						.transformModel(new String[] { this.page1Files
								.getEcoreInstancePath() });
				g.save(this.page1Files.getTGTargetFilePath());
			}
		} catch (GraphIOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Finds all EClasses given by their qualified name
	 * 
	 * @param ecls
	 *            the collection of qualified EClass names
	 * @return the EClasses that have the given names
	 */
	private ArrayList<EClass> getEClassesByName(Collection<String> ecls) {
		ArrayList<EClass> list = new ArrayList<EClass>(ecls.size());
		for (String s : ecls) {
			list.add(Ecore2TgAnalyzer
					.getEClassByName(s, this.metamodelResource));
		}
		return list;
	}

	// ////////////////////////////////////////////////////////////////
	// /// - getter and setter - //////////////////////////////////////
	// ////////////////////////////////////////////////////////////////

	/**
	 * @return the Ecore2TgConfiguration for the transformation
	 */
	protected Ecore2TgConfiguration getConfiguration() {
		return this.configuration;
	}

	/**
	 * @return the Ecore metamodel to transform
	 */
	public List<Resource> getMetamodel() {
		return this.metamodelResource;
	}

	/**
	 * Sets the Ecore metamodel to transform
	 * 
	 * @param m
	 *            the new Ecore metamodel
	 */
	public void setMetamodel(List<Resource> m) {
		this.metamodelResource = m;
	}
}
