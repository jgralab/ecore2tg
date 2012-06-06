package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider;

import org.eclipse.emf.ecore.EReference;

public class RefInfoStructure {

	public EReference reference;
	public String packageName = "";
	public boolean direction = true;;
	public String edgeClassName = "";
	
	public RefInfoStructure(EReference ref){
		this.reference = ref;
	}
	
}
