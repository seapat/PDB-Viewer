package CoV2StructureExplorer.presenter;

import CoV2StructureExplorer.model.PDBFile;
import CoV2StructureExplorer.view.AminoAcidComposition;
import CoV2StructureExplorer.view.WindowController;

public class ChartPresenter {

    public static void setupChartTab(PDBFile model, WindowController controller){
//        var aaComposition = model.getAaComposition();

        controller.getChartTab().setContent(AminoAcidComposition.setup(model,controller));
    }

}
