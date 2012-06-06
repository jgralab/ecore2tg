package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class RefDirectionEditingSupport extends EditingSupport {

	private ComboBoxViewerCellEditor editor;

	public RefDirectionEditingSupport(ColumnViewer viewer) {
		super(viewer);
		this.editor = new ComboBoxViewerCellEditor(
				(Composite) viewer.getControl(), SWT.BORDER);
		editor.setLabelProvider(new LabelProvider());
		editor.setContentProvider(new ArrayContentProvider());
		editor.setInput(new String[] { "Yes", "No" });
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return this.editor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		if (element instanceof RefInfoStructure) {
			if (((RefInfoStructure) element).direction) {
				return "Yes";
			} else {
				return "No";
			}
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof RefInfoStructure) {
			if (value.equals("Yes"))
				((RefInfoStructure) element).direction = true;
			else
				((RefInfoStructure) element).direction = false;
		}
		this.getViewer().refresh();
	}

}
