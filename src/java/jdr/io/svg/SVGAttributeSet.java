package com.dickimawbooks.jdr.io.svg;

import java.util.Enumeration;
import javax.swing.text.SimpleAttributeSet;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGAttributeSet extends SimpleAttributeSet implements SVGAttribute
{
   public SVGAttributeSet()
   {
      this((String)null);
   }

   public SVGAttributeSet(String attrId)
   {
      super();
      id = attrId;
   }

   public SVGAttributeSet(SVGAttributeSet source)
   {
      this(source, null);
   }

   public SVGAttributeSet(SVGAttributeSet source, String attrId)
   {
      super(source);
      id = attrId;
   }

   public String getId()
   {
      return id;
   }

   public void setId(String attrId)
   {
      id = attrId;
   }

   public void addAttribute(Object name, Object value)
   {
      if (value == null)
      {
         removeAttribute(name);
         return;
      }

      if (!(value instanceof SVGAttribute))
      {
         throw new IllegalArgumentException(
           "Can't add "+value.getClass() +" to attribute set");
      }

      super.addAttribute(name, value);
   }

   public void setName(String attrSetName)
   {
      name = attrSetName;
   }

   public String getName()
   {
      return name;
   }

   public Object getValue()
   {
      return this;
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
         String name = en.nextElement().toString();
         Object element = ((SVGAttribute)getAttribute(name)).clone();

         aSet.addAttribute(name, element);
      }

      aSet.name = name;
      aSet.id = id;

      return aSet;
   }

   private String id;
   private String name = null;
}
