/**
 * @Name: Jiaming XIAN
 * @Student_ID: 1336110
 */

package model.shape;



import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import model.shape.Shape;

import java.awt.*;
import java.io.Serializable;

@Getter
public class Circle extends Shape implements Serializable {

	private final int diameter;
    private final int strokeValue;

    public Circle(int x1, int y1, int diameter, Color color, int strokeValue) {
        super(x1, y1, color);
        this.diameter = diameter;
        this.strokeValue = strokeValue;
    }


    @Override
    public String getShapeType() {
        return "Circle";
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(getColorValue()));
        g.setStroke(new BasicStroke(getStrokeValue()));
        g.drawOval(getX1(), getY1(), getDiameter(), getDiameter());
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("type", getShapeType());
        obj.put("x1", getX1());
        obj.put("y1", getY1());
        obj.put("diameter", getDiameter());
        obj.put("colorValue", getColorValue());
        obj.put("strokeValue", getStrokeValue());
        return obj;
    }
}
