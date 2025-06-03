package server;

import model.shape.Shape;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RmiProcessor extends Remote {


    // add shape to the board
    void addShape(Shape shape) throws RemoteException;

    void addChatMsg(String msg,String username) throws RemoteException;

    List<Shape> getShapes() throws RemoteException;

    List<String> getUsernames() throws RemoteException;
}
