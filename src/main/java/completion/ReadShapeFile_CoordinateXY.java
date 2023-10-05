package completion;

import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ReadShapeFile_CoordinateXY {
  public static void main(String[] args) throws Exception {
    File nodeFile = new File("C:\\Users\\ihyeon\\Downloads\\ConvertCoord2\\ConvertCoord.shp");

    FileDataStore store = FileDataStoreFinder.getDataStore(nodeFile);
    SimpleFeatureSource source = store.getFeatureSource();

    Map<String, ArrayList<Integer>> duplicateNode = new HashMap<>();

    try {
      SimpleFeatureCollection featureCollection = source.getFeatures();
      SimpleFeatureIterator iterator = featureCollection.features();

      while (iterator.hasNext()) {
        SimpleFeature feature = iterator.next(); // shp 파일 한 줄씩 읽어올거야

        String coordinateValue = feature.getDefaultGeometry().toString(); // 형상좌표가 Key
//        System.out.println(coordinateValue); //POINT (899927.8233298252 1474094.5479148896)
        Integer idxname = (Integer) feature.getAttribute("idxname");
        Integer nodeid = (Integer) feature.getAttribute("nodeid");
        Integer nodeattr = (Integer) feature.getAttribute("nodeattr");

        int nodeNumber = idxname + nodeid; // Value

        if (nodeattr == 7) { //
          if (duplicateNode.containsKey(coordinateValue)) { // 값을 갖고 있으면
            // duplicateNode 키를 가져와서, 해당 값과 일치하는 nodeid를 add
            duplicateNode.get(coordinateValue).add(nodeNumber);
          } else { // 중복 없으면
            ArrayList<Integer> nodeArr = new ArrayList<>();
            nodeArr.add(nodeNumber);
            duplicateNode.put(coordinateValue, nodeArr);
          }
        }
        // 여기서 for문으로 출력하면 누적 누적 누적
        for (Entry<String, ArrayList<Integer>> entrySet : duplicateNode.entrySet()) {
          if (entrySet.getValue().size() == 2) {
            System.out.println(entrySet.getKey() + " : " + entrySet.getValue());
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
