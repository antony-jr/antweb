package org.calacademy.antweb.sort;

import java.util.*;
import org.calacademy.antweb.*;
import org.calacademy.antweb.util.*;

public class SortTaxaBySpecimens implements Comparator<Taxon> 
{
    public int compare(Taxon a, Taxon b) 
    { 
      try {
        int aC = a.getTaxonSet().getSpecimenCount();
        int bC = b.getTaxonSet().getSpecimenCount();

        int compareInt = 0;
        if (aC > bC) compareInt = -1;
        if (aC == bC) compareInt = 0;
        if (aC < bC) compareInt = 1;

        //A.log("compare() a:" + aC + " b:" + bC + " compareInt:" + compareInt);
        return compareInt;
      } catch (NullPointerException e) {
        A.log("compare() NPE a:" + a + " b:" + b);
      }
      return 0;
    } 
} 