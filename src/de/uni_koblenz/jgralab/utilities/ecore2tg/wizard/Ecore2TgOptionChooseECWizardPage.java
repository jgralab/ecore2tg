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
import org.eclipse.swt.widgets.Table;

import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgAnalyzer;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration.TransformParams;
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.EcChooseEditingSupport;
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.EcChooseLabelProvider;

public class Ecore2TgOptionChooseECWizardPage extends WizardPage implements
		ConfigurationProvider {

	private static final String pageName = "Ecore2Tg - Conceptual EdgeClasses Options";
	private static final String title = "Ecore2Tg - Conceptual EdgeClasses Options";
	private static final String description = "Choose EClasses that should become EdgeClasses";

	private Composite container;

	private TableViewer tableViewer;

	protected Ecore2TgOptionChooseECWizardPage() {
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

		this.createEdgeClassTableViewer();

		this.setControl(this.container);
		this.setPageComplete(true);
	}

	public void createEdgeClassTableViewer() {
		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(1));
		tableLayout.addColumnData(new ColumnWeightData(8));

		Table ecTable = new Table(this.container, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.V_SCROLL);
		ecTable.setLinesVisible(true);
		ecTable.setHeaderVisible(false);
		ecTable.setLayout(tableLayout);

		this.tableViewer = new TableViewer(ecTable);
		this.tableViewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		TableViewerColumn ecSelectionColumn = new TableViewerColumn(
				this.tableViewer, SWT.NONE);
		ecSelectionColumn.getColumn().setText("Selection");
		ecSelectionColumn.setEditingSupport(new EcChooseEditingSupport(
				this.tableViewer));

		TableViewerColumn ecNameColumn = new TableViewerColumn(
				this.tableViewer, SWT.NONE);
		ecNameColumn.getColumn().setText("Name");

		this.tableViewer.setContentProvider(new ArrayContentProvider());
		this.tableViewer.setLabelProvider(new EcChooseLabelProvider());
	}

	public void fillTable(Collection<EClass> edgeClasses) {
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		for (EClass ec : edgeClasses) {
			map.put(Ecore2TgAnalyzer.getQualifiedEClassName(ec), true);
		}
		this.tableViewer.setInput(map.entrySet().toArray(new Entry<?, ?>[0]));
	}

	public void fillTable2(Collection<String> edgeClasses) {
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		for (String ec : edgeClasses) {
			map.put(ec, true);
		}
		this.tableViewer.setInput(map.entrySet().toArray(new Entry<?, ?>[0]));
	}

	@Override
	public void enterConfiguration(Ecore2TgConfiguration conf) {

		// TODO Auto-generated method stub

	}

	@Override
	public void saveConfiguration(Ecore2TgConfiguration conf) {
		Entry<?, ?>[] array = (Entry<?, ?>[]) this.tableViewer.getInput();
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

}
