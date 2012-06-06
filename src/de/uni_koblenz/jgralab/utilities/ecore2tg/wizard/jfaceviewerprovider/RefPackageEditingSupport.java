package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class RefPackageEditingSupport extends EditingSupport {

	private ComboBoxViewerCellEditor cellEditor = null;
	
	public RefPackageEditingSupport(ColumnViewer viewer, String[] packageNames) {
		super(viewer);
		this.cellEditor = new ComboBoxViewerCellEditor((Composite) viewer.getControl(), SWT.READ_ONLY);
		cellEditor.setLabelProvider(new LabelProvider());
		cellEditor.setContentProvider(new ArrayContentProvider());
		cellEditor.setInput(packageNames);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return this.cellEditor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		if(element instanceof RefInfoStructure){
			return ((RefInfoStructure)element).packageName;
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if(element instanceof RefInfoStructure){
			((RefInfoStructure)element).packageName = (String) value;
		}
		this.getViewer().refresh();
	}

	
}
