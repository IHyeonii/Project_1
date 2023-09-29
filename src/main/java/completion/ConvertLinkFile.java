package completion;

import com.opencsv.CSVReader;
import org.apache.commons.lang3.math.NumberUtils;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.data.SimpleFeatureStore;
import org.geotools.api.data.Transaction;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvertLinkFile {
  public static void main(String[] args) throws Exception {

    File file = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\Link_test.csv");

    // 1. FeatureType 생성
    // SimpleFeatureType 생성 =  CSV 파일에서 읽어온 데이터를 설명, 속성 유형과 구조 정의
    final SimpleFeatureType TYPE =
        DataUtilities.createType( // DataUtilities 사용
            "LinkFile",
            "the_geom:MultiLineString:srid=4326,"
                + // <- the geometry attribute: LineString type
                "idxname:Integer,"
                + // <-  attribute
                "linkid:Integer," + "stndid:Integer," + "edndid:Integer," + "linkcate:Integer," + "roadcate:Integer,"
                + "roadno:String," + "lane:Integer," + "linkfacil:Integer," + "ks1:String," + "ks2:String,"
                + "oneway:Integer," + "speedlh:Integer," + "length:Integer"
        );
    System.out.println("TYPE:" + TYPE);

    // 2. Features 생성
    // 이제 CSV 파일을 읽고 각 줄을 파싱하여 데이터 추출하기
    List<SimpleFeature> features = new ArrayList<>();

    // 2-1. 새로운 포인트 생성을 위한 GeometryFactory 사용
    // GeometryFactory는 각 feature의 지오메트리 속성을 생성하는 데 사용
    GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    // 2-2. SimpleFeatureBuilder 사용하여 Features(SimpleFeature 객체) 생성
    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      CSVReader csvReader = new CSVReader(reader);

      /* First line of the data file is the header */
      String[] line = csvReader.readNext();
      System.out.println("Header: " + line);

      while ((line = csvReader.readNext()) != null) {
        Integer idxname = Integer.parseInt(line[0]);
        Integer linkid = Integer.parseInt(line[1]);
        Integer stndid = Integer.parseInt(line[2]);
        Integer edndid = Integer.parseInt(line[3]);
        Integer linkcate = Integer.parseInt(line[4]);
        Integer roadcate = Integer.parseInt(line[5]);
        String roadno = line[6];
        Integer lane = Integer.parseInt(line[7]);
        Integer linkfacil = Integer.parseInt(line[8]);
        String ks1 = line[9];
        String ks2 = line[10];
        String oneway = line[11];
        int oneway2 = NumberUtils.toInt(oneway);
        String speedlh = line[12];
        int speedlh2 =  NumberUtils.toInt(speedlh);
        String length = line[13];
        int length2 = NumberUtils.toInt(length);

        String lineString = line[14];
        int start = lineString.indexOf('(');
        int end = lineString.indexOf(')');
        String result = lineString.substring(start+1, end); // 괄호 안의 숫자만 출력

        // 쪼갰는데 담질못해 , 객체를 만들어서 담아라
        List<Coordinate> arr = new ArrayList<>();
        // 콤마 또는 공백을 구분자로 분리
        String[] value = result.split(",");

        /**
         * 이거이거
         * 왜 다 해놓고 이걸 못 하냐
         * */
        for (String number : value) {
          String[] strCoord = number.trim().split(" ");
          Coordinate coord = new Coordinate(Double.parseDouble(strCoord[0]), Double.parseDouble(strCoord[1]));
          arr.add(coord);
        }

        /**
         * 처음 코드
         * geometryFactory.createLineString(new Coordinate[arr.size()]);
         * arr.size()만큼 방을 만들어 놓고, 좌표값을 넣어주지 않아서 null 에러
         * createLineString 들어가보면, 매개변수를 배열로 받는게 확인 가능
         * */

        // LineString 생성
        Coordinate[] points = new Coordinate[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
          points[i] = arr.get(i);
        }
        LineString lineInfo = geometryFactory.createLineString(points);

        // featureBuilder 객체를 사용하여 point 객체랑 name, number -> feature에 추가
        featureBuilder.add(lineInfo);
        featureBuilder.add(idxname);
        featureBuilder.add(linkid);
        featureBuilder.add(stndid);
        featureBuilder.add(edndid);
        featureBuilder.add(linkcate);
        featureBuilder.add(roadcate);
        featureBuilder.add(roadno);
        featureBuilder.add(lane);
        featureBuilder.add(linkfacil);
        featureBuilder.add(ks1);
        featureBuilder.add(ks2);
        featureBuilder.add(oneway2);
        featureBuilder.add(speedlh2);
        featureBuilder.add(length2);

        // featureBuilder 사용하여 SimpleFeature 객체 생성 -> 여기에 모든 데이터가 포함 됨
        SimpleFeature feature = featureBuilder.buildFeature(null);

        // 생성한 feature를 features 리스트에 추가
        features.add(feature);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // 3. Create a shapefile From a FeatureCollection
    // getNewShapeFile 메서드 호츌
    File newFile = getNewShapeFile(file); // 읽어온 csv 파일 가져와서

    // 3-1.DataStoreFactory -> 원하는 공간 인덱스를 표시하는 매개변수와 함께 사용해야 함
    ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

    Map<String, Serializable> params = new HashMap<>();
    params.put("url", newFile.toURI().toURL());
    params.put("create spatial index", Boolean.TRUE);

    ShapefileDataStore newDataStore =
        (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

    // 3-2. ShapeFile 설정 위한 createSchema(SimpleFeatureType) 메서드 사용
    newDataStore.createSchema(TYPE); // TYPE -> 파일 내용 설명하는 템플릿으로 사용

    // 4. ShapeFile에 feature data 작성
    Transaction transaction = new DefaultTransaction("create");

    String typeName = newDataStore.getTypeNames()[0];
    SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
    SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();

    System.out.println("SHAPE:" + SHAPE_TYPE);

    if (featureSource instanceof SimpleFeatureStore) {
      SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
      /*
       * SimpleFeatureStore has a method to add features
       * SimpleFeatureCollection 개체를 사용하므로, ListFeatureCollection 사용 가능
       * class to wrap our list of features.
       */
      SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);
      featureStore.setTransaction(transaction);
      try {
        featureStore.addFeatures(collection);
        transaction.commit();
      } catch (Exception problem) {
        problem.printStackTrace();
        transaction.rollback();
      } finally {
        transaction.close();
      }
      System.exit(0); // success!
    } else {
      System.out.println(typeName + " does not support read/write access");
      System.exit(1);
    }
  }

  // 5. output shapefile
  private static File getNewShapeFile(File csvFile) { //getNewShapeFile 호출될 때 매개변수로 전달되는 파일
    File newFile = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\Link_test.shp");

    // 3. 만약 새로운 파일이 원본 CSV 파일과 동일한 경우 오류를 출력하고 프로그램을 종료합니다.
    if (newFile.equals(csvFile)) {
      System.out.println("Error: cannot replace " + csvFile);
      System.exit(0);
    }
    return newFile;
  }
}
