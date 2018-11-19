public class Main {
    public static void main(String[] args) {
        try {
            new Node(15000);

            Thread.sleep(100);
            //Node failingNode = new Node(40000, 15000);

            for (int i = 0; i < 10; i++) {
                Thread.sleep(100);
                new Node(16001 + i, 15000);
            }

            System.out.println("Nodes ready");

            Thread.sleep(5000);

            System.out.println("Some node failed!");
            //failingNode.dispose();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}