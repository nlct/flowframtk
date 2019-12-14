// File          : EPSASCIIHexDecodeFilter.java
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
public class EPSASCIIHexDecodeFilter extends EPSFilter
{
   public EPSASCIIHexDecodeFilter(EPSObject epsdata)
      throws InvalidEPSObjectException
   {
      super(epsdata);
   }

   public EPSASCIIHexDecodeFilter(EPSObject epsdata, EPSDict epsdict)
      throws InvalidEPSObjectException
   {
      super(epsdata, epsdict);
   }

   protected EPSASCIIHexDecodeFilter()
   {
      super();
   }

   public String getName()
   {
      return "ASCIIHexDecode";
   }

   public boolean isEncoding()
   {
      return false;
   }

   public void decode(PrintWriter out, EPSStack stack)
      throws InvalidFormatException,IOException,
         NoninvertibleTransformException
   {
      EPSObject data = getData();

      if (data instanceof EPSString)
      {
         char[] chars = ((EPSString)data).getChars();
         char[] hex = new char[2];

         for (int i = 0; i < chars.length; i++)
         {
            while (!EPSString.isHex(chars[i]))
            {
               i++;

               if (i >= chars.length || chars[i] == '>')
               {
                  break;
               }

               if (!EPSString.isHex(chars[i])
                && !Character.isWhitespace(chars[i]))
               {
                  throw new IOException("Invalid hex character "
                     + chars[i]);
               }
            }

            if (i >= chars.length)
            {
               break;
            }

            hex[0] = chars[i];

            i++;

            while (!EPSString.isHex(chars[i]))
            {
               i++;

               if (i >= chars.length || chars[i] == '>')
               {
                  break;
               }

               if (!EPSString.isHex(chars[i])
                && !Character.isWhitespace(chars[i]))
            {
                  throw new IOException("Invalid hex character "
                     + chars[i]);
               }
            }

            hex[1] = (i == chars.length ? '0' : chars[i]);

            int value = Integer.parseInt(new String(hex), 16);

            out.print((char)value);
         }
      }
      else if (data instanceof EPSFile)
      {
         EPSFile datafile = (EPSFile)data;
         char[] hex = new char[2];
         boolean eod = false;

         while (!eod)
         {
            int c = datafile.read();

            if (c == '>' || c == -1)
            {
               eod = true;
               break;
            }

            while (!EPSString.isHex((char)c))
            {
               c = datafile.read();

               if (c == '>' || c == -1)
               {
                  eod = true;
                  break;
               }
            }

            if (eod)
            {
               break;
            }

            hex[0] = (char)c;

            c = datafile.read();

            if (c == '>' || c == -1)
            {
               eod = true;
               c = (int)'0';
            }

            while (!EPSString.isHex((char)c))
            {
               c = datafile.read();

               if (c == '>' || c == -1)
               {
                  eod = true;
                  break;
               }
            }

            if (eod)
            {
               c = (int)'0';
            }

            hex[1] = (char)c;

            int value = Integer.parseInt(new String(hex), 16);

            out.print((char)value);
         }
      }
      else if (data instanceof EPSProc)
      {
         EPSProc proc = (EPSProc)data;

         boolean eod = false;

         EPSString inputString = new EPSString(0);
         EPSBoolean flag = new EPSBoolean(true);

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

            char[] inputChars = new char[chars.length];

            char[] hex = new char[2];

            int j = 0;

            for (int i = 0; i < chars.length; i++)
            {
               while (!EPSString.isHex(chars[i]))
               {
                  i++;

                  if (i >= chars.length)
                  {
                     break;
                  }

                  if (chars[i] == '>')
                  {
                     eod = true;
                     break;
                  }

                  if (!EPSString.isHex(chars[i])
                   && !Character.isWhitespace(chars[i]))
                  {
                     throw new IOException("Invalid hex character "
                        + chars[i]);
                  }
               }

               if (i >= chars.length || eod)
               {
                  break;
               }

               hex[0] = chars[i];

               i++;

               while (!EPSString.isHex(chars[i]))
               {
                  i++;

                  if (i >= chars.length)
                  {
                     break;
                  }

                  if (chars[i] == '>')
                  {
                     eod = true;
                     break;
                  }

                  if (!EPSString.isHex(chars[i])
                   && !Character.isWhitespace(chars[i]))
                  {
                     throw new IOException("Invalid hex character "
                        + chars[i]);
                  }
               }

               hex[1] = (i >= chars.length ? '0' : chars[i]);

               int value = Integer.parseInt(new String(hex), 16);

               out.print((char)value);
               inputChars[j] = (char)value;
               j++;
            }

            inputString = new EPSString(j);

            for (int k = 0; k < j; k++)
            {
               inputString.put(k, inputChars[k]);
            }
         }

         flag.set(false);
         stack.add(inputString);
         stack.add(flag);
         stack.execProc(proc);
      }
   }

   public Object clone()
   {
      EPSFilter filter = new EPSASCIIHexDecodeFilter();

      filter.makeEqual(this);

      return filter;
   }
}
