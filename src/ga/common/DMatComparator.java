package ga.common;

import java.util.Comparator;

import org.opencv.core.DMatch;

/**
 * DMatchクラスのComparatorです。
 * distance順にソートします。
 * @author eda
 *
 */
public class DMatComparator implements Comparator<DMatch> {

  @Override
  public int compare(DMatch o1, DMatch o2) {
    if ( o1 == null ){
      if ( o2 != null){
        return -1;
      }
      return 0;
    }
    if ( o2 == null ) return 1;
    return (int) (o1.distance - o2.distance);
  }

}
