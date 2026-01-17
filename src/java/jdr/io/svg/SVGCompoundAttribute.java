package com.dickimawbooks.jdr.io.svg;

import java.util.Enumeration;
import javax.swing.text.SimpleAttributeSet;

import com.dickimawbooks.jdr.JDRCompleteObject;
import com.dickimawbooks.jdr.exceptions.*;

public abstract class SVGCompoundAttribute extends SimpleAttributeSet
 implements SVGAttribute
{
   protected SVGCompoundAttribute(SVGHandler handler, String name)
   {
      this.handler = handler;
      this.name = name;
   }

   @Override
   public String getName()
   {
      return name;
   }

   public void addAttribute(SVGAttribute attr)
   {
      addAttribute(attr.getName(), attr);
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
      for (Enumeration en = getAttributeNames(); en.hasMoreElements(); )
      {
         SVGAttribute attr = (SVGAttribute)getAttribute(en.nextElement());
         attr.applyTo(element, object);
      }
   }

   @Override
   public Object getValue()
   {
      return valueString;
   }

   @Override
   public String getSourceValue()
   {
      return valueString;
   }

   @Override
   public void makeEqual(SVGAttribute other)
   {  
      if (other instanceof SVGCompoundAttribute)
      {  
         SVGCompoundAttribute compAttr = (SVGCompoundAttribute)other;

         compAttr.valueString = valueString;

         if (!isEmpty())
         {
            Enumeration en = getAttributeNames();
            removeAttributes(en);
         }

         for (Enumeration en = compAttr.getAttributeNames(); en.hasMoreElements(); )
         {
            SVGAttribute attr = (SVGAttribute)compAttr.getAttribute(en.nextElement());
            addAttribute(attr);
         }
      }
   }


   String name;
   String valueString;
   SVGHandler handler;
}
