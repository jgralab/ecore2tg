package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class EcTableLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof EcInfoStructure) {
			switch (columnIndex) {
			case 0: // qualified name
				return ((EcInfoStructure) element).edgeClass.getQualifiedName();
			case 1: // TO role name
				return ((EcInfoStructure) element).addToRoleName;
			case 2: // FROM role name
				return ((EcInfoStructure) element).addFromRoleName;
			}
		}
		return null;
	}

}
