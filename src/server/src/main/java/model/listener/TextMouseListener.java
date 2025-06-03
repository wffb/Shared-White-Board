package model.listener;

import lombok.extern.slf4j.Slf4j;
import model.gui.Board;
import model.gui.Gui;
import model.shape.Text;
import server.RmiProcessor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.rmi.RemoteException;

@Slf4j
public class TextMouseListener implements MouseListener, MouseMotionListener {

    private final RmiProcessor proccessor;

    private static int x1, y1;
    private final String text;
    private final Graphics2D graphics;
    private final Board board;
    private final JPanel colorNow;

    private final JButton textButton;



    public TextMouseListener(Graphics2D graphics, Board board, JSlider strokeSlider, JPanel colorNow, JButton textButton, String text, RmiProcessor proccessor) {

        this.proccessor = proccessor;
        this.graphics = graphics;
        this.board = board;
        this.text = text;
        this.colorNow = colorNow;
        this.textButton = textButton;

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && textButton.getModel().isPressed()) {
            x1 = e.getX();
            y1 = e.getY();
        }
        else {
            board.removeMouseListener(this);
            board.removeMouseMotionListener(this);
            board.setCursor(Cursor.getDefaultCursor());

            textButton.getModel().setPressed(false);
            textButton.getModel().setArmed(false);
            board.repaint();

            Gui.setSelectedShapeButton(null);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

        graphics.setPaintMode();
        graphics.setColor(colorNow.getBackground());
        String fontName = "Arial";
        int fontStyle = Font.PLAIN;
        int fontSize = 18;


        /**
         * test add
         */
        try {
            Text text1 = new Text(x1, y1, text, colorNow.getBackground(), fontName, fontStyle, fontSize);
            text1.draw(graphics);
            proccessor.addShape(text1);

        } catch (RemoteException ex) {
            log.error("Text add failed: "+ex.getMessage());
            return;
        }
        board.repaint();

        board.removeMouseListener(this);
        board.removeMouseMotionListener(this);
        board.setCursor(Cursor.getDefaultCursor());

        textButton.getModel().setPressed(false);
        textButton.getModel().setArmed(false);

        Gui.setSelectedShapeButton(null);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        ListenerTool.detectAndRemove(textButton,board,this);
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
