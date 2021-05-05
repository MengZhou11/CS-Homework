package minesweepertiles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class MinesweeperGame extends JFrame{
    private final JPanel timeLeftPanel; //timer panel
    private final int timeLeft = 1000; //游戏总共1000s
    private final JLabel timeLeftLabel; //timer bar
    private final JLabel mineStatus; //左下角的
    private int savedID;
    private int maxLoadingID;

    MinesweeperHelper mineSweeperGameBoard;  //gameBoard
    Socket socket = null;  //when use save and load


    public MinesweeperGame() {
        //set up menu bar, timeLeft, and mine counting info.
        setTitle("Minesweeper");
        createMenus();

        //timer bar
        timeLeftPanel = new JPanel();
        timeLeftLabel = new JLabel("Time Remaining: " + timeLeft);
        timeLeftPanel.add(timeLeftLabel);
        add(timeLeftPanel, BorderLayout.NORTH);

        //add center component and mine status bar
        mineStatus = new JLabel("");
        add(mineStatus, BorderLayout.SOUTH);
        mineSweeperGameBoard = new MinesweeperHelper(mineStatus, timeLeftLabel);
        add(mineSweeperGameBoard, BorderLayout.CENTER);



        //for the whole game board, location in screen, resizeable and default operation
        setResizable(false);
        setLocationRelativeTo(null);
        setSize(240, 335);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void createMenus() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        //add items into File menu
        JMenuItem New = createNewMenuItem();
        JMenuItem Open = createOpenMenuItem();
        JMenuItem Save = createSaveMenuItem();
        JMenuItem Score = createHighScoreItem();
        JMenuItem Exit = createExitMenuItem();
        fileMenu.add(New);
        fileMenu.add(Open);
        fileMenu.add(Save);
        fileMenu.add(Score);
        fileMenu.add(Exit);
    }

    // new menu is done
    private JMenuItem createNewMenuItem() {
        JMenuItem item = new JMenuItem("New");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //stop and remove the game first
                mineSweeperGameBoard.stopGame();
                remove(mineSweeperGameBoard);
                //create new game and add it to this frame
                mineSweeperGameBoard = new MinesweeperHelper(mineStatus , timeLeftLabel);
                add(mineSweeperGameBoard, BorderLayout.CENTER);
                timeLeftLabel.setText("Time Remaining: "+ timeLeft);
            }
        });
        return item;
    }
    //open menu is here
    private JMenuItem createOpenMenuItem(){
        JMenuItem item = new JMenuItem("Load");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!LoadBoard()){
                    JOptionPane.showMessageDialog(null, "Failed loading game...MAX GAME ID: "+maxLoadingID,"Loading game",JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        return item;
    }
    //save menu is here
    private JMenuItem createSaveMenuItem(){
        JMenuItem item = new JMenuItem("Save");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(SaveBoard()){
                    JOptionPane.showMessageDialog(null, "Your game has been saved! ID: "+savedID,"Saving game",JOptionPane.WARNING_MESSAGE);
                }else{
                    JOptionPane.showMessageDialog(null, "Your game could not be saved!","Saving game",JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        return item;
    }
    //for extra credit  5 high scores
    private JMenuItem createHighScoreItem(){
        JMenuItem item = new JMenuItem("Top Scores");
        class exitListener implements ActionListener {
            public void actionPerformed(ActionEvent event) {
                String str="";
                //needed to be done
                JOptionPane.showMessageDialog(null, str,"5 High Scores",JOptionPane.WARNING_MESSAGE);
            }
        }
        item.addActionListener(new exitListener());
        return item;
    }

    //exit menu is done
    private JMenuItem createExitMenuItem(){
        JMenuItem item = new JMenuItem("Exit");
        class exitListener implements ActionListener
        {
            public void actionPerformed(ActionEvent event)
            { System.exit(0); }
        }
        item.addActionListener(new exitListener());
        return item;
    }


    public boolean LoadBoard() {
        String str ="";
        String  stoppedTime="", mineLeftBar="";
        String [] whole = new String[16*16+2];

        try {
            socket = new Socket(InetAddress.getLocalHost(), 8000);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            System.out.println("\n Sending request to Socket Server to load a game");
            oos.writeObject("LOAD");
            int val = Integer.parseInt(JOptionPane.showInputDialog("Enter id of the game that you want to load: "));
            maxLoadingID = Integer.parseInt(String.valueOf(ois.readObject()));
            if(val>maxLoadingID){
                return false;
            }

            oos.writeObject(val);  //到这里没有问题
            System.out.println(val);
            str = (String) ois.readObject();
            whole = str.split(" ");
//            for(String i: whole){
//                System.out.print(i+" ");
//            }
            stoppedTime = whole[16*16];
            mineLeftBar = whole[16*16+1];


            oos.close();
            ois.close();
            socket.close();

            // 开始更新game board
            //timer and mine bar
            mineSweeperGameBoard.mineBar.setText(mineLeftBar);  //set another parameter
            mineSweeperGameBoard.timeCounting = Integer.parseInt(stoppedTime);
            timeLeftLabel.setText("Time Remaining: "+ stoppedTime);  //set parameters first
            //update board
            for(int i=0; i<whole.length-2; i++){
                mineSweeperGameBoard.cellList[i] = Integer.parseInt(whole[i]);
            }
            repaint();
            return true;

        } catch (IOException | ClassNotFoundException e) {
            return false;
        }
    }


    public boolean SaveBoard() {
        //convert cell list to a string
//        String cellListStr= new String();
//        for(int i=0; i<mineSweeperGameBoard.cellList.length; i++){
//            cellListStr += mineSweeperGameBoard.cellList[i] +" ";
//        }
        //get timer
        int stoppedTime =mineSweeperGameBoard.timeCounting; //take number only
        //get status bar
        String mineLeftBar = mineStatus.getText(); //set it with no space
        //A string for all info
        String[] str = new String[16*16+2];
        for(int i=0; i<mineSweeperGameBoard.cellList.length; i++){
            str [i] = ""+mineSweeperGameBoard.cellList[i];
        }
        str[16*16] = stoppedTime+"";
        str[16*16+1] = mineLeftBar;

        try {
            socket = new Socket(InetAddress.getLocalHost(), 8000);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            System.out.println("Sending request to Socket Server");
            oos.writeObject("SAVE");
            oos.writeObject(str);
            int val = ois.read();
            savedID = ois.read();
            System.out.println(savedID);
            if(val==1){   //if server sends 1 means saved!
                afterSavingGame();
                return true;
            }

            oos.close();
            ois.close();
            socket.close();
        } catch (IOException  e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean afterSavingGame () {
        //prepare a new board
        mineSweeperGameBoard.stopGame();
        remove(mineSweeperGameBoard);
        //create new game and add it to this frame
        mineSweeperGameBoard = new MinesweeperHelper(mineStatus, timeLeftLabel);
        add(mineSweeperGameBoard, BorderLayout.CENTER);
        timeLeftLabel.setText("Time Remaining: "+ timeLeft);
        return true;
    }



    //main is here
    public static void main(String[] args){
        MinesweeperGame minesweeper = new MinesweeperGame();
        minesweeper.setVisible(true);
    }

}