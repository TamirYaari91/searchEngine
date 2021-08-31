import java.io.*;
import java.util.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class searchEngine {

    FileReader fileReader;
    HashMap<String, ArrayList<String>> keywordsToContainingValues;
    HashMap<String, String> idToData;

    public searchEngine(FileReader fileReader) {
        this.fileReader = fileReader;
        this.idToData = populateIdToData();
        this.keywordsToContainingValues = populateKeywordsToContainingValues();

    }

    private static FileReader filenameToFile(String filename) {
        try {
            return new FileReader(filename);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private HashMap<String, String> populateIdToData() {
        HashMap<String, String> idToData = new HashMap<>();
        String thisLine;
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
        try {
            BufferedReader br = new BufferedReader(fileReader);
            while ((thisLine = br.readLine()) != null) {
                try {
                    HashMap<String, String> temp = mapper.readValue(thisLine, typeRef);
                    idToData.putAll(temp);
                } catch (JsonParseException ignored) {
                    br.readLine(); // is this OK?
                }
            }
            br.close();
        } catch (IOException ignored) {}
        return idToData;
    }

    private HashMap<String, ArrayList<String>> populateKeywordsToContainingValues() {
        HashMap<String, ArrayList<String>> keywordsToContainingValues = new HashMap<>();
        for (String key : idToData.keySet()) {
            String[] wordsInWikiPage = idToData.get(key).split("\\s+");
            for (String word : wordsInWikiPage) {
                word = word.replaceAll("[\n.,();?!]", "").toLowerCase();
                if (keywordsToContainingValues.get(word) != null) {
                    keywordsToContainingValues.get(word).add(key);
                } else {
                    ArrayList<String> wikiPageId = new ArrayList<>();
                    wikiPageId.add(key);
                    keywordsToContainingValues.put(word, wikiPageId);
                }
            }
        }
        return keywordsToContainingValues;
    }

    public void search(String input) {
        ArrayList<String> keywords = new ArrayList<>();
        boolean logic = inputParser(input, keywords);
        queryDB(keywords, logic);
    }

    private boolean inputParser(String input, ArrayList<String> keywords) {
        String[] wordsInInput = input.split("\\s+");
        boolean logic = false;
        for (String word : wordsInInput) {
            if (word.equals("AND")) {
                logic = true;
            } else if (!word.equals("OR")) {
                keywords.add(word.toLowerCase());
            }
        }
        return logic;
    }

    private void queryDB(ArrayList<String> keywords, boolean logic) {
        // logic == false -> OR, logic == true -> AND
        boolean resultsExist = false;
        ArrayList<String> possiblePages = new ArrayList<>();
        for (String word : keywords) {
            if (keywordsToContainingValues.get(word) != null) {
                possiblePages.addAll(keywordsToContainingValues.get(word));
            }
        }
        if (logic) {
            HashMap<String, Integer> pagesCounter = new HashMap<>();
            int resultsCounter = 0;
            ArrayList<String> resultsPageIds = new ArrayList<>();
            for (String page : possiblePages) {
                pagesCounter.merge(page, 1, Integer::sum);
            }
            for (String page : pagesCounter.keySet()) {
                if (pagesCounter.get(page) == keywords.size()) {
                    resultsCounter++;
                    resultsPageIds.add(page);
                    resultsExist = true;
                }
            }
            if (resultsExist) {
                System.out.println(resultsCounter + " results found!");
                for (String page : resultsPageIds) {
                    System.out.println("---");
                    System.out.println(idToData.get(page));
                }
            }
        } else {
            TreeSet<String> pagesUniques = new TreeSet<>(possiblePages);
            if (pagesUniques.size() > 0) {
                resultsExist = true;
            }
            if (resultsExist) {
                System.out.println(pagesUniques.size() + " results found!");
                for (String page : pagesUniques) {
                    System.out.println("---");
                    System.out.println(idToData.get(page));
                }
            }
        }
        if (!resultsExist) {
            System.out.println("No results found!");
        }
        System.out.println("***");
    }


    public static void main(String[] args) {
        boolean active = true;
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Enter filename:");
        String filename = keyboard.nextLine();
        FileReader fileReader = filenameToFile(filename);
        while (fileReader == null) {
            System.out.println("File not found! Please try again.");
            System.out.println("Enter filename:");
            filename = keyboard.nextLine();
            fileReader = filenameToFile(filename);
        }
        searchEngine test = new searchEngine(fileReader);
        while (active) {
            System.out.println("Enter search query:");
            String searchText = keyboard.nextLine();
            test.search(searchText);
            boolean waiting = true;
            while (waiting) {
                System.out.println("Another search? Y/N");
                String decision = keyboard.nextLine();
                if (decision.equals("Y")) {
                    waiting = false;
                } else if (decision.equals("N")) {
                    waiting = false;
                    active = false;
                } else {
                    System.out.println("Invalid input!");
                }
            }
        }
        System.out.println("Goodbye then!");
    }
}
