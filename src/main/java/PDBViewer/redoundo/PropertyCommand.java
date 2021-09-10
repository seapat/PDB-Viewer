package PDBViewer.redoundo;

import javafx.beans.property.Property;

/*
 * Daniel Huson, 2021
 */
public class PropertyCommand<T> extends SimpleCommand {
    public PropertyCommand(String name, Property<T> v, T oldValue, T newValue) {
        super(name, () -> v.setValue(oldValue), () -> v.setValue(newValue));
    }
}
