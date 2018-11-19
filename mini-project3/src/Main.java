public class Main {
    public static void main(String[] args) {
        try {
            new Node(15000);

            for (int i = 0; i < 100; i++) {
                Thread.sleep(100);
                new Node(15001 + i, 15000);
            }

            Node failingNode = new Node(40000, 15000);

            new Thread(() -> {
                try {
                    System.out.println("Nodes ready");

                    Thread.sleep(1000);
                    System.out.println("Some node failed!");
                    failingNode.dispose();


                    Thread.sleep(12000);
                    System.out.println("Attempting to insert new node (if no exceptions are thrown, then we assume success.)");
                    new Node(50000, 15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}