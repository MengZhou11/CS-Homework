//Meng Zhou mz3043
//Minesweeper game 2021/05/01
//partial code of this java file are
//learned from zetcode.com 
package minesweepertiles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class MinesweeperBoard extends JPanel{

    protected int[] cellList;  //use one list to place all cells with position
    private boolean inGame;  //to see if the game is ended
    private int minesLeft;   //cal mine left
    protected Image[] image;  //all images should be added inside
    private int allCells;  //number of row*col

    private MouseListener mouseListener;  //use to control the status of game
    protected JLabel mineBar;  //show how many mines left or won lose
    private final JLabel timeBar;  //show how much time left, timer
    private final Timer timer;
    protected int timeCounting = 1000; //set to be protected
    
    private final int CELL_COVER = 10;  //use to show user cover all IMAGE 10
    private final int MARK_FOR_CELL = 11; //use to mark for flags
    private final int EMPTY_CELL = 0; //with no mine surround
    private final int MINE_CELL = 9;  //MINE
    private final int COVERED_MINE_CELL = MINE_CELL + CELL_COVER;  //use the number to indicate there is mine under
    private final int MARKED_MINE_CELL = COVERED_MINE_CELL + MARK_FOR_CELL;  //three layers 1.mine 2.cover 3.flag
    
    protected final int MINE_TOTAL = 40; //total number of mine
    private final int CELL_SIZE = 15; //size of each cell
    protected final int NUM_OF_ROW = 16; //16 rows
    protected final int NUM_OF_COLS = 16; //16 cols
    
    //construction 
    public MinesweeperBoard(JLabel mineBar, JLabel timeLabel) {
        //width size
        int WIDTH = NUM_OF_COLS * CELL_SIZE + 1;//width size
        int HEIGHT = NUM_OF_ROW * CELL_SIZE +1; //height size
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        timeBar = timeLabel;
        this.mineBar = mineBar;
        timer=new Timer(1000, countDownListener);

        //total number of images
        final int NUM_OF_IMAGE = 13;
        image = new Image[NUM_OF_IMAGE];

        for (int i = 0; i < NUM_OF_IMAGE; i++) {
            String path = "/Users/mengzhou/Java/minesweepertiles/" + i + ".png";
            image[i] = (new ImageIcon(path)).getImage();
        }

        prepareGame();
        startGame();
    }

    //把雷和所有数字安排好 在一个cellList里面
    private void prepareGame() {
        int cell;
        Random random = new Random();
        inGame = true;
        minesLeft = MINE_TOTAL;
        mineBar.setText(Integer.toString(minesLeft)); //put 40 mines on status bar
        allCells = NUM_OF_ROW * NUM_OF_COLS; //cal all cells in total

        cellList = new int[allCells];  //each cell corresponds a position in cellList
        for (int i = 0; i < allCells; i++) {
            cellList[i] = CELL_COVER;  //所有的cell现在只有cover的size
        }

        //starting place mines 放雷
        int i = 0;
        while (i < MINE_TOTAL) {
            int position = random.nextInt(allCells); //random generate a position for mine 0-255
            //System.out.println(position);
            if ((position < allCells) && (cellList[position] != COVERED_MINE_CELL)) {  //make sure the number is in range and the position has no mine
                int current_col = position % NUM_OF_COLS;  //find the col in the board
                cellList[position] = COVERED_MINE_CELL;  //set it with 19 which is cover plus mine
                i++;  //mine increase

                //确定这个cell的右边有cells的
                if (current_col < (NUM_OF_COLS - 1)) {
                    //查看右上角的cell 在一定条件下
                    cell = position - NUM_OF_COLS + 1;
                    if (cell >= 0 && (cellList[cell] != COVERED_MINE_CELL)) {
                        cellList[cell] += 1;
                    }
                    //查看右下角的cell 如果有的情况下
                    cell = position + NUM_OF_COLS + 1;
                    if (cell < allCells && (cellList[cell] != COVERED_MINE_CELL)) {
                        cellList[cell] += 1;
                    }
                    //查看正右方的cell
                    cell = position + 1;
                    if (cell < allCells && (cellList[cell] != COVERED_MINE_CELL)) {
                        cellList[cell] += 1;
                    }
                }

                //查看正上面的cell
                cell = position - NUM_OF_COLS;
                if (cell >= 0 &&(cellList[cell] != COVERED_MINE_CELL)) {
                    cellList[cell] += 1;
                }
                //查看正下面的cell
                cell = position + NUM_OF_COLS;
                if (cell < allCells && (cellList[cell] != COVERED_MINE_CELL)) {
                    cellList[cell] += 1;
                }

                //确定这个cell的左边有cells的
                if (current_col > 0) {   //if the mine is not on the first col

                    //左上角的cell
                    cell = position - 1 - NUM_OF_COLS;  //fine its surround cells
                    if (cell >= 0 && (cellList[cell] != COVERED_MINE_CELL)) {
                            cellList[cell] += 1;   //left up 左上角的cell+1 =11
                    }
                    //正左边的cell
                    cell = position - 1;
                    if (cell >= 0 && (cellList[cell] != COVERED_MINE_CELL)) {
                            cellList[cell] += 1;
                    }
                    //左下角的cell
                    cell = position + NUM_OF_COLS - 1;
                    if (cell < allCells && (cellList[cell] != COVERED_MINE_CELL)) {
                            cellList[cell] += 1;
                    }
                }
            }
        }
    }

    //use startGame and stopGame to control game status
    public void startGame(){
        mouseListener = new MouseListener();
        addMouseListener(mouseListener);
    }
    protected void stopGame(){
        removeMouseListener(mouseListener);
        timer.stop();   //停止timer倒计时
    }


    //如果点击到了空的cell 里面的数字应该是10
    private void empty_cells_isPressed(int cellPosition) {
        int current_col = cellPosition % NUM_OF_COLS;
        int cell;

        //确定这个cell的右边有cells的
        if (current_col < (NUM_OF_COLS - 1)) {
            //右上方的cell
            cell = cellPosition - NUM_OF_COLS + 1;
            if (cell >= 0 && (cellList[cell] > MINE_CELL)) {
                cellList[cell] -= CELL_COVER;//显示真正的数字  把cover去掉
                if (cellList[cell] == EMPTY_CELL) {
                    empty_cells_isPressed(cell);//用recursion
                }
            }
            //查看右下方的cell
            cell = cellPosition + NUM_OF_COLS + 1;
            if (cell < allCells && (cellList[cell] > MINE_CELL)) {
                cellList[cell] -= CELL_COVER;//显示真正的数字  把cover去掉
                if (cellList[cell] == EMPTY_CELL) {
                    empty_cells_isPressed(cell);//用recursion
                }
            }
            //查看正右方的cell是不是空白的
            cell = cellPosition + 1;
            if (cell < allCells && (cellList[cell] > MINE_CELL)) {
                cellList[cell] -= CELL_COVER;//显示真正的数字  把cover去掉
                if (cellList[cell] == EMPTY_CELL) {
                    empty_cells_isPressed(cell);//用recursion
                }
            }
        }
            //查看cell正上方的cell是不是空白的
            cell = cellPosition - NUM_OF_COLS;
            if (cell >= 0 && (cellList[cell] > MINE_CELL)) {
                cellList[cell] -= CELL_COVER; //显示真正的数字  把cover去掉
                if (cellList[cell] == EMPTY_CELL) {
                    empty_cells_isPressed(cell);//用recursion
                }
            }
            //查看cell正下方的cell是不是空白的
            cell = cellPosition + NUM_OF_COLS;
            if (cell < allCells && (cellList[cell] > MINE_CELL)) {
                cellList[cell] -= CELL_COVER;//显示真正的数字  把cover去掉
                if (cellList[cell] == EMPTY_CELL) {
                    empty_cells_isPressed(cell);//用recursion
                }
            }

        //确定这个cell的左边有cells的
        if (current_col > 0) {
            //查看左上角的cell
            cell = cellPosition - NUM_OF_COLS - 1;
            if (cell >= 0 && (cellList[cell] > MINE_CELL) ) {//大于9并且这个cell存在的情况下
                cellList[cell] -= CELL_COVER;  //显示真正的数字  把cover去掉
                if (cellList[cell] == EMPTY_CELL) { //如果这个也是empty的  
                    empty_cells_isPressed(cell); //用recursion
                }
            }
            //查看正左边的cell
            cell = cellPosition - 1;
            if (cell >= 0  && (cellList[cell] > MINE_CELL)) {
                cellList[cell] -= CELL_COVER; //显示真正的数字  把cover去掉
                if (cellList[cell] == EMPTY_CELL) {
                    empty_cells_isPressed(cell);//用recursion
                }
            }
            //查看左上角的cell
            cell = cellPosition + NUM_OF_COLS - 1;
            if (cell < allCells && (cellList[cell] > MINE_CELL)) {
                cellList[cell] -= CELL_COVER; //显示真正的数字  把cover去掉
                if (cellList[cell] == EMPTY_CELL) {
                    empty_cells_isPressed(cell);//用recursion
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        int notCovered = 0;
        int SHOW_MINE = 9; // image 9
        int SHOW_COVER = 10; // image 10
        int SHOW_FLAG = 11; //image 11
        int SHOW_X_FLAG = 12; //image 12
        for (int i = 0; i < NUM_OF_ROW; i++) {
            for (int j = 0; j < NUM_OF_COLS; j++) {

                int cell = cellList[(i * NUM_OF_COLS) + j]; //根据position找到cell在list中的位置

                if (inGame && cell == MINE_CELL) { //如果点到了雷 游戏结束
                    inGame = false;
                }
                //接着  如果点到了雷就把所有的雷展示出来
                if (!inGame) {
                    if (cell == COVERED_MINE_CELL) {
                        cell = SHOW_MINE;
                    } else if (cell == MARKED_MINE_CELL) {
                        cell = SHOW_FLAG;
                    } else if (cell > COVERED_MINE_CELL) {
                        cell = SHOW_X_FLAG;
                    } else if (cell > MINE_CELL) {
                        cell = SHOW_COVER;
                    }
                   // stopGame();   //停止游戏 只有点new才能再重新开一盘
                } else {
                    if (cell > COVERED_MINE_CELL) {
                        cell = SHOW_FLAG;
                    } else if (cell > MINE_CELL) {
                        cell = SHOW_COVER;
                        notCovered++;
                    }
                }
                //总之，用对应的数字来展示对应的图片, 用i和j乘以每个cell的size来确定绘图的位置
                g.drawImage(image[cell], (j * CELL_SIZE), (i * CELL_SIZE), this);
            }
        }
        if (notCovered == 0 && inGame) {
            inGame = false;
            mineBar.setText("Won");
            stopGame();
        } else if (!inGame) {
            mineBar.setText("Lost");
            stopGame();
        }
    }


//for timer， make a listener
    ActionListener countDownListener=new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent e) {
            timeCounting -=1;
            timeBar.setText("Time Remaining: " + timeCounting);
            if (timeCounting <= 0) {
                timer.stop();
                timeBar.setText("Time is up!");
                mineBar.setText("Game lost");
                removeMouseListener(mouseListener);
            }
        }
    };


    private class MouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            int cCol = x / CELL_SIZE;//  System.out.println(cCol);
            int cRow = y / CELL_SIZE;//  System.out.println(cRow);
            boolean doRepaint = false;

            //timer only starts when the user first time click the board
            timer.start();
            if (!inGame) {
                prepareGame();
                repaint();
            }
            //if the mouse clicked inside of the board
            if ((x < NUM_OF_COLS * CELL_SIZE) && (y < NUM_OF_ROW * CELL_SIZE)) {  //如果鼠标event在活动范围内
                 if (e.getButton() == MouseEvent.BUTTON3) {  //如果这个鼠标是右键 flag
                    if (cellList[(cRow * NUM_OF_COLS) + cCol] > MINE_CELL) {
                        doRepaint = true;
                        if (cellList[(cRow * NUM_OF_COLS) + cCol] <= COVERED_MINE_CELL) {
                            if (minesLeft > 0) {
                                cellList[(cRow * NUM_OF_COLS) + cCol] += MARK_FOR_CELL;
                                minesLeft--;
                                mineBar.setText(""+minesLeft);
                            } else {
                                mineBar.setText("No marks left"); //when flag runs out
                            }
                        } else {
                            cellList[(cRow * NUM_OF_COLS) + cCol] -= MARK_FOR_CELL;
                            minesLeft++;
                            String msg = Integer.toString(minesLeft);
                            mineBar.setText(msg);
                        }
                    }
                } else {
                    if (cellList[(cRow * NUM_OF_COLS) + cCol] > COVERED_MINE_CELL) {
                        return;
                    }
                    if ((cellList[(cRow * NUM_OF_COLS) + cCol] > MINE_CELL)
                            && (cellList[(cRow * NUM_OF_COLS) + cCol] < MARKED_MINE_CELL)) {

                        cellList[(cRow * NUM_OF_COLS) + cCol] -= CELL_COVER;
                        doRepaint = true;
                        if (cellList[(cRow * NUM_OF_COLS) + cCol] == MINE_CELL) {
                            inGame = false;
                        }

                        if (cellList[(cRow * NUM_OF_COLS) + cCol] == EMPTY_CELL) {
                            empty_cells_isPressed((cRow * NUM_OF_COLS) + cCol);
                        }
                    }
                }
                if (doRepaint) {
                    repaint();
                }
            }
        }
    }
}