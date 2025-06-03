package common;

import com.alibaba.fastjson2.JSONObject;
import model.shape.*;
import model.shape.Rectangle;
import model.shape.Shape;

import java.awt.*;

public class ParseHelper {

    public static Shape parseShape(JSONObject shapeObject) {
        String shapeType = (String) shapeObject.get("type");

        return switch (shapeType) {

            case "Line" ->
                    new Line((Integer) shapeObject.get("x1") , ((Integer) shapeObject.get("y1")),
                            ((Integer) shapeObject.get("x2")), ((Integer) shapeObject.get("y2")),
                            new Color(((Integer) shapeObject.get("colorValue"))), ((Integer) shapeObject.get("strokeValue")));

            case "Text" ->
                    new Text(((Integer) shapeObject.get("x1")), ((Integer) shapeObject.get("y1")),
                            (String) shapeObject.get("text"), new Color(((Integer) shapeObject.get("colorValue"))),
                            (String) shapeObject.get("fontName"), ((Integer) shapeObject.get("fontStyle")), ((Integer) shapeObject.get("fontSize")));

            case "Triangle" ->
                    new  Triangle(((Integer) shapeObject.get("x1")), ((Integer) shapeObject.get("y1")),
                            ((Integer) shapeObject.get("x2")), ((Integer) shapeObject.get("y2")),
                            ((Integer) shapeObject.get("x3")), ((Integer) shapeObject.get("y3")),
                            new Color(((Integer) shapeObject.get("colorValue"))), ((Integer) shapeObject.get("strokeValue")));

            case "Circle" ->
                    new Circle(((Integer) shapeObject.get("x1")), ((Integer) shapeObject.get("y1")), ((Integer) shapeObject.get("diameter")),
                            new Color(((Integer) shapeObject.get("colorValue"))), ((Integer) shapeObject.get("strokeValue")));

            case "Oval" ->
                    new Oval(((Integer) shapeObject.get("x1")), ((Integer) shapeObject.get("y1")),
                            ((Integer) shapeObject.get("width")), ((Integer) shapeObject.get("height")),
                            new Color(((Integer) shapeObject.get("colorValue"))), ((Integer) shapeObject.get("strokeValue")));

            case "Rectangle" ->
                    new Rectangle(((Integer) shapeObject.get("x1")), ((Integer) shapeObject.get("y1")),
                            ((Integer) shapeObject.get("width")), ((Integer) shapeObject.get("height")),
                            new Color(((Integer) shapeObject.get("colorValue"))), ((Integer) shapeObject.get("strokeValue")));


            default -> null;
        };
    }
}
