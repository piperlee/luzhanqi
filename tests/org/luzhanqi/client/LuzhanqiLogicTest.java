package org.luzhanqi.client;

import static com.google.common.base.Preconditions.checkArgument;
//
//import java.util.List;
//import java.util.Map;

import java.util.List;
import java.util.Map;

import org.luzhanqi.client.GameApi.Delete;
import org.luzhanqi.client.GameApi.Operation;
import org.luzhanqi.client.GameApi.Set;
import org.luzhanqi.client.GameApi.SetVisibility;
import org.luzhanqi.client.GameApi.Shuffle;
import org.luzhanqi.client.GameApi.VerifyMove;
import org.luzhanqi.client.GameApi.VerifyMoveDone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
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
  //private static final String C = "C"; // Card key (C1 .. C54)
  private static final String WP = "WP"; // White pieces key (WP1 .. WP25)
  private static final String BP = "BP"; // Black pieces key (BP1 .. BP25)
  //private final String claim = "claim"; // a claim has the form: [3cards, rankK]
  private final String isCheater = "isCheater"; // we claim we have a cheater
  private final String yes = "yes"; // we claim we have a cheater
  private final String ready = "ready"; // after arrange pieces set ready
  //private final List<Integer> visibleToW = ImmutableList.of(wId);
  //private final List<Integer> visibleToB = ImmutableList.of(bId);
  private final Board board = new Board();
  private final Map<String, Object> wInfo = ImmutableMap.<String, Object>of(playerId, wId);
  private final Map<String, Object> bInfo = ImmutableMap.<String, Object>of(playerId, bId);
  private final List<Map<String, Object>> playersInfo = ImmutableList.of(wInfo, bInfo);
  private final Map<String, Object> emptyState = ImmutableMap.<String, Object>of();
  private final Map<String, Object> nonEmptyState = ImmutableMap.<String, Object>of("k", "v");
  
  private final Map<String, Object> turnOfS = ImmutableMap.<String, Object>of(
      turn, S,
      S, board.getBoard());
  
  private final Map<String, Object> turnOfW = ImmutableMap.<String, Object>of(
      turn, W,
      W, board.getBoard());
  
  private final Map<String, Object> turnOfB = ImmutableMap.<String, Object>of(
      turn, B,
      B, board.getBoard());
  
  private final List<Operation> arrangePiecesW = ImmutableList.<Operation>of(
      // Black always starts first
      new Set(turn,B),
      new Set(W, placePieces()),
      new Set(W, ready));
  
  private final List<Operation> arrangePiecesB = ImmutableList.<Operation>of(
      // Black always starts first
      new Set(turn,B),
      new Set(B, placePieces()),
      new Set(B, ready));
  
  private final List<Operation> illegalArrangePiecesW = ImmutableList.<Operation>of(
      // Black always starts first
      new Set(turn,B),
      new Set(W, placePieces()));
  
  private final List<Operation> illegalArrangePiecesB = ImmutableList.<Operation>of(
      // Black always starts first
      new Set(turn,W),
      new Set(W, placePieces()));
  
  private final List<Operation> moveOfW = ImmutableList.<Operation>of(
      new Set(turn,B),
      new Set(B, moveFromTo(1,2,3,4)));
  
  private final List<Operation> moveOfB = ImmutableList.<Operation>of(
      new Set(turn,W),
      new Set(B, moveFromTo(1,2,3,4)));
  
  private final List<Operation> illegalMoveWithWrongToOfB = ImmutableList.<Operation>of(
      new Set(turn,W),
      new Set(B, moveFromTo(1,2,3,4)));
  
  private final List<Operation> illegalMoveWithWrongFromOfW = ImmutableList.<Operation>of(
      new Set(turn,B),
      new Set(B, moveFromTo(1,2,3,4)));
  
  private final List<Operation> illegalMoveWithWrongPieceOfW = ImmutableList.<Operation>of(
      new Set(turn,B),
      new Set(B, moveFromTo(1,2,3,4)));


  private VerifyMove move(
      int lastMovePlayerId, Map<String, Object> lastState, List<Operation> lastMove) {
    return new VerifyMove(wId, playersInfo,
        // in cheat we never need to check the resulting state (the server makes it, and the game
        // doesn't have any hidden decisions such in Battleships)
        emptyState,
        lastState, lastMove, lastMovePlayerId);
  }
  
  private Board placePieces(){
    return board;  
  }
  
  private Board moveFromTo(int i1, int j1, int i2, int j2){
    return board;   
  }

  private List<String> concat(List<String> a, List<String> b) {
    return Lists.newArrayList(Iterables.concat(a, b));
  }

  private List<Operation> getInitialOperations() {
    List<Operation> operations = Lists.newArrayList();
    operations.add(new Set(turn, S));
    // pieces numbered?
    return operations;
  }

  @Test
  public void testInitialMove() {
    assertMoveOk(move(wId, emptyState, getInitialOperations()));
  }

  @Test
  public void testInitialMoveByWrongPlayer() {
    assertHacker(move(bId, emptyState, getInitialOperations()));
  }

  @Test
  public void testInitialMoveFromNonEmptyState() {
    assertHacker(move(wId, nonEmptyState, getInitialOperations()));
  }

  @Test
  public void testInitialMoveWithExtraOperation() {
    List<Operation> initialOperations = getInitialOperations();
    initialOperations.add(new Set(S, ready));
    assertHacker(move(wId, emptyState, initialOperations));
  }

  @Test
  public void testArrangePiecesB() {
    assertMoveOk(move(bId, turnOfB, arrangePiecesB));
  }
  
  @Test
  public void testArrangePiecesW() {
    assertMoveOk(move(wId, turnOfW, arrangePiecesW));
  }

  @Test
  public void testIllegalArrangePiecesWrongPositionW() {
    assertHacker(move(wId, turnOfS, illegalArrangePiecesW));
  }
  
  @Test
  public void testIllegalArrangePiecesWrongColorW() {
    assertHacker(move(wId, turnOfS, illegalArrangePiecesW));
  }

  @Test
  public void testIllegalArrangePiecesWrongNumberB() {
    assertHacker(move(bId, turnOfS, illegalArrangePiecesB));
  }
  
 
  @Test
  public void testClaimWithWrongCards() {
    assertHacker(move(wId, turnOfWEmptyMiddle, illegalClaimWithWrongCards));
  }

  @Test
  public void testClaimWithWrongW() {
    assertHacker(move(wId, turnOfWEmptyMiddle, illegalClaimWithWrongW));
  }

  @Test
  public void testClaimWithWrongM() {
    assertHacker(move(wId, turnOfWEmptyMiddle, illegalClaimWithWrongM));
  }

  List<Operation> claimCheaterByW = ImmutableList.<Operation>of(
      new Set(turn, W),
      new Set(isCheater, yes),
      new SetVisibility("C53"), new SetVisibility("C54"));

  @Test
  public void testClaimCheaterByWhite() {
    Map<String, Object> state = ImmutableMap.<String, Object>of(
        turn, W,
        W, getCardsInRange(1, 10),
        B, getCardsInRange(11, 52),
        M, getCardsInRange(53, 54),
        claim, ImmutableList.of("2cards", "rankA"));

    assertMoveOk(move(wId, state, claimCheaterByW));
  }

  @Test
  public void testCannotClaimCheaterWhenMiddlePileIsEmpty() {
    assertHacker(move(wId, turnOfWEmptyMiddle, claimCheaterByW));
  }

  @Test
  public void testBlackIsIndeedCheater() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put(turn, W)
        .put(isCheater, yes)
        .put("C53", "Ah")
        .put("C54", "Kh")
        .put(W, getCardsInRange(1, 10))
        .put(B, getCardsInRange(11, 52))
        .put(M, getCardsInRange(53, 54))
        .put(claim, ImmutableList.of("2cards", "rankA"))
        .build();

    List<Operation> operations = ImmutableList.<Operation>of(
        new Set(turn, B),
        new Delete(isCheater),
        new Set(B, getCardsInRange(11, 54)),
        new Set(M, ImmutableList.of()),
        new SetVisibility("C53", visibleToB),
        new SetVisibility("C54", visibleToB),
        new Shuffle(getCardsInRange(11, 54)));

    assertMoveOk(move(wId, state, operations));
    assertHacker(move(bId, state, operations));
    assertHacker(move(wId, emptyState, operations));
    assertHacker(move(wId, turnOfWEmptyMiddle, operations));
  }

  @Test
  public void testBlackWasNotCheating() {
    Map<String, Object> state = ImmutableMap.<String, Object>builder()
        .put(turn, W)
        .put(isCheater, yes)
        .put("C53", "Ah")
        .put("C54", "Ah")
        .put(W, getCardsInRange(1, 10))
        .put(B, getCardsInRange(11, 52))
        .put(M, getCardsInRange(53, 54))
        .put(claim, ImmutableList.of("2cards", "rankA"))
        .build();

    List<String> wNewCards = concat(getCardsInRange(1, 10), getCardsInRange(53, 54));
    List<Operation> operations = ImmutableList.<Operation>of(
        new Set(turn, W),
        new Delete(isCheater),
        new Set(W, wNewCards),
        new Set(M, ImmutableList.of()),
        new SetVisibility("C53", visibleToW),
        new SetVisibility("C54", visibleToW),
        new Shuffle(wNewCards));

    assertMoveOk(move(wId, state, operations));
    assertHacker(move(bId, state, operations));
    assertHacker(move(wId, emptyState, operations));
    assertHacker(move(wId, turnOfWEmptyMiddle, operations));
  }

  @Test
  public void testIncreasePreviousClaim() {
    assertMoveOk(getChangePreviousClaim("2"));
  }

  @Test
  public void testDecreasePreviousClaim() {
    assertMoveOk(getChangePreviousClaim("K"));
  }

  @Test
  public void testKeepPreviousClaim() {
    assertMoveOk(getChangePreviousClaim("A"));
  }

  @Test
  public void testIllegalNextClaim() {
    assertHacker(getChangePreviousClaim("Q"));
    assertHacker(getChangePreviousClaim("10"));
    assertHacker(getChangePreviousClaim("3"));
  }

  private VerifyMove getChangePreviousClaim(String newRank) {
    Map<String, Object> state = ImmutableMap.<String, Object>of(
        turn, W,
        W, getCardsInRange(1, 10),
        B, getCardsInRange(11, 52),
        M, getCardsInRange(53, 54),
        claim, ImmutableList.of("2cards", "rankA"));
    List<Operation> claimByW = ImmutableList.<Operation>of(
        new Set(turn, B),
        new Set(W, getCardsInRange(5, 10)),
        new Set(M, concat(getCardsInRange(53, 54), getCardsInRange(1, 4))),
        new Set(claim, ImmutableList.of("4cards", "rank" + newRank)));
    return move(wId, state, claimByW);
  }

  @Test
  public void test() {
    fail("Not yet implemented");
  }

}
