package de.uni_koblenz.jgralab.utilities.ecore2tg;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.riediger.plist.PList;
import org.riediger.plist.PListDict;
import org.riediger.plist.PListException;

public class Tg2EcoreConfiguration {

	// #########################################################
	// ##### User defined Options ##############################
	// #########################################################

	/**
	 * Option to declare a back transformation. If true, comments set from
	 * Ecore2Tg are searched and considered.
	 */
	private boolean option_backToEcore = true;

	/**
	 * Option to declare that Edges without Inheritance, without Attributes and
	 * with only one role name are transformed to an one directional EReference.
	 */
	private boolean option_oneroleToUni = false;

	/**
	 * Option to declare whether the GraphClass should become transformed to an
	 * EClass. Does not has an effect if option_backToEcore is set.
	 */
	private boolean option_transformGraphClass = true;

	/**
	 * Option to declare whether the GraphClass should become the root element
	 * of the resulting metamodel. Doesn't work, if the
	 * option_transformGraphClass is not set.
	 */
	private boolean option_makeGraphClassToRootElement = false;

	/**
	 * Option to set the name of the resulting rootpackage, if no name is set
	 * "rootpackage" is chosen.
	 */
	private String option_rootpackageName;

	/**
	 * Option to set the nsPrefix of the rootpackage, if no nsPrefix is set, the
	 * name of the GraphClass is chosen.
	 */
	private String option_nsPrefix;

	/**
	 * Option to set the nsURI of the rootpackage, if no nsURI is set, the
	 * default value is "http://"+nsPrefix+"/1.0/".
	 */
	private String option_nsURI;

	/**
	 * Option to set the two additional rolenames for an conceptual EdgeClass in
	 * Ecore. Maps the name of the EdgeClass and EdgeDirection to the additional
	 * rolename.
	 */
	private final HashMap<String, HashMap<EdgeDirection, String>> option_definerolenames;

	/**
	 * Enumeration that saves the edge direction, From or To
	 */
	public enum EdgeDirection {
		To, From;
	}

	public Tg2EcoreConfiguration() {
		this.option_definerolenames = new HashMap<String, HashMap<EdgeDirection, String>>();
	}

	// -----------------------------------------------------------

	/**
	 * @return if the transformation is a back transformation for Ecore2Tg
	 */
	public boolean isOption_backToEcore() {
		return this.option_backToEcore;
	}

	/**
	 * Transforms the schema to the metamodel considering comments set from
	 * Ecore2Tg if set. The default value is <code>false</code>. If the option
	 * is set, other options doesn't work, cause they become overwritten by the
	 * comments.
	 * 
	 * @param option_backToEcore
	 *            if the transformation is a back transformation
	 */
	public void setOption_backToEcore(boolean option_backToEcore) {
		this.option_backToEcore = option_backToEcore;
	}

	/**
	 * @return if the transformation should transform edges with only one
	 *         rolename to unidirectional EReferences
	 */
	public boolean isOption_oneroleToUni() {
		return this.option_oneroleToUni;
	}

	/**
	 * Transforms edges with only one role name to unidirectional EReferences if
	 * set. The default value is <code>false</code>
	 * 
	 * @param option_oneroleToUni
	 *            if edges with only one role name should become transformed to
	 *            unidirectional EReferences
	 */
	public void setOption_oneroleToUni(boolean option_oneroleToUni) {
		this.option_oneroleToUni = option_oneroleToUni;
	}

	/**
	 * @return if the GraphClass should become transformed
	 */
	public boolean isOption_transformGraphClass() {
		return this.option_transformGraphClass;
	}

	/**
	 * Transforms the GraphClass into an EClass if set. Otherwise, the
	 * GraphClass is ignored. The default value is <code>true</code>.
	 * 
	 * @param option_transformGraphClass
	 *            if the GraphClass should become transformed
	 */
	public void setOption_transformGraphClass(boolean option_transformGraphClass) {
		this.option_transformGraphClass = option_transformGraphClass;
	}

	/**
	 * @return if the GraphClass should become the root element
	 */
	public boolean isOption_makeGraphClassToRootElement() {
		return this.option_makeGraphClassToRootElement;
	}

	/**
	 * Transforms the GraphClass to the root element of the metamodel. Does only
	 * work if option_transformGraphClass is set.
	 * 
	 * @param mg
	 *            if the GraphClass should become the root element
	 */
	public void setOption_makeGraphClassToRootElement(boolean mg) {
		this.option_makeGraphClassToRootElement = mg;
	}

	/**
	 * @return the name of the rootpackage of the resulting metamodel
	 */
	public String getOption_rootpackageName() {
		return this.option_rootpackageName;
	}

