package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class RefTableLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof RefInfoStructure) {
			switch (columnIndex) {
			case 0: // qualified Reference name
				EReference ref = ((RefInfoStructure) element).reference;
				EClass source = ref.getEContainingClass();
				String name = source.getName() + "." + ref.getName();
				EPackage pack = source.getEPackage();
				while (pack != null) {
					name = pack.getName() + "." + name;
					pack = pack.getESuperPackage();
				}

				EClass target = ref.getEReferenceType();
				String targetName = target.getName();
				EPackage tpack = target.getEPackage();
				while (tpack != null) {
					targetName = tpack.getName() + "." + targetName;
					tpack = tpack.getESuperPackage();
				}

				if (ref.getEOpposite() != null) {
					EReference op = ref.getEOpposite();
					name = name + "\n" + "  op: " + targetName + "."
							+ op.getName();
				} else {
					name = name + "\n" + " -> " + targetName;
				}
				return name;
			case 1: // package name
				return ((RefInfoStructure) element).packageName;
			case 2: // direction
				return ((RefInfoStructure) element).direction ? "Yes" : "No";
			case 3: // EdgeClass name
				return ((RefInfoStructure) element).edgeClassName;
			default:
				return "";
			}
		}
		return null;

	}

}
