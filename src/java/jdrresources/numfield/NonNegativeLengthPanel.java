// File          : NonNegativeLengthPanel.java
// Description   : Panel for specifying a non-negative length
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
package com.dickimawbooks.jdrresources.numfield;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.JDRMessageDictionary;

import com.dickimawbooks.jdrresources.*;

/**
 * Panel for specifying a non-negative length.
 * @author Nicola L C Talbot
 */

public class NonNegativeLengthPanel extends LengthPanel
{
   public NonNegativeLengthPanel(JDRMessageDictionary msgSys, String label)
   {
      super(msgSys, label, new NonNegativeDoubleField(0.0));
   }

   public NonNegativeLengthPanel(JDRMessageDictionary msgSys, String label, char mnemonic)
   {
      super(msgSys, label, mnemonic, new NonNegativeDoubleField(0.0));
   }

   public NonNegativeLengthPanel(JDRMessageDictionary msgSys, String label, int mnemonic)
   {
      super(msgSys, label, mnemonic, new NonNegativeDoubleField(0.0));
   }

   public NonNegativeLengthPanel(JDRMessageDictionary msgSys)
   {
      super(msgSys, new NonNegativeDoubleField(0.0));
   }

   public NonNegativeLengthPanel(JDRMessageDictionary msgSys, String label, SamplePanel panel)
   {
      super(msgSys, label, panel, new NonNegativeDoubleField(0.0));
   }

   public NonNegativeLengthPanel(JDRMessageDictionary msgSys, String label, char mnemonic, SamplePanel panel)
   {
      super(msgSys, label, mnemonic, panel, new NonNegativeDoubleField(0.0));
   }

   public NonNegativeLengthPanel(JDRMessageDictionary msgSys, String label, int mnemonic, SamplePanel panel)
   {
      super(msgSys, label, mnemonic, panel, new NonNegativeDoubleField(0.0));
   }

   public NonNegativeLengthPanel(JDRMessageDictionary msgSys, SamplePanel panel)
   {
      super(msgSys, panel, new NonNegativeDoubleField(0.0));
   }
}
