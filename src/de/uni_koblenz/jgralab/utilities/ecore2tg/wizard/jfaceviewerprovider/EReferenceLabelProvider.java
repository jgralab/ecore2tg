package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.jface.viewers.LabelProvider;

import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgAnalyzer;

public class EReferenceLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof EReference) {
			return Ecore2TgAnalyzer
					.getQualifiedReferenceName((EReference) element);
		}
		return null;
	}

}
