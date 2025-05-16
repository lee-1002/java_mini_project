
package mygame;

import javax.swing.*;

public class GameMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            String[] options = {"Warrior", "Tanker"};
            int choice = JOptionPane.showOptionDialog(
                null,
                "캐릭터를 고르시오.",
                "Character Selection",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
            );
            Character player = (choice == 1) ? new Tanker() : new Warrior();
            new Game(player);
        });
    }
}
