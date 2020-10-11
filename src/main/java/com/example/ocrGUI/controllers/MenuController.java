package com.example.ocrGUI.controllers;
import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.auth.AWS3Signer;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.*;
import com.amazonaws.util.IOUtils;
import com.example.ocrGUI.models.Classifier;
import com.example.ocrGUI.models.LabeledEntry;
import com.example.ocrGUI.models.Menu;
import com.example.ocrGUI.util.BoundingBoxDrawer;
import com.example.ocrGUI.util.Pair;
import com.example.ocrGUI.util.CellLocation;
import com.example.ocrGUI.util.WordGroup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.bytedeco.javacpp.*;
import org.bytedeco.leptonica.*;
import org.bytedeco.tesseract.*;
import static org.bytedeco.leptonica.global.lept.*;
import static org.bytedeco.tesseract.global.tesseract.*;

import java.io.*;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuController {

    private static double smallestBoundingBoxHeight = Double.POSITIVE_INFINITY;
    private static double largestBoundingBoxHeight = 0;

    public Menu implementOcrAndClassifyMod2(String restaurantName, ArrayList<String> menuPages) {
        Classifier classifier = new Classifier();
        Menu menu = new Menu(restaurantName);
        List<LabeledEntry> entries = new ArrayList<>();
        try{
            for(String filename : menuPages) {

                List<Block> blocks = runOcrMod2(filename);

                List<Block> cells = new ArrayList<>();
                List<Block> tables = new ArrayList<>();
                List<Block> words = new ArrayList<>();
                List<Block> lines = new ArrayList<>();

                if(blocks != null) {
                    for (Block block : blocks) {
                        String blockType = block.getBlockType();

                        if (blockType.equals("CELL"))
                            cells.add(block);

                        else if (blockType.equals("TABLE"))
                            tables.add(block);

                        else if (blockType.equals("WORD"))
                            words.add(block);

                        else if(blockType.equals("LINE")) {
                            lines.add(block);
                            double boundingBoxHeight = block.getGeometry().getBoundingBox().getHeight();
                            if(boundingBoxHeight > largestBoundingBoxHeight)
                                largestBoundingBoxHeight = boundingBoxHeight;
                            if(boundingBoxHeight < smallestBoundingBoxHeight)
                                smallestBoundingBoxHeight = boundingBoxHeight;
                        }

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
                    List<WordGroup> wordGroups = extractWordGroups(wordList);
                    wordGroupsPerCell.add(wordGroups);
                }

                List<Block> categoryTitles = getCategoryTitles(lines);
                List<String> usedWordBlockIds = getUsedBlockIds(wordListsOfCells, categoryTitles, lines);
                categoryTitles = getMissingCategoryTitles(categoryTitles, lines, usedWordBlockIds);
                wordGroupsPerCell = mergeWithCategoryTitles(categoryTitles, wordGroupsPerCell);

                BoundingBoxDrawer drawer = new BoundingBoxDrawer();
                drawer.drawBoundingBoxes(filename, wordGroupsPerCell);

                entries.addAll(getLabeledEntries(wordGroupsPerCell));


            }
        } catch (Exception e){
            e.printStackTrace();
        }
        menu.createMenuFromLabeledEntries(entries);
        return menu;
    }
    public Menu implementOcrAndClassifyMod1(String restaurantName, ArrayList<String> menuPages) throws Exception{

        Classifier classifier = new Classifier();
        Menu menu = new Menu(restaurantName);
        List<BoundingBox> boundingBoxes = new ArrayList<>();
        List<Block> lines = new ArrayList<>();
        List<LabeledEntry> labeledMenuEntries = new ArrayList<>();
        for(String filename : menuPages){
            List<Block> blocks = runOcrMod1(filename);
            for(Block block : blocks){
                if(block.getBlockType().equals("LINE")){
                    lines.add(block);
                    boundingBoxes.add(block.getGeometry().getBoundingBox());
                }

            }
            BoundingBoxDrawer drawer = new BoundingBoxDrawer();
            drawer.drawBoundingBoxesForMod1(filename, boundingBoxes);
        }

        for(Block line : lines){
            String label = classifier.classify(line.getText());
            labeledMenuEntries.add(new LabeledEntry(line.getText(), label));
        }

        menu.createMenuFromLabeledEntries(labeledMenuEntries);
        return menu;
    }
    public List<Block> runOcrMod1(String filename) throws Exception{
        ByteBuffer imageBytes;
        InputStream inputStream = new FileInputStream(new File("/images/"+filename));
            imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
            AmazonTextract client = AmazonTextractClientBuilder.defaultClient();
            DetectDocumentTextRequest request = new DetectDocumentTextRequest()
                    .withDocument(new Document().withBytes(imageBytes));
            DetectDocumentTextResult result = client.detectDocumentText(request);

            BufferedWriter writer = new BufferedWriter(new FileWriter("resultingjson.txt"));
            writer.append(result.toString());
            writer.close();
            return result.getBlocks();

    }
    public List<Block> runOcrMod2(String filename){
        ByteBuffer imageBytes;
        try (InputStream inputStream = new FileInputStream(new File("/images/"+filename))) {
            imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
            AmazonTextract client = AmazonTextractClientBuilder.defaultClient();
            AnalyzeDocumentRequest request = new AnalyzeDocumentRequest()
                    .withFeatureTypes("TABLES")
                    .withDocument(new Document().withBytes(imageBytes));
            AnalyzeDocumentResult result = client.analyzeDocument(request);

            BufferedWriter writer = new BufferedWriter(new FileWriter("resultingjson.txt"));
            writer.append(result.toString());
            writer.close();
            return result.getBlocks();
        }catch (Exception e){
            return new ArrayList<Block>();
        }
    }

    public static List<String> extractIdsOfRelatedBlocks(List<Relationship> relationships){
        if(relationships == null)
            return new ArrayList<String>();

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
                    break;
                }
            }
        }
        return blockList;
    }

    public static List<Block> getCellsOfTable(Block table, List<Block> cells){
        List<Block> cellsOfTable = new ArrayList<>();
        List<Relationship> relationships = table.getRelationships();
        List<String> idsOfCurrentTableCells = extractIdsOfRelatedBlocks(relationships);
        cellsOfTable = getBlocksWithGivenIds(idsOfCurrentTableCells, cells);
        return cellsOfTable;
    }

    public static List<Pair<CellLocation, List<Block>>> getWordListsOfCells(List<Block> cells, List<Block> words){
        List<Pair<CellLocation, List<Block>>> wordListsOfCells = new ArrayList<>();
        for (Block cell : cells) {
            List<String> wordBlockIds = new ArrayList<>();
            List<Relationship> cellRelationships = cell.getRelationships();
            wordBlockIds = extractIdsOfRelatedBlocks(cellRelationships);
            List<Block> wordBlocksOfCell = getBlocksWithGivenIds(wordBlockIds, words);
            wordListsOfCells.add(new Pair(new CellLocation(cell.getRowIndex(), cell.getColumnIndex()),wordBlocksOfCell));

        }
        return wordListsOfCells;
    }

    public static List<WordGroup> extractWordGroups(Pair<CellLocation, List<Block>> wordList){
        if(wordList.getSecond().size() == 0)
            return new ArrayList<WordGroup>();

        int columnIndex = 0;
        int rowIndex = 0;
        if(wordList.getFirst().getFirst() != null && wordList.getFirst().getSecond() != null){ //deetected but empty cells
            columnIndex = (int)wordList.getFirst().getFirst();
            rowIndex = (int) wordList.getFirst().getSecond();
        }

        List<Block> words = wordList.getSecond();
        List<WordGroup> wordGroups = new ArrayList<>();
        Block currentWord = words.get(0);
        BoundingBox boundingBox = currentWord.getGeometry().getBoundingBox();
        WordGroup group = new WordGroup(boundingBox.getLeft(), boundingBox.getTop(), boundingBox.getLeft()+boundingBox.getWidth(), boundingBox.getHeight(), rowIndex, columnIndex);

        for(Block word : words){

            String ocrText = word.getText();
            ocrText = ocrText.trim();
            BoundingBox boundingBoxOfWord = word.getGeometry().getBoundingBox();

            if(ocrText.toLowerCase().equals("tl"))
                continue;

            if(ocrText.matches("\\d+(,\\d{1,2})?(.*)?")){
                String price = word.getText().replaceAll("[^,\\d]", "");
                WordGroup priceGroup = new WordGroup(boundingBoxOfWord.getLeft(), boundingBoxOfWord.getTop(), boundingBoxOfWord.getLeft()+boundingBoxOfWord.getWidth(), boundingBoxOfWord.getHeight(), rowIndex, columnIndex);
                priceGroup.markAsPrice();
                priceGroup.addContent(price);
                wordGroups.add(priceGroup);
                System.out.println("Price: "+price);
                continue;
            }

            if(calculateDistanceInY(currentWord, word) < smallestBoundingBoxHeight) {
                group.addContent(word.getText());
                group = maintainBoundingBoxInfo(group, word);
            }
            else{
                if(!(group.getContent().matches("\\s+") || group.getContent().equals("") )) {
                    wordGroups.add(group);
                }
                currentWord = word;
                group = new WordGroup(boundingBoxOfWord.getLeft(), boundingBoxOfWord.getTop(), boundingBoxOfWord.getWidth() + boundingBoxOfWord.getLeft(), boundingBoxOfWord.getHeight(), rowIndex, columnIndex);
                group.addContent(currentWord.getText());
            }
        }

        wordGroups.add(group);
        return wordGroups;
    }
    public static WordGroup maintainBoundingBoxInfo(WordGroup group, Block newBlock){
        BoundingBox newGroupBoundingBox = newBlock.getGeometry().getBoundingBox();
        if(group.getLeftCoordinate() > newGroupBoundingBox.getLeft())
            group.setLeftCoordinate(newGroupBoundingBox.getLeft());
        if(group.getTopCoordinate() > newGroupBoundingBox.getTop())
            group.setTopCoordinate(newGroupBoundingBox.getTop());
        if(group.getRightCoordinate() < newGroupBoundingBox.getLeft() + newGroupBoundingBox.getWidth());
            group.setRightCoordinate(newGroupBoundingBox.getLeft() + newGroupBoundingBox.getWidth());
        if(group.getBoundingBoxHeight() < newGroupBoundingBox.getHeight())
            group.setBoundingBoxHeight(newGroupBoundingBox.getHeight());
        return group;
    }
    public static List<String> getUsedBlockIds( List<Pair<CellLocation, List<Block>>> wordListsPerCell, List<Block> categories, List<Block> lines){

        List<String> ids = new ArrayList<>();

        for(Pair<CellLocation, List<Block>> wordListOfCell : wordListsPerCell){
            List<Block> wordList = wordListOfCell.getSecond();
            for(Block word : wordList) {
                ids.add(word.getId());
                for(Block line : lines){
                    List<Relationship> relationships = line.getRelationships();
                    List<String> wordIds = extractIdsOfRelatedBlocks(relationships);
                        if(wordIds.contains(word.getId())){
                            ids.add(line.getId());
                    }

                }
            }
        }
        for(Block category : categories){
            ids.add(category.getId());
        }
        return ids;
    }

    public static double calculateDistanceInY(Block block1, Block block2){
        return Math.abs(block1.getGeometry().getPolygon().get(0).getY()-block2.getGeometry().getPolygon().get(0).getY());
    }

    public static Pair<Boolean, Double>  calculateDistance(WordGroup block1, Block block2){
        double differenceInX = block1.getLeftCoordinate()-block2.getGeometry().getPolygon().get(0).getX();
        double differenceInY = block1.getTopCoordinate()-block2.getGeometry().getPolygon().get(0).getY();
        double distance = Math.sqrt(Math.pow(differenceInX,2) + Math.pow(differenceInY, 2));
        if(differenceInY < 0)
            return new Pair(true, distance);
        else
            return new Pair(false, distance);
    }

    public static List<Block> getCategoryTitles(List<Block> lines){
        Classifier classifier = new Classifier();
        List<Block> categories = new ArrayList<>();
        for(Block line : lines){
            System.out.println("Category: " + line.getText());
            if(line.getText().matches("\\d+(,\\d{1,2})?(.*)?"))
                continue;
            if(classifier.classify(line.getText()).equals("category")){ // EXTRA BİR ŞEYLER LAZIM
                System.out.println("Category title: "+line.getText());
                categories.add(line);
            }
        }
        return categories;
    }

    public static List<Block> getMissingCategoryTitles(List<Block> existingCategories, List<Block> lines, List<String> usedWordBlockIds){
        for(Block line : lines){
            if(line.getText().length() <= 3 || line.getText().matches("\\d+"))
                continue;
            boolean isCategory = false;
            List<Relationship> relationships = line.getRelationships();
            List<String> relatedBlockIds = new ArrayList<>();
            relatedBlockIds = extractIdsOfRelatedBlocks(relationships);
            relatedBlockIds.add(line.getId());
            for(String id : relatedBlockIds){
                if(!usedWordBlockIds.contains(id)){
                    isCategory = true;
                    break;
                }
            }
            if(isCategory)
                existingCategories.add(line);
        }
        return existingCategories;
    }

    public static List<LabeledEntry> getLabeledEntries(List<List<WordGroup>> wordGroupsPerCell){
        Classifier classifier = new Classifier();
        List<LabeledEntry> entries = new ArrayList<>();

        for(List<WordGroup> wordGroups : wordGroupsPerCell){
            for(WordGroup wordGroup : wordGroups){
                String entryText = wordGroup.getContent();
                if(entryText.matches("\\s+") || entryText.equals(""))
                    continue;
                if(wordGroup.isPrice()){
                    LabeledEntry entry = new LabeledEntry(wordGroup.getContent(), "price");
                    entries.add(entry);
                }
                else if(wordGroup.isCategory()){
                    LabeledEntry entry = new LabeledEntry(wordGroup.getContent(), "category");
                    entries.add(entry);
                }
                else{
                    System.out.println("Entry text: " + entryText + "content: " + wordGroup.getContent());
                    String label = classifier.classify(entryText);
                    LabeledEntry entry = new LabeledEntry(wordGroup.getContent(), label);
                    entries.add(entry);
                }
            }
        }
        return entries;
    }

    public static List<List<WordGroup>> mergeWithCategoryTitles(List<Block> categories, List<List<WordGroup>> wordGroupsPerCell){
        for(Block category : categories){
            BoundingBox categoryBoundingBox = category.getGeometry().getBoundingBox();
            Pair<Integer, Integer> indices = findClosestBlock(category, wordGroupsPerCell);
            if(indices.getFirst() == -1){
                List<WordGroup> newGroupList = new ArrayList<>();
                WordGroup group = new WordGroup(categoryBoundingBox.getLeft(), categoryBoundingBox.getTop(), categoryBoundingBox.getLeft()+categoryBoundingBox.getWidth(), categoryBoundingBox.getHeight(), 0, 0);
                newGroupList.add(group);
                wordGroupsPerCell.add(newGroupList);
            }else {
                WordGroup wordGroup = new WordGroup(categoryBoundingBox.getLeft(), categoryBoundingBox.getTop(), categoryBoundingBox.getLeft() + categoryBoundingBox.getWidth(), categoryBoundingBox.getHeight(), 0, 0);
                wordGroup.addContent(category.getText());
                wordGroup.markAsCategory();
                if (indices.getSecond() != -1)
                    wordGroupsPerCell.get(indices.getFirst()).add(indices.getSecond(), wordGroup);
                else
                    wordGroupsPerCell.get(indices.getFirst()).add(wordGroup);
            }
        }
        return wordGroupsPerCell;
    }

    public static Pair<Integer,Integer> findClosestBlock(Block category, List<List<WordGroup>> wordGroupsPerCell){
        double distance = Double.POSITIVE_INFINITY;
        WordGroup theClosest = null;
        Pair<Integer,Integer> indices = new Pair<>(-1, -1);
        for(int i = 0; i<wordGroupsPerCell.size(); i++){
            for(int j = 0; j<wordGroupsPerCell.get(i).size(); j++){
                Pair<Boolean, Double> result = calculateDistance(wordGroupsPerCell.get(i).get(j), category);
                double currentDistance = result.getSecond();
                boolean isLocatedBelow = result.getFirst();
                if(currentDistance < distance){
                    distance = currentDistance;
                    if(isLocatedBelow) {
                        if(j != wordGroupsPerCell.get(i).size()-1){
                            indices.setFirst(i);
                            indices.setSecond(j+1);
                        } else{
                            indices.setFirst(i);
                            indices.setSecond(-1); //it should be added to the end
                        }
                    }else{
                        indices.setFirst(i);
                        indices.setSecond(j);
                    }
                }
            }
        }
        return indices;
    }
}