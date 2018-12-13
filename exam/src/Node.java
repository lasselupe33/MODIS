import Messages.Election.ElectedMsg;
import Messages.Election.ElectionMsg;
import Messages.NodeHandlingMessages.*;
import Messages.ValueMessages.UpdateValueMsg;
import Models.ElectionStates;
import Models.SimpleNode;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static Models.ElectionStates.nonParticipant;
import static Models.ElectionStates.participant;

public class Node {
    /** Specifies how many neighbour references we desire that each node should have */
    public static int desiredAmountOfNeighbourReferences = 3;

    /** Specifies how many super nodes we desire in the system */
    public static int desiredAmountOfSuperNodes = 3;

    // If ever set to false, then the node process should stop. (Used to debug resilliency)
    private boolean alive;

    /** Specifies the current election state of a given node */
    private ElectionStates electionState = nonParticipant;

    // Contain basic information about self, in order to be able to filter self out of own routingTable
    public SimpleNode self;

    // Contains a colleciton of all neighbours
    private SimpleNode[] neighbours = new SimpleNode[desiredAmountOfNeighbourReferences];

    private SimpleNode neighbourBehind;

    // Contains a collection of all super nodes. The first node in the array is recognized as the primary super node
    private CopyOnWriteArrayList<SimpleNode> superNodes = new CopyOnWriteArrayList<>();

    // Contains the current incrementer value. This value will only be updated in SuperNodes
    private int value = 0;

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private Lock writeLock = lock.writeLock();
    private Lock readLock = lock.readLock();

    public Node(int port) {
        setupNode(port);

        // First node to enter network automatically gets promoted to primary super node
        superNodes.add(self);

        // We assume reflexive neighbour relations on the first node enterine the system
        // until new ones are added
        neighbourBehind = self;
        neighbours[0] = self;
        neighbours[1] = self;
        neighbours[2] = self;
    }

