class Sink {
    public static int EventPort = 2000;

    public static void main(String[] args) {

        // Ensure unsubscribe is run once the process is termminated
        Runtime.getRuntime().addShutdownHook(unsubscribe());
    }
    // Subscribe on creation

    // All messages should now be printed to Sys.out


    // Unsubscribe on destroy
    public static Thread unsubscribe() {
        return new Thread(() -> {
            System.out.println("bye");

        });
    }
}