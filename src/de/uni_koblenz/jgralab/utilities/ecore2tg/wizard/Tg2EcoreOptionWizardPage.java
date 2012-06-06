package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page to enter optional information for the Tg2Ecore transformation. It
 * allows to decide whether EdgeClasses with only one role name should become
 * unidirectional references, how the GraphClass is handled and what name,
 * nsPrefix and nsURI of the Ecore schemas root package should have
 * 
 * @author kheckelmann
 * 
 */
public class Tg2EcoreOptionWizardPage extends WizardPage {

	private static final String pageName = "Tg2EcoreOptions";
	private static final String title = "Tg2Ecore - General Options";
	private static final String description = "";

	private Composite container;

	private Button buttonOneRoleToUni;

	private Button buttonTransformGC;
	private Button buttonMakeGC2Root;

	private Text textRootPackageName;
	private Text textRootPackageNsPrefix;
	private Text textRootPackageNsURI;

	protected Tg2EcoreOptionWizardPage() {
		super(pageName);
		this.setTitle(title);
		this.setDescription(description);
		this.setControl(this.container);
	}

	@Override
	public void createControl(Composite parent) {
		this.container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		this.container.setLayout(layout);
		layout.numColumns = 1;

		this.createOneRoleToUniControl();
		this.createGraphClassControls();
		this.createRootPackageControls();

		this.setControl(this.container);
		this.setPageComplete(true);
	}

	/**
	 * Creates a check box to decide whether EdgeClasses with only one role name
	 * should be transformed to unidirectional EReferences
	 */
	private void createOneRoleToUniControl() {
		this.buttonOneRoleToUni = new Button(this.container, SWT.CHECK);
		this.buttonOneRoleToUni
				.setText("Transform Edges with only one rolename to unidirectional EReferences");
	}

	/**
	 * Creates a check box to decide whether the GraphClass should be
	 * transformed and if yes, if it should become the root element of the
	 * resulting Ecore schema
	 */
	private void createGraphClassControls() {
		Composite graphClassContainer = new Composite(this.container, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		graphClassContainer.setLayout(layout);

		this.buttonTransformGC = new Button(graphClassContainer, SWT.CHECK);
		this.buttonTransformGC.setText("Transform GraphClass to EClass");
		this.buttonTransformGC.setSelection(true);
		this.buttonTransformGC.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button b = (Button) e.widget;
				if (b.getSelection()) {
					Tg2EcoreOptionWizardPage.this.buttonMakeGC2Root
							.setEnabled(true);
				} else {
					Tg2EcoreOptionWizardPage.this.buttonMakeGC2Root
							.setSelection(false);
					Tg2EcoreOptionWizardPage.this.buttonMakeGC2Root
							.setEnabled(false);
				}
			}
		});

		this.buttonMakeGC2Root = new Button(graphClassContainer, SWT.CHECK);
		this.buttonMakeGC2Root
				.setText("Make the transformed GraphClass the root EClass of the schema");
	}

	/**
	 * Creates three text fields to give a name, nsPrefix and nsURI for the root
	 * package of the resulting Ecore schema
	 */
	private void createRootPackageControls() {
		Composite rootPackageContainer = new Composite(this.container, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		rootPackageContainer.setLayout(layout);
		rootPackageContainer.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));

		Label labelName = new Label(rootPackageContainer, SWT.NULL);
		labelName.setText("Name of root package");

		this.textRootPackageName = new Text(rootPackageContainer, SWT.BORDER
				| SWT.SINGLE);
		this.textRootPackageName.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		this.textRootPackageName.setText("");

		Label labelNsPrefix = new Label(rootPackageContainer, SWT.NULL);
		labelNsPrefix.setText("nsPrefix of root package");

		this.textRootPackageNsPrefix = new Text(rootPackageContainer,
				SWT.BORDER | SWT.SINGLE);
		this.textRootPackageNsPrefix.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		this.textRootPackageNsPrefix.setText("");

		Label labelNsURI = new Label(rootPackageContainer, SWT.NULL);
		labelNsURI.setText("nsURI of root package");

		this.textRootPackageNsURI = new Text(rootPackageContainer, SWT.BORDER
				| SWT.SINGLE);
		this.textRootPackageNsURI.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		this.textRootPackageNsURI.setText("");
	}

	/**
	 * @return whether EdgeClasses with only one role name should be transformed
	 *         to unidirectional EReferences
	 */
	public boolean getOneRoleToUni() {
		return this.buttonOneRoleToUni.getSelection();
	}

	/**
	 * @return whether the GraphClass should be transformed to an EClass
	 */
	public boolean getTransformGC() {
		return this.buttonTransformGC.getSelection();
	}

	/**
	 * @return whether the EClass resulting from the GraphClass should be made
	 *         the root element of the resulting Ecore schema
	 */
	public boolean getMakeGCToRoot() {
		return this.buttonMakeGC2Root.getSelection();
	}

	/**
	 * @return the defined name of the Ecore schemas root package
	 */
	public String getRootPackageName() {
		return this.textRootPackageName.getText();
	}

	/**
	 * @return the defined nsPrefix of the Ecore schemas root package
	 */
	public String getRootPackageNsPrefix() {
		return this.textRootPackageNsPrefix.getText();
	}

	/**
	 * @return the defined nsURI of the Ecore schemas root package
	 */
	public String getRootPackageNsURI() {
		return this.textRootPackageNsURI.getText();
	}
}
