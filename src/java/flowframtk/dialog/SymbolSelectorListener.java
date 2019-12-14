// File          : SymbolSelectorListener.java
// Description   : Interface for symbol selection listeners
// Creation Date : 6th February 2006
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
package com.dickimawbooks.flowframtk.dialog;

import java.awt.*;
import java.awt.font.*;

/**
 * Interface for symbol selection listeners.
 * @author Nicola L C Talbot
 */

public interface SymbolSelectorListener
{
   public void setSymbolText(String text);
   public Font getSymbolFont();
   public String getSymbolText();
   public RenderingHints getRenderingHints();
   public Font getSymbolButtonFont();
   public int getSymbolCaretPosition();
   public void setSymbolCaretPosition(int position);
   public void requestSymbolFocus();
}
