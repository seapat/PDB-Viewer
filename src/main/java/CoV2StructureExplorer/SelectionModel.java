package CoV2StructureExplorer;

import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import java.util.Collection;

public interface SelectionModel<T> {

    boolean select(T t);
    boolean setSelected(T t, boolean select);
    boolean selectAll(Collection<T> list);

    void clearSelection();
    boolean clearSelection(T t);
    boolean setSelection(Collection<T> list);

    ObservableSet<T> getSelectedItems();

}
