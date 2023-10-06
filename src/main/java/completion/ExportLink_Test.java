package completion;

import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExportLink_Test {
  public static void main(String[] args) throws Exception {
    File nodeFile = new File("C:\\Users\\ihyeon\\Downloads\\ConvertCoord2\\ConvertCoord.shp");
    File linkFile = new File("C:\\Users\\ihyeon\\Downloads\\ConvertCoord2\\ConvertCoord2.shp");

    FileDataStore store = FileDataStoreFinder.getDataStore(nodeFile);
    SimpleFeatureSource source = store.getFeatureSource();

    FileDataStore dataStore = FileDataStoreFinder.getDataStore(linkFile);
    SimpleFeatureSource featureSource = dataStore.getFeatureSource();

    Map<String, ArrayList<Long>> duplicateNode = new HashMap<>();

    try {
      SimpleFeatureCollection featureCollection = source.getFeatures();
      SimpleFeatureIterator iterator = featureCollection.features();

      SimpleFeatureCollection featureCollection1 = featureSource.getFeatures();
      SimpleFeatureIterator linkIterator = featureCollection1.features();

      while (iterator.hasNext()) {
        SimpleFeature feature = iterator.next(); // shp 파일 한 줄씩 읽어올거야
        String coordinateValue = feature.getDefaultGeometry().toString(); // 형상좌표가 Key
        Integer idxname = (Integer) feature.getAttribute("idxname");
        Integer nodeid = (Integer) feature.getAttribute("nodeid");
        Integer nodeattr = (Integer) feature.getAttribute("nodeattr");

        // idxname + nodeid = 고유값 생성
        String string = Integer.toString(idxname);
        String string1 = Integer.toString(nodeid);

        long nodeNumber = Long.parseLong(string + string1);

        Point point = (Point) feature.getAttribute(0);

        if (nodeattr == 7) { //
          if (duplicateNode.containsKey(coordinateValue)) { // 값을 갖고 있으면
            // duplicateNode 키 가져와서, 해당 값과 일치하는 nodeid를 add
            duplicateNode.get(coordinateValue).add(nodeNumber);
          } else {
            // 중복 없으면
            ArrayList<Long> nodeArr = new ArrayList<>();
            nodeArr.add(nodeNumber);
            duplicateNode.put(coordinateValue, nodeArr);
          }
        }
        // 여기서 for문으로 출력하면 누적 누적 누적
      }

      for (Map.Entry<String, ArrayList<Long>> entrySet : duplicateNode.entrySet()) {
        if (entrySet.getValue().size() == 2) {
          String coordinate = entrySet.getKey(); // 키 = 도곽의 좌표(포인트)
          ArrayList<Long> nodeId = entrySet.getValue(); // 값: 도과 노드id1, 노드id2
          System.out.println(nodeId);

          while (linkIterator.hasNext()) {
            SimpleFeature next = linkIterator.next();
            Integer idxname = (Integer) next.getAttribute("idxname");
            Integer stndid = (Integer) next.getAttribute("stndid");
            Integer edndid = (Integer) next.getAttribute("edndid");
            Integer length = (Integer) next.getAttribute("length");

            // idxname + nodeid = 고유값 생성
            String string = Integer.toString(idxname);
            String string1 = Integer.toString(stndid);
            String string2 = Integer.toString(edndid);

            long fromNode = Long.parseLong(string + string1);
            long toNode = Long.parseLong(string + string2);

            MultiLineString multiLineString = (MultiLineString) next.getDefaultGeometry();
            LineString lineString = (LineString)multiLineString.getGeometryN(0);
            // LINESTRING (898552.9223371984 1473777.0382949603, 898548.2987907089 1473785.2604029444) 이렇게 가져온다.
            lineString.getCoordinates(); // 이건 나중에 쓸 좌표값


            /**
             * if : 가져올 데이터를 걸러야 돼 -> 뭘로 거를래?
             * 일단!! 노드아이디랑 링크의 stid, endid 동일하면
             * 동일한 링크정보만 가져와
             * */

//            if (nodeId.contains(fromNode) || nodeId.contains(toNode)) {
//              lineString.
//
//
//            }


             /**
             * 좌표 하나에 라인이 2개잖아.
             * 어떤 라인의 start가 두 개일 수도 있고
             * 어떤 라인의 start, end 있을 수도 있고
             * 어떤 라인의 end, end 일 수도 있다.
             * 도곽의 nodeId 중 하나 == Link의 fromNode(start) 인 경우
             * ->mapValue
             *
             * map에 담은 nodeId == Link의 toNode(end) 인 경우
             * ->
             *
             * */

          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
