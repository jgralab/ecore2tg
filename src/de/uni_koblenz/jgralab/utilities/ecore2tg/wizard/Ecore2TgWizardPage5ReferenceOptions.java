package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
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
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.RefPackageEditingSupport;
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.RefTableLabelProvider;

/**
 * Wizard page to enter optional information for the Ecore2Tg transformation.
 * For each pair of EReferences that will be transformed to EdgeClasses the
 * name, package and direction can be entered.
 * 
 * @author kheckelmann
 * 
 */
public class Ecore2TgWizardPage5ReferenceOptions extends WizardPage {

	private static final String pageName = "Ecore2Tg - Reference Options";
	private static final String title = "Ecore2Tg - Reference Options";
	private static final String description = "Specify information for the EdgeClasses"
			+ " generated from the following EReference pairs";

	private Composite container;

	private TableViewer referenceTableViewer;
	private TableViewerColumn packageColumn;

	protected Ecore2TgWizardPage5ReferenceOptions() {
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
		tableLayout.addColumnData(new ColumnWeightData(6));
		tableLayout.addColumnData(new ColumnWeightData(3));
		tableLayout.addColumnData(new ColumnWeightData(1));
		tableLayout.addColumnData(new ColumnWeightData(3));

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

	@Override
	public IWizardPage getPreviousPage() {
		this.saveConfiguration(((Ecore2TgWizard) this.getWizard())
				.getConfiguration());
		return super.getPreviousPage();
	}

	/**
	 * Enters the configurations from the given Ecore2TgConfiguration into the
	 * GUI
	 * 
	 * @param conf
	 *            the Ecore2TgConfiguration of the transformation
	 */
	public void enterConfiguration(Ecore2TgConfiguration conf) {
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

	/**
	 * Save the information, the user has entered into the GUI to the
	 * Ecore2TgConfiguration
	 * 
	 * @param conf
	 *            the Ecore2TgConfiguration
	 */
	public void saveConfiguration(Ecore2TgConfiguration conf) {
		RefInfoStructure[] refArray = (RefInfoStructure[]) this.referenceTableViewer
				.getInput();
		if (refArray == null) {
			return;
		}
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

	/**
	 * Fills the table on page three that allows the definition of name, package
	 * and direction of the EdgeClasses resulting from an EReference or a pair
	 * of EReferences
	 */
	public void fillRefTable(List<Resource> rList,
			Collection<EClass> edgeClasses) {
		ArrayList<String> packageNames = new ArrayList<String>();
		packageNames.add("");
		ArrayList<EReference> refSet = new ArrayList<EReference>();
		for (Resource r : rList) {

			TreeIterator<EObject> iter = r.getAllContents();

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
					if (!(edgeClasses.contains(ref.getEContainingClass()) || edgeClasses
							.contains(ref.getEReferenceType()))) {
						if (ref.getEOpposite() == null) {
							refSet.add(ref);
						} else if (!refSet.contains(ref.getEOpposite())) {
							if (ref.getName().compareTo(
									ref.getEOpposite().getName()) < 0) {
								refSet.add(ref);
							} else {
								refSet.add(ref.getEOpposite());
							}
						}
					}
				}

			}
		}
		this.packageColumn.setEditingSupport(new RefPackageEditingSupport(
				this.referenceTableViewer, packageNames
						.toArray(new String[] {})));

		RefInfoStructure[] refInfoArray = new RefInfoStructure[refSet.size()];
		for (int i = 0; i < refInfoArray.length; i++) {
			refInfoArray[i] = new RefInfoStructure(refSet.get(i));
		}

		this.referenceTableViewer.setInput(refInfoArray);
	}
}
