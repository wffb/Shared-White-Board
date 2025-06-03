package model.shape;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.awt.*;
import java.io.Serializable;

@Data
public abstract class Shape implements Serializable {

    private final int x1, y1;
    private final int colorValue;

    public Shape(int x1, int y1, Color color) {
        this.x1 = x1;
        this.y1 = y1;
        this.colorValue = color.getRGB();
    }


    public abstract String getShapeType();

    public abstract void draw(Graphics2D g);

    public abstract JSONObject toJSONObject();

}
