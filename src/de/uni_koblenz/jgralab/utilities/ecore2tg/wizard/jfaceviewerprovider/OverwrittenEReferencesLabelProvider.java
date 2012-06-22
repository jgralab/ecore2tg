package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider;

import java.util.Map.Entry;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import de.uni_koblenz.jgralab.utilities.ecore2tg.Ecore2TgAnalyzer;

public class OverwrittenEReferencesLabelProvider extends LabelProvider
		implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof Entry<?, ?>) {
			@SuppressWarnings("unchecked")
			Entry<EReference, EReference> e = (Entry<EReference, EReference>) element;
			if (columnIndex == 0) {
				return Ecore2TgAnalyzer.getQualifiedReferenceName(e.getKey());
			} else {
				if (e.getValue() == null) {
					return "";
				}
				return Ecore2TgAnalyzer.getQualifiedReferenceName(e.getValue());
			}

		}
		return null;
	}

}
