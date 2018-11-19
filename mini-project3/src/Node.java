import Messages.NodeInsertionMessages.*;
import Messages.ResourceHandlingMessages.*;
import Messages.TraverseMessages.*;
import Models.SimpleNode;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Node {
    // If ever set to false, then the node process should stop. (Used to debug resilliency)
    private boolean alive;

    // Contain basic information about self, in order to be able to filter self out of own routingTable
    private SimpleNode self;

    // Contains an arrayList with a location for each node that has been identified as dead in the network
    private ArrayList<ArrayList<Integer>> deadNodes = new ArrayList<>();

    // Contains a collection of all resources that the current node contains
    private HashMap<Integer, String> resources = new HashMap<>();

    // Contains a backup of the resources stored at sub node or right neighbour
    private HashMap<Integer, String> backupResources = new HashMap();

    // Contains a routingTable that contains all other nodes in the current layer
    private ArrayList<SimpleNode> routingTable = new ArrayList<>();

    // Specifies the location to the current node, e.g. [1, 2, 3, 7] would map to a node on the fourth layer
    private ArrayList<Integer> prevLocations = new ArrayList<>();

    // Specifies the index of the next node in the current routing table that should propagate the next incoming node
    // downwards if necessary
    private int nextNodeIndex = 0;

    // Contains a reference to nodes existing in the level below
    private ArrayList<SimpleNode> levelBelowList = new ArrayList<>();

    // Contains a reference to nodes existing in the level above
    private ArrayList<SimpleNode> levelAboveList = new ArrayList<>();

    // Contains a reference to the socket that a get value should be returned to, if any exists.
    private Socket getReturnSocket;

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

    /** When called the node process will end itself */
    public void dispose() {
        this.alive = false;
    }

    /** Internal helper that sets up core functionality of a node */
    private void setupNode(int port) {
        try {
            this.alive = true;

            // Store information about self
            self = new SimpleNode(InetAddress.getLocalHost(), port);

            // Begin listening to incoming connections at specified port
            listen(port).start();
            heartbeat().start();
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

                while (alive) {
                    // Setup socket and accept incoming messages
                    Socket connectionSocket;
                    ObjectInputStream input;

                    try {
                        connectionSocket = inSocket.accept();
                        input = new ObjectInputStream(connectionSocket.getInputStream());
                    } catch (EOFException e) {
                        continue;
                    }

                    Object receivedObj = input.readObject();

                    // MESSAGES RELATED TO INSERTING NODES
                    if (receivedObj instanceof NewNodeMsg) {
                        NewNodeMsg msg = (NewNodeMsg) receivedObj;
                        insertNode(msg);
                    } else if (receivedObj instanceof RequestResourcesMsg) {
                        RequestResourcesMsg requestMsg = (RequestResourcesMsg) receivedObj;

                        SendResourcesMsg sendMsg = new SendResourcesMsg(resources, false);

                        sendMessage(sendMsg, requestMsg.requestingNode);
                    } else if (receivedObj instanceof SendResourcesMsg) {
                        SendResourcesMsg msg = (SendResourcesMsg) receivedObj;

                        // If the new node is a subNode, then simply move all resources from parent
                        if (msg.isSubNode) {
                            resources = msg.resources;
                        } else {
                            distributeResourcesWithNeighbour(msg.resources);
                        }
                    } else if (receivedObj instanceof UpdateResourcesMsg) {
                        UpdateResourcesMsg msg = (UpdateResourcesMsg) receivedObj;
                        resources = msg.resources;

                        // Send backup of resources to left neighbour or node above
                        sendBackupResources();

                    }
                    else if (receivedObj instanceof UpdateBackupResourcesMsg)
                    {
                        UpdateBackupResourcesMsg msg = (UpdateBackupResourcesMsg) receivedObj;
                        backupResources = msg.backupResources;
                    }
                    else if (receivedObj instanceof NewSubNodeMsg)
                    {
                        NewSubNodeMsg msg = (NewSubNodeMsg) receivedObj;
                        insertSubNode(msg.node);
                    }

                    // MESSAGES RELATED TO UPDATING INTERNAL NODE DATA
                    else if (receivedObj instanceof UpdateRoutingTableMsg) {
                        UpdateRoutingTableMsg msg = (UpdateRoutingTableMsg) receivedObj;
                        updateRoutingTable(msg.index, msg.value);
                    }
                    else if (receivedObj instanceof SetNewNodeInformationMsg) {
                        SetNewNodeInformationMsg msg = (SetNewNodeInformationMsg) receivedObj;
                        setNodeInformation(msg);
                    }
                    else if (receivedObj instanceof SetNewSubNodeInformationMsg)
                    {
                        SetNewSubNodeInformationMsg msg = (SetNewSubNodeInformationMsg) receivedObj;
                        setNodeInformation(msg);
                        levelAboveList = msg.levelAbove;
                    }
                    else if (receivedObj instanceof SetBackUpNodeMsg) {
                        SetBackUpNodeMsg msg = (SetBackUpNodeMsg) receivedObj;

                        // If the list of references is below minimum requirements, then add as backup
                        if (levelBelowList.size() < 2) {
                            levelBelowList.add(msg.backupNode);
                        }
                    }
                    else if (receivedObj instanceof UpdateCurrentPositionMsg)
                    {
                        UpdateCurrentPositionMsg msg = (UpdateCurrentPositionMsg) receivedObj;
                        nextNodeIndex = msg.newPos;
                    }

                    // MESSAGES RELATED TO PUT/GET
                    else if (receivedObj instanceof InsertResourceInNearestIndexMsg) {
                        PutMsg msg = ((InsertResourceInNearestIndexMsg) receivedObj).putMsg;
                        insertResource(msg);
                    } else if (receivedObj instanceof GetResourceInNearestIndexMsg) {
                        TraverseGetMsg msg = ((GetResourceInNearestIndexMsg) receivedObj).getMsg;
                        getResource(msg);
                    } else if (receivedObj instanceof TraverseGetMsg) {
                        TraverseGetMsg msg = (TraverseGetMsg) receivedObj;
                        Boolean isDesiredNode = traverseResourceDown(msg);

                        if (isDesiredNode) {
                            handleResourceRequest(msg);
                        }
                    } else if (receivedObj instanceof ReturnMsg) {
                        // When this message is received, that means a subnode has fetched a resource that this node
                        // shall propagate to its stored return socket that issued the request
                        ReturnMsg msg = (ReturnMsg) receivedObj;
                        ObjectOutputStream output = new ObjectOutputStream(getReturnSocket.getOutputStream());

                        if (msg.value == null) {
                            output.writeObject(null);
                        } else {
                            output.writeObject(msg);
                        }

                        getReturnSocket.close();
                        getReturnSocket = null;
                    } else if (receivedObj instanceof PutMsg) {
                        PutMsg msg = (PutMsg) receivedObj;
                        Boolean isDesiredNode = traverseResourceDown(msg);

                        if (isDesiredNode) {
                            handleResourceRequest(msg);
                        }
                    } else if (receivedObj instanceof GetMsg) {
                        GetMsg msg = (GetMsg) receivedObj;

                        // Begin traversing
                        TraverseGetMsg traverseMsg = new TraverseGetMsg(msg.key, self);
                        Boolean isDesiredNode = traverseResourceDown(traverseMsg);

                        if (isDesiredNode) {
                            handleResourceRequest(traverseMsg);
                        }

                        // Store socket to getClient in order to be able to send back result
                        getReturnSocket = connectionSocket;
                        continue;
                    }

                    connectionSocket.close();
                }

                inSocket.close();
            } catch (IOException e) {
                System.out.println("Failed to bind node to " + self.ip + ":" + self.port);
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Thread that'll continuously run and check whether or not or not its subNode and neighbor is alive, in order to
     * find out when nodes die, and should be replaced.
     */
    private Thread heartbeat() {
        return new Thread(() -> {
            while (alive) {
                try {
                    // Send heartbeat every 10 seconds
                    Thread.sleep(10000);

                    SimpleNode neighbour = routingTable.get((getIndexOfSelf() + 1) % routingTable.size());

                    if (neighbour != null) {
                        if (!checkIfNodeIsAlive(neighbour)) {
                            System.out.println("Neighbour died!");
                        }
                    }

                    if (levelBelowList.size() > 0) {
                        if (!checkIfNodeIsAlive(levelBelowList.get(0))) {
                            System.out.println("Level below died!");
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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

            // Send self to level above as backup reference
            sendMessage(new SetBackUpNodeMsg(self), newNodeMsg.node);

            // Request resources from neighbour
            requestResourcesFromNeighbour(newIndex);
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

    /** Internal helper to be called once a node that is currently being inserted should traverse down to the next level */
    private void insertSubNode(SimpleNode subNode) {
        // If there doesn't exist a level below the specified node, create it now
        if (levelBelowList.size() == 0) {
            levelBelowList.add(subNode);

            // Creating a list of references to the level above
            ArrayList<SimpleNode> levelAbove = new ArrayList<>();
            levelAbove.add(self);
            levelAbove.add(routingTable.get(Utils.getNearestIndexWithNode(routingTable, getIndexOfSelf())));

            // Create a new routingTable for the subnode
            ArrayList<SimpleNode> subNodeRoutingTable = new ArrayList<>();
            subNodeRoutingTable.add(subNode);
            sendMessage(new SetNewSubNodeInformationMsg(subNodeRoutingTable, getLocation(), levelAbove), subNode);

            // The sub node's resources is set to the resources currently stored at this node
            // This node no longer has to store these resources but it has to store a backup of the resources at the sub node
            // Therefore it sets its backup resources to the resources it is currently storing and clears its own resource table
            sendMessage(new SendResourcesMsg(resources, true), subNode);
            backupResources = resources;
            resources.clear();

        } else {
            // ... else propagate newNode message down to the next level
            sendMessage(new NewNodeMsg(subNode, getLocation()), levelBelowList.get(0));
        }
    }

    /** Internal helper that requests resources from the nodes left neighbour*/
    private void requestResourcesFromNeighbour(int index) {
        SimpleNode leftNeighbour = routingTable.get(index - 1);

        RequestResourcesMsg msg = new RequestResourcesMsg(self);

        sendMessage(msg, leftNeighbour);
    }

    /**
     * Internal helper that distributes resources between the newly inserted node and its left neighbour in the routing table
     * based on resource key hash proximity.
     *
     * NB: Will be called once a new node has been inserted into a routing table that hasn't yet been filled.
     */
    private void distributeResourcesWithNeighbour(HashMap<Integer, String> resourcesFromNeighbour) {
        int index = routingTable.size() - 1;
        int neighbourIndex = index-1;

        // Go through resources and check whether they should be moved to this node
        for(Integer key : resourcesFromNeighbour.keySet()){
            // get index of resource at the current level
            String hashedKey = Utils.hashString("" + key);
            int resourceIndex = Utils.convertHashToLocation(hashedKey).get(prevLocations.size());

            int distanceFromSelf = Math.abs(index - resourceIndex);
            int distanceFromNeighbour = Math.abs(neighbourIndex - resourceIndex);

            // If the resource is closer to the new node, then move resource here
            if(distanceFromSelf < distanceFromNeighbour) {
                resources.put(key, resourcesFromNeighbour.get(key));
                resourcesFromNeighbour.remove(key);
            }
        }

        // Send updated resources map back to neighbour
        UpdateResourcesMsg updateResourcesMsg = new UpdateResourcesMsg(resourcesFromNeighbour);
        SimpleNode neighbourNode = routingTable.get(neighbourIndex);
        sendMessage(updateResourcesMsg, neighbourNode);

        // Send backup of resources to left neighbour
        UpdateBackupResourcesMsg updateBackupResourcesMsg = new UpdateBackupResourcesMsg(resources);
        sendMessage(updateBackupResourcesMsg, neighbourNode);
    }

    /**
     * Internal helper that sends a backup of its resources to its neighbour or parent node
     */
    private void sendBackupResources() {
        UpdateBackupResourcesMsg updateBackupResourcesMsg = new UpdateBackupResourcesMsg(resources);

        int index = getIndexOfSelf();

        if (index == 0){
            // If levelAbove is null we are at the first node inserted and there is no backup table to update
            if (levelAboveList.size() > 0){
                sendMessage(updateBackupResourcesMsg, levelAboveList.get(0));
            }
        } else {
            SimpleNode neighbourNode = routingTable.get(index-1);
            sendMessage(updateBackupResourcesMsg, neighbourNode);
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

    /** Internal helper that'll traverse downwards in direction of the key specified in the msg */
    private boolean traverseResourceDown(TraverseMsg msg) {
        // Figure out what index we desire to travel to in the current level based on the location of the hash
        String hashString = Utils.hashString("" + msg.key);
        ArrayList<Integer> msgLocation = Utils.convertHashToLocation(hashString);
        int index = msgLocation.get(prevLocations.size());

        // Now, based on the hash, get the node in the current routing table we should be at
        SimpleNode targetNode = routingTable.get(index);

        if (targetNode == null) {
            // if our target doesn't exist, send and insert/get the resource at the nearest node
            sendResourceRequestToNearestNode(msg, index);
            return false;
        }


        return traverse(msg, targetNode);
    }

    /** Internal helper that'll attempt to traverse a message to the root */
    private boolean traverseToRoot(TraverseMsg msg) {
        return traverse(msg, routingTable.get(0));
    }

    /**
     * Internal helper that figures out where to traverse to in the structured network based on the passed key in the msg
     */
    private boolean traverse(TraverseMsg msg, SimpleNode targetNode) {
        if (targetNode.ip.equals(self.ip) && targetNode.port == self.port) {
            // ... else, if we already is the correct node, determine if we can go down further
            if (levelBelowList.size() == 0) {
                // ... if we can't, insert/get resource based on message
                return true;
            } else {
                // ... else, propagate the message down
                sendMessage(msg, levelBelowList.get(0));
                return false;
            }
        } else {
            // ... else, go to the correct node in the current routing table
            sendMessage(msg, targetNode);
            return false;
        }
    }

    /** Internal helper that send the resource to the node nearest given index **/
    public void sendResourceRequestToNearestNode(TraverseMsg traverseMsg, int index) {
        int nearestIndex = Utils.getNearestIndexWithNode(routingTable, index);
        SimpleNode nearestNode = routingTable.get(nearestIndex);

        if (traverseMsg instanceof PutMsg)
        {
            PutMsg msg = (PutMsg) traverseMsg;
            InsertResourceInNearestIndexMsg insertMsg = new InsertResourceInNearestIndexMsg(msg);
            sendMessage(insertMsg, nearestNode);
        }
        else if (traverseMsg instanceof TraverseGetMsg)
        {
            TraverseGetMsg msg = (TraverseGetMsg) traverseMsg;
            GetResourceInNearestIndexMsg getMsg = new GetResourceInNearestIndexMsg(msg);
            sendMessage(getMsg, nearestNode);
        }
    }

    /** Simple helper that'll handle the resourceRequest once the proper node has been found */
    private void handleResourceRequest(TraverseMsg msg) {
        if (msg instanceof PutMsg)
        {
            insertResource((PutMsg) msg);
        }
        else if (msg instanceof TraverseGetMsg)
        {
            getResource((TraverseGetMsg) msg);
        }
    }

    /** Internal helper that insert a resource in current node **/
    public void insertResource(PutMsg msg) {
        // Debug
        System.out.println("Node " + self.ip + ":" + self.port + " inserted resource with key '" + msg.key + "' and value '" + msg.value + "' at the following location:");
        System.out.println(getLocation());

        // Actually insert resource
        if (resources.containsKey(msg.key)) {
            System.out.println();
            System.out.println("Received mixed information.. Key has already been set once, setting value to null..");
            resources.put(msg.key, null);
        } else {
            resources.put(msg.key, msg.value);
        }

        // Send backup of resources to left neighbour or node above
        sendBackupResources();

        System.out.println();
    }

    /** Internal helper that returns a resource matching the key in the passed message */
    public void getResource(TraverseGetMsg msg) {
        String resource = resources.get(msg.key);

        System.out.println("Node " + self.ip + ":" + self.port + " retrieved resource with key '" + msg.key + "' and value '" + resource + "' at the following location:");
        System.out.println(getLocation());
        System.out.println();

        sendMessage(new ReturnMsg(msg.key, resource), msg.returnNode);
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

    /** Internal helper that, when called, will check if a specified node is alive */
    private boolean checkIfNodeIsAlive(SimpleNode node) {
        boolean isAlive;

        SocketAddress socketAddress = new InetSocketAddress(node.ip, node.port);
        Socket checkSocket = new Socket();

        // We determine that a node has died if it hasn't responded within two seconds..
        int timeout = 2000;


        try {
            checkSocket.connect(socketAddress, timeout);
            checkSocket.close();

            // If we get here, then the connection succeeded.
            isAlive = true;
        } catch (IOException e) {
            isAlive = false;
        }

        return isAlive;
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
}
