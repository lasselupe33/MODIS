import Messages.*;
import Models.SimpleNode;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class Node {
    // Contain basic information about self, in order to be able to filter self out of own routingTable
    private SimpleNode self;

    // Contains a collection of all resources that the current node contains
    private HashMap<Integer, String> resources;

    // Contains a routingTable that contains all other nodes in the current layer
    private ArrayList<SimpleNode> routingTable = new ArrayList<>();

    // Specifies the location to the current node, e.g. [1, 2, 3, 7] would map to a node on the fourth layer
    private ArrayList<Integer> prevLocations = new ArrayList<>();

    // Specifies the index of the next node in the current routing table that should propagate the next incoming node
    // downwards if necessary
    private int nextNodeIndex = 0;

    // Contains a reference to a single node existing in the level below
    private SimpleNode levelBelow;

    // Contains a reference to a single node existing in the level above
    private SimpleNode levelAbove;

    /** Constructor that'll be used to generate a node without linking it to any networks */
    public Node(int port) {
        setupNode(port);

        // Add self to initial routing table
        routingTable.add(self);
    }

    /** Constructor that'll connect a node to a given network on localhost via a node port that exists in the network */
    public Node(int port, int targetPort) {
        try {
            setupNode(port);
            connectToNetwork(port, targetPort, InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /** Construct that'll connect a node to a given network via a node ip/port-pair that exists in the given network */
    public Node(int port, int targetPort, String targetIp) {
        try {
            setupNode(port);
            connectToNetwork(port, targetPort, InetAddress.getByName(targetIp));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /** Internal helper that sets up core functionality of a node */
    private void setupNode(int port) {
        try {
            // Store information about self
            self = new SimpleNode(InetAddress.getLocalHost(), port);

            // Begin listening to incoming connections at specified port
            listen(port).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Internal helper that should be called once a new node should be added to the network */
    private void connectToNetwork(int port, int targetPort, InetAddress targetIp) {
        try {
            NewNodeMsg msg = new NewNodeMsg(new SimpleNode(InetAddress.getLocalHost(), port), new ArrayList<>());
            sendMessage(msg, new SimpleNode(targetIp, targetPort));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Internal helper that'll continually listen for messages send to the node and invoke appropriate methods based on
     * messages received
     */
    private Thread listen(int port) {
        return new Thread(() -> {
            try {
                ServerSocket inSocket = new ServerSocket(port);

                while (true) {
                    // Setup socket and accept incoming messages
                    Socket connectionSocket = inSocket.accept();
                    ObjectInputStream input = new ObjectInputStream(connectionSocket.getInputStream());
                    Object receivedObj = input.readObject();

                    // Determine what should happen based on received message
                    if (receivedObj instanceof NewNodeMsg)
                    {
                        NewNodeMsg msg = (NewNodeMsg) receivedObj;
                        insertNode(msg);
                    }
                    else if (receivedObj instanceof NewSubNodeMsg)
                    {
                        NewSubNodeMsg msg = (NewSubNodeMsg) receivedObj;
                        insertSubNode(msg.node);
                    }
                    else if (receivedObj instanceof UpdateRoutingTableMsg)
                    {
                        UpdateRoutingTableMsg msg = (UpdateRoutingTableMsg) receivedObj;
                        updateRoutingTable(msg.index, msg.value);
                    }
                    else if (receivedObj instanceof SetNewNodeInformationMsg)
                    {
                        SetNewNodeInformationMsg msg = (SetNewNodeInformationMsg) receivedObj;
                        setNodeInformation(msg);
                    }
                    else if (receivedObj instanceof UpdateCurrentPositionMsg)
                    {
                        UpdateCurrentPositionMsg msg = (UpdateCurrentPositionMsg) receivedObj;
                        nextNodeIndex = msg.newPos;
                    }
                    else if (receivedObj instanceof PutMsg)
                    {
                        PutMsg msg = (PutMsg) receivedObj;
                        // call method here
                    }
                    else if (receivedObj instanceof GetMsg)
                    {
                        GetMsg msg = (GetMsg) receivedObj;
                        // call method here
                    }

                    connectionSocket.close();
                }
            } catch (IOException e) {
                System.out.println("Failed to bind node to " + self.ip + ":" + self.port);
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    /** Internal helper to be called while inserting a new node to the network */
    private void insertNode(NewNodeMsg newNodeMsg) {
        // If there's room for more in our table, insert here
        if (routingTable.size() < Utils.charMapping.size()) {
            routingTable.add(newNodeMsg.node);

            // Get the index of the newly inserted node
            int newIndex = routingTable.size() - 1;

            // Broadcast the newly added node to all nodes in routingTable
            UpdateRoutingTableMsg msg = new UpdateRoutingTableMsg(newIndex, newNodeMsg.node);
            broadcast(msg);

            // Update newly inserted node's routingTable to match current routingTable
            sendMessage(new SetNewNodeInformationMsg(routingTable, prevLocations), newNodeMsg.node);

            // Get recources from neighbour
            requestResources(newIndex);
        } else {
            // Else traverse down to next level
            SimpleNode nodeAtCurrPos = routingTable.get(nextNodeIndex);

            // Ensure nextNodeIndex points to next node in current routingTable
            nextNodeIndex = (nextNodeIndex + 1) % Utils.charMapping.size();

            // Broadcast currPos
            UpdateCurrentPositionMsg updateMsg = new UpdateCurrentPositionMsg(nextNodeIndex);
            broadcast(updateMsg);

            // Propagate newNode message down to the next level
            sendMessage(new NewSubNodeMsg(newNodeMsg.node), nodeAtCurrPos);
        }
    }

    /** Internal helper that requests recources from the nodes left neighbour*/
    private void requestResources(int index) {
        SimpleNode leftNeighbour = routingTable.get(index-1);

        RequestResourcesMsg msg = new RequestResourcesMsg(self);

        sendMessage(msg, leftNeighbour);
    }

    /** Internal helper to be called once a node that is currently being inserted should traverse down to the next level */
    private void insertSubNode(SimpleNode subNode) {
        // If there doesn't exist a level below the specified node, create it now
        if (this.levelBelow == null) {
            this.levelBelow = subNode;

            // Update newly inserted node's routingTable to match current routingTable
            ArrayList<SimpleNode> subNodeRoutingTable = new ArrayList<>();
            subNodeRoutingTable.add(subNode);
            sendMessage(new SetNewNodeInformationMsg(subNodeRoutingTable, getLocation()), subNode);
        } else {
            // ... else propagate newNode message down to the next level
            sendMessage(new NewNodeMsg(subNode, getLocation()), levelBelow);
        }
    }

    /** Internal helper that sends a single message to a specified node */
    private void sendMessage(Object msg, SimpleNode node) {
        try {
            // Generate a connection, write the object, and then close the connection
            Socket socket = new Socket(node.ip, node.port);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(msg);
            socket.close();
        } catch (IOException e) {
            System.out.println("Node " + self.ip + ":" + self.port + " failed to connect to node at " + node.ip + ":" + node.port);
            e.printStackTrace();
        }
    }

    /** Internal helper that broadcasts a specified message to all nodes expect self in a routingTable */
    private void broadcast(Object msg) {
        // Broadcast to own network that a new node has been inserted
        for (SimpleNode n : routingTable) {
            // We don't need to broadcast information to ourselves.
            if (n.port == self.port && n.ip.equals(self.ip)) {
                continue;
            }

            sendMessage(msg, n);
        }
    }

    /** Internal helper that returns the location of the current node */
    private ArrayList<Integer> getLocation() {
        ArrayList<Integer> location = (ArrayList<Integer>) prevLocations.clone();

        // Insert location of self into location array
        location.add(getIndexOfSelf());

        return location;
    }

    /** Internal helper that returns the index of the current instance of the Node in the routingTable */
    private int getIndexOfSelf() {
        for (int i = 0; i < routingTable.size(); i++) {
            SimpleNode n = routingTable.get(i);

            // When we've found a matching index, return it!
            if (n.port == self.port && n.ip.equals(self.ip)) {
                return i;
            }
        }

        // If we get here, then no match was found..
        return -1;
    }

    /**
     * Internal helper that inserts/updates a node in the routingTable
     *
     * NB: Executed once an UpdateRoutingTableMsg is sent.
     */
    private void updateRoutingTable(int index, SimpleNode node) {
        // Insert new node to routing table
        if (routingTable.size() > index) {
            routingTable.set(index, node);
        } else {
            routingTable.add(node);
        }
    }

    /**
     * Internal helper that'll set information about the current network to the node.
     *
     * NB: Executed once a SetNewNodeInformationMsg is sent to the node
     */
    private void setNodeInformation(SetNewNodeInformationMsg msg) {
        this.routingTable = msg.routingTable;
        this.prevLocations = msg.prevNodeLocation;
    }

/*    public void getResource(hashCode code) {
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

*/
}
