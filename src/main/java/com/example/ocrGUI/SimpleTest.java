package com.example.ocrGUI;


public class SimpleTest {
    /*
    private final static double threshold = 0.003;
    public static class Pair <E, V>{
        private E x;
        private V y;
        public Pair(E x, V y)
        {
            this.x = x;
            this.y = y;
        }
        public E getFirst(){
            return x;
        }
        public void setFirst(E x){ this.x = x; }
        public V getSecond(){
            return y;
        }
        public void setSecond(V y){ this.y = y; }
    }
    public static class CellLocation extends Pair{
        public CellLocation(int rowIndex, int columnIndex){
            super(rowIndex, columnIndex);
        }
    }
    public static class WordGroup{
        private double y;
        private double x;
        private int rowIndex;
        private int colIndex;
        private String content = "";
        private boolean isPrice = false;

        public WordGroup(double x, double y, int rowIndex, int colIndex){
            this.x = x;
            this.y = y;
            this.rowIndex = rowIndex;
            this.colIndex = colIndex;
        }

        public void addContent(String content){
            this.content += content+" ";
        }
        public String getContent(){
            return content;
        }
        public void setTopLeftX(double x){
            this.x = x;
        }
        public double getTopLeftX(){
            return x;
        }
        public void setTopLeftY(){
            this.y = y;
        }
        public void markAsPrice(){
            this.isPrice = true;
        }
        public boolean isPrice(){
            return this.isPrice;
        }
        public double getTopLeftY(){
            return y;
        }
        public void setColIndex(int colIndex){
            this.colIndex = colIndex;
        }
        public int getColIndex(){
            return colIndex;
        }
        public void setRowIndex(int rowIndex){
            this.rowIndex = rowIndex;
        }
        public int getRowIndex(){
            return rowIndex;
        }


    }
    public static void main(String[] args) {
        String document="./src/main/resources/images/page03.jpg";

        ByteBuffer imageBytes;
        try (InputStream inputStream = new FileInputStream(new File(document))) {
            imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
            AmazonTextract client = AmazonTextractClientBuilder.defaultClient();

            AnalyzeDocumentRequest request = new AnalyzeDocumentRequest()
                    .withFeatureTypes("TABLES")
                    .withDocument(new Document().withBytes(imageBytes));
            AnalyzeDocumentResult result = client.analyzeDocument(request);
            List<Block> blocks = result.getBlocks();
            List<Block> cells = new ArrayList<>();
            List<Block> tables = new ArrayList<>();
            List<Block> words = new ArrayList<>();
            List<Block> lines = new ArrayList<>();
            try {
                FileWriter myWriter = new FileWriter("./src/main/resources/filename.txt");
                //myWriter.write(result.toString());
                //myWriter.write("\n");
                myWriter.write("**************************************************");
                if(blocks != null) {
                    for (Block block : blocks) {
                        String blockType = block.getBlockType();
                        if (blockType.equals("CELL"))
                            cells.add(block);
                        //Lines are interpreted as titles of tables
                        else if (blockType.equals("TABLE"))
                            tables.add(block);
                        else if (blockType.equals("WORD"))
                            words.add(block);
                        else if(blockType.equals("LINE"))
                            lines.add(block);

                    }
                }

                List<List<Block>> cellListsOfTables = new ArrayList<List<Block>>();
                List<Pair<CellLocation, List<Block>>> wordListsOfCells = new ArrayList<>();

                for(Block table : tables){
                    List<Block> cellsOfTable = getCellsOfTable(table, cells);
                    cellListsOfTables.add(cellsOfTable);
                    wordListsOfCells = getWordListsOfCells(cells,words);
                }

                List<List<WordGroup>> wordGroupsPerCell = new ArrayList<>();
                for(Pair<CellLocation, List<Block>> wordList : wordListsOfCells){
                    List<WordGroup> wordGroup = extractWordGroups(wordList);
                    wordGroupsPerCell.add(wordGroup);
                }

                List<Block> categoryTitles = getCategoryTitles(lines);
                wordGroupsPerCell = mergeWithCategoryTitles(categoryTitles, wordGroupsPerCell);

                for(List<WordGroup> wordGroups : wordGroupsPerCell){
                    for(WordGroup wordGroup : wordGroups){
                        myWriter.write(wordGroup.getContent());
                        System.out.println(wordGroup.getContent());
                        myWriter.write("\n");
                    }
                }


                for(List<Block> cellWords : wordListsOfCells){
                    for(Block word : cellWords){
                        myWriter.write(word.getText()+" ");
                    }
                    myWriter.write("\n");
                }




                myWriter.close();

            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
    public static List<String> extractIdsOfRelatedBlocks(List<Relationship> relationships){
        List<String> ids = new ArrayList<>();
        for (Relationship relation : relationships) {
            for (String id : relation.getIds()) {
                ids.add(id);
            }
        }
        return ids;
    }
    public static List<Block> getBlocksWithGivenIds(List<String> ids, List<Block> collectionOfBlocks){
        List<Block> blockList = new ArrayList<>();
        for(String id : ids){
            for(Block block : collectionOfBlocks){
                if(block.getId().equals(id)){
                    blockList.add(block);
                }
            }
        }
        return blockList;
    }
    public static List<Block> getCellsOfTable(Block table, List<Block> cells){
        List<Block> cellsOfTable = new ArrayList<>();
        List<Relationship> relationships = table.getRelationships();

        if(relationships != null) {

            List<String> idsOfCurrentTableCells = extractIdsOfRelatedBlocks(relationships);
            cellsOfTable = getBlocksWithGivenIds(idsOfCurrentTableCells, cells);

        }
        return cellsOfTable;
    }
    public static List<Pair<CellLocation, List<Block>>> getWordListsOfCells(List<Block> cells, List<Block> words){
        List<Pair<CellLocation, List<Block>>> wordListsOfCells = new ArrayList<>();
        for (Block cell : cells) {
            List<String> wordBlockIds = new ArrayList<>();
            List<Relationship> cellRelationships = cell.getRelationships();
            if (cellRelationships != null) {

                wordBlockIds = extractIdsOfRelatedBlocks(cellRelationships);
                List<Block> wordBlocksOfCell = getBlocksWithGivenIds(wordBlockIds, words);
                wordListsOfCells.add(new Pair(new CellLocation(cell.getRowIndex(), cell.getColumnIndex()),wordBlocksOfCell));

            }
        }
        return wordListsOfCells;
    }
    //33
    public static List<WordGroup> extractWordGroups(Pair<CellLocation, List<Block>> wordList){

        int columnIndex = (int)wordList.getFirst().getFirst();
        int rowIndex = (int)wordList.getFirst().getSecond();
        List<Block> words = wordList.getSecond();
        List<WordGroup> wordGroups = new ArrayList<>();
        Block currentWord = words.get(0);
        WordGroup group = new WordGroup(currentWord.getGeometry().getPolygon().get(0).getX(), currentWord.getGeometry().getPolygon().get(0).getY(), rowIndex, columnIndex);
        for(Block word : words){
            String ocrText = word.getText();
            ocrText = ocrText.trim();
            System.out.println("**"+ocrText+"**");
            if(ocrText.matches("\\d+(,\\d{1,2})?(.*)?")){
                String price = word.getText().replaceAll("[^,\\d]", "");
                WordGroup priceGroup = new WordGroup(word.getGeometry().getPolygon().get(0).getX(), word.getGeometry().getPolygon().get(0).getY(), rowIndex, columnIndex);
                priceGroup.markAsPrice();
                priceGroup.addContent(price);
                wordGroups.add(priceGroup);
                System.out.println("Price: "+price);
                continue;
            }
            if(calculateDistanceInY(currentWord, word) < threshold){
                group.addContent(word.getText());
            }
            else{
                wordGroups.add(group);
                currentWord = word;
                group = new WordGroup(word.getGeometry().getPolygon().get(0).getX(), word.getGeometry().getPolygon().get(0).getY(), rowIndex, columnIndex);
            }
        }
        wordGroups.add(group);
        return wordGroups;
    }

    public static double calculateDistanceInY(Block block1, Block block2){
        return Math.abs(block1.getGeometry().getPolygon().get(0).getY()-block2.getGeometry().getPolygon().get(0).getY());
    }
    public static double calculateDistance(WordGroup block1, Block block2){
        double differenceInX = Math.abs(block1.getTopLeftX()-block2.getGeometry().getPolygon().get(0).getX());
        double differenceInY = Math.abs(block1.getTopLeftY()-block2.getGeometry().getPolygon().get(0).getY());
        return Math.sqrt(Math.pow(differenceInX,2) + Math.pow(differenceInY, 2));
    }
    public static List<Block> getCategoryTitles(List<Block> lines){
        Classifier classifier = new Classifier();
        List<Block> categories = new ArrayList<>();
        for(Block line : lines){
            if(line.getText().matches("\\d+(,\\d{1,2})?(.*)?"))
                continue;
            if(classifier.classify(line.getText()).equals("category")){
                categories.add(line);
            }
        }
        return categories;
    }
    public static List<LabeledEntry> getLabeledEntries(List<List<WordGroup>> wordGroupsPerCell){
        Classifier classifier = new Classifier();
        List<LabeledEntry> entries = new ArrayList<>();

        for(List<WordGroup> wordGroups : wordGroupsPerCell){
            for(WordGroup wordGroup : wordGroups){
                String entryText = wordGroup.getContent();
                if(!wordGroup.isPrice()){
                    String label = classifier.classify(entryText);
                    LabeledEntry entry = new LabeledEntry(wordGroup.getContent(), label);
                    entries.add(entry);
                }else{
                    LabeledEntry entry = new LabeledEntry(wordGroup.getContent(), "price");
                    entries.add(entry);
                }
            }
        }
        return entries;
    }
    public static List<List<WordGroup>> mergeWithCategoryTitles(List<Block> categories, List<List<WordGroup>> wordGroupsPerCell){
        for(Block category : categories){
            Pair<Integer, Integer> indices = findClosestBlock(category, wordGroupsPerCell);
            WordGroup wordGroup = new WordGroup(category.getGeometry().getPolygon().get(0).getX(), category.getGeometry().getPolygon().get(0).getY(), 0, 0);
            wordGroup.addContent(category.getText());
            wordGroupsPerCell.get(indices.getFirst()).add(indices.getSecond(), wordGroup);
        }
        return wordGroupsPerCell;
    }
    public static Pair<Integer,Integer> findClosestBlock(Block category, List<List<WordGroup>> wordGroupsPerCell){
        double distance = Double.POSITIVE_INFINITY;
        WordGroup theClosest = null;
        Pair<Integer,Integer> indices = new Pair<>(0, 0);
        for(int i = 0; i<wordGroupsPerCell.size(); i++){
            for(int j = 0; j<wordGroupsPerCell.get(i).size(); j++){
                double currentDistance = calculateDistance(wordGroupsPerCell.get(i).get(j), category);
                if(currentDistance < distance){
                    distance = currentDistance;
                    indices.setFirst(i);
                    indices.setSecond(j);
                }
            }
        }
        return indices;
    }
    */
}
