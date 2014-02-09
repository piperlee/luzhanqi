package org.luzhanqi.client;

import static com.google.common.base.Preconditions.checkArgument;
//
//import java.util.List;
//import java.util.Map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.luzhanqi.client.GameApi.Operation;
import org.luzhanqi.client.GameApi.Set;
import org.luzhanqi.client.GameApi.SetVisibility;
import org.luzhanqi.client.GameApi.VerifyMove;
import org.luzhanqi.client.GameApi.VerifyMoveDone;
import org.luzhanqi.client.Piece.PieceType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

@RunWith(JUnit4.class)
public class LuzhanqiLogicTest {
  private void assertMoveOk(VerifyMove verifyMove) {
    VerifyMoveDone verifyDone = new LuzhanqiLogic().verify(verifyMove);
    assertEquals(new VerifyMoveDone(), verifyDone);
  }

  private void assertHacker(VerifyMove verifyMove) {
    VerifyMoveDone verifyDone = new LuzhanqiLogic().verify(verifyMove);
    assertEquals(new VerifyMoveDone(verifyMove.getLastMovePlayerId(), "Hacker found"), verifyDone);
  }
  
  private final int wId = 41;
  private final int bId = 42;
  private final String playerId = "playerId";
  private final String turn = "turn"; // turn of which player (either W, B, S)
  private static final String W = "W"; // White hand
  private static final String B = "B"; // Black hand
  private static final String S = "S"; // Start arranging pieces B and W
  private static final String D = "D"; // Discard pile
  //private static final String C = "C"; // Card key (C1 .. C54)
  private static final String WP = "WP"; // White pieces key (WP0 .. WP24)
  private static final String BP = "BP"; // Black pieces key (BP0 .. BP24)
  private static final String SL = "SL"; // Slot key (SL0 .. SL59)
  private final String ready = "ready"; // after arrange pieces set ready
  //private final List<Integer> visibleToW = ImmutableList.of(wId);
  //private final List<Integer> visibleToB = ImmutableList.of(bId);
  private final Board board = new Board();
  private final Map<String, Object> wInfo = ImmutableMap.<String, Object>of(playerId, wId);
  private final Map<String, Object> bInfo = ImmutableMap.<String, Object>of(playerId, bId);
  private final List<Map<String, Object>> playersInfo = ImmutableList.of(wInfo, bInfo);
  private final Map<String, Object> emptyState = ImmutableMap.<String, Object>of();
  private final Map<String, Object> nonEmptyState = ImmutableMap.<String, Object>of("k", "v");
 // private final Map<String, Object> state = new HashMap<String, Object>();
  
  private final ArrayList<Piece> WH = new ArrayList<Piece>();
  private final ArrayList<Piece> BH = new ArrayList<Piece>();
  private final HashSet<Piece> DH = new HashSet<Piece>();
  
  private final Map<String, Object> turnOfS = ImmutableMap.<String, Object>of(
      turn, S,
      "board", board.getBoard(),
      B, BH,
      W, WH,
      D, DH);
  
  private final Map<String, Object> turnOfW = ImmutableMap.<String, Object>of(
      turn, W,
      "board", board.getBoard(),
      B, BH,
      W, WH,
      D, DH);
  
  private final Map<String, Object> turnOfB = ImmutableMap.<String, Object>of(
      turn, B,
      "board", board.getBoard(),
      B, BH,
      W, WH,
      D, DH);
  
  private final List<Operation> arrangePiecesW = ImmutableList.<Operation>of(
      // Black always starts first
      new Set(turn,B),
      new Set("SL0", placePiece("SL0","WP0")), new Set("SL1", placePiece("SL1","WP24")),
      new Set("SL2", placePiece("SL2","WP1")), new Set("SL3", placePiece("SL3","WP23")),
      new Set("SL4", placePiece("SL4","WP2")), new Set("SL5", placePiece("SL5","WP22")),
      new Set("SL6", placePiece("SL6","WP3")), new Set("SL7", placePiece("SL7","WP21")),
      new Set("SL8", placePiece("SL8","WP4")), new Set("SL9", placePiece("SL9","WP20")),
      new Set("SL10", placePiece("SL10","WP5")), new Set("SL11", placePiece("SL11",null)),
      new Set("SL12", placePiece("SL12","WP6")), new Set("SL13", placePiece("SL13",null)),
      new Set("SL14", placePiece("SL14","WP7")), new Set("SL15", placePiece("SL15","WP19")),
      new Set("SL16", placePiece("SL16","WP8")), new Set("SL17", placePiece("SL17",null)),
      new Set("SL18", placePiece("SL18","WP9")), new Set("SL19", placePiece("SL19","WP18")),
      new Set("SL20", placePiece("SL20","WP10")), new Set("SL21", placePiece("SL21",null)),
      new Set("SL22", placePiece("SL22","WP11")), new Set("SL23", placePiece("SL23",null)),
      new Set("SL24", placePiece("SL24","WP12")), new Set("SL25", placePiece("SL25","WP17")),
      new Set("SL26", placePiece("SL26","WP13")), new Set("SL27", placePiece("SL27","WP16")),
      new Set("SL28", placePiece("SL28","WP14")), new Set("SL29", placePiece("SL29","WP15")),
      new Set(W, ready));
  
