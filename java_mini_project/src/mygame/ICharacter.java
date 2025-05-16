
package mygame;

import java.util.Map;

public interface ICharacter {
    String getName();
    int getHp();
    int getMaxHp();
    int getMp();
    int getMaxMp();
    int getAttack();
    String getImagePath();
    Map<String, Character.Skill> getSkills();
    int useSkill(Character.Skill skill);
    void restoreMp(int amount);
}
