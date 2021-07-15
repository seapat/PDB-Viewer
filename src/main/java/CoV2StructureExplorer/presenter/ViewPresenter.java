package CoV2StructureExplorer.presenter;

import CoV2StructureExplorer.model.Atom;
import CoV2StructureExplorer.model.Residue;
import CoV2StructureExplorer.selection.SetSelectionModel;
import CoV2StructureExplorer.model.PDBFile;
import CoV2StructureExplorer.view.Balls;
import CoV2StructureExplorer.view.Mesh;
import CoV2StructureExplorer.view.Sticks;
import CoV2StructureExplorer.view.WindowController;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.SetChangeListener;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

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
    private final Camera camera;

    private final SetSelectionModel<Residue> selectedAtoms =  new SetSelectionModel<>();

    ViewPresenter(WindowController controller, PDBFile model){
        controller.getFigurePane().getChildren().clear();

        this.figure = setupView(controller, model);
        SubScene subScene = new SubScene(this.figure, 800, 800, true, SceneAntialiasing.DISABLED);
        subScene.widthProperty().bind(controller.getFigurePane().widthProperty());
        subScene.heightProperty().bind(controller.getFigurePane().heightProperty());

        setupRotation(controller);
        this.camera = setupCamera(controller);
        subScene.setCamera(this.camera);

        controller.getFigurePane().getChildren().add(subScene);
    }

    public static void changeLegend(WindowController controller, Map<Object, Color> colorMap) {
        // create legends for atoms and sec. structure
        var recSize = 20;
        var spacer = 5;

        controller.getLegendPane().setVisible(true);
        controller.getLegendPane().getChildren().clear();

        var legendItems = new LinkedHashMap<Rectangle, Text>();
        // TreeMap + LinkedHashMap allow deterministic ordering of keys
        new TreeMap<>(colorMap).forEach((key, value) -> legendItems.put(
                new Rectangle(recSize, recSize, value),
                new Text(key.toString().substring(0, 1).toUpperCase() + key.toString().substring(1).toLowerCase())
        ));

        AtomicReference<Integer> idx = new AtomicReference<>(0);
        legendItems.forEach((key, value) -> {
            key.setY(idx.get() * (recSize + spacer));
            key.setStroke(Color.BLACK);

            value.setStyle("-fx-inner-fill: white");
            value.setStroke(Color.BLACK);
            value.setX(recSize + spacer);
            value.setY(key.getY() + spacer * 3);
            value.setFont(Font.font("calibri light", FontWeight.EXTRA_LIGHT, FontPosture.REGULAR, 13));
            controller.getLegendPane().getChildren().add(key);
            controller.getLegendPane().getChildren().add(value);

            idx.getAndSet(idx.get() + 1);
        });
    }

    public static<T> void setupSelectionListener(Balls balls , SetSelectionModel<T> selectionModel , //SetSelectionModel<T> selectionModel
                                                 Function<T, Set<Map.Entry<Atom, Sphere>>> itemToDots, WindowController controller) {

        selectionModel.getSelectedItems().addListener((SetChangeListener<T>) c -> {
            if (controller.getFocusChoice().getValue().equals("All")) {
                if (c.wasRemoved()) {
                    Platform.runLater ( ( ) -> {
                        for ( var shape : itemToDots.apply (c.getElementRemoved())) {
//                        shape.getValue().setDrawMode(DrawMode.FILL);
                            shape.getValue().setMaterial( new PhongMaterial(balls.getColor(controller.getColorChoice().getValue(),
                                    shape.getKey(), null)));
                        }
                    } ) ;
                }
                if (c.wasAdded()) {
                    var items = itemToDots.apply(c.getElementAdded());
                    Platform.runLater ( ( ) -> {

                        for ( var shape : items) {
//                        balls.getModelToView().values().forEach();
//                        for (var chain : balls.getModelToView().entrySet()) {
//                            for (var residue : chain.getValue().entrySet()){
//                                for (var atom: residue.getValue().entrySet()){
//                                    if (!items.contains(atom)){
//                                        atom.getValue().setDrawMode(DrawMode.LINE);
//                                    }
//                                }
//                            }
//                        }//);
                            var material = new PhongMaterial();
                            material.setDiffuseMap(new Image("/pinkblack2.png", true));
                            shape.getValue().setMaterial(material);
                        }
                    });
                }
            }

        });
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

        var balls = new Balls(model.getProtein(),
                controller.getRadiusScale().valueProperty(),
                controller.getModelChoice().getValue(),
                selectedAtoms
        );
        balls.setVisible(false);
        balls.visibleProperty().bind(controller.getAtomsChecked().selectedProperty());

        controller.getFocusChoice().setValue("All");
        balls.changeColor(controller);

        // SELECTION

        setupSelectionListener(balls, selectedAtoms,
                residue -> balls.getModelToView()
                .get(residue.getChain())
                .get(residue).entrySet(),
                controller
        );

        this.maxX = sticks.getLayoutBounds().getMaxX();
        this.maxY = sticks.getLayoutBounds().getMaxY();
        this.maxZ = sticks.getLayoutBounds().getMaxZ();
        this.minX = sticks.getLayoutBounds().getMinX();
        this.minY = sticks.getLayoutBounds().getMinY();
        this.minZ = sticks.getLayoutBounds().getMinZ();
//        this.centerX = balls.getLayoutBounds().getMinZ();

        controller.getFocusChoice().valueProperty().addListener(e -> {
            if ((controller.getFocusChoice().getValue() != null)) {
                Group selectedChain = balls.highlightChain(controller);
//                selectedChain.setTranslateX(this.camera.getTranslateX());
//                selectedChain.setTranslateY(this.camera.getTranslateY());
//                selectedChain.setTranslateZ(this.camera.getTranslateZ());
            }

        });

        controller.getColorChoice().valueProperty().addListener(e -> {
            if ((controller.getColorChoice().getValue() != null))
                controller.getFocusChoice().setValue("All");
                balls.changeColor(controller);
        });

        controller.getInfoLabel().textProperty().bind(balls.getSelectedResiduesProp());


        figure.getChildren().addAll(mesh, sticks, balls);
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
            if (!e.isShiftDown()) {
                var curr = camera.getTranslateZ();
                camera.setTranslateZ(curr + (e.getDeltaY() / Math.abs(e.getDeltaY())) * (Math.abs(maxFromCenter * 0.1) + 1));
            }
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
                    controller.getBondsChecked().isSelected() ||
                    controller.getRibbonChecked().isSelected()){

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