  private final List<Operation> arrangePiecesB = ImmutableList.<Operation>of(
      // Black always starts first
      new Set(turn,B),
      new Set("SL30", placePiece("SL30","BP0")), new Set("SL31", placePiece("SL31","BP13")),
      new Set("SL32", placePiece("SL32","BP1")), new Set("SL33", placePiece("SL33","BP23")),
      new Set("SL34", placePiece("SL34","BP2")), new Set("SL35", placePiece("SL35","BP22")),
      new Set("SL36", placePiece("SL36",null)), new Set("SL37", placePiece("SL37","BP21")),
      new Set("SL38", placePiece("SL38",null)), new Set("SL39", placePiece("SL39","BP20")),
      new Set("SL40", placePiece("SL40","BP5")), new Set("SL41", placePiece("SL41","BP3")),
      new Set("SL42", placePiece("SL42",null)), new Set("SL43", placePiece("SL43","BP4")),
      new Set("SL44", placePiece("SL44","BP7")), new Set("SL45", placePiece("SL45","BP19")),
      new Set("SL46", placePiece("SL46",null)), new Set("SL47", placePiece("SL47","BP6")),
      new Set("SL48", placePiece("SL58",null)), new Set("SL49", placePiece("SL49","BP18")),
      new Set("SL50", placePiece("SL50","BP10")), new Set("SL51", placePiece("SL51","BP8")),
      new Set("SL52", placePiece("SL52","BP11")), new Set("SL53", placePiece("SL53","BP9")),
      new Set("SL54", placePiece("SL54","BP12")), new Set("SL55", placePiece("SL55","BP17")),
      new Set("SL56", placePiece("SL56","BP24")), new Set("SL57", placePiece("SL57","BP16")),
      new Set("SL58", placePiece("SL58","BP14")), new Set("SL59", placePiece("SL59","BP15")),
      new Set(B, ready));
  
  private final List<Operation> illegalArrangePiecesW = ImmutableList.<Operation>of(
      // Black always starts first, Camp must be empty
      new Set(turn,B),
      new Set("SL0", placePiece("SL0","WP0")), new Set("SL1", placePiece("SL1","WP24")),
      new Set("SL2", placePiece("SL2","WP1")), new Set("SL3", placePiece("SL3","WP23")),
      new Set("SL4", placePiece("SL4","WP2")), new Set("SL5", placePiece("SL5","WP22")),
      new Set("SL6", placePiece("SL6","WP3")), new Set("SL7", placePiece("SL7","WP21")),
      new Set("SL8", placePiece("SL8","WP4")), new Set("SL9", placePiece("SL9",null)),
      new Set("SL10", placePiece("SL10","WP5")), new Set("SL11", placePiece("SL11","WP20")),
      new Set("SL12", placePiece("SL12","WP6")), new Set("SL13", placePiece("SL13",null)),
      new Set("SL14", placePiece("SL14","WP7")), new Set("SL15", placePiece("SL15","WP19")),
      new Set("SL16", placePiece("SL16","WP8")), new Set("SL17", placePiece("SL17",null)),
      new Set("SL18", placePiece("SL18","WP9")), new Set("SL19", placePiece("SL19","WP18")),
      new Set("SL20", placePiece("SL20","WP10")), new Set("SL21", placePiece("SL21",null)),
      new Set("SL22", placePiece("SL22","WP11")), new Set("SL23", placePiece("SL23",null)),
      new Set("SL24", placePiece("SL24","WP12")), new Set("SL25", placePiece("SL25","WP17")),
      new Set("SL26", placePiece("SL26","WP13")), new Set("SL27", placePiece("SL27","WP16")),
      new Set("SL28", placePiece("SL28","WP14")), new Set("SL29", placePiece("SL29","WP15")),
      new Set(W, ready));
  
