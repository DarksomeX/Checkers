
package game;

import java.io.Serializable;

public class Checker implements Serializable {

        boolean exist;
        boolean isKing;
        boolean Dying;
        int x;
        int y;
        byte i;
        byte j;

        
        
        public Checker(){
            exist = false;
            isKing = false;
            Dying = false;
            x = -1;
            y = -1;
            i = -1;
            j = -1;
        }
        public Checker(int x, int y, byte i, byte j) {
            exist = true;
            isKing = false;
            Dying = false;
            this.x = x;
            this.y = y;
            this.i = i;
            this.j = j;
        }

    }