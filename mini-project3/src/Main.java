public class Main {
    public static void main(String[] args) {
        try {
            new Node(15000);

            for (int i = 0; i < 50; i++) {
                Thread.sleep(10);
                new Node(15001 + i, 15000);
            }

            System.out.println("Nodes ready");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
