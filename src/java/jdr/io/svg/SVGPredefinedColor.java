package com.dickimawbooks.jdr.io.svg;

import java.awt.Color;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGPredefinedColor
{
   public SVGPredefinedColor(int r, int g, int b, String name)
   {
      this.red   = r;
      this.green = g;
      this.blue  = b;
      this.name  = name;
   }

   public String getName()
   {
      return name;
   }

   public Color getColor()
   {
      return new Color(red, green, blue);
   }

   public JDRColor getJDRColor(CanvasGraphics cg)
   {
      return new JDRColor(cg, getColor());
   }

   private int red, green, blue;
   private String name;
}

