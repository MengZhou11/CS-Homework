package minesweepertiles;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;

public class Server extends Thread{
    ServerSocket serverSocket;
    String[] saveString;
    private Connection conn;   //to connect database file
    private final String dbTableName = "Minesweeper";
    private final String col1 = "allCellList";
    private final String col2 = "stoppedTimer";
    private final String col3 = "mineLeftBar";
    private static int id;
    private final int NUM_COLS = 4;
    private String topInfo = "";

    public Server() {
        //connect to its database file
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:Minesweeper.db"); //如果这个位置没有db文件就会自动建立一个

            //create a db file if not exits. for first time.
            String strQuery2 = "CREATE TABLE IF NOT EXISTS "+dbTableName +" ("+col1+" TEXT, "+col2 +" TEXT, "+ col3 +" TEXT, ID INT, UNIQUE (ID)) ";  //总共有4列
            Statement statement2 =conn.createStatement();
            statement2.executeUpdate(strQuery2);

            String strQuery = "SELECT * FROM Minesweeper"; //if no error means there is a table Minesweeper and two cols
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(strQuery);
            ResultSetMetaData rsm = rs.getMetaData();
            int numColumns = rsm.getColumnCount();
            while (rs.next()) {
                String rowString = "";
                for (int i = 1; i <= numColumns; i++) {
                    Object o = rs.getObject(i);
                       rowString += o.toString()+" ";
                }
                System.out.println(rowString);
                id++;
                }
        } catch (SQLException e) {
            System.err.println("Connection error: " + e);
            System.exit(1);
        }

    }

    @Override
    public void run(){
        try {
            serverSocket = new ServerSocket(8000);

            while(true){
                Socket socket = serverSocket.accept();
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                System.out.println("Socket server is Receiving...");
                String mess = (String) ois.readObject();
                System.out.println(mess);
                if(mess.equals("SAVE")){
                    System.out.println("saving game....");
                    saveString = (String []) ois.readObject();  //read the next input stream
                    save(saveString);
                    //save the string[] to database
                    oos.write(1);    //save request has been complete
                    oos.write(id);
                    System.out.println("Saved!");
                }
                if(mess.equals("LOAD")){
                    System.out.println("loading game....");
                    oos.writeObject(id);
                    int val = (int)ois.readObject();
                    System.out.println(val); //problem fixed
                    oos.writeObject(load(val));
                    System.out.print(load(val));
                 //   oos.write(2);   //load request has been complete
                    System.out.println("Loaded! ");
                }
                if(mess.equals("TOP")){
                    System.out.println("loading top scores....");
                    findTop();
                    System.out.println(topInfo);
                    oos.writeObject(topInfo);
                    System.out.println("Top score has been sent! ");
                }

                oos.close();
                ois.close();
                socket.close();
            }

        } catch (IOException | ClassNotFoundException  e) {
            e.printStackTrace();
        }
    }

    public void save(String [] str){
        //get strings to from str[]
        String cellListStr="", stoppedTime="", mineLeftBar="";
        for(int i=0; i<16*16; i++){
            cellListStr += str[i]+" ";
        }
        stoppedTime=str[16*16];
        System.out.println(stoppedTime);
        mineLeftBar=str[16*16+1];
        System.out.println(mineLeftBar);

        //saving them into db
        String strQuery = "INSERT INTO Minesweeper VALUES ('"+cellListStr + "','" + stoppedTime+"','" + mineLeftBar +"'," + ++id +")";
        Statement statement = null;
        try {
            statement = conn.createStatement();
            statement.executeUpdate(strQuery);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public String load(int id){
        String wholeInfo="";
        int i=1;
        String [] str = new String[16*16+2];
        String strQuery = "SELECT "+col1 +" ,"+ col2 +" , "+ col3+" , ID FROM "+dbTableName +" WHERE ID="+ id;
        Statement statement = null;

        try {  //把数据装到local var里面
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(strQuery);
            ResultSetMetaData rsd = rs.getMetaData();
            while(rs.next()){
                if(i==1){
                    Object o = rs.getObject(i);
                    wholeInfo+=o.toString();
                    i++;
                }
                if(i==2){
                    Object o = rs.getObject(i);
                    wholeInfo+=o.toString()+" ";
                    i++;
                }
                if(i==3){
                    Object o = rs.getObject(i);
                    wholeInfo+=o.toString()+" ";
                    i++;
                }//no need id info
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return wholeInfo;
    }


    //only when somebody won can be selected on the top score
    public void findTop(){
        String strQuery3 = "SELECT * FROM Minesweeper WHERE mineLeftBar!='Lost' ORDER BY mineLeftBar, stoppedTimer DESC";
        Statement statement = null;

        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(strQuery3);
            ResultSetMetaData rsm = rs.getMetaData();
            int numColumns = rsm.getColumnCount();
            ArrayList<Integer> arr = new ArrayList<>();
            int count=0;
            topInfo="";
            while (rs.next() &&count<5) {
                for (int i = 2; i <= numColumns; i++) {
                    if(i==2){
                        int o = 1000-rs.getInt(i);
                        topInfo += "Time used: " + o;
                    }
                    if(i==3){
                        Object o = rs.getObject(i);
                        topInfo += " Mine left: " + o.toString();
                    }
                    if(i==4){
                        Object o = rs.getObject(i);
                        topInfo += " ID: " + o.toString();
                    }
                }
                topInfo+="\n";
                count++;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }


    //main is here
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
