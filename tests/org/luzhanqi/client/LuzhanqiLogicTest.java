package org.luzhanqi.client;

//import static com.google.common.base.Preconditions.checkArgument;
//
//import java.util.List;
//import java.util.Map;

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
//
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.ImmutableMap;
//import com.google.common.collect.Iterables;
//import com.google.common.collect.Lists;

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
  
  @Test
  public void test() {
    fail("Not yet implemented");
  }

}
