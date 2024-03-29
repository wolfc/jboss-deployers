/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.deployers.plugins.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.jboss.deployers.spi.Ordered.COMPARATOR;

/**
 * Simple transition ordering using transitive closure.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 * @param <T> exact domino type
 */
public class DominoOrdering<T extends Domino<?>>
{
   protected String message;

   protected List<T> dominoes;
   protected int size;
   protected int[][] connections;

   public DominoOrdering(String message)
   {
      this.message = message;
   }

   /**
    * Initialize transitions.
    *
    * @param dominoes all dominoes
    * @param cause the possible cycle cause
    */
   @SuppressWarnings("unchecked")
   protected void init(List<T> dominoes, Object cause)
   {
      this.dominoes = dominoes;
      this.size = dominoes.size();
      this.connections = new int[size][size];

      for (int i = 0; i < size - 1; i++)
      {
         for (int j = i + 1; j < size; j++)
         {
            Domino one = dominoes.get(i);
            Domino two = dominoes.get(j);
            Dots oneHead = one.getHead();
            Dots oneTail = one.getTail();
            Dots twoHead = two.getHead();
            Dots twoTail = two.getTail();
            int fstXsnd = oneTail.intersect(twoHead);
            int sndXfst = twoTail.intersect(oneHead);
            int relation = 0;
            if (fstXsnd > 0 && sndXfst > 0)
            {
               // pass-through deployers
               if (oneHead.intersect(twoHead) > 0 && oneTail.intersect(twoTail) > 0)
               {
                  // lets try to do more exact match
                  // although we should aviod singe dimension checks
                  // which are already part of match() check
                  int thXot = (oneTail.dimension() > 1 && twoHead.dimension() > 1) ? twoHead.intersect(oneTail) : 1;
                  int before = Math.max(fstXsnd, thXot);
                  int ohXtt = (oneHead.dimension() > 1 && twoTail.dimension() > 1) ? oneHead.intersect(twoTail) : 1;
                  int after = Math.max(sndXfst, ohXtt);
                  relation = after - before; // do we have some more exact match?

                  // make name compare lazy - might already be fixed with transitive order
               }
               else
               {
                  // short circut cycle - throw exception immediately
                  throwCycleException(cause);
               }
            }
            else
            {
               relation = fstXsnd > 0 ? -1 : (sndXfst > 0 ? 1 : 0);
            }

            if (relation == 0)
            {
               // lazy compare on those who don't have order set
               if (one.getRelativeOrder() != 0 && two.getRelativeOrder() != 0)
                  relation = one.getRelativeOrder() - two.getRelativeOrder();
            }
            connections[i][j] = relation;
            connections[j][i] = -connections[i][j];
         }
      }
   }

   /**
    * Order dominoes.
    *
    * @param dominoes all dominoes
    * @param cause the possible cycle cause
    * @return ordered dominoes list
    */
   public List<T> orderDominoes(List<T> dominoes, Object cause)
   {
      // prepare initial transitions
      init(dominoes, cause);
      // do transitive closure
      int cycle = fillTransitions(true);
      if (cycle >= 0)
         throwCycleException(cause);
      // name compare on 'uncomparable'
      fillCompareNames();

      List<Integer> indexes = new ArrayList<Integer>();
      for (int i = 0; i < size; i++)
         indexes.add(i);
      Collections.sort(indexes, new IndexComparator());

      List<T> list = new ArrayList<T>(size);
      for (Integer index : indexes)
         list.add(dominoes.get(index));
      return list;
   }

   /**
    * Fill transitions.
    *
    * @param fillTransitions do change
    * @return index of the domino cycle cause, -1 otherwise
    */
   protected int fillTransitions(boolean fillTransitions)
   {
      boolean changed = true;
      while (changed)
      {
         changed = false;
         for (int i = 0; i < size; i++)
         {
            for (int j = 0; j < size; j++)
            {
               int current = connections[i][j];
               if (j == i || current == 0)
                  continue;
               for (int k = 0; k < size; k++)
               {
                  if (k == i || k == j)
                     continue;
                  int lookup = connections[j][k];
                  // same signum
                  if (current * lookup > 0)
                  {
                     int next = connections[i][k];
                     // cycle
                     if (next * current < 0)
                     {
                        return i;
                     }
                     else if (fillTransitions && next == 0)
                     {
                        connections[i][k] = current;
                        changed = true;
                     }
                  }
               }
            }
         }
      }
      return -1;
   }

   /**
    * Fill transitions with name compare.
    */
   protected void fillCompareNames()
   {
      for (int i = 0; i < size - 1; i++)
      {
         for (int j = i + 1; j < size; j++)
         {
            if (connections[i][j] == 0)
            {
               T one = dominoes.get(i);
               T two = dominoes.get(j);
               connections[i][j] = COMPARATOR.compare(one, two);
               connections[j][i] = -connections[i][j];
               int cycle = fillTransitions(false);
               // we introduced cycle - flip the signum
               if (cycle >= 0)
               {
                  connections[i][j] = -connections[i][j];
                  connections[j][i] = -connections[i][j];
               }
            }
         }
      }
   }

   /**
    * Throw the cycle exception.
    *
    * @param cause the cycle cause
    */
   protected void throwCycleException(Object cause)
   {
      StringBuilder builder = new StringBuilder();
      builder.append(String.format(message, cause));
      for (T d : dominoes)
         builder.append(d.getInfo());
      throw new IllegalStateException(builder.toString());
   }

   /**
    * The index comparator.
    */
   protected class IndexComparator implements Comparator<Integer>
   {
      public int compare(Integer i1, Integer i2)
      {
         return connections[i1][i2];
      }
   }
}
