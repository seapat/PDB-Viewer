package CoV2StructureExplorer.model;

// name could be longer than one letter (technically)
public record Chain(Residue[] residues, char iCode, int idx) {
}
