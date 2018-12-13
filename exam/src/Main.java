public class Main {
    public static int nodes = 10;

    public static void main(String[] args) {
        try {
            new Node(15000);

            for (int i = 0; i < nodes - 1; i++) {
                Thread.sleep(200);
                new Node(15001 + i, 15000);
            }
            Thread.sleep(100);
            System.out.println("Network has been succesfully constructed");
            System.out.println();


            System.out.println("Clients begin requesting the system");
            System.out.println();

            System.out.println("Testing GET request on all nodes");

            for (int i = 0; i < nodes; i++) {
                Client.GET(15000 + i);
            }

            System.out.println();
            System.out.println();

            System.out.println("Testing INC followed by GET request on all nodes");
            for (int i = 0; i < nodes; i++) {
                Client.INC(15000 + i);
                Client.GET(15000 + i);
                System.out.println();
            }

            System.out.println();
            System.out.println();

            System.out.println("Testing GET on all nodes");
            for (int i = 0; i < nodes; i++) {
                Client.GET(15000 + i);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
