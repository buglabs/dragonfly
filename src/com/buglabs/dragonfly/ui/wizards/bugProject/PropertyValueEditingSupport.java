package com.buglabs.dragonfly.ui.wizards.bugProject;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

import com.buglabs.dragonfly.ui.info.ServicePropertyHelper;

/**
 * Used in CodeGenerationPage for service properties viewr
 * 
 * @author bballantine
 *
 */
public class PropertyValueEditingSupport extends EditingSupport {

	private final String[] truefalse = new String[] {"true", "false"};
	private Composite parent;
	private TextCellEditor text_editor;
	private ComboBoxCellEditor combobox_editor;
	
	public PropertyValueEditingSupport(ColumnViewer viewer) {
		super(viewer);
		parent =((TableViewer) viewer).getTable();
		text_editor = new TextCellEditor(parent);
		combobox_editor = new ComboBoxCellEditor(parent, new String[0]);
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		ServicePropertyHelper propertyHelper = ((ServicePropertyHelper) element);
		
		if (usesTextEditor(propertyHelper.getValues())) {
			// Ints and blanks use text editor
			return text_editor;
		} else {
			// everything else uses a combobox
			if (hasBools(propertyHelper.getValues()))
				// boolean combos prefill w/ true and false
				combobox_editor.setItems(truefalse);
			else
				// other types, just do set the combobox to values
				combobox_editor.setItems(propertyHelper.getValuesAsArray());
			return combobox_editor;
		}
	}

	@Override
	protected Object getValue(Object element) {
		ServicePropertyHelper propertyHelper = ((ServicePropertyHelper) element);
		
		if (usesTextEditor(propertyHelper.getValues())) {
			return propertyHelper.getSelectedValue();
		} else {
			return Integer.valueOf(propertyHelper.getSelectedIndex());
		}
	}

	@Override
	protected void setValue(Object element, Object value) {
		// Get the current service that's been selected
		
		ServicePropertyHelper propertyHelper = ((ServicePropertyHelper) element);
		
		if (usesTextEditor(propertyHelper.getValues())) {
			// if it's a text field, just set the value
			propertyHelper.setSelectedValue("" + value);
		
		} else if (hasBools(propertyHelper.getValues())) {
			// if it's a boolean, value is an index in truefalse array
			propertyHelper.setSelectedValue(truefalse[Integer.valueOf("" + value)]);
		
		} else {
			// if it's something else, value is an index in the service property values set
			String val = propertyHelper.getValueAt(Integer.valueOf("" + value));
			if (val != null) propertyHelper.setSelectedValue(val);
		}
		
		getViewer().update(element, null);
	}
	
	/**
	 * If we're looking at an integer allow user to edit it by hand
	 * 
	 * @param values
	 * @return
	 */
	private boolean usesTextEditor(List<String> values) {
		return (values.size() < 1 || hasIntegers(values));
	}
	
	private boolean hasIntegers(List<String> values) {
		if (values.size() < 1) return false;
		try {
			Integer.parseInt(values.get(0));
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	private boolean hasBools(List<String> values) {
		if (values.size() == 1) { 
			return isBool(values.get(0));
		}
		if (values.size() == 2) {
			return (isBool(values.get(0)) && isBool(values.get(1)));
		}
		return false;
	}
	
	private boolean isBool(String value) {
		String val = value.toLowerCase().trim();
		return (val.equals("true") || val.equals("false"));
	}

}
