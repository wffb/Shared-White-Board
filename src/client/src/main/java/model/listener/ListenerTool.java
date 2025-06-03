package model.listener;

import model.gui.Board;
import model.gui.Gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.EventListener;

public class ListenerTool {

    public static void detectAndRemove(JButton button, Board board, EventListener listener){
        if (!button.getModel().isPressed()) {

            board.removeMouseListener((MouseListener) listener);
            board.removeMouseMotionListener((MouseMotionListener)listener);

            board.setCursor(Cursor.getDefaultCursor());
            button.getModel().setPressed(false);
            button.getModel().setArmed(false);
            Gui.setSelectedShapeButton(null);
        }
    }
}
