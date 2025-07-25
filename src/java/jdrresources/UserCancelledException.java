package com.dickimawbooks.jdrresources;

import com.dickimawbooks.texjavahelplib.MessageSystem;
import com.dickimawbooks.jdr.io.JDRMessageDictionary;

public class UserCancelledException extends InterruptedException
{
   public UserCancelledException(JDRMessageDictionary msgSys)
   {
      super(msgSys.getMessageWithFallback("process.aborted", "Process Aborted"));
   }

   public UserCancelledException(MessageSystem msgSys)
   {
      super(msgSys.getMessageWithFallback("process.aborted", "Process Aborted"));
   }

   public UserCancelledException(String msg)
   {
      super(msg);
   }
}