    public Node(int port, int targetPort) {
        try {
            setupNode(port);
            connectToNetwork(targetPort, InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public Node(int port, int targetPort, String targetIp) {
        try {
            setupNode(port);
            connectToNetwork(targetPort, InetAddress.getByName(targetIp));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /** When called the node process will end itself */
    public void dispose() {
        this.alive = false;
    }

    /** Internal helper that sets up core functionality of a node and binds it to the network */
    private void setupNode(int port) {
        try {
            this.alive = true;

            this.self = new SimpleNode(InetAddress.getLocalHost(), port);

            // Begin listening to incoming connections at specified port
            listen(port).start();
            heartbeat().start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /** Internal helper that should be called once a new node should be added to the network */
    private void connectToNetwork(int targetPort, InetAddress targetIp) {
        NewNodeMsg msg = new NewNodeMsg(self, 0);
        sendMessage(msg, new SimpleNode(targetIp, targetPort));
    }

    /**
     * Thread that attempts to respond to all requests sent to the given node.
     */
    private Thread listen(int port) {
        return new Thread(() -> {
            try {
                ServerSocket inSocket = new ServerSocket(port);

                while (alive) {
                    // Accept incoming messages
                    Socket connectionSocket;

                    try {
                        connectionSocket = inSocket.accept();
                    } catch (EOFException e) {
                        // Skip connection if we fail to connect properly
                        continue;
                    }

                    // When a request is received, handle it on a separate thread
                    handleRequest(connectionSocket).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private Thread handleRequest(Socket connectionSocket) {
        return new Thread(() -> {
            try {
                ObjectInputStream input = new ObjectInputStream(connectionSocket.getInputStream());
                Object receivedObj = input.readObject();

                // MESSAGES RELATED TO INSERTING NEW NODES TO THE NETWORK
                if (receivedObj instanceof NewNodeMsg) {
                    NewNodeMsg msg = (NewNodeMsg) receivedObj;
                    insertNode(msg);
                } else if (receivedObj instanceof SetNewNodeInformationMsg) {
                    SetNewNodeInformationMsg msg = (SetNewNodeInformationMsg) receivedObj;
                    neighbours = msg.neighbours;
                    neighbourBehind = msg.neighbourBehind;
                    superNodes = msg.superNodes;
                } else if (receivedObj instanceof UpdateNodeBehindMsg) {
                    UpdateNodeBehindMsg msg = (UpdateNodeBehindMsg) receivedObj;
                    neighbourBehind = msg.nodeBehind;
                }

                // MESSAGES RELATED TO UPDATING NODE NEIGHBOURS ON NODE DEATH
                if (receivedObj instanceof NodeDiedMsg) {
                    NodeDiedMsg msg = (NodeDiedMsg) receivedObj;
                    neighbourBehind = msg.newNodeBehind;

                    ObjectOutputStream responseStream = new ObjectOutputStream(connectionSocket.getOutputStream());
                    responseStream.writeObject(new UpdateNeighboursMsg(generateNewNeighboursForNodeBehind(neighbours), 0, -1));
                } else if (receivedObj instanceof UpdateNeighboursMsg) {
                    UpdateNeighboursMsg msg = (UpdateNeighboursMsg) receivedObj;
                    updateNeighbours(msg);
                }

                // MESSAGES RELATED TO ELECTIONS
                if (receivedObj instanceof ElectionMsg) {
                    ElectionMsg msg = (ElectionMsg) receivedObj;
                    election(msg);
                } else if (receivedObj instanceof ElectedMsg) {
                    ElectedMsg msg = (ElectedMsg) receivedObj;
                    elected(msg);
                }

                // MESSAGES RELATED TO MAIN API FUNCTIONALITY
                if ("GET".equals(receivedObj)) {
                    getRequest(connectionSocket).start();
                    return;
                } else if ("INC".equals(receivedObj)) {
                    incrementRequest(connectionSocket).start();
                    return;
                }

                if (receivedObj instanceof UpdateValueMsg) {
                    UpdateValueMsg msg = (UpdateValueMsg) receivedObj;
                    setValue(msg, connectionSocket).start();
                    return;
                } else if ("incrementValue".equals(receivedObj)) {
                    incrementResponse(connectionSocket).start();
                    return;
                } else if ("getValue".equals(receivedObj)) {
                    getResponse(connectionSocket).start();
                    return;
                }

                if ("heartbeat".equals(receivedObj)) {
                    ObjectOutputStream outputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
                    outputStream.writeObject("alive");
                }

                connectionSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    /** Internal helper that inserts a new node in front of the node this method was called on. */
    private void insertNode(NewNodeMsg msg) {
        // If we're at the first node that should update its list of neighbours, then ensure we also
        // propagate the proper list of neighbours to the newly inserted node
        if (msg.neighbourIndex == 0) {
            SetNewNodeInformationMsg newMsg = new SetNewNodeInformationMsg(self, neighbours, superNodes);
            sendMessage(newMsg, msg.node);

            // And ensure that over previous neighbour recognizes the new node as its nodeBehind
            if (neighbours[0] != null) {
                UpdateNodeBehindMsg updateMsg = new UpdateNodeBehindMsg(msg.node);
                sendMessage(updateMsg, neighbours[0]);
            }

            if (neighbourBehind == null) {
                neighbourBehind = msg.node;
            }
        }

        // If we're currently in the initial phase of inserting nodes, proclaim first three nodes as super nodes
        // afterwards new superNodes will be determined via elections
        if (superNodes.size() < desiredAmountOfSuperNodes && !superNodes.contains(msg.node)) {
            superNodes.add(msg.node);
        }

        // Update neighbour reference by inserting new node at desired place...
        SimpleNode[] newNeighbours = neighbours.clone();
        newNeighbours[msg.neighbourIndex] = msg.node;

        // ... and shifting all other neighbours one positioned, discarding the furthest away node.
        for (int i = msg.neighbourIndex + 1; i < neighbours.length; i++) {
            newNeighbours[i] = neighbours[i - 1];
        }

        neighbours = newNeighbours;

        // If this node has neighbours that also should reference the new node in their neighbour array, propagate
        // the message to them
        if (msg.neighbourIndex < desiredAmountOfNeighbourReferences - 1 && newNeighbours[msg.neighbourIndex + 1] != null) {
            NewNodeMsg propagateMsg = new NewNodeMsg(msg.node, msg.neighbourIndex + 1);
            sendMessage(propagateMsg, neighbourBehind);
        }
    }

    /**
     * Method that can be invoked on any node once a client reaches out to in and request the current value of the
     * counter.
     */
    private Thread getRequest(Socket clientSocket) {
        return new Thread(() -> {
            try {
                ObjectOutputStream responseStream = new ObjectOutputStream(clientSocket.getOutputStream());

                Integer value;

                // If the node that received the request is the primary super node, simply process immediately
                if (isPrimarySuperNode()) {
                    value = getValue();
                } else {
                    // ... else propagate request to primary super node
                    value = (Integer) requestValue("getValue", superNodes.get(0));
                }

                responseStream.writeObject(value);
                responseStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Method that can be invoked on any node once a client reaches out to it and requests to increment the
     * value of the counter.
     */
    private Thread incrementRequest(Socket clientSocket) {
        return new Thread(() -> {
            try {
                ObjectOutputStream responseStream = new ObjectOutputStream(clientSocket.getOutputStream());

                Integer value;

                // If the node that received the request is the primary super node, simple process immediately
                if (isPrimarySuperNode()) {
                    value = incrementValue();
                } else {
                    // ... else propagate request to primary super node
                    value = (Integer) requestValue("incrementValue", superNodes.get(0));
                }

                // Once the value has been properly incremented, return it to the client
                responseStream.writeObject(value);
                responseStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Internal helper that'll be called from the Primary Super Node once another node requests to get the
     * value of the counter.
     */
    private Thread getResponse(Socket responseSocket) {
        return new Thread (() -> {
            try {
                ObjectOutputStream responseStream = new ObjectOutputStream(responseSocket.getOutputStream());

                readLock.lock();
                responseStream.writeObject(getValue());
                readLock.unlock();

                responseSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Internal helper that'll be called by the Primary Super Node once another node request to increment the counter
     * and returns the newly incremented value once completed.
     */
    private Thread incrementResponse(Socket responseSocket) {
        return new Thread (() -> {
            try {
                ObjectOutputStream responseStream = new ObjectOutputStream(responseSocket.getOutputStream());

                // Lock our value while writing
                writeLock.lock();
                int value = incrementValue();

                // Once all replicas have been updated, return the value
                responseStream.writeObject(value);
                writeLock.unlock();

                responseSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /** Simple helper to be called on SuperNodes to get the current value of the counter */
    private int getValue() {
        return this.value;
    }

    /**
     * Internal helper to be called when the main super node incerements the counter
     */
    private int incrementValue() {
        int value = ++this.value;
        System.out.println("Primary node at " + self + " sat its value to " + value);

        // Now that we've updated our node, broadcast this to all replicas
        UpdateValueMsg verificationMsg = new UpdateValueMsg(value);

        for (SimpleNode SN : superNodes) {
            if (SN == self) {
                continue;
            }

            String response = (String) requestValue(verificationMsg, SN);
        }

        return value;
    }

    /**
     * Internal helper that can get called on Backup Super Nodes in order to ensure their values matching
     * the primary super node
     */
    private Thread setValue(UpdateValueMsg msg, Socket responseSocket) {
        return new Thread (() -> {
            try {
                ObjectOutputStream responseStream = new ObjectOutputStream(responseSocket.getOutputStream());

                // Update value and reply main replica that it has been done
                writeLock.lock();
                this.value = msg.newValue;
                responseStream.writeObject("OK");
                System.out.println("Backup server at " + self + " sat its value to " + msg.newValue);
                writeLock.unlock();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Internal helper that when called will begin an election to determine a new superNode.
     *
     * If the process that starts the election already is a superNode, then it'll simply pass a node with the lowest
     * possible UID in order to ensure that it'll be replaced by a regular node somewhere along the way.
     */
    private void startElection(SimpleNode deadNode) {
        try {
            SimpleNode startCandidate = isSuperNode() ? new SimpleNode(InetAddress.getLocalHost(), 0) : self;

            ElectionMsg msg = new ElectionMsg(startCandidate, deadNode);
            electionState = participant;
            sendMessage(msg, neighbours);
            superNodes.remove(deadNode);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Part of the Chang and Roberts algorithm.. Will be called once an election is in the process of determining a new
     * superNode
     */
    private void election(ElectionMsg msg) {
        superNodes.remove(msg.deadSuperNode);

        if (msg.candidate.port > self.port || isSuperNode()) {
            electionState = participant;
            sendMessage(msg, neighbours);
        } else if (msg.candidate.port < self.port && electionState == nonParticipant) {
            electionState = participant;
            ElectionMsg newMsg = new ElectionMsg(self, msg.deadSuperNode);

            sendMessage(newMsg, neighbours);
        } else if (msg.candidate.port < self.port && electionState == participant) {
            // Discard message
        } else if (msg.candidate.port == self.port) {
            ElectedMsg electedMsg = new ElectedMsg(self);
            superNodes.add(self);

            electionState = nonParticipant;
            sendMessage(electedMsg, neighbours);
        }
    }

    /**
     * Part of the Chang and Roberts algorithm.. Will be called once an ongoing election is currently finishing
     * @param msg
     */
    private void elected(ElectedMsg msg) {
        if (electionState == nonParticipant) {
            // If we get here, everybody knows that the new SuperNode has been selected.
            System.out.println("New leader elected! " + msg.winner);
        } else {
            superNodes.add(msg.winner);
            sendMessage(msg, neighbours);
        }
    }

    /**
     * If a list of nodes is passed to the sendMessage, then we'll attempt to propagate the message to the first available
     * node
     */
    private void sendMessage(Object msg, SimpleNode[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            try {
                if (checkIfNodeIsAlive(nodes[i])) {
                    sendMessageUnsafe(msg, nodes[i]);
                } else {
                    continue;
                }

                // If we get here, then the send succeeded, no need to try another node
                break;
            } catch (IOException e) {
                // Continue
            }
        }
    }


    /** Internal helper that sends a single message to a specified node */
    private void sendMessage(Object msg, SimpleNode node) {
        try {
            sendMessageUnsafe(msg, node);
        } catch (IOException e) {
            System.out.println("Node " + self.ip + ":" + self.port + " failed to connect to node at " + node.ip + ":" + node.port);
        }
    }

    /** Internal helper that sends a single message to a specified node */
    private void sendMessageUnsafe(Object msg, SimpleNode node) throws IOException {
        // Generate a connection, write the object, and then close the connection
        Socket socket = new Socket(node.ip, node.port);
        socket.setSoTimeout(2000);
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        output.writeObject(msg);
        socket.close();
    }

    /**
     * Internal helper that requests a value from a specified node, and waits until it has a reply
     * before returning. (Unless a timeout occurs).
     */
    private Object requestValue(Object requestObject, SimpleNode node) {
        // We determine that a node has died if it hasn't responded within two seconds..
        int timeout = 4000;

        try {
            // Send request to specified node
            Socket socket = new Socket(node.ip, node.port);
            socket.setSoTimeout(timeout);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(requestObject);

            // Await response
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            Object response = input.readObject();

            socket.close();

            return response;
        } catch (SocketTimeoutException e) {
          // If we get here the request failed, retry in a bit..
            return null;
        } catch (ConnectException e) {
            return null;
        } catch (SocketException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {

        }

        // if we get here, then request failed
        return null;
    }

    /**
     * Thread that'll continuously run and check whether or not or not its subNode and neighbor is alive, in order to
     * find out when nodes die, and should be replaced.
     */
    private Thread heartbeat() {
        return new Thread(() -> {
            while (alive) {
                try {
                    if (neighbours[0] == null || neighbours[0] == self) {
                        Thread.sleep(2000);
                        continue;
                    }

                    if (!checkIfNodeIsAlive(neighbours[0])) {
                        // If we get here, then our neighbour has died!
                        System.out.println("Recognized following dead node in system: " + neighbours[0]);
                        System.out.println();

                        if (superNodes.contains(neighbours[0])) {
                            System.out.println("Started election!");
                            // Our neighbour was a superNode, begin an election as well!
                            startElection(neighbours[0]);
                        }

                        onNeighbourDied();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** Internal helper that, when called, will check if a specified node is alive */
    private boolean checkIfNodeIsAlive(SimpleNode node) {
        String response = (String) requestValue("heartbeat", new SimpleNode(node.ip, node.port));

        return "alive".equals(response);
    }

    /**
     * Internal helper that'll get called once a neighbour dies in order to restore the ring
     */
    private void onNeighbourDied() {
        UpdateNeighboursMsg msg = (UpdateNeighboursMsg) requestValue(new NodeDiedMsg(self), neighbours[1]);
        updateNeighbours(new UpdateNeighboursMsg(msg.newNeighbours, msg.timesRun, desiredAmountOfNeighbourReferences - 1));
    }

    /**
     * Internal helper to be called once neighbour references should be updated due to a dead node nearby
     * @param msg
     */
    private void updateNeighbours(UpdateNeighboursMsg msg) {
        neighbours = msg.newNeighbours;

        // Continue until all required nearby nodes have been updated.
        if (msg.timesRun < msg.requiredRuns) {
            SimpleNode[] updatedNeighbours = generateNewNeighboursForNodeBehind(msg.newNeighbours);
            UpdateNeighboursMsg newMsg = new UpdateNeighboursMsg(updatedNeighbours, msg.timesRun + 1, msg.requiredRuns);
            sendMessage(newMsg, neighbourBehind);

            System.out.println(self + " Restored its neighbours");
        }
    }

    /**
     * Hélper that'll generate an array of neighbours that should be inserted for the nodeBehind due to a node having
     * died nearby
     */
    private SimpleNode[] generateNewNeighboursForNodeBehind(SimpleNode[] references) {
        SimpleNode[] newNeighbours = neighbours.clone();
        newNeighbours[0] = self;

        for (int i = 1; i < neighbours.length; i++) {
            newNeighbours[i] = references[i - 1];
        }

        return newNeighbours;
    }

    /** Determines whether or not this node is part of the super node network */
    private boolean isSuperNode() {
        return isSuperNode(self);
    }

    /** Determines whether or not specified node is part of the super node network */
    private boolean isSuperNode(SimpleNode node) {
        for (SimpleNode superNode : superNodes) {
           if (superNode.equals(node)) {
               return true;
           }
        }

        // If self isn't part of superNode array, then we aren't a super node :-(
        return false;
    }

    /** Determines whether or not this node is currently the primary super node */
    private boolean isPrimarySuperNode() {
        return superNodes.get(0).equals(self);
    }
}
