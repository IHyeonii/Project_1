package practice;

import java.util.ArrayList;

public class ListPrac {
  public static void main(String[] args) {
   String line = "LINESTRING (124.82046648276068 37.58333328977391, 124.81370403257479 37.59274199863273, 124.80660903288769 37.602321378127925, 124.79626156494268 37.61699795570006)";

    int start = line.indexOf("(");
    int end = line.indexOf(")");
    String result = line.substring(start+1, end); // 괄호 안의 숫자만 출력
    System.out.println("result = "+ result);

    // 콤마로 구분
    String[] value = result.split(",");
    ArrayList<String> lineInfo = new ArrayList<>();

    for (String number : value) {
      String[] coors = number.trim().split(" ");
      for (String coor : coors) {
        lineInfo.add(coor);
        System.out.println("여기선? " + lineInfo);
      }
    }
    // 결과 출력
    System.out.println("lineInfo: " + lineInfo);
  }
}
