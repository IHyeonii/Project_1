package conversion;

import com.opencsv.CSVReader;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class GenerateShp_LineInfo {
  public static void main(String[] args) throws Exception {

    File targetFile = new File("C:\\Users\\ihyeon\\Desktop\\Qbic_1stTask\\Link_epsg4162_20220510.csv");
    BufferedReader reader = null;
    CSVReader openCSVReader = null;

    String[] str = null; // 한줄씩 읽어서 String 변수에 담아

    // 읽어온 변수를 담을 datas 생성
    ArrayList<Linestring> datas = new ArrayList<>();

    try {
      reader = new BufferedReader(new FileReader(targetFile));
      openCSVReader = new CSVReader(reader);

      String[] header  = openCSVReader.readNext(); //처음 필드명 빼고

      while ((str = openCSVReader.readNext()) != null) {
        Linestring linestring = new Linestring();

        String lsString = str[14];

        int start = lsString.indexOf('(');
        int end = lsString.indexOf(')');
        String result = lsString.substring(start+1, end); // 괄호 안의 숫자만 출력

        // 콤마 또는 공백을 구분자로 분리
        String[] sep = result.split(",| ");

        // 처음과 끝 값만 추출: 경도(long) 위도(latti) 순서
        linestring.setStLongitude(Double.parseDouble(sep[0])); // 경도
        linestring.setStLatitude(Double.parseDouble(sep[1])); // 위도
        linestring.setEndLongitude(Double.parseDouble(sep[sep.length-2])); //경도 끝값
        linestring.setEndLatitude(Double.parseDouble(sep[sep.length-1])); //위도 끝값

        datas.add(linestring);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if(openCSVReader != null) {
        try {
          openCSVReader.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if(reader != null) {
        try {
          reader.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

//    System.out.println(datas); // Linestring 객체에 값 담기 성공

    // SHP 파일을 저장할 디렉토리 및 파일 이름 설정
    GeometryFactory geometryFactory = new GeometryFactory();

    // Linestring을 위한 SimpleFeatureType
    SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
    typeBuilder.setName("Linestring");
    typeBuilder.setCRS(null); // 좌표계

    // Feature 타입에 속성 추가 -> dbf 파일 필드명
    typeBuilder.add("geometry", LineString.class);
    typeBuilder.add("stLongitude", Double.class);
    typeBuilder.add("stLatitude", Double.class);
    typeBuilder.add("endLongitude", Double.class);
    typeBuilder.add("endLatitude", Double.class);


    // Shapefile을 작성하기 위한 ShapefileDataStore 생성
    File shpFile = new File("C:\\Users\\ihyeon\\Desktop\\Qbic_1stTask\\lineInfo.shp");

    SimpleFeatureType simpleFeatureType = typeBuilder.buildFeatureType();
    ShapefileDataStore dataStore = new ShapefileDataStore(shpFile.toURI().toURL());
    dataStore.createSchema(simpleFeatureType);

    SimpleFeatureBuilder ddd = null;

    // Linestring 피처 보유할 SimpleFeatureBuilder 생성
//    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(linestringType);
//    List<SimpleFeature> features = new ArrayList<>();

//    for (Linestring linestring : datas) {
//      // LineString 지오메트리를 생성합니다.
//      Coordinate[] coordinates = new Coordinate[]{
//          new Coordinate(linestring.getStLongitude(), linestring.getStLatitude()),
//          new Coordinate(linestring.getEndLongitude(), linestring.getEndLatitude())
//      };
//      LineString lineString = geometryFactory.createLineString(coordinates);
//
//      featureBuilder.add(lineString);
//      featureBuilder.add(linestring.getStLongitude());
//      featureBuilder.add(linestring.getStLatitude());
//      featureBuilder.add(linestring.getEndLongitude());
//      featureBuilder.add(linestring.getEndLatitude());
//      SimpleFeature feature = featureBuilder.buildFeature(null);
//
//      features.add(feature);
//    }

    // 문제 - 읽기만 하고 쓰질 않아서 값이 담기지 않는다.
    // 여기선 shp 확장자 하나만 생성해야 하는데,


        SimpleFeatureCollection collection = dataStore.getFeatureSource(dataStore.getTypeNames()[0]).getFeatures();
//        collection;

    // 변경 내용 저장`
    dataStore.dispose();

  }
}
