package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page to enter optional information for the Ecore2Tg transformation. It
 * allows to decide how a GraphClass is generated, if big numbers should be
 * converted to long or double and if Ecore2Tg should search for conceptual
 * EdgeClasses
 * 
 * @author kheckelmann
 * 
 */
public class Ecore2TgOptionWizardPage extends WizardPage {

	private static final String pageName = "Ecore2TgOptions";
	private static final String title = "Ecore2Tg - General Options";
	private static final String description = "";

	private Composite container;

	private Button buttonSelectGraphClassFromEClasses;
	private List listWidgetEClasses;
	private Button buttonCreateNewGraphClassWithName;
	private Text textGraphClassName;

	private Button buttonConvertBigs;
	private Button buttonSearchForEdgeClasses;

	protected Ecore2TgOptionWizardPage() {
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

		this.createGraphClassNameControls();
		this.createConvertBigsControls();
		this.createECSearchControls();

		this.setControl(this.container);
		this.setPageComplete(true);
	}

	/**
	 * Creates a group of two radio buttons to decide whether a GraphClass
	 * should be generated or a present EClass should be used as GraphClass. For
	 * the first case a name must be given, for the second case a list widgets
	 * shows all available EClasses to choose one
	 */
	private void createGraphClassNameControls() {
		Label label = new Label(this.container, SWT.NULL);
		label.setText("Create GraphClass");

		Composite buttonContainer = new Composite(this.container, SWT.NULL);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.numColumns = 1;
		buttonContainer.setLayout(buttonLayout);
		buttonContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));

		this.buttonSelectGraphClassFromEClasses = new Button(buttonContainer,
				SWT.RADIO);
		this.buttonSelectGraphClassFromEClasses
				.setText("from selected EClass: ");
		this.buttonSelectGraphClassFromEClasses.setData(1);
		this.addButtonListener(this.buttonSelectGraphClassFromEClasses);
		this.listWidgetEClasses = new List(buttonContainer, SWT.SINGLE
				| SWT.V_SCROLL | SWT.BORDER);
		this.listWidgetEClasses.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));

		this.buttonCreateNewGraphClassWithName = new Button(buttonContainer,
				SWT.RADIO);
		this.buttonCreateNewGraphClassWithName.setText("with name: ");
		this.buttonCreateNewGraphClassWithName.setData(2);
		this.buttonCreateNewGraphClassWithName.setSelection(true);
		this.addButtonListener(this.buttonCreateNewGraphClassWithName);
		this.textGraphClassName = new Text(buttonContainer, SWT.BORDER
				| SWT.SINGLE);
		this.textGraphClassName.setText("GraphClass");
		this.textGraphClassName.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, false));
		this.addFocusListener(this.textGraphClassName);
	}

	/**
	 * Creates a check box to decide whether BigInteger and BigDecimal should be
	 * transformed to long or double
	 */
	private void createConvertBigsControls() {
		Label labelConvertBigs = new Label(this.container, SWT.NULL);
		labelConvertBigs
				.setText("Check box if BigInteger and BigDecimal attributes should be transformed as Long and Double.");
		this.buttonConvertBigs = new Button(this.container, SWT.CHECK);
		this.buttonConvertBigs
				.setText("convert BigInteger to Long and BigDecimal to Double");
	}

	/**
	 * Creates a check box to decide whether Ecore2Tg should search for
	 * conceptual EdgeClasses
	 */
	private void createECSearchControls() {
		Label labelSearchForConcECs = new Label(this.container, SWT.NULL);
		labelSearchForConcECs
				.setText("Check box if Ecore2Tg should look for conceptual EdgeClasses");
		this.buttonSearchForEdgeClasses = new Button(this.container, SWT.CHECK);
		this.buttonSearchForEdgeClasses.setText("search for EdgeClasses");
		this.buttonSearchForEdgeClasses.setSelection(true);
	}

	public Text getTextGraphClassName() {
		return this.textGraphClassName;
	}

	public List getListWidgetEClasses() {
		return this.listWidgetEClasses;
	}

	public boolean getButtonConvertBigs() {
		return this.buttonConvertBigs.getSelection();
	}

	public boolean getButtonSelectGraphClassFromEClasses() {
		return this.buttonSelectGraphClassFromEClasses.getSelection();
	}

	public boolean getButtonCreateNewGraphClassWithName() {
		return this.buttonCreateNewGraphClassWithName.getSelection();
	}

	public boolean getSearchForEdgeClasses() {
		return this.buttonSearchForEdgeClasses.getSelection();
	}

	private void addButtonListener(Button b) {
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Ecore2TgOptionWizardPage.this
						.setPageComplete(Ecore2TgOptionWizardPage.this
								.isComplete());
			}
		});
	}

	private void addFocusListener(Text t) {
		t.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				Ecore2TgOptionWizardPage.this
						.setPageComplete(Ecore2TgOptionWizardPage.this
								.isComplete());
			}
		});
	}

	private boolean isComplete() {
		if (this.buttonCreateNewGraphClassWithName.getSelection()) {
			return !this.textGraphClassName.getText().isEmpty();
		} else {
			return this.listWidgetEClasses.getSelectionIndex() > -1;
		}
	}
}