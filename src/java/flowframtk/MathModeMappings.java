// File          : MathModeMappings.java
// Purpose       : Math mode mappings of symbols
// Creation Date : 2014-04-25
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

/*
    Copyright (C) 2014-2025 Nicola L.C. Talbot

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/

package com.dickimawbooks.flowframtk;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;

import com.dickimawbooks.jdrresources.*;

public class MathModeMappings extends TeXMappings
{
   public MathModeMappings(JDRResources resources)
   {
      super(resources, resources.getMessage("symbol.mode.maths"));
   }

   public static MathModeMappings createDefaultMappings(JDRResources resources)
   {
      MathModeMappings mappings = new MathModeMappings(resources);

      mappings.put(0x0023, new TeXLookup("\\#", "none"));
      mappings.put(0x0025, new TeXLookup("\\%", "none"));
      mappings.put(0x00A3, new TeXLookup("\\pounds ", "none"));
      mappings.put(0x00A7, new TeXLookup("\\S ", "none"));
      mappings.put(0x00B6, new TeXLookup("\\P ", "none"));

      mappings.put(0x00B0, new TeXLookup("\\degree ", "gensymb"));
      mappings.put(0x00B5, new TeXLookup("\\micro ", "gensymb"));
      mappings.put(0x00B1, new TeXLookup("\\pm ", "none"));
      mappings.put(0x00B2, new TeXLookup("^2", "none"));
      mappings.put(0x00B3, new TeXLookup("^3", "none"));
      mappings.put(0x00B9, new TeXLookup("^1", "none"));
      mappings.put(0x00BC, new TeXLookup("\\frac{1}{4}", "none"));
      mappings.put(0x00BD, new TeXLookup("\\frac{1}{2}", "none"));
      mappings.put(0x00BE, new TeXLookup("\\frac{3}{4}", "none"));
      mappings.put(0x00F7, new TeXLookup("\\div ", "none"));

      mappings.put(0x2026, new TeXLookup("\\ldots ", "none"));

      mappings.put(0x2103, new TeXLookup("\\celsius ", "gensymb"));
      mappings.put(0x2126, new TeXLookup("\\ohm ", "gensymb"));

      mappings.put(0x2200, new TeXLookup("\\forall ", "none"));
      mappings.put(0x2201, new TeXLookup("\\complement ", "amssymb"));
      mappings.put(0x2202, new TeXLookup("\\partial ", "none"));
      mappings.put(0x2203, new TeXLookup("\\exists ", "none"));
      mappings.put(0x2204, new TeXLookup("\\nexists ", "amssymb"));
      mappings.put(0x2205, new TeXLookup("\\emptyset ", "none"));
      mappings.put(0x2206, new TeXLookup("\\triangle ", "none"));
      mappings.put(0x2207, new TeXLookup("\\nabla ", "none"));
      mappings.put(0x2208, new TeXLookup("\\in ", "none"));
      mappings.put(0x2209, new TeXLookup("\\notin ", "none"));
      mappings.put(0x220A, new TeXLookup("\\smallin ", "mathdesign"));
      mappings.put(0x220B, new TeXLookup("\\ni ", "none"));
      mappings.put(0x220C, new TeXLookup("\\notni ", "txfonts"));
      mappings.put(0x220D, new TeXLookup("\\smallowns ", "mathdesign"));
      mappings.put(0x220E, new TeXLookup("\\blacksquare ", "amssymb"));
      mappings.put(0x220F, new TeXLookup("\\prod ", "none"));
      mappings.put(0x2210, new TeXLookup("\\coprod ", "none"));
      mappings.put(0x2211, new TeXLookup("\\sum ", "none"));
      mappings.put(0x2212, new TeXLookup("-", "none"));
      mappings.put(0x2213, new TeXLookup("\\mp ", "none"));
      mappings.put(0x2214, new TeXLookup("\\dotplus ", "amssymb"));
      mappings.put(0x2216, new TeXLookup("\\setminus ", "none"));
      mappings.put(0x2217, new TeXLookup("\\ast ", "none"));
      mappings.put(0x2218, new TeXLookup("\\circ ", "none"));
      mappings.put(0x2219, new TeXLookup("\\bullet ", "none"));
      mappings.put(0x221A, new TeXLookup("\\surd ", "none"));
      mappings.put(0x221B, new TeXLookup("\\sqrt[3]{}", "none"));
      mappings.put(0x221C, new TeXLookup("\\sqrt[4]{}", "none"));
      mappings.put(0x221D, new TeXLookup("\\propto ", "none"));
      mappings.put(0x221E, new TeXLookup("\\infty ", "none"));
      mappings.put(0x221F, new TeXLookup("\\rightangle ", "mathdesign"));
      mappings.put(0x2220, new TeXLookup("\\angle ", "none"));
      mappings.put(0x2221, new TeXLookup("\\measuredangle ", "amssymb"));
      mappings.put(0x2222, new TeXLookup("\\sphericalangle ", "amssymb"));
      mappings.put(0x2223, new TeXLookup("\\divides ", "mathabx"));
      mappings.put(0x2224, new TeXLookup("\\notdivides ", "mathabx"));
      mappings.put(0x2225, new TeXLookup("\\parallel ", "MnSymbol"));
      mappings.put(0x2226, new TeXLookup("\\nparallel ", "MnSymbol"));
      mappings.put(0x2227, new TeXLookup("\\wedge ", "none"));
      mappings.put(0x2228, new TeXLookup("\\vee ", "none"));
      mappings.put(0x2229, new TeXLookup("\\cap ", "none"));
      mappings.put(0x222A, new TeXLookup("\\cup ", "none"));
      mappings.put(0x222B, new TeXLookup("\\int ", "none"));
      mappings.put(0x222C, new TeXLookup("\\iint ", "amsmath"));
      mappings.put(0x222D, new TeXLookup("\\iiint ", "amsmath"));
      mappings.put(0x222E, new TeXLookup("\\oint ", "none"));
      mappings.put(0x222F, new TeXLookup("\\oiint ", "txfonts"));
      mappings.put(0x222F, new TeXLookup("\\oiiint ", "txfonts"));
      mappings.put(0x2231, new TeXLookup("\\intclockwise ", "mathdesign"));
      mappings.put(0x2232, new TeXLookup("\\ointclockwise ", "txfonts"));
      mappings.put(0x2233, new TeXLookup("\\ointctrclockwise ", "txfonts"));
      mappings.put(0x2234, new TeXLookup("\\therefore ", "amssymb"));
      mappings.put(0x2235, new TeXLookup("\\because ", "amssymb"));
      mappings.put(0x2236, new TeXLookup("\\colon ", "none"));
      mappings.put(0x2237, new TeXLookup("\\squaredots ", "MnSymbol"));
      mappings.put(0x2238, new TeXLookup("\\dotminus ", "MnSymbol"));
      mappings.put(0x2239, new TeXLookup("\\eqcolon ", "txfonts"));
      mappings.put(0x2248, new TeXLookup("\\approx ", "none"));
      mappings.put(0x2251, new TeXLookup("\\Doteq ", "MnSymbol"));
      mappings.put(0x2253, new TeXLookup("\\risingdoteq ", "MnSymbol"));
      mappings.put(0x22C0, new TeXLookup("\\bigwedge ", "none"));
      mappings.put(0x22C1, new TeXLookup("\\bigvee ", "none"));
      mappings.put(0x22C2, new TeXLookup("\\bigcap ", "none"));
      mappings.put(0x22C3, new TeXLookup("\\bigcup ", "none"));

      mappings.put(0x2264, new TeXLookup("\\leq ", "none"));
      mappings.put(0x2265, new TeXLookup("\\geq ", "none"));
      mappings.put(0x226A, new TeXLookup("\\ll ", "none"));
      mappings.put(0x226B, new TeXLookup("\\gg ", "none"));
      mappings.put(0x2A3F, new TeXLookup("\\amalg ", "none"));
      mappings.put(0x224A, new TeXLookup("\\approxeq ", "amssymb"));
      mappings.put(0x224D, new TeXLookup("\\asymp ", "none"));
      mappings.put(0x223D, new TeXLookup("\\backsim ", "amssymb"));
      mappings.put(0x226C, new TeXLookup("\\between ", "amssymb"));
      mappings.put(0x25CB, new TeXLookup("\\bigcirc ", "none"));
      mappings.put(0x25BF, new TeXLookup("\\bigtriangledown ", "none"));
      mappings.put(0x25B5, new TeXLookup("\\bigtriangleup ", "none"));
      mappings.put(0x22C8, new TeXLookup("\\bowtie ", "none"));
      mappings.put(0x2022, new TeXLookup("\\bullet ", "none"));
      mappings.put(0x224F, new TeXLookup("\\bumpeq ", "amssymb"));
      mappings.put(0x2245, new TeXLookup("\\cong ", "none"));
      mappings.put(0x2020, new TeXLookup("\\dagger ", "none"));
      mappings.put(0x22A3, new TeXLookup("\\dashv ", "none"));
      mappings.put(0x2021, new TeXLookup("\\ddagger ", "none"));
      mappings.put(0x2B26, new TeXLookup("\\diamond ", "none"));
      mappings.put(0x2252, new TeXLookup("\\fallingdotseq ", "MnSymbol"));
      mappings.put(0x22B2, new TeXLookup("\\lhd ", "none"));
      mappings.put(0x2247, new TeXLookup("\\ncong ", "amssymb"));
      mappings.put(0x2280, new TeXLookup("\\nprec ", "amssymb"));
      mappings.put(0x22E0, new TeXLookup("\\npreceq ", "amssymb"));
      mappings.put(0x2281, new TeXLookup("\\nsucc ", "amssymb"));
      mappings.put(0x22E1, new TeXLookup("\\nsucceq ", "amssymb"));
      mappings.put(0x2299, new TeXLookup("\\odot ", "none"));
      mappings.put(0x2296, new TeXLookup("\\ominus ", "none"));
      mappings.put(0x2295, new TeXLookup("\\oplus ", "none"));
      mappings.put(0x2298, new TeXLookup("\\oslash ", "none"));
      mappings.put(0x2297, new TeXLookup("\\otimes ", "none"));
      mappings.put(0x22B3, new TeXLookup("\\rhd ", "none"));
      mappings.put(0x2293, new TeXLookup("\\sqcap ", "none"));
      mappings.put(0x2294, new TeXLookup("\\sqcup ", "none"));
      mappings.put(0x22C6, new TeXLookup("\\star ", "none"));
      mappings.put(0x00D7, new TeXLookup("\\times ", "none"));
      mappings.put(0x25C3, new TeXLookup("\\triangleleft ", "none"));
      mappings.put(0x25B9, new TeXLookup("\\triangleright ", "none"));
      mappings.put(0x22B4, new TeXLookup("\\unlhd ", "none"));
      mappings.put(0x22B5, new TeXLookup("\\unrhd ", "none"));
      mappings.put(0x228E, new TeXLookup("\\uplus ", "none"));
      mappings.put(0x2240, new TeXLookup("\\wr ", "none"));
      mappings.put(0x22A1, new TeXLookup("\\boxdot ", "amssymb"));
      mappings.put(0x229F, new TeXLookup("\\boxminus ", "amssymb"));
      mappings.put(0x229E, new TeXLookup("\\boxplus ", "amssymb"));
      mappings.put(0x22A0, new TeXLookup("\\boxtimes ", "amssymb"));
      mappings.put(0x22D2, new TeXLookup("\\Cap ", "amssymb"));
      mappings.put(0x22C5, new TeXLookup("\\cdot ", "none"));
      mappings.put(0x229B, new TeXLookup("\\circledast ", "amssymb"));
      mappings.put(0x229A, new TeXLookup("\\circledcirc ", "amssymb"));
      mappings.put(0x229D, new TeXLookup("\\circleddash ", "amssymb"));
      mappings.put(0x22D3, new TeXLookup("\\Cup ", "amssymb"));
      mappings.put(0x22CE, new TeXLookup("\\curlyvee ", "amssymb"));
      mappings.put(0x22CF, new TeXLookup("\\curlywedge ", "amssymb"));
      mappings.put(0x22C7, new TeXLookup("\\divideontimes ", "amssymb"));
      mappings.put(0x2250, new TeXLookup("\\doteq", "MnSymbol"));
      mappings.put(0x2A5E, new TeXLookup("\\doublebarwedge ", "amssymb"));
      mappings.put(0x2261, new TeXLookup("\\equiv ", "none"));
      mappings.put(0x22BA, new TeXLookup("\\intercal ", "amssymb"));
      mappings.put(0x2A1D, new TeXLookup("\\Join ", "amssymb"));
      mappings.put(0x22CB, new TeXLookup("\\leftthreetimes ", "amssymb"));
      mappings.put(0x22C9, new TeXLookup("\\ltimes ", "amssymb"));
      mappings.put(0x22A7, new TeXLookup("\\models ", "none"));
      mappings.put(0x22AC, new TeXLookup("\\nvdash ", "amssymb"));
      mappings.put(0x22AD, new TeXLookup("\\nvDash ", "amssymb"));
      mappings.put(0x22AF, new TeXLookup("\\nVDash ", "amssymb"));
      mappings.put(0x22A5, new TeXLookup("\\perp ", "none"));
      mappings.put(0x227A, new TeXLookup("\\prec ", "none"));
      mappings.put(0x227C, new TeXLookup("\\preceq ", "none"));
      mappings.put(0x22CC, new TeXLookup("\\rightthreetimes ", "amssymb"));
      mappings.put(0x22CA, new TeXLookup("\\rtimes ", "amssymb"));
      mappings.put(0x223C, new TeXLookup("\\sim ", "none"));
      mappings.put(0x2243, new TeXLookup("\\simeq ", "none"));
      mappings.put(0x29F5, new TeXLookup("\\smallsetminus ", "amssymb"));
      mappings.put(0x203F, new TeXLookup("\\smile ", "none"));// not sure about this one
      mappings.put(0x227B, new TeXLookup("\\succ ", "none"));
      mappings.put(0x227D, new TeXLookup("\\succeq ", "none"));
      mappings.put(0x2A61, new TeXLookup("\\veebar ", "amssymb"));
      mappings.put(0x29D6, new TeXLookup("\\udtimes ", "mathdesign"));
      mappings.put(0x22A2, new TeXLookup("\\vdash ", "none"));
      mappings.put(0x22A9, new TeXLookup("\\Vdash ", "amssymb"));
      mappings.put(0x22A8, new TeXLookup("\\vDash ", "amssymb"));
      mappings.put(0x22AA, new TeXLookup("\\Vvdash ", "amssymb"));
      mappings.put(0x2255, new TeXLookup("\\eqqcolon ", "txfonts"));

      mappings.put(0x1D6A4, new TeXLookup("\\imath ", "none"));
      mappings.put(0x1D6A5, new TeXLookup("\\jmath ", "none"));

      // Upright bold letters

      for (int c = 'A'; c <= 'Z'; c++)
      {
         mappings.put(0x1D400+(c-'A'),
           new TeXLookup(String.format("\\mathbf{%c}", c), "none"));
      }

      for (int c = 'a'; c <= 'z'; c++)
      {
         mappings.put(0x1D41A+(c-'a'), 
           new TeXLookup(String.format("\\mathbf{%c}", c), "none"));
      }

      // Italic letters (just convert them to Basic Latin equivalent

      for (int c = 'A'; c <= 'Z'; c++)
      {
         mappings.put(0x1D434+(c-'A'),
            new TeXLookup(String.format("%c",c), "none"));
      }

      for (int c = 'a'; c <= 'z'; c++)
      {
         if (c == 'h')
         {
            mappings.put(0x210E, new TeXLookup("h", "none"));
         }
         else
         {
            mappings.put(0x1D44E+(c-'a'), 
              new TeXLookup( String.format("%c", c), "none"));
         }
      }

      // Italic bold letters

      for (int c = 'A'; c <= 'Z'; c++)
      {
         mappings.put(0x1D468+(c-'A'), 
           new TeXLookup(String.format("\\boldsymbol{%c}", c), "amsmath"));
      }

      for (int c = 'a'; c <= 'z'; c++)
      {
         mappings.put(0x1D482+(c-'a'), 
           new TeXLookup(String.format("\\boldsymbol{%c}", c), "amsmath"));
      }

      // Calligraphic letters

      for (char c = 'A'; c <= 'Z'; c++)
      {
         int code;

         switch (c)
         {
            case 'B': code = 0x212C; break;
            case 'E': code = 0x2130; break;
            case 'F': code = 0x2131; break;
            case 'H': code = 0x210B; break;
            case 'I': code = 0x2110; break;
            case 'L': code = 0x2112; break;
            case 'M': code = 0x2133; break;
            case 'R': code = 0x211B; break;
            default: code = 0x1D49C+(c-'A');
         }

         mappings.put(code, 
           new TeXLookup(String.format("\\mathscr{%c}", c), "mathrsfs"));
      }

      // Fraktur letters

      for (char c = 'A'; c <= 'Z'; c++)
      {
         int code;

         switch (c)
         {
            case 'C': code = 0x212D; break;
            case 'H': code = 0x210C; break;
            case 'I': code = 0x2111; break;
            case 'R': code = 0x211C; break;
            case 'Z': code = 0x2128; break;
            default: code = 0x1D504+(c-'A');
         }

         mappings.put(code, 
           new TeXLookup("\\mathfrak{"+c+"}", "eufrak"));
      }

      for (char c = 'a'; c <= 'z'; c++)
      {
         mappings.put(0x1D51E+(c-'a'), 
           new TeXLookup("\\mathfrak{"+c+"}", "eufrak"));
      }

      // Blackboard bold

      for (char c = 'A'; c <= 'Z'; c++)
      {
         int code;

         switch (c)
         {
            case 'C' : code = 0x2102; break;
            case 'H' : code = 0x210D; break;
            case 'N' : code = 0x2115; break;
            case 'P' : code = 0x2119; break;
            case 'Q' : code = 0x211A; break;
            case 'R' : code = 0x211D; break;
            case 'Z' : code = 0x2124; break;
            default: code = 0x1D538+(c-'A');
         }

         mappings.put(code, 
           new TeXLookup("\\mathbb{"+c+"}", "amsfonts"));
      }

      for (char c = 'a'; c <= 'z'; c++)
      {
         mappings.put(0x1D552+(c-'a'), 
           new TeXLookup("\\mathbb{"+c+"}", "bbold"));
      }

      // Sans-serif letters

      for (char c = 'A'; c <= 'Z'; c++)
      {
         mappings.put(0x1D5A0+(c-'A'),
            new TeXLookup("\\mathsf{"+c+"}", "none"));
      }

      for (char c = 'a'; c <= 'z'; c++)
      {
         mappings.put(0x1D5BA+(c-'a'),
           new TeXLookup("\\mathsf{"+c+"}", "none"));
      }

      // Monospaced letters

      for (char c = 'A'; c <= 'Z'; c++)
      {
         mappings.put(0x1D670+(c-'A'), new TeXLookup("\\mathtt{"+c+"}", "none"));
      }

      for (char c = 'a'; c <= 'z'; c++)
      {
         mappings.put(0x1D68A+(c-'a'), new TeXLookup("\\mathtt{"+c+"}", "none"));
      }

      // Bold digits

      for (char c = '0'; c <= '9'; c++)
      {
         mappings.put(0x1D7CE+(c-'0'),
            new TeXLookup("\\mathbf{"+c+"}", "none"));
      }

      // Blackboard bold digits

      for (char c = '0'; c <= '9'; c++)
      {
         mappings.put(0x1D7D8+(c-'0'),
            new TeXLookup("\\mathbb{"+c+"}", "bbold"));
      }

      // Sans-serif digits

      for (char c = '0'; c <= '9'; c++)
      {
         mappings.put(0x1D7E2+(c-'0'),
            new TeXLookup("\\mathsf{"+c+"}", "bbold"));
      }

      // Monospaced digits

      for (char c = '0'; c <= '9'; c++)
      {
         mappings.put(0x1D7F6+(c-'0'),
            new TeXLookup("\\mathtt{"+c+"}", "bbold"));
      }


      // Greek letters (italic)

      for (int i = 0; i < GREEK_UPPER.length; i++)
      {
         mappings.put(0x1D6E2+i, new TeXLookup(GREEK_UPPER[i], "none"));
      }

      mappings.put(0x1D6FB, new TeXLookup("\\nabla ", "none"));

      for (int i = 0; i < GREEK_LOWER.length; i++)
      {
         mappings.put(0x1D6FC+i, new TeXLookup(GREEK_LOWER[i], "none"));
      }

      // Greek letters (bold italic)

      for (int i = 0; i < GREEK_UPPER.length; i++)
      {
         mappings.put(0x1D71C+i, new TeXLookup(
           "\\boldsymbol{"+GREEK_UPPER[i]+"}", "amsmath"));
      }

      mappings.put(0x1D735, new TeXLookup("\\boldsymbol{\\nabla}", "amsmath"));

      for (int i = 0; i < GREEK_LOWER.length; i++)
      {
         mappings.put(0x1D736+i, new TeXLookup(
            "\\boldsymbol{"+GREEK_LOWER[i]+"}", "amsmath"));
      }

      // Greek letters (bold upright)

      for (int i = 0; i < UPGREEK_UPPER.length; i++)
      {
         mappings.put(0x1D6A8+i, new TeXLookup(
           "\\boldsymbol{"+UPGREEK_UPPER[i]+"}", "amsmath,upgreek"));
      }

      mappings.put(0x1D735, new TeXLookup("\\boldsymbol{\\nabla}", "amsmath"));

      for (int i = 0; i < UPGREEK_LOWER.length; i++)
      {
         mappings.put(0x1D6C2+i, new TeXLookup(
            "\\boldsymbol{"+UPGREEK_LOWER[i]+"}", "amsmath,upgreek"));
      }

      mappings.put(0x1D7CA, new TeXLookup("\\digamma ", "amssymb"));

      mappings.put(0x2135, new TeXLookup("\\aleph ", "MnSymbol"));
      mappings.put(0x2136, new TeXLookup("\\beth ", "MnSymbol"));
      mappings.put(0x2137, new TeXLookup("\\gimel ", "MnSymbol"));
      mappings.put(0x2138, new TeXLookup("\\daleth ", "MnSymbol"));
      mappings.put(0x2113, new TeXLookup("\\ell ", "none"));
      mappings.put(0x210F, new TeXLookup("\\hbar ", "none"));
      mappings.put(0x2118, new TeXLookup("\\wp ", "none"));

      mappings.put(0x02070, new TeXLookup("^0", "none"));
      mappings.put(0x02071, new TeXLookup("^i", "none"));
      mappings.put(0x02074, new TeXLookup("^4", "none"));
      mappings.put(0x02075, new TeXLookup("^5", "none"));
      mappings.put(0x02076, new TeXLookup("^6", "none"));
      mappings.put(0x02077, new TeXLookup("^7", "none"));
      mappings.put(0x02078, new TeXLookup("^8", "none"));
      mappings.put(0x02079, new TeXLookup("^9", "none"));
      mappings.put(0x0207A, new TeXLookup("^+", "none"));
      mappings.put(0x0207B, new TeXLookup("^-", "none"));
      mappings.put(0x0207C, new TeXLookup("^=", "none"));
      mappings.put(0x0207D, new TeXLookup("^(", "none"));
      mappings.put(0x0207E, new TeXLookup("^)", "none"));
      mappings.put(0x0207F, new TeXLookup("^n", "none"));
      mappings.put(0x02080, new TeXLookup("_0", "none"));
      mappings.put(0x02081, new TeXLookup("_1", "none"));
      mappings.put(0x02082, new TeXLookup("_2", "none"));
      mappings.put(0x02083, new TeXLookup("_3", "none"));
      mappings.put(0x02084, new TeXLookup("_4", "none"));
      mappings.put(0x02085, new TeXLookup("_5", "none"));
      mappings.put(0x02086, new TeXLookup("_6", "none"));
      mappings.put(0x02087, new TeXLookup("_7", "none"));
      mappings.put(0x02088, new TeXLookup("_8", "none"));
      mappings.put(0x02089, new TeXLookup("_9", "none"));
      mappings.put(0x0208A, new TeXLookup("_+", "none"));
      mappings.put(0x0208B, new TeXLookup("_-", "none"));
      mappings.put(0x0208C, new TeXLookup("_=", "none"));
      mappings.put(0x0208D, new TeXLookup("_(", "none"));
      mappings.put(0x0208E, new TeXLookup("_)", "none"));
      mappings.put(0x02090, new TeXLookup("_a", "none"));
      mappings.put(0x02091, new TeXLookup("_e", "none"));
      mappings.put(0x02092, new TeXLookup("_o", "none"));
      mappings.put(0x02093, new TeXLookup("_x", "none"));
      mappings.put(0x02094, new TeXLookup("_{\\inve}", "wasysym"));
      mappings.put(0x02095, new TeXLookup("_h", "none"));
      mappings.put(0x02096, new TeXLookup("_k", "none"));
      mappings.put(0x02097, new TeXLookup("_l", "none"));
      mappings.put(0x02098, new TeXLookup("_m", "none"));
      mappings.put(0x02099, new TeXLookup("_n", "none"));
      mappings.put(0x0209A, new TeXLookup("_p", "none"));
      mappings.put(0x0209B, new TeXLookup("_s", "none"));
      mappings.put(0x0209C, new TeXLookup("_t", "none"));

      return mappings;
   }

   public static MathModeMappings load(JDRResources resources, File file)
      throws IOException
   {
      MathModeMappings mappings = new MathModeMappings(resources);

      mappings.read(file);

      return mappings;
   }

   private static final String[] GREEK_UPPER = new String[]
   {
      "A",
      "B",
      "\\Gamma ",
      "\\Delta ",
      "E",
      "Z",
      "H",
      "\\Theta ",
      "I",
      "K",
      "\\Lambda ",
      "M",
      "N",
      "\\Xi ",
      "O",
      "\\Pi ",
      "P",
      "\\Theta ",
      "\\Sigma ",
      "T",
      "Y",
      "\\Phi ",
      "X",
      "\\Psi ",
      "\\Omega "
   };

   private static final String[] GREEK_LOWER = new String[]
   {
      "\\alpha ",
      "\\beta ",
      "\\gamma ",
      "\\delta ",
      "\\varepsilon ",
      "\\zeta ",
      "\\eta ",
      "\\theta ",
      "\\iota ",
      "\\kappa ",
      "\\lambda ",
      "\\mu ",
      "\\nu ",
      "\\xi ",
      "o",
      "\\pi ",
      "\\rho ",
      "\\varsigma ",
      "\\sigma ",
      "\\tau ",
      "\\upsilon ",
      "\\varphi ",
      "\\chi ",
      "\\psi ",
      "\\omega ",
      "\\partial ",
      "\\epsilon ",
      "\\vartheta ",
      "\\kappa ",
      "\\varphi ",
      "\\varrho ",
      "\\varpi "
   };

   private static final String[] UPGREEK_UPPER = new String[]
   {
      "\\mathrm{A}",
      "\\mathrm{B}",
      "\\Upgamma ",
      "\\Updelta ",
      "\\mathrm{E}",
      "\\mathrm{Z}",
      "\\mathrm{H}",
      "\\Uptheta ",
      "\\mathrm{I}",
      "\\mathrm{K}",
      "\\Uplambda ",
      "\\mathrm{M}",
      "\\mathrm{N}",
      "\\Upxi ",
      "\\mathrm{O}",
      "\\Uppi ",
      "\\mathrm{P}",
      "\\Uptheta ",
      "\\Upsigma ",
      "\\mathrm{T}",
      "\\Upupsilon",
      "\\Upphi ",
      "\\mathrm{X}",
      "\\Uppsi ",
      "\\Upomega "
   };

   private static final String[] UPGREEK_LOWER = new String[]
   {
      "\\upalpha ",
      "\\upbeta ",
      "\\upgamma ",
      "\\updelta ",
      "\\upvarepsilon ",
      "\\upzeta ",
      "\\upeta ",
      "\\uptheta ",
      "\\upiota ",
      "\\upkappa ",
      "\\uplambda ",
      "\\upmu ",
      "\\upnu ",
      "\\upxi ",
      "o",
      "\\uppi ",
      "\\uprho ",
      "\\upvarsigma ",
      "\\upsigma ",
      "\\uptau ",
      "\\upupsilon ",
      "\\upvarphi ",
      "\\upchi ",
      "\\uppsi ",
      "\\upomega ",
      "\\partial ",
      "\\upepsilon ",
      "\\upvartheta ",
      "\\upkappa ",
      "\\upvarphi ",
      "\\upvarrho ",
      "\\upvarpi "
   };
}

