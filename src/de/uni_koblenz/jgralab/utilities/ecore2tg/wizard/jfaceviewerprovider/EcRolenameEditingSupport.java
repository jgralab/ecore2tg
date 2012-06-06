package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

public class EcRolenameEditingSupport extends EditingSupport {

	private TextCellEditor cellEditor;
	private boolean isFrom;

	public EcRolenameEditingSupport(TableViewer viewer, boolean isFrom) {
		super(viewer);
		this.cellEditor = new TextCellEditor(viewer.getTable());
		this.isFrom = isFrom;
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
		if (element instanceof EcInfoStructure) {
			if (this.isFrom) {
				return ((EcInfoStructure) element).addFromRoleName;
			} else {
				return ((EcInfoStructure) element).addToRoleName;
			}
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof EcInfoStructure) {
			if (this.isFrom) {
				((EcInfoStructure) element).addFromRoleName = (String) value;
			} else {
				((EcInfoStructure) element).addToRoleName = (String) value;
			}
		}
		this.getViewer().refresh();
	}

}
