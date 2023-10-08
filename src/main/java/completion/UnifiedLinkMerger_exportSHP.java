package completion;

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
import org.locationtech.jts.geom.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnifiedLinkMerger_exportSHP { // 병합정보 추출 완료, 이제 방향 정해서 통합링크 구성하기
  public static void main(String[] args) throws Exception {
    File nodeFile = new File("C:\\Users\\ihyeon\\Downloads\\ConvertCoord2\\ConvertCoord.shp");
    File linkFile = new File("C:\\Users\\ihyeon\\Downloads\\ConvertCoord2\\ConvertCoord2.shp");

    FileDataStore store = FileDataStoreFinder.getDataStore(nodeFile);
    SimpleFeatureSource source = store.getFeatureSource();

    FileDataStore dataStore = FileDataStoreFinder.getDataStore(linkFile);
    SimpleFeatureSource linkFeatureSource = dataStore.getFeatureSource();

    // 좌표 기준으로 노드 정보를 가지고 있음
    Map<String, ArrayList<Long>> duplicateNode = new HashMap<>(); // 중복 노드의 좌표, 노드id 2개 저장
    Map<Long, ArrayList<Object>> nodeInfo = new HashMap<>(); // 도곽 노드 정보를 가지고 있어야 함

    // 특정 노드에 대한 속성 정보를 value로 가지고 올 수 있음 key: nodeId, value: 노드에 대한 속성
    try {
      SimpleFeatureCollection featureCollection = source.getFeatures();
      SimpleFeatureIterator iterator = featureCollection.features();

      // 노드.shp 읽어서 필요한 속성정보 및 중복노드 추출
      while (iterator.hasNext()) {
        SimpleFeature feature = iterator.next();
        Integer idxname1 = (Integer) feature.getAttribute("idxname");
        Integer nodeid1 = (Integer) feature.getAttribute("nodeid");
        Integer nodeattr = (Integer) feature.getAttribute("nodeattr");
        String ndname = (String) feature.getAttribute("ndname");
        // 노드 id와 일치하는 속성정보를 -> 노드id: key, 속성정보: value 로

        // Point 좌표 추출
        Point point = (Point) feature.getAttribute(0);
        int x = (int) Math.round(point.getX());
        int y = (int) Math.round(point.getY());
        String key = x + "," + y;

        // idxname + nodeid = 고유값 생성
        String idxname = Integer.toString(idxname1);
        String nodeid = Integer.toString(nodeid1);
        long nodeNumber = Long.parseLong(idxname + nodeid);

        if (nodeattr == 7) { // nodeattr = 7; 도곽 노드 속성으로 데이터 검열해서 가져오기
          ArrayList<Object> arr = new ArrayList();
          arr.add(idxname1);
          arr.add(nodeid1);
          arr.add(nodeattr);
          arr.add(ndname);
          arr.add(point);

          nodeInfo.put(nodeNumber, arr);

          if (duplicateNode.containsKey(key)) { // 이미 키가 존재하는 경우
            duplicateNode.get(key).add(nodeNumber); // 기존 키에 노드 추가
          } else {
            // 중복 없으면 (새로운 키인 경우)
            ArrayList<Long> nodeArr = new ArrayList<>();
            nodeArr.add(nodeNumber);
            duplicateNode.put(key, nodeArr); // 새로운 키와 값을 생성
          }
        }
      }
      iterator.close();

      // 여기서 링크 읽어
      SimpleFeatureCollection linkFeatureCollection = linkFeatureSource.getFeatures();
      SimpleFeatureIterator linkIterator = linkFeatureCollection.features();

      Map<String, ArrayList<Long>> duplicateLink = new HashMap<>(); // 중복 노드의 좌표, 링크id 2개 저장
      Map<Long, ArrayList<Object>> linkInfo = new HashMap<>();

      // key: 좌표, value: 도곽 노드와 연결된 링크
      Map<String, ArrayList<SimpleFeature>> hashMap = new HashMap<>(); // 노드id와 일치하는 링크의 fromNode, toNode 저장
      while (linkIterator.hasNext()) { // 링크.shp 한 줄씩 읽기
        SimpleFeature next = linkIterator.next();

        Integer idxname = (Integer) next.getAttribute("idxname");
        Integer linkid = (Integer) next.getAttribute("linkid");
        Integer stndid = (Integer) next.getAttribute("stndid");
        Integer edndid = (Integer) next.getAttribute("edndid");
        Integer length = (Integer) next.getAttribute("length");
        MultiLineString multiLineString = (MultiLineString) next.getAttribute(0);
        LineString lineString = (LineString) multiLineString.getGeometryN(0);

        // idxname + nodeid = 고유값 생성
        String idxname1 = Integer.toString(idxname);
        String linkid1 = Integer.toString(linkid);
        String stndid1 = Integer.toString(stndid);
        String edndid1 = Integer.toString(edndid);

        long fromNode = Long.parseLong(idxname1 + stndid1);
        long toNode = Long.parseLong(idxname1 + edndid1);
        long linkId = Long.parseLong(idxname1 + linkid1);

        // 도곽 노드와 연결된 링크만 담아준다. -> 링크ID

        // 도곽일때의 링크 시작점, 끝점 추출
        // 노드 A 또는 B가 해당 링크의 시작점 또는 끝점인 경우

        if (nodeInfo.containsKey(fromNode)) {
          Point point = lineString.getStartPoint();
          int x = (int) Math.round(point.getX());
          int y = (int) Math.round(point.getY());
          String key = x + "," + y;

          // fromNode가 도곽일 때,
          // fromNode 좌표 기준으로 연결된 링크ID 정보를 저장한다.
          if (duplicateLink.containsKey(key)) { // 이미 키가 존재하는 경우
            duplicateLink.get(key).add(linkId); // 기존 키에 링크 추가
          } else {
            // 중복 없으면 (새로운 키인 경우)
            ArrayList<Long> linkArr = new ArrayList<>();
            linkArr.add(linkId);
            duplicateLink.put(key, linkArr); // 새로운 키와 값을 생성
          }
        } else if (nodeInfo.containsKey(toNode)) {
          Point point = lineString.getEndPoint();
          int x = (int) Math.round(point.getX());
          int y = (int) Math.round(point.getY());
          String key = x + "," + y;

          // toNode가 도곽일 때,
          // toNode 좌표 기준으로 연결된 링크ID 정보를 저장한다.
          if (duplicateLink.containsKey(key)) { // 이미 키가 존재하는 경우
            duplicateLink.get(key).add(linkId); // 기존 키에 링크 추가
          } else {
            // 중복 없으면 (새로운 키인 경우)
            ArrayList<Long> linkArr = new ArrayList<>();
            linkArr.add(linkId);
            duplicateLink.put(key, linkArr); // 새로운 키와 값을 생성
          }
        } else { // 도곽이 아닌 경우 -> 저장x
          continue;
        }

        // 도곽노드와 연결된 링크
        ArrayList<Object> arr = new ArrayList();
        arr.add(idxname);
        arr.add(linkid);
        arr.add(fromNode);
        arr.add(toNode);
        arr.add(length);
        arr.add(lineString);

        linkInfo.put(linkId, arr);
      }

      GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
      ArrayList<LineString> lineStringsInfo = new ArrayList<>();

      // 도곽 노드와 연결된 "링크를 병합"
      for (String key : duplicateLink.keySet()) {
        ArrayList<Long> arrNode = duplicateNode.get(key); // [41340000789, 41440000294] idxname + nodeid
        ArrayList<Long> arr = duplicateLink.get(key); // [413400002366, 41440000172] idxname + linkid
        if (arr.size() != 2) {
          continue;
        }

        long link1 = arr.get(0); // [413400002366, 41440000172] => 413400002366
        ArrayList<Object> arr1 = linkInfo.get(link1);
        LineString ls1 = (LineString) arr1.get(5); // 링크의 형상
        long link1snode = (long) arr1.get(2);
        long link1enode = (long) arr1.get(3);

        long link2 = arr.get(1);
        ArrayList<Object> arr2 = linkInfo.get(link2);
        LineString ls2 = (LineString) arr2.get(5); // 링크의 형상
        long link2snode = (long) arr2.get(2);
        long link2enode = (long) arr2.get(3);

        /**
         * 두개의 링크에 대해 형상정보를 가져왔으니까
         * 방향 비교해서 새로운 LineString 생성하기
         * */

        Long node1 = arrNode.get(0);
        Long node2 = arrNode.get(1);

        // 방향이 같은 경우
        // arrNode == 라인1의 end && 라인2의 start 이거나 : link1 -> link2
        if ((node1.equals(link1enode) && node2.equals(link2snode)) || (node2.equals(link1enode) && node1.equals(link2snode))) {
          Coordinate[] coordinates1 = ls1.getCoordinates(); // 링크1의 모든 절점
          Coordinate[] coordinates2 = ls2.getCoordinates();

          // 방을 만들어 줬다.
          Coordinate[] coordinates = new Coordinate[coordinates1.length + coordinates2.length - 1]; // 전체 절점의 갯수
          int i = 0;
          for (int j = 0; j < coordinates1.length; j++) { // 방에다 데이터를 넣어준다.
            coordinates[i++] = coordinates1[j]; // coordinate 방의 0번 부터 절점의 0번 넣어주면서 반복
          }
          for (int j = 1; j < coordinates2.length; j++) {
            coordinates[i++] = coordinates2[j];
          }
          LineString startLink1 = geometryFactory.createLineString(coordinates);
          lineStringsInfo.add(startLink1);
        }

        // arrNode == 라인1의 start 이거나 && 라인2의 end : link2 -> link1
        if ((node1.equals(link1snode) && node2.equals(link2enode)) || (node2.equals(link1snode) && node1.equals(link2enode))) {
          Coordinate[] coordinates1 = ls1.getCoordinates(); // 링크1의 모든 절점
          Coordinate[] coordinates2 = ls2.getCoordinates();

          Coordinate[] coordinates = new Coordinate[coordinates2.length + coordinates1.length - 1]; // 전체 절점의 갯수
          int i = 0;
          for (int j = 0; j < coordinates2.length; j++) { // 방에다 데이터를 넣어준다.
            coordinates[i++] = coordinates2[j]; // coordinate 방의 0번 부터 절점의 0번 넣어주면서 반복
          }

          for (int j = 1; j < coordinates1.length; j++) {
            coordinates[i++] = coordinates1[j];
          }
          LineString startLink2 = geometryFactory.createLineString(coordinates);
          lineStringsInfo.add(startLink2);
        }

        // 방향이 다른 경우
        // arrNode == 라인 1의 start && 라인2의 start
        if ((node1.equals(link1snode) && node2.equals(link2snode)) || (node2.equals(link1snode) && node1.equals(link2snode))) {
          LineString reverse = ls1.reverse();
          Coordinate[] reverseCoordinatesOfLink1 = reverse.getCoordinates();
          Coordinate[] ls2Coordinates = ls2.getCoordinates(); // 링크의 절점 다 가져와서

          // Coordinate 방을 만들어서 다 추가해줘야지
          Coordinate[] coordinates = new Coordinate[reverseCoordinatesOfLink1.length + ls2Coordinates.length - 1]; // 전체 절점의 갯수
          int i = 0;
          for (int j = 0; j < reverseCoordinatesOfLink1.length; j++) { // 방에다 데이터를 넣어준다.
            coordinates[i++] = reverseCoordinatesOfLink1[j]; // coordinate 방의 0번 부터 절점의 0번 넣어주면서 반복
          }
          for (int j = 1; j < ls2Coordinates.length; j++) {
            coordinates[i++] = ls2Coordinates[j];
          }
          LineString reverseLink1 = geometryFactory.createLineString(coordinates);
          lineStringsInfo.add(reverseLink1);
        }

        // arrNode == 라인 1의 end && 라인2의 end
        if ((node1.equals(link1enode) && node2.equals(link2enode)) || (node2.equals(link1enode) && node1.equals(link2enode))) {
          LineString reverse = ls1.reverse();
          Coordinate[] reverseCoordinatesOfLink1 = reverse.getCoordinates();
          Coordinate[] ls2Coordinates = ls2.getCoordinates(); // 링크의 절점 다 가져와서

          Coordinate[] coordinates = new Coordinate[reverseCoordinatesOfLink1.length + ls2Coordinates.length - 1]; // 전체 절점의 갯수
          int i = 0;
          for (int j = 0; j < reverseCoordinatesOfLink1.length; j++) { // 방에다 데이터를 넣어준다.
            coordinates[i++] = reverseCoordinatesOfLink1[j]; // coordinate 방의 0번 부터 절점의 0번 넣어주면서 반복
          }
          for (int j = 1; j < ls2Coordinates.length; j++) {
            coordinates[i++] = ls2Coordinates[j];
          }
          LineString reverseLink2 = geometryFactory.createLineString(coordinates);
          lineStringsInfo.add(reverseLink2);
        }

//      for (int i = 0; i < lineStringsInfo.size(); i++) {
//        System.out.println(lineStringsInfo.get(i));
      }
//      System.out.println("size: " + lineStringsInfo.size());
      } catch(IOException e){
        throw new RuntimeException(e);
      }
//      makeLink();
    }

    static void makeLink (Map < String, ArrayList < SimpleFeature >> hashMap, File linkFile) throws Exception {

      // 기존 노드. 링크 파일에서 필요한 정보 가져오고
      // 병합대상 가져와서
      // 새로운 shp 생성
      final SimpleFeatureType TYPE =
          DataUtilities.createType(
              "newLinkFile",
              "the_geom:LineString:srid=5179,"
                  + "name:String"
          );
      System.out.println("TYPE:" + TYPE);

      List<SimpleFeature> features = new ArrayList<>();

      for (Map.Entry<String, ArrayList<SimpleFeature>> entrySet : hashMap.entrySet()) {
        ArrayList<SimpleFeature> value = entrySet.getValue();
        SimpleFeature featureA = value.get(0); // 링크 1번
        SimpleFeature featureB = value.get(1); // 링크 2번

        // 링크 A의 좌표정보
        MultiLineString multiLineString = (MultiLineString) featureA.getAttribute(0);
        LineString lineString1 = (LineString) multiLineString.getGeometryN(0);
//      System.out.println(Arrays.toString(lineString1.getCoordinates())); // Coordinate는 하나만 출력

        // 링크 B도..
        MultiLineString multiLineString2 = (MultiLineString) featureB.getAttribute(0);
        LineString lineString2 = (LineString) multiLineString2.getGeometryN(0);
//      System.out.println(Arrays.toString(lineString1.getCoordinates()));

        //
        Point startPoint = lineString1.getStartPoint();
        Point endPoint = lineString2.getEndPoint();

        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

        Coordinate[] points = new Coordinate[2];
        points[0] = new Coordinate(startPoint.getX(), startPoint.getY());
        points[1] = new Coordinate(endPoint.getX(), endPoint.getY());
        LineString lineString = geometryFactory.createLineString(points);

        Integer length = (Integer) featureA.getAttribute("length");
//      System.out.println(length);
        // 양쪽좌표를 갖고, 두개를 합친다
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
        featureBuilder.add(lineString);
        featureBuilder.add("line");

        SimpleFeature feature = featureBuilder.buildFeature(null);
        features.add(feature); // 합친걸 여기다 add해
      }

      // 저장
      File newFile = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\output\\output1.shp");

      ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

      Map<String, Serializable> params = new HashMap<>();
      params.put("url", newFile.toURI().toURL());
      params.put("create spatial index", Boolean.TRUE);

      DataStore newDataStore = dataStoreFactory.createNewDataStore(params);

      newDataStore.createSchema(TYPE); // TYPE -> 파일 내용 설명하는 템플릿으로 사용

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

