package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EClass;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgAnalyzer;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration.TransformParams;
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.EcChooseEditingSupport;
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.EcChooseLabelProvider;

public class Ecore2TgWizardPage3ChooseECs extends WizardPage implements
		ConfigurationProvider {

	private static final String pageName = "Ecore2Tg - Conceptual EdgeClasses Options";
	private static final String title = "Ecore2Tg - Conceptual EdgeClasses Options";
	private static final String description = "Choose EClasses that should become EdgeClasses";

	private Composite container;

	private TableViewer tableViewer;

	private TableViewer problemTableViewer;

	protected Ecore2TgWizardPage3ChooseECs() {
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

		Label eclabel = new Label(this.container, SWT.NONE);
		eclabel.setText("Savely found conceptual EdgeClasses:");
		this.tableViewer = this.createEdgeClassTableViewer();
		Label meclabel = new Label(this.container, SWT.NONE);
		meclabel.setText("Possible EdgeClasses (Overwriting EReferences have to be defined):");
		this.problemTableViewer = this.createEdgeClassTableViewer();

		this.setControl(this.container);
		this.setPageComplete(true);
	}

	private TableViewer createEdgeClassTableViewer() {
		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(1));
		tableLayout.addColumnData(new ColumnWeightData(8));

		Table ecTable = new Table(this.container, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.V_SCROLL);
		ecTable.setLinesVisible(true);
		ecTable.setHeaderVisible(false);
		ecTable.setLayout(tableLayout);

		TableViewer tableV = new TableViewer(ecTable);
		tableV.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		TableViewerColumn ecSelectionColumn = new TableViewerColumn(tableV,
				SWT.NONE);
		ecSelectionColumn.getColumn().setText("Selection");
		ecSelectionColumn.setEditingSupport(new EcChooseEditingSupport(tableV));

		TableViewerColumn ecNameColumn = new TableViewerColumn(tableV, SWT.NONE);
		ecNameColumn.getColumn().setText("Name");

		tableV.setContentProvider(new ArrayContentProvider());
		tableV.setLabelProvider(new EcChooseLabelProvider());
		return tableV;
	}

	public void fillTable(Collection<EClass> edgeClasses,
			Collection<EClass> candWoRef) {
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		for (EClass ec : edgeClasses) {
			map.put(Ecore2TgAnalyzer.getQualifiedEClassName(ec), true);
		}
		HashMap<String, Boolean> map2 = new HashMap<String, Boolean>();
		for (EClass ec : candWoRef) {
			map2.put(Ecore2TgAnalyzer.getQualifiedEClassName(ec), false);
		}
		if (!map.isEmpty()) {
			this.tableViewer.setInput(map.entrySet()
					.toArray(new Entry<?, ?>[0]));
		}
		if (!map2.isEmpty()) {
			this.problemTableViewer.setInput(map2.entrySet().toArray(
					new Entry<?, ?>[0]));
		}
	}

	@Override
	public void enterConfiguration(Ecore2TgConfiguration conf) {

	}

	@Override
	public void saveConfiguration(Ecore2TgConfiguration conf) {
		Entry<?, ?>[] array = (Entry<?, ?>[]) this.tableViewer.getInput();
		if (array != null) {
			boolean all = true;
			HashSet<String> names = new HashSet<String>();
			for (Entry<?, ?> e : array) {
				if (!(Boolean) e.getValue()) {
					all = false;
				} else {
					names.add(e.getKey().toString());
				}
			}
			if (all) {
				if (!conf.getEdgeClassesList().containsAll(names)) {
					conf.setTransformationOption(TransformParams.AUTOMATIC_TRANSFORMATION);
				}
			} else {
				conf.getEdgeClassesList().addAll(names);
				conf.setTransformationOption(TransformParams.JUST_LIKE_ECORE);
			}
		}

		array = (Entry<?, ?>[]) this.problemTableViewer.getInput();
		if (array != null) {
			for (Entry<?, ?> e : array) {
				if ((Boolean) e.getValue()) {
					conf.getEdgeClassesList().add(e.getKey().toString());
				}
			}
		}
	}

}
