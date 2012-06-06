package de.uni_koblenz.jgralab.utilities.ecore2tg;

public class EAnnotationKeys {

	// EAnnotation
	public static final String SOURCE_STRING = "grUML";
	public static final String SOURCE_CONFIG = "Tg2Ecore_configuration";
	public static final String SOURCE_STRING_COMMENTS = "grUML_comments";

	public static final String KEY_FOR_EDGECLASS_NAME = "EdgeClass_name";
	public static final String KEY_FOR_PACKAGE_NAME = "Package_name";
	public static final String KEY_IS_AGGREGATION = "Aggregation_value";
	public static final String KEY_IS_GRAPHCLASS = "GraphClass_value";
	public static final String KEY_IS_EDGECLASS = "EdgeClass_value";
	public static final String KEY_IS_RECORDDOMAIN = "record_domain";
	public static final String KEY_FOR_DIRECTION = "Direction_of_reference";
	public static final String KEY_FOR_REF_TO_RECORD = "record_reference";
	public static final String KEY_COMES_FROM_TG2ECORE = "transformed by Tg2Ecore";

	// Comments
	public static final String ECORE_2_TG_CONFIG_FLAG = "ECORE_CONFIG";
	public static final String ECORE_2_TG_METADATA_FLAG = "ECORE_2_TG";
	public static final String ECORE_EANNOTATION_FLAG = "ECORE_EANNOTATION";

	public static final String GENERATED_GRAPHCLASS = " GraphClass added during transformation from Ecore2Tg.";
	public static final String EPACKAGE_NSPREFIX = " nsPrefix=";
	public static final String EPACKAGE_NSURI = " nsURI=";
	public static final String CHANGED_ENUM_LITERAL = " Changed EnumLiteral from to ";
	public static final String INTERFACE = " <<interface>>";
	public static final String GENERATE_DIRECTION_FROM = " direction_to_generated";
	public static final String GENERATE_DIRECTION_TO = " direction_from_generated";
	public static final String GENERATE_DIRECTION_FROM_START = " direction_from_start_generated";
	public static final String GENERATE_DIRECTION_FROM_END = " direction_from_end_generated";
	public static final String GENERATE_DIRECTION_TO_START = " direction_to_start_generated";
	public static final String GENERATE_DIRECTION_TO_END = " direction_to_end_generated";
	public static final String REFERENCE_NAME_TO_START_WAS = " reference_name_to_start_was=";
	public static final String REFERENCE_NAME_TO_TARGET_WAS = " reference_name_to_target_was=";
	public static final String WAS_BIG_INTEGER = " BigInteger was type of EAttribute";
	public static final String WAS_BIG_DECIMAL = " BigDecimal was type of EAttribute";

	public static final String DEFAULT_WAS_ROOT = " The Ecore metamodel had one rootpackage that was transformed to the default Package.";

	public static final String CONTAINMENT_EXISTS = "there exists a containment reference: ";

}
