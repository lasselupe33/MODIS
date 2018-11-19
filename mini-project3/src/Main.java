public class Main {
    public static void main(String[] args) {
        try {
            new Node(30000);

            for (int i = 0; i < 150; i++) {
                Thread.sleep(50);
                new Node(30005 + i, 30000);
            }

            Node failingNode = new Node(40000, 30000);

            System.out.println("Nodes ready");

            Thread.sleep(5000);

            System.out.println("Some node failed!");
            failingNode.dispose();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}