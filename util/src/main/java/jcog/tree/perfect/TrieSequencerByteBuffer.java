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

import java.nio.ByteBuffer;

/**
 * A {@link TrieSequencer} implementation where ByteBuffer is the sequence type.
 * 
 * @author Philip Diffenderfer
 * 
 */
public class TrieSequencerByteBuffer implements TrieSequencer<ByteBuffer>
{

   @Override
   public int matches( ByteBuffer sequenceA, int indexA, ByteBuffer sequenceB, int indexB, int count )
   {
      for (int i = 0; i < count; i++) {
         if (sequenceA.get(indexA + i) != sequenceB.get(indexB + i)) {
            return i;
         }
      }
      return count;

   }

   @Override
   public int lengthOf( ByteBuffer sequence )
   {
      return sequence.limit();
   }

   @Override
   public int hashOf( ByteBuffer sequence, int i )
   {
      return sequence.get( i ) & 0xFF; 
   }
   
}
