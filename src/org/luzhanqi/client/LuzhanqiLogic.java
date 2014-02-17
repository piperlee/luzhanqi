package org.luzhanqi.client;

import static com.google.common.base.Preconditions.checkArgument;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.luzhanqi.client.GameApi.Delete;
import org.luzhanqi.client.GameApi.EndGame;
import org.luzhanqi.client.GameApi.Operation;
import org.luzhanqi.client.GameApi.Set;
import org.luzhanqi.client.GameApi.SetTurn;
import org.luzhanqi.client.GameApi.SetVisibility;
import org.luzhanqi.client.GameApi.VerifyMove;
import org.luzhanqi.client.GameApi.VerifyMoveDone;
import org.luzhanqi.client.Piece.PieceType;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class LuzhanqiLogic {
  /* The entries used in the luzhanqi game are:
   *   isCheater:yes, W, B, M, claim, C0...C51
   *   turn, BOARD, W, B, D
   * When we send operations on these keys, it will always be in the above order.
   */
  private static final String S = "S"; // turn S
  private static final String W = "W"; // White hand
  private static final String B = "B"; // Black hand
  private static final String D = "D"; // Middle pile
  private static final String TURN = "TURN";
  private static final String BOARD = "BOARD";
 // private static final String C = "C"; // Card key (C0...C51)
 // private static final String FIRST = "first"; // first move after deploy pieces
  private static final String DEPLOY = "deploy"; // moves at which players deploy pieces
  private static final String MOVE = "move";
  // private static final String YES = "yes"; // we claim we have a cheater

  public VerifyMoveDone verify(VerifyMove verifyMove) {
    try {
      checkMoveIsLegal(verifyMove);
      return new VerifyMoveDone();
    } catch (Exception e) {
      return new VerifyMoveDone(verifyMove.getLastMovePlayerId(), e.getMessage());
    }
  }

  void checkMoveIsLegal(VerifyMove verifyMove) {
    List<Operation> lastMove = verifyMove.getLastMove();
    Map<String, Object> lastState = verifyMove.getLastState();
    // Checking the operations are as expected.
    List<Operation> expectedOperations = getExpectedOperations(
        lastState, lastMove, verifyMove.getPlayerIds(), verifyMove.getLastMovePlayerId());
    check(expectedOperations.equals(lastMove), expectedOperations, lastMove);
    // We use SetTurn, so we don't need to check that the correct player did the move.
    // However, we do need to check the first move is done by the white player (and then in the
    // first MakeMove we'll send SetTurn which will guarantee the correct player send MakeMove).
    if (lastState.isEmpty()) {
      check(verifyMove.getLastMovePlayerId() == verifyMove.getPlayerIds().get(0));
    }
  }

  /** Returns the operations for deploy pieces. */
  List<Operation> deployPiecesMove(LuzhanqiState state, List<Integer> board,
    List<Integer> playerIds, int lastMovePlayerId) {
    //Turn turnOfColor = state.getTurn();
    // deploy pieces
    // 0) new SetTurn(B)
    // 1) new Set(DEPLOY)
    // 2) new Set BOARD
    // 2) new Set W 
    // 3) new Set B 
    // 4) new Set D
    List<Operation> operations = Lists.newArrayList();
    // B first
    operations.add(new SetTurn(state.getPlayerId(Turn.B)));
    operations.add(new Set(DEPLOY, DEPLOY));
    //BLACK deploy
    if (playerIds.indexOf(lastMovePlayerId) == Turn.B.ordinal()){
      //empty other half
      for(int i = 1; i < 25; i++){
         check(board.get(i) == -1);
      }
      //empty CAMPSITE
      check(board.get(36) == -1);
      check(board.get(38) == -1);
      check(board.get(42) == -1);
      check(board.get(46) == -1);
      check(board.get(48) == -1);
      //flag position
      check(board.get(56) == 49 || board.get(58) == 49);
      //landmine and bomb position
      for (int i = 30; i < 49 ; i++){
        if (i >= 30 && i<= 34){
          check(board.get(i)!=44 && board.get(i)!=45);
        }
        check(board.get(i)!=46 && board.get(i)!=47 && board.get(i)!=48);
      }
      
    }//WHITE deploy
    else{
      //empty other half
      for(int i = 30; i < 59; i++){
        check(board.get(i) == -1);
      }
      //empty CAMPSITE
      check(board.get(11) == -1);
      check(board.get(13) == -1);
      check(board.get(17) == -1);
      check(board.get(21) == -1);
      check(board.get(23) == -1);
      //flag position
      check(board.get(3)==24 || board.get(1)==24);
    //landmine and bomb position
      for (int i = 10; i < 29 ; i++){
        if (i>=25 && i<=29){
          check(board.get(i)!=19 && board.get(i)!=20);
        }
        check(board.get(i)!=21 && board.get(i)!=22 && board.get(i)!=23);
      }
    }
    operations.add(new Set(BOARD, board));
    operations.add(new Set(W, state.getWhite()));
    operations.add(new Set(B, state.getBlack()));
    operations.add(new Set(D, state.getDiscard()));
    return operations;
  }

  /** Returns the operations for B first move. */
  List<Operation> firstMove(LuzhanqiState state, List<Integer> board) {
    // first move
    // 0) new SetTurn(B)
    // 1) new Delete(DEPLOY)
    // 2) new Set BOARD
    // 2) new Set W 
    // 3) new Set B 
    // 4) new Set D
    // 5-) new SetVisibility(all)
    List<Operation> operations = Lists.newArrayList();
    operations.add(new SetTurn(state.getPlayerId(Turn.B)));
    operations.add(new Delete(DEPLOY));
    operations.add(new Set(BOARD,board));
    operations.add(new Set(W,state.getWhite()));
    operations.add(new Set(B,state.getBlack()));
    operations.add(new Set(D,state.getBlack()));
    for(int i = 0; i < 49; i++){
      operations.add(new SetVisibility(String.valueOf(i)));
    }
    return operations;
  }

  /** Returns the operations for making a claim (e.g., I put down 3 cards of rank K). */
  List<Operation> normalMove(LuzhanqiState state, List<Integer> board, 
      List<Integer> pieceMove, List<Integer> playerIds) {
    // first move
    // 0) new SetTurn(oppo)
    // 1) new Set MOVE
    // 2) new Set BOARD
    // 2) new Set W 
    // 3) new Set B 
    // 4) new Set D
    List<Operation> operations = Lists.newArrayList();
    Turn turn = state.getTurn();
    operations.add(new SetTurn(state.getPlayerId(turn.getOppositeColor())));
    
    List<Integer> wHand = Lists.newArrayList(state.getWhite());
    List<Integer> bHand = Lists.newArrayList(state.getBlack());
    List<Integer> dHand = Lists.newArrayList(state.getDiscard());
    // EndGame: no piece to move
    // TODOTODOTODO
    
    //check move
    check(pieceMove.get(0)>=0 && pieceMove.get(1)<60, "out of board");
    check(pieceMove.get(0)!=pieceMove.get(1), "same slot move");
    check(pieceMove.get(0)!=-1, "empty slot move");
    if(turn == Turn.B){
      //only could move own pieces
      check(pieceMove.get(0)>=25 && pieceMove.get(0)<=49, "move other's piece");
      //flag and landmine cannot be moved
      check(pieceMove.get(0)!=49, "flag move");
      check(pieceMove.get(0)!=46 && pieceMove.get(0)!=47 && pieceMove.get(0)!=48, "landmine move");
      check(pieceMove.get(1)<25, "move onto one's own piece");
    }else if (turn == Turn.W){
    //only could move own pieces
      check(pieceMove.get(0)>=0 && pieceMove.get(0)<=24, "move other's piece");
      //flag and landmine cannot be moved
      check(pieceMove.get(0)!=24, "flag move");
      check(pieceMove.get(0)!=21 && pieceMove.get(0)!=22 && pieceMove.get(0)!=23, "landmine move");
      check(pieceMove.get(1)>24 || pieceMove.get(1)==-1, "move onto one's own piece");
    }
    operations.add(new Set(MOVE,pieceMove));
    List<Integer> apiBoard = Lists.newArrayList(board);
    Slot slotFrom = state.getBoard().get(pieceMove.get(0)).get();
    Slot slotTo = state.getBoard().get(pieceMove.get(1)).get();
    // move not beat
    if (pieceMove.get(1) == -1){
      check(positionValid(state,slotFrom,slotTo),"position invalid");
      apiBoard.set(slotFrom.getKey(), -1);
      apiBoard.set(slotTo.getKey(), slotFrom.getPiece().getKey());
      operations.add(new Set(BOARD,apiBoard));
      operations.add(new Set(W, state.getWhite()));
      operations.add(new Set(B, state.getBlack()));
      operations.add(new Set(D, state.getDiscard()));
    }
    // move and beat
    else{
      check(positionValid(state,slotFrom,slotTo),"position invalid");
     // check(beatValid(state,slotFrom,slotTo),"position invalid");
            
      //end game: flag is beat
      if (slotTo.getPiece().getFace()==PieceType.FLAG){        
        operations.add(new Set(MOVE,pieceMove));
        apiBoard.set(slotFrom.getKey(), -1);
        apiBoard.set(slotTo.getKey(), slotFrom.getPiece().getKey());
        operations.add(new Set(BOARD,apiBoard));
        if(turn == Turn.W){
          bHand.remove(slotTo.getPiece().getKey());
          dHand.add(slotTo.getPiece().getKey());
        }else{
          wHand.remove(slotTo.getPiece().getKey());
          dHand.add(slotTo.getPiece().getKey());
        }
        operations.add(new Set(W, wHand));
        operations.add(new Set(B, bHand));
        operations.add(new Set(D, dHand));
        operations.add(new EndGame(state.getPlayerId(turn)));
      }      
      else if (slotFrom.getPiece().getFace()==PieceType.BOMB 
          || slotTo.getPiece().getFace()==PieceType.BOMB){  
        operations.add(new Set(MOVE,pieceMove));
        apiBoard.set(slotFrom.getKey(), -1);
        apiBoard.set(slotTo.getKey(), -1);
        operations.add(new Set(BOARD,apiBoard));
        if(turn == Turn.W){
          wHand.remove(slotFrom.getPiece().getKey());
          bHand.remove(slotTo.getPiece().getKey());          
        }else{
          bHand.remove(slotFrom.getPiece().getKey());
          wHand.remove(slotTo.getPiece().getKey());
        }
        dHand.add(slotFrom.getPiece().getKey());
        dHand.add(slotTo.getPiece().getKey());
        operations.add(new Set(W, wHand));
        operations.add(new Set(B, bHand));
        operations.add(new Set(D, dHand));        
      }
      else if (slotTo.getPiece().getFace()==PieceType.LANDMINE
          && slotFrom.getPiece().getFace()==PieceType.ENGINEER){
        operations.add(new Set(MOVE,pieceMove));
        apiBoard.set(slotFrom.getKey(), -1);
        apiBoard.set(slotTo.getKey(), slotFrom.getPiece().getKey());
        operations.add(new Set(BOARD,apiBoard));
        if(turn == Turn.W){
          bHand.remove(slotTo.getPiece().getKey());
        }else{
          wHand.add(slotTo.getPiece().getKey());
        }
        dHand.add(slotTo.getPiece().getKey());
        operations.add(new Set(W, wHand));
        operations.add(new Set(B, bHand));
        operations.add(new Set(D, dHand));
      }
      else if (slotTo.getPiece().getFace()==PieceType.LANDMINE){
        operations.add(new Set(MOVE,pieceMove));
        apiBoard.set(slotFrom.getKey(), -1);
        apiBoard.set(slotTo.getKey(), -1);
        operations.add(new Set(BOARD,apiBoard));
        if(turn == Turn.W){
          wHand.remove(slotFrom.getPiece().getKey());
          bHand.remove(slotTo.getPiece().getKey());          
        }else{
          bHand.remove(slotFrom.getPiece().getKey());
          wHand.remove(slotTo.getPiece().getKey());
        }
        dHand.add(slotFrom.getPiece().getKey());
        dHand.add(slotTo.getPiece().getKey());
        operations.add(new Set(W, wHand));
        operations.add(new Set(B, bHand));
        operations.add(new Set(D, dHand));
      }
      else{
        operations.add(new Set(MOVE,pieceMove));
        int survived = (slotFrom.getPiece().getOrder()>slotTo.getPiece().getOrder())
            ?slotFrom.getPiece().getKey():slotTo.getPiece().getKey();
        int dead = (slotFrom.getPiece().getOrder()<slotTo.getPiece().getOrder())
            ?slotFrom.getPiece().getKey():slotTo.getPiece().getKey();
        apiBoard.set(slotFrom.getKey(), -1);
        apiBoard.set(slotTo.getKey(), 
            (slotFrom.getPiece().getOrder()==slotTo.getPiece().getOrder())?-1:survived);
        operations.add(new Set(BOARD,apiBoard));
        if(slotFrom.getPiece().getOrder()==slotTo.getPiece().getOrder()){
          if(turn == Turn.W){
            wHand.remove(slotFrom.getPiece().getKey());
            bHand.remove(slotTo.getPiece().getKey());          
          }else{
            bHand.remove(slotFrom.getPiece().getKey());
            wHand.remove(slotTo.getPiece().getKey());
          }
          dHand.add(slotFrom.getPiece().getKey());
          dHand.add(slotTo.getPiece().getKey());
        }else{
          if(dead>=25){
            bHand.remove(dead);        
          }else{
            wHand.remove(dead);
          }
          dHand.add(dead);
        }
        operations.add(new Set(W, wHand));
        operations.add(new Set(B, bHand));
        operations.add(new Set(D, dHand));
      }   
    }    
    return operations;
  }
  
  boolean positionValid(LuzhanqiState state, Slot slotFrom, Slot slotTo){
    if (!slotFrom.isAdj(slotTo.getKey())){
      //slotTo has to be onRail
      check(slotTo.getOnRail(),"slotTo off rail");
      if (slotFrom.getPiece().getFace() == PieceType.ENGINEER){
        check(engineerOnRail(state,slotFrom,slotTo),"no valid engineer rail path");
      }else{
        int iFrom = slotFrom.getKey()/5;
        int jFrom = slotFrom.getKey()%5;
        int iTo = slotTo.getKey()/5;
        int jTo = slotTo.getKey()%5;
        //make no turns
        check(iFrom==iTo || jFrom==jTo, "turn exists");
        //no block
        if(iFrom == iTo){
          for(int j=Math.min(jFrom,jTo); j<=Math.max(jFrom,jTo); j++){
            check(state.getBoard().get(iFrom*5+j).get().getPiece()==null,"block exists");
          }
        }else if(jFrom == jTo){
          for(int i=Math.min(iFrom,iTo); i<=Math.max(iFrom,iTo); i++){
            check(state.getBoard().get(i*5+jFrom).get().getPiece()==null,"block exists");
          }
        }
      }
    }
    return true;
  }
  
  boolean engineerOnRail(LuzhanqiState state, Slot from, Slot to){
    LinkedList<Slot> Q = new LinkedList<Slot>();
    from.setVisited(true);
    Q.add(from);
    while (!Q.isEmpty()){
      Slot cur = Q.pollFirst();
      for (int i:cur.getAdjSlots()){
        if(state.getBoard().get(i).get().getKey()==to.getKey()){
          return true;
        }
        if (state.getBoard().get(i).get().getOnRail()){
          if ((!state.getBoard().get(i).get().getVisited()) 
              && state.getBoard().get(i).get().emptySlot()){
            Q.add(state.getBoard().get(i).get());
            state.getBoard().get(i).get().setVisited(true);
          }
        }
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  List<Operation> getExpectedOperations(
      Map<String, Object> lastApiState, List<Operation> lastMove, List<Integer> playerIds,
      int lastMovePlayerId) {
    if (lastApiState.isEmpty()) {
      return getInitialMove(playerIds.get(0), playerIds.get(1));
    }
    // remember to deal with W:0, B:1, S:2
    LuzhanqiState lastState = gameApiStateToLuzhanqiState(lastApiState,
        Turn.values()[playerIds.indexOf(lastMovePlayerId)], playerIds);
    // There are 2 types of moves:
    // 1) deploy pieces
    // 2) first move B
    // 3) normal move
    if (lastMove.contains(new Set(DEPLOY,DEPLOY))) {
      Set setBoard = (Set)lastMove.get(2);
      return deployPiecesMove(lastState, (List<Integer>)setBoard.getValue(), playerIds, lastMovePlayerId);

    }else if (lastMove.contains(new Delete(DEPLOY))) {
      return firstMove(lastState,(List<Integer>)lastApiState.get(BOARD));
    }else {
      Set setBoard = (Set)lastMove.get(2);
      Set setMove = (Set)lastMove.get(1);
      return normalMove(lastState, (List<Integer>)setBoard.getValue(), 
          (List<Integer>)setMove.getValue(), playerIds);
    }
  }
  
  <T> List<T> concat(List<T> a, List<T> b) {
    return Lists.newArrayList(Iterables.concat(a, b));
  }

  <T> List<T> subtract(List<T> removeFrom, List<T> elementsToRemove) {
    check(removeFrom.containsAll(elementsToRemove), removeFrom, elementsToRemove);
    List<T> result = Lists.newArrayList(removeFrom);
    result.removeAll(elementsToRemove);
    check(removeFrom.size() == result.size() + elementsToRemove.size());
    return result;
  }

  List<Operation> getInitialMove(int whitePlayerId, int blackPlayerId) {
    List<Operation> operations = Lists.newArrayList();
    // The order of operations: turn, board, W, B, D
    // turn W,B,S
    operations.add(new SetTurn(2));
    // set board
    operations.add(new Set(BOARD,ImmutableList.of(
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1,
          -1,-1,-1,-1,-1)));
    // set W and B hands
    operations.add(new Set(W, ImmutableList.of(
        1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24)));
    operations.add(new Set(B, ImmutableList.of(
        25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49)));
    // discard pile is empty
    operations.add(new Set(D, ImmutableList.of()));
    // sets visibility
    for (int i = 0; i < 25; i++) {
      operations.add(new SetVisibility(String.valueOf(i), ImmutableList.of(whitePlayerId)));
    }
    for (int i = 25; i < 50; i++) {
      operations.add(new SetVisibility(String.valueOf(i), ImmutableList.of(blackPlayerId)));
    }
    return operations;
  }

  @SuppressWarnings("unchecked")
  private LuzhanqiState gameApiStateToLuzhanqiState(Map<String, Object> gameApiState,
      Turn turn, List<Integer> playerIds) {
    List<Optional<Slot>> board = Lists.newArrayList();
    List<Integer> apiBoard = (List<Integer>)gameApiState.get(BOARD);
    for (int i = 0; i < 60; i++) {
      Slot slot = new Slot(i,apiBoard.get(i));
      board.add(Optional.fromNullable(slot));
    }
    List<Integer> white = (List<Integer>) gameApiState.get(W);
    List<Integer> black = (List<Integer>) gameApiState.get(B);
    List<Integer> discard = (List<Integer>) gameApiState.get(D);
    return new LuzhanqiState(
        turn,
        ImmutableList.copyOf(playerIds),
        ImmutableList.copyOf(board),
        ImmutableList.copyOf(white), ImmutableList.copyOf(black),
        ImmutableList.copyOf(discard));
  }

  private void check(boolean val, Object... debugArguments) {
    if (!val) {
      throw new RuntimeException("We have a hacker! debugArguments="
          + Arrays.toString(debugArguments));
    }
  }
}

