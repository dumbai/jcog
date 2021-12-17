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

import static org.junit.jupiter.api.Assertions.*;


class TestPerfectHashMap
{

   @Test
   void testEmpty()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();

      assertEquals( 0, map.capacity() );
      assertEquals( 0, map.size);
      assertNull( map.get( 0 ) );
      assertNull( map.get( 1 ) );
      assertNull( map.get( 2 ) );
   }

   @Test
   void testOne()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();

      map.put( 45, "Hello World!" );

	   assertEquals( 45, map.min);
      assertEquals( 45, map.getMax() );
      assertEquals( 1, map.size);
      assertEquals( 1, map.capacity() );

      assertEquals( "Hello World!", map.get( 45 ) );
   }

   @Test
   void testFirstConstructor()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>(45, "Hello World!");

	   assertEquals( 45, map.min);
      assertEquals( 45, map.getMax() );
      assertEquals( 1, map.size);
      assertEquals( 1, map.capacity() );

      assertEquals( "Hello World!", map.get( 45 ) );
   }

   @Test
   void testPutAfter()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();

      map.put( 45, "First" );

      map.put( 47, "Second" );

      assertEquals( 2, map.size);
      assertEquals( 3, map.capacity() );
	   assertEquals( 45, map.min);
      assertEquals( 47, map.getMax() );
      assertEquals( "First", map.get( 45 ) );
      assertEquals( "Second", map.get( 47 ) );

   }

   @Test
   void testPutBefore()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();

      map.put( 45, "First" );

      map.put( 47, "Second" );

      assertEquals( 2, map.size);
      assertEquals( 3, map.capacity() );
	   assertEquals( 45, map.min);
      assertEquals( 47, map.getMax() );
      assertEquals( "First", map.get( 45 ) );
      assertEquals( "Second", map.get( 47 ) );

      map.put( 42, "Third" );

      assertEquals( 3, map.size);
      assertEquals( 6, map.capacity() );
	   assertEquals( 42, map.min);
      assertEquals( 47, map.getMax() );
      assertEquals( "First", map.get( 45 ) );
      assertEquals( "Second", map.get( 47 ) );
      assertEquals( "Third", map.get( 42 ) );
   }

   @Test
   void testPutMiddle()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();

      map.put( 45, "First" );

      map.put( 47, "Second" );

      assertEquals( 2, map.size);
      assertEquals( 3, map.capacity() );
	   assertEquals( 45, map.min);
      assertEquals( 47, map.getMax() );
      assertEquals( "First", map.get( 45 ) );
      assertEquals( "Second", map.get( 47 ) );

      map.put( 46, "Third" );

      assertEquals( 3, map.size);
      assertEquals( 3, map.capacity() );
	   assertEquals( 45, map.min);
      assertEquals( 47, map.getMax() );
      assertEquals( "First", map.get( 45 ) );
      assertEquals( "Second", map.get( 47 ) );
      assertEquals( "Third", map.get( 46 ) );
   }

   @Test
   void testExists()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();
      map.put( -14, "First" );
      map.put( -11, "Second" );
      map.put( -10, "Third" );
      
      assertFalse( map.exists( -15  ) );
      assertTrue( map.exists( -14  ) );
      assertFalse( map.exists( -13  ) );
      assertFalse( map.exists( -12  ) );
      assertTrue( map.exists( -11  ) );
      assertTrue( map.exists( -10  ) );
      assertFalse( map.exists( -9  ) );
      assertFalse( map.exists( -8  ) );
   }
   
   @Test
   void testRemoveFirst()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();
      map.put( -14, "First" );
      map.put( -11, "Second" );
      map.put( -10, "Third" );

	   assertEquals( -14, map.min);
      assertEquals( "First", map.get( -14 ) );
      assertEquals( 3, map.size);
      
      map.remove( -14 );

	   assertEquals( -11, map.min);
      assertNull( map.get( -14 ) );

      assertEquals( 2, map.size);
      assertEquals( "Second", map.get( -11 ) );
      assertEquals( "Third", map.get( -10 ) );
   }

   @Test
   void testRemoveMiddle()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();
      map.put( -14, "First" );
      map.put( -11, "Second" );
      map.put( -10, "Third" );

	   assertEquals( -14, map.min);
      assertEquals( "Second", map.get( -11 ) );
      assertEquals( 3, map.size);
      
      map.remove( -11 );

	   assertEquals( -14, map.min);
      assertNull( map.get( -11 ) );

      assertEquals( 2, map.size);
      assertEquals( "First", map.get( -14 ) );
      assertEquals( "Third", map.get( -10 ) );
   }

   @Test
   void testRemoveLast()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();
      map.put( -14, "First" );
      map.put( -11, "Second" );
      map.put( -10, "Third" );

	   assertEquals( -14, map.min);
      assertEquals( -10, map.getMax() );
      assertEquals( "Third", map.get( -10 ) );
      assertEquals( 3, map.size);
      
      map.remove( -10 );

	   assertEquals( -14, map.min);
      assertEquals( -11, map.getMax() );
      assertNull( map.get( -10 ) );

      assertEquals( 2, map.size);
      assertEquals( "First", map.get( -14 ) );
      assertEquals( "Second", map.get( -11 ) );
   }

   @Test
   void testRemoveAll()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();
      map.put( -14, "First" );
      map.put( -11, "Second" );
      map.put( -10, "Third" );

      map.remove( -11 );
      map.remove( -14 );
      map.remove( -10 );

      assertEquals( 0, map.size);
      assertEquals( 0, map.capacity() );
      assertTrue( map.isEmpty() );
      
      assertFalse( map.exists( -14 ) );
      assertFalse( map.exists( -11 ) );
      assertFalse( map.exists( -10 ) );
   }

   @Test
   void testClear()
   {
      PerfectHashMap<String> map = new PerfectHashMap<>();
      map.put( -14, "First" );
      map.put( -11, "Second" );
      map.put( -10, "Third" );
      map.clear();

      assertEquals( 0, map.size);
      assertEquals( 0, map.capacity() );
      assertTrue( map.isEmpty() );
      
      assertFalse( map.exists( -14 ) );
      assertFalse( map.exists( -11 ) );
      assertFalse( map.exists( -10 ) );
   }

}
