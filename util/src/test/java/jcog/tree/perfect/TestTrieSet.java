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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class TestTrieSet
{

   @Test
   void testConstructor()
   {
      TrieSet<String> set = new TrieSet<>(Tries.STRING_TRIE_SEQUENCER_CHAR_SEQUENCE);

      assertTrue( set.isEmpty() );
      assertEquals( 0, set.size() );
   }

   @Test
   void testAdd()
   {
      TrieSet<String> set = new TrieSet<>(Tries.STRING_TRIE_SEQUENCER_CHAR_SEQUENCE);;

      assertFalse( set.contains( "meow" ) );
      assertTrue( set.add( "meow" ) );
      assertTrue( set.contains( "meow" ) );
      assertFalse( set.isEmpty() );
      assertEquals( 1, set.size() );

      assertFalse( set.contains( "meowa" ) );
      assertFalse( set.contains( "meo" ) );
      assertFalse( set.contains( "me" ) );
      assertFalse( set.contains( "m" ) );
   }

   @Test
   void testAddAll()
   {
      TrieSet<String> set = new TrieSet<>(Tries.STRING_TRIE_SEQUENCER_CHAR_SEQUENCE);;

      List<String> words = Arrays.asList( "meow", "kitten", "purr" );

      set.addAll( words );

      assertEquals( 3, set.size() );
      assertTrue( set.contains( "meow" ) );
      assertTrue( set.contains( "kitten" ) );
      assertTrue( set.contains( "purr" ) );
   }

   @Test
   void testClear()
   {
      TrieSet<String> set = new TrieSet<>(Tries.STRING_TRIE_SEQUENCER_CHAR_SEQUENCE);;

      set.add( "meow" );
      set.add( "kitten" );
      set.add( "purr" );

      assertEquals( 3, set.size() );
      assertTrue( set.contains( "meow" ) );
      assertTrue( set.contains( "kitten" ) );
      assertTrue( set.contains( "purr" ) );

      set.clear();

      assertEquals( 0, set.size() );
      assertTrue( set.isEmpty() );
      assertFalse( set.contains( "meow" ) );
      assertFalse( set.contains( "kitten" ) );
      assertFalse( set.contains( "purr" ) );
   }

   @Test
   void testContainsAll()
   {
      TrieSet<String> set = new TrieSet<>(Tries.STRING_TRIE_SEQUENCER_CHAR_SEQUENCE);;

      set.add( "meow" );
      set.add( "kitten" );
      set.add( "purr" );

      assertTrue( set.contains("meow") );
      assertTrue( set.containsAll( Arrays.asList( "meow", "kitten" ) ) );
      assertTrue( set.containsAll( Arrays.asList( "meow", "kitten", "purr" ) ) );
      assertTrue( set.contains("purr") );
      assertTrue( set.containsAll(Collections.emptyList()) );

      assertFalse( set.contains("NOPE") );
      assertFalse( set.containsAll( Arrays.asList( "meow", "NOPE" ) ) );
      assertFalse( set.containsAll( Arrays.asList( "meow", "kitten", "NOPE" ) ) );
      assertFalse( set.containsAll( Arrays.asList( "meow", "kitten", "purr", "NOPE" ) ) );
      assertFalse( set.containsAll( Arrays.asList( "purr", "NOPE" ) ) );
   }
   
   @Test
   void testIterator()
   {
      TrieSet<String> set = new TrieSet<>(Tries.STRING_TRIE_SEQUENCER_CHAR_SEQUENCE);;

      set.add( "meow" );
      set.add( "kitten" );
      set.add( "purr" );

       String actual = "";
      
      for (String key : set)
      {
         actual += key;
      }

       String expected = "kittenmeowpurr";
       assertEquals( expected, actual );
   }
   
   @Test
   void testRemove()
   {
      TrieSet<String> set = new TrieSet<>(Tries.STRING_TRIE_SEQUENCER_CHAR_SEQUENCE);;

      set.add( "meow" );
      set.add( "kitten" );
      set.add( "purr" );
      
      assertTrue( set.contains( "meow" ) );
      assertTrue( set.remove( "meow" ) );
      assertFalse( set.contains( "meow" ) );
      assertEquals( 2, set.size() );
   }
   
   @Test
   void testRemoveAll()
   {
      TrieSet<String> set = new TrieSet<>(Tries.STRING_TRIE_SEQUENCER_CHAR_SEQUENCE);;

      set.add( "meow" );
      set.add( "kitten" );
      set.add( "purr" );
      
      set.removeAll( Arrays.asList( "meow", "kitten", "NOPE" ) );
      
      assertEquals( 1, set.size() );
      assertTrue( set.contains( "purr" ) );
      assertFalse( set.contains( "meow" ) );
      assertFalse( set.contains( "kitten" ) );
   }
   
   @Test
   void testRetainAll()
   {
      TrieSet<String> set = new TrieSet<>(Tries.STRING_TRIE_SEQUENCER_CHAR_SEQUENCE);;
      
      set.add( "meow" );
      set.add( "kitten" );
      set.add( "purr" );
      
      set = set.retainsAll( Arrays.asList( "meow", "kitten", "NOPE" ) );

      assertEquals( 2, set.size() );
      assertFalse( set.contains( "purr" ) );
      assertTrue( set.contains( "meow" ) );
      assertTrue( set.contains( "kitten" ) );
      assertFalse( set.contains( "NOPE" ) );
   }
   

}