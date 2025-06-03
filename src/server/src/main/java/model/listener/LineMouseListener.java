package model.listener;

import lombok.extern.slf4j.Slf4j;
import model.gui.Board;
import model.gui.Gui;
import model.shape.Line;
import server.RmiProcessor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.rmi.RemoteException;

@Slf4j
public class LineMouseListener implements MouseListener, MouseMotionListener {

    private final RmiProcessor processor;

    private static int x1, y1, x2, y2;
    private final Graphics2D graphics;
    private final Board board;
    private final JPanel colorNow;
    private final JSlider slider;
    private final JButton lineButton;


    //private final WhiteBoardServerService serverService;


//    public DrawLineMouseListener(Graphics2D graphics, Board board, JPanel currentColorPanel, JSlider strokeSlider, JButton lineButton) {
//        this.graphics = graphics;
//        this.board = board;
//        this.currentColorPanel = currentColorPanel;
//        this.strokeSlider = strokeSlider;
//        this.lineButton = lineButton;
//        //this.serverService = serverService;
//    }

    public LineMouseListener(Graphics2D graphics, Board board, JSlider strokeSlider, JPanel currentColorPanel, JButton lineButton, RmiProcessor processor) {
        this.graphics = graphics;
        this.board = board;
        this.lineButton = lineButton;
        this.slider = strokeSlider;
        this.colorNow = currentColorPanel;
        this.processor = processor;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && lineButton.getModel().isPressed()) {
            x1 = e.getX();
            y1 = e.getY();
            x2 = x1;
            y2 = y1;
        } else {
            board.removeMouseListener(this);
            board.removeMouseMotionListener(this);
            board.setCursor(Cursor.getDefaultCursor());
            lineButton.getModel().setPressed(false);
            lineButton.getModel().setArmed(false);
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


        graphics.drawLine(x1, y1, x2, y2);
        x2 = e.getX();
        y2 = e.getY();
        graphics.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        x2 = e.getX();
        y2 = e.getY();
        //graphics.setXORMode(Color.WHITE);

        // set color
        Color c;
        if(!Gui.isErased)
            c = colorNow.getBackground();
        else
            c = Color.WHITE;

        try {
            //new shape
            Line line = new Line(x1, y1, x2, y2, c, slider.getValue());
            line.draw(graphics);
            processor.addShape(line);

        }catch (RemoteException re) {
            log.error("Shape Line add failed: "+re.getMessage());
        }

        board.removeMouseListener(this);
        board.removeMouseMotionListener(this);

        board.setCursor(Cursor.getDefaultCursor());
        lineButton.getModel().setPressed(false);
        lineButton.getModel().setArmed(false);
        Gui.setSelectedShapeButton(null);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        ListenerTool.detectAndRemove(lineButton,board,this);
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