	/**
	 * Sets the name of the rootpackage of the resulting metamodel. If no name
	 * is set "rootpackage" is chosen.
	 * 
	 * @param rootpackageName
	 *            the rootpackage name of the resulting metamodel
	 */
	public void setOption_rootpackageName(String rootpackageName) {
		this.option_rootpackageName = rootpackageName;
	}

	/**
	 * @return the nsPrefix of the resulting metamodels rootpackage
	 */
	public String getOption_nsPrefix() {
		return this.option_nsPrefix;
	}

	/**
	 * Sets the nsPrefix of the resulting metamodels rootpackage. If no nsPrefix
	 * is set, the GraphClass's name is chosen.
	 * 
	 * @param nsPrefix
	 *            the nsPrefix of the resulting metamodels rootpackage
	 */
	public void setOption_nsPrefix(String nsPrefix) {
		this.option_nsPrefix = nsPrefix;
	}

	/**
	 * @return the nsURI of the resulting metamodels rootpackage
	 */
	public String getOption_nsURI() {
		return this.option_nsURI;
	}

	/**
	 * Sets the nsURI of the resulting metamodels rootpackage. If no nsURI is
	 * set, the default value is "http://"+nsPrefix+"/1.0/".
	 * 
	 * @param nsURI
	 *            the nsURI of the resulting metamodels rootpackage
	 */
	public void setOption_nsURI(String nsURI) {
		this.option_nsURI = nsURI;
	}

	/**
	 * Defines an additional rolename for the Ecore metamodel. It is possible to
	 * define one additional rolename for the from direction and one for the for
	 * direction.
	 * 
	 * @param edgename
	 *            Name of the EdgeClass to define the rolename for
	 * @param direction
	 *            Direction of the rolename
	 * @param rolename
	 *            the rolename to set
	 */
	public void addOption_definerolenames(String edgename,
			EdgeDirection direction, String rolename) {

		if (this.option_definerolenames.containsKey(edgename)) {
			this.option_definerolenames.get(edgename).put(direction, rolename);
		} else {
			HashMap<EdgeDirection, String> entry = new HashMap<EdgeDirection, String>();
			entry.put(direction, rolename);
			this.option_definerolenames.put(edgename, entry);
		}
	}

	public Map<String, HashMap<EdgeDirection, String>> getOption_definerolenames() {
		return Collections.unmodifiableMap(this.option_definerolenames);
	}

	/**
	 * Loads the given configuration files and set the options
	 * 
	 * @param uri
	 *            of the configuration file
	 */
	public static Tg2EcoreConfiguration fillWithConfigurationsFromFile(
			String uri) {
		PList x = new PList();
		try {
			x.loadFrom(uri);
		} catch (PListException e) {
			System.err
					.println("Error while loading the configuration file with uri "
							+ uri);
			e.printStackTrace();
			return null;
		}
		PListDict ds = x.getDict();
		Tg2EcoreConfiguration config = new Tg2EcoreConfiguration();
		if (ds.containsKey("ecore_backtransformation")) {
			config.setOption_backToEcore(ds
					.getBoolean("ecore_backtransformation"));
		}
		if (ds.containsKey("transform_one_role_to_uni")) {
			config.setOption_oneroleToUni(ds
					.getBoolean("transform_one_role_to_uni"));
		}
		if (ds.containsKey("transform_graphclass")) {
			config.setOption_transformGraphClass(ds
					.getBoolean("transform_graphclass"));
		}
		if (ds.containsKey("maek_graphclass_to_rootelement")) {
			config.setOption_makeGraphClassToRootElement(ds
					.getBoolean("maek_graphclass_to_rootelement"));
		}
		if (ds.containsKey("rootpackage_name")) {
			config.setOption_rootpackageName(ds.getString("rootpackage_name"));
		}
		if (ds.containsKey("ns_prefix")) {
			config.setOption_nsPrefix(ds.getString("ns_prefix"));
		}
		if (ds.containsKey("ns_uri")) {
			config.setOption_nsURI(ds.getString("ns_uri"));
		}
		if (ds.containsKey("define_rolenames")) {
			List<String> rns = ds.getArray("define_rolenames");
			for (String entry : rns) {
				String[] content = entry.split(", ");
				assert content.length == 3;
				EdgeDirection dir;
				if (content[1].equals("TO")) {
					dir = EdgeDirection.To;
				} else if (content[1].equals("FROM")) {
					dir = EdgeDirection.From;
				} else {
					System.err
							.println("Invalid direction for defined rolenames. "
									+ entry
									+ " is not a valid entry. It will become ignored.");
					continue;
				}
				config.addOption_definerolenames(content[0], dir, content[2]);
			}
		}
		return config;
	}
}
