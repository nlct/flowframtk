// File          : EPSASCII85DecodeFilter.java
// Purpose       : class representing an ASCII hex decode filter
// Date          : 25 May 2008
// Last Modified : 25 May 2008
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

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.font.*;
import java.util.*;
import java.util.regex.*;
import java.text.DateFormat;
import java.math.*;
import javax.imageio.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.EPS;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing an ASCII hex decode filter.
 * @author Nicola L C Talbot
 */
public class EPSASCII85DecodeFilter extends EPSFilter
{
   public EPSASCII85DecodeFilter(EPSObject epsdata)
      throws InvalidEPSObjectException
   {
      super(epsdata);
   }

   public EPSASCII85DecodeFilter(EPSObject epsdata, EPSDict epsdict)
      throws InvalidEPSObjectException
   {
      super(epsdata, epsdict);
   }

   protected EPSASCII85DecodeFilter()
   {
      super();
   }

   public String getName()
   {
      return "ASCII85Decode";
   }

   public boolean isEncoding()
   {
      return false;
   }

   private void decode(PrintWriter out, int[] code, int n)
   {
      int[] b = new int[4];

      int x = code[0]*52200625
            + code[1]*614125
            + code[2]*7225
            + code[3]*85
            + code[4];

      b[0] = x/0x1000000;

      x -= b[0]*0x1000000;

      b[1] = x/0x10000;

      x -= b[1]*0x10000;

      b[2] = x/0x100;

      x -= b[2]*0x100;

      b[3] = x;

      for (int i = 0; i < 4 && i < n; i++)
      {
         out.print((char)b[i]);
      }
   }

   public void decode(PrintWriter out, EPSStack stack)
      throws InvalidFormatException,IOException,
         NoninvertibleTransformException
   {
      EPSObject data = getData();

      if (data instanceof EPSString)
      {
         char[] chars = ((EPSString)data).getChars();

         int[] code = new int[5];

         int index = 0;

         for (int i = 0; i < chars.length; i++)
         {
            while (Character.isWhitespace(chars[i]))
            {
               i++;

               if (i == chars.length)
               {
                  break;
               }
            }

            code[index++] = chars[i];

            if (index == 5)
            {
               decode(out, code, 5);

               index = 0;
            }
         }

         if (index > 1 && index < 5)
         {
            decode(out, code, index);
         }
      }
      else if (data instanceof EPSFile)
      {
         EPSFile datafile = (EPSFile)data;
         boolean eod = false;

         int[] code = new int[5];

         while (!eod)
         {
            int index = 0;
            int next = -1;

            for (index = 0; index < 5; index++)
            {
               if (next == -1)
               {
                  code[index] = datafile.read();
               }
               else
               {
                  code[index] = next;
               }

               while (Character.isWhitespace(code[index]))
               {
                  code[index] = datafile.read();
               }

               if (code[index] == -1)
               {
                  eod = true;
                  break;
               }

               if (code[index] == 'z')
               {
                  for (int i = 0; i < 5; i++)
                  {
                     code[i] = 0;
                  }

                  index = 5;
                  break;
               }

               if (code[index] == '~')
               {
                  next = datafile.read();

                  if (next == '>')
                  {
                     index--;
                     eod = true;
                     break;
                  }
               }

               code[index] -= 33;
            }

            if (eod)
            {
               if (index < 1)
               {
                  break;
               }
               else
               {
                  for (int i = index+1; i < 5; i++)
                  {
                     code[i] = (int)'u'-33;
                  }
               }
            }

            decode(out, code, index);
         }
      }
      else if (data instanceof EPSProc)
      {
         EPSProc proc = (EPSProc)data;

         boolean eod = false;

         EPSString inputString = new EPSString(0);
         EPSBoolean flag = new EPSBoolean(true);

         int index = 0;
         int[] code = new int[5];

         while (!eod)
         {
            stack.add(inputString);
            stack.add(flag);
            stack.execProc(proc);

            EPSString string = stack.popEPSString();

            char[] chars = string.getChars();

            if (chars.length == 0)
            {
               eod = true;
               break;
            }

            for (int i = 0; i < chars.length; i++)
            {
               while (Character.isWhitespace(chars[i]))
               {
                  i++;

                  if (i == chars.length)
                  {
                     break;
                  }
               }

               code[index++] = chars[i];

               if (index == 5)
               {
                  decode(out, code, 5);

                  index = 0;
               }
            }
         }

         if (index > 1 && index < 5)
         {
            decode(out, code, index);
         }

         flag.set(false);
         stack.add(inputString);
         stack.add(flag);
         stack.execProc(proc);
      }
   }

   public Object clone()
   {
      EPSFilter filter = new EPSASCII85DecodeFilter();

      filter.makeEqual(this);

      return filter;
   }
}
