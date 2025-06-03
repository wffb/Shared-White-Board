package model.gui;

import client.ClientProcessor;
import client.ClientSocketHandler;
import com.alibaba.fastjson2.JSONObject;
import config.ClientConfig;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import model.listener.*;
import model.shape.Shape;
import server.RmiProcessor;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Objects;


public class Gui extends JFrame{


    @Getter
    private static Gui INSTANCE;
    @Getter
    private static JPanel chatPanel;


//    private static JButton selectedShapeButton;
//    private static JComboBox<String> fontComboBox;

    @Getter
    @Setter
    private static JButton selectedShapeButton;

    @Getter
    private static JPanel userPanel;

    private static Board board;

    public static Boolean isErased = false;

    private static JSlider slider;
    private static JPanel colorNow;
    private static JLabel status;
    private static ArrayList<String> usernameList;

    private static ArrayList<String> chatMsgList = new ArrayList<>();
    private static JPanel chatMsgContainer;



    public static void init(ArrayList<Shape> shapeList,ArrayList<String> userList1, RmiProcessor processor){

        //user
        usernameList = userList1;

        INSTANCE = new Gui(shapeList,processor);
        //show board
        INSTANCE.setVisible(true);



    }


    private Gui(ArrayList<Shape> shapeList, RmiProcessor processor){


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

                    ClientSocketHandler.clientQuit();

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

        userPanel.removeAll();
        userPanel.revalidate();

        int userCount = 0;

        for (String user : usernameList) {

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
            userRowPanel.setMaximumSize(new Dimension(3000, 35));

            JLabel userNameLabel = new JLabel(user);
            userRowPanel.add(userNameLabel);



            //manager
            if (userCount == 0) {
                JLabel managerLabel = new JLabel("[ Manager ]");
                userRowPanel.add(Box.createHorizontalGlue());
                userRowPanel.add(managerLabel);
                userRowPanel.add(Box.createHorizontalStrut(10));
            }

            //client
            if( userCount > 0) {
                JLabel managerLabel = new JLabel("[ Client ]");
                userRowPanel.add(Box.createHorizontalGlue());
                userRowPanel.add(managerLabel);
                userRowPanel.add(Box.createHorizontalStrut(10));
            }

            userPanel.add(userRowPanel);
            userCount++;
        }

        userPanel.repaint();
        userPanel.revalidate();
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
                //refresh
                addChatMessage(ClientConfig.username,currentMessage );
                //send to server
                ClientProcessor.addChatMsg(ClientConfig.username,currentMessage);

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

    public static void setUsers(ArrayList<String> newList){
        usernameList = newList;
    }

}
