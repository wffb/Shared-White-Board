package model.shape;

import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;

import java.awt.*;

@Getter
public class Triangle extends Shape{

    private final int x2, y2, x3, y3;
    private final int strokeValue;

    public Triangle(int x1, int y1, int x2, int y2,int x3,int y3, Color color, int strokeValue) {
        super(x1, y1, color);
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
        this.strokeValue = strokeValue;
    }

    @Override
    public String getShapeType() {
        return "Triangle";
    }

    @Override
    public void draw(Graphics2D g) {

        g.setColor(new Color(getColorValue()));
        g.setStroke(new BasicStroke(getStrokeValue()));
        g.drawPolygon(new int[]{getX1(),x2,x3},new int[]{getY1(),y2,y3},3);
    }

    @Override
    public JSONObject toJSONObject() {

        JSONObject obj = new JSONObject();
        obj.put("type", getShapeType());
        obj.put("x1", getX1());
        obj.put("y1", getY1());
        obj.put("x2", getX2());
        obj.put("y2", getY2());
        obj.put("x3", getX3());
        obj.put("y3", getY3());
        obj.put("colorValue", getColorValue());
        obj.put("strokeValue", getStrokeValue());
        return obj;

    }
}
