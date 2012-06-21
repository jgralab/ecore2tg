package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider;

import java.util.Map.Entry;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import de.uni_koblenz.jgralab.utilities.ecore2tg.plugin.Activator;

public class EcChooseLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	private static final Image CHECKED = Activator.getImageDescriptor(
			"icons/checked.gif").createImage();
	private static final Image UNCHECKED = Activator.getImageDescriptor(
			"icons/unchecked.gif").createImage();

	@SuppressWarnings("unchecked")
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof Entry<?, ?>) {
			if (columnIndex == 0) {
				if (((Entry<String, Boolean>) element).getValue()) {
					return CHECKED;
				} else {
					return UNCHECKED;
				}
			}
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof Entry<?, ?>) {
			if (columnIndex == 1) {
				return ((Entry<?, ?>) element).getKey().toString();
			}
		}
		return null;
	}

}
