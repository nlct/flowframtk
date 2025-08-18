package com.dickimawbooks.jdrresources;

import com.dickimawbooks.jdr.io.JDRMessageDictionary;

public class TimedOutException extends InterruptedException
{
   public TimedOutException(JDRMessageDictionary msgDict, long maxTime)
   {
      this(msgDict.getMessageWithFallback("error.timedout",
       "Process timed-out (process time limit: {0}ms)", maxTime));
   }

   public TimedOutException(String msg)
   {
      super(msg);
   }
}
