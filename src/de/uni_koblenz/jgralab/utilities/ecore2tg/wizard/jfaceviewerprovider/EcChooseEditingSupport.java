package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider;

import java.util.Map.Entry;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.widgets.Composite;

public class EcChooseEditingSupport extends EditingSupport {

	public EcChooseEditingSupport(ColumnViewer viewer) {
		super(viewer);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new CheckboxCellEditor((Composite) this.getViewer().getControl());
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		if (element instanceof Entry<?, ?>) {
			return ((Entry<?, ?>) element).getValue();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof Entry<?, ?>) {
			((Entry<String, Boolean>) element).setValue((Boolean) value);
		}
		this.getViewer().refresh();
	}

}
