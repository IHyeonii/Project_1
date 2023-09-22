package practice;

public class NodeData { // CSV 파일에서 읽은 노드 정보를 이 클래스 변수에 담을거야
    private int id;
    private int nodeId;
    private int nodeAttribute;
    private String nodeName;

  // 생성자에 대해 생각해보자 ..
// 아래 코드가 살아있으면? ReadCSV의 생성자 매개변수에 값을 넘겨줘야 한다.
//    public practice.NodeData(int id, int nodeId, int nodeAttribute, String nodeName) {
//      this.id = id;
//      this.nodeId = nodeId;
//      this.nodeAttribute = nodeAttribute;
//      this.nodeName = nodeName;
//    }

    // 이 객체를 다른 클래스에서 값 넣어주려면 set 해줘야 돼 = builder
    public int getId() {
      return id;
    }
    public void setId(int id) {
      this.id = id;
    }
    public int getNodeId() {
      return nodeId;
    }
    public void setNodeId(int nodeId) {
      this.nodeId = nodeId;
    }
    public int getNodeAttribute() {
      return nodeAttribute;
    }
    public void setNodeAttribute(int nodeAttribute) {
      this.nodeAttribute = nodeAttribute;
    }
    public String getNodeName() {
      return nodeName;
    }
    public void setNodeName(String nodeName) {
      this.nodeName = nodeName;
    }

    @Override
    public String toString() {
      return "practice.NodeData{" +
          "id=" + id +
          ", nodeId=" + nodeId +
          ", nodeAttribute=" + nodeAttribute +
          ", nodeName='" + nodeName + '\'' +
          '}';
    }
}
