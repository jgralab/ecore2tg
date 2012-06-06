package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

public class RefNameEditingSupport extends EditingSupport{

	private TextCellEditor cellEditor;
	
	public RefNameEditingSupport(TableViewer viewer) {
		super(viewer);
		this.cellEditor = new TextCellEditor(viewer.getTable());
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
			return ((RefInfoStructure)element).edgeClassName;
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if(element instanceof RefInfoStructure){
			((RefInfoStructure)element).edgeClassName = (String) value;
		}
		this.getViewer().refresh();
	}

}
