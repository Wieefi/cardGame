package com.example.cardgame;

import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import javax.print.attribute.standard.Media;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStream;
import java.util.*;


/**
 * This is the CardGameController class
 * It handles game logic for the card game & manages card display and user input
 */
public class CardGameController {
    @FXML
    private ImageView cardImage1;
    @FXML
    private ImageView cardImage2;
    @FXML
    private ImageView cardImage3;
    @FXML
    private ImageView cardImage4;
    @FXML
    private TextField expressionField;
    @FXML
    private VBox Main_vbox;
    @FXML
    private StackPane rootPane;
    @FXML
    private TextField hintField;
    private int[] cardValues = new int[4];
    private Random random = new Random();
    private final String[] suits = {"clubs", "diamonds", "hearts", "spades"};

    private String cachedSolution = null;
    private boolean partialShown = false;
    /**
     * This method initializes the controller
     * It generates random cards for display
     */
    @FXML
    public void initialize(){
        generateRandomCards();
    }

    /**
     * This method generates random cards from 1 to 13
     * It selects a random suit & loads the card image from resources
     */
    private void generateRandomCards(){
        for (int i=0;i<4;i++){
            cardValues[i] = random.nextInt(13)+1;
            String suit = suits[random.nextInt(suits.length)];
            String valueString;
            switch (cardValues[i]){
                case 1:
                    valueString="ace";
                    break;
                case 11:
                    valueString="jack";
                    break;
                case 12:
                    valueString="queen";
                    break;
                case 13:
                    valueString="king";
                    break;
                default:
                    valueString=String.valueOf(cardValues[i]);
                    break;
            }
            String fileName = valueString+"_of_"+suit+".png";
            InputStream is = getClass().getResourceAsStream("/images/png/"+fileName);
            if(is==null){
                System.err.println("Could not load image: "+fileName);
                continue;
            }
            Image image = new Image(is);
            switch(i){
                case 0:
                    cardImage1.setImage(image);
                    break;
                case 1:
                    cardImage2.setImage(image);
                    break;
                case 2:
                    cardImage3.setImage(image);
                    break;
                case 3:
                    cardImage4.setImage(image);
                    break;
            }
        }
        cachedSolution = null;
        partialShown = false;
        hintField.clear();
        expressionField.clear();
    }

    /**
     * This method handles verification of the user input expression
     * It evaluates the expression using a JavaScript engine & checks if the result is 24
     */
    @FXML
    private void handleVerify(){
        String expr=expressionField.getText();
        if(expr==null||expr.isEmpty()){
            showAlert(Alert.AlertType.ERROR,"Error","Please enter an expression :|");
            return;
        }
        ScriptEngineManager mgr=new ScriptEngineManager();
        ScriptEngine engine=mgr.getEngineByName("JavaScript");
        if(engine==null){
            showAlert(Alert.AlertType.ERROR,"Engine Error","JavaScript engine not available. Please add a dependency for a JavaScript engine (e.g. Nashorn or GraalJS).");
            return;
        }
        try{
            Object result=engine.eval(expr);
            double value=Double.parseDouble(result.toString());
            if(Math.abs(value-24)<0.0001){
                playConfettiAnimation();
                showAlert(Alert.AlertType.INFORMATION,"YESS!","Nice job! Your expression evaluates to 24 :D");
            }else{
                showAlert(Alert.AlertType.ERROR,"WRONG!!","Your expression does not evaluate to 24. It evaluates to: "+value+" >:( try again.");
            }
        }catch(ScriptException e){
            showAlert(Alert.AlertType.ERROR,"Error","Invalid expression. Please enter a VALID arithmetic expression >:(");
        }
    }

    /**
     * Plays a confetti animation over the whole scene
     * It creates a confetti pane & adds it to the root stackpane of the scene
     * It then creates 100 confetti circles with random sizes & colors that drop from the top with random trajectories
     * When each confetti finishes its drop it is removed from the confetti pane
     */
    private void playConfettiAnimation(){
        StackPane root = (StackPane) Main_vbox.getScene().getRoot();
        Pane confettiPane = new Pane();
        confettiPane.setPickOnBounds(false);
        root.getChildren().add(confettiPane);
        for(int i=0;i<100;i++){
            Circle confetti = new Circle(5+random.nextDouble()*5);
            confetti.setFill(Color.rgb(random.nextInt(256),random.nextInt(256),random.nextInt(256)));
            confetti.setLayoutX(random.nextDouble()*root.getWidth());
            confetti.setLayoutY(-20);
            confettiPane.getChildren().add(confetti);
            TranslateTransition tt = new TranslateTransition(Duration.seconds(3+random.nextDouble()*2),confetti);
            tt.setByY(root.getHeight()+40);
            tt.setByX(random.nextDouble()*100-50);
            tt.setCycleCount(1);
            tt.setOnFinished(e -> confettiPane.getChildren().remove(confetti));
            tt.play();
        }
        PauseTransition pt = new PauseTransition(Duration.seconds(5));
        pt.setOnFinished(e -> root.getChildren().remove(confettiPane));
        pt.play();
    }


