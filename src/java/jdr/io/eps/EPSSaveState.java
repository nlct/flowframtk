// File          : EPSSaveState.java
// Purpose       : class representing save object
// Date          : 24 June 2008
// Last Modified : 24 June 2008
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

/*
    Copyright (C) 2006 Nicola L.C. Talbot

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package com.dickimawbooks.jdr.io.eps;

import java.util.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.EPS;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing a save object. This is not fully
 * implemented.
 * @author Nicola L C Talbot
 */
public class EPSSaveState implements EPSObject
{
   private EPSSaveState()
   {
   }

   public EPSSaveState(EPS eps)
   {
      EPSStack stack = eps.getStack();

      graphicsState = eps.getCurrentGraphicsState();

      composites = new Vector<SaveComposite>();

      for (int i = 0, n=stack.getDictStackSize(); i < n; i++)
      {
         EPSDict dict = stack.getDict(i);

         for (Enumeration<EPSObject> en = dict.elements();
              en.hasMoreElements();)
         {
            EPSObject obj = en.nextElement();

            if ((obj instanceof EPSComposite)
            && !(obj instanceof EPSString))
            {
               composites.add(new SaveComposite((EPSComposite)obj));
            }
         }
      }
   }

   public class SaveComposite
   {
      public SaveComposite(EPSComposite composite)
      {
         object = composite;
         state  = (EPSComposite)composite.clone();
      }

      private SaveComposite()
      {
      }

      public boolean equals(SaveComposite composite)
      {
         return ((object == composite.object)
               && state.equals(composite.state));
      }

      public Object clone()
      {
         SaveComposite composite = new SaveComposite();

         composite.object = object;
         composite.state = (EPSComposite)state.clone();

         return composite;
      }

      public EPSComposite object, state;
   }

   public void restore(EPS eps)
   {
      eps.grestoreall();

      EPSStack stack = eps.getStack();

      for (int i = 0; i < composites.size(); i++)
      {
         SaveComposite composite = composites.get(i);

         composite.object.makeEqual(composite.state);
      }
   }

   public String toString()
   {
      return "save";
   }

   public boolean equals(Object object)
   {
      if (this == object)
      {
         return true;
      }

      if (!(object instanceof EPSSaveState))
      {
         return false;
      }

      EPSSaveState obj = (EPSSaveState)object;

      if (obj.graphicsState != graphicsState)
      {
         return false;
      }

      if (!obj.composites.equals(composites))
      {
         return false;
      }

      return true;
   }

   public EPSName pstype()
   {
      return new EPSName("savetype");
   }

   public GraphicsState getGraphicsState()
   {
      return graphicsState;
   }

   public Object clone()
   {
      EPSSaveState state = new EPSSaveState();

      state.composites 
         = new Vector<SaveComposite>(composites.size());

      for (int i = 0; i < composites.size(); i++)
      {
         state.composites.add(i,
            (SaveComposite)composites.get(i).clone());
      }

      state.graphicsState = graphicsState;

      return state;
   }

   private Vector<SaveComposite> composites;

   private GraphicsState graphicsState;
}
