package CoV2StructureExplorer.selection;

import javafx.collections.ObservableSet;

import java.util.Collection;

// SELECTION
public interface SelectionModel<T> {

    boolean select(T t);

    boolean setSelected(T t, boolean select);

    boolean selectAll(Collection<T> list);

    void clearSelection();

    boolean clearSelection(T t);

    boolean setSelection(Collection<T> list);

    ObservableSet<T> getSelectedItems(); //key thing to monitor set of items

}
