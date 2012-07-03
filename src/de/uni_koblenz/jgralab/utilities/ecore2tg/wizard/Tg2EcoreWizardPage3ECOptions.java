package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard;

import java.util.ArrayList;
import java.util.HashMap;

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

import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Tg2EcoreConfiguration;
import de.uni_koblenz.jgralab.utilities.ecore2tg.Tg2EcoreConfiguration.EdgeDirection;
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.EcInfoStructure;
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.EcRolenameEditingSupport;
import de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider.EcTableLabelProvider;

/**
 * Wizard page to enter optional information for the Tg2Ecore transformation.
 * For each EdgeClass that will be transformed to an EClass additional FROM and
 * TO role names can be entered.
 * 
 * @author kheckelmann
 * 
 */
public class Tg2EcoreWizardPage3ECOptions extends WizardPage {

	private static final String pageName = "Tg2EcoreECOptions";
	private static final String title = "Tg2Ecore - EdgeClass Options";
	private static final String description = "";

	private Composite container;

	private TableViewer ecTableViewer;

	protected Tg2EcoreWizardPage3ECOptions() {
		super(pageName);
		this.setTitle(title);
		this.setDescription(description);
		this.setControl(this.container);
	}

	@Override
	public void createControl(Composite parent) {
		this.container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		this.container.setLayout(layout);

		this.createEcTableControls();

		this.setControl(this.container);
		this.setPageComplete(true);
	}

	/**
	 * Creates a table with three columns, the first contains the qualified
	 * EdgeClass names and the other two offer the possibility to enter
	 * additional FROM and TO role names.
	 */
	private void createEcTableControls() {
		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(2));
		tableLayout.addColumnData(new ColumnWeightData(1));
		tableLayout.addColumnData(new ColumnWeightData(1));

		Table ecTable = new Table(this.container, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.V_SCROLL);
		ecTable.setLinesVisible(true);
		ecTable.setHeaderVisible(true);
		ecTable.setLayout(tableLayout);

		this.ecTableViewer = new TableViewer(ecTable);
		this.ecTableViewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		TableViewerColumn ecNameColumn = new TableViewerColumn(
				this.ecTableViewer, SWT.NONE);
		ecNameColumn.getColumn().setText("EdgeClass");

		TableViewerColumn toRoleColumn = new TableViewerColumn(
				this.ecTableViewer, SWT.NONE);
		toRoleColumn.getColumn().setText("additional TO rolename");
		toRoleColumn.setEditingSupport(new EcRolenameEditingSupport(
				this.ecTableViewer, false));

		TableViewerColumn fromRoleColumn = new TableViewerColumn(
				this.ecTableViewer, SWT.NONE);
		fromRoleColumn.getColumn().setText("additional FROM rolename");
		fromRoleColumn.setEditingSupport(new EcRolenameEditingSupport(
				this.ecTableViewer, true));

		this.ecTableViewer.setContentProvider(new ArrayContentProvider());
		this.ecTableViewer.setLabelProvider(new EcTableLabelProvider());
	}

	/**
	 * @return the TableViewer for the EdgeClass table
	 */
	public TableViewer getEdgeClassTableViewer() {
		return this.ecTableViewer;
	}

	/**
	 * Sets the input of the table on page three that allows the definition of
	 * additional FROM and TO role names for the EdgeClasses that will be
	 * transformed to EClasses
	 */
	public void enterConfiguration(Tg2EcoreConfiguration conf) {
		ArrayList<EcInfoStructure> ecInfos = new ArrayList<EcInfoStructure>();
		Tg2EcoreWizard wiz = (Tg2EcoreWizard) this.getWizard();
		for (EdgeClass ec : wiz.getSchema().getGraphClass().getEdgeClasses()) {
			if (ec.hasAttributes() || !ec.getAllSuperClasses().isEmpty()
					|| !ec.getAllSubClasses().isEmpty()) {
				EcInfoStructure str = new EcInfoStructure(ec);
				if (conf.getOption_definerolenames().containsKey(
						ec.getQualifiedName())) {
					HashMap<EdgeDirection, String> map = conf
							.getOption_definerolenames().get(
									ec.getQualifiedName());
					str.addFromRoleName = map.get(EdgeDirection.From);
					str.addToRoleName = map.get(EdgeDirection.To);
				}
				ecInfos.add(str);
			}
		}
		this.ecTableViewer.setInput(ecInfos.toArray(new EcInfoStructure[] {}));
	}

	public void saveConfiguration(Tg2EcoreConfiguration conf) {
		// Option: additional rolenames
		EcInfoStructure[] infos = (EcInfoStructure[]) this
				.getEdgeClassTableViewer().getInput();
		if (infos == null) {
			return;
		}
		for (int i = 0; i < infos.length; i++) {
			if (infos[i].addToRoleName != null
					&& !infos[i].addToRoleName.equals("")) {
				conf.addOption_definerolenames(
						infos[i].edgeClass.getQualifiedName(),
						Tg2EcoreConfiguration.EdgeDirection.To,
						infos[i].addToRoleName);
			}
			if (infos[i].addFromRoleName != null
					&& !infos[i].addFromRoleName.equals("")) {
				conf.addOption_definerolenames(
						infos[i].edgeClass.getQualifiedName(),
						Tg2EcoreConfiguration.EdgeDirection.From,
						infos[i].addFromRoleName);
			}

		}
	}

}
