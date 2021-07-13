package CoV2StructureExplorer.presenter;

import CoV2StructureExplorer.model.Atom;
import CoV2StructureExplorer.model.Chain;
import CoV2StructureExplorer.selection.SelectionDots;
import CoV2StructureExplorer.selection.SetSelectionModel;
import CoV2StructureExplorer.model.PDBFile;
import CoV2StructureExplorer.view.Balls;
import CoV2StructureExplorer.view.Mesh;
import CoV2StructureExplorer.view.Sticks;
import CoV2StructureExplorer.view.WindowController;
import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.SetChangeListener;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

import java.util.*;

public class ViewPresenter {
    
    /* 
    TODO : Das wurde glaub nur im meeting besprochen... du musst einfach den mittelpunkt der einzelnen moleküle berechnen und diesen vektor normalisieren
     */

    private Double maxX;
    private Double maxY;
    private Double maxZ;
    private Double minX;
    private Double minY;
    private Double minZ;
    private final Group figure;

    ViewPresenter(WindowController controller, PDBFile model){
        controller.getFigurePane().getChildren().clear();

        this.figure = setupView(controller, model);
        SubScene subScene = new SubScene(this.figure, 800, 800, true, SceneAntialiasing.DISABLED);
        subScene.widthProperty().bind(controller.getFigurePane().widthProperty());
        subScene.heightProperty().bind(controller.getFigurePane().heightProperty());

        setupRotation(controller);
        subScene.setCamera(setupCamera(controller));

        controller.getFigurePane().getChildren().add(subScene);
    }

    protected Group setupView(WindowController controller, PDBFile model) {
        Group figure = new Group();

        var mesh = new Mesh(model.getProtein(), controller.getModelChoice().getValue());
        mesh.setVisible(false);
        mesh.visibleProperty().bind(controller.getRibbonChecked().selectedProperty());

        var sticks = new Sticks(model.getProtein(),
                controller.getDiameterScale().valueProperty(),
                controller.getModelChoice().getValue()
        );
        sticks.setVisible(false);
        sticks.visibleProperty().bind(controller.getBondsChecked().selectedProperty());

        SetSelectionModel<Atom> selectedAtoms =  new SetSelectionModel<>();
        var balls = new Balls(model.getProtein(),
                controller.getRadiusScale().valueProperty(),
                controller.getModelChoice().getValue(),
                selectedAtoms
        );
        balls.setVisible(false);
        balls.visibleProperty().bind(controller.getAtomsChecked().selectedProperty());
        balls.changeColor(controller.getColorChoice().getValue(), controller);

        // SELECTION
        selectedAtoms.getSelectedItems().addListener((SetChangeListener<? super Atom>) e -> System.out.println("setchange: ObsSet changes"));
        selectedAtoms.getSelectedItems().addListener((InvalidationListener) e -> System.out.println("invalid: ObsSet changes"));
        SelectionDots.setup(controller.getSelectionPane(), selectedAtoms, atom -> {
            System.out.println("Lambda is executed");
            return (List<? extends Shape3D>) balls.getModelToView().get(atom.getResidue().getChain()).get(atom.getResidue()).values();
        }, balls.layoutXProperty(), balls.layoutYProperty());

        // FIXME: Maybe write this as a single loop over all atoms? might be faster overall
//        this.maxX = balls.getChildren().stream().map(x -> x.translateXProperty().getValue()).max(Double::compare).orElse(0d);
//        this.maxY = balls.getChildren().stream().map(y -> y.translateYProperty().getValue()).max(Double::compare).orElse(0d);
//        this.maxZ = balls.getChildren().stream().map(z -> z.translateZProperty().getValue()).max(Double::compare).orElse(0d);
//        this.minX = balls.getChildren().stream().map(x -> x.translateXProperty().getValue()).min(Double::compare).orElse(0d);
//        this.minY = balls.getChildren().stream().map(y -> y.translateYProperty().getValue()).min(Double::compare).orElse(0d);
//        this.minZ = balls.getChildren().stream().map(z -> z.translateZProperty().getValue()).min(Double::compare).orElse(0d);

        this.maxX = sticks.getLayoutBounds().getMaxX();
        this.maxY = sticks.getLayoutBounds().getMaxY();
        this.maxZ = sticks.getLayoutBounds().getMaxZ();
        this.minX = sticks.getLayoutBounds().getMinX();
        this.minY = sticks.getLayoutBounds().getMinY();
        this.minZ = sticks.getLayoutBounds().getMinZ();
//        this.centerX = balls.getLayoutBounds().getMinZ();

        // populate focus choice
        controller.getFocusChoice().getItems().clear();
        controller.getFocusChoice().getItems().add("All");
        model.getProtein().get(0).forEach( chain ->  controller.getFocusChoice().getItems().add(String.valueOf(chain.getChainID())));

        controller.getColorChoice().valueProperty().addListener(e -> {
                    if ((controller.getColorChoice().getValue() != null))
                    balls.changeColor(controller.getColorChoice().getValue(), controller);
        }

        );
        controller.getFocusChoice().setValue("All");


//        figure.getChildren().addAll(mesh, sticks, balls);
        figure.getChildren().add(sticks);
        figure.getChildren().add(balls);
        figure.getChildren().add(mesh);
//         figure.getChildren().addAll(mesh.getChildren());

        controller.getFocusChoice().valueProperty().addListener( e ->
                balls.highlightChain(controller.getFocusChoice().getValue(),
                        controller.getColorChoice().getValue()

                ) );

        return figure;
    }



