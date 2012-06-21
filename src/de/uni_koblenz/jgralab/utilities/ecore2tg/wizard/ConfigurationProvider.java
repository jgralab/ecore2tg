package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard;

import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgConfiguration;

public interface ConfigurationProvider {

	public void enterConfiguration(Ecore2TgConfiguration conf);

	public void saveConfiguration(Ecore2TgConfiguration conf);
}
