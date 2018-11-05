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
    private SimpleNode self;
    private ServerSocket inSocket;

    private HashMap<Integer, String> resources;


    private static char[] charMapping = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
    private ArrayList<SimpleNode> routingTable = new ArrayList<>();
    private int currPos = 0;
    private int currLevel = 0;

    private SimpleNode levelBelow;
    private SimpleNode levelAbove;

    public Node(int port) {
        // Setup own socket
        try {
            inSocket = new ServerSocket(port);
            listen().start();

            // Store information about self
            self = new SimpleNode(InetAddress.getLocalHost(), port);
            routingTable.add(self);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Node(int port, int targetPort) {
        try {
            setupNode(port, targetPort, InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public Node(int port, int targetPort, String targetIp) {
        // Instantiate self
        try {
            setupNode(port, targetPort, InetAddress.getByName(targetIp));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // Call insertNode with self on given nodePort
    }

    private void setupNode(int port, int targetPort, InetAddress targetIp) {
        try {
            // Store information about self
            self = new SimpleNode(InetAddress.getLocalHost(), port);

            // Setup own socket
            inSocket = new ServerSocket(port);
            listen().start();

            // Add self to network
            NewNodeMsg msg = new NewNodeMsg(new SimpleNode(InetAddress.getLocalHost(), port), currLevel);
            sendMessage(msg, new SimpleNode(targetIp, targetPort));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Thread listen() {
        return new Thread(() -> {
            while (true) {
                try {
                    Socket connectionSocket = inSocket.accept();
                    ObjectInputStream input = new ObjectInputStream(connectionSocket.getInputStream());
                    Object receivedObj = input.readObject();

                    if (receivedObj instanceof NewNodeMsg) {
                        insertNode(((NewNodeMsg) receivedObj));
                    } else if (receivedObj instanceof NewSubNodeMsg) {
                        insertSubNode(((NewSubNodeMsg)receivedObj).node);
                    } else if (receivedObj instanceof UpdateRoutingTableMsg) {
                        updateRoutingTable(((UpdateRoutingTableMsg) receivedObj).index, ((UpdateRoutingTableMsg) receivedObj).value);
                    } else if (receivedObj instanceof SetNewNodeInformationMsg) {
                        setNodeInformation((SetNewNodeInformationMsg) receivedObj);
                    } else if (receivedObj instanceof UpdateCurrentPositionMsg) {
                        currPos = ((UpdateCurrentPositionMsg) receivedObj).newPos;
                    }

                    connectionSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // During insertion
    public void insertNode(NewNodeMsg newNodeMsg) {
        // If there's room for more in our table, insert here
        if (routingTable.size() < charMapping.length) {
            // Insert at this level
            routingTable.add(newNodeMsg.node);

            int newIndex = routingTable.size() - 1;

            // Broadcast the newly added node to all nodes in routingTable
            UpdateRoutingTableMsg msg = new UpdateRoutingTableMsg(newIndex, newNodeMsg.node);
            broadcast(msg);

            // Update newly inserted node's routingTable to match current routingTable
            sendMessage(new SetNewNodeInformationMsg(routingTable, newNodeMsg.currLevel), newNodeMsg.node);
        } else {
            // Else traverse down to next level
            SimpleNode nodeAtCurrPos = routingTable.get(currPos);
            currPos = (currPos + 1) % charMapping.length;

            // Broadcast currPos
            UpdateCurrentPositionMsg updateMsg = new UpdateCurrentPositionMsg(currPos);
            broadcast(updateMsg);

            // Propagate newNode message down to the next level
            sendMessage(new NewSubNodeMsg(newNodeMsg.node), nodeAtCurrPos);
        }
    }

    public void insertSubNode(SimpleNode subNode) {
            if (this.levelBelow == null) {
                this.levelBelow = subNode;

                // Update newly inserted node's routingTable to match current routingTable
                sendMessage(new SetNewNodeInformationMsg(new ArrayList<>(), currLevel + 1), subNode);
            } else {
                // Propagate newNode message down to the next level
                sendMessage(new NewNodeMsg(subNode, currLevel + 1), levelBelow);
            }
    }

    public void updateRoutingTable(int index, SimpleNode node) {
        // Insert new node to routing table
        if (routingTable.size() > index) {
            routingTable.set(index, node);
        } else {
            routingTable.add(node);
        }

        // DEBUGGING:
        if (self.port == 4020) {
            for (var i = 0; i < routingTable.size(); i++) {
                //System.out.println("Table@" + i + ": " + routingTable.get(i).port);
            }
        }
    }

    public void setNodeInformation(SetNewNodeInformationMsg msg) {
        this.routingTable = msg.routingTable;
        this.currLevel = msg.level;
    }

    public void broadcast(Object msg) {
        // Broadcast to own network that a new node has been inserted
        for (SimpleNode n : routingTable) {
            // We don't need to broadcast information to ourselves.
            if (n.port == self.port && n.ip == self.ip) {
                continue;
            }

            sendMessage(msg, n);
        }
    }

    public void sendMessage(Object msg, SimpleNode node) {
        // Broadcast to own network that a new node has been inserted
        try {
            Socket socket = new Socket(node.ip, node.port);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(msg);
            socket.close();
        } catch (IOException e) {
            System.out.println("Failed to connect to node at " + node.ip + " " + node.port);
        }
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
