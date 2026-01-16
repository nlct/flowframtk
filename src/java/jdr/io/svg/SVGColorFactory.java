package com.dickimawbooks.jdr.io.svg;

import java.awt.Color;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGColorFactory
{
   public static JDRColor getPredefinedColor(SVGHandler handler, String name)
     throws SVGException
   {
      CanvasGraphics cg = handler.getCanvasGraphics();

      for (int i = 0; i < PREDEFINED_COLORS.length; i++)
      {
         if (PREDEFINED_COLORS[i].getName().equals(name))
         {
            return PREDEFINED_COLORS[i].getJDRColor(cg);
         }
      }

      throw new UnknownColorNameException(handler, name);
   }

   public static SVGPredefinedColor getPredefinedColor(int index)
   {
      return PREDEFINED_COLORS[index];
   }

   public static int getPredefinedCount()
   {
      return PREDEFINED_COLORS.length;
   }

   public static final SVGPredefinedColor ALICEBLUE = new SVGPredefinedColor(240, 248, 255, "aliceblue");
   public static final SVGPredefinedColor ANTIQUEWHITE = new SVGPredefinedColor(250, 235, 215, "antiquewhite");
   public static final SVGPredefinedColor AQUA = new SVGPredefinedColor(0, 255, 255, "aqua");
   public static final SVGPredefinedColor AQUAMARINE = new SVGPredefinedColor(127, 255, 212, "aquamarine");
   public static final SVGPredefinedColor AZURE = new SVGPredefinedColor(240, 255, 255, "azure");
   public static final SVGPredefinedColor BEIGE = new SVGPredefinedColor(245, 245, 220, "beige");
   public static final SVGPredefinedColor BISQUE = new SVGPredefinedColor(255, 228, 196, "bisque");
   public static final SVGPredefinedColor BLACK = new SVGPredefinedColor(0,0,0, "black");
   public static final SVGPredefinedColor BLANCHEDALMOND = new SVGPredefinedColor(255, 235, 205, "blanchedalmond");
   public static final SVGPredefinedColor BLUE = new SVGPredefinedColor(0, 0, 255, "blue");
   public static final SVGPredefinedColor BLUEVIOLET = new SVGPredefinedColor(138, 43, 226, "blueviolet");
   public static final SVGPredefinedColor BROWN = new SVGPredefinedColor(165, 42, 42, "brown");
   public static final SVGPredefinedColor BURLYWOOD = new SVGPredefinedColor(222, 184, 135, "burlywood");
   public static final SVGPredefinedColor CADETBLUE = new SVGPredefinedColor(95, 158, 160, "cadetblue");
   public static final SVGPredefinedColor CHARTREUSE = new SVGPredefinedColor(127, 255, 0, "chartreuse");
   public static final SVGPredefinedColor CHOCOLATE = new SVGPredefinedColor(210, 105, 30, "chocolate");
   public static final SVGPredefinedColor CORAL = new SVGPredefinedColor(255, 127, 80, "coral");
   public static final SVGPredefinedColor CORNFLOWERBLUE = new SVGPredefinedColor(100, 149, 237, "cornflowerblue");
   public static final SVGPredefinedColor CORNSILK = new SVGPredefinedColor(255, 248, 220, "cornsilk");
   public static final SVGPredefinedColor CRIMSON = new SVGPredefinedColor(220, 20, 60, "crimson");
   public static final SVGPredefinedColor CYAN = new SVGPredefinedColor(0, 255, 255, "cyan");
   public static final SVGPredefinedColor DARKBLUE = new SVGPredefinedColor(0, 0, 139, "darkblue");
   public static final SVGPredefinedColor DARKCYAN = new SVGPredefinedColor(0, 139, 139, "darkcyan");
   public static final SVGPredefinedColor DARKGOLDENROD = new SVGPredefinedColor(184, 134, 11, "darkgoldenrod");
   public static final SVGPredefinedColor DARKGRAY = new SVGPredefinedColor(169, 169, 169, "darkgray");
   public static final SVGPredefinedColor DARKGREEN = new SVGPredefinedColor(0, 100, 0, "darkgreen");
   public static final SVGPredefinedColor DARKGREY = new SVGPredefinedColor(169, 169, 169, "darkgrey");
   public static final SVGPredefinedColor DARKKHAKI = new SVGPredefinedColor(189, 183, 107, "darkkhaki");
   public static final SVGPredefinedColor DARKMAGENTA = new SVGPredefinedColor(139, 0, 139, "darkmagenta");
   public static final SVGPredefinedColor DARKOLIVEGREEN = new SVGPredefinedColor(85, 107, 47, "darkolivegreen");
   public static final SVGPredefinedColor DARKORANGE = new SVGPredefinedColor(255, 140, 0, "darkorange");
   public static final SVGPredefinedColor DARKORCHID = new SVGPredefinedColor(153, 50, 204, "darkorchid");
   public static final SVGPredefinedColor DARKRED = new SVGPredefinedColor(139, 0, 0, "darkred");
   public static final SVGPredefinedColor DARKSALMON = new SVGPredefinedColor(233, 150, 122, "darksalmon");
   public static final SVGPredefinedColor DARKSEAGREEN = new SVGPredefinedColor(143, 188, 143, "darkseagreen");
   public static final SVGPredefinedColor DARKSLATEBLUE = new SVGPredefinedColor(72, 61, 139, "darkslateblue");
   public static final SVGPredefinedColor DARKSLATEGRAY = new SVGPredefinedColor(47, 79, 79, "darkslategray");
   public static final SVGPredefinedColor DARKSLATEGREY = new SVGPredefinedColor(47, 79, 79, "darkslategrey");
   public static final SVGPredefinedColor DARKTURQUOISE = new SVGPredefinedColor(0, 206, 209, "darkturquoise");
   public static final SVGPredefinedColor DARKVIOLET = new SVGPredefinedColor(148, 0, 211, "darkviolet");
   public static final SVGPredefinedColor DEEPPINK = new SVGPredefinedColor(255, 20, 147, "deeppink");
   public static final SVGPredefinedColor DEEPSKYBLUE = new SVGPredefinedColor(0, 191, 255, "deepskyblue");
   public static final SVGPredefinedColor DIMGRAY = new SVGPredefinedColor(105, 105, 105, "dimgray");
   public static final SVGPredefinedColor DIMGREY = new SVGPredefinedColor(105, 105, 105, "dimgrey");
   public static final SVGPredefinedColor DODGERBLUE = new SVGPredefinedColor(30, 144, 255, "dodgerblue");
   public static final SVGPredefinedColor FIREBRICK = new SVGPredefinedColor(178,34,34, "firebrick");
   public static final SVGPredefinedColor FLORALWHITE = new SVGPredefinedColor(255, 250, 240, "floralwhite");
   public static final SVGPredefinedColor FORESTGREEN = new SVGPredefinedColor(34, 139, 34, "forestgreen");
   public static final SVGPredefinedColor FUCHSIA = new SVGPredefinedColor(255, 0, 255, "fuchsia");
   public static final SVGPredefinedColor GAINSBORO = new SVGPredefinedColor(220, 220, 220, "gainsboro");
   public static final SVGPredefinedColor GHOSTWHITE = new SVGPredefinedColor(248, 248, 255, "ghostwhite");
   public static final SVGPredefinedColor GOLD = new SVGPredefinedColor(255, 215, 0, "gold");
   public static final SVGPredefinedColor GOLDENROD = new SVGPredefinedColor(218, 165, 32, "goldenrod");
   public static final SVGPredefinedColor GRAY = new SVGPredefinedColor(128, 128, 128, "gray");
   public static final SVGPredefinedColor GREY = new SVGPredefinedColor(128, 128, 128, "grey");
   public static final SVGPredefinedColor GREEN = new SVGPredefinedColor(0, 128, 0, "green");
   public static final SVGPredefinedColor GREENYELLOW = new SVGPredefinedColor(173, 255, 47, "greenyellow");
   public static final SVGPredefinedColor HONEYDEW = new SVGPredefinedColor(240, 255, 240, "honeydew");
   public static final SVGPredefinedColor HOTPINK = new SVGPredefinedColor(255, 105, 180, "hotpink");
   public static final SVGPredefinedColor INDIANRED = new SVGPredefinedColor(205, 92, 92, "indianred");
   public static final SVGPredefinedColor INDIGO = new SVGPredefinedColor(75, 0, 13, "indigo");
   public static final SVGPredefinedColor IVORY = new SVGPredefinedColor(255, 255, 240, "ivory");
   public static final SVGPredefinedColor KHAKI = new SVGPredefinedColor(240, 230, 140, "khaki");
   public static final SVGPredefinedColor LAVENDER = new SVGPredefinedColor(230, 230, 250, "lavender");
   public static final SVGPredefinedColor LAVENDERBLUSH = new SVGPredefinedColor(255, 240, 245, "lavenderblush");
   public static final SVGPredefinedColor LAWNGREEN = new SVGPredefinedColor(124, 252, 0, "lawngreen");
   public static final SVGPredefinedColor LEMONCHIFFON = new SVGPredefinedColor(255, 250, 205, "lemonchiffon");
   public static final SVGPredefinedColor LIGHTBLUE = new SVGPredefinedColor(173, 216, 230, "lightblue");
   public static final SVGPredefinedColor LIGHTCORAL = new SVGPredefinedColor(240, 128, 128, "lightcoral");
   public static final SVGPredefinedColor LIGHTCYAN = new SVGPredefinedColor(224, 255, 255, "lightcyan");
   public static final SVGPredefinedColor LIGHTGOLDENRODYELLOW = new SVGPredefinedColor(250, 250, 210, "lightgoldenrodyellow");
   public static final SVGPredefinedColor LIGHTGRAY = new SVGPredefinedColor(211, 211, 211, "lightgray");
   public static final SVGPredefinedColor LIGHTGREEN = new SVGPredefinedColor(144, 238, 144, "lightgreen");
   public static final SVGPredefinedColor LIGHTGREY = new SVGPredefinedColor(211, 211, 211, "lightgrey");
   public static final SVGPredefinedColor LIGHTPINK = new SVGPredefinedColor(255, 182, 193, "lightpink");
   public static final SVGPredefinedColor LIGHTSALMON = new SVGPredefinedColor(255, 160, 122, "lightsalmon");
   public static final SVGPredefinedColor LIGHTSEAGREEN = new SVGPredefinedColor(32, 178, 170, "lightseagreen");
   public static final SVGPredefinedColor LIGHTSKYBLUE = new SVGPredefinedColor(135, 206, 250, "lightskyblue");
   public static final SVGPredefinedColor LIGHTSLATEGRAY = new SVGPredefinedColor(119, 136, 153, "lightslategray");
   public static final SVGPredefinedColor LIGHTSLATEGREY = new SVGPredefinedColor(119, 136, 153, "lightslategrey");
   public static final SVGPredefinedColor LIGHTSTEELBLUE = new SVGPredefinedColor(176, 196, 222, "lightsteelblue");
   public static final SVGPredefinedColor LIGHTYELLOW = new SVGPredefinedColor(255, 255, 224, "lightyellow");
   public static final SVGPredefinedColor LIME = new SVGPredefinedColor(0, 255, 0, "lime");
   public static final SVGPredefinedColor LIMEGREEN = new SVGPredefinedColor(50, 205, 50, "limegreen");
   public static final SVGPredefinedColor LINEN = new SVGPredefinedColor(250, 240, 230, "linen");
   public static final SVGPredefinedColor MAGENTA = new SVGPredefinedColor(255, 0, 255, "magenta");
   public static final SVGPredefinedColor MAROON = new SVGPredefinedColor(128, 0, 0, "maroon");
   public static final SVGPredefinedColor MEDIUMAQUAMARINE = new SVGPredefinedColor(102, 205, 170, "mediumaquamarine");
   public static final SVGPredefinedColor MEDIUMBLUE = new SVGPredefinedColor(0, 0, 205, "mediumblue");
   public static final SVGPredefinedColor MEDIUMORCHID = new SVGPredefinedColor(186, 85, 211, "mediumorchid");
   public static final SVGPredefinedColor MEDIUMPURPLE = new SVGPredefinedColor(147, 112, 219, "mediumpurple");
   public static final SVGPredefinedColor MEDIUMSEAGREEN = new SVGPredefinedColor(60, 179, 113, "mediumseagreen");
   public static final SVGPredefinedColor MEDIUMSLATEBLUE = new SVGPredefinedColor(123, 104, 238, "mediumslateblue");
   public static final SVGPredefinedColor MEDIUMSPRINGGREEN = new SVGPredefinedColor(0, 250, 154, "mediumspringgreen");
   public static final SVGPredefinedColor MEDIUMTURQUOISE = new SVGPredefinedColor(72, 209, 204, "mediumturquoise");
   public static final SVGPredefinedColor MEDIUMVIOLETRED = new SVGPredefinedColor(199, 21, 133, "mediumvioletred");
   public static final SVGPredefinedColor MIDNIGHTBLUE = new SVGPredefinedColor(25, 25, 112, "midnightblue");
   public static final SVGPredefinedColor MINTCREAM = new SVGPredefinedColor(245, 255, 250, "mintcream");
   public static final SVGPredefinedColor MISTYROSE = new SVGPredefinedColor(255, 228, 225, "mistyrose");
   public static final SVGPredefinedColor MOCCASIN = new SVGPredefinedColor(255, 228, 181, "moccasin");
   public static final SVGPredefinedColor NAVAJOWHITE = new SVGPredefinedColor(255, 222, 173, "navajowhite");
   public static final SVGPredefinedColor NAVY = new SVGPredefinedColor(0, 0, 128, "navy");
   public static final SVGPredefinedColor OLDLACE = new SVGPredefinedColor(253, 245, 230, "oldlace");
   public static final SVGPredefinedColor OLIVE = new SVGPredefinedColor(128, 128, 0, "olive");
   public static final SVGPredefinedColor OLIVEDRAB = new SVGPredefinedColor(107, 142, 35, "olivedrab");
   public static final SVGPredefinedColor ORANGE = new SVGPredefinedColor(255, 165, 0, "orange");
   public static final SVGPredefinedColor ORANGERED = new SVGPredefinedColor(255, 69, 0, "orangered");
   public static final SVGPredefinedColor ORCHID = new SVGPredefinedColor(218, 112, 214, "orchid");
   public static final SVGPredefinedColor PALEGOLDENROD = new SVGPredefinedColor(238, 232, 170, "palegoldenrod");
   public static final SVGPredefinedColor PALEGREEN = new SVGPredefinedColor(152, 251, 152, "palegreen");
   public static final SVGPredefinedColor PALETURQUOISE = new SVGPredefinedColor(175, 238, 238, "paleturquoise");
   public static final SVGPredefinedColor PALEVIOLETRED = new SVGPredefinedColor(219, 112, 147, "palevioletred");
   public static final SVGPredefinedColor PAPAYAWHIP = new SVGPredefinedColor(255, 239, 213, "papayawhip");
   public static final SVGPredefinedColor PEACHPUFF = new SVGPredefinedColor(255, 218, 185, "peachpuff");
   public static final SVGPredefinedColor PERU = new SVGPredefinedColor(205, 133, 63, "peru");
   public static final SVGPredefinedColor PINK = new SVGPredefinedColor(255, 192, 203, "pink");
   public static final SVGPredefinedColor PLUM = new SVGPredefinedColor(221, 160, 221, "plum");
   public static final SVGPredefinedColor POWDERBLUE = new SVGPredefinedColor(176, 224, 230, "powderblue");
   public static final SVGPredefinedColor PURPLE = new SVGPredefinedColor(128, 0, 128, "purple");
   public static final SVGPredefinedColor RED = new SVGPredefinedColor(255, 0, 0, "red");
   public static final SVGPredefinedColor ROSYBROWN = new SVGPredefinedColor(188, 143, 143, "rosybrown");
   public static final SVGPredefinedColor ROYALBLUE = new SVGPredefinedColor(65, 105, 225, "royalblue");
   public static final SVGPredefinedColor SADDLEBROWN = new SVGPredefinedColor(139, 69, 19, "saddlebrown");
   public static final SVGPredefinedColor SALMON = new SVGPredefinedColor(250, 128, 114, "salmon");
   public static final SVGPredefinedColor SANDYBROWN = new SVGPredefinedColor(244, 164, 96, "sandybrown");
   public static final SVGPredefinedColor SEAGREEN = new SVGPredefinedColor(46, 139, 87, "seagreen");
   public static final SVGPredefinedColor SEASHELL = new SVGPredefinedColor(255, 245, 238, "seashell");
   public static final SVGPredefinedColor SIENNA = new SVGPredefinedColor(160, 82, 45, "sienna");
   public static final SVGPredefinedColor SILVER = new SVGPredefinedColor(192, 192, 192, "silver");
   public static final SVGPredefinedColor SKYBLUE = new SVGPredefinedColor(135, 206, 235, "skyblue");
   public static final SVGPredefinedColor SLATEBLUE = new SVGPredefinedColor(106, 90, 205, "slateblue");
   public static final SVGPredefinedColor SLATEGRAY = new SVGPredefinedColor(112, 128, 144, "slategray");
   public static final SVGPredefinedColor SLATEGREY = new SVGPredefinedColor(112, 128, 144, "slategrey");
   public static final SVGPredefinedColor SNOW = new SVGPredefinedColor(255, 250, 250, "snow");
   public static final SVGPredefinedColor SPRINGGREEN = new SVGPredefinedColor(0, 255, 127, "springgreen");
   public static final SVGPredefinedColor STEELBLUE = new SVGPredefinedColor(70, 130, 180, "steelblue");
   public static final SVGPredefinedColor TAN = new SVGPredefinedColor(210, 180, 140, "tan");
   public static final SVGPredefinedColor TEAL = new SVGPredefinedColor(0, 128, 128, "teal");
   public static final SVGPredefinedColor THISTLE = new SVGPredefinedColor(216, 191, 216, "thistle");
   public static final SVGPredefinedColor TOMATO = new SVGPredefinedColor(255, 99, 71, "tomato");
   public static final SVGPredefinedColor TURQUOISE = new SVGPredefinedColor(64, 224, 208, "turquoise");
   public static final SVGPredefinedColor VIOLET = new SVGPredefinedColor(238, 130, 238, "violet");
   public static final SVGPredefinedColor WHEAT = new SVGPredefinedColor(245, 222, 179, "wheat");
   public static final SVGPredefinedColor WHITE = new SVGPredefinedColor(255, 255, 255, "white");
   public static final SVGPredefinedColor WHITESMOKE = new SVGPredefinedColor(245, 245, 245, "whitesmoke");
   public static final SVGPredefinedColor YELLOW = new SVGPredefinedColor(255, 255, 0, "yellow");
   public static final SVGPredefinedColor YELLOWGREEN = new SVGPredefinedColor(154, 205, 50, "yellowgreen");


   public static final SVGPredefinedColor[] PREDEFINED_COLORS =
   {
      ALICEBLUE ,
      ANTIQUEWHITE ,
      AQUA ,
      AQUAMARINE ,
      AZURE ,
      BEIGE ,
      BISQUE ,
      BLACK ,
      BLANCHEDALMOND ,
      BLUE ,
      BLUEVIOLET ,
      BROWN ,
      BURLYWOOD ,
      CADETBLUE ,
      CHARTREUSE ,
      CHOCOLATE ,
      CORAL ,
      CORNFLOWERBLUE ,
      CORNSILK ,
      CRIMSON ,
      CYAN ,
      DARKBLUE ,
      DARKCYAN ,
      DARKGOLDENROD ,
      DARKGRAY ,
      DARKGREEN ,
      DARKGREY ,
      DARKKHAKI ,
      DARKMAGENTA ,
      DARKOLIVEGREEN ,
      DARKORANGE ,
      DARKORCHID ,
      DARKRED ,
      DARKSALMON ,
      DARKSEAGREEN ,
      DARKSLATEBLUE ,
      DARKSLATEGRAY ,
      DARKSLATEGREY ,
      DARKTURQUOISE ,
      DARKVIOLET ,
      DEEPPINK ,
      DEEPSKYBLUE ,
      DIMGRAY ,
      DIMGREY ,
      DODGERBLUE ,
      FIREBRICK ,
      FLORALWHITE ,
      FORESTGREEN ,
      FUCHSIA ,
      GAINSBORO ,
      GHOSTWHITE ,
      GOLD ,
      GOLDENROD ,
      GRAY ,
      GREY ,
      GREEN ,
      GREENYELLOW ,
      HONEYDEW ,
      HOTPINK ,
      INDIANRED ,
      INDIGO ,
      IVORY ,
      KHAKI ,
      LAVENDER ,
      LAVENDERBLUSH ,
      LAWNGREEN ,
      LEMONCHIFFON ,
      LIGHTBLUE ,
      LIGHTCORAL ,
      LIGHTCYAN ,
      LIGHTGOLDENRODYELLOW ,
      LIGHTGRAY ,
      LIGHTGREEN ,
      LIGHTGREY ,
      LIGHTPINK ,
      LIGHTSALMON ,
      LIGHTSEAGREEN ,
      LIGHTSKYBLUE ,
      LIGHTSLATEGRAY ,
      LIGHTSLATEGREY ,
      LIGHTSTEELBLUE ,
      LIGHTYELLOW ,
      LIME ,
      LIMEGREEN ,
      LINEN ,
      MAGENTA ,
      MAROON ,
      MEDIUMAQUAMARINE ,
      MEDIUMBLUE ,
      MEDIUMORCHID ,
      MEDIUMPURPLE ,
      MEDIUMSEAGREEN ,
      MEDIUMSLATEBLUE ,
      MEDIUMSPRINGGREEN ,
      MEDIUMTURQUOISE ,
      MEDIUMVIOLETRED ,
      MIDNIGHTBLUE ,
      MINTCREAM ,
      MISTYROSE ,
      MOCCASIN ,
      NAVAJOWHITE ,
      NAVY ,
      OLDLACE ,
      OLIVE ,
      OLIVEDRAB ,
      ORANGE ,
      ORANGERED ,
      ORCHID ,
      PALEGOLDENROD ,
      PALEGREEN ,
      PALETURQUOISE ,
      PALEVIOLETRED ,
      PAPAYAWHIP ,
      PEACHPUFF ,
      PERU ,
      PINK ,
      PLUM ,
      POWDERBLUE ,
      PURPLE ,
      RED ,
      ROSYBROWN ,
      ROYALBLUE ,
      SADDLEBROWN ,
      SALMON ,
      SANDYBROWN ,
      SEAGREEN ,
      SEASHELL ,
      SIENNA ,
      SILVER ,
      SKYBLUE ,
      SLATEBLUE ,
      SLATEGRAY ,
      SLATEGREY ,
      SNOW ,
      SPRINGGREEN ,
      STEELBLUE ,
      TAN ,
      TEAL ,
      THISTLE ,
      TOMATO ,
      TURQUOISE ,
      VIOLET ,
      WHEAT ,
      WHITE ,
      WHITESMOKE ,
      YELLOW ,
      YELLOWGREEN 
   };
}