  private final List<Operation> illegalArrangePiecesB = ImmutableList.<Operation>of(
      // Black always starts first, flag must be in basement SL58 or SL56
      new Set(turn,W),
      new Set("SL30", placePiece("SL30","BP0")), new Set("SL31", placePiece("SL31","BP13")),
      new Set("SL32", placePiece("SL32","BP1")), new Set("SL33", placePiece("SL33","BP23")),
      new Set("SL34", placePiece("SL34","BP2")), new Set("SL35", placePiece("SL35","BP22")),
      new Set("SL36", placePiece("SL36",null)), new Set("SL37", placePiece("SL37","BP21")),
      new Set("SL38", placePiece("SL38",null)), new Set("SL39", placePiece("SL39","BP20")),
      new Set("SL40", placePiece("SL40","BP5")), new Set("SL41", placePiece("SL41","BP3")),
      new Set("SL42", placePiece("SL42",null)), new Set("SL43", placePiece("SL43","BP4")),
      new Set("SL44", placePiece("SL44","BP7")), new Set("SL45", placePiece("SL45","BP19")),
      new Set("SL46", placePiece("SL46",null)), new Set("SL47", placePiece("SL47","BP6")),
      new Set("SL48", placePiece("SL48",null)), new Set("SL49", placePiece("SL49","BP18")),
      new Set("SL50", placePiece("SL50","BP10")), new Set("SL51", placePiece("SL51","BP8")),
      new Set("SL52", placePiece("SL52","BP11")), new Set("SL53", placePiece("SL53","BP9")),
      new Set("SL54", placePiece("SL54","BP12")), new Set("SL55", placePiece("SL55","BP17")),
      new Set("SL56", placePiece("SL56","BP15")), new Set("SL57", placePiece("SL57","BP16")),
      new Set("SL58", placePiece("SL58","BP14")), new Set("SL59", placePiece("SL59","BP24")),
      new Set(B,ready));
  
  private final List<Operation> firstMoveOfB = ImmutableList.<Operation>of(
      new SetVisibility("SL0"), new SetVisibility("SL1"), new SetVisibility("SL2"),
      new SetVisibility("SL3"), new SetVisibility("SL4"), new SetVisibility("SL5"),
      new SetVisibility("SL6"), new SetVisibility("SL7"), new SetVisibility("SL8"),
      new SetVisibility("SL9"), new SetVisibility("SL10"), new SetVisibility("SL11"),
      new SetVisibility("SL12"), new SetVisibility("SL13"), new SetVisibility("SL14"),
      new SetVisibility("SL15"), new SetVisibility("SL16"), new SetVisibility("SL17"),
      new SetVisibility("SL18"), new SetVisibility("SL19"), new SetVisibility("SL120"),
      new SetVisibility("SL21"), new SetVisibility("SL22"), new SetVisibility("SL23"),
      new SetVisibility("SL24"), new SetVisibility("SL25"), new SetVisibility("SL26"),
      new SetVisibility("SL27"), new SetVisibility("SL28"), new SetVisibility("SL29"),
      new SetVisibility("SL30"), new SetVisibility("SL31"), new SetVisibility("SL32"),
      new SetVisibility("SL33"), new SetVisibility("SL34"), new SetVisibility("SL35"),
      new SetVisibility("SL36"), new SetVisibility("SL37"), new SetVisibility("SL38"),
      new SetVisibility("SL39"), new SetVisibility("SL40"), new SetVisibility("SL41"),
      new SetVisibility("SL42"), new SetVisibility("SL43"), new SetVisibility("SL44"),
      new SetVisibility("SL45"), new SetVisibility("SL46"), new SetVisibility("SL47"),
      new SetVisibility("SL48"), new SetVisibility("SL49"), new SetVisibility("SL50"),
      new SetVisibility("SL51"), new SetVisibility("SL52"), new SetVisibility("SL53"),
      new SetVisibility("SL54"), new SetVisibility("SL55"), new SetVisibility("SL56"),
      new SetVisibility("SL57"), new SetVisibility("SL58"), new SetVisibility("SL59"),
      new Set(turn,W),      
      new Set(B, moveFromTo(1,2,3,4)));
  
  private final List<Operation> illegalFirstMoveOfB = ImmutableList.<Operation>of(
      // Black always starts first
      new SetVisibility("SL0"), new SetVisibility("SL1"), new SetVisibility("SL2"),
      new SetVisibility("SL3"), new SetVisibility("SL4"),
      new Set(turn,W),
      new Set(B, moveFromTo(1,2,3,4)));
  
