package net.ubuntudaily.quickquest.commons.collections;


import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang3.Validate;

public final class Lists
{
  public static <E> ArrayList<E> newArrayList()
  {
    return new ArrayList();
  }

  public static <E> ArrayList<E> newArrayList(E[] elements)
  {
    Validate.notNull(elements);

    ArrayList list = new ArrayList(elements.length);
    Collections.addAll(list, elements);
    return list;
  }
}
