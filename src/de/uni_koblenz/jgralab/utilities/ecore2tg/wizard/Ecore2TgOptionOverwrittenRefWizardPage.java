package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration;

public class Ecore2TgOptionOverwrittenRefWizardPage extends WizardPage
		implements ConfigurationProvider {

	private static final String pageName = "Ecore2Tg - Overwriting Options";
	private static final String title = "Ecore2Tg - Overwriting Options";
	private static final String description = "Specify which EReferences a given EReference overwrites";

	private Composite container;

	public Ecore2TgOptionOverwrittenRefWizardPage() {
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

		this.setControl(this.container);
		this.setPageComplete(true);
	}

	@Override
	public void enterConfiguration(Ecore2TgConfiguration conf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveConfiguration(Ecore2TgConfiguration conf) {
		// TODO Auto-generated method stub

	}
}
