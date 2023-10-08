package output;

import org.geotools.api.data.*;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtractFunction_Node { // 좌표변환한 노드shp 파일 -> 병합대상 아닌 값만 추출
  public static void main(String[] args) throws Exception {
    System.setProperty("org.geotools.referencing.forceXY", "true");

    File file = new File("C:\\Users\\ihyeon\\Downloads\\ConvertCoord2\\ConvertCoord.shp");

    FileDataStore store = FileDataStoreFinder.getDataStore(file);
    SimpleFeatureSource source = store.getFeatureSource();
    ArrayList<SimpleFeature> ret = new ArrayList<>(); // 병합대상 아닌 노드정보

    try {
      SimpleFeatureCollection featureCollection = source.getFeatures();
      SimpleFeatureIterator iterator = featureCollection.features();

      // 노드.shp 파일 한 줄씩 읽기
      while (iterator.hasNext()) {
        SimpleFeature feature = iterator.next();
        Integer nodeattr = (Integer) feature.getAttribute("nodeattr");

        if (nodeattr != 7) {
          ret.add(feature);
        }
      }
      iterator.close();
      // 메소드 호출 -> 매개변수에 넘겨줄 값 작성
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    exportNodeShp(ret);
  }

  // 5. output shapefile
  static void exportNodeShp (ArrayList<SimpleFeature> ret) throws Exception { // 여긴 받을 준비
    // 새로운 shp 파일 저장경로 및 파일명 설정
    File newFile = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\output\\nodeOutput.shp");

    // SimpleFeatureType 생성 =  CSV 파일에서 읽어온 데이터를 설명, 속성 유형과 구조 정의
    final SimpleFeatureType TYPE =
        DataUtilities.createType( // DataUtilities 사용
            "NodeFile",
            "the_geom:Point:srid=5179,"
                + // <- the geometry attribute
                "idxname:Integer," + "nodeid:Integer," + "nodeattr:Integer"
        );
    System.out.println("TYPE:" + TYPE);

    List<SimpleFeature> features = new ArrayList<>();

    for (SimpleFeature nodeInfo : ret) {
      Point point1 = (Point) nodeInfo.getAttribute(0);
      Coordinate coordinate = point1.getCoordinate();

      Integer idxname = (Integer) nodeInfo.getAttribute("idxname");
      Integer nodeid = (Integer) nodeInfo.getAttribute("nodeid");
      Integer nodeattr = (Integer) nodeInfo.getAttribute("nodeattr");

      GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
      Point point = geometryFactory.createPoint(coordinate);

      SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
      featureBuilder.add(point);
      featureBuilder.add(idxname);
      featureBuilder.add(nodeid);
      featureBuilder.add(nodeattr);

      SimpleFeature feature = featureBuilder.buildFeature(null);
      features.add(feature);
    }

    ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

    Map<String, Serializable> params = new HashMap<>();
    params.put("url", newFile.toURI().toURL());
    params.put("create spatial index", Boolean.TRUE);

    DataStore newDataStore = dataStoreFactory.createNewDataStore(params);
    newDataStore.createSchema(TYPE); // TYPE -> 파일 내용 설명하는 템플릿으로 사용

    // ShapeFile에 feature data 작성
    Transaction transaction = new DefaultTransaction("create");

    String typeName = newDataStore.getTypeNames()[0];
    SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
    SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();

    System.out.println("SHAPE:" + SHAPE_TYPE);

    if (featureSource instanceof SimpleFeatureStore) {
      SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
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
}
