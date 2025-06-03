package model.gui;

import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.EventListener;
import java.util.List;

import model.listener.LineMouseListener;
import  model.shape.Shape;

import javax.swing.*;

public class Board extends Canvas {

    private final int width;
    private final int height;
    public final java.util.List<Shape> shapeList;


    public Board(List<Shape> shapeList) {
        this.shapeList = shapeList;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(1050, 800));
        setMaximumSize(new Dimension(1050, 800));
        this.width = getPreferredSize().width;
        this.height = getPreferredSize().height;
    }

    public BufferedImage createCanvasImage() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        paint(g2d);
        g2d.dispose();
        return image;
    }

    public void drawImage(BufferedImage image) {
        Graphics2D g2d = (Graphics2D) getGraphics();
        g2d.drawImage(image, 0, 0, null);
    }

    //clean all existed listeners in the board
    public void clean(){
        for (MouseListener listener : getMouseListeners()) {
            removeMouseListener(listener);
        }
        for (MouseMotionListener listener : getMouseMotionListeners()) {
            removeMouseMotionListener(listener);
        }
    }


    @Override
    public void update(Graphics g) {
        Image offscreen = createImage(getWidth(), getHeight());
        Graphics2D g2d = (Graphics2D) offscreen.getGraphics();
        for (Shape s : shapeList) {
            s.draw(g2d);
        }
        g.drawImage(offscreen, 0, 0, null);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        update(g2d);
    }
}
