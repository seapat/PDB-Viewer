package CoV2StructureExplorer.selection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.Collection;
import java.util.HashSet;

// SELECTION
public class SetSelectionModel<T> implements SelectionModel<T> {

    private ObservableSet<T> selectedObjects = FXCollections.observableSet(new HashSet<>());

    @Override
    public boolean select(T t) {
        return selectedObjects.add(t);
    }

    // FIXME: correct implementation?
    @Override
    public boolean setSelected(T t, boolean select) {
        if (select) { return selectedObjects.add(t); }
        return selectedObjects.remove(t);
    }

    @Override
    public boolean selectAll(Collection<T> list) {
        return selectedObjects.addAll(list);
    }

    @Override
    public void clearSelection() {
        selectedObjects = FXCollections.observableSet(new HashSet<>());
    }

    @Override
    public boolean clearSelection(T t) {
        return selectedObjects.remove(t);
    }

    @Override
    public boolean setSelection(Collection<T> list) {
        return selectedObjects.addAll(list);
    }

    @Override
    public ObservableSet<T> getSelectedItems() {
        return selectedObjects;
    }
}
