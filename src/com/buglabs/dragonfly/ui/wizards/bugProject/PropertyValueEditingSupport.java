package com.buglabs.dragonfly.ui.wizards.bugProject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

import com.buglabs.dragonfly.ui.info.ServicePropertyHelper;

/**
 * Used in ServicePropertySelectorDialog for the service properties TableViewer
 * 
 * @author bballantine
 * 
 */
public class PropertyValueEditingSupport extends EditingSupport {

	private final String[] truefalse = new String[] { "true", "false" };
	private Composite parent;
	private TextCellEditor text_editor;
	private ComboBoxCellEditor combobox_editor;
	private Map<ServicePropertyHelper, Object> temp_values;

	public PropertyValueEditingSupport(ColumnViewer viewer) {
		super(viewer);
		parent = ((TableViewer) viewer).getTable();
		text_editor = new TextCellEditor(parent);
		combobox_editor = new ComboBoxCellEditor(parent, new String[0]);
		temp_values = new HashMap<ServicePropertyHelper, Object>();
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
			return getValue(propertyHelper);
		} else {
			return getValueIndex(propertyHelper);
		}
	}

	@Override
	protected void setValue(Object element, Object value) {
		// Get the current service that's been selected

		ServicePropertyHelper propertyHelper = ((ServicePropertyHelper) element);

		if (usesTextEditor(propertyHelper.getValues())) {
			// if it's a text field, just set the value
			temp_values.put(propertyHelper, "" + value);

		} else {
			// if it's a boolean, value is an index in truefalse array
			temp_values.put(propertyHelper, Integer.valueOf("" + value));
		}

		getViewer().update(element, null);
	}

	/**
	 * Commit Le Changes
	 * 
	 * We've kept our changes in the temp_values map, keyed off of the
	 * propertyHelper objects now we iterate across the map, making the temp
	 * values permanent
	 * 
	 * This allows us to cancel our changes in the viewer
	 */
	public void commitChanges() {
		Set<ServicePropertyHelper> keys = temp_values.keySet();
		for (ServicePropertyHelper helper : keys) {
			helper.setSelectedValue(getLabel(helper));
		}
	}

	/**
	 * The editing support keeps temp values for items the user has modified
	 * these should be used for String row labels, etc. Here we do the work for
	 * figuring out what the label should be based on the type of property and
	 * temp_value.l
	 * 
	 * @param helper
	 * @return
	 */
	public String getLabel(ServicePropertyHelper helper) {
		String label = "";
		if (temp_values.containsKey(helper)) {
			Object o = temp_values.get(helper);
			if (o instanceof Integer) {
				if (hasBools(helper.getValues()))
					label = truefalse[(Integer) o];
				else
					label = helper.getValueAt((Integer) o);
			} else {
				label = (String) temp_values.get(helper);
			}
		} else {
			label = helper.getSelectedValue();
		}
		return label;
	}

	/**
	 * Gets the temporary value if it finds it
	 * 
	 * @param helper
	 * @return
	 */
	private String getValue(ServicePropertyHelper helper) {
		return (temp_values.containsKey(helper)) ? (String) temp_values.get(helper) : helper.getSelectedValue();
	}

	/**
	 * Gets the temporary value of something stored as an index if it finds it
	 * 
	 * @param helper
	 * @return
	 */
	private Integer getValueIndex(ServicePropertyHelper helper) {
		return (temp_values.containsKey(helper)) ? (Integer) temp_values.get(helper) : Integer.valueOf(helper.getSelectedIndex());
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
		if (values.size() < 1)
			return false;
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
