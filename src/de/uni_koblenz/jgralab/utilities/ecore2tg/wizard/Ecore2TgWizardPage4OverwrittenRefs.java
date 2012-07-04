package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgAnalyzer;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration;
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.OverwrittenEReferencesColumnEditingSupport;
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.OverwrittenEReferencesLabelProvider;

public class Ecore2TgWizardPage4OverwrittenRefs extends WizardPage {

	private static final String pageName = "Ecore2Tg - Overwriting Options";
	private static final String title = "Ecore2Tg - Overwriting Options";
	private static final String description = "Specify which EReferences a given EReference overwrites";

	private Composite container;

	private TableViewer viewer;
	private TableViewerColumn chooseRefColumn;

	/**
	 * HashMap mapping EReferences to EReferences they overwrite
	 */
	private HashMap<EReference, EReference> refmap;

	/**
	 * Constructor
	 */
	public Ecore2TgWizardPage4OverwrittenRefs() {
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

		this.createTableViewerControl();

		this.setControl(this.container);
		this.setPageComplete(true);
	}

	private void createTableViewerControl() {
		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(1));
		tableLayout.addColumnData(new ColumnWeightData(1));

		Table referenceTable = new Table(this.container, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.V_SCROLL);
		referenceTable.setLinesVisible(true);
		referenceTable.setHeaderVisible(true);
		referenceTable.setLayout(tableLayout);

		this.viewer = new TableViewer(referenceTable);
		this.viewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		TableViewerColumn refNameColumn = new TableViewerColumn(this.viewer,
				SWT.NONE);
		refNameColumn.getColumn().setText("EReference");
		this.chooseRefColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		this.chooseRefColumn.getColumn().setText("Overwritten EReference");

		this.viewer.setContentProvider(new ArrayContentProvider());
		this.viewer.setLabelProvider(new OverwrittenEReferencesLabelProvider());
	}

	/**
	 * Fills the list viewer with the calculated choices
	 * 
	 * @param rm
	 *            HashMap mapping EReferences to a set of EReferences that are
	 *            candidates to become overwritten by the key EReference
	 */
	public void fillListViewer(HashMap<EReference, HashSet<EReference>> rm) {
		this.refmap = new HashMap<EReference, EReference>();
		for (EReference e : rm.keySet()) {
			this.refmap.put(e, null);
		}
		this.chooseRefColumn
				.setEditingSupport(new OverwrittenEReferencesColumnEditingSupport(
						this.viewer, rm));
		this.viewer
				.setInput(this.refmap.entrySet().toArray(new Entry<?, ?>[0]));
		this.viewer.refresh();
	}

	/**
	 * Enters the configurations from the given Ecore2TgConfiguration into the
	 * GUI
	 * 
	 * @param conf
	 *            the Ecore2TgConfiguration of the transformation
	 */
	public void enterConfiguration(Ecore2TgConfiguration conf) {
		for (String refName : conf.getPairsOfOverwritingEReferences().keySet()) {
			this.refmap
					.put(Ecore2TgAnalyzer.getEReferenceByName(refName,
							((Ecore2TgWizard) this.getWizard()).getMetamodel()),
							Ecore2TgAnalyzer.getEReferenceByName(
									conf.getPairsOfOverwritingEReferences()
											.get(refName),
									((Ecore2TgWizard) this.getWizard())
											.getMetamodel()));
		}

	}

	/**
	 * Save the information, the user has entered into the GUI to the
	 * Ecore2TgConfiguration
	 * 
	 * @param conf
	 *            the Ecore2TgConfiguration
	 */
	public void saveConfiguration(Ecore2TgConfiguration conf) {
		if (this.refmap != null) {
			for (EReference ref : this.refmap.keySet()) {
				if (this.refmap.get(ref) != null) {
					conf.getPairsOfOverwritingEReferences().put(
							Ecore2TgAnalyzer.getQualifiedReferenceName(ref),
							Ecore2TgAnalyzer
									.getQualifiedReferenceName(this.refmap
											.get(ref)));
				}
			}
		}
	}
}
