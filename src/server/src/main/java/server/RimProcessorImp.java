package server;

import common.entity.User;
import common.helper.BoardcastHelper;
import model.gui.Gui;
import model.shape.Shape;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class RimProcessorImp extends UnicastRemoteObject implements RmiProcessor {

    private final List<Shape> shapeList;


    public RimProcessorImp(List<Shape> shapeList) throws RemoteException{
        this.shapeList = shapeList;
    }

    @Override
    public List<Shape> getShapes() throws RemoteException {
        return shapeList;
    }

    @Override
    public void addShape(Shape shape) throws RemoteException {
        //add to the server
        shapeList.add(shape);

        //refresh
        Gui.getINSTANCE().repaint();

        //let other clients know new shape
        BoardcastHelper.addShape(shape);

    }

    @Override
    public List<String> getUsernames() throws RemoteException {
        return UserProcessor.getINSTANCE().getUsernames();
    }

    @Override
    public void addChatMsg(String msg, String username) throws RemoteException {
        Gui.getINSTANCE().addChatMessage(msg,username);
    }

}
