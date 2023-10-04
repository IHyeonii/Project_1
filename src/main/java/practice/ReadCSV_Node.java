package practice;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ReadCSV_Node {
  public static void main(String[] args) throws Exception {
    File targetFile = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\Node_test.csv");
    BufferedReader reader = new BufferedReader(new FileReader(targetFile));
    CSVReader openCSVReader = new CSVReader(reader);

    // 읽어온 변수를 담을 datas 생성
    ArrayList<NodeData> datas = new ArrayList<>();

    try {
      String[] header  = openCSVReader.readNext(); //처음 필드명 빼고
      String[] str = openCSVReader.readNext();

      System.out.println("reader: " + reader.readLine());
      System.out.println("openCSVReader_idxname: " + str[0]);
      System.out.println("openCSVReader_nodeattr: " + str[2]);

      while ((str = openCSVReader.readNext()) != null) {
        NodeData nodeData = new NodeData();

        nodeData.setId(Integer.parseInt( str[0]));
        nodeData.setNodeId(Integer.parseInt( str[1]));
        nodeData.setNodeAttribute(Integer.parseInt( str[2]));
        nodeData.setNodeName(str[3]);

        datas.add(nodeData); // 한 줄 씩 읽어서 반복한 걸 만들어 둔 datas에 담아
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
