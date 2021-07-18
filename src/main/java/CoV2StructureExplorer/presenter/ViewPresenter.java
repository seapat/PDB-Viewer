package CoV2StructureExplorer.presenter;

import CoV2StructureExplorer.model.Chain;
import CoV2StructureExplorer.model.PDBFile;
import CoV2StructureExplorer.model.Residue;
import CoV2StructureExplorer.selection.SetSelectionModel;
import CoV2StructureExplorer.view.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ViewPresenter {

    private final Group figure;
    private final Camera camera;
    private final SimpleIntegerProperty sizeChainChoiceSize;
    private final HashMap<Chain, Group> chainGroups = new HashMap<>();
    private final StringProperty selectedResiduesProp;
    private final Balls balls;
    private final Sticks sticks;
    private final Meshes meshes;
    private Double maxX;
    private Double maxY;
    private Double maxZ;
    private Double minX;
    private Double minY;
    private Double minZ;
    private Double centerX;
    private Double centerY;
    private Double centerZ;

    ViewPresenter(WindowController controller, PDBFile model) {

        SetSelectionModel<Residue> selectedResidues = new SetSelectionModel<>();
        this.selectedResiduesProp = new SimpleStringProperty(selectedResidues, "StringBuilder");
        sizeChainChoiceSize = new SimpleIntegerProperty(controller.getFocusChoice().getItems().size(), "sizeChainChoiceSize");

        this.meshes = new Meshes(model.getProtein(), chainGroups, controller,
                selectedResiduesProp, selectedResidues
        );
        this.sticks = new Sticks(model.getProtein(), chainGroups, controller,
                selectedResiduesProp, selectedResidues
        );
        this.balls = new Balls(model.getProtein(), selectedResidues, chainGroups,
                controller, selectedResiduesProp
        );
        balls.changeColor(controller);

        calcLayoutBonds(controller, model);

        setupSelectionListener(selectedResidues, controller, model);

        setupFocusChoice(controller);

        controller.getColorChoice().valueProperty().addListener((v , o , n) -> {
            if (!n.equals(o)) {
                controller.getFocusChoice().setValue("All");
                balls.changeColor(controller);
            }
        });

        controller.getInfoLabel().textProperty().bind(this.getSelectedResiduesProp());

        this.figure = new Group();
        figure.getChildren().addAll(chainGroups.values());
        var subScene = new SubScene(this.figure, 800, 800, true, SceneAntialiasing.DISABLED);
        subScene.widthProperty().bind(controller.getFigurePane().widthProperty());
        subScene.heightProperty().bind(controller.getFigurePane().heightProperty());

        controller.getFigurePane().getChildren().add(subScene);

        camera = setupCamera(controller);
        subScene.setCamera(camera);

        this.setupMouseRotation(controller);
        this.setupExplosionAnimation(controller);
        this.setupScaleAnimation(controller);
        this.setupRotateAnimation(controller);
        sizeChainChoiceSize.setValue(controller.getFocusChoice().getItems().size());
    }

    public static void changeLegend(WindowController controller, Map<Object, Color> colorMap) {
        // create legends for atoms and sec. structure
        var recSize = 20;
        var spacer = 5;

        controller.getLegendPane().setVisible(true);
        controller.getLegendPane().getChildren().clear();

        var legendItems = new LinkedHashMap<Rectangle, Text>();
        colorMap.forEach((key, value) -> legendItems.put(
                new Rectangle(recSize, recSize, value),
                new Text(key.toString().substring(0, 1).toUpperCase() + key.toString().substring(1).toLowerCase())
        ));

        AtomicReference<Integer> idx = new AtomicReference<>(0);
        legendItems.forEach((key, value) -> {
            key.setY(idx.get() * (recSize + spacer));
            key.setStroke(Color.BLACK);

            value.setFill(Color.BLACK);
            value.setX(recSize + spacer);
            value.setY(key.getY() + spacer * 3);
            value.setFont(Font.font("calibri light", FontWeight.EXTRA_BOLD, FontPosture.REGULAR, 15));

            controller.getLegendPane().getChildren().add(key);
            controller.getLegendPane().getChildren().add(value);

            idx.getAndSet(idx.get() + 1);
        });
    }

    private void calcLayoutBonds(WindowController controller, PDBFile model) {
        this.minX = model.getProtein().get(controller.getModelChoice().getValue() - 1).getCoordExtrema().minX();
        this.minY = model.getProtein().get(controller.getModelChoice().getValue() - 1).getCoordExtrema().minY();
        this.minZ = model.getProtein().get(controller.getModelChoice().getValue() - 1).getCoordExtrema().minZ();
        this.maxX = model.getProtein().get(controller.getModelChoice().getValue() - 1).getCoordExtrema().maxX();
        this.maxY = model.getProtein().get(controller.getModelChoice().getValue() - 1).getCoordExtrema().maxY();
        this.maxZ = model.getProtein().get(controller.getModelChoice().getValue() - 1).getCoordExtrema().maxZ();
        this.centerX = (this.maxX + this.minX) / 2;
        this.centerY = (this.maxY + this.minY) / 2;
        this.centerZ = (this.maxZ + this.minZ) / 2;
    }

    private void setupFocusChoice(WindowController controller) {
        controller.getFocusChoice().valueProperty().addListener(e -> {
            if ((controller.getFocusChoice().getValue() != null))
                balls.highlightChain(controller);
        });
        controller.getFocusChoice().disableProperty().bind(sizeChainChoiceSize.greaterThan(2).not());
        controller.getFocusLabel().disableProperty().bind(sizeChainChoiceSize.greaterThan(2).not());
    }

    public ObservableValue<? extends String> getSelectedResiduesProp() {
        return selectedResiduesProp;
    }

    public void setupSelectionListener(SetSelectionModel<Residue> selectionModel, WindowController controller, PDBFile model) {

        controller.getAllSelectionMenu().setOnAction(e -> selectionModel.selectAll(
                model.getProtein().get(controller.getModelChoice().getValue() -1).stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList())
        ));
        controller.getFocusChoice().valueProperty().addListener(e -> {
            if (null != controller.getFocusChoice().valueProperty().getValue() && !controller.getFocusChoice().valueProperty().getValue().equals("All")) {
                selectionModel.clearSelection();
            }
        });

        var colorChoiceChangedProperty = new SimpleBooleanProperty(controller.getColorChoice(), "color changed", true);
        controller.getColorChoice().valueProperty().addListener(e -> {
            colorChoiceChangedProperty.setValue(true);
            selectionModel.clearSelection();
        });

        controller.getClearSelectionMenu().setOnAction(e -> {
            colorChoiceChangedProperty.setValue(true);
            selectedResiduesProp.setValue("");
            balls.changeColor(controller);
            meshes.forEach(mesh -> mesh.setMaterial(new PhongMaterial(Color.PURPLE)));
            sticks.forEach(stick -> stick.setMaterial(new PhongMaterial(Color.GREY)));
            controller.getFocusChoice().setValue("All");
        });

        // concat all nodes to reduce loop nesting
        var allShapes = new ArrayList<Shape3D>();
        allShapes.addAll(balls);
        allShapes.addAll(sticks);
        allShapes.addAll(meshes);


        selectionModel.getSelectedItems().addListener((SetChangeListener<Residue>) c -> {

            if (controller.getFocusChoice().getValue().equals("All")) {
                if (c.wasRemoved() && !colorChoiceChangedProperty.getValue()) {
                    Platform.runLater(() -> {
                        var removedResidue = (Residue) c.getElementRemoved();
                        for (Shape3D shape : allShapes) {
                            var shapeIds = shape.getId().split(":");
                            var chainID = shapeIds[0];
                            var resID = shapeIds[2];

                            var oldCol = ((PhongMaterial) shape.getMaterial()).getDiffuseColor();

                            if (!resID.equals(String.valueOf(removedResidue.getId()))
                                    && chainID.equals(removedResidue.getChain().getChainID().toString())) {
                                shape.setMaterial(new PhongMaterial(
                                        new Color(oldCol.getRed(), oldCol.getGreen(), oldCol.getBlue(),
                                                0.3)
                                ));
                            }
                        }
                    });
                } else {
                    colorChoiceChangedProperty.setValue(false);
                }
                if (c.wasAdded()) {

                    Platform.runLater(() -> {

//                         if nothing was selected, turn all dark
                        if (selectionModel.getSelectedItems().size() == 1) {
                            allShapes.forEach(shape -> {
                                var oldCol = ((PhongMaterial) shape.getMaterial()).getDiffuseColor();
                                shape.setMaterial(new PhongMaterial(
                                        new Color(oldCol.getRed(), oldCol.getGreen(), oldCol.getBlue(),
                                                0.3)));
                            });
                        }

                        // colorize selected
                        var selectedResidue = (Residue) c.getElementAdded();
                        for (Shape3D shape : allShapes) {
                            var shapeIds = shape.getId().split(":");

                            var chainID = shapeIds[0];
                            var resID = shapeIds[1];
                            // this is the second member of the bond for sticks, "dummy" for meshes
                            var atomID = shapeIds[2];

                            if (resID.equals(String.valueOf(selectedResidue.getId()))
                                    && (chainID.equals(selectedResidue.getChain().getChainID().toString()))) {

                                if (shape instanceof Sphere) {

                                    for (var atom : selectedResidue.values()) {

                                        if (atom.getId() == Integer.parseInt(atomID)) {
                                            shape.setMaterial(
                                                    new PhongMaterial(balls.getColor(
                                                            controller.getColorChoice().getValue(),
                                                            atom))
                                            );
                                        }

                                    }
                                } else if (shape instanceof MeshView) {
                                    shape.setMaterial(new PhongMaterial(Color.PURPLE));
                                } else if (shape instanceof Cylinder && resID.equals(shapeIds[2])) {
                                    shape.setMaterial(new PhongMaterial(Color.GREY));
                                }
                            }
                        }
                    });

                }
            }
        });
    }

    public Camera setupCamera(WindowController controller) {
        var camera = new PerspectiveCamera(true);

        var maxFromCenter = Math.abs(Collections.max(
                Arrays.asList(this.maxX, this.maxY, this.maxZ, this.minX, this.minY, this.minZ),
                Comparator.comparingDouble(Math::abs))
        );
        camera.setFarClip(100 * maxFromCenter);
        camera.setNearClip(0.1);

        camera.setTranslateX(-centerX);
        camera.setTranslateY(-centerY);
        camera.setTranslateZ(-maxFromCenter * 4);

        figure.setTranslateX(-centerX);
        figure.setTranslateY(-centerY);
        figure.setTranslateZ(-centerZ);

        controller.getFigurePane().setOnScroll((ScrollEvent e) -> {
            if (!e.isShiftDown()) {
                var curr = camera.getTranslateZ();
                camera.setTranslateZ(curr + (e.getDeltaY() / Math.abs(e.getDeltaY())) * (Math.abs(maxFromCenter * 0.1) + 1));
            }
        });

        for (var item : chainGroups.values()) {
            item.getTransforms().add(new Translate(-centerX, -centerY, -centerZ));
        }
        return camera;
    }

    public void setupMouseRotation(WindowController controller) {

        MouseInteraction.installMouseScrollZoom(camera, controller.getFigurePane());
        Property<Transform> figureTransformProperty = new SimpleObjectProperty<>(new Rotate());
        figureTransformProperty.addListener((v, o, n) -> this.figure.getTransforms().setAll(n));
        MouseInteraction.installMouseDragRotate(controller.getFigurePane(), figureTransformProperty);
    }

    private void setupExplosionAnimation(WindowController controller) {

        controller.getExplodeButton().setSelected(false);
        controller.getExplodeButton().setText("Explode");

        var zero = new Point3D(centerX, centerY, centerZ);

        var explodingCollapsingProperty = new SimpleBooleanProperty(controller.getExplodeButton(), "explodingCollapsing");
        controller.getExplodeButton().disableProperty().bind(sizeChainChoiceSize.greaterThan(2).not()
                .or(explodingCollapsingProperty)
        );

        controller.getExplodeButton().setOnAction(e -> {

            controller.getExplodeButton().setSelected(false);
            var explodeString = "Explode";

            for (var chain : chainGroups.entrySet()) {

                var chainMid = new Point3D(
                        (chain.getKey().getCoordExtrema().maxX() + chain.getKey().getCoordExtrema().minX()) / 2,
                        (chain.getKey().getCoordExtrema().maxY() + chain.getKey().getCoordExtrema().minY()) / 2,
                        (chain.getKey().getCoordExtrema().maxZ() + chain.getKey().getCoordExtrema().minZ()) / 2
                );

                var endPoint = Shape3DUtils.computeOpposite(chainMid, zero);

                Duration startTime;
                Duration endTime;
                if (explodeString.equals(controller.getExplodeButton().getText())) {
                    startTime = Duration.millis(0);
                    endTime = Duration.millis(2000);
                } else {
                    startTime = Duration.millis(2000);
                    endTime = Duration.millis(0);
                }

                var keyFrame1 = new KeyFrame(startTime,
                        new KeyValue(chain.getValue().translateXProperty(), 0),
                        new KeyValue(chain.getValue().translateYProperty(), 0),
                        new KeyValue(chain.getValue().translateZProperty(), 0)
                );
                var keyFrame2 = new KeyFrame(endTime,
                        new KeyValue(chain.getValue().translateXProperty(), endPoint.getX() - zero.getX()),
                        new KeyValue(chain.getValue().translateYProperty(), endPoint.getY() - zero.getY()),
                        new KeyValue(chain.getValue().translateZProperty(), endPoint.getZ() - zero.getY())
                );

                var timeline = new Timeline(keyFrame1, keyFrame2);

                explodingCollapsingProperty.set(true);
                timeline.play();
                timeline.setOnFinished(actionEvent -> explodingCollapsingProperty.set(false));
            }
            if (explodeString.equals(controller.getExplodeButton().getText())) {
                controller.getExplodeButton().setText("Collapse");
            } else {
                controller.getExplodeButton().setText(explodeString);
            }
        });
    }

    private void setupScaleAnimation(WindowController controller) {

        var cyclingProperty = new SimpleBooleanProperty(controller.getScaleChainsButton(), "Cycling");
        controller.getScaleChainsButton().disableProperty()
                .bind(controller.getAtomsChecked().selectedProperty().not()
                        .or(cyclingProperty)
                        .or(sizeChainChoiceSize.greaterThan(2).not()));

        var sequential = new SequentialTransition();
        controller.getScaleChainsButton().setOnAction(e -> {

            cyclingProperty.setValue(true);

            for (var chain : chainGroups.values()) {

                var transition = new ScaleTransition(Duration.millis(2000));
                chain.getChildren().forEach(transition::setNode);
                transition.setByX(1.f);
                transition.setByY(1.f);
                transition.setByZ(1.f);
                transition.setCycleCount(2);
                transition.setAutoReverse(true);
                sequential.getChildren().add(transition);
            }
            cyclingProperty.setValue(true);
            sequential.play();
            sequential.setOnFinished(actionEvent -> cyclingProperty.setValue(false));
        });
    }

    private void setupRotateAnimation(WindowController controller) {

        controller.getRotateProteinButton().setSelected(false);
        controller.getRotateProteinButton().setText("Rotate");

        var rotate = new RotateTransition();

        controller.getRotateProteinButton().setOnAction(e -> {
            rotate.setAxis(new Point3D(0, 1, 0));
            rotate.setNode(figure);
            rotate.setByAngle(360);
            rotate.setDuration(Duration.seconds(5));
            rotate.setCycleCount(1);
            rotate.setInterpolator(Interpolator.LINEAR);
            rotate.play();

            rotate.setOnFinished(actionEvent -> {
                if (!controller.getRotateProteinButton().isSelected()) {
                    rotate.stop();
                } else {
                    rotate.play();
                }
            });
        });

    }

    private static class MouseInteraction {
        private static double x;
        private static double y;

        public static void installMouseScrollZoom(Camera camera, Pane pane) {
            pane.setOnScroll(scrollEvent ->
                    camera.setTranslateZ(camera.getTranslateZ() + scrollEvent.getDeltaY() * 1));
        }

        public static void installMouseDragRotate(Pane pane, Property<Transform> figureTransformProperty) {
            pane.setOnMousePressed(mouseEvent -> {
                x = mouseEvent.getSceneX();
                y = mouseEvent.getSceneY();
            });

            pane.setOnMouseDragged(mouseEvent -> {
                pane.setCursor(Cursor.CLOSED_HAND);
                var delta = new Point2D(mouseEvent.getSceneX() - x, mouseEvent.getSceneY() - y);
                var dragOrthogonalAxis = new Point3D(delta.getY(), -delta.getX(), 0);
                var rotate = new Rotate(0.5 * delta.magnitude(), dragOrthogonalAxis);
                figureTransformProperty.setValue(rotate.createConcatenation(figureTransformProperty.getValue()));

                x = mouseEvent.getSceneX();
                y = mouseEvent.getSceneY();
            });
            pane.setOnMouseReleased(mouseEvent -> pane.setCursor(Cursor.DEFAULT));
        }
    }

}
