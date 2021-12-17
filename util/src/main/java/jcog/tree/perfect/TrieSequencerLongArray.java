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


/**
 * A {@link TrieSequencer} implementation where long[] is the sequence type.
 *
 * @author Philip Diffenderfer
 */
public class TrieSequencerLongArray implements TrieSequencer<long[]> {

//    public static class TrieSequencerLong implements TrieSequencer<Long> {
//
//        @Override
//        public int matches(Long sequenceA, int indexA, Long sequenceB, int indexB, int count) {
//            for (int i = 0; i < count; i++) {
//                if (sequenceA[indexA + i] != sequenceB[indexB + i]) {
//                    return i;
//                }
//            }
//
//            return count;
//        }
//
//        @Override
//        public int lengthOf(Long sequence) {
//            return sequence.length;
//        }
//
//        @Override
//        public int hashOf(long[] sequence, int i) {
//            return (int) sequence[i];
//        }
//
//    }


    @Override
    public int matches(long[] sequenceA, int indexA, long[] sequenceB, int indexB, int count) {
        for (int i = 0; i < count; i++) {
            if (sequenceA[indexA + i] != sequenceB[indexB + i]) {
                return i;
            }
        }
        return count;

    }

    @Override
    public int lengthOf(long[] sequence) {
        return sequence.length;
    }

    @Override
    public int hashOf(long[] sequence, int i) {
        return (int) sequence[i];
    }

}
