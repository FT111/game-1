package game.components;

import engine_interfaces.objects.Component;

public class DoorComponent extends Component {
    public char[] closedCharacters;
    public char[] openCharacters;
    public int width;
    public DoorOrientation orientation;
    public boolean collidable;
    public boolean blocksVision;
    public boolean canBeOpened;
    public String requiredItemName;
    public boolean isOpen = false;

    public DoorComponent(char character, int width, DoorOrientation orientation) {
        this(new char[]{character}, width, orientation, true, true);
    }

    public DoorComponent(char[] characters, int width, DoorOrientation orientation, boolean collidable, boolean blocksVision) {
        this(characters, new char[]{' '}, width, orientation, collidable, blocksVision, true, null);
    }

    public DoorComponent(
            char[] closedCharacters,
            char[] openCharacters,
            int width,
            DoorOrientation orientation,
            boolean collidable,
            boolean blocksVision,
            boolean canBeOpened,
            String requiredItemName
    ) {
        if (closedCharacters == null || closedCharacters.length == 0) {
            throw new IllegalArgumentException("Door characters cannot be empty");
        }
        if (width <= 0) {
            throw new IllegalArgumentException("Door width must be greater than 0");
        }
        this.closedCharacters = closedCharacters.clone();
        this.openCharacters = (openCharacters == null || openCharacters.length == 0)
                ? new char[]{' '}
                : openCharacters.clone();
        this.width = width;
        this.orientation = orientation;
        this.collidable = collidable;
        this.blocksVision = blocksVision;
        this.canBeOpened = canBeOpened;
        this.requiredItemName = requiredItemName;
    }
}
