package org.luzhanqi.client;

import java.util.ArrayList;

enum SlotType{
  POST, CAMPSITE, MOUNTAIN, FRONTLINE, HEADQUARTER 
}

class Slot{
  private String key;
  private SlotType type;
  private boolean isEmpty;
  private boolean onRail;
  private ArrayList<Slot> adjSlots;
  private Piece curPiece;
  
  public void setPiece(Piece p){
    this.curPiece = p; 
  }
}

public class Board {
  public static int BOARD_ROW = 12;
  public static int BOARD_COL = 5;
  
  private Slot [][] board;
  
  public Board(){
    initiateBoard();  
  }
  public boolean placePiece(int i, int j, Piece p){
    board[i][j].setPiece(p);
    return true;    
  }
  
  public Slot[][] getBoard(){
    return this.board;
  }
  
  private void initiateBoard(){
    this.board = new Slot[BOARD_ROW][BOARD_COL];
    // initiate board with slots
    // with no pieces
  }
}
