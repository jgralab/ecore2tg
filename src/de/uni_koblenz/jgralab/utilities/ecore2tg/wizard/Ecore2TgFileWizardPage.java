package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2Tg;

/**
 * WizardPage to enter mandatory informations for the Ecore2Tg transformation:
 * the paths to the Ecore schema and the Ecore model to transform and the path
 * of the resulting TG file, the qualified name of the resulting JGraLab schema,
 * whether default options should be used and if a configuration file should
 * become loaded
 * 
 * 
 * @author kheckelmann
 * 
 */
public class Ecore2TgFileWizardPage extends WizardPage {

	private final static String pageName = "Ecore2TgFileChoose";
	private final static String title = "Ecore2Tg";
	private final static String description = "";

	private Composite container;
	private Text textEcoreFile;
	private Text textXMIFile;
	private Text textTgFile;
	private Text textTgSchemaName;
	private Button buttonDefaultOptions;
	private Button buttonUseConfigFileCheck;
	private Text textConfigFile;
	private Button buttonConfigFileBrowse;

	private Resource ecoreSchema;

	protected Ecore2TgFileWizardPage() {
		super(pageName);
		this.setTitle(title);
		this.setDescription(description);
		this.setControl(this.textEcoreFile);
	}

	@Override
	public void createControl(Composite parent) {
		this.container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		this.container.setLayout(layout);
		layout.numColumns = 3;

		this.createEnterEcoreSchemaControls();
		this.createEnterEcoreModelControls();

		this.createEnterTargetTgFileControls();

		this.createEnterSchemaNameControls();

		this.createChooseDefaultOptionsControls();
		this.createLoadConfigFileControls();

		this.setControl(this.container);
		this.setPageComplete(false);
	}

