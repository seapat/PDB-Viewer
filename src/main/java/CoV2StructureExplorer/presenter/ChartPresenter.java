package CoV2StructureExplorer.presenter;

import CoV2StructureExplorer.model.PDBFile;
import CoV2StructureExplorer.view.AminoAcidComposition;
import CoV2StructureExplorer.view.WindowController;

public class ChartPresenter {

    public static void setupChartTab(PDBFile model, WindowController controller){

        // TODO: maybe add option to choose if you want to use the flip() function in AminoAcidComposition

        controller.getChartTab().setContent(AminoAcidComposition.setup(model,controller));
    }

}
