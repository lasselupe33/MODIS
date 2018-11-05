public class Main {
    public static void main(String[] args) {
        new Node(4000);

        for (int i = 0; i < 90; i++) {
            new Node(4001 + i, 4000);
        }
    }
}
