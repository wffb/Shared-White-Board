package model.gui;

import com.alibaba.fastjson2.JSONObject;
import common.entity.User;
import common.helper.BoardcastHelper;
import config.ServerConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import model.listener.*;
import model.shape.Shape;
import server.RmiProcessor;
import server.ServerSocketHandler;
import server.UserProcessor;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;


@Slf4j
public class Gui extends JFrame{


    @Getter
    private static Gui INSTANCE;


//    private static JButton selectedShapeButton;
//    private static JComboBox<String> fontComboBox;

    @Getter
    @Setter
    private static JButton selectedShapeButton;

    @Getter
    private static JPanel userPanel;
    @Getter
    private static JPanel chatPanel;

    private static Board board;

    public static Boolean isErased = false;

    private static JSlider slider;
    private static JPanel colorNow;
    private static JLabel status;


    /**
     * can change to concurrentMap
     */



    private static CopyOnWriteArrayList<String> chatMsgList = new CopyOnWriteArrayList<>();
    private static JPanel chatMsgContainer;


    public static void init(CopyOnWriteArrayList<Shape> shapeList, RmiProcessor processor){

        INSTANCE = new Gui(shapeList,processor);

        //show board
        INSTANCE.setVisible(true);

    }


    private Gui(CopyOnWriteArrayList<Shape> shapeList, RmiProcessor processor){


        /**
         * frame set
         */
        setSize(1100, 800);


        JPanel boardPanel = new JPanel();
        board = new Board(shapeList);
        boardPanel.add(board);

        //
        PanelFactory.buildLayerPanel(getLayeredPane());
        //
        JPanel controlPanel = PanelFactory.buildControlPanel(getWidth(),processor);
        //
        userPanel = buildUserPanel();
        //
        chatPanel = buildChatPanel();

        // mainPanel add
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(userPanel,BorderLayout.WEST);
        mainPanel.add(chatPanel,BorderLayout.EAST);

        setContentPane(mainPanel);

        //quit
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int dialogResult = JOptionPane.showConfirmDialog(Gui.this, "Are you sure you want to quit?", "Confirm quit", JOptionPane.YES_NO_OPTION);
                if (dialogResult == JOptionPane.YES_OPTION) {

                    BoardcastHelper.serverQuit();

//                    else {
//                        try {
//                            serverService.removeUserName(WhiteBoardGUI.getUserName());
//                            JSONObject jsonObject = new JSONObject();
//                            jsonObject.put("request", "quit");
//                            sendEncrypted(jsonObject.toJSONString(), writer);
//                        } catch (RemoteException re) {
//                            re.printStackTrace();
//                        }
//                    }
                    System.exit(0);
                }
            }
        });



        board.repaint();
    }

    public synchronized void repaint() {
        board.repaint();
    }


    public static class PanelFactory {

        static public void buildLayerPanel(JLayeredPane layeredPane){
            // labels of buttons
            status = new JLabel();
            status.setFont(new Font("Serif", Font.BOLD, 14));
            status.setHorizontalAlignment(JLabel.CENTER);
            status.setVerticalAlignment(JLabel.CENTER);
            status.setOpaque(true);
            status.setBackground(new Color(255, 255, 192, 255));
            Border border = BorderFactory.createLineBorder(new Color(226, 226, 218, 255), 2, true);
            status.setBorder(border);
            status.setVisible(false);

            layeredPane.add(status, JLayeredPane.POPUP_LAYER);
        }

        static public JPanel buildControlPanel(int width, RmiProcessor proccessor){
            // controlPanel on the top
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.LINE_AXIS));
            controlPanel.setPreferredSize(new Dimension(width, 60));
            controlPanel.setMaximumSize(new Dimension(width, 60));

            /**
             * color
             */
            // show current color
            colorNow = new JPanel(new BorderLayout());
            colorNow.setBackground(Color.BLACK);
            //currentColorPanel.setMinimumSize(new Dimension(25, 25));
            colorNow.setPreferredSize(new Dimension(25, 25));
            colorNow.setMaximumSize(new Dimension(25, 25));
            controlPanel.add(Box.createHorizontalStrut(25));
            controlPanel.add(colorNow);
            controlPanel.add(Box.createHorizontalStrut(15));

            controlPanel.add(buildColorPanel(colorNow));
            controlPanel.add(Box.createHorizontalStrut(15));

            /**
             * stroke slider
             */
            slider = buildStriker();
            controlPanel.add(slider);
            controlPanel.add(Box.createHorizontalStrut(15));

            /**
             * shape
             */
            controlPanel.add(buildShapePanel(proccessor));
            controlPanel.add(Box.createHorizontalStrut(15));

            /**
             * eraser
             */
            controlPanel.add(buildEraserPanel());

            return controlPanel;
        }

        static private JToggleButton buildEraserPanel(){
            /**
             *  eraser
             */
            //JPanel eraserPanel = new JPanel(new GridLayout(1, 5, 10, 0));

            //eraser button
            JToggleButton eraserButton = new JToggleButton();
            eraserButton.setPreferredSize(new Dimension(80, 40));
            eraserButton.setMaximumSize(new Dimension(80, 40));
            eraserButton.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "eraser"));
            eraserButton.addActionListener(e -> {
                if (Gui.getSelectedShapeButton() != null) {
                    Gui.getSelectedShapeButton().getModel().setPressed(true);
                }
                Gui.isErased = !Gui.isErased;
                eraserButton.getModel().setSelected(Gui.isErased);
            });

            return eraserButton;
        }

        static private JPanel buildColorPanel(JPanel colorNow){
            Color[] colorList = {
                    Color.BLACK, Color.GRAY, new Color(255, 102, 102), new Color(255, 178, 102),
                    new Color(255, 255, 102), new Color(178, 255, 102), new Color(102, 255, 102), new Color(102, 255, 178),
                    new Color(102, 255, 255), new Color(102, 178, 255), new Color(178, 102, 255), new Color(255, 102, 255),
                    new Color(255, 51, 0), new Color(255, 153, 51), new Color(0, 153, 51), new Color(153, 51, 255)
            };
            JPanel colorPanel = new JPanel(new GridLayout(2, 8, 5, 5));
            colorPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Colors"));
            for (Color c : colorList) {
                JButton button = new JButton();
                button.setBackground(c);
                button.setMinimumSize(new Dimension(15, 15));
                button.setPreferredSize(new Dimension(15, 15));
                button.setMaximumSize(new Dimension(15, 15));
                button.addActionListener(e -> {
                    colorNow.setBackground(button.getBackground());
                    if (Gui.getSelectedShapeButton() != null) {
                        Gui.getSelectedShapeButton().getModel().setPressed(true);
                    }
                });
                colorPanel.add(button);
            }
            colorPanel.setPreferredSize(new Dimension(180, 60));
            colorPanel.setMaximumSize(new Dimension(180, 60));

            return colorPanel;
        }

        static private JSlider buildStriker(){

            // Add  slider to the right
            JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 10, 3);
            slider.setPreferredSize(new Dimension(160, 55));
            slider.setMaximumSize(new Dimension(200, 55));
            slider.setPaintTicks(true);
            slider.setMinorTickSpacing(1);
            slider.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Stroke"));
            slider.addChangeListener(e -> {
                if (Gui.getSelectedShapeButton() != null) {
                    Gui.getSelectedShapeButton().getModel().setPressed(true);
                }
            });

            return slider;
        }

        static private JPanel buildShapePanel(RmiProcessor processor){
            // Add shape buttons to the center
            JPanel shapePanel = new JPanel(new GridLayout(1, 5, 10, 0));
            shapePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "shape"));
            shapePanel.setPreferredSize(new Dimension(350, 55));
            shapePanel.setMinimumSize(new Dimension(350, 55));
            shapePanel.setMaximumSize(new Dimension(450, 55));

            //add buttons
            buildShapeButtons(shapePanel,processor);

            return shapePanel;
        }


        static private void buildShapeButtons(JPanel panel, RmiProcessor processor) {
            //line
            JButton lineButton = new JButton("Line");

            // draw line button
            lineButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {

                    super.mouseClicked(e);
                    if (e.getButton() == MouseEvent.BUTTON1 && !lineButton.getModel().isPressed()) {
                        //Gui.setTextComboBoxVisible(false);
                        judgeSelected(lineButton);

                        board.addMouseListener(new LineMouseListener((Graphics2D) board.getGraphics(), board, slider, colorNow, lineButton,processor));
                        board.addMouseMotionListener(new LineMouseListener((Graphics2D) board.getGraphics(), board, slider, colorNow, lineButton,processor));
                    }
                }

            });
            //add
            panel.add(lineButton);

            // draw circle button
            JButton circleButton = new JButton("Circ");
            circleButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    if (e.getButton() == MouseEvent.BUTTON1 && !circleButton.getModel().isPressed()) {

                        judgeSelected(circleButton);

                        board.addMouseListener(new CircleMouseListener((Graphics2D) board.getGraphics(), board, slider, colorNow, circleButton,processor));
                        board.addMouseMotionListener(new CircleMouseListener((Graphics2D) board.getGraphics(), board, slider, colorNow, circleButton,processor));
                    }
                }

            });
            panel.add(circleButton);

            // draw oval button
            JButton ovalButton = new JButton("Oval");
            ovalButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    if (e.getButton() == MouseEvent.BUTTON1 && !ovalButton.getModel().isPressed()) {

                        judgeSelected(ovalButton);

                        board.addMouseListener(new OvalMouseListener((Graphics2D) board.getGraphics(), board, slider, colorNow, ovalButton,processor));
                        board.addMouseMotionListener(new OvalMouseListener((Graphics2D) board.getGraphics(), board, slider, colorNow, ovalButton,processor));
                    }
                }
            });
            panel.add(ovalButton);

            // draw rectangle button
            JButton rectangleButton = new JButton("Rect");
            rectangleButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    if (e.getButton() == MouseEvent.BUTTON1 && !ovalButton.getModel().isPressed()) {

                        judgeSelected(rectangleButton);

                        board.addMouseListener(new RectangleMouseListener((Graphics2D) board.getGraphics(), board, slider, colorNow, rectangleButton,processor));
                        board.addMouseMotionListener(new RectangleMouseListener((Graphics2D) board.getGraphics(), board, slider, colorNow, rectangleButton,processor));
                    }
                }
            });
            panel.add(rectangleButton);

            // draw rectangle button
            JButton triangleButton = new JButton("Tria");
            triangleButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    if (e.getButton() == MouseEvent.BUTTON1 && !ovalButton.getModel().isPressed()) {

                        judgeSelected(triangleButton);

                        board.addMouseListener(new TriangleMouseListener((Graphics2D) board.getGraphics(), board, slider, colorNow, triangleButton,processor));
                        board.addMouseMotionListener(new TriangleMouseListener((Graphics2D) board.getGraphics(), board, slider, colorNow, triangleButton,processor));
                    }
                }
            });
            panel.add(triangleButton);

            //draw text button
            JButton textButton = new JButton("Text");

            textButton.setPreferredSize(new Dimension(25, 25));
            textButton.setMaximumSize(new Dimension(25, 25));
            textButton.setMinimumSize(new Dimension(25, 25));

            textButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);

                    board.clean();

                    if (e.getButton() == MouseEvent.BUTTON1 && !textButton.getModel().isPressed()) {
                        if (selectedShapeButton != null) {
                            selectedShapeButton.getModel().setPressed(false);
                            selectedShapeButton.getModel().setArmed(false);
                            Gui.setSelectedShapeButton(null);
                        }
                        //show input area
                        String text = JOptionPane.showInputDialog(board, "Please enter text here: ", "Text Input Area", JOptionPane.PLAIN_MESSAGE);

                        textButton.getModel().setPressed(true);
                        if (!Objects.isNull(text) && !text.isEmpty()) {
                            Gui.setSelectedShapeButton(textButton);

                            board.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                            board.addMouseListener(new TextMouseListener((Graphics2D) board.getGraphics(), board, slider, colorNow, textButton, text,processor));
                            board.addMouseMotionListener(new TextMouseListener((Graphics2D) board.getGraphics(), board, slider, colorNow, textButton, text,processor));
                        } else {
                            board.setCursor(Cursor.getDefaultCursor());
                            textButton.getModel().setPressed(false);
                            textButton.getModel().setArmed(false);

                            Gui.setSelectedShapeButton(null);
                        }
                    }
                }

            });
            panel.add(textButton);
        }
        private static void judgeSelected(JButton button) {

            board.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            if (selectedShapeButton != null) {
                selectedShapeButton.getModel().setPressed(false);
                selectedShapeButton.getModel().setArmed(false);
                Gui.setSelectedShapeButton(null);
            }
            button.getModel().setPressed(true);
            Gui.setSelectedShapeButton(button);

            board.clean();
        }
    }

    public Boolean managerJudgeJoin(String userName, String userAddress){

        String dialogMessage = userName + " from "+userAddress +" is requesting to join. Do you accept?";
        String[] buttonLabels = {"Accept", "Decline"};
        int option = JOptionPane.showOptionDialog(null, dialogMessage, "Join request",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, buttonLabels, buttonLabels[0]);

        return option == JOptionPane.YES_NO_OPTION;
    }


    private JPanel buildUserPanel(){

        userPanel = new JPanel();
        userPanel.setBackground(Color.WHITE);
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));

        userPanel.setBorder(BorderFactory.createTitledBorder("User List"));
        userPanel.setPreferredSize(new Dimension(200, 200));
        userPanel.setMaximumSize(new Dimension(200, 200));

        refreshUserPanel();

        return userPanel;
    }

    public static void refreshUserPanel() {

        int userCount = 0;
        JPanel newPanel = new JPanel();

        for (User user : UserProcessor.getINSTANCE().getUsers()) {

            JPanel userRowPanel = new JPanel();
            //border
            userRowPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.WHITE),
                    BorderFactory.createEmptyBorder(5, 0, 5, 0)
            ));
            //color
            if (userCount == 0){
                userRowPanel.setBackground(Color.LIGHT_GRAY);
            }

            else{
                userRowPanel.setBackground(Color.WHITE);
                userRowPanel.setBorder(BorderFactory.createEtchedBorder());
            }


            userRowPanel.setLayout(new BoxLayout(userRowPanel, BoxLayout.X_AXIS));
            userRowPanel.setPreferredSize(new Dimension(150, 35));
            userRowPanel.setMaximumSize(new Dimension(150, 35));

            JLabel userNameLabel = new JLabel(user.getUsername());
            userNameLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    JOptionPane.showMessageDialog(
                            null,
                            "User name:                 " + user.getUsername() + System.lineSeparator() + "User Remote address:       "+ user.getRemoteAddress() ,
                            "User Information", JOptionPane.INFORMATION_MESSAGE
                    );
                }
             });
            userRowPanel.add(userNameLabel);



            //manager
            if (userCount == 0) {
                JLabel managerLabel = new JLabel("[ Manager ]");
                userRowPanel.add(Box.createHorizontalGlue());
                userRowPanel.add(managerLabel);
                userRowPanel.add(Box.createHorizontalStrut(10));
            }

            //user management
            //view
            if( userCount > 0) {

                //kick
                JButton kickButton = new JButton("Kick");
                Insets insets = new Insets(2, 1, 2, 1);
                kickButton.setMargin(insets);
                kickButton.setPreferredSize(new Dimension(36, 20));
                kickButton.setMaximumSize(new Dimension(36, 20));

                kickButton.addActionListener(e -> {

                    String[] buttonLabels = {"Yes", "No"};

                    int option = JOptionPane.showOptionDialog(null, "Do you want to kick this user?", "Confirm Kick",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, buttonLabels, buttonLabels[0]);

                    if (option == JOptionPane.YES_OPTION) {

                        removeUser(user);

                    }
                });

                userRowPanel.add(Box.createHorizontalGlue());
                userRowPanel.add(Box.createHorizontalStrut(5));
                userRowPanel.add(kickButton);
                userRowPanel.add(Box.createHorizontalStrut(10));
            }
            newPanel.add(userRowPanel);
            userCount++;
        }

        //Concurrent control of the limited scope
        synchronized (Gui.class){

            userPanel = newPanel;
            userPanel.repaint();
            userPanel.revalidate();

        }

    }

    private JPanel buildChatPanel(){
        JPanel jPanel = new JPanel();
        jPanel.setBackground(Color.WHITE);
        //jPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));

        jPanel.setBorder(BorderFactory.createTitledBorder("Chat List"));
        jPanel.setPreferredSize(new Dimension(200, 200));
        jPanel.setMaximumSize(new Dimension(200, 200));


        jPanel.add(buildChatMsgPanel(),BorderLayout.CENTER);
        jPanel.add(buildChatInputPanel(),BorderLayout.SOUTH);
        return jPanel;
    }

    private JScrollPane buildChatMsgPanel(){



        chatMsgContainer = new JPanel();
        chatMsgContainer.setLayout(new BoxLayout(chatMsgContainer, BoxLayout.Y_AXIS));
        chatMsgContainer.setMinimumSize(new Dimension(250, 1000));
        chatMsgContainer.setPreferredSize(new Dimension(250, 1000));
        chatMsgContainer.setMaximumSize(new Dimension(1000, 1000));

       // addChatMessage("hb","something here");

        JScrollPane jScrollPane = new JScrollPane(chatMsgContainer,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setMinimumSize(new Dimension(190, 600));
        jScrollPane.setPreferredSize(new Dimension(190, 600));
        jScrollPane.setBackground(Color.BLUE);





        return  jScrollPane;
    }


    private JPanel buildChatInputPanel(){
        // chatInputPanel on the right bottom
        JPanel chatInputPanel = new JPanel(new BorderLayout());
        chatInputPanel.setBorder(BorderFactory.createEtchedBorder());
        JTextField chatInputField = new JTextField(10);
        JButton chatSendButton = new JButton("Send");

        chatSendButton.addActionListener(e -> {
            String currentMessage = chatInputField.getText();

            if (currentMessage != null && !currentMessage.equals("")){
                //add to server
                addChatMessage(ServerConfig.adminUsername,currentMessage );

                chatInputField.setText("");
            }
        });

        chatInputPanel.add(chatInputField, BorderLayout.CENTER);
        chatInputPanel.add(chatSendButton, BorderLayout.EAST);

        return chatInputPanel;
    }


    public void addChatMessage(String username,String msg){
        //add
        chatMsgList.add(username+": "+msg);
        chatMsgContainer.add(addChatPanel(username+": "+msg),BorderLayout.WEST);

        SwingUtilities.updateComponentTreeUI(chatMsgContainer);

        //board cast
        BoardcastHelper.addChatMsg(username,msg);
    }

    private JPanel addChatPanel(String chat){

        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //border
        rowPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE),
                BorderFactory.createEmptyBorder(5, 0, 5, 0)
        ));
        //color
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(BorderFactory.createEtchedBorder());

        rowPanel.setPreferredSize(new Dimension(180, 35));
        rowPanel.setMaximumSize(new Dimension(500, 35));

        JLabel msgLabel = new JLabel(chat);
        rowPanel.add(msgLabel);


        return rowPanel;

    }

    public static void removeUser(User user){

        if(Objects.isNull(user))
            return;

        String username = user.getUsername();
        String userAddress = user.getRemoteAddress();

        //cancel in cache
        UserProcessor.getINSTANCE().deleteUser(user);
        refreshUserPanel();

        //inform user
        BoardcastHelper.kickOther(username);


        // cancel in server
        try {
            user.getChannel().close();
            user.getKey().cancel();

        } catch (IOException e) {
            log.error("User remove failed:"+e.getMessage());
            return;
        }

        log.info(username+" "+userAddress+": " + " outline for kicked out");
    }

    public static void removeUserByUsername(String username){

         removeUser(UserProcessor.getINSTANCE().getUserByUsername(username));
    }
}
