package ru.myapp.semester_work.client.controller;

import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import javafx.util.Duration;
import ru.myapp.semester_work.client.service.ClientService;
import ru.myapp.semester_work.models.PlayerType;
import ru.myapp.semester_work.models.TeamType;
import ru.myapp.semester_work.models.WordData;

import java.util.List;
import java.util.Random;

public class GameSessionController {

    private final ClientService clientService;
    private final Random random = new Random();
    private static final int GRID_SIZE = 5;
    private StackPane[][] cardContainers = new StackPane[GRID_SIZE][GRID_SIZE];

    public GameSessionController(ClientService clientService) {
        this.clientService = clientService;
    }

    @FXML
    private Label redCommanderLabel;

    @FXML
    private Label redMemberLabel;

    @FXML
    private Label blueCommanderLabel;

    @FXML
    private Label blueMemberLabel;

    @FXML
    private Label redCounterLabel;

    @FXML
    private Label blueCounterLabel;

    @FXML
    private GridPane wordsGridPane;

    @FXML
    private Label redEndTurnLabel;

    @FXML
    private Label blueEndTurnLabel;

    @FXML
    private TextField redTextField;

    @FXML
    private TextField blueTextField;

    @FXML
    private ListView<String> redListView;

    @FXML
    private ListView<String> blueListView;

    private ObservableList<String> redMessages;
    private ObservableList<String> blueMessages;

    @FXML
    public void initialize() {
        redMessages = FXCollections.observableArrayList();
        blueMessages = FXCollections.observableArrayList();

        redListView.setItems(redMessages);
        blueListView.setItems(blueMessages);

        setupListView(redListView, "#42110a", Color.rgb(204, 204, 204));
        setupListView(blueListView, "#2b3c47", Color.rgb(204, 204, 204));

        redTextField.setOnAction(event -> {
            String text = redTextField.getText().trim();
            if (!text.isEmpty()) {
                onRedTextFieldEntered(text);
                redTextField.clear();
            }
        });

        blueTextField.setOnAction(event -> {
            String text = blueTextField.getText().trim();
            if (!text.isEmpty()) {
                onBlueTextFieldEntered(text);
                blueTextField.clear();
            }
        });
    }

