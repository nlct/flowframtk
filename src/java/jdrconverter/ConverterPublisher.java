/*
    Copyright (C) 2025 Nicola L.C. Talbot
    www.dickimaw-books.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/

package com.dickimawbooks.jdrconverter;

import com.dickimawbooks.jdr.io.JDRDefaultMessage;

public class ConverterPublisher extends JDRDefaultMessage
{
   public ConverterPublisher(JDRConverter converter)
   {
      this.converter = converter;
   }

   @Override
   public String getMessageWithFallback(String tag, String altFormat,
     Object... values)
   {  
      return converter.getMessageWithFallback(tag, altFormat, values);
   }

   @Override
   public boolean isDebuggingOn() 
   {
      return debugMode;
   }

   public void setDebugMode(boolean on)
   {
      debugMode = on;
   }

   @Override
   public String getApplicationName()
   {
      return converter.getApplicationName();
   }

   @Override
   public String getApplicationVersion()
   {
      return converter.getApplicationVersion();
   }

   JDRConverter converter;
   boolean debugMode=false;
}
