package com.dickimawbooks.jdrresources;

import com.dickimawbooks.jdr.io.JDRMessageDictionary;

public class TimedOutException extends InterruptedException
{
   public TimedOutException(JDRResources resources, long maxTime)
   {
      this(resources.getMessage("error.timedout", maxTime));
   }

   public TimedOutException(String msg)
   {
      super(msg);
   }
}
