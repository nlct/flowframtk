package com.dickimawbooks.jdr.io.svg;

import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGDisplayStyleAttribute implements SVGNumberAttribute
{
   public SVGDisplayStyleAttribute(SVGHandler handler, String valueString)
     throws InvalidFormatException
   {
      this.handler = handler;
      parse(valueString);
   }


   public void parse(String valueString)
     throws InvalidFormatException
   {
      if (valueString == null || valueString.equals("inherit"))
      {
         style = null;
         return;
      }

      valueString = valueString.toLowerCase();

      if (valueString.equals("inline"))
      {
         style = Integer.valueOf(INLINE);
      }
      else if (valueString.equals("block"))
      {
         style = Integer.valueOf(BLOCK);
      }
      else if (valueString.equals("list-item"))
      {
         style = Integer.valueOf(LIST_ITEM);
      }
      else if (valueString.equals("run-in"))
      {
         style = Integer.valueOf(RUN_IN);
      }
      else if (valueString.equals("compact"))
      {
         style = Integer.valueOf(COMPACT);
      }
      else if (valueString.equals("marker"))
      {
         style = Integer.valueOf(MARKER);
      }
      else if (valueString.equals("table"))
      {
         style = Integer.valueOf(TABLE);
      }
      else if (valueString.equals("inline-table"))
      {
         style = Integer.valueOf(INLINE_TABLE);
      }
      else if (valueString.equals("table-row-group"))
      {
         style = Integer.valueOf(TABLE_ROW_GROUP);
      }
      else if (valueString.equals("table-header-group"))
      {
         style = Integer.valueOf(TABLE_HEADER_GROUP);
      }
      else if (valueString.equals("table-footer-group"))
      {
         style = Integer.valueOf(TABLE_FOOTER_GROUP);
      }
      else if (valueString.equals("table-row"))
      {
         style = Integer.valueOf(TABLE_ROW);
      }
      else if (valueString.equals("table-column-group"))
      {
         style = Integer.valueOf(TABLE_COLUMN_GROUP);
      }
      else if (valueString.equals("table-column"))
      {
         style = Integer.valueOf(TABLE_COLUMN);
      }
      else if (valueString.equals("table-cell"))
      {
         style = Integer.valueOf(TABLE_CELL);
      }
      else if (valueString.equals("table-caption"))
      {
         style = Integer.valueOf(TABLE_CAPTION);
      }
      else if (valueString.equals("none"))
      {
         style = Integer.valueOf(NONE);
      }
      else
      {
         throw new InvalidFormatException("Unknown display style '"+valueString+"'");
      }
   }

   public int getDisplayStyle()
   {
      return style.intValue();
   }

   public String getName()
   {
      return "display";
   }

   public Object getValue()
   {
      return style;
   }

   public int intValue(SVGAbstractElement element)
   {
      return style.intValue();
   }

   public double doubleValue(SVGAbstractElement element)
   {
      return (double)intValue(element);
   }

   public Object clone()
   {
      try
      {
         SVGDisplayStyleAttribute attr = new SVGDisplayStyleAttribute(handler, null);

         attr.makeEqual(this);

         return attr;
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   public void makeEqual(SVGDisplayStyleAttribute attr)
   {
      if (attr.style == null)
      {
         style = null;
      }
      else
      {
         style = Integer.valueOf(attr.style.intValue());
      }
   }

   private Integer style;
   SVGHandler handler;

   public static final int INLINE=0;
   public static final int BLOCK=1;
   public static final int LIST_ITEM=2;
   public static final int RUN_IN=3;
   public static final int COMPACT=4;
   public static final int MARKER=5;
   public static final int TABLE=6;
   public static final int INLINE_TABLE=7;
   public static final int TABLE_ROW_GROUP=8;
   public static final int TABLE_HEADER_GROUP=9;
   public static final int TABLE_FOOTER_GROUP=10;
   public static final int TABLE_ROW=11;
   public static final int TABLE_COLUMN_GROUP=12;
   public static final int TABLE_COLUMN=13;
   public static final int TABLE_CELL=14;
   public static final int TABLE_CAPTION=15;
   public static final int NONE=16;
}
