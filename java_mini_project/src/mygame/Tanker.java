
package mygame;

public class Tanker extends Character {
    private boolean defending=false, taunting=false;

    public Tanker() {
        super("Tanker", 200, 40, 15,
              "https://png.pngtree.com/png-clipart/20190611/original/pngtree-beautifully-textured-roman-warrior-vector-material-png-image_2643728.jpg");
    }
    @Override
    protected void initializeSkills() {
        skills.put("Shield Bash", new Skill("Shield Bash", 2, 3, "2x Damage", "shield"));
        skills.put("Iron Defense", new Skill("Iron Defense", 0, 2, "Block next", "defense"));
        skills.put("Taunt", new Skill("Taunt", 0, 3, "Reduce enemy ATK", "taunt"));
        skills.put("Earthquake", new Skill("Earthquake", 3, 1, "3x AoE", "earthquake"));
    }
    @Override
    public int useSkill(Skill skill) {
        if (mp < skill.getMpCost()) return 0;
        mp -= skill.getMpCost();
        int dmg = attack * skill.getDamageMultiplier();
        switch(skill.getName()) {
            case "Iron Defense": defending = true; break;
            case "Taunt": taunting = true; break;
        }
        return dmg;
    }
    public boolean isDefending()    { return defending; }
    public void resetDefense()      { defending=false; }
    public boolean isTaunting()     { return taunting; }
    public int getReducedDamage(int orig) {
        return taunting ? (int)(orig*0.7) : orig;
    }
}
