package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard;

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

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * WizardPage to enter mandatory informations for the Tg2Ecore transformation:
 * the path to the TG file with the original JGraLab schema (and graph) to
 * transform and the paths of the resulting Ecore schema and model, whether
 * default options should be used and if a configuration file should become
 * loaded
 * 
 * @author kheckelmann
 * 
 */
public class Tg2EcoreWizardPage1Files extends WizardPage {

	private static final String pageName = "Tg2EcoreFileChoose";
	private static final String title = "Tg2Ecore";
	private static final String description = "";

	private Composite container;
	private Text textTgFile;
	private Text textEcoreFile;
	private Text textXMIFile;
	private Button buttonDefaultOptions;
	private Button buttonUseConfigFileCheck;
	private Text textConfigFile;
	private Button buttonConfigFileBrowse;
	private Button buttonBack2Ecore;

	private Schema tgSchema;

	protected Tg2EcoreWizardPage1Files() {
		super(pageName);
		this.setTitle(title);
		this.setDescription(description);
		this.setControl(this.textTgFile);
	}

	@Override
	public void createControl(Composite parent) {
		this.container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		this.container.setLayout(layout);
		layout.numColumns = 3;

		this.createEnterTgFileControls();

		this.createEnterEcoreFileControl();
		this.createEnterXMIFileControl();

		this.createUseDefaultOptionsControl();

		this.createLoadConfigFileControl();

		this.createButtonBack2EcoreControl();

		this.setControl(this.container);
		this.setPageComplete(false);
	}

