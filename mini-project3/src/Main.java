public class Main {
    public static void main(String[] args) {
        try {
            new Node(15000);

            for (int i = 0; i < 90; i++) {
                Thread.sleep(10);
                new Node(15001 + i, 15000);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
