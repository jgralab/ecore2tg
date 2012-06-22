package de.uni_koblenz.jgralab.utilities.ecore2tg.wizard.jfaceviewerprovider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class OverwrittenEReferencesColumnEditingSupport extends EditingSupport {

	private HashMap<EReference, HashSet<EReference>> refmap;

	public OverwrittenEReferencesColumnEditingSupport(ColumnViewer viewer,
			HashMap<EReference, HashSet<EReference>> rm) {
		super(viewer);
		this.refmap = rm;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected CellEditor getCellEditor(Object element) {
		if (element instanceof Entry<?, ?>) {
			ComboBoxViewerCellEditor editor = new ComboBoxViewerCellEditor(
					(Composite) this.getViewer().getControl(), SWT.READ_ONLY);
			editor.setLabelProvider(new EReferenceLabelProvider());
			editor.setContentProvider(new ArrayContentProvider());
			editor.setInput(this.refmap.get(
					((Entry<EReference, EReference>) element).getKey())
					.toArray(new EReference[] {}));
			return editor;
		}
		return null;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object getValue(Object element) {
		if (element instanceof Entry<?, ?>) {
			return ((Entry<EReference, EReference>) element).getValue();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof Entry<?, ?>) {
			((Entry<EReference, EReference>) element)
					.setValue((EReference) value);
		}
		this.getViewer().refresh();
	}

}