    private void setupListView(ListView<String> listView, String backgroundColor, Color textColor) {
        listView.setStyle("-fx-background-color: " + backgroundColor + "; " +
                "-fx-control-inner-background: " + backgroundColor + "; " +
                "-fx-background-insets: 0; " +
                "-fx-padding: 0;" +
                "-fx-hbar-policy: never;");

        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                            setStyle("-fx-background-color: " + backgroundColor + ";");
                        } else {
                            setText(item);
                            setTextFill(textColor);
                            setFont(Font.font("System", 10));

                            setWrapText(true);

                            setMaxWidth(listView.getWidth() - 10);
                            setPrefWidth(listView.getWidth() - 10);

                            setStyle("-fx-background-color: " + backgroundColor + ";" +
                                    "-fx-padding: 2px 5px;" +
                                    "-fx-border-color: transparent;");
                        }
                    }
                };
            }
        });

        listView.setSelectionModel(null);
    }

    private void onRedTextFieldEntered(String message) {
        clientService.sendAddMessage(message, TeamType.RED);
        clientService.sendEndTurn(TeamType.RED, PlayerType.COMMANDER);
    }

    private void onBlueTextFieldEntered(String message) {
        clientService.sendAddMessage(message, TeamType.BLUE);
        clientService.sendEndTurn(TeamType.BLUE, PlayerType.COMMANDER);
    }

    public void addMessageToRedListView(String message) {
        redMessages.add(message);
        redListView.scrollTo(redMessages.size() - 1);
    }

    public void addMessageToBlueListView(String message) {
        blueMessages.add(message);
        blueListView.scrollTo(blueMessages.size() - 1);
    }

    @FXML
    private void onRedCommanderLabelClick() {
        clientService.joinTeam(TeamType.RED, PlayerType.COMMANDER);
    }

    @FXML
    private void onRedMemberLabelClick() {
        clientService.joinTeam(TeamType.RED, PlayerType.MEMBER);
    }

    @FXML
    private void onBlueCommanderLabelClick() {
        clientService.joinTeam(TeamType.BLUE, PlayerType.COMMANDER);
    }

    @FXML
    private void onBlueMemberLabelClick() {
        clientService.joinTeam(TeamType.BLUE, PlayerType.MEMBER);
    }

    private Label getLabelByTypes(TeamType teamType, PlayerType playerType) {
        return switch (teamType) {
            case RED -> switch (playerType) {
                case COMMANDER -> redCommanderLabel;
                case MEMBER -> redMemberLabel;
                default -> null;
            };
            case BLUE -> switch (playerType) {
                case COMMANDER -> blueCommanderLabel;
                case MEMBER -> blueMemberLabel;
                default -> null;
            };
            default -> null;
        };
    }

    public void setPlayerNameToLabel(String clientName, TeamType teamType, PlayerType playerType) {
        Label targetLabel = getLabelByTypes(teamType, playerType);

        targetLabel.setText(clientName);
        targetLabel.setMouseTransparent(true);
    }

    public void clearOldLabel(TeamType teamType, PlayerType playerType) {
        Label targetLabel = getLabelByTypes(teamType, playerType);

        String labelText = playerType.getDisplayName();
        targetLabel.setText(labelText);
        targetLabel.setMouseTransparent(false);
    }

    @FXML
    public void onStartButtonClick() {
        clientService.startGameSession();
    }

    public void setCounters(int redCount, int blueCount) {
        redCounterLabel.setText(Integer.toString(redCount));
        blueCounterLabel.setText(Integer.toString(blueCount));
    }

    public void blockTurn() {
        wordsGridPane.setMouseTransparent(true);
        redEndTurnLabel.setVisible(false);
        blueEndTurnLabel.setVisible(false);
        redTextField.setVisible(false);
        blueTextField.setVisible(false);
    }

    public void doTurn(TeamType teamType, PlayerType playerType) {
        switch (teamType) {
            case RED:
                switch (playerType) {
                    case COMMANDER:
                        doRedCommanderTurn();
                        return;
                    case MEMBER:
                        doRedMemberTurn();
                        return;
                    default:
                        return;
            }
            case BLUE:
                switch (playerType) {
                    case COMMANDER:
                        doBlueCommanderTurn();
                        return;
                    case MEMBER:
                        doBlueMemberTurn();
                        return;
                    default:
                        return;
            }
            default:
        }
    }

    public void doRedCommanderTurn() {
        redTextField.setVisible(true);
    }

    public void doRedMemberTurn() {
        wordsGridPane.setMouseTransparent(false);
        redEndTurnLabel.setVisible(true);
    }

    public void doBlueCommanderTurn() {
        blueTextField.setVisible(true);
    }

    public void doBlueMemberTurn() {
        wordsGridPane.setMouseTransparent(false);
        blueEndTurnLabel.setVisible(true);
    }

    public void onRedEndTurnLabelClick() {
        clientService.sendEndTurn(TeamType.RED, PlayerType.MEMBER);
    }

    public void onBlueEndTurnLabelClick() {
        clientService.sendEndTurn(TeamType.BLUE, PlayerType.MEMBER);
    }

    public void generateWordCards(List<WordData> words, PlayerType playerType) {
        if (words == null || words.size() != GRID_SIZE * GRID_SIZE) {
            throw new IllegalArgumentException("Должно быть ровно 25 слов для сетки 5x5");
        }

        wordsGridPane.getChildren().clear();

        double gridWidth = wordsGridPane.getPrefWidth();
        double gridHeight = wordsGridPane.getPrefHeight();
        double padding = 2;
        double hgap = 1;
        double vgap = 1;

        double availableWidth = gridWidth - (2 * padding) - (4 * hgap);
        double availableHeight = gridHeight - (2 * padding) - (4 * vgap);

        double cardWidth = availableWidth / GRID_SIZE;
        double cardHeight = availableHeight / GRID_SIZE;

        cardWidth -= 5;
        cardHeight -= 5;

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                WordData wordData = words.get(i * GRID_SIZE + j);
                StackPane card = createWordCard(wordData, cardWidth, cardHeight, playerType);
                cardContainers[i][j] = card;

                GridPane.setHalignment(card, javafx.geometry.HPos.CENTER);
                GridPane.setValignment(card, javafx.geometry.VPos.CENTER);

                wordsGridPane.add(card, j, i);
            }
        }

        animateCardsAppearance();
    }

    private StackPane createWordCard(WordData wordData, double width, double height, PlayerType playerType) {
        Rectangle background = new Rectangle(width, height);
        background.setArcWidth(12);
        background.setArcHeight(12);
        background.setStroke(Color.BLACK);
        background.setStrokeWidth(1.5);
        if (playerType == PlayerType.COMMANDER) {
            switch (wordData.getTeamType()) {
                case TeamType.RED ->  background.setFill(Color.rgb(255, 100, 100));
                case TeamType.BLUE -> background.setFill(Color.rgb(100, 100, 255));
                case TeamType.NONE -> background.setFill(Color.WHITE);
            }
        } else {
            background.setFill(Color.rgb(152, 123, 92));
        }

        Text wordText = new Text(wordData.getWord().toUpperCase());
        wordText.setFont(Font.font("Arial", calculateFontSize(wordData.getWord(), width)));
        wordText.setTextAlignment(TextAlignment.CENTER);
        wordText.setWrappingWidth(width - 10);

        StackPane.setAlignment(wordText, Pos.CENTER);

        StackPane card = new StackPane();
        card.getChildren().addAll(background, wordText);
        card.setPrefSize(width, height);
        card.setMaxSize(width, height);
        card.setMinSize(width, height);

        card.setId("card_" + wordData.getPositionX() + "_" + wordData.getPositionY());

        card.setOnMouseClicked(event -> onCardClick(wordData));

        card.setOpacity(0);
        card.setScaleX(0.3);
        card.setScaleY(0.3);
        card.setRotate(random.nextInt(20) - 10);

        return card;
    }

    private void onCardClick(WordData wordData) {
        clientService.sendCardClick(wordData);
    }

    public void handleCardClicked(TeamType wordTeamType, PlayerType playerType, int positionX, int positionY) {
        StackPane card = cardContainers[positionY][positionX];

        ScaleTransition clickScale = new ScaleTransition(Duration.millis(150), card);
        clickScale.setToX(0.95);
        clickScale.setToY(0.95);
        clickScale.setAutoReverse(true);
        clickScale.setCycleCount(2);
        clickScale.play();

        applyCardColor(card, wordTeamType, playerType);
    }

    private void applyCardColor(StackPane card, TeamType teamType, PlayerType playerType) {
        Rectangle background = (Rectangle) card.getChildren().getFirst();

        Color color;
        if (playerType == PlayerType.MEMBER) {
            color = switch (teamType) {
                case RED -> Color.rgb(255, 100, 100);
                case BLUE -> Color.rgb(100, 100, 255);
                case NONE -> Color.WHITE;
            };
        } else {
            color = Color.rgb(111,111,111);
        }

        FillTransition colorTransition = new FillTransition(Duration.seconds(0.5), background);
        colorTransition.setToValue(color);
        colorTransition.play();
    }

    private double calculateFontSize(String word, double cardWidth) {
        int baseSize = 14;
        if (cardWidth < 80) baseSize = 10;
        else if (cardWidth < 100) baseSize = 12;

        if (word.length() > 10) return baseSize - 2;
        if (word.length() > 8) return baseSize - 1;
        return baseSize;
    }

    private void animateCardsAppearance() {
        // Анимация появляется волной от центра
        int centerX = GRID_SIZE / 2;
        int centerY = GRID_SIZE / 2;

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                int distanceFromCenter = Math.abs(i - centerX) + Math.abs(j - centerY);
                double delay = distanceFromCenter * 0.08;

                StackPane card = cardContainers[i][j];

                // Параллельная анимация
                ParallelTransition parallelTransition = new ParallelTransition();

                // Анимация появления
                FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.4), card);
                fadeTransition.setFromValue(0);
                fadeTransition.setToValue(1);

                // Анимация масштабирования
                ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.5), card);
                scaleTransition.setFromX(0.3);
                scaleTransition.setFromY(0.3);
                scaleTransition.setToX(1);
                scaleTransition.setToY(1);

                // Анимация вращения
                RotateTransition rotateTransition = new RotateTransition(Duration.seconds(0.4), card);
                rotateTransition.setFromAngle(card.getRotate());
                rotateTransition.setToAngle(0);
                rotateTransition.setInterpolator(Interpolator.EASE_OUT);

                // Анимация "пружины"
                TranslateTransition bounceTransition = new TranslateTransition(Duration.seconds(0.5), card);
                bounceTransition.setFromY(10);
                bounceTransition.setToY(0);
                bounceTransition.setInterpolator(Interpolator.SPLINE(0.25, 0.1, 0.25, 1));

                parallelTransition.getChildren().addAll(
                        fadeTransition,
                        scaleTransition,
                        rotateTransition,
                        bounceTransition
                );

                // Устанавливаем задержку
                PauseTransition pause = new PauseTransition(Duration.seconds(delay));
                pause.setOnFinished(event -> parallelTransition.play());

                pause.play();
            }
        }

        // После завершения всех анимаций добавляем легкую пульсацию
        PauseTransition finalPause = new PauseTransition(Duration.seconds(1.5));
        finalPause.setOnFinished(event -> addCardHoverEffects());
        finalPause.play();
    }

    private void addCardHoverEffects() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                StackPane card = cardContainers[i][j];

                // Эффект при наведении
                card.setOnMouseEntered(event -> {
                    ScaleTransition hoverScale = new ScaleTransition(Duration.millis(150), card);
                    hoverScale.setToX(1.03);
                    hoverScale.setToY(1.03);
                    hoverScale.play();

                    // Легкое поднятие
                    TranslateTransition hoverTranslate = new TranslateTransition(Duration.millis(150), card);
                    hoverTranslate.setToY(-3);
                    hoverTranslate.play();
                });

                card.setOnMouseExited(event -> {
                    ScaleTransition hoverScale = new ScaleTransition(Duration.millis(150), card);
                    hoverScale.setToX(1.0);
                    hoverScale.setToY(1.0);
                    hoverScale.play();

                    TranslateTransition hoverTranslate = new TranslateTransition(Duration.millis(150), card);
                    hoverTranslate.setToY(0);
                    hoverTranslate.play();
                });
            }
        }
    }
}
