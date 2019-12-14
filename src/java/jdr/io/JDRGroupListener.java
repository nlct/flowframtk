// File          : JDRGroupListener.java
// Creation Date : 29th February 2008
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

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

package com.dickimawbooks.jdr.io;

import java.io.*;
import java.util.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Loader listener for paths.
 * @author Nicola L C Talbot
 */

public class JDRGroupListener implements JDRObjectLoaderListener
{
   public char getId(float version)
   {
      return 'G';
   }

   public JDRObject getObject(JDRAJR jdr, JDRObject object, float version)
   {
      return object;
   }

   public void write(JDRAJR jdr, JDRObject object)
     throws IOException
   {
      JDRGroup group = (JDRGroup)object;

      JDRObjectLoader objectLoader = jdr.getObjectLoader();

      int n = group.size();
      jdr.writeInt(n);

      int maxProgress = jdr.getMessageSystem().getMaxProgress();
      int progress = jdr.getMessageSystem().getProgress();

      jdr.getMessageSystem().getPublisher().publishMessages(
          MessageInfo.createProgress(n));

      for (int i = 0; i < n; i++)
      {
         objectLoader.save(jdr, group.get(i));

         jdr.getMessageSystem().getPublisher().publishMessages(
            MessageInfo.createIncProgress());
      }

      jdr.getMessageSystem().getPublisher().publishMessages(
         MessageInfo.createProgress(maxProgress),
         MessageInfo.createSetProgress(progress));
   }

   public JDRObject read(JDRAJR jdr)
     throws InvalidFormatException
   {
      int n = jdr.readIntGe(InvalidFormatException.GROUP_SIZE, 0);

      int maxProgress = jdr.getMessageSystem().getMaxProgress();
      int progress = jdr.getMessageSystem().getProgress();

      jdr.getMessageSystem().getPublisher().publishMessages(
         MessageInfo.createProgress(n));

      JDRGroup group = new JDRGroup(jdr.getCanvasGraphics(), n);

      JDRObjectLoader objectLoader = JDR.getObjectLoader();

      for (int i = 0; i < n; i++)
      {
         JDRCompleteObject object 
            = (JDRCompleteObject)objectLoader.load(jdr);

         if (object != null)
         {
            if (object instanceof JDRGroup)
            {
               // discard empty subgroups
               if (((JDRGroup)object).size() > 0)
               {
                  group.add(object);
               }
            }
            else
            {
               group.add(object);
            }
         }

         jdr.getMessageSystem().getPublisher().publishMessages(
            MessageInfo.createIncProgress());
      }

      jdr.getMessageSystem().getPublisher().publishMessages(
         MessageInfo.createProgress(maxProgress),
         MessageInfo.createSetProgress(progress));

      return group;
   }

}
