package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider;

import de.uni_koblenz.jgralab.schema.EdgeClass;

public class EcInfoStructure {

	public EdgeClass edgeClass;
	public String addToRoleName;
	public String addFromRoleName;

	public EcInfoStructure(EdgeClass ec) {
		this.edgeClass = ec;
		this.addToRoleName = "";
		this.addFromRoleName = "";
	}

	@Override
	public String toString() {
		return this.edgeClass.getQualifiedName() + " :: from:"
				+ this.addFromRoleName + " :: to: " + this.addToRoleName;
	}
}
