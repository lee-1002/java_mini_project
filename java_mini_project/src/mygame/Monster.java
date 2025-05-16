package mygame;

import javax.swing.*;
import java.util.Random;

public class Monster {
    public String name;
    public int hp;
    public int maxHp;
    public int attack;
    public String imageUrl;
    public ImageIcon image;
    public String imagePath;

    public Monster(String name, int hp, int attack, String imageUrl) {
        this.name = name;
        this.hp = this.maxHp = hp;
        this.attack = attack;
        this.imageUrl = imageUrl;
        this.image = new ImageIcon(imageUrl);
        this.imagePath = imageUrl;  // 이미지 경로 설정 중요!
    }

    public static Monster getRandomMonster() {
        String[] names = {"Goblin", "Orc", "Skeleton", "Dark Knight"};
        String[] urls = {
            "https://e7.pngegg.com/pngimages/610/439/png-clipart-malifaux-wyrd-goblin-game-through-the-breach-others-miscellaneous-legendary-creature.png",
            "https://e7.pngegg.com/pngimages/756/984/png-clipart-orc-orc.png",
            "https://e7.pngegg.com/pngimages/163/687/png-clipart-dungeons-dragons-pathfinder-roleplaying-game-skeleton-monster-undead-skeleton-legendary-creature-game.png",
            "https://png.pngtree.com/png-clipart/20250208/original/pngtree-dark-fantasy-warrior-in-heavy-armor-with-a-large-sword-png-image_20387175.png"
        };

        int index = new Random().nextInt(names.length);
        int hp = 80 + index * 10;
        int atk = 10 + index * 5;
        return new Monster(names[index], hp, atk, urls[index]);
    }

	public String getHp() {
		return null;
	}

	public String getMaxHp() {
		return null;
	}

	
}
