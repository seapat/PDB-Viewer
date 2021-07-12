package CoV2StructureExplorer.view;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 * utilities for 3D shapes
 * Daniel Huson, 6.2021
 */
public class Shape3DUtils {
    private static final Point3D YAXIS = new Point3D(0, 100, 0);

    /**
     * creates a 3D-line as a cylinder with given start and end properties
     *
     * @param startXProperty start x
     * @param startYProperty start y
     * @param startZProperty start z
     * @param endXProperty   end x
     * @param endYProperty   end y
     * @param endZProperty   end z
     * @param radius         cylinder radius
     * @param color          color
     * @return cylinder
     */
    public static Cylinder createLine3D(DoubleProperty startXProperty, DoubleProperty startYProperty, DoubleProperty startZProperty,
                                        DoubleProperty endXProperty, DoubleProperty endYProperty, DoubleProperty endZProperty, double radius, Color color) {
        var cylinder = new Cylinder(radius, 100);
        cylinder.setMaterial(new PhongMaterial(color));

        var listener = createInvalidationListener(cylinder, startXProperty, startYProperty, startZProperty, endXProperty, endYProperty, endZProperty);
        listener.invalidated(null); // fire now
        cylinder.setUserData(listener); // keep reference

        startXProperty.addListener(new WeakInvalidationListener(listener));
        startYProperty.addListener(new WeakInvalidationListener(listener));
        startZProperty.addListener(new WeakInvalidationListener(listener));
        endXProperty.addListener(new WeakInvalidationListener(listener));
        endYProperty.addListener(new WeakInvalidationListener(listener));
        endZProperty.addListener(new WeakInvalidationListener(listener));

        return cylinder;
    }

    private static InvalidationListener createInvalidationListener(Cylinder cylinder, DoubleProperty startX, DoubleProperty startY, DoubleProperty startZ, DoubleProperty endX, DoubleProperty endY, DoubleProperty endZ) {
        return v -> {
            var start = new Point3D(startX.get(), startY.get(), startZ.get());
            var end = new Point3D(endX.get(), endY.get(), endZ.get());
            var midpoint = start.midpoint(end);
            var direction = end.subtract(start);

            var perpendicularAxis = YAXIS.crossProduct(direction);
            var angle = YAXIS.angle(direction);
            cylinder.setRotationAxis(perpendicularAxis);
            cylinder.setRotate(angle);

            cylinder.setTranslateX(midpoint.getX());
            cylinder.setTranslateY(midpoint.getY());
            cylinder.setTranslateZ(midpoint.getZ());

            cylinder.setScaleY(start.distance(end) / cylinder.getHeight());
        };
    }

    /**
     * creates part of a ribbon
     * @param ca1 C-alpha of amino acid i
     * @param cb1 C-beta of amino acid i
     * @param op1 opposite
     * @param ca2 C-alpha  of amino acid i+1
     * @param cb2 C-beta of amino acid i+1
     * @param op2 opposite
     * @return mesh view
     */
    public static MeshView createRibbonPart(Point3D ca1, Point3D cb1, Point3D op1, Point3D ca2, Point3D cb2, Point3D op2) {

        var points = new float[]{
                (float) op1.getX(), (float) op1.getY(), (float) op1.getZ(),
                (float) ca1.getX(), (float) ca1.getY(), (float) ca1.getZ(),
                (float) cb1.getX(), (float) cb1.getY(), (float) cb1.getZ(),
                (float) op2.getX(), (float) op2.getY(), (float) op2.getZ(),
                (float) ca2.getX(), (float) ca2.getY(), (float) ca2.getZ(),
                (float) cb2.getX(), (float) cb2.getY(), (float) cb2.getZ(),
        };

        var faces = new int[]{
                0, 0, 1, 1, 4, 4,
                0, 0, 4, 4, 5, 5,
                1, 1, 2, 2, 3, 3,
                1, 1, 3, 3, 4, 4,

                0, 0, 4, 4, 1, 1,
                0, 0, 5, 5, 4, 4,
                1, 1, 3, 3, 2, 2,
                1, 1, 4, 4, 3, 3,

        };
        var texCoords = new float[]{
                0, 0, // t0
                0, 0.5f, // t1
                0, 1, // t2
                1, 1, // t3
                1, 0.5f, // t4
                1, 0, // t5
        };

        var smoothing = new int[]{1, 1, 1, 1, 2, 2, 2, 2};

        var mesh = new TriangleMesh();
        mesh.getPoints().addAll(points);
        mesh.getTexCoords().addAll(texCoords);
        mesh.getFaces().addAll(faces);
        mesh.getFaceSmoothingGroups().addAll(smoothing);

        var meshView = new MeshView(mesh);
        meshView.setMaterial(new PhongMaterial(Color.PLUM));

        return meshView;
    }

    /**
     * compute the other point (mirror image of b over a)
     *
     * @param a
     * @param b
     * @return other
     */
    public static Point3D computeOpposite(Point3D a, Point3D b) {
        return a.subtract(b.subtract(a));
    }
}

