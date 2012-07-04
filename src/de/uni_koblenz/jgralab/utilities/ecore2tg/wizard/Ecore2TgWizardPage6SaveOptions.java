package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Wizard page to save the entered configurations to a specified file
 * 
 * @author kheckelmann
 * 
 */
public class Ecore2TgWizardPage6SaveOptions extends WizardPage {

	private static final String pageName = "Ecore2Tg - Save configuration";
	private static final String title = "Ecore2Tg - Save configuration";
	private static final String description = "Do you want to save your options?";

	private Composite container;

	private Text fileNameText;

	public Ecore2TgWizardPage6SaveOptions() {
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
		layout.numColumns = 2;

		this.createSaveConfigFileTextAndButton();

		this.setControl(this.container);
		this.setPageComplete(true);
	}

	/**
	 * Create a text field with a browse button and a save button to enter the
	 * configuration file path and save the Ecore2TgConfiguration to the
	 * specified file
	 */
	private void createSaveConfigFileTextAndButton() {
		this.fileNameText = new Text(this.container, SWT.BORDER);
		this.fileNameText.setText("");
		this.fileNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button browseButton = new Button(this.container, SWT.PUSH);
		browseButton.setText("browse");
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IWorkbenchWindow window = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				Shell shell = window.getShell();

				FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
				fileDialog.setText("Select the target plist file");
				fileDialog.setFilterExtensions(new String[] { "*.plist" });
				fileDialog
						.setFilterNames(new String[] { "configuration files (*.plist)" });
				String path = fileDialog.open();

				if (path != null) {
					Ecore2TgWizardPage6SaveOptions.this.fileNameText
							.setText(path);
				}

			}
		});

		Button saveButton = new Button(this.container, SWT.PUSH);
		saveButton.setText("save");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (Ecore2TgWizardPage6SaveOptions.this.fileNameText.getText() != null
						&& !Ecore2TgWizardPage6SaveOptions.this.fileNameText
								.getText().equals("")) {
					((Ecore2TgWizard) Ecore2TgWizardPage6SaveOptions.this
							.getWizard()).getConfiguration()
							.saveConfigurationToFile(Ecore2TgWizardPage6SaveOptions.this.fileNameText
									.getText());
				}
			}
		});
	}

	/**
	 * Set the default path for the configuration file
	 * 
	 * @param s
	 *            the new path of the configuration file
	 */
	public void setFileNameText(String s) {
		this.fileNameText.setText(s);
	}

}
