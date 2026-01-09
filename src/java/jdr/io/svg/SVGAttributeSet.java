package com.dickimawbooks.jdr.io.svg;

import java.util.Enumeration;
import javax.swing.text.SimpleAttributeSet;

import com.dickimawbooks.jdr.JDRCompleteObject;
import com.dickimawbooks.jdr.exceptions.*;

public class SVGAttributeSet extends SimpleAttributeSet
{
   public SVGAttributeSet()
   {
      super();
   }

   public SVGAttributeSet(SVGAttributeSet source)
   {
      super(source);
   }

   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
      for (Enumeration en = getAttributeNames(); en.hasMoreElements(); )
      {
         String attrName = en.nextElement().toString();
         SVGAttribute attr = (SVGAttribute)getAttribute(attrName);

         attr.applyTo(element, object);
      }
   }

   public Object clone()
   {
      SVGAttributeSet resolvedParent 
         = (SVGAttributeSet)getResolveParent();

      SVGAttributeSet aSet;

      if (resolvedParent == null)
      {
         aSet = new SVGAttributeSet();
      }
      else
      {
         aSet = new SVGAttributeSet(resolvedParent);
      }

      for (Enumeration en = getAttributeNames(); en.hasMoreElements(); )
      {
         String attrName = en.nextElement().toString();
         Object element = ((SVGAttribute)getAttribute(attrName)).clone();

         aSet.addAttribute(attrName, element);
      }

      return aSet;
   }

}