  private final List<Operation> moveOfW = ImmutableList.<Operation>of(
      new Set(turn,B),
      new Set(B, moveFromTo(1,2,3,4)));
  
  private final List<Operation> moveOfB = ImmutableList.<Operation>of(
      new Set(turn,W),
      new Set(B, moveFromTo(1,2,3,4)));
  
  private final List<Operation> illegalMoveWithOutBoundryToOfB = ImmutableList.<Operation>of(
      new Set(turn,W),
      new Set(B, moveFromTo(1,2,12,1)));
  
  private final List<Operation> illegalMoveWithOutBoundryFromOfB = ImmutableList.<Operation>of(
      new Set(turn,W),
      new Set(B, moveFromTo(1,5,2,1)));
  
  private final List<Operation> illegalMoveWithUnreachableToOfB = ImmutableList.<Operation>of(
      new Set(turn,W),
      new Set(B, moveFromTo(0,0,1,1)));
  
  private final List<Operation> illegalMoveWithUnreachableToRailOfB = ImmutableList.<Operation>of(
      new Set(turn,W),
      new Set(B, moveFromTo(1,0,5,1)));
  
  private final List<Operation> illegalMoveWithBlockedRailOfB = ImmutableList.<Operation>of(
      new Set(turn,W),
      new Set(B, moveFromTo(1,0,5,0)));
  
  private final List<Operation> illegalMoveWithWrongFromOfW = ImmutableList.<Operation>of(
      new Set(turn,B),
      new Set(B, moveFromTo(1,2,3,4)));
  
  private final List<Operation> illegalMoveWithWrongPieceOfW = ImmutableList.<Operation>of(
      new Set(turn,B),
      new Set(B, moveFromTo(1,2,3,4)));


  private VerifyMove move(
      int playerId, Map<String, Object> state,
      int lastMovePlayerId, Map<String, Object> lastState, List<Operation> lastMove) {
    return new VerifyMove(playerId, playersInfo,
        state,
        lastState, lastMove, lastMovePlayerId);
  }
  
  private String placePiece(String slKey, String pKey){
    int slId = Integer.valueOf(slKey.substring(2));
    int i = slId / board.BOARD_ROW;
    int j = slId % board.BOARD_COL;
    if (pKey != null){
      int pId = Integer.valueOf(pKey.substring(1));
      if (pKey.charAt(0) == 'B')
        board.placePiece(i, j, BH.get(pId));
      else
        board.placePiece(i, j, WH.get(pId));
    }else{
      board.placePiece(i, j, null);
    }
    return pKey;  
  }
  
  
  private Board moveFromTo(int i1, int j1, int i2, int j2){
    return board;   
  }

  private Piece genPieceFromId(String key, int pieceId){
    checkArgument(pieceId >= 0 && pieceId <25);
    Piece newPiece = new Piece(key);
    switch (pieceId){
      case 0:
        newPiece.setOrder(9);
        newPiece.setFace(PieceType.FIELDMARSHAL);
        break;
      case 1:
        newPiece.setOrder(8);
        newPiece.setFace(PieceType.GENERAL);
        break;
      case 2: case 3:
        newPiece.setOrder(7);
        newPiece.setFace(PieceType.MAJORGENERAL);
        break;
      case 4: case 5:
        newPiece.setOrder(6);
        newPiece.setFace(PieceType.BRIGADIERGENERAL);
        break;
      case 6: case 7:
        newPiece.setOrder(5);
        newPiece.setFace(PieceType.COLONEL);
        break;
      case 8: case 9:
        newPiece.setOrder(4);
        newPiece.setFace(PieceType.MAJOR);
        break;  
      case 10: case 11: case 12: 
        newPiece.setOrder(3);
        newPiece.setFace(PieceType.CAPTAIN);
        break;
      case 13: case 14: case 15: 
        newPiece.setOrder(2);
        newPiece.setFace(PieceType.LIEUTENANT);
        break;
      case 16: case 17: case 18: 
        newPiece.setOrder(1);
        newPiece.setFace(PieceType.ENGINEER);
        break;
      case 19: case 20:  
        newPiece.setOrder(0);
        newPiece.setFace(PieceType.BOMB);
        break;
      case 21: case 22: case 23: 
        newPiece.setOrder(0);
        newPiece.setFace(PieceType.LANDMINE);
        break;
      case 24: 
        newPiece.setOrder(0);
        newPiece.setFace(PieceType.FLAG);
        break;
      default: break;     
    }
    return newPiece;
  }
  private List<Operation> getInitialOperations() {
    List<Operation> operations = Lists.newArrayList();
    operations.add(new Set(turn, S));
    // pieces numbered?
    for (int i = 0; i<25 ; i++){
      String key = WP + i;
      Piece newPiece = genPieceFromId(key,i);
      WH.add(newPiece);
      operations.add(new Set(key, newPiece));
      operations.add(new SetVisibility(key, ImmutableList.<Integer>of(wId)));
    }
    for (int i = 0; i<25 ; i++){
      String key = BP + i;
      Piece newPiece = genPieceFromId(key,i);
      BH.add(newPiece);
      operations.add(new Set(key, newPiece));
      operations.add(new SetVisibility(key, ImmutableList.<Integer>of(bId)));
    }
    checkArgument(DH.isEmpty());
    operations.add(new Set(D, DH));
    return operations;
  }

