package com.dickimawbooks.flowframtk;

import com.dickimawbooks.jdr.exceptions.InvalidFormatException;

public class InvalidConfigValueException extends InvalidFormatException
{
   public InvalidConfigValueException(String message, String value, int lineNum)
   {
      super(message+" (found '"+value+"')", lineNum);
   }

   public InvalidConfigValueException(String message, String value, int lineNum, Throwable cause)
   {
      super(message+" (found '"+value+"')", lineNum, cause);
   }
}
