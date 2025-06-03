package model.listener;

import model.gui.Board;
import model.gui.Gui;
import model.shape.Circle;
import server.RmiProcessor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.rmi.RemoteException;

public class CircleMouseListener implements MouseListener, MouseMotionListener {

    private final RmiProcessor processor;

    private static int x1, y1, x2, y2, tx, ty;
    private int diameter;
    private final Graphics2D graphics;
    private final Board board;
    private final JPanel colorNow;
    private final JSlider slider;
    private final JButton circleButton;

    //private final WhiteBoardServerService serverService;

    public CircleMouseListener(Graphics2D graphics, Board board, JSlider strokeSlider, JPanel currentColorPanel, JButton circleButton, RmiProcessor processor) {
        this.graphics = graphics;
        this.board = board;
        this.colorNow = currentColorPanel;
        this.slider = strokeSlider;
        this.circleButton = circleButton;
        this.processor = processor;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && circleButton.getModel().isPressed()) {
            x1 = e.getX();
            y1 = e.getY();
            x2 = x1;
            y2 = y1;
            tx = x1;
            ty = y1;
            diameter = 0;
        }
        else {
            board.removeMouseListener(this);
            board.removeMouseMotionListener(this);
            board.setCursor(Cursor.getDefaultCursor());
            circleButton.getModel().setPressed(false);
            circleButton.getModel().setArmed(false);
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
        graphics.drawOval(tx, ty, diameter, diameter);
        x2 = e.getX();
        y2 = e.getY();
        diameter = Math.min(Math.abs(x2 - x1), Math.abs(y2 - y1));
        int[] xy = computeX1Y1(x1, y1, x2, y2, diameter);
        tx = xy[0];
        ty = xy[1];
        graphics.drawOval(tx, ty, diameter, diameter);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        x2 = e.getX();
        y2 = e.getY();
        diameter = Math.min(Math.abs(x2 - x1), Math.abs(y2 - y1));
        int[] xy = computeX1Y1(x1, y1, x2, y2, diameter);
        tx = xy[0];
        ty = xy[1];

        Color c;

        if(!Gui.isErased)
            c = colorNow.getBackground();
        else
            c = Color.WHITE;

        try {

            Circle circle = new Circle(tx, ty, diameter, c, slider.getValue());
            circle.draw(graphics);

            //new shape
            processor.addShape(circle);


        }catch (RemoteException re) {
            throw new RuntimeException(re);
        }

        board.removeMouseListener(this);
        board.removeMouseMotionListener(this);
        board.setCursor(Cursor.getDefaultCursor());
        circleButton.getModel().setPressed(false);
        circleButton.getModel().setArmed(false);
        Gui.setSelectedShapeButton(null);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        ListenerTool.detectAndRemove(circleButton,board,this);
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

    private int[] computeX1Y1(int x1, int y1, int x2, int y2, int diameter) {
        int[] xy = new int[2];
        if (x2 < x1) {
            if (y2 < y1) {
                xy[0] = x1 - diameter;
                xy[1] = y1 - diameter;
            }
            else {
                xy[0] = x1 - diameter;
                xy[1] = y1;
            }
        }
        else {
            if (y2 < y1) {
                xy[0] = x1;
                xy[1] = y1 - diameter;
            }
            else {
                xy[0] = x1;
                xy[1] = y1;
            }
        }
        return xy;
    }
}
