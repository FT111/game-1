package game.menus;

public record KeyInputBind(char character, boolean ctrl, boolean shift, boolean alt) {
     public KeyInputBind(char character) {
         this(character, false, false, false);
     }
}