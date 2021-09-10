package PDBViewer.redoundo;

/*
 * Daniel Huson, 2021
 */
public class SimpleCommand implements Command {
    private final String name;
    private final Runnable runUndo;
    private final Runnable runRedo;

    public SimpleCommand(String name, Runnable runUndo, Runnable runRedo) {
        this.name = name;
        this.runUndo = runUndo;
        this.runRedo = runRedo;
    }

    @Override
    public void undo() {
        runUndo.run();
    }

    @Override
    public void redo() {
        runRedo.run();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean canUndo() {
        return runUndo != null;
    }

    @Override
    public boolean canRedo() {
        return runRedo != null;
    }
}
