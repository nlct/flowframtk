// File          : JDRMessage.java
// Purpose       : Interface for messages
// Creation Date : 12th June 2008
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
package com.dickimawbooks.jdr.io;

import java.io.*;

import com.dickimawbooks.jdr.*;

/**
 * Interface for messages.
 * @author Nicola L C Talbot
 */

public interface JDRMessage extends JDRMessageDictionary
{
   public void postMessage(MessageInfo info);

   public void setVerbosity(int verbosity);
   public int getVerbosity();

   public default boolean isDebuggingOn() { return false; }

   public int getProgress();
   public int getMaxProgress();

   public boolean isIndeterminate();

   public MessageInfoPublisher getPublisher();
   public void setPublisher(MessageInfoPublisher publisher);
}