    public Camera setupCamera(WindowController controller) {
        var camera = new PerspectiveCamera(true);

        var maxFromCenter = Math.abs(Collections.max(
                Arrays.asList(this.minX, this.minZ, this.minY, this.maxX, this.maxY, this.maxZ),
                Comparator.comparingDouble(Math::abs))
        );
        System.out.println("maxFromCenter " + maxFromCenter);
        camera.setFarClip(10 * maxFromCenter);
        camera.setNearClip(0.1);
        camera.setTranslateX((this.maxX + this.minX) / 2);
        camera.setTranslateY((this.maxY + this.minY) / 2);
        camera.setTranslateZ(-2000 * Math.log(maxFromCenter));

        System.out.println("maxX " + this.maxX);
        System.out.println("minX " + this.minX);
        System.out.println("maxY " + this.maxY);
        System.out.println("minY " + this.minY);
        System.out.println("maxZ " + this.maxZ);
        System.out.println("minZ " + this.minZ);

        controller.getFigurePane().setOnScroll((ScrollEvent e) -> {
            var curr = camera.getTranslateZ();

            camera.setTranslateZ(curr + (e.getDeltaY() / Math.abs(e.getDeltaY())) * (Math.abs(maxFromCenter * 0.1) + 1));
            // (e.getDeltaY() * (Math.abs(Math.max(minZ, maxZ)*0.005) + 1)))
        });

        return camera;
    }

    private double x = 0;
    private double y = 0;

    public void setupRotation(WindowController controller) {

        var pane = controller.getFigurePane();

        Property<Transform> figureTransformProperty = new SimpleObjectProperty<>(new Rotate());
        figureTransformProperty.addListener((v, o, n) -> this.figure.getTransforms().setAll(n));

        pane.setOnMousePressed(e -> {
            x = e.getSceneX();
            y = e.getSceneY();
        });

        pane.setOnMouseDragged(e -> {
            if (controller.getRibbonChecked().isSelected() ||
                    controller.getAtomsChecked().isSelected()  ||
                    controller.getBondsChecked().isSelected() ){

                pane.setCursor(Cursor.CLOSED_HAND);
                var delta = new Point2D((e.getSceneX() - x), (e.getSceneY() - y));
                var dragOrthogonalAxis = new Point3D(delta.getY(), -delta.getX(), 0);
                var rotate = new Rotate(0.5 * delta.magnitude(), dragOrthogonalAxis);

                rotate.setPivotX((maxX + minX) / 2);
                rotate.setPivotY((maxY + minY) / 2);
                rotate.setPivotZ((maxZ + minZ) / 2);

                figureTransformProperty.setValue(rotate.createConcatenation(figureTransformProperty.getValue()));
                x = e.getSceneX();
                y = e.getSceneY();
                e.consume();
            }
        });

        pane.setOnMouseReleased(e -> pane.setCursor(Cursor.DEFAULT));
    }
}
