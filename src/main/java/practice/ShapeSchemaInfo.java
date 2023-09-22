package practice;

import org.geotools.data.shapefile.ShapefileDataStore;

import java.io.File;
import java.net.URL;

public class ShapeSchemaInfo {
  public static void main(String[] args) {
    try {

      URL url = new File("C:\\Users\\ihyeon\\Desktop\\ctprvn_20230729\\ctprvn.shp").toURI().toURL();

      //ShapefileDataStore 접근하면 SimpleFeatureCollection 에서 스키마에 접근할 수 있다.
      ShapefileDataStore ds = new ShapefileDataStore(url);
//      SimpleFeatureCollection fc = ds.getFeatureSource((Name) ds.getFeatureSource()).getFeatures();

      //proj4 정의를 스트링으로 가져온다.
//      String projWkt = fc.getSchema().getCoordinateReferenceSystem().toWKT();

      //lookupEpsgCode는 CRS 좌표계 시스템을 epsg 코드를 찾아서 리턴한다.
//      int epgs= CRS.lookupEpsgCode(fc.getSchema().getCoordinateReferenceSystem(), true);

      //shape 파일 속성 인코딩 정보
      String charsetStr = ds.getCharset().toString();
//      System.out.println("EPSG:"+epgs); //
//      System.out.println("PROJ="+projWkt);
      System.out.println("Charset:"+charsetStr);


    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