	/**
	 * Create a text field to enter a path to an JGralLab schema or graph with a
	 * browse button
	 */
	private void createEnterTgFileControls() {
		Label labelTgFile = new Label(this.container, SWT.NULL);
		labelTgFile.setText("TG file:");

		this.textTgFile = new Text(this.container, SWT.BORDER | SWT.SINGLE);
		this.textTgFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.textTgFile.setText("");
		this.textTgFile.addFocusListener(this.getTestFinishFocusListener());

		Button buttonSelectTgFile = new Button(this.container, SWT.PUSH);
		buttonSelectTgFile.setText("browse");
		buttonSelectTgFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IWorkbenchWindow window = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				Shell shell = window.getShell();

				FileDialog fileDialog = new FileDialog(shell);
				fileDialog.setText("Select a TG file");
				fileDialog.setFilterExtensions(new String[] { "*.tg" });
				fileDialog.setFilterNames(new String[] { "TG-Files (*.tg)" });
				String path = fileDialog.open();

				if (path != null) {
					Tg2EcoreWizardPage1Files.this.textTgFile.setText(path);
					try {
						Tg2EcoreWizardPage1Files.this.tgSchema = GraphIO
								.loadSchemaFromFile(path);
						if (Tg2EcoreWizardPage1Files.this.isComplete()) {
							Tg2EcoreWizardPage1Files.this.setPageComplete(true);
						}
					} catch (GraphIOException e1) {
						Tg2EcoreWizardPage1Files.this.textTgFile.setText("");
						Tg2EcoreWizardPage1Files.this.setPageComplete(false);
						e1.printStackTrace();
					}
				} else {
					Tg2EcoreWizardPage1Files.this.setPageComplete(false);
				}
			}
		});
	}

	/**
	 * Creates a text fielt to enter the path to the target Ecore schema with a
	 * browse button
	 */
	private void createEnterEcoreFileControl() {
		Label labelEcoreFile = new Label(this.container, SWT.NULL);
		labelEcoreFile.setText("Target Ecore file: ");

		this.textEcoreFile = new Text(this.container, SWT.BORDER | SWT.SINGLE);
		this.textEcoreFile.setText("");
		this.textEcoreFile
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.textEcoreFile.addFocusListener(this.getTestFinishFocusListener());

		Button buttonSelectEcoreFile = new Button(this.container, SWT.PUSH);
		buttonSelectEcoreFile.setText("browse");
		buttonSelectEcoreFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IWorkbenchWindow window = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				Shell shell = window.getShell();

				FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
				fileDialog.setText("Select the target Ecore file");
				fileDialog.setFilterExtensions(new String[] { "*.ecore" });
				fileDialog.setFileName("Ecore-files (*.ecore)");
				String path = fileDialog.open();

				if (path != null) {
					Tg2EcoreWizardPage1Files.this.textEcoreFile.setText(path);
					if (!Tg2EcoreWizardPage1Files.this.textTgFile.getText()
							.isEmpty()) {
						Tg2EcoreWizardPage1Files.this.setPageComplete(true);
					}
				} else {
					Tg2EcoreWizardPage1Files.this.setPageComplete(false);
				}

			}
		});
	}

	/**
	 * Creates a text field to enter a path to the target Ecore model with a
	 * browse button
	 */
	private void createEnterXMIFileControl() {
		Label labelEcoreFile = new Label(this.container, SWT.NULL);
		labelEcoreFile.setText("Target XMI file: ");

		this.textXMIFile = new Text(this.container, SWT.BORDER | SWT.SINGLE);
		this.textXMIFile.setText("");
		this.textXMIFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.textXMIFile.addFocusListener(this.getTestFinishFocusListener());

		Button buttonSelectFile = new Button(this.container, SWT.PUSH);
		buttonSelectFile.setText("browse");
		buttonSelectFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IWorkbenchWindow window = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				Shell shell = window.getShell();

				FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
				fileDialog.setText("Select the target model file");
				String path = fileDialog.open();

				if (path != null) {
					Tg2EcoreWizardPage1Files.this.textXMIFile.setText(path);
				}

			}
		});
	}

	/**
	 * Create a check button to decide whether to use the default options
	 */
	private void createUseDefaultOptionsControl() {
		this.buttonDefaultOptions = new Button(this.container, SWT.CHECK);
		this.buttonDefaultOptions.setText("Use default options.");
		this.buttonDefaultOptions.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button b = (Button) e.widget;
				if (b.getSelection()) {
					Tg2EcoreWizardPage1Files.this.buttonUseConfigFileCheck
							.setEnabled(false);
					Tg2EcoreWizardPage1Files.this.buttonBack2Ecore
							.setEnabled(false);
				} else {
					Tg2EcoreWizardPage1Files.this.buttonUseConfigFileCheck
							.setEnabled(true);
					Tg2EcoreWizardPage1Files.this.buttonBack2Ecore
							.setEnabled(true);
				}
				Tg2EcoreWizardPage1Files.this
						.setPageComplete(Tg2EcoreWizardPage1Files.this
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
	private void createLoadConfigFileControl() {
		this.buttonUseConfigFileCheck = new Button(this.container, SWT.CHECK);
		this.buttonUseConfigFileCheck.setText("Load config file");
		this.buttonUseConfigFileCheck
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Button b = (Button) e.widget;
						if (b.getSelection()) {
							Tg2EcoreWizardPage1Files.this.buttonDefaultOptions
									.setEnabled(false);
							Tg2EcoreWizardPage1Files.this.buttonBack2Ecore
									.setEnabled(false);
						} else {
							Tg2EcoreWizardPage1Files.this.buttonDefaultOptions
									.setEnabled(true);
							Tg2EcoreWizardPage1Files.this.buttonBack2Ecore
									.setEnabled(true);
						}
						Tg2EcoreWizardPage1Files.this
								.setPageComplete(Tg2EcoreWizardPage1Files.this
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
						fileDialog.setText("Select an Tg2Ecore config file");
						fileDialog
								.setFilterExtensions(new String[] { "*.plist" });
						fileDialog
								.setFilterNames(new String[] { "Tg2Ecore-configuration-files (*.plist)" });
						String path = fileDialog.open();

						if (path != null) {
							Tg2EcoreWizardPage1Files.this.textConfigFile
									.setText(path);
						}
						Tg2EcoreWizardPage1Files.this
								.setPageComplete(Tg2EcoreWizardPage1Files.this
										.isComplete());
					}
				});
	}

	/**
	 * Creates a check button to indicate that this is a BACK TO ECORE
	 * transformation and therefore comments should be used
	 */
	private void createButtonBack2EcoreControl() {
		this.buttonBack2Ecore = new Button(this.container, SWT.CHECK);
		this.buttonBack2Ecore
				.setText("This is a BACK transformation, use the comments.");
		GridData data = new GridData();
		data.horizontalSpan = 3;
		this.buttonBack2Ecore.setLayoutData(data);
		this.buttonBack2Ecore.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button b = (Button) e.widget;
				boolean enable;
				if (b.getSelection()) {
					enable = false;
				} else {
					enable = true;
				}

				Tg2EcoreWizardPage1Files.this.buttonDefaultOptions
						.setEnabled(enable);
				Tg2EcoreWizardPage1Files.this.buttonUseConfigFileCheck
						.setEnabled(enable);
				Tg2EcoreWizardPage1Files.this
						.setPageComplete(Tg2EcoreWizardPage1Files.this
								.isPageComplete());
			}

		});
	}

	/**
	 * @return the path of the original TG file
	 */
	public String getTgFilePath() {
		return this.textTgFile.getText();
	}

	/**
	 * @return the path to the target Ecore schema file
	 */
	public String getEcoreFilePath() {
		return this.textEcoreFile.getText();
	}

	/**
	 * @return the path to the target Ecore model file
	 */
	public String getXMIFilePath() {
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
	 * @return whether this is a back transformation
	 */
	public boolean isBackToEcore() {
		return this.buttonBack2Ecore.getSelection();
	}

	/**
	 * @return the JGraLab schema to transform
	 */
	public Schema getOriginalSchema() {
		return this.tgSchema;
	}

	/**
	 * @return a focus listener that decides whether the page is complete
	 */
	private FocusListener getTestFinishFocusListener() {
		return new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (Tg2EcoreWizardPage1Files.this.isComplete()) {
					Tg2EcoreWizardPage1Files.this.setPageComplete(true);
				} else {
					Tg2EcoreWizardPage1Files.this.setPageComplete(false);
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
				&& (!this.buttonUseConfigFileCheck.getSelection() || !this.textConfigFile
						.getText().isEmpty())) {
			return true;
		}
		return false;
	}
}
