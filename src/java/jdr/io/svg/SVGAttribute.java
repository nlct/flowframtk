package com.dickimawbooks.jdr.io.svg;

import com.dickimawbooks.jdr.exceptions.*;

public interface SVGAttribute extends Cloneable
{
   public String getName();
   public Object getValue();
   public Object clone();
}
