package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.ecore.EClass;
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
	private Ecore2TgFileWizardPage filePage;

	/**
	 * Optional page for deciding how the GraphClass is generated, if big
	 * numbers should become transformed and if the transformation should search
	 * for conceptual EdgeClasses.
	 */
	private Ecore2TgOptionWizardPage generalOptionsPage;

	/**
	 * Optional page for deciding if all found conceptual EdgeClasses should be
	 * transformed to real EdgeClasses
	 */
	private Ecore2TgOptionChooseECWizardPage chooseEcOptionsPage;

	/**
	 * Optional page for deciding which EReferences should override which
	 */
	private Ecore2TgOptionOverwrittenRefWizardPage chooseOverwrittenOptionsPage;

	/**
	 * Optional page for defining package, direction and name for the
	 * EdgeClasses resulting from a EReference or a pair of opposite
	 * EReferences.
	 */
	private Ecore2TgOptionReferencesWizardPage referenceOptionsPage;

	/**
	 * Configuration to fill
	 */
	protected Ecore2TgConfiguration configuration;

	private Ecore2TgAnalyzer anal;

	private Resource metamodelResource;

	public Resource getMetamodel() {
		return this.metamodelResource;
	}

	public void setMetamodel(Resource m) {
		this.metamodelResource = m;
	}

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
		this.chooseEcOptionsPage = new Ecore2TgOptionChooseECWizardPage();
		this.chooseOverwrittenOptionsPage = new Ecore2TgOptionOverwrittenRefWizardPage();
		this.referenceOptionsPage = new Ecore2TgOptionReferencesWizardPage();
		this.addPage(this.filePage);
		this.addPage(this.generalOptionsPage);
		this.addPage(this.chooseEcOptionsPage);
		this.addPage(this.chooseOverwrittenOptionsPage);
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
			this.generalOptionsPage.fillEClassesListWidget(
					this.filePage.getEcoreSchemaResource(), this.configuration);
			this.generalOptionsPage.enterConfiguration(this.configuration);
		}
		// Second page: fill table of third page before showing
		else if (page instanceof Ecore2TgOptionWizardPage) {
			this.generalOptionsPage.saveConfiguration(this.configuration);
			this.anal = new Ecore2TgAnalyzer(
					this.filePage.getEcoreSchemaResource());
			this.anal
					.searchForEdgeClasses(TransformParams.AUTOMATIC_TRANSFORMATION);
			// If look for EdgeClasses, show result
			if (this.generalOptionsPage.getSearchForEdgeClasses()) {

				if (this.anal.getFoundEdgeClasses().isEmpty()
						&& this.anal
								.getEdgeClassCandidatesWithUnclearEReferences()
								.isEmpty()) {
					this.referenceOptionsPage.fillRefTable(
							this.filePage.getEcoreSchemaResource(),
							this.anal.getFoundEdgeClasses());
					this.referenceOptionsPage
							.enterConfiguration(this.configuration);
					return this.referenceOptionsPage;
				} else {
					this.chooseEcOptionsPage.fillTable(this.anal
							.getFoundEdgeClasses(), this.anal
							.getEdgeClassCandidatesWithUnclearEReferences());
				}
			}
			// Don't look for EdgeClasses, show what is in config
			else {
				// If nothing is in config -> next page
				if (this.configuration.getEdgeClassesList().isEmpty()) {
					this.referenceOptionsPage.fillRefTable(this.filePage
							.getEcoreSchemaResource(), this
							.getEClassesByName(this.configuration
									.getEdgeClassesList()));
					this.referenceOptionsPage
							.enterConfiguration(this.configuration);
					return this.referenceOptionsPage;
				} else {
					ArrayList<EClass> savedEdgeClasses = this
							.getEClassesByName(this.configuration
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
					this.chooseEcOptionsPage.fillTable(okECs, prECs);
				}
			}
			this.chooseEcOptionsPage.enterConfiguration(this.configuration);
		}
		// Third page
		else if (page instanceof Ecore2TgOptionChooseECWizardPage) {
			this.chooseEcOptionsPage.saveConfiguration(this.configuration);
			if (this.anal.getEdgeClassCandidatesWithUnclearEReferences()
					.isEmpty()
					|| this.testConfigContainsunclear(this.anal
							.getEdgeClassCandidatesWithUnclearEReferences(),
							this.configuration)) {
				if (this.configuration.getTransformationOption().equals(
						TransformParams.AUTOMATIC_TRANSFORMATION)) {
					this.referenceOptionsPage.fillRefTable(
							this.filePage.getEcoreSchemaResource(),
							this.anal.getFoundEdgeClasses());
				} else {
					this.referenceOptionsPage.fillRefTable(this.filePage
							.getEcoreSchemaResource(), this
							.getEClassesByName(this.configuration
									.getEdgeClassesList()));
				}
				this.referenceOptionsPage
						.enterConfiguration(this.configuration);
				return this.referenceOptionsPage;
			}
			this.chooseOverwrittenOptionsPage.fillListViewer(this.anal
					.getEReferenceToOverwriteCandidatesMap());
			this.chooseOverwrittenOptionsPage
					.enterConfiguration(this.configuration);
		}
		// Fourth page
		else if (page instanceof Ecore2TgOptionOverwrittenRefWizardPage) {
			this.chooseOverwrittenOptionsPage
					.saveConfiguration(this.configuration);
			if (this.configuration.getTransformationOption().equals(
					TransformParams.AUTOMATIC_TRANSFORMATION)) {
				this.referenceOptionsPage.fillRefTable(
						this.filePage.getEcoreSchemaResource(),
						this.anal.getFoundEdgeClasses());
			} else {
				this.referenceOptionsPage.fillRefTable(this.filePage
						.getEcoreSchemaResource(), this
						.getEClassesByName(this.configuration
								.getEdgeClassesList()));
			}
			this.referenceOptionsPage.enterConfiguration(this.configuration);
		}
		// Fifth page
		else if (page instanceof Ecore2TgOptionReferencesWizardPage) {
			this.referenceOptionsPage.saveConfiguration(this.configuration);
		}

		return super.getNextPage(page);
	}

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
		if (this.filePage.isPageComplete()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean performFinish() {

		this.referenceOptionsPage.saveConfiguration(this.configuration);

		Ecore2Tg ecore2tg = new Ecore2Tg(
				this.filePage.getEcoreSchemaResource(), this.configuration);

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

	private ArrayList<EClass> getEClassesByName(Collection<String> ecls) {
		ArrayList<EClass> list = new ArrayList<EClass>(ecls.size());
		for (String s : ecls) {
			list.add(Ecore2TgAnalyzer.getEClassByName(s,
					this.filePage.getEcoreSchemaResource()));
		}
		return list;
	}

}
