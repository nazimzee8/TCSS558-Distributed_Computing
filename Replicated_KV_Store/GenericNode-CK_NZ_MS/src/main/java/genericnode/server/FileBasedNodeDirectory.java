package genericnode.server;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileBasedNodeDirectory implements NodeDirectory {
    private static final String FILE_LOCATION = "nodeDirectory.txt";

    public FileBasedNodeDirectory() {
    }

    @Override
    public boolean register(INode node) {
        return true;
    }

    @Override
    public boolean deRegister(INode node) {
        return true;
    }

    @Override
    public List<INode> getNodes() {
        List<INode> nodes = new ArrayList<>();
        try {
            URL url = getClass().getClassLoader().getResource(FILE_LOCATION);
            File file = new File(url.toURI());
            BufferedReader br = new BufferedReader(new FileReader(file));
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine() && br.readLine() != null) {
                String line = scanner.nextLine();
                String[] pair = line.split(":", 2);
                String host = pair[0];
                int port = Integer.parseInt(pair[1]);
                nodes.add(new Node(host, port));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return nodes;
    }
}

