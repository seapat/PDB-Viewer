package CoV2StructureExplorer.redoundo;

/*
 * Daniel Huson, 2021
 */
public interface Command {
    void undo();

    void redo();

    String name();

    boolean canUndo();

    boolean canRedo();
}
