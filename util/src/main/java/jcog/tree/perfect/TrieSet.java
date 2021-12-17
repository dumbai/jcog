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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;


/**
 * A {@link Set} where the underlying data structure is a Trie&lt;E,
 * Object&gt;. a TrieSet can be encoded into a minimized byte array and can be
 * decoded from an encoded byte array.
 * 
 * @author Philip Diffenderfer
 * 
 * @param <E>
 *        The element type.
 */
public final class TrieSet<E> implements Set<E> {

   /**
    * The flag used in the underlying Trie of a TrieSet to indicate the given
    * value exists in the TrieSet.
    */
   public static final Object FLAG = new Object();

   /**
    * The flag used in the underlying Trie of a TrieSet to indicate the given
    * value does not exist in the TrieSet.
    */
   public static final Object FLAG_NONE = null;

   protected final Trie<E, Object> trie;

   /**
    * Instantiates a TrieSet given a trie. 
    * <h3>Example Usage</h3>
    * <pre>
    * TrieSet&lt;String&gt; set = new TrieSet&lt;String&gt;( Tries.forStrings() );
    * </pre>
    * 
    * @param trie
    *        The trie to use as the base.
    */
   public TrieSet( TrieSequencer<E> seq )
   {
      this(new Trie(seq) {
         @Override
         public TrieMatch defaultMatch() {
            return TrieMatch.EXACT;
         }
      });
      this.trie.setDefaultValue( FLAG_NONE );
   }

   private TrieSet( Trie<E, Object> t )
   {
      this.trie = t;
      this.trie.setDefaultValue( FLAG_NONE );
   }

   /**
    * Returns the reference to the underlying Trie. The reference to the
    * underlying Trie may change if the {@link #retainAll(Collection)} method is
    * called after the reference is gotten.
    * 
    * @return The reference to the underlying Trie.
    */
   public Trie<E, Object> trie()
   {
      return trie;
   }

   @Override
   public boolean add( E value )
   {
      return trie.put( value, FLAG ) == FLAG_NONE;
   }

   @Override
   public boolean addAll( Collection<? extends E> collection )
   {
      boolean acc = false;
      for (E e : collection) {
         boolean add = add(e);
         acc = acc || add;
      }
      boolean changed = acc;

       return changed;
   }

   @Override
   public boolean retainAll(Collection<?> collection) {
      throw new RuntimeException("use retainsAll");
      
   }

   @Override
   public void clear()
   {
      trie.clear();
   }

   @Override
   public boolean contains( Object value )
   {
      return trie.containsKey( value );
   }

   @Override
   public boolean containsAll( Collection<?> collection )
   {

      return collection.stream().allMatch(trie::containsKey);
   }

   @Override
   public boolean isEmpty()
   {
      return trie.isEmpty();
   }

   @Override
   public Iterator<E> iterator()
   {
      return trie.keySet().iterator();
   }

   @Override
   public boolean remove( Object value )
   {
      return trie.remove( value ) == FLAG;
   }

   @Override
   public boolean removeAll( Collection<?> collection )
   {
      boolean acc = false;
      for (Object o : collection) {
         boolean remove = remove(o);
         acc = acc || remove;
      }
      boolean changed = acc;

       return changed;
   }

   public TrieSet<E> retainsAll( Collection<?> collection ) {
      int previousSize = trie.size();
      Trie<E, Object> newTrie = trie.newEmptyClone();

      for (Object element : collection) {
         if (trie.containsKey(element)) {
            newTrie.put((E) element, FLAG);
         }
      }
      if (previousSize!=newTrie.size())
         return new TrieSet(newTrie); 

      return this;
      
   }





















   @Override
   public int size()
   {
      return trie.size();
   }

   @Override
   public Object[] toArray()
   {
      return trie.keySet().toArray();
   }

   @Override
   public <T> T[] toArray( T[] arr )
   {
      return trie.keySet().toArray( arr );
   }


}
