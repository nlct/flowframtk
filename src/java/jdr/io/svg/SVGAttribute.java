package com.dickimawbooks.jdr.io.svg;

import com.dickimawbooks.jdr.JDRCompleteObject;
import com.dickimawbooks.jdr.exceptions.*;

public interface SVGAttribute extends Cloneable
{
   public String getName();
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object);
   public Object getValue();
   public Object clone();
   public String getSourceValue();
}
