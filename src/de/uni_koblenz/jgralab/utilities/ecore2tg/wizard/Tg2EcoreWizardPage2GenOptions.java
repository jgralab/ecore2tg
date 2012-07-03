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

import de.uni_koblenz.jgralab.utilities.ecore2tg.Tg2EcoreConfiguration;

/**
 * Wizard page to enter optional information for the Tg2Ecore transformation. It
 * allows to decide whether EdgeClasses with only one role name should become
 * unidirectional references, how the GraphClass is handled and what name,
 * nsPrefix and nsURI of the Ecore schemas root package should have
 * 
 * @author kheckelmann
 * 
 */
public class Tg2EcoreWizardPage2GenOptions extends WizardPage {

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

	protected Tg2EcoreWizardPage2GenOptions() {
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
					Tg2EcoreWizardPage2GenOptions.this.buttonMakeGC2Root
							.setEnabled(true);
				} else {
					Tg2EcoreWizardPage2GenOptions.this.buttonMakeGC2Root
							.setSelection(false);
					Tg2EcoreWizardPage2GenOptions.this.buttonMakeGC2Root
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

	public void enterConfiguration(Tg2EcoreConfiguration conf) {
		if (conf.isOption_oneroleToUni()) {
			this.buttonOneRoleToUni.setSelection(true);
		}

		if (conf.isOption_transformGraphClass()) {
			this.buttonTransformGC.setSelection(true);
		}

		if (conf.isOption_makeGraphClassToRootElement()) {
			this.buttonMakeGC2Root.setSelection(true);
		}

		if (this.textRootPackageName.getText() != null
				&& !this.textRootPackageName.getText().equals("")) {
			conf.setOption_rootpackageName(this.textRootPackageName.getText());
		}

		if (this.textRootPackageNsPrefix.getText() != null
				&& !this.textRootPackageNsPrefix.getText().equals("")) {
			conf.setOption_nsPrefix(this.textRootPackageNsPrefix.getText());
		}

		if (this.textRootPackageNsURI.getText() != null
				&& !this.textRootPackageNsURI.getText().equals("")) {
			conf.setOption_nsURI(this.textRootPackageNsURI.getText());
		}

	}

	public void saveConfiguration(Tg2EcoreConfiguration conf) {
		// Option: Only one Rolename -> unidirectional references
		conf.setOption_oneroleToUni(this.buttonOneRoleToUni.getSelection());

		// Option: transform GraphClass
		conf.setOption_transformGraphClass(this.buttonTransformGC
				.getSelection());
		// Option: make GraphClass to root
		conf.setOption_makeGraphClassToRootElement(this.buttonMakeGC2Root
				.getSelection());

		// Option: root package name
		String rootName = this.textRootPackageName.getText();
		if (rootName != null && !rootName.equals("")) {
			conf.setOption_rootpackageName(rootName);
		}
		// Option: root package nsPrefix
		String nsPrefix = this.textRootPackageNsPrefix.getText();
		if (nsPrefix != null && !nsPrefix.equals("")) {
			conf.setOption_nsPrefix(nsPrefix);
		}
		// Option: root package nsURI
		String nsURI = this.textRootPackageNsURI.getText();
		if (nsURI != null && !nsURI.equals("")) {
			conf.setOption_nsURI(nsURI);
		}

	}
}
