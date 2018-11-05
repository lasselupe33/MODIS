public class Node {
    boolean isLeaf;
    Map<int, String> resources;


    static int maxRoutingNodes = 40;
    Map<Character, Node> routingTable;
    Node levelBelow;

    public Node(int port, int nodePort) {
        // Instantiate self

        // Call insertNode with self on given nodePort
    }

    public void getResource(hashCode code) {
        Node currNode = this;

        for (char c : code) {
            if (currNode.isLeaf()) {
                // Bail out
                break;
            }

            currNode = currNode.traverse(c);


        }

        // Find resource

    }

    public Node traverse(char c) {
        // Figure out where to traverse down

        // If node never responds
            // Broadcast that node has been killed
            // Use prev node to find a reference to the correct level below
    }


    // During insertion
    public void insertNode(Node n) {
        // If there's room for more in our table, insert here
            // Give routingTable and position
            // Broadcast new node

        // Else traverse down to next level
    }

    public void broadcastNewNode() {
        // Broadcast to own network that a new node has been inserted
        routingTable.forEach((node) => {
            node.storeNewNode(newNode, 4);
        });
    }

    public void storeNewNode(Node newNode, int index) {
        // Insert new node to routing table
    }
}
