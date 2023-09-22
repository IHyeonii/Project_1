package conversion;

public class Linestring {
  // 텍스트 문자를 String으로 읽어옴 , 스펠링, 표기법, 초기화
  // 근데 필요한 값은 결국 숫자임 -> 그럼 ????

  private double stlongtitue = 0; // 경도
  private double endlongtitue; // 경도 끝값
  private double stlattitue; // 위도
  private double endlattitue; // 위도 끝값
  /**
   * @return the stlongtitue
   */
  public double getStlongtitue() {
    return stlongtitue;
  }
  /**
   * @param stlongtitue the stlongtitue to set
   */
  public void setStlongtitue(double stlongtitue) {
    this.stlongtitue = stlongtitue;
  }
  /**
   * @return the endlongtitue
   */
  public double getEndlongtitue() {
    return endlongtitue;
  }
  /**
   * @param endlongtitue the endlongtitue to set
   */
  public void setEndlongtitue(double endlongtitue) {
    this.endlongtitue = endlongtitue;
  }
  /**
   * @return the stlattitue
   */
  public double getStlattitue() {
    return stlattitue;
  }
  /**
   * @param stlattitue the stlattitue to set
   */
  public void setStlattitue(double stlattitue) {
    this.stlattitue = stlattitue;
  }
  /**
   * @return the endlattitue
   */
  public double getEndlattitue() {
    return endlattitue;
  }
  /**
   * @param endlattitue the endlattitue to set
   */
  public void setEndlattitue(double endlattitue) {
    this.endlattitue = endlattitue;
  }
  @Override
  public String toString() {
    return "ValueExtract [stlongtitue=" + stlongtitue + ", endlongtitue=" + endlongtitue + ", stlattitue="
        + stlattitue + ", endlattitue=" + endlattitue + "]";
  }
}
