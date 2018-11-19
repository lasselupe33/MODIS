public class Main {
    public static void main(String[] args) {
        try {
            new Node(25000);

            for (int i = 0; i < 50; i++) {
                Thread.sleep(100);
                new Node(25001 + i, 25000);
                System.out.println(i);
            }

            Node failingNode = new Node(40000, 25000);

            new Thread(() -> {
                try {
                    System.out.println("Nodes ready");

                    Thread.sleep(1000);
                    System.out.println("Some node failed!");
                    failingNode.dispose();


                    Thread.sleep(12000);
                    System.out.println("Attempting to insert new node");
                    new Node(50000, 25000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}