    /**
     * This method handles refreshing of the game
     * It generates new cards & clears the expression and hint fields
     */
    @FXML
    private void handleRefresh(){
        generateRandomCards();
        expressionField.clear();
        hintField.clear();
    }

    /**
     * This method shows an alert box
     * It takes an alert type a title & a message
     */
    private void showAlert(Alert.AlertType type,String title,String message){
        Alert alert=new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * This method attempts to solve the 24 game for the current card values
     * It returns the full solution as a string or a message if no solution is found
     */
    private String solve24(int[] cardValues){
        List<Double> nums=new ArrayList<>();
        List<String> exprs=new ArrayList<>();
        for(int val: cardValues){
            nums.add((double)val);
            exprs.add(String.valueOf(val));
        }
        String solution=solveHelper(nums,exprs);
        return (solution==null ? "Cannot find Solution :(" : solution);
    }

    /**
     * This is the recursive helper method that tries all combinations of operations
     * It returns a valid expression that evaluates to 24 or null if none is found
     */
    private String solveHelper(List<Double> nums,List<String> exprs){
        if(nums.size()==1){
            if(Math.abs(nums.get(0)-24)<1e-6){
                return exprs.get(0);
            }else{
                return null;
            }
        }
        for(int i=0;i<nums.size();i++){
            for(int j=i+1;j<nums.size();j++){
                double a=nums.get(i);
                double b=nums.get(j);
                String exprA=exprs.get(i);
                String exprB=exprs.get(j);
                List<Double> nextNums=new ArrayList<>();
                List<String> nextExprs=new ArrayList<>();
                for(int k=0;k<nums.size();k++){
                    if(k!=i && k!=j){
                        nextNums.add(nums.get(k));
                        nextExprs.add(exprs.get(k));
                    }
                }
                nextNums.add(a+b);
                nextExprs.add("("+exprA+"+"+exprB+")");
                String result=solveHelper(nextNums,nextExprs);
                if(result!=null)return result;
                nextNums.remove(nextNums.size()-1);
                nextExprs.remove(nextExprs.size()-1);
                nextNums.add(a*b);
                nextExprs.add("("+exprA+"*"+exprB+")");
                result=solveHelper(nextNums,nextExprs);
                if(result!=null)return result;
                nextNums.remove(nextNums.size()-1);
                nextExprs.remove(nextExprs.size()-1);
                nextNums.add(a-b);
                nextExprs.add("("+exprA+"-"+exprB+")");
                result=solveHelper(nextNums,nextExprs);
                if(result!=null)return result;
                nextNums.remove(nextNums.size()-1);
                nextExprs.remove(nextExprs.size()-1);
                nextNums.add(b-a);
                nextExprs.add("("+exprB+"-"+exprA+")");
                result=solveHelper(nextNums,nextExprs);
                if(result!=null)return result;
                nextNums.remove(nextNums.size()-1);
                nextExprs.remove(nextExprs.size()-1);
                if(Math.abs(b)>1e-6){
                    nextNums.add(a/b);
                    nextExprs.add("("+exprA+"/"+exprB+")");
                    result=solveHelper(nextNums,nextExprs);
                    if(result!=null)return result;
                    nextNums.remove(nextNums.size()-1);
                    nextExprs.remove(nextExprs.size()-1);
                }
                if(Math.abs(a)>1e-6){
                    nextNums.add(b/a);
                    nextExprs.add("("+exprB+"/"+exprA+")");
                    result=solveHelper(nextNums,nextExprs);
                    if(result!=null)return result;
                    nextNums.remove(nextNums.size()-1);
                    nextExprs.remove(nextExprs.size()-1);
                }
            }
        }
        return null;
    }

    /**
     * This method returns a partial solution string as a hint
     * It reveals only the first half of the solution string
     */
    private String getPartialSolution(String solution){
        if(solution==null||solution.isEmpty()||solution.equals("Cannot find Solution :(")){
            return "No solution available.";
        }
        int half = solution.length()/2;
        return solution.substring(0,half).trim()+" ...";
    }

    /**
     * This method handles the hint button action
     * On the first press it shows a partial solution & on a second press it shows the full solution
     */
    @FXML
    private void handleHintButton(){
        String fullSolution = solve24(cardValues);
        if(fullSolution.startsWith("Cannot")){
            hintField.setText(getHint());
            return;
        }
        if(!partialShown){
            String partial = getPartialSolution(fullSolution);
            hintField.setText(partial);
            partialShown = true;
            cachedSolution = fullSolution;
        } else{
            hintField.setText(cachedSolution);
            partialShown = false;
        }
    }

    /**
     * This method returns a generic hint based on the current card values
     * It suggests a pairing strategy if a solution cannot be found
     */
    private String getHint(){
        int sum = cardValues[0]+cardValues[1]+cardValues[2]+cardValues[3];
        if(sum==24){
            return "Hint: Try simply adding all the cards together.";
        }
        int[] sorted = cardValues.clone();
        Arrays.sort(sorted);
        return "Hint: Consider pairing "+sorted[0]+" (the smallest) & "+sorted[3]+" (the largest) & "+sorted[1]+" & "+sorted[2]+" then combine the results.";
    }
}






