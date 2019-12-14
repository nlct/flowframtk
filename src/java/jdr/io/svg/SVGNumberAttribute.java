package com.dickimawbooks.jdr.io.svg;

import com.dickimawbooks.jdr.exceptions.*;

public interface SVGNumberAttribute extends SVGAttribute
{
   public int intValue(SVGAbstractElement element);
   public double doubleValue(SVGAbstractElement element);
}
