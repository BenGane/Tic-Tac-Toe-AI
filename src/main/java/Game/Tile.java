package Game;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Tile extends StackPane {

    private Text text;
    private Rectangle tile;
    private Paint colour;

    public Tile(double dimensions) {
        tile = new Rectangle(dimensions, dimensions);
        tile.setArcWidth(5.0);
        tile.setArcHeight(5.0);
        colour = Paint.valueOf("EBEBEB");
        tile.setFill(colour);
        tile.setStroke(Paint.valueOf("black"));
        tile.setStrokeWidth(0.5);
        setAlignment(Pos.CENTER);
        text = new Text();
        text.setFont(Font.font(40));
        getChildren().addAll(tile, text);
    }

    public void draw(String s) {
        text.setText(s);
    }

    public String getText() {
        return text.getText();
    }

    public void resetColour() {
        setFill(colour);
    }

    public void setTextColour(Paint paintValue) {
        text.setFill(paintValue);
    }

    public void setFill(Paint paintValue) {
        tile.setFill(paintValue);
    }

    public void makeTransparent() {
        tile.setOpacity(0.2);
        text.setOpacity(0.2);
    }

}
