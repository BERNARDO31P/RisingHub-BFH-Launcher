package fm.bernardo.risinghub.classes;

public final class ExecuteBashCommand {

    public static void executeCommand(final String command) {
        final String[] commands = {"bash", "-c", command};
        new Thread(() -> {
            try {
                final Process p = Runtime.getRuntime().exec(commands);
                p.waitFor();
            } catch (Exception ignore) {
            }
        }).start();
    }
}