package org.luzhanqi.client;

//0, one Field Marshal, order 9
//1, one General or Army Commander, order 8
//2-3, two Major Generals or Division Commanders, order 7
//4-5, two Brigadier Generals or Brigade Commanders, order 6
//6-7, two Colonels or Regiment Commanders, order 5
//8-9, two Majors or Battalion Commanders, order 4
//10-12, three Captains or Company Commanders, order 3
//13-15, three Lieutenants or Platoon Commanders, order 2
//16-18, three Engineers or Sappers, order 1
//19-20, two Bombs 0
//21-23, three Landmines 0
//24, one Flag 0

enum PieceType{
  FIELDMARSHAL, GENERAL, MAJORGENERAL, BRIGADIERGENERAL,
  COLONEL, MAJOR, CAPTAIN, LIEUTENANT, ENGINEER, BOMB,
  LANDMINE, FLAG  
}

public class Piece {
  private String key;
  private int order;
  private PieceType face;
  
  public Piece(String k){
    this.key = k;
  }
  
  public String getKey(){
    return this.key;
  }
  
  public int getOrder(){
    return this.order;
  }
  
  public PieceType getFace(){
    return this.face;
  }
  
  public void setKey(String k){
    this.key = k;
  }
  
  public void setOrder(int o){
    this.order = o;
  }
  
  public void setFace(PieceType f){
    this.face = f;
  }

}
