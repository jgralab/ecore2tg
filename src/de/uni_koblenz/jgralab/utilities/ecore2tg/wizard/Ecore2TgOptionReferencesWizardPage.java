package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgAnalyzer;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration;
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.RefDirectionEditingSupport;
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.RefInfoStructure;
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.RefNameEditingSupport;
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.RefTableLabelProvider;

/**
 * Wizard page to enter optional information for the Ecore2Tg transformation.
 * For each pair of EReferences that will be transformed to EdgeClasses the
 * name, package and direction can be entered.
 * 
 * @author kheckelmann
 * 
 */
public class Ecore2TgOptionReferencesWizardPage extends WizardPage {

	private static final String pageName = "Ecore2Tg - Reference Options";
	private static final String title = "Ecore2Tg - Reference Options";
	private static final String description = "Specify information for the EdgeClasses"
			+ " generated from the following EReference pairs";

	private Composite container;

	private TableViewer referenceTableViewer;
	private TableViewerColumn packageColumn;

	protected Ecore2TgOptionReferencesWizardPage() {
		super(pageName);
		this.setTitle(title);
		this.setDescription(description);
		this.setControl(this.container);
	}

	@Override
	public void createControl(Composite parent) {
		this.container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		this.container.setLayout(layout);
		layout.numColumns = 1;

		this.createReferenceTableControls();

		this.setControl(this.container);
		this.setPageComplete(true);

	}

	/**
	 * Creates a table with four columns, the first contains the qualified
	 * EReference names, the second offers the possibility to choose a package,
	 * the third to enter a name and the fourth to choose the direction of the
	 * the resulting EdgeClass.
	 */
	private void createReferenceTableControls() {
		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(2));
		tableLayout.addColumnData(new ColumnWeightData(1));
		tableLayout.addColumnData(new ColumnWeightData(1));
		tableLayout.addColumnData(new ColumnWeightData(1));

		Table referenceTable = new Table(this.container, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.V_SCROLL);
		referenceTable.setLinesVisible(true);
		referenceTable.setHeaderVisible(true);
		referenceTable.setLayout(tableLayout);

		this.referenceTableViewer = new TableViewer(referenceTable);
		this.referenceTableViewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		TableViewerColumn refNameColumn = new TableViewerColumn(
				this.referenceTableViewer, SWT.NONE);
		refNameColumn.getColumn().setText("Reference pair");
		this.packageColumn = new TableViewerColumn(this.referenceTableViewer,
				SWT.NONE);
		this.packageColumn.getColumn().setText("Package");
		TableViewerColumn directionColumn = new TableViewerColumn(
				this.referenceTableViewer, SWT.NONE);
		directionColumn.getColumn().setText("Direction");
		directionColumn.getColumn().setToolTipText(
				"Check if ERefence should determine the direction.");
		directionColumn.setEditingSupport(new RefDirectionEditingSupport(
				this.referenceTableViewer));
		TableViewerColumn ecNameColumn = new TableViewerColumn(
				this.referenceTableViewer, SWT.NONE);
		ecNameColumn.getColumn().setText("Name");
		ecNameColumn.setEditingSupport(new RefNameEditingSupport(
				this.referenceTableViewer));

		this.referenceTableViewer
				.setContentProvider(new ArrayContentProvider());
		this.referenceTableViewer.setLabelProvider(new RefTableLabelProvider());
	}

	/**
	 * @return the TableViewer for the EReference table
	 */
	public TableViewer getReferenceTableViewer() {
		return this.referenceTableViewer;
	}

	/**
	 * @return the TableViewerColumn of the the package column to fill with the
	 *         available packages
	 */
	public TableViewerColumn getPackageColumn() {
		return this.packageColumn;
	}

	@Override
	public IWizardPage getPreviousPage() {
		this.saveConfigurations(((Ecore2TgWizard) this.getWizard()).configuration);
		return super.getPreviousPage();
	}

	protected void enterConfiguration(Ecore2TgConfiguration conf) {
		RefInfoStructure[] refArray = (RefInfoStructure[]) this.referenceTableViewer
				.getInput();
		for (RefInfoStructure s : refArray) {
			String refname = Ecore2TgAnalyzer
					.getQualifiedReferenceName(s.reference);

			// Direction
			if (conf.getDirectionMap().containsKey(refname)) {
				int i = conf.getDirectionMap().get(refname);
				if (i == Ecore2TgConfiguration.TO) {
					s.direction = true;
				} else {
					s.direction = false;
				}
			}

			// Package
			if (conf.getDefinedPackagesOfEdgeClassesMap().containsKey(refname)) {
				s.packageName = conf.getDefinedPackagesOfEdgeClassesMap().get(
						refname);
			}

			// Name
			if (conf.getNamesOfEdgeClassesMap().containsKey(refname)) {
				s.edgeClassName = conf.getNamesOfEdgeClassesMap().get(refname);
			}

			// OPPOSITE if exists
			if (s.reference.getEOpposite() != null) {
				String opname = Ecore2TgAnalyzer
						.getQualifiedReferenceName(s.reference.getEOpposite());

				// Direction
				if (conf.getDirectionMap().containsKey(opname)) {
					int i = conf.getDirectionMap().get(opname);
					if (i == Ecore2TgConfiguration.TO) {
						s.direction = false;
					} else {
						s.direction = true;
					}
				}

				// Package
				if (conf.getDefinedPackagesOfEdgeClassesMap().containsKey(
						opname)) {
					s.packageName = conf.getDefinedPackagesOfEdgeClassesMap()
							.get(opname);
				}

				// Name
				if (conf.getNamesOfEdgeClassesMap().containsKey(opname)) {
					s.edgeClassName = conf.getNamesOfEdgeClassesMap().get(
							opname);
				}

			}

		}
		this.referenceTableViewer.refresh();
	}

	protected void saveConfigurations(Ecore2TgConfiguration conf) {
		RefInfoStructure[] refArray = (RefInfoStructure[]) this.referenceTableViewer
				.getInput();
		for (RefInfoStructure s : refArray) {
			String refname = Ecore2TgAnalyzer
					.getQualifiedReferenceName(s.reference);

			String opname = null;
			if (s.reference.getEOpposite() != null) {
				opname = Ecore2TgAnalyzer.getQualifiedReferenceName(s.reference
						.getEOpposite());
			}

			String dirref = refname;
			// Direction
			if (s.direction) {
				conf.getDirectionMap().put(refname, Ecore2TgConfiguration.TO);
				if (opname != null) {
					conf.getDirectionMap().remove(opname);
				}
			} else {
				if (opname != null) {
					conf.getDirectionMap()
							.put(opname, Ecore2TgConfiguration.TO);
					conf.getDirectionMap().remove(refname);
					dirref = opname;
				} else {
					conf.getDirectionMap().put(refname,
							Ecore2TgConfiguration.FROM);
				}
			}

			// Package
			if (s.packageName != null && !s.packageName.equals("")) {
				conf.getDefinedPackagesOfEdgeClassesMap().remove(refname);
				conf.getDefinedPackagesOfEdgeClassesMap().remove(opname);
				conf.getDefinedPackagesOfEdgeClassesMap().put(dirref,
						s.packageName);
			}

			// Name
			if (s.edgeClassName != null && !s.edgeClassName.equals("")) {
				conf.getNamesOfEdgeClassesMap().remove(refname);
				conf.getNamesOfEdgeClassesMap().remove(opname);
				conf.getNamesOfEdgeClassesMap().put(dirref, s.edgeClassName);
			}
		}
	}

}
