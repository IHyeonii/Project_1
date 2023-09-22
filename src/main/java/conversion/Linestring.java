package conversion;

public class Linestring {
  // 텍스트 문자를 String 으로 읽어옴
  // 근데 필요한 값은 결국 숫자임 -> 그럼 ????

//  private double stlongitude = 0; // 왜 초기화 해야하지
  private double stLongitude; // 경도
  private double stLatitude; // 위도
  private double endLongitude; // 경도 끝값
  private double endLatitude; // 위도 끝값

  public double getStLongitude() {
    return stLongitude;
  }

  public void setStLongitude(double stLongitude) {
    this.stLongitude = stLongitude;
  }

  public double getStLatitude() {
    return stLatitude;
  }

  public void setStLatitude(double stLatitude) {
    this.stLatitude = stLatitude;
  }

  public double getEndLongitude() {
    return endLongitude;
  }

  public void setEndLongitude(double endLongitude) {
    this.endLongitude = endLongitude;
  }

  public double getEndLatitude() {
    return endLatitude;
  }

  public void setEndLatitude(double endLatitude) {
    this.endLatitude = endLatitude;
  }
  @Override
  public String toString() {
    return "Linestring{" +
        "stLongitude=" + stLongitude +
        ", stLatitude=" + stLatitude +
        ", endLongitude=" + endLongitude +
        ", endLatitude=" + endLatitude +
        '}';
  }
}
