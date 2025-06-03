package model.listener;

import lombok.extern.slf4j.Slf4j;
import model.gui.Board;
import model.gui.Gui;
import model.shape.Triangle;
import server.RmiProcessor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.rmi.RemoteException;

@Slf4j
public class TriangleMouseListener implements MouseListener, MouseMotionListener {

    private final RmiProcessor processor;

    private static int x1, y1, x2, y2, tx, ty;
    private int width, height;
    private final Graphics2D graphics;
    private final Board board;

    private final JPanel colorNow;
    private final JSlider slider;
    private final JButton rectangleButton;

    //private final WhiteBoardServerService serverService;

    public TriangleMouseListener(Graphics2D graphics, Board board, JSlider strokeSlider, JPanel currentColorPanel, JButton rectangleButton, RmiProcessor processor) {
        this.graphics = graphics;
        this.board = board;
        this.colorNow = currentColorPanel;
        this.slider = strokeSlider;
        this.rectangleButton = rectangleButton;
        this.processor = processor;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && rectangleButton.getModel().isPressed()) {
            x1 = e.getX();
            y1 = e.getY();
            x2 = x1;
            y2 = y1;
            tx = x1;
            ty = y1;
            width = 0;
            height = 0;
        }
        else {
            board.removeMouseListener(this);
            board.removeMouseMotionListener(this);
            board.setCursor(Cursor.getDefaultCursor());
            rectangleButton.getModel().setPressed(false);
            rectangleButton.getModel().setArmed(false);
            board.repaint();
            Gui.setSelectedShapeButton(null);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        graphics.setXORMode(Color.WHITE);

        if(!Gui.isErased)
            graphics.setColor(colorNow.getBackground());
        else
            graphics.setColor(Color.BLACK);

        graphics.setStroke(new BasicStroke(slider.getValue()));
        graphics.drawPolygon(new int[]{x1,x2,tx},new int[]{y1,y2,ty},3);

        x2 = e.getX();
        y2 = e.getY();
        width = Math.abs(x2 - x1);
        height = Math.abs(y2 - y1);
        int[] xy= computeX1Y1(x1, y1, x2, y2, width, height);
        tx = xy[0];
        ty = xy[1];
        graphics.drawPolygon(new int[]{x1,x2,tx},new int[]{y1,y2,ty},3);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        x2 = e.getX();
        y2 = e.getY();
        width = Math.abs(x2 - x1);
        height = Math.abs(y2 - y1);
        int[] xy3 = computeX1Y1(x1, y1, x2, y2, width, height);

        Color c;
        if(!Gui.isErased)
            c = colorNow.getBackground();
        else
            c = Color.WHITE;

        try {
            //new shape
            Triangle triangle = new Triangle(x1, y1, x2, y2,xy3[0],xy3[1], c, slider.getValue());
            triangle.draw(graphics);
            processor.addShape(triangle);

        }catch (RemoteException re) {
            log.error("Shape Triangle add failed: "+re.getMessage());
        }

        board.removeMouseListener(this);
        board.removeMouseMotionListener(this);
        board.setCursor(Cursor.getDefaultCursor());
        rectangleButton.getModel().setPressed(false);
        rectangleButton.getModel().setArmed(false);
        Gui.setSelectedShapeButton(null);
    }

    private int[] computeX1Y1(int x1, int y1, int x2, int y2, int width, int height) {
        int[] xy = new int[2];
        //x
        if(x1>x2)
            xy[0] = x1+width;
        else
            xy[0] = x1-width;
        //y
        xy[1] = y2;

        return xy;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        ListenerTool.detectAndRemove(rectangleButton,board,this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
