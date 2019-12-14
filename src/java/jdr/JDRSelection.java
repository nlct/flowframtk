// File          : JDRSelection.java
// Purpose       : Stores information about the current selection
// Creation Date : 2014-04-26
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

package com.dickimawbooks.jdr;

public class JDRSelection implements JDRConstants
{
   public JDRSelection()
   {
      selections = new int[OBJECT_MAX_INDEX+1];
      selectionFlag = 0;

      for (int i = 0; i < selections.length; i++)
      {
         selections[i] = 0;
      }
   }

   public void addToSelection(JDRCompleteObject object)
   {
      int completeObjectFlag = object.getObjectFlag();

      selectionFlag = (selectionFlag | completeObjectFlag);

      int objectFlag = completeObjectFlag;

      if (object instanceof JDRCompoundShape
       || object instanceof JDRGroup)
      {
         objectFlag = (objectFlag & ~SELECT_FLAG_PATH);
         objectFlag = (objectFlag & ~SELECT_FLAG_TEXT);
         objectFlag = (objectFlag & ~SELECT_FLAG_BITMAP);
      }

      for (int i = 0; i < selections.length; i++)
      {
         if (((1 << i) & objectFlag) != 0)
         {
            selections[i]++;
         }
      }
   }

   public int getSelectionFlag()
   {
      return selectionFlag;
   }

   public int[] getSelectionCount()
   {
      return selections;
   }

   public static JDRSelection getSelections(JDRGroup group)
   {
      int n = group.size();

      if (n == 0)
      {
         return null;
      }

      JDRSelection selection = new JDRSelection();

      for (int i = 0; i < n; i++)
      {
         JDRCompleteObject object = group.get(i);

         if (object.isSelected())
         {
            selection.addToSelection(object);
         }
      }

      if (selection.selectionFlag == 0)
      {
         selection.selectionFlag = SELECT_FLAG_NONE;
      }

      return selection;
   }

   private int selectionFlag;
   private int[] selections;
}