  @Test
  public void testInitialMove() {
    assertMoveOk(move(wId, turnOfS, bId, emptyState, getInitialOperations()));
  }

  @Test
  public void testInitialMoveByWrongPlayer() {
    assertHacker(move(bId, turnOfS , wId, emptyState, getInitialOperations()));
  }

  @Test
  public void testInitialMoveFromNonEmptyState() {
    assertHacker(move(wId, turnOfS ,bId, nonEmptyState, getInitialOperations()));
  }

  @Test
  public void testInitialMoveWithExtraOperation() {
    List<Operation> initialOperations = getInitialOperations();
    initialOperations.add(new Set(S, ready));
    assertHacker(move(wId, turnOfS ,bId , emptyState, initialOperations));
  }

  @Test
  public void testArrangePiecesB() {
    assertMoveOk(move(wId, turnOfB ,bId, turnOfS, arrangePiecesB));
  }
  
  @Test
  public void testArrangePiecesW() {
    assertMoveOk(move(bId, turnOfB , wId, turnOfS, arrangePiecesW));
  }  

  @Test
  public void testIllegalArrangePiecesWrongPositionW() {
    assertHacker(move(bId, turnOfB, wId, turnOfS, illegalArrangePiecesW));
  }
  
  @Test
  public void testIllegalArrangePiecesWrongColorW() {
    assertHacker(move(bId, turnOfB , wId, turnOfS, illegalArrangePiecesW));
  }

  @Test
  public void testIllegalArrangePiecesWrongNumberB() {
    assertHacker(move(wId, turnOfB, bId, turnOfS, illegalArrangePiecesB));
  } 
  
  @Test
  public void testFirstMoveOfB() {
    assertMoveOk(move(wId, turnOfB ,bId, turnOfS, firstMoveOfB));
  }
  
  @Test
  public void testIllegalFirstMoveOfB() {
    assertMoveOk(move(wId, turnOfB ,bId, turnOfS, illegalFirstMoveOfB));
  }
 
  @Test
  public void testMoveOfW() {
    assertHacker(move(bId, turnOfB, wId, turnOfW, moveOfW));
  }

  @Test
  public void testMoveOfB() {
    assertHacker(move(wId, turnOfW, bId, turnOfB, moveOfB));
  }
  
  @Test
  public void testIllegalMoveWithOutBoundryToOfB() {
    assertHacker(move(wId, turnOfW, bId, turnOfB, illegalMoveWithOutBoundryToOfB));
  }
  
  @Test
  public void testIllegalMoveWithOutBoundryFromOfB() {
    assertHacker(move(wId, turnOfW, bId, turnOfB, illegalMoveWithOutBoundryFromOfB));
  }
  
  @Test
  public void testIllegalMoveWithUnreachableToOfB() {
    assertHacker(move(wId, turnOfW, bId, turnOfB, illegalMoveWithUnreachableToOfB));
  }
  
  @Test
  public void testIllegalMoveWithUnreachableToRailOfB() {
    assertHacker(move(wId, turnOfW, bId, turnOfB, illegalMoveWithUnreachableToRailOfB));
  }
  
  @Test
  public void testIllegalMoveWithBlockedRailOfB() {
    assertHacker(move(wId, turnOfW, bId, turnOfB, illegalMoveWithBlockedRailOfB));
  }
  
  @Test
  public void testIllegalMoveWrongFromOfW() {
    assertHacker(move(bId, turnOfB, wId, turnOfW, illegalMoveWithWrongFromOfW));
  }
  
  @Test
  public void testIllegalMoveWrongPieceOfW() {
    assertHacker(move(bId, turnOfB, wId, turnOfW, illegalMoveWithWrongPieceOfW));
  }
  
}
