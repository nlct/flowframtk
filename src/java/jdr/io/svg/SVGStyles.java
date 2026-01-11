package com.dickimawbooks.jdr.io.svg;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Vector;

import com.dickimawbooks.jdr.JDRCompleteObject;
import com.dickimawbooks.jdr.exceptions.*;

public class SVGStyles extends HashMap<SVGStyles.Rule,SVGAttributeSet>
{
   public SVGStyles()
   {
      super();
   }

   protected void addRule(Rule rule, SVGAttributeSet atSet)
   {
      SVGAttributeSet as = get(rule);

      if (as != null)
      {
         as.addAttributes(atSet);
      }
      else
      {
         put(rule, (SVGAttributeSet)atSet.clone());
      }
   }

   public void putRule(String selectorElem, String selectorId,
     String selectorClass, SVGAttributeSet atSet)
   {
      put(new Rule(selectorElem, selectorId, selectorClass), atSet);
   }

   public void addRules(String selectorRules, SVGAttributeSet atSet)
   {
      String[] split = selectorRules.split(",");

      for (int i = 0; i < split.length; i++)
      {
         String selector = split[i].trim();

         // pseudo-classes and hierarchy not supported

         if (!selector.matches("[\\s>+:\\[\\]]"))
         {
            String selectorElem = selector;
            String selectorId = "";
            int idx = selector.indexOf('.');

            if (idx > -1)
            {
               selectorElem = selector.substring(0, idx);
               String clsString = selector.substring(idx+1);

               for (String s : clsString.split("\\."))
               {
                  addRule(new Rule(selectorElem, selectorId, s), atSet);
               }
            }
            else
            {
               idx = selector.indexOf('#');

               if (idx > -1)
               {
                  selectorElem = selector.substring(0, idx);
                  selectorId = selector.substring(idx+1);
               }

               addRule(new Rule(selectorElem, selectorId, ""), atSet);
            }
         }
      }
   }

   public SVGAttributeSet getFor(String elementName, String elementId, String[] elementClasses)
   {
      Vector<Rule> rules = null;

      Iterator<Rule> it = keySet().iterator();

      while (it.hasNext())
      {
         Rule rule = it.next();

         if (rule.matches(elementName, elementId, elementClasses))
         {
            if (rules == null)
            {
               rules = new Vector<Rule>();
            }

            rules.add(rule);
         }
      }

      if (rules == null) return null;

      rules.sort(null);

      SVGAttributeSet atSet = new SVGAttributeSet();

      for (Rule rule : rules)
      {
         atSet.addAttributes(get(rule));
      }

      return atSet;
   }

   public class Rule implements Cloneable, Comparable<Rule>
   {
      protected Rule() { }

      public Rule(String selectorElem, String selectorId, String selectorClass)
      {
         setRule(selectorElem, selectorId, selectorClass);
      }

      public void setRule(String selectorElem, String selectorId, String selectorClass)
      {
         if (selectorElem == null || selectorElem.equals("*"))
         {
            selectorElem = "";
         }

         if (selectorId == null)
         {
            selectorId = "";
         }

         if (selectorClass == null)
         {
            selectorClass = "";
         }

         this.selectorElement = selectorElem;
         this.selectorId = selectorId;
         this.selectorClass = selectorClass;
         this.selector = selectorElement;

         if (!selectorId.isEmpty())
         {
            this.selector += "#" + selectorId;
         }
         else if (!selectorClass.isEmpty())
         {
            this.selector += "." + selectorClass;
         }

         comparator = selectorElement + "!" + selectorId + "#" + selectorClass;
      }

      public String getSelector()
      {
         return selector;
      }

      public String getSelectorElement()
      {
         return selectorElement;
      }

      public String getSelectorId()
      {
         return selectorId;
      }

      public String getSelectorClass()
      {
         return selectorClass;
      }

      @Override
      public Object clone()
      {
         Rule rule = new Rule();
         rule.selector = selector;
         rule.selectorElement = selectorElement;
         rule.selectorId = selectorId;
         rule.selectorClass = selectorClass;
         rule.comparator = comparator;

         return rule;
      }

      @Override
      public boolean equals(Object other)
      {
         if (other == null || !(other instanceof Rule)) return false;

         Rule rule = (Rule)other;

         return selector.equals(rule.selector);
      }

      @Override
      public int compareTo(Rule other)
      {
         if (selector.equals(other.selector)) return 0;

         return comparator.compareTo(other.comparator);
      }

      public boolean matches(String element, String elemId, String[] elemClasses)
      {
         if (! (selectorElement.isEmpty() || selectorElement.equals(element)) )
         {
            return false;
         }

         if (elemId == null)
         {
            elemId = "";
         }

         if (!selectorId.isEmpty())
         {
            return selectorId.equals(elemId);
         }

         if (selectorClass.isEmpty()) return true;

         int n = (elemClasses == null ? 0 : elemClasses.length);

         for (int i = 0; i < n; i++)
         {
            if (selectorClass.equals(elemClasses[i]))
            {
               return true;
            }
         }

         return false;
      }

      private String selector="";
      private String selectorElement = "";
      private String selectorId = "";
      private String selectorClass = "";

      private String comparator="";
   }
}
