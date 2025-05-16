
package mygame;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Character implements ICharacter {
    protected String name;
    protected int hp, maxHp, mp, maxMp, attack;
    protected String imagePath;
    protected Map<String, Skill> skills = new LinkedHashMap<>();

    public Character(String name, int hp, int mp, int attack, String imagePath) {
        this.name = name;
        this.hp = this.maxHp = hp;
        this.mp = this.maxMp = mp;
        this.attack = attack;
        this.imagePath = imagePath;
        initializeSkills();
    }

    @Override public String getName()       { return name; }
    @Override public int getHp()            { return hp; }
    @Override public int getMaxHp()         { return maxHp; }
    @Override public int getMp()            { return mp; }
    @Override public int getMaxMp()         { return maxMp; }
    @Override public int getAttack()        { return attack; }
    @Override public String getImagePath()  { return imagePath; }
    @Override public Map<String, Skill> getSkills() { return skills; }
    @Override public void restoreMp(int a)  { mp = Math.min(maxMp, mp + a); }

    protected abstract void initializeSkills();
    @Override public abstract int useSkill(Skill skill);

    public static class Skill {
        private String name;
        private int mpCost, damageMultiplier;
        private String description, effectType;
        public Skill(String n,int cost,int mult,String desc,String effect) {
            name=n; mpCost=cost; damageMultiplier=mult;
            description=desc; effectType=effect;
        }
        public String getName() { return name; }
        public int getMpCost()  { return mpCost; }
        public int getDamageMultiplier() { return damageMultiplier; }
        public String getDescription()   { return description; }
        public String getEffectType()    { return effectType; }
        public boolean canUse(int curMp)  { return curMp>=mpCost; }
        public String getStatus()        { return name+" ("+mpCost+" MP)"; }
    }
}
