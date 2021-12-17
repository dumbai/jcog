/* 
 * NOTICE OF LICENSE
 * 
 * This source file is subject to the Open Software License (OSL 3.0) that is 
 * bundled with this package in the file LICENSE.txt. It is also available 
 * through the world-wide-web at http:
 * If you did not receive a copy of the license and are unable to obtain it 
 * through the world-wide-web, please send an email to magnos.software@gmail.com 
 * so we can send you a copy immediately. If you use any of this software please
 * notify me via our website or email, your feedback is much appreciated. 
 * 
 * @copyright   Copyright (c) 2011 Magnos Software (http:
 * @license     http:
 *              Open Software License (OSL 3.0)
 */

package jcog.tree.perfect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class TestTrieSequencerCharArray
{

   private static final char[] SEQ1 = "HELLO".toCharArray();
   private static final char[] SEQ2 = "HE".toCharArray();
   private static final char[] SEQ3 = "WORLD".toCharArray();
   private static final char[] SEQ4 = "HELLOWORLD".toCharArray();
   private static final char[] SEQ5 = "WOW".toCharArray();
   
   @Test
   void testMatches()
   {
      TrieSequencer<char[]> seq = new TrieSequencerCharArray();
      
      assertEquals( 2, seq.matches( SEQ1, 0, SEQ2, 0, 2 ) );
      assertEquals( 0, seq.matches( SEQ1, 1, SEQ2, 0, 2 ) );
      
      assertEquals( 5, seq.matches( SEQ3, 0, SEQ4, 5, 5 ) );
      assertEquals( 2, seq.matches( SEQ5, 0, SEQ4, 5, 2 ) );
   }
   
   @Test
   void testLengthOf()
   {
      TrieSequencer<char[]> seq = new TrieSequencerCharArray();
      
      assertEquals( 5, seq.lengthOf( SEQ1 ) );
      assertEquals( 2, seq.lengthOf( SEQ2 ) );
      assertEquals( 5, seq.lengthOf( SEQ3 ) );
      assertEquals( 10, seq.lengthOf( SEQ4 ) );
      assertEquals( 3, seq.lengthOf( SEQ5 ) );
   }
   
   @Test
   void testHashOf()
   {
      TrieSequencer<char[]> seq = new TrieSequencerCharArray();
      
      assertEquals( 'W', seq.hashOf( SEQ3, 0 ) );
      assertEquals( 'O', seq.hashOf( SEQ3, 1 ) );
      assertEquals( 'R', seq.hashOf( SEQ3, 2 ) );
      assertEquals( 'L', seq.hashOf( SEQ3, 3 ) );
      assertEquals( 'D', seq.hashOf( SEQ3, 4 ) );
   }
   
}