	/**
	 * Creates a text field to enter a path to an Ecore schema with a browse
	 * button
	 */
	private void createEnterEcoreSchemaControls() {
		Label labelEcoreSchema = new Label(this.container, SWT.NULL);
		labelEcoreSchema.setText("Ecore schema");

		this.textEcoreFile = new Text(this.container, SWT.BORDER | SWT.SINGLE);
		this.textEcoreFile
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.textEcoreFile.setText("");
		this.textEcoreFile.addFocusListener(this.getTestFinishFocusListener());

		Button buttonSelectEcoreSchema = new Button(this.container, SWT.PUSH);
		buttonSelectEcoreSchema.setText("browse");
		buttonSelectEcoreSchema.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IWorkbenchWindow window = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				Shell shell = window.getShell();

				FileDialog fileDialog = new FileDialog(shell);
				fileDialog.setText("Select an Ecore schema file");
				fileDialog.setFilterExtensions(new String[] { "*.ecore" });
				fileDialog
						.setFilterNames(new String[] { "Ecore-files (*.ecore)" });
				String path = fileDialog.open();

				if (path != null) {
					Ecore2TgFileWizardPage.this.textEcoreFile.setText(path);
					Ecore2TgFileWizardPage.this.ecoreSchema = Ecore2Tg
							.loadMetaModelFromEcoreFile(path);
					((Ecore2TgWizard) Ecore2TgFileWizardPage.this.getWizard())
							.setMetamodel(Ecore2TgFileWizardPage.this.ecoreSchema);
					if (Ecore2TgFileWizardPage.this.isComplete()) {
						Ecore2TgFileWizardPage.this.setPageComplete(true);
					}
				} else {
					Ecore2TgFileWizardPage.this.setPageComplete(false);
				}
			}
		});
	}

	/**
	 * Creates a text field to enter a path to an Ecore model with a browse
	 * button
	 */
	private void createEnterEcoreModelControls() {
		Label labelEcoreModel = new Label(this.container, SWT.NULL);
		labelEcoreModel.setText("Ecore model");

		this.textXMIFile = new Text(this.container, SWT.BORDER | SWT.SINGLE);
		this.textXMIFile.setText("");
		this.textXMIFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button buttonSelectEcoreModel = new Button(this.container, SWT.PUSH);
		buttonSelectEcoreModel.setText("browse");
		buttonSelectEcoreModel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IWorkbenchWindow window = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				Shell shell = window.getShell();

				FileDialog fileDialog = new FileDialog(shell);
				fileDialog.setText("Select an Ecore model file");
				String path = fileDialog.open();

				if (path != null) {
					Ecore2TgFileWizardPage.this.textXMIFile.setText(path);
				}
			}
		});
	}

	/**
	 * Creates a text field to enter a path to the target TG file with a browse
	 * button
	 */
	private void createEnterTargetTgFileControls() {
		Label labelTgFolder = new Label(this.container, SWT.NULL);
		labelTgFolder.setText("Target TG file:");

		this.textTgFile = new Text(this.container, SWT.BORDER | SWT.SINGLE);
		this.textTgFile.setText("");
		this.textTgFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.textTgFile.addFocusListener(this.getTestFinishFocusListener());

		Button buttonSelectTargetFolder = new Button(this.container, SWT.PUSH);
		buttonSelectTargetFolder.setText("browse");
		buttonSelectTargetFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IWorkbenchWindow window = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				Shell shell = window.getShell();

				FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
				fileDialog.setText("Select the target TG file");
				fileDialog.setFilterExtensions(new String[] { "*.tg" });
				fileDialog.setFilterNames(new String[] { "TG-files (*.tg)" });
				String path = fileDialog.open();

				if (path != null) {
					Ecore2TgFileWizardPage.this.textTgFile.setText(path);
					if (Ecore2TgFileWizardPage.this.isComplete()) {
						Ecore2TgFileWizardPage.this.setPageComplete(true);
					}
				} else {
					Ecore2TgFileWizardPage.this.setPageComplete(false);
				}
			}
		});
	}

	/**
	 * Creates a text field to enter a qualified schema name
	 */
	private void createEnterSchemaNameControls() {
		Label labelTgSchemaName = new Label(this.container, SWT.NULL);
		labelTgSchemaName.setText("Qualified Schema name:");

		this.textTgSchemaName = new Text(this.container, SWT.BORDER
				| SWT.SINGLE);
		this.textTgSchemaName.setText("package.prefix.SchemaName");
		this.textTgSchemaName.addFocusListener(this
				.getTestFinishFocusListener());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		this.textTgSchemaName.setLayoutData(gd);
	}

	/**
	 * Create a check button to decide whether to use the default options
	 */
	private void createChooseDefaultOptionsControls() {
		this.buttonDefaultOptions = new Button(this.container, SWT.CHECK);
		this.buttonDefaultOptions.setText("Use default options.");
		this.buttonDefaultOptions.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button b = (Button) e.widget;
				if (b.getSelection()) {
					Ecore2TgFileWizardPage.this.buttonUseConfigFileCheck
							.setEnabled(false);
				} else {
					Ecore2TgFileWizardPage.this.buttonUseConfigFileCheck
							.setEnabled(true);
				}
				Ecore2TgFileWizardPage.this
						.setPageComplete(Ecore2TgFileWizardPage.this
								.isPageComplete());
			}
		});
		GridData gd = new GridData();
		gd.horizontalSpan = 3;
		this.buttonDefaultOptions.setLayoutData(gd);
	}

	/**
	 * Create a check button and a text field with browse button to specify
	 * whether a configuration file should be used
	 */
	private void createLoadConfigFileControls() {
		this.buttonUseConfigFileCheck = new Button(this.container, SWT.CHECK);
		this.buttonUseConfigFileCheck.setText("Load config file");
		this.buttonUseConfigFileCheck
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Button b = (Button) e.widget;
						if (b.getSelection()) {
							Ecore2TgFileWizardPage.this.buttonDefaultOptions
									.setEnabled(false);
						} else {
							Ecore2TgFileWizardPage.this.buttonDefaultOptions
									.setEnabled(true);
						}
						Ecore2TgFileWizardPage.this
								.setPageComplete(Ecore2TgFileWizardPage.this
										.isComplete());
					}
				});

		this.textConfigFile = new Text(this.container, SWT.BORDER | SWT.SINGLE);
		this.textConfigFile.setText("");
		this.textConfigFile
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.textConfigFile.addFocusListener(this.getTestFinishFocusListener());

		this.buttonConfigFileBrowse = new Button(this.container, SWT.PUSH);
		this.buttonConfigFileBrowse.setText("browse");
		this.buttonConfigFileBrowse
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						IWorkbenchWindow window = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow();
						Shell shell = window.getShell();

						FileDialog fileDialog = new FileDialog(shell);
						fileDialog.setText("Select an Ecore2Tg config file");
						fileDialog
								.setFilterExtensions(new String[] { "*.plist" });
						fileDialog
								.setFilterNames(new String[] { "Ecore2Tg-configuration-files (*.plist)" });
						String path = fileDialog.open();

						if (path != null) {
							Ecore2TgFileWizardPage.this.textConfigFile
									.setText(path);
						}
						Ecore2TgFileWizardPage.this
								.setPageComplete(Ecore2TgFileWizardPage.this
										.isComplete());
					}
				});
	}

	/**
	 * @return the Resource of the loaded Ecore schema
	 */
	public Resource getEcoreSchemaResource() {
		return this.ecoreSchema;
	}

	/**
	 * @return the qualified name of the resulting schema
	 */
	public String getQualifiedSchemaName() {
		return this.textTgSchemaName.getText();
	}

	/**
	 * @return the path to the target TG file to save the schema or graph
	 */
	public String getTGTargetFilePath() {
		return this.textTgFile.getText();
	}

	/**
	 * @return the path to the target Ecore model file
	 */
	public String getEcoreInstancePath() {
		return this.textXMIFile.getText();
	}

	/**
	 * @return true if the transformation should use the default options
	 */
	public boolean useDefaultOptions() {
		return this.buttonDefaultOptions.getSelection();
	}

	/**
	 * @return the path to the configuration file if it should be used
	 */
	public String getConfigFilePath() {
		return this.textConfigFile.getText();
	}

	/**
	 * @return a focus listener that decides whether the page is complete
	 */
	private FocusListener getTestFinishFocusListener() {
		return new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (Ecore2TgFileWizardPage.this.isComplete()) {
					Ecore2TgFileWizardPage.this.setPageComplete(true);
				} else {
					Ecore2TgFileWizardPage.this.setPageComplete(false);
				}

			}
		};
	}

	/**
	 * @return whether all necessary fields of this page are filled
	 */
	private boolean isComplete() {
		if (!this.textEcoreFile.getText().isEmpty()
				&& !this.textTgFile.getText().isEmpty()
				&& !this.textTgSchemaName.getText().isEmpty()
				&& (!this.buttonUseConfigFileCheck.getSelection() || !this.textConfigFile
						.getText().isEmpty())) {
			return true;
		}
		return false;
	}
}
